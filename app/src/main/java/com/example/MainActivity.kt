package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddCustomLoreDialog
import com.example.ui.components.AdventureTopBar
import com.example.ui.components.ArchivistAssistantDialog
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.BatteryOptimizationDialog
import com.example.ui.components.DossierIntroOverlay
import com.example.ui.components.GeofenceBanner
import com.example.ui.components.LivePermissionsCard
import com.example.ui.components.OralHistoryTapeDeckModal
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.SiteDetailDialog
import com.example.ui.components.StartupVideoIntroOverlay
import com.example.ui.components.VoiceSettingsDialog
import com.example.data.model.CuratedMysteries
import com.example.ui.components.CipherDecoderDialog
import com.example.ui.components.TactileInstrumentDock
import com.example.ui.screens.ArchivePackScreen
import com.example.ui.screens.CameraViewfinderScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.ExplorerWorktableScreen
import com.example.ui.screens.FieldJournalScreen
import com.example.ui.screens.KarstHydrophoneScreen
import com.example.ui.theme.LanternLightingMode
import com.example.ui.theme.LanternLightingState
import com.example.ui.theme.LocalLanternLighting
import com.example.util.LocalExpeditionAudio
import com.example.ui.screens.LibraryBookcaseScreen
import com.example.ui.screens.RadarMapScreen
import com.example.ui.screens.RelicLabScreen
import com.example.ui.screens.TomeDossierReader
import com.example.ui.screens.TourModeScreen
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SouthernIndianaLoreTheme
import com.example.ui.viewmodel.LoreViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: LoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            SouthernIndianaLoreTheme(darkTheme = true) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val siteId = intent?.getStringExtra(NotificationHelper.EXTRA_SITE_ID)
        if (!siteId.isNullOrBlank()) {
            viewModel.selectSiteById(siteId)
        }
    }
}

