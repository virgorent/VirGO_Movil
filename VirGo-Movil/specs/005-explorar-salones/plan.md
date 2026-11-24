# Plan — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Versión | 1.1.0 |
| Estado | En revisión; consolidación autorizada el 2026-11-19 |
| Fecha | 2026-11-19 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

La implementación actual vive en `:app`, paquete `mx.virgo.app.ui.explore`.
Un único `ExploreViewModel` coordina lista, mapa, filtros y recomendaciones,
expone `ExploreUiState` por `StateFlow` y consume casos de uso de `:core:domain`.
No existe todavía `:feature:explorarsalones`; extraerlo no pertenece a esta consolidación.

`core:data` implementa los repositorios, `core:network` contiene Retrofit/DTOs y
`core:datastore` conserva el token cifrado. La presentación aún reutiliza
`VenueSample`, mapeos y fotografías de `ui/guest`; se documenta como deuda,
no como aislamiento de funcionalidades ya cumplido.

### Composición autorizada

- Estado inicial `ExploreMode.LIST`, fotografías a todo el ancho de 164 dp y
  etiqueta «Imagen de ejemplo» en tarjetas ficticias; mapa como alternativa.
- `VenuePhotoContent` comparte cinco recursos por categoría únicamente para IDs
  `sample-`. No descarga imágenes remotas ni atribuye muestras a inmuebles reales.
- `SamplePhotoCredits` abre autores, fuentes, licencias y modificaciones;
  disponible al final de la lista, en tarjetas de mapa/visitante y al principio
  de la hoja «Para ti» cuando hay recomendaciones de muestra.
- `LocalDevMenu` integra el menú de debug en la cabecera sin reservar una fila.
  Se retira la barra inferior de un único destino.
- `BackHandler` cierra hojas; ajustes de insets/teclado en hojas, login y registro.
  No se cambian los contratos de autenticación ni navegación de sesión.

## 2. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ⚠️ Pendiente | Consolidación autorizada; revisión del spec y cierre con evidencia aún pendientes. |
| II | ⚠️ Pendiente | `000` sigue 38/40; no se oculta el pendiente de cierre de la base. |
| III | ⚠️ Parcial | MVVM/UDF y capas core; demo decidida en UI y dependencia de `ui/guest` por corregir posteriormente. |
| IV | ⚠️ Parcial | `/api/v1`, Bearer y errores propios; contratos favoritos/avisos no entregados como endpoints operativos. |
| V | ✅ Diseño | Fotos locales públicas, sin credenciales añadidas; gates de seguridad exigidos antes del merge. |
| VI | ⚠️ Pendiente | Pruebas automáticas y revisión manual diferenciadas en `check.md`; humo completo bloqueado. |
| VII | ⚠️ Parcial | Tokens/componentes base y textos en recursos; quedan estilos locales y validación adaptable pendiente. |
| VIII | ⚠️ Pendiente | PR #27 apilado sobre #19; revisión/checks actuales requeridos; no se lanza release. |
| IX | ✅ Sin cambios | Se reutilizan correlación y logging restringido de la base. |
| X | ✅ Documentación | Trazabilidad en `task.md`, evidencia en `check.md` y bitácora en `memoria.md`. |
| XI | ✅ Alcance | Solo consolidación aprobada; sin secretos, release ni despliegues. |
| XII | ✅ Cumple | Documentación/textos en español; identificadores en inglés. |

No se declara alineación final ni cierre mientras subsistan estos pendientes.

## 3. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | `ViewModel` con Turbine y casos de uso |
| Integración | Repositorio con MockWebServer: éxito, 4xx/5xx y sin conexión |
| UI | Compose Testing de cada pantalla, con escala de fuente 2.0 |
| Seguridad | Sin datos sensibles en logs |
| Mutación | Pitest en casos de uso |
| Humo | La pantalla funciona contra la URL de producción |

Regresiones de consolidación: entrada en lista, selección a mapa y regreso,
créditos desde ambas vistas, mensaje seguro ante error de API y reintento tras
desconexión. Las capturas anteriores son evidencia histórica y no prueban el
último rediseño. Fuente 2.0, teclado, tema oscuro y Atrás en dispositivo requieren
evidencia específica antes de marcar su aceptación.

Verificación local, sin compilaciones simultáneas:

```powershell
.\gradlew.bat ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug
```

Si Windows bloquea `R.jar`, se usa el init script local de salida aislada descrito
en `memoria.md`; no se detienen daemons ni se borran archivos de otras ejecuciones.

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| El endpoint aún no existe | MockWebServer con el contrato del spec de API |
| Catálogo vacío confundido con éxito productivo | Declarar fallback de muestras y separar fuentes en un spec posterior. |
| Evidencia visual desactualizada | T509 mantiene abierta la aceptación manual del rediseño. |
| Fotografías sin atribución o confundidas con reales | Créditos dentro de la app y `docs/CREDITOS-IMAGENES.md`; limitar recursos a muestras. |

## 5. Contrato, entrega y fases

Se conserva el contrato existente; favoritos/avisos se verifican con servidor
simulado hasta la entrega del backend. La fecha no prueba disponibilidad.
No se añaden SDKs AWS, acceso directo a almacenamiento ni llaves en el APK.

1. T508: consolidar cambios autorizados, regresiones, documentación y push al PR #27.
2. T509: revisión visual del APK actual y actualización de capturas con el equipo.
3. T507: integración/humo completo cuando el backend entregue endpoints y JSON.

Karasu realiza la fusión después de los checks y la revisión. Se mantiene la base
de #27 en `feat/004-recuperar-contrasena` mientras #19 siga abierto. Cualquier
release se coordina aparte; esta fase no publica artefactos.
