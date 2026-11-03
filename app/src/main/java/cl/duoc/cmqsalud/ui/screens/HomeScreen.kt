package cl.duoc.cmqsalud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.cmqsalud.model.Documento
import cl.duoc.cmqsalud.model.FiltrosDocumentos
import cl.duoc.cmqsalud.model.ReglasRevision
import cl.duoc.cmqsalud.ui.components.DocumentoCard
import cl.duoc.cmqsalud.ui.theme.CMQAzulProfundo
import cl.duoc.cmqsalud.ui.theme.CMQTurquesa
import cl.duoc.cmqsalud.viewmodel.DocumentoViewModel

/** Inicio optimizado para usar el piloto sin pasar por formularios o menús innecesarios. */
@Composable
fun HomeScreen(
    viewModel: DocumentoViewModel,
    nombreUsuario: String,
    esAdministrador: Boolean,
    onCerrarSesion: () -> Unit,
    onAbrirAdmin: (() -> Unit)? = null,
    onAbrirDocumentos: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    val documentos by viewModel.documentos.collectAsStateWithLifecycle()
    val resumen = FiltrosDocumentos.resumen(documentos)
    val recientes = FiltrosDocumentos.filtrar(documentos, "").take(3)
    val primerNombre = nombreUsuario.trim().substringBefore(' ').ifBlank { "usuario/a" }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 22.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().background(
                        Brush.horizontalGradient(listOf(CMQAzulProfundo, CMQTurquesa)),
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    ).padding(start = 20.dp, end = 16.dp, top = 25.dp, bottom = 28.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.94f), modifier = Modifier.size(58.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        primerNombre.take(1).uppercase(),
                                        color = CMQAzulProfundo,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 13.dp)) {
                                Text("¡Hola, $primerNombre!", color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(if (esAdministrador) "Administrador" else "Funcionario",
                                    color = Color.White.copy(alpha = 0.86f),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = onCerrarSesion) {
                                Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión",
                                    tint = Color.White)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("CMQ SALUD", color = Color.White,
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Gestión documental · Piloto académico",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tu panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Indicador("Documentos", resumen.total, Modifier.weight(1f))
                        Indicador("Pendientes", resumen.pendientes, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Indicador("Aprobados", resumen.aprobados, Modifier.weight(1f))
                        Indicador("Rechazados", resumen.rechazados, Modifier.weight(1f))
                    }
                }
            }
            // El acceso a documentos, el registro y el perfil ya se encuentran en la barra inferior.
            // Evitamos duplicar destinos en Inicio y destacamos solo la gestión del administrador.
            if (esAdministrador) onAbrirAdmin?.let { abrirAdministracion ->
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Zona de administración",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            onClick = abrirAdministracion,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Panel de administración",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Cuentas, auditoría y papelera",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Icon(
                                    Icons.Outlined.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimos documentos", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = onAbrirDocumentos) {
                        Text("Ver todos")
                        Icon(Icons.Outlined.ArrowForwardIos, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (recientes.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Aún no hay documentos registrados.")
                            Text("Usa «Nuevo documento» para comenzar.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(recientes, key = { it.id }) { documento ->
                    Box(Modifier.padding(horizontal = 18.dp)) {
                        DocumentoCard(documento, onClick = { onVerDetalle(documento.id) })
                    }
                }
            }
            item {
                Text("Datos ficticios almacenados localmente. La revisión documental es una propuesta pendiente de validación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

/** Apartado independiente: búsqueda y filtros solo cuando el usuario los necesita. */
@Composable
fun DocumentosScreen(
    viewModel: DocumentoViewModel,
    onNuevoDocumento: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    val documentos by viewModel.documentos.collectAsStateWithLifecycle()
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtroEstado by rememberSaveable { mutableStateOf(FiltrosDocumentos.TODOS) }
    var filtroTipo by rememberSaveable { mutableStateOf(FiltrosDocumentos.TODOS) }
    var recientesPrimero by rememberSaveable { mutableStateOf(true) }
    var filtrosVisibles by rememberSaveable { mutableStateOf(false) }
    val filtrados = FiltrosDocumentos.filtrar(documentos, busqueda, filtroTipo, filtroEstado, recientesPrimero)

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Mis documentos", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Text("Consulta y administra tus registros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = busqueda, onValueChange = { busqueda = it },
                    label = { Text("Buscar nombre, funcionario o descripción") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = filtrosVisibles, onClick = { filtrosVisibles = !filtrosVisibles },
                        label = { Text(if (filtrosVisibles) "Ocultar filtros" else "Filtrar") },
                        leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, Modifier.size(18.dp)) })
                    FilterChip(selected = !recientesPrimero, onClick = { recientesPrimero = !recientesPrimero },
                        label = { Text(if (recientesPrimero) "Recientes" else "Antiguos") })
                    Spacer(Modifier.weight(1f))
                    Text("${filtrados.size} registros", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (filtrosVisibles) {
                item {
                    Text("Estado", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (listOf(FiltrosDocumentos.TODOS) + ReglasRevision.estados).forEach { estado ->
                            FilterChip(selected = filtroEstado == estado,
                                onClick = { filtroEstado = estado }, label = { Text(estado) })
                        }
                    }
                }
                item {
                    Text("Tipo", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todos", "Contrato", "Certificado", "Permiso").forEach { tipo ->
                            FilterChip(selected = filtroTipo == tipo, onClick = { filtroTipo = tipo },
                                label = { Text(tipo) })
                        }
                    }
                }
            }
            item {
                Card(
                    onClick = onNuevoDocumento,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AddCircleOutline, contentDescription = null)
                        Text("  Registrar nuevo documento", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (filtrados.isEmpty()) {
                item {
                    Text(if (documentos.isEmpty()) "Todavía no hay documentos guardados."
                         else "No encontramos documentos con esos filtros.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(filtrados, key = { it.id }) { documento ->
                    DocumentoCard(documento = documento, onClick = { onVerDetalle(documento.id) })
                }
            }
        }
    }
}

@Composable
private fun Indicador(etiqueta: String, cantidad: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("$cantidad", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}
