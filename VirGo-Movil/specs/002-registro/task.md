# Tareas — 002 Registro

| Campo | Valor |
|---|---|
| Módulo | `002-registro` |
| Versión | 1.0.0 |
| Estado | Aprobado (2026-11-12, aprobación retroactiva; ver `memoria.md`). 5/5 tareas hechas. |
| Fecha | 2026-11-05 |
| Rol | Visitante |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T201 | Pantalla A2 Registro y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T202 | Pantalla A4 Registro con errores y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T203 | Repositorio y casos de uso | T201 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T204 | Pruebas unitarias, de UI y de seguridad | T202 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T205 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo | ✅ |

**Evidencia:** T201/T202 en commit `ec2882c` (refactor MVVM con UiState). T204: `ktlintCheck detekt testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL (2026-11-09); `:core:domain:test` 30 pruebas y `:app:testDebugUnitTest` 29 pruebas, 0 fallos.

> **T203** ✅ con el contrato entregado por `VirGo-API` en `specs/005-auth-register/plan.md`: `POST /api/v1/auth/register` con `{"name","email","password","phone"}` → `201 {"id","name","email","phone","role","isVerified"}`, correo duplicado → `409`, datos inválidos → `400` en Problem Details y el rol inicial siempre `client`. En `:core:common` se agregó `AppError.Conflict` (409); en `:core:network`, `RegisterRequestDto`/`RegisterResponseDto` y `AuthApi.register`; en `:core:data`, `AuthRepositoryImpl.register` que convierte 409 a `Conflict` y mapea la respuesta a `RegisteredUser` (`:core:domain`); `RegisterUseCase` normaliza antes de enviar (trim, correo en minúsculas y prefijo `+52` al teléfono) y `RegisteredEmailStore.contains()` hace el fast-path de duplicado sin mutar el almacén. **Pruebas:** `AuthRepositoryImplTest` +3 con MockWebServer (201 → `RegisteredUser` y `path == /api/v1/auth/register` con los 4 campos en el cuerpo; 409 → `AppError.Conflict`; 400 → `AppError.Validation` con el campo `email`), `RegisterUseCaseTest` 2 (normalización exacta de nombre, correo, teléfono y contraseña; el fallo se propaga sin tocar) y `RegisteredEmailStoreTest` +1 (`contains` sin agregar). El test de dominio usa un fake local en su propia suite porque `:core:domain` no puede depender de `:core:testing` (dependencia circular). **Pipeline local en verde:** `ktlintCheck detekt lint testDebugUnitTest assembleDebug` + `:core:domain:test` + `:core:common:test` + `pitest` → BUILD SUCCESSFUL con **115 pruebas, 0 fallos**.
> **T205** ✅ con humo contra producción (emulador `Pixel_10_Pro`, 2026-11-14): con la app recién instalada y `API_BASE_URL` = `https://rcof55a3c9.execute-api.us-east-1.amazonaws.com/`, el formulario A2 completo (nombre, correo, teléfono y contraseña) mandó `POST /api/v1/auth/register` → **201** en 7524 ms con cuerpo de 167 bytes y navegó al inicio del rol (RF-09). Reiniciando la app con `force-stop` (para vaciar `RegisteredEmailStore`, que vive en memoria) y repitiendo con el mismo correo, el backend respondió **409** en 6394 ms y la pantalla mostró la píldora «Ya existe una cuenta con este correo. Inicia sesión.» sin cierre de la app (RF-08 y CA-03). El correo de prueba se normalizó a minúsculas en el envío. Evidencia: `docs/evidencia/T205-registro-201-exitoso.png` y `docs/evidencia/T205-registro-409-duplicado.png`. La prueba se corrió contra la API de producción, no contra un backend local, por lo que «API apagada» se simuló con el modo avión (CA-04).
