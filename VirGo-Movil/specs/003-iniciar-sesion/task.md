# Tareas — 003 Iniciar y cerrar sesión

| Campo | Valor |
|---|---|
| Módulo | `003-iniciar-sesion` |
| Versión | 1.0.0 |
| Estado | Borrador |
| Fecha | 2026-11-05 |
| Rol | Visitante |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T301 | Pantalla A5 Iniciar sesión y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T302 | Repositorio y casos de uso | T301 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T303 | Pruebas unitarias, de UI y de seguridad | T301 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T304 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo | ⏳ |

**Evidencia:** T301 en commit `ec2882c` (refactor MVVM con UiState). T302: contrato `POST /api/v1/auth/login` de `VirGo-API/specs/003-auth-login`; `AppError` (7 variantes) y `AppResult` en `:core:common`, `AuthApi` + DTOs + `createAuthApi` (OkHttp 10/20 s) en `:core:network`, `AuthRepository` y `LoginUseCase` en `:core:domain`, `AuthRepositoryImpl` con mapeo a `AppError` en `:core:data`; `AuthRepositoryImplTest` con 8 pruebas MockWebServer (200 con aserción de path/body, 401, 400 con `errors`, 500 con `traceId`, 404, sin conexión, timeout y 200 malformado) que cubren las 7 variantes de `AppError`, 0 fallos; `ktlintCheck detekt testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL (2026-11-09 UTC). T303: `ktlintCheck detekt testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL (2026-11-09); `:core:domain:test` 30 pruebas y `:app:testDebugUnitTest` 29 pruebas, 0 fallos.
