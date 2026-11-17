# Tareas — 030 Sesiones

| Campo | Valor |
|---|---|
| Módulo | `030-sesiones` |
| Versión | 0.1.0 |
| Estado | Borrador. **Las tareas no se ejecutan hasta aprobar este archivo.** |
| Fecha | 2026-11-16 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |
| Constitución | v1.1.1 |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

**[P]** = puede ejecutarse en paralelo con otras [P] de la misma fase.

## F0 · Datos y dominio

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3001 | Inyectar `TokenStore` en `AuthRepositoryImpl` y guardar el `accessToken` tras un login 200 (sin guardar en 401/400/500/sin conexión) | `000`: T034 | `dev-android` | `AuthRepositoryImplTest` con `FakeTokenStore` en verde | ⏳ |
| T3002 | Exponer `isAuthenticated: Flow<Boolean>` en `AuthRepository` a partir de `TokenStore.token` | T3001 | `dev-android` | Prueba de flujo: emite `true` tras guardar y `false` sin token | ⏳ |

## F1 · Arranque

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3003 | `SessionViewModel` en `:app` y `startDestination` dinámico en `VirGoNavHost` (con sesión → `HomeRoute`; sin sesión → `GuestHomeRoute`) | T3002 | `dev-android` | Prueba de navegación de arranque en verde; sin regresión del arranque visitante | ⏳ |

## F2 · Verificación

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3004 | [P] Pruebas unitarias, de UI y de seguridad (token cifrado, sin logs, rutas de arranque) | T3003 | `qa-movil` | `ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug` → BUILD SUCCESSFUL | ⏳ |
| T3005 | Humo en el emulador `Pixel_10_Pro` con APK *debug*: login → matar proceso → reabrir → home; instalación limpia → visitante | T3004 | `qa-movil` | Capturas en `docs/evidencia/` y `check.md` §2 | ⏳ |

## F3 · Cierre

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T3006 | Completar `check.md`, actualizar el estado del spec y registrar la entrada en `memoria.md` | T3005 | `qa-movil` | `check.md` completo + entrada en `memoria.md` | ⏳ |

## Matriz de trazabilidad

| Requerimiento / Criterio | Tareas |
|---|---|
| HT-01 | T3001, T3002, T3003 |
| HT-02 | T3001, T3004 |
| RF-01 | T3001 |
| RF-02 | T3002, T3003 |
| RF-03 | T3003 |
| RF-04 | T3001, T3005 |
| RNF-01 | T3001, T3004 |
| RNF-02 | T3003 |
| RNF-03 | T3002, T3003 |
| RNF-04 | T3006 |
| CA-01 | T3001, T3004 |
| CA-02 | T3003, T3005 |
| CA-03 | T3003, T3005 |
| CA-04 | T3001 |
| CA-05 | T3004 |
