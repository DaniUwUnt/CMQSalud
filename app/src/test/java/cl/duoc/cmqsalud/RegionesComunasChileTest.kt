package cl.duoc.cmqsalud

import cl.duoc.cmqsalud.model.RegionesComunasChile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionesComunasChileTest {
    @Test fun catalogoLocalIncluyeDieciseisRegionesYTrescientasCuarentaYSeisComunas() {
        assertEquals(16, RegionesComunasChile.regiones.size)
        assertEquals(346, RegionesComunasChile.regiones.sumOf { RegionesComunasChile.comunasDe(it).size })
    }

    @Test fun alCambiarRegionNoSeAceptaComunaDeOtraRegion() {
        assertTrue(RegionesComunasChile.comunaValida("Valparaíso", "Viña del Mar"))
        assertTrue(RegionesComunasChile.comunaValida("Valparaíso", "Olmué"))
        assertFalse(RegionesComunasChile.comunaValida("Coquimbo", "Viña del Mar"))
        assertFalse(RegionesComunasChile.comunaValida("", ""))
    }
}
