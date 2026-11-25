# Check — 024 Solicitudes de cotización del propietario

| Campo | Valor |
|---|---|
| Módulo | `024-propietario-solicitudes` |
| Estado del spec | Aprobado (2026-11-25) · v2.0.0 contra el contrato real · implementado bajo excepción; humo pendiente del despliegue |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

## 1. Especificación y gobernanza

- [x] Spec v2.0.0 aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion»).
- [x] Excepción ampliada y registrada antes de implementar.
- [ ] Backend desplegado con el PR #41 (responder exige rol `owner`).

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `OwnerRequestsViewModelTest.tabsFilterByRealStatus` + `OwnerRequestsScreenTest.listShowsClientAndTabsSwitch` + `emptyTabSaysSo` | ✅ |
| CA-02 | `answeringSendsOnceAndUpdatesTheList` + `QuotesRepositoryImplTest.respondSendsTheOwnerAnswerAndReadsTheAnsweredQuote` + `sentRequestShowsTheFormAndSends` | ✅ |
| CA-03 | `invalidAmountsNeverReachTheServer` + `QuoteAnswerTest` | ✅ |
| CA-04 | `conflictWarnsAndReloadsTheServerState` + `respondConflictAndForbiddenAreMapped` + `fieldErrorsAndConflictAreShown` | ✅ |
| CA-05 | `answeredRequestIsReadOnly` | ✅ |
| CA-06 | `BetaAccessibilityTest.ownerVenuesAreAccessibleInBothThemes` (lista, vacío, formulario con errores y respondida; 320 dp/2.0, ambos temas) | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `QuoteAnswerTest` 4, `OwnerRequestsViewModelTest` 7 | ✅ |
| Integración | `QuotesRepositoryImplTest` +2: cuerpo de la respuesta (sin `deposit`/`message` nulos), `409` y `403` | ✅ |
| UI | `OwnerRequestsScreenTest` 5 | ✅ |
| Seguridad | Montos y notas no se registran en logs | ✅ |
| Regresión | ktlint, detekt, lint, pitest y `assembleDebug` en verde. `NavigationTest` (usa la API real) falló por red en la corrida completa; aislado 2/2 y la suite de `app` completa en verde: **89 suites / 528 pruebas, 0 fallos (+18)** | ✅ |
| Humo | Responder una cotización real tras el despliegue | ⛔ |

## 4. PR

- [ ] PR: enlace —
- [ ] Una revisión aprobada y checks requeridos en verde. No se fusiona antes del despliegue de backend.

## 5. Memoria

- [x] Entrada en `memoria.md`.
