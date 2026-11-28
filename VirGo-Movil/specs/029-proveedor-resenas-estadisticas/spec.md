# Spec — 029 Reseñas y estadísticas del proveedor

| Campo | Valor |
|---|---|
| Módulo | `029-proveedor-resenas-estadisticas` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Proveedor |
| Fuente | `Checklist VirGo.md` (S8, S9); specs del backend en `VirGo-API/specs` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El proveedor ve qué opinan sus clientes y cómo le va por mes. El backend tiene estos endpoints especificados pero aún no implementados, así que la pantalla se construye contra esos specs, se prueba con MockWebServer (Art. VIII.6) y dice «llegará pronto» mientras no existan.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[S8] Reseñas:** promedio, número de reseñas y cada opinión (`GET /providers/:id/reviews`). |
| RF-02 | **[S9] Estadísticas:** por mes, en vista de 3 o 6 meses, solicitudes recibidas, aceptadas y conversión, calculadas en la app con `GET /quotes`. |
| RF-03 | Cuando un endpoint aún no existe (`404`/`501`), la pantalla dice «Esta parte llegará pronto» sin error técnico; `401` cierra la sesión; sin conexión, error con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** 4 solicitudes y 1 aceptada en un mes, **cuando** abro «Estadísticas», **entonces** la conversión es 25 %. |
| CA-02 | **Dado** que no hay reseñas, **cuando** abro «Reseñas», **entonces** veo el estado vacío. |

## 4. Fuera de alcance

- Visitas al perfil (`079`, requiere contar visitas en el backend).
- Responder reseñas.

## 5. Dependencias

| Endpoint | Spec del backend (estado 2026-11-27) |
|---|---|
| `GET /providers/:id/reviews` | `073` (📝 especificado) |
| `GET /quotes` | `060` (📝 especificado) |
