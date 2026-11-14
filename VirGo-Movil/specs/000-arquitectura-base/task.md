# Tareas — 000 Arquitectura base del Móvil

| Campo | Valor |
|---|---|
| Módulo | `000-arquitectura-base` |
| Versión | 1.1.0 |
| Estado | Aprobado. 36/40 tareas hechas. |
| Fecha | 2026-10-29 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |
| Constitución | v1.1.1 |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

**[P]** = puede ejecutarse en paralelo con otras tareas [P] de la misma fase.

**🧑 Requiere confirmación humana** = cambia la configuración de GitHub, los secretos o publica un release (Art. XI.5).

---

## F0 · Repositorio y gobernanza

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T001 | Confirmar que `VirGo-Movil/` entró a `main` del repositorio `virgorent/VirGO_Movil` con su `.gitignore` DevSecOps (Art. V.8) y su `README.md` en inglés | — | `seguridad-ci` | `git remote -v` correcto; `git check-ignore local.properties` lo ignora | ✅ |
| T002 | 🧑 Coordinar con `VirGo-API` T207 el ruleset único `main-protection` del repositorio (PR, 1 revisión, checks requeridos, sin *force push* ni borrado) y la protección de push contra secretos | T001 | `seguridad-ci` | Reglas visibles en GitHub (captura o `gh api`) | ✅ |
| T003 | [P] Agregar `.editorconfig` (ktlint) y, en la raíz del repositorio, el bloque de `VirGo-Movil` en `.github/dependabot.yml`; la plantilla de PR es común (Gradle agrupado por familia y Actions) | T001 | `seguridad-ci` | Archivos en el repositorio; Dependabot activo | ✅ |
| T004 | [P] Revisar la alineación de la gobernanza con `verificar-constitucion` | — | `arquitecto-movil` | Resultado «Alineado» en `memoria.md` | ✅ |
| T037 | [P] Instalar y probar los MCP `figma` (lectura del *canvas* de «Pantallas VirGo») y `github` de `opencode.json`, con tokens en variables de entorno | — | `arquitecto-movil` | `opencode mcp list` los muestra conectados; captura en `memoria.md` | ⛔ |

> T037 bloqueada: `figma` responde `HTTP 405` en `https://mcp.figma.com/mcp` (problema conocido de opencode) y `github` sigue deshabilitado; requiere OAuth de Figma y `GITHUB_MCP_PAT`.

> T004 hecha: auditoría con `verificar-constitucion` sobre la gobernanza en dos pasadas con el subagente `arquitecto-movil`. La primera pasada dio **No alineado** (❌ Art. II y ❌ Art. XI); tras las correcciones, la re-verificación dio **Alineado** (0 ❌, 6 ⚠️ registrados como pendientes de cierre). Correcciones aplicadas con aprobación del equipo: aprobación retroactiva de los specs `001`–`004` (`spec.md`, `plan.md` y `task.md`), enmienda constitucional **1.1.1** (repo `VirGO_Movil` y VIII.7), referencias stale corregidas en `AGENTS.md` (raíz y móvil), `README.md`, `plan.md`, T001 y cabecera/ADRs de `memoria.md`, tarea **T040** (Timber, Art. IX) y reparación de las tablas F7/F8. Tabla final de 12 artículos y decisiones humanas en la entrada de `memoria.md`.

> T002 hecha (2026-11-13): decisión humana «Aplicar su JSON completo» → el ruleset vivo de `VirGO_Backend` (id 26558561) pasó de `no push main` (0 revisiones, 0 checks) a `main-protection` idéntico a su `.github/rulesets/main-protection.json`: PR obligatorio con 1 revisión, hilos resueltos y 7 checks requeridos (`api-build-test`, `api-iac`, `api-schema`, `CodeQL`, `database`, `gitleaks`, `dependency review`), más `deletion` y `non_fast_forward`; era el único ruleset activo tras el `PUT`. Evidencia: `gh api repos/virgorent/VirGO_Backend/rulesets/26558561` con `enforcement=active` y `required_approving_review_count=1`; el PR #19 del backend reporta sus 7 checks en verde. El móvil ya cumplía con `main-protection` (id 27049892) y push protection + secret scanning activados en ambos repos. El *drift* JSON↔ruleset vivo quedó eliminado.

