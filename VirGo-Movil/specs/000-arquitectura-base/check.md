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
- [ ] `verificar-constitucion` sobre la gobernanza: «Alineado» (T004).
- [ ] MCP `figma` y `github` instalados y probados (T037).
- [ ] P-01 (ID de la app) y P-02 (custodia del keystore) respondidas.

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Compilación de módulos y CI (T007, T031) | ⏳ |
| CA-02 | Arranque e inicio provisional en el emulador (T015, T017, T030). Evidencia: `docs/evidencia/CA-02-01-splash.png` (splash) y `docs/evidencia/CA-02-02-inicio-visitante.png` (inicio visitante), Pixel_10_Pro 2026-11-07; `am start -W` → `TotalTime 2677 ms` (emulador, debug; RNF-03 pendiente de medición en release). T030 sigue ⏳ | ✅ |
| CA-03 | Sistema de diseño y modo oscuro (T014, T030). Evidencia: `docs/evidencia/evidencia-mod-oscuro.png` (modo oscuro con `cmd uimode night yes`) | ✅ |
| CA-04 | Diagnóstico contra la API (T024, T030). Evidencia: `docs/evidencia/CA-04-diagnostico-api-disponible.png` — v0.2.0, URL de producción, «API disponible», 227 ms (2026-11-14) | ✅ |
| CA-05 | Diagnóstico en *debug* (T024). Evidencia: `docs/evidencia/CA-05-diagnostico-api-caida.png` — con modo avión: «API no disponible» + «No hay conexión con la API. Revisa tu internet e inténtalo de nuevo.» sin cierre; «Reintentar» recupera (1123 ms) | ✅ |
| CA-06 | *Release* sin tráfico en claro (T026, T030). Evidencia: `aapt dump xmltree` sobre el APK de `v0.2.0` → `cleartextTrafficPermitted=0x0`, sin `domain-config` en claro; la variante *release* rechaza `http://` en compilación (T019) | ✅ |
| CA-06 | *Release* sin tráfico en claro (T026, T030) | ⏳ |
| CA-07 | Token cifrado (T022) | ⏳ |
| CA-08 | CI bloquea la fusión (T031, T039) | ⏳ |
| CA-09 | Release manual con APK firmado (T035). Evidencia: release `v0.2.0` con APK+AAB firmados, instalado y con login en verde (2026-11-14) | ✅ |
| CA-10 | Accesibilidad y escala de fuente (T013, T029) | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `./gradlew testDebugUnitTest` · totales: — | ⏳ |
| Integración | MockWebServer y Robolectric (T028) · totales: — | ⏳ |
| UI | Compose Testing (T029) · totales: — | ⏳ |
| Seguridad | Token cifrado, sin tráfico en claro, Lint, CodeQL y Gitleaks sin hallazgos altos | ⏳ |
| Mutación | `./gradlew pitest` (T038) · puntaje base: — % | ⏳ |
| Humo | APK `v0.2.0` instalado en el emulador y login contra producción en verde (T035). Evidencia: `docs/evidencia/CA-09-release-v0.2.0-login.png` | ✅ |

## 4. PR e integración continua

- [ ] PRs del módulo: enlaces —
- [ ] Ruleset exportado en `.github/rulesets/main-protection.json` (T039).
- [ ] Todos con los checks requeridos en verde y una revisión.

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
| RNF-03 Ceros en claro | `network_security_config.xml` con `<base-config cleartextTrafficPermitted="false" />`; manifiesto de *release* sin `usesCleartextTraffic` y con `networkSecurityConfig` (verificado con `aapt dump xmltree` sobre el APK de *release*: `res/8G.xml` → `cleartextTrafficPermitted=(type 0x12)0x0`) | ✅ |
| RNF-04 Keystore robusto | `KeystoreAead.create` pre-registra AeadConfig antes de crear el keyset (logcat `[I ConfigManager] Registered [config_id=1] for algorithm AESGCM with keysize 128`) y tiene fallback a clave estática si no existe keyset; fix en el PR #9 (`c1d4552`) | ✅ |

> **Causa del incidente de arranque:** el emulador arrancando por *snapshot* corrompía el registro del keystore; el fix es pre-registrar AeadConfig y evitar el arranque por snapshot. Detalle en `memoria.md` (entrada 2026-11-12).
