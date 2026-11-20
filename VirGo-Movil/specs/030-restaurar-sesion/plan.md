# Plan — 030 Restaurar sesión

| Campo | Valor |
|---|---|
| Módulo | `030-restaurar-sesion` |
| Versión | 0.1.0 |
| Estado | Borrador; propuesta no aprobada |
| Fecha | 2026-11-19 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |
| Constitución | Móvil v1.1.1 vigente según memoria; v1.2.0 propuesta |

## 1. Resumen técnico

- Reutilizar `app` y `core:*`; sin nueva librería de red ni SDK AWS.
- Repositorio de sesión como única fuente de verdad, inyectado por Hilt.
- Registro cifrado, versionado y atómico con access token, vencimiento e identidad mínima.
- Estado raíz explícito; UI sin acceso a credenciales ni almacenamiento.
- No implementar refresh, logout ni validación mediante `/users/me`.

## 2. Contexto técnico

| Aspecto | Situación / propuesta |
|---|---|
| Stack | Kotlin, Compose Material 3, MVVM/UDF, Hilt, Flow, Retrofit/OkHttp |
| Persistencia actual | `TokenStore` solo guarda `auth_token` cifrado con DataStore/Tink/Keystore |
| Login actual | `AuthRepositoryImpl` devuelve `AuthSession`; `LoginViewModel` descarta su contenido |
| Navegación actual | `VirGoNavHost` arranca en `GuestHomeRoute`; login/registro navegan a `HomeRoute` |
| Red actual | `AuthInterceptor` lee token sin vigencia ni invalidación por 401 |
| Plataforma | Android min 26 / compile-target 36; toolchain de compilación 17 |
| Pruebas | JUnit, Turbine, Robolectric/Compose, MockWebServer, Pitest en módulos JVM |

## 3. Constitution Check

| Art. | Tema | Resultado | Evidencia / condición |
|---|---|---|---|
| I | SDD y atomicidad | ✅ Propuesta | Un flujo, cuatro documentos; ninguna tarea de implementación autorizada. |
| II | Base primero | ⚠️ Bloqueado | Se permite redactar; ejecución sujeta a cierre de `000` T033/T036. |
| III | Arquitectura | ✅ Diseño | Capas existentes, dominio puro, repositorio y DI; sin dependencias entre features. |
| IV | Contrato | ⚠️ Pendiente | Handoff login versionado y semántica `expiresIn` por confirmar; sin endpoints nuevos. |
| V | Seguridad | ⚠️ Pendiente | AEAD/Keystore sin fallback en claro; política de reloj/Keystore requiere aprobación. |
| VI | Pruebas | ✅ Diseño | Todos los tipos previstos en sección 8; ninguna ejecución acreditada todavía. |
| VII | Diseño | ✅ Diseño | Estados con componentes existentes; revisión claro/oscuro y fuente 2.0 prevista. |
| VIII | CI y entrega | ✅ Diseño | PR/revisión/gates; release manual posterior con autorización, sin despliegues backend. |
| IX | Observabilidad | ✅ Diseño | Correlación existente; nada de tokens/cuerpos ni datos personales en logs. |
| X | Trazabilidad | ✅ Diseño | Matriz en task, CA en check y memoria canónica. |
| XI | Agentes | ✅ Alcance | Roles existentes; no ejecutar implementación ni release sin aprobación. |
| XII | Idioma | ✅ Cumple | Documentación/textos en español, nombres de código en inglés. |

Resultado: **borrador condicionado**. No está habilitado para implementación;
resolver P-01–P-07 del spec antes de aprobar.

## 4. Estructura propuesta

```text
core/domain/      estado público sin secretos, política de vigencia y contrato de repositorio
core/data/        coordinación login → guardado → éxito y resolución de sesión
core/datastore/   registro cifrado versionado; lectura/guardado/invalidez
core/common/      puertos mínimos compartidos de credencial/generación si hacen falta
core/network/     autorización de solicitudes y notificación de 401 de la sesión usada
app/             ViewModel raíz, estados de arranque y navegación idempotente
core/testing/    fakes de repositorio, reloj y fallos para suites Android
```