## F1 · Proyecto Gradle y módulos

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T005 | Crear el proyecto (wrapper de Gradle, `settings.gradle.kts`, `gradle/libs.versions.toml`) con versiones **estables y compatibles** de Kotlin, AGP, KSP, Compose BOM y Hilt; registrar las versiones en `memoria.md` | T001 | `dev-android` | `./gradlew help` sin errores; tabla de versiones en la memoria | ✅ |
| T006 | *Convention plugins* en `build-logic/convention`: `virgo.android.application`, `virgo.android.library`, `virgo.android.compose`, `virgo.hilt` y `virgo.jvm.library` (SDK 26/36, JDK 17) | T005 | `dev-android` | Un módulo de prueba aplica los plugins y compila | ✅ |
| T007 | Crear `:app` (`mx.virgo.app`) y `:core:designsystem`, `:core:common`, `:core:domain` (JVM puro), `:core:network`, `:core:data`, `:core:datastore` y `:core:testing`, respetando las dependencias permitidas del plan (sección 4) | T006 | `dev-android` | `./gradlew assembleDebug` OK; `:core:domain` sin dependencias de Android | ✅ |
| T008 | [P] Configurar ktlint y detekt (`config/detekt.yml`) sin *baseline* de errores | T007 | `dev-android` | `./gradlew ktlintCheck detekt` en verde | ✅ |
| T009 | [P] Configurar Hilt: `VirGoApplication` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`) y un módulo de *dispatchers* en `:core:common` | T007 | `dev-android` | La app arranca en el emulador sin errores de Hilt | ✅ |

## F2 · Sistema de diseño

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T010 | Tokens de color claro y oscuro según el plan (sección 6.1), en `Color.kt` y `ColorScheme` | T007 | `ui-designsystem` | Los *previews* muestran la paleta en los dos temas | ✅ |
| T011 | [P] Tipografía: Manrope y DM Sans en `res/font` con su licencia OFL y `VirGoTypography` con la escala de Material 3 | T007 | `ui-designsystem` | Los *previews* de la escala tipográfica usan las fuentes correctas | ✅ |
| T012 | Formas, espaciado y `VirGoTheme` (claro, oscuro y el color dinámico desactivado para respetar la marca) | T010, T011 | `ui-designsystem` | `VirGoTheme` se aplica en `MainActivity` | ✅ |
| T013 | Componentes base: `VirGoButton`, `VirGoTextField`, `VirGoCard`, `VirGoBadge` (incluye «Estimación IA»), `VirGoTopBar`, `VirGoBottomBar`, `EmptyState`, `LoadingState` y `ErrorBanner`, cada uno con `@Preview` en claro y oscuro | T012 | `ui-designsystem` | Catálogo de *previews* completo (CA-10) | ✅ |
| T014 | Revisión de accesibilidad: contraste AA en los dos temas, objetivos táctiles de 48 dp o más, `contentDescription` y escala de fuente 2.0 | T013 | `ui-designsystem` + `qa-movil` | Tabla de contrastes en `memoria.md`; sin cortes con letra grande (CA-03) | ✅ |

## F3 · Arranque y navegación

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T015 | *Splash* con `core-splashscreen` y la marca VirGo; `strings.xml` base en español | T012 | `dev-android` | Al abrir la app se ve el *splash* (CA-02) | ✅ |
| T016 | `VirGoNavHost` con rutas tipadas `@Serializable`, extensiones `NavGraphBuilder` por destino, `TopLevelDestination` y `HomePlaceholder` | T009, T013 | `dev-android` | La navegación abre el inicio provisional | ✅ |
| T017 | `VirGoApp()` raíz: tema, `Scaffold`, barra inferior configurable (oculta en la base) y `SnackbarHost` | T016 | `dev-android` | La app muestra el inicio provisional con el tema de la maqueta (CA-02) | ✅ |

## F4 · Red, datos y almacenamiento seguro

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T018 | `AppError` y `AppResult` en `:core:common` | T007 | `dev-android` | Compila; las pruebas de T028 cubren cada variante | ✅ |
| T019 | `:core:network`: OkHttp (timeouts de 10 y 20 s), Retrofit con kotlinx.serialization y `API_BASE_URL` por variante; *release* falla al compilar si la URL no es `https://` | T018 | `dev-android` | `assembleDebug` OK; `assembleRelease` con una URL `http://` falla con un mensaje claro | ✅ |
| T020 | [P] Interceptores: `CorrelationIdInterceptor`, `AuthInterceptor` (con `TokenStore`) y logging solo en *debug* | T019, T022 | `dev-android` | Las pruebas de MockWebServer verifican los encabezados (T028) | ✅ |
| T021 | [P] `ProblemDetailsDto`, conversión de error de HTTP o excepción a `AppError`, y `HealthApi` (`GET health`) | T019 | `dev-android` | Pruebas de conversión en verde (T028) | ✅ |
| T022 | `:core:datastore`: `TokenStore` con DataStore y Tink AEAD con una llave en el Android Keystore; revisión de `seguridad-ci` | T007 | `dev-android` + `seguridad-ci` | Guardar, leer y borrar funcionan; el archivo no tiene el token en claro (CA-07) | ✅ |
| T023 | `CheckApiHealthUseCase` en `:core:domain` y `HealthRepository` en `:core:data` (mide la latencia y devuelve `AppResult`) | T021 | `dev-android` | Pruebas unitarias en verde (T028) | ✅ |

