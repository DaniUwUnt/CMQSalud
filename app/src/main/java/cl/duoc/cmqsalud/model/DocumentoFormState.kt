package cl.duoc.cmqsalud.model

data class DocumentoFormErrors(
    val nombre: String? = null,
    val funcionario: String? = null,
    val tipo: String? = null
) {
    fun hayErrores(): Boolean = nombre != null || funcionario != null || tipo != null
}

data class DocumentoFormUiState(
    val editarId: Int? = null,
    val fechaOriginal: Long? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val funcionario: String = "",
    val tipo: String = "",
    val estado: String = "Pendiente",
    val adjuntoUri: String? = null,
    val adjuntoNombre: String? = null,
    val errores: DocumentoFormErrors = DocumentoFormErrors(),
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val errorGuardado: String? = null
)
