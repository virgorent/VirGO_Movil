# Corrección de fundamentos visuales — T010–T014

| Campo | Valor |
|---|---|
| Fecha | 2026-11-20; seguimiento 2026-11-21 |
| Rama | `fix/000-fundamentos-visuales` |
| Base | `73ed69b` |
| Autorización | Usuario: «sí ya haz todo y aplica los cambios visuales» |
| Alcance ejecutado | Reparación de tareas aprobadas de `000`, no ejecución de nuevos flujos |
| Estado | T010–T013 cerradas técnicamente; T014 abierta; aceptación visual y cierre de `000` pendientes |
| Entrega | Código `693da47`, rama remota `fix/000-fundamentos-visuales`; [PR #29 en borrador](https://github.com/virgorent/VirGO_Movil/pull/29), base #27 |

## Límites

Se corrigen paleta, fuentes empaquetadas, formas y controles compartidos
existentes, según `000/plan.md` §6.1, ADR-06 y Art. VII. No se cambian navegación,
APIs, sesión, modelos de datos, fotografías, workflows ni permisos.

El spec documental `031` y el inventario completo permanecen en el worktree
`virgo-visual-reference`; esta reparación no les atribuye implementación ni
aprobaciones de etapas pendientes. El resto de pantallas requiere su flujo SDD
y el cierre de `000`.

La paleta exacta de marca se conserva. Para texto normal se distinguen tonos
accesibles: violeta original/blanco ≈4,467:1 y gris original/blanco ≈4,465:1 no
alcanzan 4,5:1. No se certifica toda la app por corregir componentes compartidos:
las pantallas con colores directos todavía requieren migración por bloque.

## Verificación

- Fuentes originales: Manrope 164 700 bytes y DM Sans 240 164 bytes; total
  404 864 bytes sin compresión, sin modificación de los TTF variables.
  Firmas básicas TTF y tablas `fvar`, `cmap`, `name`, `glyf` verificadas.
  Origen fijo, hashes y licencia en [`FUENTES.md`](../FUENTES.md).
- Catálogo T013: los nueve componentes (`VirGoButton`, `VirGoTextField`,
  `VirGoCard`, `VirGoBadge`, `VirGoTopBar`, `VirGoBottomBar`, `EmptyState`,
  `LoadingState` y `ErrorBanner`) tienen `@Preview` en claro y oscuro. La paleta
  y la escala de 15 estilos tipográficos tienen preview propio en ambos temas.
  El muestrario unificado conserva variantes a 320 dp con fuente 2.0. Son
  definiciones de preview, **no capturas renderizadas ni aceptación humana**.
- Regresiones nuevas: 23 ejecuciones de `:core:designsystem` (11 de tema,
  7 de accesibilidad y 5 de estados), además de las pruebas existentes. Cubren
  familias de los 15 estilos, carga de fuentes API 26/36, licencia empaquetada,
  roles del tema, contraste, semántica de error, áreas táctiles de 48 dp,
  selección de la barra inferior, región viva del error y reintento.
- Pipeline local con salida aislada en
  `%TEMP%\opencode\virgo-foundation-build`; no se comparten artefactos con otras ramas.
- No se ejecuta ninguna release ni se presume aprobación visual del usuario.

### Ejecución local

- Ejecución final del pipeline completo tras las correcciones de acceso:
  **BUILD SUCCESSFUL in 2m 4s** (383 tareas; 29 ejecutadas, 354 actualizadas).
  Es una compilación incremental sobre el árbol estable, no desde cero.
  Comando: `ktlintCheck detekt lint testDebugUnitTest :core:domain:test
  :core:common:test pitest assembleDebug` con el init script de salida aislada.
- Totales de los XML: **43 suites, 237 pruebas, 0 fallos, 0 errores y 0 omitidas**
  (`app` 95; `core` 142: common 4, data 41, datastore 4, designsystem 28,
  domain 56, network 9). `DesignSystemStatesTest` aporta 5 pruebas y
  `DesignSystemThemeTest`/`DesignSystemAccessibilityTest` las 18 restantes de
  la ronda. Acceso: `FontScaleTest` 4, `CompactAccessTest` 10 y
  `AuthAccessibilityTest` 3, todas en verde.
- Verificación focalizada previa de `:core:designsystem`:
  `BUILD SUCCESSFUL in 1m 28s` con ktlint, detekt y pruebas en verde.
- Lint: 35 avisos y 0 errores; ninguno señala archivos de `:core:designsystem`
  (son recursos sin usar, versiones disponibles, plurales, icono monocromo,
  reglas de extracción de datos y SDK obsoleto del proyecto).
- Pitest: dominio 60 % (57/95 mutaciones), common 33 % (1/3); 93 % y 100 % de
  fuerza de pruebas respectivamente. Sin cambios respecto de la corrida anterior.
- La prueba focalizada `ExploreScreenTest.filtersWithNoMatchShowEmptyState`
  pasó antes de esta ejecución (`BUILD SUCCESSFUL in 2m 32s`) tras corregir el
  desplazamiento anidado del panel de filtros.

### APK y capturas

- **APK final** `app-debug.apk`: 23 889 777 bytes, SHA-256
  `ccb9c4b5b02e51ef1583d0946902eb928a989aaf260667231a12d465f4df4fb1`.
  Instalación `adb install -r` → `Success`; `sha256sum` del `base.apk`
  instalado coincide exactamente con el artefacto local.
  Firma debug verificada con `apksigner`, certificado SHA-256
  `de462575df353dc1730784dbb30fe939bd5ce1f5a4f8070c20355c1bccfe0ded`.
- Capturas del **APK final** en Pixel_10_Pro/API 37, 960×1920/480 dpi
  (viewport configurado 320×640 dp), fuente 2.0:

  | Pantalla/estado | Evidencia |
  |---|---|
  | Login oscuro, cabecera/sociales | [`T014-final-login-320dp-2x-oscuro.png`](T014-final-login-320dp-2x-oscuro.png) |
  | Login oscuro, pie alcanzado por desplazamiento | [`T014-final-login-320dp-2x-oscuro-pie.png`](T014-final-login-320dp-2x-oscuro-pie.png) |
  | Registro oscuro, cabecera/beneficios | [`T014-final-registro-320dp-2x-oscuro.png`](T014-final-registro-320dp-2x-oscuro.png) |
  | Registro oscuro, envío/pie/términos | [`T014-final-registro-320dp-2x-oscuro-pie.png`](T014-final-registro-320dp-2x-oscuro-pie.png) |
  | Recuperación oscura, primer paso | [`T014-final-recuperacion-320dp-2x-oscuro.png`](T014-final-recuperacion-320dp-2x-oscuro.png) |
  | Recuperación oscura, IME abierto | [`T014-final-recuperacion-320dp-2x-oscuro-teclado.png`](T014-final-recuperacion-320dp-2x-oscuro-teclado.png) |
  | Recuperación clara, aviso completo tras desplazar | [`T014-final-recuperacion-320dp-2x-claro-pie.png`](T014-final-recuperacion-320dp-2x-claro-pie.png) |
  | **Bloqueo** de visitante compacto oscuro: solapamiento y superficies claras | [`T014-visitante-compacto-2x-oscuro-bloqueo.png`](T014-visitante-compacto-2x-oscuro-bloqueo.png) |

  `dumpsys input_method` confirmó `mInputShown=true` y
  `mIsInputViewShown=true`. Se descartó la toma transitoria con el aviso de
  cambio de fuente del teclado; la imagen conservada muestra el estado estable.
  No se introdujeron credenciales ni se pulsó envío de recuperación/login.
  La API local no disponible provoca el estado de error del catálogo:
  no se presenta como un fallo de autenticación ni prueba contra producción.
- Restauración del emulador: 1280×2856, 480 dpi, fuente 1.0 y modo claro.
  Las capturas acreditan estados concretos, **no aceptación visual, TalkBack
  completo ni accesibilidad global**.

#### Artefactos anteriores conservados

- APK instalado durante el seguimiento anterior: 23 889 777 bytes, SHA-256
  `aaf72ef3854bac596538d264d46207986ae054654bdf474f393b899836877db1`.
  `adb install -r` terminó con `Success`, sin borrar datos. No contiene las
  correcciones compactas añadidas después; no es el APK final de esta ronda.
- Firma debug del artefacto anterior verificada con `apksigner verify --print-certs`
  (2026-11-21): SHA-256 de certificado
  `de462575df353dc1730784dbb30fe939bd5ce1f5a4f8070c20355c1bccfe0ded`.
- Las capturas iniciales
  [`T010-T014-login-claro.png`](T010-T014-login-claro.png) (1280×2856, tema
  claro, fuente 1.0) y las compactas/2.0 documentan el layout anterior, el corte
  de «Google» y el enlace final fuera de viewport. Son evidencia de defectos de
  T014, no de aprobación. Las capturas `T014-acceso-*.png` del seguimiento
  anterior muestran login, registro y recuperación con fuente 1.0/2.0 en claro
  a 1280×2856/480 dpi. Su APK es el indicado arriba: **no acreditan el árbol
  compacto corregido después**, ambos temas ni comportamiento con teclado.
- La sesión del emulador se interrumpió por reinicio del servidor. Se arrancó de
  nuevo en frío sin snapshots ni borrado de datos para continuar la verificación.

### Catálogo T013 completado

Se añadieron `VirGoTopBar`, `VirGoBottomBar` + `VirGoBottomBarItem`, `EmptyState`,
`LoadingState` y `ErrorBanner`, y previews claro/oscuro a los cuatro controles
que solo aparecían en el muestrario. También se añadieron previews de paleta y
escala tipográfica. La barra inferior usa `selectableGroup`, rol `Tab`, estado
seleccionado y objetivo táctil de 64 dp; el error usa `liveRegion` y el botón de
reintento mide al menos 48 dp.

Durante la verificación, detekt marcó `MatchingDeclarationName` en
`VirGoBottomBar.kt`; `VirGoBottomBarItem` se movió a su archivo propio sin
desactivar reglas. `LongMethod` en el muestrario se resolvió extrayendo
`AiBadgeSample`. Las pruebas de área táctil revelaron que `IconButton` y
`TextButton` medían 40 dp; se corrigieron a 48 dp en los componentes, no se
relajaron las aserciones.

### Corrección de acceso para T014

Tras revisar las capturas con fuente 2.0 se corrigieron defectos concretos de la
pantalla de inicio de sesión, sin cambiar navegación ni contrato:

- La cabecera pasa de altura fija a `heightIn(min = 220.dp)` para que el título
  no se recorte al crecer la letra; el botón de regreso sube de 40 a 48 dp.
- La tarjeta y el fondo usan `colorScheme.surface`/`background` en vez de blanco
  y gris fijos, por lo que el modo oscuro deja de mostrar una tarjeta blanca.
- El pie deja de ser una `Row` y pasa a `Column` centrada: «Crear cuenta» ya no
  queda fuera del viewport cuando la fuente crece. La acción de recuperación y
  el enlace de registro mantienen 48 dp mínimos.
- Las etiquetas de Google y Apple usan `labelLarge`; el seguimiento compacto
  apila las acciones con fuente ≥1.5 y permite ajustar sus textos en vez de
  ocultar un desbordamiento con `maxLines=1`.
- `FontScaleTest` ahora comprueba a fuente 2.0 que Google, Apple, recuperación,
  envío, «¿No tienes cuenta?» y «Crear cuenta» estén visibles, y que el envío
  siga siendo pulsable. No se retiró ninguna aserción existente.

### Seguimiento compacto (2026-11-21)

- Se corrigen los colores fijos de error, divisor y visibilidad de contraseña en
  `AuthComponents` por roles del tema; la acción de visibilidad exige 48×48 dp.
- El selector de roles pasa de mínimo 40 a 48 dp, declara `selectableGroup` y
  apila opciones con fuente ≥1.5. No se altera el registro ni sus contratos.
- La cabecera de registro coloca regreso y textos en una misma columna para
  reservar su separación incluso cuando crece el texto. Los beneficios permiten
  varias líneas también a escalas intermedias.
- Recuperación incorpora `imePadding`; los campos de acceso conservan su
  identificación semántica con valores presentes y exponen el error concreto.
  Los errores nuevos declaran región viva cortés. Falta verificar la experiencia
  real con lector de pantalla, no solo la presencia de estas propiedades.
- Se añade `CompactAccessTest`, a 320×640 dp/fuente 2.0 en claro y oscuro,
  con ancho efectivo comprobado, límites reales de líneas, altura, ausencia de
  elipsis y truncado, y prueba de 48×48 dp/grupo/selección exclusiva/callback del
  selector a fuente normal. Incluye separación geométrica de cabecera con/sin
  salón y paso 2/error de recuperación con datos ficticios, sin llamadas reales.
- `AuthAccessibilityTest` comprueba nombre accesible con valor no vacío,
  error específico y su eliminación al corregir, región viva y cambio de
  descripción/objetivo táctil al mostrar/ocultar contraseña.
- Revisión independiente de QA, solo lectura: además detecta riesgos en
  cabecera de registro, beneficios a escalas intermedias, recuperación con IME,
  identificación de campos llenos y anuncio de errores. Una revisión estática
  no demuestra que esos layouts se hayan capturado ni aceptado.
- El `check.md` elimina una fila duplicada y contradictoria de CA-06 y alinea
  los IDs RNF con el spec. Las evidencias históricas se conservan como históricas.
- Consulta GitHub de solo lectura: #17–#20 están `APPROVED` y `OPEN`; #27/#28
  están `OPEN` sin decisión de revisión. No se ejecutó merge, push ni release.

La disposición apilada con letra grande es una adaptación de accesibilidad,
no una nueva interpretación del prototipo a escala normal. Tampoco reemplaza
las fotografías pendientes ni acredita equivalencia con Figma.

Esta sección no declara aceptación visual ni cierre de T014/T036.

### Incidencias y límites del seguimiento

- Detekt rechazó los literales `1.5f` sin nombre; se introdujeron constantes
  `LARGE_FONT_SCALE`, sin desactivar reglas.
- El primer chequeo compacto con `hasVisualOverflow` señaló etiquetas cortas.
  El diagnóstico registró, por ejemplo, «Google»: texto medido 91×34 px,
  párrafo asignado 232×34 px, `didOverflowWidth=true`. No se interpreta
  automáticamente esa asignación sobrante como glifos cortados. El test final
  verifica límites reales izquierdo/derecho de cada línea, altura, elipsis y
  truncado. Los textos centrados del selector/pies ocupan el ancho disponible.
- Una consulta de «Nueva contraseña» era ambigua porque coincide con la cabecera
  y con el campo del paso 2; se selecciona explícitamente la cabecera en esa
  prueba, manteniendo las demás comprobaciones.
- **T014 continúa abierta:** el inicio visitante real aún se solapa a 320 dp/2.0
  y no adapta todas sus superficies al modo oscuro. También falta verificación
  manual completa de TalkBack, estados alternativos y teclado en ambos pasos.
- Gitleaks no está disponible en PATH; no se acredita escaneo local de secretos.
  La publicación posterior en #29 habilita el seguimiento de CI por head;
  no se atribuyen resultados remotos sin comprobarlos. CodeQL está filtrado a
  PRs contra `main` y debe verificarse antes de integrar allí.

### Incidencias de la primera verificación

- Pipeline inicial: `BUILD FAILED in 2m 20s`, por `UnusedPrivateMember` de
  detekt sobre la función `@Preview` privada. Se cambió su visibilidad a
  `internal`, sin desactivar reglas ni excluir el archivo.
- Segunda ejecución: `BUILD FAILED in 3m 6s`, por el overload experimental de
  carga de fuentes variables. Se añadió `@OptIn(ExperimentalTextApi::class)`
  exclusivamente al helper que selecciona los pesos; no se cambió la versión de
  Compose ni se desactivó una comprobación global.
- El primer arranque del AVD terminó con código 1 al cargar `default_boot`.
  Se intenta arranque en frío sin cargar ni guardar snapshot, con renderer de
  software y Vulkan desactivado. No se usa `wipe-data` ni se borran apps/datos.
- El arranque en frío logró `sys.boot_completed=1`, API 37, 1280×2856,
  480 dpi, fuente 1.0 y tema claro. Al abrir el APK anterior durante la
  compilación apareció un ANR de **System UI**, no evidencia de un crash de
  VirGo. Se eligió «Wait»; no se cerró ni borró la app.
- APK previamente instalado: SHA-256
  `76622b161857b9a6a046dae4790809f36709ab4107cd5fa3738b750d8a1adf90`,
  23 032 267 bytes. Su commit no está acreditado; no se usa como evidencia del
  nuevo código ni como baseline de rendimiento comparable.
- Tercera ejecución: `BUILD FAILED in 7m 6s`; de 80 pruebas de `app`, una falló:
  `ExploreScreenTest.filtersWithNoMatchShowEmptyState` no encontraba «2 km».
  El primer `.performScrollTo()` desplazaba solo el contenedor horizontal. Se
  desplaza primero su `onParent()`, luego el chip y «Aplicar», sin retirar
  aserciones; la focalizada y la ejecución completa posteriores quedaron verdes.
- La sesión inicial del emulador terminó y dejó la instalación pendiente; el
  seguimiento posterior logró instalar el APK y obtener capturas parciales,
  antes del siguiente reinicio del servidor.

### Revisión estática independiente

Revisión de seguridad sin hallazgos abiertos tras recalcular los pares corregidos
de ayuda y error oscuro. Compatible con la reparación de T010–T014; no certifica
accesibilidad global, ejecución, fuentes en dispositivos ni cierre de `000`.
La comprobación de bytes/hash y estructura TTF la realizó el agente principal;
la revisión independiente verificó avisos y condiciones OFL contra el origen.

### Contraste calculado de pares sólidos

Tras detectar insuficiencia del gris inicial sobre el fondo suave y del primer
texto de error oscuro, se corrigieron ambos antes de compilar. Valores derivados
de los tokens Kotlin, sRGB:

| Par | Claro | Oscuro |
|---|---|---|
| Texto/botón primario | 6,288:1 | 8,451:1 |
| Texto/insignia | 5,527:1 | 13,040:1 |
| Texto/superficie | 17,534:1 | 13,982:1 |
| Ayuda/fondo de campo | 4,965:1 | 6,096:1 |
| Error/fondo de campo | 6,038:1 | 6,099:1 |
| Texto/fondo de error | 6,461:1 | 7,717:1 |
| Contorno/superficie | 3,531:1 | 4,862:1 |
| Contorno/fondo de campo | 3,301:1 | 3,442:1 |

Se comprobaron 22 pares en total, incluidos secundarios, terciarios y contenedor
de error. Esta tabla no evalúa estados deshabilitados, capturas ni colores
directos de todas las pantallas; no certifica accesibilidad global de la app.
