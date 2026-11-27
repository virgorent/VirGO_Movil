# Plan — 023 Inicio y calendario del propietario

| Campo | Valor |
|---|---|
| Módulo | `023-propietario-inicio-calendario` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
| `:core:network` | `AvailabilityApi`: disponibilidad por rango, crear y borrar bloqueo. DTO con `id` opcional. |
| `:core:data` | `AvailabilityRepositoryImpl` con el `safeCall` de siempre. |
| `:core:domain` | `VenueDay`, `BlockReason`, `AvailabilityRepository`; `OwnerDashboard` calculado de salones, cotizaciones y reservaciones (función pura con pruebas). |
| `:app` `ui/ownerhome` | `OwnerHomeViewModel` + pantalla; `ownerHome` deja de abrir «Mis salones». |
| `:app` `ui/ownercalendar` | `OwnerCalendarViewModel` + pantalla con mes, días, detalle del día y hoja de bloqueo. |
| `:app` `demo/RoleWorkspace` | `ownerHome` y `ownerCalendar` pasan a pantallas conectadas. |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Cálculo del tablero; marcas del mes; empalme; `ViewModel` con éxito, error, 401 y 409. |
| Integración | MockWebServer: rutas, query `from`/`to`, cuerpo del bloqueo, 201/400/404/409 e `id` opcional. |
| UI | Inicio con datos y vacío; calendario con días marcados, detalle y hoja de bloqueo; fuente 2.0. |
