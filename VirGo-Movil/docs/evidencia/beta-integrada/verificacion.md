# Verificación de la beta integrada

**Fecha:** 2026-11-24. Rama `feat/034-beta-integrada`, desde `main` tras la
fusión de #32. Alcance: [spec 034](../../../specs/034-beta-integrada/spec.md).

## Qué cambia

- Entrada normal de visitante y cliente con catálogo del repositorio real,
  sin fallback a salones ficticios ni mapa con ubicación supuesta.
- Navegación por los tres roles existentes y menú «Secciones».
- Destinos sin servicio con estado «Próximamente» y acción deshabilitada.
- Cuenta solo lectura con identidad de la sesión; cierre local explícito.
- Sin ruta ni enlace al recorrido demo, tampoco en debug.
- Recuperación de contraseña informa indisponibilidad en vez de abrir un
  recorrido cuyo endpoint no está operativo.
- No se eliminan los componentes históricos ni sus pruebas. No son destinos
  accesibles de la beta.

## Pipeline local final

```powershell
$env:ANDROID_HOME = 'C:\Users\cesar\AppData\Local\Android\Sdk'
.\gradlew.bat -I C:\Users\cesar\AppData\Local\Temp\opencode\virgo-isolated-build.gradle ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug
```

- **BUILD SUCCESSFUL**, 2 min 21 s, 383 tareas.
- **60 suites, 334 pruebas, cero fallos, errores u omitidas**, contando los XML
  de `test-results` de app y módulos core.
- Pitest del dominio existente en verde (incremental); no se añadió lógica de
  dominio nueva.
- Una primera ejecución falló por `LongMethod` en catálogo; se extrajeron
  cabecera y tarjeta, sin suprimir reglas, y el pipeline completo posterior pasó.
- Permanecen avisos de APIs de test Compose deprecadas y de compatibilidad
  futura con Gradle 10; no son errores del pipeline actual.

El init script local solo redirige las salidas a `virgo-build`; no es necesario
en CI ni está versionado. No se modificaron workflows ni gates.

## Pruebas específicas

- `BetaCatalogViewModelTest`: vacío sin muestras, error/reintento, datos sin
  coordenadas, carga suspendida y prevención de refrescos concurrentes.
- `BetaScreensTest`: carga/vacío/error/datos, acciones pendientes deshabilitadas,
  capacidad ausente no inventada, cuenta sin edición, sección en oscuro con
  fuente 2.0 y recorrido por todos los destinos pendientes de los tres roles.
- `NavigationTest`: entrada beta sin demo/selector, registro sin seleccionar un
  salón ficticio, regreso y acceso al login existente.
- `UnifiedWorkspaceTest`: logout local y menú propietario sin destinos proveedor.
- Se conservan las regresiones de autenticación, sesión y componentes históricos.
- Revisión estática de seguridad sin bloqueantes; se corrigió mostrar «Hasta 0
  personas» cuando la API no aporta capacidad.

## APK y emulador

- APK debug: `virgo-build/app/outputs/apk/debug/app-debug.apk`.
- SHA-256:
  `1d6834b699b51fc6bbe99a28fac86287a72ef91f4c70165067c1b9c6d2cc9e2c`.
- Instalado con `adb install -r` en `emulator-5554` (`Pixel_10_Pro`).
- Arranque y catálogo comprobados; capturas finales leídas visualmente:
  - [01 · error de catálogo](01-catalogo-error.png).
  - [02 · catálogo vacío claro](02-catalogo-vacio.png).
  - [03 · catálogo vacío oscuro](03-catalogo-vacio-oscuro.png).
- Debug conserva `10.0.2.2:5122`. El estado vacío se probó con un servidor
  temporal local que devolvía únicamente `{"data":[]}` para catálogo. Se detuvo
  después; el error se capturó sin ese servidor.
- No se crearon cuentas, no se introdujeron credenciales ni se probaron pagos,
  reservas, IA o mensajes. Esta comprobación **no** es humo de producción.
- Una captura inicial tras reinstalar mostró solo el fondo durante la
  transición; se descartó y se repitió con la pantalla visible.

## Límites pendientes

- Aceptación visual exclusiva de `ccabrera-dev8`, TalkBack y revisión integral
  de todos los tamaños/navegación con fuente ampliada.
- Perfil y logout remotos siguen pendientes de `030`; no hay refresh.
- Catálogo vacío de producción requiere publicar salones desde backend/operación,
  no reintroducir muestras.
- La navegación de secciones está conservada; su composición es un estado vacío
  deliberado, no las tarjetas, formularios y resultados de la maqueta anterior.
- CI remoto y revisión de PR antes de fusionar.
- Sin release, sin cambio de versión, sin cierre de `000` ni otros specs.
