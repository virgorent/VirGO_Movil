# Memoria — VirGo Móvil (Android)

Bitácora oficial del proyecto (Art. X de [`constitution.md`](constitution.md)). Cada sesión agrega una entrada al final de «Bitácora» con la skill [`registrar-memoria`](.opencode/skills/registrar-memoria/SKILL.md).

## Estado actual

| Campo | Valor |
|---|---|
| Constitución | v1.1.0 aprobada por el equipo el 2026-11-05 |
| Módulo en curso | `000-arquitectura-base`: T001, T003, T005, T006, T007, T008, T009, T010, T011, T012, T013, T014 hechas |
| Tareas | 12 / 39 hechas (T037 bloqueada por MCP) |
| `API_BASE_URL` de *release* | Pendiente (la entrega `VirGo-API` en `002`: T210) |
| Última versión publicada | Ninguna |
| Siguiente paso | Ejecutar T006 y T007; desbloquear T037; resolver P-01/P-02 |

## Módulos

| Módulo | Estado | Notas |
|---|---|---|
| `000-arquitectura-base` | 🚧 Aprobado; T001/T003/T005 hechas | Base técnica sin pantallas del negocio |
| `001-…` pantallas | ⏸ No iniciados | Un spec atómico por pantalla o flujo, al vuelo; los primeros serán «Iniciar sesión» y «Cerrar sesión», con el JSON de `VirGo-API` `003` y `004` |

## Decisiones (ADR)

| ADR | Fecha | Decisión | Motivo |
|---|---|---|---|
| ADR-01 | 2026-10-29 | **Kotlin + Jetpack Compose** nativo | Stack del curso y del proyecto de referencia |
| ADR-02 | 2026-10-29 | Multi-módulo `:app` + `:core:*` con *convention plugins* | Capas aisladas y base para `:feature:*` |
| ADR-03 | 2026-10-29 | MVVM + UDF con `StateFlow` | Patrón recomendado y sin dependencias extra |
| ADR-04 | 2026-10-29 | Retrofit + OkHttp + kotlinx.serialization | Estándar y tipado |
| ADR-05 | 2026-10-29 | DataStore + Tink con llave en el Keystore | `EncryptedSharedPreferences` está obsoleta |
| ADR-06 | 2026-10-29 | Manrope y DM Sans empaquetadas | Fieles a la maqueta y funcionan sin conexión |
| ADR-07 | 2026-10-29 | Distribución por **GitHub Releases** | $0 |
| ADR-08 | 2026-10-29 | `API_BASE_URL` por variante; *release* solo `https://` | Simple y seguro |
| ADR-09 | 2026-10-29 | Robolectric para las pruebas de UI en el CI | Rápido y gratis |
| ADR-10 | 2026-11-03 | **Release manual** (`workflow_dispatch` desde `main`) | Indicación del curso (clase del 29/10) |
| ADR-11 | 2026-11-03 | **Ruleset** de GitHub en `main` | Mecanismo actual; exportable a JSON |
| ADR-12 | 2026-11-03 | **Pitest** (`gradle-pitest-plugin`) en módulos JVM puros | Mutación sin dispositivo; umbral tras medir |
| ADR-13 | 2026-11-03 | MCP permitidos: `figma` (lectura del *canvas*) y `github` | Pantallas al vuelo desde la maqueta oficial |
| ADR-14 | 2026-11-03 | Google Play como destino futuro, con enmienda | Tiene un pago único; por ahora GitHub Releases |
| ADR-15 | 2026-11-03 | **Repositorio único** `virgorent/virGO_App` con carpetas por proyecto | Decisión del equipo |

## Cambios de contrato (compartidos con Backend)

| Fecha | Cambio | Registrado en VirGo-API |
|---|---|---|
| 2026-10-29 | Contrato base acordado: `/api/v1`, Problem Details, `camelCase`, fechas UTC, JWT Bearer, `GET /health`, `GET /openapi/v1.json`, `X-Correlation-Id` | ✅ |
| 2026-11-03 | **Propuesto:** `POST /api/v1/auth/login` (devuelve `accessToken` de 15 min, `refreshToken` de 30 días y `user` con `role`) y `POST /api/v1/auth/logout` (204; ante 204 o 401 la app borra sus tokens). JSON en `VirGo-API/specs/003-auth-login` y `004-auth-logout` | ✅ |

## Versiones de librerías

Versiones fijadas en T005 y verificadas con `./gradlew help`.

| Librería / herramienta | Versión |
|---|---|
| Gradle | 9.8.2 |
| Android Gradle Plugin | 9.5.1 |
| Kotlin (KGP, Compose y serialización) | 2.4.10 |
| KSP | 2.3.13 |
| Hilt | 2.60.1 |
| Compose BOM | 2026.09.00 |
| JDK de ejecución local | 25 (el CI usará JDK 17) |

---

## Bitácora

### 2026-10-29 · Archivos de gobernanza y specs de la arquitectura base

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** `specs/000-arquitectura-base` · ninguna ejecutada
- **Qué se hizo:**
  - Se crearon `constitution.md` v1.0.0, `AGENTS.md` y esta `memoria.md`.
  - Se crearon 5 subagentes en `.opencode/agents/`: `arquitecto-movil`, `dev-android`, `ui-designsystem`, `qa-movil` y `seguridad-ci`.
  - Se crearon 4 skills internas: `sdd-flujo`, `registrar-memoria`, `verificar-constitucion` y `compilar-publicar-apk`, más la lista filtrada de skills externas en `AGENTS.md`.
  - Se redactaron `spec.md` (7 historias técnicas, 11 RF, 7 RNF y 10 CA), `plan.md` (Constitution Check, 9 ADR, tokens de diseño de la maqueta, red, seguridad y CI/CD a $0) y `task.md` (36 tareas en 10 fases, con matriz de trazabilidad).
  - El contrato con la API se alineó con `VirGo-API/constitution.md` (Art. IV).
- **Evidencia:** archivos en el repositorio. El Constitution Check del plan tiene los 12 artículos en ✅.
- **Decisiones:** ADR-01 a ADR-09.
- **Costo:** $0.
- **Pendiente:**
  - Revisión del profesor y del equipo.
  - Responder P-01 (ID de la app) y P-02 (custodia del keystore).
  - Al aprobarse, empezar por T001. **No se ejecutan tareas hasta la aprobación.**

### 2026-11-03 · Lineamientos de clase: specs atómicos, check.md, DevSecOps, release manual y MCP

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · `specs/000-arquitectura-base` · ninguna tarea ejecutada
- **Qué se hizo:**
  - Se propuso la enmienda **v1.1.0** de `constitution.md`: `check.md` y specs atómicos por pantalla, redacción anticipada, `.gitignore` y Gitleaks, pruebas de seguridad, mutación y humo, ruleset, **release manual**, Google Play como destino futuro, entrega de URL y JSON por el backend, MCP y README en inglés.
  - Se crearon `README.md` (inglés), `.gitignore` DevSecOps y `opencode.json` (MCP `figma` y `github`).
  - `000` pasó a v1.1.0: release solo con `workflow_dispatch`, ruleset, nuevas tareas T037 (MCP), T038 (Pitest) y T039 (ruleset exportado), y `check.md`.
  - Se actualizaron `AGENTS.md`, los 5 subagentes y las skills `sdd-flujo`, `verificar-constitucion`, `compilar-publicar-apk` y `registrar-memoria`.
  - Se registró como propuesto el contrato de login y logout del backend.
