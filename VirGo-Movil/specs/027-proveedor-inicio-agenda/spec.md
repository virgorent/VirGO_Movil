# Spec — 027 Inicio y agenda del proveedor

| Campo | Valor |
|---|---|
| Módulo | `027-proveedor-inicio-agenda` |
| Versión | 2.0.0 |
| Estado | Borrador v2.0.0, recortado; pendiente de aprobación del usuario |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Proveedor |
| Fuente | `Checklist VirGo.md` (S3, H74); specs del backend en `VirGo-API/specs` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Tablero del proveedor y su agenda semanal de servicios contratados. El backend tiene estos endpoints especificados pero aún no implementados, así que la pantalla se construye contra esos specs, se prueba con MockWebServer (Art. VIII.6) y dice «llegará pronto» mientras no existan.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[S3] Inicio:** solicitudes nuevas, cotizaciones enviadas y aceptadas, y los próximos 3 servicios de la agenda, calculados en la app con `GET /quotes` y `GET /providers/me/gigs`. Accesos a «Solicitudes», «Agenda» y «Mi perfil». |
| RF-02 | **[H74] Agenda semanal:** servicios de la semana por día, con flechas para cambiar de semana. |
| RF-03 | **[H74] Agregar y editar:** nombre del evento, cliente, lugar, fecha, hora de inicio y fin y monto opcional (`POST /providers/me/gigs`, `PATCH /providers/me/gigs/:id`). |
| RF-04 | **[H74] Empalmes:** si dos servicios del mismo día se cruzan, ambos se marcan en rojo con «Se empalma con …»; se calcula en la app. |
| RF-05 | Cuando un endpoint aún no existe (`404`/`501`), la pantalla dice «Esta parte llegará pronto» sin error técnico; `401` cierra la sesión; sin conexión, error con «Reintentar». Textos en `strings.xml`. |

## 3. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** dos servicios el martes de 18:00 a 22:00 y de 21:00 a 23:00, **cuando** veo la semana, **entonces** ambos dicen que se empalman. |
| CA-02 | **Dado** la semana actual, **cuando** toco la flecha derecha, **entonces** veo la siguiente. |
| CA-03 | **Dado** 2 solicitudes sin responder, **cuando** abro «Inicio», **entonces** dice «2 solicitudes nuevas». |

## 4. Fuera de alcance

- Avisos recientes (notificaciones).
- Borrar un servicio (backend `042` requiere decisión).

## 5. Dependencias

| Endpoint | Spec del backend (estado 2026-11-27) |
|---|---|
| `GET`/`POST /providers/me/gigs`, `PATCH /providers/me/gigs/:id` | `039`, `040`, `041` (📝 especificado) |
| `GET /quotes` del proveedor | `060` (📝 especificado) |
