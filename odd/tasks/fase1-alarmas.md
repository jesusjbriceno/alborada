# Feature: Fase 1 — Núcleo de alarmas

Estado: **en curso** · Rama: master (commits directos, trabajo reviewable)

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

## Siguiente unidad

- Amanecer simulado (brillo/color progresivo) + crescendo (tareas #3 y #4).