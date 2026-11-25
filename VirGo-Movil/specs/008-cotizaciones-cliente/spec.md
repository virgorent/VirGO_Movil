# Spec — 008 Cotizaciones del cliente

| Campo | Valor |
|---|---|
| Módulo | `008-cotizaciones-cliente` |
| Versión | 2.0.0 |
| Estado | Propuesto para aprobación (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C21, C22, C7), backend `008-quotes-create`, `060`, `061` y `API-HANDOFF.md` §9.8 |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente que encontró un salón (`006`) necesita pedirle una cotización y seguir
su estado. Este módulo conecta las tres pantallas con el backend real:
**solicitar** (`POST /api/v1/quotes`), **listar** (`GET /api/v1/quotes`) y
**ver el detalle** (`GET /api/v1/quotes/:id`).

La v1.0.0 incluía respuestas automáticas «de ejemplo», notificaciones y
cotizaciones a proveedores. En el MVP las cotizaciones son solo a **salones**, el
estado lo decide el servidor y nada se simula.

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como cliente, quiero pedir una cotización a un salón con la fecha, tipo de evento e invitados. |
| HT-02 | Como cliente, quiero ver mis solicitudes y su estado real para saber cuáles ya respondieron. |
| HT-03 | Como cliente, quiero abrir una cotización respondida y reservar desde ahí. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C21]** Se abre desde «Solicitar cotización» en `006` con el `venueId`; muestra el nombre del salón (solo lectura). |
| RF-02 | **[C21]** Campos: tipo de evento (Boda, XV años, Cumpleaños, Corporativo, Otro), fecha del evento (selector; no se permiten fechas pasadas), invitados (entero ≥ 1) y mensaje opcional (≤ 500 caracteres). |
| RF-03 | **[C21]** Servicios de interés: selección múltiple con los `services` del salón si el servidor los envía; si no hay, la sección no aparece. |
| RF-04 | **[C21]** Si los invitados superan la capacidad conocida del salón (> 0), se avisa sin bloquear el envío. |
| RF-05 | **[C21]** «Enviar solicitud» valida en el cliente, envía `POST /api/v1/quotes` con `target {type:"venue", id}` y, ante `201`, muestra «Solicitud enviada» y abre «Mis cotizaciones». El botón se desactiva mientras envía (sin envíos dobles). |
| RF-06 | **[C21]** Errores: `400` muestra los mensajes de campo de Problem Details en español; `404` «Este salón ya no está disponible»; `401` cierra sesión (`030`); red/5xx «No pudimos enviar tu solicitud» con reintento, conservando lo capturado. |
| RF-07 | **[C22]** «Mis cotizaciones» lista `GET /api/v1/quotes`: salón, fecha del evento, invitados y estado. Orden: más reciente primero. |
| RF-08 | **[C22]** Estados mostrados tal como los envía el servidor: `sent` → «Enviada», `responded` → «Respondida», `accepted` → «Aceptada», `rejected` → «Rechazada», `expired` → «Vencida». Un estado desconocido se muestra como «En revisión» sin fallar. |
| RF-09 | **[C22]** Estado vacío: «Aún no has pedido cotizaciones» con botón al catálogo. Estados de carga y error con reintento. Se puede actualizar tirando hacia abajo. |
| RF-10 | **[C7]** El detalle (`GET /api/v1/quotes/:id`) muestra los datos de la solicitud. Si el servidor envía conceptos, subtotal, IVA, total o vigencia, se muestran tal cual; si no los envía, se lee «El salón aún no responde». |
| RF-11 | **[C7]** Con estado `responded`, el botón «Reservar» abre `009` con `venueId`, `quoteId` y la fecha. En otros estados no hay acción. |
| RF-12 | El acceso a «Mis cotizaciones» sustituye la sección «Cotizaciones» de muestra del espacio del cliente (beta `034`). |
| RF-13 | Textos en `strings.xml`; fechas en formato local y montos en MXN. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño, claro/oscuro, formulario con nombres de campo persistentes. |
| RNF-02 | **Accesibilidad:** errores asociados a su campo y anunciados; 48 dp; sin cortes a 320 dp/fuente 2.0. |
| RNF-03 | **Seguridad:** el mensaje del cliente no se registra en logs. |
| RNF-04 | **Honestidad de datos:** sin respuestas automáticas ni montos inventados. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un cliente en `006`, **cuando** toca «Solicitar cotización», llena los campos y envía, **entonces** se hace `POST /quotes` con el cuerpo del contrato y aparece en «Mis cotizaciones» como «Enviada». |
| CA-02 | **Dado** una fecha pasada o 0 invitados, **cuando** intenta enviar, **entonces** ve el error en el campo y no se hace la petición. |
| CA-03 | **Dado** un `400` con errores de campo, **cuando** llega, **entonces** se muestran junto a cada campo sin texto técnico. |
| CA-04 | **Dado** un fallo de red, **cuando** reintenta, **entonces** conserva lo capturado y no se duplica la solicitud. |
| CA-05 | **Dado** cotizaciones en varios estados, **cuando** abre la lista, **entonces** cada una muestra su estado real; uno desconocido se lee «En revisión». |
| CA-06 | **Dado** que no tiene cotizaciones, **cuando** abre la lista, **entonces** ve el estado vacío con acceso al catálogo. |
| CA-07 | **Dado** una cotización `responded` con total, **cuando** abre el detalle, **entonces** ve los montos del servidor y «Reservar» abre `009`. |
| CA-08 | **Dado** una cotización `sent`, **cuando** abre el detalle, **entonces** lee «El salón aún no responde» y no hay botón «Reservar». |
| CA-09 | **Dado** las tres pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 6. Seguridad y privacidad

