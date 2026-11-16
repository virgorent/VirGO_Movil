# Check — 002 Registro

| Campo | Valor |
|---|---|
| Módulo | `002-registro` |
| Estado del spec | Aprobado; 5/5 tareas hechas, cierre pendiente de la aprobación del PR |
| Revisó | `qa-movil` (opencode), 2026-11-16 |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Pantalla A2 limpia en el emulador `Pixel_10_Pro`: `docs/evidencia/CA-01-registro-a2-formulario.png` | ✅ |
| CA-02 | Pantalla A4 con la píldora de error tras el `409` del backend: `docs/evidencia/T205-registro-409-duplicado.png` | ✅ |
| CA-03 | `POST /api/v1/auth/register` → **409** (6394 ms) mostrado como «Ya existe una cuenta con este correo. Inicia sesión.», sin texto técnico ni cierre de la app (logcat `okhttp.OkHttpClient: <-- 409`) | ✅ |
| CA-04 | Con el modo avión activo, el envío falla con `UnknownHostException` y aparece «No pudimos crear tu cuenta. Revisa tu conexión e inténtalo de nuevo.» con el botón «Crear cuenta» habilitado para reintentar: `docs/evidencia/CA-04-registro-sin-conexion.png` | ✅ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: **115 pruebas, 0 fallos** (`app` 43 + `core` 72; incluye `RegisterViewModelTest` 8, `RegisterUseCaseTest` 2, `RegisteredEmailStoreTest` 4 y `ValidateRegisterFormUseCaseTest` 8) | ✅ |
| Integración | Totales: **24** con MockWebServer (`AuthRepositoryImplTest` 11, incluye 201/409/400 de registro; `HealthRepositoryImplTest` 4; interceptores y cliente 9) | ✅ |
| UI | Totales: **15** con Robolectric/Compose (`SmokeTest` 4, `DesignSystemComponentsTest` 5, `NavigationTest` 2, `FontScaleTest` 2, `DiagnosticsScreenTest` 2) | ✅ |
| Seguridad | **13**: `TokenStoreTest` 4 (token cifrado, nunca en claro) + `AuthInterceptorTest` 3 + `CorrelationIdInterceptorTest` 2 + `OkHttpClientConfigurationTest` 4 (Bearer, correlación, timeouts y logging solo en debug) | ✅ |
| Mutación | Puntaje: **63 %** (`39/62`: `:core:domain` 64 % = 38/59 con test strength 95 %, `:core:common` 33 % = 1/3) | ✅ |
| Humo | APK *debug* contra producción (2026-11-14): registro → **201** en 7524 ms → «Inicio» (RF-09); duplicado tras `force-stop` → **409** en 6394 ms → píldora de duplicado (RF-08). Evidencia: `docs/evidencia/T205-registro-201-exitoso.png` y `docs/evidencia/T205-registro-409-duplicado.png` | ✅ |

## 3. PR y release

- [x] PR: https://github.com/virgorent/VirGO_Movil/pull/17 (`feat(T203,T205): registro conectado a la API (002-registro 5/5)`).
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md`.
