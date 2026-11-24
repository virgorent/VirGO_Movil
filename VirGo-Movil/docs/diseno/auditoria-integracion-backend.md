# Auditoría de integración móvil–backend

**Fecha:** 2026-11-24
**Rama auditada:** `feat/033-app-unificada` (`4ab3131`)
**Alcance:** inventario del contrato desplegado, clasificación de lo real y lo
simulado en la app, y priorización de un primer recorrido de punta a punta.

Esta auditoría **no** aprueba specs, no cierra tareas de `000`, no certifica
paridad visual y no autoriza pagos, reservas, IA ni mensajes reales. Describe lo
que existe hoy y lo que falta.

## 1. Resumen ejecutivo

- La app tiene **55 pantallas** y **15 hojas**; solo una parte mínima consume
  servicios reales.
- El backend desplegado expone **11 rutas** (12 operaciones) en OpenAPI. De
  ellas, **8 rutas de negocio**; el resto son salud y diagnóstico.
- El móvil tiene **5 interfaces de API**: autenticación, catálogo, favoritos,
  avisos y salud. Están cableadas login, registro, catálogo, favoritos y avisos;
  estas dos últimas llaman a rutas que hoy responden **404**.
- El catálogo real responde **200 con 0 salones publicados**. La app cae
  entonces en **7 salones de muestra**; el respaldo está identificado, pero no
  distingue visualmente "sin catálogo" de "catálogo real".
- **Bloqueadas hoy:** recuperación de contraseña (404), favoritos (404), avisos
  (404), detalle de salón (sin endpoint), perfil real (sin consumo), revocación
  de sesión en servidor (no se llama).
- **Primer recorrido real recomendado:** cuenta y sesión
  (`login → GET /users/me → POST /auth/logout`), porque sus tres endpoints ya
  están desplegados y no dependen de datos ni de otros módulos.
- **Segundo recorrido:** catálogo real, en cuanto se publiquen salones y exista
  `GET /venues/{id}`.
- Cotizaciones y reservas **no** son un recorrido real todavía: los endpoints
  `POST /quotes` y `POST /bookings` existen, pero no hay eventos, salones
  publicados, cotizaciones ni capa móvil que los consuma.

## 2. Contrato desplegado

Fuente: `GET /openapi/v1.json` de producción, consultado el 2026-11-24, más
sondas sin credenciales. También se enviaron peticiones POST sin token y sin
datos válidos para comprobar rechazos; no se crearon recursos.

| Ruta | Método | Auth esperada | Estado observado | Consumo móvil |
|---|---|---|---|---|
| `/health` | GET | Pública | 200 | Solo diagnóstico debug |
| `/api/v1/auth/register` | POST | Pública | Desplegado; 201/409 según spec | `RegisterScreen` real |
| `/api/v1/auth/login` | POST | Pública | Desplegado; 200/401/429 | `LoginScreen` real |
| `/api/v1/auth/logout` | POST | Bearer | Sin token: 401 | **No consumido**; logout local |
| `/api/v1/users/me` | GET | Bearer | Sin token: 401 | **No consumido** |
| `/api/v1/users/me` | DELETE | Bearer | Sin token: 401 | **No consumido** |
| `/api/v1/users/{id}/public` | GET | Pública | UUID inexistente: 404 | **No consumido** |
| `/api/v1/venues` | GET | Pública | 200 con `data: []` | `ExploreScreen` y visitante; respaldo a muestras |
| `/api/v1/quotes` | POST | Bearer | Sin token: 401; GET: 405 | **Sin API móvil** |
| `/api/v1/bookings` | POST | Bearer | Sin token: 401; GET: 405 | **Sin API móvil** |
| `/api/v1/_diagnostics/secure` | GET | Bearer | Sin token: 401 | No consumido |
| `/api/v1/_diagnostics/failure` | GET | — | 404 | No consumido |

Advertencias del contrato:

1. El OpenAPI desplegado **no declara esquemas de petición/respuesta ni
   seguridad**. Sirve como inventario de rutas, no como contrato verificable.
