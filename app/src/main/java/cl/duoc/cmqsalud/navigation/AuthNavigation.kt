package cl.duoc.cmqsalud.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.cmqsalud.ui.screens.LoginScreen
import cl.duoc.cmqsalud.ui.screens.RegistroUsuarioScreen
import cl.duoc.cmqsalud.viewmodel.AuthViewModel

@Composable
fun AuthNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel = viewModel, onIrRegistro = { navController.navigate("registro_usuario") })
        }
        composable("registro_usuario") {
            RegistroUsuarioScreen(viewModel = viewModel, onVolver = { navController.popBackStack() })
        }
    }
}
