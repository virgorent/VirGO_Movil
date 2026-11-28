# Spec — 014 Mi evento

| Campo | Valor |
|---|---|
| Módulo | `014-mi-evento` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario («hazlos todos de una vez tienes mi aprobacion», 2026-11-25) |
| Fecha | 2026-11-25 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Pantallas | D27 |
| Backend | `GET /events`, `GET /events/:id` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Centro de planeación del cliente: sus eventos y, por evento, los datos clave con accesos a editar (`007`), invitados (`016`) y pendientes (`017`). El backend no guarda proveedores, pagos por evento ni gasto: la barra de pasos, la gráfica de gasto, la lista de proveedores y el chat de la v1.0.0 quedan fuera.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[D27]** «Mi evento» lista `GET /api/v1/events` (por fecha) con nombre, tipo, fecha e invitados; vacío con «Crear evento»; carga y error con reintento. |
| RF-02 | **[D27]** El detalle (`GET /api/v1/events/:id`) muestra tipo, nombre, días que faltan (o «Ya pasó»), fecha, salón si hay, invitados, horas y presupuesto en MXN. |
| RF-03 | **[D27]** Accesos: Editar (`007`), Invitados (`016`) y Pendientes (`017`). |
| RF-04 | Las rutas «Mi Evento», «Invitados» y «Pendientes» del espacio del cliente abren esta pantalla en lugar de la sección «Próximamente». |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** sistema de diseño, claro/oscuro, campos con nombre persistente; textos en `strings.xml`. |
| RNF-02 | **Accesibilidad:** errores asociados a su campo; 48 dp; sin cortes a 320 dp/fuente 2.0. |
| RNF-03 | **Seguridad:** sin datos personales en logs; Bearer por `AuthInterceptor`. |
| RNF-04 | **Honestidad:** solo datos del servidor; nada de muestra. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** eventos del cliente, **cuando** abre «Mi evento», **entonces** ve la lista real y al tocar uno ve sus datos y accesos. |
| CA-02 | **Dado** que no tiene eventos, **cuando** abre «Mi evento», **entonces** ve el vacío con «Crear evento». |
| CA-03 | **Dado** las pantallas a 320 dp/fuente 2.0 en ambos temas, **cuando** se revisan, **entonces** no hay cortes ni controles < 48 dp. |

## 5. Fuera de alcance

- Barra de pasos, gráfica de gasto, proveedores con estado de pago, calificar y chat (sin backend por evento).
- 12 accesos rápidos: solo los que tienen backend.

## 6. Contrato verificado (2026-11-25)

Leído en `VirGO_Backend` `feat/api-mobile-priority` (`7cf6124`), `EventsController`; **aún no desplegado**. Cualquier usuario con sesión puede usarlo.

- `GET /api/v1/events` → `{data: [Event]}` (propios y donde es colaborador, por fecha). `GET /events/:id` → `Event` o `404`.
