# Check — 008 Cotizaciones del cliente

| Campo | Valor |
|---|---|
| Módulo | `008-cotizaciones-cliente` |
| Estado del spec | Aprobado (lote MVP, 2026-11-25) · v2.1.0 ajustada al contrato · implementado bajo excepción; humo pendiente del despliegue |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [x] Excepción ampliada y registrada («hazlos todos de una vez»).
- [x] P-01 resuelta (`eventId` opcional en el backend). P-03 y P-04 respondidas con el código de backend (ver spec §9).
- [ ] P-02: la pantalla del propietario para responder no está en el MVP (se cubre con `024`).
- [ ] Backend desplegado (`POST`/`GET /quotes`, `GET /quotes/:id`).

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `QuoteRequestViewModelTest.sendCreatesOneQuoteAndShowsItAsSent` + `QuotesRepositoryImplTest.createSendsTheContractBodyAndReadsTheSentQuote` + `clientExploreGoesFromCatalogToDetailToQuoteOrBookingAndBack` | ✅ |
| CA-02 | `invalidFieldsNeverReachTheServer` + `QuoteTest.eachFieldIsChecked` | ✅ |
| CA-03 | `serverFieldErrorsAreShownNextToTheirField` + `fieldErrorsAndOtherFailuresAreMapped` + `errorsAndCapacityWarningAreShownNextToTheirField` | ✅ |
| CA-04 | `retryAfterANetworkFailureKeepsTheDataAndDoesNotDuplicate` + `QuoteTest.retryAfterAnUncertainFailureReusesTheIdenticalSentQuote` + `draftSurvivesRecreationAndANewVenueStartsFresh` | ✅ |
| CA-05 | `listShowsRealStatusesAndOpensADetail` («Enviada», «Respondida», «En revisión») + `unknownStatusAndMissingFieldsDoNotFail` | ✅ |
| CA-06 | `emptyListLeadsToTheCatalog` | ✅ |
| CA-07 | `answeredQuoteShowsServerAmountsAndBooks` + `clientQuotesOpensBookingWithTheQuote` (abre `009` con `quoteId` y fecha) | ✅ |
| CA-08 | `sentQuoteWaitsWithoutBooking` | ✅ |
| CA-09 | `BetaAccessibilityTest.quotesAreAccessibleInBothThemes`: solicitud con errores y aviso, enviada, lista, vacío y detalle; 320 dp/2.0, ambos temas | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `QuoteTest` 9, `QuoteRequestViewModelTest` 9, `QuotesViewModelTest` 4 | ✅ |
| Integración | `QuotesRepositoryImplTest` 5: cuerpo del contrato (sin `eventId`, `clientName` ni `target.name`), lectura de `answered` con `response`, estados desconocidos, errores y tiempo agotado | ✅ |
| UI | `QuotesScreensTest` 9 + flujo catálogo → detalle → cotizar | ✅ |
| Seguridad | El mensaje del cliente no se registra en logs; solo vive en el `ViewModel` y `SavedStateHandle` | ✅ |
| Regresión | Pipeline aislado completo en verde salvo un fallo intermitente de `NavigationTest` (usa la API real); repetido aislado 3/3 y la suite de `app` completa en verde: **86 suites / 510 pruebas, 0 fallos (+36)** | ✅ |
| Mutación | `pitest` de `:core:domain` en verde | ✅ |
| Humo | Contra producción tras el despliegue | ⛔ |

## 4. PR

- [x] PR: [virgorent/VirGO_Movil#44](https://github.com/virgorent/VirGO_Movil/pull/44) (borrador, base `feat/009-reservar`).
- [ ] Una revisión aprobada y checks requeridos en verde. No se fusiona antes del despliegue de backend.

## 5. Memoria

- [x] Entrada en `memoria.md`.
