# Spec — 026 Perfil del proveedor

| Campo | Valor |
|---|---|
| Módulo | `026-proveedor-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Proveedor |
| Fuente | `Checklist VirGo.md` (S2, S7); specs del backend en `VirGo-API/specs` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El proveedor arma su perfil público y lo publica para aparecer en el catálogo de los clientes (`011`). El backend tiene estos endpoints especificados pero aún no implementados, así que la pantalla se construye contra esos specs, se prueba con MockWebServer (Art. VIII.6) y dice «llegará pronto» mientras no existan.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[S2] Crear y editar:** nombre del negocio, categoría, descripción (mín. 20), cobro por persona o por evento, tiempo de respuesta y paquetes (nombre, precio, qué incluye). Crear con `POST /providers` (queda en borrador); editar con `PATCH /providers/me`. |
| RF-02 | **[S2] Validar:** no deja publicar si falta un campo obligatorio o no hay al menos un paquete, y marca qué falta. |
| RF-03 | **[S2] Publicar:** «Publicar» cambia el estado a `published` con `PATCH /providers/me`; desde entonces aparece en `011`. |
| RF-04 | **[S7] Zonas:** selección de zonas de cobertura desde la lista del catálogo de zonas. |
| RF-05 | Cuando un endpoint aún no existe (`404`/`501`), la pantalla dice «Esta parte llegará pronto» sin error técnico; `401` cierra la sesión; sin conexión, error con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un perfil sin paquetes, **cuando** toco «Publicar», **entonces** no se envía y se marca «Agrega al menos un paquete». |
| CA-02 | **Dado** un perfil completo, **cuando** lo publico, **entonces** se envía `status: "published"` y la pantalla dice «Publicado». |
| CA-03 | **Dado** un proveedor que ya tiene perfil, **cuando** abro «Mi perfil», **entonces** veo sus datos (`GET /providers/me`) para editarlos. |

## 4. Fuera de alcance

- [S6] Subir fotos de la galería (contrato §8) y la opción «fotos de ejemplo».
- Zonas sobre el mapa.

## 5. Dependencias

| Endpoint | Spec del backend (estado 2026-11-27) |
|---|---|
| `GET /providers/me`, `POST /providers`, `PATCH /providers/me` | `036`, `037`, `038` (📝 especificado) |
