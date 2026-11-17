# Tareas — 001 Inicio para visitantes

| Campo | Valor |
|---|---|
| Módulo | `001-inicio-visitante` |
| Versión | 1.0.0 |
| Estado | Aprobado (2026-11-12, aprobación retroactiva; ver `memoria.md`). 5/5 tareas hechas. |
| Fecha | 2026-11-05 |
| Rol | Visitante |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T101 | Pantalla A1 Inicio para visitantes y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T102 | Pantalla A3 Vista previa del salón y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T103 | Repositorio y casos de uso | T101 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T104 | Pruebas unitarias, de UI y de seguridad | T102 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T105 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo | ✅ |

**Evidencia:** T101/T102 en commit `ec2882c` (refactor MVVM con UiState). T104: `ktlintCheck detekt testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL (2026-11-09); `:core:domain:test` 30 pruebas y `:app:testDebugUnitTest` 29 pruebas, 0 fallos.

> **T103** ✅ con el contrato `VirGo-API/specs/007-venues-catalog/plan.md`: `GET /api/v1/venues?zone=&category=` → `200 {"data":[…]}`. En `:core:network`, `VenuesApi` + `VenueDtos` (`VenuesResponseDto`, `VenueDto` con defaults, `VenueRatingDto` y `VenueLocationDto` GeoJSON `{type, coordinates:[lng,lat]}`), `createVenuesApi` en `NetworkClientFactory` y `provideVenuesApi` en `NetworkModule`; en `:core:domain`, `Venue` (con `latitude`/`longitude` opcionales), `VenueRepository` y `GetVenuesUseCase`; en `:core:data`, `VenueRepositoryImpl` (reutiliza `Throwable.toAppError()`, 404 → `NotFound`, 500 → `Server`, sin conexión → `Network`) y `VenueModule` (`@Binds`); en `:core:testing`, `FakeVenueRepository`; en `:app`, `GuestHomeViewModel` con `getVenues` (carga en `init` y `refresh()` para el botón de reintento), estados `isLoading`/`hasError` y mapeo `Venue` → `VenueSample` con haversine y rumbo desde el centro fijo de Juriquilla (`VenueMapping.kt`); si el catálogo viene **vacío** (estado actual de producción) se conserva la muestra local `VenueSamples`, y si la llamada **falla** se muestra el error sin perder el mapa. **Pruebas:** `VenueRepositoryImplTest` 7 con MockWebServer (200 con mapeo completo, `?zone=&category=`, catálogo vacío, venue sin `location` → coordenadas nulas, 404, 500 y sin conexión) y `GetVenuesUseCaseTest` 3 con fake local en su propia suite (`:core:domain` no puede depender de `:core:testing`). **Pipeline local en verde:** `ktlintCheck detekt lint testDebugUnitTest` + `:core:domain:test` + `:core:common:test` + `pitest` + `assembleDebug` → BUILD SUCCESSFUL con **123 pruebas, 0 fallos** y mutación **61 %** (37/61).
> **T105** ✅ con humo contra producción (emulador `Pixel_10_Pro`, 2026-11-16): APK *debug* instalado y arrancado limpio (`pm clear`), la pantalla A1 disparó `GET https://rcof55a3c9.execute-api.us-east-1.amazonaws.com/api/v1/venues` → **200** en 8738 ms con `{"data":[]}` (catálogo aún vacío en producción) → fallback a `VenueSamples` con «5 salones a 5 km» y los 5 pines (RF-04/RF-05). Tocar el pin de «J. Magnolia» abrió la vista previa A3 con «Jardín Magnolia · El Refugio · ★ 4.8 · hasta 160» (RF-09/RF-10). Con el modo avión activo y arranque en frío, el catálogo falla y aparece «No pudimos cargar los salones. Revisa tu conexión e inténtalo de nuevo.» con el botón «Reintentar» (CA-04); al reactivar la conexión y tocar «Reintentar», la llamada se repite y el error desaparece. Evidencia: `docs/evidencia/CA-01-inicio-visitante-a1.png`, `docs/evidencia/CA-02-inicio-visitante-a3-preview.png` y `docs/evidencia/CA-04-inicio-visitante-sin-conexion.png`.
