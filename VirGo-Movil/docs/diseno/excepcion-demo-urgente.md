# Excepción temporal · demo visual urgente

## Aprobación humana

El 2026-11-22 el usuario pidió entregar lo visual cuanto antes y posponer los
specs. Se le explicó que las reglas actuales exigían etapas previas y se propuso
una excepción temporal acotada, sin relajar seguridad, pruebas ni trazabilidad.
Respondió: «dale estoy de acuerdo».

Esta autorización corresponde a una **demo visual Android**, no a un producto
integrado, una aceptación visual o un cierre de `000`/`031`. Se registra antes de
implementar para no atribuir aprobación retroactiva a specs inexistentes.
La excepción solicitada afecta el orden documental de SDD y el gate de base
para esta entrega aislada; no se modifica silenciosamente la constitución
vigente ni se extiende esta autorización a producción.

## Límites obligatorios

- Implementación exclusivamente en la variante `debug`, con acceso separado
  «VirGo Demo». Los flujos existentes y la variante `release` se conservan.
- Referencia: prototipo React local autorizado. No afirmar paridad con Figma.
- Datos ficticios identificados persistentemente; ningún pago, reserva,
  cotización, mensaje o resultado IA se presenta como operación real.
- Sin red en el demo, sesión real, permisos nuevos, dependencias nuevas, secretos
  ni almacenamiento de datos personales. No invocar APIs desde formularios demo.
- No descargar fotografías de derechos desconocidos. Recursos de muestra
  geométricos/iconos se identifican como tales; no equivalen a fotos finales.
- Mantener análisis, pruebas, escaneo de secretos y revisión obligatoria de PR.
  No fusionar ni publicar release. Karasu coordina la integración.

## Trabajo autorizado y verificación

| ID provisional | Entrega | Verificación |
|---|---|---|
| DV-01 | Contenedor debug, selector de rol, historial y navegación | Aislamiento del manifest, pruebas de rutas y regreso |
| DV-02 | Destinos cliente y flujos locales | Rutas alcanzables, formularios locales y etiquetas de simulación |
| DV-03 | Destinos propietario | Catálogo, editor, calendario y solicitudes demo navegables |
| DV-04 | Destinos proveedor | Perfil, galería de muestra, zonas, agenda y solicitudes |
| DV-05 | Destinos compartidos | Perfil, mensajes, chat, avisos y ayuda sin envío real |
| DV-06 | Integración y APK debug | ktlint, detekt, lint, pruebas, compilación e instalación |

El inventario de referencia tiene 59 destinos: cuatro de acceso ya existentes
y 55 autenticados a representar en el demo, además de hojas internas. La
existencia de una ruta no acredita fidelidad visual ni que sus estados estén
completos. Las brechas se informarán explícitamente.

## Regularización pendiente

Tras esta entrega: trasladar los paquetes provisionales a specs atómicos,
completar planes/tareas/checks con evidencia, resolver T014/T036 y aprobar
visualmente cada bloque. No marcar aprobación humana, cierre o integración
real a partir de pruebas automatizadas.
