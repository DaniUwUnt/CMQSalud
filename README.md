# CMQ Salud — versión 1.8.12 (teléfonos únicos en registro)

**Novedades de esta versión:** [LEEME_VERSION_1_8_12_TELEFONOS_UNICOS.md](LEEME_VERSION_1_8_12_TELEFONOS_UNICOS.md)  
**Base de interfaz:** [LEEME_VERSION_1_8_INTERFAZ_UBICACION.md](LEEME_VERSION_1_8_INTERFAZ_UBICACION.md)

# CMQ Salud — Piloto documental (v1.8.12, preparación EA2)

**Duoc UC · DSY1105 Desarrollo de Aplicaciones Móviles**  
**Integrantes:** Daniela Salinas y Benjamín Bravo.  
**Contexto:** gestión documental de prueba para el caso de Corporación Municipal de Quilpué, Área Salud. **No es una app institucional** y se debe usar únicamente con información sintética.

## Novedades 1.8.12

- Impide registrar otra cuenta con un celular ya usado, aunque el número esté escrito con otro formato (espacios, guiones, con/sin prefijo).
- También comprueba el número de teléfono fijo, si se especificó, frente a ambos campos telefónicos de las cuentas existentes.
- El bloqueo se realiza en `AuthRepository.registrar` antes de guardar datos, no solo en la interfaz; avisa junto al campo correspondiente y al pie del formulario.
- Incluye cuentas deshabilitadas en la comprobación; las cuentas preexistentes con números repetidos no se modifican ni borran.
- No cambia el esquema Room ni los documentos guardados.

## Novedades 1.8.11

- Se añade en la pantalla de ingreso la opción «¿Has olvidado tu contraseña?», ubicada debajo del campo de contraseña.
- Al pulsarla, se muestra un diálogo informativo que aclara que la recuperación es una propuesta y todavía no está activa.
- No se envían correos ni se cambia ninguna contraseña. La recuperación real requerirá un servicio de correo, verificación de identidad y un flujo seguro de restablecimiento.
- No se modifica la base de datos ni el flujo de inicio de sesión.

## Novedades 1.8.10

- Se corrige el área segura superior del componente reutilizable `AppHeader` mediante `statusBarsPadding()`.
- Las pantallas Registrar/Editar documento, Mi perfil y Detalle del documento mantienen títulos y acciones debajo de la barra de estado y fuera de la zona de la cámara frontal.
- El fondo de la cabecera se conserva detrás de la barra de estado, sin cambiar los márgenes laterales ni el tamaño de los textos.
- Sin cambios de datos, historial ni esquema Room.

## Novedades 1.8.9

- Se incorpora `fechaModificacion` en Room v5 con migración no destructiva desde Room v4; se recupera la última edición/revisión de la auditoría cuando existe y, si no, se usa la fecha de creación.
- Las tarjetas de documentos activos y en papelera muestran fecha de creación y última modificación, ambas con hora (`dd/MM/yyyy HH:mm`).
- El detalle muestra ambas fechas y contiene el historial de creación, edición, revisión y otras acciones, con autor y hora.
- Cada edición real y cada revisión actualiza automáticamente la fecha de última modificación; guardar sin cambios no altera la fecha.
- La creación original permanece intacta. No se eliminan documentos ni historial existente.

## Novedades 1.8.8

- Se restaura el tamaño anterior del logo local del Gobierno de Chile (62 dp).
- Se baja el contenido de la cabecera 12 dp para separar el título de la zona de notificaciones y de la cámara frontal.
- Se mantiene el espaciado anterior entre el logo y el texto; no se modifica el resto de las pantallas.

## Novedades 1.8.7

- Ajuste anterior de cabecera; el tamaño del logo se restaura en 1.8.8 según la vista solicitada.
- Se mantienen la fecha/hora de creación y el logo local sin dependencia de Internet.

## Novedades 1.8.6

- Se elimina la tarjeta de identidad institucional de Inicio.
- El logo del Gobierno de Chile se incluye como PNG local en `app/src/main/res/drawable-nodpi/`; no depende de Internet ni de un enlace externo.
- El logo aparece de forma compacta en la cabecera de acceso y registro, sin una tarjeta ni texto descriptivo añadido.
- Las fechas de creación de documentos muestran día, mes, año y hora (`dd/MM/yyyy HH:mm`), también en la papelera.
- Se mantiene el aviso de piloto académico y datos ficticios; no se presenta como aplicación oficial.

## Funcionalidades

