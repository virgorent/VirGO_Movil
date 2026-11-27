# Plan — 010 Mis reservaciones: buscar por folio y detalle

| Campo | Valor |
|---|---|
| Módulo | `010-reservaciones` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26) |
| Fecha | 2026-11-26 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

Se extiende lo que ya existe en `009`; no se crea módulo `:feature:*` (igual que `005`, `009` y `030`).

| Capa | Cambio |
|---|---|
| `:core:network` | `BookingsApi.byFolio(@Path("folio") folio)` → `BookingDto`. |
| `:core:data` | `BookingsRepositoryImpl.byFolio()` con el mismo `safeCall` y mapeo `toBooking()` de `009`. |
| `:core:domain` | `FindBookingByFolioUseCase` y `BookingFolio` (normaliza y valida `^VG-\d{4}-\d{4}$`). |
| `:app` `ui/bookings` | `BookingsViewModel` suma `folioQuery`, `searching` y `searchError`; un `200` reutiliza `ReceiptState`. `BookingReceipt` suma el bloque informativo de RF-07. |

## 2. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I y II | ✅ | Spec atómico; recorta la v1.0.0 a lo que tiene backend. |
| III | ✅ | MVVM/UDF; estado inmutable por `StateFlow`. |
| IV | ✅ | Solo `/api/v1`, Bearer y `AppError`. |
| V | ✅ | Sin folio ni token en logs. |
| VI | ✅ | Sección 3. |
| VII | ✅ | Sistema de diseño y `strings.xml`. |
| VIII.6 | ✅ | MockWebServer hasta que producción tenga el backend nuevo. |

## 3. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | `BookingFolio`: espacios, minúsculas, formatos inválidos. `BookingsViewModel`: búsqueda con 200, 404, 401, red; botón desactivado mientras busca. |
| Integración | MockWebServer: ruta `api/v1/bookings/by-folio/VG-2026-0001`, Bearer presente, 200/404/401/500 y sin conexión. |
| UI | Campo, error de formato, mensaje de 404 y bloque de RF-07 sin botones de cancelar; fuente 2.0 a 320 dp. |
| Seguridad | El folio no aparece en logs; el folio va en la ruta, no en la query. |
| Humo | `scripts/humo-produccion.py` modo `lectura` tras el despliegue. |

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| El formato del folio cambia en el backend | La validación vive en un solo lugar (`BookingFolio`) y tiene pruebas. |
| El usuario espera cancelar desde la app | RF-07 lo dice claro; queda en «Fuera de alcance». |
