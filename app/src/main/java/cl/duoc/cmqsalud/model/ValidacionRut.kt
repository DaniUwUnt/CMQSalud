package cl.duoc.cmqsalud.model

/** Verificación módulo 11 de un RUT chileno de demostración. No confirma identidad. */
object ValidacionRut {
    fun normalizado(valor: String): String = valor.uppercase().filter { it.isDigit() || it == 'K' }

    /** Da formato a un RUT completo al abandonar el campo: 123456785 -> 12.345.678-5. */
    fun formateado(valor: String): String {
        val sinFormato = normalizado(valor)
        if (sinFormato.length !in 8..9) return valor.trim()
        val cuerpo = sinFormato.dropLast(1)
        if (!cuerpo.all(Char::isDigit)) return valor.trim()
        val cuerpoConPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpoConPuntos-${sinFormato.last()}"
    }

    fun valido(valor: String): Boolean {
        val texto = normalizado(valor)
        if (texto.length !in 8..9) return false
        val cuerpo = texto.dropLast(1)
        if (cuerpo.length !in 7..8 || !cuerpo.all(Char::isDigit)) return false
        var factor = 2
        var suma = 0
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val calculado = 11 - (suma % 11)
        val dv = when (calculado) {
            11 -> "0"
            10 -> "K"
            else -> calculado.toString()
        }
        return texto.last().toString() == dv
    }
}
