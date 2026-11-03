# CMQ Salud — Avance de Parcial 2 (prototipo académico)

**Integrantes:** Daniela Salinas y Benjamín Bravo  
**Asignatura:** DSY1105, Desarrollo de Aplicaciones Móviles  
**Organización referencial:** Corporación Municipal de Quilpué, Área Salud  
**Importante:** utilizar exclusivamente datos, RUT y documentos ficticios.

## Problema y objetivo

El caso de CMQ Salud propone apoyar la gestión de documentación de funcionarios desde una aplicación móvil. El piloto implementa registro, consulta, adjuntos y trazabilidad local. No conecta a bases institucionales ni a ClaveÚnica. No presupone reglas de aprobación reales.

## Avances comprobables en la aplicación

| Código interno de demostración | Funcionalidad | Evidencia que debe mostrar el equipo |
|---|---|---|
| RF03 | Registrar documento y adjunto desde cámara/archivos | Crear con campos válidos; error si nombre vacío |
| RF04 | Buscar y consultar documentos | Buscar nombre, descripción o funcionario |
| RF05 | Editar y eliminar lógicamente | Modificar; enviar a papelera y restaurar |
| RF06 (piloto) | Control de cuentas y roles | Administrador gestiona usuarios; funcionario sin acceso al panel |
| RF07 (piloto) | Historial documental | Ver autor, fecha, acción y descripción de eventos nuevos |
| RF08 (propuesta) | Revisión documental | Admin aprueba/rechaza, explica motivo y queda evento de revisión |
| RF09 (piloto) | Dashboard y filtros | Estadísticas derivadas de Room; filtro por tipo/estado y orden |

**Nota de trazabilidad:** los códigos RF06–RF09 son identificadores internos propuestos para esta versión. Contrastar con el documento oficial de requerimientos v2 antes de entregar.

## Flujo de la nueva funcionalidad (propuesta por validar)

Inicio → Buscar documento → Detalle → Administrador escribe observación → Aprobar/Rechazar → Repository verifica rol → Room actualiza estado y auditoría en la misma transacción → StateFlow actualiza Dashboard y Detalle.

- Solo una cuenta `ADMINISTRADOR` puede ejecutar la revisión; el repositorio también verifica el rol.
- Un rechazo requiere una observación. La aprobación permite observación opcional.
- Si posteriormente se edita el contenido de un documento revisado, vuelve a `Pendiente` y se registra el cambio. **Esta regla es propuesta del piloto**, no una regla confirmada por CMQ Salud.
- La interfaz muestra cuándo y por quién se revisó y el historial conserva el cambio.
- Documentos antiguos quedan con estado previo y campos de revisión vacíos; no se inventa autoría retroactiva.

## Arquitectura y relación con materia del docente

- **Jetpack Compose / Material 3:** `Scaffold`, `LazyColumn`, `Card`, `Button`, `OutlinedTextField`, `FilterChip` y diseño reutilizable.
- **MVVM:** las pantallas presentan estado y envían eventos; `DocumentoViewModel` procesa y expone `StateFlow`.
- **Navegación:** `NavController` y `NavHost` conectan ingreso, inicio, formulario, detalle y administración.
- **Persistencia:** Room almacena documentos, observaciones de revisión y auditoría. Migración no destructiva `3→4`.
- **Validaciones:** RUT chileno, formularios obligatorios y comentario requerido para rechazar.
- **Estado reactivo:** `collectAsStateWithLifecycle` observa listados y cambios sin recarga manual.
- **Animación:** confirmación al guardar.
- **Recurso nativo:** cámara de resolución completa y selector del sistema para imagen/PDF.
- **Mantenimiento:** papelera de 20 días y trabajo diferido con WorkManager; no es exacto al minuto.
- **Pruebas:** pruebas unitarias de filtros, estadísticas, revisión, RUT y plazo de papelera.

## Delimitación del MVP

### Incluido

Registro/inicio local de prueba; perfiles; roles de demostración; documentos ficticios; cámara y archivos; búsqueda; filtros; dashboard; revisión propuesta; auditoría local; papelera de 20 días; persistencia y navegación.

### No incluido

Integración real con ClaveÚnica (botón desactivado), autenticación institucional, servidor central, firmas electrónicas, aprobación oficial, intercambio de datos reales, seguridad de producción, auditoría antifalsificación ni recuperación remota de cuentas.

## Dudas críticas a validar con docente/CITT

1. ¿Quién está facultado para aprobar/rechazar documentos en la organización? ¿Qué perfiles o firmas se requieren?
2. ¿Qué estados documentales reconoce realmente la organización?
3. ¿Una modificación posterior a la aprobación obliga a una nueva revisión? ¿Qué ocurre con la aprobación previa?
4. ¿Qué documentos caducan y cuáles deben conservarse? ¿Es adecuado el plazo de 20 días en papelera?
5. ¿Qué información personal es estrictamente necesaria para el piloto y bajo qué condiciones se resguarda?
6. ¿El historial documental puede verlo cualquier funcionario autorizado o solo personal supervisor?
7. ¿Dónde y cómo se almacenaría la auditoría real para evitar alteraciones?

## Evidencia para Parcial 2

Llevar: compilación `BUILD SUCCESSFUL`, video/capturas del flujo real, test válido e inválido, cierre y reapertura mostrando persistencia, trabajo con 2 cuentas de distintos roles, repositorio GitHub con aportes auténticos de ambos integrantes, README, mockups actualizados, mapa de navegación y modelo de datos actualizado.

No afirmar que esta versión ya fue compilada en un emulador hasta ejecutar la prueba en Android Studio.
