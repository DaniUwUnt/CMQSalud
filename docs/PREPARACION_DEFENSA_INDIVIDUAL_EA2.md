# CMQ Salud — Preparación de defensa individual (EA2)

**Integrantes:** Daniela Salinas y Benjamín Bravo. **Modalidad:** proyecto en pareja, defensa individual.  
**Base:** guía DSY1105 de Preparación Parcial 3. **Duración indicada:** 15 minutos.  
**Datos:** solo datos sintéticos; aplicación local académica, no autorizada para uso institucional.

## Guion de 15 minutos

| Minutos | Qué mostrar | Archivos que debes encontrar |
|---|---|---|
| 0–2 | Build, Run, apertura de la app | `MainActivity.kt`, `build.gradle.kts`, `AndroidManifest.xml` |
| 2–4 | Explicar capas MVVM | `ui/screens/`, `viewmodel/`, `data/`, `model/` |
| 4–6 | Registrar documento válido y mostrar error por campo vacío | `RegistroDocumentoScreen.kt`, `DocumentoViewModel.kt` |
| 6–8 | Estado reactivo, listado, dashboard y confirmación animada | `HomeScreen.kt`, `RegistroDocumentoScreen.kt`, `DocumentoViewModel.kt` |
| 8–11 | Mostrar CRUD con Room, cerrar y reabrir | `Documento.kt`, `DocumentoDao.kt`, `CMQDatabase.kt`, `DocumentoRepository.kt` |
| 11–13 | Cámara o selector de archivos y miniatura | `RegistroDocumentoScreen.kt`, `AdjuntoUtils.kt`, `AndroidManifest.xml` |
| 13–15 | Practicar modificación simple solicitada por docente | Ajustar validación, filtro, etiqueta, consulta DAO o Composable |

## Flujo de dato que debes explicar sin apuntes

`RegistroDocumentoScreen` (evento Guardar) → `DocumentoViewModel.guardarDocumento()` (valida y usa `viewModelScope.launch`) → `DocumentoRepository.guardar()` (transacción, auditoría) → `DocumentoDao.insertar()` → `CMQDatabase` (Room/SQLite).  
Para recuperar: `DocumentoDao.observarTodos(): Flow` → Repository → `StateFlow` en ViewModel → `collectAsStateWithLifecycle()` en Compose → el listado se recompone.

## CRUD real y eliminación

- **Create:** `DocumentoDao.insertar()`; se crea registro con ID único automático.
- **Read:** `observarTodos()`, `observarPorId()` y `buscarPorId()`.
- **Update:** `actualizar()`; preserva ID y deja auditoría de la edición.
- **Delete lógico:** `DocumentoRepository.eliminar()` mueve a papelera 20 días.
- **Delete definitivo:** desde **Administración → Auditoría → Abrir papelera → Borrar definitivamente**, se usa `DocumentoRepository.borrarDefinitivamente()` y la consulta `DocumentoDao.eliminarDefinitivamente()`. Confirma antes de borrar; solamente rol administrador. Los eventos de auditoría se conservan. También existe purga automática luego del plazo de retención (aproximado).

## Ensayo sin ayuda externa

1. Compilar y ejecutar con un emulador ya completamente iniciado.
2. Probar formulario inválido: nombre vacío; observar error visible.
3. Registrar `Contrato de prueba`, funcionario `Funcionario ficticio` y tipo `Contrato`.
4. Abrir detalle y editar nombre; verificar actualización sin ID nuevo.
5. Cerrar la app completamente, volver a abrir y comprobar que el mismo registro existe.
6. Eliminar (papelera). Como admin, restaurar y luego eliminar definitivamente un documento de prueba; verificar auditoría.
7. Tomar foto o seleccionar imagen; explicar cómo se entrega la URI y quién mantiene el estado de pantalla.
8. Mostrar un commit propio de cada integrante y explicar su contribución real.
9. Ensayar cambiar `mínimo 3 caracteres` a `5` en ViewModel, ejecutar y comprobar. Luego revertir y hacer pruebas de nuevo.
10. Ensayar añadir un `Text` o `Card` reutilizable en `ui/components/` sin modificar la lógica de persistencia.

**Importante:** no usar IA ni material extra durante la defensa si así lo exige la evaluación. Practicar con antelación sin memorizar un guion ciego.

## Dudas críticas no confirmadas para docente/CITT

- ¿Qué roles reales pueden crear, consultar, modificar, revisar y eliminar documentos?
- ¿Existe circuito formal de aprobación/rechazo? ¿En qué condiciones se reabre una revisión?
- ¿Qué tipos de documentos y campos son oficiales?
- ¿El plazo de papelera de 20 días sería aceptable? ¿Quién autoriza borrado definitivo?
- ¿Qué datos sensibles realmente se requieren y qué política de custodia aplica?
- ¿Debe haber autenticación institucional o ClaveÚnica? Aquí ClaveÚnica está desactivada como propuesta.

La propuesta de revisión y la retención son **decisiones del piloto**, no reglas institucionales establecidas.
