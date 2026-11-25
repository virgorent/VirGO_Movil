# Spec — 030 Cuenta y sesión

| Campo | Valor |
|---|---|
| Módulo | `030-cuenta-sesion` |
| Versión | 0.2.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-24 |
| Constitución | v1.2.0 |
| Rol | Cliente, Propietario y Proveedor |
| Fuente | Auditoría móvil–backend y specs `003-auth-login`, `004-auth-logout`, `006-users-me` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

La app ya inicia sesión contra producción y conserva el token cifrado, pero el
perfil autenticado sigue siendo una pantalla de muestra y el cierre de sesión no
revoca nada en el servidor. Este módulo conecta la cuenta real: lee el perfil con
`GET /api/v1/users/me`, cierra la sesión con `POST /api/v1/auth/logout` y trata
como inválida cualquier sesión revocada, vencida o con un rol que no coincide.

No sustituye los specs `002-registro`, `003-iniciar-sesion` ni `021-favoritos-perfil`.
Solo cubre la identidad de la cuenta y el ciclo de vida de la sesión autenticada.
Se propone delimitar con `021` el perfil autenticado antes de aprobar ambos:
este borrador no cambia la autoridad ni el alcance aprobado de otros specs.

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como persona autenticada, quiero ver los datos reales de mi cuenta para confirmar que la app y el servidor coinciden. |
| HT-02 | Como persona autenticada, quiero cerrar sesión y que el servidor revoque el token para que nadie más lo use. |
| HT-03 | Como persona cuya sesión venció o fue revocada, quiero que la app me lo explique y me pida iniciar sesión de nuevo, sin dejarme en un estado falso. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C39]** Al abrir Perfil con sesión, la app consulta `GET /api/v1/users/me` con el Bearer de la sesión y muestra `name`, `email`, `phone`, `role` e `isVerified`. |
| RF-02 | **[C39]** Mientras carga, muestra un estado de carga y la identidad ya validada de la sesión (`name`, `email`, `role`); no inventa `phone` ni `isVerified`. |
| RF-03 | **[C39]** El perfil autenticado es de solo lectura: no permite editar nombre, correo, teléfono ni verificación, ni guarda cambios locales. La hoja H06 no se ofrece en este perfil. |
| RF-04 | **[C39]** Si la consulta falla por red, timeout o 5xx, muestra la identidad de la sesión con el aviso de que no se pudo actualizar desde el servidor y un botón Reintentar; `phone` e `isVerified` aparecen como no disponibles. |
| RF-05 | **[C39]** Un 401 en `GET /users/me` cierra la sesión local, borra el token y lleva al inicio de visitante o al login con el mensaje «Tu sesión expiró. Inicia sesión de nuevo.». |
| RF-06 | **[C39]** Un 404 en `GET /users/me` se trata como cuenta no disponible: cierra la sesión y explica que la cuenta ya no existe o no está disponible. |
| RF-07 | Si el rol del servidor no coincide con el rol validado de la sesión, o no es `client`, `owner` o `provider`, la app cierra la sesión y no abre el workspace. |
| RF-08 | Cerrar sesión envía `POST /api/v1/auth/logout` con el Bearer; un `204` o un `200` se considera revocación confirmada. |
| RF-09 | Después de cerrar sesión, la app borra el token cifrado y el estado local del workspace, y vuelve al inicio de visitante, incluso si la revocación falla. |
| RF-10 | Si el logout falla por red, timeout o 5xx, la app cierra la sesión local y avisa que no pudo confirmar la revocación en el servidor; no reintenta en segundo plano ni conserva el token. |
| RF-11 | Si el logout responde 401, se considera que la sesión ya no existía; se cierra localmente sin mostrar un error. |
| RF-12 | La sesión sigue expirando a los 15 minutos; al expirar se borra el token y se muestra el mismo aviso que un 401. No se usa el refresh token. |
| RF-13 | El perfil no persiste `phone` ni `isVerified`; solo viven en el estado de la pantalla y se pierden al cerrar la app. |
| RF-14 | El perfil real se identifica como «Datos de tu cuenta». Las secciones sin servicio siguen mostrando «Próximamente», como en la beta `034`. |
| RF-15 | Todos los textos visibles están en `strings.xml`; el perfil usa el sistema de diseño y cumple contraste AA y áreas táctiles de 48 dp. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** solo HTTPS en release, Bearer en el encabezado, sin tokens ni datos personales en logs, y borrado del token incluso cuando la revocación falla. |
| RNF-02 | **Rendimiento:** el perfil muestra primero la identidad de la sesión y no bloquea la navegación mientras llega la respuesta. |
| RNF-03 | **Accesibilidad:** contraste AA, áreas táctiles de 48 dp, texto escalable y descripciones en iconos con acción. |
| RNF-04 | **Diseño:** solo componentes y tokens de `:core:designsystem`; tema claro y oscuro; textos en `strings.xml` (Art. VII). |
| RNF-05 | **Trazabilidad:** cada RF se relaciona con un CA y una tarea de `task.md`; la evidencia vive en `check.md`. |
| RNF-06 | **Compatibilidad:** la implementación acepta `200` y `204` en logout mientras el OpenAPI publicado y el código del backend se alinean. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un token válido, **cuando** se abre Perfil, **entonces** se muestran `name`, `email`, `phone`, `role` e `isVerified` del servidor. |
| CA-02 | **Dado** el perfil autenticado, **cuando** se intenta editar un campo, **entonces** no hay edición ni guardado local, y H06 no está disponible. |
| CA-03 | **Dado** que no hay conexión, **cuando** se abre Perfil, **entonces** se ve la identidad de la sesión, un aviso de datos sin actualizar y Reintentar; no se inventan teléfono ni verificación. |
| CA-04 | **Dado** un 401 en `GET /users/me`, **cuando** llega la respuesta, **entonces** la sesión local se cierra, el token se borra y se explica el motivo en el inicio de visitante o login. |
| CA-05 | **Dado** un 404 o un rol distinto al de la sesión, **cuando** llega la respuesta, **entonces** la sesión se cierra y no se abre el workspace. |
| CA-06 | **Dado** un logout con `204` o `200`, **cuando** termina, **entonces** el token cifrado se borra y el token anterior responde 401 en el servidor. |
| CA-07 | **Dado** un logout sin red o con 5xx, **cuando** termina, **entonces** la sesión local se cierra igual y se avisa que la revocación no se pudo confirmar. |
| CA-08 | **Dado** un logout con 401, **cuando** termina, **entonces** la sesión local se cierra sin mostrar un error de revocación. |
| CA-09 | **Dado** que pasan 15 minutos, **cuando** expira el token, **entonces** la sesión se cierra y se muestra el mismo aviso que un 401. |
| CA-10 | **Dado** un recorrido real con una cuenta desechable, **cuando** se inicia sesión, se consulta el perfil y se cierra sesión, **entonces** el perfil coincide con el servidor y el token anterior queda revocado. |
| CA-11 | **Dado** el cambio, **cuando** se revisan los registros de la app, **entonces** no aparecen tokens, correos ni teléfonos. |
| CA-12 | **Dado** el perfil en tema claro y oscuro, **cuando** se compara con el sistema de diseño, **entonces** cumple contraste, espaciado y 48 dp, y los textos están en `strings.xml`. |

