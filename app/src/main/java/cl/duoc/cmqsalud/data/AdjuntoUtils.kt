package cl.duoc.cmqsalud.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

/** Adjuntos ficticios del MVP: las fotos se guardan en el espacio privado de la app. */
object AdjuntoUtils {
    fun crearArchivoFoto(context: Context): File = File(
        context.filesDir,
        "documento_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
    ).also { it.createNewFile() }

    fun uriCamara(context: Context, archivo: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )

    fun nombre(context: Context, uri: Uri): String {
        if (uri.scheme == "file") return File(uri.path.orEmpty()).name
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index)
                else uri.lastPathSegment ?: "adjunto"
            } ?: (uri.lastPathSegment ?: "adjunto")
        } catch (_: Exception) {
            uri.lastPathSegment ?: "adjunto"
        }
    }

    private fun uriCompartible(context: Context, uri: Uri): Uri =
        if (uri.scheme == "file") {
            val archivo = File(uri.path ?: throw FileNotFoundException("Ruta vacía"))
            // No exponer archivos ajenos: solo fotos existentes dentro de filesDir.
            require(archivo.exists() && archivo.canonicalFile.parentFile == context.filesDir.canonicalFile) {
                "El adjunto no está disponible en el almacenamiento de la aplicación"
            }
            uriCamara(context, archivo)
        } else uri

    fun abrir(context: Context, uriGuardada: String, nombre: String?): String? {
        return try {
            val original = Uri.parse(uriGuardada)
            val compartible = uriCompartible(context, original)
            val extension = (nombre ?: original.lastPathSegment.orEmpty())
                .substringAfterLast('.', "").lowercase()
            val mime = context.contentResolver.getType(compartible)
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: "application/octet-stream"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(compartible, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir adjunto"))
            null
        } catch (_: ActivityNotFoundException) {
            "No hay una aplicación instalada para abrir este tipo de archivo."
        } catch (_: Exception) {
            "No se pudo abrir el adjunto. Comprueba que el archivo todavía exista."
        }
    }

    /** Nunca toca documentos externos: solo fotos propias dentro de filesDir. */
    fun borrarFotoInterna(context: Context, uriGuardada: String): Boolean {
        return try {
            val uri = Uri.parse(uriGuardada)
            if (uri.scheme != "file") return false
            val archivo = File(uri.path ?: return false).canonicalFile
            val carpeta = context.filesDir.canonicalFile
            if (archivo.parentFile != carpeta ||
                !archivo.name.startsWith("documento_") ||
                !archivo.name.endsWith(".jpg", ignoreCase = true)) return false
            !archivo.exists() || archivo.delete()
        } catch (_: Exception) {
            false
        }
    }

    /** Decodifica una miniatura para no cargar fotografías grandes a tamaño completo. */
    fun miniatura(context: Context, uriGuardada: String, ladoMaximo: Int = 900): Bitmap? {
        return try {
            val uri = Uri.parse(uriGuardada)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            var muestra = 1
            while (bounds.outWidth / muestra > ladoMaximo || bounds.outHeight / muestra > ladoMaximo) {
                muestra *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = muestra }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }
}
