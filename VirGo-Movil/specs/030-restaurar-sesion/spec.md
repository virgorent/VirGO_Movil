# Spec — 030 Restaurar sesión

| Campo | Valor |
|---|---|
| Módulo | `030-restaurar-sesion` |
| Versión | 0.1.0 |
| Estado | Borrador; solo redacción autorizada, no implementación |
| Fecha | 2026-11-19 |
| Constitución | Móvil v1.1.1 vigente; v1.2.0 sigue propuesta según `memoria.md` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El login obtiene credenciales, pero la app actual descarta la sesión recibida y
arranca siempre en visitante. Se propone conservar y recuperar una sesión de login
que aún tenga acceso vigente, sin pedir otra vez la contraseña ni mostrar contenido
autenticado antes de resolver el estado.

Esta capacidad no renueva credenciales. Con la duración de acceso documentada
actualmente (900 segundos), solo permite recuperar una sesión dentro de ese
intervalo, no mantener el acceso durante los 30 días del refresh token.
La restauración local no prueba que el servidor no haya revocado el acceso.

## 2. Historias

| ID | Historia |
|---|---|
| HU-01 | Como cliente que inició sesión, quiero recuperar mi acceso al reabrir la app mientras siga vigente para no repetir el login innecesariamente. |
| HT-01 | Como responsable de seguridad, quiero que datos ausentes, corruptos o vencidos no habiliten acceso autenticado. |
| HT-02 | Como responsable de integración, quiero que navegación y solicitudes utilicen el mismo estado de sesión para no enviar credenciales que la app ya considere inválidas. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | Un login válido se conserva de forma duradera antes de comunicar éxito a la navegación. Solo se retiene la información mínima necesaria. |
| RF-02 | Al arrancar, la app resuelve la sesión antes de mostrar visitante o el destino autenticado; mientras tanto muestra un estado de comprobación. |
| RF-03 | Solo se restaura una sesión completa cuyo acceso siga vigente. Reabrir la app no reinicia ni extiende su duración. |
| RF-04 | Ausencia, formato heredado insuficiente, datos corruptos o acceso vencido conducen a estado no autenticado y esas credenciales no se envían. |
| RF-05 | Si falla el guardado tras login, la app informa un error recuperable y no presenta una sesión autenticada como confirmada. |
| RF-06 | Un fallo transitorio de lectura no concede acceso ni destruye automáticamente el registro; se ofrece reintentar. |
| RF-07 | La restauración es local y funciona sin conexión. Los fallos posteriores de red del contenido no invalidan por sí solos una sesión vigente. |
| RF-08 | La navegación y el envío de credenciales comparten la misma sesión resuelta. Un acceso conocido como vencido no se envía en solicitudes nuevas y deja de habilitar el destino autenticado. |
| RF-09 | Un 401 de una operación protegida que utilizó la sesión actual bloquea su uso de inmediato e intenta persistir su invalidación, sin renovación ni reintento automático. Solo se confirma como durable tras completar la persistencia; si falla se muestra recuperación sin volver a habilitar acceso en ese proceso. Un 401 de login o de una sesión anterior no invalida un login nuevo. La política de reinicio tras fallo de persistencia queda bloqueada por P-07. |
| RF-10 | Un registro de cuenta sin credenciales no crea una sesión restaurable ni convierte datos de perfil en autorización. |

La propuesta inicial cubre el rol `client`. El tratamiento seguro de otros roles
y la navegación posterior al registro deben acordarse antes de aprobar este spec
(P-02/P-03); no se declaran resueltos por esta redacción.

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** credenciales cifradas en reposo y en tránsito; sin contraseñas guardadas ni tokens en UI, navegación, logs, capturas o reportes. No se conserva una credencial de renovación que este flujo no utiliza. |
| RNF-02 | **Rendimiento:** comprobación sin bloquear el hilo principal ni introducir esperas artificiales. Medir el arranque antes/después y acordar umbrales a partir de evidencia. |
| RNF-03 | **Accesibilidad:** estados de comprobación/error/reintento comprensibles, con texto en español, tema claro/oscuro y fuente 2.0 sin cortes. |
| RNF-04 | **Costo y compatibilidad:** Android 26–36; sin servicios, permisos peligrosos ni herramientas de pago nuevas. |
| RNF-05 | **Calidad:** pruebas deterministas de tiempo, persistencia, proceso recreado y respuestas tardías; trazabilidad y gates habituales del repositorio. |

