# Publicar VirGo en Google Play

Guía para subir una versión de la app Android de VirGo (`mx.virgo.app`) a Google Play: desde clonar el repositorio hasta dejarla disponible para los *testers*.

> Se adaptó de la guía de otro proyecto (Nexo Local, en Flutter). VirGo es **Android nativo con Kotlin y Gradle** y su backend está en **AWS**, así que los comandos son distintos.

## Antes de empezar

- **La llave de firma no se cambia.** Después de la primera versión en Play, todas las actualizaciones se firman con la misma llave de subida. Si se pierde, hay que pedirle a Google que la reinicie, y eso tarda días. Respáldala en cuanto la generes.
- **El `versionCode` nunca se repite ni baja.** Play rechaza el bundle sin más explicación.
- **Google Play exige AAB, no APK.**
- **Para la prueba interna ya no hay bloqueos de código.** Para la prueba cerrada y producción faltan B4 a B6 (sección 1).

## 1. Bloqueos actuales

| # | Bloqueo | Responsable | Qué falta |
|---|---|---|---|
| B1 | ~~El workflow genera APK, no AAB~~ | Móvil | ✅ Resuelto en el PR #10: `movil-release.yml` genera APK y AAB, verifica las firmas y publica los dos |
| B2 | ~~La app no tiene icono~~ | Móvil | ✅ Resuelto en el PR #10: icono adaptativo provisional ("V" blanca sobre morado) y `docs/play/icon-512.png`. Cambiarlo por el logo final antes de producción |
| B3 | ~~`allowBackup` no está declarado~~ | Móvil | ✅ Resuelto en el PR #10: `allowBackup="false"` y `dataExtractionRules` que excluyen el DataStore y el *keyset* del token |
| B4 | No hay aviso de privacidad ni página de eliminación de cuenta | Equipo | Publicarlos con GitHub Pages (sección 6) |
| B5 | Eliminar la cuenta está bloqueado (`018 DELETE /users/me`) | Equipo y API | Google exige que una app con registro permita borrar la cuenta desde la app y desde una URL |
| B6 | No hay cuenta de prueba en producción para el revisor de Google | Base de datos | Usuario de prueba real en la base `virgo` (la cuenta `demo@virgo.mx` solo existe en local) |

## 2. Qué necesitas instalado

| Herramienta | Para qué | Cómo comprobar |
|---|---|---|
| JDK 17 | Gradle y `keytool` | `java -version` |
| Android SDK (API 36 y *build-tools*) | Compilar | Android Studio → SDK Manager |
| Git | Bajar el código | `git --version` |

Gradle se descarga solo con el *wrapper* (`gradlew.bat`).

## 3. Bajar el código y comprobar que compila

```powershell
git clone https://github.com/virgorent/VirGO_Movil.git
cd VirGO_Movil\VirGo-Movil
.\gradlew.bat ktlintCheck detekt lint testDebugUnitTest
```

Si algo falla, no publiques: arréglalo primero. Es lo mismo que revisa `movil-ci` en cada PR.

## 4. Crear la llave de firma (solo la primera vez)

Si ya existe la llave del proyecto, pídesela a quien la tenga y salta a la sección 5.

```powershell
keytool -genkey -v -keystore $HOME\virgo-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Pide, en orden: la contraseña del almacén, el nombre y apellidos, la unidad organizativa, la organización, la ciudad, el estado, el código de país (`MX`) y una confirmación. Si el programa está en español, la confirmación se responde con `si`.

- Guarda el `.jks` y las dos contraseñas en un gestor de contraseñas del equipo, **fuera del repositorio**.
- Nunca los mandes por chat ni los subas a git. Gitleaks bloquea el PR si lo intentas.

## 5. Generar el AAB firmado

### 5.1 Versión

En `VirGo-Movil/app/build.gradle.kts`:

```kotlin
versionCode = 3          // siempre mayor que el último subido a Play (0.2.0 usó el 2)
versionName = "0.3.0"    // el que ve la gente
```

Registra la versión en `memoria.md` con la fecha y lo que cambió.

### 5.2 A mano, desde tu equipo

La firma y la URL de la API se pasan como **variables de entorno**, no en un archivo. Así nunca quedan escritas en el proyecto:

```powershell
cd VirGo-Movil
$env:SIGNING_KEYSTORE_FILE = "$HOME\virgo-release.jks"
$env:SIGNING_KEY_ALIAS = "upload"
$env:SIGNING_STORE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR((Read-Host "Contraseña del almacén" -AsSecureString)))
$env:SIGNING_KEY_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR((Read-Host "Contraseña de la llave" -AsSecureString)))
$env:VIRGO_API_BASE_URL = "https://rcof55a3c9.execute-api.us-east-1.amazonaws.com"
.\gradlew.bat :app:bundleRelease
```

- Si falta alguna variable de firma, el build se detiene a propósito: release **nunca** se firma con la llave de depuración (T026).
- Si `VIRGO_API_BASE_URL` no empieza con `https://`, el build también se detiene (ADR-08).
- El resultado queda en `app\build\outputs\bundle\release\app-release.aab`.

