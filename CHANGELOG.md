## 1.8.12 — Unicidad de teléfonos en registro (30/10/2026)
- Al registrar, se revisa si el celular o teléfono fijo ya figura en cualquier cuenta local, incluso cuentas deshabilitadas.
- La validación se aplica en el repositorio antes de persistir; los mensajes aparecen también junto al campo del formulario.
- Se normalizan separadores de números y variantes del celular chileno para evitar duplicados por formato.
- Sin cambios en Room ni borrado automático de cuentas antiguas.

## 1.8.10 — Área segura superior (28/10/2026)
- `AppHeader` aplica `statusBarsPadding()` para que títulos y acciones no queden detrás de la barra de estado ni la cámara frontal.
- Afecta a Registro/Edición de documentos, Mi perfil y Detalle del documento.
- Conserva el fondo de la cabecera y el diseño existente; no modifica datos ni Room.

## 1.8.9 — Fecha de última modificación (28/10/2026)
- Room v5 agrega `fechaModificacion` mediante migración no destructiva 4→5; los documentos existentes conservan su creación y usan esa fecha como punto de partida.
- Al crear un documento, fecha de creación y última modificación comienzan iguales.
- Las ediciones con cambios reales y las revisiones administrativas actualizan la última modificación y registran autor/fecha en el historial.
- Las tarjetas de documentos y papelera muestran ambas fechas con hora; el detalle muestra ambas y mantiene el historial dentro del documento.
- Guardar sin cambios no modifica el timestamp; no se usa migración destructiva.

## 1.8.8 — Separación de la barra superior (28/10/2026)
- Se conserva el tamaño original de 62 dp del logo local.
- Se baja la cabecera para alejar el título de las notificaciones y la cámara frontal.

## 1.8.7 — Ajuste visual de cabecera (28/10/2026)
- Logo del Gobierno de Chile más pequeño (48 dp en vez de 62 dp), con un leve desplazamiento hacia abajo.
- Cabecera ligeramente más compacta y con menos separación entre logo y título.
- Sin cambios en datos, Room ni funcionalidades.

## 1.8.6 — Logo local y fecha/hora (28/10/2026)
- Se elimina la tarjeta de identidad institucional de la pantalla Inicio.
- El logo del Gobierno de Chile queda empaquetado como PNG local y se muestra de forma compacta en la cabecera de acceso/registro; no depende de URL ni conexión.
- Las tarjetas de documentos y la papelera muestran fecha y hora de creación (`dd/MM/yyyy HH:mm`).
- Se elimina Coil y el permiso INTERNET que se habían agregado solo para cargar los logos.
- Se conserva la advertencia de piloto académico y datos ficticios. No hay cambios a Room ni a los documentos almacenados.

## 1.8.5 — Fecha de creación e identidad visual (28/10/2026)
- Se muestra la fecha original de creación en las tarjetas de documentos activos y restaurados.
- La papelera también muestra la fecha de creación original junto a la fecha de eliminación y el plazo de retención.
- Inicio incorpora los logotipos de la Corporación Municipal de Quilpué y del Gobierno de Chile en una tarjeta compacta.
- Los logotipos se cargan en línea con texto alternativo si falla la conexión; se agrega permiso INTERNET y Coil Compose.
- Se aclara en la interfaz que es un piloto académico no oficial y se atribuye el logo de Gobierno de Chile al Estado de Chile (CC BY 3.0 Chile).
- Sin cambios a Room ni a los datos guardados.

## 1.8.4 — Colores por estado (28/10/2026)
- Tarjetas documentales verdes para Aprobado, rojas para Rechazado y neutras para Pendiente.
- Etiquetas con colores legibles y texto explícito de estado.
- Un único cambio en `DocumentoCard` afecta Inicio y Documentos; sin migración de datos.

