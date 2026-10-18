package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores user-owned modifications (favorites, visits, custom field notes).
 * Separated from the read-only / replaceable Lore Pack data so that pack updates,
 * schema migrations, or re-indexing can NEVER wipe or overwrite user journal entries.
 */
@Entity(tableName = "user_site_journal")
data class UserSiteJournalEntity(
    @PrimaryKey val siteId: String,
    val isFavorite: Boolean = false,
    val isVisited: Boolean = false,
    val userNotes: String = "",
    val lastVisitedTimestamp: Long = 0L,
    val photoUri: String? = null,
    val encounterCount: Int = 0,
    val firstDiscoveredTimestamp: Long = 0L,
    val lastEncounterTimestamp: Long = 0L,
    val hasListenedAudio: Boolean = false
)
