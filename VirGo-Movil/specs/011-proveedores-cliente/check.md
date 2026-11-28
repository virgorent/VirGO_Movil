# Check — 011 Proveedores para el cliente

| Campo | Valor |
|---|---|
| Módulo | `011-proveedores-cliente` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `ProvidersTest.catalogFiltersByCategoryAndComparesAtMostThree`; `ProvidersRepositoryImplTest` (filtro `category`) | 🟡 Unitaria ✅ · emulador: «llegará pronto» hasta que el backend implemente el endpoint |
| CA-02 | `ProvidersTest.catalogFiltersByCategoryAndComparesAtMostThree`, `compareHighlightsTheBestRated` | 🟡 Unitaria ✅ · emulador: «llegará pronto» hasta que el backend implemente el endpoint |
| CA-03 | `QuoteRequestViewModel.start(QuoteTarget.provider…)` envía `targetType`; el backend aún no acepta `provider` | 🟡 Unitaria ✅ · depende del backend |
| CA-04 | `ProvidersTest.missingEndpointSaysComingSoon`; `ProvidersRepositoryImplTest.missingEndpointIsComingSoon` | ✅ Unitaria y emulador (2026-11-27) |
