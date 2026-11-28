# Spec — 006 Detalle del salón

| Campo | Valor |
|---|---|
| Módulo | `006-detalle-salon` |
| Versión | 2.1.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25) · v2.1.0 ajustada al contrato real (§9) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente y visitante |
| Fuente | `Checklist VirGo.md` (B13), backend `024-get-venues-id` y `API-HANDOFF.md` §9.5 |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Desde el catálogo beta (`034`) el cliente ve la lista de salones publicados, pero
no puede abrir ninguno: el botón de cada tarjeta dice «Próximamente». Este
módulo agrega el **detalle real del salón** (`GET /api/v1/venues/:id`) y lo
convierte en la puerta de entrada al flujo MVP: desde aquí se solicita una
cotización (`008`) o se reserva (`009`).

La versión 1.0.0 de este spec copiaba la maqueta completa (tour 360°, reseñas,
calendario, extras con precio, IA, chat y favoritos). Para el MVP se reduce a lo
que el backend puede servir; el resto queda fuera de alcance (§7).

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como visitante, quiero abrir un salón del catálogo y ver su información real para decidir si me interesa. |
| HT-02 | Como cliente con sesión, quiero ver el precio del salón y pasar a cotizar o reservar desde su detalle. |
| HT-03 | Como visitante, quiero que al tocar «Cotizar» o «Reservar» la app me lleve a crear cuenta o iniciar sesión sin perder el salón elegido. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[Catálogo]** La tarjeta de cada salón del catálogo beta sustituye «Próximamente» por **«Ver detalle»**, que abre B13 con su `id`. |
| RF-02 | **[B13]** Consulta `GET /api/v1/venues/:id` y muestra: nombre, zona, dirección, descripción, capacidad (solo si es > 0), categorías, amenidades y servicios incluidos que envíe el servidor. |
| RF-03 | **[B13]** Sin galería en el MVP: no existe endpoint de carga de fotos (backend `026` P-01) y la app no tiene librería de imágenes. B13 no muestra fotos ni imágenes de relleno (ver P-04). |
| RF-04 | **[B13]** Calificación: solo se muestra si `rating.count > 0` («4.8 · 12 reseñas»), sin enlace a reseñas. Con `count = 0` se muestra «Sin reseñas todavía». |
| RF-05 | **[B13]** Insignia «Verificado» solo si el servidor envía `verified: true`. |
| RF-06 | **[B13]** Precio: con sesión, se muestra el precio base y su unidad («$12,000 por día», «$2,500 por hora»; el servidor usa `day` u `hour`). Sin sesión, se muestra «Inicia sesión para ver precios» (regla A3 del backend). Si el servidor no envía precio, se muestra «Precio a cotizar». |
| RF-07 | **[B13]** Acciones: «Solicitar cotización» (abre `008`) y «Reservar» (abre `009`). Ambas requieren sesión con rol `client`. Mientras `008`/`009` no estén implementados se muestran deshabilitadas con un aviso, sin simular el flujo. |
| RF-08 | **[B13]** Sin sesión, las dos acciones llevan a registro; el detalle queda en la pila de navegación y desde registro o login se vuelve a él. (`RegisterRoute.venueId` es un entero de las muestras anteriores y no aplica a los `id` reales del servidor.) |
| RF-09 | **[B13]** Con sesión de rol `owner` o `provider`, las acciones no se muestran y aparece «Disponible para cuentas de cliente». |
| RF-10 | **[B13]** Estados: carga; error de red o 5xx con «Reintentar»; `404` como «Este salón ya no está disponible» con botón para volver al catálogo. |
| RF-11 | **[B13]** Botón de regreso al catálogo con área táctil de 48 dp. |
| RF-12 | Todos los textos están en `strings.xml`; los importes se formatean en MXN con la configuración regional del dispositivo. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** solo componentes y tokens de `:core:designsystem`; tema claro y oscuro (Art. VII). |
| RNF-02 | **Accesibilidad:** contraste AA, áreas táctiles de 48 dp, sin cortes a 320 dp con fuente 2.0. |
| RNF-03 | **Rendimiento:** la pantalla muestra contenido en < 1 s con respuesta de red local. |
| RNF-04 | **Honestidad de datos:** no se inventan fotos, distancias, reseñas, disponibilidad ni precios (decisión de la beta `034`). |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un salón publicado, **cuando** se toca «Ver detalle» en el catálogo, **entonces** se abre B13 con los datos de `GET /venues/:id`. |
| CA-02 | **Dado** un salón con capacidad 0 y sin reseñas, **cuando** se abre, **entonces** no aparece galería ni capacidad, y se lee «Sin reseñas todavía». |
| CA-03 | **Dado** un visitante sin sesión, **cuando** abre el detalle, **entonces** no ve el precio, y «Solicitar cotización» o «Reservar» lo llevan a registro con el salón como contexto. |
| CA-04 | **Dado** un cliente con sesión, **cuando** abre el detalle, **entonces** ve el precio base y su unidad, y las acciones abren `008` y `009` con el `id` del salón. |
| CA-05 | **Dado** una sesión de propietario o proveedor, **cuando** abre el detalle, **entonces** no ve las acciones de cliente. |
| CA-06 | **Dado** un `404`, **cuando** llega, **entonces** se lee «Este salón ya no está disponible» y se puede volver al catálogo. |
| CA-07 | **Dado** que no hay conexión o hay 5xx, **cuando** se abre, **entonces** se muestra un error claro con «Reintentar», sin texto técnico. |
| CA-08 | **Dado** el detalle a 320 dp con fuente 2.0 en ambos temas, **cuando** se revisa, **entonces** no hay cortes ni controles < 48 dp. |

