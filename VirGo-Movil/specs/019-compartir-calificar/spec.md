# Spec — 019 Calificar el salón

| Campo | Valor |
|---|---|
| Módulo | `019-compartir-calificar` |
| Versión | 2.1.0 |
| Estado | Aprobado por el usuario (v2.0.0: 2026-11-27, «Aprobados, arranca», recortado; v2.1.0: 2026-11-27, «Sí, ambos») |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C34); backend `072`, `074`; esquema `reviews` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente califica el salón de una de sus reservaciones. El backend exige `bookingId` para reseñas de salón
(074 RF-03) y permite una por persona y salón (074 RF-02). Compartir con colaboradores (C30) queda fuera: no
hay invitaciones en el backend.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C34]** En el comprobante de una reservación no cancelada aparece «Calificar el salón» **solo cuando la fecha del evento ya pasó** (v2.1: no se califica un salón antes de usarlo). |
| RF-02 | **[C34]** Estrellas de 1 a 5 (cada una con nombre para TalkBack) y comentario de 1 a 1000 caracteres; «Publicar» se activa con estrellas y comentario. |
| RF-03 | Envía `POST /api/v1/reviews` con `targetType: "venue"`, `targetId`, `bookingId`, `stars` y `text`. `201`: «Gracias, tu reseña ya aparece en el salón». |
| RF-04 | `409`: «Ya calificaste este salón». `403`: «Podrás calificar cuando tu reservación esté confirmada». `404`/`501`: «Las reseñas llegarán pronto». Red/5xx: el formulario se conserva con «Reintentar». `401` cierra la sesión. |
| RF-05 | Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** 4 estrellas y comentario, **cuando** publico, **entonces** se envían con el `bookingId` de la reservación. |
| CA-02 | **Dado** `409`, **cuando** publico, **entonces** veo «Ya calificaste este salón». |
| CA-04 | **(v2.1) Dado** una reservación cuya fecha es hoy o futura, o una cancelada, **cuando** abro su comprobante, **entonces** no aparece «Calificar el salón»; al día siguiente del evento sí. |
| CA-03 | **Dado** sin estrellas o sin comentario, **cuando** veo el formulario, **entonces** «Publicar» está desactivado. |

## 4. Fuera de alcance

- Compartir el evento con colaboradores (C30), editar o borrar la reseña (`076`, `077`) y reseñas a proveedores.

## 5. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `POST /api/v1/reviews` | **Contrato §5** (074) |
