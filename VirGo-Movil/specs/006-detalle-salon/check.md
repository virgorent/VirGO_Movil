# Check — 006 Detalle del salón

| Campo | Valor |
|---|---|
| Módulo | `006-detalle-salon` |
| Estado del spec | Aprobado (lote MVP, 2026-11-25) · v2.1.0 ajustada al contrato · implementado bajo excepción; humo pendiente del despliegue |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [x] Excepción ampliada y registrada antes de implementar («Sí, sigue con 006»).
- [x] P-01–P-03 resueltas con el código de `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`): no envía `services` ni `verified`; `price` solo con sesión. P-04 (fotos) sigue fuera del MVP.
- [ ] Backend desplegado (`GET /venues/:id`) y al menos un salón publicado.

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `VenueDetailScreenTest.catalogOpensTheDetailWithTheVenueId` + `clientExploreOpensTheDetailAndReturnsToTheCatalog` + `VenueDetailRepositoryTest`. Humo en producción: ⏳ T606 | 🔄 |
| CA-02 | `zeroCapacityAndNoReviewsAreHonest` (sin capacidad, «Sin reseñas todavía», «Precio a cotizar», sin «Verificado») | ✅ |
| CA-03 | `guestSeesNoPriceAndActionsLeadToSignIn`; la app oculta el precio sin sesión aunque llegue (ADR-02) | ✅ |
| CA-04 | `clientSeesPriceAndActionsWithTheVenueId`; mientras `008`/`009` no existan, `clientActionsWaitForQuotesAndBookingsModules` | ✅ |
| CA-05 | `ownerOrProviderDoNotSeeClientActions` + `VenueDetailViewModelTest.sessionRoleDecidesTheViewer` | ✅ |
| CA-06 | `notFoundOffersToGoBack` | ✅ |
| CA-07 | `networkErrorOffersRetryWithoutTechnicalText` + `failureKeepsTheErrorAndRetryRecovers` | ✅ |
| CA-08 | `BetaAccessibilityTest.venueDetailIsAccessibleInBothThemes`: 3 tipos de usuario, carga, 404 y error; 320 dp/2.0, ambos temas | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `VenueDetailViewModelTest` 6 (incluye formato MXN) | ✅ |
| Integración | `VenueDetailRepositoryTest` 5: 200 con y sin sesión (Bearer), mínimo tolerante, unidad desconocida no se adivina, 404/500/timeout | ✅ |
| UI | `VenueDetailScreenTest` 9 | ✅ |
| Seguridad | Sin precio para visitantes; sin datos de cuenta en logs | ✅ |
| Regresión | Pipeline aislado completo: **BUILD SUCCESSFUL**; 76 suites / 442 pruebas, 0 fallos (+20) | ✅ |
| Humo | Contra producción tras el despliegue y el primer salón publicado | ⛔ |

## 4. PR

- [x] PR: [virgorent/VirGO_Movil#42](https://github.com/virgorent/VirGO_Movil/pull/42) (borrador, base `feat/022-registrar-salon`).
- [ ] Una revisión aprobada y checks requeridos en verde. No se fusiona antes del despliegue de backend.

## 5. Memoria

- [x] Entrada en `memoria.md`.
