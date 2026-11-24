# Verificación · app unificada

Fecha: 2026-11-23. Rama `feat/033-app-unificada`, contra `main`.
Autorización y alcance: [integración](../../diseno/integracion-app-unificada.md).
Código/evidencia: `a5a6739`, [PR #32 en borrador](https://github.com/virgorent/VirGO_Movil/pull/32).

## Resultado local

```text
ktlintCheck detekt lint testDebugUnitTest
:core:domain:test :core:common:test pitest assembleDebug
BUILD SUCCESSFUL in 1m 46s
383 tareas: 8 ejecutadas, 1 desde caché, 374 actualizadas
```

- XML: **57 suites, 318 pruebas; 0 fallos, errores u omitidas**.
- Incluye sesión, rechazo de roles desconocidos, expiración, corrupción,
  persistencia cifrada, limpieza y estado compartido de hojas.
- Ejecución incremental; Pitest de dominio/common reutilizado. No se
  atribuye cobertura de mutación a Compose ni se afirma ausencia de avisos.
- Revisión técnica automatizada de solo lectura: sin bloqueantes identificados
  en los archivos inspeccionados; no sustituye Gitleaks ni aceptación visual.

## APK debug

- 24 383 621 bytes.
- SHA-256: `24f896168cb4b67c31917bfe9249ef181fda393bd02427955e959e4d36c931f0`.
- Instalación `adb -s emulator-5554 install -r`: correcta.
- Inicio `.MainActivity`: correcto.
- `cmd package query-activities` con MAIN/LAUNCHER: **una actividad**,
  `mx.virgo.app/.MainActivity`. Ya no existe `VisualDemoActivity`.
- No se compiló ni publicó release. No se actuó sobre dispositivos físicos.

## Recorrido en emulador

Se usó el servidor **local simulado**, en `10.0.2.2:5122`, con el contrato
de login y datos ficticios `example.invalid`. No se usaron credenciales
reales ni se crearon cuentas de producción. El servidor de prueba y sus
logs no forman parte del código de la app ni del repositorio.

1. Login cliente → espacio cliente, sin selector de rol.
2. `force-stop` y nuevo arranque → restauración de la sesión cifrada vigente.
3. Cerrar sesión → visitante.
4. Login propietario y proveedor → sus espacios correspondientes; logout
   entre cuentas. Métricas, operaciones y nombres demo siguen identificados.
5. Muestra sin cuenta → selector dentro de la misma actividad.

Esto verifica el recorrido del APK contra un contrato simulado, **no acredita
autenticación de los tres roles contra producción** ni revocación remota.
El catálogo simulado vacío activa las muestras existentes identificadas;
favoritos y avisos locales respondieron 404.

El emulador se reinició durante la continuación y mostró un ANR de System UI;
se reanudó el recorrido sin borrar datos deliberadamente. Arranques de 33,9 s
y 5,0 s no acreditan el objetivo histórico de arranque inferior a 2 s.

## Capturas

| Archivo | Estado |
|---|---|
| [01-inicio.png](01-inicio.png) | Visitante, error de catálogo previo al servidor local |
| [02-login.png](02-login.png) | Login existente |
| [03-cliente-autenticado-local.png](03-cliente-autenticado-local.png) | Cliente con sesión local de prueba |
| [04-sesion-restaurada-local.png](04-sesion-restaurada-local.png) | Sesión restaurada tras reiniciar proceso |
| [05-cierre-sesion.png](05-cierre-sesion.png) | Retorno a visitante |
| [06-propietario-autenticado-local.png](06-propietario-autenticado-local.png) | Propietario con datos ficticios |
| [07-proveedor-autenticado-local.png](07-proveedor-autenticado-local.png) | Proveedor con datos ficticios |
| [08-muestra-misma-actividad.png](08-muestra-misma-actividad.png) | Muestra sin cuenta, sin launcher adicional |

## Límites y gates pendientes

- Sesión limitada a la vigencia del access token (máximo 15 minutos);
  no se almacena refresh token ni se inventa un endpoint de renovación.
- Logout local limpia sesión y workspace; no revoca tokens en el servidor.
- Hojas compartidas: cambios en memoria, sin sincronización, descartados al salir.
- Recuperación, favoritos y avisos requieren resolver los contratos/servicios
  pendientes; no se presenta la simulación como integración real.
- T014/T036, TalkBack manual, aceptación visual y regularización SDD siguen
  pendientes. No se afirma paridad demostrada con Figma.
- CI remoto (Gitleaks, dependencias, CodeQL y build) se consulta por el head
  del PR. No se acreditan esos gates con las pruebas locales.
- PR en borrador, revisión visual exclusiva de `ccabrera-dev8`, sin fusión.
  La asignación no equivale a aprobación y GitHub impide autoaprobación formal.

## UI-07 · Pulido visual tras observaciones del usuario

La captura del usuario identificó las cuatro secciones de IA Studio como
botones primarios repetidos y pidió mejorar separación e iconos.

- Estilo, Mesas, Personal e Ideas ahora son pestañas exclusivas con indicador,
  iconos y color distinto para la sección no seleccionada.
- La elección de concepto es un campo desplegable con flecha y opción
  seleccionada marcada visualmente y en semántica.
- Los enlaces de navegación de cliente y los grupos principales de gestión
  de propietario/proveedor usan filas neutras con flecha; las confirmaciones
  conservan la jerarquía de botón principal.
- Acciones de hojas con margen lateral de 20 dp, separación horizontal de
  12 dp y vertical de 8 dp; toolbar autenticada separada y desplazable.
- Iconos Material delineados en la barra de los tres roles y en IA Studio.
  Sin dependencias, APIs, permisos ni capacidades nuevas.

### Verificación de UI-07

- Pipeline completo final: **BUILD SUCCESSFUL in 1m22s**, incremental,
  383 tareas (7 ejecutadas, 376 actualizadas).
- **58 suites/322 pruebas, 0 fallos, errores u omitidas**.
- Cuatro regresiones nuevas: pestañas con rol semántico y selección exclusiva,
  desplegable y selección, pantalla compacta oscura/fuente 2.0 y separación
  entre enlaces que conservan la navegación.
- Fallos corregidos de pruebas: import inválido y búsqueda ambigua entre
  campo, tarjeta y opción del menú; se localiza la opción por texto y selección,
  sin retirar la aserción.
- La primera compilación incremental reportó duplicados tras la migración
  debug→main; recompilación no incremental correcta, sin cambiar fuentes
  ajenas. `NavigationTest` tuvo un fallo aislado al no hallar «Fiestas»;
  suite completa repetida en verde, causa no determinada.
- APK debug final: **25 217 319 bytes**, SHA-256
  `10ace40cbf1a7141f332375ee0ac908b583ceccfc77285e7509dccad8628e49c`.
  Instalado en `emulator-5554`; recorrido sin cuenta, pestañas y desplegable
  comprobados. Tema del emulador restaurado a claro.

| Captura del APK UI-07 | Estado |
|---|---|
| [09-studio-pestanas-claro.png](09-studio-pestanas-claro.png) | Pestañas, jerarquía, márgenes e iconos |
| [10-studio-selector-claro.png](10-studio-selector-claro.png) | Menú de conceptos abierto |
| [11-studio-pestanas-oscuro.png](11-studio-pestanas-oscuro.png) | Estilo en tema oscuro |
| [12-studio-personal-oscuro.png](12-studio-personal-oscuro.png) | Personal seleccionado, sin pila de botones |

La evidencia anterior de sesión corresponde al APK de consolidación, no al
hash de UI-07. Las capturas nuevas no certifican aceptación visual, TalkBack,
ni una auditoría exhaustiva de todas las pantallas. El usuario sigue siendo
el único responsable de aceptación visual. CI del nuevo head pendiente.