Respetar el grafo existente; no introducir `domain → Android`, `network → data`
ni ciclos de Hilt. Los nombres definitivos de clases se fijan al aprobar el plan.

## 5. Decisiones técnicas propuestas (no ratificadas)

| ADR local | Decisión | Alternativa descartada / motivo |
|---|---|---|
| 030-01 | Persistir un único registro cifrado atómico y versionado | Escrituras separadas podrían dejar token/identidad/vencimiento incoherentes. |
| 030-02 | Guardar solo access token y metadatos mínimos, sin refresh token | No conservar una credencial de larga duración sin capacidad aprobada que la utilice. |
| 030-03 | Persistir vencimiento absoluto calculado una sola vez | Reiniciar `expiresIn` al arrancar extiende acceso indebidamente. |
| 030-04 | Resolver localmente sin llamar `/me` | Evitar dependencia de un endpoint no entregado y definir claramente el límite offline. |
| 030-05 | Invalidación condicionada a generación local de sesión | Un 401 tardío no debe borrar un login posterior. |
| 030-06 | Descartar como restaurable el formato antiguo de solo token | Carece de vencimiento fiable; no inventar vigencia ni interpretar JWT como verificación. |

## 6. Diseño

### 6.1 Contrato y persistencia

Usar únicamente el login ya existente `POST /api/v1/auth/login` y su respuesta
`accessToken`, `tokenType`, `expiresIn`, `refreshToken`,
`refreshTokenExpiresAt`, `user`. No cambiar DTOs para inventar una expiración remota.
Los campos refresh se reciben por compatibilidad del contrato, pero no se persisten
ni se publican a UI en este spec.

Propuesta de registro: versión, generación local, access token, instante de
referencia, vencimiento absoluto e identificador/rol mínimo del usuario.
No guardar nombre/correo si no se necesitan para el arranque. La escritura debe
completarse antes del éxito del login; propagar cancelación y reportar errores.

Reloj inyectable. Proponer `expiresAt = requestStartedAt + expiresIn` como cálculo
conservador frente a latencia, pendiente de confirmar con backend. Validar duración
positiva, desbordamientos y coherencia de fechas; `now >= expiresAt` no es vigente.
Revalidar al volver a primer plano, al llegar el vencimiento estando activo y antes
de enviar una solicitud nueva. No reiniciar la duración al restaurar.

### 6.2 Resolución y navegación

Estado público sin secretos: `Restoring`, `Authenticated`, `Unauthenticated`,
`RestoreError`. Mientras se resuelve, no crear la pantalla autenticada ni disparar
sus solicitudes. No bloquear el splash indefinidamente por un fallo recuperable.

Ausencia/registro inválido → no autenticado. Fallo transitorio de lectura →
`RestoreError` y reintento, sin borrado automático. Keystore inaccesible no debe
provocar un cierre en la construcción del grafo de Hilt: diseñar su frontera
recuperable en datos. No degradar a texto en claro.

Limpiar el back stack de formularios tras login; no duplicar destinos con cambios
de configuración. Registro sin token no se considera autenticado. Su transición y
roles no cliente quedan bloqueados por las decisiones P-02/P-03.

### 6.3 Coherencia de red e invalidación

El proveedor de credenciales deriva de la misma sesión válida del repositorio.
Un evento de 401 se asocia a la generación usada en esa solicitud, no a cualquier
sesión presente al recibir la respuesta. El coordinador en datos invalida solo si
coincide con la actual, elimina el registro y actualiza el estado raíz. No incluye
tokens en eventos, errores, tags de diagnóstico o logs.

Si falla el borrado por almacenamiento, bloquear el uso de la sesión y ofrecer
recuperación; no afirmar que la invalidación es durable hasta confirmarla.
La prueba debe ejercitar el fallo y posterior reinicio, no solo vaciar memoria.
Con el registro previo intacto no se puede garantizar invalidación después de una
muerte del proceso y restauración offline: CA-09 se limita a persistencia exitosa,
CA-12 cubre el fallo durante ese proceso y P-07 bloquea la aprobación hasta decidir
si se requiere validación remota de arranque o se acepta esa limitación.
No se presenta el registro de un fallo en otro archivo como garantía cuando toda
escritura puede fallar.

