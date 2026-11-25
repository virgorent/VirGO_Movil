# Tareas — 030 Cuenta y sesión

| Campo | Valor |
|---|---|
| Módulo | `030-cuenta-sesion` |
| Versión | 0.1.0 |
| Estado | Aprobado por el usuario (lote MVP, 2026-11-25). **Tareas ⛔ por el Art. II.2** hasta cerrar `000` y recibir la URL y el JSON de backend (o excepción aprobada). |
| Fecha | 2026-11-24 |
| Constitución | v1.2.0 |
| Rol | Cliente, Propietario y Proveedor |
| Fuente | Auditoría móvil–backend y specs `003-auth-login`, `004-auth-logout`, `006-users-me` |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

> Todas las tareas están ⛔ por el Art. II.2: `000-arquitectura-base` sigue
> 38/40 (T014 y T036). Se desbloquean cuando `000` cierre o cuando exista una
> excepción aprobada y registrada. La aprobación de este spec no las habilita.

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3001 | Red: `UsersApi`, `UserProfileDto`, `logout()` en `AuthApi`, fábrica y DI | Specs de API `004` y `006`; `000` cerrado o excepción | `dev-android` | MockWebServer: 200, 204, 401, 404, 500 y timeout | ⛔ |
| T3002 | Dominio: `UserProfile`, `AccountRepository`, `GetProfileUseCase`, `LogoutUseCase` | T3001 | `dev-android` | Unitarias y Pitest en verde | ⛔ |
| T3003 | Datos: `AccountRepositoryImpl`, mapeo de errores y módulo Hilt | T3001, T3002 | `dev-android` | MockWebServer con el contrato real; sin datos en logs | ⛔ |
| T3004 | Sesión: motivo de cierre, `invalidate()`, logout orquestado y expiración | T3002 | `dev-android` | Unitarias de `SessionViewModel` y `SessionRepository` | ⛔ |
| T3005 | UI: perfil autenticado real, solo lectura, H06 fuera y estados de carga/error | T3003, T3004 | `dev-android` + `ui-designsystem` | Compose Testing; claro/oscuro; 48 dp; textos en `strings.xml` | ⛔ |
| T3006 | Navegación: aviso único de sesión vencida, revocada, rol inconsistente o logout no confirmado | T3004, T3005 | `dev-android` | Prueba de que el aviso aparece una vez y no al rotar | ⛔ |
| T3007 | Calidad: unitarias, integración, UI, seguridad, mutación y análisis estático | T3003–T3006 | `qa-movil` | Pipeline del plan en verde | ⛔ |
| T3008 | Humo y cierre: cuenta desechable, perfil real, logout y token anterior 401; evidencia y memoria | T3007 | `qa-movil` | `check.md` completo y entrada en `memoria.md` | ⛔ |

## Trazabilidad

| RF | CA | Tarea |
|---|---|---|
| RF-01, RF-02 | CA-01 | T3001, T3003, T3005 |
| RF-03 | CA-02 | T3005 |
| RF-04 | CA-03 | T3005 |
| RF-05 | CA-04 | T3004, T3006 |
| RF-06, RF-07 | CA-05 | T3003, T3004, T3006 |
| RF-08 | CA-06 | T3001, T3003, T3004 |
| RF-09, RF-10 | CA-07 | T3004, T3006 |
| RF-11 | CA-08 | T3004 |
| RF-12 | CA-09 | T3004 |
| RF-13 | CA-11 | T3003, T3005 |
| RF-14 | CA-12 | T3005 |
| RF-15 | CA-12 | T3005 |

## Evidencia prevista

- `check.md` con capturas de perfil real, error sin red y aviso de sesión.
- Salida de pruebas unitarias, integración, UI, seguridad y Pitest.
- Resultado del humo con una cuenta desechable, sin credenciales ni token en el
  repositorio.
- Entrada en `memoria.md` con la diferencia `200/204` del logout y la decisión de
  no persistir `phone` ni `isVerified`.

## Notas de alcance

- No se implementa edición de perfil, borrado de cuenta, refresh token ni
  `:feature:*`.
- No se modifican los demás módulos ni se publica un release.
- Si el backend no corrige el OpenAPI, se mantiene la tolerancia a `200` y `204`
  y se registra la deuda en `memoria.md`.