Al terminar, cierra la terminal para que las contraseñas no queden en memoria.

### 5.3 Con el pipeline (recomendado)

`movil-release.yml` se lanza con **Run workflow** desde `main`. Necesita, en **Settings → Secrets and variables → Actions** del repositorio:

| Nombre | Tipo | Valor |
|---|---|---|
| `SIGNING_KEYSTORE_BASE64` | Secret | El `.jks` en Base64 (ver abajo) |
| `SIGNING_KEY_ALIAS` | Secret | `upload` |
| `SIGNING_KEY_PASSWORD` | Secret | Contraseña de la llave |
| `SIGNING_STORE_PASSWORD` | Secret | Contraseña del almacén |
| `VIRGO_API_BASE_URL` | Variable | `https://rcof55a3c9.execute-api.us-east-1.amazonaws.com` |

Para el Base64 usa PowerShell, no `certutil`: `certutil` mete saltos de línea y el CI no puede leer el archivo.

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$HOME\virgo-release.jks")) | Set-Clipboard
```

### 5.4 Comprobar el AAB

No te fíes de que el build terminó:

```powershell
keytool -printcert -jarfile app\build\outputs\bundle\release\app-release.aab
```

Debe mostrar el propietario de tu llave, **no** `CN=Android Debug`.

```powershell
Get-ChildItem app\build\intermediates -Recurse -Filter AndroidManifest.xml |
  Where-Object FullName -like '*release*' |
  Select-String -Pattern 'package=|minSdkVersion|targetSdkVersion|allowBackup|android:icon'
```

Debe decir `package="mx.virgo.app"`, `minSdkVersion="26"`, `targetSdkVersion="36"`, `allowBackup="false"` y un `android:icon`.

### 5.5 Probar en un teléfono antes de subir

Con el teléfono conectado por USB y la depuración USB activa, y las mismas variables de la sección 5.2:

```powershell
.\gradlew.bat :app:installRelease
```

Comprueba al menos que inicia sesión, que el catálogo de salones carga, que una reservación se crea y que el icono es el de VirGo.

## 6. Avisos legales

Google exige dos URL públicas: la **política de privacidad** y la de **eliminación de cuenta**. Se proponen en GitHub Pages del repositorio `VirGO_Movil`:

| Documento | Archivo | URL (cuando se publique) |
|---|---|---|
| Aviso de privacidad | `docs/legal/privacidad/index.md` | `https://virgorent.github.io/VirGO_Movil/legal/privacidad/` |
| Términos de uso | `docs/legal/terminos/index.md` | `https://virgorent.github.io/VirGO_Movil/legal/terminos/` |
| Eliminar cuenta | `docs/legal/eliminar-cuenta/index.md` | `https://virgorent.github.io/VirGO_Movil/legal/eliminar-cuenta/` |

El aviso de privacidad debe cumplir con la LFPDPPP (artículos 15 a 17) y describir **lo que la app hace de verdad**, incluido lo de la sección 8. Declarar algo que la app no hace, o callar algo que sí hace, es lo que se sanciona.

## 7. Play Console

### 7.1 La primera vez

1. Crear la cuenta de desarrollador: pago único de **$25 USD** y verificación de identidad.
2. **Crear aplicación:** nombre `VirGo`, idioma español (México), tipo *Aplicación*, **Sin coste**. Esta última opción es irreversible.
3. Aceptar las declaraciones: políticas, firma de apps de Play y leyes de exportación de EE. UU.

> **Cuentas personales nuevas:** Google pide una **prueba cerrada con al menos 12 *testers* durante 14 días seguidos** antes de dejar pasar a producción. Confirma la regla vigente en Play Console al crear la cuenta y planea el calendario con ese plazo.

### 7.2 Subir la versión

**Prueba interna → Crear una versión:**

1. Acepta la **firma de apps de Google Play**. Tu `.jks` queda como llave de subida y Google firma la app con una propia.
2. Arrastra `app-release.aab`.
3. Nombre de la versión: el `versionName` y el código, por ejemplo `0.2.0 (2)`.
4. Notas de la versión en español, con lo que cambió. Si hay pantallas con datos de ejemplo, dilo.
5. Pestaña **Verificadores:** una lista con los correos de Gmail del equipo.
6. **Revisar versión → Iniciar lanzamiento.**

Play da un enlace de aceptación. Cada persona lo abre, acepta y ya puede instalar desde Play.

## 8. Declaraciones del panel

La prueba interna no las exige; la prueba cerrada y producción sí. Estas son las respuestas que corresponden a VirGo **según los datos que guarda hoy** (`VirGo-API/db/schema` en el backend). Si cambia lo que la app trata, se actualizan aquí y en el aviso de privacidad.

