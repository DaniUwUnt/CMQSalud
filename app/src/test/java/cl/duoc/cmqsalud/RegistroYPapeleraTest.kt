package cl.duoc.cmqsalud

import cl.duoc.cmqsalud.model.ValidacionRut
import cl.duoc.cmqsalud.data.PapeleraRetencion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroYPapeleraTest {
    @Test fun rutSeFormateaAlSalirDelCampo() {
        assertEquals("12.345.678-5", ValidacionRut.formateado("123456785"))
        assertEquals("12.345.678-5", ValidacionRut.formateado("12.345.678-5"))
        assertTrue(ValidacionRut.valido("123456785"))
        assertFalse(ValidacionRut.valido("123456780"))
    }

    @Test fun retencionEsDeVeinteDias() {
        val origen = 1_000_000L
        assertFalse(PapeleraRetencion.vencido(origen, origen + PapeleraRetencion.MILISEGUNDOS - 1L))
        assertTrue(PapeleraRetencion.vencido(origen, origen + PapeleraRetencion.MILISEGUNDOS))
        assertEquals(20L, PapeleraRetencion.diasRestantes(origen, origen))
        assertEquals(0L, PapeleraRetencion.diasRestantes(origen, PapeleraRetencion.venceEn(origen)))
    }
}
