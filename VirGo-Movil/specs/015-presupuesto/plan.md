# Plan — 015 Presupuesto del evento

| Campo | Valor |
|---|---|
| Módulo | `015-presupuesto` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27) |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
|  | core:domain: BudgetRules (reparto y preferencias del prototipo) y EventFinance |
|  | core:network: budgetPlan en EventDto y PATCH parcial |
|  | app ui/finance: BudgetViewModel, pantalla de presupuesto y de sugerencia |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Reglas de dominio y `ViewModel` con éxito, error, 401 y endpoint ausente (404/501). |
| Integración | MockWebServer: ruta, cuerpo y códigos de respuesta del contrato. |
| UI | Estados de la pantalla con fuente 2.0 a 320 dp. |
