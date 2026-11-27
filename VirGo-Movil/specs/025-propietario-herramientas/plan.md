# Plan — 025 Herramientas del propietario

| Campo | Valor |
|---|---|
| Módulo | `025-propietario-herramientas` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27) |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
|  | core:domain: VenuePricing (vista previa), OwnerStats y OwnerPayments (cálculo con reservaciones y cotizaciones) |
|  | core:network: PATCH de precio y montajes |
|  | app ui/ownertools: pantallas de precios, montajes, pagos recibidos, estadísticas y reseñas |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Reglas de dominio y `ViewModel` con éxito, error, 401 y endpoint ausente (404/501). |
| Integración | MockWebServer: ruta, cuerpo y códigos de respuesta del contrato. |
| UI | Estados de la pantalla con fuente 2.0 a 320 dp. |
