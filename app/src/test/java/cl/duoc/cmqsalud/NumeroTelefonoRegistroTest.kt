package cl.duoc.cmqsalud

import cl.duoc.cmqsalud.model.NumeroTelefonoRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumeroTelefonoRegistroTest {
    @Test fun rechazaCelularExistenteAunqueCambieFormato() {
        val cuentas = listOf("+56 9 1234-5678" to "")
        assertEquals(NumeroTelefonoRegistro.Campo.CELULAR,
            NumeroTelefonoRegistro.buscarDuplicado("+56912345678", "", cuentas))
        assertEquals(NumeroTelefonoRegistro.Campo.CELULAR,
            NumeroTelefonoRegistro.buscarDuplicado("12345678", "", cuentas))
    }

    @Test fun rechazaTelefonoFijoRepetido() {
        val cuentas = listOf("+56912345678" to "+56 32 234 5678")
        assertEquals(NumeroTelefonoRegistro.Campo.FIJO,
            NumeroTelefonoRegistro.buscarDuplicado("+56987654321", "+56322345678", cuentas))
    }

    @Test fun revisaAmbosCamposYNoSoloLaMismaColumna() {
        val cuentas = listOf("+56912345678" to "")
        assertEquals(NumeroTelefonoRegistro.Campo.FIJO,
            NumeroTelefonoRegistro.buscarDuplicado("+56987654321", "+56912345678", cuentas))
    }

    @Test fun aceptaTelefonosNoRegistradosYFijoVacio() {
        val cuentas = listOf("+56912345678" to "+56322345678")
        assertNull(NumeroTelefonoRegistro.buscarDuplicado("+56987654321", "", cuentas))
    }
}
