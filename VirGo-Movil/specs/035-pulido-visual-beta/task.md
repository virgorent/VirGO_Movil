# Tareas — 035 Pulido visual de la beta

| Campo | Valor |
|---|---|
| Módulo | `035-pulido-visual-beta` |
| Versión | 0.1.0 |
| Estado | Aprobado por el usuario (2026-11-25). Tareas autorizadas por la excepción temporal ampliada ([`excepcion-030-sin-cierre-000.md`](../../docs/diseno/excepcion-030-sin-cierre-000.md)). |
| Fecha | 2026-11-25 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3501 | Capturas «antes» a 393 dp claro/oscuro de catálogo, secciones, sección sin servicio y Cuenta | Aprobación; P-01 | `qa-movil` | `docs/evidencia/035/antes-*.png` | ⏳ |
| T3502 | `VirGoPageHeader`, `VirGoRow`, `VirGoSection` con previews y pruebas | T3501 | `ui-designsystem` | Pruebas de componentes; 48 dp | ⏳ |
| T3503 | Ajustes de `VirGoCard`, `EmptyState`, `VirGoBadge` (tonos) y fondo `soft` | T3502 | `ui-designsystem` | Contraste AA de tonos en ambos temas | ⏳ |
| T3504 | Catálogo con encabezado, vacío/error con ícono y tarjetas nuevas | T3503 | `dev-android` | CA-01 | ⏳ |
| T3505 | Menú «Secciones» con filas e insignias; sección sin servicio con `EmptyState`; mapa de íconos | T3503 | `dev-android` | CA-02, CA-03 | ⏳ |
| T3506 | Cuenta con iniciales, filas y «Cerrar sesión» al final | T3503; `030` | `dev-android` | CA-04 y pruebas de `030` en verde | ⏳ |
| T3507 | Encabezado del espacio de trabajo y píldora de la barra inferior; revisión de acceso | T3503 | `dev-android` | CA-05 | ⏳ |
| T3508 | Barrido de accesibilidad 320 dp/2.0 ambos temas y pipeline completo | T3504–T3507 | `qa-movil` | CA-06; pipeline en verde | ⏳ |
| T3509 | Capturas «después», `check.md`, `memoria.md` y entrega para aceptación visual | T3508 | `qa-movil` | CA-07 pendiente del usuario | ⏳ |

## Matriz de trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01–RF-07 | CA-06 | T3502, T3503 |
| RF-08 | CA-01 | T3504 |
| RF-11, RF-12 | CA-02, CA-03 | T3505 |
| RF-13 | CA-04 | T3506 |
| RF-09, RF-10, RF-14 | CA-05 | T3507 |
| RF-15, RNF-01–RNF-05 | CA-06, CA-07 | T3508, T3509 |
