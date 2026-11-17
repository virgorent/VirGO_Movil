# Spec — 030 Sesiones

| Campo | Valor |
|---|---|
| Módulo | `030-sesiones` |
| Versión | 0.1.0 |
| Estado | Borrador |
| Fecha | 2026-11-16 |
| Constitución | v1.1.1 |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

El login (`003-iniciar-sesion`) ya funciona contra producción y la API devuelve `accessToken` y `refreshToken`, pero la app no los guarda: `AuthRepositoryImpl.login()` solo devuelve la sesión y `TokenStore` (DataStore cifrado con AEAD y Android Keystore, entregado por `000-arquitectura-base`) no se usa fuera de sus pruebas. Por eso cada arrancada empieza en la pantalla de visitante y no existe «sesión iniciada». Este módulo cablea el almacenamiento para que la app recuerde la sesión entre arrancadas.

## 2. Historias

| ID | Historia |
|---|---|
| HT-01 | Como **usuario registrado**, quiero que la app **recuerde mi sesión** para no volver a iniciar cada vez que la abro. |
| HT-02 | Como **usuario registrado**, quiero que **mi token viva solo en el almacen cifrado** para que nadie más lo lea de mi dispositivo. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | Cuando el login responde 200, se guarda el `accessToken` en `TokenStore` (cifrado, Art. V.3) antes de navegar a la pantalla principal. |
| RF-02 | Al arrancar la app con un token guardado, se abre directo en la pantalla principal del rol (`HomeRoute`), sin pasar por A5 ni por la home de visitante. |
| RF-03 | Al arrancar sin token guardado, se abre la home de visitante (`GuestHomeRoute`), como hoy. |
| RF-04 | La sesión guardada sobrevive a un reinicio completo de la app (matar el proceso y volver a abrirla). |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Seguridad:** el token vive solo en `TokenStore` (DataStore cifrado con llave del Keystore); nunca en logs, en preferencias en claro ni en el `UiState` (Art. V.3 e IX.1). |
| RNF-02 | **Rendimiento:** la decisión de arranque se resuelve sin pantalla de espera artificial; la lectura local de DataStore no bloquea el primer fotograma de forma perceptible. |
| RNF-03 | **Arquitectura:** la UI no conoce DataStore: el estado de sesión sale del repositorio como `Flow`/`StateFlow` y la navegación solo lo observa (Art. III.2, III.3 y III.6). |
| RNF-04 | **Trazabilidad:** los commits citan los IDs de tarea (`feat(T3001): …`) y el módulo queda con evidencia en `check.md` (Art. X.2). |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un login exitoso, **cuando** se navega a la home, **entonces** `TokenStore` contiene el `accessToken` cifrado y ninguna prueba de seguridad lo encuentra en claro. |
| CA-02 | **Dado** una sesión guardada, **cuando** se mata el proceso y se vuelve a abrir la app, **entonces** se entra directo a la pantalla principal del rol sin volver a A5. |
| CA-03 | **Dado** que no hay sesión guardada, **cuando** se arranca la app, **entonces** se abre la home de visitante (sin regresiones del flujo actual). |
| CA-04 | **Dado** un login fallido (401/400/red), **cuando** ocurre, **entonces** no se guarda ningún token. |
| CA-05 | **Dado** el almacenamiento, **cuando** se inspecciona el DataStore en la prueba de seguridad, **entonces** el valor está cifrado (ciphertext distinto del token) y no hay token en los logs. |

## 6. Seguridad y privacidad

| Amenaza | Control |
|---|---|
| Fuga de secretos | Token solo en `TokenStore` cifrado (Keystore/AEAD); sin tokens en logs ni en errores (R8 en release). |
| Acceso no autorizado | Sin credenciales guardadas; el token caducado lo rechaza la API (401) y el manejo del 401 llegará con el primer endpoint autenticado. |
| Datos personales | No se persiste ningún dato del formulario de login; solo el token opaco. |
| Abuso o fuerza bruta | No agrega superficie de red: consume el flujo de login ya existente. |

## 7. Fuera de alcance

- **UI y flujo de cerrar sesión** (borrar la sesión desde la app): llega con `021-favoritos-perfil` (RF-08), que llamará `TokenStore.clear()`.
- **Renovación del token** con `POST /api/v1/auth/refresh` (`013-post-auth-refresh`): el endpoint responde **404** en producción (sondeo 2026-11-16); el `refreshToken` no se guarda hasta que exista.
- **Invalidación ante 401**: llegará con el primer endpoint autenticado (`006-users-me`).
- **Enrutar por rol** (cliente/propietario/proveedor): `HomeRoute` sigue siendo `HomePlaceholder` hasta sus specs.
- **Múltiples cuentas** en el mismo dispositivo.

## 8. Supuestos, dependencias y preguntas abiertas

**Supuestos**

- Arrancar en la home de visitante sin sesión es el comportamiento correcto (el actual).
- `TokenStore` del `000` es la única vía de persistencia de la sesión; no se crea otro almacén.

**Dependencias**

| Endpoint / pieza | Spec | Estado (2026-11-16) |
|---|---|---|
| `POST /api/v1/auth/login` | `003-auth-login` del backend | ✅ En producción (humo T304) |
| `TokenStore` (DataStore + AEAD) | `000-arquitectura-base` | ✅ Entregado y probado (`TokenStoreTest`) |
| `POST /api/v1/auth/refresh` | `013-post-auth-refresh` del backend | ❌ 404 en producción → fuera de alcance |

**Preguntas abiertas**

| # | Pregunta | Responsable |
|---|---|---|
| P-01 | ¿La ruta de arranque espera la resolución del token mostrando el tema de la app, o se acepta un parpadeo mínimo a la home visitante? (Propuesta: esperar la resolución, sin splash nuevo.) | equipo |
