# Plan de pruebas — CMQ Salud 1.6

Abrir Android Studio → `Open` → carpeta `CMQSalud_Semana9` → Gradle Sync → `Ctrl + F9` → Run (Pixel 6a).

**No desinstalar el piloto anterior ni ejecutar Wipe Data** si se desean mantener las cuentas y documentos. Actualizar encima del mismo `applicationId=cl.duoc.cmqsalud`.

## Matriz manual

| ID | Procedimiento | Resultado esperado | Resultado observado |
|---|---|---|---|
| P01 | Iniciar sesión con cuenta administradora | En Inicio aparece Administración y auditoría | ☐ |
| P02 | Crear un documento válido de demostración | Guardado y regreso automático al listado | ☐ |
| P03 | Intentar guardar sin nombre | Mensaje de error, sin documento nuevo | ☐ |
| P04 | Cambiar la búsqueda y filtros de estado/tipo | Solo registros que cumplen los criterios | ☐ |
| P05 | Revisar como admin con Aprobar | Estado Aprobado; dashboard suma 1 aprobado; auditoría REVISIÓN | ☐ |
| P06 | Rechazar sin observación | Error específico y estado sin modificación | ☐ |
| P07 | Rechazar con observación | Estado Rechazado y observación visible en detalle | ☐ |
| P08 | Editar datos de un documento revisado | Vuelve a Pendiente (regla de piloto); queda EDICIÓN en bitácora | ☐ |
| P09 | Entrar con cuenta FUNCIONARIO | Puede consultar documento, pero no aparece el panel de revisión | ☐ |
| P10 | Eliminar y restaurar | Entra a papelera y vuelve al listado; eventos registrados | ☐ |
| P11 | Cerrar app y volver a abrirla | Documentos, revisión y eventos persisten | ☐ |
| P12 | Adjuntar imagen y abrirla desde detalle | Archivo visible/abrible; si se deniega el permiso, muestra error | ☐ |
| P13 | Ejecutar `testDebugUnitTest` desde Gradle | Pasan pruebas de reglas puras | ☐ |
| P14 | Revisar nuevo Room desde base esquema v3 | Migración `3→4` conserva documentos y agrega campos vacíos | ☐ |

## Qué registrar en GitHub

Cada integrante debe contribuir de manera real, con commits pequeños. Ejemplos de títulos:

- `feat: dashboard con indicadores de Room y filtros`
- `feat: revisión documental y registro de auditoría`
- `test: pruebas de validación y filtros`
- `docs: guía de avance y demostración parcial 2`

No crear commits ficticios a nombre del otro integrante. No subir `.gradle/`, contraseñas, datos de personas reales ni archivos de base de datos del emulador.

## Limitaciones conocidas

Room/local SharedPreferences permiten mostrar un prototipo; no constituyen una seguridad o auditoría institucional inalterable. La aprobación/rechazo y sus reglas son propuestas para validación docente/CITT; las evidencias deben usar datos ficticios.
