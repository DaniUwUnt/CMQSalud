package cl.duoc.cmqsalud.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.cmqsalud.data.AuthRepository
import cl.duoc.cmqsalud.data.DatosRegistroUsuario
import cl.duoc.cmqsalud.model.ValidacionRut
import cl.duoc.cmqsalud.model.RegionesComunasChile
import cl.duoc.cmqsalud.data.CuentaAdministrable
import cl.duoc.cmqsalud.data.EventoAdmin
import cl.duoc.cmqsalud.data.RolDemo
import cl.duoc.cmqsalud.data.UsuarioDemo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val usuario: UsuarioDemo? = null,
    val procesando: Boolean = false,
    val nombreError: String? = null,
    val correoError: String? = null,
    val claveError: String? = null,
    val confirmarError: String? = null,
    val mensaje: String? = null,
    val erroresRegistro: Map<String, String> = emptyMap()
)

data class AdministracionUiState(
    val cuentas: List<CuentaAdministrable> = emptyList(),
    val auditoria: List<EventoAdmin> = emptyList(),
    val procesando: Boolean = false,
    val mensaje: String? = null
)

enum class AccionAdmin { ELIMINAR, DESHABILITAR, REACTIVAR, HACER_ADMIN, HACER_FUNCIONARIO }

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _estado = MutableStateFlow(AuthUiState(usuario = repository.sesionActual()))
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    private val _administracion = MutableStateFlow(AdministracionUiState())
    val administracion: StateFlow<AdministracionUiState> = _administracion.asStateFlow()

    fun actualizarCuentasAdmin() {
        val admin = _estado.value.usuario
        if (admin?.rol != RolDemo.ADMINISTRADOR) {
            _administracion.value = AdministracionUiState(mensaje = "Acceso denegado.")
            return
        }
        _administracion.update {
            it.copy(
                cuentas = repository.listarCuentas(admin.correo),
                auditoria = repository.listarAuditoria(admin.correo)
            )
        }
    }

    fun ejecutarAccionAdmin(correo: String, accion: AccionAdmin) {
        if (_administracion.value.procesando) return
        val administrador = _estado.value.usuario
        if (administrador?.rol != RolDemo.ADMINISTRADOR) {
            _administracion.update { it.copy(mensaje = "Acceso denegado.") }
            return
        }
        _administracion.update { it.copy(procesando = true, mensaje = null) }
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                when (accion) {
                    AccionAdmin.ELIMINAR -> repository.eliminarCuenta(administrador.correo, correo)
                    AccionAdmin.DESHABILITAR -> repository.cambiarEstado(administrador.correo, correo, false)
                    AccionAdmin.REACTIVAR -> repository.cambiarEstado(administrador.correo, correo, true)
                    AccionAdmin.HACER_ADMIN -> repository.cambiarRol(administrador.correo, correo, RolDemo.ADMINISTRADOR)
                    AccionAdmin.HACER_FUNCIONARIO -> repository.cambiarRol(administrador.correo, correo, RolDemo.FUNCIONARIO)
                }
            }
            _administracion.update {
                it.copy(
                    procesando = false,
                    mensaje = error ?: "Cambio realizado correctamente.",
                    cuentas = repository.listarCuentas(administrador.correo),
                    auditoria = repository.listarAuditoria(administrador.correo)
                )
            }
        }
    }

    fun limpiarErrores() {
        _estado.update {
            it.copy(nombreError = null, correoError = null, claveError = null,
                confirmarError = null, mensaje = null, erroresRegistro = emptyMap())
        }
    }

    fun iniciarSesion(correo: String, clave: String) {
        if (_estado.value.procesando) return
        val correoErr = if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches())
            "Ingresa un correo electrónico válido." else null
        val claveErr = if (clave.isBlank()) "La contraseña es obligatoria." else null
        _estado.update {
            it.copy(correoError = correoErr, claveError = claveErr, mensaje = null)
        }
        if (correoErr != null || claveErr != null) return

        _estado.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val error = withContext(Dispatchers.Default) { repository.iniciarSesion(correo, clave) }
            _estado.update {
                it.copy(procesando = false, usuario = if (error == null) repository.sesionActual() else null,
                    mensaje = error)
            }
        }
    }

    fun registrar(datos: DatosRegistroUsuario, clave: String, confirmacion: String) {
        if (_estado.value.procesando) return
        val errores = mutableMapOf<String, String>()
        if (datos.nombres.trim().length < 2) errores["nombres"] = "Ingresa tu nombre o nombres."
        if (datos.apellidoPaterno.isBlank()) errores["apellidoPaterno"] = "El apellido paterno es obligatorio."
        if (datos.apellidoMaterno.isBlank()) errores["apellidoMaterno"] = "El apellido materno es obligatorio."
        if (!ValidacionRut.valido(datos.rut)) errores["rut"] = "El RUT y su dígito verificador no coinciden."
        if (!Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches())
            errores["correo"] = "Ingresa un correo electrónico válido."
        if (!Regex("\\+569[0-9]{8}").matches(datos.celular))
            errores["celular"] = "Ingresa los 8 dígitos después de +569."
        if (datos.fijo.isNotBlank() && !Regex("\\+56[0-9]{8,10}").matches(datos.fijo))
            errores["fijo"] = "Usa formato +56 y entre 8 y 10 dígitos."
        if (!RegionesComunasChile.regionValida(datos.region))
            errores["region"] = "Selecciona una región."
        if (!RegionesComunasChile.comunaValida(datos.region, datos.comuna))
            errores["comuna"] = "Selecciona una comuna de la región indicada."
        if (clave.length < 8 || !clave.any(Char::isLetter) || !clave.any(Char::isDigit))
            errores["clave"] = "Mínimo 8 caracteres con letras y números."
        if (clave != confirmacion || confirmacion.isBlank())
            errores["confirmacion"] = "Las contraseñas no coinciden."
        _estado.update { it.copy(erroresRegistro = errores, mensaje = null) }
        if (errores.isNotEmpty()) return

        _estado.update { it.copy(procesando = true) }
        viewModelScope.launch {
            val error = withContext(Dispatchers.Default) { repository.registrar(datos, clave) }
            val erroresTelefono = when (error) {
                AuthRepository.ERROR_CELULAR_DUPLICADO -> mapOf("celular" to error)
                AuthRepository.ERROR_FIJO_DUPLICADO -> mapOf("fijo" to error)
                else -> emptyMap()
            }
            _estado.update {
                it.copy(procesando = false, usuario = if (error == null) repository.sesionActual() else null,
                    mensaje = error, erroresRegistro = erroresTelefono)
            }
        }
    }

    fun perfilActual() = repository.perfilActual()

    fun actualizarUbicacion(region: String, comuna: String, onResultado: (String?) -> Unit) {
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) { repository.actualizarUbicacion(region, comuna) }
            onResultado(error)
        }
    }


    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = AuthUiState()
        _administracion.value = AdministracionUiState()
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java))
        return AuthViewModel(repository) as T
    }
}
