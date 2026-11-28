# Check — 027 Inicio y agenda del proveedor

| Campo | Valor |
|---|---|
| Módulo | `027-proveedor-inicio-agenda` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `ProviderRulesTest.gigsThatCrossTheSameDayOverlapButTouchingOnesDoNot`; `ProvidersTest.overlappingGigsAreFlagged` | ✅ |
| CA-02 | `ProviderRulesTest.weekStartsOnMondayAndKeepsOnlyItsGigs` | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
| CA-03 | `ProviderDashboard.from` cuenta las solicitudes sin responder | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