2. `POST /api/v1/bookings` requiere `Idempotency-Key`; no está documentado en el
   OpenAPI publicado.
3. El OpenAPI declara `200` para logout; el handoff del backend dice `204`.
   Confirmar el código antes de implementar el consumo.
4. Los endpoints ausentes más relevantes: recuperación de contraseña, favoritos,
   avisos, detalle de salón, proveedores, eventos, cotizaciones de lectura,
   reservas de lectura, pagos y conversaciones.
5. `VirGo-API/specs/` contiene specs por endpoint, pero **estar redactados no
   significa desplegados**. La autoridad operativa es el OpenAPI y la respuesta
   real.

## 3. Qué es real hoy en la app

| Función | Implementación | Resultado |
|---|---|---|
| Registro | `POST /auth/register` | Real; redirige a login |
| Inicio de sesión | `POST /auth/login` | Real; rol desde la sesión validada |
| Sesión local | Cifrada, con expiración máxima de 15 min | Real; sin refresh almacenado |
| Restauración tras cierre | Token cifrado | Real hasta que expire |
| Cierre de sesión | Limpieza local | Real local; **no revoca en servidor** |
| Catálogo | `GET /venues` | Real, pero 0 filas; respaldo a 7 muestras |
| Detalle de salón | Ninguno | Simulado con datos fijos |
| Favoritos | `GET/POST/DELETE /favorites` | Bloqueado: 404 |
| Avisos | `GET /notices` | Bloqueado: 404 |
| Recuperación de contraseña | `POST /auth/password/forgot` y `/reset` | Bloqueado: 404 |
| Perfil | Ninguno | Simulado; no lee `GET /users/me` |
| Todo lo demás | Estado local en memoria | Simulado y señalizado por el aviso de la workspace |

## 4. Matriz pantalla → contrato

Leyenda de la columna **Móvil**:

- **Real:** consume una ruta desplegada.
- **Real con respaldo:** consume una ruta desplegada, pero cae en muestras.
- **Simulada:** datos y cambios locales, sin llamada de negocio.
- **Bloqueada:** la llamada existe en el móvil, pero la ruta responde 404.

### 4.1 Rol cliente (29 pantallas)

| Pantalla | Endpoint / spec | Backend | Móvil | Brecha principal |
|---|---|---|---|---|
| Explorar | `GET /venues` (007), favoritos 081-083, avisos 090-092 | Catálogo desplegado y vacío; favoritos/avisos no desplegados | Real con respaldo | Sin salones publicados; favoritos y avisos 404 |
| Favoritos | 081-083 | Spec, no desplegado | Simulada | Ruta 404; pantalla local |
| Detalle del salón | 024, reseñas 072 | Spec, no desplegado | Simulada | No hay endpoint ni id desde el catálogo |
| IA Studio | Sin endpoint de diseño | — | Simulada | Funciones de IA no reales |
| Mi Evento | Eventos 043/045/046, tareas 052-055 | Spec, no desplegado | Simulada | Sin evento creado |
| Proveedores | 034 | Spec, no desplegado | Simulada | Sin catálogo de proveedores |
| Detalle del proveedor | 035 | Spec, no desplegado | Simulada | Sin id real |
| Comparar proveedores | 034/035 | Spec, no desplegado | Simulada | Comparación local |
| Solicitar cotización | `POST /quotes` (008) | Desplegado; sin esquema OpenAPI | Simulada | Sin API móvil, sin salón/evento |
| Mis cotizaciones | 060 | Spec, no desplegado | Simulada | Sin listado |
| Detalle de cotización | 061-064 | Spec, no desplegado | Simulada | Sin cotización real |
| Reservar | `POST /bookings` (009) | Desplegado; requiere idempotencia | Simulada | Sin API móvil ni prerrequisitos |
| Pago de ejemplo | 070 | Spec, no desplegado | Simulada | No se cobra nada |
| Comprobante de muestra | 071 | Spec, no desplegado | Simulada | Sin pago real |
| Mis reservaciones | 066 | Spec, no desplegado | Simulada | Sin listado |
| Administrar reservación | 069 | Spec, no desplegado | Simulada | Sin reserva real |
| Presupuesto | 008/060 | Parcial | Simulada | Cálculo local |
| Presupuesto con IA | 094, Bedrock 906 | IA apagada | Simulada | Sin llamada a IA |
| Pagos | 070/071, tarjetas 019-023 | No desplegado | Simulada | Sin cobros ni tarjetas |
| Invitados | 048-051 | Spec, no desplegado | Simulada | Sin evento |
| Pendientes | 052-055 | Spec, no desplegado | Simulada | Sin evento |
| Cronograma | 045/046, 052-055 | Spec, no desplegado | Simulada | Sin evento |
| Recordatorios | 090-092/053 | No desplegado | Simulada | Sin avisos |
| Compartir evento | 056-059 | Spec, no desplegado | Simulada | Sin colaboradores |
| Invitación | 049/056 | Spec, no desplegado | Simulada | Sin envío real |
| Recomendados | 007 | Catálogo vacío | Simulada | Ranking local sobre muestras |
| Opiniones del salón | 072 | Spec, no desplegado | Simulada | Sin reseñas |
| Escribir reseña | 074 | Spec, no desplegado | Simulada | Sin envío real |
| Asistente IA | 094 | Spec; Bedrock apagado | Simulada | Sin IA real |

