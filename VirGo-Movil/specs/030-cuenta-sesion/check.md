# Check — 030 Cuenta y sesión

| Campo | Valor |
|---|---|
| Módulo | `030-cuenta-sesion` |
| Estado del spec | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Revisó | — |
| Fecha | 2026-11-24 |
| Constitución | v1.2.0 |

Implementado el 2026-11-25 bajo la excepción [`excepcion-030-sin-cierre-000.md`](../../docs/diseno/excepcion-030-sin-cierre-000.md).
Los criterios que exigen producción (CA-06, CA-10) siguen pendientes del humo con cuenta desechable.

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `AccountProfileViewModelTest.matchingRolePublishesServerProfile` + `AccountProfileScreenTest.serverProfileShowsPhoneAndVerification` | ✅ |
| CA-02 | `AccountProfileScreenTest.profileIsReadOnly` (sin `SetText`, «Guardar» ni «Editar») | ✅ |
| CA-03 | `networkFailureKeepsSessionAndMarksDataOutdatedThenRetryRecovers` + `outdatedDataOffersRetry` + `loadingShowsSessionIdentityWithoutInventingPhoneOrVerification` | ✅ |
| CA-04 | `unauthorizedRevokesSession`: token borrado y motivo `REVOKED` | ✅ |
| CA-05 | `notFoundMeansAccountUnavailable`, `differentRoleClosesSessionWithoutPublishingProfile`, `unknownRoleIsRejected` | ✅ |
| CA-06 | Humo: logout 204/200 y token anterior 401 | ⏳ |
| CA-07 | `failedLogoutStillClearsTokenAndReportsUnconfirmed`, `slowLogoutTimesOutAndClearsLocally` | ✅ |
| CA-08 | `unauthorizedLogoutMeansAlreadyRevokedAndShowsNoError` | ✅ |
| CA-09 | `expiryShowsSessionExpiredNoticeOnceAndCanBeConsumed` | ✅ |
| CA-10 | Humo con cuenta desechable: login → perfil → logout → token anterior 401 | ⏳ |
| CA-11 | Revisión de logs y pruebas de seguridad: sin tokens, correos ni teléfonos | ⏳ |
| CA-12 | `AccountProfileScreenTest` a 320 dp/fuente 2.0 en claro y oscuro; textos en `account_strings.xml` | 🔄 capturas y aceptación visual pendientes |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales del pipeline: 63 suites / 362 pruebas, 0 fallos (2026-11-25) | ✅ |
| Integración | Totales: —; MockWebServer con 200/204/401/404/500/timeout | ⏳ |
| UI | Totales: —; perfil, solo lectura, sin red, aviso único | ⏳ |
| Seguridad | Token solo en `Authorization`; sin datos personales en logs; borrado local aun con fallo | ⏳ |
| Mutación | Puntaje: — %; `GetProfileUseCase`, `LogoutUseCase`, validación de rol | ⏳ |
| Humo | APK contra producción con cuenta desechable | ⏳ |

## 3. Pipeline

| Comprobación | Evidencia | Estado |
|---|---|---|
| `ktlintCheck` | — | ⏳ |
| `detekt` | — | ⏳ |
| `lint` | — | ⏳ |
| `testDebugUnitTest` | — | ⏳ |
| `pitest` | — | ⏳ |
| `assembleDebug` | — | ⏳ |
| Gitleaks / dependency review / CodeQL | — | ⏳ |

## 4. PR y release

- [ ] PR: enlace —
- [ ] Una revisión aprobada.
- [ ] Checks requeridos en verde.
- [ ] Release: no aplica en este módulo.

## 5. Memoria y aprendizaje

- [ ] Entrada en `memoria.md` con la decisión de logout y la diferencia
  `200/204`.
- [ ] Deuda registrada si el OpenAPI no se alinea.
- [ ] Confirmación de que `000` sigue cerrado o de la excepción aplicada.

## 6. Límites de este documento

- No acredita paridad visual ni aceptación de los tres roles.
- No cierra `021-favoritos-perfil`, `000-arquitectura-base` ni ningún otro spec.
- No autoriza release, despliegue ni cambios de contrato en el backend.
- Las capturas y la evidencia se generan solo después de implementar y ejecutar
  las pruebas, no durante la redacción del spec.
