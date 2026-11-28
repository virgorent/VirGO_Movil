# Plan — 016 Invitados

| Campo | Valor |
|---|---|
| Módulo | `016-invitados` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25) |
| Fecha | 2026-11-25 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

Bloque del evento del cliente (`007`, `014`, `016`, `017`) en una sola rama `feat/evento-cliente`:

- `:core:network`: `EventsApi` (eventos) y `EventPlanningApi` (invitados y pendientes) con sus DTOs.
- `:core:domain`: `ClientEvent`, `EventGuest`, `EventTask`, validadores y repositorios `EventsRepository` y `EventPlanningRepository`.
- `:core:data`: `EventsRepositoryImpl` y `EventPlanningRepositoryImpl`, módulo Hilt.
- `:app`: `ui/events/` con `ClientEvents` (lista → detalle → formulario, invitados o pendientes). Las rutas `event`, `guests` y `tasks` del espacio del cliente la abren.

## 2. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ✅ | Flujos cortos por spec. |
| II | ⚠️ Excepción | `000` sin cerrar y sin URL desplegada; ver la excepción ampliada. |
| III | ✅ | MVVM/UDF; validación en `:core:domain`. |
| IV | ✅ | Contrato verificado en el código de backend. |
| V | ✅ | Sin datos personales en logs. |
| VI–XII | ✅ | Unitarias, MockWebServer, Compose Testing y barrido de accesibilidad; pipeline existente; memoria al cerrar. |

## 3. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Validadores y `ViewModel` |
| Integración | MockWebServer con la forma real: cuerpos enviados, lectura, `400`/`401`/`404`/`5xx` |
| UI | Criterios de aceptación del spec |
| Accesibilidad | 320 dp/2.0 en ambos temas |
| Humo | Contra producción tras el despliegue |
