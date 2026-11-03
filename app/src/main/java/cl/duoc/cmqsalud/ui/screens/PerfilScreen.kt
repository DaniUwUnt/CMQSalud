package cl.duoc.cmqsalud.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cl.duoc.cmqsalud.model.RegionesComunasChile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.cmqsalud.ui.components.AppHeader
import cl.duoc.cmqsalud.viewmodel.AuthViewModel

@Composable
fun PerfilScreen(viewModel: AuthViewModel, onVolver: () -> Unit, onCerrarSesion: () -> Unit) {
    var perfil by remember { mutableStateOf(viewModel.perfilActual()) }
    var region by remember { mutableStateOf(perfil?.region.orEmpty()) }
    var comuna by remember { mutableStateOf(perfil?.comuna.orEmpty()) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = {
        AppHeader(titulo = "Mi perfil", subtitulo = "Datos de cuenta · Entorno de prueba",
            accion = {
                IconButton(onClick = onCerrarSesion) {
                    Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                }
            })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Capturar el estado delegado en una variable local inmutable permite
            // acceder a sus propiedades sin errores de smart cast.
            val datosPerfil = perfil
            if (datosPerfil == null) {
                Text("Inicia sesión para ver los datos de tu perfil.")
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Información de la cuenta", style = MaterialTheme.typography.titleLarge)
                        Text("Nombre(s): ${datosPerfil.nombre}")
                        TextoPerfil("Apellidos", datosPerfil.apellidos)
                        TextoPerfil("RUT ficticio", datosPerfil.rut)
                        Text("Correo: ${datosPerfil.correo}")
                        TextoPerfil("Celular", datosPerfil.celular)
                        TextoPerfil("Fijo", datosPerfil.fijo)
                        TextoPerfil("Región", datosPerfil.region)
                        TextoPerfil("Comuna", datosPerfil.comuna)
                        Text("Rol: ${datosPerfil.rol.name}", color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Mi región y comuna", style = MaterialTheme.typography.titleMedium)
                        Text("Puedes completar este dato aunque tu cuenta sea de una versión anterior.",
                            style = MaterialTheme.typography.bodySmall)
                        SelectorUbicacion(
                            valor = region, titulo = "Región",
                            opciones = RegionesComunasChile.regiones,
                            error = null,
                            onSeleccion = { region = it; comuna = ""; mensaje = null }
                        )
                        SelectorUbicacion(
                            valor = comuna, titulo = "Comuna",
                            opciones = RegionesComunasChile.comunasDe(region),
                            error = null, habilitado = region.isNotBlank(),
                            onSeleccion = { comuna = it; mensaje = null }
                        )
                        Button(
                            onClick = {
                                viewModel.actualizarUbicacion(region, comuna) { error ->
                                    mensaje = error ?: "Ubicación guardada correctamente."
                                    if (error == null) perfil = viewModel.perfilActual()
                                }
                            },
                            enabled = RegionesComunasChile.comunaValida(region, comuna),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar ubicación") }
                        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                }
                Text("Las cuentas antiguas pueden tener datos pendientes. Usa información ficticia en el piloto.",
                    style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
        }
    }
}

@Composable
private fun TextoPerfil(titulo: String, valor: String) {
    Text("$titulo: ${valor.ifBlank { "Pendiente (cuenta anterior)" }}")
}
