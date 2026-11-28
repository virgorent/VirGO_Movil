# Plan — 012 IA Studio

| Campo | Valor |
|---|---|
| Módulo | `012-ia-studio` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27) |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
|  | core:domain: TableLayout y StaffEstimate (reglas del prototipo), DecorIdea y AiRepository |
|  | core:network: AiApi con POST /api/v1/ai/ideas |
|  | app ui/studio: StudioViewModel y pantalla con pestañas Mesas, Personal e Ideas |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Reglas de dominio y `ViewModel` con éxito, error, 401 y endpoint ausente (404/501). |
| Integración | MockWebServer: ruta, cuerpo y códigos de respuesta del contrato. |
| UI | Estados de la pantalla con fuente 2.0 a 320 dp. |
