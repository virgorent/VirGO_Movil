# Check — 030 Cuenta y sesión

| Campo | Valor |
|---|---|
| Módulo | `030-cuenta-sesion` |
| Estado del spec | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Revisó | — |
| Fecha | 2026-11-24 |
| Constitución | v1.2.0 |

Este `check.md` no contiene evidencia todavía: el módulo no se ha implementado.
No se marca ningún criterio como cumplido sin una ejecución real.

## 1. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Captura o prueba del perfil con `name`, `email`, `phone`, `role` e `isVerified` del servidor | ⏳ |
| CA-02 | Prueba de UI: campos de solo lectura y H06 ausente en el perfil autenticado | ⏳ |
| CA-03 | Captura o prueba sin red: identidad de la sesión, aviso y Reintentar; sin teléfono ni verificación inventados | ⏳ |
| CA-04 | Prueba de 401: token borrado y aviso en el inicio de visitante o login | ⏳ |
| CA-05 | Prueba de 404 y de rol distinto: sesión cerrada y workspace no abierto | ⏳ |
| CA-06 | Humo: logout 204/200 y token anterior 401 | ⏳ |
| CA-07 | Prueba de logout sin red/5xx: cierre local y aviso de revocación no confirmada | ⏳ |
| CA-08 | Prueba de logout 401: cierre local sin error de revocación | ⏳ |
| CA-09 | Prueba de expiración a los 15 minutos: cierre y aviso | ⏳ |
| CA-10 | Humo con cuenta desechable: login → perfil → logout → token anterior 401 | ⏳ |
| CA-11 | Revisión de logs y pruebas de seguridad: sin tokens, correos ni teléfonos | ⏳ |
| CA-12 | Capturas claro/oscuro y prueba de accesibilidad; textos en `strings.xml` | ⏳ |

## 2. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: — | ⏳ |
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
