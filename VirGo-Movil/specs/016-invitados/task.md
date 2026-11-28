# Tareas — 016 Invitados

| Campo | Valor |
|---|---|
| Módulo | `016-invitados` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25). T1601–T1603 autorizadas por la excepción ampliada; T1604 espera el despliegue. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T1601 | Red, dominio y datos de invitados | `007` T701 | `dev-android` | Pipeline en verde | ✅ |
| T1602 | UI D32: lista, totales, formulario y eliminar; C31 compartir | T1601 | `dev-android` | Pipeline en verde | ✅ |
| T1603 | Calidad: pruebas, accesibilidad y pipeline | T1602 | `dev-android` | Pipeline en verde | ✅ |
| T1604 | Humo contra producción | T1603; backend desplegado | `qa-movil` | `check.md` y `memoria.md` | ⛔ |