No se añaden campos de texto editables; límites de entrada de formularios existentes
pertenecen a sus respectivos specs.

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un inicio sin sesión, **cuando** termina la comprobación, **entonces** se muestra visitante sin llamar a renovación, logout o perfil. |
| CA-02 | **Dado** un login guardado correctamente, **cuando** se mata y recrea el proceso antes del vencimiento, **entonces** se restaura la sesión sin mostrar primero visitante/login ni pedir contraseña. |
| CA-03 | **Dado** un fallo de guardado tras login, **cuando** termina el intento, **entonces** aparece un error recuperable y no se navega como autenticado. |
| CA-04 | **Dado** un acceso cuya hora de vencimiento es igual o anterior a la actual, **cuando** se evalúa al arrancar, al volver a primer plano o antes de una solicitud, **entonces** no habilita acceso ni se envía y no se intenta renovar. |
| CA-05 | **Dado** un registro antiguo con solo token, incompleto o indescifrable, **cuando** se arranca, **entonces** no se concede acceso ni hay cierre inesperado ni vigencia inventada. |
| CA-06 | **Dado** un fallo transitorio de lectura, **cuando** se arranca, **entonces** se ofrece reintento sin conceder acceso ni borrar credenciales por ese fallo. |
| CA-07 | **Dado** una sesión vigente sin conexión, **cuando** se reabre, **entonces** se restaura localmente y un fallo de red al cargar contenido no la elimina. |
| CA-08 | **Dado** una sesión resuelta, **cuando** hay rotación o recomposición, **entonces** no se duplican destinos ni Atrás abre el formulario de login de la sesión vigente. |
| CA-09 | **Dado** un 401 protegido de la sesión actual y una invalidación persistida correctamente, **cuando** se reinicia, **entonces** esa sesión no vuelve a habilitar acceso; un 401 tardío de otra sesión, 403, 404, 5xx o desconexión no borran una sesión vigente nueva. |
| CA-10 | **Dado** un registro 201 que solo devuelve perfil, **cuando** se reinicia, **entonces** no existe una sesión restaurable derivada de ese perfil. |
| CA-11 | **Dado** una sesión guardada, **cuando** se inspeccionan almacenamiento, estado de UI y salidas de prueba, **entonces** no aparecen credenciales en claro. |
| CA-12 | **Dado** un 401 actual y un fallo al persistir la invalidación, **cuando** se informa el resultado, **entonces** el proceso actual sigue sin acceso, ofrece recuperación y no anuncia borrado durable; la aceptación del comportamiento tras muerte del proceso exige resolver P-07. |

## 6. Seguridad y privacidad

| Amenaza | Control propuesto |
|---|---|
| Exposición de credenciales | Cifrado de todo el registro, minimización de identidad y ausencia de cuerpos/tokens en logs. |
| Acceso con datos parciales o duración reiniciada | Persistencia atómica, versión de formato y vencimiento absoluto verificado. |
| Revocación desconocida sin conexión | No presentar restauración local como validación del servidor; backend autoriza cada operación y 401 actual invalida localmente. |
| Respuesta tardía que borra un login nuevo | Invalidación ligada a la generación de sesión utilizada por la solicitud. |
| Reloj o almacenamiento no confiable | Denegar acceso ante vigencia no determinable; política exacta pendiente P-05. |

## 7. Fuera de alcance

- Renovación/rotación de refresh token y reintento automático de solicitudes.
- Logout manual, su botón y revocación remota.
- Login automático después del registro.
- Nuevas pantallas de propietario/proveedor, perfil, enlaces profundos o `/users/me`.
- Datos offline de negocio, favoritos persistentes y bandeja de avisos.
- Detectar revocación remota sin comunicación o prometer acceso autenticado offline
  al servidor. La invalidación local por vencimiento/401 no implementa logout.

## 8. Supuestos, dependencias y preguntas abiertas

**Supuestos**

- El backend sigue siendo la autoridad de autenticación y autorización.
- Login entrega duración relativa y usuario; registro entrega perfil sin sesión.
- No se deduce identidad decodificando claims del token ni se inventan endpoints.

**Dependencias**

- Cierre pendiente de `000-arquitectura-base` (T033 y T036), según Art. II.
- `003-iniciar-sesion` y contrato login entregado/versionado; `002-registro` para
  acordar su navegación sin sesión.
- Consolidación de `005` para la ruta de destino; aprobación de spec, plan y tareas
  en etapas separadas antes de escribir código.

**Preguntas abiertas — bloquean aprobación/implementación**

| # | Pregunta | Responsable |
|---|---|---|
| P-01 | ¿Se acepta restauración solo mientras el access token siga vigente? Mantener sesión durante días requiere otro spec de renovación. | Producto |
| P-02 | ¿Se limita inicialmente a `client` y qué destino seguro corresponde a `owner`/`provider` sin pantallas implementadas? | Producto y arquitectura |
| P-03 | ¿Cómo se ajusta el inicio tras registro exigido por `002` sin simular autenticación? No cambiar esa navegación sin aprobar la coordinación. | Producto y arquitectura |
| P-04 | Confirmar unidad y semántica de `expiresIn`, rol y respuesta de login mediante OpenAPI/JSON probado versionado; reconciliar estado documental del backend con humo histórico móvil. | Backend |
| P-05 | Aprobar política conservadora ante reloj atrasado y Keystore inaccesible, distinguiendo corrupción de fallo transitorio. | Seguridad y arquitectura |
| P-06 | Confirmar cierre de `000` y aclarar título/dependencias de `003`, que mencionan logout/refresh no implementados. | Equipo |
| P-07 | Si falla toda escritura de invalidación y muere el proceso, el registro antiguo puede seguir restaurable offline. ¿Se exige validación remota al arrancar (cambiaría RF-07 y el alcance) o se acepta explícitamente esa limitación del almacenamiento? Sin decisión no aprobar ni implementar. | Seguridad y producto |

Contratos revisados como referencia documental (no sonda de disponibilidad):
`virgorent/VirGO_Backend`, `VirGo-API/specs/003-auth-login/plan.md`,
`005-auth-register/plan.md`, `006-users-me/plan.md`,
`013-post-auth-refresh/plan.md` y `004-auth-logout/plan.md`.
El plan exige fijar el commit del handoff antes de implementar.
