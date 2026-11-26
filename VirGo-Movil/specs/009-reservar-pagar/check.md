# Check — 009 Reservar (sin cobro en la app)

| Campo | Valor |
|---|---|
| Módulo | `009-reservar-pagar` |
| Estado del spec | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Revisó | — |
| Fecha | 2026-11-25 |

Sin evidencia todavía: el módulo no se ha implementado.

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [ ] `000` cerrado o excepción aprobada (Art. II.2).
- [ ] P-01 (`eventId`/`quoteId` opcionales) y P-02 (pago fuera de la app) decididas.

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | MockWebServer: cuerpo sin `deposit` y encabezado; prueba de UI; humo | ⏳ |
| CA-02 | Prueba con `quoteId` y fecha prellenada | ⏳ |
| CA-03 | Unitaria de validación + prueba de UI | ⏳ |
| CA-04 | Integración: reintento con la misma clave | ⏳ |
| CA-05 | MockWebServer 400 horas mínimas + UI | ⏳ |
| CA-06 | Prueba de UI del comprobante y avisos | ⏳ |
| CA-07 | Prueba de lista y apertura de comprobante | ⏳ |
| CA-08 | Barrido de accesibilidad 320 dp/2.0 ambos temas | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: — | ⏳ |
| Integración | Totales: — | ⏳ |
| UI | Totales: — | ⏳ |
| Seguridad | Sin datos de pago ni claves en logs | ⏳ |
| Mutación | Puntaje: — % | ⏳ |
| Humo | APK contra producción | ⏳ |

## 4. PR

- [ ] PR: enlace —
- [ ] Una revisión aprobada y checks requeridos en verde.

## 5. Memoria

- [ ] Entrada en `memoria.md`.
