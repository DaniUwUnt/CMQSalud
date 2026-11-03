package cl.duoc.cmqsalud.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TipoDocumentoSelector(
    seleccionado: String,
    error: String?,
    onSeleccionar: (String) -> Unit
) {
    val opciones = listOf("Contrato", "Certificado", "Permiso")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tipo de documento", style = MaterialTheme.typography.titleMedium)
        opciones.forEach { opcion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = seleccionado == opcion,
                    onClick = { onSeleccionar(opcion) }
                )
                Text(opcion)
            }
        }
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
