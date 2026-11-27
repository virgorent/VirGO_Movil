# Spec — 010 Mis reservaciones: buscar por folio y detalle

| Campo | Valor |
|---|---|
| Módulo | `010-reservaciones` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26, «Aprobados, impleméntalos») |
| Fecha | 2026-11-26 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C32, C33) y backend `066`, `067` y `068` en la cadena hacia `main` (`#39` + `#46` + `#47`) |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

La v1.0.0 prometía cancelar con reembolso del 50 % y reprogramar sin costo. **El backend no tiene esas
operaciones**: en la cadena hacia `main` solo existen `GET /bookings`, `GET /bookings/:id` y
`GET /bookings/by-folio/:folio`; `POST /bookings/:id/cancel` (`069`) tiene spec, pero no tiene controlador. Además, la
regla de reembolso es una decisión de negocio del salón que nadie ha acordado.

La lista «Mis reservaciones» y el comprobante ya los cubre `009` (RF-08 y RF-09). Este spec solo agrega lo que el
backend sí permite hoy:

1. **[C32]** Buscar una reservación propia por su folio (`VG-AAAA-NNNN`).
2. **[C33]** Un detalle de solo lectura que explica, sin inventar reglas, que los cambios se tratan con el salón.

## 2. Historias

| ID | Historia |
|---|---|
| H-01 | Como cliente, quiero escribir el folio que me dio el salón y ver esa reservación, sin recorrer toda la lista. |
| H-02 | Como cliente, quiero saber qué puedo hacer si necesito cancelar o cambiar la fecha, sin que la app me prometa un reembolso. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C32]** «Mis reservaciones» (`009` RF-08) muestra arriba de la lista un campo «Buscar por folio» con el botón «Buscar». |
| RF-02 | **[C32]** La app quita espacios, pasa el texto a mayúsculas y valida el formato `VG-` + 4 dígitos + `-` + 4 dígitos antes de enviar. Si no cumple, muestra «Escribe el folio como aparece en tu comprobante, por ejemplo VG-2026-0001» y no llama al servidor. |
| RF-03 | **[C32]** Con un folio válido envía `GET /api/v1/bookings/by-folio/:folio` con el Bearer de la sesión; el folio va como segmento de ruta codificado, nunca en la query. |
| RF-04 | **[C32]** `200` abre el detalle (RF-06) con los datos del servidor. `404` muestra «No encontramos ese folio en tus reservaciones», sin decir si existe en otra cuenta. `401` cierra la sesión (`030`). Red, timeout o 5xx muestran un error con «Reintentar». |
| RF-05 | **[C32]** El botón se desactiva mientras busca. Borrar el campo vuelve a la lista sin recargarla. |
| RF-06 | **[C33]** El detalle de una reservación (desde la lista o desde la búsqueda) muestra folio, salón, fecha, horas, total, anticipo y estado, con los textos de estado de `009` RF-10. Reutiliza el comprobante de `009`; no crea otra pantalla de datos. |
| RF-07 | **[C33]** Debajo de los datos aparece el bloque informativo «¿Necesitas cancelar o cambiar la fecha?»: «Por ahora los cambios se acuerdan directamente con el salón. Ten a la mano tu folio.». No hay botones de cancelar ni de reprogramar, ni montos de reembolso. |
| RF-08 | Todos los textos están en `strings.xml`; montos en MXN y fechas en formato local, como en `009`. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** solo componentes de `:core:designsystem`; el bloque de RF-07 usa el estilo informativo, no el de error. |
| RNF-02 | **Accesibilidad:** el campo tiene etiqueta y texto de error leídos por TalkBack; áreas táctiles de 48 dp; fuente 2.0 sin cortes a 320 dp. |
| RNF-03 | **Seguridad:** el folio no se escribe en logs; el Bearer solo vive en `TokenStore`; un `404` no distingue «no existe» de «es de otra cuenta» (lo garantiza el backend y la app no lo contradice). |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un folio propio, **cuando** lo busco, **entonces** veo su detalle con los datos del servidor. |
| CA-02 | **Dado** un texto con formato inválido, **cuando** pulso «Buscar», **entonces** veo el aviso de RF-02 y no sale ninguna petición. |
| CA-03 | **Dado** un folio que no es mío o no existe, **cuando** lo busco, **entonces** veo «No encontramos ese folio en tus reservaciones». |
| CA-04 | **Dado** que no hay conexión, **cuando** busco, **entonces** veo un error con «Reintentar» y puedo seguir viendo la lista. |
| CA-05 | **Dado** el detalle, **cuando** lo abro, **entonces** veo el bloque de RF-07 y ningún botón de cancelar, reprogramar o reembolsar. |

## 6. Fuera de alcance

Queda pendiente para un spec posterior, cuando el backend lo tenga y el negocio lo acuerde:

- Cancelar (`069`), reprogramar, reembolsos y cualquier regla de días de anticipación.
- Pagar el anticipo en la app (`070`, `071`) y tarjetas.
- Confirmación o rechazo por el propietario, y la vista de reservaciones del propietario.
- Filtros, pestañas «Próximas / Pasadas» y notificaciones.

## 7. Dependencias

| Endpoint | Spec backend | Estado (2026-11-26) |
|---|---|---|
| `GET /api/v1/bookings` | `066-get-bookings` | En la cadena hacia `main`; ya lo usa `009` |
| `GET /api/v1/bookings/:id` | `067-get-bookings-id` | En la cadena hacia `main`; ya lo usa `009` |
| `GET /api/v1/bookings/by-folio/:folio` | `068-get-bookings-by-folio-folio` | En la cadena hacia `main`; folio de hasta 32 caracteres; `404` si no es visible para el usuario |
| `POST /api/v1/bookings/:id/cancel` | `069-post-bookings-id-cancel` | **Sin controlador**: fuera de alcance |

Producción sigue con el backend anterior hasta el despliegue; mientras tanto se prueba con MockWebServer
(Art. VIII.6) y con el backend local.

## 8. Preguntas abiertas

| ID | Pregunta | Quién decide | Propuesta |
|---|---|---|---|
| P-01 | ¿Qué política de cancelación tendrá VirGo? | Usuario + propietarios | Fuera del MVP; RF-07 no promete nada. |
| P-02 | ¿El texto de RF-07 basta para la revisión del profesor? | Usuario | Sí; si no, se ajusta solo el texto. |
