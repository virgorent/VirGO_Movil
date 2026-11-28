# Check — 036 Fotos de los salones

| Campo | Valor |
|---|---|
| Módulo | `036-fotos-salones` |
| Versión | 1.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando») |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `BetaCatalogScreen` usa `VirGoCoverPhoto` con la primera foto; `VenueDetailRepositoryTest` mapea `photos` | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
| CA-02 | `VirGoPhotoTest.pagerAnnouncesEachPhotoAndUpdatesTheIndicator` | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
| CA-03 | `VirGoPhotoTest.onlyHttpsUrlsAreShown`, `withoutPhotosOrWithAnUnsafeUrlNoImageIsRequested` | 🟡 Unitaria ✅ · emulador pendiente (pase único al final del MVP) |
| CA-04 | `VirGoPhotoTest.pagerAnnouncesEachPhotoAndUpdatesTheIndicator` (descripción por foto) | 🟡 Unitaria ✅ · TalkBack real pendiente al final |
