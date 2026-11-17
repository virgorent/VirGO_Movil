# Check — 003 Iniciar y cerrar sesión

| Campo | Valor |
|---|---|
| Módulo | `003-iniciar-sesion` |
| Estado del spec | Aprobado; 4/4 tareas hechas |
| Revisó | `qa-movil` (opencode), 2026-11-16 |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Pantalla A5 en el emulador `Pixel_10_Pro` contra producción: hero «Salones cerca de ti», «Qué gusto verte», botones Google/Apple, campos de correo y contraseña, enlace «¿Olvidaste tu contraseña?», «Entrar» y «Regístrate»: `docs/evidencia/CA-01-iniciar-sesion-a5.png` | ✅ |
| CA-02 | Credenciales inexistentes contra `POST /api/v1/auth/login` (401 real, 2026-11-16) → «Correo o contraseña incorrectos», sin decir cuál de los dos falló ni texto técnico (RF-02): `docs/evidencia/CA-02-iniciar-sesion-error.png` | ✅ |
| CA-03 | Modo avión → «Sin conexión. Revisa tu internet e inténtalo de nuevo.» con «Entrar» habilitado para reintentar; tras restaurar la red, el reintento completa el login (RF-03): `docs/evidencia/CA-03-iniciar-sesion-sin-conexion.png` | ✅ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: **67** (`:core:domain` 32 con `ValidateLoginFormUseCaseTest` 6; `:core:common` 4; `app` 31 con `LoginViewModelTest` 7) — suite completa: **107 pruebas, 0 fallos** (24 suites) | ✅ |
| Integración | Totales: **12** con MockWebServer (`AuthRepositoryImplTest` 8: 200 con path/body, 401, 400 con `errors`, 500 con `traceId`, 404, sin conexión, timeout y 200 malformado; `HealthRepositoryImplTest` 4) | ✅ |
| UI | Totales: **15** con Robolectric/Compose (`SmokeTest` 4, `DesignSystemComponentsTest` 5, `NavigationTest` 2, `FontScaleTest` 2, `DiagnosticsScreenTest` 2) | ✅ |
| Seguridad | **13**: `TokenStoreTest` 4 (token cifrado, nunca en claro) + `AuthInterceptorTest` 3 + `CorrelationIdInterceptorTest` 2 + `OkHttpClientConfigurationTest` 4 (Bearer, correlación, timeouts y logging solo en debug) | ✅ |
| Mutación | Puntaje: **71 %** (`36/51`: `:core:domain` 73 % = 35/48 con test strength 97 %, `:core:common` 33 % = 1/3) | ✅ |
| Humo | APK *debug* contra producción (2026-11-16) con la cuenta QA: login real → home del rol (RF-03), 401 con mensaje genérico (CA-02) y sin conexión + reintento (CA-03): `docs/evidencia/RF-03-iniciar-sesion-inicio-rol.png` | ✅ |

## 3. PR y release

- [x] PR: https://github.com/virgorent/VirGO_Movil/pull/20 (`docs(T304): humo de login contra produccion (003 4/4)`).
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md`.
