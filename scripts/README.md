# scripts/

Automatizaciones locales y de mantenimiento del monorepo.

- Los scripts no deben contener secretos: se leen de variables de entorno.
- Cada script debe documentar su propósito, uso y requisitos.
- Los scripts de CI/CD viven en `.github/workflows/`, no aquí.
- No ejecutar scripts descargados sin revisar su contenido.

## Scripts

| Script | Propósito | Uso |
|---|---|---|
| `humo-produccion.py` | Pruebas de humo del MVP contra una API desplegada. Modo `lectura` (predeterminado): health, catálogo y detalle sin sesión; no escribe nada. Modo `cuentas`: además cuenta, sesión y evento con invitado y pendiente (T3008, T704, T1403, T1604, T1704); borra las cuentas, pero el evento queda guardado. Modo `completo`: además salón, cotización, respuesta y reserva (T2208, T808, T2405, T908). | `python scripts/humo-produccion.py --base <url-de-la-api> [--modo lectura\|cuentas\|completo]`. `completo` deja un salón de prueba publicado (la API no permite borrarlo): úsalo solo con el visto bueno de quien administra la base. Requiere Python 3.10+, sin dependencias. |