### 4.2 Rol propietario (12 pantallas)

| Pantalla | Endpoint / spec | Backend | Móvil | Brecha principal |
|---|---|---|---|---|
| Inicio | 080, 025, 060 | No desplegado | Simulada | Sin métricas ni salones propios |
| Mis salones | 025/024 | No desplegado | Simulada | Sin listado propio |
| Editar salón | 026-029, medios 904 | Specs; sin endpoint de carga | Simulada | Sin edición ni multimedia real |
| Calendario | 031-033, 066 | No desplegado | Simulada | Sin disponibilidad |
| Precios | 027 | No desplegado | Simulada | Sin actualización real |
| Montajes | Sin endpoint específico | — | Simulada | No modelado |
| Solicitudes | 060/061 | No desplegado | Simulada | Sin cotizaciones |
| Detalle de solicitud | 062-064 | No desplegado | Simulada | Sin respuesta real |
| Pagos | 070/071, 019-023 | No desplegado | Simulada | Sin cobros |
| Estadísticas | 078 | No desplegado | Simulada | Sin métricas |
| Reseñas | 072/075 | No desplegado | Simulada | Sin reseñas |
| Aliados | 034 (parcial) | No desplegado | Simulada | Sin modelo de alianza |

### 4.3 Rol proveedor (9 pantallas)

| Pantalla | Endpoint / spec | Backend | Móvil | Brecha principal |
|---|---|---|---|---|
| Inicio | 036/080 | No desplegado | Simulada | Sin perfil ni métricas |
| Mi servicio | 036-038 | No desplegado | Simulada | Sin perfil real |
| Galería | 904 + 038 | Medios planificados; sin carga | Simulada | Sin subida real |
| Zonas de cobertura | 038 | No desplegado | Simulada | Sin actualización |
| Solicitudes | 060/061 | No desplegado | Simulada | Sin cotizaciones |
| Detalle de solicitud | 062 | No desplegado | Simulada | Sin respuesta real |
| Agenda | 039-042, 066 | No desplegado | Simulada | Sin trabajos ni reservas |
| Reseñas | 073/075 | No desplegado | Simulada | Sin reseñas |
| Estadísticas | 079 | No desplegado | Simulada | Sin métricas |

### 4.4 Pantallas compartidas (5)

| Pantalla | Endpoint / spec | Backend | Móvil | Brecha principal |
|---|---|---|---|---|
| Perfil | 006, 015, 016, 018 | `GET/DELETE /users/me` desplegados; el resto no | Simulada | No se lee el perfil real |
| Mensajes | 084-089 | No desplegado | Simulada | Sin conversaciones |
| Conversación | 087/088 | No desplegado | Simulada | Sin mensajes |
| Notificaciones | 090-092 | No desplegado | Simulada | Sin avisos |
| Ayuda | Sin endpoint | — | Simulada | Contenido estático |

