# Plan — 030 Sesiones

| Campo | Valor |
|---|---|
| Módulo | `030-sesiones` |
| Versión | 0.1.0 |
| Estado | Borrador |
| Fecha | 2026-11-16 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |
| Constitución | v1.1.1 |

## 1. Resumen técnico

- `AuthRepositoryImpl` recibe `TokenStore` (`:core:data` ya depende de `:core:datastore`) y guarda el `accessToken` solo cuando el login responde 200.
- `AuthRepository` expone `val isAuthenticated: Flow<Boolean>` derivado de `TokenStore.token` (fuente única de verdad: el repositorio, Art. III.6).
- En `:app`, un `SessionViewModel` resuelve una vez el estado de arranque (`Cargando` → `Autenticado`/`Visitante`) y `VirGoNavHost` recibe el `startDestination` como parámetro.
- `AuthInterceptor` ya envía `Authorization: Bearer` leyendo `AuthTokenProvider` (el mismo `TokenStore`): no hay cambios en la capa de red.
- Pruebas: `AuthRepositoryImplTest` con `FakeTokenStore`, prueba de navegación de arranque, prueba de seguridad de cifrado y humo con APK *debug* en el emulador.

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Lenguaje / framework | Kotlin, Jetpack Compose (Material 3), MVVM/UDF, Hilt |
| Persistencia | `TokenStore` existente: DataStore de preferencias cifrado con AEAD (Android Keystore) |
| Seguridad | Token opaco cifrado en reposo; sin logs; R8 en release |
| CI/CD | `movil-ci.yml` y `movil-codeql.yml` existentes (ktlint → detekt → lint → pruebas → pitest → assembleDebug) |

## 3. Constitution Check

| Art. | Tema | Resultado | Cómo se cumple |
|---|---|---|---|
| I.1–I.2 | SDD con aprobaciones | ✅ | Este plan se aprueba antes de tocar código; las tareas quedan ⏳ en `task.md`. |
| I.4 | Cuatro archivos | ✅ | `specs/030-sesiones/{spec,plan,task,check}.md`. |
| I.5 | Spec atómico | ✅ | Cubre solo «recordar la sesión»; «cerrar sesión» queda en `021-favoritos-perfil`. |
| III.2–III.3 | MVVM/UDF y capas | ✅ | La UI observa un `StateFlow` de sesión; no toca DataStore ni Retrofit. |
| III.5 | Hilt y corrutinas | ✅ | Inyección existente; sin `GlobalScope`. |
| III.6 | Fuente única | ✅ | El repositorio es el único que decide si hay sesión. |
| IV.4 | Autenticación Bearer | ✅ | El token guardado alimenta `AuthInterceptor` vía `AuthTokenProvider`. |
| V.1 | Solo HTTPS | ✅ | No cambia la capa de red. |
| V.3 | Tokens en DataStore cifrados | ✅ | Reutiliza `TokenStore` (AEAD + Keystore) sin alterarlo. |
| VI.2–VI.3, VI.6 | Pruebas por niveles | ✅ | Unitarias, Robolectric/Compose, seguridad (cifrado) y humo; red simulada. |
| VIII.6 | URL y JSON probados | ✅ | No consume endpoints nuevos; el login ya está humado (T304). |
| X.2 | Commits citan tarea | ✅ | `feat(T3001): …`. |
| XII.1–XII.2 | Idioma | ✅ | Documentación en español; código en inglés. |

> Un ❌ bloquea la aprobación del plan. **Sin ❌.**

## 4. Estructura

