# Traspaso del proyecto a Karasu

**Fecha:** 2026-11-24. Estado consultado en GitHub; volver a comprobar antes de
fusionar. Este paquete es documental, no una autorización para implementar.

## 1. Punto de entrada

1. Revisar [la auditoría móvil–backend](auditoria-integracion-backend.md).
2. Revisar el [spec 030 Cuenta y sesión](../../specs/030-cuenta-sesion/spec.md),
   su [plan](../../specs/030-cuenta-sesion/plan.md),
   [tareas](../../specs/030-cuenta-sesion/task.md) y
   [check](../../specs/030-cuenta-sesion/check.md).
3. Continuar sobre el código del PR #32, no confundir el estado de `main` con
   el de la rama auditada.

## 2. Código y fusión pendiente

| Elemento | Estado al preparar el traspaso |
|---|---|
| Repositorio móvil | `virgorent/VirGO_Movil` |
| PR de unificación | [#32](https://github.com/virgorent/VirGO_Movil/pull/32) |
| Rama auditada | `feat/033-app-unificada` |
| Commit auditado | `4ab3131c446fff19624e211f294345dc129eefc1` |
| Base del PR | `main` |
| Revisión solicitada | `Karasu0001` |
| Estado | Abierto, no borrador; `REVIEW_REQUIRED`, sin aprobaciones |
| Checks | `gitleaks`, `dependency review`, `movil-build-test`, `analyze` y CodeQL verdes |

El usuario autorizó fusionar #32, pero falta la revisión exigida por el
repositorio. No saltar el ruleset ni autoaprobar. Este PR de documentación nace
de `origin/main` y no añade commits al PR #32.

## 3. Qué se entrega y qué no

- App unificada: registro y login existentes, sesión cifrada y navegación por
  el rol de la cuenta; las funciones sin servicio siguen usando muestras.
- Auditoría de 55 destinos y 15 hojas, con contratos disponibles y brechas.
- Propuesta `030` en **borrador**: perfil real y logout remoto. Sus tareas no se
  han ejecutado y no hay pruebas ni aceptación del módulo.
- El controlador del backend devuelve `204` en logout; el OpenAPI anuncia
  `200`. No se probó un logout autenticado en esta preparación.
- No se entrega una release ni una nueva validación funcional contra producción.

La auditoría y `030` describen el contexto de #32 (constitución móvil v1.2.0,
base 38/40). El `main` desde el que nace este paquete conserva documentación
anterior (v1.1.1 y 35/40). Reconciliar tras integrar los PRs; no reemplazar
bitácoras ni dar tareas por cerradas por ese motivo.

## 4. Secuencia recomendada

1. Revisar e integrar #32 con aprobación y checks requeridos.
2. Incorporar este paquete de documentación y resolver los posibles conflictos
   de `memoria.md` conservando ambas entradas.
3. Revisar `030` con el responsable humano: aprobar spec y plan por separado
   de la aprobación del PR documental.
4. Resolver el cierre de `000` (T014/T036 en la rama auditada) o conseguir una
   excepción explícita para integración funcional. La excepción visual anterior
   no autoriza automáticamente este flujo.
5. Refinar las tareas y ejecutar una a la vez: red → dominio/datos → sesión →
   perfil → pruebas → humo autorizado.
6. Tratar después el catálogo real y el detalle de salón; no conectar pagos,
   reservas, IA ni mensajes como si ya estuvieran operativos.

### Preguntas que quedan antes de aprobar `030`

- Delimitar su alcance frente a `003` y `021`, sin duplicar requisitos.
- Confirmar contrato y JSON autenticado de `/users/me`; alinear OpenAPI.
- Validar `id` de perfil además del rol y descartar respuestas tardías de una
  sesión anterior tras logout o cambio de cuenta.
- Garantizar limpieza local aun ante cancelación, timeout o fallo de disco;
  no permitir doble logout ni peticiones que usen la identidad equivocada.
- Definir cuándo un 401 de otras llamadas protegidas invalida la sesión:
  el borrador solo detalla `/users/me`.
- Dividir tareas grandes, completar trazabilidad CA-10 y RNF, y comprobar la
  ubicación de validaciones para que las pruebas de mutación puedan ejecutarlas.
- No considerar que aceptar `200` prueba revocación: verificar que el token
  anterior reciba 401 en un humo autorizado.

## 5. Restricciones que se conservan

- Referencia visual autorizada: `C:\Users\cesar\OneDrive\Desktop\VirGo Event Management App`.
  No afirmar paridad con Figma.
- Aceptación visual exclusiva de `ccabrera-dev8`; no pedir otros revisores
  humanos para lo visual. Revisión técnica y aceptación visual son distintas.
- Mantener registro, login y APIs reales. Muestras claramente identificadas;
  sin credenciales ni red en el recorrido de muestra.
- No publicar release, subir a Google Play, cambiar secretos ni desplegar
  backend sin autorización nueva.
- No reutilizar identificadores de muestra como recursos reales.
- Credenciales de pruebas solo por un canal seguro; nunca en Git, logs,
  comentarios, capturas o archivos de traspaso.

## 6. Comprobación inicial sugerida

```powershell
gh pr view 32 --json state,headRefOid,reviewDecision,mergeStateStatus
gh pr checks 32
git fetch origin
```

Tras la fusión y las aprobaciones, consultar el pipeline del plan `030`.
El paquete no ejecuta tareas funcionales ni cierra módulos.
