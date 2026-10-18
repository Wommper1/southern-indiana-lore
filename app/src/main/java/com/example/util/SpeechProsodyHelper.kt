package com.example.util

/**
 * Transforms raw story strings into rhythmically paced, human-like narrative speech.
 * Inserts rhetorical pauses at colons, dashes, and periods to eliminate robotic rush,
 * and expands abbreviations and place names for natural oratorical delivery.
 */
object SpeechProsodyHelper {

    fun formatForHumanNarration(rawText: String): String {
        if (rawText.isBlank()) return ""

        var cleaned = rawText
            // Clean markdown asterisks and bold/header tags
            .replace("**", "")
            .replace("*", "")
            .replace("###", "")
            .replace("##", "")
            .replace("#", "")
            .replace("_", "")
            .replace("`", "")
            // Expand common abbreviations for natural pronunciation
            .replace(Regex("\\bCo\\.", RegexOption.IGNORE_CASE), "County")
            .replace(Regex("\\bRR\\b", RegexOption.IGNORE_CASE), "Railroad")
            .replace(Regex("\\bB&O\\b", RegexOption.IGNORE_CASE), "B and O Railroad")
            .replace(Regex("\\bK&I\\b", RegexOption.IGNORE_CASE), "K and I Bridge")
            .replace(Regex("\\bSt\\.", RegexOption.IGNORE_CASE), "Street")
            .replace(Regex("\\bHwy\\b", RegexOption.IGNORE_CASE), "Highway")
            .replace(Regex("\\bSR\\s*(\\d+)", RegexOption.IGNORE_CASE), "State Road $1")
            .replace(Regex("\\bIN-(\\d+)", RegexOption.IGNORE_CASE), "Indiana State Road $1")
            .replace(Regex("\\bUS-(\\d+)", RegexOption.IGNORE_CASE), "U.S. Route $1")
            .replace(Regex("\\bFt\\.", RegexOption.IGNORE_CASE), "Fort")
            .replace(Regex("\\bMt\\.", RegexOption.IGNORE_CASE), "Mount")
            .replace(Regex("\\bca\\.", RegexOption.IGNORE_CASE), "circa")
            .replace(Regex("\\bc\\.\\s*(\\d{4})", RegexOption.IGNORE_CASE), "circa $1")
            .replace("—", ", ")
            .replace("–", ", ")
            // Pronunciation assists for regional terms
            .replace(Regex("\\bKnobs\\b"), "Nawbs")
            .replace(Regex("\\bFloyds Knobs\\b"), "Floyds Nawbs")
            .replace(Regex("\\bWyandotte\\b"), "Why-an-dot")

        // Insert natural breath-points and rhetorical pauses for an engaging storytelling rhythm
        cleaned = cleaned
            .replace("; ", " ... ")
            .replace(": ", " ... ")
            .replace(". ", ".  ")
            .replace("\n\n", " ... ")
            .replace("\n", " ")

        return cleaned.trim()
    }

    /**
     * Builds a rich, deeply informative narrative combining:
     * - Title & Subtitle setting the scene
     * - Historical era and geographical county
     * - Physical/geological context (e.g. subterranean cave or bridge span)
     * - The complete full historical & folklore narrative
     * - Archival verification & field records
     */
    fun buildRichNarration(
        title: String,
        subtitle: String,
        era: String,
        county: String,
        fullStory: String,
        verifiedHistory: String,
        folkloreTradition: String,
        whatMayBeUnderground: String = "",
        youJustCrossedEvent: String = ""
    ): String {
        return buildString {
            append(title).append(". ")
            if (subtitle.isNotBlank()) {
                append(subtitle).append(". ")
            }
            if (youJustCrossedEvent.isNotBlank()) {
                append(youJustCrossedEvent).append(". ")
            }
            if (era.isNotBlank()) {
                append("Dating to the ").append(era).append(" in ").append(county).append(". ")
            }
            if (whatMayBeUnderground.isNotBlank()) {
                append("Underneath this ground: ").append(whatMayBeUnderground).append(". ")
            }
            append(fullStory)
            if (verifiedHistory.isNotBlank()) {
                append(" ... Historical Record: ").append(verifiedHistory)
            }
            if (folkloreTradition.isNotBlank()) {
                append(" ... Local Tradition: ").append(folkloreTradition)
            }
        }
    }
}
