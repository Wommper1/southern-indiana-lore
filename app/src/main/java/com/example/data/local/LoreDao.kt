package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoreDao {

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        ORDER BY s.title ASC
    """)
    fun getAllSites(): Flow<List<LoreSiteEntity>>

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        WHERE s.id = :id
    """)
    suspend fun getSiteById(id: String): LoreSiteEntity?

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        WHERE s.category = :category
        ORDER BY s.title ASC
    """)
    fun getSitesByCategory(category: String): Flow<List<LoreSiteEntity>>

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        WHERE COALESCE(j.isFavorite, s.isFavorite) = 1
        ORDER BY s.title ASC
    """)
    fun getFavoriteSites(): Flow<List<LoreSiteEntity>>

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        WHERE COALESCE(j.isVisited, s.isVisited) = 1
        ORDER BY COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) DESC
    """)
    fun getVisitedSites(): Flow<List<LoreSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sites: List<LoreSiteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: LoreSiteEntity)

    @Update
    suspend fun updateSite(site: LoreSiteEntity)

    @Query("DELETE FROM lore_sites WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomSite(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJournal(journal: UserSiteJournalEntity)

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri)
        VALUES (:id, :isFavorite, 0, '', 0, NULL)
        ON CONFLICT(siteId) DO UPDATE SET isFavorite = :isFavorite
    """)
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri)
        VALUES (:id, 0, :isVisited, '', :timestamp, NULL)
        ON CONFLICT(siteId) DO UPDATE SET isVisited = :isVisited, lastVisitedTimestamp = :timestamp
    """)
    suspend fun setVisited(id: String, isVisited: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri)
        VALUES (:id, 0, 0, :notes, 0, NULL)
        ON CONFLICT(siteId) DO UPDATE SET userNotes = :notes
    """)
    suspend fun updateUserNotes(id: String, notes: String)

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri)
        VALUES (:id, 0, 0, '', 0, :photoUri)
        ON CONFLICT(siteId) DO UPDATE SET photoUri = :photoUri
    """)
    suspend fun updatePhotoUri(id: String, photoUri: String?)

    @Query("SELECT COUNT(*) FROM lore_sites")
    suspend fun getSiteCount(): Int

    @Query("SELECT COUNT(*) FROM lore_sites WHERE isCustom = 1")
    suspend fun getCustomSiteCount(): Int

    @Query("SELECT * FROM lore_sites WHERE isCustom = 0")
    suspend fun getPackOwnedSites(): List<LoreSiteEntity>

    @Query("DELETE FROM lore_sites WHERE isCustom = 0")
    suspend fun deletePackOwnedSites()

    /**
     * Atomically replaces pack-owned sites without touching user-created lore (isCustom = 1)
     * or user journal records (favorites, visits, custom photos, notes).
     */
    @Transaction
    suspend fun replacePackSitesSafely(newSites: List<LoreSiteEntity>) {
        deletePackOwnedSites()
        val sanitized = newSites.map { it.copy(isCustom = false) }
        insertAll(sanitized)
    }

    @Query("SELECT * FROM user_site_journal")
    suspend fun getAllJournalEntries(): List<UserSiteJournalEntity>

    @Query("SELECT * FROM user_site_journal WHERE siteId = :siteId")
    suspend fun getJournalEntry(siteId: String): UserSiteJournalEntity?

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri, encounterCount, firstDiscoveredTimestamp, lastEncounterTimestamp, hasListenedAudio)
        VALUES (:siteId, 0, 0, '', 0, NULL, 1, :timestamp, :timestamp, 0)
        ON CONFLICT(siteId) DO UPDATE SET 
            encounterCount = encounterCount + 1,
            lastEncounterTimestamp = :timestamp,
            firstDiscoveredTimestamp = CASE WHEN firstDiscoveredTimestamp == 0 THEN :timestamp ELSE firstDiscoveredTimestamp END
    """)
    suspend fun recordEncounter(siteId: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        INSERT INTO user_site_journal (siteId, isFavorite, isVisited, userNotes, lastVisitedTimestamp, photoUri, encounterCount, firstDiscoveredTimestamp, lastEncounterTimestamp, hasListenedAudio)
        VALUES (:siteId, 0, 0, '', 0, NULL, 0, 0, 0, 1)
        ON CONFLICT(siteId) DO UPDATE SET hasListenedAudio = 1
    """)
    suspend fun recordListenedAudio(siteId: String)

    // --- Installed Pack Metadata Operations ---

    @Query("SELECT * FROM installed_pack_metadata WHERE packId = :packId LIMIT 1")
    suspend fun getInstalledPack(packId: String): InstalledPackEntity?

    @Query("SELECT * FROM installed_pack_metadata")
    fun getAllInstalledPacks(): Flow<List<InstalledPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstalledPack(pack: InstalledPackEntity)

    @Query("DELETE FROM installed_pack_metadata WHERE packId = :packId")
    suspend fun deleteInstalledPack(packId: String)

    // --- Spatial Range Querying (Indexed Bounding Box) ---

    @Query("""
        SELECT s.*,
               COALESCE(j.photoUri, s.photoUri) AS photoUri,
               COALESCE(j.isFavorite, s.isFavorite) AS isFavorite,
               COALESCE(j.isVisited, s.isVisited) AS isVisited,
               COALESCE(j.userNotes, s.userNotes) AS userNotes,
               COALESCE(j.lastVisitedTimestamp, s.lastVisitedTimestamp) AS lastVisitedTimestamp,
               COALESCE(j.encounterCount, s.encounterCount, 0) AS encounterCount,
               COALESCE(j.firstDiscoveredTimestamp, s.firstDiscoveredTimestamp, 0) AS firstDiscoveredTimestamp,
               COALESCE(j.lastEncounterTimestamp, s.lastEncounterTimestamp, 0) AS lastEncounterTimestamp,
               COALESCE(j.hasListenedAudio, s.hasListenedAudio, 0) AS hasListenedAudio
        FROM lore_sites s
        LEFT JOIN user_site_journal j ON s.id = j.siteId
        WHERE s.latitude BETWEEN :minLat AND :maxLat
          AND s.longitude BETWEEN :minLng AND :maxLng
        ORDER BY s.title ASC
    """)
    suspend fun getSitesInBoundingBox(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<LoreSiteEntity>

    // --- Rich Media Asset Linking Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAssets(assets: List<LoreMediaAssetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAsset(asset: LoreMediaAssetEntity)

    @Query("SELECT * FROM lore_media_assets WHERE siteId = :siteId ORDER BY priority DESC")
    suspend fun getMediaAssetsForSite(siteId: String): List<LoreMediaAssetEntity>

    @Query("SELECT * FROM lore_media_assets WHERE siteId = :siteId AND triggerTier = :triggerTier LIMIT 1")
    suspend fun getMediaAssetForTier(siteId: String, triggerTier: String): LoreMediaAssetEntity?

    @Query("SELECT * FROM lore_media_assets ORDER BY priority DESC")
    suspend fun getAllMediaAssets(): List<LoreMediaAssetEntity>

    @Query("DELETE FROM lore_media_assets WHERE siteId = :siteId")
    suspend fun deleteMediaAssetsForSite(siteId: String)
}
