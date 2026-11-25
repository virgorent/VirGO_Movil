# Plan — 008 Cotizaciones del cliente

| Campo | Valor |
|---|---|
| Módulo | `008-cotizaciones-cliente` |
| Versión | 2.1.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- `:core:network`: `QuotesApi` (`create`, `list`, `get`), `CreateQuoteRequestDto`, `QuoteDto`, `QuotesResponseDto`.
- `:core:domain`: `Quote`, `QuoteStatus` (con `UNKNOWN`), `QuoteRequest`, `QuotesRepository`,
  `ValidateQuoteRequestUseCase` (fecha no pasada, invitados ≥ 1, mensaje ≤ 500),
  `CreateQuoteUseCase`, `GetQuotesUseCase`, `GetQuoteUseCase`.
- `:core:data`: `QuotesRepositoryImpl`; Problem Details con errores de campo → `AppError.Validation(fields)`.
- `:app`: `ui/quotes/` con `QuoteRequestScreen` (+`ViewModel`) y `ClientQuotes` (lista y detalle con `QuotesViewModel`).
  *v2.1.0:* como `006`/`009`, la navegación vive dentro del espacio del cliente (`ClientExplore` y la sección «quotes»), sin rutas nuevas en el `NavHost`.
  La sección «quotes» del espacio del cliente abre `QuotesScreen` en lugar de la tarjeta «Próximamente».

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Fecha | `java.time.LocalDate` (nativo desde API 26, que es el `minSdk`; sin desugaring) serializada `yyyy-MM-dd` |
| Validación | En dominio (JVM puro, Pitest); el servidor sigue siendo la autoridad |
| Envíos dobles | Botón deshabilitado durante el envío; el estado no se reenvía al rotar |
| Persistencia | Ninguna; borrador del formulario en `SavedStateHandle` |

## 3. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ⚠️ Pendiente | Flujo corto de tres pantallas; falta aprobación. |
| II | ⚠️ Pendiente | Requiere `000` cerrado o excepción. |
| III | ✅ Cumple | MVVM/UDF; validación en `:core:domain`. |
| IV | ⚠️ Parcial | Contrato de `POST` definido; `eventId` (P-01) y forma de lista/detalle (P-04) pendientes. |
| V | ✅ Cumple | Sin datos del evento en logs. |
| VI–XII | ✅ Planificado | Pruebas en §8; pipeline existente; memoria al cerrar. |

## 4. Estructura

```text
core/network/.../api/QuotesApi.kt          # nuevo
core/network/.../dto/QuoteDtos.kt          # nuevo
core/domain/.../Quote.kt                   # Quote, QuoteStatus, QuoteRequest
core/domain/.../QuotesRepository.kt
core/domain/.../ValidateQuoteRequestUseCase.kt
core/domain/.../CreateQuoteUseCase.kt · GetQuotesUseCase.kt · GetQuoteUseCase.kt
core/data/.../repository/QuotesRepositoryImpl.kt · di/QuotesModule.kt
app/.../ui/quotes/QuoteRequest{Screen,ViewModel}.kt
app/.../ui/quotes/Quotes{Screen,ViewModel}.kt
app/.../ui/quotes/QuoteDetail{Screen,ViewModel}.kt
app/.../navigation/{Routes,VirGoNavHost}.kt · demo/RoleWorkspace.kt
app/src/main/res/values/quotes_strings.xml
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | `QuoteStatus.UNKNOWN` en lugar de fallar | El backend puede añadir estados. |
| ADR-02 | Sin `eventId` en el cuerpo mientras P-01 siga abierta | `007` no está en el MVP. |
| ADR-03 | Los montos se muestran solo si el servidor los envía | No inventar precios. |
| ADR-04 | Errores de campo de Problem Details mapeados por nombre de campo | Mensajes junto al campo (RNF-02). |
| ADR-05 | Sin `Idempotency-Key` en el servidor: tras un envío sin respuesta, el reintento busca una solicitud idéntica «Enviada» antes de reenviar (v2.1.0) | CA-04 sin depender de un cambio de backend. |
| ADR-06 | Reservar desde la cotización reutiliza la pantalla de `009` con `quoteId` y la fecha (v2.1.0) | Un solo flujo de reserva; el total lo calcula el servidor. |

## 6. Diseño

- `QuoteRequestUiState(venueName, eventType, date, guests, services, selected, message, fieldErrors, sending, capacityWarning)`.
- `QuotesUiState(loading, error, quotes)`; `QuoteDetailUiState(loading, error, quote)`.
- Etiquetas de estado con color semántico **y** texto (no solo color).

## 7. DevSecOps y CI/CD

Pipeline existente; Gitleaks, dependency review y CodeQL. Sin dependencias nuevas.

## 8. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Validación (fechas límite, invitados, mensaje 500/501), `ViewModel` de las tres pantallas, mapeo de estados |
| Integración | MockWebServer: `201`, `400` con campos, `401`, `404`, `500`, timeout; lista vacía/con estados desconocidos; detalle con y sin montos |
| UI | CA-01 a CA-08; barrido de accesibilidad 320 dp/2.0 en ambos temas |
| Seguridad | Mensaje y datos del evento ausentes en logs |
| Mutación | Pitest en `ValidateQuoteRequestUseCase` y mapeo de `QuoteStatus` |
| Humo | Crear una cotización real contra producción con cuenta desechable cuando `060`/`061` estén desplegados |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Nadie responde cotizaciones (P-02) | Decisión explícita antes de implementar; la reserva directa en `009` no depende de esto. |
| Contrato de lista/detalle cambia | DTO tolerante y `UNKNOWN`. |

## 10. Fases

F1 red/dominio/datos (T801–T803) · F2 UI (T804–T806) · F3 calidad y humo (T807–T808).
