# Verificación · demo visual de roles

Fecha: 2026-11-23. Rama `feat/032-demo-visual-roles`, basada en
`fix/000-fundamentos-visuales` (`42490ec`); sin fusión ni release.
Código/evidencia: `9e96bf4`; [PR #31 en borrador](https://github.com/virgorent/VirGO_Movil/pull/31).

## Alcance

55 destinos debug: 29 cliente, 12 propietario, 9 proveedor y 5 compartidos.
15 hojas H01–H15. Los cuatro destinos existentes de acceso/visitante siguen
en el acceso normal de la app; no se sustituyen por autenticación ficticia.

Ver [guía y límites](../../diseno/demo-visual-roles.md) y
[autorización de excepción temporal](../../diseno/excepcion-demo-urgente.md).
No se acredita aceptación humana ni cierre de `000`/`031`.

## Pipeline local final

Con JDK/toolchain y SDK existentes, usando el init-script local que aísla
los artefactos fuera del checkout:

```text
ktlintCheck detekt lint testDebugUnitTest
:core:domain:test :core:common:test pitest assembleDebug
BUILD SUCCESSFUL in 2m 29s
383 tareas: 19 ejecutadas, 364 actualizadas
```

- XML: **49 suites, 274 pruebas, 0 fallos, 0 errores, 0 omitidas**.
- Ejecución incremental: no implica reejecución física de todas las tareas.
- 18 pruebas nuevas de demo: inventario/unicidad/aislamiento por rol,
  historial acotado, renderizado de destinos, selección de rol, restauración,
  pago exitoso/fallido ficticio, conversación local, límites 500/501/1500,
  apertura/cierre alcanzable de las 15 hojas y validación de importes.
- Se incluyen las seis regresiones de acceso a fuente 1.3 anteriores a esta
  demo. El total anterior era 256.
- Se incorporaron debug y testDebug a las fuentes de detekt del módulo app,
  conservando las fuentes previas. No se omiten reglas ni se crea baseline.
- Android Lint de app: **0 errores, 42 avisos, 11 sugerencias**. Permanecen
  advertencias de APIs obsoletas de Compose Testing/Gradle y otras existentes;
  no se presenta como una compilación sin avisos.
- Pitest se reutiliza sin cambios de dominio/common. No se atribuye cobertura
  de mutación a estas pantallas Compose.

## Fallos encontrados y corregidos

- Recursos duplicados de descripciones de roles: nombres separados.
- Constantes/formato y reglas de detekt: correcciones sin suprimir reglas.
  La ampliación detectó también un literal en el test de diagnóstico:
  se extrajo una constante, sin cambiar el caso.
- La prueba de hojas detectó «Cerrar» no visible. Las hojas largas se abren
  expandidas, sin estado parcialmente expandido, y la prueba conserva la
  aserción de visibilidad tras desplazar. Suite focalizada en verde.
- Lint detectó porcentajes literales como formato inválido; el recurso ahora
  declara correctamente `formatted="false"`.

## APK y comprobación en emulador

- APK **debug**, 24 595 173 bytes.
- SHA-256:
  `e66c6e7abcb8644fb51e0c5a3ca0bd99d5f86739a4fed251d242aa3d3302f2df`.
- Firma debug verificada mediante `apksigner verify --print-certs`:
  `de462575df353dc1730784dbb30fe939bd5ce1f5a4f8070c20355c1bccfe0ded`.
- Instalado con `adb -s emulator-5554 install -r`, sin borrar datos.
  SHA-256 del `base.apk` instalado coincidente.
- Inicio explícito de `.demo.VisualDemoActivity`: `Status: ok`.
  El primer arranque frío tardó 22 720 ms durante inestabilidad del emulador;
  no se acredita con ello el RNF histórico de arranque <2 s.
- Recorrido manual automatizado: selector → cada uno de los tres roles →
  cambiar rol; índice → detalle de salón; tema oscuro; abrir/cerrar búsqueda
  y preferencias. Solo se actuó sobre el emulador, no sobre teléfonos conectados.
- El emulador presentó ANR de Pixel Launcher y System UI. Se cerró el diálogo
  del componente del sistema y se reanudó el recorrido, sin `wipe-data`.
  Las capturas de diálogos/splash se reemplazaron por las pantallas reales.
  Ese incidente no se atribuye a una prueba fallida del producto.

## Capturas reales del APK final

Pixel_10_Pro, 1280×2856 px, 480 dpi, fuente 1.0
(aproximadamente 427×952 dp físicos; el contenido descuenta barras del sistema).

| Captura | Estado |
|---|---|
| [01-selector-claro.png](01-selector-claro.png) | Selector de rol y aviso demo |
| [02-cliente-inicio-claro.png](02-cliente-inicio-claro.png) | Explorar con mapa local |
| [03-propietario-inicio-claro.png](03-propietario-inicio-claro.png) | Panel, métricas y reservas ficticias |
| [04-proveedor-inicio-claro.png](04-proveedor-inicio-claro.png) | Panel y solicitud de muestra |
| [05-cliente-detalle-claro.png](05-cliente-detalle-claro.png) | Detalle e ilustración local identificada |
| [06-cliente-detalle-oscuro.png](06-cliente-detalle-oscuro.png) | Mismo detalle en oscuro |
| [07-proveedor-inicio-oscuro.png](07-proveedor-inicio-oscuro.png) | Panel proveedor oscuro |
| [08-hoja-busqueda-claro.png](08-hoja-busqueda-claro.png) | Hoja de búsqueda/calendario desplazable |
| [09-hoja-preferencias-claro.png](09-hoja-preferencias-claro.png) | Preferencias, resumen y cierre alcanzables |

Capturas revisadas técnicamente: corresponden a la app, no a splash/diálogos;
sin superposiciones evidentes en los estados mostrados. Los controles
horizontales de hojas/calendario se desplazan y lo indican.
Esto no certifica fidelidad de todos los estados, TalkBack ni aceptación humana.
Emulador restaurado a resolución/densidad físicas, fuente 1.0 y tema claro.

## Seguridad y revisión

- Launcher, clases y recursos de demo solo en `src/debug`; sin nuevos
  permisos, dependencias, endpoints ni cambios de secretos.
- Revisión estática automatizada de solo lectura: sin bloqueantes de
  seguridad detectados en el alcance inspeccionado; no equivale a Gitleaks.
- Sin generación ni publicación de release, sin merge ni cambios de ruleset.
- CodeQL filtra PRs a `main`; una base apilada no acredita ese check.
- CI remoto y revisión visual se registran en el PR. **Aceptación visual
  reservada exclusivamente al usuario solicitante (`ccabrera-dev8`).**
