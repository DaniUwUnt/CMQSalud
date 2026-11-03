package cl.duoc.cmqsalud.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.data.AdjuntoUtils
import cl.duoc.cmqsalud.model.ReglasRevision
import cl.duoc.cmqsalud.ui.components.AppHeader
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleDocumentoScreen(
    id: Int,
    viewModel: DocumentoViewModel,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    esAdministrador: Boolean
) {
    val documento by remember(id) { viewModel.documentoPorId(id) }
        .collectAsStateWithLifecycle(initialValue = null)
    val historial by remember(id) { viewModel.historialPorDocumento(id) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var historialExpandido by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }
    var errorAlEliminar by remember { mutableStateOf<String?>(null) }
    var errorAlAbrir by remember { mutableStateOf<String?>(null) }
    var observacionNueva by remember(id) { mutableStateOf("") }
    var revisando by remember { mutableStateOf(false) }
    var resultadoRevision by remember { mutableStateOf<String?>(null) }

    val imagen by produceState<android.graphics.Bitmap?>(null, documento?.adjuntoUri) {
        val uri = documento?.adjuntoUri
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            AdjuntoUtils.miniatura(context, uri)
        }
    }

    Scaffold(topBar = { AppHeader(titulo = "Detalle del documento", subtitulo = "Consulta y acciones") }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (documento == null) {
                Text("Cargando documento...")
            } else {
                val actual = documento!!
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(actual.nombre, style = MaterialTheme.typography.headlineSmall)
                        if (actual.descripcion.isNotBlank()) Text("Descripción: ${actual.descripcion}")
                        Text("Tipo: ${actual.tipo}")
                        Text("Funcionario: ${actual.funcionario}")
                        Text("Estado: ${actual.estado}")
                        if (actual.revisadoPor != null) {
                            Text("Revisado por: ${actual.revisadoPor}")
                            actual.fechaRevision?.let { Text("Revisión: ${formatearFecha(it)}") }
                        }
                        if (actual.observacionRevision.isNotBlank()) {
                            Text("Observación de revisión: ${actual.observacionRevision}")
                        }
                        Text("Fecha de creación: ${formatearFecha(actual.fechaCarga)}")
                        Text("Última modificación: ${formatearFecha(actual.fechaModificacion)}")
                        Text("Adjunto: ${actual.adjuntoNombre ?: "Sin adjunto"}")
                    }
                }
                if (actual.adjuntoUri != null) {
                    imagen?.let { bitmap ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Fotografía adjunta al documento",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth().height(220.dp)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            errorAlAbrir = AdjuntoUtils.abrir(context, actual.adjuntoUri, actual.adjuntoNombre)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir adjunto")
                    }
                    errorAlAbrir?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
                if (esAdministrador) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Revisión documental · piloto", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "El circuito de aprobación es una propuesta sujeta a validación con el docente/CITT. " +
                                    "Las modificaciones posteriores devuelven el documento a Pendiente.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = observacionNueva,
                                onValueChange = { observacionNueva = it.take(500); resultadoRevision = null },
                                label = { Text("Observación de revisión") },
                                supportingText = { Text("Opcional al aprobar; obligatoria al rechazar (${observacionNueva.length}/500)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                                enabled = !revisando
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        revisando = true
                                        resultadoRevision = null
                                        viewModel.revisarDocumento(actual.id, ReglasRevision.APROBADO, observacionNueva) { error ->
                                            revisando = false
                                            resultadoRevision = error ?: "Revisión aprobada y registrada en auditoría."
                                            if (error == null) observacionNueva = ""
                                        }
                                    },
                                    enabled = !revisando,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Aprobar") }
                                Button(
                                    onClick = {
                                        revisando = true
                                        resultadoRevision = null
                                        viewModel.revisarDocumento(actual.id, ReglasRevision.RECHAZADO, observacionNueva) { error ->
                                            revisando = false
                                            resultadoRevision = error ?: "Rechazo registrado con su motivo."
                                            if (error == null) observacionNueva = ""
                                        }
                                    },
                                    enabled = !revisando,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Rechazar") }
                            }
                            resultadoRevision?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Historial del documento", style = MaterialTheme.typography.titleMedium)
                        Text("Creación, ediciones, revisiones y otras acciones, con su autor y fecha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (historial.isEmpty()) {
                            Text("No hay eventos registrados. Los documentos anteriores a la auditoría pueden no tener historial previo.",
                                style = MaterialTheme.typography.bodySmall)
                        } else {
                            val visibles = if (historialExpandido) historial else historial.take(3)
                            visibles.forEach { evento ->
                                Text("${evento.accion} · ${evento.actorNombre} · ${formatearFecha(evento.fecha)}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(evento.descripcion, style = MaterialTheme.typography.bodySmall)
                            }
                            if (historial.size > 3) {
                                TextButton(onClick = { historialExpandido = !historialExpandido }) {
                                    Text(if (historialExpandido) "Mostrar menos" else "Mostrar historial completo (${historial.size})")
                                }
                            }
                        }
                    }
                }
                Button(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar documento")
                }
                errorAlEliminar?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = { mostrarConfirmacion = true },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (eliminando) "Eliminando..." else "Eliminar documento")
                }
                if (mostrarConfirmacion) {
                    AlertDialog(
                        onDismissRequest = { if (!eliminando) mostrarConfirmacion = false },
                        title = { Text("¿Eliminar documento?") },
                        text = { Text("El documento irá a la papelera por 20 días. Durante ese plazo el administrador podrá restaurarlo. Después se eliminará definitivamente y quedará un evento en la bitácora.") },
                        confirmButton = {
                            TextButton(
                                enabled = !eliminando,
                                onClick = {
                                    mostrarConfirmacion = false
                                    eliminando = true
                                    errorAlEliminar = null
                                    viewModel.eliminarDocumento(actual) { eliminado ->
                                        if (eliminado) onVolver()
                                        else {
                                            eliminando = false
                                            errorAlEliminar = "No fue posible eliminar el documento."
                                        }
                                    }
                                }
                            ) { Text("Sí, eliminar", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !eliminando,
                                onClick = { mostrarConfirmacion = false }
                            ) { Text("Cancelar") }
                        }
                    )
                }
            }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al listado")
            }
        }
    }
}

private fun formatearFecha(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
