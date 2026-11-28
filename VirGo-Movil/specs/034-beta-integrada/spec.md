# Spec — 034 Beta integrada sin simulaciones

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-11-24 |
| Estado | Alcance autorizado por el usuario en esta sesión; aceptación visual pendiente |
| Constitución | Móvil v1.2.0 |
| Plan / tareas / evidencia | [plan.md](plan.md) · [task.md](task.md) · [check.md](check.md) |

## Objetivo e historia

Como usuario de la beta, quiero navegar por mi rol sin ver datos u operaciones
ficticias, distinguiendo las capacidades disponibles de las pendientes.

El usuario eligió explícitamente «Sí, beta integrada»: conservar los destinos de
los tres roles, retirar recorrido demo y datos ficticios, mostrar estados vacíos
y deshabilitar acciones sin servicio. Se mantiene el alcance visual de la
excepción existente; no se habilitan nuevas integraciones ni se cierra `000`.

## Requisitos y aceptación

| RF / CA | Comportamiento verificable |
|---|---|
| RF-01 / CA-01 | Ni visitante ni cuenta autenticada acceden a un recorrido demo o selector de roles, incluso en debug. |
| RF-02 / CA-02 | El catálogo solo muestra salones entregados por su repositorio real; vacío, carga y error son estados distintos con reintento. Sin mapa ilustrativo, distancias desde ubicación supuesta, fotos o valoraciones inventadas. |
| RF-03 / CA-03 | Los destinos por rol se conservan como secciones. Sin servicio muestran «Próximamente» y no aceptan envíos, pagos, mensajes, reservas ni cambios locales simulados. |
| RF-04 / CA-04 | Cuenta muestra solo nombre, correo y rol de la sesión validada, en modo lectura. Explica que cerrar sesión es local; sin afirmar perfil actualizado o revocación remota. |
| RF-05 / CA-05 | Login y registro conservan sus pantallas y repositorios existentes; no se añaden APIs, permisos ni credenciales. |
| RF-06 / CA-06 | La beta usa tokens y componentes del sistema de diseño, espaciado uniforme, tema claro/oscuro, controles de 48 dp y texto escalable. Sin formularios nuevos. |

## Fuera de alcance y límites

- `030`: perfil remoto, logout remoto, refresh y nuevas reglas de sesión.
- Publicación, firma, subida a tiendas, cambios de versión y despliegues.
- Borrar los componentes históricos de muestra: quedan sin acceso desde la
  navegación normal, para no destruir referencias o pruebas existentes.
- Implementar funcionalidad de los destinos pendientes o cerrar specs previos.
- El entorno debug conserva su configuración de API local; no se cambia a
  producción sin una decisión independiente.
- Aceptación visual solo de `ccabrera-dev8`. Sin afirmar paridad con Figma.
