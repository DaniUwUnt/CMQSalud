package cl.duoc.cmqsalud.data

import java.util.concurrent.TimeUnit

/** Política del piloto: retención de 20 días desde el momento de eliminación lógica. */
object PapeleraRetencion {
    const val DIAS = 20
    const val MILISEGUNDOS: Long = 20L * 24L * 60L * 60L * 1000L

    fun venceEn(eliminadoEn: Long): Long = eliminadoEn + MILISEGUNDOS

    fun vencido(eliminadoEn: Long, ahora: Long = System.currentTimeMillis()): Boolean =
        ahora >= venceEn(eliminadoEn)

    fun diasRestantes(eliminadoEn: Long, ahora: Long = System.currentTimeMillis()): Long =
        ((venceEn(eliminadoEn) - ahora).coerceAtLeast(0L) + TimeUnit.DAYS.toMillis(1) - 1) /
            TimeUnit.DAYS.toMillis(1)
}
