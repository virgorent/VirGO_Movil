# Spec — 011 Proveedores para el cliente

| Campo | Valor |
|---|---|
| Módulo | `011-proveedores-cliente` |
| Versión | 2.0.0 |
| Estado | Borrador v2.0.0, recortado; pendiente de aprobación del usuario |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C19, B18, C20); specs del backend en `VirGo-API/specs` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente encuentra proveedores (catering, música, fotografía, decoración, mobiliario), compara 2 o 3 y les pide cotización igual que a un salón. El backend tiene estos endpoints especificados pero aún no implementados, así que la pantalla se construye contra esos specs, se prueba con MockWebServer (Art. VIII.6) y dice «llegará pronto» mientras no existan.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C19] Catálogo:** filtros por categoría (Catering, Música, Fotografía, Decoración, Mobiliario) y búsqueda por nombre; tarjetas con nombre, categoría, precio desde, calificación y zonas. Solo publicados (`GET /providers`). |
| RF-02 | **[B18] Comparar:** marcar 2 o 3 proveedores y verlos lado a lado: precio desde, cobro (por persona o por evento), paquetes, calificación, tiempo de respuesta y zonas. Se resalta el de mejor calificación; la comparación se calcula en la app. |
| RF-03 | **[C20] Detalle:** descripción, zonas, paquetes con precio y qué incluyen, portada y galería si tienen URL (componentes de `036`), y reseñas (`GET /providers/:id/reviews`). |
| RF-04 | **[C20] Cotizar:** formulario igual al de salones (`008`) con `target: { type: "provider", id }`; la cotización aparece en «Mis cotizaciones». |
| RF-05 | **[C20] Mensaje:** abre la conversación con el proveedor sobre el contrato de mensajes (`020`). |
| RF-06 | Cuando un endpoint aún no existe (`404`/`501`), la pantalla dice «Esta parte llegará pronto» sin error técnico; `401` cierra la sesión; sin conexión, error con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** proveedores publicados de dos categorías, **cuando** filtro «Música», **entonces** solo veo los de música. |
| CA-02 | **Dado** 3 proveedores marcados, **cuando** abro «Comparar», **entonces** veo sus datos lado a lado y no me deja marcar un cuarto. |
| CA-03 | **Dado** el detalle de un proveedor, **cuando** envío «Cotizar», **entonces** se manda con `target.type = "provider"` y aparece en «Mis cotizaciones». |
| CA-04 | **Dado** que `GET /providers` responde `404`, **cuando** abro «Proveedores», **entonces** dice «llegará pronto». |

## 4. Fuera de alcance

- «Agregar a Mi Evento» un paquete y favoritos de proveedores.
- Proveedores aliados de un salón (P15).

## 5. Dependencias

| Endpoint | Spec del backend (estado 2026-11-27) |
|---|---|
| `GET /providers`, `GET /providers/:id` | `034`, `035` (📝 especificado) |
| `GET /providers/:id/reviews` | `073` (📝 especificado) |
| `POST /quotes` con `target.type = "provider"` | `008` (hoy solo acepta salón) |
