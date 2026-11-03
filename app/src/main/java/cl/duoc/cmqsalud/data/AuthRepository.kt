package cl.duoc.cmqsalud.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import cl.duoc.cmqsalud.model.NumeroTelefonoRegistro
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class RolDemo { ADMINISTRADOR, FUNCIONARIO }

data class UsuarioDemo(val nombre: String, val correo: String, val rol: RolDemo)

data class DatosRegistroUsuario(
    val nombres: String, val apellidoPaterno: String, val apellidoMaterno: String,
    val rut: String, val correo: String, val celular: String, val fijo: String,
    val region: String, val comuna: String
)


data class PerfilUsuarioDemo(
    val nombre: String, val apellidos: String, val rut: String, val correo: String,
    val celular: String, val fijo: String, val region: String,
    val comuna: String, val rol: RolDemo
)
data class CuentaAdministrable(
    val nombre: String, val correo: String, val rol: RolDemo, val habilitada: Boolean,
    val rut: String = "", val celular: String = ""
)
data class EventoAdmin(val fecha: Long, val descripcion: String)

/**
 * Sólo para el MVP académico, sin datos personales ni permisos institucionales.
 * Los roles son locales y NO constituyen autorización segura de producción.
 */
class AuthRepository(context: Context) {
    companion object {
        const val ERROR_CELULAR_DUPLICADO = "Este número de celular ya está registrado en otra cuenta."
        const val ERROR_FIJO_DUPLICADO = "Este número de teléfono fijo ya está registrado en otra cuenta."
    }

    private val preferences = context.applicationContext.getSharedPreferences(
        "cmq_salud_cuentas_demo_v1", Context.MODE_PRIVATE
    )

    init {
        depurarCamposDomicilioAnteriores()
        asegurarAdministradorInicial()
    }

    /** Cambio de modelo de perfil: elimina calles y números ya innecesarios. */
    @Synchronized
    private fun depurarCamposDomicilioAnteriores() {
        if (preferences.getBoolean("campos_ubicacion_v2", false)) return
        val cuentas = cargarCuentas()
        for (i in 0 until cuentas.length()) {
            val cuenta = cuentas.optJSONObject(i) ?: continue
            cuenta.remove("direccion")
            cuenta.remove("numeroDomicilio")
        }
        // Si falla el commit, se vuelve a intentar la próxima vez que abra la app.
        preferences.edit().putString("cuentas", cuentas.toString())
            .putBoolean("campos_ubicacion_v2", true).commit()
    }

    @Synchronized
    fun sesionActual(): UsuarioDemo? {
        val correo = preferences.getString("sesion", null) ?: return null
        val cuenta = obtenerCuenta(correo) ?: return null
        if (!cuenta.optBoolean("habilitada", true)) return null
        return usuarioDe(cuenta)
    }

    @Synchronized
    fun perfilActual(): PerfilUsuarioDemo? {
        val sesion = sesionActual() ?: return null
        val cuenta = obtenerCuenta(sesion.correo) ?: return null
        val apellidos = listOf(cuenta.optString("apellidoPaterno"), cuenta.optString("apellidoMaterno"))
            .filter(String::isNotBlank).joinToString(" ")
        return PerfilUsuarioDemo(
            nombre = cuenta.optString("nombres").ifBlank { cuenta.optString("nombre") },
            apellidos = apellidos, rut = cuenta.optString("rut"),
            correo = sesion.correo, celular = cuenta.optString("celular"),
            fijo = cuenta.optString("fijo"), region = cuenta.optString("region"),
            comuna = cuenta.optString("comuna"), rol = sesion.rol
        )
    }

