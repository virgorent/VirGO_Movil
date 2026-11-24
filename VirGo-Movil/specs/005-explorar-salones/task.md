# Tareas — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Versión | 1.1.0 |
| Estado | En revisión; consolidación automática verificada, aceptación visual pendiente |
| Fecha | 2026-11-19 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T501 | Pantalla B8 Explorar (mapa) y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T502 | Pantalla C9 Explorar (lista) y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T503 | Pantalla C10 Filtros y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T504 | Pantalla C11 Recomendados para ti y su `ViewModel` | `000`: T017 | `dev-android` | Coincide con la maqueta; textos en `strings.xml` | ✅ |
| T505 | Repositorio y casos de uso | T501 | `dev-android` | Pruebas con MockWebServer en verde | ✅ |
| T506 | Pruebas unitarias, de UI y de seguridad | T504 | `qa-movil` | `./gradlew testDebugUnitTest` en verde | ✅ |
| T507 | Integrar con la URL real y hacer la prueba de humo | Specs de API | `qa-movil` | `check.md` completo; JSON y endpoints operativos entregados | ⛔ Avisos/favoritos 404 en la última sonda |
| T508 | Consolidar fotos, catálogo inicial, ajustes de layout y pruebas de regresión; actualizar docs y PR #27 | T501–T506 | `dev-android` + `qa-movil` | Pipeline completo actual; código `5d14843`, evidencia T508 e historial del PR #27; sin cierre visual | ✅ |
| T509 | Validar visualmente el APK del rediseño y actualizar capturas de lista/mapa/hojas | T508 | `qa-movil` + equipo | Capturas actuales, revisión en pantalla pequeña, fuente 2.0, oscuro, teclado y Atrás | ⏳ Sin dispositivo conectado al iniciar la consolidación |

**Evidencia:**

> **T501** ✅ Pantalla B8 (mapa) con `ExploreMapContent`, tarjeta resumen del salón seleccionado, marcador de favorito y estados de carga/error/vacío. La cabecera (marca, campana de avisos con contador, buscador, categorías y conmutador mapa/lista) vive en `ExploreScreen`/`ExploreHeader`; el `ExploreViewModel` es único para B8/C9/C10/C11 (`e09071f`). Captura (CA-01): `docs/evidencia/CA-01-explorar-b8-mapa.png`.

> **T502** ✅ Pantalla C9 (lista) con `ExploreListContent`: tarjetas con foto, calificación, capacidad, precio y distancia (`ExplorePriceDistanceRow` en `ExploreVenueParts.kt`), orden recomendado/precio/distancia y cambio a mapa al tocar un salón (`e09071f`). Captura (CA-02): `docs/evidencia/CA-02-explorar-c9-lista.png`.

> **T503** ✅ Hoja C10 de filtros (`ExploreFiltersPanel` en `ExplorePanel.kt` + `ExploreFilters.kt`): zona, invitados, presupuesto, fecha, distancia y categorías; validación de campos (RF-13/RF-14/RF-16) que deja el panel abierto y marca el error; botones Aplicar/Limpiar (`e09071f`). Captura (CA-03): `docs/evidencia/CA-03-explorar-c10-filtros.png`.

> **T504** ✅ Hoja C11 recomendaciones (`ExploreRecommendedPanel`): score local 0..100, `ExploreReason`, badge «Estimación IA», máx. 5 sugerencias y filtro por tipo de evento (`e09071f`). Captura (CA-04): `docs/evidencia/CA-04-explorar-c11-recomendados.png`.

> **T505** ✅ Contrato `VenueDto` ampliado aditivo (precio, categorías, fotos con defaults), endpoints `FavoritesApi`/`NoticesApi` con sus DTOs, `FavoritesRepositoryImpl`/`NoticesRepositoryImpl` y casos de uso `GetFavorites`, `GetNotices`, `ToggleFavorite` (`94f3a73`). Verificado con MockWebServer: 6 pruebas de favoritos (alta/baja/listado, 404/409/sin conexión) y 5 de avisos (leído/no leído, vacío, 404/500/sin conexión) en verde (`e77b50b`).

> **T506** ✅ `./gradlew ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug` → **BUILD SUCCESSFUL**; **193 pruebas, 0 fallos** en 38 suites (19 `ExploreViewModelTest`, 6 `ExploreScreenTest` de UI con Robolectric) y mutación combinada **59%** (`:core:domain` 60%, 57/95, strength 93%; `:core:common` 33%) (`f298967`).

> La evidencia T501–T506 anterior corresponde a los commits indicados, no al último
> rediseño. T508/T509 registran por separado su regresión y aceptación pendiente.

> **T508** ✅ Código `5d14843`: catálogo inicial con fotos amplias de muestra,
> créditos dentro de la app (incluido «Para ti»), layout e insets, y regresiones.
> Pipeline completo aislado → **BUILD SUCCESSFUL in 5m 26s**; **199 pruebas,
> 0 fallos/errores/omitidas, 38 suites**, con **12 pruebas de UI de Explorar**.
> Pitest: dominio 60 % (57/95), common 33 % (1/3).
> Evidencia: [`T508-consolidacion.md`](../../docs/evidencia/T508-consolidacion.md).
> Publicación en la rama del PR #27; gates remotos, revisión y T509 se verifican
> aparte y no quedan acreditados por este verde local.

## Matriz de trazabilidad

| Requerimiento / criterio | Tareas |
|---|---|
| RF-01–RF-09, CA-01 | T501, T505–T507; límites de cartografía/avisos en `spec.md` |
| RF-10–RF-11, CA-02 | T502, T506, T508–T509 |
| RF-12–RF-20, CA-03 | T503, T506, T509; disponibilidad por fecha pendiente de contrato |
| RF-21–RF-23, CA-04 | T504, T506, T509; puntuación local |
| CA-05–CA-06 | T505–T508: pruebas explícitas de API fallida, desconexión y reintento |
| RF-24, CA-07 | T508–T509 |
| RF-25, CA-08 | T508–T509 |
| RF-26, CA-09 | T508–T509 |
| RNF-01–RNF-02 | T506, T508–T509; estilos y accesibilidad aún no certificados |
| RNF-03 | T505–T508; gates de seguridad del PR |