### 4.5 Hojas (15)

| Hoja | Sección | Endpoint / spec | Estado |
|---|---|---|---|
| H01 | Búsqueda y filtros | 007 (filtros locales) | Local; la API admite `zone`/`category`, no cableados |
| H02 | Fechas | 031-033 | No desplegado |
| H03 | Presupuesto | Sin endpoint directo | Local |
| H04 | Evento | 044/046 | No desplegado |
| H05 | Equipo | 057 | No desplegado |
| H06 | Perfil | 015 | No desplegado |
| H07 | Preferencias | 015 | No desplegado |
| H08 | Métodos de pago | 019-023 | No desplegado |
| H09 | Pago | 070 | No desplegado |
| H10 | Invitado | 049 | No desplegado |
| H11 | Pendiente | 053 | No desplegado |
| H12 | Recordatorio | 092/053 | No desplegado |
| H13 | Colaborador | 057 | No desplegado |
| H14 | Bloqueo de agenda | 032 | No desplegado |
| H15 | Agenda del proveedor | 040 | No desplegado |

## 5. Hallazgos

| ID | Hallazgo | Impacto | Prioridad |
|---|---|---|---|
| F-01 | El catálogo de producción devuelve 0 salones y la app muestra 7 muestras | Se confunde "sin catálogo" con "catálogo real" | Alta |
| F-02 | El aviso global y los créditos identifican la muestra, pero no hay distinción por fila; con un catálogo parcial no se sabrá qué filas son reales | Trazabilidad de datos ficticios | Alta |
| F-03 | `POST /auth/logout` existe y no se llama; el cierre local no revoca la sesión | Una sesión robada sigue vigente hasta expirar | Alta |
| F-04 | No se consume `GET /users/me`; el perfil es local | El perfil puede divergir del servidor | Media |
| F-05 | El token dura 15 min y no hay refresh; un 401 no fuerza cierre de sesión | Errores confusos tras expirar | Media |
| F-06 | Favoritos y avisos llaman rutas 404 en cada carga del explorar | Ruido, funciones aparentes que no guardan | Media |
| F-07 | No existe `GET /venues/{id}`; el detalle no recibe el id seleccionado | El catálogo real no se puede abrir | Alta |
| F-08 | `POST /quotes` y `POST /bookings` están desplegados, pero sin esquema OpenAPI, sin API móvil y sin prerrequisitos | No hay transacción real posible | Alta |
| F-09 | El OpenAPI desplegado no documenta esquemas ni seguridad | No sirve como contrato para el móvil | Media |
| F-10 | El release exige `VIRGO_API_BASE_URL`; si falta, la URL queda vacía | APK de release inoperante | Media |
| F-11 | `versionCode = 2` y `versionName = 0.2.0` ya se publicaron | Una prueba nueva necesita incremento | Media |
| F-12 | Los diagnósticos aparecen en el OpenAPI público | Superficie innecesaria en producción | Baja |

## 6. Priorización

### P0 · Se puede hacer sin backend nuevo

1. **Sesión real completa:** llamar `POST /auth/logout` al cerrar sesión y
   manejar el 401 cerrando la sesión local.
2. **Perfil real mínimo:** consumir `GET /users/me` tras confirmar campos con el
   backend; mostrar nombre, correo y rol del servidor.
3. **Estado de catálogo:** si `GET /venues` devuelve vacío, mostrar un estado
   "sin salones publicados" y mantener las muestras solo con etiqueta explícita.
4. **Prueba de contrato:** comparar rutas/estados del OpenAPI con las interfaces
   del móvil en CI, sin credenciales.

### P1 · Depende de backend u operación

1. Publicar salones en producción y verificar `GET /venues` con filas reales.
2. Desplegar `GET /venues/{id}` (024) para abrir el detalle desde el catálogo.
3. Desplegar favoritos 081-083; el móvil ya tiene la capa de datos.
4. Desplegar avisos 090-092.
5. Publicar el OpenAPI con esquemas y seguridad.

