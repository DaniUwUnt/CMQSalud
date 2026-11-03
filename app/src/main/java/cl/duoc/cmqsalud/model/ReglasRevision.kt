package cl.duoc.cmqsalud.model

/** Reglas propuestas SOLO para el piloto; validar circuito real con docente/CITT. */
object ReglasRevision {
    const val PENDIENTE = "Pendiente"
    const val APROBADO = "Aprobado"
    const val RECHAZADO = "Rechazado"

    val estados = listOf(PENDIENTE, APROBADO, RECHAZADO)

    fun errorRevision(nuevoEstado: String, observacion: String): String? = when {
        nuevoEstado !in listOf(APROBADO, RECHAZADO) -> "Selecciona Aprobar o Rechazar."
        nuevoEstado == RECHAZADO && observacion.isBlank() -> "Indica el motivo del rechazo."
        observacion.length > 500 -> "La observación no puede superar 500 caracteres."
        else -> null
    }
}
