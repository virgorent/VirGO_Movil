# Excepción temporal · `030-cuenta-sesion` antes del cierre de `000`

## Aprobación humana

El 2026-11-25 el usuario aprobó el lote de specs del MVP (`030`, `006`, `008`,
`009`, `022`). Se le explicó que el Art. II.2 de la constitución impide ejecutar
sus tareas hasta que (1) `000-arquitectura-base` esté cerrado y (2) backend haya
entregado la URL y el JSON de los endpoints. Se propuso una excepción acotada a
`030`, cuyos endpoints ya están desplegados, sin tocar los demás specs ni la
constitución. Respondió: «Sí, registra excepción».

Se registra **antes** de implementar. No modifica la constitución ni se extiende
a otros módulos.

## Alcance

| Condición del Art. II.2 | `030` | Efecto de la excepción |
|---|---|---|
| `000` cerrado | ❌ 38/40 (T014 TalkBack y T036 pendientes) | **Se exceptúa** solo para `030` |
| URL y JSON de backend | ✅ `GET /api/v1/users/me` y `POST /api/v1/auth/logout` desplegados (spec backend `006` y `004`); campos verificados en el controlador | No se exceptúa: ya se cumple |

Tareas autorizadas: **T3001–T3008** de [`specs/030-cuenta-sesion/task.md`](../../specs/030-cuenta-sesion/task.md).

## Límites obligatorios

- Solo el alcance aprobado de `030`; ningún otro spec del lote se implementa por esta excepción.
  `006`, `008`, `009` y `022` siguen ⛔ hasta cerrar `000` y recibir sus contratos.
- Sin relajar gates: ktlint, detekt, lint, pruebas (unitarias, integración, UI,
  seguridad, mutación), Gitleaks, dependency review y CodeQL en verde.
