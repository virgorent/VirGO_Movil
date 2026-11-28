# Plan — 021 Perfil: borrar cuenta (favoritos y ayuda, pendientes)

| Campo | Valor |
|---|---|
| Módulo | `021-favoritos-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26) |
| Fecha | 2026-11-26 |
| Spec | [spec.md](spec.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Resumen técnico

Se extiende `030`; no se crea módulo `:feature:*`.

| Capa | Cambio |
|---|---|
| `:core:network` | `UsersApi.deleteMe()` (`@DELETE("api/v1/users/me")`, `Response<Unit>`). |
| `:core:data` | `AccountRepositoryImpl.deleteMe()`: `204` → éxito; el resto se mapea a `AppError` con el `safeCall` existente. |
| `:core:domain` | `DeleteAccountUseCase`. |
| `:app` `session` | `SessionEndReason.ACCOUNT_DELETED` para el aviso de RF-04. |
| `:app` `ui/account` | `AccountProfileViewModel` suma `confirmingDelete` y `deleting`; en `204` llama a `session.invalidate(ACCOUNT_DELETED)`, el mismo camino de limpieza que el logout de `030`. `AccountProfileScreen` suma la acción y el diálogo, solo para `client`. |
| `:app` `ui/explore` | `FAVORITES_ENABLED = false`: `ExploreList` y `ExploreMap` no pintan el corazón y `ExploreViewModel` no llama a `GetFavoritesUseCase`. |

## 2. Constitution Check

| Art. | Resultado | Cómo se cumple |
|---|---|---|
| I y II | ✅ | Spec atómico; recorta la v1.0.0 a lo que tiene backend. |
| III | ✅ | MVVM/UDF; la limpieza de sesión reutiliza `SessionRepository`. |
| IV | ✅ | Solo `/api/v1`, Bearer y `AppError`. |
| V | ✅ | Tras el `204` no queda token ni datos de usuario. |
| VI | ✅ | Sección 3. |
| VII | ✅ | Sistema de diseño y `strings.xml`. |
| VIII.6 | ✅ | MockWebServer hasta que producción tenga el backend nuevo. |

## 3. Estrategia de pruebas

| Tipo | Casos |
|---|---|
| Unitarias | `AccountProfileViewModel`: cancelar, `204`, `401`, red/5xx; no hay doble envío. |
| Integración | MockWebServer: método `DELETE`, ruta y Bearer; `204`, `401`, `500` y sin conexión. |
| UI | Acción visible solo para `client`; diálogo con foco en «Cancelar»; fuente 2.0 a 320 dp; «Explorar» sin corazón. |
| Seguridad | Tras `204`: `TokenStore` vacío y `DataStore` sin sesión; sin llamadas a `/favorites`. |
| Humo | Borrar una cuenta de prueba creada por `humo-produccion.py --modo cuentas` tras el despliegue. |

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| Borrado accidental | Diálogo con «Cancelar» por defecto y texto explícito. |
| El backend cambia lo que conserva | P-03: se revisa el texto con Esaú o Gafass antes de fusionar. |
| Ocultar favoritos parece una regresión | `005` RF-08 decía favoritos solo de muestra; el corazón de `ExploreScreen` no podía guardar nada; hoy esa pantalla ni siquiera está conectada. |
