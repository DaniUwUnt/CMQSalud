package cl.duoc.cmqsalud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.data.CuentaAdministrable
import cl.duoc.cmqsalud.data.RolDemo
import cl.duoc.cmqsalud.ui.components.AdminPanelHeader
import cl.duoc.cmqsalud.viewmodel.AccionAdmin
import cl.duoc.cmqsalud.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ConfirmacionAdmin(val cuenta: CuentaAdministrable, val accion: AccionAdmin)

@Composable
fun AdministracionScreen(viewModel: AuthViewModel, onVolver: () -> Unit, onAbrirAuditoria: () -> Unit) {
    val autenticacion by viewModel.estado.collectAsStateWithLifecycle()
    val admin by viewModel.administracion.collectAsStateWithLifecycle()
    var busqueda by rememberSaveable { mutableStateOf("") }
    var verActividad by rememberSaveable { mutableStateOf(false) }
    var confirmacion by remember { mutableStateOf<ConfirmacionAdmin?>(null) }

    LaunchedEffect(autenticacion.usuario?.correo) { viewModel.actualizarCuentasAdmin() }
    val correoActual = autenticacion.usuario?.correo
    val cuentas = admin.cuentas.filter {
        busqueda.isBlank() || it.nombre.contains(busqueda, ignoreCase = true) ||
            it.correo.contains(busqueda, ignoreCase = true)
    }

    Scaffold(topBar = {
        AdminPanelHeader("Administración", "Cuentas y permisos · Piloto académico", onVolver)
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Resumen de cuentas", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenAdmin("Total", admin.cuentas.size, Modifier.weight(1f))
                    ResumenAdmin("Activas", admin.cuentas.count { it.habilitada }, Modifier.weight(1f))
                    ResumenAdmin("Admin", admin.cuentas.count { it.rol == RolDemo.ADMINISTRADOR }, Modifier.weight(1f))
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirAuditoria),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Shield, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text("Auditoría y papelera", style = MaterialTheme.typography.titleMedium)
                            Text("Actividad de documentos y restauraciones",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                    }
                }
            }
            item {
                Text("Usuarios registrados", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                Text("Busca una cuenta y abre sus opciones solo cuando necesites administrarla.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Limpiar búsqueda")
                        }
                    },
                    placeholder = { Text("Nombre o correo") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Text("${cuentas.size} cuenta(s) encontrada(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp))
            }
            admin.mensaje?.let { mensaje ->
                item {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)) {
                        Text(mensaje, Modifier.fillMaxWidth().padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (cuentas.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp) {
                        Text("No hay cuentas que coincidan con la búsqueda.",
                            Modifier.fillMaxWidth().padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            items(cuentas, key = { it.correo }) { cuenta ->
                CuentaAdminCard(
                    cuenta = cuenta,
                    esPropia = cuenta.correo == correoActual,
                    procesando = admin.procesando,
                    onAccion = { confirmacion = ConfirmacionAdmin(cuenta, it) }
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                TextButton(onClick = { verActividad = !verActividad }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.History, contentDescription = null)
                    Text(if (verActividad) "  Ocultar historial de cuentas"
                         else "  Ver historial de cuentas (${admin.auditoria.size})")
                }
                if (verActividad) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)) {
                        if (admin.auditoria.isEmpty()) {
                            Text("Todavía no hay acciones registradas.")
                        } else {
                            admin.auditoria.take(10).forEach { evento ->
                                val fecha = remember(evento.fecha) {
                                    SimpleDateFormat("dd/MM HH:mm", Locale("es", "CL")).format(Date(evento.fecha))
                                }
                                Text("$fecha · ${evento.descripcion}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Text("Historial local de demostración; no es una bitácora institucional.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    confirmacion?.let { pendiente ->
        AlertDialog(
            onDismissRequest = { confirmacion = null },
            title = { Text(tituloAccion(pendiente.accion)) },
            text = {
                Text("Cuenta: ${pendiente.cuenta.nombre} (${pendiente.cuenta.correo}). " +
                    if (pendiente.accion == AccionAdmin.ELIMINAR)
                        "Esta eliminación no se puede deshacer. Los documentos del piloto permanecerán guardados."
                    else "¿Confirmas este cambio de permisos o estado?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.ejecutarAccionAdmin(pendiente.cuenta.correo, pendiente.accion)
                    confirmacion = null
                }, enabled = !admin.procesando) {
                    Text("Confirmar", color = if (pendiente.accion == AccionAdmin.ELIMINAR)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = { TextButton(onClick = { confirmacion = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ResumenAdmin(etiqueta: String, valor: Int, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$valor", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CuentaAdminCard(
    cuenta: CuentaAdministrable,
    esPropia: Boolean,
    procesando: Boolean,
    onAccion: (AccionAdmin) -> Unit
) {
    var expandida by rememberSaveable(cuenta.correo) { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val iniciales = cuenta.nombre.split(" ").filter { it.isNotBlank() }
                    .take(2).map { it.first().uppercaseChar() }.joinToString("")
                Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(iniciales.ifBlank { "U" }, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text(cuenta.nombre, style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(cuenta.correo, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically) {
                EtiquetaCuenta(if (cuenta.rol == RolDemo.ADMINISTRADOR) "Admin" else "Funcionario")
                EtiquetaCuenta(if (cuenta.habilitada) "Activa" else "Bloqueada")
                if (esPropia) EtiquetaCuenta("Tú")
            }
            if (esPropia) {
                Text("Tu cuenta está protegida frente a cambios administrativos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TextButton(onClick = { expandida = !expandida }, enabled = !procesando) {
                    Text(if (expandida) "Ocultar opciones" else "Administrar esta cuenta")
                }
                if (expandida) {
                    HorizontalDivider()
                    if (cuenta.rut.isNotBlank())
                        Text("RUT ficticio: ••••${cuenta.rut.takeLast(4)}",
                            style = MaterialTheme.typography.bodySmall)
                    if (cuenta.celular.isNotBlank())
                        Text("Celular de prueba: ${cuenta.celular}",
                            style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { onAccion(if (cuenta.habilitada)
                                AccionAdmin.DESHABILITAR else AccionAdmin.REACTIVAR) },
                            enabled = !procesando, modifier = Modifier.weight(1f)
                        ) { Text(if (cuenta.habilitada) "Bloquear" else "Reactivar") }
                        OutlinedButton(
                            onClick = { onAccion(if (cuenta.rol == RolDemo.ADMINISTRADOR)
                                AccionAdmin.HACER_FUNCIONARIO else AccionAdmin.HACER_ADMIN) },
                            enabled = !procesando, modifier = Modifier.weight(1f)
                        ) { Text(if (cuenta.rol == RolDemo.ADMINISTRADOR) "Quitar admin" else "Dar admin") }
                    }
                    TextButton(onClick = { onAccion(AccionAdmin.ELIMINAR) },
                        enabled = !procesando, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar esta cuenta", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun EtiquetaCuenta(etiqueta: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(30.dp)) {
        Text(etiqueta, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelSmall)
    }
}

private fun tituloAccion(accion: AccionAdmin): String = when (accion) {
    AccionAdmin.ELIMINAR -> "Eliminar cuenta"
    AccionAdmin.DESHABILITAR -> "Bloquear cuenta"
    AccionAdmin.REACTIVAR -> "Reactivar cuenta"
    AccionAdmin.HACER_ADMIN -> "Conceder rol administrador"
    AccionAdmin.HACER_FUNCIONARIO -> "Cambiar a funcionario"
}
