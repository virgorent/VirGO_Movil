# Spec — 017 Pendientes

| Campo | Valor |
|---|---|
| Módulo | `017-pendientes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion», 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Pantallas | C27 |
| Backend | `GET/POST/PATCH/DELETE /events/:id/tasks` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Checklist del evento con fechas límite. El servidor guarda título, fecha límite y si está hecho. No genera sugerencias ni recordatorios y no hay línea de tiempo: C28, C29 y la lista sugerida quedan fuera.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C27]** Lista `GET /api/v1/events/:id/tasks` agrupada en **Vencidas** (no hechas con fecha pasada), **Próximas** (no hechas) y **Hechas**, por fecha límite. |
| RF-02 | **[C27]** Marcar o desmarcar como hecho (`PATCH` con todos los campos). |
| RF-03 | **[C27]** Agregar y editar con título (≤ 150) y fecha límite; eliminar con confirmación. |
| RF-04 | Errores: campo junto al campo; `404`; `401` cierra sesión; red/5xx con reintento. |

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
| CA-01 | **Dado** pendientes vencidos, próximos y hechos, **cuando** abre la lista, **entonces** aparecen en su grupo. |
| CA-02 | **Dado** un pendiente, **cuando** lo marca como hecho, **entonces** se envía `PATCH` con `done: true` y pasa a «Hechas». |
| CA-03 | **Dado** un título vacío, **cuando** guarda, **entonces** ve el error y no se hace la petición. |
| CA-04 | **Dado** las pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 5. Fuera de alcance

- Lista sugerida, recordatorios automáticos (C29) y línea de tiempo (C28): sin backend.

## 6. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`), `EventsController`; **aún no desplegado**. Cualquier usuario con sesión puede usarlo.

- `GET /events/:id/tasks` → `{data: [{id, eventId, title, dueDate, done, suggested}]}`.
- `POST` y `PATCH /events/:id/tasks/:taskId`: `{title ≤ 150, dueDate yyyy-MM-dd, done}` (el `PATCH` exige todo). `DELETE` → `204`.
