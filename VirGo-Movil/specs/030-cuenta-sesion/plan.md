# Plan — 030 Cuenta y sesión

| Campo | Valor |
|---|---|
| Módulo | `030-cuenta-sesion` |
| Versión | 0.1.0 |
| Estado | Borrador |
| Fecha | 2026-11-24 |
| Constitución | v1.2.0 |
| Rol | Cliente, Propietario y Proveedor |
| Fuente | Auditoría móvil–backend y specs `003-auth-login`, `004-auth-logout`, `006-users-me` |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

- Se implementa en `:app` y `:core:*`; no se crea `:feature:*` todavía.
- `:core:network` añade `UsersApi`, el DTO `UserProfileDto` y `logout()` en
  `AuthApi`. El logout usa `Response<Unit>` para aceptar `200` y `204`.
- `:core:domain` define `UserProfile`, `AccountRepository`, `GetProfileUseCase`
  y `LogoutUseCase`. No conoce Retrofit ni DataStore.
- `:core:data` implementa `AccountRepositoryImpl`, reutiliza el mapeo de errores
  existente y traduce `401` a `AppError.Unauthorized`.
- `:app` extiende la sesión con un motivo de cierre, un estado de logout y una
  invalidación idempotente. La pantalla de perfil autenticado pasa a ser real;
  el perfil de muestra sin cuenta se conserva solo en la vista previa de debug.
- La hoja H06 deja de ofrecerse en el perfil autenticado mientras no exista un
  endpoint de actualización. En la muestra sin cuenta, H06 sigue marcada como
  ficticia.

## 2. Contexto técnico

| Aspecto | Decisión |
|---|---|
| Lenguaje / framework | Kotlin, Jetpack Compose (Material 3), MVVM/UDF, Hilt |
| Persistencia | Solo el token cifrado existente en `:core:datastore`; `phone` e `isVerified` no se persisten |
| Seguridad | Bearer por `AuthInterceptor`; sin refresh; cierre local aun si falla la revocación; sin datos personales en logs |
| Red | Retrofit + OkHttp; `GET /api/v1/users/me` y `POST /api/v1/auth/logout` |
| Pruebas | JUnit/MockK/Turbine, MockWebServer, Compose Testing, Robolectric, Pitest |
| CI/CD | Pipeline existente; sin workflows nuevos; sin release en este módulo |

## 3. Constitution Check

| Art. | Tema | Resultado | Cómo se cumple |
|---|---|---|---|
| I | SDD | ⚠️ Pendiente | Spec atómico en borrador; falta aprobación humana antes de implementar. |
| II | Modularidad | ⚠️ Pendiente | `000` sigue 38/40 (T014 y T036); las tareas quedan bloqueadas hasta su cierre o una excepción aprobada. |
| III | Arquitectura | ✅ Cumple | UI → dominio → datos; `:app` no usa Retrofit; sin `GlobalScope`. |
| IV | Contrato | ⚠️ Parcial | `/api/v1`, Bearer y Problem Details; el OpenAPI de `/users/me` no declara esquema y el de logout dice `200` mientras el código responde `204`. |
| V | Seguridad | ✅ Cumple | Token cifrado y borrado siempre; sin secretos nuevos; sin logs con cuerpos, tokens ni datos personales. |
| VI | Calidad | ⚠️ Pendiente | Los cinco tipos de prueba están planificados; la evidencia se completa al implementar. |
| VII | Diseño | ✅ Cumple | Componentes y tokens existentes, textos en `strings.xml`, claro/oscuro y 48 dp. |
| VIII | CI/CD | ⚠️ Pendiente | Se usará el pipeline existente; el PR se abrirá después de aprobar spec y plan. |
| IX | Observabilidad | ✅ Sin cambios | Se reutilizan correlación y logging restringido de la base; no se añaden datos personales. |
| X | Memoria | ✅ Planificado | `check.md` y entrada en `memoria.md` al cerrar. |
| XI | Gobierno | ✅ Cumple | Sin secretos, despliegues ni releases; solo se prepara documentación. |
| XII | Idioma | ✅ Cumple | Documentación y textos en español; identificadores en inglés. |

