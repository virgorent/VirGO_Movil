# Fuentes empaquetadas

La aplicación incluye Manrope (títulos) y DM Sans (cuerpo y controles) sin
descargas en tiempo de ejecución. Los archivos variables originales no se
modificaron; sus pesos se seleccionan mediante el eje `wght`.

## Origen y trazabilidad

- Repositorio: [google/fonts](https://github.com/google/fonts).
- Revisión fijada: `0f3edfe222bdb0b49ce0c631d677a83542ad6d5a`.
- Fecha de incorporación: 2026-11-20.
- Licencia: SIL Open Font License 1.1; copia y avisos en
  `core/designsystem/src/main/assets/licenses/fonts-ofl.txt`, incluidos en el APK.
- Manrope conserva avisos de 2018 en su licencia y de 2019 en metadatos de la
  distribución; se incluyen ambos, sin asumir que son versiones idénticas del aviso.

| Recurso local | Ruta original en la revisión | SHA-256 |
|---|---|---|
| `res/font/manrope_variable.ttf` | `ofl/manrope/Manrope[wght].ttf` | `3ae11c49db0455a3cc33e37d380f20fdb8c7f8b41dc07625c177e3d87a9d6ae6` |
| `res/font/dm_sans_variable.ttf` | `ofl/dmsans/DMSans[opsz,wght].ttf` | `8cd08d97e89c24d0aa92edd2f0f4c8ee6195eee9b7c9f154865a58b02f0c1c0d` |

Los recursos locales pertenecen a `core/designsystem/src/main/`.
Los archivos `METADATA.pb` y `OFL.txt` de cada familia en la revisión indicada
identifican origen y licencia. No se copió `node_modules`, fotografías ni código
del prototipo; sus derechos se revisan por separado.
