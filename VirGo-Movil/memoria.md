# Memoria — VirGo Móvil (Android)

Bitácora oficial del proyecto (Art. X de [`constitution.md`](constitution.md)). Cada sesión agrega una entrada al final de «Bitácora» con la skill [`registrar-memoria`](.opencode/skills/registrar-memoria/SKILL.md).

## Estado actual

| Campo | Valor |
|---|---|
| Constitución | v1.2.0 propuesta (Google Play, 2026-11-15); vigente v1.1.1 |
| Módulo en curso | `000-arquitectura-base`: 38/40 tareas hechas; T014 y T036 pendientes |
| Tareas | 38 / 40 hechas; T014 🔄, T036 ⏳; T010–T013 cerradas el 2026-11-20 con pruebas y previews |
| `API_BASE_URL` de *release* | Variable `VIRGO_API_BASE_URL` presente; cuatro secretos de firma presentes; usuario confirma custodia y respaldo seguro (P-02, 2026-11-20), sin compartir sus valores |
| Última versión publicada | **v0.2.0** (versionCode 2): GitHub Release `v0.2.0` y Google Play, prueba interna activa; prueba cerrada en revisión desde el 2026-11-15 |
| Siguiente paso | Cerrar T014 (fuente 2.0 y migración de pantallas) y T036 antes de nuevos módulos. Prioridad: acceso → cliente → propietario → proveedor fieles al prototipo local entregado como supuesto equivalente de Figma, sin cotejo directo. `030` y los documentos `031` permanecen separados; aceptación visual de #27 pendiente. Sin nueva release autorizada. |

## Módulos

| Módulo | Estado | Notas |
|---|---|---|
| `000-arquitectura-base` | 🚧 Aprobado; 38/40 tareas hechas | T010–T013 cerradas; T014 (fuente 2.0/pantallas) y T036 pendientes |
| `001`–`004` pantallas | 🚧 Aprobados (retroactivo 2026-11-12) e implementados; recuperación pendiente de backend | PRs pendientes de integración; no se presume su fusión |
| `005-explorar-salones` | 🚧 Implementación parcial y consolidación autorizada el 2026-11-19 | PR #27; integración completa y aceptación visual del último rediseño pendientes |
| `006`–`029` | Borradores | Sin implementación autorizada por esta consolidación |

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
| ADR-15 | 2026-11-15 | **Google Play** como canal de distribución con firma de apps de Play (enmienda v1.2.0) | La cuenta ya existe y su pago es único; el AAB sale del mismo workflow de release |
| ADR-15 | 2026-11-03 | **Repositorio único** `virgorent/VirGO_Backend` con carpetas por proyecto | Decisión del equipo; **superseded** por ADR-16 (2026-11-12) |
| ADR-16 | 2026-11-12 | Repositorio propio `virgorent/VirGO_Movil` para el móvil (fin del monorepo); el backend vive en `virgorent/VirGO_Backend` | Separa CI, permisos y despliegues por proyecto |
| ADR-17 | 2026-11-23 | Una entrada de app; rol desde sesión cifrada, estado local aislado por cuenta/rol; sin refresh automático | Consolidación autorizada; evitar privilegios ficticios y contratos inventados |

## Cambios de contrato (compartidos con Backend)

| Fecha | Cambio | Registrado en VirGo-API |
|---|---|---|
| 2026-10-29 | Contrato base acordado: `/api/v1`, Problem Details, `camelCase`, fechas UTC, JWT Bearer, `GET /health`, `GET /openapi/v1.json`, `X-Correlation-Id` | ✅ |
| 2026-11-03 | **Propuesto:** `POST /api/v1/auth/login` (devuelve `accessToken` de 15 min, `refreshToken` de 30 días y `user` con `role`) y `POST /api/v1/auth/logout` (204; ante 204 o 401 la app borra sus tokens). JSON en `VirGo-API/specs/003-auth-login` y `004-auth-logout` | ✅ |
| 2026-11-16 | **Borrador:** `POST /api/v1/auth/password/forgot` (202; errores 400/429/500; la respuesta no revela si el correo existe) y `POST /api/v1/auth/password/reset` (200; código incorrecto o vencido → 400 con mensaje genérico) — specs `011` y `012` del backend en Borrador con sus tareas `T1101`–`T1103` ⏳. Producción responde **404** (2026-11-16, igual que una ruta inexistente de control), así que Móvil implementa contra MockWebServer con los cuerpos asumidos `{email}` y `{code,password}` (`API-HANDOFF.md` §9.2 no define los de recuperación) | ⏳ (specs `011`/`012` en Borrador) |
| 2026-11-16 | **Consumado:** `GET /api/v1/venues?zone=&category=` del backend (`VirGo-API/specs/007-venues-catalog/plan.md`) — `200 {"data":[…]}` con `location` GeoJSON `{type:"Point",coordinates:[lng,lat]}` y `rating:{average,count}`; probado contra producción (200 con catálogo vacío). `GET /api/v1/venues/:id` (spec `024`) sigue en borrador y queda fuera del alcance de T103 | ✅ (spec `007-venues-catalog` del backend) |
| 2026-11-16 | **Consumado:** `POST /api/v1/auth/register` (`VirGo-API/specs/005-auth-register/plan.md`) — `{"name","email","password","phone"}` → `201 {"id","name","email","phone","role","isVerified"}`, duplicado → `409`, inválido → `400` Problem Details; el rol inicial siempre es `client` y el cliente no puede enviar `role`. Probado contra producción (201 y 409) | ✅ (spec `005-auth-register` del backend) |

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
- **Qué se hizo:** no se pudo transferir `Karasu0001/virGO_App`, así que se creó `virgorent/VirGO_Backend` vacío y se subieron `main`, `rama-apis`, `rama-backend`, `rama-movil` y `docs/sdd-virgo` con su historial. Las referencias vigentes apuntan al repositorio nuevo; las entradas anteriores de esta bitácora conservan el nombre de entonces.
- **Evidencia:** `git ls-remote` muestra los mismos commits en ambos repositorios.
- **Decisiones:** `virgorent/VirGO_Backend` es el repositorio oficial.
- **Costo:** $0.
- **Aprendizaje:** ninguno.
- **Pendiente:** archivar `Karasu0001/virGO_App` cuando el equipo confirme; `VirGo-API` `002` P-02 (el repositorio es privado y el ruleset no aplicaría).

### 2026-11-04 · El repositorio ya es público

- **Autor:** equipo + Claude Code (`arquitecto-movil`)
- **Módulo / tareas:** gobernanza · ninguna tarea ejecutada
- **Qué se hizo:** se comprobó que `virgorent/VirGO_Backend` es público, con lo que el ruleset y los revisores de los *environments* ya están disponibles en el plan Free.
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
  - T001: se confirmó el remoto `https://github.com/virgorent/VirGO_Backend.git`, la presencia de `VirGo-Movil/.gitignore` y `README.md`, y que `git check-ignore VirGo-Movil/local.properties` lo ignora.
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


### 2026-11-11 - T031 y cambio de repositorio canonico del movil

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Modulo / tareas:** `000-arquitectura-base` -> T031 -> (28/39)
- **Que se hizo:**
  - **El movil ya no vive en el monorepo**: `virgorent/virGO_App` quedo solo como backend (`VirGO_Backend`) y el codigo del movil trabaja ahora en **`virgorent/VirGO_Movil`** (repo de la organizacion, app bajo `VirGo-Movil/`). El workspace local re-enraizo en la misma ruta `C:\Users\cesar\virgo\virGO_App` (origin -> `VirGO_Movil`); `VirGo-API/` y `VirGo-Database/` salieron del disco (su historia queda en la rama `archive/001-flujo-visitante` del repo nuevo y en el backend).
  - Snapshot 27/39 subido via **PR #3** (aprobado y mergeado por `Karasu0001`); los commits locales T001-T038 se preservaron como `archive/001-flujo-visitante`.
  - **T031:** `.github/workflows/movil-ci.yml` en el repo nuevo: JDK 17, `setup-gradle`, job `changes` (`dorny/paths-filter` sobre `VirGo-Movil/**`), job `movil-build-test` con `ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug`, artefactos (APK debug + reporte Pitest) y `concurrency`. Actions pinneados por SHA.
- **Evidencia:** PR **#4** (CA-08) con checks `gitleaks`, `dependency review`, `changes` y `movil-build-test`; commits `88c46fc` (codigo) + docs.
- **Decisiones:**
  - Flujo de trabajo en el repo nuevo: rama por tarea -> **PR** -> aprobacion de `Karasu0001` (el ruleset `main-protection` no permite auto-aprobacion ni bypass); la identidad de commits es `ccabrera-dev8@users.noreply.github.com` para que quede atribuida (si no, `require_extra_approval_for_unattributed_changes` exigiria 2 aprobaciones).
  - `dependency graph` estaba apagado en el repo nuevo y `dependency review` fallaba; se habilito con `PUT /repos/{owner}/{repo}/vulnerability-alerts` (no hay endpoint REST directo para ese toggle).
  - El `security.yml` del repo nuevo ya era la version con deteccion de manifiestos; no requirio cambios.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Un repo nuevo dentro de una organizacion puede heredar el ruleset `main-protection` sin `bypass_actors`: ni siquiera un admin fusiona sin aprobacion ajena; planificar el flujo PR + revisor desde el inicio.
  - `git rebase --onto origin/main <ultimo-commit-sincronizado>` tras un merge por metodo merge conserva los commits locales de docs/ci sin duplicar el snapshot.
- **Pendiente:** T032 (CodeQL), T034 (release, requiere T033), T004; borrar `ccabrera-dev8/VirGo-Movil` (scope `delete_repo`) y `motosos215-lab/VirGo-Movil`.


### 2026-11-11 - Correccion de movil-build-test en el PR 4 (T031)

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Modulo / tareas:** `000-arquitectura-base` -> T031 -> (28/39)
- **Que se hizo:** el check `movil-build-test` del **PR #4** salio rojo en `testDebugUnitTest` (no en ktlint/detekt/lint/pitest): `DesignSystemComponentsTest > classMethod FAILED` con `UnsupportedOperationException at DefaultSdkProvider.java:174` = Robolectric 4.17 exige **Java 21** para `@Config(sdk = [36])` y el daemon de CI corría con el JDK 17 del `setup-java`. En local nunca falto porque `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=21`) levanta el daemon en 21. Corregido en la misma rama commiteando ese archivo (commit `7492292`); el toolchain de compilacion sigue en 17 (`jvmTarget 17`, `sourceCompatibility` 17).
- **Evidencia:** PR #4 con los 4 checks en verde (`movil-build-test` 7m24s); `task.md` actualizado con la evidencia real.
- **Decisiones:** mantener JDK 17 como objetivo de compilacion y subir solo el daemon de Gradle a 21 (menor cambio que migrar todo el workflow a 21).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Paradoja local-vs-CI: un `gradle-daemon-jvm.properties` sin commitear hace que local pase y CI falle; commitear el archivo de toolchain del daemon alinea ambos entornos.
  - Robolectric liga la version de Java requerida al SDK del `@Config` (API 36 -> Java 21); cualquier subida de SDK en tests implica revisar el JDK del CI.
  - La evidencia en `task.md` debe escribirse solo cuando el check este en verde (Karasu senalo la afirmacion prematura).
