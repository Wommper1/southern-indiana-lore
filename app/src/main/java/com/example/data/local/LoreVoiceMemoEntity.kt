package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an oral history field recording, eyewitness interview, or audio EVP memo
 * logged at a Southern Indiana lore coordinate.
 */
@Entity(
    tableName = "lore_voice_memos",
    indices = [
        Index(value = ["siteId"]),
        Index(value = ["timestamp"])
    ]
)
data class LoreVoiceMemoEntity(
    @PrimaryKey
    val id: String,
    val siteId: String? = null,
    val title: String,
    val informantName: String = "",
    val audioFilePath: String,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val fieldNotes: String = ""
)
