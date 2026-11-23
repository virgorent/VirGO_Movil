# T014 · Corrección adaptativa del inicio visitante

Seguimiento: 2026-11-21–22. Rama `fix/000-fundamentos-visuales`, PR #29.

## Alcance y autorización

- Continuación autorizada por el usuario sobre el PR #29, sin fusión ni release.
- Reparación de accesibilidad existente: `000` T014/T029 y `001` T101/T102/T104.
- No cambia ViewModel, repositorios, endpoints, filtros de distancia, IDs de
  registro, navegación ni fotografías. No implementa `031` ni nuevas funciones.
- El mapa conserva su carácter demostrativo; no se incorpora ubicación real.

## Defecto reproducido

La captura [`T014-visitante-compacto-2x-oscuro-bloqueo.png`](T014-visitante-compacto-2x-oscuro-bloqueo.png)
del APK anterior muestra encabezado/resumen/error superpuestos, texto sin espacio
horizontal y superficies blancas bajo tema oscuro. Es evidencia del fallo,
no del resultado corregido.

## Reparación

- `GuestHomeLayout` usa un flujo vertical desplazable cuando el ancho es menor
  de 360 dp, el alto menor de 700 dp o la fuente al menos 1.3. Cabecera, mapa de
  360 dp de alto y pie ocupan regiones distintas. La composición superpuesta se
  conserva en espacio normal con fuente 1.0.
- Marca/login se apilan en el modo adaptativo. El resumen separa información y
  selector; los tres rangos crecen con el texto, exponen grupo/selección y
  objetivos mínimos de 48×48 dp.
- Vista previa compacta apila fotografía y detalles; la distancia sale de la
  fotografía de 96 dp para no recortarse. El nombre reserva sitio al cierre.
  Cierre y créditos conservan objetivos de 48 dp.
- Tarjetas, texto, pines, marcador y mapa consumen roles semánticos del tema.
  El círculo punteado usa primario sólido; las calles siguen siendo decorativas.
- Las etiquetas del mapa se miden con la tipografía/fuente efectiva, en lugar de
  estimarse por caracteres y altura fija. Se consideran límites y colisiones
  para seleccionar etiqueta o pin compacto, sin cambiar los datos del salón.
- El flujo error → reintento y la prioridad de selección/vista previa se
  conservan.

## Verificación focalizada

`ktlintCheck`, `detekt` y las suites `GuestAccessibilityTest`, `NavigationTest`,
`FontScaleTest`, `SmokeTest`: **BUILD SUCCESSFUL in 33s**, 22 pruebas,
sin fallos en la ejecución focalizada.

`GuestAccessibilityTest` aporta 13 casos (12 de tamaños/temas más la regresión
de pines este/oeste con etiquetas mayores que el mapa):

| Configuración | Temas | Estados |
|---|---|---|
| 320×640 dp / fuente 2.0 | Claro y oscuro | Inicio, vista previa, error/reintento |
| 393×800 dp / fuente 1.0 | Claro y oscuro | Inicio, vista previa, error/reintento |

Comprueba ancho/alto efectivos, regiones compactas no superpuestas, líneas sin
truncado/elipsis, controles ≥48×48 dp, selección de rango con el número de
muestras esperado, cierre de preview y una llamada adicional al reintentar.

La regresión de navegación conserva el flujo pin → vista previa → registro →
regreso y sus aserciones; desplaza explícitamente las acciones antes de tocarlas
porque ahora pueden quedar fuera del viewport compacto.

## Límites

- Las pruebas JVM no sustituyen capturas ni aceptación visual humana.
- No se atribuye paridad completa con el prototipo ni con Figma.
- TalkBack manual y revisión integral de la app siguen pendientes.
- El pipeline remoto verde de `7b76aef` pertenece a la entrega anterior:
  no acredita esta modificación hasta que se publique y se vuelva a verificar.
- T014/T036 continúan abiertas mientras no se complete la evidencia y revisión
  exigida; esta reparación no cierra `000`.

## Regresiones adicionales (2026-11-22)

