# Spec — 013 Asistente IA

| Campo | Valor |
|---|---|
| Módulo | `013-asistente-ia` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C18) y prototipo local («Asistente VirGo», `Social.tsx`) |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

Pantalla de chat para que Karasu conecte el modelo de IA desde el backend. La app manda la pregunta y muestra la
respuesta; la llave, el modelo y las reglas viven en el servidor.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C18]** Saludo fijo: «¡Hola! Puedo ayudarte con presupuesto, mesas, personal, invitaciones y fechas», con la etiqueta «Respuestas orientativas». |
| RF-02 | **[C18]** Sin mensajes, cuatro sugerencias rápidas: «¿Cuánto gastar en cada cosa?», «¿Cuántos meseros necesito?», «¿Cuándo envío invitaciones?» y «¿Con cuánto tiempo reservo?». |
| RF-03 | Campo de texto (1 a 500 caracteres, sin espacios a los lados) y botón «Enviar», desactivado si está vacío o mientras espera. |
| RF-04 | Envía `POST /api/v1/assistant/messages` con `message`, `eventId` del primer evento si existe y los últimos 10 mensajes (`history`). |
| RF-05 | Muestra `reply`. Si el servidor marca `uncertain: true`, la burbuja dice además «No estoy seguro; confírmalo con el salón o en Ayuda». |
| RF-06 | `404`/`501`: «El asistente todavía no está conectado». `429`: «Espera un momento y vuelve a intentar». Red/5xx: la pregunta queda con «Reintentar». `401` cierra la sesión. |
| RF-07 | La conversación vive solo en la pantalla (se pierde al salir); no se guarda en el dispositivo. |
| RF-08 | Se abre desde «IA Studio» (botón «Asistente») y desde la sección «Asistente IA». Textos en `strings.xml`. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** sin llaves en la app; el texto no se escribe en logs. |
| RNF-02 | **Accesibilidad:** cada burbuja se lee con su autor («Tú:», «Asistente:»); la respuesta nueva se anuncia. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** el chat vacío, **cuando** toco una sugerencia, **entonces** se envía como pregunta. |
| CA-02 | **Dado** una respuesta `uncertain`, **cuando** llega, **entonces** veo el aviso de que no está seguro. |
| CA-03 | **Dado** que el endpoint no existe, **cuando** pregunto, **entonces** veo «El asistente todavía no está conectado». |
| CA-04 | **Dado** un error de red, **cuando** toco «Reintentar», **entonces** se vuelve a enviar la misma pregunta. |

## 5. Fuera de alcance

- Guardar conversaciones, voz, adjuntos e imágenes.

## 6. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/events` | En la cadena hacia `main` |
| `POST /api/v1/assistant/messages` | **Nuevo, en el contrato** (`docs/contrato-apis-pendientes.md`) |
