# Demo visual de tres roles

Entrega bajo la [excepción temporal aprobada](excepcion-demo-urgente.md).
La aceptación visual corresponde **exclusivamente al usuario solicitante**.
Una revisión técnica automatizada no sustituye su decisión.

## Cómo recorrerla

1. Instalar el APK **debug**, no un APK de producción.
2. Abrir el acceso **VirGo Demo** del launcher. El acceso **VirGo** conserva
   la aplicación y sus integraciones existentes.
3. Elegir Cliente, Propietario de salón o Proveedor.
4. Recorrer las pestañas inferiores y las acciones de cada pantalla.
   «Todas las pantallas de este rol» permite revisar el inventario sin
   depender de un recorrido concreto.
5. Abrir las hojas mediante sus acciones en la parte superior del destino.
   «Volver», el botón de regreso Android y «Cambiar rol» permiten salir.

No se requiere registro, contraseña, token ni cuenta real para la demo.
No se envían mensajes, cotizaciones, invitaciones, pagos o reservaciones.

## Cobertura implementada

| Grupo | Destinos exclusivos | Entrada |
|---|---:|---|
| Cliente | 29 | Explorar |
| Propietario | 12 | Inicio |
| Proveedor | 9 | Inicio |
| Compartidos | 5 | Perfil, Mensajes, Conversación, Notificaciones, Ayuda |
| **Total demo** | **55** | Registro tipado por rol en `DemoDestinations.kt` |

Los cuatro destinos existentes de acceso/visitante están separados y no se
cuentan como implementación nueva: inventario total de referencia, **59**.

Hay 15 hojas de muestra, H01–H15, en `DemoSheetsRegistry.kt`: búsqueda, fecha,
presupuesto, evento, equipo, edición de perfil, preferencias, método de pago
ficticio, registro de pago, invitado, pendiente, recordatorio, colaboración,
bloqueo de fechas y evento de agenda. No suman destinos.

## Límites explícitos

- Es una demo offline, no la implementación integrada de los módulos `006`–`029`.
- La referencia es el prototipo React local autorizado; no se afirma paridad
  demostrada con Figma ni aceptación visual.
- No se incorporaron fotografías nuevas de derechos desconocidos. Las
  ilustraciones geométricas locales están identificadas como muestras.
- Calendarios y actividad contienen fechas/datos fijos de ejemplo, no
  disponibilidad o estadísticas reales.
- Algunos registros comparten un detalle de muestra; no son catálogos reales.
- Los formularios y hojas mantienen estado local de vista. Las hojas no
  actualizan el contenido de las pantallas de fondo; su confirmación lo explica.
  `rememberSaveable` puede restaurar estado mediante Android al recrear la
  actividad; no es una sesión autenticada ni persistencia backend.
- Los campos de texto compartidos aceptan como máximo 500 caracteres.
- No hay subida de archivos, envío, motor IA, pasarela bancaria ni
  programación real de notificaciones.
- Modo claro/oscuro hereda el sistema de diseño existente. No se certifica
  TalkBack manual ni equivalencia visual en todos los tamaños por compilar.

## Revisión y publicación

El PR permanece en **borrador**, sin merge ni release. No se solicitan
revisores humanos adicionales para la aceptación visual. Se mantienen
los checks automáticos y los requisitos vigentes de la rama protegida;
esta indicación no puede desactivarlos.

Si quien creó el PR es también el usuario revisor, GitHub no permite
solicitarse una revisión formal a sí mismo. En ese caso se identifica
la responsabilidad visual en el cuerpo y la asignación del PR.

Resultados técnicos y capturas: ver el
[informe de evidencia](../evidencia/demo-visual/verificacion.md);
la presencia de una ruta en el inventario no acredita por sí sola sus
acciones, su fidelidad o su integración.
