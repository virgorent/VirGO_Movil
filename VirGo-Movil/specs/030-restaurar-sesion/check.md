# Check — 030 Restaurar sesión

Solo se marca Cerrado con evidencia completa o no aplicación justificada.
La redacción de un borrador no acredita ejecución de tareas ni aprobación.

| Campo | Valor |
|---|---|
| Módulo | `030-restaurar-sesion` |
| Estado del spec | Borrador |
| Revisó | Revisión documental inicial del agente; aprobación humana pendiente |
| Fecha de cierre | — |

## 1. Especificación y gobernanza

| Control | Evidencia | Estado |
|---|---|---|
| Cuatro documentos basados en plantillas | Spec, plan, task y check presentes | ✅ |
| Alcance atómico | Restauración local; renovación/logout excluidos | ✅ |
| Spec, plan y tareas aprobados por etapa | Sin aprobación de contenido todavía | ⏳ |
| Constitution Check habilitado para implementación | Base `000`, contrato y políticas pendientes | ⏳ |
| Preguntas abiertas respondidas | P-01–P-07 en spec; especial decisión de reinicio tras fallo de invalidación | ⏳ |
| Handoff versionado y probado | T3001; no basta un borrador del backend | ⏳ |
| Coherencia de `002`/`003` y roles | T3001–T3002 | ⏳ |

## 2. Criterios de aceptación

| CA | Evidencia requerida | Estado |
|---|---|---|
| CA-01 | Arranque sin sesión y cero llamadas de renovación/logout/perfil | ⏳ |
| CA-02 | Persistencia y recreación real del proceso antes del vencimiento | ⏳ |
| CA-03 | Fallo de guardado sin navegación autenticada | ⏳ |
| CA-04 | Reloj fake: límite exacto y posterior, primer plano y solicitudes nuevas | ⏳ |
| CA-05 | Formato antiguo, incompleto y corrupto sin acceso ni cierre | ⏳ |
| CA-06 | Fallo transitorio de lectura, reintento y ausencia de borrado destructivo | ⏳ |
| CA-07 | Restauración offline y error de contenido que conserva sesión vigente | ⏳ |
| CA-08 | Rotación/recomposición/Atrás sin destinos duplicados ni formulario anterior | ⏳ |
| CA-09 | 401 actual con invalidación persistida correctamente; 401 tardío aislado y 403/404/5xx/red no invalidantes | ⏳ |
| CA-10 | Registro 201 sin tokens y reinicio sin sesión inventada | ⏳ |
| CA-11 | Inspección de archivo/salidas/UI y revisión de credenciales | ⏳ |
| CA-12 | Fallo de persistencia de invalidación sin acceso ni falsa confirmación; reinicio según decisión P-07 | ⏳ |

## 3. Pruebas

| Tipo | Evidencia requerida | Estado |
|---|---|---|
| Unitarias | Política y repositorio; totales/resultados del head final | ⏳ |
| Integración | MockWebServer, persistencia y respuestas tardías | ⏳ |
| UI | Arranque, error/reintento, navegación, claro/oscuro/fuente 2.0 | ⏳ |
| Seguridad | AEAD en Robolectric más Keystore real en dispositivo | ⏳ |
| Mutación | Pitest con línea base y condiciones críticas de vigencia | ⏳ |
| Longitudes | Sin nuevos campos editables; formularios existentes fuera de alcance | ➖ |
| Humo | APK publicado, proceso recreado, expiración y offline; QA autorizado | ⏳ |
| Rendimiento | T3018: arranque antes/después, sin bloqueo del hilo principal y umbral acordado tras medir | ⏳ |
| Compatibilidad | T3019: arranque, persistencia y Keystore en API 26 y 36 con evidencia por versión | ⏳ |

## 4. DevSecOps

| Control | Evidencia requerida | Estado |
|---|---|---|
| Secretos | Gitleaks del head publicado sin hallazgos | ⏳ |
| Dependencias | Dependency review sin vulnerabilidades altas | ⏳ |
| Estático | ktlint, detekt, Lint y CodeQL en verde | ⏳ |
| Permisos mínimos | Sin permisos peligrosos, SDKs de nube ni cambios de secretos | ⏳ |
| Salidas saneadas | Sin tokens, contraseñas ni datos personales en logs/capturas/reportes | ⏳ |

## 5. PR e integración continua

- [x] PR de documentación en borrador: [#28](https://github.com/virgorent/VirGO_Movil/pull/28), apilado sobre #27; no implica aprobación.
- [ ] PR de implementación posterior a aprobaciones: no creado.
- [ ] Checks del head definitivo en verde y una revisión.
- [ ] Fusión coordinada con Karasu; no automática.

## 6. Release

- [ ] Autorización humana previa y aviso a Karasu.
- [ ] Versión, commit y ejecución manual registrados.
- [ ] APK/AAB firmados y verificados.
- [ ] Humo sobre ese mismo APK publicado.

## 7. Memoria y aprendizaje

- [x] Entrada «2026-11-19 · Borrador 030 de restauración local de sesión» en `memoria.md`, con límites y preguntas.
- [ ] Entrada de cierre, solo después de implementación/publicación/verificación.
- [x] Mejora propuesta: separar restauración, renovación y logout; no prometer duración de refresh sin implementar su contrato.
