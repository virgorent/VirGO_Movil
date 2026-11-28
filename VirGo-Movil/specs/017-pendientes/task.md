# Tareas — 017 Pendientes

| Campo | Valor |
|---|---|
| Módulo | `017-pendientes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25). T1701–T1703 autorizadas por la excepción ampliada; T1704 espera el despliegue. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T1701 | Red, dominio y datos de pendientes | `007` T701 | `dev-android` | Pipeline en verde | ✅ |
| T1702 | UI C27: grupos, marcar, formulario y eliminar | T1701 | `dev-android` | Pipeline en verde | ✅ |
| T1703 | Calidad: pruebas, accesibilidad y pipeline | T1702 | `dev-android` | Pipeline en verde | ✅ |
| T1704 | Humo contra producción | T1703; backend desplegado | `qa-movil` | `check.md` y `memoria.md` | ⛔ |
