package cl.duoc.cmqsalud.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import cl.duoc.cmqsalud.data.RolDemo
import cl.duoc.cmqsalud.ui.screens.AdministracionScreen
import cl.duoc.cmqsalud.ui.screens.AuditoriaScreen
import cl.duoc.cmqsalud.ui.screens.DetalleDocumentoScreen
import cl.duoc.cmqsalud.ui.screens.DocumentosScreen
import cl.duoc.cmqsalud.ui.screens.HomeScreen
import cl.duoc.cmqsalud.ui.screens.PerfilScreen
import cl.duoc.cmqsalud.ui.screens.RegistroDocumentoScreen
import cl.duoc.cmqsalud.viewmodel.AuthViewModel
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel

object Routes {
    const val HOME = "home"
    const val DOCUMENTOS = "documentos"
    const val REGISTRO = "registro"
    const val DETALLE = "detalle/{id}"
    const val EDITAR = "editar/{id}"
    const val ADMINISTRACION = "administracion"
    const val AUDITORIA = "auditoria"
    const val PERFIL = "perfil"

    fun detalle(id: Int) = "detalle/$id"
    fun editar(id: Int) = "editar/$id"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: DocumentoViewModel,
    nombreUsuario: String,
    rolUsuario: RolDemo,
    authViewModel: AuthViewModel,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val rutasPrincipales = setOf(Routes.HOME, Routes.DOCUMENTOS, Routes.REGISTRO, Routes.PERFIL)

    fun irPestana(destino: String) {
        if (destino == Routes.REGISTRO && rutaActual != Routes.REGISTRO) viewModel.prepararNuevoRegistro()
        navController.navigate(destino) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = destino != Routes.REGISTRO
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tu información guardada seguirá disponible en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = { confirmarSalida = false; onCerrarSesion() }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }

    // Las pantallas interiores gestionan sus propias barras del sistema.
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (rutaActual in rutasPrincipales) {
                NavigationBar {
                    val tabs = listOf(
                        Triple(Routes.HOME, "Inicio", Icons.Outlined.Home),
                        Triple(Routes.DOCUMENTOS, "Documentos", Icons.Outlined.Description),
                        Triple(Routes.REGISTRO, "Nuevo", Icons.Outlined.AddCircleOutline),
                        Triple(Routes.PERFIL, "Mi perfil", Icons.Outlined.PersonOutline)
                    )
                    tabs.forEach { (ruta, etiqueta, icono) ->
                        NavigationBarItem(
                            selected = rutaActual == ruta,
                            onClick = { if (rutaActual != ruta) irPestana(ruta) },
                            icon = { Icon(icono, contentDescription = etiqueta) },
                            label = { Text(etiqueta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    nombreUsuario = nombreUsuario,
                    esAdministrador = rolUsuario == RolDemo.ADMINISTRADOR,
                    onCerrarSesion = { confirmarSalida = true },
                    onAbrirDocumentos = { irPestana(Routes.DOCUMENTOS) },
                    onAbrirAdmin = if (rolUsuario == RolDemo.ADMINISTRADOR) {
                        { navController.navigate(Routes.ADMINISTRACION) }
                    } else null,
                    onVerDetalle = { id -> navController.navigate(Routes.detalle(id)) }
                )
            }
            composable(Routes.DOCUMENTOS) {
                DocumentosScreen(
                    viewModel = viewModel,
                    onNuevoDocumento = { irPestana(Routes.REGISTRO) },
                    onCerrarSesion = { confirmarSalida = true },
                    onVerDetalle = { id -> navController.navigate(Routes.detalle(id)) }
                )
            }
            composable(Routes.PERFIL) {
                PerfilScreen(viewModel = authViewModel, onVolver = { irPestana(Routes.HOME) },
                    onCerrarSesion = { confirmarSalida = true })
            }
            composable(Routes.REGISTRO) {
                RegistroDocumentoScreen(viewModel = viewModel,
                    onVolver = { irPestana(Routes.DOCUMENTOS) })
            }
            if (rolUsuario == RolDemo.ADMINISTRADOR) {
                composable(Routes.ADMINISTRACION) {
                    AdministracionScreen(
                        viewModel = authViewModel,
                        onVolver = { navController.popBackStack() },
                        onAbrirAuditoria = { navController.navigate(Routes.AUDITORIA) }
                    )
                }
                composable(Routes.AUDITORIA) {
                    AuditoriaScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
                }
            }
            composable(
                route = Routes.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { item ->
                val id = item.arguments?.getInt("id") ?: return@composable
                DetalleDocumentoScreen(
                    id = id,
                    viewModel = viewModel,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(Routes.editar(id)) },
                    esAdministrador = rolUsuario == RolDemo.ADMINISTRADOR
                )
            }
            composable(
                route = Routes.EDITAR,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { item ->
                val id = item.arguments?.getInt("id") ?: return@composable
                RegistroDocumentoScreen(
                    viewModel = viewModel,
                    documentoId = id,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}
