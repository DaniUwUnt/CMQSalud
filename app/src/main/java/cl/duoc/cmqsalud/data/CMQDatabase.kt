package cl.duoc.cmqsalud.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento

@Database(entities = [Documento::class, EventoAuditoriaDocumento::class], version = 5, exportSchema = false)
abstract class CMQDatabase : RoomDatabase() {
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile private var INSTANCE: CMQDatabase? = null

        /** Conserva documentos existentes: nunca usar destructiveMigration. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN deletedAt INTEGER")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `auditoria_documentos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `documentoId` INTEGER NOT NULL,
                    `documentoNombre` TEXT NOT NULL,
                    `actorCorreo` TEXT NOT NULL,
                    `actorNombre` TEXT NOT NULL,
                    `accion` TEXT NOT NULL,
                    `descripcion` TEXT NOT NULL,
                    `fecha` INTEGER NOT NULL
                )""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_auditoria_documentos_documentoId` ON `auditoria_documentos` (`documentoId`)")
            }
        }

        /** La descripción es opcional. Migración no destructiva; los anteriores quedan con texto vacío. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Flujo de revisión demostrativo: conserva documentos de versiones previas. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN observacionRevision TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE documentos ADD COLUMN revisadoPor TEXT")
                db.execSQL("ALTER TABLE documentos ADD COLUMN fechaRevision INTEGER")
            }
        }

        /** Agrega la última modificación sin borrar documentos existentes; inicializa con su fecha de creación. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN fechaModificacion INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""UPDATE documentos
                    SET fechaModificacion = MAX(fechaCarga, COALESCE(
                        (SELECT MAX(fecha) FROM auditoria_documentos
                         WHERE auditoria_documentos.documentoId = documentos.id
                           AND auditoria_documentos.accion IN ('EDICIÓN', 'REVISIÓN')),
                        fechaCarga
                    ))""")
            }
        }

        fun getInstance(context: Context): CMQDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, CMQDatabase::class.java, "cmq_salud.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
        }
    }
}
