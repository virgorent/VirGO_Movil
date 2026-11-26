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

## Regularización

- Cerrar `000` (T014 TalkBack, T036) y registrar en su `check.md` que `030` se
  implementó bajo esta excepción.
- Al cerrar `000`, esta excepción deja de ser necesaria y se marca como regularizada en `memoria.md`.
