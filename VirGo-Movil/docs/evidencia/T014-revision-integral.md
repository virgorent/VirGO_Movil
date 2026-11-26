# T014 · Revisión integral de accesibilidad (beta integrada)

Fecha: 2026-11-25. Rama `fix/000-cierre-accesibilidad`, apilada sobre `feat/034-beta-integrada` (PR #35).

## Alcance

Completa la revisión de T014 sobre las pantallas que hoy son alcanzables en la
app (beta `034`). Acceso (login, registro, recuperación) y visitante ya tenían
evidencia en [`T010-T014-correccion-visual.md`](T010-T014-correccion-visual.md)
y [`T014-visitante-adaptativo.md`](T014-visitante-adaptativo.md). No cambia
código de producción, pantallas, navegación ni endpoints.

## Pruebas automatizadas nuevas

### `BetaAccessibilityTest` (6 pruebas, `app/src/testDebug`)

Barrido genérico del árbol semántico de cada pantalla a **320 dp, fuente 2.0,
tema claro y oscuro**:

| Pantalla | Estados / variantes |
|---|---|
| Catálogo beta | Cargando, vacío, error con reintento, con datos (capacidad > 0 y = 0) |
| Menú «Secciones» | Cliente, propietario y proveedor |
| Destinos por rol | Todos los de `DemoDestinations` salvo `explore` (incluye Cuenta) |
| Recuperación | Pantalla de indisponibilidad |
| Espacio de trabajo | Propietario y proveedor, con barra inferior y menú abierto |

En **cada nodo** comprueba:

- Texto: sin desbordar alto, sin superar `maxLines`, sin elipsis y sin líneas
  fuera del ancho medido.
- Control tocable (`OnClick`, incluidos los deshabilitados): **≥ 48×48 dp**,
  usando el tamaño sin recorte del viewport.
- Imagen (`Role.Image`): con `contentDescription`.
- Ancho efectivo del viewport = 320 dp.

La primera ejecución detectó controles con 0 dp por estar fuera del viewport
(recorte del scroll, no defecto de diseño); se mide el tamaño real del control.
Esto confirma que el barrido inspecciona los objetivos y no pasa en vacío.

### Contraste (`DesignSystemThemeTest`, `:core:designsystem`)

La beta usa `primary` como **color de texto** (etiqueta «Beta», rol en Cuenta).
Antes solo se verificaba `primary/surface` a 3:1 (no texto). Se añadieron, en
ambos temas, con mínimo AA de texto normal **4.5:1**:

| Par | Uso en la beta |
|---|---|
| `primary` / `surface` | Rol en la tarjeta de Cuenta |
| `primary` / `background` | Etiqueta «Beta» del encabezado |
| `onSurfaceVariant` / `background` | Pistas del catálogo |
| `onBackground` / `background` | Títulos fuera de tarjeta |

Resultado: 11/11 en verde. Se suman a los 22 pares ya registrados en `memoria.md`.

## Resultado

Ninguna pantalla beta presentó cortes, elipsis, objetivos < 48 dp, imágenes sin
descripción ni pares de texto bajo 4.5:1. **No hubo que corregir código de
producción.**

## Verificación manual con TalkBack (pendiente del usuario)

Las pruebas JVM no sustituyen a TalkBack. Lista de ~5 minutos en el teléfono
(Ajustes → Accesibilidad → TalkBack) con la APK debug de esta rama:

| # | Paso | Esperado | ✅/❌ |
|---|---|---|---|
| 1 | Abrir la app sin sesión y deslizar por el catálogo | Lee «VirGo · Beta», título, pista, «Iniciar sesión», «Crear cuenta», estado (vacío/error) y «Actualizar» en orden lógico | |
| 2 | Tocar dos veces «Crear cuenta» | Abre registro; lee nombres de campo y roles como grupo seleccionable | |
| 3 | En login, enfocar contraseña y el botón de visibilidad | El botón anuncia su acción («Mostrar/Ocultar contraseña») | |
| 4 | Con sesión: recorrer la barra inferior | Cada pestaña lee su nombre y si está seleccionada | |
| 5 | Abrir una sección «Próximamente» | Lee el título y anuncia el botón como **desactivado** | |
| 6 | Abrir Cuenta | Lee nombre, correo, rol y el aviso de cierre de sesión local | |

Al completarse sin ❌, T014 pasa a ✅ y se desbloquea el cierre T036.

## Límites

- No acredita paridad con Figma ni la aceptación visual del usuario.
- La pantalla de catálogo del cliente dentro del espacio de trabajo usa
  `hiltViewModel()`; su contenido se cubre vía `BetaCatalogContent` directo.
- Pantallas de demo históricas no alcanzables (`VisualDemoApp`, hojas) quedan
  fuera de alcance: no son destinos de la app.
