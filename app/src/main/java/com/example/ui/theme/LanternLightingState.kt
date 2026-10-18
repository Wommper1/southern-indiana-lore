package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Lighting modes for the Explorer's Antiquarian Field Console:
 * - KEROSENE: Warm 2200K amber candlelight, authentic 19th-century vellum tone
 * - TORCH: Karst Cavern torchlight with emerald & subterranean phosphorescence
 * - BLACKLIGHT_UV: 365nm ultraviolet lamp that reveals hidden cartographer notes,
 *                  secret Underground Railroad route marks, and cave depth runes
 */
enum class LanternLightingMode(
    val displayName: String,
    val subtitle: String,
    val glowColor: Color,
    val indicatorHex: Long
) {
    KEROSENE(
        displayName = "Kerosene Lamp",
        subtitle = "Warm Amber 2200K • Rag Vellum",
        glowColor = Color(0xFFF59E0B),
        indicatorHex = 0xFFF59E0B
    ),
    TORCH(
        displayName = "Karst Torch",
        subtitle = "Emerald Cavern Phosphorescence",
        glowColor = Color(0xFF10B981),
        indicatorHex = 0xFF10B981
    ),
    BLACKLIGHT_UV(
        displayName = "Blacklight UV",
        subtitle = "Secret Cartography & Luminescent Ink",
        glowColor = Color(0xFFB388FF),
        indicatorHex = 0xFFB388FF
    );

    fun next(): LanternLightingMode = when (this) {
        KEROSENE -> TORCH
        TORCH -> BLACKLIGHT_UV
        BLACKLIGHT_UV -> KEROSENE
    }
}

/**
 * Global lighting state consumed across all explorer worktable components,
 * folded parchment cards, map overlays, and marginalia notes.
 */
data class LanternLightingState(
    val mode: LanternLightingMode = LanternLightingMode.KEROSENE,
    val intensity: Float = 0.90f
) {
    val isUvActive: Boolean get() = mode == LanternLightingMode.BLACKLIGHT_UV

    val ambientGlowColor: Color get() = mode.glowColor.copy(alpha = (intensity * 0.45f).coerceIn(0.15f, 0.85f))

    val borderBezelHighlight: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color(0xFFE5B85C)
        LanternLightingMode.TORCH -> Color(0xFF34D399)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0xFFC084FC)
    }

    val deskSurfaceTint: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color(0xFF130B06)
        LanternLightingMode.TORCH -> Color(0xFF071410)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0xFF0B0716)
    }

    val paperSurfaceColor: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color(0xFFF6EEDB)
        LanternLightingMode.TORCH -> Color(0xFFE5EDE6)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0xFF1F1A2E) // Darkened under blacklight so fluorescent ink pops!
    }

    val standardInkColor: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color(0xFF1C1917)
        LanternLightingMode.TORCH -> Color(0xFF14241B)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0xFF7E7294) // Recedes under UV
    }

    val secretMarginaliaColor: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color(0x228B5E3C) // Barely visible watermark
        LanternLightingMode.TORCH -> Color(0x3310B981)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0xFFE879F9) // Vivid ultraviolet luminescent script
    }

    val secretMarginaliaGlow: Color get() = when (mode) {
        LanternLightingMode.KEROSENE -> Color.Transparent
        LanternLightingMode.TORCH -> Color(0x2210B981)
        LanternLightingMode.BLACKLIGHT_UV -> Color(0x88C084FC)
    }

    val secretInkVisible: Boolean get() = mode == LanternLightingMode.BLACKLIGHT_UV
}

val LocalLanternLighting = compositionLocalOf { LanternLightingState() }
