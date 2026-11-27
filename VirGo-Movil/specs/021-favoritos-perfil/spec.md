# Spec — 021 Perfil: borrar cuenta (favoritos y ayuda, pendientes)

| Campo | Valor |
|---|---|
| Módulo | `021-favoritos-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26, «Aprobados, impleméntalos») |
| Fecha | 2026-11-26 (v1.0.0 del 2026-11-05 sustituida) |
| Constitución | v1.2.0 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` (C38, C39, C40), `030-cuenta-sesion` y backend `018-delete-users-me` en la cadena hacia `main` |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Contexto y objetivo

La v1.0.0 juntaba favoritos, perfil editable, métodos de pago, asistente IA, mensajes, preferencias y ayuda con
formulario de contacto. Contra el backend que va a `main` (2026-11-26):

| Pieza de la v1.0.0 | Backend | Decisión |
|---|---|---|
| Ver perfil y cerrar sesión | `GET /users/me`, `POST /auth/logout` | **Ya cubierto por `030`**; no se repite. |
| Borrar cuenta | `DELETE /users/me` existe | **Entra en este spec.** |
| Favoritos | No hay controlador de `/favorites` | Fuera; además se oculta el corazón que hoy falla (RF-07). |
| Editar perfil y contraseña | `PATCH /users/me` y `/password` no existen | Fuera. |
| Métodos de pago | `/payment-cards` no existe | Fuera. |
| Ayuda y contacto | `POST /contact` no existe | Fuera. |
| Mensajes, notificaciones, IA y preferencias | No existen | Fuera. |

Borrar la cuenta desde la app también es un requisito de Google Play para apps que permiten crear cuentas.

## 2. Historias

| ID | Historia |
|---|---|
| H-01 | Como cliente, quiero borrar mi cuenta desde la app, sabiendo antes qué pasa con mis datos. |
| H-02 | Como cliente, no quiero ver un botón de favoritos que siempre falla. |

## 3. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[C39]** En «Datos de tu cuenta» (`030`), con rol `client`, aparece al final la acción «Borrar mi cuenta», con estilo de acción destructiva y separada de «Cerrar sesión». |
| RF-02 | **[C39]** Al tocarla se abre un diálogo de confirmación: «¿Borrar tu cuenta? Se cerrará tu sesión en todos tus dispositivos y ya no podrás entrar con este correo. Tus reservaciones y cotizaciones se conservan para el salón con el nombre que tenían. Esta acción no se puede deshacer.». Botones «Cancelar» (por defecto) y «Borrar cuenta». |
| RF-03 | **[C39]** Confirmar envía `DELETE /api/v1/users/me` con el Bearer de la sesión. Mientras espera, el diálogo muestra progreso y no se puede cerrar ni repetir el envío. |
| RF-04 | **[C39]** `204` borra el token cifrado y el estado local del workspace (igual que `030` RF-09) y lleva al inicio de visitante con el aviso «Tu cuenta se borró.». |
| RF-05 | **[C39]** `401` se trata como sesión expirada (`030` RF-05): la cuenta **no** se borró, y el aviso lo dice: «Tu sesión expiró. Inicia sesión de nuevo para borrar tu cuenta.». |
| RF-06 | **[C39]** Red, timeout o 5xx: la sesión sigue abierta, el diálogo se cierra y se muestra «No se pudo borrar tu cuenta. Inténtalo de nuevo.». No se reintenta en segundo plano. |
| RF-07 | **[C38]** Mientras el backend no tenga `/api/v1/favorites`, «Explorar» (lista y mapa) no muestra el corazón de favorito ni llaman a esa API. Se controla con una sola bandera en código, `FAVORITES_ENABLED = false`. |
| RF-08 | Favoritos, Ayuda y las demás secciones de la v1.0.0 siguen mostrando «Próximamente», como en `030` RF-14. |
| RF-09 | Todos los textos están en `strings.xml`. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** el diálogo usa el componente de diálogo del sistema de diseño y el color de error solo en el botón destructivo. |
| RNF-02 | **Accesibilidad:** el foco inicial cae en «Cancelar»; TalkBack lee el título y el texto completo; áreas táctiles de 48 dp; fuente 2.0 sin cortes. |
| RNF-03 | **Seguridad:** no se pide la contraseña en la app (el backend no la exige); se borra el token aunque falle la navegación; nada del usuario queda en `DataStore` ni en logs tras un `204`. |

