package cl.duoc.cmqsalud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cmqsalud.data.DocumentoRepository
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.DocumentoFormErrors
import cl.duoc.cmqsalud.model.DocumentoFormUiState
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento
import cl.duoc.cmqsalud.model.ReglasRevision
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentoViewModel(private val repository: DocumentoRepository) : ViewModel() {
    private val _formState = MutableStateFlow(DocumentoFormUiState())
    val formState: StateFlow<DocumentoFormUiState> = _formState.asStateFlow()

    val documentos: StateFlow<List<Documento>> = repository.observarTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onNombreChange(valor: String) = _formState.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null), guardadoExitoso = false)
    }

    fun onDescripcionChange(valor: String) = _formState.update {
        it.copy(descripcion = valor.take(500), guardadoExitoso = false)
    }

    fun onFuncionarioChange(valor: String) = _formState.update {
        it.copy(funcionario = valor, errores = it.errores.copy(funcionario = null), guardadoExitoso = false)
    }

    fun onTipoChange(valor: String) = _formState.update {
        it.copy(tipo = valor, errores = it.errores.copy(tipo = null), guardadoExitoso = false)
    }

    fun onAdjuntoSeleccionado(uri: String?, nombre: String?) = _formState.update {
        it.copy(adjuntoUri = uri, adjuntoNombre = nombre, guardadoExitoso = false)
    }

    fun prepararNuevoRegistro() {
        _formState.value = DocumentoFormUiState()
    }

    fun prepararEdicion(documento: Documento) {
        _formState.value = DocumentoFormUiState(
            editarId = documento.id,
            fechaOriginal = documento.fechaCarga,
            nombre = documento.nombre,
            descripcion = documento.descripcion,
            funcionario = documento.funcionario,
            tipo = documento.tipo,
            estado = documento.estado,
            adjuntoUri = documento.adjuntoUri,
            adjuntoNombre = documento.adjuntoNombre
        )
    }

    fun guardarDocumento() {
        val actual = _formState.value
        if (actual.guardando || actual.guardadoExitoso) return
        val errores = validar(actual)
        _formState.update { it.copy(errores = errores, guardadoExitoso = false, errorGuardado = null) }
        if (errores.hayErrores()) return

        _formState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val documento = Documento(
                    id = actual.editarId ?: 0,
                    nombre = actual.nombre.trim(),
                    descripcion = actual.descripcion.trim(),
                    tipo = actual.tipo,
                    funcionario = actual.funcionario.trim(),
                    estado = actual.estado,
                    adjuntoUri = actual.adjuntoUri,
                    adjuntoNombre = actual.adjuntoNombre,
                    fechaCarga = actual.fechaOriginal ?: System.currentTimeMillis()
                )
                val idGuardado = if (actual.editarId == null) {
                    repository.guardar(documento).toInt()
                } else {
                    if (!repository.actualizar(documento)) error("El documento ya no existe")
                    actual.editarId
                }
                _formState.update {
                    it.copy(
                        editarId = idGuardado,
                        fechaOriginal = documento.fechaCarga,
                        guardando = false,
                        guardadoExitoso = true
                    )
                }
            } catch (_: Exception) {
                _formState.update {
                    it.copy(guardando = false, errorGuardado = "No se pudieron guardar los datos. Intenta nuevamente.")
                }
            }
        }
    }

    fun revisarDocumento(id: Int, nuevoEstado: String, observacion: String, onResultado: (String?) -> Unit) {
        val error = ReglasRevision.errorRevision(nuevoEstado, observacion)
        if (error != null) {
            onResultado(error)
            return
        }
        viewModelScope.launch {
            val resultado = runCatching { repository.revisar(id, nuevoEstado, observacion) }
            onResultado(
                resultado.fold(
                    onSuccess = { if (it) null else "No se pudo revisar el documento." },
                    onFailure = { it.message ?: "No fue posible realizar la revisión." }
                )
            )
        }
    }

    fun documentoPorId(id: Int) = repository.observarPorId(id)
    fun historialPorDocumento(id: Int) = repository.observarHistorial(id)
    val historialGlobal = repository.observarAuditoriaGlobal().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val papelera = repository.observarPapelera().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun restaurarDocumento(id: Int, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val correcto = runCatching { repository.restaurar(id) }.getOrDefault(false)
            onResultado(correcto)
        }
    }

    /** Borrado físico separado de la eliminación lógica, para mostrar CRUD en la defensa. */
    fun borrarDefinitivamente(id: Int, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val correcto = runCatching { repository.borrarDefinitivamente(id) }.getOrDefault(false)
            onResultado(correcto)
        }
    }

    fun eliminarDocumento(documento: Documento, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val resultado = runCatching { repository.eliminar(documento) }.isSuccess
            onResultado(resultado)
        }
    }

    private fun validar(state: DocumentoFormUiState) = DocumentoFormErrors(
        nombre = when {
            state.nombre.isBlank() -> "El nombre del documento es obligatorio"
            state.nombre.trim().length < 3 -> "Ingrese al menos 3 caracteres"
            else -> null
        },
        funcionario = if (state.funcionario.isBlank()) "Debe indicar el funcionario asociado" else null,
        tipo = if (state.tipo.isBlank()) "Debe seleccionar un tipo de documento" else null
    )
}
