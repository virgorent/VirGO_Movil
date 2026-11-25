# Spec — 024 Solicitudes de cotización del propietario

| Campo | Valor |
|---|---|
| Módulo | `024-propietario-solicitudes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion», 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Propietario |
| Fuente | `Checklist VirGo.md` (P7, P8), backend `060`, `061`, `062` (PR #41) |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente ya pide cotizaciones (`008`), pero sin esta pantalla nadie las responde
desde la app y nunca llegan a «Respondida». El propietario ve las solicitudes de
sus salones y responde con un total, un anticipo opcional y una nota.

La v1.0.0 incluía conceptos editables y «Mensajes» (P16). El backend solo acepta
un total, anticipo y mensaje, y las conversaciones son comunes a todos los roles:
P16 pasa a `020-comunicacion`.

## 2. Historias

| ID | Historia |
|---|---|
| HP-01 | Como propietario, quiero ver las solicitudes nuevas de mis salones para responderlas a tiempo. |
| HP-02 | Como propietario, quiero responder con un total, un anticipo y una nota para que el cliente pueda reservar. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[P7]** «Solicitudes» del espacio del propietario lista `GET /api/v1/quotes` con pestañas **Nuevas** (`sent`), **Respondidas** (`answered`) y **Todas**, más reciente primero. |
| RF-02 | **[P7]** Cada fila: salón, cliente (`clientName`), fecha, invitados y estado. Vacío, carga y error con reintento; tirar para actualizar. |
| RF-03 | **[P8]** El detalle (`GET /api/v1/quotes/:id`) muestra la solicitud y el mensaje del cliente. |
| RF-04 | **[P8]** Con estado `sent` aparece el formulario: total en MXN (obligatorio, > 0), anticipo (opcional, ≥ 0 y ≤ total) y nota para el cliente (opcional, ≤ 2000). |
| RF-05 | **[P8]** «Enviar al cliente» valida, envía `POST /api/v1/quotes/:id/response` y al `200` muestra la respuesta y la cotización como «Respondida». Sin envíos dobles. |
| RF-06 | **[P8]** Errores: `400` «Revisa el total»; `404` «Esta solicitud ya no está disponible»; `409` «Esta solicitud ya fue respondida» y se recarga; `403` «Solo el propietario del salón puede responder»; `401` cierra sesión; red/5xx con reintento conservando lo capturado. |
| RF-07 | Con otro estado (respondida, aceptada, rechazada, vencida o desconocido) el detalle es de solo lectura y muestra la respuesta enviada si existe. |
| RF-08 | Textos en `strings.xml`; montos en MXN y fechas en formato local. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño, claro/oscuro, campos con nombre persistente. |
| RNF-02 | **Accesibilidad:** errores asociados a su campo; 48 dp; sin cortes a 320 dp/fuente 2.0. |
| RNF-03 | **Seguridad:** ni montos ni mensajes en logs; Bearer por `AuthInterceptor`. |
| RNF-04 | **Honestidad:** sin conceptos ni precios inventados; solo lo que acepta el servidor. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** solicitudes en varios estados, **cuando** el propietario abre «Solicitudes», **entonces** las pestañas filtran por estado real y «Todas» las muestra todas. |
| CA-02 | **Dado** una solicitud `sent`, **cuando** responde con total y nota, **entonces** se hace `POST /quotes/:id/response` con el cuerpo del contrato y queda «Respondida». |
| CA-03 | **Dado** un total vacío o ≤ 0, o un anticipo mayor que el total, **cuando** intenta enviar, **entonces** ve el error en el campo y no se hace la petición. |
| CA-04 | **Dado** un `409`, **cuando** llega, **entonces** se avisa que ya fue respondida y se recarga el detalle. |
| CA-05 | **Dado** una solicitud ya respondida, **cuando** abre el detalle, **entonces** ve la respuesta enviada sin formulario. |
| CA-06 | **Dado** las pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 6. Fuera de alcance

- Conceptos, cantidades y precios por línea (v1.0.0): el backend solo acepta un total.
- Notificación al cliente (no hay push en el MVP).
- «Mensajes» (P16): pasa a `020-comunicacion`.

## 7. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`) más el PR #41; **aún no desplegado**.

- `GET /api/v1/quotes` devuelve `{data: [Quote]}` con las cotizaciones del usuario y las dirigidas a sus salones; `GET /quotes/:id` igual para una.
- `POST /api/v1/quotes/:id/response` (rol `owner`, PR #41): `{total > 0, deposit?, message? ≤ 2000}`. Solo desde `sent`; otro estado → `409`. Salón ajeno → `404`. Respuesta `200` con la cotización en `answered` y `response {total, deposit, message}`.
- El servidor no valida `deposit ≤ total`; la app lo valida.
