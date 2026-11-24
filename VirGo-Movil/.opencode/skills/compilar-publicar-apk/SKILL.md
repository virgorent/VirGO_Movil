---
name: compilar-publicar-apk
description: Compila, verifica, firma y publica el APK y el AAB de la app VirGo en GitHub Releases y sube el AAB a Google Play. Úsala para preparar o revisar un release; publicar requiere confirmación humana.
---

# Compilar y publicar el APK

> ⚠️ Publicar un release o cambiar los secretos de firma requiere **confirmación explícita** de la persona responsable (Art. XI.5).

## 1. Revisión previa

- [ ] CI en verde en `main`: ktlint, detekt, Lint y pruebas unitarias.
- [ ] `versionCode` incrementado y `versionName` en *SemVer* dentro de `app/build.gradle.kts`.
- [ ] `API_BASE_URL` de *release* apunta a la URL HTTPS vigente del backend (ver `memoria.md`).
- [ ] R8 activo (`isMinifyEnabled = true`) y `network_security_config` de *release* sin tráfico en claro.
- [ ] El keystore y sus contraseñas están solo en GitHub Secrets: `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` y `SIGNING_KEY_PASSWORD`.

## 2. Compilación local (opcional, sin publicar)

```powershell
./gradlew clean ktlintCheck detekt lint testDebugUnitTest pitest assembleDebug
```

## 3. Publicación (camino normal)

1. En *Actions → Release → Run workflow*, elige la rama `main` y escribe la versión `X.Y.Z` (Art. VIII.3). Una fusión a `main` **no** publica nada.
2. El workflow decodifica el keystore, ejecuta `assembleRelease bundleRelease`, verifica las firmas (`apksigner verify` y `keytool -printcert -jarfile`), crea la etiqueta `vX.Y.Z` y publica `app-release.apk` y `app-release.aab` en **GitHub Releases** con las notas y el commit.
3. La titular de la cuenta de Play sube ese `app-release.aab` a la pista que corresponda. Los pasos están en [`docs/PUBLICAR-PLAY.md`](../../../docs/PUBLICAR-PLAY.md).

## 4. Verificación

1. Descarga el APK del release e instálalo en un dispositivo o emulador.
2. Abre la pantalla de diagnóstico (en *debug*) o verifica que la app arranque y muestre el estado de la API.
3. **Prueba de humo:** la app abre sin errores y el diagnóstico llega a la API de producción por HTTPS.
4. Registra en `memoria.md` la versión, el commit, el enlace al release, el resultado y lo aprendido; llena la sección «Release» del `check.md`.

> **Google Play** (Art. VIII.4, v1.2.0): antes de subir, confirma que el certificado del AAB sea el de la llave de subida (SHA-256 en `memoria.md`) y que las declaraciones del panel y `docs/legal/` describan lo que hace la versión.

## 5. Reversión

Marca el release defectuoso como *pre-release*, publica el anterior como *latest* y registra el motivo en `memoria.md`.