> **T018** hecha con T302 de `003` (commit `c76d17a`): `AppError` (Network, Timeout, Unauthorized, NotFound, Validation, Server, Unknown) y `AppResult` en `:core:common`; las 7 variantes quedan cubiertas por `AuthRepositoryImplTest` (`:core:data`). **T019** hecha con T301 de `003` (commit `32e6eec`): `API_BASE_URL` por variante con `buildConfigField` en `:core:network` (debug: `local.properties → virgo.apiBaseUrl` o `http://10.0.2.2:5122/`; release: `VIRGO_API_BASE_URL`), validación que rechaza una URL `http://` de release con mensaje claro, y `NetworkModule` con `@Provides AuthApi`; verificado con `assembleDebug` y `assembleRelease` en verde y con `VIRGO_API_BASE_URL=http://…` → BUILD FAILED. **T021 y T023** hechas en el commit `d6a7c9b`: `HealthApi` (`GET health` → `HealthStatusDto`) con `@Provides` en `NetworkModule`, `HealthRepository`/`CheckApiHealthUseCase` y `HealthRepositoryImpl` que mide la latencia con `TimeSource.Monotonic`; pruebas MockWebServer en `:core:data` (4) y del caso de uso en `:core:domain` (2). **T022** hecha en el commit `456d738`: `TokenStore` (`token: Flow<String?>`, `save`, `clear`) en `:core:datastore` con DataStore Preferences 1.3.0, cifrando con un `Aead` de Tink 1.18.0 (`AES256_GCM`) cuya llave maestra vive en el Android Keystore (`AndroidKeysetManager`); en el archivo solo queda el texto cifrado en Base64 y un valor ilegible se lee como `null`. Pruebas Robolectric (4) en `:core:datastore`: guardar→leer, borrar→null, **el archivo no contiene el token en claro (CA-07)** y Base64 inválido o cifrado ajeno→null. El `Aead` de Keystore se inyecta como dependencia porque Robolectric no implementa `AndroidKeyStore`; las pruebas usan un `AesGcmJce` local (la ruta del Keystore real queda en T030/T027). **T020** hecha en el commit `0600d0f` y **T028** en `86624c5`: puerto `AuthTokenProvider` en `:core:common` (para respetar §4: `network → common`; `TokenStore` lo implementa y `TokenStoreModule` lo liga con `@Binds`), `CorrelationIdInterceptor` (UUID nuevo en `X-Correlation-Id` por petición), `AuthInterceptor` (`Authorization: Bearer` solo cuando hay token) y `HttpLoggingInterceptor` con nivel `BASIC` solo si `BuildConfig.IS_DEBUGGABLE` (`createOkHttpClient` en `NetworkClientFactory`; `NetworkModule` provee un `OkHttpClient` `@Singleton` compartido por `AuthApi` y `HealthApi`). Dependencia nueva: `okhttp logging-interceptor` 4.12.0. Pruebas MockWebServer (9): `X-Correlation-Id` único y que reemplaza al del caller, `Bearer` presente/ausente/en blanco, logging solo en *debug* con nivel `BASIC`, timeouts 10 s/20 s y los 2 interceptores siempre presentes. La conversión a `AppError` ya estaba cubierta por `AuthRepositoryImplTest` (8) y `HealthRepositoryImplTest` (4). **F4 completa (7/7)**; `testDebugUnitTest` en **94 pruebas, 0 fallos** (T028 verificado).

