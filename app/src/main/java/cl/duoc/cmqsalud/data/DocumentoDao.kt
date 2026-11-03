package cl.duoc.cmqsalud.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documentos WHERE deletedAt IS NULL ORDER BY id DESC")
    fun observarTodos(): Flow<List<Documento>>

    @Query("SELECT * FROM documentos WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observarEliminados(): Flow<List<Documento>>

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Documento?>

    @Query("SELECT * FROM documentos WHERE deletedAt IS NOT NULL AND deletedAt <= :limite")
    suspend fun buscarExpirados(limite: Long): List<Documento>

    @Query("DELETE FROM documentos WHERE id = :id AND deletedAt IS NOT NULL AND deletedAt <= :limite")
    suspend fun purgarPorId(id: Int, limite: Long): Int

    /** Borrado físico bajo autorización del Repository, solo desde papelera. */
    @Query("DELETE FROM documentos WHERE id = :id AND deletedAt IS NOT NULL")
    suspend fun eliminarDefinitivamente(id: Int): Int

    @Query("SELECT COUNT(*) FROM documentos WHERE adjuntoUri = :uri")
    suspend fun contarReferenciasAdjunto(uri: String): Int

    @Query("SELECT adjuntoUri FROM documentos WHERE adjuntoUri IS NOT NULL")
    suspend fun todasUrisAdjuntas(): List<String>

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Documento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: Documento): Long

    @Update
    suspend fun actualizar(documento: Documento): Int

    @Insert
    suspend fun registrarEvento(evento: EventoAuditoriaDocumento)

    @Query("SELECT * FROM auditoria_documentos WHERE documentoId = :id ORDER BY fecha DESC, id DESC")
    fun observarHistorial(id: Int): Flow<List<EventoAuditoriaDocumento>>

    @Query("SELECT * FROM auditoria_documentos ORDER BY fecha DESC, id DESC")
    fun observarAuditoriaGlobal(): Flow<List<EventoAuditoriaDocumento>>
}