## 6. Fuera de alcance

- Editar el perfil, cambiar avatar, cambiar contraseña o borrar la cuenta. Aunque
  `DELETE /users/me` existe, no se consume en este módulo.
- Favoritos, ayuda, mensajes, notificaciones, pagos, reservas, IA y cualquier otra
  pantalla de muestra.
- Renovación silenciosa con refresh token, biometría o persistencia de `phone` e
  `isVerified`.
- Extraer un módulo `:feature:*`; la implementación vive en `:app` y `:core:*`,
  como en `005-explorar-salones`.
- Desplegar endpoints, corregir el OpenAPI del backend o publicar un release.
- Aceptación visual de los tres roles; eso sigue siendo una revisión aparte.

## 7. Dependencias

**Endpoints que consume:**

| Endpoint | Spec en el backend (`virgorent/VirGO_Backend`, `VirGo-API/specs`) | Estado observado |
|---|---|---|
| `GET /api/v1/users/me` | `006-users-me` | Desplegado; sin token responde 401; campos `id`, `name`, `email`, `phone`, `role`, `isVerified` |
| `POST /api/v1/auth/logout` | `004-auth-logout` | Desplegado; sin token responde 401; el controlador responde `204 NoContent` |
| `POST /api/v1/auth/login` | `003-auth-login` | Desplegado; fuera de alcance, ya implementado |

Advertencias de contrato:

1. El OpenAPI publicado declara `200` para logout, pero el controlador
   `AuthController.Logout` devuelve `NoContent()` (`204`). La app acepta ambos y
   se solicita al backend alinear el OpenAPI.
2. El OpenAPI publicado no declara esquemas de respuesta para `/users/me`. Los
   campos se tomaron del controlador y del spec `006-users-me`.
3. La sesión dura 15 minutos y no hay endpoint de refresh. Al vencer, se cierra;
   no se inventa una renovación.

## 8. Estado actual y límites

- Desde la beta `034` (PR #35) el perfil ya es de solo lectura con la identidad
  de la sesión validada (nombre, correo y rol), sin alias local ni «Guardar».
  Falta consultar `GET /users/me` para `phone` e `isVerified`.
- «Cerrar sesión» solo limpia el almacenamiento local; no llama al endpoint.
- Este módulo corrige únicamente esas dos cosas.
- Forma parte del lote MVP (ver `docs/diseno/mvp-contratos-backend.md`). La
  implementación empieza cuando `000-arquitectura-base` cierre (T014 y T036) o
  con una excepción aprobada, según el Art. II.2.
- La aprobación de este spec no autoriza por sí sola un release ni la fusión de
  otros PR.
