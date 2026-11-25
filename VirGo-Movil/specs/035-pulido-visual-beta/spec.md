# Spec — 035 Pulido visual de la beta

| Campo | Valor |
|---|---|
| Módulo | `035-pulido-visual-beta` |
| Versión | 0.1.0 |
| Estado | Propuesto para aprobación (2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Rol | Cliente, Propietario y Proveedor (y visitante) |
| Fuente | Prototipo autorizado `VirGo Event Management App` (`src/index.css`, `src/components/ui.tsx`, `BottomNav.tsx`, `VenueCard.tsx`) y `Checklist VirGo.md` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

La beta `034` retiró los demos y dejó pantallas honestas pero planas: textos
sueltos, tarjetas genéricas y botones de contorno apilados. El usuario indicó
que la app «no se ve tan increíble». Los **tokens** (colores, fuentes) ya
coinciden con el prototipo; lo que difiere es la **composición** de las
pantallas.

Este módulo lleva las pantallas alcanzables al lenguaje visual del prototipo y
agrega al sistema de diseño las piezas que faltan para que `006`, `008`, `009` y
`022` nazcan con el mismo aspecto. **Es solo visual**: no cambia datos,
endpoints, navegación ni reglas de negocio.

No se afirma paridad con el prototipo ni con Figma. La aceptación visual es
exclusiva del usuario.

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como persona que abre la app, quiero que se vea cuidada y coherente para confiar en ella. |
| HT-02 | Como persona con letra grande o tema oscuro, quiero que el nuevo diseño siga siendo legible y usable. |

## 3. Requerimientos funcionales

### 3.1 Sistema de diseño (`:core:designsystem`)

| ID | Requerimiento |
|---|---|
| RF-01 | **`VirGoPageHeader`**: encabezado de página con botón de regreso circular opcional (48 dp táctil, 36 dp visibles, fondo blanco y sombra suave), *kicker* opcional en violeta en mayúsculas con espaciado amplio, título en Manrope y zona de acciones a la derecha. Fondo `soft` (`#F7F7F9`). |
| RF-02 | **`VirGoRow`**: fila con ícono en burbuja (32 dp, radio 10 dp, fondo `violet-soft` y ícono violeta; variantes de tono), título, subtítulo de hasta 2 líneas, contenido a la derecha opcional y flecha cuando es tocable. Alto mínimo 48 dp. Se usa sola (borde y radio 13 dp) o dentro de `VirGoCard` (separadores de 1 dp). |
| RF-03 | **`VirGoSection`**: agrupador con *kicker* y título de sección y acción opcional. |
| RF-04 | **`EmptyState`** (existente) adopta el estilo del prototipo: ícono violeta grande, título en tinta, texto en `muted` con ancho máximo y botón primario opcional. |
| RF-05 | **`VirGoBadge`** (existente) añade tonos `green`, `amber`, `violet`, `gray` y `red` con fondo suave y texto de contraste AA, para estados como «Publicado», «Pendiente» o «Próximamente». |
| RF-06 | **`VirGoCard`** usa radio 14 dp y borde `line` (`#E9E9ED`) sin elevación en tema claro; en oscuro, superficie y borde del esquema oscuro. |
| RF-07 | Fondo de página `soft` en claro (equivalente oscuro en tema oscuro). |

### 3.2 Pantallas

| ID | Requerimiento |
|---|---|
| RF-08 | **Catálogo (visitante y cliente):** `VirGoPageHeader` con *kicker* «VIRGO · BETA» y título «Encuentra tu espacio»; acciones de cuenta como botón primario + secundario; estados vacío y error con `EmptyState` e ícono; tarjetas de salón con nombre, zona, capacidad con ícono y la acción en el pie de la tarjeta. Sin fotos, mapas, distancias ni valoraciones inventadas. |
| RF-09 | **Espacio de trabajo:** la barra superior pasa a `VirGoPageHeader` con el título de la sección actual y regreso cuando aplica. «Cerrar sesión» sale de la barra superior y vive en Cuenta. |
| RF-10 | **Barra inferior:** ícono dentro de una píldora `violet-soft` al estar seleccionado, etiqueta en violeta; no seleccionado en gris medio; fondo blanco con borde superior `line`. |
| RF-11 | **Menú «Secciones»:** lista de `VirGoRow` con ícono por destino, título y flecha, agrupada en `VirGoCard`; los destinos sin servicio muestran una insignia «Próximamente» a la derecha. |
| RF-12 | **Sección sin servicio:** `EmptyState` con ícono del destino, título de la sección y el texto actual de «Próximamente»; sin botón deshabilitado de ancho completo. |
| RF-13 | **Cuenta:** encabezado con iniciales en círculo violeta, nombre y correo; datos (teléfono, verificación, rol) como `VirGoRow` dentro de `VirGoCard`; aviso de datos sin actualizar y «Reintentar» como en `030`; botón «Cerrar sesión» al final, en tono de error. |
| RF-14 | **Acceso (login, registro, recuperación):** se revisan contra el prototipo y solo se ajustan fondo, tarjetas y espaciado si difieren; no cambia su lógica. |
| RF-15 | Todos los textos nuevos en `strings.xml`; íconos con `contentDescription` o marcados como decorativos. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Accesibilidad:** contraste AA de texto (4.5:1) y no texto (3:1) en ambos temas; objetivos ≥ 48 dp; sin cortes a 320 dp con fuente 2.0. Se conserva la escala tipográfica de Material 3 del sistema de diseño (el prototipo usa 7–11 px porque simula un teléfono en el navegador; no se copia). |
| RNF-02 | **Tema oscuro:** cada componente nuevo tiene variante oscura con los roles del esquema; ningún blanco fijo. |
| RNF-03 | **Sin dependencias nuevas:** íconos de `material-icons-extended` ya presente. |
| RNF-04 | **Honestidad:** no se reintroducen datos de muestra (decisión de `034`). |
| RNF-05 | **Sin regresiones:** las pruebas existentes de `034` y `030` siguen en verde. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** el catálogo vacío, con error y con datos, **cuando** se compara con el prototipo, **entonces** usa encabezado, estado vacío con ícono y tarjetas del nuevo estilo, sin datos inventados. |
| CA-02 | **Dado** el menú «Secciones» de cada rol, **cuando** se abre, **entonces** cada destino es una fila con ícono y flecha, y los no disponibles muestran «Próximamente». |
| CA-03 | **Dado** una sección sin servicio, **cuando** se abre, **entonces** se ve un estado vacío con ícono y no un botón deshabilitado. |
| CA-04 | **Dado** Cuenta, **cuando** se abre, **entonces** muestra iniciales, filas de datos y «Cerrar sesión» al final, y conserva el comportamiento de `030`. |
| CA-05 | **Dado** el espacio de trabajo, **cuando** se navega, **entonces** el encabezado muestra la sección actual y la barra inferior resalta la pestaña activa con su píldora. |
| CA-06 | **Dado** todas las pantallas anteriores a 320 dp/fuente 2.0 en tema claro y oscuro, **cuando** se ejecuta el barrido de accesibilidad, **entonces** no hay cortes, controles < 48 dp ni pares de contraste bajo AA. |
| CA-07 | **Dado** capturas antes/después a 393 dp en ambos temas, **cuando** el usuario las revisa, **entonces** las acepta (aceptación visual exclusiva del usuario). |

## 6. Seguridad y privacidad

Sin cambios: no se tocan red, sesión, almacenamiento ni permisos.

## 7. Fuera de alcance

- Pantallas aún no implementadas (`006`, `008`, `009`, `022`): solo heredan los componentes.
- Mapa, fotos, favoritos, búsqueda y filtros del catálogo del prototipo (no hay datos reales que mostrar).
- Animaciones, desenfoque de fondo (`backdrop-filter`) y barra de estado simulada del prototipo.
- Cambios de tokens de color o fuentes (ya coinciden).
- Pantallas de demo históricas no alcanzables.

## 8. Supuestos, dependencias y preguntas abiertas

- Depende de `034` (#35) y `030` (#39); se implementa apilado sobre ellos.
- Art. II.2: es un spec **visual** sin endpoints nuevos, por lo que no requiere contratos de backend; sí requiere cerrar `000` o una excepción aprobada.

| ID | Pregunta | Quién | Propuesta |
|---|---|---|---|
| P-01 | ¿Se cubre con la excepción vigente de `030` o con una nueva? | Usuario | Ampliar la excepción temporal a `035` (visual, sin red ni datos), con los mismos límites. |
| P-02 | ¿Íconos por destino? | Usuario | Íconos de Material equivalentes a los de `lucide` del prototipo; lista en `plan.md`. |
