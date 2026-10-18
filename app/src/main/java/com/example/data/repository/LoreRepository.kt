package com.example.data.repository

import android.content.Context
import com.example.data.local.InstalledPackEntity
import com.example.data.local.LoreDao
import com.example.data.local.LoreSiteEntity
import com.example.data.pack.LorePackManager
import com.example.data.pack.PackValidationResult
import kotlinx.coroutines.flow.Flow

import com.example.data.local.LoreVoiceMemoEntity
import com.example.data.local.VoiceMemoDao
import kotlinx.coroutines.flow.flowOf

class LoreRepository(
    private val loreDao: LoreDao,
    private val voiceMemoDao: VoiceMemoDao? = null
) {

    val allSites: Flow<List<LoreSiteEntity>> = loreDao.getAllSites()
    val favoriteSites: Flow<List<LoreSiteEntity>> = loreDao.getFavoriteSites()
    val visitedSites: Flow<List<LoreSiteEntity>> = loreDao.getVisitedSites()
    val installedPacks: Flow<List<InstalledPackEntity>> = loreDao.getAllInstalledPacks()
    val allVoiceMemos: Flow<List<LoreVoiceMemoEntity>> = voiceMemoDao?.getAllMemosFlow() ?: flowOf(emptyList())

    fun getSitesByCategory(category: String): Flow<List<LoreSiteEntity>> {
        return loreDao.getSitesByCategory(category)
    }

    suspend fun getSiteById(id: String): LoreSiteEntity? {
        return loreDao.getSiteById(id)
    }

    suspend fun toggleFavorite(id: String, currentFavorite: Boolean) {
        loreDao.setFavorite(id, !currentFavorite)
    }

    suspend fun toggleVisited(id: String, currentVisited: Boolean) {
        val next = !currentVisited
        loreDao.setVisited(id, next, if (next) System.currentTimeMillis() else 0L)
    }

    suspend fun updateUserNotes(id: String, notes: String) {
        loreDao.updateUserNotes(id, notes)
    }

    suspend fun getInstalledPack(packId: String): InstalledPackEntity? {
        return loreDao.getInstalledPack(packId)
    }

    /**
     * Authoritatively synchronizes lore pack. Pack is the source of truth.
     * Hardcoded LoreSeedData is only used as an emergency fallback if pack is invalid/missing and DB is empty.
     */
    suspend fun syncAuthoritativePack(context: Context): PackValidationResult {
        return LorePackManager.syncAuthoritativePack(context, loreDao)
    }

    suspend fun revalidatePack(context: Context): PackValidationResult {
        return LorePackManager.validatePack(context)
    }

    suspend fun insertCustomSite(site: LoreSiteEntity) {
        loreDao.insertSite(site.copy(isCustom = true))
    }

    suspend fun deleteCustomSite(id: String) {
        loreDao.deleteCustomSite(id)
    }

    suspend fun updatePhotoUri(id: String, photoUri: String?) {
        loreDao.updatePhotoUri(id, photoUri)
    }

    suspend fun recordEncounter(siteId: String, timestamp: Long = System.currentTimeMillis()) {
        loreDao.recordEncounter(siteId, timestamp)
    }

    suspend fun recordListenedAudio(siteId: String) {
        loreDao.recordListenedAudio(siteId)
    }

    suspend fun getJournalEntry(siteId: String) = loreDao.getJournalEntry(siteId)

    suspend fun getSiteCount(): Int = loreDao.getSiteCount()
    suspend fun getCustomSiteCount(): Int = loreDao.getCustomSiteCount()

    // --- Spatial Range Querying (Bounding Box) ---
    suspend fun getSitesInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<LoreSiteEntity> {
        return loreDao.getSitesInBoundingBox(minLat, maxLat, minLng, maxLng)
    }

    // --- Rich Media Asset Operations ---
    suspend fun getMediaAssetsForSite(siteId: String): List<com.example.data.local.LoreMediaAssetEntity> {
        return loreDao.getMediaAssetsForSite(siteId)
    }

    suspend fun getMediaAssetForTier(siteId: String, triggerTier: String): com.example.data.local.LoreMediaAssetEntity? {
        return loreDao.getMediaAssetForTier(siteId, triggerTier)
    }

    suspend fun insertMediaAssets(assets: List<com.example.data.local.LoreMediaAssetEntity>) {
        loreDao.insertMediaAssets(assets)
    }

    suspend fun getAllMediaAssets(): List<com.example.data.local.LoreMediaAssetEntity> {
        return loreDao.getAllMediaAssets()
    }

    // --- Oral History Voice Memo Operations ---

    fun getVoiceMemosForSite(siteId: String): Flow<List<LoreVoiceMemoEntity>> {
        return voiceMemoDao?.getMemosForSiteFlow(siteId) ?: flowOf(emptyList())
    }

    suspend fun insertVoiceMemo(memo: LoreVoiceMemoEntity) {
        voiceMemoDao?.insertMemo(memo)
    }

    suspend fun deleteVoiceMemo(id: String) {
        voiceMemoDao?.deleteMemo(id)
    }

    suspend fun getVoiceMemoById(id: String): LoreVoiceMemoEntity? {
        return voiceMemoDao?.getMemoById(id)
    }
}
