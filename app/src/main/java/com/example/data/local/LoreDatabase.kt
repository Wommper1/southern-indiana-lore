package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.pack.LorePackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LoreSiteEntity::class,
        UserSiteJournalEntity::class,
        InstalledPackEntity::class,
        LoreMediaAssetEntity::class,
        LoreVoiceMemoEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class LoreDatabase : RoomDatabase() {

    abstract fun loreDao(): LoreDao
    abstract fun voiceMemoDao(): VoiceMemoDao

    companion object {
        @Volatile
        private var INSTANCE: LoreDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create separate user_site_journal table to safely preserve user favorites, visits, notes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_site_journal` (
                        `siteId` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `isVisited` INTEGER NOT NULL,
                        `userNotes` TEXT NOT NULL,
                        `lastVisitedTimestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`siteId`)
                    )
                """.trimIndent())

                // 2. Migrate existing user-owned data from v1 lore_sites table
                db.execSQL("""
                    INSERT OR REPLACE INTO `user_site_journal` (`siteId`, `isFavorite`, `isVisited`, `userNotes`, `lastVisitedTimestamp`)
                    SELECT `id`, `isFavorite`, `isVisited`, `userNotes`, `lastVisitedTimestamp`
                    FROM `lore_sites`
                    WHERE `isFavorite` = 1 OR `isVisited` = 1 OR length(`userNotes`) > 0
                """.trimIndent())

                // 3. Add new historical confidence and regional columns to lore_sites
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `county` TEXT NOT NULL DEFAULT 'Floyd County'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `verifiedHistory` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `folkloreTradition` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `confidenceLevel` TEXT NOT NULL DEFAULT 'VERIFIED'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `sources` TEXT NOT NULL DEFAULT ''")

                // 4. Backfill verifiedHistory with fullStory where blank
                db.execSQL("UPDATE `lore_sites` SET `verifiedHistory` = `fullStory` WHERE `verifiedHistory` = ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_site_journal` ADD COLUMN `photoUri` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `isCustom` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `photoUri` TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `installed_pack_metadata` (
                        `packId` TEXT NOT NULL,
                        `version` TEXT NOT NULL,
                        `schemaVersion` TEXT NOT NULL,
                        `contentHash` TEXT NOT NULL,
                        `installedTimestamp` INTEGER NOT NULL,
                        `siteCount` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`packId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `alternativeNames` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `siteType` TEXT NOT NULL DEFAULT 'POINT'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `subType` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `triggerRadiusType` TEXT NOT NULL DEFAULT 'LOCAL_SITE'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `driveStory` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `whyHere` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `historyNerdDiscovery` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `aboveLayer` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `surfaceLayer` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `belowLayer` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `time1800` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `time1850` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `time1900` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `time1950` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `timeToday` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `whatMayBeUnderground` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `visibleRemains` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `relatedLocations` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `relatedPeople` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `relatedEvents` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `mapEvidence` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `unresolvedQuestions` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `accessLevel` TEXT NOT NULL DEFAULT 'PUBLIC ACCESS'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `isSensitiveLocation` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `isExactCoordinateSafe` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `radarType` TEXT NOT NULL DEFAULT 'HISTORIC_NEARBY'")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `youJustCrossedEvent` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `geometryCoordinates` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `undergroundConfidence` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `encounterCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `firstDiscoveredTimestamp` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `lastEncounterTimestamp` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `lore_sites` ADD COLUMN `hasListenedAudio` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_site_journal` ADD COLUMN `encounterCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_site_journal` ADD COLUMN `firstDiscoveredTimestamp` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_site_journal` ADD COLUMN `lastEncounterTimestamp` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_site_journal` ADD COLUMN `hasListenedAudio` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lore_media_assets` (
                        `id` TEXT NOT NULL,
                        `siteId` TEXT NOT NULL,
                        `triggerTier` TEXT NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `mediaPath` TEXT NOT NULL,
                        `personaId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL DEFAULT '',
                        `priority` INTEGER NOT NULL DEFAULT 5,
                        `durationSec` INTEGER NOT NULL DEFAULT 0,
                        `duckAmbientPercent` REAL NOT NULL DEFAULT 0.2,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lore_media_assets_siteId` ON `lore_media_assets` (`siteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lore_media_assets_triggerTier` ON `lore_media_assets` (`triggerTier`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lore_sites_latitude_longitude` ON `lore_sites` (`latitude`, `longitude`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lore_voice_memos` (
                        `id` TEXT NOT NULL,
                        `siteId` TEXT,
                        `title` TEXT NOT NULL,
                        `informantName` TEXT NOT NULL DEFAULT '',
                        `audioFilePath` TEXT NOT NULL,
                        `durationMs` INTEGER NOT NULL DEFAULT 0,
                        `timestamp` INTEGER NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `fieldNotes` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lore_voice_memos_siteId` ON `lore_voice_memos` (`siteId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lore_voice_memos_timestamp` ON `lore_voice_memos` (`timestamp`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): LoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoreDatabase::class.java,
                    "southern_indiana_lore.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(LoreDatabaseCallback(context.applicationContext, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class LoreDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(context, database.loreDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Guarantee pack is validated & installed without trampling custom lore
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.loreDao().getSiteCount() == 0) {
                            populateDatabase(context, database.loreDao())
                        }
                    }
                }
            }

            suspend fun populateDatabase(context: Context, dao: LoreDao) {
                LorePackManager.syncAuthoritativePack(context, dao)
                if (dao.getAllMediaAssets().isEmpty()) {
                    dao.insertMediaAssets(LoreMediaSeedData.assets)
                }
            }
        }
    }
}
