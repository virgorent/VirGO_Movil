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
