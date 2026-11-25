# Plan — 034 Beta integrada

**Fecha:** 2026-11-24. Alcance visual autorizado; sin APIs nuevas.

[Spec](spec.md) · [Tareas](task.md) · [Evidencia](check.md).

## Diseño

- Mantener login/registro y sesión sin cambios.
- Reutilizar el catálogo existente mediante `GetVenuesUseCase`, con estado
  inmutable y ViewModel. Una lista compartida entre visitante y cliente evita
  las muestras y coordenadas supuestas de las vistas históricas.
- Mantener destinos y tabs por rol; mostrar estados pendientes uniformes en
  vez de componer pantallas con datos ficticios.
- Cuenta de solo lectura desde la sesión; logout local identificado.
- Retirar enlace y registro de la ruta de muestra en `VirGoNavHost`.
- Las pantallas históricas quedan disponibles solo como componentes/pruebas,
  no como destinos de la beta.
- No nuevas dependencias; presentación en `:app`, dominio/red existentes.

## Constitution Check

| Art. | Estado |
|---|---|
| I | Alcance y ejecución visual autorizados explícitamente; aceptación pendiente. |
| II | `000` permanece abierto. Continuación visual acotada, no integración funcional nueva. |
| III | MVVM/UDF; casos de uso existentes; sin Retrofit en UI. |
| IV | Contrato de catálogo existente; no se inventan endpoints. |
| V | Sin nuevos permisos, secretos ni datos sensibles en logs. |
| VI | Pruebas de estados, UI, rutas y regresiones; evidencia pendiente. |
| VII | Componentes/tokens existentes, claro/oscuro, 48 dp y fuente ampliada. |
| VIII | Sin release; checks y aprobación requeridos antes de fusionar. |
| IX | Logging existente, sin cambios. |
| X | Check y memoria al terminar. |
| XI | Skills locales y revisión; sin despliegues. |
| XII | Documentación/textos en español; código en inglés. |

## Pruebas y riesgos

- Unitarias: catálogo vacío, con datos, error y reintento; sin fallback.
- UI: destinos de los tres roles sin datos ficticios; cuenta sin edición;
  navegación sin launcher de muestra; estado pendiente deshabilitado.
- Regresión: sesión, login, registro y tests históricos de componentes.
- Seguridad: ninguna llamada a APIs pendientes desde las nuevas secciones.
- Mutación: sin nueva lógica de dominio; ejecutar Pitest existente.
- Humo: APK debug local; no crear cuentas o recursos productivos.
- Riesgo: destinos sin backend se verán vacíos, deliberadamente; no sustituir
  ausencia de datos por muestras para hacer más vistosa la beta.
