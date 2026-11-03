package cl.duoc.cmqsalud.model

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "documentos")
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    @ColumnInfo(defaultValue = "''")
    val descripcion: String = "",
    val tipo: String,
    val funcionario: String,
    val estado: String = "Pendiente",
    val adjuntoUri: String? = null,
    val adjuntoNombre: String? = null,
    val fechaCarga: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    val fechaModificacion: Long = fechaCarga,
    val deletedAt: Long? = null,
    @ColumnInfo(defaultValue = "''")
    val observacionRevision: String = "",
    val revisadoPor: String? = null,
    val fechaRevision: Long? = null
)
