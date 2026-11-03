package cl.duoc.cmqsalud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CMQColorScheme = lightColorScheme(
    primary = CMQTurquesa,
    onPrimary = CMQSuperficie,
    primaryContainer = CMQCielo,
    onPrimaryContainer = CMQAzulProfundo,
    secondary = CMQVerdeSalud,
    onSecondary = CMQSuperficie,
    secondaryContainer = CMQContenedor,
    tertiary = CMQAzulProfundo,
    background = CMQFondo,
    onBackground = CMQTexto,
    surface = CMQSuperficie,
    onSurface = CMQTexto,
    surfaceVariant = CMQContenedor,
    onSurfaceVariant = CMQTextoSuave,
    outline = CMQTextoSuave
)

@Composable
fun CMQSaludTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CMQColorScheme, content = content)
}
