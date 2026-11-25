# Spec — 022 Propietario: registrar salón

| Campo | Valor |
|---|---|
| Módulo | `022-propietario-registrar-salon` |
| Versión | 2.0.0 |
| Estado | Propuesto para aprobación (lote MVP, 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Propietario |
| Fuente | `Checklist VirGo.md` (P3, P4, P5), backend `025`, `026`, `027`, `029` y esquema `venues` de `024` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El catálogo de producción está **vacío**: nadie puede publicar salones. Sin este
módulo el flujo del cliente (`005`→`006`→`008`/`009`) no tiene nada que mostrar.
Aquí el propietario registra su salón en tres pasos, lo guarda como borrador
(`POST`/`PATCH /api/v1/venues`) y lo publica (`POST /api/v1/venues/:id/publish`)
para que aparezca en el catálogo.

La v1.0.0 incluía fotos, servicios con precio y «actividad de ejemplo» generada
al publicar. En el MVP no hay carga de fotos (no existe endpoint) ni actividad
simulada.

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como propietario, quiero registrar mi salón con sus datos, capacidad y precio para que los clientes lo encuentren. |
| HT-02 | Como propietario, quiero guardar un borrador y terminarlo después. |
| HT-03 | Como propietario, quiero ver mis salones y saber cuáles están publicados. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[Mis salones]** El espacio del propietario muestra «Mis salones» (`GET /api/v1/venues/mine`): nombre, zona y estado (`draft` → «Borrador», `review` → «En revisión», `published` → «Publicado»; desconocido → «En revisión»). Vacío: «Aún no registras salones» con «Registrar salón». |
| RF-02 | **[P3 · Datos]** Nombre (≤ 100), nombre corto (≤ 20), descripción (20–2000), dirección (≤ 200) y zona (Norte, Sur, Centro, Oriente, Poniente). |
| RF-03 | **[P4 · Capacidad y precio]** Capacidad (entero ≥ 1), precio base (MXN > 0), unidad («por hora» o «por evento»), horas mínimas (entero ≥ 1), categorías y amenidades (selección múltiple de listas fijas en `strings.xml`) y servicios incluidos (lista de textos, opcional). |
| RF-04 | **[P5 · Revisar]** Resumen de todo lo capturado con «Editar» por paso, «Guardar borrador» y «Publicar». |
| RF-05 | La primera vez que se guarda (al pasar de P3 a P4 o con «Guardar borrador») se crea el salón con `POST /api/v1/venues` (`201`, estado `draft`); los cambios posteriores usan `PATCH /api/v1/venues/:id`. Nunca se crean dos salones por el mismo registro. |
| RF-06 | «Publicar» guarda los cambios pendientes y llama `POST /api/v1/venues/:id/publish`. Ante éxito: «Tu salón ya aparece en el catálogo» y vuelta a «Mis salones». |
| RF-07 | Validación local por campo antes de avanzar de paso; errores `400` del servidor junto a su campo; `401` cierra sesión (`030`); `403` «Solo las cuentas de propietario pueden registrar salones»; red/5xx con reintento sin perder lo capturado. |
| RF-08 | Tocar un borrador en «Mis salones» reabre el asistente con sus datos (leídos de `GET /api/v1/venues/:id`) para continuar. |
| RF-09 | Sin fotos en el MVP: P3 muestra «Las fotos se podrán agregar pronto» y no ofrece «Usar fotos de ejemplo». |
| RF-10 | Solo el rol `owner` ve «Mis salones» y el asistente. Textos en `strings.xml`. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño; indicador de paso «1 de 3»; claro/oscuro. |
| RNF-02 | **Accesibilidad:** nombres de campo persistentes; errores anunciados; 48 dp; sin cortes a 320 dp/fuente 2.0. |
| RNF-03 | **Fiabilidad:** el borrador no se pierde al rotar ni al fallar la red. |
| RNF-04 | **Honestidad:** sin actividad, reseñas ni reservaciones de ejemplo al publicar. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un propietario sin salones, **cuando** abre «Mis salones», **entonces** ve el estado vacío y «Registrar salón». |
| CA-02 | **Dado** P3 con descripción de 19 caracteres o sin zona, **cuando** intenta avanzar, **entonces** ve el error en el campo y no avanza. |
| CA-03 | **Dado** P3 válido, **cuando** avanza, **entonces** se hace un solo `POST /venues` y los pasos siguientes usan `PATCH`. |
| CA-04 | **Dado** P5, **cuando** toca «Guardar borrador», **entonces** el salón aparece en «Mis salones» como «Borrador» y no en el catálogo. |
| CA-05 | **Dado** P5, **cuando** toca «Publicar» y el servidor responde éxito, **entonces** el salón aparece como «Publicado» y en `GET /venues` del catálogo. |
| CA-06 | **Dado** un fallo de red al guardar, **cuando** reintenta, **entonces** conserva los datos y no se duplica el salón. |
| CA-07 | **Dado** un borrador, **cuando** lo abre desde «Mis salones», **entonces** el asistente muestra sus datos y puede publicarlo. |
| CA-08 | **Dado** el asistente a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisa, **entonces** no hay cortes ni controles < 48 dp. |

## 6. Seguridad y privacidad

- Bearer por `AuthInterceptor`; el servidor exige rol `owner` y propiedad del salón.
- La app no envía `rating`, `verified`, `status`, `ownerId` ni fechas (el servidor los ignora o asigna).
- No se registran en logs dirección ni descripción.

## 7. Fuera de alcance

- Fotos, planos y tour 360° (sin endpoint de carga: backend `026` P-01).
- Servicios adicionales con precio, configuraciones de montaje y proveedores aliados.
- Revisión por un administrador (`submit`, backend `028`): ver P-01.
- Calendario, solicitudes y herramientas del propietario (`023`–`025`).

## 8. Supuestos, dependencias y preguntas abiertas

**Endpoints que consume:**

| Endpoint | Spec backend | Estado observado (2026-11-25) |
|---|---|---|
| `GET /api/v1/venues/mine` | `025-get-venues-mine` | ❌ no desplegado |
| `POST /api/v1/venues` | `026-post-venues` (Borrador) | ❌ no desplegado |
| `PATCH /api/v1/venues/:id` | `027-patch-venues-id` (Borrador) | ❌ no desplegado |
| `GET /api/v1/venues/:id` | `024-get-venues-id` (Borrador; el dueño ve sus borradores) | ❌ no desplegado |
| `POST /api/v1/venues/:id/publish` | `029-post-venues-id-publish` (requiere decisión) | ❌ no desplegado |

**Dependencias del móvil:** `030` (rol y 401); `000` cerrado o excepción.

| ID | Pregunta | Quién | Propuesta |
|---|---|---|---|
| P-01 | ¿Quién aprueba la publicación? No hay rol de administrador (backend `028`/`029` P-01). | Usuario + backend | MVP: **publicación directa** por el propietario (`publish`); la revisión se agrega después. |
| P-02 | El esquema exige `location` (GeoJSON), pero la app no tiene mapa ni selector de ubicación. | Backend | `location` opcional en el MVP o geocodificación de `address` en el servidor. |
| P-03 | El esquema exige `photos`. | Backend | Aceptar lista vacía en el MVP. |
| P-04 | Forma de `price` (`{base, unit, minHours}`) y valores de `unit`. | Backend | `unit`: `hour` o `event`, coherente con el cálculo de `POST /bookings`. |
| P-05 | Listas de categorías y amenidades. | Usuario | Lista inicial fija en la app; el servidor las guarda como texto. |
