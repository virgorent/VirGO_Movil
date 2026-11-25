# Check — 022 Propietario: registrar salón

| Campo | Valor |
|---|---|
| Módulo | `022-propietario-registrar-salon` |
| Estado del spec | Aprobado (lote MVP, 2026-11-25) · v2.1.0 ajustada al contrato · implementado bajo excepción; humo pendiente del despliegue |
| Revisó | Claude Code (evidencia técnica); aceptación: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [x] Excepción aprobada y registrada antes de implementar («Sí, amplío y dale»; `docs/diseno/excepcion-030-sin-cierre-000.md`).
- [x] P-01–P-05 resueltas con el contrato real de `VirGO_Backend` PR #37 (`7cf6124`).
- [ ] Backend desplegado (`mine`, `POST`/`PATCH /venues`, `publish`) y registro con rol `owner` (`VirGO_Backend` PR #42).

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `OwnerVenuesScreenTest.emptyListOffersToRegister` | ✅ |
| CA-02 | `VenueDraftValidatorTest` (límites 19/20/2000/2001, zona) + `VenueWizardViewModelTest.shortDescriptionOrMissingZoneBlocksTheFirstStep` + `OwnerVenuesScreenTest.fieldErrorsAreShownAndAnnouncedNextToTheirField` | ✅ |
| CA-03 | `VenueWizardViewModelTest.firstSaveCreatesOnceAndLaterSavesPatch` + `OwnerVenuesRepositoryImplTest.createSendsTheServerContractOnlyOnce` | ✅ |
| CA-04 | `firstSaveCreatesOnceAndLaterSavesPatch` (estado `draft`) + `listShowsStatusAndOpensAVenue`; en local el borrador no aparece en `GET /venues` | ✅ |
| CA-05 | `publishSavesThenPublishes` + `publishedOutcomeIsAnnounced`; en local el salón publicado aparece en `GET /venues`. Humo en producción: ⏳ T2208 | 🔄 |
| CA-06 | `networkFailureKeepsDataAndRetryDoesNotDuplicate`, `createFailureKeepsNoIdSoRetryCreates` + `unansweredCreateRecoversTheVenueInsteadOfDuplicating` (ADR-04) | ✅ |
| CA-07 | `resumingADraftPatchesWithoutServicesUntilTheyChange` + `stateSurvivesRecreationAndStartIsIgnoredForTheSameKey` | ✅ |
| CA-08 | `BetaAccessibilityTest.ownerVenuesAreAccessibleInBothThemes`: lista (4 estados) y 3 pasos con errores, 320 dp/2.0, ambos temas. Encontró un defecto real (el resumen cortaba textos largos), corregido | ✅ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `VenueDraftValidatorTest` 10, `OwnerVenueUseCasesTest` 4, `VenueWizardViewModelTest` 14, `OwnerVenuesViewModelTest` 3 | ✅ |
| Integración | `OwnerVenuesRepositoryImplTest` 9 (MockWebServer con cuerpos reales del backend local: 201, 200, 400 por campo, 401, 403, 404, 500, timeout con recuperación) | ✅ |
| UI | `OwnerVenuesScreenTest` 5 + barrido de accesibilidad | ✅ |
| Seguridad | La petición no envía `status`, `ownerId`, `rating`, `verified`, `photos` ni `location` (`createSendsTheServerContractOnlyOnce`); sin datos del salón en logs | ✅ |
| Regresión | Pipeline aislado `ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug`: **BUILD SUCCESSFUL in 4m 10s**; 73 suites / 422 pruebas, 0 fallos (+46) | ✅ |
| Humo local | Backend `feat/api-mobile-priority` en local: crear 201 → editar 200 → `mine` borrador → publicar 200 → aparece en catálogo; cliente 403; sin sesión 401 | ✅ |
| Humo producción | Requiere despliegue de backend y cuenta `owner` (PR #42) | ⛔ |

## 4. PR

- [ ] PR: enlace —
- [ ] Una revisión aprobada y checks requeridos en verde. No se fusiona antes del despliegue de backend (excepción).

## 5. Memoria

- [x] Entrada en `memoria.md` (2026-11-25 · `022` implementado bajo excepción).
