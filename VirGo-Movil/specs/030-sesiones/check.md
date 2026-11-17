# Check — 030 Sesiones

Cada casilla lleva evidencia. El módulo se marca *Cerrado* solo con todo en ✅ o ➖ justificado.

**Leyenda:** ✅ Cumple · ➖ No aplica (justificar) · ⏳ Pendiente

| Campo | Valor |
|---|---|
| Módulo | `030-sesiones` |
| Estado del spec | Borrador |
| Revisó | — |
| Fecha de cierre | — |

## 1. Especificación y gobernanza

| Control | Evidencia | Estado |
|---|---|---|
| Spec, plan y tareas aprobados | PR de aprobación del equipo — | ⏳ |
| Constitution Check sin ❌ | [plan.md](plan.md) §3 | ✅ |
| Sin secretos en el repositorio | Gitleaks en el PR del módulo — | ⏳ |
| Preguntas abiertas respondidas | P-01 (arranque sin splash) — | ⏳ |

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | — | ⏳ |
| CA-02 | — | ⏳ |
| CA-03 | — | ⏳ |
| CA-04 | — | ⏳ |
| CA-05 | — | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `./gradlew testDebugUnitTest` · totales: — | ⏳ |
| Integración | MockWebServer en `AuthRepositoryImplTest` — | ⏳ |
| Seguridad | `TokenStoreTest` + aserción de cifrado — | ⏳ |
| Humo | Capturas en `docs/evidencia/` — | ⏳ |

## 4. DevSecOps

| Control | Evidencia | Estado |
|---|---|---|
| Gitleaks sin hallazgos | check del PR — | ⏳ |
| Dependency review sin vulnerabilidades altas | check del PR — | ⏳ |
| SAST (CodeQL / linters) en verde | checks del PR — | ⏳ |
| Workflows con permisos mínimos | sin cambios de workflows en este módulo | ➖ |
| Sin credenciales en logs ni artefactos | revisión del PR — | ⏳ |

## 5. PR e integración continua

- [ ] PRs del módulo: enlace —
- [ ] Checks requeridos en verde y una revisión.

## 6. Release

- [ ] Versión y commit: —
- [ ] Artefacto firmado y verificado: —

## 7. Memoria y aprendizaje

- [ ] Entrada de cierre en `memoria.md`.
- [ ] Mejoras propuestas a la constitución, `AGENTS.md`, skills o subagentes (o «ninguna»).