### P2 · Primer flujo transaccional

1. Eventos 043-046 (crear y consultar).
2. Cotizaciones 008, 060 y 061-064.
3. Reservas 009, 066-069 y pagos 070-071.

No tiene sentido cablear `POST /quotes` o `POST /bookings` antes de P1: hoy no
hay salón publicado ni evento al que apuntar.

## 7. Primer recorrido real recomendado

**Flujo A · Cuenta y sesión**

Spec propuesto: [`030-cuenta-sesion`](../../specs/030-cuenta-sesion/spec.md)
(borrador; no implementado).

1. Registro o login contra producción.
2. `GET /users/me` con el token de la sesión. Campos del contrato:
   `id`, `name`, `email`, `phone`, `role`, `isVerified`.
3. Mostrar el perfil del servidor en la pantalla de perfil.
4. `POST /auth/logout` al cerrar sesión. El handoff espera `204`; el OpenAPI
   publicado declara `200`. Confirmar el código real antes de implementar.
5. Limpiar el almacenamiento cifrado y volver al inicio de visitante.

Por qué este primero:

- Los tres endpoints ya están desplegados.
- No depende de catálogo, eventos, pagos ni IA.
- Valida token, errores 401, revocación y limpieza.
- Es pequeño: una interfaz de API, un DTO con seis campos, un caso de uso y la
  pantalla.

Criterios de aceptación:

- Con token válido, el perfil muestra los datos del servidor.
- Con token expirado o revocado, la app cierra la sesión y explica el motivo.
- Tras logout, el token anterior responde 401.
- Sin red, se muestra un error y no se inventa un perfil.

Piezas móviles previstas, sin implementar todavía:

| Capa | Archivo o cambio |
|---|---|
| Red | `UsersApi` con `GET /users/me`; `AuthApi.logout()` |
| DTO | `UserProfileDto` con los seis campos del contrato |
| Dominio | `GetProfileUseCase`, `LogoutUseCase` |
| Datos | Repositorio que traduzca 401 a cierre de sesión |
| UI | Perfil autenticado con estados de carga, error y sin red |
| Pruebas | MockWebServer 200/401 y prueba de que logout limpia el token |

**Flujo B · Catálogo real**

1. Publicar salones.
2. `GET /venues` con filas reales.
3. Abrir detalle con `GET /venues/{id}`.
4. Distinguir muestra de dato real en la UI.

## 8. Release de prueba

Estado comprobado, sin publicar nada:

- Los cuatro secretos de firma existen en el repositorio.
- `VIRGO_API_BASE_URL` existe como variable y apunta a la API de producción.
- El workflow de release es manual, firma APK/AAB y crea un GitHub Release; no
  sube a Google Play.
- `versionCode = 2` y `versionName = 0.2.0` ya se usaron; una prueba nueva debe
  incrementarlos.
- El catálogo de producción está vacío; una prueba mostraría muestras.

Antes de publicar una prueba:

1. Fusionar el PR #32 con la aprobación requerida.
2. Incrementar versión.
3. Verificar el APK firmado y el recorrido de sesión.
4. Etiquetar en la app y en la release las funciones simuladas.
5. No anunciar pagos, reservas, IA ni mensajes como reales.

## 9. Fuentes y límites

- `GET /openapi/v1.json` de `https://rcof55a3c9.execute-api.us-east-1.amazonaws.com`.
- Pruebas HTTP de solo lectura sin credenciales: salud, catálogo, favoritos,
  avisos, recuperación, usuarios, cotizaciones, reservas y diagnósticos.
- Código móvil: `core/network`, `core/data`, `ui/explore`, `ui/guest`,
  `ui/login`, `ui/register`, `ui/recover`, `demo/`.
- Contrato de referencia: `VirGo-API/API-HANDOFF.md` y specs del backend.
- No se usaron credenciales, no se crearon recursos y no se modificaron datos.
- Las pruebas 401/404/405 confirman existencia o ausencia de ruta, no la
  corrección funcional completa.
- La auditoría no sustituye la aceptación visual del usuario ni el cierre de
  T014/T036.
