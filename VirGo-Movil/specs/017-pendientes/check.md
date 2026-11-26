# Check — 017 Pendientes

| Campo | Valor |
|---|---|
| Módulo | `017-pendientes` |
| Estado del spec | Aprobado (2026-11-25) · v2.0.0 · implementado bajo excepción; humo pendiente |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-26 |

⚠️ **Backend:** `EventsController` existe en `feat/api-mobile-priority` (PR #37, cerrado sin fusionar) pero **no** en la cadena nueva hacia `main` (#38 → #39 → #40). Hasta que el backend lo incluya, este módulo no tiene servidor en producción.

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `ClientEventTest.tasksAreValidatedAndGrouped` + `tasksAreGroupedAndToggle` | ✅ |
| CA-02 | `tasksAreToggledSavedAndDeleted` + PATCH con `done` en `tasksAreReadCreatedUpdatedAndDeleted` | ✅ |
| CA-03 | `tasksAreToggledSavedAndDeleted` (título y fecha obligatorios) | ✅ |
| CA-04 | `BetaAccessibilityTest.quotesAreAccessibleInBothThemes` («tasks», «task form») | ✅ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `ClientEventTest` 6, `EventViewModelsTest` 8 | ✅ |
| Integración | `EventsRepositoryImplTest` 5 con la forma real de `EventsController` | ✅ |
| UI | `EventScreensTest` 7 | ✅ |
| Regresión | ktlint, detekt, lint, pitest y `assembleDebug` en verde; **93 suites / 554 pruebas, 0 fallos (+26)** | ✅ |
| Humo | Contra producción cuando el backend incluya `EventsController` | ⛔ |

## 3. PR

- [ ] PR: enlace —

## 4. Memoria

- [x] Entrada en `memoria.md` (bloque del evento).
