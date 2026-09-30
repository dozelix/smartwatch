# Smartwatch IoT · Clase 8 (Android nativo)

App de teléfono con estética de reloj — espejo nativo de la versión web (`T:\smartwatch\index.html`).
Kotlin + Views + Material. `minSdk 26`, `targetSdk/compileSdk 35`, Java 17.

## Funciones (igual que la clase)
| Pantalla | Permiso / API |
|---|---|
| Reloj | Hora viva cada 1 s + fecha ES + avatar de sesión |
| Clima | `ACCESS_FINE_LOCATION` + FusedLocation + Open-Meteo (sin key); fallback ciudad manual |
| Alarma | `POST_NOTIFICATIONS` pedido solo al programar + `AlarmManager.setAndAllowWhileIdle` + beep + vibración |
| Pasos | `TYPE_STEP_COUNTER` si existe, si no simulación; meta 10.000 |
| IoT | Telemetría ESP32 simulada; hook Firestore comentado en `IotFragment.kt` para C09 |
| Cuenta | Firebase Auth real si hay backend, si no demo local (`SharedPreferences`) |

## Abrir en Android Studio
1. **Open** → `T:\smartwatch-android` (NO `T:\smartwatch`, esa es la versión web).
2. Deja que sincronice Gradle (descarga dependencias, ~5 min la primera vez).
3. **Run ▶** en un emulador (Pixel, API 35) o teléfono con depuración USB.

## Firebase real (opcional, para la nota máxima)
1. `console.firebase.google.com` → crea proyecto → agrega app Android con package `cl.inacap.smartwatch`.
2. Activa **Authentication → Email/Password**.
3. Descarga `google-services.json` en `app/` y en `build.gradle.kts` raíz agrega el plugin
   `id("com.google.gms.google-services") version "4.4.2" apply false`, más
   `id("com.google.gms.google-services")` en `app/build.gradle.kts`.
4. Sin ese archivo la app usa demo local y avisa en pantalla (no se rompe).

## Checklist auditor (Clase 8)
1. El segundero corre en vivo y cambia el minuto exacto.
2. Clima con GPS aceptado muestra ciudad; con GPS bloqueado permite escribirla.
3. Registro/login muestra email en Reloj (iniciales en el avatar).
4. Alarma a 10 s notifica aunque la app esté en segundo plano.

## Compilar por línea de comandos
```
cd T:\smartwatch-android
gradle wrapper --gradle-version 8.9   # una sola vez (usa el Gradle descargado en setup)
.\gradlew assembleDebug
# APK: app\build\outputs\apk\debug\app-debug.apk
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
