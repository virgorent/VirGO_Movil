# Spec — 016 Invitados

| Campo | Valor |
|---|---|
| Módulo | `016-invitados` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion», 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Pantallas | D32, C31 |
| Backend | `GET/POST/PATCH/DELETE /events/:id/guests` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Lista de invitados del evento con estado de confirmación y totales. La invitación digital (C31) se comparte con la hoja de compartir de Android usando los datos reales del evento; no hay plantillas en el servidor.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[D32]** Lista `GET /api/v1/events/:id/guests` con nombre, teléfono, acompañantes, mesa y estado. |
| RF-02 | **[D32]** Estados del contrato: `confirmed` «Confirmado», `pending` «Pendiente», `declined` «No asiste»; uno desconocido se lee «Sin respuesta». |
| RF-03 | **[D32]** Totales: personas confirmadas (invitado + acompañantes), pendientes y no asisten. |
| RF-04 | **[D32]** Agregar (`POST`) y editar (`PATCH`) con nombre (≤ 100), teléfono (obligatorio, ≤ 16), acompañantes (≥ 0), mesa (opcional, ≤ 20) y estado. Eliminar (`DELETE`) con confirmación. |
| RF-05 | **[C31]** «Compartir invitación» abre la hoja de compartir de Android con un texto armado con el nombre, tipo y fecha del evento y el salón si hay. |
| RF-06 | Errores: campo junto al campo; `404` «Este evento ya no está disponible»; `401` cierra sesión; red/5xx con reintento. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño, claro/oscuro, campos con nombre persistente; textos en `strings.xml`. |
| RNF-02 | **Accesibilidad:** errores asociados a su campo; 48 dp; sin cortes a 320 dp/fuente 2.0. |
| RNF-03 | **Seguridad:** sin datos personales en logs; Bearer por `AuthInterceptor`. |
| RNF-04 | **Honestidad:** solo datos del servidor; nada de muestra. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** invitados en varios estados, **cuando** abre la lista, **entonces** ve sus estados y los totales correctos. |
| CA-02 | **Dado** el formulario, **cuando** agrega o edita, **entonces** se envía el cuerpo del contrato y la lista se actualiza. |
| CA-03 | **Dado** un teléfono vacío o de más de 16 caracteres, **cuando** guarda, **entonces** ve el error y no se hace la petición. |
| CA-04 | **Dado** que confirma eliminar, **cuando** el servidor responde `204`, **entonces** el invitado sale de la lista. |
| CA-05 | **Dado** las pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 5. Fuera de alcance

- Ajustar el evento al número de confirmados: el cliente lo hace al editar el evento.
- Plantillas de invitación y envío directo por WhatsApp o correo desde el servidor.

## 6. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`), `EventsController`; **aún no desplegado**. Cualquier usuario con sesión puede usarlo.

- `GET /events/:id/guests` → `{data: [{id, eventId, name, phone, companions, status, table?}]}`.
- `POST` y `PATCH /events/:id/guests/:guestId`: `{name ≤ 100, phone ≤ 16, companions ≥ 0, status ∈ {confirmed, pending, declined}, table? ≤ 20}` (el `PATCH` exige todo). `DELETE` → `204`.
