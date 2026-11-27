# Check — 010 Mis reservaciones: buscar por folio y detalle

| Campo | Valor |
|---|---|
| Módulo | `010-reservaciones` |
| Versión del spec | 2.0.0 |
| Estado del spec | Aprobado por el usuario (2026-11-26) |
| Revisó | — |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `BookingsViewModelTest.ownFolioOpensItsReceiptNormalized`; `BookingsRepositoryImplTest.byFolioPutsTheFolioInThePathAndMapsErrors` | ✅ |
| CA-02 | `BookingTest.folioIsNormalizedAndValidated`; `BookingsViewModelTest.badFormatNeverReachesTheServer`; `BookingsScreensTest.folioSearchShowsFormatErrorAndSearches` | ✅ |
| CA-03 | `BookingsViewModelTest.unknownFolioAndNetworkErrorsAreDistinct`; `BookingsScreensTest.unknownFolioMessageDoesNotHideTheList` | ✅ |
| CA-04 | `BookingsViewModelTest.unknownFolioAndNetworkErrorsAreDistinct`; `BookingsRepositoryImplTest.byFolioWithoutNetworkMapsToNetwork` | ✅ |
| CA-05 | `BookingsScreensTest.receiptExplainsChangesWithoutCancelButtons` | ✅ |

## 2. Pruebas

Corrida del 2026-11-26 sobre la punta del #51 con estos cambios: `ktlintCheck detekt testDebugUnitTest` **BUILD SUCCESSFUL**, 583 pruebas y 0 fallas en todo el proyecto.

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `BookingTest` (5) y `BookingsViewModelTest` (8) | ✅ |
| Integración | `BookingsRepositoryImplTest` (7): ruta, 200/404/401/500 y sin red | ✅ |
| UI | `BookingsScreensTest` (10) | ✅ |
| Seguridad | El folio va en la ruta, no en la query (prueba de integración); no se agregaron logs | ✅ |
| Accesibilidad | Etiqueta y error del campo en semántica; **falta** revisión con TalkBack y fuente 2.0 a 320 dp | ⏳ |
| Humo | APK contra producción tras el despliegue | ⏳ |

## 3. PR y release

- [ ] PR: enlace —
- [ ] Una revisión aprobada.
- [ ] Aceptación visual de `ccabrera-dev8`.

## 4. Memoria y aprendizaje

- [ ] Entrada en `memoria.md`.
