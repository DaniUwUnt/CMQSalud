package cl.duoc.cmqsalud

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import cl.duoc.cmqsalud.navigation.AppNavigation
import cl.duoc.cmqsalud.navigation.AuthNavigation
import cl.duoc.cmqsalud.viewmodel.AuthViewModel
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel

@Composable
fun CMQSaludApp(viewModel: DocumentoViewModel, authViewModel: AuthViewModel) {
    val auth by authViewModel.estado.collectAsStateWithLifecycle()
    if (auth.usuario == null) {
        AuthNavigation(viewModel = authViewModel)
    } else {
        val navController = rememberNavController()
        AppNavigation(
            navController = navController,
            viewModel = viewModel,
            nombreUsuario = auth.usuario!!.nombre,
            rolUsuario = auth.usuario!!.rol,
            authViewModel = authViewModel,
            onCerrarSesion = authViewModel::cerrarSesion
        )
    }
}
