# Plan — 009 Reservar (sin cobro en la app)

| Campo | Valor |
|---|---|
| Módulo | `009-reservar-pagar` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- `:core:network`: `BookingsApi` (`create` con `@Header("Idempotency-Key")`, `list`, `get`), `CreateBookingRequestDto`, `BookingDto`.
- `:core:domain`: `Booking`, `BookingStatus` (con `UNKNOWN`), `BookingRequest`, `BookingsRepository`,
  `ValidateBookingRequestUseCase` (fecha no pasada, horas ≥ max(1, minHours)),
  `CreateBookingUseCase(request, idempotencyKey)`, `GetBookingsUseCase`, `GetBookingUseCase`.
- `:core:data`: `BookingsRepositoryImpl`; errores de campo como en `008`.
- `:app`: `ui/bookings/` con `BookingConfirmScreen`, `BookingReceiptScreen`, `BookingsScreen` y sus `ViewModel`.
  Rutas `BookingConfirmRoute(venueId, quoteId?, date?)`, `BookingReceiptRoute(bookingId)`, `BookingsRoute`.

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Idempotencia | `UUID.randomUUID()` al iniciar el intento; guardado en `SavedStateHandle`; se renueva solo tras éxito o si cambian fecha/horas |
| Dinero | Montos del servidor como `BigDecimal` desde texto decimal; formato MXN local |
| Pagos | Ninguno; sin SDK de pagos ni datos de tarjeta |
| Persistencia | Ninguna local; la lista se lee del servidor |

## 3. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ✅ Cumple | Flujo corto aprobado (lote MVP). |
| II | ⚠️ Excepción | Ampliación aprobada («Sí, sigue con 009»), sin `000` cerrado ni URL desplegada. |
| III | ✅ Cumple | MVVM/UDF; validación e idempotencia en dominio. |
| IV | ⚠️ Parcial | Contrato verificado en el código de backend (§9 del spec): `quoteId`/`eventId` opcionales, lectura con el `Booking` completo; falta la URL desplegada. |
| V | ✅ Cumple | Sin datos de pago; sin claves en logs. |
| VI–XII | ✅ Planificado | Pruebas en §8; pipeline existente. |

## 4. Estructura

```text
core/network/.../api/BookingsApi.kt · dto/BookingDtos.kt
core/domain/.../Booking.kt · BookingsRepository.kt · ValidateBookingRequestUseCase.kt
core/domain/.../CreateBookingUseCase.kt · GetBookingsUseCase.kt · GetBookingUseCase.kt
core/data/.../repository/BookingsRepositoryImpl.kt · di/BookingsModule.kt
app/.../ui/bookings/BookingConfirm{Screen,ViewModel}.kt
app/.../ui/bookings/BookingReceipt{Screen,ViewModel}.kt
app/.../ui/bookings/Bookings{Screen,ViewModel}.kt
app/.../navigation/{Routes,VirGoNavHost}.kt · demo/RoleWorkspace.kt
app/src/main/res/values/bookings_strings.xml
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | Reservar sin cobro en la app | Pagos, confirmación y bloqueo de fecha no existen en backend; evita PCI en el MVP. |
| ADR-02 | `Idempotency-Key` estable por intento, en `SavedStateHandle` | Reintentos y rotación no duplican reservaciones. |
| ADR-03 | No enviar `deposit` | El servidor aplica el 30 % (handoff §9.9); la app no calcula dinero. |
| ADR-04 | No mostrar total estimado antes de confirmar | Lo calcula el servidor; se muestra solo el precio base de referencia. |

## 6. Diseño

- `BookingConfirmUiState(venueName, priceReference, date, hours, minHours, fieldErrors, sending, idempotencyKey)`.
- Comprobante: folio grande, tabla salón/fecha/horas/total/anticipo/estado y aviso informativo.

## 7. DevSecOps y CI/CD

Pipeline existente; sin dependencias ni secretos nuevos. Revisión de seguridad
específica: ningún campo de pago en UI, red ni logs.

## 8. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Validación (horas mínimas, fechas límite), idempotencia (reintento reutiliza, cambio de datos renueva), `ViewModel` de las tres pantallas |
| Integración | MockWebServer: `201`, `400` horas, `401`, `404`, `500`, timeout; encabezado `Idempotency-Key` presente e igual en el reintento; cuerpo sin `deposit` |
| UI | CA-01 a CA-07; accesibilidad 320 dp/2.0 en ambos temas |
| Seguridad | Sin datos de pago; clave y datos ausentes en logs |
| Mutación | Pitest en validación y política de idempotencia |
| Humo | Una reservación real contra producción con cuenta desechable sobre un salón de prueba publicado por `022` |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Expectativa de pago inmediato | Aviso fijo en confirmación y comprobante (RF-03, RF-07). |
| Duplicados por reintento | ADR-02 y prueba de integración específica. |
| Reservas de prueba en producción | Salón y cuenta de prueba identificables; limpieza coordinada con backend. |

## 10. Fases

F1 red/dominio/datos (T901–T903) · F2 UI (T904–T906) · F3 calidad y humo (T907–T908).
