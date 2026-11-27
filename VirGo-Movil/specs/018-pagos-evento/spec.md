# Spec — 018 Pagos del evento

| Campo | Valor |
|---|---|
| Módulo | `018-pagos-evento` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C26); esquema `events.externalPayments` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

La app no cobra. El cliente lleva el registro de lo que paga por fuera (anticipo al salón, DJ, banquete) para
saber cuánto le falta. Se guarda en `events.externalPayments` con `PATCH /events/:id` (contrato §4).

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C26]** Historial con fecha, concepto, monto y estado («Pagado», «Pendiente», «Falló», «Reembolsado»), más reciente primero. |
| RF-02 | **[C26]** Resumen: presupuesto del evento, pagado (suma `completed`), pendiente por registrar (suma `pending`) y saldo (presupuesto − pagado). |
| RF-03 | **[C26]** «Registrar pago»: concepto (1–150), monto (> 0, máximo 2 decimales), estado y fecha (no futura). El `id` lo genera la app. |
| RF-04 | Quitar un pago con confirmación. Cada cambio envía la lista completa. |
| RF-05 | Si el guardado falla, la lista vuelve a como estaba y se avisa; si el backend aún no acepta el campo, «El registro de pagos llegará pronto». |
| RF-06 | Aviso fijo: «VirGo no cobra; aquí solo registras lo que pagas por fuera». |
| RF-07 | Sin evento: acceso a «Mi Evento». `401` cierra la sesión. Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** pagos de $3 000 y $5 000 pagados y $2 000 pendiente con presupuesto de $150 000, **cuando** abro «Pagos», **entonces** veo pagado $8 000 y saldo $142 000. |
| CA-02 | **Dado** un monto 0 o un concepto vacío, **cuando** guardo, **entonces** veo el error y no se envía nada. |
| CA-03 | **Dado** que el guardado falla, **cuando** registré un pago, **entonces** la lista vuelve a como estaba. |

## 4. Fuera de alcance

- Cobrar en la app, tarjetas y comprobantes.

## 5. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/events/:id` | Falta devolver `externalPayments` (contrato §4) |
| `PATCH /api/v1/events/:id` con `externalPayments` | **Contrato §4** |
