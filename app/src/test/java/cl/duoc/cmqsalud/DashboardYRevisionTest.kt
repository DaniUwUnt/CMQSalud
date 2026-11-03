package cl.duoc.cmqsalud

import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.FiltrosDocumentos
import cl.duoc.cmqsalud.model.ReglasRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardYRevisionTest {
    private val documentos = listOf(
        Documento(id = 1, nombre = "Contrato demo", tipo = "Contrato", funcionario = "Funcionaria 01",
            estado = "Pendiente", fechaCarga = 1_000L),
        Documento(id = 2, nombre = "Certificado demo", tipo = "Certificado", funcionario = "Funcionario 02",
            estado = "Aprobado", descripcion = "Estudios", fechaCarga = 3_000L),
        Documento(id = 3, nombre = "Permiso demo", tipo = "Permiso", funcionario = "Funcionaria 01",
            estado = "Rechazado", fechaCarga = 2_000L),
        Documento(id = 4, nombre = "Registro borrado", tipo = "Contrato", funcionario = "Funcionario 03",
            estado = "Pendiente", deletedAt = 4_000L)
    )

    @Test fun fechaDeModificacionInicialCoincideConFechaDeCreacion() {
        val documento = Documento(
            id = 10, nombre = "Documento de prueba", tipo = "Certificado",
            funcionario = "Funcionario ficticio", fechaCarga = 1_750_000_000_000L
        )
        assertEquals(documento.fechaCarga, documento.fechaModificacion)
    }

    @Test fun dashboardCuentaPorEstadoSinPapelera() {
        val resumen = FiltrosDocumentos.resumen(documentos.filter { it.deletedAt == null })
        assertEquals(3, resumen.total)
        assertEquals(1, resumen.pendientes)
        assertEquals(1, resumen.aprobados)
        assertEquals(1, resumen.rechazados)
    }

    @Test fun combinaBusquedaConTipoEstadoYOrden() {
        val filtrados = FiltrosDocumentos.filtrar(documentos, "demo", "Certificado", "Aprobado")
        assertEquals(listOf(2), filtrados.map { it.id })
        assertEquals(listOf(2, 3, 1), FiltrosDocumentos.filtrar(documentos, "").map { it.id })
        assertEquals(listOf(1, 3, 2), FiltrosDocumentos.filtrar(documentos, "", recientesPrimero = false).map { it.id })
    }

    @Test fun rechazarExigeMotivoPeroAprobarNo() {
        assertNotNull(ReglasRevision.errorRevision("Rechazado", ""))
        assertNull(ReglasRevision.errorRevision("Rechazado", "Documento incompleto"))
        assertNull(ReglasRevision.errorRevision("Aprobado", ""))
        assertNotNull(ReglasRevision.errorRevision("Pendiente", ""))
    }
}
