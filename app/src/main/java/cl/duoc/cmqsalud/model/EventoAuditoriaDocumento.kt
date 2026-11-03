package cl.duoc.cmqsalud.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Bitácora local de demostración. Sin garantías de inmutabilidad institucional. */
@Entity(tableName = "auditoria_documentos", indices = [Index("documentoId")])
data class EventoAuditoriaDocumento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentoId: Int,
    val documentoNombre: String,
    val actorCorreo: String,
    val actorNombre: String,
    val accion: String,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis()
)