## F5 · Diagnóstico de integración

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T024 | `DiagnosticsViewModel` y `DiagnosticsScreen` en el *source set* `debug`: versión de la app, `API_BASE_URL`, estado y latencia de `/health`, y errores convertidos a textos en español | T017, T023 | `dev-android` | Con la API local en marcha muestra «API disponible» (CA-04); con la API apagada, un mensaje claro sin cierre (CA-05) | ✅ |
| T025 | Menú de desarrollo en la barra superior (solo *debug*) para abrir el diagnóstico | T024 | `dev-android` | El menú no existe en el APK de *release* | ✅ |
| T040 | [P] Timber solo en *debug*: plantar un `Tree` de plantilla en `VirGoApplication` (nivel `debug`), sin imprimir tokens ni cuerpos de respuesta; en *release* no se planta ningún árbol (Art. IX) | T009 | `dev-android` | `ktlintCheck detekt` en verde; sin `Log`/`println` en el código (revisión T027) | ✅ |

> **T025** ✅ y **T024** 🔄 en el commit `b826b05`: `DiagnosticsRoute`/`DiagnosticsViewModel`/`DiagnosticsScreen` y los textos viven solo en `app/src/debug` (`src/debug/res/values/strings.xml`); `DevMenu` y `developmentDestinations(...)` tienen implementación no-op `= Unit` en `app/src/release`; el menú se muestra en el `topBar` del `Scaffold` solo en el inicio provisional. Pruebas en `app/src/testDebug` (5): `DiagnosticsViewModelTest` con Turbine (carga → éxito, carga → error, reintento) y `DiagnosticsScreenTest` con Robolectric («API disponible» y mensaje de error sin cierre). **Evidencia T025:** en `assembleRelease` no existe ninguna clase `Diagnostics*`, el `DevMenu` de release es un stub sin `DropdownMenu` y los `values.xml` de release no contienen `diagnostics_*` ni `dev_menu_*` (sí los de `main`). **Pendiente en T024:** la evidencia real CA-04/CA-05 («API disponible» con la API local y mensaje claro con ella apagada) requiere `VirGo-API` corriendo (`dotnet run` + `GET /health`); el backend todavía no tiene código (T009), así que queda para T030.

> **T040** hecha: Timber `5.0.1` en el version catalog (`com.jakewharton.timber:timber`) y `implementation(libs.timber)` en `app/build.gradle.kts`; en `VirGoApplication.onCreate` se planta `Timber.DebugTree()` **solo** dentro de `if (BuildConfig.DEBUG)`, por lo que en *release* no se planta ningún árbol y R8 elimina el código muerto (ninguna llamada a `Timber.d(...)` existe todavía; los únicos puntos de registro son este `plant`). **Evidencia:** pipeline local completo en verde — `ktlintCheck detekt lint` (7m06s), `testDebugUnitTest` + `assembleDebug` + `assembleRelease` + `:core:domain:test` + `:core:common:test` + `pitest` → **BUILD SUCCESSFUL** (7m06s); grep sobre `*.kt` sin `Log.[vdiew](`, `println(` ni `System.out.print` (revisión T027), y `Timber.` aparece una sola vez, en `VirGoApplication.kt`. Evidencia CI: **PR #6** con los 6 checks en verde - `movil-build-test` (4m34s), `analyze` (5m45s), `CodeQL`, `gitleaks`, `dependency review` y `changes`.

