# Feature: Arranque del proyecto

Estado: **en curso** · Repo: `jesusjbriceno/alborada` (público) · Licencia: MIT

## Contexto

- Proyecto Android nativo (Kotlin + Jetpack Compose): app de alarma «Alborada» con
  amanecer simulado, música propia sin conexión y sonidos naturales.
- Fuente de requisitos: `D:\_nextcloud\_obsidian\Desarrollo\03_Projects\despertador`
  (docs/analisis-y-requisitos.md, TAREAS.md, resources/audio/CATALOGO.md).
- Distribución: APK instalada directamente en el dispositivo, sin Play Store.
- Decisión de hoy: nombre **Alborada** (sin colisión en el nicho del store, verificado),
  repo público **alborada**, licencia **MIT**.

## Tareas

- [x] Decidir nombre y licencia (Alborada / MIT)
- [ ] Esqueleto Android: Gradle 8.11.1 + AGP 8.7.3 + Kotlin 2.1.0 + Compose BOM 2024.12.01
      (package `com.jesusjbriceno.alborada`, minSdk 26 / target 35) — delegado a gentle-ai-worker
- [ ] README + LICENSE + .gitignore
- [ ] Repo público en GitHub (`gh repo create alborada --public`) y primero(s) commit(s)
- [ ] Toolchain local (JDK 17 + Android SDK) para compilar — pendiente, decisión del usuario

## Decisiones abiertas (heredadas)

- Nombre definitivo visible: Alborada (candidato).
- Licencias de sonido: CC0/dominio público ya catalogado; CC-BY queda abierto.