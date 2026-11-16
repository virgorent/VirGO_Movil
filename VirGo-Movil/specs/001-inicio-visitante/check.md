# Check — 001 Inicio para visitantes

| Campo | Valor |
|---|---|
| Módulo | `001-inicio-visitante` |
| Estado del spec | Aprobado; 5/5 tareas hechas, cierre pendiente de la aprobación del PR |
| Revisó | `qa-movil` (opencode), 2026-11-16 |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Pantalla A1 en el emulador `Pixel_10_Pro` con los 5 pines y el selector de rango: `docs/evidencia/CA-01-inicio-visitante-a1.png` | ✅ |
| CA-02 | Vista previa A3 tras tocar el pin de «J. Magnolia» (nombre, zona, ★ 4.8, capacidad 160, «Ver más…» y aviso de cuenta): `docs/evidencia/CA-02-inicio-visitante-a3-preview.png` | ✅ |
| CA-03 | Toda falla del catálogo cae en un único mensaje sin texto técnico («No pudimos cargar los salones. Revisa tu conexión e inténtalo de nuevo.»): mapeo 500 → `AppError.Server` probado en `VenueRepositoryImplTest.serverErrorResponseMapsToServerError` y `hasError` en `GuestHomeViewModelTest.failedLoadShowsErrorAndKeepsSampleVenues`; el mensaje renderizado está en `docs/evidencia/CA-04-inicio-visitante-sin-conexion.png` | ✅ |
| CA-04 | Con el modo avión activo y arranque en frío, la pantalla muestra el estado de error con el botón «Reintentar»: `docs/evidencia/CA-04-inicio-visitante-sin-conexion.png`; el reintento se verificó en vivo (al reactivar la conexión y tocar «Reintentar», la petición se repite y el error desaparece) | ✅ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: **76** (`app` 37 con `GuestHomeViewModelTest` 13 y `LoginViewModelTest` 7; `:core:domain` 35 con `GetVenuesUseCaseTest` 3; `:core:common` 4) — suite completa: **123 pruebas, 0 fallos** | ✅ |
| Integración | Totales: **19** con MockWebServer (`VenueRepositoryImplTest` 7, `AuthRepositoryImplTest` 8, `HealthRepositoryImplTest` 4) | ✅ |
| UI | Totales: **15** con Robolectric/Compose (`SmokeTest` 4, `DesignSystemComponentsTest` 5, `NavigationTest` 2, `FontScaleTest` 2, `DiagnosticsScreenTest` 2) | ✅ |
| Seguridad | **13**: `TokenStoreTest` 4 (token cifrado, nunca en claro) + `AuthInterceptorTest` 3 + `CorrelationIdInterceptorTest` 2 + `OkHttpClientConfigurationTest` 4 (Bearer, correlación, timeouts y logging solo en debug) | ✅ |
| Mutación | Puntaje: **61 %** (`37/61`: `:core:domain` 62 % = 36/58 con test strength 97 %, `:core:common` 33 % = 1/3) | ✅ |
| Humo | APK *debug* contra producción (2026-11-16): `GET /api/v1/venues` → **200** en 8738 ms con `{"data":[]}` → muestra de demostración en A1; pin → vista previa A3; modo avión → estado de error con «Reintentar» y recuperación al reintentar. Evidencia: `docs/evidencia/CA-01-inicio-visitante-a1.png`, `docs/evidencia/CA-02-inicio-visitante-a3-preview.png` y `docs/evidencia/CA-04-inicio-visitante-sin-conexion.png` | ✅ |

## 3. PR y release

- [x] PR: https://github.com/virgorent/VirGO_Movil/pull/18 (`feat(T103,T105): catalogo de salones conectado (001-inicio-visitante 5/5)`).
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md`.
