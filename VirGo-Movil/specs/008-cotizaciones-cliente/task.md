# Tareas — 008 Cotizaciones del cliente

| Campo | Valor |
|---|---|
| Módulo | `008-cotizaciones-cliente` |
| Versión | 2.0.0 |
| Estado | Propuesto para aprobación (lote MVP). **Las tareas no se ejecutan hasta aprobar este archivo y desbloquear `000`.** |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T801 | Red: `QuotesApi` y DTOs | `000`; P-01 decidida | `dev-android` | MockWebServer: 201, 400, 401, 404, 500, timeout | ⏳ |
| T802 | Dominio: modelos, `ValidateQuoteRequestUseCase` y casos de uso | T801 | `dev-android` | Unitarias y Pitest en verde | ⏳ |
| T803 | Datos: `QuotesRepositoryImpl`, errores de campo y módulo Hilt | T801, T802 | `dev-android` | Integración con el contrato | ⏳ |
| T804 | UI C21: formulario, validación, aviso de capacidad y envío único | T803; `006` T603 | `dev-android` + `ui-designsystem` | Compose Testing CA-01–CA-04 | ⏳ |
| T805 | UI C22: lista, estados, vacío, error y actualizar | T803 | `dev-android` | Compose Testing CA-05, CA-06 | ⏳ |
| T806 | UI C7: detalle con/sin respuesta y «Reservar» hacia `009`; sección del espacio del cliente | T805 | `dev-android` | Compose Testing CA-07, CA-08; `NavigationTest` | ⏳ |
| T807 | Calidad: accesibilidad, seguridad y pipeline completo | T804–T806 | `qa-movil` | Pipeline en verde; CA-09 | ⏳ |
| T808 | Humo contra producción y cierre | T807; backend `060`/`061` desplegados | `qa-movil` | `check.md` y `memoria.md` | ⏳ |

## Matriz de trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01–RF-05 | CA-01, CA-02, CA-04 | T802, T804 |
| RF-06 | CA-03, CA-04 | T803, T804 |
| RF-07–RF-09 | CA-05, CA-06 | T805 |
| RF-10, RF-11 | CA-07, CA-08 | T806 |
| RF-12, RF-13 | CA-09 | T806, T807 |
