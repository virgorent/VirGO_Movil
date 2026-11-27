# Tareas — 010 Mis reservaciones: buscar por folio y detalle

| Campo | Valor |
|---|---|
| Módulo | `010-reservaciones` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26) |
| Fecha | 2026-11-26 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

Las tareas T1001–T1005 de la v1.0.0 se sustituyen. El spec se aprobó el 2026-11-26.

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T1001 | `BookingFolio` y `FindBookingByFolioUseCase` | — | `dev-android` | Pruebas unitarias en verde | ⏳ |
| T1002 | `BookingsApi.byFolio` y repositorio | T1001 | `dev-android` | MockWebServer 200/404/401/500/sin red | ⏳ |
| T1003 | Campo «Buscar por folio» en «Mis reservaciones» y estado en `BookingsViewModel` | T1002 | `dev-android` | Pruebas de `ViewModel` y de UI | ⏳ |
| T1004 | Bloque informativo de RF-07 en el comprobante | — | `dev-android` | Prueba de UI: sin botones de cancelar | ⏳ |
| T1005 | Accesibilidad y seguridad | T1003, T1004 | `qa-movil` | Fuente 2.0 a 320 dp; folio fuera de logs | ⏳ |
| T1006 | Humo contra producción tras el despliegue | Despliegue del backend | `qa-movil` | `check.md` completo | ⛔ |
