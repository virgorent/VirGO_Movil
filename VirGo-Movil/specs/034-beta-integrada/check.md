# Check — 034 Beta integrada

**Estado:** implementado y verificado localmente; sin aceptación visual ni release.

Informe: [verificacion.md](../../docs/evidencia/beta-integrada/verificacion.md).

| Criterio | Evidencia | Estado |
|---|---|---|
| CA-01 | `NavigationTest`; ruta VisualPreview retirada del grafo y de Routes | Verificado localmente |
| CA-02 | `BetaCatalogViewModelTest`, `BetaScreensTest`; capturas 01–03 | Verificado localmente |
| CA-03 | `BetaScreensTest.everyPendingDestinationAcrossAllRolesHasOnlyADisabledAction` | Verificado localmente |
| CA-04 | `BetaScreensTest`, `UnifiedWorkspaceTest`; identidad de sesión, sin edición | Verificado localmente |
| CA-05 | `NavigationTest` y suites existentes de login/registro/sesión | Regresión local en verde; sin humo de producción |
| CA-06 | Capturas claro/oscuro, sección a 320 dp con fuente 2.0, botones del sistema de diseño ≥48 dp | Parcial; aceptación humana/TalkBack pendientes |

- Unitarias, integración, UI y regresiones: 60 suites/334 pruebas, sin fallos.
- Seguridad: revisión estática sin bloqueantes; rol/sesión conservados, sin APIs
  nuevas; Gitleaks y CodeQL remotos pendientes.
- Pitest: dominio existente en verde, sin lógica de dominio nueva.
- ktlint, detekt, lint y assembleDebug: en verde, pipeline final 2m21s.
- Instalación y arranque debug en emulador; fixture de catálogo vacío local,
  no acreditan funcionamiento de producción.
- [PR #35](https://github.com/virgorent/VirGO_Movil/pull/35) en borrador, asignado
  solo a `ccabrera-dev8`; CI y aceptación humana pendientes. Sin release.
