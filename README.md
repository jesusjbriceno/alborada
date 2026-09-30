# Alborada 🌅

App de alarma para Android con **amanecer simulado**, música propia **sin
conexión** y **sonidos naturales**. Despertar suave, sin publicidad y sin
versión de pago.

> Referencia: «Sunrise Alarm: Wake-Up Light» (la app que usábamos), con dos
> diferencias: alarmas múltiples y música propia son parte de la app desde el
> principio.

## Estado

Esqueleto del proyecto Android creado (Kotlin + Jetpack Compose). La fase 1
(MVP: alarma exacta + amanecer + crescendo) está por empezar.

| Fase | Contenido | Estado |
|---|---|---|
| 1. MVP | Alarma exacta por días + amanecer + crescendo | Pendiente |
| 2. Contenido | Galería de música, tonos del sistema, sonidos naturales | Pendiente |
| 3. Modo noche | Fondos, estilos de reloj, protección de pantalla | Pendiente |
| 4. Publicación | Pruebas en móviles reales, APK | Pendiente |

La distribución es una **APK instalada directamente** en el dispositivo; no se
publica en Play Store.

## Stack

- **Nativo Android**: Kotlin + Jetpack Compose (Material 3)
- Gradle 8.11.1 (wrapper incluido) · AGP 8.7.3 · Kotlin 2.1.0 · Compose BOM 2024.12.01
- `minSdk 26` · `targetSdk 35` · `compileSdk 35`
- `applicationId`: `com.jesusjbriceno.alborada`

## Requisitos de requisitos

La documentación de origen vive en Obsidian:
`D:\_nextcloud\_obsidian\Desarrollo\03_Projects\despertador`
(`docs/analisis-y-requisitos.md`, `TAREAS.md`, `resources/audio/CATALOGO.md`).
El catálogo de audio (54 MB, 8 pistas en dominio público / CC0, normalizadas a
−18 LUFS) se importará al proyecto en la fase 2.

## Cómo construir

Necesitas JDK 17+ y Android SDK. Con `local.properties` apuntando al SDK (o la
variable `ANDROID_HOME`):

```bash
# APK de depuración
./gradlew :app:assembleDebug
# APK en ./app/build/outputs/apk/debug/app-debug.apk

# Tests
./gradlew :app:testDebugUnitTest   # unitarios
./gradlew :app:connectedDebugAndroidTest  # instrumentados, con dispositivo
```

## Estructura

```
app/src/main/java/com/jesusjbriceno/alborada/
├── MainActivity.kt          # Punto de entrada + pantalla inicial
└── ui/theme/                # Tema Alborada (paleta de amanecer, claro/oscuro)
app/src/main/res/            # Recursos: strings, tema, icono adaptable
gradle/libs.versions.toml    # Catálogo de versiones
odd/tasks/                   # Seguimiento del proyecto (ODD)
```

## Licencia

MIT — ver `LICENSE`. Los sonidos incluidos en el futuro serán de dominio
público / CC0 (política de licencias en el catálogo de audio).