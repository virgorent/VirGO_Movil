# Spec — 007 Crear y editar evento

| Campo | Valor |
|---|---|
| Módulo | `007-crear-evento` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion», 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Pantallas | C12 |
| Backend | `POST /events`, `PATCH /events/:id` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente registra su evento para organizar invitados y pendientes. El servidor guarda nombre, tipo, fecha, invitados, presupuesto, horas y, opcionalmente, un salón publicado. La v1.0.0 adaptaba «recomendaciones y estilos de IA»: no hay backend para eso y queda fuera.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C12]** Nombre del evento (obligatorio, ≤ 100). |
| RF-02 | **[C12]** Tipo: Boda, XV años, Cumpleaños, Corporativo u Otro (valores exactos del contrato). |
| RF-03 | **[C12]** Fecha (selector; no pasada al crear o al cambiarla), invitados (≥ 1), horas (≥ 1) y presupuesto en MXN (≥ 0, hasta 2 decimales). |
| RF-04 | **[C12]** Si el evento ya tiene salón, se conserva al editar (el `PATCH` reemplaza todo y sin `venueId` lo quitaría). |
| RF-05 | «Guardar» valida, envía `POST /api/v1/events` (o `PATCH /api/v1/events/:id` al editar) y vuelve a «Mi evento». Errores de campo del servidor junto a su campo; `404` al editar «Este evento ya no está disponible»; `401` cierra sesión; red/5xx con reintento conservando lo capturado. Sin envíos dobles. |

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
| CA-01 | **Dado** el formulario completo, **cuando** guarda, **entonces** se hace `POST /events` con el cuerpo del contrato y vuelve a «Mi evento». |
| CA-02 | **Dado** un evento con salón, **cuando** edita solo el nombre, **entonces** el `PATCH` conserva el `venueId`. |
| CA-03 | **Dado** campos inválidos, **cuando** guarda, **entonces** ve el error junto al campo y no se hace la petición. |
| CA-04 | **Dado** el formulario a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisa, **entonces** no hay cortes ni controles < 48 dp. |

## 5. Fuera de alcance

- Recomendaciones y estilos de IA (`012`, sin backend).
- Elegir salón desde el formulario: en esta versión la app no asigna salón al evento.

## 6. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`), `EventsController`; **aún no desplegado**. Cualquier usuario con sesión puede usarlo.

- `POST /api/v1/events` y `PATCH /api/v1/events/:id`: `{name ≤ 100, eventType ∈ {Boda, XV años, Cumpleaños, Corporativo, Otro}, date yyyy-MM-dd, guests ≥ 1, budget ≥ 0, hours ≥ 1, venueId?}`. El `PATCH` exige todos los campos y reemplaza el salón.
- Respuesta: `{id, ownerId, name, eventType, date, guests, budget, hours, venue {venueId, name, photoUrl}?, collaborators, createdAt, updatedAt}`; `design`, `budgetPlan` e `invitation` siempre `null`.
- `400` Problem Details con errores por campo; `404` si el evento no es del usuario.