- Sin dependencias, permisos ni secretos nuevos; sin release ni cambio de versión.
- PR con revisión obligatoria del usuario; no se autoaprueba. Se fusiona después
  de la beta `034` (#35), de la que depende el perfil de solo lectura.
- El humo contra producción (T3008) usa una cuenta desechable que crea el usuario;
  el agente no crea cuentas ni escribe contraseñas.

## Ampliación a `035-pulido-visual-beta` (2026-11-25)

El usuario aprobó el spec `035` y amplió esta excepción: «Apruebo y amplío».
Se registra antes de implementar. Alcance: tareas **T3501–T3509** de
[`specs/035-pulido-visual-beta/task.md`](../../specs/035-pulido-visual-beta/task.md).
`035` es solo visual: no consume endpoints, así que la segunda condición del
Art. II.2 no aplica. Rigen los mismos límites: sin relajar gates, sin
dependencias, permisos ni secretos nuevos, sin release, revisión obligatoria
del usuario y sin datos de muestra.

## Regularización

- Cerrar `000` (T014 TalkBack, T036) y registrar en su `check.md` que `030` se
  implementó bajo esta excepción.
- Al cerrar `000`, esta excepción deja de ser necesaria y se marca como regularizada en `memoria.md`.

## Ampliación a `022-propietario-registrar-salon` (2026-11-25)

Se explicó al usuario que `022` incumple las **dos** condiciones del Art. II.2:
`000` sigue abierto y los endpoints de salones del propietario no están
desplegados (viven en `VirGO_Backend` PR #37, sin fusionar). Se propuso
implementar contra el backend corriendo en local con el JSON real de ese PR.
Respondió: «Sí, amplío y dale». Se registra antes de implementar.

| Condición del Art. II.2 | `022` | Efecto |
|---|---|---|
| `000` cerrado | ❌ | **Se exceptúa** |
| URL y JSON de backend | ⚠️ JSON verificado en el código de `feat/api-mobile-priority` (`7cf6124`) y ejecutado en local; **sin URL desplegada** | **Se exceptúa la URL**; el JSON se toma del código y de respuestas reales en local |

Endpoints: `POST /api/v1/venues`, `GET /api/v1/venues/mine`,
`POST /api/v1/venues/{id}/publish`.

Límites adicionales:

- Las pruebas usan MockWebServer con cuerpos copiados de respuestas reales del
  backend local; el humo contra producción queda **pendiente** hasta el despliegue.
- Si el contrato cambia al fusionar o desplegar, se ajusta en la misma rama y se
  registra en `memoria.md` de ambos proyectos.
- El PR no se fusiona a `main` antes de que backend despliegue esos endpoints.
- Mismos límites que arriba: gates completos, sin dependencias, permisos ni
  secretos nuevos, sin release, revisión obligatoria del usuario y sin datos de muestra.

## Ampliación a `006-detalle-salon` (2026-11-25)

Tras entregar `022`, se propuso seguir con `006` bajo la misma excepción y el
usuario respondió: «Sí, sigue con 006». Se registra antes de implementar.

| Condición del Art. II.2 | `006` | Efecto |
|---|---|---|
| `000` cerrado | ❌ | **Se exceptúa** |
| URL y JSON de backend | ⚠️ `GET /api/v1/venues/:id` verificado en el código de `feat/api-mobile-priority` (`7cf6124`) y ejecutado en local; **sin URL desplegada** | **Se exceptúa la URL** |

Tareas autorizadas: **T601–T605**; T606 (humo en producción) espera el
despliegue. Rigen los mismos límites que en `022`: MockWebServer con cuerpos
reales del backend local, sin fusionar antes del despliegue, gates completos,
sin dependencias, permisos ni secretos nuevos, sin release y sin datos de muestra.

## Ampliación a `009-reservar-pagar` (2026-11-25)

Tras entregar `006`, se propuso seguir con `009` bajo la misma excepción y el
usuario respondió: «Sí, sigue con 009». Se registra antes de implementar.

| Condición del Art. II.2 | `009` | Efecto |
|---|---|---|
| `000` cerrado | ❌ | **Se exceptúa** |
| URL y JSON de backend | ⚠️ `POST /bookings`, `GET /bookings` y `GET /bookings/:id` verificados en el código de `feat/api-mobile-priority` (`7cf6124`); **sin URL desplegada** | **Se exceptúa la URL** |

Tareas autorizadas: **T901–T907**; T908 (humo en producción) espera el
despliegue. Mismos límites: sin cobro ni datos de pago, MockWebServer con la
forma real del backend, sin fusionar antes del despliegue, gates completos y
sin dependencias, permisos ni secretos nuevos.

## Ampliación a `008-cotizaciones-cliente` (2026-11-25)

Tras entregar `009`, se preguntó si seguir con `008` y el usuario respondió:
«hazlos todos de una vez». Se registra antes de cerrar la implementación.

| Condición del Art. II.2 | `008` | Efecto |
|---|---|---|
| `000` cerrado | ❌ | **Se exceptúa** |
| URL y JSON de backend | ⚠️ `POST /quotes`, `GET /quotes` y `GET /quotes/:id` verificados en el código de `feat/api-mobile-priority` (`7cf6124`) más el PR #41 de backend; **sin URL desplegada** | **Se exceptúa la URL** |

Tareas autorizadas: **T801–T807**; T808 (humo en producción) espera el
despliegue. Mismos límites: MockWebServer con la forma real del backend, sin
fusionar antes del despliegue, gates completos y sin dependencias, permisos ni
secretos nuevos. La respuesta del propietario (P-02) no entra en esta ampliación.

## Aprobación del resto de specs (2026-11-25)

Tras entregar `008`, el usuario pidió los 20 specs restantes: «hazlos todos de una
vez tienes mi aprobacion». Se registra como aprobación de cada spec reescrito a la
v2.0.0 contra el contrato real del backend y como ampliación de esta excepción a
sus tareas de implementación. Cada spec deja aquí su línea antes de implementarse.

Límites, además de los anteriores: solo pantallas con endpoint real (sin datos de
muestra), sin manejo de tarjetas ni cobros, y los specs sin backend (`012`, `013`)
no se implementan.

| Spec | Tareas autorizadas | Contrato |
|---|---|---|
| `024-propietario-solicitudes` | T2401–T2404 | `GET /quotes`, `GET /quotes/:id`, `POST /quotes/:id/response` (PR #41) |
| `007-crear-evento` | T701–T703 | `POST /events`, `PATCH /events/:id` |
| `014-mi-evento` | T1401–T1402 | `GET /events`, `GET /events/:id` |
| `016-invitados` | T1601–T1603 | `GET/POST/PATCH/DELETE /events/:id/guests` |
| `017-pendientes` | T1701–T1703 | `GET/POST/PATCH/DELETE /events/:id/tasks` |