## 6. Seguridad y privacidad

- El endpoint es público; el Bearer se envía si existe sesión (para recibir precios).
- No se registran en logs el `id` del salón junto con datos de la cuenta.

## 7. Fuera de alcance

- Tour 360° (C13), reseñas (C14), calendario de disponibilidad, horas ajustables,
  servicios adicionales con interruptor y total en vivo.
- Favoritos, chat con el salón, «Simular con IA» y proveedores aliados.
- Mapa del salón y distancia.
- Galería de fotos (P-04).
- Todo lo anterior queda para módulos posteriores; no se muestra ni como «Próximamente» en B13.

## 8. Supuestos, dependencias y preguntas abiertas

**Endpoints que consume:**

| Endpoint | Spec backend | Estado observado (2026-11-25) |
|---|---|---|
| `GET /api/v1/venues/:id` | `024-get-venues-id` (Borrador) | ❌ no desplegado |
| `GET /api/v1/venues` | `007-venues-catalog` | ✅ desplegado (catálogo vacío) |

**Dependencias del móvil:** PR #35 (`034` beta) fusionado; `000` cerrado o excepción aprobada (Art. II.2).

| ID | Pregunta | Quién | Propuesta |
|---|---|---|---|
| P-01 | Forma exacta del JSON de `GET /venues/:id`. El handoff §9.5 no la fija. | Backend | Mismo objeto que `GET /venues` más `description`, `address`, `amenities`, `services`, `verified`. La app tolera campos ausentes. |
| P-02 | ¿El backend oculta `price` sin Bearer (regla A3) o lo oculta la app? | Backend | El backend lo omite; la app además no lo muestra sin sesión. |
| P-03 | ¿`services` es lista de textos u objetos con precio? | Backend | En el MVP solo se muestran sus nombres. |
| P-04 | ¿Fotos en el MVP? Requiere endpoint de carga (backend) y una librería de imágenes (Coil), dependencia nueva sujeta a revisión | Usuario + backend | Fuera del MVP; se añade en un spec propio. |

Mientras P-01 siga abierta, la implementación usa MockWebServer con la propuesta (Art. VIII.6).

## 9. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`) y ejecutado en local; **aún no desplegado**.

- `GET /api/v1/venues/:id` (anónimo) solo devuelve salones `published`; un borrador responde `404`, también a su dueño (el dueño usa `GET /venues/mine`).
- `price` solo viene con sesión (regla A3); sin sesión se omite. Respuesta: `price: {baseAmount, unit}` con `unit` `hour` o `day`; `minimumHours` en la raíz.
- `photos` es una lista de URLs de texto (hoy vacía: no hay carga de fotos); `rating: {average, count}`.
- `zone` usa colonias de Querétaro (`Juriquilla`, `Centro`, `El Refugio`, `Santa Fe`, `Antea`, `Jurica`, `Corregidora`, `El Marqués`); `categories` y `amenities` son listas cerradas del servidor (ver `022` RF-03).