    /** Completa o modifica únicamente la ubicación declarada por el usuario activo. */
    @Synchronized
    fun actualizarUbicacion(region: String, comuna: String): String? {
        val usuario = sesionActual() ?: return "La sesión expiró. Ingresa nuevamente."
        if (!cl.duoc.cmqsalud.model.RegionesComunasChile.comunaValida(region, comuna)) {
            return "Selecciona una región y una comuna que correspondan."
        }
        val cuentas = cargarCuentas()
        val cuenta = buscarEn(cuentas, usuario.correo) ?: return "Cuenta no encontrada."
        cuenta.put("region", region)
        cuenta.put("comuna", comuna)
        return if (registrarCambio(cuentas, "${usuario.correo} actualizó su ubicación de perfil")) null
        else "No fue posible guardar la región y comuna."
    }

    /** Primera cuenta = admin del piloto. Con cuentas anteriores, la primera existente se migra. */
    @Synchronized
    private fun asegurarAdministradorInicial() {
        val cuentas = cargarCuentas()
        if (cuentas.length() == 0) return
        val tieneAdmin = (0 until cuentas.length()).any {
            rolDe(cuentas.optJSONObject(it)) == RolDemo.ADMINISTRADOR
        }
        if (!tieneAdmin) {
            val primera = cuentas.optJSONObject(0) ?: return
            primera.put("rol", RolDemo.ADMINISTRADOR.name)
            registrarCambio(cuentas, "Administrador inicial asignado a ${primera.optString("correo")}")
        }
    }

    /** Devuelve null cuando se completa la operación, o un mensaje si falla. */
    @Synchronized
    fun registrar(datos: DatosRegistroUsuario, clave: String): String? {
        val normalized = normalizar(datos.correo)
        val cuentasPrevias = cargarCuentas()
        if (buscarEn(cuentasPrevias, normalized) != null) return "Este correo ya está registrado en el piloto."
        val rutNuevo = datos.rut.uppercase(Locale.ROOT).filter { it.isDigit() || it == 'K' }
        if ((0 until cuentasPrevias.length()).any {
                cuentasPrevias.optJSONObject(it)?.optString("rut")?.let { guardado ->
                    guardado.uppercase(Locale.ROOT).filter { ch -> ch.isDigit() || ch == 'K' } == rutNuevo
                } == true
            }) return "Este RUT ficticio ya fue registrado."

        // Se valida sobre TODAS las cuentas guardadas, incluso las deshabilitadas.
        // No basta con comprobar el campo del formulario: el repositorio también
        // debe rechazar el registro antes de escribir datos o crear una sesión.
        val telefonosRegistrados = (0 until cuentasPrevias.length()).mapNotNull { i ->
            cuentasPrevias.optJSONObject(i)?.let { cuenta ->
                cuenta.optString("celular") to cuenta.optString("fijo")
            }
        }
        when (NumeroTelefonoRegistro.buscarDuplicado(datos.celular, datos.fijo, telefonosRegistrados)) {
            NumeroTelefonoRegistro.Campo.CELULAR -> return ERROR_CELULAR_DUPLICADO
            NumeroTelefonoRegistro.Campo.FIJO -> return ERROR_FIJO_DUPLICADO
            null -> Unit
        }
        return try {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val digest = hash(clave, salt)
            val cuentas = cuentasPrevias
            val primerUsuario = cuentas.length() == 0
            val rol = if (primerUsuario) RolDemo.ADMINISTRADOR else RolDemo.FUNCIONARIO
            cuentas.put(JSONObject().apply {
                put("nombre", "${datos.nombres.trim()} ${datos.apellidoPaterno.trim()}".trim())
                put("nombres", datos.nombres.trim())
                put("apellidoPaterno", datos.apellidoPaterno.trim())
                put("apellidoMaterno", datos.apellidoMaterno.trim())
                put("rut", rutNuevo)
                put("celular", datos.celular.trim())
                put("fijo", datos.fijo.trim())
                put("region", datos.region.trim())
                put("comuna", datos.comuna.trim())
                put("correo", normalized)
                put("salt", Base64.getEncoder().encodeToString(salt))
                put("claveHash", Base64.getEncoder().encodeToString(digest))
                put("rol", rol.name)
                put("habilitada", true)
            })
            val audit = cargarAuditoria().apply {
                put(JSONObject().put("fecha", System.currentTimeMillis())
                    .put("descripcion", "Cuenta creada: $normalized (${rol.name})"))
            }
            val guardado = preferences.edit()
                .putString("cuentas", cuentas.toString())
                .putString("sesion", normalized)
                .putString("auditoria", ultimosEventos(audit).toString())
                .commit()
            if (guardado) null else "No se pudieron guardar los datos de acceso."
        } catch (_: Exception) {
            "No se pudo completar el registro. Inténtalo nuevamente."
        }
    }

