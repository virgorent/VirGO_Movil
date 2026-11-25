# Tareas — 009 Reservar (sin cobro en la app)

| Campo | Valor |
|---|---|
| Módulo | `009-reservar-pagar` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25). **Tareas ⛔ por el Art. II.2** hasta cerrar `000` y recibir la URL y el JSON de backend (o excepción aprobada). |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T901 | Red: `BookingsApi` con `Idempotency-Key` y DTOs | `000`; P-01 decidida | `dev-android` | MockWebServer: 201, 400, 401, 404, 500, timeout; encabezado presente | ⏳ |
| T902 | Dominio: modelos, validación y casos de uso con clave de idempotencia | T901 | `dev-android` | Unitarias y Pitest en verde | ⏳ |
| T903 | Datos: `BookingsRepositoryImpl` y módulo Hilt | T901, T902 | `dev-android` | Integración: reintento con la misma clave | ⏳ |
| T904 | UI C8: confirmación, aviso sin cobro, validación y envío único | T903; `006` T603 | `dev-android` + `ui-designsystem` | Compose Testing CA-01–CA-05 | ⏳ |
| T905 | UI C23: comprobante con datos del servidor | T904 | `dev-android` | Compose Testing CA-06 | ⏳ |
| T906 | UI Mis reservaciones: lista, vacío, error y apertura del comprobante; sección del espacio del cliente | T903 | `dev-android` | Compose Testing CA-07; `NavigationTest` | ⏳ |
| T907 | Calidad: accesibilidad, seguridad (sin datos de pago) y pipeline completo | T904–T906 | `qa-movil` + `seguridad-ci` | Pipeline en verde; CA-08 | ⏳ |
| T908 | Humo contra producción y cierre | T907; backend `009`, `066`, `067` desplegados; salón de prueba | `qa-movil` | `check.md` y `memoria.md` | ⏳ |

## Matriz de trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01, RF-02 | CA-01, CA-02, CA-03 | T902, T904 |
| RF-03, RF-07 | CA-06 | T904, T905 |
| RF-04, RF-05 | CA-01, CA-04, CA-05 | T901, T903, T904 |
| RF-06 | CA-06 | T905 |
| RF-08, RF-09 | CA-07 | T906 |
| RF-10 | CA-08 | T905–T907 |
