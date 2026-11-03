# Matriz de verificación · no marcar como aprobado sin ejecutar

| Caso | Prueba | Resultado esperado | Ejecutado por | Estado |
|---|---|---|---|---|
| T01 | Build en Android Studio (`Ctrl+F9`) | `BUILD SUCCESSFUL` | — | Pendiente |
| T02 | Login con cuenta ficticia | Abre inicio; funcionario no accede a administración | — | Pendiente |
| T03 | Registro con nombre vacío | Muestra error, no crea documento | — | Pendiente |
| T04 | Registro con datos válidos | Aparece confirmación y documento en listado | — | Pendiente |
| T05 | Editar documento | Cambian datos sin crear nueva ID | — | Pendiente |
| T06 | Eliminar documento | Sale del listado y entra en papelera | — | Pendiente |
| T07 | Restaurar desde papelera (admin) | Reaparece en listado; hay evento de auditoría | — | Pendiente |
| T08 | Borrar definitivamente desde papelera (admin) | Ya no es recuperable; queda evento de auditoría | — | Pendiente |
| T09 | Cerrar totalmente y volver a abrir | Persisten documentos que no se hayan borrado | — | Pendiente |
| T10 | Foto desde cámara | Aparece miniatura y URI asociada al documento | — | Pendiente |
| T11 | Selector nativo de archivos | Se muestra adjunto guardado y permite abrirlo | — | Pendiente |
| T12 | Aprobar/rechazar como admin | Cambia estado con observación y auditoría | — | Pendiente |
| T13 | Intentar rechazar sin observación | Error de campo; operación no se ejecuta | — | Pendiente |
| T14 | `testDebugUnitTest` | Tests puros de reglas pasan | — | Pendiente |
| T15 | `connectedDebugAndroidTest` | Prueba CRUD Room en memoria pasa | — | Pendiente |
| T16 | Modificación en vivo de una regla UI | Compila, ejecuta y funciona | — | Pendiente |

## Evidencia para GitHub y Drive

Capturas del flujo válido/inválido, interfaz en ejecución, resumen de Build, persistencia después de reiniciar, matriz rellenada con fecha y resultado real, nombres y commits reales de ambos integrantes. Las dudas CITT se documentan en `PREPARACION_DEFENSA_INDIVIDUAL_EA2.md`. **No subir bases con personas reales, secretos, `.gradle/`, `local.properties` ni información sensible.**
