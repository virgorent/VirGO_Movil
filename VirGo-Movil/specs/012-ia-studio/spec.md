# Spec — 012 IA Studio

| Campo | Valor |
|---|---|
| Módulo | `012-ia-studio` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados, arranca»), recortado |
| Fecha | 2026-11-27 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C15–C17, E43) y prototipo local «VirGo Event Management App» (`Studio.tsx`) |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

«IA Studio» dice «Próximamente». Karasu integrará el modelo de IA **en el backend**; la app solo llama a un
endpoint y nunca guarda llaves. Mesas y personal no necesitan IA: el prototipo usa reglas fijas que se muestran al
usuario, así que se calculan en la app y funcionan desde hoy. Ideas de decoración sí depende del endpoint nuevo.

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | Pestañas «Mesas», «Personal» e «Ideas». Los invitados salen del primer evento del cliente (`GET /events`); si no tiene, se capturan (1 a 2000). |
| RF-02 | **[E43]** Tres distribuciones: Pista al centro (10 por mesa, 85 % del aforo), Máximo aforo (10, 100 %) e Imperial (12, 75 %). Muestra el número de mesas (`ceil(invitados / por mesa)`). |
| RF-03 | **[E43]** Si el evento tiene salón con capacidad conocida, avisa «No caben en esta distribución» cuando los invitados superan la capacidad ajustada. |
| RF-04 | **[C16]** Personal sugerido: meseros 1 por cada 30 (mínimo 2), seguridad 1 por cada 100 (mínimo 1) y limpieza 1 por cada 80 (mínimo 1). Cada fila muestra su regla y se ajusta con − y + (mínimo 0). |
| RF-05 | **[C16]** Si se ajustó a mano, «Volver a la sugerencia» restablece los valores. Leyenda: «Es una sugerencia; ajústala a tus necesidades». No se guarda en el servidor (no hay campo). |
| RF-06 | **[C17]** «Ideas» pide al servidor ideas de decoración para el tipo de evento, invitados y presupuesto del evento (`POST /api/v1/ai/ideas`): título, descripción corta y costo estimado. |
| RF-07 | **[C17]** Indicador de si la suma de ideas va dentro o por encima del 12 % del presupuesto (regla del prototipo para decoración). |
| RF-08 | Mientras el endpoint no exista (`404`/`501`) se muestra «Las ideas con IA llegarán pronto» sin error técnico; `429`: «Espera un momento y vuelve a intentar»; red/5xx con «Reintentar». |
| RF-09 | Todas las respuestas de IA llevan la etiqueta «Sugerencia de IA». Textos en `strings.xml`. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** sin llaves de IA en la app; solo se envían tipo de evento, invitados y presupuesto (sin nombres ni teléfonos). |
| RNF-02 | **Accesibilidad:** − y + con nombre («Menos meseros», «Más meseros») y 48 dp; fuente 2.0. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** 200 invitados, **cuando** abro «Mesas», **entonces** veo 20, 20 y 17 mesas. |
| CA-02 | **Dado** 200 invitados, **cuando** abro «Personal», **entonces** veo 7 meseros, 2 de seguridad y 3 de limpieza con sus reglas. |
| CA-03 | **Dado** que ajusto un valor, **cuando** toco «Volver a la sugerencia», **entonces** regresa al cálculo. |
| CA-04 | **Dado** que el endpoint no existe, **cuando** abro «Ideas», **entonces** veo «Las ideas con IA llegarán pronto». |
| CA-05 | **Dado** ideas que suman más del 12 % del presupuesto, **cuando** se muestran, **entonces** veo «Por encima del presupuesto de decoración». |

## 5. Fuera de alcance

- Comparador antes/después con imagen generada (C15) y temas con foto: requieren generación de imágenes.
- Guardar el diseño en «Mi Evento» y descargar el resumen (no hay campos en el backend).

## 6. Dependencias

| Endpoint | Estado (2026-11-27) |
|---|---|
| `GET /api/v1/events` | En la cadena hacia `main`; ya lo usa `014` |
| `GET /api/v1/venues/:id` | En `main`; capacidad del salón |
| `POST /api/v1/ai/ideas` | **Nuevo, en el contrato** (`docs/contrato-apis-pendientes.md`) |
