# Check — 021 Perfil: borrar cuenta (favoritos y ayuda, pendientes)

| Campo | Valor |
|---|---|
| Módulo | `021-favoritos-perfil` |
| Versión del spec | 2.0.0 |
| Estado del spec | Aprobado por el usuario (2026-11-26) |
| Revisó | — |

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `AccountProfileViewModelTest.deletedAccountClearsTheSessionAndTheToken`; `AccountRepositoryImplTest.deleteAccountSendsDeleteWithBearerAndAccepts204` | ✅ |
| CA-02 | `AccountProfileViewModelTest.cancellingTheConfirmationSendsNothing`; `AccountProfileScreenTest.confirmationExplainsWhatHappensAndCancelSendsNothing` | ✅ |
| CA-03 | `AccountProfileViewModelTest.networkFailureKeepsTheSessionAndShowsTheError`; `AccountProfileScreenTest.deleteFailureShowsRetryableError` | ✅ |
| CA-04 | `AccountProfileViewModelTest.unauthorizedDeleteClosesTheSessionAndSaysTheAccountWasNotDeleted` | ✅ |
| CA-05 | `ExploreViewModelTest.favoritesAreNotRequestedWhileTheBackendLacksTheEndpoint`; `ExploreScreenTest.venueCardsHaveNoFavoriteHeartWhileFavoritesAreOff` | ✅ |
| CA-06 | `AccountProfileViewModelTest.ownersCannotDeleteTheirAccountYet`; `AccountProfileScreenTest.ownerDoesNotSeeDeleteAction` | ✅ |

## 2. Pruebas

Corrida del 2026-11-26 sobre la punta del #51 con estos cambios: `ktlintCheck detekt testDebugUnitTest` **BUILD SUCCESSFUL**, 583 pruebas y 0 fallas en todo el proyecto.

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `AccountProfileViewModelTest` (13), incluye el doble envío | ✅ |
| Integración | `AccountRepositoryImplTest` (11): `DELETE`, Bearer, 204/401/500 y sin red | ✅ |
| UI | `AccountProfileScreenTest` (10, fuente 2.0 a 320 dp) y `ExploreScreenTest` (13) | ✅ |
| Seguridad | Tras 204 no queda token (`store.stored` nulo); sin llamadas a `/favorites` | ✅ |
| Accesibilidad | El foco inicial en «Cancelar» se pide en código; **falta** comprobarlo con TalkBack | ⏳ |
| Humo | Borrar una cuenta de prueba contra producción tras el despliegue | ⏳ |

## 3. PR y release

- [ ] PR: enlace —
- [ ] Una revisión aprobada.
- [ ] Aceptación visual de `ccabrera-dev8`.

## 4. Memoria y aprendizaje

- [ ] Entrada en `memoria.md`.
