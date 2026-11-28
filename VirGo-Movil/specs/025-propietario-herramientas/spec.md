# Spec — 025 Herramientas del propietario

| Campo | Valor |
|---|---|
| Módulo | `025-propietario-herramientas` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Propietario |
| Fuente | `Checklist VirGo.md` (P10–P14); esquema `venues` (`price`, `setups`), `reviews`; backend `072`, `075` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Herramientas del propietario sobre sus salones publicados. Pagos y estadísticas se calculan en la app con
reservaciones y cotizaciones reales; precios, montajes y reseñas usan los campos del esquema (contrato §5 y §6).
«Mis salones» y editar un salón ya existen (`022`).

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[P10] Paquetes y precios:** por salón, aumento de viernes y sábado (0–100 %), aumento de temporada alta (0–100 %) y sus meses (1–12). Vista previa del precio por hora o por día en el próximo martes, el próximo sábado y un día de temporada alta. «Guardar» envía `price` completo con `PATCH /venues/:id`. |
| RF-02 | **[P11] Planos y montajes:** capacidad para banquete, auditorio y coctel (enteros ≥ 0), guardada en `setups` con `PATCH /venues/:id`. Subir el plano queda «Próximamente» (contrato §8). |
| RF-03 | **[P12] Pagos recibidos:** reservaciones de sus salones con fecha, cliente, folio, anticipo y estado; total de anticipos de las confirmadas y lo pendiente de las que esperan pago. La app no cobra. |
| RF-04 | **[P13] Estadísticas:** por mes, en vista de 3 o 6 meses, solicitudes recibidas, reservaciones y conversión (reservaciones ÷ solicitudes). Visitas fuera (078 RF-03). |
| RF-05 | **[P14] Reseñas:** por salón, calificación promedio, número de reseñas y cada opinión con su respuesta. «Responder» o «Editar respuesta» (1–1000) con `POST /reviews/:id/reply`. |
| RF-06 | Cuando un endpoint del contrato no existe (`404`/`501`, o `400` por un campo que el backend aún no acepta), la pantalla dice «Esta parte llegará pronto» sin error técnico. `401` cierra la sesión. Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** $3 500 por hora, 15 % en fin de semana y 20 % en noviembre, **cuando** veo la vista previa, **entonces** el sábado cuesta $4 025 y un martes de noviembre $4 200. |
| CA-02 | **Dado** reservaciones confirmadas y pendientes, **cuando** abro «Pagos recibidos», **entonces** los totales coinciden con sus anticipos. |
| CA-03 | **Dado** 4 solicitudes y 1 reservación en un mes, **cuando** abro «Estadísticas», **entonces** la conversión es 25 %. |
| CA-04 | **Dado** una reseña sin respuesta, **cuando** respondo, **entonces** se envía y aparece debajo. |

## 4. Fuera de alcance

- Visitas (078), proveedores aliados (P15) y subir el plano (contrato §8).

## 5. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /venues/mine`, `GET /bookings`, `GET /quotes` | En la cadena hacia `main` |
| `PATCH /venues/:id` con `price.weekendPct`, `highSeasonPct`, `highSeasonMonths`, `setups` | **Contrato §6** |
| `GET /venues/:id/reviews`, `POST /reviews/:id/reply` | **Contrato §5** (072, 075) |
