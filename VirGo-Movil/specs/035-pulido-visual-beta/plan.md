# Plan — 035 Pulido visual de la beta

| Campo | Valor |
|---|---|
| Módulo | `035-pulido-visual-beta` |
| Versión | 0.1.0 |
| Estado | Aprobado por el usuario (2026-11-25) |
| Fecha | 2026-11-25 |
| Constitución | v1.2.0 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- Primero el sistema de diseño (`:core:designsystem`): componentes nuevos y ajustes, cada uno con
  *preview* claro/oscuro y pruebas. Después las pantallas de `:app` los consumen.
- Ningún cambio en `:core:domain`, `:core:data`, `:core:network` ni en los `ViewModel`.
- Las pantallas conservan sus `*Content` sin estado; solo cambia su composición.

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Framework | Compose Material 3 |
| Íconos | `material-icons-extended` (ya incluido por `virgo.android.compose`) |
| Tipografía | Escala existente de `VirGoTypography` (Manrope/DM Sans); no se copian los 7–11 px del prototipo |
| Colores | Tokens existentes (`VirGoPurple`, `VirGoPurpleContainer`, `VirGoInk`, `VirGoMuted`, `VirGoLine`, `VirGoSoft`, `SuccessGreen`, `WarningYellow`, `ErrorRed`) mapeados a roles del esquema |

## 3. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I | ⚠️ Pendiente | Spec visual atómico; falta aprobación. |
| II | ⚠️ Pendiente | Sin endpoints; requiere cierre de `000` o excepción (P-01). |
| III | ✅ Cumple | Sin cambios de arquitectura ni de estado. |
| IV–V | ✅ Sin cambios | No toca red, sesión ni datos. |
| VI | ✅ Planificado | Pruebas de componentes, barrido de accesibilidad y regresión. |
| VII | ✅ Cumple | Refuerza el sistema de diseño; pantallas solo con sus componentes. |
| VIII–XII | ✅ Cumple | Pipeline existente; memoria; español/inglés. |

## 4. Estructura

```text
core/designsystem/.../components/VirGoPageHeader.kt   # nuevo
core/designsystem/.../components/VirGoRow.kt          # nuevo (VirGoRow, VirGoRowIcon, tonos)
core/designsystem/.../components/VirGoSection.kt      # nuevo
core/designsystem/.../components/VirGoBadge.kt        # + VirGoBadgeTone
core/designsystem/.../components/VirGoCard.kt         # radio 14 dp, borde line
core/designsystem/.../components/VirGoStateComponents.kt  # EmptyState con ícono destacado
core/designsystem/.../components/DesignSystemPreview.kt   # previews nuevos
core/designsystem/src/test/.../                       # pruebas de componentes y contraste de tonos
app/.../ui/beta/BetaCatalogScreen.kt                  # encabezado, vacío, tarjetas
app/.../ui/beta/BetaSectionScreen.kt                  # menú con filas, sección con EmptyState
app/.../ui/account/AccountProfileScreen.kt            # iniciales, filas, logout al final
app/.../demo/RoleWorkspace.kt                         # VirGoPageHeader; logout a Cuenta
app/.../demo/DemoNavigationBar.kt                     # píldora del ítem activo
app/.../demo/DestinationIcons.kt                      # nuevo: ruta → ícono
app/src/main/res/values/beta_strings.xml              # textos nuevos
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Motivo |
|---|---|---|
| ADR-01 | Componentes en `:core:designsystem`, no en `:app` | `006`/`008`/`009`/`022` los reutilizan (Art. VII). |
| ADR-02 | Escala tipográfica accesible en lugar de los px del prototipo | El prototipo escala un teléfono en el navegador; copiarlo rompe RNF-01. |
| ADR-03 | Insignias con texto **y** color | El color solo no comunica estado (accesibilidad). |
| ADR-04 | «Cerrar sesión» en Cuenta y no en la barra superior | Igual que el prototipo; evita un botón de ancho completo en cada pantalla. |
| ADR-05 | Sin desenfoque ni sombras grandes | `backdrop-filter` no tiene equivalente barato en Compose; se usa fondo sólido `soft`. |

## 6. Diseño

### 6.1 Medidas (dp)

| Elemento | Valor |
|---|---|
| Margen de página | 14–16 |
| Separación entre bloques | 12 |
| Radio de tarjeta / fila suelta / burbuja de ícono | 14 / 13 / 10 |
| Burbuja de ícono | 32 |
| Botón de regreso (visible / táctil) | 36 / 48 |
| Píldora de la barra inferior | 34 × 28, radio 15 |
| Insignia | radio 7, padding 4 × 7 |

### 6.2 Íconos por destino (P-02)

Se amplía el mapa de `demoTabIcon` a todos los destinos con equivalentes de Material:
explorar → `Explore`, favoritos → `FavoriteBorder`, estudio → `AutoAwesome`, evento →
`Event`, cuenta → `Person`, mensajes → `ChatBubbleOutline`, notificaciones →
`Notifications`, cotizaciones → `RequestQuote`, reservaciones → `EventAvailable`,
pagos → `Payments`, salones → `Storefront`, calendario/agenda → `CalendarMonth`,
solicitudes → `Inbox`, estadísticas → `BarChart`, ayuda → `HelpOutline`; el resto →
`ChevronRight` neutro.

### 6.3 Seguridad

Sin cambios.

## 7. DevSecOps y CI/CD

Pipeline existente; sin dependencias, permisos ni secretos nuevos.

## 8. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | Contraste AA de los cinco tonos de insignia y de la burbuja de ícono en ambos temas |
| UI (componentes) | `VirGoPageHeader` con/sin regreso y acciones; `VirGoRow` con/sin flecha y 48 dp; `EmptyState` con acción |
| UI (pantallas) | CA-01 a CA-05; las pruebas existentes de `034`/`030` se ajustan solo en selectores, no en lo que verifican |
| Accesibilidad | Barrido de `BetaAccessibilityTest` (PR #36) ampliado a Cuenta, a 320 dp/2.0 en ambos temas |
| Regresión | Pipeline completo en verde |
| Evidencia visual | Capturas antes/después a 393 dp (claro/oscuro) en `docs/evidencia/035/` |

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Expectativa de «igual al prototipo» | El spec no afirma paridad; aceptación visual del usuario por captura. |
| Pruebas que dependen de textos de botones | Se ajustan selectores manteniendo la intención (p. ej. «Próximamente» pasa a insignia). |
| Conflictos con `030`/`034` sin fusionar | Rama apilada sobre `feat/030-cuenta-sesion`. |

## 10. Fases

F1 sistema de diseño (T3501–T3503) · F2 pantallas (T3504–T3507) · F3 accesibilidad, capturas y cierre (T3508–T3509).
