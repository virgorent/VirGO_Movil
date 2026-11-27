# Plan — 036 Fotos de los salones

| Campo | Valor |
|---|---|
| Módulo | `036-fotos-salones` |
| Versión | 1.0.0 |
| Estado | Borrador, pendiente de aprobación del usuario |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
| Dependencias | Coil 3 en `libs.versions.toml`, usando el mismo `OkHttpClient` de la app (sin cabecera de sesión en imágenes). |
| core:designsystem | `VenuePhoto` (portada con fondo e ícono de respaldo) y `VenuePhotoPager` (carrusel con indicador). |
| app | Catálogo, detalle del salón y «Mis salones» usan esos componentes. |
| scripts | `seed-demo.py` envía `photos` al crear cada salón. |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Filtro de URLs (`https` sí, `http`/vacía no) e indicador «n de m». |
| UI | Respaldo sin fotos, carrusel con 3 páginas y descripciones de TalkBack, con un cargador de imágenes falso (sin red). |
| Emulador | Catálogo y detalle con los datos de demostración. |