    @Synchronized
    fun iniciarSesion(correo: String, clave: String): String? {
        val cuenta = obtenerCuenta(normalizar(correo)) ?: return "Correo o contraseña incorrectos."
        if (!cuenta.optBoolean("habilitada", true)) return "Cuenta deshabilitada. Contacta al administrador."
        return try {
            val salt = Base64.getDecoder().decode(cuenta.getString("salt"))
            val esperado = Base64.getDecoder().decode(cuenta.getString("claveHash"))
            if (!MessageDigest.isEqual(esperado, hash(clave, salt))) {
                "Correo o contraseña incorrectos."
            } else if (preferences.edit().putString("sesion", normalizar(correo)).commit()) {
                null
            } else {
                "No se pudo guardar la sesión de demostración."
            }
        } catch (_: Exception) {
            "No fue posible acceder. Inténtalo nuevamente."
        }
    }

    fun cerrarSesion() { preferences.edit().remove("sesion").apply() }

    @Synchronized
    fun listarCuentas(solicitante: String): List<CuentaAdministrable> {
        if (!esAdministradorActivo(solicitante)) return emptyList()
        val cuentas = cargarCuentas()
        return (0 until cuentas.length()).mapNotNull { cuentas.optJSONObject(it) }.map { cuenta ->
            CuentaAdministrable(
                cuenta.optString("nombre"), cuenta.optString("correo"),
                rolDe(cuenta), cuenta.optBoolean("habilitada", true),
                cuenta.optString("rut"), cuenta.optString("celular")
            )
        }
    }

    @Synchronized
    fun listarAuditoria(solicitante: String): List<EventoAdmin> {
        if (!esAdministradorActivo(solicitante)) return emptyList()
        val eventos = cargarAuditoria()
        return (0 until eventos.length()).mapNotNull { eventos.optJSONObject(it) }
            .map { EventoAdmin(it.optLong("fecha"), it.optString("descripcion")) }
            .asReversed().take(15)
    }

    @Synchronized
    fun cambiarEstado(solicitante: String, objetivo: String, habilitar: Boolean): String? {
        val error = validarGestion(solicitante, objetivo)
        if (error != null) return error
        val cuentas = cargarCuentas()
        val cuenta = buscarEn(cuentas, objetivo) ?: return "La cuenta ya no existe."
        if (!habilitar && rolDe(cuenta) == RolDemo.ADMINISTRADOR && numeroAdministradores(cuentas) <= 1) {
            return "No puedes deshabilitar al último administrador."
        }
        cuenta.put("habilitada", habilitar)
        return if (registrarCambio(cuentas, "${solicitante}: ${if (habilitar) "reactivó" else "deshabilitó"} ${normalizar(objetivo)}")) null
        else "No se pudo actualizar la cuenta."
    }

    @Synchronized
    fun cambiarRol(solicitante: String, objetivo: String, rol: RolDemo): String? {
        val error = validarGestion(solicitante, objetivo)
        if (error != null) return error
        val cuentas = cargarCuentas()
        val cuenta = buscarEn(cuentas, objetivo) ?: return "La cuenta ya no existe."
        if (rolDe(cuenta) == RolDemo.ADMINISTRADOR && rol != RolDemo.ADMINISTRADOR && numeroAdministradores(cuentas) <= 1) {
            return "Debe quedar al menos una cuenta administradora."
        }
        cuenta.put("rol", rol.name)
        return if (registrarCambio(cuentas, "$solicitante cambió a ${normalizar(objetivo)} al rol ${rol.name}")) null
        else "No se pudo cambiar el rol."
    }

