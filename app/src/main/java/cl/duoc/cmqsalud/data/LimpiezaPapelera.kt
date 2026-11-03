package cl.duoc.cmqsalud.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento
import java.util.concurrent.TimeUnit

/** Elimina físicamente documentos tras 20 días. La auditoría de purga se conserva. */
object LimpiezaPapelera {
    suspend fun ejecutar(context: Context): Int {
        val db = CMQDatabase.getInstance(context)
        val dao = db.documentoDao()
        val limite = System.currentTimeMillis() - PapeleraRetencion.MILISEGUNDOS
        val archivosCandidatos = db.withTransaction {
            val vencidos = dao.buscarExpirados(limite)
            val adjuntos = mutableListOf<String>()
            vencidos.forEach { doc ->
                if (dao.purgarPorId(doc.id, limite) > 0) {
                    doc.adjuntoUri?.let(adjuntos::add)
                    dao.registrarEvento(
                        EventoAuditoriaDocumento(
                            documentoId = doc.id,
                            documentoNombre = doc.nombre,
                            actorCorreo = "sistema@local",
                            actorNombre = "Sistema",
                            accion = "PURGA AUTOMÁTICA",
                            descripcion = "Eliminación definitiva tras 20 días en papelera"
                        )
                    )
                }
            }
            Pair(vencidos.size, adjuntos.distinct())
        }
        // Verificación posterior: no borrar una fotografía compartida con otro registro.
        archivosCandidatos.second.forEach { uri ->
            if (dao.contarReferenciasAdjunto(uri) == 0) {
                AdjuntoUtils.borrarFotoInterna(context, uri)
            }
        }
        // Recupera fotos huérfanas muy antiguas, por ejemplo si la app se cerró
        // entre borrar el registro de Room y eliminar físicamente su fotografía.
        val referenciados = dao.todasUrisAdjuntas().toSet()
        val limiteHuerfanos = System.currentTimeMillis() - (PapeleraRetencion.MILISEGUNDOS + 24L * 60L * 60L * 1000L)
        context.filesDir.listFiles()?.forEach { foto ->
            if (foto.isFile && foto.name.startsWith("documento_") && foto.extension.equals("jpg", true) &&
                foto.lastModified() < limiteHuerfanos && Uri.fromFile(foto).toString() !in referenciados) {
                AdjuntoUtils.borrarFotoInterna(context, Uri.fromFile(foto).toString())
            }
        }
        return archivosCandidatos.first
    }

    fun programar(context: Context) {
        // WorkManager es aproximado: no ejecuta exactamente a la hora 20 días.
        val solicitud = PeriodicWorkRequestBuilder<LimpiezaPapeleraWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "limpieza_papelera_20_dias", ExistingPeriodicWorkPolicy.KEEP, solicitud
        )
    }
}

class LimpiezaPapeleraWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        LimpiezaPapelera.ejecutar(applicationContext)
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
