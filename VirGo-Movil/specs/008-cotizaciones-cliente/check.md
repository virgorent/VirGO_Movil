# Check — 008 Cotizaciones del cliente

| Campo | Valor |
|---|---|
| Módulo | `008-cotizaciones-cliente` |
| Estado del spec | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Revisó | — |
| Fecha | 2026-11-25 |

Sin evidencia todavía: el módulo no se ha implementado.

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 (lote MVP).
- [ ] `000` cerrado o excepción aprobada (Art. II.2).
- [ ] P-01 (`eventId`) y P-02 (respuesta del propietario) decididas.

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | Prueba de UI + MockWebServer del cuerpo enviado; humo | ⏳ |
| CA-02 | Unitaria de validación + prueba de UI | ⏳ |
| CA-03 | MockWebServer 400 con campos + prueba de UI | ⏳ |
| CA-04 | Prueba de reintento sin duplicado | ⏳ |
| CA-05 | Prueba de lista con estados conocidos y desconocido | ⏳ |
| CA-06 | Prueba de estado vacío | ⏳ |
| CA-07 | Prueba de detalle respondido y navegación a `009` | ⏳ |
| CA-08 | Prueba de detalle enviado sin acción | ⏳ |
| CA-09 | Barrido de accesibilidad 320 dp/2.0 ambos temas | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Totales: — | ⏳ |
| Integración | Totales: — | ⏳ |
| UI | Totales: — | ⏳ |
| Seguridad | — | ⏳ |
| Mutación | Puntaje: — % | ⏳ |
| Humo | APK contra producción | ⏳ |

## 4. PR

- [ ] PR: enlace —
- [ ] Una revisión aprobada y checks requeridos en verde.

## 5. Memoria

- [ ] Entrada en `memoria.md`.
