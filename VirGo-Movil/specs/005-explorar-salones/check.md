# Check — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Versión | 1.1.0 |
| Estado del spec | En revisión; consolidación autorizada, sin cierre visual ni humo completo |
| Revisó | Agente: consistencia documental; aceptación humana pendiente |

## 1. Especificación

- [x] Alcance y límites reales documentados en spec/plan.
- [x] Matriz de RF/RNF/CA en `task.md`.
- [ ] Constitution Check sin pendientes de cierre.
- [ ] Contratos operativos completos de favoritos y avisos.
- [ ] Revisión humana de la versión consolidada.

## 2. Criterios de aceptación

Las capturas `CA-01`–`CA-04` existentes se conservan como evidencia histórica del
commit `c3451b4`; no acreditan el catálogo inicial con fotografías amplias.

| CA | Evidencia / pendiente | Estado |
|---|---|---|
| CA-01 | `docs/evidencia/CA-01-explorar-b8-mapa.png` histórica; mapa ilustrativo y revisión nueva pendiente | 🔄 |
| CA-02 | `docs/evidencia/CA-02-explorar-c9-lista.png` histórica; fotografía amplia pendiente de captura/aceptación | 🔄 |
| CA-03 | `docs/evidencia/CA-03-explorar-c10-filtros.png` histórica; nueva revisión de teclado/tamaño/fuente pendiente | 🔄 |
| CA-04 | `docs/evidencia/CA-04-explorar-c11-recomendados.png` histórica; nueva revisión de hoja pendiente | 🔄 |
| CA-05 | `ExploreScreenTest.apiFailureShowsSafeMessageAndRetry` en verde; evidencia T508 | ✅ |
| CA-06 | `ExploreScreenTest.offlineErrorCanRetryAndShowCatalog` en verde; evidencia T508 | ✅ |
| CA-07 | `listModeShowsVenueCards`, `venueCardOpensMapAndCanReturnToList` y estado inicial en `ExploreViewModelTest` en verde; revisión visual pendiente | 🔄 |
| CA-08 | `sampleCreditsAreAvailableInApp`, `initialListCanScrollToSampleCredits`, `recommendationsIdentifySamplePhotosAndExposeCredits` en verde; créditos en app y `docs/CREDITOS-IMAGENES.md` | ✅ |
| CA-09 | `BackHandler` en `ExploreScreen`; evidencia de Atrás en dispositivo pendiente | ⏳ |

## 3. Pruebas

| Tipo | Evidencia | Estado |
|---|---|---|
| Unitarias | Pipeline completo actual: 199 pruebas, 0 fallos/errores/omitidas, 38 suites | ✅ |
| Integración | MockWebServer: favoritos (6), avisos (5), incluidos en pipeline actual en verde | ✅ |
| UI | `ExploreScreenTest`: 12 pruebas; lista/mapa/créditos/error/reintento en verde; no certifica aceptación visual | ✅ |
| Longitudes | Límites máximos y pruebas límite/límite+1/1500 de filtros no acreditados; no se certifica ese requisito | ⏳ |
| Seguridad | Diff revisado sin credenciales nuevas; hallazgo de identificación de muestras en «Para ti» corregido y re-verificado; gates del head remoto pendientes | 🔄 |
| Mutación | Pipeline actual: dominio 60 % (57/95, strength 93 %), common 33 % (1/3) | ✅ |
| Humo | Completo bloqueado por avisos/favoritos; no se repitió sonda productiva | ⛔ |
| Visual/adaptable | Sin dispositivo conectado en esta consolidación; T509 pendiente | ⏳ |

Evidencia de ejecución, reportes y hash del APK:
[`docs/evidencia/T508-consolidacion.md`](../../docs/evidencia/T508-consolidacion.md).

## 4. PR e integración continua

- [x] PR: [#27](https://github.com/virgorent/VirGO_Movil/pull/27).
- [x] Base verificada el 2026-11-19: `feat/004-recuperar-contrasena`; #19 sigue abierto.
- [x] Código y evidencia consolidados: `5d14843` y documentación de T508; el historial de #27 registra su publicación.
- [ ] Checks del nuevo head en verde; los verdes de `c3451b4` no cubren cambios locales.
- [ ] Revisión aprobada después del último push.
- [ ] Fusión por Karasu.

## 5. Release

- [ ] Release manual coordinada con el equipo; **no solicitada ni ejecutada** aquí.
- [ ] Humo sobre APK publicado y versión/commit registrados.

## 6. Memoria y aprendizaje

- [x] Entradas históricas de implementación y refinamiento en `memoria.md`.
- [x] Entrada de consolidación en `memoria.md` con resultado exacto y pendientes; no es cierre del módulo.
- [x] Mejora propuesta: exigir evidencia específica de errores y distinguir capturas históricas del head actual.
