# Spec — 009 Reservar (sin cobro en la app)

| Campo | Valor |
|---|---|
| Módulo | `009-reservar-pagar` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C8, C23), backend `009-bookings-create`, `066`, `067` y `API-HANDOFF.md` §9.9 |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente quiere apartar un salón. El backend ya crea reservaciones en estado
`pending_payment` (`POST /api/v1/bookings`): calcula el total desde el precio del
salón, genera el folio `VG-AAAA-NNNN` y fija un anticipo del 30 % si no se envía.
Los pagos, la confirmación y el bloqueo de la fecha **todavía no existen** en el
backend.

Por eso el MVP **reserva sin cobrar en la app**: el cliente confirma fecha y
horas, recibe su folio y ve claramente que la reservación está pendiente de
pago y que la fecha aún no está apartada. El nombre de la carpeta se conserva;
el pago con tarjeta o transferencia queda para un spec posterior (§7).

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como cliente, quiero reservar un salón para una fecha y número de horas, desde su detalle o desde una cotización respondida. |
| HT-02 | Como cliente, quiero recibir un folio y ver el total y el anticipo calculados por VirGo. |
| HT-03 | Como cliente, quiero volver a ver mis reservaciones y su estado después de cerrar la app. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C8]** «Confirmar reservación» se abre desde «Reservar» en `006` (solo `venueId`) o en `008` (`venueId`, `quoteId` y fecha). Muestra nombre del salón y su precio base de referencia con unidad. |
| RF-02 | **[C8]** Campos: fecha (selector; no pasada; prellenada si viene de una cotización) y horas (entero ≥ 1 y ≥ horas mínimas del salón si el servidor las envía). |
| RF-03 | **[C8]** Aviso fijo antes de confirmar: «No se cobra nada en la app. VirGo calcula el total y un anticipo del 30 %; el salón te indicará cómo pagarlo. La fecha se aparta cuando el pago se valide.» |
| RF-04 | **[C8]** «Confirmar reservación» envía `POST /api/v1/bookings` con `venueId`, `date`, `hours` y `quoteId` si existe; sin `deposit` (el servidor aplica el 30 %). Lleva un encabezado `Idempotency-Key` (UUID) generado una vez por intento y **reutilizado en los reintentos**. |
| RF-05 | **[C8]** El botón se desactiva mientras envía. Errores: `400` con mensajes de campo (p. ej. horas mínimas); `404` «Este salón ya no está disponible»; `401` cierra sesión (`030`); red/5xx con reintento usando la misma clave. |
| RF-06 | **[C23]** Ante éxito se abre «Reservación registrada» con los datos del servidor: folio, salón, fecha, horas, total, anticipo y estado. `pending_payment` se lee «Pendiente de pago». |
| RF-07 | **[C23]** El comprobante repite que no se ha cobrado nada y que la fecha no está apartada todavía. Acciones: «Ver mis reservaciones» y «Volver al catálogo». |
| RF-08 | **[Mis reservaciones]** Lista `GET /api/v1/bookings`: folio, salón, fecha y estado; más reciente primero. Vacío: «Aún no tienes reservaciones». Carga, error y reintento. Sustituye la sección «bookings» de muestra del espacio del cliente. |
| RF-09 | **[Mis reservaciones]** Tocar una reservación abre su comprobante leyendo `GET /api/v1/bookings/:id`. |
| RF-10 | Estados desconocidos se muestran como «En revisión» sin fallar. Montos en MXN y fechas en formato local. Textos en `strings.xml`. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño; claro/oscuro; el aviso de RF-03 usa el estilo informativo, no de error. |
| RNF-02 | **Accesibilidad:** 48 dp; sin cortes a 320 dp/fuente 2.0; el folio se anuncia completo. |
| RNF-03 | **Fiabilidad:** un reintento nunca crea una segunda reservación (misma `Idempotency-Key`). |
| RNF-04 | **Honestidad:** no se muestra «Disponible ✓», ni pago, ni fecha apartada mientras el backend no lo confirme. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un cliente en `006`, **cuando** elige fecha y horas y confirma, **entonces** se hace `POST /bookings` sin `deposit`, con `Idempotency-Key`, y se ve el folio del servidor. |
| CA-02 | **Dado** que viene de una cotización respondida, **cuando** confirma, **entonces** el cuerpo incluye `quoteId` y la fecha prellenada. |
| CA-03 | **Dado** una fecha pasada o 0 horas, **cuando** intenta confirmar, **entonces** ve el error en el campo y no se hace la petición. |
| CA-04 | **Dado** un fallo de red tras enviar, **cuando** reintenta, **entonces** se reutiliza la misma `Idempotency-Key` y solo existe una reservación. |
| CA-05 | **Dado** un `400` por horas mínimas, **cuando** llega, **entonces** se muestra junto al campo horas. |
| CA-06 | **Dado** el comprobante, **cuando** se lee, **entonces** indica «Pendiente de pago», que no se cobró nada y que la fecha no está apartada. |
| CA-07 | **Dado** reservaciones existentes, **cuando** abre «Mis reservaciones», **entonces** ve su lista real y puede abrir cada comprobante. |
| CA-08 | **Dado** las pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 6. Seguridad y privacidad

