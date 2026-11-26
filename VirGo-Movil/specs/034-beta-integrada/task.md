# Tareas — 034 Beta integrada

[Spec](spec.md) · [Plan](plan.md) · [Check](check.md).

| ID | Trabajo | Dependencia | Verificación | Estado |
|---|---|---|---|---|
| T3401 | Catálogo beta sin muestras, con estados explícitos | Alcance autorizado | CA-02, unitarias y UI | Hecha; ver check |
| T3402 | Secciones por rol, cuenta de lectura y bloqueo de simulaciones | T3401 | CA-03/04/06, UI | Hecha; aceptación visual pendiente |
| T3403 | Retirar acceso y ruta demo; conservar autenticación | T3402 | CA-01/05, pruebas de rutas | Hecha; ver check |
| T3404 | Regresiones, análisis estático y APK | T3403 | Pipeline; CA-01–06 | Hecha localmente; CI remoto pendiente |
| T3405 | Evidencia, memoria y aceptación visual | T3404 | Check; revisión de ccabrera-dev8 | En curso; falta aceptación humana |

Responsables: agente principal para código/documentación, revisión humana para
aceptación visual. No se marcan tareas completas sin evidencia.