Un ❌ bloquearía el plan; los ⚠️ se resuelven antes de marcar el módulo como
cerrado. La aprobación del spec no desbloquea por sí sola las tareas de `000`.

## 4. Estructura

```text
VirGo-Movil/
├── core/
│   ├── network/
│   │   └── src/main/kotlin/mx/virgo/core/network/
│   │       ├── api/AuthApi.kt                 # + logout()
│   │       ├── api/UsersApi.kt                # nuevo
│   │       ├── dto/AuthDtos.kt                # + UserProfileDto
│   │       ├── NetworkClientFactory.kt        # + createUsersApi
│   │       └── di/NetworkModule.kt            # + provideUsersApi
│   ├── domain/
│   │   └── src/main/kotlin/mx/virgo/core/domain/
│   │       ├── UserProfile.kt                 # nuevo
│   │       ├── AccountRepository.kt           # nuevo
│   │       ├── GetProfileUseCase.kt           # nuevo
│   │       └── LogoutUseCase.kt               # nuevo
│   └── data/
│       └── src/main/kotlin/mx/virgo/core/data/
│           ├── repository/AccountRepositoryImpl.kt  # nuevo
│           └── di/AccountModule.kt                  # nuevo
├── app/src/main/kotlin/mx/virgo/app/
│   ├── session/
│   │   ├── SessionUiState.kt                  # + motivo y estado de logout
│   │   ├── SessionRepository.kt               # + invalidate()
│   │   └── SessionViewModel.kt                # + orquestación del logout
│   ├── ui/account/
│   │   ├── AccountProfileScreen.kt            # nuevo
│   │   └── AccountProfileViewModel.kt         # nuevo
│   ├── demo/
│   │   ├── RoleWorkspace.kt                   # perfil real si hay cuenta
│   │   └── DemoSheetsRegistry.kt              # H06 fuera del perfil autenticado
│   └── VirGoApp.kt                            # aviso único de fin de sesión
└── app/src/main/res/values/
    ├── strings.xml                            # textos de sesión y perfil
    └── unified_strings.xml                    # aviso de logout no confirmado
```

## 5. Decisiones técnicas (ADR)

| ADR | Decisión | Alternativas descartadas | Motivo |
|---|---|---|---|
| ADR-01 | `AccountRepository` separado de `AuthRepository` | Añadir perfil y logout a `AuthRepository` | Mantiene autenticación y cuenta como responsabilidades distintas. |
| ADR-02 | `logout(): Response<Unit>` en Retrofit | `Unit` estricto con solo `204` | Tolera la diferencia `200`/`204` mientras se corrige el contrato. |
| ADR-03 | Cerrar la sesión local siempre, aunque falle la revocación | Mantener el token para reintentar | No dejar un token utilizable en el dispositivo tras una orden de logout. |
| ADR-04 | Invalidar la sesión ante `401`, `404` de `/users/me` o rol inconsistente | Mostrar el error y seguir dentro | Evita autorizar una identidad que el servidor ya no reconoce. |
| ADR-05 | Sin persistir `phone` ni `isVerified` | Guardarlos en DataStore | Amplía el dato sensible sin necesidad; se leen al abrir el perfil. |
| ADR-06 | Perfil autenticado de solo lectura y sin H06 | Mantener edición local | No simular una actualización que el backend no ofrece. |
| ADR-07 | Aviso de fin de sesión una sola vez | Diálogo permanente en cada recomposición | Evita repetir el mensaje y no bloquea el inicio de visitante. |
| ADR-08 | Implementar en `:app` y `:core:*` | Crear `:feature:cuenta` ahora | Sigue la consolidación actual y evita una migración no pedida. |

## 6. Diseño

### 6.1 Red

- `UsersApi`:
  - `@GET("api/v1/users/me") suspend fun me(): UserProfileDto`
