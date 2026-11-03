package cl.duoc.cmqsalud.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.cmqsalud.model.Documento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tarjeta reutilizada en Inicio y Documentos.
 * La tonalidad del fondo y de la etiqueta corresponde al estado del documento;
 * los estados desconocidos permanecen neutros para no comunicar una aprobación falsa.
 */
@Composable
fun DocumentoCard(
    documento: Documento,
    onClick: () -> Unit
) {
    val (fondoTarjeta, fondoEtiqueta, colorEstado) = when (documento.estado.trim().lowercase()) {
        "aprobado" -> Triple(
            Color(0xFFE4F5E9), // verde suave, legible en fondos claros
            Color(0xFFC7EBD2),
            Color(0xFF205B39)
        )
        "rechazado" -> Triple(
            Color(0xFFFCE8EA), // rojo suave; no saturar la pantalla
            Color(0xFFF5CED3),
            Color(0xFF922E3B)
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondoTarjeta)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                documento.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text("Tipo: ${documento.tipo}", color = MaterialTheme.colorScheme.onSurface)
            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        documento.estado,
                        color = colorEstado,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = fondoEtiqueta,
                    labelColor = colorEstado
                )
            )
            Text(
                "Fecha de creación: ${formatoFechaCreacion(documento.fechaCarga)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Última modificación: ${formatoFechaCreacion(documento.fechaModificacion)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (documento.descripcion.isNotBlank()) {
                Text(
                    documento.descripcion,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Funcionario: ${documento.funcionario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatoFechaCreacion(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(timestamp))
