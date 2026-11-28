# Check — 000 Arquitectura base del Móvil

Lista de verificación del módulo (Art. I.4). Cada casilla lleva su evidencia. El módulo se marca *Cerrado* solo con todas las casillas en ✅ o ➖ justificado.

| Campo | Valor |
|---|---|
| Módulo | `000-arquitectura-base` |
| Estado del spec | Aprobado; implementación en curso |
| Revisó | Equipo (2026-11-05) |
| Fecha de cierre | — |

## 1. Especificación y gobernanza

- [ ] Spec, plan y tareas aprobados por el profesor y el equipo. Equipo: 2026-11-05; profesor: pendiente.
- [x] El Constitution Check del plan no tiene ❌ (constitución v1.1.0 aprobada).
- [x] `README.md` en inglés y `.gitignore` DevSecOps en el primer commit (T001). Evidencia: `git ls-tree origin/main VirGo-Movil/.gitignore VirGo-Movil/README.md`; `git check-ignore -v VirGo-Movil/local.properties`.
- [x] `verificar-constitucion` sobre la gobernanza: «Alineado» (T004). Evidencia: `task.md` T004 (segunda pasada «Alineado» tras corregir Art. II y XI).
- [x] MCP `figma` y `github`: resueltos como **no aplica** (T037). Evidencia: `task.md` T037 (decisión 2026-11-16; `figma` → `HTTP 405`, sustituido por maqueta + `core:designsystem`; `github` → `gh` CLI).
- [ ] P-01 (ID de la app): reconciliar evidencia de aprobación en cierre.
- [x] P-02: el usuario confirma custodia y respaldo seguro el 2026-11-20; cuatro nombres de secretos de firma y variable de API presentes. No se prueba restauración ni se accede a los valores.

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Compilación de módulos y CI (T007, T031). Evidencia: `movil-build-test` en verde sobre `9322952` (PR #35, run `45554493579`, intento 2, 2026-11-25; el intento 1 falló por red del runner al descargar `android-all` de Robolectric) y pipeline local completo en verde (§3) | ✅ |
| CA-02 | T010–T013 cerradas técnicamente; APK final instalado con `install -r`, hash instalado coincidente y capturas `T014-final-*.png`. Acredita estados de acceso, no paridad completa con la maqueta. Ver `docs/evidencia/T010-T014-correccion-visual.md` | 🔄 |
| CA-03 | Acceso verificado por pruebas y capturas, incluida recuperación con IME. Visitante reparado tras el fallo histórico de `T014-visitante-compacto-2x-oscuro-bloqueo.png`; 13 pruebas nuevas, APK instalado con hash coincidente y capturas nuevas a 320/393 dp en ambos temas (`T014-visitante-*.png`). Revisión integral automatizada de la beta completada el 2026-11-25 (`BetaAccessibilityTest`, 6 pruebas). Falta solo la verificación manual con TalkBack. Ver `docs/evidencia/T014-visitante-adaptativo.md` y `docs/evidencia/T014-revision-integral.md` | 🔄 |
| CA-04 | Diagnóstico contra la API (T024, T030). Evidencia: `docs/evidencia/CA-04-diagnostico-api-disponible.png` — v0.2.0, URL de producción, «API disponible», 227 ms (2026-11-14) | ✅ |
| CA-05 | Diagnóstico en *debug* (T024). Evidencia: `docs/evidencia/CA-05-diagnostico-api-caida.png` — con modo avión: «API no disponible» + «No hay conexión con la API. Revisa tu internet e inténtalo de nuevo.» sin cierre; «Reintentar» recupera (1123 ms) | ✅ |
| CA-06 | *Release* sin tráfico en claro (T026, T030). Evidencia: `aapt dump xmltree` sobre el APK de `v0.2.0` → `cleartextTrafficPermitted=0x0`, sin `domain-config` en claro; la variante *release* rechaza `http://` en compilación (T019) | ✅ |
| CA-07 | Token cifrado (T022). Evidencia: `TokenStoreTest` (8 pruebas: ida y vuelta, borrado, **archivo sin token en claro**, valor ilegible → nulo, token heredado sin metadatos no autentica, sesión corrupta no expone identidad, identidad cifrada, fallo de cifrado no publica media sesión) | ✅ |
| CA-08 | CI bloquea la fusión (T031, T039). Evidencia: ruleset `main-protection` (id `27049892`) **activo** en `main`; exige PR con 1 aprobación, hilos resueltos y los checks `gitleaks`, `dependency review`, `movil-build-test` y `analyze`; sin borrado ni *force push*. Coincide con `.github/rulesets/main-protection.json` salvo valores por defecto del servidor. El PR #35 aparece `BLOCKED`/`REVIEW_REQUIRED` (consulta 2026-11-25) | ✅ |
| CA-09 | Release manual con APK firmado (T035). Evidencia: release `v0.2.0` con APK+AAB firmados, instalado y con login en verde (2026-11-14) | ✅ |
| CA-10 | Catálogo T013 completo: previews claro/oscuro por componente, paleta y escala tipográfica; 5 pruebas de estados y 23 nuevas de `:core:designsystem` en verde. Revisión integral de T014 (2026-11-25): sin cortes con fuente 2.0 en las pantallas beta alcanzables. Ver `docs/evidencia/T014-revision-integral.md` | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `./gradlew testDebugUnitTest` + `:core:domain:test` + `:core:common:test` · totales 2026-11-25: **61 suites, 340 pruebas, 0 fallos/errores/omitidas** | ✅ |
| Integración | MockWebServer y Robolectric (T028) · incluidas en los totales de Unitarias | ✅ |
| UI | Compose Testing (T029) · incluidas en los totales; accesibilidad en `CompactAccessTest`, `AuthAccessibilityTest`, `GuestAccessibilityTest` y `BetaAccessibilityTest` | ✅ |
| Seguridad | Token cifrado (CA-07), sin tráfico en claro (CA-06), lint local sin errores; `CodeQL`, `analyze` y `gitleaks` en verde sobre `9322952` (PR #35) | ✅ |
| Mutación | `./gradlew pitest` (T038) · puntaje base 2026-11-25: `:core:domain` **60 %** (57/95, cobertura de línea 88 %), `:core:common` **33 %** (1/3). Sin umbral obligatorio (ADR-12: umbral tras medir) | ✅ |
| Humo | APK `v0.2.0` instalado en el emulador y login contra producción en verde (T035). Evidencia: `docs/evidencia/CA-09-release-v0.2.0-login.png` | ✅ |

> **Ejecución local (2026-11-21):** pipeline completo `BUILD SUCCESSFUL in 2m 4s`
> (incremental, 383 tareas; 29 ejecutadas); 43 suites, 237 pruebas, 0 fallos/errores/omitidas;
> Pitest dominio 60 % (57/95) y common 33 % (1/3). Reportes aislados en
> `%TEMP%\opencode\virgo-foundation-build`. No sustituye la verificación de cierre
> ni los checks remotos del PR #29, cuyos resultados deben verificarse por head.

> Esta ejecución incluye las correcciones finales de acceso: `FontScaleTest` 4,
> `CompactAccessTest` 10 y `AuthAccessibilityTest` 3. No acredita toda la app.

> **Seguimiento local (2026-11-22):** pipeline completo verde en 2m 20s,
> 44 suites / 250 pruebas, sin fallos/errores/omitidas. Incluye 13 pruebas de
> visitante y regresiones de pines compactos y región viva. APK instalado y hash
> coincidente; capturas nuevas a 320/393 dp, fuente 1.0/2.0 y ambos temas.
> No se acredita aceptación visual, TalkBack ni cierre de T014/T036.

> **Cierre técnico (2026-11-25):** rama `fix/000-cierre-accesibilidad` sobre
> `9322952`. Pipeline completo `ktlintCheck detekt lint testDebugUnitTest
> :core:domain:test :core:common:test pitest assembleDebug` → **BUILD SUCCESSFUL
> in 3m 24s**; 61 suites / 340 pruebas, 0 fallos/errores/omitidas.

## 4. PR e integración continua

- [x] Reparación T010–T014 publicada en el [PR #29](https://github.com/virgorent/VirGO_Movil/pull/29), **en borrador**, apilado sobre #27 (`feat/005-explorar-salones`); código `693da47`. No implica cierre ni aprobación de merge.
- [x] Ruleset exportado en `.github/rulesets/main-protection.json` (T039) y **aplicado** en GitHub (ver CA-08).
- [ ] Todos con los checks requeridos en verde y una revisión.

> El workflow de CodeQL solo admite PRs contra `main`; no se presume ejecución
> sobre la base apilada de #29. Antes de integrar en `main`, resolver la base con
> Karasu y exigir todos los gates, sin modificar ni relajar sus reglas.

## 5. Release

- [x] Ejecución de `movil-release.yml`: https://github.com/virgorent/VirGO_Movil/actions/runs/43544624048 (2026-11-14, en verde)
- [x] Versión y commit: `v0.2.0` (`versionCode 2`) en `d9ac7e7`; release https://github.com/virgorent/VirGO_Movil/releases/tag/v0.2.0 con `app-release.aab` (5 661 199 B) y `app-release.apk` (2 530 468 B)
- [x] `API_BASE_URL` de *release*: `https://rcof55a3c9.execute-api.us-east-1.amazonaws.com/` (variable `VIRGO_API_BASE_URL` del repositorio)
- [ ] Arranque en frío medido: — s (RNF-03)

## 6. Memoria y aprendizaje

- [ ] Entrada de cierre en `memoria.md` con lo aprendido.
- [ ] Mejoras propuestas a la constitución, `AGENTS.md`, skills o subagentes (o «ninguna»).

## 7. Retos no funcionales (RNF)

| RNF | Evidencia | Estado |
|---|---|---|
| RNF-01 Compatibilidad API 26+ | Configuración SDK en los plugins de convención; carga de fuentes probada en API 26/36. No equivale a una prueba integral en todos los dispositivos | 🔄 |
| RNF-02 Seguridad | Evidencia histórica de CA-06 sobre `v0.2.0`: `cleartextTrafficPermitted=0x0`; T022/T027 documentan el almacenamiento cifrado. Los gates remotos del árbol corregido siguen pendientes | 🔄 |
| RNF-03 Arranque < 2 s | T030 registra 1791/1854/1885 ms en debug y 698 ms en `v0.2.0`. Son mediciones históricas, no del APK corregido actual | 🔄 |
| RNF-04 Accesibilidad | T014: contraste AA (22 pares registrados más 4 pares de texto nuevos por tema), 48 dp, semántica y fuente 2.0 en todas las pantallas alcanzables; pendiente solo TalkBack manual. Ver `docs/evidencia/T014-revision-integral.md` | 🔄 |
| RNF-05 Calidad | Análisis y pruebas locales documentados en §3; checks remotos de estos cambios pendientes | 🔄 |
| RNF-06 Costo | Herramientas locales, fuentes OFL y servicios existentes; sin nuevos servicios de pago | ✅ |
| RNF-07 Trazabilidad | Release histórico `v0.2.0` con commit en §5; la reparación actual sigue local, sin commit/publicación | 🔄 |

> **Causa del incidente de arranque:** el emulador arrancando por *snapshot* corrompía el registro del keystore; el fix es pre-registrar AeadConfig y evitar el arranque por snapshot. Detalle en `memoria.md` (entrada 2026-11-12).