- Registro local de cuentas de demostración, inicio/cierre de sesión, perfil y rol administrador/funcionario. Primera cuenta en la instalación recibe rol administrador **solo para probar** el panel; ClaveÚnica se muestra desactivada como propuesta.
- Registro de documentos con nombre, funcionario, tipo y descripción opcional; validaciones visibles; fotografías con cámara y adjuntos desde el selector del sistema.
- Listado y detalle; búsqueda, filtros, ordenamiento y panel con métricas derivadas de Room.
- Edición, eliminación lógica, restauración desde papelera y **borrado definitivo autorizado al administrador** con confirmación. La papelera tiene retención propuesta de 20 días y mantenimiento periódico aproximado.
- Revisión documental de demostración (Pendiente/Aprobado/Rechazado), con observaciones y auditoría; esta regla está **pendiente de confirmar con el docente/CITT**.
- Confirmación centrada y animada tras guardar, con retorno automático al listado/detalle; actualización reactiva por `StateFlow`.

## Arquitectura y CRUD (para explicar en la defensa)

```text
Pantalla (ui/screens, Jetpack Compose)
  └── DocumentoViewModel (StateFlow, validación, viewModelScope.launch)
      └── DocumentoRepository (operaciones y auditoría; control de rol)
          └── DocumentoDao (insertar, observarTodos, actualizar, eliminarDefinitivamente)
              └── CMQDatabase (Room / SQLite, versión 5)
```

`Documento` es `@Entity`; `DocumentoDao` es `@Dao`; `CMQDatabase` es `@Database`. La navegación se centraliza en `navigation/AppNavigation.kt`. La cámara y los adjuntos utilizan `AdjuntoUtils.kt` y `FileProvider`.

**Borrado:** el botón Eliminar documento lo envía a papelera (`deletedAt`); el administrador puede restaurar o borrar definitivamente desde Administración → Auditoría → Papelera. El historial de auditoría se conserva para la demostración. Una limpieza programada purga elementos vencidos; WorkManager no garantiza minuto exacto.

## Actualización 1.7.1 — Confirmación visible

Se cambió el mensaje bajo el formulario por un diálogo central animado con ícono de éxito y regreso automático después de 1,9 segundos. Solo se muestra cuando Room completó el guardado. No se modifica el esquema de Room. Detalles en `LEEME_CONFIRMACION_CENTRAL_V1_7_1.md`.

## Instalación

1. Descomprimir el ZIP **fuera de OneDrive si se producen bloqueos** y abrir `CMQSalud_Semana9` desde Android Studio.
2. Sincronizar Gradle. `compileSdk=35`, Kotlin `2.0.21`, Android Gradle Plugin `8.7.3`, Java 17 y Gradle 8.9. Android Studio puede descargar dependencias y requerirá conexión para primera instalación.
3. `Ctrl+F9` para compilar. Si hay errores, abrir Build y leer el **primer error real** antes de tocar versiones.
4. Encender Pixel 6/6a con Android 15 API 35 y esperar pantalla de inicio; seleccionar dispositivo → Run.
5. Si estás actualizando sobre una instalación anterior, conserva `applicationId=cl.duoc.cmqsalud` y **no desinstales ni borres datos**. La base se actualiza a Room v5 mediante migraciones no destructivas v1→v5; conserva documentos e historial existentes. **Un emulador nuevo tiene almacenamiento distinto**.

> En este ZIP se conservan los archivos de wrapper Gradle originales; no se incluye `gradle-wrapper.jar`. Android Studio puede abrir el proyecto por sus archivos `settings.gradle.kts` y `build.gradle.kts`. Para ejecutar `./gradlew` por terminal, genera/restaura el wrapper oficial desde una instalación de Gradle. La compilación Android completa requiere Android SDK y no fue verificada dentro de este entorno.

## Pruebas

- `app/src/test/...`: pruebas unitarias puras (RUT, plazo de papelera, filtro y revisión).
- `app/src/androidTest/...`: prueba instrumental SQLite/Room en memoria de Create–Read–Update–Delete, **requiere emulador o dispositivo**.
- Matriz de pruebas: `docs/MATRIZ_PRUEBAS_CRUD_Y_DEFENSA.md`.
- Guion de defensa individual: `docs/PREPARACION_DEFENSA_INDIVIDUAL_EA2.md`.
- Avances históricos de Semana 9 y Parcial 2: `docs/`.

**Límites:** SharedPreferences locales y auditoría local NO equivalen a autenticación institucional, permisos resistentes a manipulación, trazabilidad certificada, cumplimiento sanitario ni uso con documentos reales. No usar RUT, teléfonos ni claves reales. No realizar commits ficticios del compañero.
