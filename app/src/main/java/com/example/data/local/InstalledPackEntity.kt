package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent record of installed lore packs in Room.
 * Ensures the app knows on startup what pack is installed without guessing or re-syncing unconditionally.
 */
@Entity(tableName = "installed_pack_metadata")
data class InstalledPackEntity(
    @PrimaryKey
    val packId: String,
    val version: String,
    val schemaVersion: String,
    val contentHash: String,
    val installedTimestamp: Long,
    val siteCount: Int,
    val status: String // VALID, FALLBACK, etc.
)