> **T024** ✅ con evidencia runtime en el emulador (Pixel_10_Pro, build de `main` con todos los fixes, 2026-11-14): login QA real contra producción (`POST /api/v1/auth/login` → **200**, 7439 ms) y DevMenu → Diagnóstico mostrando **v0.2.0**, `https://rcof55a3c9.execute-api.us-east-1.amazonaws.com/`, «API disponible» y latencia **227 ms** (CA-04). Con `cmd connectivity airplane-mode enable` el estado cambia a «API no disponible» / «Sin dato» con el mensaje en español **«No hay conexión con la API. Revisa tu internet e inténtalo de nuevo.»** y la app **no cierra** (CA-05); al desactivar el modo avión y tocar «Reintentar» vuelve a «API disponible» (1123 ms). Evidencia: `docs/evidencia/CA-04-diagnostico-api-disponible.png`, `docs/evidencia/CA-05-diagnostico-api-caida.png` y `docs/evidencia/T030-devmenu-diagnostico.png`. La «API apagada» se simuló con modo avión porque la API real es la de producción (no la apaga el equipo).

## F6 · Seguridad de la compilación

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T026 | `network_security_config` de *debug* (solo `10.0.2.2` en claro) y de *release* (sin tráfico en claro); R8 con sus reglas; firma de *release* desde variables de entorno sin respaldo con la llave de *debug* | T019 | `seguridad-ci` | `assembleRelease` minificado; en *release* una solicitud `http://` se bloquea (CA-06) | ✅ |
| T027 | Revisión de seguridad de la app: permisos, secretos, logs, almacenamiento y R8 | T022, T026 | `seguridad-ci` | Reporte sin hallazgos críticos ni altos | ✅ |

