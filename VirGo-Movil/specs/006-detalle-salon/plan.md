# Plan — 006 Detalle del salón

| Campo | Valor |
|---|---|
| Módulo | `006-detalle-salon` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- Se implementa en `:app` y `:core:*`, como `005` y `030`; no se crea `:feature:*`.
- `:core:network`: `VenuesApi.venue(id)` y `VenueDetailDto` (extiende los campos de `VenueDto`).
- `:core:domain`: `VenueDetail`, `GetVenueDetailUseCase`; `VenueRepository.venue(id)`.
- `:core:data`: implementación en el repositorio de salones existente; `404` → `AppError.NotFound`.
- `:app`: `VenueDetailRoute(venueId)`, `VenueDetailViewModel` y `VenueDetailScreen` en `ui/venuedetail/`.
  El catálogo beta cambia el botón deshabilitado por «Ver detalle».

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Lenguaje / framework | Kotlin, Compose (Material 3), MVVM/UDF, Hilt |
| Red | Retrofit; `GET /api/v1/venues/{id}`; Bearer opcional por `AuthInterceptor` |
| Imágenes | Ninguna en el MVP (P-04); sin dependencias nuevas |
| Persistencia | Ninguna; el detalle vive en el estado del `ViewModel` |
| Pruebas | JUnit/Turbine, MockWebServer, Compose Testing (Robolectric), Pitest |

## 3. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ⚠️ Pendiente | Spec atómico (una pantalla); falta aprobación humana. |
| II | ⚠️ Pendiente | Requiere `000` cerrado o excepción aprobada. |
| III | ✅ Cumple | UI → dominio → datos; un `StateFlow` por `ViewModel`. |
| IV | ⚠️ Parcial | `/api/v1`, Problem Details; forma del JSON pendiente (P-01). |
| V | ✅ Cumple | Sin datos sensibles en logs; HTTPS en release. |
| VI | ✅ Planificado | Sección 8. |
| VII | ✅ Cumple | Sistema de diseño, `strings.xml`, claro/oscuro. |
| VIII–XII | ✅ Cumple | Pipeline existente; memoria al cerrar; español/inglés. |

## 4. Estructura

```text
core/network/.../api/VenuesApi.kt          # + venue(id)
core/network/.../dto/VenueDtos.kt          # + VenueDetailDto
core/domain/.../VenueDetail.kt             # nuevo
core/domain/.../GetVenueDetailUseCase.kt   # nuevo
core/domain/.../VenueRepository.kt         # + venue(id)
core/data/.../repository/VenueRepositoryImpl.kt
app/.../navigation/Routes.kt               # + VenueDetailRoute(venueId)
app/.../navigation/VirGoNavHost.kt         # destino y acciones
app/.../ui/beta/BetaCatalogScreen.kt       # «Ver detalle»
app/.../ui/venuedetail/VenueDetailScreen.kt
app/.../ui/venuedetail/VenueDetailViewModel.kt
app/src/main/res/values/venue_detail_strings.xml
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | DTO tolerante (`ignoreUnknownKeys`, campos opcionales) | El JSON exacto está pendiente (P-01). |
| ADR-02 | Ocultar precio sin sesión también en la app | Defensa en profundidad de la regla A3. |
| ADR-03 | Las acciones navegan con `venueId`, no con el objeto | `008` y `009` vuelven a leer el salón; evita estado obsoleto. |
| ADR-04 | Sin galería en el MVP | No hay carga de fotos en backend ni librería de imágenes; no se inventan fotos (`034`). |

## 6. Diseño

`VenueDetailUiState(loading, error: AppError?, venue: VenueDetail?, signedIn, role)`.
Orden visual: regreso · nombre + verificado · zona/dirección ·
calificación · precio · descripción · capacidad · categorías/amenidades/servicios ·
acciones fijas al pie. A 320 dp/fuente 2.0 las acciones se apilan.

## 7. DevSecOps y CI/CD

Pipeline existente (`ktlint → detekt → lint → pruebas → pitest → assembleDebug`),
Gitleaks, dependency review y CodeQL. Sin workflows ni secretos nuevos.

## 8. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | `VenueDetailViewModel`: carga, éxito, 404, red, reintento, precio con/sin sesión, roles |
| Integración | MockWebServer: 200 completo, 200 mínimo, 404, 500, timeout, Bearer presente/ausente |
| UI | Compose Testing: CA-02 a CA-07; barrido de accesibilidad a 320 dp/2.0 en ambos temas |
| Seguridad | Sin datos de cuenta en logs |
| Mutación | Pitest en `GetVenueDetailUseCase` y el mapeo de dominio |
| Humo | Contra producción cuando exista un salón publicado y el endpoint |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| El endpoint no existe aún | MockWebServer con la propuesta P-01; integración al desplegarse. |
| Catálogo de producción vacío | Depende de `022`; el humo se hace tras publicar el primer salón. |

## 10. Fases

F1 red/dominio/datos (T601–T602) · F2 UI y navegación (T603–T604) · F3 calidad y humo (T605–T606).
