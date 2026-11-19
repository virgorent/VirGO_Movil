# Check — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Estado del spec | Borrador |
| Revisó | — |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Prueba o captura (pendiente captura en emulador) | 🔄 |
| CA-02 | Prueba o captura (pendiente captura en emulador) | 🔄 |
| CA-03 | Prueba o captura (pendiente captura en emulador) | 🔄 |
| CA-04 | Prueba o captura (pendiente captura en emulador) | 🔄 |
| CA-05 | `ExploreScreenTest.filtersWithNoMatchShowEmptyState` y `invalidGuestsKeepsFiltersPanelOpen` en verde | ✅ |
| CA-06 | `ExploreScreenTest` (6 pruebas de UI) en verde | ✅ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: 193 pruebas, 0 fallos (38 suites) | ✅ |
| Integración | MockWebServer: `FavoritesRepositoryImplTest` (6) y `NoticesRepositoryImplTest` (5) | ✅ |
| UI | `ExploreScreenTest` (6, Robolectric) | ✅ |
| Seguridad | Sin secretos en código; `movil-build-test` + `gitleaks` en el PR | 🔄 |
| Mutación | Puntaje: 59 % (`:core:domain` 60 % — 57/95, strength 93 %; `:core:common` 33 %) | ✅ |
| Humo | APK contra producción | ⏳ |

## 3. PR y release

- [ ] PR: enlace —
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md` (2026-11-18, implementación de `005`).
