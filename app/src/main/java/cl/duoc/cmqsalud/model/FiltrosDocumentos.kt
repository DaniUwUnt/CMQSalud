package cl.duoc.cmqsalud.model

/** Búsqueda local y estadísticas derivadas de datos Room, sin valores simulados. */
data class ResumenDocumental(
    val total: Int, val pendientes: Int, val aprobados: Int, val rechazados: Int
)

object FiltrosDocumentos {
    const val TODOS = "Todos"

    fun resumen(documentos: List<Documento>): ResumenDocumental = ResumenDocumental(
        total = documentos.size,
        pendientes = documentos.count { it.estado == ReglasRevision.PENDIENTE },
        aprobados = documentos.count { it.estado == ReglasRevision.APROBADO },
        rechazados = documentos.count { it.estado == ReglasRevision.RECHAZADO }
    )

    fun filtrar(
        documentos: List<Documento>,
        busqueda: String,
        tipo: String = TODOS,
        estado: String = TODOS,
        recientesPrimero: Boolean = true
    ): List<Documento> {
        val termino = busqueda.trim()
        val encontrados = documentos.filter { doc ->
            (termino.isBlank() || listOf(doc.nombre, doc.descripcion, doc.tipo, doc.funcionario)
                .any { it.contains(termino, ignoreCase = true) }) &&
                (tipo == TODOS || doc.tipo == tipo) &&
                (estado == TODOS || doc.estado == estado) &&
                doc.deletedAt == null
        }
        return if (recientesPrimero) encontrados.sortedByDescending { it.fechaCarga }
        else encontrados.sortedBy { it.fechaCarga }
    }
}
