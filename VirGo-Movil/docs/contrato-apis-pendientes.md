# Contrato de APIs pendientes (cliente y propietario)

| Campo | Valor |
|---|---|
| Fecha | 2026-11-27 |
| Para | Karasu y Rafita (APIs), Esaú (base de datos) |
| De | App móvil (`ccabrera-dev8`) |
| Base | `https://…/api/v1`, Bearer JWT, errores en Problem Details |

La app ya está programada contra este contrato. Si cambian un nombre de campo o una ruta, avisen y se ajusta
la app; si lo dejan igual, solo se enciende. Los campos y límites salen de los esquemas de `db/schema/*.json` y de
los specs del backend que ya existen (los números entre paréntesis).

**Reglas para todos los endpoints**

- `401` sin token o con token vencido; `403` si el rol no corresponde; `404` si el recurso no existe **o es de
  otra persona** (no revelar cuál de los dos).
- `400` con `errors` por campo para datos inválidos; nunca `500` por datos del usuario.
- Fechas `yyyy-MM-dd`; fecha-hora ISO 8601 en UTC; montos como número en MXN.
- Cada consulta filtra por el usuario del token. El rol se revisa con `[Authorize(Roles = …)]`.

## Prioridad

| Prioridad | Qué | Por qué |
|---|---|---|
| **P0** | Desplegar lo que ya está en el #39 | Sin esto ninguna pantalla nueva se ve en producción |
| **P1** | 1. Bloqueos con `id` · 2. Asistente IA · 3. Mensajes · 4. Presupuesto y pagos del evento | Pantallas ya hechas en la app |
| **P2** | 5. Reseñas · 6. Precios y montajes del salón · 7. Favoritos | Pantallas ya hechas en la app |
| **P3** | 8. Subir fotos · 9. Ideas con IA | Requieren S3 o modelo de IA |

---

## 1. Disponibilidad: agregar `id` del bloqueo (cambio pequeño)

`GET /venues/:id/availability?from=yyyy-MM-dd&to=yyyy-MM-dd` (031) ya existe. Solo falta mandar el `id` de cada
bloqueo para que el propietario pueda desbloquear con `DELETE /venues/:id/blocks/:blockId` (033).

```json
{ "data": [ { "id": "uuid", "date": "2027-05-25", "kind": "blocked", "reason": "maintenance", "note": "Pintura" } ] }
```

## 2. Asistente IA (093, 094) — rol `client`

`POST /assistant/messages` → `201`

```json
// petición
{ "message": "¿Cuántos meseros necesito?", "eventId": "uuid|null",
  "history": [ { "role": "user|assistant", "text": "…" } ] }
// respuesta
{ "id": "uuid", "reply": "Para 120 invitados, unos 4 meseros.", "uncertain": false, "createdAt": "2026-11-27T20:00:00Z" }
```

- `message` de 1 a 500 caracteres; `history` como máximo 10 elementos (el servidor puede ignorarlo si guarda su
  propio historial de 30 días, como dice 094).
- `uncertain: true` cuando el modelo no tiene datos suficientes; la app agrega «No estoy seguro…».
- `429` si se excede el límite de uso. Mientras no exista, `404` o `501` (la app muestra «todavía no está conectado»).
- La llave del modelo vive **solo** en el servidor (SSM). No mandar datos personales al modelo: solo tipo de evento,
  invitados, presupuesto y fecha del evento si hace falta.

## 3. Mensajes con el salón (084–088) — roles `client` y `owner`

| Método y ruta | Respuesta |
|---|---|
| `GET /conversations` | `200` `{ "data": [Conversation] }`, más reciente primero |
| `POST /conversations` `{ "target": { "type": "venue", "id": "uuid" }, "text": "…" }` | `201` la nueva, o `200` la existente (una por cliente y salón) |
| `GET /conversations/:id/messages?before=ISO&limit=30` | `200` `{ "data": [Message] }`, más antiguo primero |
| `POST /conversations/:id/messages` `{ "text": "…" }` | `201` `Message` |

```json
// Conversation
{ "id": "uuid", "target": { "type": "venue", "id": "uuid", "name": "Jardín Los Encinos" },
  "clientName": "Ana Torres", "lastMessage": "¿Tienen fecha?", "lastAt": "2026-11-27T20:00:00Z", "unread": 1 }
// Message
{ "id": "uuid", "text": "Hola", "sentAt": "2026-11-27T20:00:00Z", "mine": true }
```