| Declaración | Respuesta |
|---|---|
| Política de privacidad | URL de `privacidad/` |
| Acceso a la app | Sí, hay partes restringidas. Dar una **cuenta de prueba real en producción** (B6), con las instrucciones en inglés |
| Anuncios | No contiene anuncios |
| Audiencia objetivo | Solo mayores de 18 (contrataciones y pagos) |
| Aplicaciones gubernamentales | No |
| Funciones financieras | «No proporciona funciones financieras»: VirGo cobra servicios de eventos, no da créditos, inversiones ni cuentas |
| Salud | No |
| Categoría de la tienda | Eventos |

### 8.1 Clasificación de contenido

Todo en **No**, salvo:

- Los usuarios interactúan o intercambian contenido → **Sí** (chat, reseñas y cotizaciones).
- La app permite compras de bienes o servicios del mundo real → **Sí** (reservaciones de salones y proveedores).

### 8.2 Seguridad de los datos

- **Cifrado en tránsito:** Sí (solo HTTPS; la app rechaza HTTP).
- **Creación de cuenta:** correo y contraseña.
- **URL de eliminación de cuenta:** la de `eliminar-cuenta/` (requiere B5).
- **Se comparten:** ninguno. AWS, MongoDB Atlas y la pasarela de pago tratan datos **por cuenta de VirGo** (encargados), y eso está exento.

| Tipo de dato (Play) | De dónde sale en VirGo | ¿Obligatorio? | Para qué |
|---|---|---|---|
| Nombre | `users.name` | Obligatorio | Funcionalidad, gestión de la cuenta |
| Correo electrónico | `users.email` | Obligatorio | Funcionalidad, gestión de la cuenta, prevención de fraude |
| Número de teléfono | `users.phone` | Opcional | Funcionalidad |
| Dirección | `users.city` | Opcional | Funcionalidad |
| **Información de pago** | `paymentCards` (marca, últimos 4 dígitos, vencimiento y *token* de la pasarela; **nunca** el número completo) | Opcional | Funcionalidad (pagar reservaciones) |
| Historial de compras | `bookings` y pagos | Opcional | Funcionalidad |
| Mensajes en la app | `messages`, `contactMessages` y `assistantMessages` | Opcional | Funcionalidad, asistencia |
| Fotos | Galerías de salones y proveedores | Opcional | Funcionalidad |
| Otro contenido generado por el usuario | Lista de invitados (`eventGuests`: **nombre y teléfono de terceros**), reseñas y eventos | Opcional | Funcionalidad |
| IDs de dispositivo u otros | Nombre del dispositivo en `sessions.device` | Opcional | Seguridad de la cuenta |

**No marques:** ubicación (la app no pide permiso de ubicación), contactos (los invitados se escriben a mano; la app no lee la agenda), salud ni archivos de audio.

> Si se activa el asistente con Amazon Bedrock (`906-ia-bedrock`), los mensajes al asistente se envían a un modelo de IA en AWS. Hay que decirlo en el aviso de privacidad antes de activarlo.

## 9. Ficha de la tienda

| Recurso | Requisito |
|---|---|
| Icono | 512 × 512 PNG |
| Gráfico destacado | 1024 × 500 PNG o JPG |
| Capturas de teléfono | Mínimo 2; sube 4 o más de 1080 px o más por lado |
| Descripción breve | Hasta 80 caracteres |
| Descripción completa | Hasta 4000 caracteres |

Capturas sugeridas: el catálogo de salones con resultados, el detalle de un salón, el flujo de cotización y la agenda del propietario. **Sin pantallas vacías ni datos reales de nadie.**

## 10. Problemas comunes

| Síntoma | Causa |
|---|---|
| «Debes subir un Android App Bundle» | Se subió el APK; usa `bundleRelease` (B1) |
| «Ya se ha usado el código de versión» | `versionCode` repetido |
| «El APK o AAB está firmado en modo de depuración» | Faltan las variables `SIGNING_*` |
| El build dice que falta la firma de release | Es intencional (T026): define las cuatro variables `SIGNING_*` |
| `VIRGO_API_BASE_URL de release debe empezar con https://` | Falta la variable o no es HTTPS |
| El CI no lee la llave | El Base64 se generó con `certutil`; usa `[Convert]::ToBase64String` |
| La app abre pero no carga nada | El AAB se generó sin `VIRGO_API_BASE_URL`, o la base `virgo` no tiene datos |
| Google rechaza por no poder acceder | La cuenta de prueba no existe en producción (B6) |
| Google rechaza por falta de eliminación de cuenta | Falta B5 o la URL de `eliminar-cuenta/` |

## 11. Antes de prometer nada

- La API en AWS (Lambda) está encendida siempre. No hay que preocuparse de apagarla durante la revisión, pero sí de que **la base `virgo` tenga datos** y de que la cuenta de prueba funcione.
- Hoy solo hay 7 endpoints implementados (registro, inicio y cierre de sesión, perfil, catálogo, cotizaciones y reservaciones). Las pantallas que dependan de endpoints pendientes deben ocultarse o avisar claramente, nunca mostrar datos inventados como si fueran reales.
- El asistente de IA está apagado (`906`). Si la app tiene la pantalla, debe decir que no está disponible.