@Composable
fun MainAppScreen(viewModel: LoreViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val sites by viewModel.filteredSites.collectAsStateWithLifecycle()
    val allSites by viewModel.allSitesUnfiltered.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCounty by viewModel.selectedCounty.collectAsStateWithLifecycle()
    val selectedEra by viewModel.selectedEra.collectAsStateWithLifecycle()
    val proximityRadiusKm by viewModel.proximityRadiusKm.collectAsStateWithLifecycle()
    val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsStateWithLifecycle()
    val filterVisitedOnly by viewModel.filterVisitedOnly.collectAsStateWithLifecycle()

    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLng by viewModel.userLongitude.collectAsStateWithLifecycle()

    val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
    val activeGeofenceSite by viewModel.activeGeofenceSite.collectAsStateWithLifecycle()
    val isTourActive by viewModel.isTourSimulationActive.collectAsStateWithLifecycle()
    val isLiveGpsActive by viewModel.isLiveGpsActive.collectAsStateWithLifecycle()
    val gpsAccuracyMeters by viewModel.gpsAccuracyMeters.collectAsStateWithLifecycle()
    val isForegroundTourActive by viewModel.isForegroundServiceActive.collectAsStateWithLifecycle()
    val currentLocationMode by viewModel.locationMode.collectAsStateWithLifecycle()
    val showBatteryGuide by viewModel.showBatteryGuide.collectAsStateWithLifecycle()
    val showStartupVideo by viewModel.showStartupVideo.collectAsStateWithLifecycle()
    val showColdStartIntro by viewModel.showColdStartIntro.collectAsStateWithLifecycle()
    val showVoiceSettingsDialog by viewModel.showVoiceSettingsDialog.collectAsStateWithLifecycle()

    val hardwareHeading by viewModel.hardwareHeading.collectAsStateWithLifecycle()
    val isCompassAvailable by viewModel.isCompassAvailable.collectAsStateWithLifecycle()
    val showAddCustomDialog by viewModel.showAddCustomDialog.collectAsStateWithLifecycle()
    val showArchivistDialog by viewModel.showArchivistDialog.collectAsStateWithLifecycle()
    val archivistSiteContext by viewModel.archivistSiteContext.collectAsStateWithLifecycle()

    val isAudioPlaying by viewModel.narratorManager.isPlaying.collectAsStateWithLifecycle()
    val narratingSiteId by viewModel.narratorManager.currentNarrationSiteId.collectAsStateWithLifecycle()
    val narratingTitle by viewModel.narratorManager.currentNarrationTitle.collectAsStateWithLifecycle()
    val speechRate by viewModel.narratorManager.speechRate.collectAsStateWithLifecycle()
    val isSoundFxEnabled by viewModel.isSoundFxEnabled.collectAsStateWithLifecycle()

    val installedPacks by viewModel.installedPacks.collectAsStateWithLifecycle(emptyList())
    val packValidationResult by viewModel.packValidationResult.collectAsStateWithLifecycle()
    val upcomingSubterraneanCrossing by viewModel.upcomingSubterraneanCrossing.collectAsStateWithLifecycle()
    val upcomingBridgeCrossing by viewModel.upcomingBridgeCrossing.collectAsStateWithLifecycle()
    val selectedTomeSite by viewModel.selectedTomeSite.collectAsStateWithLifecycle()
    val activeExpeditionTrail by viewModel.activeExpeditionTrail.collectAsStateWithLifecycle()
    val showFieldJournal by viewModel.showFieldJournal.collectAsStateWithLifecycle()
    val viewfinderSite by viewModel.viewfinderSite.collectAsStateWithLifecycle()
    val activeCipherRiddle by viewModel.activeCipherRiddle.collectAsStateWithLifecycle()
    val solvedCipherIds by viewModel.solvedCipherIds.collectAsStateWithLifecycle()
    val showKarstHydrophone by viewModel.showKarstHydrophone.collectAsStateWithLifecycle()

    val voiceMemos by viewModel.voiceMemos.collectAsStateWithLifecycle()
    val isRecordingVoiceMemo by viewModel.isRecordingVoiceMemo.collectAsStateWithLifecycle()
    val recordingDurationMs by viewModel.recordingDurationMs.collectAsStateWithLifecycle()
    val liveMicAmplitude by viewModel.liveMicAmplitude.collectAsStateWithLifecycle()
    val currentlyPlayingVoiceMemoId by viewModel.currentlyPlayingVoiceMemoId.collectAsStateWithLifecycle()
    val isPlayingVoiceMemo by viewModel.isPlayingVoiceMemo.collectAsStateWithLifecycle()
    val memoPlaybackProgressMs by viewModel.memoPlaybackProgressMs.collectAsStateWithLifecycle()
    val memoPlaybackDurationMs by viewModel.memoPlaybackDurationMs.collectAsStateWithLifecycle()
    val showTapeDeckRecorder by viewModel.showTapeDeckRecorder.collectAsStateWithLifecycle()
    val activeRecordingSiteId by viewModel.activeRecordingSiteId.collectAsStateWithLifecycle()

    val visitedCount = allSites.count { it.isVisited }
    val favoriteCount = allSites.count { it.isFavorite }

    var lanternLightingState by remember { mutableStateOf(LanternLightingState()) }

    CompositionLocalProvider(
        LocalLanternLighting provides lanternLightingState,
        LocalExpeditionAudio provides viewModel.expeditionAudio
    ) {
      // The first-launch intro is drawn AFTER the Scaffold inside this Box so it sits above the
      // bottom dock and audio bar. (Inside the Scaffold content it was painted underneath them,
      // which hid the "Begin Expedition" button.)
      Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateNight),
            containerColor = SlateNight,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                Column(
                    modifier = Modifier.testTag("bottom_nav_bar_container")
                ) {
                    // Persistent floating audio player bar when narration is active
                    AudioPlayerBar(
                        isPlaying = isAudioPlaying,
                        siteTitle = narratingTitle,
                        onStopClick = { viewModel.stopNarration() },
                        speechRate = speechRate,
                        onSpeechRateChange = { viewModel.setSpeechRate(it) }
                    )

                    // Tactile Antiquarian Brass Instrument Dock
                    TactileInstrumentDock(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        soundEngine = viewModel.adventureSoundEngine
                    )
                }
            }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Compact Adventure Top Bar (Sleek ~56dp profile ensuring maximum screen for maps & explore list)
            AdventureTopBar(
                totalSitesCount = allSites.size,
                visitedCount = visitedCount,
                favoriteCount = favoriteCount,
                isTourActive = isTourActive || isForegroundTourActive,
                isLiveGpsActive = isLiveGpsActive,
                isSoundFxEnabled = isSoundFxEnabled,
                onToggleSoundFx = { viewModel.toggleSoundEffects() },
                onOpenVoiceSettings = { viewModel.openVoiceSettings() },
                onOpenFieldJournal = { viewModel.openFieldJournal() },
                onOpenViewfinder = { viewModel.openViewfinder() },
                onOpenRelicLab = { selectedTab = 4 },
                lightingState = lanternLightingState,
                onLightingModeChanged = { lanternLightingState = lanternLightingState.copy(mode = it) },
                soundEngine = viewModel.adventureSoundEngine
            )

            // Permissions prompt for S25 Ultra Live Alerts
            LivePermissionsCard(
                onPermissionsGranted = { viewModel.toggleLiveGps(true) }
            )

            // Geofence proximity breach banner
            GeofenceBanner(
                activeSite = activeGeofenceSite,
                onOpenDetails = { site -> viewModel.selectSite(site) },
                onListenNarration = { site -> viewModel.playNarration(site) },
                onDismiss = { viewModel.dismissGeofenceAlert() }
            )

            // Main Tab Content
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = selectedTab, label = "tab_fade") { tabIndex ->
                    when (tabIndex) {
                        0 -> ExplorerWorktableScreen(
                            sites = sites,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            selectedCounty = selectedCounty,
                            onCountySelected = { viewModel.selectCounty(it) },
                            selectedEra = selectedEra,
                            onEraSelected = { viewModel.selectEra(it) },
                            proximityRadiusKm = proximityRadiusKm,
                            onProximityRadiusKmChanged = { viewModel.setProximityRadiusKm(it) },
                            filterFavoritesOnly = filterFavoritesOnly,
                            onToggleFavoritesFilter = { viewModel.toggleFavoritesFilter() },
                            filterVisitedOnly = filterVisitedOnly,
                            onToggleVisitedFilter = { viewModel.toggleVisitedFilter() },
                            currentNarratingSiteId = narratingSiteId,
                            onCardClick = { viewModel.selectSite(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it.id, it.isFavorite) },
                            onToggleVisited = { viewModel.toggleVisited(it.id, it.isVisited) },
                            onNarrateClick = { viewModel.playNarration(it) },
                            onOpenAddCustomDialog = { viewModel.openAddCustomDialog() },
                            onOpenArchivist = { viewModel.openArchivistDialog() },
                            onOpenVoiceSettings = { viewModel.openVoiceSettings() },
                            onOpenLibrary = { selectedTab = 3 },
                            onOpenFieldJournal = { viewModel.openFieldJournal() },
                            onOpenRelicLab = { selectedTab = 4 },
                            onOpenKarstHydrophone = { viewModel.openKarstHydrophone() },
                            onOpenRadar = { selectedTab = 1 },
                            isLiveGpsActive = isLiveGpsActive,
                            hardwareHeading = hardwareHeading,
                            soundEngine = viewModel.adventureSoundEngine
                        )
                        1 -> RadarMapScreen(
                            userLat = userLat,
                            userLng = userLng,
                            sites = sites,
                            selectedSite = selectedSite,
                            isLiveGpsActive = isLiveGpsActive,
                            gpsAccuracyMeters = gpsAccuracyMeters,
                            hardwareHeadingDegrees = hardwareHeading,
                            isCompassAvailable = isCompassAvailable,
                            onToggleLiveGps = { viewModel.toggleLiveGps(it) },
                            onSiteSelected = { viewModel.selectSite(it) },
                            onSetLocation = { lat, lng -> viewModel.setUserLocation(lat, lng) },
                            onResetCenter = { viewModel.resetToNewAlbanyCenter() },
                            onOpenBatteryGuide = { viewModel.openBatteryGuide() },
                            soundEngine = viewModel.adventureSoundEngine
                        )
                        2 -> TourModeScreen(
                            isTourActive = isTourActive,
                            onStartTour = { viewModel.startTourSimulation() },
                            onStopTour = { viewModel.stopTourSimulation() },
                            isForegroundTourActive = isForegroundTourActive,
                            onToggleForegroundTour = { enable ->
                                if (enable) viewModel.startForegroundTourService(currentLocationMode)
                                else viewModel.stopForegroundTourService()
                            },
                            currentLocationMode = currentLocationMode,
                            onLocationModeChanged = { viewModel.setLocationMode(it) },
                            onOpenBatteryGuide = { viewModel.openBatteryGuide() },
                            sites = sites,
                            onSiteClick = { viewModel.selectSite(it) },
                            userLat = userLat,
                            userLng = userLng,
                            upcomingSubterraneanCrossing = upcomingSubterraneanCrossing,
                            upcomingBridgeCrossing = upcomingBridgeCrossing,
                            activeExpeditionTrail = activeExpeditionTrail,
                            onClearExpeditionTrail = { viewModel.clearExpeditionTrail() },
                            onOpenViewfinder = { viewModel.openViewfinder(it) },
                            onOpenFieldJournal = { viewModel.openFieldJournal() },
                            soundEngine = viewModel.adventureSoundEngine
                        )
                        3 -> LibraryBookcaseScreen(
                            allSites = allSites,
                            onSelectSite = { site, roman ->
                                viewModel.selectTome(site, roman)
                            },
                            onStartTrailTour = { trail ->
                                viewModel.startExpeditionTrail(trail)
                                selectedTab = 2
                                viewModel.startTourSimulation()
                            },
                            onOpenCipher = { riddle ->
                                viewModel.openCipher(riddle)
                            },
                            onOpenViewfinder = { siteId ->
                                viewModel.openViewfinderBySiteId(siteId)
                            },
                            solvedCipherIds = solvedCipherIds,
                            soundEngine = viewModel.adventureSoundEngine
                        )
                        4 -> RelicLabScreen(
                            soundEngine = viewModel.adventureSoundEngine,
                            ambientSoundEngine = viewModel.ambientSoundEngine,
                            narratorManager = viewModel.narratorManager,
                            onNavigateBack = { selectedTab = 0 },
                            onNavigateToSite = { siteId ->
                                val site = sites.find { it.id == siteId }
                                if (site != null) {
                                    viewModel.selectSite(site)
                                }
                                selectedTab = 0
                            },
                            onSaveRubbingRecord = {
                                viewModel.adventureSoundEngine.playWaxSealBreak()
                            }
                        )
                        5 -> ArchivePackScreen(
                            sites = sites,
                            onSiteClick = { viewModel.selectSite(it) },
                            ambientSoundEngine = viewModel.ambientSoundEngine,
                            onToggleAmbientTheme = { viewModel.toggleAmbientTheme(it) },
                            installedPack = installedPacks.firstOrNull(),
                            packValidationResult = packValidationResult,
                            onRevalidatePack = { viewModel.revalidatePack() },
                            onReloadPackFromAssets = { viewModel.reloadPackFromAssets() }
                        )
                    }
                }
            }
        }

        // Samsung S25 Ultra Battery Optimization Guide Dialog
        if (showBatteryGuide) {
            BatteryOptimizationDialog(
                onDismiss = { viewModel.dismissBatteryGuide() }
            )
        }

        // Custom Lore Discovery Dialog (Real-world field logging)
        if (showAddCustomDialog) {
            AddCustomLoreDialog(
                currentGpsLat = userLat,
                currentGpsLng = userLng,
                onDismiss = { viewModel.dismissAddCustomDialog() },
                onSaveSite = { entity -> viewModel.addCustomSite(entity) }
            )
        }

        // Archivist (offline keyword lookup) dialog
        if (showArchivistDialog) {
            ArchivistAssistantDialog(
                archivistService = viewModel.archivistService,
                siteContext = archivistSiteContext,
                onDismiss = { viewModel.dismissArchivistDialog() }
            )
        }

        // Voice Persona, Cadence & Sound FX Settings Dialog
        if (showVoiceSettingsDialog) {
            VoiceSettingsDialog(
                narratorManager = viewModel.narratorManager,
                soundEngine = viewModel.adventureSoundEngine,
                ambientSoundEngine = viewModel.ambientSoundEngine,
                onReplayIntro = { viewModel.replayColdStartIntro() },
                onDismiss = { viewModel.dismissVoiceSettings() }
            )
        }

        // Full Narrative Dialog
        selectedSite?.let { site ->
            SiteDetailDialog(
                site = site,
                isCurrentlyNarrating = narratingSiteId == site.id,
                onDismiss = { viewModel.clearSelectedSite() },
                onToggleFavorite = { viewModel.toggleFavorite(site.id, site.isFavorite) },
                onToggleVisited = { viewModel.toggleVisited(site.id, site.isVisited) },
                onNarrateClick = { viewModel.playNarration(site) },
                onSaveUserNotes = { notes -> viewModel.saveUserNotes(site.id, notes) },
                onAttachPhoto = { photoUri -> viewModel.updateSitePhoto(site.id, photoUri) },
                onAskArchivist = { viewModel.openArchivistDialog(site.title) },
                onDeleteCustomSite = if (site.isCustom) {
                    { viewModel.deleteCustomSite(site.id) }
                } else null,
                onOpenInLibrary = {
                    val entity = allSites.firstOrNull { it.id == site.id }
                    if (entity != null) {
                        val index = allSites.indexOfFirst { it.id == site.id }
                        val roman = com.example.data.model.LoreTome.fromSite(entity, if (index >= 0) index else 0).romanNumeral
                        viewModel.clearSelectedSite()
                        viewModel.selectTome(entity, roman)
                    }
                },
                onOpenViewfinder = {
                    viewModel.openViewfinder(site)
                    viewModel.clearSelectedSite()
                },
                onRecordVoiceMemo = {
                    viewModel.openTapeDeckRecorder(site.id)
                }
            )
        }

        // Fullscreen Athenaeum Tome Dossier Reader
        selectedTomeSite?.let { (tomeSite, romanNumeral) ->
            TomeDossierReader(
                site = tomeSite,
                romanNumeral = romanNumeral,
                onBack = { viewModel.clearSelectedTome() },
                onToggleBookmark = { siteId ->
                    viewModel.toggleFavorite(siteId, tomeSite.isFavorite)
                },
                onToggleVisited = { siteId ->
                    viewModel.toggleVisited(siteId, tomeSite.isVisited)
                },
                onPlayNarration = { entity ->
                    val dist = viewModel.proximityEngine.calculateDistanceToSite(userLat, userLng, entity)
                    viewModel.playNarration(entity.toDomain(dist))
                },
                onStopNarration = { viewModel.stopNarration() },
                isAudioPlaying = isAudioPlaying,
                narratingSiteId = narratingSiteId,
                onOpenInCockpit = { entity ->
                    val dist = viewModel.proximityEngine.calculateDistanceToSite(userLat, userLng, entity)
                    viewModel.selectSite(entity.toDomain(dist))
                    viewModel.clearSelectedTome()
                    selectedTab = 2
                },
                onEditNotes = { entity ->
                    val dist = viewModel.proximityEngine.calculateDistanceToSite(userLat, userLng, entity)
                    viewModel.selectSite(entity.toDomain(dist))
                }
            )
        }

        // Expedition Field Journal (Full Screen Sketchbook)
        if (showFieldJournal) {
            FieldJournalScreen(
                allSites = allSites,
                onBack = { viewModel.closeFieldJournal() },
                onOpenViewfinder = { siteId ->
                    viewModel.closeFieldJournal()
                    viewModel.openViewfinderBySiteId(siteId)
                },
                onOpenCipher = { riddle ->
                    viewModel.openCipher(riddle)
                },
                solvedCipherIds = solvedCipherIds,
                soundEngine = viewModel.adventureSoundEngine,
                voiceMemos = voiceMemos,
                currentlyPlayingMemoId = currentlyPlayingVoiceMemoId,
                isPlayingMemo = isPlayingVoiceMemo,
                playbackProgressMs = memoPlaybackProgressMs,
                playbackDurationMs = memoPlaybackDurationMs,
                onOpenTapeDeck = { viewModel.openTapeDeckRecorder() },
                onPlayMemo = { viewModel.playVoiceMemo(it) },
                onPauseMemo = { viewModel.pauseVoiceMemo() },
                onSeekMemo = { viewModel.seekVoiceMemo(it) },
                onDeleteMemo = { viewModel.deleteVoiceMemo(it) },
                onShareMemo = { viewModel.shareVoiceMemo(context, it) }
            )
        }

        // Vintage Analog Oral History Tape Deck Modal
        if (showTapeDeckRecorder) {
            val siteTitle = activeRecordingSiteId?.let { sid -> allSites.find { it.id == sid }?.title }
            OralHistoryTapeDeckModal(
                siteTitle = siteTitle,
                isRecording = isRecordingVoiceMemo,
                recordingDurationMs = recordingDurationMs,
                liveMicAmplitude = liveMicAmplitude,
                onStartRecording = { viewModel.startRecordingVoiceMemo() },
                onStopAndSave = { title, informant, notes ->
                    viewModel.stopAndSaveVoiceMemo(title, informant, notes)
                },
                onCancel = { viewModel.cancelRecordingVoiceMemo() },
                onDismiss = { viewModel.closeTapeDeckRecorder() }
            )
        }

        // National Treasure AR Viewfinder HUD Screen
        viewfinderSite?.let { site ->
            val clue = CuratedMysteries.allClues.firstOrNull { it.siteId == site.id }
            CameraViewfinderScreen(
                currentSite = site,
                clueEntry = clue,
                headingDegrees = hardwareHeading,
                onBack = { viewModel.closeViewfinder() },
                onCaptureSnapshot = { label ->
                    viewModel.captureViewfinderSnapshot(label)
                },
                soundEngine = viewModel.adventureSoundEngine
            )
        }

        // Interactive Concentric Brass Cipher Decoder Dialog
        activeCipherRiddle?.let { riddle ->
            CipherDecoderDialog(
                riddle = riddle,
                onDismiss = { viewModel.closeCipher() },
                onCipherSolved = { solvedRiddle ->
                    viewModel.solveCipher(solvedRiddle)
                },
                soundEngine = viewModel.adventureSoundEngine
            )
        }

        // Subterranean Karst Hydrophone Sounding Station
        if (showKarstHydrophone) {
            KarstHydrophoneScreen(
                audioEngine = viewModel.expeditionAudio,
                onNavigateBack = { viewModel.closeKarstHydrophone() }
            )
        }
    } // end Scaffold content

        // Cold-Start Animated Leather Dossier Intro (first launch only), above everything
        if (showColdStartIntro) {
            DossierIntroOverlay(
                soundEngine = viewModel.adventureSoundEngine,
                narratorManager = viewModel.narratorManager,
                onBeginExpedition = { viewModel.dismissColdStartIntro() }
            )
        }
      } // end Box
    } // end CompositionLocalProvider
}
