package com.example.util

/**
 * Persona profiles for high-fidelity adventure audio narration.
 * Configures natural voice selection, pitch, and speech rates to deliver engaging,
 * living storytelling rather than flat robotic audio.
 */
enum class NarratorPersona(
    val id: String,
    val displayName: String,
    val titleSubtitle: String,
    val iconEmoji: String,
    val basePitch: Float,
    val baseSpeechRate: Float,
    val sampleAuditionLine: String,
    val voiceKeywords: List<String>
) {
    HISTORIAN(
        id = "HISTORIAN",
        displayName = "Archivist & Historian",
        titleSubtitle = "Articulate scholarly cadence with clear historical resonance",
        iconEmoji = "📜",
        basePitch = 1.05f,
        baseSpeechRate = 0.98f,
        sampleAuditionLine = "Archivist dispatch. Consulting county archives and verified historical lore of Southern Indiana.",
        voiceKeywords = listOf("en-gb", "rjs", "female", "gis", "neural", "en-us-x-sfg")
    ),
    FOLKLORE_SAGE(
        id = "FOLKLORE_SAGE",
        displayName = "Folklore Sage",
        titleSubtitle = "Deep, gravelly fireside storyteller with atmospheric cadence",
        iconEmoji = "🕯️",
        basePitch = 0.55f,
        baseSpeechRate = 0.78f,
        sampleAuditionLine = "Listen closely, traveler. The old hills hold ancient secrets, and legendary spirits walk in the shadows.",
        voiceKeywords = listOf("en-us-x-iom", "male", "en-us-x-iol", "neural")
    ),
    FIELD_EXPLORER(
        id = "FIELD_EXPLORER",
        displayName = "Expedition Guide",
        titleSubtitle = "Crisp, dynamic, energetic backcountry scout",
        iconEmoji = "🧭",
        basePitch = 1.45f,
        baseSpeechRate = 1.22f,
        sampleAuditionLine = "Field scout online! Compass calibrated, coordinates locked, and trail pack ready. Let's explore!",
        voiceKeywords = listOf("en-us-x-tpd", "en-us-x-iob", "female", "en-au", "neural")
    ),
    ADVENTURE_CHRONICLER(
        id = "ADVENTURE_CHRONICLER",
        displayName = "Adventure Chronicler",
        titleSubtitle = "Resonant male baritone, cinematic pacing, frontier documentary style",
        iconEmoji = "🎙️",
        basePitch = 0.76f,
        baseSpeechRate = 0.90f,
        sampleAuditionLine = "Frontier dispatch. Chronicling forgotten limestone caverns, ghost rails, and historic river crossings.",
        voiceKeywords = listOf("en-us-x-sfg", "en-us-x-iol", "male", "en-gb-x-rjs", "neural")
    ),
    WIZARD_NARRATOR(
        id = "WIZARD_NARRATOR",
        displayName = "Karst Lore Wizard",
        titleSubtitle = "Mysterious, archaic wizard narrator steeped in esoteric Southern Indiana lore",
        iconEmoji = "🧙‍♂️",
        basePitch = 0.62f,
        baseSpeechRate = 0.82f,
        sampleAuditionLine = "Halt, wanderer. By limestone root and subterranean spring, ancient lore awakens beneath your feet.",
        voiceKeywords = listOf("en-us-x-iom", "en-gb-x-rjs", "male", "neural")
    ),
    RETRO_RADIO(
        id = "RETRO_RADIO",
        displayName = "Retro Dispatch (1940s)",
        titleSubtitle = "Vintage AM radio broadcaster with brisk newsreel cadence and retro flair",
        iconEmoji = "📻",
        basePitch = 1.25f,
        baseSpeechRate = 1.15f,
        sampleAuditionLine = "We interrupt this broadcast for a special bulletin! You have just entered the historic Southern Indiana perimeter.",
        voiceKeywords = listOf("en-us-x-tpd", "en-us-x-sfg", "neural")
    );

    companion object {
        fun fromId(id: String?): NarratorPersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HISTORIAN
        }
    }
}