- Bearer por `AuthInterceptor`; `401` delega en el cierre de sesión de `030`.
- No se registran mensaje, fecha ni invitados en logs.
- El cliente no envía `target.name` ni `clientName`: los resuelve el servidor.

## 7. Fuera de alcance

- Cotizar a proveedores, notificaciones push, chat «Pedir cambios».
- Aceptar o rechazar una cotización (`063`/`064`): en el MVP reservar desde la cotización equivale a aceptarla (P-03).
- Respuesta del propietario (`062`): ver P-02.

## 8. Supuestos, dependencias y preguntas abiertas

**Endpoints que consume:**

| Endpoint | Spec backend | Estado observado (2026-11-25) |
|---|---|---|
| `POST /api/v1/quotes` | `008-quotes-create` | Implementación local; declarado en OpenAPI; sin probar |
| `GET /api/v1/quotes` | `060-get-quotes` (Borrador) | ❌ no desplegado |
| `GET /api/v1/quotes/:id` | `061-get-quotes-id` (Borrador) | ❌ no desplegado |

**Dependencias del móvil:** `006` (punto de entrada), `030` (manejo de 401); `000` cerrado o excepción.

| ID | Pregunta | Quién | Propuesta |
|---|---|---|---|
| P-01 | El contrato exige `eventId`, pero «Crear evento» (`007`) no está en el MVP. | Backend | `eventId` **opcional** en el MVP; la app no lo envía. |
| P-02 | Sin la respuesta del propietario (`POST /quotes/:id/response`, `062`) ninguna cotización pasa a «Respondida». | Usuario + backend | Añadir al lote un spec mínimo «propietario responde cotización» (lista de solicitudes + monto total), o aceptar que en el MVP la reserva directa (`009`) no requiere cotización. |
| P-03 | ¿Reservar desde una cotización la marca `accepted` en el servidor? | Backend | Sí, dentro de `POST /bookings` con `quoteId`. |
| P-04 | Forma del JSON de lista/detalle y nombres de estado. | Backend | Los de RF-08; la app tolera estados nuevos. |
