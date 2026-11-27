# Spec — 020 Mensajes con el salón

| Campo | Valor |
|---|---|
| Módulo | `020-comunicacion` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente y Propietario |
| Fuente | `Checklist VirGo.md` (C35, C36); backend `084`–`088`; esquemas `conversations` y `messages` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El cliente y el dueño del salón hablan dentro de la app. Las rutas existen como specs del backend (`084`–`088`);
los campos están en el contrato §3.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C36]** «Mensajes» lista las conversaciones, más reciente primero, con el salón (cliente) o el cliente y el salón (propietario), el último mensaje, su hora y los no leídos. |
| RF-02 | **[C35]** Tocar una conversación abre sus mensajes, más antiguo primero; los míos a la derecha. «Atrás» vuelve a la lista. |
| RF-03 | **[C35]** Escribir (1 a 1000 caracteres) y «Enviar»; el botón se desactiva vacío o mientras envía. Si falla, el texto se conserva con «Reintentar». |
| RF-04 | El cliente inicia una conversación con «Nuevo mensaje»: elige uno de los salones a los que ya pidió cotización (`GET /quotes`) y escribe el primer mensaje (`POST /conversations`); si ya existía, se abre esa. |
| RF-05 | Mientras el endpoint no exista (`404`/`501` en la lista), «Los mensajes llegarán pronto». `401` cierra la sesión; red/5xx con «Reintentar». |
| RF-06 | La sección «Conversación» del cliente abre la misma pantalla. Textos en `strings.xml`. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** nada del texto en logs; el servidor decide quién participa. |
| RNF-02 | **Accesibilidad:** cada mensaje se lee con su autor y hora; el nuevo se anuncia. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** conversaciones del servidor, **cuando** abro «Mensajes», **entonces** las veo con su último mensaje. |
| CA-02 | **Dado** una conversación abierta, **cuando** envío un texto, **entonces** aparece al final como mío. |
| CA-03 | **Dado** un cliente con cotizaciones, **cuando** inicia «Nuevo mensaje», **entonces** puede elegir esos salones. |
| CA-04 | **Dado** que el endpoint no existe, **cuando** abro «Mensajes», **entonces** veo «Los mensajes llegarán pronto». |

## 5. Fuera de alcance

- Notificaciones (C37), marca de leído (`089`), adjuntos, mensajes con proveedores y respuestas automáticas.

## 6. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/conversations`, `POST /api/v1/conversations` | **Contrato §3** (084, 085) |
| `GET`/`POST /api/v1/conversations/:id/messages` | **Contrato §3** (087, 088) |
| `GET /api/v1/quotes` | En la cadena hacia `main` |
