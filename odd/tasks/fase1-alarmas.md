# Feature: Fase 1 — Núcleo de alarmas

Estado: **en curso** (unidad amanecer+crescendo hecha) · Rama: master

## Contexto

Fase 1 (MVP) del análisis: alarma exacta por días + amanecer + crescendo.
Esta unidad cubre el **núcleo de alarmas** (tareas TAREAS.md #2 y #5):
permiso de alarma exacta, AlarmManager con días de la semana, varias alarmas
con persistencia y UI de gestión. El amanecer simulado y el crescendo quedan
como unidad siguiente (tareas #3 y #4).

Requisitos relevantes del análisis:

- AlarmManager con alarma exacta y rearmado tras reinicio (BootReceiver).
- Permiso SCHEDULE_EXACT_ALARM (Android 12+) y USE_EXACT_ALARM (declarados en el manifest).
- POST_NOTIFICATIONS con pedido en runtime (13+).
- Varias alarmas, cada una con sus días de la semana.
- Sin permisos innecesarios, sin datos que salgan del móvil.

## Decisiones de diseño

- **Persistencia**: Room (KSP), `daysBitmask: Int` (bit 0=DOMINGO … bit 6=SÁBADO,
  `Calendar.SUNDAY..SATURDAY`), conversión a `Set<Int>` en el dominio.
- **Programación**: `AlarmManager.setExactAndAllowWhileIdle` + `PendingIntent`
  al `AlarmReceiver` con el id de la alarma; reprogramación de la próxima
  ocurrencia al disparar, al editar y tras boot.
- **Próxima ocurrencia**: `NextAlarmCalculator` pura (fecha/hora + días),
  testeada con JUnit (es el único reloj del sistema en el proyecto).
- **UI**: pantalla única (lista + FAB + diálogo crear/editar con TimePicker de
  Material3 y chips de días + switch por alarma). Sin navegación aún.
- **Permisos runtime**: pedido de POST_NOTIFICATIONS al arrancar; si falta
  SCHEDULE_EXACT_ALARM, aviso con enlace a la ficha de la app en ajustes.

## Tareas

- [x] Fundaciones de datos: Room + KSP en el catálogo, entidad/DAO/DB/repositorio
- [x] `NextAlarmCalculator` + tests unitarios (10/10 verdes)
- [x] Scheduler: `AlarmScheduler` + implementación AlarmManager exacto
- [x] Receivers: `AlarmReceiver` (notificación + reprograma) y `BootReceiver` (rearmar)
- [x] `AlarmListViewModel` + UI de lista/creación/edición
- [x] Permisos runtime y actualización de MainActivity
- [x] Build de verificación + análisis LSP (0 hallazgos) + commit por unidad
- [ ] Revisión nativa (bloqueada en selección de base_ref para C1, C2 y C3; pendiente de reintento)

## Evidencia

| Commit | Mensaje | Checks |
|---|---|---|
| `98fcd0d` | dominio/room + calculador | tests 10/10 · build OK |
| `fdfb69d` | scheduler + receivers | build OK |
| `8863374` | UI + permisos | build OK · LSP 0 hallazgos |

Checks: `:app:testDebugUnitTest` verde (10 tests), `:app:assembleDebug` OK,
LSP source=lsp 0 findings. Review nativa intentada para C1 (3×) y C3 (1×):
el controlador se queda en `empty_candidate_base_ref_required` con
`baseRef` = commit padre válido y `committedOnly:true`; mismo stop que el
bootstrap inicial. Tras revisar por paquete este estado, lo trato como
bloqueo de tooling del controlador en este repo (no como fallo de código):
los checks funcionales (tests + build + LSP) quedan como evidencia y la
revisión como pendiente de reintento (posible `gentle-ai` repair o
consentimiento explícito). Las alarmas además se probarán en dispositivo
(tarea #6 de TAREAS.md).

## Unidad 2 — Amanecer simulado + crescendo (hecha · tareas #3 y #4)

- `Alarm.anticipationMinutes` (por defecto 15, slider 0..60 en pasos de 5) con
  migración Room v1→v2 (ALTER TABLE, sin pérdida de alarmas).
- El scheduler dispara **al inicio del amanecer** (`alarmStart − anticipation`)
  con `setExactAndAllowWhileIdle` y pasa la hora real de la alarma en extras.
- `AlarmActivity` full-screen (showWhenLocked + turnScreenOn + KEEP_SCREEN_ON):
  colores de fases + brillo de ventana vía `SunriseRenderer` (puro, 5 tests),
  tono en crescendo con Media3 (volumen smoothstep 0→1, loop) y estado de
  alarma con botón Detener.
- Tono incluido: `res/raw/sunrise_tone.ogg` = `brisa-y-pajaros.ogg`
  (dominio público, Wikimedia Commons, 413 KB — fuente: CATALOGO.md).
- `AlarmNotifier` eliminado: la actividad ES la alarma (el servicio en primer
  plano queda como endurecimiento para la semana de prueba en el móvil).

| Commit | Mensaje | Checks |
|---|---|---|
| `b799101` | amanecer + crescendo | tests 16/16 · build OK · LSP 0 |
| `54ff063` | fix UI: chips en FlowRow, semana lunes-domingo, editor con scroll | tests 16/16 · build OK |

### Feedback de usuario (capturas emulador)

- Días cortados en el diálogo → `FlowRow` (wrap en 2 líneas).
- La semana arranca en **lunes** (L M X J V S D); almacenamiento sigue en
  `Calendar.DAY_OF_WEEK`, solo cambia el orden de UI.
- Landscape ilegible → editor con `verticalScroll`.
### Verificación en emulador (API 35, Android 15) — HECHA

- Scheduler: `dumpsys alarm` muestra `RTC_WAKEUP` exacto con `policy_permission`.
- Disparo: receiver corrió a su hora (3 wakeups confirmados en stats) y la
  notificación de alarma se posteó (logcat `NotifAttentionHelper` id=4).
- **Full-screen intent**: bloqueado el arranque de la actividad desde el
  receiver (BAL) → solución canónica: notificación con `setFullScreenIntent`
  + permiso `USE_FULL_SCREEN_INTENT` (denegado por defecto en target 35,
  habilitable en ajustes). Pantalla bloqueada → abre sobre el lock screen;
  desbloqueada → heads-up (comportamiento documentado de Android).
- Pantalla de alarma: «Es hora de despertar» + «Amanecer de 1 min» + botón
  «Detener» verificado; Detener vuelve a la lista y limpia la notificación.
- Falta aún: rampa visual real de fases (necesita pantalla bloqueada con
  minutos de antelación; el anillo está verificado y el renderer testado),
  prueba en el móvil real (doze/APPs de fabricante) y la semana de prueba
  (tarea #6).

## Siguiente unidad

- Probar una semana en el móvil de Jesús (tarea #6): adb install, alarma a
  1-2 min con antelación 0 para verificar el disparo exacto; luego amanecer.
- Endurecimiento si el sistema mata la alarma: foreground service + wake lock.

## Fase 2 — Contenido de audio (en curso)

### 2a: catálogo + selector de sonido — HECHA (commit `5192959`)

- 54 MB / 8 pistas (dominio público / CC0, −18 LUFS) importadas a
  `app/src/main/assets/sounds/` con metadatos en `SoundCatalog`
  (naturaleza / dormir / ruido blanco). Fuente: CATALOGO.md.
- `Alarm.soundUri` (Room v3, migración 1→2→3 sin pérdida).
- Picker de sonido en el editor: sonidos de Alborada por categoría +
  **tonos del sistema** (RingtoneManager). La alarma reproduce el sonido
  elegido (Media3), default «Brisa y pájaros».

### 2b: galería de música propia (pendiente, tarea siguiente)

- Importación con el selector del sistema (SAF) copiando a storage privado.
- Reproducción sin conexión + lista de reproducción / reproducción continua.

### 2c (próximo): modo reloj nocturno — tareas #7/#8 de TAREAS.md

- Fondo oscuro personalizable + varios estilos de reloj (recordado por el usuario).