La revisión detectó que el pin compacto reutilizaba los límites de una etiqueta
descartada: nombres mayores que el mapa podían colapsar al centro. Se conservan
ahora las coordenadas originales y se limitan según el objetivo dibujado
(48 dp); sus cajas se registran para decidir el etiquetado posterior.
La regresión este/oeste verifica separación, límites, tamaño y callbacks mediante
toque físico. No garantiza separación de coordenadas idénticas en este mapa demo.

La región viva está en el texto del error, con reintento independiente.
Los cuatro escenarios de error comprueban esa propiedad sin atribuir una
validación manual de TalkBack. Una revisión independiente de solo lectura
confirmó ambas reparaciones sin detectar regresiones obvias.

Las primeras verificaciones detectaron un import de `click` faltante y,
posteriormente, su orden incorrecto. Se corrigieron y se volvió a ejecutar
el pipeline; no se eliminaron aserciones.

## Pipeline y artefacto local

- Pipeline completo: **BUILD SUCCESSFUL in 2m 20s**, 383 tareas (14 ejecutadas,
  1 desde caché, 368 actualizadas). Incluye ktlint, detekt, lint, pruebas,
  Pitest y `assembleDebug`.
- XML: **44 suites, 250 pruebas, 0 fallos, 0 errores, 0 omitidas**.
  `GuestAccessibilityTest` tiene ahora 13 casos.
- Pitest reutilizado sin cambios de dominio/common; no se atribuye mutación a UI.
- APK debug: **23 889 777 bytes**, SHA-256
  `ce2ed276394de05e61ae9ee5db9ed789a0df9ef6a42b0e94fedb3b7cc709e473`.
- `apksigner verify --print-certs`: correcto, certificado Android Debug SHA-256
  `de462575df353dc1730784dbb30fe939bd5ce1f5a4f8070c20355c1bccfe0ded`.
- Instalado con `adb install -r` sin borrar datos. `sha256sum` de `base.apk`
  instalado coincide con el hash anterior. Paquete real: `mx.virgo.app`;
  un primer intento de arranque con sufijo `.debug` falló y se corrigió.
- Capturas resueltas: la primera tanda salió vacía porque PowerShell
  redirigía el PNG como UTF-16. Se sustituyó por `cmd /c "adb exec-out
  screencap -p > archivo"` y se obtuvo imagen real. El ANR de System UI
  desapareció tras esperar/confirmar; no se borraron datos ni se usó
  `wipe-data`. Las capturas vacías se descartaron y no se versionaron.
- Emulador restaurado a tamaño físico 1280×2856, 480 dpi, fuente 1.0 y claro.
- CI del head `dda6fdb`: `gitleaks`, `dependency review` y `changes` en verde.
  `movil-build-test` falló por `IOException` de Robolectric al descargar su
  artefacto (`MavenArtifactFetcher`), no por una prueba de la app; se relanzó
  el job fallido y queda pendiente su resultado.

## Capturas del APK actual

Todas son del APK `ce2ed2…` instalado en `Pixel_10_Pro`, con datos de ejemplo
y error de catálogo por la API local ausente. No son aceptación humana.

