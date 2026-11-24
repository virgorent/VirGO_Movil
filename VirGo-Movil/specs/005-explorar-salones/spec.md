# Spec — 005 Explorar salones

| Campo | Valor |
|---|---|
| Módulo | `005-explorar-salones` |
| Versión | 1.1.0 |
| Estado | En revisión; refinamiento visual autorizado por el equipo el 2026-11-19; integración completa pendiente |
| Fecha | 2026-11-19 |
| Rol | Cliente |
| Fuente | `Checklist VirGo.md` y Figma «Pantallas VirGo» |
| Plan | [plan.md](plan.md) · Tareas: [task.md](task.md) · Verificación: [check.md](check.md) |

## 1. Pantallas

El equipo autorizó continuar la consolidación del catálogo con fotografías amplias
como entrada predeterminada. El mapa se conserva como alternativa. Esta revisión
documenta la implementación parcial; no certifica cartografía real, endpoints
pendientes ni cierre del módulo.

| Código | Pantalla |
|---|---|
| B8 | Explorar (mapa) |
| C9 | Explorar (lista) |
| C10 | Filtros |
| C11 | Recomendados para ti |

## 2. Requerimientos funcionales

| ID | Requerimiento |
|---|---|
| RF-01 | **[B8]** Mapa con los salones cercanos y tu ubicación |
| RF-02 | **[B8]** Tarjeta inferior con foto, capacidad, precio y distancia del salón seleccionado |
| RF-03 | **[B8]** Buscador que abre los filtros |
| RF-04 | **[B8]** Categorías: Jardines, Alberca, Elegante, Haciendas e Infantil |
| RF-05 | **[B8]** Botón «Para ti», con recomendaciones |
| RF-06 | **[B8]** Acceso a Proveedores |
| RF-07 | **[B8]** Campana de notificaciones con el número de avisos sin leer |
| RF-08 | **[B8]** Botón de favorito (corazón) en cada salón |
| RF-09 | **[C9]** Botón para cambiar entre mapa y lista |
| RF-10 | **[C9]** Tarjetas con foto, calificación, capacidad, precio y distancia |
| RF-11 | **[C9]** Orden por Recomendados, Cercanía, Precio o Calificación |
| RF-12 | **[C10]** Zona o nombre del salón |
| RF-13 | **[C10]** Número de invitados |
| RF-14 | **[C10]** Presupuesto para el salón |
| RF-15 | **[C10]** Distancia máxima |
| RF-16 | **[C10]** Fecha disponible |
| RF-17 | **[C10]** Orden de resultados |
| RF-18 | **[C10]** Solo muestra los salones que cumplen todos los filtros |
| RF-19 | **[C10]** Avisa cuando ningún salón cumple |
| RF-20 | **[C10]** Botón para limpiar los filtros |
| RF-21 | **[C11]** Recomendaciones según el tipo de evento, los invitados y el presupuesto |
| RF-22 | **[C11]** Porcentaje en que «encaja» cada salón, con el motivo |
| RF-23 | **[C11]** Insignia de Estimación IA |
| RF-24 | **[C9]** Al entrar a Explorar se muestra la lista con fotografías amplias; el mapa sigue disponible mediante el selector. |
| RF-25 | Las fotografías ilustrativas se aplican solo a salones ficticios, se identifican como imágenes de ejemplo y sus créditos y licencias se pueden consultar dentro de la app. No se usan como fotos de salones reales. |
| RF-26 | La cabecera integra las acciones sin una fila vacía de desarrollo ni una barra inferior de un único destino; Atrás cierra primero la hoja abierta. |

## 3. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Diseño:** solo componentes de `:core:designsystem`; textos en `strings.xml` (Art. VII). |
| RNF-02 | **Accesibilidad:** contraste AA, áreas táctiles de 48 dp y texto escalable. |
| RNF-03 | **Seguridad:** sin datos sensibles en logs; tokens solo en `TokenStore`. |

## 4. Criterios de aceptación

| ID | Criterio |
|---|---|
| CA-01 | **Dado** la pantalla B8, **cuando** se compara con la maqueta, **entonces** cumple todos sus requerimientos. |
| CA-02 | **Dado** la pantalla C9, **cuando** se compara con la maqueta, **entonces** cumple todos sus requerimientos. |
| CA-03 | **Dado** la pantalla C10, **cuando** se compara con la maqueta, **entonces** cumple todos sus requerimientos. |
| CA-04 | **Dado** la pantalla C11, **cuando** se compara con la maqueta, **entonces** cumple todos sus requerimientos. |
| CA-05 | **Dado** una respuesta de error de la API, **cuando** llega, **entonces** se muestra un mensaje claro, sin texto técnico. |
| CA-06 | **Dado** que no hay conexión, **cuando** se abre la pantalla, **entonces** se muestra un estado de error con opción de reintentar. |
| CA-07 | **Dado** un catálogo de muestra, **cuando** se entra a Explorar, **entonces** se ve inicialmente la lista con fotos amplias e identificación de ejemplo; al seleccionar un salón se puede consultar el mapa y volver a la lista. |
| CA-08 | **Dado** un salón de muestra, **cuando** se consultan los créditos desde lista o mapa, **entonces** se muestran autores, fuentes, licencias y modificaciones y se puede cerrar el diálogo. |
| CA-09 | **Dado** una hoja de filtros o recomendaciones abierta, **cuando** se pulsa Atrás, **entonces** se cierra la hoja antes de salir de Explorar. |

## 5. Fuera de alcance

- Lo que la maqueta simula (sección 7 del checklist), salvo que el spec de API lo implemente.
- Restauración/renovación de sesión, cierre de sesión remoto, bandeja de avisos,
  persistencia de favoritos demo y modularización `:feature:*`.

### Límites actuales que impiden dar el módulo por terminado

- RF-01: mapa ilustrativo en Canvas, centro fijo de Juriquilla; no cartografía ni
  permiso de ubicación real.
- RF-07: contador cuando la API responde; la campana aún muestra «próxima versión».
- RF-08: favoritos de muestras en memoria, nunca enviados a la API.
- RF-16/RF-18: fecha validada y conservada, pero sin filtrado de disponibilidad
  hasta recibir ese contrato. Los demás filtros y recomendaciones son locales.
- RF-21–RF-23: puntuación heurística local; la insignia no representa una llamada
  a un servicio de IA.
- Catálogo vacío: todavía activa muestras desde presentación. Separar demo/remoto
  en repositorios es trabajo posterior; no se afirma que esté resuelto.
- Fotografías remotas: no se cargan todavía; los salones reales usan marcador neutro.
- RNF-01/RNF-02: quedan estilos locales, blancos fijos y panel con altura del 90 %;
  faltan revisión integral de tema oscuro, pantalla pequeña, fuente 2.0 y teclado.

## 6. Dependencias

**Endpoints que consume:**

| Endpoint | Spec en el backend (`virgorent/VirGO_Backend`, `VirGo-API/specs`) |
|---|---|
| `GET /api/v1/venues` | `007-venues-catalog` |
| `GET /api/v1/notices` | `090-get-notices` |
| `GET /api/v1/favorites` | Contrato de listado por confirmar con backend |
| `POST /api/v1/favorites` | `082-post-favorites` |
| `DELETE /api/v1/favorites/:targetType/:targetId` | `083-delete-favorites-targettype-targetid` |

Sin URL y JSON probados del backend, la pantalla se prueba solo con MockWebServer (Art. VIII.6).

La sonda histórica registrada el 2026-11-19 recibió catálogo `200` vacío y
avisos/favoritos `404`. No se ha repetido esa comprobación en esta consolidación.
