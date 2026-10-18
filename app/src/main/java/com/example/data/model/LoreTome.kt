package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.data.local.LoreSiteEntity
import com.example.ui.theme.LeatherAmber
import com.example.ui.theme.LeatherBurgundy
import com.example.ui.theme.LeatherCharcoal
import com.example.ui.theme.LeatherForest
import com.example.ui.theme.LeatherNavy

data class LoreTome(
    val id: String,
    val title: String,
    val subtitle: String,
    val county: String,
    val era: String,
    val category: String,
    val romanNumeral: String,
    val leatherColor: Color,
    val heightFraction: Float,
    val spineRibs: Int,
    val isVisited: Boolean,
    val isFavorite: Boolean,
    val hasListenedAudio: Boolean,
    val site: LoreSiteEntity
) {
    companion object {
        private val ROMAN_NUMERALS = listOf(
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
            "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX",
            "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX",
            "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX", "XL",
            "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L",
            "LI", "LII", "LIII", "LIV", "LV"
        )

        private val LEATHER_PALETTE = listOf(
            LeatherBurgundy,
            LeatherNavy,
            LeatherForest,
            LeatherAmber,
            LeatherCharcoal
        )

        fun fromSite(site: LoreSiteEntity, index: Int): LoreTome {
            val roman = if (index in ROMAN_NUMERALS.indices) ROMAN_NUMERALS[index] else "VOL.${index + 1}"
            val color = LEATHER_PALETTE[Math.abs(site.id.hashCode()) % LEATHER_PALETTE.size]
            // Deterministic slight height variation between 0.88f and 1.0f for authentic bookshelf texture
            val heightVariation = 0.88f + (Math.abs(site.id.hashCode() % 13) / 100f)
            val ribs = 3 + (Math.abs(site.id.hashCode() % 3))

            return LoreTome(
                id = site.id,
                title = site.title,
                subtitle = site.subtitle,
                county = site.county,
                era = site.era,
                category = site.category,
                romanNumeral = roman,
                leatherColor = color,
                heightFraction = heightVariation.coerceIn(0.88f, 1.0f),
                spineRibs = ribs,
                isVisited = site.isVisited,
                isFavorite = site.isFavorite,
                hasListenedAudio = site.hasListenedAudio,
                site = site
            )
        }
    }
}
