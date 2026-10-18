package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceMemoDao {

    @Query("SELECT * FROM lore_voice_memos ORDER BY timestamp DESC")
    fun getAllMemosFlow(): Flow<List<LoreVoiceMemoEntity>>

    @Query("SELECT * FROM lore_voice_memos WHERE siteId = :siteId ORDER BY timestamp DESC")
    fun getMemosForSiteFlow(siteId: String): Flow<List<LoreVoiceMemoEntity>>

    @Query("SELECT * FROM lore_voice_memos WHERE id = :id LIMIT 1")
    suspend fun getMemoById(id: String): LoreVoiceMemoEntity?

    @Query("SELECT COUNT(*) FROM lore_voice_memos")
    fun getMemoCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: LoreVoiceMemoEntity)

    @Query("DELETE FROM lore_voice_memos WHERE id = :id")
    suspend fun deleteMemo(id: String)
}
