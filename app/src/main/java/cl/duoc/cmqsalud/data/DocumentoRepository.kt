package cl.duoc.cmqsalud.data

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.EventoAuditoriaDocumento
import cl.duoc.cmqsalud.model.ReglasRevision
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentoRepository(
    private val database: CMQDatabase,
    private val auth: AuthRepository,
    private val context: Context
) {
    private val dao = database.documentoDao()
    fun observarTodos(): Flow<List<Documento>> = dao.observarTodos()
    fun observarPorId(id: Int): Flow<Documento?> = dao.observarPorId(id)
    fun observarHistorial(id: Int): Flow<List<EventoAuditoriaDocumento>> = dao.observarHistorial(id)
    fun observarPapelera(): Flow<List<Documento>> = dao.observarEliminados().map {
        if (auth.sesionActual()?.rol == RolDemo.ADMINISTRADOR) it else emptyList()
    }
    fun observarAuditoriaGlobal(): Flow<List<EventoAuditoriaDocumento>> = dao.observarAuditoriaGlobal().map {
        if (auth.sesionActual()?.rol == RolDemo.ADMINISTRADOR) it else emptyList()
    }

    private fun actor() = auth.sesionActual() ?: error("Debes iniciar sesión para modificar documentos.")

    suspend fun guardar(documento: Documento): Long = database.withTransaction {
        val responsable = actor()
        // Al crear, la fecha de modificación parte igual a la fecha de creación.
        val nuevoDocumento = documento.copy(fechaModificacion = documento.fechaCarga)
        val id = dao.insertar(nuevoDocumento)
        dao.registrarEvento(EventoAuditoriaDocumento(
            documentoId = id.toInt(), documentoNombre = nuevoDocumento.nombre,
            actorCorreo = responsable.correo, actorNombre = responsable.nombre,
            accion = "CREACIÓN", descripcion = "Documento registrado en el piloto",
            fecha = nuevoDocumento.fechaCarga
        ))
        id
    }

    suspend fun actualizar(documento: Documento): Boolean = database.withTransaction {
        val responsable = actor()
        val anterior = dao.buscarPorId(documento.id) ?: return@withTransaction false
        if (anterior.deletedAt != null) return@withTransaction false
        val cambios = buildList {
            if (anterior.nombre != documento.nombre) add("nombre")
            if (anterior.descripcion != documento.descripcion) add("descripción")
            if (anterior.tipo != documento.tipo) add("tipo")
            if (anterior.funcionario != documento.funcionario) add("funcionario asociado")
            if (anterior.adjuntoUri != documento.adjuntoUri || anterior.adjuntoNombre != documento.adjuntoNombre) add("adjunto")
        }
        if (cambios.isEmpty()) return@withTransaction true
        // Propuesta del piloto: editar un documento ya revisado exige volver a revisarlo.
        // El estado anterior queda conservado en la auditoría.
        val reabrir = anterior.estado != ReglasRevision.PENDIENTE
        val ahora = System.currentTimeMillis()
        val actualizado = documento.copy(
            fechaModificacion = ahora,
            estado = if (reabrir) ReglasRevision.PENDIENTE else anterior.estado,
            observacionRevision = if (reabrir) "" else anterior.observacionRevision,
            revisadoPor = if (reabrir) null else anterior.revisadoPor,
            fechaRevision = if (reabrir) null else anterior.fechaRevision
        )
        val correcto = dao.actualizar(actualizado) > 0
        if (correcto) dao.registrarEvento(EventoAuditoriaDocumento(
            documentoId = documento.id, documentoNombre = documento.nombre,
            actorCorreo = responsable.correo, actorNombre = responsable.nombre,
            accion = "EDICIÓN", descripcion = "Campos: ${cambios.joinToString(", ")}" +
                if (reabrir) ". Estado anterior: ${anterior.estado}; vuelve a Pendiente (regla del piloto)" else "",
            fecha = ahora
        ))
        correcto
    }

    /** La revisión es exclusiva del rol administrador, validado aquí y NO solo en UI. */
    suspend fun revisar(id: Int, nuevoEstado: String, observacion: String): Boolean = database.withTransaction {
        val responsable = actor()
        if (responsable.rol != RolDemo.ADMINISTRADOR) error("Solo una cuenta administradora puede revisar documentos")
        ReglasRevision.errorRevision(nuevoEstado, observacion)?.let { error(it) }
        val anterior = dao.buscarPorId(id) ?: return@withTransaction false
        if (anterior.deletedAt != null) return@withTransaction false
        if (anterior.estado == nuevoEstado && anterior.observacionRevision == observacion.trim())
            return@withTransaction true
        val ahora = System.currentTimeMillis()
        val revisado = anterior.copy(
            estado = nuevoEstado,
            observacionRevision = observacion.trim(),
            revisadoPor = responsable.nombre,
            fechaRevision = ahora,
            fechaModificacion = ahora
        )
        val correcto = dao.actualizar(revisado) > 0
        if (correcto) dao.registrarEvento(EventoAuditoriaDocumento(
            documentoId = id, documentoNombre = anterior.nombre,
            actorCorreo = responsable.correo, actorNombre = responsable.nombre,
            accion = "REVISIÓN", descripcion = "Estado: ${anterior.estado} → $nuevoEstado" +
                if (observacion.isNotBlank()) ". Observación: ${observacion.trim()}" else ". Sin observaciones",
            fecha = ahora
        ))
        correcto
    }

    /** Eliminación lógica: el contenido y la bitácora se conservan para restauración. */
    suspend fun eliminar(documento: Documento) = database.withTransaction {
        val responsable = actor()
        val anterior = dao.buscarPorId(documento.id) ?: error("Documento no encontrado")
        if (anterior.deletedAt != null) error("Documento ya eliminado")
        if (dao.actualizar(anterior.copy(deletedAt = System.currentTimeMillis())) == 0)
            error("No se pudo eliminar el documento")
        dao.registrarEvento(EventoAuditoriaDocumento(
            documentoId = anterior.id, documentoNombre = anterior.nombre,
            actorCorreo = responsable.correo, actorNombre = responsable.nombre,
            accion = "ELIMINACIÓN", descripcion = "Movido a la papelera de recuperación"
        ))
    }

    suspend fun restaurar(id: Int): Boolean = database.withTransaction {
        val responsable = actor()
        if (responsable.rol != RolDemo.ADMINISTRADOR) error("Acción exclusiva de administración")
        val anterior = dao.buscarPorId(id) ?: return@withTransaction false
        if (anterior.deletedAt == null || PapeleraRetencion.vencido(anterior.deletedAt))
            return@withTransaction false
        val correcto = dao.actualizar(anterior.copy(deletedAt = null)) > 0
        if (correcto) dao.registrarEvento(EventoAuditoriaDocumento(
            documentoId = anterior.id, documentoNombre = anterior.nombre,
            actorCorreo = responsable.correo, actorNombre = responsable.nombre,
            accion = "RESTAURACIÓN", descripcion = "Documento recuperado desde la papelera"
        ))
        correcto
    }

    /**
     * CRUD / Delete permanente para pruebas. Solo documentos ya enviados a papelera,
     * y solo por administrador. El historial permanece en Room.
     * No elimina URIs externas del selector del sistema.
     */
    suspend fun borrarDefinitivamente(id: Int): Boolean {
        val resultado: Pair<Boolean, String?> = database.withTransaction {
            val responsable = actor()
            if (responsable.rol != RolDemo.ADMINISTRADOR) {
                error("Solo un administrador puede eliminar definitivamente")
            }
            val anterior = dao.buscarPorId(id) ?: return@withTransaction false to null
            if (anterior.deletedAt == null) return@withTransaction false to null
            if (dao.eliminarDefinitivamente(id) == 0) return@withTransaction false to null
            dao.registrarEvento(EventoAuditoriaDocumento(
                documentoId = anterior.id,
                documentoNombre = anterior.nombre,
                actorCorreo = responsable.correo,
                actorNombre = responsable.nombre,
                accion = "PURGA MANUAL",
                descripcion = "Eliminación definitiva desde papelera; conservada la auditoría"
            ))
            true to anterior.adjuntoUri
        }
        if (resultado.first) {
            resultado.second?.let { uri ->
                // Solo eliminamos la foto privada si ningún otro documento la necesita.
                if (dao.contarReferenciasAdjunto(uri) == 0) {
                    AdjuntoUtils.borrarFotoInterna(context, uri)
                }
            }
        }
        return resultado.first
    }

}
