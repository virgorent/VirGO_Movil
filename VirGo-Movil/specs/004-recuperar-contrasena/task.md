# Tareas — 004 Recuperar contraseña

| Campo | Valor |
|---|---|
| Módulo | `004-recuperar-contrasena` |
| Versión | 1.0.0 |
| Estado | Aprobado (2026-11-12, aprobación retroactiva; ver `memoria.md`). 4/5 tareas hechas; T405 bloqueada por el backend. |
| Fecha | 2026-11-05 |
| Rol | Visitante |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T401 | Pantalla A6 Recuperar contraseña y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T402 | Pantalla A7 Nueva contraseña y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T403 | Repositorio y casos de uso | T401 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T404 | Pruebas unitarias, de UI y de seguridad | T402 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T405 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo | ⛔ |

**Evidencia:** T401/T402 en commit `ec2882c` (refactor MVVM con UiState). T404: `ktlintCheck detekt testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL (2026-11-09); `:core:domain:test` 30 pruebas y `:app:testDebugUnitTest` 29 pruebas, 0 fallos. CA-01 y CA-02 con capturas en `docs/evidencia/` (2026-11-16).

> **T403** ✅ contra los specs `011-post-auth-password-forgot` y `012-post-auth-password-reset` del backend (ambos en **Borrador**), aplicando el riesgo del plan: «MockWebServer con el contrato del spec de API». En `:core:network`, `AuthApi.forgotPassword`/`resetPassword` + `ForgotPasswordRequestDto` (`{"email":…}`) y `ResetPasswordRequestDto` (`{"code":…,"password":…}`); en `:core:domain`, `RecoveryRepository`, `ForgotPasswordUseCase` y `ResetPasswordUseCase`; en `:core:data`, `RecoveryRepositoryImpl` (reutiliza `Throwable.toAppError()`: 400 → `Validation`, 404 → `NotFound`, 500/429 → `Server`, sin conexión → `Network`) y `RecoveryModule` (`@Binds`); en `:core:testing`, `FakeRecoveryRepository`. **Pruebas:** `RecoveryRepositoryImplTest` 8 con MockWebServer (202 con ruta y cuerpo, 400 con `errors`, 429, 500, sin conexión, 200 con cuerpo, 400 genérico de código incorrecto y sin conexión en reset) + `ForgotPasswordUseCaseTest` 2 y `ResetPasswordUseCaseTest` 2 con fake local en su propia suite (`:core:domain` no puede depender de `:core:testing`). **Pipeline local en verde:** `ktlintCheck detekt lint testDebugUnitTest` + `:core:domain:test` + `:core:common:test` + `pitest` + `assembleDebug` → BUILD SUCCESSFUL con **119 pruebas, 0 fallos** y mutación **72 %** (38/53).
> **T405** ⛔ bloqueada por el backend: `POST /api/v1/auth/password/forgot` responde **404** en producción (2026-11-16) con el mismo Problem Details que una ruta inexistente de control, los specs `011`/`012` siguen en Borrador y sus tareas `T1101`–`T1103` están ⏳. La P-01 (no hay mecanismo de envío de correo aprobado) además impide que la app reciba el código real, así que `RecoverViewModel` conserva por ahora el flujo aprobado en RF-02 (código generado localmente y mostrado en pantalla) y la conexión a los casos de uso reales se hará al integrar, cuando el backend entregue el JSON probado.
