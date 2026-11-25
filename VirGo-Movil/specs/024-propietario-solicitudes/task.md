# Tareas — 024 Solicitudes de cotización del propietario

| Campo | Valor |
|---|---|
| Módulo | `024-propietario-solicitudes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25). T2401–T2404 autorizadas por la excepción ampliada; T2405 espera el despliegue. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T2401 | Red, dominio y datos: `respond`, `QuoteAnswerValidator`, `clientName` | `008` | `dev-android` | Unitarias y MockWebServer | ✅ |
| T2402 | UI P7: lista con pestañas, vacío, error y actualizar | T2401 | `dev-android` | Compose Testing CA-01 | ✅ |
| T2403 | UI P8: detalle, formulario y envío único; errores | T2401 | `dev-android` | Compose Testing CA-02–CA-05 | ✅ |
| T2404 | Calidad: accesibilidad y pipeline completo | T2402, T2403 | `qa-movil` | Pipeline en verde; CA-06 | ✅ |
| T2405 | Humo contra producción | T2404; backend desplegado con PR #41 | `qa-movil` | `check.md` y `memoria.md` | ⛔ |
