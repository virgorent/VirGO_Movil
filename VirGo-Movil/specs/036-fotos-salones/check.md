# Check — 036 Fotos de los salones

| Campo | Valor |
|---|---|
| Módulo | `036-fotos-salones` |
| Versión | 1.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando») |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `BetaCatalogScreen` usa `VirGoCoverPhoto` con la primera foto; `VenueDetailRepositoryTest` mapea `photos` | ✅ Unitaria y emulador (2026-11-27) |
| CA-02 | `VirGoPhotoTest.pagerAnnouncesEachPhotoAndUpdatesTheIndicator` | ✅ Unitaria y emulador (2026-11-27) |
| CA-03 | `VirGoPhotoTest.onlyHttpsUrlsAreShown`, `withoutPhotosOrWithAnUnsafeUrlNoImageIsRequested` | ✅ Unitaria y emulador (2026-11-27) (VirGo Demo Venue sin fotos) |
| CA-04 | `VirGoPhotoTest.pagerAnnouncesEachPhotoAndUpdatesTheIndicator` (descripción por foto) | ✅ Unitaria y árbol de accesibilidad en emulador («Foto 1 de 3 de Jardín Los Encinos») |
