# Plan — 022 Propietario: registrar salón

| Campo | Valor |
|---|---|
| Módulo | `022-propietario-registrar-salon` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- `:core:network`: `OwnerVenuesApi` (`mine`, `create`, `update`, `publish`) y DTOs `VenueDraftDto`, `OwnerVenueDto`;
  `get(id)` reutiliza `VenuesApi.venue(id)` de `006`.
- `:core:domain`: `VenueDraft`, `VenueZone`, `PriceUnit`, `OwnerVenue`, `OwnerVenuesRepository`,
  `ValidateVenueDraftUseCase` (por paso), `SaveVenueDraftUseCase` (crea o actualiza según exista `id`),
  `PublishVenueUseCase`, `GetMyVenuesUseCase`.
- `:core:data`: `OwnerVenuesRepositoryImpl`.
- `:app`: `ui/ownervenues/` con `MyVenuesScreen` y `VenueWizardScreen` (tres pasos en un solo `ViewModel` con `SavedStateHandle`).
  La sección del propietario que hoy muestra «Próximamente» para «Mis salones» abre `MyVenuesScreen`.

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Asistente | Un `ViewModel` para los tres pasos; el `id` del salón se guarda en cuanto el `POST` responde |
| Duplicados | `POST` solo si no hay `id`; si el `POST` falla por red sin respuesta, el reintento consulta `GET /venues/mine` antes de volver a crear (ADR-04) |
| Dinero | `BigDecimal`; entrada en MXN con validación > 0 |
| Persistencia | `SavedStateHandle` (rotación/proceso); sin almacenamiento local del borrador |

## 3. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ⚠️ Pendiente | Flujo de alta de salón; falta aprobación. |
| II | ⚠️ Pendiente | Requiere `000` cerrado o excepción. |
| III | ✅ Cumple | Validación por paso en dominio. |
| IV | ⚠️ Parcial | Endpoints en borrador; `location`, `photos`, `price` y publicación pendientes (P-01–P-04). |
| V | ✅ Cumple | Sin datos del salón en logs. |
| VI–XII | ✅ Planificado | Pruebas en §8. |

## 4. Estructura

```text
core/network/.../api/OwnerVenuesApi.kt · dto/OwnerVenueDtos.kt
core/domain/.../VenueDraft.kt · OwnerVenue.kt · OwnerVenuesRepository.kt
core/domain/.../ValidateVenueDraftUseCase.kt · SaveVenueDraftUseCase.kt
core/domain/.../PublishVenueUseCase.kt · GetMyVenuesUseCase.kt
core/data/.../repository/OwnerVenuesRepositoryImpl.kt · di/OwnerVenuesModule.kt
app/.../ui/ownervenues/MyVenues{Screen,ViewModel}.kt
app/.../ui/ownervenues/VenueWizard{Screen,ViewModel}.kt · VenueWizardSteps.kt
app/.../demo/RoleWorkspace.kt · navigation/{Routes,VirGoNavHost}.kt
app/src/main/res/values/owner_venues_strings.xml
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | Crear el borrador al salir de P3 | Asegura un `id` temprano y permite retomar. |
| ADR-02 | Publicación directa (`publish`) | No hay rol de administrador (P-01). |
| ADR-03 | Sin fotos ni ubicación capturadas | Sin endpoint de carga ni mapa; depende de P-02/P-03. |
| ADR-04 | Verificar en `mine` antes de reintentar un `POST` sin respuesta | Evita duplicados sin exigir idempotencia al backend. |

## 6. Diseño

`VenueWizardUiState(step, draft, venueId, fieldErrors, saving, publishing, result)`.
Indicador «Paso N de 3»; acciones «Atrás»/«Siguiente» al pie; P5 con secciones
plegables por paso y «Editar».

## 7. DevSecOps y CI/CD

Pipeline existente; sin dependencias ni secretos nuevos.

## 8. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Validación por paso (límites 19/20/2000/2001, ≤ 20 en nombre corto, capacidad, precio, horas), crear-vs-actualizar, `ViewModel` del asistente |
| Integración | MockWebServer: `POST 201`, `PATCH 200`, `publish 200`, `400` con campos, `401`, `403`, `500`, timeout; un solo `POST` en todo el flujo |
| UI | CA-01 a CA-07; accesibilidad 320 dp/2.0 en ambos temas |
| Seguridad | Sin datos del salón en logs; la app no envía campos de autoridad del servidor |
| Mutación | Pitest en validación y decisión crear/actualizar |
| Humo | Publicar un salón de prueba en producción y verlo en el catálogo de `005` |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| `location`/`photos` obligatorios bloquean el alta | P-02/P-03 antes de implementar; la app no inventa coordenadas. |
| Salones de prueba visibles a clientes | Nombre claramente de prueba y retiro coordinado con backend. |

## 10. Fases

F1 red/dominio/datos (T2201–T2203) · F2 UI (T2204–T2206) · F3 calidad y humo (T2207–T2208).
