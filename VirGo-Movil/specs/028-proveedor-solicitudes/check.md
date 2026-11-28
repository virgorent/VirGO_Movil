# Check — 028 Solicitudes del proveedor

| Campo | Valor |
|---|---|
| Módulo | `028-proveedor-solicitudes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `ProviderRulesTest.conceptsStartFromPackagesAndRecalculateTheTotal`; `ProvidersTest.conceptsFillTheAnswerTotal` | ✅ |
| CA-02 | Reusa la respuesta de `024` (`OwnerRequests`) con sus pruebas | 🟡 Unitaria ✅ · emulador: «llegará pronto» hasta que el backend implemente el endpoint |
