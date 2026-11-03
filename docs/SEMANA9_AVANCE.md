# CMQ Salud · Bitácora técnica de avance (Semana 9)

## Estado

La versión base **fue compilada y ejecutada en un Pixel 6a** según las pruebas realizadas por el equipo: formulario vacío mostró errores, se registró «Contrato de prueba», apareció en el listado y el detalle, y persistió tras cerrar y abrir la app. También se observó la confirmación animada.

La **versión mejorada de este ZIP** agrega cámara a resolución completa, apertura del adjunto, edición y búsqueda. **Estas nuevas funciones requieren nueva prueba de Build/Run en Android Studio**.

## Trazabilidad

| RF | Pantalla / función | Modelo / estado | Prueba |
|---|---|---|---|
| RF03 Registrar documento | RegistroDocumentoScreen | DocumentoFormUiState / DocumentoViewModel | Ingreso válido e inválido |
| RF04 Consultar documentos | HomeScreen / DetalleDocumentoScreen | StateFlow + Room | Buscar, ver detalle y reiniciar app |
| RF05 Editar metadatos | Ruta editar/{id} / Formulario | DocumentoViewModel + Dao @Update | Cambiar nombre y verificar mismo ID |
| RF06 Eliminar documento | Detalle + AlertDialog | Dao @Delete | Cancelar y luego eliminar |
| RF07 Adjuntar evidencia | Cámara y OpenDocument | AdjuntoUtils + uri en Documento | Capturar y abrir desde detalle |

## MVVM

La UI invoca eventos del ViewModel y observa StateFlow. El ViewModel valida y decide guardar/actualizar. El Repository accede a Room. La pantalla no escribe directamente en SQLite.

## Consideraciones de seguridad

Solo datos inventados. FileProvider evita compartir `file://` con aplicaciones externas; la app no solicita GPS ni permisos innecesarios de ubicación. La selección de archivos se hace mediante el selector del sistema. **Sin autorización institucional no usar datos reales.**

## Dudas para el profesor / CITT

- ¿Quién puede cargar, editar, eliminar y consultar documentos por rol?
- ¿La organización requiere que la eliminación sea física, lógica o que exista historial/auditoría?
- ¿Cuáles son los tipos documentales y estados autorizados?
- ¿Qué clasificación de confidencialidad y tiempos de retención aplican?
- ¿Se exige conexión con SharePoint o es suficiente un piloto local con datos ficticios?

## Siguiente mejora posible

Implementar autorización por perfiles *simulados*, estados documentales acordados con el docente y pruebas automatizadas. No hacerlos sin definir las reglas de negocio antes.


## Avance adicional — Prototipo de acceso y registro

Se propone y se implementa una pantalla de ingreso con usuarios locales de demostración, alta de cuentas ficticias y cierre de sesión. No representa autenticación oficial ni se usan credenciales de CMQ Salud. El acceso mediante **ClaveÚnica** se muestra **deshabilitado**, exclusivamente como posibilidad futura sujeta a validación por docente/CITT/organización. Se mantiene el mismo repositorio Room de documentos ficticios, compartido en este piloto entre las cuentas locales. Paleta actualizada a azul petróleo, turquesa, verde salud y fondos claros.
