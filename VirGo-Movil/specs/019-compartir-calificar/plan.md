# Plan — 019 Calificar el salón

| Campo | Valor |
|---|---|
| Módulo | `019-compartir-calificar` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27) |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
|  | core:domain: Review y ReviewsRepository |
|  | core:network: ReviewsApi (072, 074, 075) |
|  | app ui/reviews: ReviewFormViewModel y formulario en el comprobante |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Reglas de dominio y `ViewModel` con éxito, error, 401 y endpoint ausente (404/501). |
| Integración | MockWebServer: ruta, cuerpo y códigos de respuesta del contrato. |
| UI | Estados de la pantalla con fuente 2.0 a 320 dp. |
