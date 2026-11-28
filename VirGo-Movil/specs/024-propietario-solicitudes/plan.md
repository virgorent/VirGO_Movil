# Plan — 024 Solicitudes de cotización del propietario

| Campo | Valor |
|---|---|
| Módulo | `024-propietario-solicitudes` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- `:core:network`: `QuotesApi.respond(id, QuoteAnswerRequestDto)`; `QuoteDto` lee `clientName`.
- `:core:domain`: `Quote.clientName`; `QuoteAnswer(total, deposit?, message)`, `QuoteAnswerValidator`, `QuotesRepository.respond`, `RespondQuoteUseCase`.
- `:core:data`: `QuotesRepositoryImpl.respond`.
- `:app`: `ui/ownerrequests/` con `OwnerRequestsViewModel` (lista, pestaña, detalle y respuesta) y `OwnerRequests` (lista con pestañas y detalle con formulario). La ruta `ownerRequests` del espacio del propietario la abre en lugar de la sección «Próximamente».

## 2. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ✅ | Flujo corto: lista y responder. |
| II | ⚠️ Excepción | `000` sin cerrar y sin URL desplegada; ver la excepción ampliada. |
| III | ✅ | MVVM/UDF; validación en `:core:domain`. |
| IV | ✅ | Contrato verificado en el código de backend. |
| V | ✅ | Sin montos ni mensajes en logs. |
| VI–XII | ✅ | Pruebas en §5; pipeline existente; memoria al cerrar. |

## 3. Decisiones (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | Filtrar pestañas en el cliente sobre una sola lista | El backend no filtra por estado. |
| ADR-02 | Montos como `BigDecimal` con hasta 2 decimales | Sin errores de redondeo. |
| ADR-03 | `409` recarga el detalle | Otro dispositivo pudo responder antes. |

## 4. Diseño

`OwnerRequestsUiState(loading, failed, quotes, tab, detail)`; `RequestDetailState(quoteId, loading, quote, failed, form, fieldErrors, sending, error)`.

## 5. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Validador (total, anticipo, nota), `ViewModel` (pestañas, envío único, errores, 409) |
| Integración | MockWebServer: cuerpo de la respuesta, `200`, `400`, `403`, `404`, `409` |
| UI | CA-01–CA-05 |
| Accesibilidad | Barrido 320 dp/2.0 en ambos temas |
| Humo | Responder una cotización real tras el despliegue |
