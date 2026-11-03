package cl.duoc.cmqsalud.model

/** Reglas locales para evitar reutilizar teléfonos en cuentas de demostración. */
object NumeroTelefonoRegistro {
    enum class Campo { CELULAR, FIJO }

    /** Ignora espacios, guiones y paréntesis; acepta variantes anteriores del móvil chileno. */
    fun normalizar(numero: String): String {
        val digitos = numero.filter(Char::isDigit)
        return when {
            digitos.isBlank() -> ""
            digitos.length == 8 -> "569$digitos" // Campo celular antiguo, sin prefijo.
            digitos.length == 9 && digitos.startsWith('9') -> "56$digitos"
            else -> digitos
        }
    }

    /** Compara ambos campos nuevos con ambos campos de TODAS las cuentas previas. */
    fun buscarDuplicado(
        celular: String,
        fijo: String,
        registrados: List<Pair<String, String>>
    ): Campo? {
        val existentes = registrados.flatMap { listOf(it.first, it.second) }
            .map(::normalizar).filter(String::isNotBlank).toSet()
        val celularNuevo = normalizar(celular)
        val fijoNuevo = normalizar(fijo)
        return when {
            celularNuevo.isNotEmpty() && celularNuevo in existentes -> Campo.CELULAR
            fijoNuevo.isNotEmpty() && fijoNuevo in existentes -> Campo.FIJO
            else -> null
        }
    }
}