- `AuthApi`:
  - `@POST("api/v1/auth/logout") suspend fun logout(): Response<Unit>`
- `UserProfileDto`: `id`, `name`, `email`, `phone` (nullable), `role`,
  `isVerified`. `ignoreUnknownKeys = true` ya está configurado.
- `NetworkModule` provee `UsersApi` con el mismo `OkHttpClient`, que ya añade
  `Authorization` y correlación.

### 6.2 Dominio y datos

- `UserProfile(id, name, email, phone, role, isVerified)`.
- `AccountRepository`:
  - `suspend fun getProfile(): AppResult<UserProfile>`
  - `suspend fun logout(): AppResult<Unit>`
- `AccountRepositoryImpl` reutiliza `Throwable.toAppError()` de
  `AuthRepositoryImpl` y trata `401` como `AppError.Unauthorized`, `404` como
  `AppError.NotFound` y el resto con el mapeo existente.
- No se añade `DELETE /users/me` ni edición de perfil.

### 6.3 Sesión

- `SessionEndReason`: `EXPIRED`, `REVOKED`, `ROLE_MISMATCH`,
  `ACCOUNT_UNAVAILABLE`, `LOGOUT_UNCONFIRMED`.
- `SessionUiState` añade `loggingOut: Boolean` y
  `endReason: SessionEndReason?`.
- `SessionRepository.invalidate(reason)` es idempotente: bloquea la sesión,
  borra el token y publica el motivo. `logout()` local se conserva para el
  cierre sin motivo. `establish()` limpia `endReason`, `loggingOut` y `error` al
  iniciar una sesión nueva.
- `SessionViewModel.logout()` ignora una segunda llamada mientras `loggingOut`
  esté activo:
  1. Marca `loggingOut`.
  2. Llama a `LogoutUseCase` con un límite de 10 segundos.
  3. Si la respuesta es `204`/`200`, cierra localmente sin motivo.
  4. Si es `401`, cierra localmente sin error.
  5. Si falla por red, timeout o 5xx, cierra localmente con
     `LOGOUT_UNCONFIRMED`.
- El temporizador de expiración existente llama a `invalidate(EXPIRED)`.

### 6.4 Perfil

- `AccountProfileViewModel` recibe `GetProfileUseCase` y `SessionRepository`.
- Estado: `loading`, `profile`, `fallback` (identidad de la sesión), `error`;
  acción `retry`.
- Al recibir `AppError.Unauthorized` → `invalidate(REVOKED)`.
- Al recibir `AppError.NotFound` → `invalidate(ACCOUNT_UNAVAILABLE)`.
- Si el rol del servidor no coincide con `ActiveSession.role` o no se reconoce →
  `invalidate(ROLE_MISMATCH)` antes de publicar el perfil.
- Sin red o 5xx → `fallback` con nombre, correo y rol de la sesión; `phone` e
  `isVerified` como no disponibles; botón Reintentar.
- La pantalla no tiene campos editables; muestra los enlaces de muestra
  existentes (mensajes, notificaciones, ayuda) con el aviso global.
- `RoleWorkspace` usa `AccountProfileScreen` cuando hay cuenta. La vista previa
  sin cuenta conserva `DemoProfile` y H06.

### 6.5 Navegación y aviso

- `VirGoNavHost` conserva su navegación actual: al quedar la sesión en `null`,
  lleva al inicio de visitante.
- `VirGoApp` observa `SessionUiState.endReason` y muestra el mensaje una sola vez
  en el `SnackbarHost` existente; después llama a `consumeEndReason()`.
- `SessionUiState.error` se conserva para fallos de almacenamiento o restauración
  y no se duplica con `endReason`.
- Mensajes según el motivo:
  - `EXPIRED`/`REVOKED`: «Tu sesión expiró. Inicia sesión de nuevo.».
  - `ROLE_MISMATCH`: «No pudimos validar el rol de tu cuenta. Inicia sesión de nuevo.».
  - `ACCOUNT_UNAVAILABLE`: «Tu cuenta ya no está disponible.».
  - `LOGOUT_UNCONFIRMED`: «Cerramos tu sesión en este dispositivo, pero no pudimos confirmar la revocación en el servidor.».
