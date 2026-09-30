package com.jesusjbriceno.alborada.alarm

/**
 * Bundled sounds imported from the project audio catalog
 * (resources/audio/CATALOGO.md): all public-domain or CC0, normalized to
 * 64 kbps mono at -18 LUFS. An empty `Alarm.soundUri` resolves to the
 * default dawn tone; a stored value is an asset URI, a content URI (system
 * ringtone) or a catalog id.
 */
object SoundCatalog {
    data class Sound(
        val id: String,
        val title: String,
        val category: String,
        val uri: String,
        val license: String,
    )

    const val DEFAULT_ID = "default"

    /** The default ringing tone (the morning brisa y pájaros). */
    val defaultSound =
        Sound(
            id = DEFAULT_ID,
            title = "Brisa y pájaros (por defecto)",
            category = "Por defecto",
            uri = "asset:///sounds/naturaleza/brisa-y-pajaros.ogg",
            license = "Dominio público",
        )

    val bundled: List<Sound> =
        listOf(
            defaultSound,
            Sound("rio", "Río poco profundo", "Naturaleza", "asset:///sounds/naturaleza/rio-poco-profundo.ogg", "Dominio público"),
            Sound("bosque", "Ambiente de bosque", "Naturaleza", "asset:///sounds/naturaleza/ambiente-bosque.ogg", "Dominio público"),
            Sound("tormenta", "Tormenta de verano", "Naturaleza", "asset:///sounds/naturaleza/tormenta-1.ogg", "Dominio público"),
            Sound("ambient", "Tonos ambientales", "Dormir", "asset:///sounds/dormir/ambient003_64kb.mp3", "Dominio público"),
            Sound("calm-pills", "Ambiente para dormir", "Dormir", "asset:///sounds/dormir/calm-pills-1-still-habitat.mp3", "CC0"),
            Sound(
                "guitarra",
                "Guitarra y piano suave",
                "Dormir",
                "asset:///sounds/dormir/relax-music---guitar-soft---piano---instrumental2.mp3",
                "Dominio público",
            ),
            Sound(
                "ruido-blanco",
                "Ruido blanco clásico",
                "Ruido blanco",
                "asset:///sounds/ruido-blanco/classic-white-noise.ogg",
                "Dominio público",
            ),
        )

    /** Category-ordered groups of selectable bundled sounds. */
    val bundledByCategory: List<Pair<String, List<Sound>>> =
        listOf("Naturaleza", "Dormir", "Ruido blanco", "Por defecto")
            .map { category -> category to bundled.filter { it.category == category } }
            .filter { it.second.isNotEmpty() }

    fun titleOf(soundUri: String): String? = bundled.firstOrNull { it.uri == soundUri }?.title

    /** Resolves a stored [Alarm.soundUri] to an empty default-safe media uri. */
    fun resolveUri(soundUri: String): String = if (soundUri.isBlank()) defaultSound.uri else soundUri
}
