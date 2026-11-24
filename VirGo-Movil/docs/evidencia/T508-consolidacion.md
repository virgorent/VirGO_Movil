# T508 — Verificación automática de la consolidación de Explorar

| Campo | Evidencia |
|---|---|
| Fecha | 2026-11-19 |
| Código verificado | Commit `5d14843` en `feat/005-explorar-salones` |
| PR | [#27](https://github.com/virgorent/VirGO_Movil/pull/27) |
| Resultado local | `BUILD SUCCESSFUL in 5m 26s` |
| Suites | 38 |
| Pruebas | 199; 0 fallos, 0 errores, 0 omitidas |
| UI de Explorar | `ExploreScreenTest`: 12 pruebas, 0 fallos/errores |
| Pitest dominio | 60 %: 57/95 mutaciones detectadas; test strength 93 % |
| Pitest common | 33 %: 1/3 mutaciones detectadas; test strength 100 % |
| APK debug | 23 679 340 bytes; compilado, no publicado como release |
| SHA-256 APK | `62792804d9e5dc93f8ef69eb8712aba258c25a5906324f06f44feb31ae233a61` |

## Ejecución

Desde `VirGo-Movil/`, con JDK de compilación 17:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\jdk-17.0.21+9"
.\gradlew.bat --init-script "$env:TEMP\opencode\virgo-isolated-build.gradle" ktlintCheck detekt lint testDebugUnitTest :core:domain:test :core:common:test pitest assembleDebug
```

El init script local redirige la salida de cada proyecto a
`$env:TEMP\opencode\virgo-build`, evitando el bloqueo de `R.jar` en la carpeta
habitual. No se compilaron proyectos simultáneamente ni se detuvieron otras
ejecuciones para esta verificación.

## Reportes comprobados

En esa salida aislada:

- `**/test-results/**/TEST-*.xml`: 38 reportes, sumados por atributos de suite.
- `app/test-results/testDebugUnitTest/TEST-mx.virgo.app.ui.explore.ExploreScreenTest.xml`.
- `core/domain/reports/pitest/index.html`.
- `core/common/reports/pitest/index.html`.
- `app/reports/lint-results-debug.html`.
- `app/outputs/apk/debug/app-debug.apk`.

Se verificaron con pruebas el inicio en lista, la selección lista → mapa → lista,
los créditos desde lista/mapa/recomendaciones, el mensaje seguro de error de API
y el reintento después de desconexión.

## Límites de esta evidencia

- No sustituye una revisión visual en dispositivo. T509 y las capturas nuevas
  siguen pendientes; no se certifican oscuro, pantalla pequeña, teclado ni Atrás
  del último rediseño con esta ejecución.
- No es humo de producción ni verifica disponibilidad de favoritos/avisos.
- Robolectric no equivale a una comprobación nueva del Keystore real.
- Los checks remotos del head definitivo se consultan en #27. Los resultados
  anteriores a este push no cubren estos cambios.
- Existen advertencias de APIs de pruebas Compose obsoletas y de compatibilidad
  futura con Gradle 10; no bloquearon el pipeline actual.