- **Evidencia:** archivos en el repositorio.
- **Decisiones:** ADR-10 a ADR-14.
- **Costo:** $0.
- **Aprendizaje:** «Canvas» en la clase se interpretó como el *canvas* de Figma; Canva queda en evaluación.
- **Pendiente:**
  - Aprobar la enmienda v1.1.0 (Art. XI.2).
  - Responder P-01 (ID de la app) y P-02 (custodia del keystore).
  - Redactar los specs de pantalla «Iniciar sesión» y «Cerrar sesión» cuando el backend confirme el contrato.

### 2026-11-03 · Repositorio único `Karasu0001/virGO_App`

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · `specs/000-arquitectura-base` · ninguna tarea ejecutada
- **Qué se hizo:** el proyecto pasa a la carpeta `VirGo-Movil/` del repositorio único; `.github/` va en la raíz con prefijo `movil-`, `security.yml` común y ruleset único coordinado con `VirGo-API` T207. Esta constitución se subordina a la global de la raíz en los principios generales.
- **Evidencia:** PR desde la rama `docs/sdd-virgo`.
- **Decisiones:** ADR-15.
- **Costo:** $0.
- **Aprendizaje:** ninguno nuevo; ver `VirGo-API/memoria.md`.
- **Pendiente:** la visibilidad del repositorio (`VirGo-API` `002` P-02).

### 2026-11-04 · El repositorio pasa a la organización `virgorent`

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · `specs/000-arquitectura-base` · ninguna tarea ejecutada
- **Qué se hizo:** no se pudo transferir `Karasu0001/virGO_App`, así que se creó `virgorent/virGO_App` vacío y se subieron `main`, `rama-apis`, `rama-backend`, `rama-movil` y `docs/sdd-virgo` con su historial. Las referencias vigentes apuntan al repositorio nuevo; las entradas anteriores de esta bitácora conservan el nombre de entonces.
- **Evidencia:** `git ls-remote` muestra los mismos commits en ambos repositorios.
- **Decisiones:** `virgorent/virGO_App` es el repositorio oficial.
- **Costo:** $0.
- **Aprendizaje:** ninguno.
- **Pendiente:** archivar `Karasu0001/virGO_App` cuando el equipo confirme; `VirGo-API` `002` P-02 (el repositorio es privado y el ruleset no aplicaría).

### 2026-11-04 · El repositorio ya es público

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · ninguna tarea ejecutada
- **Qué se hizo:** se comprobó que `virgorent/virGO_App` es público, con lo que el ruleset y los revisores de los *environments* ya están disponibles en el plan Free.
- **Evidencia:** la API pública de GitHub responde `visibility: public`. `Karasu0001/virGO_App` responde «not found».
- **Decisiones:** se resuelve `VirGo-API` `002` P-02.
- **Costo:** $0.
- **Aprendizaje:** al ser público, todo lo que se suba es visible; Gitleaks y la protección de push son prioritarios.
- **Pendiente:** coordinar el ruleset con `VirGo-API` T207.

### 2026-11-04 · Regla de longitudes tras la clase

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · ninguna tarea ejecutada
- **Qué se hizo:** `sdd-flujo` y `verificar-constitucion` exigen ahora longitud máxima por campo y comprobar lo afirmado contra la realidad.
- **Evidencia:** archivos en el repositorio.
- **Decisiones:** ninguna.
- **Costo:** $0.
- **Aprendizaje:** ver `VirGo-API/memoria.md`.
- **Pendiente:** usar las longitudes de `Modelo de datos VirGo.md` (sección 8) en los formularios.

### 2026-11-05 · Aprobación de la gobernanza móvil y corrección del puerto local

