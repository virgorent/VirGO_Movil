# Check — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Estado del spec | Borrador |
| Revisó | — |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Captura B8 en emulador `Pixel_10_Pro`: `docs/evidencia/CA-01-explorar-b8-mapa.png` | ✅ |
| CA-02 | Captura C9 en emulador: `docs/evidencia/CA-02-explorar-c9-lista.png` | ✅ |
| CA-03 | Captura C10 en emulador: `docs/evidencia/CA-03-explorar-c10-filtros.png` | ✅ |
| CA-04 | Captura C11 en emulador: `docs/evidencia/CA-04-explorar-c11-recomendados.png` | ✅ |
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

- [x] PR: [#27](https://github.com/virgorent/VirGO_Movil/pull/27) (apilado sobre #19)
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md` (2026-11-18, implementación de `005`).
