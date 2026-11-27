# Spec — 015 Presupuesto del evento

| Campo | Valor |
|---|---|
| Módulo | `015-presupuesto` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C24, C25); prototipo local (`Planning.tsx`: `Budget`, `BudgetAI`); esquema `events.budgetPlan` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El evento ya tiene presupuesto total (`014`). Este spec agrega el plan por categoría, guardado en
`events.budgetPlan` mediante `PATCH /events/:id` (contrato §4), y el reparto sugerido con las reglas del
prototipo, que se calcula en la app.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C25]** «Presupuesto» del primer evento: total del evento, total planeado, pagado (suma de pagos `completed` de `018`) y disponible (total − pagado). |
| RF-02 | **[C25]** Plan por categoría: Salón, Catering, Decoración, Música, Fotografía y Otro, con monto planeado y porcentaje del total. |
| RF-03 | **[C25]** Alertas: «El plan supera tu presupuesto por $X» si lo planeado excede el total; «Llevas el N % de tu presupuesto» si lo pagado llega al 90 %. |
| RF-04 | **[C24]** «Presupuesto sugerido»: reparto por tipo de evento del prototipo (p. ej. Boda: Salón 30, Catering 35, Decoración 12, Música 8, Fotografía 8, Otro 7) redondeado a centenas. |
| RF-05 | **[C24]** Preferencias que mueven porcentajes: «Comida de primera», «Mucha decoración», «Música en vivo» y «Ahorrar en salón» (deltas del prototipo; ningún porcentaje baja de 0). |
| RF-06 | **[C24]** Cada monto se puede editar antes de aplicar; «Restablecer» vuelve a la sugerencia; «Aplicar» guarda `budgetPlan`. |
| RF-07 | Sin evento: «Crea tu evento para planear el presupuesto» con acceso a «Mi Evento». |
| RF-08 | Mientras el backend no acepte `budgetPlan`, guardar muestra «El presupuesto por categoría llegará pronto» (`400`/`404` del PATCH con ese campo); el plan editado no se pierde de la pantalla. |
| RF-09 | `401` cierra la sesión; red/5xx con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** una boda de $150 000, **cuando** abro la sugerencia, **entonces** Catering propone $52 500. |
| CA-02 | **Dado** «Comida de primera», **cuando** la elijo, **entonces** Catering sube y Decoración baja. |
| CA-03 | **Dado** un plan mayor al total, **cuando** lo veo, **entonces** aparece la alerta de exceso. |
| CA-04 | **Dado** que aplico la sugerencia, **cuando** el servidor responde, **entonces** se envía `budgetPlan` con las seis categorías. |

## 4. Fuera de alcance

- Gasto por proveedor contratado (no existen proveedores en el backend).

## 5. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/events`, `GET /api/v1/events/:id` | En la cadena hacia `main`; falta devolver `budgetPlan` (contrato §4) |
| `PATCH /api/v1/events/:id` con `budgetPlan` | **Contrato §4** |