    @Synchronized
    fun eliminarCuenta(solicitante: String, objetivo: String): String? {
        val error = validarGestion(solicitante, objetivo)
        if (error != null) return error
        val cuentas = cargarCuentas()
        val cuenta = buscarEn(cuentas, objetivo) ?: return "La cuenta ya no existe."
        if (rolDe(cuenta) == RolDemo.ADMINISTRADOR && numeroAdministradores(cuentas) <= 1) {
            return "No puedes eliminar al último administrador."
        }
        val restantes = JSONArray()
        for (i in 0 until cuentas.length()) {
            val item = cuentas.optJSONObject(i) ?: continue
            if (item.optString("correo") != normalizar(objetivo)) restantes.put(item)
        }
        return if (registrarCambio(restantes, "$solicitante eliminó la cuenta ${normalizar(objetivo)}")) null
        else "No se pudo eliminar la cuenta."
    }

    private fun validarGestion(solicitante: String, objetivo: String): String? = when {
        !esAdministradorActivo(solicitante) -> "Acceso denegado: se requiere rol administrador."
        normalizar(solicitante) == normalizar(objetivo) -> "No puedes modificar tu propia cuenta desde este panel."
        else -> null
    }

    private fun esAdministradorActivo(correo: String): Boolean {
        val actual = preferences.getString("sesion", null)
        if (actual != normalizar(correo)) return false
        val cuenta = obtenerCuenta(correo) ?: return false
        return cuenta.optBoolean("habilitada", true) && rolDe(cuenta) == RolDemo.ADMINISTRADOR
    }

    private fun numeroAdministradores(cuentas: JSONArray): Int =
        (0 until cuentas.length()).count { rolDe(cuentas.optJSONObject(it)) == RolDemo.ADMINISTRADOR }

    private fun registrarCambio(cuentas: JSONArray, descripcion: String): Boolean {
        val eventos = cargarAuditoria()
        eventos.put(JSONObject().put("fecha", System.currentTimeMillis()).put("descripcion", descripcion))
        return preferences.edit()
            .putString("cuentas", cuentas.toString())
            .putString("auditoria", ultimosEventos(eventos).toString()).commit()
    }

    private fun ultimosEventos(eventos: JSONArray): JSONArray {
        val resultado = JSONArray()
        for (i in maxOf(0, eventos.length() - 60) until eventos.length()) {
            resultado.put(eventos.optJSONObject(i))
        }
        return resultado
    }

    private fun cargarAuditoria(): JSONArray = try {
        JSONArray(preferences.getString("auditoria", "[]") ?: "[]")
    } catch (_: Exception) { JSONArray() }

    private fun normalizar(correo: String) = correo.trim().lowercase(Locale.ROOT)
    private fun cargarCuentas(): JSONArray = try {
        JSONArray(preferences.getString("cuentas", "[]") ?: "[]")
    } catch (_: Exception) { JSONArray() }

    private fun obtenerCuenta(correo: String): JSONObject? = buscarEn(cargarCuentas(), correo)

    private fun buscarEn(cuentas: JSONArray, correo: String): JSONObject? {
        for (i in 0 until cuentas.length()) {
            val cuenta = cuentas.optJSONObject(i) ?: continue
            if (cuenta.optString("correo") == normalizar(correo)) return cuenta
        }
        return null
    }

    private fun usuarioDe(cuenta: JSONObject) = UsuarioDemo(
        cuenta.optString("nombre"), cuenta.optString("correo"), rolDe(cuenta)
    )

    private fun rolDe(cuenta: JSONObject?): RolDemo =
        if (cuenta?.optString("rol") == RolDemo.ADMINISTRADOR.name) RolDemo.ADMINISTRADOR
        else RolDemo.FUNCIONARIO

    private fun hash(clave: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), salt, 120_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally { spec.clearPassword() }
    }
}