- `text` de 1 a 1000 caracteres (el esquema permite 2000; 1500 debe dar `400`).
- El cliente solo ve las suyas; el propietario, las de sus salones. `mine` lo calcula el servidor con el token.
- `lastMessage` recortado a 200 caracteres (esquema `conversations`).

## 4. Presupuesto y pagos del evento — rol `client`

Se extiende `PATCH /events/:id` y `GET /events/:id` (ya existen) con dos campos del esquema `events`:

```json
{ "budgetPlan": { "Salón": 60000, "Banquete": 45000, "Decoración": 18000, "Música": 12000, "Otros": 15000 },
  "externalPayments": [
    { "id": "uuid", "concept": "Anticipo DJ", "amount": 3000, "status": "completed", "paidAt": "2026-11-20T00:00:00Z" } ] }
```

- `budgetPlan`: categoría → monto ≥ 0; máximo 12 categorías de hasta 40 caracteres.
- `externalPayments`: la app manda la lista completa; `id` lo genera la app (UUID); `concept` ≤ 150;
  `amount` > 0; `status` en `completed|pending|failed|refunded`.
- `GET /events/:id` debe devolver ambos campos (vacíos si no hay).

## 5. Reseñas (072, 074, 075)

| Método y ruta | Rol | Respuesta |
|---|---|---|
| `GET /venues/:id/reviews` | público | `200` `{ "rating": 4.6, "count": 12, "data": [Review] }` |
| `POST /reviews` `{ "targetType": "venue", "targetId": "uuid", "bookingId": "uuid", "stars": 5, "text": "…" }` | `client` | `201` `Review`; `409` si ya calificó; `403` si la reservación no es suya |
| `POST /reviews/:id/reply` `{ "text": "…" }` | `owner` del salón | `200` `Review` (crea o edita la respuesta) |

```json
// Review
{ "id": "uuid", "authorName": "Ana T.", "stars": 5, "text": "Todo excelente", "createdAt": "…",
  "reply": { "text": "¡Gracias!", "at": "…" } }
```

- `stars` 1–5; `text` ≤ 1000; guardar la reseña actualiza `venues.rating` (074 RF-04).

## 6. Precios y montajes del salón — rol `owner`

`PATCH /venues/:id` (ya existe) acepta además, según el esquema `venues`:

```json
{ "price": { "base": 3500, "unit": "hour", "minHours": 5, "weekendPct": 15, "highSeasonPct": 20, "highSeasonMonths": [11, 12] },
  "setups": { "banquet": 200, "auditorium": 250, "cocktail": 300 },
  "floorPlanUrl": "https://…" }
```

- `weekendPct` y `highSeasonPct` de 0 a 100; meses del 1 al 12 sin repetir; montajes ≥ 0.
- `GET /venues/mine` y `GET /venues/:id` devuelven esos campos.

## 7. Favoritos (081–083) — rol `client`

| Método y ruta | Respuesta |
|---|---|
| `GET /favorites` | `200` `{ "data": [ { "targetType": "venue", "targetId": "uuid", "name": "…", "photoUrl": "…", "rating": 4.6 } ] }` |
| `POST /favorites` (mismo cuerpo) | `201`, o `200` si ya existía |
| `DELETE /favorites/:targetType/:targetId` | `204`, o `404` si no existía |

## 8. Subir fotos — roles `owner` y `client`

`POST /uploads` (`multipart/form-data`: `file`, `purpose` = `venue_photo|floor_plan`) → `201 { "url": "https://…" }`.

- JPG o PNG, máximo 5 MB, revisar el tipo real del archivo (no solo la extensión).
- Guardar en el S3 que ya existe y servir por CloudFront; la URL se guarda después con `PATCH /venues/:id`
  (`photos` o `floorPlanUrl`).

## 9. Ideas de decoración con IA (nuevo) — rol `client`

`POST /ai/ideas` `{ "eventType": "Boda", "guests": 120, "budget": 150000 }` →
`200 { "data": [ { "title": "Centros de mesa con eucalipto", "description": "…", "cost": 6500 } ] }`

- Máximo 6 ideas; `cost` estimado en MXN. Mismas reglas de llave, `429` y `501` que el asistente.

---

## Lo que la app calcula sola (no necesita API)

- Inicio y calendario del propietario: con `venues/mine`, `quotes`, `bookings` y `availability`.
- Mesas y personal sugerido: reglas fijas del prototipo.
- Pagos recibidos del propietario: anticipos de sus reservaciones (`GET /bookings`).
- Estadísticas del propietario: solicitudes, reservaciones y conversión por mes con `quotes` y `bookings`
  (visitas fuera, como dice 078).
- Compartir: el texto de la invitación se comparte con el menú del teléfono.
