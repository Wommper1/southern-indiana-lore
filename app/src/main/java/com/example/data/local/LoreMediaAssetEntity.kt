package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Rich media asset linked to a Southern Indiana Lore POI.
 * Links coordinate triggers and multi-tier geofence boundaries directly to custom audio,
 * narration voiceovers, retro broadcasts, or ambient soundscapes.
 */
@Entity(
    tableName = "lore_media_assets",
    indices = [
        Index(value = ["siteId"]),
        Index(value = ["triggerTier"])
    ]
)
data class LoreMediaAssetEntity(
    @PrimaryKey val id: String,
    val siteId: String,
    val triggerTier: String,       // "APPROACH_1500M", "APPROACH_500M", "ARRIVAL_100M", "CORRIDOR_CROSS", "AMBIENT_PAD"
    val mediaType: String,         // "NARRATION_TTS", "LOCAL_AUDIO_FILE", "PROCEDURAL_ACOUSTIC", "RADIO_BROADCAST"
    val mediaPath: String,         // e.g. "narrator://wizard", "raw/old_buffalo_trace_crossing.mp3", "procedural://radio_static"
    val personaId: String,         // "WIZARD_NARRATOR", "ADVENTURE_CHRONICLER", "RETRO_RADIO", "FOLKLORE_SAGE", "HISTORIAN"
    val title: String,
    val description: String = "",
    val priority: Int = 5,         // 1 = background ambient, 5 = standard narration, 10 = critical corridor cross
    val durationSec: Int = 0,
    val duckAmbientPercent: Float = 0.2f
)
