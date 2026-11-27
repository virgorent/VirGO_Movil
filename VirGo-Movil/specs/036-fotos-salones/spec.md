# Spec — 036 Fotos de los salones

| Campo | Valor |
|---|---|
| Módulo | `036-fotos-salones` |
| Versión | 1.0.0 |
| Estado | Borrador, pendiente de aprobación del usuario |
| Fecha | 2026-11-27 |
| Constitución | v1.2.0 |
| Rol | Cliente y propietario |
| Fuente | Prototipo local «VirGo Event Management App» (catálogo y detalle); esquema `venues.photos`; contrato §8 |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El backend ya guarda y devuelve `photos` de cada salón (`POST`/`PATCH /venues` aceptan `[{ url, order }]` y
`GET /venues` regresa la lista de URLs, la primera es la portada), pero la app no las dibuja. Este spec solo
**muestra** fotos que ya tienen URL; **subirlas** desde el teléfono sigue fuera (contrato §8, necesita S3).

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **Catálogo:** cada tarjeta muestra la portada (primera foto) arriba, en 16:9 con esquinas redondeadas. |
| RF-02 | **Detalle del salón:** carrusel deslizable con todas las fotos y un indicador «2 de 5». |
| RF-03 | **Mis salones (propietario):** cada salón muestra su portada en miniatura. |
| RF-04 | Mientras carga se ve un fondo neutro; si la URL falla o el salón no tiene fotos, un ícono de salón sobre ese fondo, nunca un hueco ni un error técnico. |
| RF-05 | Solo se cargan URLs `https`; las demás se ignoran como si no hubiera foto. La imagen se reduce al tamaño de pantalla y se guarda en caché de disco y memoria. |
| RF-06 | Cada foto tiene descripción para TalkBack: «Foto 2 de 5 de Jardín Los Encinos»; la portada del catálogo es decorativa (la tarjeta ya dice el nombre). |
| RF-07 | **Datos de demostración:** `scripts/seed-demo.py` agrega 3 fotos por salón con URLs públicas de imágenes libres de uso. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un salón con 3 fotos, **cuando** abro el catálogo, **entonces** su tarjeta muestra la primera. |
| CA-02 | **Dado** ese salón, **cuando** abro su detalle y deslizo, **entonces** veo las 3 y el indicador cambia de «1 de 3» a «3 de 3». |
| CA-03 | **Dado** un salón sin fotos o con una URL rota o `http`, **cuando** lo veo, **entonces** aparece el ícono de salón y la app no falla. |
| CA-04 | **Dado** TalkBack activo, **cuando** recorro el carrusel, **entonces** cada foto se anuncia con su número y el nombre del salón. |

## 4. Fuera de alcance

- Subir, ordenar o borrar fotos desde la app (contrato §8).
- Galería de proveedores (`011`/`026`) y foto de perfil.
- Tour 360° y plano del salón.

## 5. Dependencias

| Dependencia | Estado (2026-11-27) |
|---|---|
| `GET /venues`, `GET /venues/:id`, `GET /venues/mine` con `photos` | En la cadena hacia `main` |
| Librería Coil 3 (`coil-compose`, `coil-network-okhttp`) | Nueva; licencia Apache 2.0; pasa por `dependency-review` |
