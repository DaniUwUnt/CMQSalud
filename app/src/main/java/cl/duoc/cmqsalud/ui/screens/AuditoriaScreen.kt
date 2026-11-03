package cl.duoc.cmqsalud.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.data.PapeleraRetencion
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento
import cl.duoc.cmqsalud.ui.components.AdminPanelHeader
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditoriaScreen(viewModel: DocumentoViewModel, onVolver: () -> Unit) {
    val eventos by viewModel.historialGlobal.collectAsStateWithLifecycle()
    val eliminados by viewModel.papelera.collectAsStateWithLifecycle()
    var filtro by rememberSaveable { mutableStateOf("TODOS") }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var pestana by rememberSaveable { mutableStateOf(0) } // 0: actividad, 1: papelera
    var confirmarRestauracion by remember { mutableStateOf<Int?>(null) }
    var confirmarBorrado by remember { mutableStateOf<Int?>(null) }
    var operando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<String?>(null) }

    val filtrados = eventos.filter { item ->
        (filtro == "TODOS" || item.accion == filtro) &&
            (busqueda.isBlank() || item.documentoNombre.contains(busqueda, true) ||
                item.actorCorreo.contains(busqueda, true) || item.actorNombre.contains(busqueda, true) ||
                item.documentoId.toString() == busqueda.trim())
    }

    Scaffold(topBar = {
        Column {
            AdminPanelHeader("Auditoría documental", "Actividad y papelera · Solo administradores", onVolver)
            TabRow(selectedTabIndex = pestana) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 },
                    text = { Text("Actividad (${eventos.size})") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 },
                    text = { Text("Papelera (${eliminados.size})") })
            }
        }
    }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            resultado?.let { mensaje ->
                item {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(mensaje, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { resultado = null }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Cerrar aviso")
                            }
                        }
                    }
                }
            }
            if (pestana == 0) {
                item {
                    Text("Historial de actividad", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Cada operación conserva su autor y fecha. Toca un evento para ver sus detalles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = { Text("Buscar eventos") },
                        placeholder = { Text("Documento, usuario, correo o ID") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Limpiar búsqueda")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TODOS", "CREACIÓN", "EDICIÓN", "REVISIÓN", "ELIMINACIÓN",
                            "RESTAURACIÓN", "PURGA AUTOMÁTICA", "PURGA MANUAL")
                            .forEach { opcion ->
                                FilterChip(
                                    selected = filtro == opcion,
                                    onClick = { filtro = opcion },
                                    label = {
                                        Text(if (opcion == "TODOS") "Todos" else
                                            opcion.lowercase().replaceFirstChar(Char::uppercase))
                                    }
                                )
                            }
                    }
                    Text("${filtrados.size} evento(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (filtrados.isEmpty()) {
                    item {
                        PanelVacio("No se encontraron eventos.",
                            "Prueba otra búsqueda o selecciona el filtro Todos.")
                    }
                }
                items(filtrados, key = { it.id }) { evento ->
                    EventoAuditoriaCard(evento)
                }
                item {
                    Text("Auditoría local de demostración. No es un registro institucional inalterable.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                item {
                    Text("Papelera recuperable", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Puedes restaurar documentos antes del vencimiento o borrarlos de forma definitiva.",
                        style = MaterialTheme.typography.bodyMedium)
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)) {
                        Text("Los documentos permanecen hasta 20 días. La limpieza automática puede ejecutarse después del plazo si Android retrasa las tareas.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (eliminados.isEmpty()) {
                    item { PanelVacio("La papelera está vacía", "No hay documentos pendientes de restauración.") }
                }
                items(eliminados, key = { it.id }) { documento ->
                    DocumentoPapeleraCard(
                        documento = documento,
                        operando = operando,
                        onRestaurar = { confirmarRestauracion = documento.id },
                        onEliminar = { confirmarBorrado = documento.id }
                    )
                }
            }
        }
    }

    confirmarBorrado?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!operando) confirmarBorrado = null },
            title = { Text("¿Borrar definitivamente?") },
            text = { Text("Esta acción elimina el documento de la base local y no se puede deshacer. Su evento de auditoría permanece. Úsala solo con datos ficticios.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = null
                    operando = true
                    viewModel.borrarDefinitivamente(id) { correcto ->
                        operando = false
                        resultado = if (correcto) "Documento borrado definitivamente. La auditoría se conservó."
                        else "No fue posible borrar el documento. Verifica la cuenta y su estado."
                    }
                }, enabled = !operando) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrado = null }) { Text("Cancelar") } }
        )
    }
    confirmarRestauracion?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!operando) confirmarRestauracion = null },
            title = { Text("¿Restaurar documento?") },
            text = { Text("Volverá al listado de documentos activos y quedará registrada su restauración.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRestauracion = null
                    operando = true
                    viewModel.restaurarDocumento(id) { correcto ->
                        operando = false
                        resultado = if (correcto) "Documento restaurado correctamente."
                            else "No se pudo restaurar el documento."
                    }
                }, enabled = !operando) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { confirmarRestauracion = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PanelVacio(titulo: String, descripcion: String) {
    Surface(shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DocumentoPapeleraCard(
    documento: Documento,
    operando: Boolean,
    onRestaurar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaEliminacion = documento.deletedAt
    val vencido = fechaEliminacion == null || PapeleraRetencion.vencido(fechaEliminacion)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(documento.nombre, style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("#${documento.id} · ${documento.tipo}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Text("Funcionario: ${documento.funcionario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2)
            Text("Fecha de creación: ${formatoFecha(documento.fechaCarga)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Última modificación: ${formatoFecha(documento.fechaModificacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            if (fechaEliminacion != null) {
                Text("Eliminado el ${formatoFecha(fechaEliminacion)}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Eliminación prevista: ${formatoFecha(PapeleraRetencion.venceEn(fechaEliminacion))}",
                    style = MaterialTheme.typography.bodySmall)
                Surface(
                    color = if (vencido) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (vencido) "Plazo vencido · pendiente de limpieza"
                        else "${PapeleraRetencion.diasRestantes(fechaEliminacion)} día(s) para restaurar",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onRestaurar, modifier = Modifier.weight(1f),
                    enabled = !operando && !vencido) {
                    Icon(Icons.Outlined.Restore, contentDescription = null)
                    Text(" Restaurar")
                }
                OutlinedButton(onClick = onEliminar, modifier = Modifier.weight(1f),
                    enabled = !operando) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error)
                    Text(" Borrar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EventoAuditoriaCard(evento: EventoAuditoriaDocumento) {
    var expandido by rememberSaveable(evento.id) { mutableStateOf(false) }
    val fecha = remember(evento.fecha) { formatoFecha(evento.fecha) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)) {
                    Text(evento.accion.lowercase().replaceFirstChar(Char::uppercase),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall)
                }
                Text(fecha, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(evento.documentoNombre, style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Por ${evento.actorNombre}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ocultar detalle" else "Ver detalle")
            }
            AnimatedVisibility(visible = expandido) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider()
                    Text("Documento #${evento.documentoId}", style = MaterialTheme.typography.bodySmall)
                    Text("Correo: ${evento.actorCorreo}", style = MaterialTheme.typography.bodySmall)
                    Text(evento.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun formatoFecha(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(timestamp))

