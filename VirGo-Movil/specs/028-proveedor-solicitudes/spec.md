# Spec — 028 Solicitudes del proveedor

| Campo | Valor |
|---|---|
| Módulo | `028-proveedor-solicitudes` |
| Versión | 2.0.0 |
| Estado | Borrador v2.0.0, recortado; pendiente de aprobación del usuario |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Proveedor |
| Fuente | `Checklist VirGo.md` (S4, S5, S10); specs del backend en `VirGo-API/specs` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El proveedor recibe las cotizaciones que le piden los clientes y las responde con sus paquetes, igual que el propietario (`024`). El backend tiene estos endpoints especificados pero aún no implementados, así que la pantalla se construye contra esos specs, se prueba con MockWebServer (Art. VIII.6) y dice «llegará pronto» mientras no existan.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[S4] Lista:** solicitudes con cliente, tipo y fecha del evento, invitados y estado (`GET /quotes`). |
| RF-02 | **[S5] Responder:** conceptos precargados con sus paquetes; puede editar concepto, cantidad y precio, y agregar o quitar. Total y anticipo se calculan en la app y se envían con `POST /quotes/:id/response` y una nota de hasta 1000 caracteres. |
| RF-03 | **[S5] Agenda:** si el cliente acepta, el servicio aparece en la agenda (`027`) porque el backend crea el servicio con su `quoteId`. |
| RF-04 | **[S10] Mensajes:** conversación con el cliente sobre el contrato de mensajes (`020`). |
| RF-05 | Cuando un endpoint aún no existe (`404`/`501`), la pantalla dice «Esta parte llegará pronto» sin error técnico; `401` cierra la sesión; sin conexión, error con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un paquete de $12 000 y 2 conceptos, **cuando** cambio una cantidad, **entonces** el total se recalcula antes de enviar. |
| CA-02 | **Dado** una solicitud pendiente, **cuando** la respondo, **entonces** pasa a «Respondida». |

## 4. Fuera de alcance

- Rechazar con motivo y plantillas de respuesta.

## 5. Dependencias

| Endpoint | Spec del backend (estado 2026-11-27) |
|---|---|
| `GET /quotes`, `GET /quotes/:id`, `POST /quotes/:id/response` del proveedor | `060`, `061`, `062` (📝 especificado) |
