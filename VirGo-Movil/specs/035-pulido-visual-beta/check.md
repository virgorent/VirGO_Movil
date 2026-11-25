# Check — 035 Pulido visual de la beta

| Campo | Valor |
|---|---|
| Módulo | `035-pulido-visual-beta` |
| Estado del spec | Aprobado por el usuario (2026-11-25) · implementado, pendiente de aceptación visual |
| Revisó | Claude Code (evidencia técnica); aceptación visual: `ccabrera-dev8` (pendiente) |
| Fecha | 2026-11-25 |

La revisión técnica no sustituye la aceptación visual del usuario (CA-07). No se afirma paridad con Figma: la referencia es el prototipo local «VirGo Event Management App».

## 1. Especificación y gobernanza

- [x] Spec, plan y tareas aprobados por el usuario el 2026-11-25 («Apruebo y amplío»).
- [x] P-01: excepción ampliada y registrada (`docs/diseno/excepcion-030-sin-cierre-000.md`).

## 2. Criterios de aceptación

| CA | Evidencia | Estado |
|---|---|---|
| CA-01 | `BetaScreensTest` (catálogo con datos, vacío y error) + `despues-catalogo-*.png` | ✅ |
| CA-02 | `UnifiedWorkspaceTest` y `BetaScreensTest` (menú por rol, insignias «Próximamente» sin acción) + `despues-secciones-*.png` | ✅ |
| CA-03 | `BetaScreensTest` (sección sin servicio con `EmptyState`) + `despues-seccion-pendiente-*.png` | ✅ |
| CA-04 | `AccountProfileScreenTest` + pruebas de `030` en verde + `despues-cuenta-*.png` | ✅ |
| CA-05 | `NavigationTest` y `UnifiedWorkspaceTest` (encabezado, barra inferior) + `despues-espacio-proveedor-*.png` | ✅ |
| CA-06 | `BetaAccessibilityTest`: 320 dp, escala 2.0, ambos temas, 48 dp, sin texto recortado ni de ancho mínimo; `ToneContrastTest` (AA) | ✅ |
| CA-07 | Aceptación visual del usuario sobre `docs/evidencia/035/antes-*` y `despues-*` | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | `ToneContrastTest` (3), `PrototypeComponentsTest` (4) | ✅ |
| UI | `app` 33 suites / 209 pruebas, `core:designsystem` 6 / 35 | ✅ |
| Accesibilidad | `BetaAccessibilityTest` (6): defecto real corregido en `VirGoRow` (el contenido final se apila bajo el título a escala ≥ 1.3) | ✅ |
| Regresión | Pipeline aislado `ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug`: **BUILD SUCCESSFUL in 6m 13s**; 67 suites / 376 pruebas, 0 fallos (+14 sobre `030`) | ✅ |

## 4. PR

- [x] PR: [virgorent/VirGO_Movil#40](https://github.com/virgorent/VirGO_Movil/pull/40) (base `feat/030-cuenta-sesion`).
- [ ] Una revisión aprobada y checks requeridos en verde.

## 5. Memoria

- [x] Entrada en `memoria.md` (2026-11-25 · `035-pulido-visual-beta` implementado).
