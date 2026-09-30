# Feature: Arranque del proyecto

Estado: **hecho** · Repo: `jesusjbriceno/alborada` (público) · Licencia: MIT

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
- [x] Esqueleto Android: Gradle 8.11.1 + AGP 8.7.3 + Kotlin 2.1.0 + Compose BOM 2024.12.01
      (package `com.jesusjbriceno.alborada`, minSdk 26 / target 35)
- [x] README + LICENSE + .gitattributes + .gitignore
- [x] Repo público en GitHub y push inicial
- [x] Revisión nativa del candidato (RDD on): 1 hallazgo CRITICAL corregido y validado

## Evidencia de commits

| Commit | Mensaje | Nota |
|---|---|---|
| `d9d825c` | chore: bootstrap repository | gitignore + seguimiento ODD |
| `c6937ea` | feat: scaffold Android skeleton | esqueleto Kotlin + Compose |
| `0ca464a` | docs: add README and MIT license | |
| `231cdbd` | fix: mark gradlew executable | corrección revisada (R3-001) |

El commit `231cdbd` está **local, sin pushear** (push = decisión del usuario).

## Revisión nativa (lineage `review-1ffb3b37a01b26e5`)

- Tier medium · lente `review-reliability` · 24 archivos / 897 líneas · presupuesto 200.
- Hallazgo `R3-001` CRITICAL (determinístico, introduced): `gradlew` commitado como
  `100644` por `core.filemode=false` en Windows. Corregido con
  `git update-index --chmod=+x` + commit; validado por el validador dirigido;
  **aprobado y acknowledge quemado** (`authority: burned`).
- Lección: en Windows/NTFS, registrar el bit de ejecución requiere
  `git update-index --chmod=+x <archivo>` explícito.

## Decisiones abiertas (heredadas)

- Nombre definitivo visible: Alborada (candidato).
- Licencias de sonido: CC0/dominio público ya catalogado; CC-BY queda abierto.
- Toolchain local (JDK 17 + Android SDK) para compilar: pendiente, decisión del usuario.

## Siguiente paso

1. Toolchain: instalar Temurin JDK 17 y Android SDK cmdline-tools (o Android Studio).
2. Fase 1, tarea 1: alarma exacta + AlarmManager con días de la semana.