401 de login público no invalida otra sesión. 403/404/5xx, timeout y desconexión
no son expiración. No usar `Authenticator` para renovar o repetir solicitudes.

### 6.4 Seguridad y límites del reloj

No parsear JWT para obtener nombre/correo ni atribuirle validación criptográfica.
Denegar acceso ante tiempo incoherente conocido; un retroceso antes del instante
de referencia invalida la restauración propuesta. El reloj local no garantiza
detectar toda manipulación: backend aplica expiración real y autorización.
La política final de reloj y de errores de Keystore requiere P-05.

## 7. DevSecOps y CI/CD

| Gate | Herramienta | Cuándo |
|---|---|---|
| Secretos | Gitleaks | PR y push a main |
| Dependencias | Dependabot / dependency review | Cadencia existente / PR |
| Estático | ktlint, detekt, Android Lint, CodeQL | PR |
| Pruebas | Suites y Pitest existentes; APK debug | PR |
| Release | `movil-release`, firma verificada, APK/AAB | Manual, solo tras confirmación humana |

Sin cambios de permisos/cloud ni servicios de pago. Karasu coordina integración.
No se ejecuta release ni se cambia ningún secreto para redactar estos documentos.

## 8. Estrategia de pruebas

| Tipo | Casos previstos |
|---|---|
| Unitarias | Vigencia antes/en/después del límite, duración inválida/overflow, tiempo incoherente, estado e invalidación por generación. |
| Integración | MockWebServer para login/401; fake reloj/store; éxito solo tras guardado, cancelación, fallo de borrado y respuesta tardía. |
| Persistencia | Robolectric: registro atómico, nueva instancia, formato antiguo, corrupción y errores transitorios diferenciados. |
| UI | Resolución sin flash, error/reintento, back stack, rotación y registro sin sesión; claro/oscuro/fuente 2.0. |
| Seguridad | Archivo sin token/identidad en claro, sin secretos en UI/logs, no envío del acceso vencido, Keystore inaccesible sin fallback. |
| Mutación | Pitest de política pura y decisiones de vigencia; medir línea base, sin umbral inventado. |
| Humo | APK publicado: login QA autorizado → muerte de proceso sin borrar datos → reapertura antes del vencimiento y después; offline dentro de vigencia. |
| Rendimiento | T3018 mide arranque base y posterior, verifica ausencia de bloqueo principal y registra umbral acordado con el equipo tras medir. |
| Compatibilidad | T3019 ejecuta arranque/persistencia/Keystore real en emulador o dispositivo API 26 y 36; no sustituirlo por solo compilar. |

Las suites usan credenciales sintéticas y MockWebServer, no producción.
Robolectric con AEAD inyectado no prueba por sí solo Android Keystore real; verificar
esa ruta en dispositivo. Solo el humo autorizado usa backend real y evidencia saneada.

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Usuario espera sesión de días | Explicitar límite de 900 s documentados; renovación es otro spec. |
| Backend/documentación difieren | Fijar commit de OpenAPI/handoff y confirmar JSON probado antes de implementar. |
| Estado heredado/Keystore falla | Migración segura sin vigencia inventada y frontera de error recuperable. |
| Inicio por registro o rol no soportado | Resolver P-02/P-03 antes de aprobar tareas de navegación. |
| Almacenamiento falla durante invalidación | Prueba de durabilidad y recuperación; no limitarse a borrar estado en memoria. |
| Base no cerrada | No ejecutar tareas de código hasta resolver el gate de `000`. |

## 10. Fases

| Fase | Contenido | Tareas |
|---|---|---|
| F0 | Contrato, preguntas y aprobación por etapa | T3001–T3002 |
| F1 | Política y persistencia | T3003–T3006 |
| F2 | Login, navegación y transporte coherentes | T3007–T3011 |
| F3 | Seguridad, mutación, rendimiento, compatibilidad y PR | T3012–T3014, T3018–T3019 |
| F4 | Release autorizado, humo y cierre | T3015–T3017 |
