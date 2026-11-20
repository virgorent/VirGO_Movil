# Tareas — 030 Restaurar sesión

| Campo | Valor |
|---|---|
| Módulo | `030-restaurar-sesion` |
| Versión | 0.1.0 |
| Estado | Borrador. Ninguna tarea se ejecuta hasta aprobar spec, plan y este archivo por etapa. |
| Fecha | 2026-11-19 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |
| Constitución | Móvil v1.1.1 vigente según memoria |

Estados: ⏳ Pendiente · 🔄 En curso · ✅ Hecha con evidencia · ⛔ Bloqueada.
🧑 exige confirmación humana. Cada tarea debe durar 30 min–2 h; dividirla antes de
ejecutar si la estimación supera ese rango. El gate de `000` aplica a toda tarea
de implementación, aunque no se repita en cada fila.

## F0 · Contrato y aprobación

| ID | Fase | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|---|
| T3001 | F0 | Fijar handoff de login y resolver P-01–P-07 | Entrega backend / producto | `arquitecto-movil` | OpenAPI/JSON probado con commit, políticas y respuestas registradas sin secretos | ⏳ |
| T3002 | F0 | 🧑 Obtener aprobación explícita de spec, después plan y después tareas | T3001 | `arquitecto-movil` + equipo | Aprobaciones por etapa y Constitution Check actualizado; cierre de `000` antes de código | ⏳ |

## F1 · Dominio y almacenamiento

| ID | Fase | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|---|
| T3003 | F1 | Definir estado, reloj y política pura de vigencia | T3002, cierre de `000` | `dev-android` | Dominio JVM sin Android; límites y contrato definidos | ⏳ |
| T3004 | F1 | Probar política de vigencia/restauración | T3003 | `qa-movil` | Antes/en/después de vencimiento, duración inválida/overflow, reloj incoherente | ⏳ |
| T3005 | F1 | Añadir registro cifrado atómico y tratamiento del formato heredado | T3003 | `dev-android` | Guardar/leer registro completo; sin migración con duración inventada | ⏳ |
| T3006 | F1 | Probar persistencia, corrupción y fallos transitorios | T3005 | `qa-movil` | Robolectric/nueva instancia; fallo de lectura/guardado/borrado diferenciado | ⏳ |

## F2 · Integración de sesión

| ID | Fase | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|---|
| T3007 | F2 | Coordinar login con guardado antes del éxito | T3005 | `dev-android` | Repositorio es fuente de verdad; cancelación y fallo de guardado no autentican | ⏳ |
| T3008 | F2 | Probar login/persistencia con MockWebServer | T3007 | `qa-movil` | 200, 401 público, fallo de guardado y cancelación con datos sintéticos | ⏳ |
| T3009 | F2 | Integrar estado raíz y navegación idempotente | T3007, decisión de roles/registro | `dev-android` | Sin flash autenticado, error/reintento y back stack correcto | ⏳ |
| T3010 | F2 | Alinear transporte e invalidación con generación y vigencia actuales | T3007 | `dev-android` | Sin token vencido; 401 viejo no borra sesión nueva; invalidación durable o error seguro | ⏳ |
| T3011 | F2 | Probar UI, ciclo de vida, transporte y regresión de registro | T3008–T3010 | `qa-movil` | CA-01–CA-10; rotación, proceso recreado y errores no equivalentes a 401 | ⏳ |

## F3 · Calidad y PR

| ID | Fase | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|---|
| T3012 | F3 | Revisar seguridad de almacenamiento, Keystore y salidas | T3006, T3011 | `seguridad-ci` | CA-11, fallo Keystore recuperable, sin tokens en UI/logs ni fallback en claro | ⏳ |
| T3013 | F3 | Ejecutar mutación de la política de sesión | T3004 | `qa-movil` | Reporte Pitest y línea base; condiciones críticas cubiertas | ⏳ |
| T3018 | F3 | Medir arranque antes/después y acordar umbral de rendimiento con el equipo | T3002 para base; T3009 para comparación | `qa-movil` + equipo | Mediciones, método y umbral acordado registrados; sin bloqueo de hilo principal | ⏳ |
| T3019 | F3 | Verificar persistencia y Keystore en extremos Android soportados | T3011–T3012 | `qa-movil` | Ejecución en API 26 y 36 con arranque, guardado/restauración y error de Keystore; evidencia por versión | ⏳ |
| T3014 | F3 | Preparar PR y verificar gates del head definitivo | T3012–T3013, T3018–T3019 | `seguridad-ci` | Linters, pruebas, Pitest, APK, CodeQL, Gitleaks y dependencias en verde; revisión | ⏳ |

## F4 · Publicación y cierre

| ID | Fase | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|---|
| T3015 | F4 | 🧑 Publicar versión mediante workflow manual | T3014, merge y autorización de release | `seguridad-ci` + equipo | Run, commit, APK/AAB y firma verificada; coordinar antes con Karasu | ⏳ |
| T3016 | F4 | Ejecutar humo autorizado del APK publicado | T3015 | `qa-movil` | Login → muerte de proceso → restauración; expiración y offline; evidencia sin secretos | ⏳ |
| T3017 | F4 | Completar check y memoria de cierre | T3016 | `qa-movil` + `arquitecto-movil` | CA y pruebas acreditados; aprendizajes y versión; sin cierre anticipado | ⏳ |

## Matriz de trazabilidad

| Requerimiento / criterio | Tareas |
|---|---|
| HU-01 | T3007–T3009, T3011, T3016 |
| HT-01 | T3003–T3006, T3010–T3012 |
| HT-02 | T3007, T3009–T3011 |
| RF-01, RF-05, CA-03 | T3005–T3008 |
| RF-02, CA-01, CA-08 | T3009, T3011 |
| RF-03, CA-02, CA-04 | T3003–T3006, T3009–T3011, T3016 |
| RF-04, CA-05 | T3003–T3006, T3010–T3011 |
| RF-06, CA-06 | T3005–T3006, T3009, T3011 |
| RF-07, CA-07 | T3009, T3011, T3016 |
| RF-08, RF-09, CA-09, CA-12 | T3001–T3002, T3010–T3012 |
| RF-10, CA-10 | T3001–T3002, T3009, T3011 |
| RNF-01, CA-11 | T3005–T3006, T3010, T3012, T3014 |
| RNF-02 | T3009, T3011, T3016, T3018 |
| RNF-03 | T3009, T3011, T3016 |
| RNF-04 | T3002, T3012, T3014, T3019 |
| RNF-05 | T3004, T3006, T3008, T3011–T3017 |
