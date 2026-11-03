package cl.duoc.cmqsalud.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.cmqsalud.R
import cl.duoc.cmqsalud.model.RegionesComunasChile
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.data.DatosRegistroUsuario
import cl.duoc.cmqsalud.model.ValidacionRut
import cl.duoc.cmqsalud.ui.theme.CMQAzulProfundo
import cl.duoc.cmqsalud.ui.theme.CMQTurquesa
import cl.duoc.cmqsalud.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onIrRegistro: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var mostrarDialogoRecuperacion by rememberSaveable { mutableStateOf(false) }

    AuthScaffold(titulo = "Bienvenido/a", subtitulo = "Ingresa a tu espacio documental de prueba") {
        CampoAcceso(
            valor = correo,
            onCambio = { correo = it; viewModel.limpiarErrores() },
            titulo = "Correo electrónico",
            error = estado.correoError,
            tipo = KeyboardType.Email
        )
        CampoAcceso(
            valor = clave,
            onCambio = { clave = it; viewModel.limpiarErrores() },
            titulo = "Contraseña",
            error = estado.claveError,
            contrasena = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { mostrarDialogoRecuperacion = true }) {
                Text("¿Has olvidado tu contraseña?")
            }
        }
        estado.mensaje?.let { TextoError(it) }
        Button(
            onClick = { viewModel.iniciarSesion(correo, clave) },
            enabled = !estado.procesando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (estado.procesando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            else Text("Iniciar sesión")
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ClaveUnicaPropuesta()
        Text("¿Aún no tienes cuenta de prueba?", style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(
            onClick = { viewModel.limpiarErrores(); onIrRegistro() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crear cuenta") }
    }

    if (mostrarDialogoRecuperacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperacion = false },
            title = { Text("Recuperar contraseña") },
            text = {
                Text(
                    "Esta opción es una propuesta para futuras versiones. En este piloto todavía no se envían correos ni se cambia la contraseña. Para habilitarla se necesita un servicio de recuperación y verificación de la cuenta."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoRecuperacion = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun RegistroUsuarioScreen(viewModel: AuthViewModel, onVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidoPaterno by rememberSaveable { mutableStateOf("") }
    var apellidoMaterno by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var celular by rememberSaveable { mutableStateOf("") }
    var fijo by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var comuna by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    val errores = estado.erroresRegistro
    val limpiar = { viewModel.limpiarErrores() }

    AuthScaffold(titulo = "Registro de usuario", subtitulo = "Datos ficticios · Piloto académico") {
        Text("Identificación", style = MaterialTheme.typography.titleMedium)
        CampoAcceso(nombres, { nombres = it; limpiar() }, "Nombre(s) *", errores["nombres"])
        CampoAcceso(apellidoPaterno, { apellidoPaterno = it; limpiar() }, "Apellido paterno *", errores["apellidoPaterno"])
        CampoAcceso(apellidoMaterno, { apellidoMaterno = it; limpiar() }, "Apellido materno *", errores["apellidoMaterno"])
        CampoAcceso(
            valor = rut,
            onCambio = { rut = it; limpiar() },
            titulo = "RUT *",
            error = errores["rut"],
            modifier = Modifier.onFocusChanged { foco ->
                if (!foco.isFocused) rut = ValidacionRut.formateado(rut)
            }
        )
        Text("Formato automático al salir del campo. Ej.: 12.345.678-5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Contacto y ubicación", style = MaterialTheme.typography.titleMedium)
        CampoAcceso(correo, { correo = it; limpiar() }, "Correo electrónico *", errores["correo"], tipo = KeyboardType.Email)
        CampoCelularChile(
            digitos = celular,
            onCambio = { valor ->
                celular = valor.filter(Char::isDigit).take(8)
                limpiar()
            },
            titulo = "Celular (8 dígitos) *",
            error = errores["celular"]
        )
        CampoAcceso(fijo, { fijo = it; limpiar() }, "Teléfono fijo (opcional, +56...)", errores["fijo"], tipo = KeyboardType.Phone)
        SelectorUbicacion(
            valor = region,
            titulo = "Región *",
            opciones = RegionesComunasChile.regiones,
            error = errores["region"],
            onSeleccion = { seleccion ->
                region = seleccion
                comuna = ""  // No conservar comuna al cambiar de región.
                limpiar()
            }
        )
        SelectorUbicacion(
            valor = comuna,
            titulo = "Comuna *",
            opciones = RegionesComunasChile.comunasDe(region),
            error = errores["comuna"],
            habilitado = region.isNotBlank(),
            onSeleccion = { comuna = it; limpiar() }
        )
        Text("Acceso y rol", style = MaterialTheme.typography.titleMedium)
        CampoAcceso(clave, { clave = it; limpiar() }, "Contraseña *", errores["clave"], contrasena = true)
        CampoAcceso(confirmacion, { confirmacion = it; limpiar() }, "Confirmar contraseña *", errores["confirmacion"], contrasena = true)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Rol: Funcionario (asignación automática)", style = MaterialTheme.typography.titleSmall)
                Text("Solo un administrador puede cambiar los roles. La primera cuenta registrada en un piloto nuevo recibe el rol administrador inicial.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("* Obligatorio. Solo datos de prueba: no registres RUT ni teléfonos reales.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        estado.mensaje?.let { TextoError(it) }
        Button(
            onClick = {
                viewModel.registrar(
                    DatosRegistroUsuario(nombres, apellidoPaterno, apellidoMaterno, rut, correo,
                        "+569$celular", fijo, region, comuna), clave, confirmacion
                )
            },
            enabled = !estado.procesando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (estado.procesando) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            else Text("Crear cuenta y entrar")
        }
        TextButton(onClick = { limpiar(); onVolver() }) {
            Text("Ya tengo cuenta · Volver al ingreso")
        }
    }
}

@Composable
private fun AuthScaffold(
    titulo: String,
    subtitulo: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(CMQAzulProfundo, CMQTurquesa)))
                .padding(horizontal = 20.dp)
                // Baja el contenido de la cabecera para despejar la zona de notificaciones y cámara.
                .padding(top = 32.dp, bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_gobierno_chile),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(62.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CMQ SALUD", color = Color.White,
                        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Gestión documental móvil", color = Color.White,
                        style = MaterialTheme.typography.titleMedium)
                    Text("PILOTO ACADÉMICO  •  DATOS FICTICIOS", color = Color(0xFFDEFAF6),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(titulo, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                content()
            }
        }
        Text(
            "Prototipo sin conexión institucional. No ingresar datos ni claves reales.",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
    }
}

/** Selector accesible y con búsqueda local. No requiere permisos de ubicación. */
@Composable
fun SelectorUbicacion(
    valor: String,
    titulo: String,
    opciones: List<String>,
    error: String?,
    habilitado: Boolean = true,
    onSeleccion: (String) -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    OutlinedButton(
        onClick = { busqueda = ""; abierto = true },
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("$titulo: ${valor.ifBlank { if (habilitado) "Seleccionar" else "Elige primero una región" }}")
    }
    if (error != null) TextoError(error)
    if (abierto) {
        AlertDialog(
            onDismissRequest = { abierto = false },
            title = { Text("Seleccionar $titulo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = { Text("Buscar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val visibles = opciones.filter { it.contains(busqueda.trim(), ignoreCase = true) }
                    LazyColumn(modifier = Modifier.heightIn(max = 370.dp)) {
                        items(visibles, key = { it }) { opcion ->
                            TextButton(
                                onClick = { onSeleccion(opcion); abierto = false },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(opcion) }
                        }
                        if (visibles.isEmpty()) item { Text("No se encontraron coincidencias.") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CampoCelularChile(
    digitos: String,
    onCambio: (String) -> Unit,
    titulo: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = digitos,
        onValueChange = { onCambio(it.filter(Char::isDigit).take(8)) },
        label = { Text(titulo) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        prefix = { Text("+569 ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
        isError = error != null,
        supportingText = {
            if (error != null) TextoError(error)
            else Text("Ingresa solo los 8 dígitos del celular.")
        },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun CampoAcceso(
    valor: String,
    onCambio: (String) -> Unit,
    titulo: String,
    error: String?,
    contrasena: Boolean = false,
    tipo: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(titulo) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (contrasena) KeyboardType.Password else tipo),
        visualTransformation = if (contrasena) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        isError = error != null,
        supportingText = { if (error != null) TextoError(error) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun TextoError(mensaje: String) {
    Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun ClaveUnicaPropuesta() {
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text("Ingresar con ClaveÚnica  ·  Próximamente")
    }
    Text(
        "Propuesta futura: opción desactivada. Requiere integración y autorización oficial; no solicita RUT ni credenciales.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
