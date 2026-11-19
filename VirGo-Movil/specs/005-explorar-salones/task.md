# Tareas — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Versión | 1.0.0 |
| Estado | Borrador |
| Fecha | 2026-11-05 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T501 | Pantalla B8 Explorar (mapa) y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T502 | Pantalla C9 Explorar (lista) y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T503 | Pantalla C10 Filtros y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T504 | Pantalla C11 Recomendados para ti y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T505 | Repositorio y casos de uso | T501 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T506 | Pruebas unitarias, de UI y de seguridad | T504 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T507 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo | ⏳ |

**Evidencia:**

> **T501** ✅ Pantalla B8 (mapa) con `ExploreMapContent`, tarjeta resumen del salón seleccionado, marcador de favorito y estados de carga/error/vacío. La cabecera (marca, campana de avisos con contador, buscador, categorías y conmutador mapa/lista) vive en `ExploreScreen`/`ExploreHeader`; el `ExploreViewModel` es único para B8/C9/C10/C11 (`e09071f`).

> **T502** ✅ Pantalla C9 (lista) con `ExploreListContent`: tarjetas con foto, calificación, capacidad, precio y distancia (`ExplorePriceDistanceRow` en `ExploreVenueParts.kt`), orden recomendado/precio/distancia y cambio a mapa al tocar un salón (`e09071f`).

> **T503** ✅ Hoja C10 de filtros (`ExploreFiltersPanel` en `ExplorePanel.kt` + `ExploreFilters.kt`): zona, invitados, presupuesto, fecha, distancia y categorías; validación de campos (RF-13/RF-14/RF-16) que deja el panel abierto y marca el error; botones Aplicar/Limpiar (`e09071f`).

> **T504** ✅ Hoja C11 recomendaciones (`ExploreRecommendedPanel`): score local 0..100, `ExploreReason`, badge «Estimación IA», máx. 5 sugerencias y filtro por tipo de evento (`e09071f`).

> **T505** ✅ Contrato `VenueDto` ampliado aditivo (precio, categorías, fotos con defaults), endpoints `FavoritesApi`/`NoticesApi` con sus DTOs, `FavoritesRepositoryImpl`/`NoticesRepositoryImpl` y casos de uso `GetFavorites`, `GetNotices`, `ToggleFavorite` (`94f3a73`). Verificado con MockWebServer: 6 pruebas de favoritos (alta/baja/listado, 404/409/sin conexión) y 5 de avisos (leído/no leído, vacío, 404/500/sin conexión) en verde (`e77b50b`).

> **T506** ✅ `./gradlew ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug` → **BUILD SUCCESSFUL**; **193 pruebas, 0 fallos** en 38 suites (19 `ExploreViewModelTest`, 6 `ExploreScreenTest` de UI con Robolectric) y mutación combinada **59%** (`:core:domain` 60%, 57/95, strength 93%; `:core:common` 33%) (`f298967`).
