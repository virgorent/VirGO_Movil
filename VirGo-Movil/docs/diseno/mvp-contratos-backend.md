# MVP cliente — contratos que el móvil necesita del backend

Fecha: 2026-11-25. Alcance decidido por el usuario (ver `memoria.md`, entrada
del 2026-11-25): `030` cuenta/sesión, `005` explorar, `006` detalle, `008`
cotizar, `009` reservar y `022` propietario registra salón.

El móvil implementará contra MockWebServer con el contrato acordado aquí y se
conectará a producción cuando cada endpoint esté desplegado. Cualquier cambio
de contrato se registra en `memoria.md` de **ambos** proyectos.

Fuente de los endpoints: secciones de contrato de cada `spec.md`/`plan.md` del
móvil. Estado en producción: consulta del 2026-11-24/25 a
`https://rcof55a3c9.execute-api.us-east-1.amazonaws.com` y OpenAPI publicado.

## Resumen por prioridad

| Prioridad | Endpoint | Módulo | Producción |
|---|---|---|---|
| **P0** | `GET /api/v1/users/me` | `030` | ✅ desplegado (OpenAPI sin esquema de respuesta) |
| **P0** | `POST /api/v1/auth/logout` | `030` | ✅ desplegado (controlador 204, OpenAPI dice 200) |
| **P0** | `GET /api/v1/venues` | `005` | ✅ desplegado, catálogo vacío |
| **P0** | `GET /api/v1/venues/:id` | `006` | ❌ no existe (spec `024` del backend en borrador) |
| **P0** | `POST /api/v1/venues` | `022` | ❌ no existe |
| **P0** | `POST /api/v1/venues/:id/submit` o `/publish` | `022` | ❌ no existe — **sin esto el catálogo seguirá vacío** |
| **P0** | `POST /api/v1/quotes` | `008` | ✅ declarado en OpenAPI (no probado) |
| **P0** | `GET /api/v1/quotes` · `GET /api/v1/quotes/:id` | `008` | ❌ no existe |
| **P0** | `POST /api/v1/bookings` | `009` | ✅ declarado en OpenAPI (no probado) |
| **P0** | `GET /api/v1/bookings/:id` | `009` | ❌ no existe |
| P1 | `PATCH /api/v1/venues/:id` | `022` | ❌ no existe |
| P1 | `POST /api/v1/quotes/:id/accept` · `/reject` | `008` | ❌ no existe |
| P1 | `GET /api/v1/venues/:id/availability` | `006` | ❌ no existe |
| P2 | `GET /api/v1/venues/:id/reviews` | `006` | ❌ no existe |
| P2 | `GET/POST/DELETE /api/v1/favorites…` | `005`/`006` | ❌ 404 |
| P2 | `GET /api/v1/notices` | `005` | ❌ 404 |
| P2 | `POST /api/v1/conversations` | `006` | ❌ no existe |
| **Decidir** | `POST /api/v1/bookings/:id/payments` · `GET/POST /api/v1/payment-cards` | `009` | ❌ no existe |

- **P0:** sin él no hay flujo MVP de punta a punta.
- **P1:** necesario para un MVP usable; el móvil puede mostrar «Próximamente» mientras tanto.
- **P2:** fuera del MVP; se queda en «Próximamente».

## Decisiones que hay que tomar con backend

1. **Pago dentro de la app (`009`).** Requiere proveedor de pagos, PCI y
   tarjetas guardadas. Propuesta: el MVP **reserva sin cobrar en la app**
   (estado «pendiente de pago» que confirma el propietario) y el pago queda
   para una fase posterior. Reduce semanas de trabajo y riesgo.
2. **Publicación de salones (`022`).** ¿`submit` (revisión por un
   administrador) o `publish` directo? Para la beta, `publish` directo por el
   propietario verificado es lo más rápido.
3. **Contratos pendientes de corregir:** `POST /auth/logout` (204 frente a 200
   en OpenAPI) y esquema de `GET /users/me` en OpenAPI.
4. **Forma del detalle de salón:** extender el objeto de `GET /venues` (ya
   tiene `location` GeoJSON y `rating {average,count}`) con descripción,
   fotos (URLs), servicios, precios y reglas.

## Qué se pide a backend

Para cada endpoint P0/P1: URL, método, cuerpo de petición, respuesta `2xx` de
ejemplo y errores en Problem Details (`400/401/403/404/409`), con la misma
convención del contrato base (`/api/v1`, `camelCase`, fechas UTC, JWT Bearer).
Con eso el móvil puede avanzar en paralelo.

## Límites

- Este documento no aprueba specs ni amplía el alcance de `034`.
- El estado «declarado en OpenAPI» no garantiza que el endpoint funcione: no se
  han creado cotizaciones ni reservas reales.