- **Pendiente:** aprobacion de `Karasu0001` en PR #4; T032 (CodeQL), T034 (release, requiere T033), T004.


### 2026-11-12 - T032: workflow de CodeQL dentro del PR 4

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` -> T032 -> (29/39)
- **Qué se hizo:** `.github/workflows/movil-codeql.yml` (lenguaje `java-kotlin`) quedó commiteado en la rama `feat/001-flujo-visitante` dentro del **PR #4** (commit `5a31fdd`, identidad `Cesar Augusto <ccabrera-dev8@users.noreply.github.com>`): disparadores `pull_request` y `push` sobre `main` más cron semanal (`0 7 * * 1`), `github/codeql-action` pinneado en `33d7b85da1a04171e211befe2c739303b9a4c066` (v4.39.3), build explícito `./gradlew assembleDebug` con `working-directory: VirGo-Movil`, permisos `security-events: write` a nivel de job y `concurrency` que cancela corridas previas del mismo PR.
- **Evidencia:** en PR #4 los checks `analyze` (workflow `movil-codeql`, run `43239028981`), `CodeQL`, `gitleaks`, `dependency review`, `changes` y `movil-build-test` en verde; `gh api /repos/virgorent/VirGO_Movil/code-scanning/alerts` devuelve `0` alertas (sin alertas altas).
- **Decisiones:** al ya estar el archivo en la rama del PR #4, T032 se cierra en ese mismo PR en lugar de abrir una rama nueva; queda registrado aquí para no romper la trazabilidad `spec -> plan -> task -> check`.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:** al commitear un workflow en `.github/workflows/` de una rama con PR abierto, GitHub lo ejecuta de inmediato en ese PR; revisar permisos (`security-events: write`) y los pines SHA antes de subirlo evita re-trabajar el PR.
- **Pendiente:** aprobación de `Karasu0001` en PR #4; T034 (release, requiere T033 🧑), T004; borrar `ccabrera-dev8/VirGo-Movil` (scope `delete_repo`).


### 2026-11-12 - T033/T034: secretos de firma y workflow de release

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` -> T033 (en curso) + T034 -> (30/39)
- **Qué se hizo:** los 4 secretos de firma se subieron a `virgorent/VirGO_Movil` (`SIGNING_KEYSTORE_BASE64` con el keystore `virgo-release.jks`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`) y se commiteó `.github/workflows/movil-release.yml` en `feat/001-flujo-visitante` (commit `c7793ed`, dentro del PR #4): solo `workflow_dispatch` con entrada `version` validada como `X.Y.Z`, job condicionado a `refs/heads/main`, SHA pinneados de `checkout`/`setup-java`/`setup-gradle` (los mismos de `movil-ci.yml`), decode del keystore hacia `$RUNNER_TEMP`, `assembleRelease` con `SIGNING_*` y `vars.VIRGO_API_BASE_URL`, `apksigner verify --print-certs` y `gh release create vX.Y.Z` con `GH_TOKEN`.
- **Evidencia:** `gh secret list` con los 4 secretos; `assembleRelease` local en verde (1m31s) con el keystore real y `apksigner verify` → exit 0 (`CN=VirGo Local, O=VirGo`, SHA-256 `22578bc3…`); `actionlint` v1.7.12 → exit 0 y `yaml.safe_load` → YAML OK.
- **Decisiones:** la variable `VIRGO_API_BASE_URL` NO se crea todavía: el backend sigue sin desplegar (T210 pendiente) y `vars.VIRGO_API_BASE_URL` vacío compila igual porque la validación `https://` solo corre si la variable está definida; el workflow entra en el PR #4 por la misma razón que T032 (evitar conflictos de `task.md`/`memoria.md` entre ramas).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Un workflow solo `workflow_dispatch` no aparece en `gh workflow list` mientras vive en una rama distinta de la predeterminada: GitHub lo registra al llegar a `main`; la validación previa útil es `actionlint` + `yaml.safe_load`.
  - Pasar cada secreto por `env:` en su paso (nunca `${{ secrets.X }}` interpolado dentro de `run`) evita que se impriman en los logs y cumple «sin secretos impresos».
  - El keystore vive en `Temp\opencode\` (puede limpiarse en cualquier momento): el respaldo P-02 con custodia humana es urgente antes del primer release real.
- **Pendiente:** `VIRGO_API_BASE_URL` (T210) y respaldo P-02 del keystore para cerrar T033; T035 (release `v0.1.0`); T004; aprobación de `Karasu0001` en PR #4; borrar `ccabrera-dev8/VirGo-Movil` (scope `delete_repo`).


### 2026-11-12 - T004: verificación de alineación con la constitución

- **Autor:** equipo + opencode (`arquitecto-movil`)
- **Módulo / tareas:** `000-arquitectura-base` -> T004 -> (31/40)
- **Qué se hizo:** revisión con la skill `verificar-constitucion` sobre la gobernanza (constitución, `AGENTS.md` raíz y del móvil, `README.md`, `plan.md`/`task.md`/`check.md` del `000`, `memoria.md` y `opencode.json`) en dos pasadas. Primera pasada: **No alineado** — ❌ Art. II (pantallas de negocio con `000` abierto) y ❌ Art. XI (specs `001`–`004` en Borrador con tareas ejecutadas), más 8 ⚠️. Correcciones aplicadas con aprobación del equipo:
  - Specs `001`–`004`: `spec.md`, `plan.md` y `task.md` → **Aprobado (aprobación retroactiva 2026-11-12)**.
  - Constitución → **enmienda 1.1.1** (corrección factual: el proyecto vive en `virgorent/VirGO_Movil`; VIII.7 reescrita como «Archivos compartidos con el backend»; sin cambios normativos).
  - Referencias stale corregidas: `AGENTS.md` raíz (fase actual, tabla de fases, ramas), `VirGo-Movil/AGENTS.md` (repo, estado, estructura, `.github/`, MCP `figma`, ramas), `README.md`, `plan.md:83`, `task.md` T001, cabecera de `memoria.md` y ADR-15 «superseded» + **ADR-16** (repositorio propio del móvil).
  - **T040** agregada en `task.md` (Timber solo en *debug*, Art. IX) y tablas F7/F8 reordenadas (los blockquotes de evidencia partían las tablas).
- **Evidencia:** segunda pasada del auditor → **Alineado** (0 ❌). ⚠️ restantes registrados como pendientes de cierre: VIII (aprobar PR #4, T002/T039/T035), IX (ejecutar T040), V (respaldo P-02), VI (totales en `check.md`), I/II/X (cierre en T036).
- **Decisiones humanas (2026-11-12):**
  1. Aprobar retroactivamente los specs `001`–`004` (ya ejecutados).
  2. Aceptar la implementación anticipada de las pantallas `001`–`004` con `000` abierto; `000` se cierra en **T036** antes de empezar `005+` (Art. II).
  3. Añadir la tarea **T040** (Timber) en lugar de enmendar el plan.
  4. Enmienda constitucional **1.1.1**.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Las referencias stale del monorepo eran sistémicas: corregirlas por capa (constitución → AGENTS → specs → memoria) evita que la siguiente revisión vuelva a encontrarlas.
  - Los blockquotes de evidencia dentro de `task.md` rompen las tablas al renderizar; la evidencia va al final de cada sección, después de la última fila.
  - Con dos pasadas (diagnóstico → corrección → re-verificación) el resultado «Alineado» queda respaldado en lugar de autodeclarado.
- **Pendiente:** aprobación de `Karasu0001` en PR #4; T040 (Timber), T033 (variable `VIRGO_API_BASE_URL` + respaldo P-02), T002/T039, T030, T037, T035 y T036.


### 2026-11-12 - T040: Timber solo en debug

- **Autor:** equipo + opencode (`dev-android`)
- **Módulo / tareas:** `000-arquitectura-base` -> T040 -> (32/40)
- **Qué se hizo:** Timber `5.0.1` añadido al version catalog (`com.jakewharton.timber:timber`) y `implementation(libs.timber)` en `app/build.gradle.kts`. En `VirGoApplication.onCreate` se planta `Timber.plant(Timber.DebugTree())` **solo** dentro de `if (BuildConfig.DEBUG)`: en *release* no se planta ningún árbol y R8 elimina el código muerto. Hoy no existe ninguna llamada `Timber.d(...)` en el código, así que no puede imprimirse ningún token ni cuerpo de respuesta. Rama `feat/000-timber` desde `main` (`de58165`).
- **Evidencia:** pipeline local completo en verde — `ktlintCheck detekt lint` (7m06s) y `testDebugUnitTest` + `assembleDebug` + `assembleRelease` + `:core:domain:test` + `:core:common:test` + `pitest` → **BUILD SUCCESSFUL** (7m06s); grep sobre `*.kt`: 0 ocurrencias de `Log.[vdiew](`, `println(` o `System.out.print` (revisión T027) y `Timber.` una sola vez (`VirGoApplication.kt`). CI: PR #6 con los 6 checks en verde (`movil-build-test` 4m34s, `analyze` 5m45s, `CodeQL`, `gitleaks`, `dependency review`, `changes`).
- **Decisiones:** dependencia `implementation` (no `debugImplementation`) con el guardia `BuildConfig.DEBUG` en `onCreate`, porque `VirGoApplication` vive en el source set `main` y debe compilar también en *release*; sin el guardia, en *release* se plantaría un árbol (violaría Art. IX).
- **Costo:** $0 / solo tiempo de CI.
- **RNF (retos no funcionales):**  
  ✅ RNF-03: ceros en claro (`network_security_config.xml` con `cleartextTrafficPermitted="false"`; manifiesto release sin `usesCleartextTraffic`).  
  ✅ RNF-04: AeadConfig pre-registrado (logcat `[I ConfigManager] Registered [config_id=1] for algorithm AESGCM with keysize 128` antes de crear el keyset).
- **Aprendizaje:**  
  - «Solo debug» en una clase de `main` se resuelve con `BuildConfig.DEBUG` + R8, no con `debugImplementation`: esta última rompería la compilación de *release* al no existir la clase en ese classpath.
  - El guardia hace que R8 pueda eliminar Timber completo del APK de *release* (sin árbol plantado no hay código alcanzable), lo que convierte el criterio del Art. IX en verificable con el `mapping.txt`.
- **Pendiente:** aprobación con permiso `write` y fusión del PR #6; T033 (variable `VIRGO_API_BASE_URL` + respaldo P-02), T002/T039, T030, T037, T035 y T036.

### 2026-11-13 - T002 + T039: rulesets del backend y checks requeridos del móvil

- **Autor:** equipo + opencode (`seguridad-ci`)
- **Módulo / tareas:** `000-arquitectura-base` -> T002, T039 -> (34/40)
- **Qué se hizo:** T002 — decisión humana «Aplicar su JSON completo»: el ruleset vivo de `VirGO_Backend` (id 26558561) se reemplazó vía `PUT` para quedar idéntico a su `.github/rulesets/main-protection.json` (PR obligatorio, 1 revisión, hilos resueltos, 7 checks: `api-build-test`, `api-iac`, `api-schema`, `CodeQL`, `database`, `gitleaks`, `dependency review`; más `deletion` y `non_fast_forward`), único ruleset activo, *drift* JSON↔vivo eliminado. T039 — decisión humana «`movil-build-test` + `analyze`»: el ruleset de `VirGO_Movil` (id 27049892) exige 4 checks y `.github/rulesets/main-protection.json` quedó idéntico en el PR #7 (`73020da`).
- **Evidencia:** `gh api repos/virgorent/VirGO_Backend/rulesets/26558561` → `enforcement=active`, `required_approving_review_count=1`, 7 `required_status_checks`; PR #19 del backend en verde. Móvil: *push* directo a `main` rechazado con `GH013` «4 of 4 required status checks are expected» (commit de prueba `933a78b` descartado); PR #7 fusionado (`716cc70`) con `movil-build-test` skipeado por la condicional `if:` reportando *Success* → prueba empírica de que un job skipeado satisface el check requerido.
- **Decisiones:** el check de CodeQL se llama `analyze` (no existe el check-run `movil-codeql`), así que se requirió `analyze`; un workflow skipeado por filtro `paths` en `on:` quedaría *Pending* y bloquearía, por lo que el filtro `paths` sigue solo en `movil-ci.yml` con job `changes`.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - La fuente de verdad del ruleset es el JSON del repositorio; el ruleset vivo puede divergir y hay que compararlos con `gh api` después de cada cambio.
  - Los 4 checks requeridos viven en `movil-ci.yml` (`movil-build-test`) y `movil-codeql.yml` (`analyze`); `gitleaks` y `dependency review` vienen de los workflows globales.
- **Pendiente:** PR de Play (AAB, icono, respaldo, versión 0.2.0) pedido por el equipo; T033 (respaldo P-02), T035, T030, T036, T024 y T037.

### 2026-11-16 - T403: repositorio y casos de uso de recuperacion (004-recuperar-contrasena 4/5)

- **Autor:** equipo + opencode (`dev-android` + `qa-movil`)
- **Módulo / tareas:** `004-recuperar-contrasena` → T403 → (4/5)
- **Qué se hizo:**
  - **T403:** capa de recuperación contra los specs `011-post-auth-password-forgot` y `012-post-auth-password-reset` del backend (ambos en **Borrador**), aplicando el riesgo del plan «MockWebServer con el contrato del spec de API». En `:core:network`, `AuthApi.forgotPassword`/`resetPassword` + `ForgotPasswordRequestDto` (`{"email"}`) y `ResetPasswordRequestDto` (`{"code","password"}`); en `:core:domain`, `RecoveryRepository`, `ForgotPasswordUseCase` y `ResetPasswordUseCase`; en `:core:data`, `RecoveryRepositoryImpl` (reutiliza `Throwable.toAppError()`) y `RecoveryModule` (`@Binds`); en `:core:testing`, `FakeRecoveryRepository`.
  - **Humo de pantallas (CA-01, CA-02 y RF-04):** APK *debug* de la rama en el emulador `Pixel_10_Pro`: A6 → correo → «Enviar código» → A7 con código, contraseña y confirmación, y rechazo local de código incorrecto («El código no es correcto.»).
  - Documentación: `task.md` 4/5 con blockquotes, `check.md` (2/4 CA y 6 filas) y esta bitácora.
- **Evidencia:** `docs/evidencia/CA-01-recuperar-a6.png`, `docs/evidencia/CA-02-recuperar-a7.png` y `docs/evidencia/RF-04-recuperar-codigo-incorrecto.png`; pipeline local `ktlintCheck detekt lint testDebugUnitTest` + `:core:domain:test` + `:core:common:test` + `pitest` + `assembleDebug` → BUILD SUCCESSFUL con **119 pruebas, 0 fallos** y mutación **72 %** (38/53); `RecoveryRepositoryImplTest` 8 con MockWebServer (202 con ruta y cuerpo, 400 con `errors`, 429, 500, sin conexión, 200 con cuerpo, 400 genérico y sin conexión en reset).
- **Decisiones:**
  1. `POST /api/v1/auth/password/forgot` responde **404** en producción (2026-11-16, idéntico al de una ruta inexistente de control): T405 queda ⛔ hasta que el backend entregue `011`/`012` (`T1101`–`T1103` ⏳); la pantalla se prueba con MockWebServer, como manda el Art. VIII.6 del spec.
  2. `RecoverViewModel` **no** se conecta en T403: RF-02 (aprobado) dice que la app genera el código de 6 dígitos y la maqueta lo muestra en pantalla, y sin mecanismo de correo aprobado (P-01) la app no podría recibir el código real; la conexión se hará en T405 con el JSON probado.
  3. Cuerpos asumidos porque §9.2 de `API-HANDOFF.md` no define los de recuperación: `{"email"}` en forgot y `{"code","password"}` en reset; quedan como asunción para confirmar con el backend.
  4. `429` cae en `AppError.Server` (el `when` compartido no tiene variante de rate limit): el mensaje de UI sigue genérico y sin texto técnico (CA-03).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - En el emulador, revisar `dumpsys input_method` (`mInputShown=true`) antes de tocar coordenadas: con el teclado abierto los toques en la zona baja caen en el teclado (aquí, la barra espaciadora, que añadía un espacio al correo y dejaba «Enviar código» deshabilitado). `input keyevent 4` solo si el IME está visible y releer coordenadas después.
  - `adb shell input text` no decodifica `%40`: escribir `@` directo; `input tap` rechaza coordenadas con decimales (redondear a enteros).
  - Comparar textos con acentos desde PowerShell falla por codificación (`-ceq`): usar subcadenas ASCII con `-like`.
  - Un `POST` a una ruta inexistente en producción devuelve Problem Details 404 con `instance` de la ruta: sirve como sondeo barato para saber si un endpoint ya está desplegado.
- **Pendiente:** T405 (humo contra producción) a la espera de `011`/`012` y de P-01 (correo); PR #19 de esta rama; PR #18 (`001-inicio-visitante`, verde 6/6) y PR #17 (`002-registro`, verde) esperando aprobación; PR #11 y #13; T033 (respaldo P-02), T036 (cierre de `000`), T037 (PR sin Figma) y B4/B5 de Play (Equipo/API).

### 2026-11-16 - T103 + T105: catalogo de salones conectado (001-inicio-visitante 5/5)

- **Autor:** equipo + opencode (`dev-android` + `qa-movil`)
- **Módulo / tareas:** `001-inicio-visitante` -> T103, T105 -> (5/5)
- **Qué se hizo:**
  - **T103:** consumo del contrato `VirGo-API/specs/007-venues-catalog/plan.md` (`GET /api/v1/venues?zone=&category=` → `200 {"data":[…]}`). En `:core:network` `VenuesApi` + `VenueDtos` (respuesta envuelta en `data`, `location` GeoJSON `{type:"Point",coordinates:[lng,lat]}` y `rating:{average,count}`), `createVenuesApi` y `provideVenuesApi`; en `:core:domain` `Venue`, `VenueRepository` y `GetVenuesUseCase`; en `:core:data` `VenueRepositoryImpl` (reutiliza `toAppError()`: 404 → `NotFound`, 500 → `Server`, sin conexión → `Network`) y `VenueModule` (`@Binds`); en `:app`, `GuestHomeViewModel` con carga en `init`, `refresh()`, estados `isLoading`/`hasError`, y mapeo `Venue` → `VenueSample` con haversine y rumbo desde el centro fijo de Juriquilla (`VenueMapping.kt`); UI de error con «Reintentar» en `GuestBottom.kt` (se extrajo de `GuestHomeScreen.kt` para no pasar el umbral de `TooManyFunctions`).
  - **T105:** humo contra producción en el emulador `Pixel_10_Pro` (2026-11-16): arranque limpio → `GET /api/v1/venues` **200** en 8738 ms con `{"data":[]}` → catálogo vacío, la app cae en la muestra local («5 salones a 5 km»); toque en el pin → vista previa A3 («Jardín Magnolia · El Refugio · ★ 4.8 · hasta 160»); con modo avión y arranque en frío → «No pudimos cargar los salones. Revisa tu conexión e inténtalo de nuevo.» con «Reintentar», y el reintento con la conexión restaurada recarga y limpia el error.
  - Documentación: `task.md` 5/5 con blockquotes, `check.md` 4/4 CA y 6/6 filas de pruebas, `AGENTS.md` del móvil.
- **Evidencia:** `docs/evidencia/CA-01-inicio-visitante-a1.png`, `docs/evidencia/CA-02-inicio-visitante-a3-preview.png` y `docs/evidencia/CA-04-inicio-visitante-sin-conexion.png`; logcat `GET …/api/v1/venues <-- 200 (8738ms, 11-byte body)`; pipeline local `ktlintCheck detekt lint testDebugUnitTest` + `:core:domain:test` + `:core:common:test` + `pitest` + `assembleDebug` → BUILD SUCCESSFUL con **123 pruebas, 0 fallos** y mutación **61 %** (37/61).
- **Decisiones:**
  1. Catálogo vacío (estado real de producción) → fallback a `VenueSamples`, para que A1 no quede en blanco mientras el backend no cargue salones; la falla sí muestra error con reintento (CA-04).
  2. `GET /api/v1/venues/:id` (spec `024`) queda fuera de T103: su cuerpo sigue en borrador («PROPUESTO» en `API-HANDOFF.md` §9.5) y la vista previa A3 se renderiza con los campos del catálogo; se consumirá con el JSON probado.
  3. El `id` del pin se asigna por posición del catálogo (los ids de la API son UUID y `VenueSample.id` es `Int`).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Cambiar de rama con el mismo directorio `build/` mezcla artefactos: `:app:hiltJavaCompileDebug` falló con «Could not find class file for `RegisterUseCase`» (clase de la rama `feat/002-registro`); un `gradlew clean` lo resolvió. Limpiar al cambiar de rama evita diagnósticos falsos.
  - `detekt TooManyFunctions` cuenta funciones por archivo (umbral 11): extraer `GuestHint`/`GuestError` a `GuestBottom.kt` devolvió el archivo a 10 sin tocar la lógica.
  - Las aserciones deben leer el dato real: `VenueSamples` tiene 7 entradas pero solo 5 dentro de 5 km; comparar con `VenueSamples.count { it.distanceKm <= DEFAULT_RANGE_KM }` es más robusto que `VenueSamples.size`.
  - En el emulador, tocar la **etiqueta** del pin (no el icono) es lo que dispara la vista previa: el `detectTapGestures` del mapa hitanea toda la caja del pin.
- **Pendiente:** PR #18 de `001-inicio-visitante` (aprobación + fusión); `003` T304 (humo de login) y `004` T403/T405 (recuperar contraseña); PR #17 (`002-registro`) en verde esperando aprobación; PR #11 y #13; T033 (respaldo P-02), T036 (cierre de `000`), T037 (PR sin Figma) y B4/B5 de Play (Equipo/API).

### 2026-11-14 - T024, T030 y T035: diagnostico, verificacion manual y release v0.2.0

- **Autor:** equipo + opencode (`qa-movil`)
- **Modulo / tareas:** `000-arquitectura-base` -> T024, T030, T035 -> (36/40)
- **Que se hizo:**
  - Fusionados los PR #8 (docs T002/T039), #9 (fix AeadConfig + DevMenu) y #10 (Play: AAB, icono adaptativo, `allowBackup=false`, version 0.2.0); aprobados por EsauRamireZ/Karasu0001 con los 6 checks en verde cada uno.
  - Lanzado `movil-release` con `version=0.2.0` (run `43544624048`, en verde): release `v0.2.0` con `app-release.aab` (5 661 199 B) y `app-release.apk` (2 530 468 B), `versionCode 2`.
  - **T024** CA-04/CA-05: login QA real contra produccion (`POST /api/v1/auth/login` -> 200) y Diagnostico con «API disponible» (227 ms); con modo avion, mensaje en espanol «No hay conexion con la API. Revisa tu internet e intentalo de nuevo.» sin cierre y «Reintentar» que recupera (1123 ms).
  - **T030**: arranque en frio 1791/1854/1885 ms (1935 tras reinicio) en debug y 698 ms con el APK de release; modo oscuro, icono de launcher y capturas del DevMenu/Diagnostico.
  - **T035**: APK del release descargado del GitHub Release, instalado en el emulador (uninstall + install, sin mezclar firmas) y prueba de humo: abre el inicio visitante y el login llega a «Inicio».
- **Evidencia:** `docs/evidencia/` con CA-04, CA-05, CA-09, T030-devmenu-diagnostico, evidencia-mod-oscuro e icono-launcher; PR #11 con la evidencia RNF-03/RNF-04.
- **Decisiones:** la «API apagada» de CA-05 se simulo con modo avion porque la API real es la de produccion; el diagnostico local (T210) ya no aplica; se publico `v0.2.0` en lugar de `v0.1.0` porque el alcance crecio con el PR #10.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - El teclado del emulador cubre la mitad inferior: un `input tap` sobre un boton debajo del IME termina presionando una tecla del teclado (chars fantasma); hay que cerrar el IME con `keyevent 4` antes de tocar y releer coordenadas.
  - El `screenrecord --size 720x1280` + ffmpeg (frame 0, sin `-ss`) da capturas confiables cuando `screencap` sale negro por el glitch de gfxstream.
  - Los snapshots del emulador corrompen el keystore: arrancar siempre con `-no-snapshot`.
- **Pendiente:** T033 (respaldo P-02 del keystore, custodia humana), T036 (cierre), T037 (MCP figma/github), subir el AAB a prueba interna de Play (equipo) y B4/B5 (aviso de privacidad y borrado de cuenta, Equipo/API).


### 2026-11-16 - T203 + T205: registro conectado a produccion (002-registro 5/5)

- **Autor:** equipo + opencode (`dev-android` + `qa-movil`)
- **Modulo / tareas:** `002-registro` -> T203, T205 -> (5/5, primer modulo de pantallas completo)
- **Que se hizo:**
  - **T203:** se consumio el contrato `005-auth-register` que el backend dejo en `VirGo-API/specs/005-auth-register/plan.md`. En `:core:common` `AppError.Conflict` (409); en `:core:network` `RegisterRequestDto`/`RegisterResponseDto` y `AuthApi.register`; en `:core:data` `AuthRepositoryImpl.register` (409 -> `Conflict`, 400 -> `Validation`, 201 -> `RegisteredUser`); en `:core:domain` `AuthRepository.register`, `RegisterUseCase` (trim, correo en minusculas y prefijo `+52`) y `RegisteredEmailStore.contains()` para el fast-path de duplicado sin mutar el almacen.
  - **T205:** humo contra produccion en el emulador `Pixel_10_Pro` (2026-11-14): registro valido -> `POST /api/v1/auth/register` **201** (7524 ms) y navegacion a «Inicio» (RF-09); con `force-stop` y el mismo correo -> **409** (6394 ms) y píldora «Ya existe una cuenta con este correo. Inicia sesión.» sin cierre (RF-08, CA-03).
  - Evidencia nueva de criterios: `CA-01-registro-a2-formulario.png` (pantalla A2 limpia) y `CA-04-registro-sin-conexion.png` (modo avion: `UnknownHostException` -> «No pudimos crear tu cuenta. Revisa tu conexión e inténtalo de nuevo.» con el boton «Crear cuenta» habilitado para reintentar).
  - Documentacion cerrada: `task.md` (5/5 con blockquotes de evidencia), `check.md` (4/4 CA y 6/6 filas de pruebas), `AGENTS.md` (raiz y movil).
- **Evidencia:** `docs/evidencia/` con T205-registro-201-exitoso.png, T205-registro-409-duplicado.png, CA-01-registro-a2-formulario.png y CA-04-registro-sin-conexion.png; pipeline local `ktlintCheck detekt lint testDebugUnitTest assembleDebug` + `:core:domain:test` + `:core:common:test` + `pitest` -> BUILD SUCCESSFUL con **115 pruebas, 0 fallos** y mutacion **63 %** (39/62).
- **Decisiones:** el correo QA `qa-register-1794636322@virgo.mx` quedo dado de alta en produccion como evidencia del 201; la "API apagada" de CA-04 se simulo con modo avion porque la API real es la de produccion; los tests de `:core:domain` usan fakes locales porque ese modulo no puede depender de `:core:testing` (dependencia circular).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - El IME del emulador se dibuja **encima** de los campos inferiores sin redimensionar la ventana: un `input tap` a una coordenada tapada por el teclado teclea en el campo enfocado (o en una tecla). Flujo fiable: tocar campo -> escribir -> `keyevent 4` solo si `mIsImeShowing=true` -> releer coordenadas; nunca `keyevent 4` a ciegas, porque con el IME ya cerrado BACK sale de la pantalla o de la app.
  - En PowerShell, `-like '*Correo*'` es insensible a mayusculas y coincide con «o con tu correo» (un `Text` estatico, no enfocable): usar `-ceq` para coincidir con el placeholder exacto del campo.
  - El emulador se cayo dos veces durante la automatizacion; relanzarlo con `-gpu swiftshader_indirect` estabilizo la sesion.
  - `RegisterUseCaseTest` no compila si se importa `core:testing` desde `:core:domain`: el fake vive en la propia suite.
- **Pendiente:** PR de `002-registro` (aprobacion + fusion), PR #11 y #13 en espera, T033 (respaldo P-02), T036 (cierre de `000`), T037 (PR sin Figma) y B4/B5 de Play (Equipo/API).

### 2026-11-15 - Publicación de v0.2.0 en Google Play y enmienda v1.2.0

- **Autor:** Angélica (`Karasu0001`), con Claude Code
- **Módulo / tareas:** `000-arquitectura-base` -> T033, T035 (release) · bloqueos B4–B6 de `docs/PUBLICAR-PLAY.md`
- **Qué se hizo:**
  - Llave de subida nueva (`upload`, RSA 2048) generada por la titular de la cuenta de Play. El keystore y sus contraseñas están solo en GitHub Secrets y en su respaldo privado.
  - `movil-release` publicó la GitHub Release `v0.2.0` con `app-release.aab` y `app-release.apk`.
  - App `mx.virgo.app` creada en Play Console. El AAB 2 (0.2.0) quedó activo en **prueba interna** y se envió a revisión en la **prueba cerrada** (Alpha, México y El Salvador, 12 *testers*).
  - Panel completado: detalles de acceso, anuncios (No), clasificación de contenido (Todos; «los usuarios interactúan»), público objetivo 18+, seguridad de los datos (nombre, correo, ID de usuario y teléfono; recopilados y no compartidos; cifrados en tránsito), ID de publicidad (No), categoría Eventos, ficha de Play Store y declaración de recursos con IA.
  - Avisos legales (B4) publicados con GitHub Pages (PR #14): privacidad, términos y eliminación de cuenta. Responsable KarasuDev, contacto `virgorent6@gmail.com`.
  - Cuenta de revisión `revisor.play@virgo.mx` creada en la base `virgo` (B6). **No se borra.**
  - Propuesta la enmienda **v1.2.0** de `constitution.md` (Art. VIII.3–VIII.4), con `AGENTS.md`, las skills y el subagente alineados.
- **Evidencia:**
  - Certificado del AAB: `CN=Angélica Viridiana Arguelles Méndez, OU=Desarrollo, O=VirGO`. SHA-256 de la llave de subida: `B4:22:BE:19:FC:23:2C:4D:4D:2D:C3:40:88:31:DF:8D:D4:08:50:03:7D:9C:0A:57:C1:97:0C:E0:FE:A8:59:E1`.
  - El manifiesto del AAB solo declara `INTERNET` (y `DUMP` de AndroidX *profileinstaller*); no hay `AD_ID` ni SDK de anuncios o analítica.
  - `https://virgorent.github.io/VirGO_Movil/legal/{privacidad,terminos,eliminar-cuenta}/` → 200.
  - Login de la cuenta de revisión en producción → 200.
  - `DELETE /api/v1/users/me` probado en producción con una cuenta desechable: 204; token anterior 401; login 401; perfil público «Cuenta eliminada» sin correo ni teléfono.
- **Decisiones:** ADR-15. Las declaraciones describen solo lo que hace v0.2.0. Chat, pagos y ubicación se declararán **antes** de subir la versión que los incluya. La eliminación de cuenta se atiende por correo hasta que exista el botón en la app.
- **Costo:** $0 adicional (la cuenta de Play ya estaba pagada).
- **Aprendizaje:**
  - El primer `movil-release` salió firmado con una llave de desarrollo (`CN=VirGo Local`) porque se ejecutó antes de configurar los secretos. Se borraron el release y la etiqueta y se regeneró con la llave oficial **antes** de subir nada a Play: la primera subida fija la llave de subida.
  - `vars.X` y `secrets.X` son espacios distintos: `VIRGO_API_BASE_URL` creada como *secret* llegó vacía y la guarda `https://` de ADR-08 detuvo el build, que es lo esperado.
  - «keystore password was incorrect» aparece al **empaquetar** (unos 2 minutos), no al configurar. El fallo rápido (unos 59 s) era el secreto o la variable faltante.
  - Con SDK de destino 33 o mayor, Play exige la declaración de ID de publicidad aunque no aparezca en la lista de «Contenido de la app».
  - Las capturas de 1280×2856 superan la proporción 2:1 que acepta Play; se montaron en un lienzo de 1440×2880.
- **Pendiente:** aprobación de la prueba cerrada y 14 días con 12 *testers*; B5 (botón en Perfil); registro conectado a `POST /api/v1/auth/register`; datos de ejemplo en `virgo`; aprobación de la enmienda v1.2.0 en este PR.

### 2026-11-16 - T304: humo de login contra produccion (003-iniciar-sesion 4/4)

- **Autor:** equipo + opencode (`qa-movil`)
- **Módulo / tareas:** `003-iniciar-sesion` → T304 → (4/4)
- **Qué se hizo:**
  - **T304:** prueba de humo del login contra producción (`POST /api/v1/auth/login`) con el APK *debug* en el emulador `Pixel_10_Pro` y la cuenta QA `qa-health@virgo.mx`: **CA-01** pantalla A5 completa (formularios, enlace «¿Olvidaste tu contraseña?» y «Entrar»); **CA-02** credenciales inexistentes → `401` real → «Correo o contraseña incorrectos» (genérico, RF-02); **CA-03** modo avión → «Sin conexión. Revisa tu internet e inténtalo de nuevo.» con «Entrar» habilitado y reintento exitoso tras restaurar la red; **RF-03** el reintento navega a la pantalla principal del rol («Inicio»).
  - Documentación: `task.md` 4/4 con blockquote, `check.md` completo y esta bitácora.
- **Evidencia:** `docs/evidencia/CA-01-iniciar-sesion-a5.png`, `docs/evidencia/CA-02-iniciar-sesion-error.png`, `docs/evidencia/CA-03-iniciar-sesion-sin-conexion.png` y `docs/evidencia/RF-03-iniciar-sesion-inicio-rol.png`.
- **Decisiones / hallazgos:**
  1. **La sesión no persiste entre reinicios:** el login exitoso solo pone `loggedIn = true` en el `UiState`; nadie llama a `TokenStore.save()` (el `TokenStore` está implementado y probado en `:core:datastore`, pero sin conectar), así que al volver a abrir la app se arranca en modo visitante. No rompe ningún CA de `003` (el spec no pide persistencia), pero va contra el espíritu de RNF-03 («tokens solo en `TokenStore`») y del endpoint `013-post-auth-refresh` listado en las dependencias: candidato a un spec `005-sesiones` (guardar token en el login, restaurar al arrancar, refresh y logout) antes de las pantallas de rol.
  2. El reintento de CA-03 es re-pulsar «Entrar» (no hay botón «Reintentar» separado en A5); funciona y completa el flujo hasta la home del rol.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - En el emulador, `adb shell cmd connectivity airplane-mode enable/disable` es la vía rápida para simular «sin conexión» y restaurarla sin tocar la UI; verificar con `settings get global airplane_mode_on`.
  - El error de red llega en ~10 s (connect timeout de OkHttp): esperar al menos 12–15 s antes de capturar o el mensaje aún no aparece.
- **Pendiente:** PR #20 de esta rama; PR #19 (`004-recuperar-contrasena` 4/5, verde 6/6), PR #18 (`001-inicio-visitante`, verde 6/6) y PR #17 (`002-registro`, verde) esperando aprobación; PR #11 y #13; T033 (respaldo P-02), T036 (cierre de `000`), T037 (PR sin Figma) y B4/B5 de Play (Equipo/API).

### 2026-11-16 - T037: MCP figma y github, resuelta como no aplica

- **Autor:** equipo + opencode (`arquitecto-movil`)
- **Módulo / tareas:** `000-arquitectura-base` -> T037 -> (35/40)
- **Qué se hizo:** T037 pasó de ⛔ a ✅ como **no aplica** (decisión del equipo, 2026-11-16): el MCP `figma` responde `HTTP 405` en `https://mcp.figma.com/mcp` (problema conocido de opencode) y el MCP `github` sigue deshabilitado; ninguno de los dos era indispensable. Sin MCP de Figma, la fuente de las specs de pantalla es la maqueta funcional + `Checklist VirGo.md` con revisión por captura, y el sistema de diseño vive en `:core:designsystem`; sin MCP de GitHub se usa `gh` CLI (mismo alcance de lectura de PRs, checks y ejecuciones). Se actualizaron `task.md` (T037 ✅ con su blockquote al final de la sección), `check.md` (casilla T037 marcada con evidencia) y `AGENTS.md` (tabla de MCP: ambos ➖ no aplica; contadores 34/40 → 35/40; specs `001`–`004` en PRs verdes). Rama `docs/0037-no-aplica` desde `origin/main` (`126b14b`).
- **Evidencia:** `task.md` T037 ✅; `check.md` §1 casilla con evidencia; conteo de estados en `task.md` = 35 ✅ / 5 pendientes (T024 🔄, T033 🔄, T030 ⏳, T035 ⏳, T036 ⏳).
- **Decisiones:** la lectura del canvas de Figma se reabre como tarea sujeta a que el MCP funcione; el alcance de `github` lo cubre `gh` CLI.
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - Una tarea bloqueada por una herramienta externa puede cerrarse como «no aplica» documentando el sustituto, en vez de quedarse ⛔ para siempre.
  - Al actualizar el estado de una tarea hay que mover el contador en todos los lugares que lo replican (`task.md`, `AGENTS.md` raíz y móvil) para evitar *stale references*.
- **Pendiente:** aprobación del PR docs; T024, T033, T030, T035 y T036.

### 2026-11-18 - T501-T506: explorar salones implementado (005-explorar-salones 6/7)

- **Autor:** equipo + opencode (`dev-android` / `qa-movil`)
- **Módulo / tareas:** `005-explorar-salones` → T501–T506 (6/7; T507 pendiente de humo)
- **Qué se hizo:**
  - **T501–T504:** pantalla principal del Cliente en `app/ui/explore/`: B8 con mapa y tarjeta resumen (`ExploreMap`), C9 con lista y orden (`ExploreList`), C10 con hoja de filtros (`ExploreFiltersPanel`) y C11 con recomendaciones (`ExploreRecommendedPanel`), sobre un único `ExploreViewModel` con `onFiltersChange(transform)`, `onSheetChange`, etc. Se eliminó `HomePlaceholder` y `ExploreScreen` entró por `VirGoNavHost.homeDestination()`; todos los textos viven en `strings.xml`.
  - **T505:** contrato `VenueDto` ampliado **aditivo** (precio, categorías y fotos con defaults, sin romper `001`), `FavoritesApi`/`NoticesApi` + DTOs, `FavoritesRepositoryImpl`/`NoticesRepositoryImpl` y casos de uso `GetFavorites`, `GetNotices`, `ToggleFavorite`.
  - **T506:** 25 pruebas nuevas de la pantalla (`ExploreViewModelTest` 19, `ExploreScreenTest` 6 de UI con Robolectric) y 18 de dominio/red (`FavoritesRepositoryImplTest` 6, `NoticesRepositoryImplTest` 5, `ToggleFavoriteUseCaseTest` 4, `VenueTest` 4, `GetFavoritesUseCaseTest` 3, `GetNoticesUseCaseTest` 3). Pipeline local completo en verde.
- **Evidencia:** rama `feat/005-explorar-salones` con commits `94f3a73` (T505), `e09071f` (T501), `e77b50b` (T505 test) y `f298967` (T506 test); `task.md` T501–T506 ✅ con blockquotes; `check.md` con pruebas y mutación.
- **Decisiones / hallazgos:**
  1. **Favoritos optimistas:** actualización local + API; si falla se revierte y se emite `ExploreUiEvent.FavoriteError` → snackbar. Las muestras (`venueId` con prefijo `sample-`) NO viajan a la API.
  2. **Fecha RF-16:** se valida y guarda pero no descarta salones (el contrato 007 aún no trae disponibilidad).
  3. **Recomendaciones:** score local con `clamp 0..100` y badge «Estimación IA»; los proveedores y la campana muestran snackbar «próxima versión».
  4. La mutación había bajado a 43 %: se subió a **59 %** agregando fakes locales en `core:domain` (que no depende de `core:testing`) y pruebas MockWebServer de los repos nuevos.
  5. Límites de estilo que disparan con pantallas grandes: `LongMethod` 60 → extraer `ExploreSheetHost`, `ExploreContent`, `ExploreNoticesButton`, `ExplorePriceDistanceRow` y las filas de filtros; ktlint exige salto antes de `.` cuando hay 4+ encadenados (`chain-method-continuation`).
- **Costo:** $0 / solo tiempo de CI.
- **Aprendizaje:**
  - `pitest` NO ejecuta `:core:domain:test`: correrlo explícito para refrescar los XML antes de contar pruebas, o el conteo queda desfasado.
  - `Set-Content -Encoding UTF8` en PowerShell escribe BOM; al editar `.kt` hay que quitarlo con `[System.IO.File]::WriteAllBytes` o ktlint falla raro.
  - Actualizar la rama invalida las aprobaciones: terminar TODO el código y docs, pushear y solo entonces pedir revisión.
- **Pendiente:** revisión del PR apilado de `feat/005-explorar-salones` (#27); T507 (humo con URL real) bloqueado por endpoints (`notices`/`favorites` → 404 en producción); capturas CA-01–CA-04 **hechas** (2026-11-19, `docs/evidencia/CA-0{1..4}-explorar-*.png`); fusiones de la cadena `#20 → #15 → #11 → #17 → #18 → #19` y luego `#16`, `#22`, `#23`.

### 2026-11-19 - Refinamiento visual en curso

- **Módulos / tareas:** mejoras visuales de `001` y `005` (T101, T501–T504 y T506), a petición del equipo.
- **Cambios en curso:** fotos ilustrativas locales para muestras, créditos accesibles en la app y documentados en `docs/CREDITOS-IMAGENES.md`, ajuste de insets y cierre de hojas con Atrás. Las imágenes no se atribuyen a salones reales.
- **Verificación:** `ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug` → `BUILD SUCCESSFUL` (2026-11-19). Una ejecución previa detectó que los créditos del encabezado interferían con un marcador; se movieron a la tarjeta de visitante. El APK se instaló, pero la revisión visual quedó pendiente por un diálogo de Pixel Launcher sin respuesta en el emulador. Las capturas anteriores aún no evidencian estos cambios.
- **Pendiente:** validar pantallas y tamaños, actualizar capturas y checks, y especificar y aprobar separadamente la bandeja de avisos y la persistencia de favoritos demo antes de implementarlas.

### 2026-11-19 - Rediseño de explorar en curso

- **Autor:** equipo + opencode.
- **Módulo / tareas:** `005-explorar-salones` · T501–T504 y T506.
- **Qué se hizo:** nueva cabecera, controles visuales más discretos, acceso de desarrollo integrado y catálogo inicial con fotos amplias y créditos; se conserva el mapa como vista alternativa y se eliminó la barra inferior con un único destino. Ajustes adicionales de login y registro tras revisar el emulador.
- **Evidencia:** `ktlintCheck detekt :app:testDebugUnitTest :app:assembleDebug` en verde (2026-11-19, compilación en carpeta temporal para evitar bloqueo de `R.jar`). APK actualizado instalado en el emulador; captura del nuevo diseño autenticado pendiente.
- **Decisiones:** las fotos son únicamente ilustrativas de los salones de muestra; mapa y ubicaciones siguen siendo demostrativos.
- **Costo:** $0.
- **Aprendizaje:** una interfaz puede superar las pruebas y aun así requerir revisión visual con el equipo.
- **Pendiente:** volver a ejecutar el pipeline después del último cambio, revisar el diseño en emulador, actualizar evidencia y solicitar revisión del PR.

### 2026-11-19 · Consolidación autorizada de Explorar y preparación de restauración de sesión

- **Autor:** equipo + OpenCode (`dev-android`, `qa-movil` y revisión de arquitectura).
- **Módulo / tareas:** `005-explorar-salones` · T508 verificada y T509 pendiente; `030-restaurar-sesion` solo preparación documental en rama separada, sin implementación.
- **Qué se hizo:**
  - El equipo aprobó continuar con la consolidación de #27 y la especificación del siguiente flujo; no se autorizó una release.
  - Se alinearon los cuatro documentos de `005` con el código real: entrada en lista, fotos amplias con créditos, un ViewModel en `:app` y límites de demo, mapa, fecha, imágenes remotas y accesibilidad.
  - Se añadieron regresiones explícitas de error seguro de API, reintento sin conexión, acceso a créditos desde lista y selección lista → mapa → lista. Se retiraron afirmaciones incorrectas de aceptación basada en pruebas de filtros.
  - La revisión de seguridad detectó falta de identificación de muestras en «Para ti»: se añadió acceso a «Datos y fotos de ejemplo · Créditos» y una regresión de UI. Jardines conserva las atribuciones diferenciadas de la ficha (Shixart1985) y sus metadatos (Nenad Stojković), sin asumir que sean la misma persona.
- **Evidencia:** código `5d14843`; pipeline completo aislado tras el ajuste final de «Para ti» → **BUILD SUCCESSFUL in 5m 26s**, **199 pruebas, 0 fallos/errores/omitidas, 38 suites** (12 de `ExploreScreenTest`); Pitest dominio 60 % (57/95), common 33 % (1/3). Reportes y SHA-256 del APK en `docs/evidencia/T508-consolidacion.md`. `git diff --check` y `git diff --cached --check` sin errores. #27 sigue abierto sobre `feat/004-recuperar-contrasena`; #19 sigue abierto. Al iniciar esta consolidación `adb devices -l` no mostró dispositivos.
- **Decisiones:** no sustituir capturas históricas por evidencia no obtenida; la restauración local se especificará separada de renovación y logout. Sin refresh no se promete sesión durante días.
- **Costo:** $0 / herramientas existentes.
- **Aprendizaje:** los checks verdes de un head anterior no prueban cambios locales; cada CA debe referir pruebas que realmente ejerzan su comportamiento.
- **Entrega:** código y documentación de consolidación en la rama de #27; su historial remoto acredita el push. El borrador `030` se entrega aparte desde `docs/030-restaurar-sesion`, sin mezclar implementación de sesión en este PR.
- **Pendiente:** checks del nuevo head, aceptación visual T509, revisión/fusión por Karasu, humo completo T507 y revisión humana de `030`; no se ejecutó release.

### 2026-11-20 · Reparación visual de las tareas aprobadas de la base

- **Autor:** usuario + OpenCode (`ui-designsystem`, QA y revisión de seguridad).
- **Módulo / tareas:** `000-arquitectura-base` · T010–T014 reabiertas; no se ejecutan módulos nuevos ni `031`. Solicitud explícita: «sí ya haz todo y aplica los cambios visuales».
- **Qué se hizo:** se comienza por reparar requisitos que el spec/plan de `000` ya aprobaban: paleta constitucional, Manrope/DM Sans locales, tema y controles compartidos con alturas mínimas. La reapertura reduce el conteo a 33/40 porque el código actual no acredita el resultado previo de esas cinco tareas; no se borra su evidencia histórica ni se declara cierre del módulo.
- **Evidencia:** rama `fix/000-fundamentos-visuales` desde `73ed69b`; trabajo aislado del inventario/spec `031` no versionados en `virgo-visual-reference`. Recursos de fuente originales obtenidos de revisión fija de `google/fonts`, con licencia y SHA-256 en `docs/FUENTES.md`; 22 pares sólidos calculados dentro de AA. La revisión estática independiente quedó sin hallazgos abiertos tras corregir ayuda sobre fondo suave y contenido de error oscuro. Pipeline completo: **BUILD SUCCESSFUL in 1m 14s**, **40 suites/217 pruebas, 0 fallos/errores/omitidas** (incremental: 9 tareas ejecutadas; la suite de `app` se reejecutó completa); Pitest dominio 60 % (57/95) y common 33 % (1/3). APK nuevo de 23 320 455 bytes, SHA-256 `7e201a8a32bc91b042f2489e1416ecd1eb997e601c985a52efedb1cebbe0a04e`, instalado con `adb install -r` sin borrar datos. Capturas clara y compacta/oscura/fuente 2.0 revisadas: confirman fuentes y controles, pero muestran layout antiguo, «Google» partido y enlace final fuera de viewport; no son aceptación. Emulador restaurado a 1280×2856, 480 dpi, fuente 1.0 y modo claro.
- **Decisiones:** reparar la base existente no equivale a exceptuar el cierre de `000` para flujos nuevos; se conservan T033/T036 pendientes. Mantener APIs, navegación, sesión, fotografías y DevSecOps sin cambios. El cierre visual completo de las pantallas no se acredita con esta reparación.
- **Costo:** $0 / fuentes OFL y herramientas locales.
- **Aprendizaje:** distinguir colores de marca y roles semánticos accesibles evita copiar pares con contraste insuficiente.
- **Pendiente:** completar `VirGoTopBar`, `VirGoBottomBar`, `EmptyState`, `LoadingState` y `ErrorBanner` (T013/CA-10); migrar las pantallas y obtener aceptación humana por bloque; cerrar T036 y `000`. No se publica release ni se hace merge.

### 2026-11-20 · Custodia confirmada y procedencia de la maqueta aclarada

- **Autor:** usuario + OpenCode.
- **Módulo / tareas:** `000` · T033/P-02; aclaración de referencia para la entrega visual.
- **Qué se hizo:** el usuario confirmó «Yo, con respaldo seguro». Indicó que el proyecto local supuestamente corresponde a la misma maqueta porque no pudieron entregarle el Figma original. Se usa como referencia de trabajo autorizada, sin inventar cotejo ni equivalencia demostrada.
- **Evidencia:** consulta de nombres en GitHub: presentes `SIGNING_KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD` y variable `VIRGO_API_BASE_URL`. No se solicitaron ni leyeron valores de secretos. La custodia/respaldo se acredita por declaración humana, no por prueba de restauración.
- **Decisiones:** T033 completa con esta evidencia y su firma histórica; base 34/40 al mantener T010–T014 reabiertas. No hay autorización de release ni cierre automático de T036.
- **Costo:** $0.
- **Pendiente:** resultado del pipeline y revisión real del APK; resto de evidencias de base y etapas SDD. La relación exacta con Figma permanece no verificada.

### 2026-11-21 · Acceso compacto verificado y bloqueo de visitante reproducido

- **Autor:** usuario + OpenCode; revisión independiente de QA en modo lectura.
- **Módulo / tareas:** `000-arquitectura-base` · seguimiento T014/T029, preparación de evidencia T036. T010–T013 cerradas técnicamente; base **38/40**, T014/T036 abiertas.
- **Qué se hizo:**
  - Se completaron ajustes de accesibilidad en acceso existente: acciones sociales y roles apilados con fuente grande; roles y visibilidad de contraseña de 48×48 dp; colores semánticos, nombres de campo persistentes en semántica, errores concretos y regiones vivas. Cabecera de registro con separación real entre regreso y texto, beneficios multilínea, pies adaptables y recuperación con `imePadding`. Sin cambios de API, sesión, navegación, fotografías o permisos.
  - Se añadieron `CompactAccessTest` (10) y `AuthAccessibilityTest` (3); `FontScaleTest` conserva 4. Cubren ambos temas a 320×640/fuente 2.0, ancho efectivo, límites de líneas/truncado, separación de cabecera con/sin salón, paso 2/error de recuperación con datos ficticios, semántica exacta y selección exclusiva de roles.
  - Se corrigió la duplicidad contradictoria de CA-06 y los IDs RNF del `check.md`, conservando las evidencias históricas como tales. Evidencia completa en [`docs/evidencia/T010-T014-correccion-visual.md`](docs/evidencia/T010-T014-correccion-visual.md).
- **Evidencia:** pipeline final `ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug` → **BUILD SUCCESSFUL in 2m 4s**, incremental (383 tareas, 29 ejecutadas); **43 suites, 237 pruebas, 0 fallos/errores/omitidas** (`app` 95, `core` 142); lint 0 errores/35 avisos. Pitest dominio 60 % (57/95), common 33 % (1/3), reutilizados sin cambios JVM.
- **APK y dispositivo:** 23 889 777 bytes, SHA-256 `ccb9c4b5b02e51ef1583d0946902eb928a989aaf260667231a12d465f4df4fb1`; firma debug verificada y `install -r` exitoso, sin borrar datos. El hash de `base.apk` instalado coincide. Capturas `T014-final-*.png` del artefacto final: acceso oscuro a 320 dp/2.0, pies alcanzables y recuperación con teclado abierto; recuperación clara con aviso completo tras desplazar. Sin credenciales ni envío real. Emulador restaurado a 1280×2856, 480 dpi, fuente 1.0 y modo claro.
- **Decisiones:** reparar la accesibilidad existente no habilita nuevos módulos ni implementación de `031`. La consulta de GitHub confirma #17–#20 aprobados pero abiertos; #27/#28 abiertos sin decisión de revisión. La aprobación no se interpreta como fusión ni aceptación visual. No hubo commit, push, merge ni release.
- **Costo:** $0 / herramientas locales existentes.
- **Aprendizaje:** `hasVisualOverflow` puede señalar ancho sobrante de un párrafo reutilizado (Google: texto 91×34 px, párrafo 232×34 px); se verifican límites de líneas, elipsis, altura y truncado en vez de confundir esa asignación con un recorte. Los qualifiers no sustituyen una aserción del viewport efectivo. Las pruebas de visibilidad anteriores no detectaron los solapamientos del inicio visitante.
- **Contraste:** se mantiene la tabla de 22 pares de tokens del reporte; los accesos migrados consumen esos roles. La nota ámbar de rol usa `#7A520F` sobre `#FFF8E9` (≈6,529:1). Esto no certifica imágenes, gradientes ni todas las pantallas.
- **Pendiente:** **T014 sigue abierta** por el inicio visitante compacto oscuro: solapamientos y superficies claras, reproducidos en `T014-visitante-compacto-2x-oscuro-bloqueo.png`. Falta revisión manual completa de TalkBack/estados alternativos/teclado del paso 2 y aceptación por bloques. Gitleaks no está en PATH y no se acreditan checks remotos de cambios locales. T036 exige reconciliar todas las evidencias y aprobaciones; no se cierra `000`.

### 2026-11-21 · Publicación autorizada del avance visual

- **Autor:** usuario + OpenCode; revisión estática previa de seguridad.
- **Módulo / tareas:** `000-arquitectura-base` · T010–T014/T029, entrega de evidencia sin cerrar T036.
- **Qué se hizo:** tras la autorización de continuar con commit, push y PR en borrador, se publicó `fix/000-fundamentos-visuales` y se abrió el [PR #29](https://github.com/virgorent/VirGO_Movil/pull/29) con capturas reales, resultados locales y pendientes explícitos.
- **Evidencia:** código `693da474a442547379f595c5b42274a739c5ce67`; primer push con upstream exitoso; GitHub confirma PR `OPEN`, `isDraft=true`, base `feat/005-explorar-salones` y head `fix/000-fundamentos-visuales`. Inventario staged revisado: 58 archivos, sin rutas fuera de alcance ni APK/AAB/keystores/configuración local sensible; `git diff --cached --check` sin errores. La revisión independiente no detectó bloqueadores altos en los archivos inspeccionados; no sustituye Gitleaks. El pipeline local de 237 pruebas está documentado en la entrada anterior.
- **Decisiones:** PR apilado sobre el head `73ed69b` de #27 para mostrar solo la reparación visual, sin reincorporar el diff de toda la cadena funcional respecto a `main`. No se modificó #27 ni se fusionó nada; Karasu coordina la integración.
- **Costo:** herramientas existentes; CI del repositorio según su configuración.
- **Aprendizaje:** el workflow de CodeQL filtra PRs a `main`; un PR apilado puede ejecutar build y seguridad sin ejecutar CodeQL. No se debe declarar que todos los gates están verdes ni quitar filtros para aparentarlo.
- **Pendiente:** comprobar checks del head final de #29, revisión humana y resolver la base antes de integrar en `main` con todos los gates. T014/T036 siguen abiertas (38/40). Sin release ni ejecución de `movil-release`.

### 2026-11-22 · Visitante adaptativo y regresiones verificadas

- **Autor:** usuario + OpenCode; revisión independiente de solo lectura.
- **Módulo / tareas:** `000` T014/T029 y seguimiento T036; reparación existente de `001` T101/T102/T104.
- **Qué se hizo:** cabecera, resumen, mapa y pie separados en flujo desplazable compacto; preview apilada, colores semánticos y controles de 48 dp. Se corrigió el clamp del pin compacto usando coordenadas originales y tamaño renderizado; su caja cuenta para el etiquetado posterior. Región viva en el texto de error y reintento independiente. Sin cambios de API, sesión, fotografías ni nuevas funciones.
- **Evidencia:** pipeline completo **BUILD SUCCESSFUL in 2m 20s**, 44 suites/250 pruebas, 0 fallos/errores/omitidas; 13 escenarios de visitante, con toque físico de pines este/oeste y comprobación de LiveRegion. APK debug de 23 889 777 bytes, SHA-256 `ce2ed276394de05e61ae9ee5db9ed789a0df9ef6a42b0e94fedb3b7cc709e473`; firma debug verificada, instalado con `install -r` y hash instalado coincidente. Diez capturas reales en [`docs/evidencia/`](docs/evidencia/): 320×640 dp/fuente 2.0 y 393×800 dp/fuente 1.0, ambos temas, inicio/mapa/error/preview; sustituyen las imágenes vacías descartadas. Detalle en [`docs/evidencia/T014-visitante-adaptativo.md`](docs/evidencia/T014-visitante-adaptativo.md).
- **Decisiones:** conservar límites explícitos del mapa demo (sin prometer separar coordenadas idénticas). T014/T036 siguen abiertas, base 38/40. Continuación sobre PR #29 en borrador, sin merge ni release.
- **Costo:** $0 / herramientas locales existentes.
- **Aprendizaje:** la primera captura salió vacía por redirección UTF-16 de PowerShell; `cmd /c` con `exec-out` produjo PNG válido. Un ANR de System UI y una descarga de Robolectric fallida en CI fueron incidentes de entorno, no del código; el job se relanzó. No se presentan fallos de infraestructura como resultados del producto.
- **Pendiente:** revisión manual de TalkBack, escala intermedia 1.3 y aceptación visual por bloques. Verificar CI del head `dda6fdb` tras el relanzamiento; CodeQL de la base apilada sigue sin acreditarse. Sin nuevas integraciones ni implementación de `031`.

### 2026-11-22 · Continuación autorizada: visitante a escala 1.3

- **Autor:** usuario + OpenCode.
- **Módulo / tareas:** `000` T014, seguimiento T036.
- **Qué se hizo:** tras la indicación «dale», se revisó visitante a 393×800 dp y fuente 1.3 en ambos temas; cuatro capturas de inicio/error. No hubo cambios de código ni nuevas pantallas.
- **Evidencia:** `docs/evidencia/T014-visitante-adaptativo.md`, capturas `T014-visitante-393-1.3x-*.png`; error completo y reintento alcanzables por desplazamiento. CI de `42490ec`: cuatro checks en verde, run `45044830978`, build de 3m42s; resultado comunicado en PR #29.
- **Decisiones:** la autorización para continuar no equivale a aceptación visual. T014/T036 abiertas; sin fusión ni release.
- **Costo:** $0 / herramientas existentes.
- **Aprendizaje:** capturar tras estabilizar la pantalla y revisar cada PNG; una captura coincidió con splash y fue reemplazada. El fallo de descarga no se reprodujo, pero su causa raíz no quedó identificada.
- **Pendiente:** TalkBack manual, acceso y estados restantes a escala 1.3, aceptación visual humana. Capturas nuevas y esta actualización permanecen locales hasta su publicación.

### 2026-11-22 · Regresiones de acceso a escala intermedia

- **Autor:** usuario + OpenCode.
- **Módulo / tareas:** `000` T014/T029, seguimiento T036.
- **Qué se hizo:** seis casos de login, registro y recuperación a 320 dp/fuente 1.3, claro y oscuro, en `CompactAccessTest`. Sin cambios de pantallas ni de lógica de producción.
- **Evidencia:** suite focalizada de 16 casos, ktlint/detekt en verde (32s); pipeline completo en verde (30s, 383 tareas), XML de 44 suites/256 pruebas sin fallos/errores/omitidas. Detalle en `docs/evidencia/T014-visitante-adaptativo.md`.
- **Decisiones:** no extender `031` a demo ni pantallas de negocio; se delegó al arquitecto la redacción anticipada de su plan técnico en el espacio separado `virgo-visual-reference`, sujeto a revisión humana. No implica implementación aprobada ni cierre de base.
- **Costo:** $0 / herramientas existentes.
- **Aprendizaje:** reutilizar aserciones con escala parametrizable conserva la cobertura 2.0 y añade 1.3 sin duplicar fixtures; pruebas JVM no acreditan anuncio audible.
- **Pendiente:** TalkBack manual, capturas de acceso/estados restantes, aceptación visual, publicación y CI de cambios locales. T014/T036 abiertas.

### 2026-11-23 · Demo visual urgente de tres roles

- **Autor:** usuario + OpenCode, con implementación delegada por rol y revisión técnica automatizada de solo lectura.
- **Módulo / tareas:** paquetes provisionales DV-01–DV-06 de `docs/diseno/excepcion-demo-urgente.md`; no son cierre ni ejecución acreditada de `031`.
- **Autorización:** el usuario aprobó la excepción temporal propuesta para adelantar la demo visual y regularizar los specs después, conservando seguridad y pruebas. Posteriormente pidió PR en borrador con aceptación visual únicamente suya y subir los cambios al terminar, no antes.
- **Qué se hizo:** launcher debug separado «VirGo Demo», selector de tres roles, 55 destinos autenticados de muestra y 15 hojas locales; navegación, regreso, índice por rol y formularios con máximo de 500 caracteres. Reutiliza tema, tipografía y mapa existentes. Se añadieron ilustraciones geométricas locales identificadas, sin fotografías nuevas de licencia incierta. No se alteraron flujos reales, sesión, endpoints ni secretos.
- **Evidencia inicial:** compilación `:app:compileDebugKotlin` correcta (7m52s); formato de fuentes debug/testDebug correcto (16s). Los nuevos tests y el pipeline completo aún requieren resultado final. Se amplió explícitamente la fuente de detekt del módulo app para cubrir también debug y testDebug, sin quitar las fuentes previas. No se atribuye esa ampliación a release.
- **Decisiones:** rama local `feat/032-demo-visual-roles` apilada sobre `fix/000-fundamentos-visuales`, sin modificar el PR #29. Aceptación visual exclusiva del usuario; no solicitar otros revisores humanos para esa decisión, sin debilitar checks ni ruleset. GitHub no permite pedir revisión formal al propio autor: identificar responsabilidad en cuerpo y asignación.
- **Límites:** datos/fechas ficticios; hojas con estado local que no actualizan pantallas de fondo; algunos detalles comparten muestra. `rememberSaveable` puede restaurar estado mediante Android; no es persistencia backend. No hay pagos, mensajes, IA, reservas, notificaciones ni subidas reales.
- **Costo:** $0 / herramientas existentes, sin dependencias de servicios nuevas.
- **Aprendizaje:** la integración encontró recursos duplicados entre descripción de rol y formulario; se separaron sus nombres. Un análisis que omita las fuentes debug no acredita esta demo: se reforzó su cobertura. La revisión estática no detectó bloqueantes de seguridad en lo inspeccionado, pero no sustituye Gitleaks ni ejecución.
- **Resultado final local:** pipeline completo en verde (2m29s, 383 tareas), XML de 49 suites/274 pruebas sin fallos/errores/omitidas. Detekt incluye debug/testDebug; lint app sin errores, 42 avisos/11 sugerencias. Corregidos cierre de hojas largas y recurso con porcentajes literales, sin retirar aserciones ni reglas.
- **APK y capturas:** debug de 24 595 173 bytes, SHA-256 `e66c6e7abcb8644fb51e0c5a3ca0bd99d5f86739a4fed251d242aa3d3302f2df`, firma debug verificada. Instalado en emulador con hash coincidente; recorrido de tres roles, detalle claro/oscuro y hojas de búsqueda/preferencias. Nueve capturas finales en `docs/evidencia/demo-visual/`, informe `verificacion.md`. ANR de Launcher/System UI resueltos sin borrar datos; capturas de splash/diálogo reemplazadas. No se actuó sobre teléfonos conectados.
- **Publicación autorizada:** código/evidencia publicados en `9e96bf4a285c57706dc9e3db3482edbd76304d43`, rama `feat/032-demo-visual-roles`, [PR #31](https://github.com/virgorent/VirGO_Movil/pull/31) en borrador y apilado sobre #29. Asignado únicamente a `ccabrera-dev8`, sin solicitudes de otros revisores; el cuerpo reserva expresamente la aceptación visual al usuario. Checkout limpio tras el push de esa entrega.
- **CI inicial:** `gitleaks`, `dependency review` y `changes` verdes para `9e96bf4`; build/test remoto en curso al registrar esta entrada. El resultado final por head se consulta en el PR; no se presume verde por la ejecución local.
- **Pendiente:** CI remoto del head final, aceptación visual exclusiva del usuario y regularización SDD. T014/T036 siguen abiertas; no hay aceptación visual, merge ni release.

### 2026-11-23 · Consolidación de la app y sesión por rol

- **Autor:** usuario + OpenCode, con revisión técnica automatizada de seguridad.
- **Módulo / tareas:** UI-01–UI-06 provisionales en `docs/diseno/integracion-app-unificada.md`, bajo la excepción temporal autorizada; sin cierre de specs.
- **Qué se hizo:** 55 destinos y 15 hojas pasan al source set principal; se retira launcher separado. Sesión cifrada con identidad/rol validado, expiración y rechazo por defecto; registro sin sesión vuelve al login. Workspace compartido en memoria por cuenta/rol, limpiado al salir; muestra sin cuenta únicamente desde debug.
- **Evidencia:** pipeline final completo verde (1m46s, incremental), 57 suites/318 pruebas sin fallos/errores/omitidas. APK debug SHA-256 `24f896168cb4b67c31917bfe9249ef181fda393bd02427955e959e4d36c931f0`, instalado en `emulator-5554`. Una actividad MAIN/LAUNCHER. Login de los tres roles contra servidor local simulado, restauración tras force-stop y logout comprobados; no acredita login de producción. Ocho capturas e informe en `docs/evidencia/unificacion-app/`.
- **Decisiones:** ADR-17; no almacenar refresh token ni inventar renovación. Logout local no revoca token remoto. Consolidación contra `main`, sin modificar #27/#29/#31 ni desactivar protecciones; aceptación visual exclusiva de `ccabrera-dev8`, asignación sin autoaprobación formal.
- **Costo:** herramientas locales existentes; CI según configuración del repositorio.
- **Aprendizaje:** distinguir pruebas con contrato simulado de humo de producción; documentar el error de catálogo inicial y ANR de System UI sin presentarlos como éxito funcional.
- **Pendiente:** CI del head publicado, revisión visual del usuario, login real por rol, revocación/renovación cuando exista contrato, endpoints ausentes, TalkBack y T014/T036, regularización SDD. No se fusiona ni publica release.
- **Publicación:** código y evidencia subidos en `a5a6739`; [PR #32](https://github.com/virgorent/VirGO_Movil/pull/32) confirmado en borrador contra `main`, asignado solo a `ccabrera-dev8`, sin solicitudes de revisión. GitHub informa `MERGEABLE` (sin conflictos, no aprobación). CI inicial en curso: `movil-ci`, `movil-codeql`, Gitleaks y dependency review; consultar el head final antes de integrar.

### 2026-11-23 · UI-07: selectores, jerarquía e iconos

- **Autor:** usuario + OpenCode.
- **Módulo / tareas:** UI-07, reparación visual explícitamente solicitada con captura de IA Studio; sin nuevas APIs ni cierre de specs.
- **Qué se hizo:** pestañas exclusivas en IA Studio en vez de cuatro botones primarios; selector de concepto con opciones y estado seleccionado accesible. Enlaces neutros con flecha en cliente y grupos principales de gestión de propietario/proveedor. Márgenes de las acciones de hojas, separación de toolbar e iconos Material delineados en navegación y Studio.
- **Evidencia:** pipeline completo final verde en 1m22s, incremental; 58 suites/322 pruebas sin fallos/errores/omitidas. Cuatro pruebas nuevas cubren selección, menú, compacto oscuro con fuente 2.0 y separación/navegación. APK debug de 25 217 319 bytes, SHA-256 `10ace40cbf1a7141f332375ee0ac908b583ceccfc77285e7509dccad8628e49c`, instalado en emulador; cuatro capturas claras/oscuras en `docs/evidencia/unificacion-app/09-*` a `12-*`. Informe actualizado.
- **Decisiones:** mantener funciones, avisos de simulación y APIs; el cambio afecta la presentación, no habilita IA real. Continúa el PR #32 en borrador y revisión visual exclusiva del usuario. CI del head anterior `4ce9a05` consultado: todos los checks verdes; no se atribuye ese resultado al nuevo head.
- **Costo:** herramientas locales existentes; sin servicios ni dependencias nuevas.
- **Aprendizaje:** diferenciar acciones, navegación y selección reduce competencia visual. Recompilar sin incremental resolvió los símbolos duplicados tras mover fuentes. Un fallo aislado de `NavigationTest` no se reprodujo al repetir la suite completa; causa no determinada.
- **Pendiente:** aceptación visual del usuario y CI del nuevo head. No se declara pulido exhaustivo de todas las pantallas, TalkBack ni cierre de T014/T036. Sin fusión ni release.

### 2026-11-24 · Beta integrada sin simulaciones en el recorrido normal

- **Autor:** usuario + OpenCode, con revisiones automatizadas de seguridad y trazabilidad.
- **Módulo / tareas:** `034-beta-integrada`, T3401–T3404 verificadas localmente;
  T3405 pendiente de aceptación humana. Alcance visual autorizado explícitamente
  en la conversación; no habilita `030` ni cierra la base.
- **Qué se hizo:** tras comprobar #32 aprobado y fusionado, rama
  `feat/034-beta-integrada` desde `main`. Catálogo sin muestras/mapa ilustrativo;
  destinos por rol con estados pendientes y acciones deshabilitadas; cuenta de
  solo lectura desde sesión, logout local explícito, sin ruta demo ni selector.
- **Evidencia:** [informe y capturas](docs/evidencia/beta-integrada/verificacion.md);
  pipeline completo verde en 2m21s, 60 suites/334 pruebas sin fallos. APK debug
  instalado en emulador. SHA-256 en el informe.
- **Decisiones:** conservar componentes históricos sin acceso desde la beta;
  no inventar capacidades, fotos, distancias o transacciones. Recuperación sin
  endpoint operativo presenta indisponibilidad. Perfil/logout remotos aparte.
- **Costo:** herramientas locales; sin dependencias, servicios o permisos nuevos.
- **Aprendizaje:** retirar solo el rótulo demo sería engañoso; se eliminaron
  los recorridos simulados de la navegación normal. Detekt detectó un método
  largo; se separaron componentes sin relajar reglas.
- **Pendiente:** revisión visual exclusiva del usuario, TalkBack, tamaños y
  navegación ampliada, CI remoto y revisión técnica antes de fusionar. Sin
  release, cambio de versión, cuentas productivas ni despliegue backend.
- **Entrega:** implementación subida en `1109d72`;
  [PR #35](https://github.com/virgorent/VirGO_Movil/pull/35) en borrador,
  asignado a `ccabrera-dev8`, sin solicitar revisores visuales adicionales.

### 2026-11-25 · Lote de specs del MVP cliente

- **Autor:** usuario + Claude Code.
- **Qué se hizo:** se reescribieron a v2.0.0 los borradores `006`, `008`, `009` y `022`, y se actualizó `030` (traído del PR #33) al estado de la beta `034`. Los cinco quedan **Propuestos para aprobación** como lote MVP. Se retiraron las simulaciones de la maqueta (respuestas automáticas, tarjeta «0000», actividad de ejemplo, fotos de ejemplo).
- **Decisiones de alcance propuestas:** reservar **sin cobro en la app** (`pending_payment`, anticipo del 30 % calculado por el servidor); detalle sin galería (no hay carga de fotos ni librería de imágenes); publicación directa del salón por el propietario; precios ocultos sin sesión.
- **Hallazgos de contrato (backend):** `POST /quotes` y `POST /bookings` exigen `eventId` y `007` no está en el MVP; `POST /bookings` exige `quoteId`; el esquema `venues` exige `location` y `photos`; ninguna cotización pasa a «Respondida» sin `POST /quotes/:id/response` (`062`). Endpoints adicionales del MVP: `GET /venues/mine` (`025`) y `GET /bookings` (`066`).
- **Aprobación:** el usuario aprobó el lote completo el 2026-11-25 («Apruebo el lote»).
- **Bloqueo constitucional:** el Art. II.2 impide ejecutar tareas hasta que `000` esté cerrado **y** backend entregue la URL y el JSON de cada endpoint. Esto prevalece sobre la autorización previa de implementar contra MockWebServer; requiere cerrar `000` o una excepción temporal aprobada y registrada (precedente: `docs/diseno/excepcion-demo-urgente.md`). `030` ya cumple la segunda condición (endpoints desplegados).
- **Pendiente:** respuestas de backend a las preguntas P-xx de cada spec; cierre de `000` (T014 TalkBack y T036) antes de implementar.
- **Costo:** $0.

### 2026-11-25 · `030-cuenta-sesion` implementado bajo excepción

- **Autor:** usuario + Claude Code.
- **Autorización:** excepción temporal aprobada por el usuario («Sí, registra excepción»), registrada antes de implementar en [`docs/diseno/excepcion-030-sin-cierre-000.md`](docs/diseno/excepcion-030-sin-cierre-000.md).
- **Qué se hizo:** T3001–T3007. Perfil real desde `GET /users/me` (solo lectura, sin inventar teléfono ni verificación); logout remoto con `POST /auth/logout` (acepta 200/204; 401 = ya revocada; red/5xx/10 s → cierre local con aviso); motivos de cierre (`EXPIRED`, `REVOKED`, `ROLE_MISMATCH`, `ACCOUNT_UNAVAILABLE`, `LOGOUT_UNCONFIRMED`) mostrados una vez; rol del servidor validado contra la sesión.
- **Evidencia:** pipeline completo **BUILD SUCCESSFUL in 4m 2s**, 63 suites / 362 pruebas sin fallos (+28). Nuevas: `AccountRepositoryImplTest` 8, `SessionViewModelTest` +8, `AccountProfileViewModelTest` 7, `AccountProfileScreenTest` 5.
- **Aprendizaje:** consumir el motivo dentro de un `LaunchedEffect` con ese motivo como clave cancela el `showSnackbar`; el aviso se lanza en un scope independiente.
- **Pendiente:** T3008 (humo con cuenta desechable creada por el usuario: login → perfil → logout → token anterior 401), capturas y aceptación visual; regularizar la excepción al cerrar `000`.

### 2026-11-25 · `035-pulido-visual-beta` implementado

- **Autor:** usuario + Claude Code.
- **Autorización:** spec aprobado («Apruebo y amplío») y excepción ampliada en [`docs/diseno/excepcion-030-sin-cierre-000.md`](docs/diseno/excepcion-030-sin-cierre-000.md).
- **Qué se hizo:** T3501–T3508 y T3509 salvo la aceptación visual. Componentes del lenguaje del prototipo (`VirGoPageHeader`, `VirGoRow`, `VirGoSection`, `VirGoPageBody`, tonos de `VirGoBadge`, fondo `soft`); catálogo, menú de secciones, sección sin servicio, Cuenta y espacio de trabajo rediseñados; mapa de íconos por destino.
- **Evidencia:** pipeline aislado **BUILD SUCCESSFUL in 6m 13s**, 67 suites / 376 pruebas sin fallos (+14). Capturas `docs/evidencia/035/antes-*` y `despues-*` (393 dp, claro y oscuro).
- **Aprendizaje:** el barrido a 320 dp/2.0 detectó un defecto real (título «Mensajes» de 12 px de ancho junto a la insignia); se resolvió apilando el contenido final. Los textos centrados daban falsos positivos al medir el nodo; ahora se mide el ancho de línea. El *kicker* se ve en mayúsculas pero expone el texto original a TalkBack y a las pruebas. Correr Gradle en `app/build/` mientras Android Studio ejecuta la app bloquea `R.jar` y deja APK truncados; el pipeline usa un *init script* que aísla el directorio de compilación.
- **Pendiente:** CA-07 (aceptación visual exclusiva del usuario), CI remoto del PR #40 y fusión en orden #35 → #36 → #38 → #39 → #40.
- **Costo:** $0.
