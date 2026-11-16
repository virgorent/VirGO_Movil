# Check — 004 Recuperar contraseña

| Campo | Valor |
|---|---|
| Módulo | `004-recuperar-contrasena` |
| Estado del spec | Aprobado; 4/5 tareas hechas, T405 bloqueada por el backend (`011`/`012` en Borrador) |
| Revisó | `qa-movil` (opencode), 2026-11-16 |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Pantalla A6 en el emulador `Pixel_10_Pro` (kicker, título, campo de correo, aviso de 15 minutos y botón «Enviar código» deshabilitado hasta que el correo sea válido): `docs/evidencia/CA-01-recuperar-a6.png` | ✅ |
| CA-02 | Pantalla A7 tras «Enviar código» (correo como kicker, código de 6 dígitos, nueva contraseña y confirmación, «Reenviar código» y «Guardar contraseña»): `docs/evidencia/CA-02-recuperar-a7.png` | ✅ |
| CA-03 | Requiere la integración con la API (T405): en la capa de datos ya está probado el mapeo a mensaje sin texto técnico (`400` → `Validation`, `500`/`429` → `Server`, sin conexión → `Network` en `RecoveryRepositoryImplTest`), pero la pantalla aún no consume el endpoint | ⛔ |
| CA-04 | Requiere la integración con la API (T405): sin llamada real no hay estado de error con «Reintentar» que mostrar; el endpoint responde 404 en producción | ⛔ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: **71** (`:core:domain` 36 con `ForgotPasswordUseCaseTest` 2 y `ResetPasswordUseCaseTest` 2; `:core:common` 4; `app` 31 con `RecoverViewModelTest` 8) — suite completa: **119 pruebas, 0 fallos** | ✅ |
| Integración | Totales: **20** con MockWebServer (`RecoveryRepositoryImplTest` 8, `AuthRepositoryImplTest` 8, `HealthRepositoryImplTest` 4) | ✅ |
| UI | Totales: **15** con Robolectric/Compose (`SmokeTest` 4, `DesignSystemComponentsTest` 5, `NavigationTest` 2, `FontScaleTest` 2, `DiagnosticsScreenTest` 2) | ✅ |
| Seguridad | **13**: `TokenStoreTest` 4 (token cifrado, nunca en claro) + `AuthInterceptorTest` 3 + `CorrelationIdInterceptorTest` 2 + `OkHttpClientConfigurationTest` 4 (Bearer, correlación, timeouts y logging solo en debug) | ✅ |
| Mutación | Puntaje: **72 %** (`38/53`: `:core:domain` 74 % = 37/50 con test strength 97 %, `:core:common` 33 % = 1/3) | ✅ |
| Humo | APK *debug* contra producción (2026-11-16): `POST /api/v1/auth/password/forgot` → **404** (ruta inexistente; los specs `011`/`012` siguen en Borrador y sus tareas `T1101`–`T1103` están ⏳). Pantallas A6 y A7 humadas en el emulador con el flujo aprobado (código local): `docs/evidencia/CA-01-recuperar-a6.png`, `docs/evidencia/CA-02-recuperar-a7.png` y `docs/evidencia/RF-04-recuperar-codigo-incorrecto.png` (rechazo de código incorrecto, RF-04) | ⛔ |

## 3. PR y release

- [ ] PR: enlace —
- [ ] Una revisión aprobada.

## 4. Memoria y aprendizaje

- [x] Entrada en `memoria.md`.
