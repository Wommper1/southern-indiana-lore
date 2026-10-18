package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Structured Historical Confidence Level for Southern Indiana lore records.
 * Replaces informal free-text labels to strictly separate archival facts from folk legends.
 *
 * Sprint 1.5 Truth Layer:
 * Unknown, missing, or corrupted levels MUST FAIL CLOSED to UNVERIFIED.
 */
enum class ConfidenceLevel(
    val label: String,
    val description: String,
    val badgeColorHex: Long
) {
    OFFICIAL_SOURCE(
        label = "Official Source",
        description = "Supported by an official government, archival, landmark, museum, or equivalent source",
        badgeColorHex = 0xFF15803D // Deep Forest Green
    ),
    VERIFIED(
        label = "Archivally Verified",
        description = "Supported by strong historical documentation, primary deeds, or government archives",
        badgeColorHex = 0xFF1D4ED8 // Archival Blue
    ),
    WELL_SUPPORTED(
        label = "Well Supported",
        description = "Multiple consistent historical records and primary cartographic evidence",
        badgeColorHex = 0xFF0369A1 // Navy / Slate Blue
    ),
    PLAUSIBLE(
        label = "Plausible",
        description = "Evidence supports this account, though minor details remain unconfirmed",
        badgeColorHex = 0xFF0E7490 // Cyan-Teal
    ),
    PROBABLE(
        label = "Probable History",
        description = "Evidence supports this account, but verification is incomplete",
        badgeColorHex = 0xFF0E7490 // Cyan-Teal
    ),
    ORAL_HISTORY(
        label = "Oral History",
        description = "Community memory or documented oral tradition",
        badgeColorHex = 0xFFB45309 // Amber / Warm Rust
    ),
    FOLKLORE(
        label = "Folklore Tradition",
        description = "Folklore or supernatural tradition, celebrated as cultural heritage rather than fact",
        badgeColorHex = 0xFF7E22CE // Mystical Purple
    ),
    LEGEND(
        label = "Folk Legend",
        description = "Folklore or supernatural tradition, not established historical fact",
        badgeColorHex = 0xFF7E22CE // Mystical Purple
    ),
    DISPUTED(
        label = "Disputed",
        description = "Conflicting historical accounts or contested claims in the archival record",
        badgeColorHex = 0xFFC026D3 // Magenta
    ),
    DEBUNKED(
        label = "Debunked",
        description = "Disproven by modern archival, physical, or cartographic examination",
        badgeColorHex = 0xFFDC2626 // Crimson Red
    ),
    UNVERIFIED(
        label = "Unverified",
        description = "Not yet independently verified against archival or historical documentation",
        badgeColorHex = 0xFF64748B // Slate Grey
    );

    fun getBadgeColor(): Color = Color(badgeColorHex)

    companion object {
        /**
         * Fails closed: if [name] is null, blank, or unrecognized, returns UNVERIFIED.
         * Never defaults or infers VERIFIED.
         */
        fun fromString(name: String?): ConfidenceLevel {
            if (name.isNullOrBlank()) return UNVERIFIED
            val normalized = name.trim().uppercase().replace(" ", "_")
            return try {
                when (normalized) {
                    "WELL_SUPPORTED" -> WELL_SUPPORTED
                    "FOLKLORE_TRADITION", "FOLK_LORE" -> FOLKLORE
                    else -> valueOf(normalized)
                }
            } catch (_: IllegalArgumentException) {
                UNVERIFIED
            }
        }
    }
}