- No se capturan, transmiten ni almacenan datos de tarjetas ni bancarios.
- Bearer por `AuthInterceptor`; `401` delega en `030`.
- La `Idempotency-Key` vive solo en memoria del `ViewModel` (y `SavedStateHandle`); no se registra en logs.

## 7. Fuera de alcance

- Pago del anticipo (tarjeta guardada, nueva o transferencia), tarjetas (`019`/`020`), `POST /bookings/:id/payments` (`070`).
- Confirmación por el propietario, bloqueo de fecha, cancelación y reembolsos.
- «Ir a Mi Evento», Pagos y presupuesto del evento.
- Simulaciones de la maqueta (tarjeta «0000», folio inventado).

## 8. Supuestos, dependencias y preguntas abiertas

**Endpoints que consume:**

| Endpoint | Spec backend | Estado observado (2026-11-25) |
|---|---|---|
| `POST /api/v1/bookings` | `009-bookings-create` | Implementación local; declarado en OpenAPI; sin probar |
| `GET /api/v1/bookings` | `066-get-bookings` (Borrador) | ❌ no desplegado |
| `GET /api/v1/bookings/:id` | `067-get-bookings-id` (Borrador) | ❌ no desplegado |

**Dependencias del móvil:** `006`; `008` para el camino con cotización; `030`; `000` cerrado o excepción.

| ID | Pregunta | Quién | Propuesta |
|---|---|---|---|
| P-01 | El contrato incluye `eventId` y `quoteId`. | Backend | Ambos **opcionales** en el MVP; reserva directa desde `006` sin cotización. |
| P-02 | ¿Cómo paga el cliente el anticipo en el MVP? | Usuario + propietario | Fuera de la app (el salón lo indica); el pago en la app es un spec posterior. |
| P-03 | ¿El servidor devuelve horas mínimas en `GET /venues/:id`? | Backend | Sí (`price.minHours`); si no llega, la app solo exige ≥ 1. |
| P-04 | Forma del JSON de `GET /bookings` y `/bookings/:id` y nombres de estado. | Backend | Folio, venue `{id,name}`, `date`, `hours`, `total`, `deposit`, `status`; la app tolera estados nuevos. |

## 9. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`) más el PR #41; **aún no desplegado**.

- `POST /api/v1/bookings` (rol `client`) exige el encabezado `Idempotency-Key` (≤ 128); repetirlo devuelve la misma reservación. Cuerpo: `{venueId, date (yyyy-MM-dd), hours, deposit?, quoteId?, eventId?}`; `quoteId` y `eventId` son **opcionales**.
- El servidor calcula `total` y el anticipo por omisión; un `deposit` mayor que el total → `400`. Respuesta `201` con `status: "pending_payment"` y `folio`.
- Errores `400` solo con `title` (sin errores por campo).
- Pagos: con el PR #41 un pago del cliente queda `pending` y **no** confirma la reservación. El MVP no cobra en la app, como ya define este spec.