- **Autor:** equipo + opencode (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · `000-arquitectura-base` · T001/T003/T037 habilitadas
- **Qué se hizo:** se aprobó la constitución v1.1.0 y el spec, plan y tareas de `000`; se corrigió el puerto local de la API de `5080` a `5122` en `spec.md` y `plan.md` para alinearlo con `VirGo-API/src/VirGo.Api/Properties/launchSettings.json`.
- **Evidencia:** `git diff --check` limpio; `launchSettings.json` usa `http://localhost:5122`.
- **Decisiones:** el equipo aprueba la gobernanza móvil el 2026-11-05; el visto bueno del profesor queda pendiente en `check.md`.
- **Costo:** $0.
- **Aprendizaje:** el puerto local del backend debe ser la fuente de verdad para la URL de debug del móvil.
- **Pendiente:** PR de aprobación/corrección; P-01 (ID de app), P-02 (keystore), MCP Figma/GitHub (T037) y ejecución de T001/T003.

### 2026-11-05 · T001, T003 y T005: repositorio, estilo y proyecto Gradle

- **Autor:** equipo + opencode (`seguridad-ci` / `dev-android`)
- **Módulo / tareas:** `000-arquitectura-base` · T001, T003 y T005 hechas; T037 bloqueada
- **Qué se hizo:**
  - T001: se confirmó el remoto `https://github.com/virgorent/virGO_App.git`, la presencia de `VirGo-Movil/.gitignore` y `README.md`, y que `git check-ignore VirGo-Movil/local.properties` lo ignora.
  - T003: se agregó `VirGo-Movil/.editorconfig` con `ktlint_official`, longitud máxima de 120 y estilo Kotlin; el bloque `gradle` de `VirGo-Movil` ya existía en `.github/dependabot.yml`.
  - T005: se creó el wrapper de Gradle 9.8.2, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` y `gradle/libs.versions.toml` con AGP 9.5.1, Kotlin 2.4.10, KSP 2.3.13, Hilt 2.60.1 y Compose BOM 2026.09.00. Se usa Kotlin integrado de AGP 9 en módulos Android y KGP 2.4.10 para JVM.
- **Evidencia:** `./gradlew help` termina en `BUILD SUCCESSFUL`; `git check-ignore -v VirGo-Movil/local.properties` responde con la regla de `.gitignore`.
- **Decisiones:** AGP 9.5.1 activa Kotlin integrado; no se aplica `org.jetbrains.kotlin.android` en módulos Android. La versión de Kotlin del *catalog* se usa en módulos JVM y en los plugins de Compose y serialización.
- **Costo:** $0.
- **Aprendizaje:** con AGP 9 el proyecto no debe declarar `kotlin-android`; el wrapper de Gradle 9.8.2 funciona con el JDK 25 local, aunque el CI mantiene JDK 17.
- **Pendiente:** T006 (convention plugins), T007 (módulos), T037 (Figma devuelve HTTP 405 y GitHub sigue deshabilitado), P-01 y P-02.

### 2026-11-05 · T006, T007, T008 y T009: convention plugins, estructura modular y herramientas de calidad

- **Autor:** equipo + opencode (`dev-android` / `seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` · T006, T007, T008 y T009 hechas
- **Qué se hizo:**
  - T006: Se implementaron los *convention plugins* en `build-logic/convention`: `virgo.android.application`, `virgo.android.library`, `virgo.android.compose`, `virgo.hilt` y `virgo.jvm.library`.
  - T007: Se crearon e integraron en `settings.gradle.kts` los módulos `:app` y la capa `:core:*` (`:core:designsystem`, `:core:common`, `:core:domain`, `:core:network`, `:core:data`, `:core:datastore`, `:core:testing`).
  - T008: Se configuraron Ktlint y Detekt en `build.gradle.kts` y `config/detekt.yml` ajustando `jvmTarget = "17"` para compatibilidad con la Toolchain de Kotlin y JDK local.
  - T009: Configuración básica de Hilt en `VirGoApplication`, `MainActivity` y el módulo de dispatchers en `:core:common`.
- **Evidencia:** `./gradlew ktlintCheck detekt assembleDebug` finaliza con `BUILD SUCCESSFUL`.
- **Decisiones:** Se fuerza `jvmTarget = "17"` en las tareas de Kotlin y Detekt para alinearse con el JDK 17 exigido por la constitución.
- **Costo:** $0.
- **Aprendizaje:** Detekt requiere la especificación explícita de `jvmTarget` cuando la JVM de ejecución difiere de la JVM objetivo del proyecto.
- **Pendiente:** Fase 2 (Sistema de diseño en `:core:designsystem`: T010-T014).

### 2026-11-05 · T010 a T014: Sistema de diseño en `:core:designsystem`

- **Autor:** equipo + opencode (`ui-designsystem` / `dev-android`)
- **Módulo / tareas:** `000-arquitectura-base` · T010, T011, T012, T013 y T014 hechas
- **Qué se hizo:**
  - T010 & T012: Creación de `Color.kt`, `Shape.kt`, `Spacing.kt` y `Theme.kt` con la paleta de la marca VirGo (`#7357F6`, `#F5F5F7`, Slate palette) y `VirGoTheme` para modo claro y oscuro.
  - T011: Configuración de `Type.kt` con `VirGoTypography` según la escala tipográfica de Material 3.
  - T013: Implementación de componentes atómicos en `:core:designsystem`: `VirGoButton`, `VirGoOutlinedButton`, `VirGoTextField` (con toggle de contraseña e input background `#F5F5F7`), `VirGoCard` y `VirGoBadge`.
  - T014: Ajustes de accesibilidad (áreas táctiles >= 48 dp, contraste AA) y actualización de `.editorconfig` y `detekt.yml` para soportar convenciones de Jetpack Compose (`@Composable`).
- **Evidencia:** `./gradlew ktlintCheck detekt assembleDebug` finaliza con `BUILD SUCCESSFUL`.
- **Decisiones:** Se adaptaron las reglas de ktlint y detekt para permitir nombres en mayúscula en composables y listas de parámetros extendidas en componentes de UI.
- **Costo:** $0.
- **Aprendizaje:** ktlint requiere `ktlint_function_naming_ignore_when_annotated_with = Composable` en `.editorconfig` para no reportar nombres de funciones composables como violaciones de estilo.
- **Pendiente:** Fase 3 (Arranque y navegación: T015-T017) e integración de la pantalla de Login basada en el diseño recibido.

### 2026-11-07 — T015 a T017 y flujo visitante (specs 001–004): splash, navegación y pantallas A1–A7

- **Autor:** equipo + opencode (`dev-android`)
- **Módulo / tareas:** `000-arquitectura-base` → T015, T016 y T017 hechas; `001-inicio-visitante`, `002-registro`, `003-iniciar-sesion` y `004-recuperar-contrasena` → T101, T102, T201, T202, T301, T401 y T402 en curso (UI entregada, sin `ViewModel`).
- **Qué se hizo:**
  - T015: *splash* con `core-splashscreen` (`Theme.VirGo.Splash`, `ic_virgo_splash`) y `strings.xml` base en español.
  - T016: `navigation/Routes.kt` con rutas `@Serializable`, `VirGoNavHost` (inicio en `GuestHomeRoute`) y `HomePlaceholder`.
  - T017: `VirGoApp()` con tema, `Scaffold`, `SnackbarHost` vía `LocalSnackbarHost` y barra inferior condicional a `HomeRoute`; `MainActivity` con `installSplashScreen()` y `enableEdgeToEdge()`.
  - Specs 001–004 (UI con estado local, textos en `strings.xml`): A1/A3 `ui/guest` (`GuestHomeScreen`, `GuestMap` con mapa estático en `Canvas`, `VenueSample`); A2/A4 `ui/register` (`RegisterScreen`, `RegisterHero`, `RegisterRole` con roles y nota ámbar); A5 `ui/auth/AuthComponents` + `ui/login/LoginScreen`; A6/A7 `ui/recover` (`RecoverScreen`, `RecoverErrors`, código de un solo uso en memoria).
  - Dependencias añadidas: `navigation-compose 2.10.2`, `core-splashscreen 1.2.0`, `kotlinx-serialization-json 1.12.0`.
- **Evidencia:** `./gradlew ktlintCheck detekt assembleDebug` finaliza con `BUILD SUCCESSFUL` (APK debug generado); `local.properties` con `sdk.dir` (ignorado por git).
- **Decisiones:**
  - Estado local con `rememberSaveable` y sin `ViewModel` (los planes 001–004 aún prevén un `ViewModel` por pantalla: pendiente de alineación plan↔código antes de T103/T203/T302/T403).
  - AGP `9.0.0` → `9.5.1` y `compileSdk` `36` → `37`: `navigation-compose 2.10.2` y `lifecycle 2.11.0` exigen por AAR metadata `compileSdk ≥ 37` y AGP ≥ 9.1, y AGP 9.1 solo acepta hasta `compileSdk 36.1` (SDK XML v4). `targetSdk` sigue en 36; el Platform 37.0 se auto-descargó con las licencias ya aceptadas.
  - `Icons.Filled.AutoAwesome` en lugar de `Sparkles` (no existe en `material-icons-extended`).
  - Verificación local con JDK 17 (`~/.jdks/jdk-17.0.21+9`).
- **Costo:** $0.
- **Aprendizaje:**
  - El `checkDebugAarMetadata` es la primera señal de incompatibilidad AGP/`compileSdk`; conviene revisarlo al fijar dependencias del BOM.
  - `ktlintFormat` corrige por sí solo orden de imports (el grupo `kotlin.*` va al final) y firmas multiline; `TopLevelPropertyNaming` exige `SCREAMING_SNAKE_CASE` para `const val`.
  - No usar `Get-Content | Set-Content` de PowerShell sobre archivos UTF-8 (los convierte a ANSI/CRLF); editar con herramientas que preservan UTF-8/LF.
  - La entrada T005 anotaba AGP 9.5.1 y BOM 2026.09.00, pero el `libs.versions.toml` real traía AGP 9.0.0 y BOM 2026.06.01; se llevó AGP a 9.5.1 y se deja constancia de la discrepancia del BOM.
- **Pendiente:** `ViewModel` + repositorios/casos de uso de 001–004 (T103, T203, T302, T403), pruebas (T104, T204, T303, T404), integración con la URL real, CA-02 (prueba de arranque en emulador) y resolver la discrepancia del BOM anotada en T005.

### 2026-11-08 — CA-02, refactor MVVM de 001–004 y pruebas en verde

- **Autor:** equipo + opencode (`qa-movil` / `dev-android`)
- **Módulo / tareas:** `001-inicio-visitante`, `002-registro`, `003-iniciar-sesion` y `004-recuperar-contrasena` → T101, T102, T201, T202, T301, T401 y T402 ✅; T104, T204, T303 y T404 ✅; T103, T203 y T403 ⛔ (backend); T302 ⏳ (aprobación)
- **Qué se hizo:**
  - CA-02: evidencia de arranque en emulador (`am start -W` → `TotalTime 2677 ms`) con capturas en `docs/evidencia/CA-02-0*.png` y `check.md` marcado ✅ (commit `ba0d5fd`).
  - Refactor MVVM (commit `ec2882c`): casos de uso de validación y recuperación en `:core:domain`, cuatro `ViewModel` con `UiState` inmutable y un solo `StateFlow`, eventos vía `SharedFlow`, y pantallas 001–004 con `hiltViewModel()` + `collectAsState()`.
  - Pruebas: 30 unitarias de `:core:domain` + 19 de los cuatro `ViewModel` (Turbine) + 4 de UI Compose con Robolectric (`SmokeTest`), deps de testing en `libs.versions.toml` y `testOptions.unitTests.isIncludeAndroidResources`.
- **Evidencia:** `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` → `BUILD SUCCESSFUL` (2026-11-09 UTC); `:core:domain:test` 30 pruebas y `:app:testDebugUnitTest` 29 pruebas, 0 fallos (re-ejecutado con `--rerun-tasks`).
- **Decisiones:**
  - AGP `9.5.1` → `9.3.0` en `libs.versions.toml` (ajuste hecho desde Android Studio; builds en verde, no revertir); Kotlin 2.4.10 y KSP 2.3.13 se mantienen.
  - Tests de UI en Robolectric (sin emulador): `@Config(sdk = [36])` y `@GraphicsMode(NATIVE)`; en `app/build.gradle.kts` se añaden `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED` y `--add-opens=java.base/java.io=ALL-UNNAMED` al task de tests.
  - Corrección de fidelidad en `GuestHomeViewModel.guestState`: `selectedVenueId = selected?.id ?: NO_VENUE` (achicar el radio limpia la selección, como en la UI original).
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - `performClick` no sincroniza el frame: tras un click que dispara `LaunchedEffect` hace falta `compose.waitForIdle()` antes de aserciones sobre callbacks.
  - `onNodeWithText` ambiguo cuando un título coincide con la etiqueta de un campo («Nueva contraseña»): usar un nodo único (`field_code`) o `performScrollTo()` para contenido fuera de pantalla (viewport Robolectric ≈ 320×470 dp).
  - Robolectric 4.17 + JDK 17 + SDK 36 falla en `ApplicationSharedMemory` sin `--add-exports jdk.internal.access`.
  - `packageDebug` puede fallar en Windows por bloqueo transitorio del APK; un reintento lo resuelve.
- **Pendiente:** commit de esta fase; T302 (login + MockWebServer) pendiente de aprobación; T103/T203/T403 ⛔ hasta que `VirGo-API` entregue contratos; T105/T205/T304/T405 (integración con URL real); regla `createComposeRule` v1 deprecada → migrar a `v2` cuando se estabilice.

### 2026-11-08 - T302: repositorio de login y caso de uso con MockWebServer

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `003-iniciar-sesion` → T302 ✅; `000-arquitectura-base` → T018 ✅, T019 y T021 🔄 (parciales)
- **Qué se hizo:**
  - `:core:common`: `AppError` (Network, Timeout, Unauthorized, NotFound, Validation, Server, Unknown) y `AppResult` (T018).
  - `:core:network`: DTOs `LoginRequestDto`/`LoginResponseDto`/`UserDto`/`ProblemDetailsDto`, `AuthApi` (`POST api/v1/auth/login`) y `createAuthApi` con OkHttp (timeouts 10/20 s) + converter kotlinx.serialization; en `libs.versions.toml` `retrofit = 3.0.0` y `okhttp = 4.12.0`.
  - `:core:domain`: `AuthRepository`, `AuthSession`/`LoggedInUser` y `LoginUseCase` (delega al repositorio, sin normalizar el correo).
  - `:core:data`: `AuthRepositoryImpl` con `@Inject` y mapeo de errores: 400 → `Validation(errors)`, 401 → `Unauthorized`, 404 → `NotFound`, 5xx → `Server(traceId)`, `IOException` → `Network`, `SocketTimeoutException` → `Timeout`, resto → `Unknown`; `CancellationException` se propaga.
  - `AuthRepositoryImplTest`: 8 pruebas MockWebServer (200 con aserción de path y body, 401, 400 con `errors`, 500 con `traceId`, 404, sin conexión, timeout con `NO_RESPONSE`, 200 malformado) que cubren las 7 variantes de `AppError`.
- **Evidencia:** commit `c76d17a`; `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` → `BUILD SUCCESSFUL` (2026-11-09 UTC); `:core:data:testDebugUnitTest` 8 pruebas, 0 fallos; total del pipeline 67 pruebas (30 dominio + 29 app + 8 data).
- **Decisiones:**
  - Retrofit 3.0.0: `HttpException.response()` es nullable → `response()?.toAppError() ?: AppError.Unknown`.
  - `catch (Exception)` en `login` con `@Suppress("TooGenericExceptionCaught")` (detekt corre con `buildUponDefaultConfig`) y rethrow de `CancellationException`; códigos HTTP como constantes `HTTP_*` por `MagicNumber`.
  - El 429 del contrato se mapea a `Server(traceId)` porque no hay variante `RateLimited`; candidata a nueva variante si el rate limit se vuelve visible en UI.
  - El wiring de `LoginViewModel` → `LoginUseCase` queda como siguiente paso (cambia T301: el test del VM usará un fake de `AuthRepository`).
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - El plugin `kotlin.serialization` se aplica solo en el módulo que declara `@Serializable`; basta con la librería en quien decodifica.
  - `Json.asConverterFactory("application/json".toMediaType())` exige `ignoreUnknownKeys = true` para tolerar campos nuevos del backend sin romper el login.
  - MockWebServer corre en JVM dentro de un módulo Android sin Robolectric; `SocketPolicy.NO_RESPONSE` + timeouts de 100 ms reproducen `SocketTimeoutException` de forma rápida y estable.
  - `ktlintFormat` reordena imports (grupo `java`/`javax` al final) y reformatea firmas; conviene releer el archivo tras formatear antes de volver a editarlo.
- **Pendiente:** wiring de `LoginViewModel` al caso de uso (ofrecido al usuario); T019 (API_BASE_URL por variante + guarda `https://` en release) y T021 (`HealthApi`); T103/T203/T403 ⛔ (backend); T105/T205/T304/T405 (URL real); T028 (pruebas de red completas).

### 2026-11-08 - T301: LoginViewModel integrado con LoginUseCase y T019 cerrada

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `003-iniciar-sesion` → T301 (integración con T302); `000-arquitectura-base` → T019 ✅ (17/39)
- **Qué se hizo:**
  - `LoginViewModel` inyecta `LoginUseCase`; `submit()` valida en local, evita doble envío, marca `isLoading` y mapea `AppResult` → `loggedIn` o `error: AppError?` en `LoginUiState`; editar un campo limpia el error.
  - `LoginScreen` muestra el error de API con textos de `strings.xml` (`login_error_generic` para 401 según RF-02, `login_error_network`, `login_error_timeout`, `login_error_server`) y deshabilita «Entrar» mientras carga (CA-02); bloque de error extraído a `LoginErrorText` por el límite de 60 líneas de detekt.
  - DI: `NetworkModule` (`@Provides AuthApi` con `BuildConfig.API_BASE_URL`) en `:core:network` y `AuthModule` (`@Binds AuthRepositoryImpl → AuthRepository`) en `:core:data`, ambos con el plugin `virgo.hilt`.
  - `API_BASE_URL` por variante (ADR-08) en `:core:network`: debug = `local.properties → virgo.apiBaseUrl` o `http://10.0.2.2:5122/`; release = `VIRGO_API_BASE_URL` con validación `https://` que falla la construcción si es `http://`.
  - `:core:testing` con `FakeAuthRepository`, `sampleAuthSession()` y `MainDispatcherRule` (plan §4); `LoginViewModelTest` con 7 pruebas y `SmokeTest` actualizados al nuevo constructor.
- **Evidencia:** commit `32e6eec`; `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` → `BUILD SUCCESSFUL` (2026-11-09 UTC); 70 pruebas, 0 fallos (32 app + 30 dominio + 8 data); `VIRGO_API_BASE_URL=http://inseguro.example/ ./gradlew help` → `BUILD FAILED` con «VIRGO_API_BASE_URL de release debe empezar con https:// (ADR-08)».
- **Decisiones:**
  - El error de API vive en el `UiState` como `AppError?` y la pantalla lo resuelve a texto (el VM no ve `Context`).
  - `device` se manda `null` (opcional en el contrato) hasta que se defina qué identificador de dispositivo envía la app.
  - `VIRGO_API_BASE_URL` queda vacío en release mientras no exista T033; la validación solo corre cuando la variable está definida.
  - CA-03 (estado de error al abrir la pantalla sin conexión, con reintento) queda para la fase de `check.md`, no para el wiring.
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - Con `MainDispatcherRule` (`Dispatchers.setMain(UnconfinedTestDispatcher())`), `viewModelScope.launch` con repo fake corre inline y los tests del VM siguen siendo síncronos.
  - Los `@Module/@Binds` viven en bibliotecas Android y necesitan `virgo.hilt` (KSP + compiler) además del `@Inject` del constructor; la cadena Hilt completa es `AuthApi → AuthRepositoryImpl → AuthRepository → LoginUseCase → LoginViewModel`.
  - detekt `LongMethod` (60) aplica a composables: extraer bloques como el texto de error a su propio `@Composable` lo resuelve sin tocar la lógica.
  - `buildFeatures { buildConfig = true }` sigue funcionando en AGP 9, también en módulos biblioteca.
- **Pendiente:** T021 (`HealthApi`) y T020 (interceptores); T028 (pruebas de red completas); T103/T203/T403 ⛔ (backend); T105/T205/T304/T405 (URL real); CA-03 para `check.md`.

### 2026-11-08 - T021 y T023: HealthApi, HealthRepository con latencia y caso de uso

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `000-arquitectura-base` → T021 ✅ y T023 ✅ (19/39)
- **Qué se hizo:**
  - `:core:network`: `HealthApi` (`GET health` → `HealthStatusDto`, contrato `VirGo-API/specs/000-arquitectura-base/plan.md`: `200 {"status":"Healthy"}` sin auth) y refactor de `NetworkClientFactory` con `createRetrofit` compartido por `createAuthApi`/`createHealthApi`; `NetworkModule` agrega `@Provides HealthApi`.
  - `:core:domain`: modelo `ApiHealth(status, latencyMs)`, interfaz `HealthRepository` y `CheckApiHealthUseCase` (delegación pura, `@Inject`).
  - `:core:data`: `HealthRepositoryImpl` mide la latencia con `TimeSource.Monotonic` y convierte errores con el `toAppError()` de T302 (CancellationException se propaga); `HealthModule` con `@Binds`.
  - Pruebas: `HealthRepositoryImplTest` (4 MockWebServer: 200 con aserción de path y latencia ≥ 0, 503 → `Server`, sin conexión → `Network`, JSON malformado → `Unknown`) y `CheckApiHealthUseCaseTest` (2: delegación y propagación de fallo).
- **Evidencia:** commit `d6a7c9b`; `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` → `BUILD SUCCESSFUL` (2026-11-09 UTC) y `:core:domain:test` en verde; **76 pruebas, 0 fallos** (32 app + 30+2 dominio + 8+4 data).
- **Decisiones:**
  - La latencia se mide en el repositorio (cerca de la llamada real), no en el caso de uso, para que el `AppResult` lleve el tiempo verdadero de la red.
  - `status` se conserva como texto crudo (`"Healthy"`) y no se normaliza a `Boolean`; la UI de T024 decidirá el formato.
  - T021 y T023 se ejecutaron juntas por la dependencia directa (T023 depende de T021) con evidencia por tarea.
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - `./gradlew testDebugUnitTest` **no** ejecuta `:core:domain:test`: los módulos JVM puros usan el task `test`. El pipeline de CI (T031) debe usar `test` (o agregar `:core:domain:test`) para no perder las pruebas de dominio.
  - `kotlin.time.TimeSource.Monotonic` es stdlib y no necesita dependencia ni opt-in en Kotlin 2.4.
  - `createRetrofit` compartido evita duplicar la configuración de JSON/OkHttp cuando aparece un segundo API.
- **Pendiente:** T022 (`TokenStore`) → desbloquea T020 (interceptores) → T028 (pruebas de red completas); T024-T025 (diagnóstico en debug, sobre T023); T103/T203/T403 ⛔ (backend); T105/T205/T304/T405 (URL real).

### 2026-11-09 - T024 (en curso) y T025: diagnóstico en debug y menú de desarrollo

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `000-arquitectura-base` → T025 ✅ y T024 🔄 (20/39)
- **Qué se hizo:**
  - Source set `debug` de `:app`: `DiagnosticsRoute`, `DiagnosticsUiState`/`DiagnosticsViewModel` (el `init` lanza `CheckApiHealthUseCase` y expone versión de `BuildConfig`, `API_BASE_URL` de `:core:network`, estado, latencia y `AppError`) y `DiagnosticsScreen` (encabezado con regresar, `VirGoCard` con versión/URL/estado/latencia, texto de error en español y botón Reintentar); textos en `app/src/debug/res/values/strings.xml`.
  - T025: `DevMenu` (⋮ → «Diagnóstico» → `navigate(DiagnosticsRoute)`) y `NavGraphBuilder.developmentDestinations(...)` con implementación real en `debug` y no-op `= Unit` en `app/src/release`; `VirGoApp` muestra el menú en el `topBar` del `Scaffold` solo en el inicio provisional y `VirGoNavHost` registra `developmentDestinations(navController)`.
  - `buildFeatures.buildConfig = true` en `:app` (para `BuildConfig.VERSION_NAME`) y `FakeHealthRepository` en `:core:testing` con `yield()` para poder observar el estado de carga en las pruebas.
  - Pruebas nuevas en `app/src/testDebug` (5): `DiagnosticsViewModelTest` con Turbine y `MainDispatcherRule(StandardTestDispatcher())` (carga → éxito, carga → error y reintento tras error) y `DiagnosticsScreenTest` con Robolectric («API disponible» y mensaje de error claro sin cerrar la pantalla).
- **Evidencia:** commit `b826b05`; pipeline `ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → `BUILD SUCCESSFUL`; **81 pruebas, 0 fallos**. T025 verificado sobre los artefactos de `assembleRelease`: sin clases `Diagnostics*`, el bytecode de `DevMenuKt` de release no contiene `DropdownMenu`/`MoreVert` y los `values.xml` de release no incluyen `diagnostics_*` ni `dev_menu_*` (debug sí).
- **Decisiones:**
  - Las pruebas de pantallas de debug viven en `src/testDebug` (no en `src/test`) para que `testReleaseUnitTest` no intente compilar clases que no existen en release.
  - El menú solo en la pantalla de inicio: en release el `topBar` queda vacío (0 dp) y no cambia la UI.
  - T024 queda 🔄: el código y las pruebas están hechos, pero CA-04/CA-05 exigen la evidencia real contra la API local y `VirGo-API` todavía no tiene código (T009) → se retoma en T030.
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - En AGP 9 las clases compiladas ya no están en `app/build/tmp/kotlin-classes/{variante}`: están en `app/build/intermediates/built_in_kotlinc/{variante}/compile{Variante}Kotlin/classes`, y los strings combinados en `app/build/intermediates/incremental/{variante}/merge{Variante}Resources/merged.dir/values/values.xml`. Así se aporta la evidencia de «ausente en release».
  - Un `@Composable fun X(...) = Unit` sirve como stub de variante sin disparar `EmptyFunctionBlock` de detekt.
- **Pendiente:** T022 (`TokenStore`) → desbloquea T020 (interceptores) → T028 (pruebas de red completas); T024 evidencia CA-04/CA-05 con la API local (T030); T026/T027 (seguridad); T103/T203/T403 ⛔ (backend); T105/T205/T304/T405 (URL real).

### 2026-11-10 - T022: TokenStore cifrado con DataStore y Tink AEAD

- **Autor:** equipo + opencode (`dev-android`)
- **Módulo / tareas:** `000-arquitectura-base` → T022 ✅ (21/39)
- **Qué se hizo:**
  - `:core:datastore`: interfaz `TokenStore` (`val token: Flow<String?>`, `suspend fun save(token)`, `suspend fun clear()`) y `TokenStoreImpl` que cifra con un `Aead` de Tink y guarda en DataStore Preferences solo el texto cifrado en Base64; al leer, `GeneralSecurityException` o Base64 inválido se convierten en `null`.
  - `KeystoreAead` fabrica el `Aead` con `AndroidKeysetManager` (`withSharedPref` + `withMasterKeyUri("android-keystore://virgo_token_master_key")` + plantilla `AES256_GCM`): la llave de datos queda protegida por el Android Keystore.
  - Módulos Hilt en `core/datastore/.../di/`: `DataStoreModule` (`@Provides DataStore<Preferences>` con el delegate `preferencesDataStore(name = "token_store")` y `@Provides Aead`) y `TokenStoreModule` (`@Binds TokenStore`).
  - Dependencias nuevas en `libs.versions.toml`: `androidx.datastore:datastore-preferences:1.3.0` y `com.google.crypto.tink:tink-android:1.18.0`.
  - 4 pruebas Robolectric (`TokenStoreTest`): guardar→leer, borrar→null, el archivo no contiene el token en claro (CA-07) y valor ilegible (Base64 inválido o cifrado ajeno)→`null`.
- **Evidencia:** commit `456d738`; pipeline `ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → BUILD SUCCESSFUL; **85 pruebas, 0 fallos** (+4).
- **Decisiones:**
  - El `Aead` de producción (Keystore) es una dependencia inyectable: Robolectric no implementa `AndroidKeyStore`, así que las pruebas usan un `AesGcmJce` local con llave aleatoria para verificar guardar/leer/borrar y que el archivo no contiene el token en claro; la ruta del Keystore real se valida en dispositivo (T030/T027).
  - Un `@Module` de Dagger no puede mezclar `@Provides` concretos con `@Binds` abstractos: se separó en `DataStoreModule` (object) y `TokenStoreModule` (clase abstracta), siguiendo el patrón de `NetworkModule` y `AuthModule`.
  - `SwallowedException` de detekt se cumple nombrando el parámetro del `catch` como `ignored`.
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - En DataStore 1.3.0 (KMP) `PreferenceDataStoreFactory` vive en `androidx.datastore.preferences.core` (artefacto `datastore-preferences-core`, expuesto como `api` por `datastore-preferences`), mientras que el delegate `preferencesDataStore` sigue en `androidx.datastore.preferences`.
  - `tink-android` 1.18.0 trae `AndroidKeysetManager` en el paquete `com.google.crypto.tink.integration.android` y `KeyTemplates.get("AES256_GCM")` funciona sin registrar claves.
- **Pendiente:** T020 (interceptores, ya desbloqueado por T022) → T028 (pruebas de red completas); T024 evidencia CA-04/CA-05 con la API local (T030); T026/T027 (seguridad); T103/T203/T403 ⛔ (backend); T105/T205/T304/T405 (URL real).

### 2026-11-10 - T020 y T028: interceptores de red y pruebas MockWebServer

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `000-arquitectura-base` → T020 ✅ y T028 ✅ (23/39); **F4 completa (7/7)**
- **Qué se hizo:**
  - Puerto `AuthTokenProvider` (`val token: Flow<String?>`) en `:core:common`: `:core:network` no puede depender de `:core:datastore` (§4), así que `TokenStore` lo implementa y `TokenStoreModule` lo liga con `@Binds`.
  - `CorrelationIdInterceptor` (UUID nuevo en `X-Correlation-Id` por petición, reemplaza el valor que traiga la petición) y `AuthInterceptor` (`Authorization: Bearer <token>` solo si hay token; lee el `Flow` con `runBlocking { first() }` porque OkHttp es síncrono).
  - `createOkHttpClient(authTokenProvider, isDebug)` en `NetworkClientFactory`: timeouts 10 s/20 s + correlación + auth, y `HttpLoggingInterceptor` con nivel `BASIC` solo si `BuildConfig.IS_DEBUGGABLE` (nuevo `buildConfigField` por variante en `:core:network`). `NetworkModule` ahora provee un `OkHttpClient` `@Singleton` compartido por `AuthApi` y `HealthApi`.
  - Dependencia nueva: `com.squareup.okhttp3:logging-interceptor:4.12.0`.
  - T028: 4 clases de prueba en `:core:network` (9 pruebas) con MockWebServer: `CorrelationIdInterceptorTest` (id único por petición y reemplaza al del caller), `AuthInterceptorTest` (Bearer presente, ausente y en blanco) y `OkHttpClientConfigurationTest` (BASIC solo en debug, sin logging en release, timeouts 10/20 s y 2 interceptores). La conversión a `AppError` ya estaba cubierta por `AuthRepositoryImplTest` (8) y `HealthRepositoryImplTest` (4); `CheckApiHealthUseCaseTest` (2) y `TokenStoreTest` (4) completan lo que pide T028.
- **Evidencia:** commits `0600d0f` (T020) y `86624c5` (T028); pipeline `ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → BUILD SUCCESSFUL; **94 pruebas, 0 fallos** (+9).
- **Decisiones:**
  - El puerto del token vive en `:core:common` (no en `:core:network`) para que `:core:datastore` pueda implementarlo sin depender de red.
  - Logging con nivel `BASIC` (solo línea de petición/ respuesta): no imprime headers ni body, así `Authorization` nunca llega a los logs (refuerzo de T027).
  - `AuthInterceptor` usa `runBlocking`: OkHttp es síncrono y DataStore ya cachea el valor en memoria; el costo por petición es mínimo.
- **Costo:** $0 / sin cambios (misma versión de OkHttp).
- **Aprendizaje:**
  - Un `@Module` de Dagger acepta varios `@Binds` abstractos; lo que no puede es mezclar `@Provides` concretos con `@Binds` en el mismo módulo.
  - `BuildConfig.IS_DEBUGGABLE` explícito por variante es más portable que depender de `BuildConfig.DEBUG` en módulos biblioteca.
- **Pendiente:** T024 evidencia CA-04/CA-05 (T030); T026/T027 (seguridad); T029/T030/T038 (pruebas); T031–T035/T039 (CI y release, varios 🧑); T002/T004/T036/T037.

### 2026-11-10 - T026: network security config, R8 y firma de release

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` → T026 ✅ (24/39)
- **Qué se hizo:**
  - `network_security_config` en `app/src/main` con `cleartextTrafficPermitted="false"` y el atributo `android:networkSecurityConfig` en el manifiesto; en `app/src/debug` un `domain-config` que permite en claro **solo** `10.0.2.2` (host del emulador hacia el backend).
  - R8 en *release*: `isMinifyEnabled = true`, `isShrinkResources = true`, `isDebuggable = false` y `app/proguard-rules.pro` con reglas de kotlinx.serialization (`**$$serializer` y `Companion`), retrofit2 (`Call`, `Response`, `Continuation`) y `-keep class com.google.crypto.tink.** { *; }`.
  - Firma de *release* desde variables de entorno (`SIGNING_KEYSTORE_FILE`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`): si se pide una tarea `*Release*` sin ellas, la configuración lanza `GradleException` con mensaje claro; *release* nunca se firma con la llave de debug.
- **Evidencia:** commit `c0b7e64`; pipeline `ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → BUILD SUCCESSFUL; **94 pruebas, 0 fallos**; `app/build/outputs/mapping/release/mapping.txt` generado (42 MB); `apksigner verify --print-certs` → firma **V2** con el keystore local dedicado; el XML empaquetado de *release* no contiene `10.0.2.2` ni `domain-config` (el de *debug* sí); manifiesto mergeado de *release* sin `android:debuggable` (el de *debug* lo trae); `assembleRelease` sin las 4 variables → `BUILD FAILED` con «Falta la firma de release… Release nunca se firma con la llave de debug (T026)».
- **Decisiones:**
  - Keystore local dedicado (autofirmado, fuera del repo, en el temp de opencode) para poder correr el pipeline con firma; el keystore real llega en T033 (🧑, GitHub Secrets) y la custodia es P-02.
  - La evidencia runtime de CA-06 (bloqueo de una solicitud `http://` en *release*) queda en T030: exige dispositivo/emulador y la API local; T026 aporta la evidencia de build (manifiesto y XML por variante).
- **Costo:** $0 / keystore autofirmado local; `SIGNING_*` viven en variables de entorno de usuario, nunca en el repo.
- **Aprendizaje:**
  - En AGP 9 el XML empaquetado por variante queda en `app/build/intermediates/packaged_res/{variante}/package{Variante}Resources/xml/` y el manifiesto mergeado en `intermediates/merged_manifest/{variante}`.
  - `jarsigner -verify` reporta «jar is unsigned» porque AGP firma con esquema v2/v3; la verificación correcta es `apksigner verify --print-certs`.
- **Pendiente:** T027 (revisión de seguridad, ya desbloqueada); T029/T030/T038 (pruebas); T031–T035/T039 (CI y release, varios 🧑); T002/T004/T036/T037.

### 2026-11-10 - T027: revisión de seguridad de la app

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` → T027 ✅ (25/39)
- **Qué se hizo (reporte):**
  - **Permisos:** hallazgo **alto** — el manifiesto no declaraba `android.permission.INTERNET`; sin él la app no puede hacer ninguna llamada de red en dispositivo (las pruebas MockWebServer corren en JVM y no lo detectan). Corregido en `app/src/main/AndroidManifest.xml`; el manifiesto mergeado de *release* ahora lo incluye. No hay permisos peligrosos (cámara, ubicación, contactos, etc.); solo la firma `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` de AndroidX.
  - **Secretos:** sin credenciales, tokens ni API keys en código (las únicas coincidencias son strings de prueba: `virgo-token-abc`, `password1`); ningún `.jks`/`.keystore`/`local.properties` trackeado; las variables `SIGNING_*` viven en el entorno, nunca en el repo. Gitleaks corre en CI (`security.yml`, T031).
  - **Logs:** cero `Log.*`, `println` o `System.out` en el código; `HttpLoggingInterceptor` con nivel `BASIC` solo en *debug* (cubierto por `OkHttpClientConfigurationTest`).
  - **Almacenamiento:** sin `SharedPreferences` en claro; el único almacenamiento es `TokenStore` cifrado con Tink + Keystore (CA-07).
  - **R8:** `isMinifyEnabled`/`isShrinkResources` en *release*, `proguard-rules.pro` completo y `mapping.txt` generado (42 MB).
  - **Lint (hallazgos medios corregidos):** 8 errores — `LocalContextGetResourceValueCall` ×3 en `RecoverScreen` (strings izados con `stringResource` en composición y `format(event.code)` dentro del efecto) y `ViewModelConstructorInComposable` ×5 (2 previews → `@SuppressLint`, patrón ya usado por Login/Register; 3 tests de `SmokeTest` → construcción del VM izada fuera de `setContent`, como el test de login de ese mismo archivo).
- **Evidencia:** commit `ad6a18c`; pipeline `ktlintCheck detekt lint testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` → BUILD SUCCESSFUL; `lint` **0 errores, 21 warnings** (versiones de dependencias, recursos sin usar, `MissingApplicationIcon`, `PluralsCandidate` — sin hallazgos críticos ni altos); manifest mergeado de *release* con `INTERNET` y sin `debuggable`; **94 pruebas, 0 fallos**.
- **Decisiones:**
  - Los previews usan `@SuppressLint("ViewModelConstructorInComposable")` (patrón existente en el proyecto) porque construir el VM es intencional ahí; en tests se izó la construcción, que además evita recrear el VM en cada recomposición.
  - El icono de app (`MissingApplicationIcon`) y los strings sin usar quedan para cuando el spec de diseño traiga el recurso; no son hallazgos de seguridad.
- **Costo:** $0 / sin cambios.
- **Aprendizaje:**
  - `lint` no era parte del pipeline local; al agregarlo aparecieron 8 errores preexistentes que el CI de T031 habría bloqueado — correr el pipeline completo del plan (`ktlintCheck detekt lint …`) desde el inicio evita sorpresas en CI.
  - OkHttp/Retrofit no agregan `INTERNET` al manifiesto: es responsabilidad de la app declararlo.
- **Pendiente:** T029/T030/T038 (pruebas); T031–T035/T039 (CI y release, varios 🧑); T002/T004/T036/T037.



### 2026-11-11 - T029: pruebas de UI, designsystem y navegacion del flujo visitante

- **Autor:** equipo + opencode (`qa-movil`)
- **Modulo / tareas:** `000-arquitectura-base` -> T029 -> (26/39)
- **Que se hizo:**
  - `DesignSystemComponentsTest` (5 pruebas, modulo `core/designsystem`): se agregaron las dependencias de test al `build.gradle.kts` del modulo y se cubrieron los componentes base del sistema de diseno.
  - `FontScaleTest` (2 pruebas): escala de fuente 2.0 con `FontScale`.
  - `NavigationTest` (2 pruebas): destino inicial provisional (marca + login) y flujo pin -> "Ver mas." -> `register_title` -> back -> inicio provisional.
  - Fix en `GuestMap.kt`: el `pointerInput` con `detectTapGestures` (limpiar seleccion) se movio del `Box` ancestro al `Canvas` (hermano detras de los pins); con eso un pin aislado si recibe el toque.
- **Evidencia:** commit `7d8e0c7`; pipeline `ktlintCheck detekt lint testDebugUnitTest assembleDebug assembleRelease` + `:core:domain:test` -> BUILD SUCCESSFUL; **103 pruebas, 0 fallos**; APK debug (21.7 MB) y release (2.4 MB) generados.
- **Decisiones:**
  - Robolectric no implementa `AndroidKeyStore`: los destinos con `hiltViewModel`/Hilt (`KeystoreAead.create`) crascan en "The token keyset could not be initialized"; por eso los tests de navegacion usan destinos con VMs puros (registro via pin).
  - Hallazgo de UI: en pantalla completa la tarjeta opaca de la cabecera (`GuestCard`, bounds `(16,76)-(304,222)`) cubre fisicamente los pins cercanos al centro (p. ej. Casa Oliva `(151,178)-(199,226)`): el toque en su centro no llega a ningun handler. El test usa un pin visible debajo de la tarjeta (Brincolin Fiestas, y 230-278) con matcher por subcadena ASCII que funciona con ambas variantes (`CompactPin` con `contentDescription` y `LabelledPin` con texto).
- **Costo:** $0 / sin cambios de producto; solo pruebas y el fix del puntero.
- **Aprendizaje:**
  - El hit-test de Compose reparte el toque por z-order: un nodo opaco posterior (la tarjeta) absorbe los toques de los pins que quedan debajo; verificar la visibilidad geometrica antes de diagnosticar fallos de dispatch.
  - `computePins` alterna `CompactPin` (cd `guest_pin_cd`) y `LabelledPin` (texto `"<short>, <km> km"`) segun solape; buscar pins por cd exacta con acentos es fragil (normalizacion Unicode), mejor matcher por subcadena ASCII.
- **Pendiente:** T030/T038 (pruebas); T031-T035/T039 (CI y release, varios P); T002/T004/T036/T037.


### 2026-11-11 - T038: linea base de mutacion con Pitest

- **Autor:** equipo + opencode (`qa-movil`)
- **Modulo / tareas:** `000-arquitectura-base` -> T038 -> (27/39)
- **Que se hizo:**
  - Plugin `info.solidsoft.pitest` 1.19.0 (`gradle-pitest-plugin`) en `:core:domain` y `:core:common` via catalogo de versiones (`libs.versions.toml`), con `alias(... ) apply false` en la raiz y `targetClasses` explicitos por paquete (`mx.virgo.core.domain.*`, `mx.virgo.core.common.*`): sin el filtro, el default usa `project.group` (vacio) y PIT falla con "No mutations found".
  - `:core:common` no tenia pruebas; se agrego `AppResultTest` (4 pruebas: valor de Success/Failure, igualdad de valores) mas `testImplementation(libs.junit)`, porque PIT exige al menos una prueba que pueda matar mutaciones.
- **Evidencia:** commit `04cbcd2`; `./gradlew pitest` -> BUILD SUCCESSFUL. Linea base: **`:core:domain` 73% de mutacion (35/48 mutaciones matadas, test strength 97% 35/36, linea 71%)**; **`:core:common` 33% (1/3, test strength 100%, linea 100%)** - solo 3 mutaciones porque el modulo son tipos sellados. Pipeline completo (`ktlintCheck detekt lint testDebugUnitTest assembleDebug assembleRelease :core:domain:test :core:common:test pitest`) -> BUILD SUCCESSFUL; **107 pruebas, 0 fallos**.
- **Decisiones:**
  - Primero se mide, despues se fija el umbral (plan sec. "Mutacion"): el 73% de `domain` es la referencia actual; el umbral de CI llega con T031 cuando se integre `pitest` al workflow.
  - `common` queda con porcentaje bajo por artefacto (3 mutaciones en tipos sellados sin logica); no representa riesgo y se documenta para no fijar un umbral global ingenuo.
- **Costo:** $0 / plugin open source corriendo en local y en CI.
- **Aprendizaje:**
  - Un timeout de build (15 min) puede dejar el arbol de dependencias Hilt a medias: `VirGoApplication_ComponentTreeDeps.java` perdio los `@Module` de `:core:data` (`AuthModule`, `HealthModule`) y `hiltJavaCompileDebug` fallo con `Dagger/MissingBinding` aunque el codigo estaba intacto. Fix: `gradlew :app:hiltAggregateDepsDebug --rerun-tasks` (el nombre de tarea en esta version es `hiltAggregateDeps*`, no `hiltAggregateDependencies*`).
  - Con `build-logic` (plugins precompilados), verificar nombres de tarea reales con `gradlew :app:tasks --all` antes de diagnosticar.
- **Pendiente:** T030 (emulador); T031/T032/T034 (workflows `.github/`, T034 requiere T033 🧑); T002/T004/T033/T035/T036/T037/T039.


### 2026-11-11 - Sincronizacion a virgorent/VirGO_Movil (equipo Play Store)

- **Autor:** equipo + opencode
- **Modulo / tareas:** solicitud extra fuera del task.md (apoyo a T035/T036)
- **Que se hizo:**
  - Snapshot del contenido de `VirGo-Movil/` (274 archivos trackeados, sin artefactos de build ni `gradle-daemon-jvm.properties`) extraido con `git archive` desde `451418d` (27/39) y sincronizado al repo de la organizacion **`virgorent/VirGO_Movil`** (espejo del monorepo creado por `Karasu0001`, con la app bajo `VirGo-Movil/`).
  - El `main` de ese repo esta protegido por el ruleset `main-protection` (PR + 1 aprobacion + checks `gitleaks` y `dependency review`), asi que la subida fue por **PR #3** (rama `sync/001-snapshot-27-39`, commit `fbf6774`, autor `ccabrera-dev8`): 147 cambios = 95 archivos nuevos, 0 archivos propios del repo eliminados (el snapshot es superset de la copia que ya tenia).
  - Motivo: el equipo que subira la app a Play Store necesita el codigo en un repo propio.
- **Evidencia:** PR https://github.com/virgorent/VirGO_Movil/pull/3 con **ambos checks en verde** (`gitleaks` + `dependency review`); clone de prueba en temp con **274 archivos** (identico al origen) y `gradlew help` -> OK; revision solicitada a `Karasu0001`.
- **Decisiones:**
  - **Snapshot unico, sin sincronizacion automatica**: los cambios futuros de `virGO_App` no llegan solos; se repite el snapshot (`git archive` + rama + PR) cuando se pida.
  - **Habilitar Dependency graph en repos nuevos**: `VirGO_Movil` nacio con el graph apagado y el check `dependency review` fallaba con "Dependency graph is not enabled"; no hay endpoint REST para ese toggle, pero `PUT /repos/{owner}/{repo}/vulnerability-alerts` lo habilita como efecto necesario (verificado con `GET .../dependency-graph/sbom`).
  - **Identidad del commit**: usar siempre `ccabrera-dev8` (`-c user.name/-c user.email`); el primer snapshot (`a1e44c3`) quedo con el autor de la cuenta con la que estaba autenticado `gh` y por eso se descarto como destino.
- **Intentos descartados:**
  - `motosos215-lab/VirGo-Movil` (cuenta ajena, token local sin `delete_repo`): quedo **vacio y privado**, pendiente de borrar desde su Danger Zone por su propietario.
  - `ccabrera-dev8/VirGo-Movil`: creado por error con el snapshot (`a1e44c3`); **pendiente de borrar** (requiere scope `delete_repo` en `gh auth refresh`).
- **Colaboradores del repo de la org:** `Karasu0001` y `ccabrera-dev8`; el equipo de Play Store se invita con `gh api -X PUT repos/virgorent/VirGO_Movil/collaborators/<usuario> -f permission=push`.
- **Pendiente:** merge del PR #3 (falta la aprobacion de `Karasu0001`; el autor no puede aprobar su propio PR); repetir snapshot al cierre de T036; borrar `ccabrera-dev8/VirGo-Movil` y `motosos215-lab/VirGo-Movil`.

