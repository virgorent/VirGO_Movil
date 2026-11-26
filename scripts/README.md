# scripts/

Automatizaciones locales y de mantenimiento del monorepo.

- Los scripts no deben contener secretos: se leen de variables de entorno.
- Cada script debe documentar su propósito, uso y requisitos.
- Los scripts de CI/CD viven en `.github/workflows/`, no aquí.
- No ejecutar scripts descargados sin revisar su contenido.

## Scripts

| Script | Propósito | Uso |
|---|---|---|
| `humo-produccion.py` | Pruebas de humo del MVP contra una API desplegada (T3008, T606, T704, T1403, T1604, T1704 y, con `--completo`, T2208, T808, T2405, T908). Usa cuentas de prueba generadas al vuelo y las borra al final. | `python scripts/humo-produccion.py --base <url-de-la-api>`. `--completo` deja un salón de prueba publicado (la API no permite borrarlo): úsalo solo con el visto bueno de quien administra la base. Requiere Python 3.10+, sin dependencias. |
