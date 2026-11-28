# Plan — 026 Perfil del proveedor

| Campo | Valor |
|---|---|
| Módulo | `026-proveedor-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-27, «Aprobados mi hermando»), recortado |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

| Capa | Cambio |
|---|---|
| core:domain | Modelos y reglas calculadas en la app |
| core:network | DTO y rutas según los specs del backend, con `contractCall` |
| app | Pantallas del rol proveedor conectadas al espacio de trabajo |

## 2. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Reglas y `ViewModel` con éxito, error, 401 y endpoint ausente (404/501). |
| Integración | MockWebServer: ruta, cuerpo y códigos de los specs del backend. |
| UI | Estados de la pantalla con fuente 2.0 a 320 dp. |