- El motivo se consume después de mostrarse para que no reaparezca al rotar.

### 6.6 Seguridad

- El token solo viaja en el encabezado `Authorization`; no se registra ni se
  pasa como parámetro de navegación.
- No se añaden logs de cuerpo, correo, teléfono ni identificadores de cuenta.
- El logout limpia el token aunque la llamada falle; no se guarda para reintentar.
- No se usa el refresh token ni se amplía la vigencia de 15 minutos.
- Las pruebas de seguridad verifican el borrado del token y la ausencia de datos
  personales en logs.

## 7. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | `AccountProfileViewModel` (carga, éxito, sin red, 401, 404, rol distinto), `SessionViewModel` (logout 204, 401, error, timeout) y casos de uso |
| Integración | MockWebServer: `GET /users/me` 200/401/404/500/timeout; `POST /auth/logout` 204/200/401/500; DTO con campos extra y `phone` nulo |
| UI | Perfil con carga, éxito, sin red y solo lectura; aviso de fin de sesión una sola vez; tema claro/oscuro; fuente 2.0 |
| Seguridad | Token solo en `Authorization`; sin datos personales en logs; borrado local aun con fallo de red; sin HTTP en release |
| Mutación | Pitest en `GetProfileUseCase`, `LogoutUseCase` y la validación de rol |
| Humo | Cuenta desechable contra producción: login → perfil → logout; el token anterior responde 401 |

Comandos previstos, sin ejecutarlos en esta fase de documentación:

```powershell
.\gradlew.bat ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug
```

La prueba de humo usa una cuenta desechable. Las credenciales y el token no se
guardan en el repositorio, en capturas ni en la evidencia.

## 8. Riesgos

| Riesgo | Mitigación |
|---|---|
| `000` no está cerrado | Mantener las tareas bloqueadas hasta el cierre de T014/T036 o una excepción aprobada. |
| OpenAPI de logout dice `200` y el código responde `204` | Aceptar ambos; pedir al backend alinear el OpenAPI; no depender de un único código. |
| El token dura 15 minutos y no hay refresh | Mensaje claro al expirar; no prometer sesión persistente. |
| Logout sin red deja el token vigente en el servidor | Cerrar localmente y avisar que la revocación no se confirmó; no conservar el token. |
| `/users/me` cambia o elimina campos | DTO tolerante a campos extra y `phone` nulo; contrato documentado en spec y memoria. |
| El perfil real se confunde con las demás pantallas de muestra | Identificarlo como «Datos de tu cuenta» y conservar el aviso global. |
| Se filtra un token o un dato personal en logs o evidencia | Prohibir cuerpos y encabezados en logs; revisar capturas y pruebas de seguridad. |
| El release usa una URL vacía | Fuera de alcance; se verifica antes de cualquier release, no en este módulo. |

## 9. Entrega y fases

1. **Aprobación:** revisar y aprobar `spec.md` y `plan.md`; no se escribe código
   antes de la aprobación (Art. I).
2. **Desbloqueo:** cerrar `000` o aprobar una excepción explícita (Art. II.2).
3. **Contrato:** implementar red, DTO y pruebas con MockWebServer (T3001).
4. **Dominio y datos:** modelos, repositorio y casos de uso (T3002–T3003).
5. **Sesión:** invalidación, logout y expiración (T3004).
6. **Perfil y navegación:** pantalla real, H06 y aviso de fin de sesión
   (T3005–T3006).
7. **Calidad:** pruebas, análisis estático, mutación y evidencia (T3007).
8. **Humo y cierre:** cuenta desechable, `check.md` y `memoria.md` (T3008).

El PR se abre solo después de la aprobación y con el pipeline en verde. La fusión
requiere la revisión del repositorio; este plan no autoriza releases.
