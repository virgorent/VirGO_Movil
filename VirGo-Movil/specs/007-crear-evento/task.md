# Tareas — 007 Crear y editar evento

| Campo | Valor |
|---|---|
| Módulo | `007-crear-evento` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25). T701–T703 autorizadas por la excepción ampliada; T704 espera el despliegue. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T701 | Red, dominio y datos: `EventsApi`, `EventValidator`, `EventsRepository` | `000` | `dev-android` | Pipeline en verde | ✅ |
| T702 | UI C12: formulario de crear/editar con envío único | T701 | `dev-android` | Pipeline en verde | ✅ |
| T703 | Calidad: pruebas, accesibilidad y pipeline | T702 | `dev-android` | Pipeline en verde | ✅ |
| T704 | Humo contra producción | T703; backend desplegado | `qa-movil` | `check.md` y `memoria.md` | ⛔ |