| Archivo | Configuración | Estado |
|---|---|---|
| [`T014-visitante-320-2x-oscuro-inicio.png`](T014-visitante-320-2x-oscuro-inicio.png) | 320×640 dp, fuente 2.0, oscuro | Cabecera apilada, resumen y selector sin superposición |
| [`T014-visitante-320-2x-oscuro-mapa.png`](T014-visitante-320-2x-oscuro-mapa.png) | ídem | Mapa a ancho completo, pines compactos separados, superficies oscuras |
| [`T014-visitante-320-2x-oscuro-error.png`](T014-visitante-320-2x-oscuro-error.png) | ídem | Mensaje completo y «Reintentar» alcanzable |
| [`T014-visitante-320-2x-oscuro-preview.png`](T014-visitante-320-2x-oscuro-preview.png) | ídem | Foto apilada, cierre, «Ver más…», aviso y créditos legibles |
| [`T014-visitante-320-2x-claro-inicio.png`](T014-visitante-320-2x-claro-inicio.png) | 320×640 dp, fuente 2.0, claro | Equivalente en tema claro |
| [`T014-visitante-320-2x-claro-error.png`](T014-visitante-320-2x-claro-error.png) | ídem | Error y reintento completos |
| [`T014-visitante-320-2x-claro-preview.png`](T014-visitante-320-2x-claro-preview.png) | ídem | Vista previa completa |
| [`T014-visitante-393-1x-claro-inicio.png`](T014-visitante-393-1x-claro-inicio.png) | 393×800 dp, fuente 1.0, claro | Composición superpuesta normal con etiquetas |
| [`T014-visitante-393-1x-oscuro-inicio.png`](T014-visitante-393-1x-oscuro-inicio.png) | 393×800 dp, fuente 1.0, oscuro | Equivalente oscuro |
| [`T014-visitante-393-1x-oscuro-preview.png`](T014-visitante-393-1x-oscuro-preview.png) | ídem | Vista previa inferior sobre el mapa |

## Seguimiento: escala intermedia y CI (2026-11-22)

- El head `42490ec` pasó `changes`, `gitleaks`, `dependency review` y
  `movil-build-test` (3m42s), run
  [45044830978](https://github.com/virgorent/VirGO_Movil/actions/runs/45044830978).
  El fallo de descarga de Robolectric no se reprodujo; no se aplicó una
  mitigación ni se considera identificada su causa raíz.
- Revisión adicional en el emulador: 393×800 dp, fuente 1.3, claro y oscuro.
  Las cuatro capturas siguientes muestran el inicio adaptativo y el error
  completo con «Reintentar» alcanzable por desplazamiento, sin superposición:
  - [Inicio claro](T014-visitante-393-1.3x-claro-inicio.png).
  - [Error claro](T014-visitante-393-1.3x-claro-error.png).
  - [Inicio oscuro](T014-visitante-393-1.3x-oscuro-inicio.png).
  - [Error oscuro](T014-visitante-393-1.3x-oscuro-error.png).
- La jerarquía de UI confirmó flujo desplazable, rango 5 km seleccionado
  y botón de reintento de 48 dp de alto. No se probó TalkBack ni el anuncio
  audible, ni todos los estados de acceso a escala 1.3.
- Se reemplazó una captura tomada durante el splash y otra tomada antes
  de terminar el desplazamiento; no se usan como evidencia del resultado.
- Emulador restaurado a 1280×2856, 480 dpi, fuente 1.0 y tema claro.

Pendiente para el cierre de T014: TalkBack manual, revisión de estados
alternativos, resto del acceso a escala 1.3 y aceptación visual humana por
bloques. T014/T036 permanecen abiertas.

## Regresión adicional: acceso a escala 1.3 (2026-11-22)

`CompactAccessTest` incorpora seis escenarios nuevos: login, registro y primer
paso de recuperación, cada uno en claro y oscuro, a 320 dp y fuente 1.3.
Conservan las comprobaciones de desplazamiento, visibilidad, límites de línea,
altura y elipsis usadas a escala 2.0; no sustituyen capturas ni TalkBack.

- Suite focalizada: 16 pruebas, sin fallos; ktlint y detekt en verde (32s).
- Pipeline completo: `ktlintCheck detekt lint testDebugUnitTest
  :core:domain:test :core:common:test pitest assembleDebug`,
  **BUILD SUCCESSFUL in 30s**, 383 tareas (8 ejecutadas, 375 actualizadas).
- XML: **44 suites / 256 pruebas**, 0 fallos, errores u omitidas.
- Solo se modificó código de pruebas; APK y lógica de producción sin cambios.
- Este resultado es local; los checks de `42490ec` no acreditan estas nuevas
  pruebas hasta publicarlas y volver a ejecutar CI.

Siguen pendientes TalkBack manual, revisión integral de estados alternativos
(incluido paso 2 de recuperación a 1.3), capturas de acceso a esa escala y
aceptación visual humana. No se cierra `000`.