## 5. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** un cliente con sesión, **cuando** confirma «Borrar cuenta» y el servidor responde `204`, **entonces** vuelve al inicio de visitante, sin token guardado, y ve «Tu cuenta se borró.». |
| CA-02 | **Dado** el diálogo, **cuando** toca «Cancelar», **entonces** no sale ninguna petición. |
| CA-03 | **Dado** un error de red, **cuando** confirma, **entonces** sigue con sesión y ve el error con la opción de volver a intentar. |
| CA-04 | **Dado** un `401`, **cuando** confirma, **entonces** se cierra la sesión y el aviso dice que la cuenta no se borró. |
| CA-05 | **Dado** «Explorar» con el backend actual, **cuando** se muestra un salón, **entonces** no hay corazón y no sale ninguna llamada a `/favorites`. |
| CA-06 | **Dado** una cuenta `owner` o `provider`, **cuando** abre su perfil, **entonces** no ve «Borrar mi cuenta» (ver P-01). |

## 6. Fuera de alcance

Pendiente para specs posteriores, cuando el backend lo tenga:

- Favoritos (`081`–`083`), con pestañas de salones y proveedores.
- Editar perfil, avatar y contraseña (`015`, `016`).
- Métodos de pago (`019`–`023`).
- Ayuda: preguntas frecuentes y formulario de contacto (`096`).
- Mensajes, notificaciones, asistente IA y preferencias.
- Borrar la cuenta de propietario o proveedor (P-01).
- Página web para pedir el borrado sin la app (P-02).

## 7. Dependencias

| Endpoint | Spec backend | Estado (2026-11-26) |
|---|---|---|
| `DELETE /api/v1/users/me` | `018-delete-users-me` | En la cadena hacia `main`. Revoca todas las sesiones; cambia el nombre a «Cuenta eliminada» y anonimiza correo, teléfono y contraseña; responde `204`. No borra reservaciones, cotizaciones ni salones, y estas guardan su propia copia del nombre del cliente. |
| `GET /api/v1/users/me`, `POST /api/v1/auth/logout` | `006`, `004` | Ya los usa `030`. |
| `/api/v1/favorites` | `081`–`083` | **Sin controlador**; la app hoy los llama desde «Explorar» y fallan. |

Producción sigue con el backend anterior hasta el despliegue; mientras tanto se prueba con MockWebServer
(Art. VIII.6) y con el backend local.

## 8. Preguntas abiertas

| ID | Pregunta | Quién decide | Propuesta |
|---|---|---|---|
| P-01 | ¿Qué pasa con los salones publicados si un propietario borra su cuenta? | Usuario + Esaú | Hasta decidirlo, el borrado solo se ofrece a clientes. |
| P-02 | Google Play pide además un enlace web para pedir el borrado. ¿Quién lo publica? | Usuario | Fuera de la app; se resuelve antes de la ficha de Play. |
| P-03 | ¿El texto de RF-02 describe bien lo que hace el backend? | Esaú / Gafass | Se verificó contra `DeleteAccountHandler` y `MongoAuthRepositories.AnonymizeAsync` el 2026-11-26; confirmar si cambia. |
| P-04 | ¿Las reservaciones y cotizaciones deben dejar de mostrar el nombre del cliente borrado? | Usuario + Esaú | Es un cambio del backend; mientras no exista, RF-02 lo dice tal cual. |
