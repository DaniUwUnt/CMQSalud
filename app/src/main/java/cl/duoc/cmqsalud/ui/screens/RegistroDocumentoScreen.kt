package cl.duoc.cmqsalud.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.data.AdjuntoUtils
import cl.duoc.cmqsalud.model.DocumentoFormUiState
import cl.duoc.cmqsalud.ui.components.AppHeader
import cl.duoc.cmqsalud.ui.components.BotonPrincipal
import cl.duoc.cmqsalud.ui.components.CampoFormulario
import cl.duoc.cmqsalud.ui.components.TipoDocumentoSelector
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/** La misma pantalla sirve para RF03 registrar y para editar un documento existente. */
@Composable
fun RegistroDocumentoScreen(
    viewModel: DocumentoViewModel,
    onVolver: () -> Unit,
    documentoId: Int? = null
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    if (documentoId != null) {
        val documento by remember(documentoId) { viewModel.documentoPorId(documentoId) }
            .collectAsStateWithLifecycle(initialValue = null)
        LaunchedEffect(documentoId, documento?.id) {
            documento?.let(viewModel::prepararEdicion)
        }
        if (documento == null || state.editarId != documentoId) {
            Scaffold(topBar = { AppHeader(titulo = "Editar documento") }) { padding ->
                Text("Cargando datos del documento...", Modifier.padding(padding).padding(20.dp))
            }
        } else {
            FormularioDocumento(state, viewModel, onVolver, edicion = true)
        }
    } else {
        FormularioDocumento(state, viewModel, onVolver, edicion = false)
    }
}

@Composable
private fun FormularioDocumento(
    state: DocumentoFormUiState,
    viewModel: DocumentoViewModel,
    onVolver: () -> Unit,
    edicion: Boolean
) {
    val context = LocalContext.current
    // La confirmación se muestra centrada, independientemente del scroll del formulario.
    // La navegación ocurre solo después de que Room confirmó el guardado.
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            delay(1900L)
            onVolver()
        }
    }
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var errorAdjunto by remember { mutableStateOf<String?>(null) }

    // OpenDocument permite conservar permisos de lectura del PDF/imagen después de reiniciar.
    val selectorArchivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // El proveedor no siempre permite persistir el permiso; se intenta usar la URI.
            }
            viewModel.onAdjuntoSeleccionado(uri.toString(), AdjuntoUtils.nombre(context, uri))
            errorAdjunto = null
        }
    }

    // TakePicture escribe la foto completa en la URI segura entregada mediante FileProvider.
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        rutaPendiente = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (exito && archivo.exists() && archivo.length() > 0L) {
                viewModel.onAdjuntoSeleccionado(Uri.fromFile(archivo).toString(), archivo.name)
                errorAdjunto = null
            } else {
                archivo.delete()
                if (exito) errorAdjunto = "La cámara no devolvió una fotografía válida."
            }
        }
    }

    val miniatura by produceState<android.graphics.Bitmap?>(null, state.adjuntoUri) {
        val uri = state.adjuntoUri
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            AdjuntoUtils.miniatura(context, uri)
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                titulo = if (edicion) "Editar documento" else "Registrar documento",
                subtitulo = if (edicion) "Corregir datos sin perder el registro" else "RF03 · Gestión documental"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Datos del documento",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            CampoFormulario(
                valor = state.nombre,
                etiqueta = "Nombre del documento",
                error = state.errores.nombre,
                onChange = viewModel::onNombreChange
            )
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción (opcional)") },
                supportingText = { Text("Observaciones breves · máximo 500 caracteres (${state.descripcion.length}/500)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            CampoFormulario(
                valor = state.funcionario,
                etiqueta = "Funcionario asociado (ficticio)",
                error = state.errores.funcionario,
                onChange = viewModel::onFuncionarioChange
            )
            TipoDocumentoSelector(
                seleccionado = state.tipo,
                error = state.errores.tipo,
                onSeleccionar = viewModel::onTipoChange
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Adjunto", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Fotografía a resolución completa o PDF/imagen del dispositivo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val archivo = AdjuntoUtils.crearArchivoFoto(context)
                                    rutaPendiente = archivo.absolutePath
                                    camara.launch(AdjuntoUtils.uriCamara(context, archivo))
                                } catch (_: Exception) {
                                    rutaPendiente = null
                                    errorAdjunto = "No se pudo abrir la cámara en este dispositivo."
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Text(" Cámara")
                        }
                        OutlinedButton(
                            onClick = { selectorArchivo.launch(arrayOf("application/pdf", "image/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Text(" Archivo")
                        }
                    }
                    state.adjuntoNombre?.let { archivo ->
                        Text("Seleccionado: $archivo", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { viewModel.onAdjuntoSeleccionado(null, null) }) {
                            Text("Quitar adjunto")
                        }
                    }
                    miniatura?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Vista previa del adjunto",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().height(190.dp)
                        )
                    }
                    errorAdjunto?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            BotonPrincipal(
                texto = when {
                    state.guardando -> "Guardando..."
                    edicion -> "Guardar cambios"
                    else -> "Guardar documento"
                },
                onClick = viewModel::guardarDocumento,
                enabled = !state.guardando && !state.guardadoExitoso
            )
            state.errorGuardado?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.guardando && !state.guardadoExitoso
            ) {
                Text(if (edicion) "Volver al detalle" else "Volver al listado")
            }
        }
    }

    // Dialog usa una ventana superpuesta: no depende del desplazamiento del formulario.
    // No se puede cerrar accidentalmente antes del retorno automático.
    if (state.guardadoExitoso) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(280)) +
                    scaleIn(initialScale = 0.86f, animationSpec = tween(280))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (edicion) "¡Cambios guardados!" else "¡Documento guardado!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (edicion)
                                "Los cambios se guardaron correctamente. Volviendo al detalle…"
                            else
                                "El documento se registró correctamente. Volviendo al listado…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
