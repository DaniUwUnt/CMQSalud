package cl.duoc.cmqsalud

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.cmqsalud.data.CMQDatabase
import cl.duoc.cmqsalud.model.Documento
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba instrumental con Room real (SQLite en memoria): no modifica datos del piloto. */
@RunWith(AndroidJUnit4::class)
class DocumentoDaoCrudTest {
    private lateinit var db: CMQDatabase

    @Before fun preparar() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), CMQDatabase::class.java
        ).build()
    }

    @After fun cerrar() = db.close()

    @Test fun createReadUpdateDelete() = runBlocking {
        val dao = db.documentoDao()
        val id = dao.insertar(Documento(nombre = "Contrato ejemplo", tipo = "Contrato", funcionario = "Funcionario ficticio")).toInt()
        assertTrue(id > 0)
        assertEquals("Contrato ejemplo", dao.buscarPorId(id)?.nombre)
        assertEquals(1, dao.observarTodos().first().size)

        val original = dao.buscarPorId(id)!!
        assertEquals(1, dao.actualizar(original.copy(nombre = "Contrato editado")))
        assertEquals("Contrato editado", dao.buscarPorId(id)?.nombre)

        assertEquals(1, dao.actualizar(original.copy(nombre = "Contrato editado", deletedAt = System.currentTimeMillis())))
        assertTrue(dao.observarTodos().first().isEmpty())
        assertEquals(1, dao.observarEliminados().first().size)
        assertEquals(1, dao.eliminarDefinitivamente(id))
        assertNull(dao.buscarPorId(id))
    }
}
