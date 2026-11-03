package cl.duoc.cmqsalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cmqsalud.data.AuthRepository
import cl.duoc.cmqsalud.data.CMQDatabase
import cl.duoc.cmqsalud.data.DocumentoRepository
import cl.duoc.cmqsalud.data.LimpiezaPapelera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cl.duoc.cmqsalud.ui.theme.CMQSaludTheme
import cl.duoc.cmqsalud.viewmodel.AuthViewModel
import cl.duoc.cmqsalud.viewmodel.AuthViewModelFactory
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = CMQDatabase.getInstance(applicationContext)
        LimpiezaPapelera.programar(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { LimpiezaPapelera.ejecutar(applicationContext) }
        }
        val authRepository = AuthRepository(applicationContext)
        val docRepository = DocumentoRepository(database, authRepository, applicationContext)
        val docFactory = DocumentoViewModelFactory(docRepository)
        val authFactory = AuthViewModelFactory(authRepository)

        setContent {
            CMQSaludTheme {
                val documentoViewModel: DocumentoViewModel = viewModel(factory = docFactory)
                val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                CMQSaludApp(viewModel = documentoViewModel, authViewModel = authViewModel)
            }
        }
    }
}
