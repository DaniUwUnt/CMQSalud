# Modelo de datos y navegación — CMQ Salud v1.6

## Modelo local preliminar

```mermaid
erDiagram
  DOCUMENTO ||--o{ EVENTO_AUDITORIA_DOCUMENTO : "genera"
  DOCUMENTO {
    int id PK
    string nombre
    string descripcion
    string tipo
    string funcionario
    string estado
    string adjuntoUri
    string adjuntoNombre
    long fechaCarga
    long deletedAt
    string observacionRevision
    string revisadoPor
    long fechaRevision
  }
  EVENTO_AUDITORIA_DOCUMENTO {
    long id PK
    int documentoId FK
    string documentoNombre
    string actorCorreo
    string actorNombre
    string accion
    string descripcion
    long fecha
  }
```

**Advertencia técnica:** `documentoId` es una relación lógica, sin constraint `FOREIGN KEY` de SQLite en esta versión, porque se conserva el historial incluso tras la purga definitiva del documento. `actorCorreo` es una referencia a una cuenta local, no una relación SQL: las cuentas demo están en SharedPreferences. No representa todavía un modelo institucional normalizado de usuarios.

## Mapa de navegación

```mermaid
flowchart TD
  A[Ingreso] --> B[Registro de cuenta]
  A --> C[Inicio y Dashboard]
  B --> C
  C --> D[Registrar documento]
  D --> C
  C --> E[Listado / búsqueda / filtros]
  E --> F[Detalle del documento]
  F --> G[Editar]
  G --> F
  F --> H[Revisar: aprobar / rechazar]
  H --> F
  C --> I[Administrar cuentas, solo admin]
  I --> J[Auditoría global y papelera]
  C --> K[Mi perfil]
```

El rol `FUNCIONARIO` no ve la revisión ni administración. Además de ocultar controles, la revisión valida el rol en `DocumentoRepository`.

**Pendiente de validación:** La funcionalidad de revisar, los estados Aprobado/Rechazado y la reapertura a Pendiente son decisiones explícitas del prototipo, no reglas proporcionadas por CMQ Salud en el enunciado.
