# Check — 026 Perfil del proveedor

| Campo | Valor |
|---|---|
| Módulo | `026-proveedor-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `ProvidersTest.publishingWithoutPackagesIsBlockedAndSaysWhy`; `ProviderRulesTest.publishingNeedsEveryFieldAndAPricedPackage` | ✅ |
| CA-02 | `ProvidersRepositoryImplTest.withoutProfileMineIsEmptyAndPublishSendsOnlyTheStatus` | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
| CA-03 | `ProvidersRepositoryImplTest` (`GET /providers/me`) y `ProviderProfileViewModel` carga el borrador | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