```text
core/domain/src/main/kotlin/mx/virgo/core/domain/
├── AuthRepository.kt                  # + val isAuthenticated: Flow<Boolean>
core/data/src/main/kotlin/mx/virgo/core/data/repository/
├── AuthRepositoryImpl.kt              # + TokenStore inyectado, save en 200, isAuthenticated
app/src/main/kotlin/mx/virgo/app/
├── ui/session/SessionViewModel.kt     # nuevo: resuelve el estado de arranque
├── navigation/VirGoNavHost.kt         # startDestination como parámetro
├── MainActivity.kt                    # observa SessionViewModel y decide la ruta
core/data/src/test/.../AuthRepositoryImplTest.kt   # + FakeTokenStore
app/src/test/.../ui/session/SessionViewModelTest.kt  # nuevo
app/src/test/.../NavigationStartTest.kt            # nuevo (arranque con y sin sesión)
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Alternativas descartadas | Motivo |
|---|---|---|---|
| ADR-01 | Guardar solo el `accessToken` en la llave existente de `TokenStore` | Ampliar `TokenStore` para guardar también `refreshToken` | `refresh` responde 404 (fuera de alcance) y el store es de una llave; se amplía cuando `013` se despliegue. |
| ADR-02 | Resolver el `startDestination` una vez al arrancar con `SessionViewModel` | Observar la sesión continuamente para re-enrutar en caliente | Los cambios de ruta ya los maneja la navegación explícita (login); menos estados intermedios. |
| ADR-03 | Esperar la resolución local (DataStore, milisegundos) sin crear una pantalla de splash nueva | Splash screen propio, o arrancar siempre en visitante | Evita código de UI extra y el parpadeo a visitante que provocaría un arranque doble. |
| ADR-04 | Exponer el estado de sesión en `AuthRepository` (dominio) | Nuevo repositorio/caso de uso `SessionRepository` | Mismo concepto (sesión = autenticación) y una sola fuente de verdad menos (III.6). |

## 6. Diseño

### 6.1 Guardar y exponer la sesión

`AuthRepositoryImpl.login()` conserva el flujo actual y, tras mapear la respuesta, llama `tokenStore.save(accessToken)` antes de devolver `Success`. Si `save` falla, se devuelve `AppResult.Failure` (no se navega con sesión sin guardar). `isAuthenticated = tokenStore.token.map { it != null }`.

### 6.2 Arranque y navegación

`SessionViewModel` expone `startRoute: StateFlow<Route?>` (`null` mientras resuelve). `MainActivity` espera el primer valor y compone `VirGoNavHost(startDestination = …)`: con sesión → `HomeRoute`; sin sesión → `GuestHomeRoute` (idéntico al de hoy). El login sigue navegando a `HomeRoute` como ahora.

### 6.3 Seguridad

- El token nunca entra en un `UiState` ni en logs: solo `TokenStore` (cifrado AEAD, llave del Keystore).
- Sin cambios en la capa de red: `AuthInterceptor` ya lee `AuthTokenProvider`.
- Sin datos del formulario persistidos.

## 7. DevSecOps y CI/CD

| Gate | Herramienta | Cuándo |
|---|---|---|
| Secretos | Gitleaks | PR y push a `main` |
| Dependencias | Dependabot + dependency review | Semanal y PR |
| SAST | CodeQL / ktlint / detekt / Lint | PR |
| Pruebas | `testDebugUnitTest` + `pitest` | PR |
| Release | `movil-release.yml` (manual) | Cuando el equipo lo pida |

## 8. Estrategia de pruebas

| Nivel | Dónde | Qué cubre |
|---|---|---|
| Unitarias | `:core:data`, `:app` | `save` solo en 200; `isAuthenticated`; `SessionViewModel` |
| Integración | `:core:data` (MockWebServer) + Robolectric | Login 200 → token guardado; 401/400/red → sin token |
| UI | `:app` (Compose/Robolectric) | Arranque con y sin sesión llega a la ruta correcta |
| Seguridad | `:core:datastore` (existente) + aserción nueva | Valor cifrado en el DataStore; sin token en claro ni en logs |
| Humo | APK *debug* en el emulador | Login → matar proceso → reabrir → home; instalación limpia → visitante |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Token caducado guardado (expira en 15 min): la app abre la home y la primera llamada autenticada fallará con 401 | Fuera de alcance documentado: el manejo del 401 llega con `006-users-me`; el login sigue disponible desde la home |
| DataStore/Keystore alterado por snapshots del emulador | Arrancar el emulador con `-no-snapshot` (lección registrada en `memoria.md`) |
| Parpadeo de ruta al arrancar | ADR-03: esperar la resolución local sin splash nuevo; si el equipo prefiere splash, se cambia el plan (P-01) |
| Regresión del flujo visitante | CA-03 y prueba de arranque sin sesión en el CI |

## 10. Fases

| Fase | Contenido | Tareas |
|---|---|---|
| F0 · Datos y dominio | Guardar el token y exponer el estado de sesión | T3001–T3002 |
| F1 · Arranque | Ruta dinámica en la app | T3003 |
| F2 · Verificación | Pruebas por niveles y humo | T3004–T3005 |
| F3 · Cierre | Evidencia y memoria | T3006 |
