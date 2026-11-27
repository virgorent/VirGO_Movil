# Tareas — 021 Perfil: borrar cuenta (favoritos y ayuda, pendientes)

| Campo | Valor |
|---|---|
| Módulo | `021-favoritos-perfil` |
| Versión | 2.0.0 |
| Estado | Aprobado por el usuario (2026-11-26) |
| Fecha | 2026-11-26 |
| Spec / Plan / Check | [spec.md](spec.md) · [plan.md](plan.md) · [check.md](check.md) |

**Estados:** ⏳ Pendiente · 🔄 En curso · ✅ Hecha (con evidencia) · ⛔ Bloqueada

Las tareas T2101–T2106 de la v1.0.0 se sustituyen. El spec se aprobó el 2026-11-26.

| ID | Tarea | Depende de | Responsable | Verificación | Estado |
|---|---|---|---|---|---|
| T2101 | `UsersApi.deleteMe`, repositorio y `DeleteAccountUseCase` | — | `dev-android` | MockWebServer 204/401/500/sin red | ⏳ |
| T2102 | Acción y diálogo en «Datos de tu cuenta», solo para `client` | T2101 | `dev-android` | Pruebas de `ViewModel` y de UI | ⏳ |
| T2103 | Limpieza de sesión con `ACCOUNT_DELETED` y aviso en el inicio de visitante | T2101 | `dev-android` | Prueba: sin token tras `204` | ⏳ |
| T2104 | Ocultar el corazón de favoritos con `FAVORITES_ENABLED = false` | — | `dev-android` | Prueba de UI y sin llamadas a `/favorites` | ⏳ |
| T2105 | Accesibilidad y seguridad | T2102, T2104 | `qa-movil` | Foco, TalkBack, fuente 2.0 | ⏳ |
| T2106 | Humo contra producción tras el despliegue | Despliegue del backend | `qa-movil` | `check.md` completo | ⛔ |
