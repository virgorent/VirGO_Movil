# Tareas — 014 Mi evento

| Campo | Valor |
|---|---|
| Módulo | `014-mi-evento` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25). T1401–T1402 autorizadas por la excepción ampliada; T1403 espera el despliegue. |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T1401 | UI D27: lista, vacío, detalle y accesos; rutas del espacio del cliente | `007` T701 | `dev-android` | Pipeline en verde | ✅ |
| T1402 | Calidad: pruebas, accesibilidad y pipeline | T1401 | `dev-android` | Pipeline en verde | ✅ |
| T1403 | Humo contra producción | T1402; backend desplegado | `qa-movil` | `check.md` y `memoria.md` | ⛔ |