> **T026** hecha en el commit `c0b7e64`: `network_security_config` en `app/src/main` (`cleartextTrafficPermitted="false"`) con `android:networkSecurityConfig` en el manifiesto, y en `app/src/debug` un `domain-config` adicional que permite en claro **solo** `10.0.2.2` (emulador). R8: `isMinifyEnabled`/`isShrinkResources` en *release* con `proguard-rules.pro` (kotlinx.serialization `**$$serializer`/`Companion`, `retrofit2.Call`/`Response`, `Continuation` y `com.google.crypto.tink.**`). Firma desde variables de entorno (`SIGNING_KEYSTORE_FILE`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`): si se pide una tarea `*Release*` sin ellas, el build falla con «Falta la firma de release… Release nunca se firma con la llave de debug (T026)» — verificado (`BUILD FAILED`, exit 1). **Evidencia:** `assembleRelease` minificado → BUILD SUCCESSFUL con `mapping.txt` (42 MB); `apksigner verify` → firma **V2** con el keystore local dedicado (nunca el de debug); el XML empaquetado de *release* no contiene `10.0.2.2` ni `domain-config` (el de *debug* sí); manifiesto mergeado de *release* sin `android:debuggable`. Pipeline en verde con **94 pruebas, 0 fallos**. **Queda en T030** la evidencia runtime de CA-06 (bloqueo de `http://` en dispositivo) porque el tráfico real sale contra la API local.

> **T027** hecha en el commit `ad6a18c` (reporte completo en `memoria.md`): revisión de permisos, secretos, logs, almacenamiento y R8. **1 hallazgo alto corregido:** el manifiesto no declaraba `android.permission.INTERNET` (sin él la app no puede hacer ninguna llamada en dispositivo; las pruebas MockWebServer corren en JVM y no lo detectan). Hallazgos medios corregidos: `lint` marcaba 8 errores (`LocalContextGetResourceValueCall` en `RecoverScreen` → strings izados con `stringResource`, y `ViewModelConstructorInComposable` en 2 previews → `@SuppressLint` como ya hacían Login/Register, y en 3 tests de `SmokeTest` → construcción del VM izada fuera de `setContent`, como el test de login). **Resultado:** `lint` 0 errores / 21 warnings benignos (versiones de dependencias, recursos sin usar, icono de app pendiente), sin permisos peligrosos, sin secretos en código (solo strings de prueba), sin `Log`/`println`, sin `SharedPreferences` en claro (solo `TokenStore` cifrado), R8 activo con `mapping.txt` y `apksigner` en verde. Pipeline `ktlintCheck detekt lint testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → **94 pruebas, 0 fallos**.

## F7 · Pruebas y verificación

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T028 | [P] Pruebas unitarias y de red: conversión a `AppError`, interceptores (MockWebServer), `HealthRepository`, `CheckApiHealthUseCase` y `TokenStore` (Robolectric) | T020–T023 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T029 | [P] Pruebas de UI (Compose Test + Robolectric): componentes base, navegación al inicio provisional, `DiagnosticsViewModel` con Turbine y escala de fuente 2.0 | T013, T017, T024 | `qa-movil` | Pruebas en verde (CA-10) | ✅ |
| T038 | [P] Pruebas de mutación con `gradle-pitest-plugin` en `:core:domain` y `:core:common`; medir la línea base | T028 | `qa-movil` | `./gradlew pitest` termina; puntaje base en `memoria.md` | ✅ |
| T030 | Verificación manual en el emulador: arranque en frío medido, modo oscuro, diagnóstico contra la API local y APK de *release* sin tráfico en claro | T026, T029 | `qa-movil` | Tiempos y capturas en `memoria.md` (CA-02, CA-03, CA-04, CA-06; RNF-03) | ✅ |

> T029 hecha: 103 pruebas verdes (0 fallos) con `ktlintCheck detekt lint testDebugUnitTest assembleDebug assembleRelease :core:domain:test` en verde. Cubre `DesignSystemComponentsTest` (5), `FontScaleTest` (2), `NavigationTest` (2: inicio provisional y pin → registro → back) y las suites existentes. Fix necesario: el `pointerInput` de limpieza vive en el `Canvas` del mapa (no en el `Box` padre) para que los pins reciban el toque; la tarjeta de cabecera cubre pins cercanos al centro, así que el flujo de navegación usa un pin visible debajo de la tarjeta (commit `7d8e0c7`).

> T038 hecha: plugin `info.solidsoft.pitest` 1.19.0 en `:core:domain` y `:core:common` con `targetClasses` explícitos por paquete. `./gradlew pitest` → BUILD SUCCESSFUL. Línea base: **dominio 73% de mutación (35/48, test strength 97%, línea 71%)** y **common 33% (1/3, test strength 100%, línea 100%)** — common solo tiene tipos sellados (3 mutaciones); su primera suite (`AppResultTest`, 4 pruebas) se agregó para que PIT tuviera qué matar (commit `04cbcd2`).

> **T030** ✅ con evidencia en `docs/evidencia/` (Pixel_10_Pro, 2026-11-13/14): **arranque en frío** `am start -W` → 1791 / 1854 / 1885 ms (1935 ms tras reinicio) en debug y **698 ms** con el APK de *release* `v0.2.0` — todos por debajo del RNF-03 (< 2 s); **modo oscuro** con `cmd uimode night yes` → `evidencia-mod-oscuro.png` (CA-03); **diagnóstico** contra la API de producción → «API disponible» 227 ms con su captura (CA-04) y mensaje en español sin cierre con la red caída (CA-05, ver T024); **icono de launcher** → `icono-launcher-evidencia.png`. **Sin tráfico en claro (CA-06):** el APK de *release* publicado en `v0.2.0` se verificó con `aapt dump xmltree` → `networkSecurityConfig` presente y `cleartextTrafficPermitted=0x0` sin `domain-config` en claro; además la variante *release* rechaza en compilación cualquier `API_BASE_URL` `http://` (T019), así que la app no tiene ninguna ruta de tráfico en claro que ejecutar en runtime. **Diagnóstico contra la API local:** no aplica ya — el backend entregó producción (`https://rcof55a3c9.execute-api.us-east-1.amazonaws.com`), que es la que usó la evidencia.

## F8 · CI/CD y publicación

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T031 | `.github/workflows/movil-ci.yml`: JDK 17, `setup-gradle`, jobs `changes` y `movil-build-test` con `ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug`, artefactos (APK de *debug* y reporte de Pitest) y `concurrency` | T028, T029, T038 | `seguridad-ci` | PR de prueba con todos los checks en verde (CA-08) | ✅ |
| T032 | [P] `.github/workflows/movil-codeql.yml` (`java-kotlin`) | T031 | `seguridad-ci` | Check de CodeQL en el PR sin alertas altas | ✅ |
| T033 | 🧑 Generar el keystore de *release* y guardarlo en GitHub Secrets (`SIGNING_*`); crear la variable `VIRGO_API_BASE_URL` con la URL HTTPS que entrega T210 del backend | T026 | `seguridad-ci` | Secretos y variable visibles en GitHub; respaldo del keystore confirmado (P-02) | 🔄 |
| T034 | `.github/workflows/movil-release.yml`: **solo `workflow_dispatch`** desde `main` con la versión como entrada → `assembleRelease`, `apksigner verify` y `gh release create vX.Y.Z` con el APK | T031, T033 | `seguridad-ci` | YAML válido; sin secretos impresos en los logs | ✅ |
| T035 | 🧑 Primer release con *Run workflow*: descargar, instalar y hacer la prueba de humo (la app abre y llega a la API de producción por HTTPS) | T034 | `seguridad-ci` + `qa-movil` | Release publicado con el APK firmado, la versión y el commit (CA-09) | ✅ |

> T031 hecha: workflow en `.github/workflows/movil-ci.yml` del repositorio `virgorent/VirGO_Movil` (el móvil ya no vive en el monorepo). SHA pinneados de actions, job `changes` con `dorny/paths-filter` sobre `VirGo-Movil/**` y `working-directory: VirGo-Movil`. Evidencia CA-08: **PR #4** con checks `gitleaks`, `dependency review`, `changes` y `movil-build-test` en verde (7m24s). El primer intento falló en `testDebugUnitTest`: Robolectric 4.17 con `@Config(sdk = [36])` exige Java 21 y el daemon de CI corría con el JDK 17 del `setup-java`; se corrigió en la misma rama con `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=21`, commit `7492292`) — el toolchain de compilación sigue en 17 (`jvmTarget 17`).

> T032 hecha: `.github/workflows/movil-codeql.yml` commiteado en este mismo PR #4 (commit `5a31fdd`) de `virgorent/VirGO_Movil`. Disparadores `pull_request` y `push` sobre `main` más cron semanal; lenguaje `java-kotlin` con `github/codeql-action` pinneado en `33d7b85da1a04171e211befe2c739303b9a4c066` (v4.39.3), build explícito `./gradlew assembleDebug` con `working-directory: VirGo-Movil`, permisos `security-events: write` solo en el job y `concurrency` que cancela corridas previas del mismo PR. Evidencia: checks `analyze` (workflow `movil-codeql`, run `43239028981`) y `CodeQL` en verde en el PR #4, y 0 alertas de code scanning (`gh api /repos/virgorent/VirGO_Movil/code-scanning/alerts` → `0`), es decir, sin alertas altas.

> T033 en curso: keystore de release `virgo-release.jks` (alias `virgo-release`, certificado `CN=VirGo Local, O=VirGo`) y sus 4 secretos en `virgorent/VirGO_Movil` — `SIGNING_KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD` — visibles con `gh secret list` (2026-11-12). Firma probada en local con el keystore real: `SIGNING_*=… ./gradlew assembleRelease` → `BUILD SUCCESSFUL` (1m31s) y `apksigner verify` → exit 0. **Falta:** la variable `VIRGO_API_BASE_URL` (T210 del backend sigue sin desplegar: «AWS … todavía sin desplegar con URL HTTPS», `VirGO_Backend/docs/ESTADO-PROYECTO.md`) y el respaldo del keystore (P-02): el archivo vive en `Temp\opencode\` y requiere custodia humana.

> T034 hecha: `.github/workflows/movil-release.yml` en la raíz del repositorio (commit `c7793ed`): solo `workflow_dispatch` con entrada `version` validada (`^[0-9]+\.[0-9]+\.[0-9]+$`), job condicionado a `refs/heads/main`, SHA pinneados de `checkout`, `setup-java` y `setup-gradle` (los mismos de `movil-ci.yml`), keystore decodificado desde `SIGNING_KEYSTORE_BASE64` hacia `$RUNNER_TEMP`, `assembleRelease` con `SIGNING_*` y `vars.VIRGO_API_BASE_URL`, `apksigner verify --print-certs` y `gh release create vX.Y.Z` con `GH_TOKEN` (`permissions: contents: write` solo en el job). Los secretos se pasan por `env:` en cada paso, nunca interpolados dentro del `run`, por lo que no se imprimen en los logs. Evidencia: `actionlint` v1.7.12 → exit 0 y `yaml.safe_load` → YAML OK; el workflow no aparece en `gh workflow list` desde una rama distinta de la predeterminada (GitHub solo registra `workflow_dispatch` desde `main`), su ejecución real es T035.

> **T035** ✅: primer release lanzado el 2026-11-14 con `version=0.2.0` (se publicó `v0.2.0`, no `v0.1.0`, porque el alcance creció con el icono, el respaldo y el AAB del PR #10) — run `43544624048` en verde; release https://github.com/virgorent/VirGO_Movil/releases/tag/v0.2.0 con `app-release.aab` (5 661 199 B) y `app-release.apk` (2 530 468 B), `versionCode 2` / `versionName 0.2.0`. **Humo:** el APK del release se descargó del release, se instaló en el emulador (`uninstall` + `install`, para no mezclar firmas), arrancó en `TotalTime 698 ms`, abrió el inicio visitante y el login QA contra producción llegó a «Inicio» (CA-09). Evidencia: `docs/evidencia/CA-09-release-v0.2.0-login.png`. Pendiente solo del lado del equipo: subir el AAB a prueba interna de Play Console.

## F9 · Cierre

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T039 | Agregar `movil-build-test` y `movil-codeql` como checks requeridos del ruleset único y actualizar `.github/rulesets/main-protection.json` en la raíz | T002, T031 | `seguridad-ci` | *Push* directo a `main` rechazado; JSON en el repositorio | ✅ |
| T036 | Completar `check.md`; verificación final con `verificar-constitucion`; marcar el spec, el plan y las tareas como *Cerrado*; entrada de cierre en `memoria.md` con lo aprendido | T001–T039 | `qa-movil` + `arquitecto-movil` | Resultado «Alineado» y todos los criterios de aceptación con evidencia | ⏳ |

> T039 hecha (2026-11-13): decisión humana «`movil-build-test` + `analyze`» → el ruleset vivo de `VirGO_Movil` (id 27049892) exige hoy 4 checks: `gitleaks`, `dependency review`, `movil-build-test` y `analyze` (el check de CodeQL se llama `analyze`; `movil-codeql` no existe como check-run, por lo que se requirió `analyze` en lugar del nombre del workflow). El JSON `.github/rulesets/main-protection.json` de la raíz quedó idéntico en el PR #7 (commit `73020da`). Evidencia del criterio de aceptación: *push* directo a `main` rechazado con `GH013` «Changes must be made through a pull request» y «4 of 4 required status checks are expected» (commit de prueba `933a78b` descartado con `git reset --hard`). **Prueba empírica del *skip*:** el PR #7 solo tocaba `.github/` (fuera de `VirGo-Movil/**`), `movil-build-test` se skipeó con la condicional `if:` y reportó *Success*; tras la aprobación el merge pasó a `CLEAN` y se fusionó (`716cc70`), confirmando que un job skipeado por `if:` satisface el check requerido (documentación de GitHub) mientras que un workflow skipeado por filtro `paths` en `on:` quedaría *Pending* y bloquearía.

---

## Matriz de trazabilidad

| Requerimiento / Criterio | Tareas |
|---|---|
| RF-01 Módulos | T005, T006, T007 |
| RF-02 Arranque e inicio provisional | T015, T016, T017 |
| RF-03 Sistema de diseño | T010–T014 |
| RF-04 Navegación extensible | T016, T017 |
| RF-05 Cliente de red por variante | T019, T020 |
| RF-06 Errores a `AppError` | T018, T021, T028 |
| RF-07 Almacenamiento cifrado | T022, T028 |
| RF-08 Diagnóstico en *debug* | T023, T024, T025 |
| RF-09 Textos en recursos | T015, T024 |
| RF-10 CI en PR | T031, T032, T038, T039 |
| RF-11 APK firmado publicado | T033, T034, T035 |
| RNF-01 Android 8.0 o superior | T006 |
| RNF-02 Seguridad | T002, T022, T026, T027, T033, T039 |
| RNF-03 Arranque < 2 s | T030 |
| RNF-04 Accesibilidad | T014, T029 |
| RNF-05 Calidad | T008, T031 |
| RNF-06 Costo $0 | T031, T034 |
| RNF-07 Trazabilidad | T005, T035, T036 |
| CA-01 … CA-10 | T007/T031 · T015/T017/T030 · T014/T030 · T024/T030 · T024 · T026/T030 · T022 · T031 · T035 · T013/T029 |
