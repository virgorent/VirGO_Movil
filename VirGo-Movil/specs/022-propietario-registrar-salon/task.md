# Tareas — 022 Propietario: registrar salón

| Campo | Valor |
|---|---|
| Módulo | `022-propietario-registrar-salon` |
| Versión | 2.0.0 |
| Estado | Propuesto para aprobación (lote MVP). **Las tareas no se ejecutan hasta aprobar este archivo y desbloquear `000`.** |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T2201 | Red: `OwnerVenuesApi` y DTOs | `000`; P-01–P-04 decididas | `dev-android` | MockWebServer: 201, 200, 400, 401, 403, 500, timeout | ⏳ |
| T2202 | Dominio: modelos, validación por paso, guardar y publicar | T2201 | `dev-android` | Unitarias y Pitest en verde | ⏳ |
| T2203 | Datos: `OwnerVenuesRepositoryImpl` y módulo Hilt | T2201, T2202 | `dev-android` | Integración: un solo `POST` por registro | ⏳ |
| T2204 | UI Mis salones: lista, estados, vacío y entrada del asistente | T2203; `030` (rol) | `dev-android` | Compose Testing CA-01 | ⏳ |
| T2205 | UI asistente P3–P5: validación, borrador, publicar y edición | T2203 | `dev-android` + `ui-designsystem` | Compose Testing CA-02–CA-06 | ⏳ |
| T2206 | Retomar borrador desde Mis salones | T2204, T2205; `006` T601 | `dev-android` | Compose Testing CA-07 | ⏳ |
| T2207 | Calidad: accesibilidad, seguridad y pipeline completo | T2204–T2206 | `qa-movil` | Pipeline en verde; CA-08 | ⏳ |
| T2208 | Humo: publicar un salón de prueba y verlo en el catálogo; cierre | T2207; backend `025`–`027`, `029` desplegados | `qa-movil` | `check.md` y `memoria.md` | ⏳ |

## Matriz de trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01, RF-10 | CA-01 | T2204 |
| RF-02, RF-03, RF-07 | CA-02 | T2202, T2205 |
| RF-04, RF-05 | CA-03, CA-04, CA-06 | T2203, T2205 |
| RF-06 | CA-05 | T2205, T2208 |
| RF-08 | CA-07 | T2206 |
| RF-09 | CA-08 | T2205, T2207 |