## 1.8.2 — Navegación sin accesos duplicados
- Inicio despejado: se eliminan las tarjetas redundantes de Documentos, Nuevo y Mi perfil.
- Administración es la única acción destacada, exclusiva del rol administrador, y sigue enlazada al panel existente.
- Barra inferior: Inicio, Documentos (incluye búsqueda), Nuevo y Mi perfil.
- No se modifica el esquema ni el almacenamiento local.

## 1.8.0 — Experiencia de uso y ubicación (28/10/2026)

- Pantalla Inicio más simple, indicadores y accesos rápidos.
- Barra inferior: Inicio, Documentos, Nuevo, Mi perfil.
- Cierre de sesión en la esquina superior derecha, con confirmación.
- Registro y perfil con región/comuna dependientes, búsqueda offline de 346 comunas.
- Limpieza de calle/número de domicilio anteriores; cuentas y documentos intactos.
- Mantiene Room 4, auditoría, papelera y confirmación centrada.

## Versión 1.7.1 — Confirmación central de guardado

- Diálogo centrado con animación, ícono de éxito y fondo atenuado.
- Regreso automático en 1,9 segundos al listado o al detalle.
- Evita repetir guardados al aparecer la confirmación.
- Conserva el campo de celular con prefijo +569 fijo y no altera Room.

# Historial de versiones

## 1.7 — Preparación defensa individual EA2

- Conserva estructura y funciones de la v1.6; **misma base Room v4 y mismo identificador**.
- Agrega borrado definitivo desde papelera para administrador, con confirmación y evento de auditoría, permitiendo demostrar Delete real.
- Agrega prueba instrumental de DAO/Room (SQLite temporal en memoria) para Create–Read–Update–Delete.
- Guía de defensa de 15 minutos y matriz de pruebas verificables. Evitar funciones grandes a último momento.

---

## v1.4-demo — Auditoría y registro completo
- Migración Room 1→2 sin borrado destructivo; tabla de auditoría y eliminación lógica.
- Autor, fecha y operación documentados en creación/edición/eliminación/restauración.
- Historial por documento; global con filtros y papelera solo para administradores.
- Registro completo con RUT verificado, celular +569, datos de domicilio y rol no editable durante registro.
- Pantalla de perfil; cuentas anteriores mantienen acceso sin datos inventados.

# Registro de cambios — CMQ Salud

## Piloto 1.3 — Panel administrativo y retorno automático
- Rol de demostración `ADMINISTRADOR` / `FUNCIONARIO` en cuentas locales existentes.
- Migración sin borrar datos para asignar administración a primera cuenta previa.
- Acciones de consulta, bloqueo, reactivación, cambio de rol y eliminación con confirmaciones.
- Restricciones sobre cuenta activa y último administrador, comprobadas también en repositorio.
- Registro local de últimas acciones de administración.
- Retorno automático a listado/detalle tras tarjeta animada de guardado exitoso.
- Sin cambios al esquema Room ni a los archivos de documentos.

## v1.5 — 15/10/2026
- RUT se formatea al cambiar de campo (sin «de prueba» en la etiqueta).
- Descripción opcional de documento, también editable y consultable.
- Room esquema v3 + migración 2→3 sin pérdida de registros.
- Retención de papelera por 20 días, purga periódica y al abrir; eliminación de fotos internas y registro de auditoría de purga. WorkManager no garantiza ejecución en el minuto exacto.

## v1.6 — 17/10/2026 · Dashboard, filtros y revisión documental (propuesta)
- Dashboard con conteos desde Room: total activo, pendiente, aprobado y rechazado.
- Búsqueda combinada por texto, tipo, estado y orden temporal; chips Material 3.
- Revisión documental desde detalle, disponible solo a administradores y protegida también en repositorio.
- Observación obligatoria al rechazar; opcional al aprobar.
- Autor, fecha, estado y observación de revisión se persisten; acciones registradas en auditoría.
- Al editar contenido revisado, vuelve a Pendiente (regla propuesta del piloto, pendiente de validación externa).
- Room v4, migración no destructiva 3→4 y pruebas unitarias nuevas.
- Guía de avance y matriz de pruebas para la presentación de Parcial 2.
