# Spec — 023 Inicio y calendario del propietario

| Campo | Valor |
|---|---|
| Módulo | `023-propietario-inicio-calendario` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado al backend real |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Propietario |
| Fuente | `Checklist VirGo.md` (G54, G58, P9); backend `031`, `032`, `033`, `066` y `024` en la cadena hacia `main` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El propietario hoy entra a «Mis salones» y a «Solicitudes»; su inicio y su calendario dicen «Próximamente».
El backend ya tiene lo necesario para ambos: sus salones (`GET /venues/mine`), las cotizaciones y reservaciones de
sus salones (`GET /quotes` y `GET /bookings` devuelven también lo de los salones propios), la disponibilidad
(`GET /venues/:id/availability`) y los bloqueos (`POST`/`DELETE /venues/:id/blocks`).

No existen visitas, ingresos, estadísticas ni avisos del propietario: quedan fuera (spec `025` y contrato).

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[G54]** «Inicio» saluda con el nombre de la sesión y muestra tres números: salones publicados de los registrados, solicitudes por responder (estado `sent`) y reservaciones próximas (fecha de hoy en adelante). |
| RF-02 | **[G54]** Lista de las próximas tres reservaciones de sus salones: salón, fecha, folio y estado; tocarla abre el calendario. |
| RF-03 | **[G54]** Accesos a «Mis salones», «Solicitudes» y «Calendario». |
| RF-04 | **[G54]** Sin salones registrados: estado vacío con «Registrar salón», que abre «Mis salones» (asistente de `022`). |
| RF-05 | **[G58]** «Calendario» con selector de salón (solo publicados: la disponibilidad solo existe para ellos) y vista mensual con mes anterior y siguiente. |
| RF-06 | **[G58]** Cada día se marca como **reservado** (hay una reservación de ese salón), **bloqueado** (la disponibilidad lo trae como `blocked`) o libre. La marca no depende solo del color: lleva texto para TalkBack. |
| RF-07 | **[G58]** Tocar un día muestra sus reservaciones (folio, cliente, horas y estado) y, si está bloqueado, el motivo y la nota. |
| RF-08 | **[G58]** Si un día tiene dos o más reservaciones, se muestra «Hay más de una reservación este día». |
| RF-09 | **[P9]** En un día libre, de hoy en adelante, «Bloquear este día» pide motivo (Mantenimiento, Evento propio u Otro) y una nota opcional de hasta 200 caracteres, y envía `POST /api/v1/venues/:id/blocks` con `date`, `reason` y `note`. |
| RF-10 | **[P9]** `201` marca el día como bloqueado. `409`: «Ese día ya no está libre». `400`: «Revisa el motivo y la nota». `404`: el salón ya no es tuyo; se recarga. |
| RF-11 | **[P9]** «Desbloquear» envía `DELETE /api/v1/venues/:id/blocks/:blockId`. Solo se ofrece cuando la disponibilidad trae el `id` del bloqueo; hoy el backend no lo envía (petición en el contrato), así que el botón aparece en cuanto lo agregue. |
| RF-12 | Cargando, error con «Reintentar» y `401` que cierra la sesión, como en los demás módulos. Textos en `strings.xml`. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño; referencia visual: prototipo local «VirGo Event Management App». |
| RNF-02 | **Accesibilidad:** cada día tiene descripción completa («12 de marzo, reservado»); áreas táctiles de 48 dp; fuente 2.0. |
| RNF-03 | **Seguridad:** el nombre del cliente solo se muestra al propietario del salón (lo filtra el backend); nada en logs. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un propietario con salones, reservaciones y solicitudes, **cuando** abre «Inicio», **entonces** ve los tres números y las próximas reservaciones. |
| CA-02 | **Dado** un propietario sin salones, **cuando** abre «Inicio», **entonces** ve «Registrar salón». |
| CA-03 | **Dado** el calendario, **cuando** cambia de mes o de salón, **entonces** los días reservados y bloqueados corresponden a ese salón y ese mes. |
| CA-04 | **Dado** un día libre, **cuando** lo bloquea con motivo, **entonces** queda bloqueado; **dado** `409`, **entonces** ve el aviso y el día no cambia. |
| CA-05 | **Dado** un día con dos reservaciones, **cuando** lo toca, **entonces** ve el aviso de empalme. |
| CA-06 | **Dado** un error de red, **cuando** carga, **entonces** ve un error con «Reintentar». |

## 5. Fuera de alcance

- Visitas, ingresos, estadísticas y avisos recientes (no hay endpoints; van al contrato y a `025`).
- Bloquear un rango de días de una vez: el backend recibe un día por petición.
- Calendario de salones en borrador.

## 6. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/venues/mine` | En `main`; ya lo usa `022` |
| `GET /api/v1/quotes` | En la cadena hacia `main`; al propietario le devuelve las de sus salones |
| `GET /api/v1/bookings` | En la cadena hacia `main`; al propietario le devuelve las de sus salones |
| `GET /api/v1/venues/:id/availability?from&to` | En la cadena hacia `main`; solo salones publicados; **sin `id` del bloqueo** |
| `POST /api/v1/venues/:id/blocks` | En la cadena hacia `main`; un día por petición |
| `DELETE /api/v1/venues/:id/blocks/:blockId` | En la cadena hacia `main` |
