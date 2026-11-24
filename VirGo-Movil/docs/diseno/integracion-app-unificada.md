# Integración de la experiencia en VirGo

## Autorización y alcance

El 2026-11-23 el usuario pidió unificar la app, conservar el login real,
dirigir por rol, conectar las hojas con sus pantallas y preparar la integración
a `main`, con revisión visual exclusivamente suya. La indicación fue:
«Hazlo todo y prepáralo para integrarlo a main».

Extiende la [excepción temporal de entrega visual](excepcion-demo-urgente.md)
a la integración dentro de la aplicación normal. No equivale a aprobación
retroactiva/cierre de los specs `000`, `030` o `031`. No autoriza a eliminar
checks, modificar el ruleset ni fusionar/publicar automáticamente.

## Paquetes de integración

| ID provisional | Trabajo | Verificación |
|---|---|---|
| UI-01 | Sesión cifrada, expiración, restauración y cierre | Pruebas de persistencia, corrupción, expiración y logout |
| UI-02 | Navegación normal según rol acreditado | Cuenta sin selector de privilegios, desconocidos denegados |
| UI-03 | Mover pantallas al source set principal y retirar launcher separado | Manifest con una entrada; compilación de aplicación |
| UI-04 | Estado compartido de formularios y hojas, aislado por cuenta | Cambios visibles al cerrar hoja, limpieza al cerrar sesión |
| UI-05 | Conservar catálogo/autenticación reales | Sin reemplazo por repositorios ficticios ni contratos inventados |
| UI-06 | PR de consolidación contra main | Pipeline, seguridad, evidencia y revisión humana pendientes de decisión |
| UI-07 | Pulido visual solicitado: pestañas/selector de IA Studio, jerarquía de enlaces, separación e iconos delineados | Selección exclusiva, opciones, navegación y accesibilidad compacta |

UI-07 fue solicitado explícitamente por el usuario tras señalar botones
confundidos con selectores y controles pegados, aportando una captura de
IA Studio y autorizando continuar con iconos más actuales. No añade APIs
ni modifica permisos o reglas de sesión; conserva los avisos de simulación.

## Separación de funciones reales y locales

- El login y el catálogo existentes conservan sus repositorios/API.
- El registro que no entrega una sesión vuelve al login; no se convierte
  la selección de un rol en una autorización.
- El rol de una cuenta se valida desde la sesión; no se elige en un selector.
  Los roles no confirmados por contrato no se habilitan por suposición.
- Funciones sin servicio confirmado conservan datos de muestra y cambios
  locales, con aviso visible de que no están sincronizados.
- No se activan pagos, reservas, IA, invitaciones, mensajes, subida de archivos
  ni notificaciones reales sin sus contratos e integraciones.
- Los datos locales se acotan al espacio de cuenta/rol y se limpian al salir.
  El token nunca se pasa a rutas, estado guardado de Compose ni textos de UI.
- La muestra sin cuenta se abre desde la navegación de la misma actividad,
  solo en debug; no crea sesión ni privilegios reales.

## Preparación para main

La rama `feat/033-app-unificada` parte de la entrega de #31 y se actualizó
mediante merge no destructivo de `origin/main`. La consolidación incluye
antecedentes de #27, #29 y #31 aún no fusionados; no se cierran esos PRs
ni se altera su revisión automáticamente.

Se solicita aceptación visual exclusivamente de `ccabrera-dev8`, sin pedir
otros revisores humanos. Si ese usuario es también autor del PR, GitHub no
permite su autoaprobación formal. Esa limitación no se sortea eliminando
protecciones ni suponiendo que asignación equivale a aprobación.

La [evidencia local y los pendientes](../evidencia/unificacion-app/verificacion.md)
registran el resultado de la verificación. Este documento no acredita por sí solo implementación
completa, compilación de release, aceptación visual o preparación de producción.
