# Tareas — 006 Detalle del salón

| Campo | Valor |
|---|---|
| Módulo | `006-detalle-salon` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25). Tareas T601–T605 autorizadas por la excepción ampliada ([`excepcion-030-sin-cierre-000.md`](../../docs/diseno/excepcion-030-sin-cierre-000.md)); T606 espera el despliegue de backend. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T601 | Red: `VenuesApi.venue(id)` y `VenueDetailDto` tolerante | PR #35; `000` | `dev-android` | MockWebServer: 200 completo/mínimo, 404, 500, timeout | ✅ |
| T602 | Dominio y datos: `VenueDetail`, `GetVenueDetailUseCase`, `VenueRepository.venue(id)` | T601 | `dev-android` | Unitarias y Pitest en verde | ✅ |
| T603 | UI: `VenueDetailScreen` + `ViewModel` con estados, precio por sesión y acciones por rol | T602 | `dev-android` + `ui-designsystem` | Compose Testing CA-02–CA-07; textos en `strings.xml` | ✅ |
| T604 | Navegación: «Ver detalle» en el catálogo, `VenueDetailRoute`, registro con `venueId` y regreso | T603 | `dev-android` | `NavigationTest`: catálogo → detalle → registro → detalle | ✅ |
| T605 | Calidad: accesibilidad 320 dp/2.0, seguridad y pipeline completo | T604 | `qa-movil` | Pipeline en verde | ✅ |
| T606 | Humo contra producción y cierre (`check.md`, `memoria.md`) | T605; backend `024` desplegado; 1 salón publicado | `qa-movil` | CA-01 contra producción | ⛔ |

## Matriz de trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01 | CA-01 | T604 |
| RF-02, RF-03, RF-04, RF-05 | CA-01, CA-02 | T601, T602, T603 |
| RF-06 | CA-03, CA-04 | T603 |
| RF-07, RF-08 | CA-03, CA-04 | T603, T604 |
| RF-09 | CA-05 | T603 |
| RF-10 | CA-06, CA-07 | T602, T603 |
| RF-11, RF-12 | CA-08 | T603, T605 |
