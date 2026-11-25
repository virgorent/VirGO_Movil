# Check — 009 Reservar (sin cobro en la app)

| Campo | Valor |
|---|---|
| Módulo | `009-reservar-pagar` |
| Estado del spec | Aprobado (lote MVP, 2026-11-25) · v2.1.0 ajustada al contrato · implementado bajo excepción; humo pendiente del despliegue |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [x] Excepción ampliada y registrada antes de implementar («Sí, sigue con 009»).
- [x] P-01 (`quoteId`/`eventId` opcionales), P-03 (`minimumHours` en el detalle) y P-04 (forma de lectura) resueltas con el código de backend. P-02 (cómo se paga) sigue fuera de la app.
- [ ] Backend desplegado (`POST`/`GET /bookings`, `GET /bookings/:id`).

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `BookingConfirmViewModelTest.confirmCreatesOneBookingWithoutDepositAndShowsTheServerFolio` + `BookingsRepositoryImplTest.createSendsKeyAndNoDepositAndReadsTheServerBooking` + `clientExploreGoesFromCatalogToDetailToBookingAndBack` | ✅ |
| CA-02 | `quoteContextIsSentAndPrefillsTheDate` + `quoteIdIsSentWhenPresent` (el camino desde `008` se conecta al implementarlo) | ✅ |
| CA-03 | `pastDateOrZeroHoursNeverReachTheServer` + `hoursBelowTheVenueMinimumAreRejectedLocally` + `BookingTest` | ✅ |
| CA-04 | `networkRetryReusesTheKeyAndCreatesOnlyOneBooking` + `retryUsesTheSameKey` + `draftSurvivesRecreationWithTheSameKey` | ✅ |
| CA-05 | `serverRejectionIsShownNextToHours` + `fieldErrorsAndServerRejectionAreShownNextToTheirField` (el `400` del servidor no trae detalle por campo) | ✅ |
| CA-06 | `receiptIsHonestAboutPaymentAndDate` («Pendiente de pago», no se cobró, fecha no apartada) | ✅ |
| CA-07 | `BookingsViewModelTest` + `listOpensAReceiptAndEmptyStateIsClear` + `receiptFromTheListComesFromTheServer` | ✅ |
| CA-08 | `BetaAccessibilityTest.clientFlowIsAccessibleInBothThemes`: confirmar (con errores), comprobante y lista; 320 dp/2.0, ambos temas | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `BookingTest` 5, `BookingConfirmViewModelTest` 12, `BookingsViewModelTest` 4 | ✅ |
| Integración | `BookingsRepositoryImplTest` 5: clave presente e igual en el reintento, cuerpo sin `deposit`, `quoteId` opcional, lectura y errores | ✅ |
| UI | `BookingsScreensTest` 6 + flujo catálogo → detalle → reservar | ✅ |
| Seguridad | Sin datos de pago en UI, red ni logs; la clave solo vive en el `ViewModel` y `SavedStateHandle` | ✅ |
| Regresión | Pipeline aislado completo: **BUILD SUCCESSFUL in 2m 39s**; 81 suites / 474 pruebas, 0 fallos (+32). Una corrida previa tuvo un fallo intermitente de `NavigationTest` (usa la API real); repetido aislado 3/3 y el pipeline siguiente en verde | ✅ |
| Humo | Contra producción tras el despliegue | ⛔ |

## 4. PR

- [x] PR: [virgorent/VirGO_Movil#43](https://github.com/virgorent/VirGO_Movil/pull/43) (borrador, base `feat/006-detalle-salon`).
- [ ] Una revisión aprobada y checks requeridos en verde. No se fusiona antes del despliegue de backend.

## 5. Memoria

- [x] Entrada en `memoria.md`.
