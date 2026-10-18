package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.archivist.LocalArchivistService
import com.example.data.local.InstalledPackEntity
import com.example.data.local.LoreDatabase
import com.example.data.local.LoreSiteEntity
import com.example.data.model.CipherRiddle
import com.example.data.model.CrossingCategory
import com.example.data.model.CrossingCountdown
import com.example.data.model.CuratedMysteries
import com.example.data.model.ExpeditionTrail
import com.example.data.model.JournalClueEntry
import com.example.data.model.LoreCategories
import com.example.data.model.LoreCounties
import com.example.data.model.LoreEras
import com.example.data.model.LoreSite
import com.example.data.model.WaxCylinderRecording
import com.example.service.CrossingState
import com.example.service.ProximityTier
import com.example.service.SpatialProximityCoordinator
import com.example.data.local.LoreMediaAssetEntity
import com.example.data.pack.PackValidationResult
import com.example.data.repository.LoreRepository
import com.example.service.HardwareGeofenceRegistrar
import com.example.service.LiveLocationProvider
import com.example.service.LocationMode
import com.example.service.LoreTourForegroundService
import com.example.util.NarratorPersona
import com.example.util.AmbientSoundEngine
import com.example.util.CompassOrientationProvider
import com.example.util.LocationUtils
import com.example.util.NarratorManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.local.LoreVoiceMemoEntity
import com.example.util.AudioPlaybackManager
import com.example.util.AudioRecordingManager
import java.io.File
import java.util.UUID

/** SharedPreferences key ("lore_settings"): the first-launch dossier intro has been dismissed. */
private const val PREF_INTRO_SEEN = "intro_seen"

class LoreViewModel(application: Application) : AndroidViewModel(application) {

    val narratorManager = NarratorManager(application)
    val liveLocationProvider = LiveLocationProvider(application)
    val geofenceRegistrar = HardwareGeofenceRegistrar(application)
    val compassOrientationProvider = CompassOrientationProvider(application)
    val archivistService = LocalArchivistService()
    val ambientSoundEngine = AmbientSoundEngine(application)
    val adventureSoundEngine = com.example.util.AdventureSoundEngine(application)
    val expeditionAudio = com.example.util.ExpeditionAudioEngine(
        context = application,
        adventure = adventureSoundEngine,
        ambient = ambientSoundEngine
    )
    val proximityEngine = com.example.service.LocalProximityEngine(debounceCooldownMs = 3 * 60 * 1000L)

    private val _showKarstHydrophone = MutableStateFlow(false)
    val showKarstHydrophone: StateFlow<Boolean> = _showKarstHydrophone.asStateFlow()

    fun openKarstHydrophone() {
        expeditionAudio.playBrassToggleSwitch()
        _showKarstHydrophone.value = true
    }

    fun closeKarstHydrophone() {
        expeditionAudio.playTactileClick()
        _showKarstHydrophone.value = false
    }

    private val _isSoundFxEnabled = MutableStateFlow(adventureSoundEngine.isSoundEnabled)
    val isSoundFxEnabled: StateFlow<Boolean> = _isSoundFxEnabled.asStateFlow()

    fun toggleSoundEffects() {
        val newState = !adventureSoundEngine.isSoundEnabled
        adventureSoundEngine.isSoundEnabled = newState
        _isSoundFxEnabled.value = newState
        if (newState) {
            adventureSoundEngine.playSonarPing()
        }
    }

    private val db = LoreDatabase.getDatabase(application, viewModelScope)
    private val repository: LoreRepository = LoreRepository(db.loreDao(), db.voiceMemoDao())
    val spatialCoordinator = SpatialProximityCoordinator(repository)

    // --- Oral History Voice Memo & Field Audio Engine ---
    val audioRecordingManager = AudioRecordingManager(application)
    val audioPlaybackManager = AudioPlaybackManager(application)

    val voiceMemos: StateFlow<List<LoreVoiceMemoEntity>> = repository.allVoiceMemos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isRecordingVoiceMemo: StateFlow<Boolean> = audioRecordingManager.isRecording
    val recordingDurationMs: StateFlow<Long> = audioRecordingManager.recordingDurationMs
    val liveMicAmplitude: StateFlow<Float> = audioRecordingManager.liveNormalizedAmplitude

    val currentlyPlayingVoiceMemoId: StateFlow<String?> = audioPlaybackManager.currentlyPlayingId
    val isPlayingVoiceMemo: StateFlow<Boolean> = audioPlaybackManager.isPlaying
    val memoPlaybackProgressMs: StateFlow<Long> = audioPlaybackManager.currentPositionMs
    val memoPlaybackDurationMs: StateFlow<Long> = audioPlaybackManager.durationMs

    private val _activeRecordingDraftId = MutableStateFlow<String?>(null)
    val activeRecordingDraftId: StateFlow<String?> = _activeRecordingDraftId.asStateFlow()

    private val _activeRecordingSiteId = MutableStateFlow<String?>(null)
    val activeRecordingSiteId: StateFlow<String?> = _activeRecordingSiteId.asStateFlow()

    private val _activeRecordingFile = MutableStateFlow<File?>(null)

    private val _showTapeDeckRecorder = MutableStateFlow(false)
    val showTapeDeckRecorder: StateFlow<Boolean> = _showTapeDeckRecorder.asStateFlow()

    val installedPacks = repository.installedPacks
    private val _packValidationResult = MutableStateFlow<PackValidationResult?>(null)
    val packValidationResult: StateFlow<PackValidationResult?> = _packValidationResult.asStateFlow()

    private val _pendingSiteId = MutableStateFlow<String?>(null)

    private val prefs = application.getSharedPreferences("lore_settings", android.content.Context.MODE_PRIVATE)

    private val _locationMode = MutableStateFlow(
        try {
            val saved = prefs.getString("location_mode", LocationMode.ROAD_TRIP.name)
            LocationMode.valueOf(saved ?: LocationMode.ROAD_TRIP.name)
        } catch (_: Exception) {
            LocationMode.ROAD_TRIP
        }
    )
    val locationMode: StateFlow<LocationMode> = _locationMode.asStateFlow()

    // Live GPS tracking vs Simulation
    val isLiveGpsActive: StateFlow<Boolean> = liveLocationProvider.isLiveGpsActive
    val gpsAccuracyMeters: StateFlow<Float?> = liveLocationProvider.gpsAccuracyMeters

    // Hardware Compass Orientation (S25 Ultra IMU / Magnetometer)
    val hardwareHeading: StateFlow<Float> = compassOrientationProvider.azimuthDegrees
    val isCompassAvailable: StateFlow<Boolean> = compassOrientationProvider.isSensorAvailable

    private val _isForegroundServiceActive = MutableStateFlow(false)
    val isForegroundServiceActive: StateFlow<Boolean> = _isForegroundServiceActive.asStateFlow()

    private val _showBatteryGuide = MutableStateFlow(false)
    val showBatteryGuide: StateFlow<Boolean> = _showBatteryGuide.asStateFlow()

    // Startup video intro overlay state (plays on fresh launch / app open)
    private val _showStartupVideo = MutableStateFlow(true)
    val showStartupVideo: StateFlow<Boolean> = _showStartupVideo.asStateFlow()

    fun dismissStartupVideo() {
        _showStartupVideo.value = false
    }

    // Voice Persona & Cadence Settings Dialog
    private val _showVoiceSettingsDialog = MutableStateFlow(false)
    val showVoiceSettingsDialog: StateFlow<Boolean> = _showVoiceSettingsDialog.asStateFlow()

    fun openVoiceSettings() {
        _showVoiceSettingsDialog.value = true
    }

    fun dismissVoiceSettings() {
        _showVoiceSettingsDialog.value = false
    }

    // Dialog state for adding custom site & archivist
    private val _showAddCustomDialog = MutableStateFlow(false)
    val showAddCustomDialog: StateFlow<Boolean> = _showAddCustomDialog.asStateFlow()

    private val _showArchivistDialog = MutableStateFlow(false)
    val showArchivistDialog: StateFlow<Boolean> = _showArchivistDialog.asStateFlow()

    private val _archivistSiteContext = MutableStateFlow<String?>(null)
    val archivistSiteContext: StateFlow<String?> = _archivistSiteContext.asStateFlow()

    // User Location (Defaults to downtown New Albany, Floyd County, IN)
    private val _userLatitude = MutableStateFlow(LocationUtils.DEFAULT_LATITUDE)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(LocationUtils.DEFAULT_LONGITUDE)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(LoreCategories.ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCounty = MutableStateFlow(LoreCounties.ALL)
    val selectedCounty: StateFlow<String> = _selectedCounty.asStateFlow()

    private val _selectedEra = MutableStateFlow(LoreEras.ALL)
    val selectedEra: StateFlow<String> = _selectedEra.asStateFlow()

    // Proximity radius filter in kilometers (0f means no radius limit / all)
    private val _proximityRadiusKm = MutableStateFlow(0f)
    val proximityRadiusKm: StateFlow<Float> = _proximityRadiusKm.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    private val _filterVisitedOnly = MutableStateFlow(false)
    val filterVisitedOnly: StateFlow<Boolean> = _filterVisitedOnly.asStateFlow()

    // Dialog & Detail state
    private val _selectedSite = MutableStateFlow<LoreSite?>(null)
    val selectedSite: StateFlow<LoreSite?> = _selectedSite.asStateFlow()

    // Athenaeum Bookcase & Expedition Passport state
    private val _selectedTomeSite = MutableStateFlow<Pair<LoreSiteEntity, String>?>(null)
    val selectedTomeSite: StateFlow<Pair<LoreSiteEntity, String>?> = _selectedTomeSite.asStateFlow()

    private val _activeExpeditionTrail = MutableStateFlow<ExpeditionTrail?>(null)
    val activeExpeditionTrail: StateFlow<ExpeditionTrail?> = _activeExpeditionTrail.asStateFlow()

    // Cold-Start Animated Leather Dossier Intro Sequence State.
    // Shown on the very first launch only; dismissing it is remembered. It can be replayed from
    // the narrator/voice settings dialog.
    private val _showColdStartIntro = MutableStateFlow(!prefs.getBoolean(PREF_INTRO_SEEN, false))
    val showColdStartIntro: StateFlow<Boolean> = _showColdStartIntro.asStateFlow()

    // Expedition Field Journal, Viewfinder HUD & Cipher Decoder State
    private val _showFieldJournal = MutableStateFlow(false)
    val showFieldJournal: StateFlow<Boolean> = _showFieldJournal.asStateFlow()

    private val _viewfinderSite = MutableStateFlow<LoreSite?>(null)
    val viewfinderSite: StateFlow<LoreSite?> = _viewfinderSite.asStateFlow()

    private val _activeCipherRiddle = MutableStateFlow<CipherRiddle?>(null)
    val activeCipherRiddle: StateFlow<CipherRiddle?> = _activeCipherRiddle.asStateFlow()

    // Vintage Phonograph Radio & Eyewitness Oral History Archive
    private val _showPhonographRadio = MutableStateFlow(false)
    val showPhonographRadio: StateFlow<Boolean> = _showPhonographRadio.asStateFlow()

    private val _solvedCipherIds = MutableStateFlow<Set<String>>(emptySet())
    val solvedCipherIds: StateFlow<Set<String>> = _solvedCipherIds.asStateFlow()

    // Geofence & Tour Simulator State
    private val _isTourSimulationActive = MutableStateFlow(false)
    val isTourSimulationActive: StateFlow<Boolean> = _isTourSimulationActive.asStateFlow()

    private val _activeGeofenceSite = MutableStateFlow<LoreSite?>(null)
    val activeGeofenceSite: StateFlow<LoreSite?> = _activeGeofenceSite.asStateFlow()

    private var tourJob: Job? = null

    private data class FilterOptions(
        val query: String,
        val category: String,
        val county: String,
        val era: String,
        val proximityKm: Float,
        val favOnly: Boolean,
        val visOnly: Boolean
    )

    private data class LocationCoordinates(
        val lat: Double,
        val lng: Double
    )

    private val categoryCountyEraFilterState = combine(
        _selectedCategory,
        _selectedCounty,
        _selectedEra
    ) { category, county, era ->
        Triple(category, county, era)
    }

    private val searchAndFilterState = combine(
        _searchQuery,
        categoryCountyEraFilterState,
        _proximityRadiusKm,
        _filterFavoritesOnly,
        _filterVisitedOnly
    ) { query, (category, county, era), proximityKm, favOnly, visOnly ->
        FilterOptions(query, category, county, era, proximityKm, favOnly, visOnly)
    }

    private val userLocationState = combine(
        _userLatitude,
        _userLongitude
    ) { lat, lng ->
        LocationCoordinates(lat, lng)
    }

    // Combined Lore List with dynamic distance calculation
    val filteredSites: StateFlow<List<LoreSite>> = combine(
        repository.allSites,
        searchAndFilterState,
        userLocationState
    ) { rawSites, filter, loc ->
        rawSites
            .map { entity ->
                val distance = proximityEngine.calculateDistanceToSite(
                    loc.lat, loc.lng,
                    entity
                )
                entity.toDomain(distance)
            }
            .filter { site ->
                val matchesCategory = (filter.category == LoreCategories.ALL || site.category == filter.category)
                val matchesCounty = (filter.county == LoreCounties.ALL || site.county.equals(filter.county, ignoreCase = true))
                val matchesEra = LoreEras.matchesEra(site.era, filter.era)
                val matchesProximity = filter.proximityKm <= 0f || (site.distanceMeters <= filter.proximityKm * 1000f)
                val q = filter.query.trim()
                val matchesSearch = q.isBlank() ||
                        site.title.contains(q, ignoreCase = true) ||
                        site.subtitle.contains(q, ignoreCase = true) ||
                        site.county.contains(q, ignoreCase = true) ||
                        site.era.contains(q, ignoreCase = true) ||
                        site.fullStory.contains(q, ignoreCase = true) ||
                        site.verifiedHistory.contains(q, ignoreCase = true) ||
                        site.folkloreTradition.contains(q, ignoreCase = true) ||
                        site.sources.contains(q, ignoreCase = true)
                val matchesFav = !filter.favOnly || site.isFavorite
                val matchesVis = !filter.visOnly || site.isVisited

                matchesCategory && matchesCounty && matchesEra && matchesProximity && matchesSearch && matchesFav && matchesVis
            }
            .sortedBy { it.distanceMeters }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSitesUnfiltered: StateFlow<List<LoreSiteEntity>> = repository.allSites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Real-Time Subterranean Void Crossing Countdown (Karst caves, underground rivers, railroad cuts/tunnels)
    val upcomingSubterraneanCrossing: StateFlow<CrossingCountdown?> = combine(
        repository.allSites,
        userLocationState,
        locationMode
    ) { rawSites: List<LoreSiteEntity>, loc: LocationCoordinates, mode: LocationMode ->
        val candidates = rawSites.filter { site ->
            site.radarType.equals("UNDERGROUND", ignoreCase = true) ||
            site.whatMayBeUnderground.isNotBlank() ||
            site.title.contains("Cave", ignoreCase = true) ||
            site.title.contains("Tunnel", ignoreCase = true) ||
            site.title.contains("Cavern", ignoreCase = true)
        }
        if (candidates.isEmpty()) return@combine null

        val nearest = candidates.minByOrNull {
            proximityEngine.calculateDistanceToSite(loc.lat, loc.lng, it)
        } ?: return@combine null

        val dist = proximityEngine.calculateDistanceToSite(loc.lat, loc.lng, nearest)
        val bearing = LocationUtils.calculateBearing(loc.lat, loc.lng, nearest.latitude, nearest.longitude)
        val domainSite = nearest.toDomain(dist)
        val speedMps = when (mode) {
            LocationMode.BROWSE -> 1.4f
            LocationMode.EXPLORE -> 1.4f
            LocationMode.ROAD_TRIP -> 18.0f
            LocationMode.PASSIVE -> 12.0f
        }
        val etaSec = (dist / speedMps).toInt()
        val horizon = 1500f
        val progress = (1f - (dist / horizon)).coerceIn(0f, 1f)
        val state = proximityEngine.evaluateCrossingState(domainSite, loc.lat, loc.lng)

        CrossingCountdown(
            site = domainSite,
            distanceMeters = dist,
            bearingDegrees = bearing,
            estimatedSeconds = etaSec,
            category = CrossingCategory.SUBTERRANEAN_VOID,
            progressPercent = progress,
            state = state
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Real-Time Historic River & Rail Bridge Span Crossing Countdown
    val upcomingBridgeCrossing: StateFlow<CrossingCountdown?> = combine(
        repository.allSites,
        userLocationState,
        locationMode
    ) { rawSites: List<LoreSiteEntity>, loc: LocationCoordinates, mode: LocationMode ->
        val candidates = rawSites.filter { site ->
            site.radarType.equals("BRIDGE", ignoreCase = true) ||
            site.title.contains("Bridge", ignoreCase = true) ||
            site.title.contains("Trestle", ignoreCase = true) ||
            site.title.contains("Viaduct", ignoreCase = true)
        }
        if (candidates.isEmpty()) return@combine null

        val nearest = candidates.minByOrNull {
            proximityEngine.calculateDistanceToSite(loc.lat, loc.lng, it)
        } ?: return@combine null

        val dist = proximityEngine.calculateDistanceToSite(loc.lat, loc.lng, nearest)
        val bearing = LocationUtils.calculateBearing(loc.lat, loc.lng, nearest.latitude, nearest.longitude)
        val domainSite = nearest.toDomain(dist)
        val speedMps = when (mode) {
            LocationMode.BROWSE -> 1.4f
            LocationMode.EXPLORE -> 1.4f
            LocationMode.ROAD_TRIP -> 18.0f
            LocationMode.PASSIVE -> 12.0f
        }
        val etaSec = (dist / speedMps).toInt()
        val horizon = 1500f
        val progress = (1f - (dist / horizon)).coerceIn(0f, 1f)
        val state = proximityEngine.evaluateCrossingState(domainSite, loc.lat, loc.lng)

        CrossingCountdown(
            site = domainSite,
            distanceMeters = dist,
            bearingDegrees = bearing,
            estimatedSeconds = etaSec,
            category = CrossingCategory.HISTORIC_BRIDGE,
            progressPercent = progress,
            state = state
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Closest Crossing Countdown (Whichever is nearer: subterranean void or bridge span)
    val closestCrossingCountdown: StateFlow<CrossingCountdown?> = combine(
        upcomingSubterraneanCrossing,
        upcomingBridgeCrossing
    ) { sub, bridge ->
        when {
            sub == null -> bridge
            bridge == null -> sub
            sub.distanceMeters <= bridge.distanceMeters -> sub
            else -> bridge
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        NotificationHelper.createNotificationChannels(application)
        compassOrientationProvider.startListening()

        viewModelScope.launch {
            val result = repository.syncAuthoritativePack(application)
            _packValidationResult.value = result
            // Automatically register hardware geofences once seeded
            val sites = repository.allSites.first().map { it.toDomain() }
            geofenceRegistrar.registerNearbyGeofences(
                anchorLat = _userLatitude.value,
                anchorLng = _userLongitude.value,
                sites = sites,
                onSuccess = {},
                onError = {}
            )
        }

        // Handle cold-start pending site selection once sites are loaded
        viewModelScope.launch {
            filteredSites.collect { list ->
                val pendingId = _pendingSiteId.value
                if (pendingId != null && list.isNotEmpty()) {
                    val match = list.firstOrNull { it.id == pendingId }
                    if (match != null) {
                        _selectedSite.value = match
                        _pendingSiteId.value = null
                    }
                }
            }
        }

        // Collect live GPS hardware coordinates if active
        viewModelScope.launch {
            liveLocationProvider.currentLocation.collect { loc ->
                if (loc != null) {
                    setUserLocation(loc.latitude, loc.longitude)
                }
            }
        }

        // Re-evaluate geofences immediately whenever sites are loaded or updated from database
        viewModelScope.launch {
            allSitesUnfiltered.collect { sitesList ->
                if (sitesList.isNotEmpty()) {
                    checkGeofences(_userLatitude.value, _userLongitude.value)
                }
            }
        }

        // Auto-start live GPS hardware tracking if permissions are granted
        if (liveLocationProvider.hasLocationPermission()) {
            liveLocationProvider.startLiveTracking()
        }

        // Automatic audio ducking: when narrator speaks, duck ambient synthesizer volume
        viewModelScope.launch {
            narratorManager.isPlaying.collect { speaking ->
                ambientSoundEngine.setDucked(speaking)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectCounty(county: String) {
        _selectedCounty.value = county
    }

    fun selectEra(era: String) {
        _selectedEra.value = era
    }

    fun setProximityRadiusKm(radiusKm: Float) {
        _proximityRadiusKm.value = radiusKm
    }

    fun toggleFavoritesFilter() {
        _filterFavoritesOnly.value = !_filterFavoritesOnly.value
    }

    fun toggleVisitedFilter() {
        _filterVisitedOnly.value = !_filterVisitedOnly.value
    }

    fun openAddCustomDialog() {
        _showAddCustomDialog.value = true
    }

    fun dismissAddCustomDialog() {
        _showAddCustomDialog.value = false
    }

    fun addCustomSite(entity: LoreSiteEntity) {
        viewModelScope.launch {
            repository.insertCustomSite(entity)
            _showAddCustomDialog.value = false
            // Select newly added site
            selectSiteById(entity.id)
        }
    }

    fun deleteCustomSite(id: String) {
        viewModelScope.launch {
            repository.deleteCustomSite(id)
            if (_selectedSite.value?.id == id) {
                _selectedSite.value = null
            }
        }
    }

    fun updateSitePhoto(id: String, photoUri: String?) {
        viewModelScope.launch {
            repository.updatePhotoUri(id, photoUri)
            if (_selectedSite.value?.id == id) {
                _selectedSite.value = _selectedSite.value?.copy(photoUri = photoUri)
            }
        }
    }

    fun openArchivistDialog(siteContext: String? = null) {
        _archivistSiteContext.value = siteContext
        _showArchivistDialog.value = true
    }

    fun dismissArchivistDialog() {
        _showArchivistDialog.value = false
        _archivistSiteContext.value = null
    }

    fun setSpeechRate(rate: Float) {
        narratorManager.setSpeechRate(rate)
    }

    fun restoreOrSyncSeeds() {
        viewModelScope.launch {
            val result = repository.syncAuthoritativePack(getApplication())
            _packValidationResult.value = result
        }
    }

    fun selectSite(site: LoreSite) {
        _selectedSite.value = site
        viewModelScope.launch {
            repository.recordEncounter(site.id)
        }
    }

    fun selectSiteById(siteId: String) {
        viewModelScope.launch {
            val fromFiltered = filteredSites.value.firstOrNull { it.id == siteId }
            if (fromFiltered != null) {
                _selectedSite.value = fromFiltered
                _pendingSiteId.value = null
                repository.recordEncounter(siteId)
                return@launch
            }
            // Direct query from repository to eliminate cold-start race conditions
            val entity = repository.getSiteById(siteId)
            if (entity != null) {
                val distance = proximityEngine.calculateDistanceToSite(
                    _userLatitude.value, _userLongitude.value,
                    entity
                )
                _selectedSite.value = entity.toDomain(distance)
                _pendingSiteId.value = null
                repository.recordEncounter(siteId)
            } else {
                _pendingSiteId.value = siteId
            }
        }
    }

    fun clearSelectedSite() {
        _selectedSite.value = null
    }

    fun selectTome(site: LoreSiteEntity, roman: String) {
        _selectedTomeSite.value = Pair(site, roman)
        viewModelScope.launch {
            repository.recordEncounter(site.id)
        }
    }

    fun clearSelectedTome() {
        _selectedTomeSite.value = null
    }

    fun startExpeditionTrail(trail: ExpeditionTrail) {
        _activeExpeditionTrail.value = trail
    }

    fun clearExpeditionTrail() {
        _activeExpeditionTrail.value = null
    }

    fun openFieldJournal() {
        _showFieldJournal.value = true
        adventureSoundEngine.playTactileClick()
    }

    fun closeFieldJournal() {
        _showFieldJournal.value = false
    }

    fun openViewfinder(site: LoreSite? = null) {
        _viewfinderSite.value = site
        adventureSoundEngine.playSonarPing()
    }

    fun openViewfinderBySiteId(siteId: String?) {
        val site = filteredSites.value.firstOrNull { it.id == siteId }
        _viewfinderSite.value = site
        adventureSoundEngine.playSonarPing()
    }

    fun closeViewfinder() {
        _viewfinderSite.value = null
    }

    fun openCipher(riddle: CipherRiddle) {
        _activeCipherRiddle.value = riddle
        adventureSoundEngine.playTactileClick()
    }

    fun closeCipher() {
        _activeCipherRiddle.value = null
    }

    fun dismissColdStartIntro() {
        _showColdStartIntro.value = false
        prefs.edit().putBoolean(PREF_INTRO_SEEN, true).apply()
        adventureSoundEngine.playTactileClick()
        narratorManager.stop()
    }

    fun replayColdStartIntro() {
        _showColdStartIntro.value = true
        adventureSoundEngine.playLeatherUnclasp()
    }

    fun solveCipher(riddle: CipherRiddle) {
        _activeCipherRiddle.value = null
        _solvedCipherIds.value = _solvedCipherIds.value + riddle.id
        adventureSoundEngine.vibrateSuccess()
        adventureSoundEngine.playDiscoveryChime()
        viewModelScope.launch {
            repository.recordEncounter(riddle.targetSiteId)
        }
    }

    fun openPhonographRadio() {
        _showPhonographRadio.value = true
        adventureSoundEngine.playTactileClick()
    }

    fun closePhonographRadio() {
        _showPhonographRadio.value = false
    }

    fun playWaxCylinder(cylinder: WaxCylinderRecording) {
        adventureSoundEngine.playPhonographNeedleDrop()
        narratorManager.speak(
            siteId = cylinder.id,
            title = cylinder.title,
            text = "${cylinder.title}. Eyewitness account of ${cylinder.eyewitnessName}, ${cylinder.speakerRole}, recorded ${cylinder.recordedDate}. ${cylinder.transcript}"
        )
    }

    fun stopWaxCylinder() {
        narratorManager.stop()
    }

    fun captureViewfinderSnapshot(label: String) {
        adventureSoundEngine.playDiscoveryChime()
        _viewfinderSite.value?.let { site ->
            viewModelScope.launch {
                repository.recordEncounter(site.id)
            }
        }
    }

    fun toggleFavorite(siteId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(siteId, currentFavorite)
            // Update selected site if opened
            if (_selectedSite.value?.id == siteId) {
                _selectedSite.value = _selectedSite.value?.copy(isFavorite = !currentFavorite)
            }
            if (_selectedTomeSite.value?.first?.id == siteId) {
                val currentPair = _selectedTomeSite.value!!
                _selectedTomeSite.value = Pair(currentPair.first.copy(isFavorite = !currentFavorite), currentPair.second)
            }
        }
    }

    fun toggleVisited(siteId: String, currentVisited: Boolean) {
        viewModelScope.launch {
            repository.toggleVisited(siteId, currentVisited)
            if (_selectedSite.value?.id == siteId) {
                _selectedSite.value = _selectedSite.value?.copy(isVisited = !currentVisited)
            }
            if (_selectedTomeSite.value?.first?.id == siteId) {
                val currentPair = _selectedTomeSite.value!!
                _selectedTomeSite.value = Pair(currentPair.first.copy(isVisited = !currentVisited), currentPair.second)
            }
        }
    }

    fun saveUserNotes(siteId: String, notes: String) {
        viewModelScope.launch {
            repository.updateUserNotes(siteId, notes)
            if (_selectedSite.value?.id == siteId) {
                _selectedSite.value = _selectedSite.value?.copy(userNotes = notes)
            }
        }
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLatitude.value = latitude
        _userLongitude.value = longitude
        checkGeofences(latitude, longitude)
    }

    private var dismissedGeofenceSiteId: String? = null

    // Active Multi-Tier Proximity State
    val activeProximityTier: StateFlow<ProximityTier> = combine(
        userLocationState,
        repository.allSites
    ) { loc, sites ->
        if (sites.isEmpty()) return@combine ProximityTier.FAR
        val nearestDist = sites.minOfOrNull { site ->
            LocationUtils.calculateDistanceMeters(loc.lat, loc.lng, site.latitude, site.longitude)
        } ?: Float.MAX_VALUE

        when {
            nearestDist <= 100f -> ProximityTier.IMMEDIATE_TRIGGER
            nearestDist <= 500f -> ProximityTier.APPROACH_NEAR
            nearestDist <= 1500f -> ProximityTier.APPROACH_HORIZON
            else -> ProximityTier.FAR
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProximityTier.FAR)

    private fun checkGeofences(lat: Double, lng: Double) {
        viewModelScope.launch {
            val (evaluations, _) = spatialCoordinator.evaluateUserLocation(lat, lng, 0f)
            for (evaluation in evaluations) {
                if (evaluation.currentTier == ProximityTier.IMMEDIATE_TRIGGER) {
                    val breachEntity = evaluation.site
                    val domainSite = breachEntity.toDomain(evaluation.distanceMeters)
                    _activeGeofenceSite.value = domainSite

                    // Play distinctive acoustic sound cue based on feature type
                    if (breachEntity.radarType.equals("UNDERGROUND", ignoreCase = true) ||
                        breachEntity.whatMayBeUnderground.isNotBlank()) {
                        adventureSoundEngine.playCaveEcho()
                    } else if (breachEntity.radarType.equals("BRIDGE", ignoreCase = true) ||
                               breachEntity.title.contains("Bridge", ignoreCase = true) ||
                               breachEntity.title.contains("Trestle", ignoreCase = true) ||
                               breachEntity.title.contains("Viaduct", ignoreCase = true)) {
                        adventureSoundEngine.playBridgeResonance()
                    } else {
                        adventureSoundEngine.playDiscoveryChime()
                    }

                    // Post Heads-up Notification
                    NotificationHelper.showGeofenceAlertNotification(
                        getApplication(),
                        domainSite
                    )

                    repository.recordEncounter(breachEntity.id)

                    // In Tour Mode, automatically begin rich narration with linked media persona
                    if (_isTourSimulationActive.value || _isForegroundServiceActive.value) {
                        val mediaAsset = evaluation.linkedMediaAsset
                        if (mediaAsset != null) {
                            narratorManager.setPersona(NarratorPersona.fromId(mediaAsset.personaId))
                        }
                        playNarration(domainSite)
                    }
                } else if (evaluation.currentTier == ProximityTier.APPROACH_NEAR && evaluation.isNewTierEntry) {
                    adventureSoundEngine.playSonarPing()
                }
            }
        }
    }

    fun playLinkedMediaAsset(site: LoreSite) {
        viewModelScope.launch {
            val assets = repository.getMediaAssetsForSite(site.id)
            val topAsset = assets.maxByOrNull { it.priority }
            if (topAsset != null) {
                narratorManager.setPersona(NarratorPersona.fromId(topAsset.personaId))
                val speech = if (topAsset.description.isNotBlank()) {
                    topAsset.title + ". " + topAsset.description + " " + site.audioNarration
                } else {
                    site.audioNarration
                }
                narratorManager.speak(site.id, topAsset.title, speech)
                repository.recordListenedAudio(site.id)
            } else {
                playNarration(site)
            }
        }
    }

    fun dismissGeofenceAlert() {
        dismissedGeofenceSiteId = _activeGeofenceSite.value?.id
        _activeGeofenceSite.value = null
    }

    fun playNarration(site: LoreSite) {
        val depth = narratorManager.narrationDepth.value
        val textToSpeak = if (depth == "QUICK_DRIVE") {
            buildString {
                append(site.title).append(". ")
                if (site.subtitle.isNotBlank()) append(site.subtitle).append(". ")
                if (site.youJustCrossedEvent.isNotBlank()) append(site.youJustCrossedEvent).append(". ")
                if (site.whatMayBeUnderground.isNotBlank()) append("Underneath this ground: ").append(site.whatMayBeUnderground).append(". ")
                if (site.driveStory.isNotBlank()) {
                    append(site.driveStory)
                } else {
                    append(site.fullStory.take(300))
                }
            }
        } else {
            com.example.util.SpeechProsodyHelper.buildRichNarration(
                title = site.title,
                subtitle = site.subtitle,
                era = site.era,
                county = site.county,
                fullStory = site.fullStory,
                verifiedHistory = site.verifiedHistory,
                folkloreTradition = site.folkloreTradition,
                whatMayBeUnderground = site.whatMayBeUnderground,
                youJustCrossedEvent = site.youJustCrossedEvent
            )
        }
        narratorManager.speak(site.id, site.title, textToSpeak)
        viewModelScope.launch {
            repository.recordListenedAudio(site.id)
        }
    }

    fun stopNarration() {
        narratorManager.stop()
    }

    fun toggleAmbientTheme(theme: String) {
        if (ambientSoundEngine.isAmbientPlaying.value && ambientSoundEngine.ambientTheme.value == theme) {
            ambientSoundEngine.stopAmbient()
        } else {
            ambientSoundEngine.startAmbient(theme)
        }
    }

    fun stopAmbientSound() {
        ambientSoundEngine.stopAmbient()
    }

    // Scenic Driving Tour simulation route through Floyd County
    private val tourWaypoints = listOf(
        Pair(38.2818, -85.8195), // Ohio Riverfront Amphitheater
        Pair(38.2847, -85.8239), // Town Clock Church
        Pair(38.2838, -85.8252), // Scribner House
        Pair(38.2863, -85.8145), // Culbertson Mansion
        Pair(38.2932, -85.8340), // Falling Run Trestle
        Pair(38.2910, -85.8450), // Silver Hills Overlook
        Pair(38.3320, -85.8190), // Floyd County Poor Farm
        Pair(38.3315, -85.8755), // Floyds Knobs Skyline Drive
        Pair(38.3718, -85.9860)  // Greenville Springs
    )

    fun startTourSimulation() {
        if (_isTourSimulationActive.value) return
        _isTourSimulationActive.value = true
        tourJob = viewModelScope.launch {
            for (pt in tourWaypoints) {
                setUserLocation(pt.first, pt.second)
                delay(4000) // Stay 4 seconds at each waypoint
            }
            _isTourSimulationActive.value = false
        }
    }

    fun stopTourSimulation() {
        tourJob?.cancel()
        _isTourSimulationActive.value = false
    }

    fun jumpToSite(site: LoreSite) {
        setUserLocation(site.latitude, site.longitude)
    }

    fun resetToNewAlbanyCenter() {
        setUserLocation(LocationUtils.DEFAULT_LATITUDE, LocationUtils.DEFAULT_LONGITUDE)
    }

    fun toggleLiveGps(enable: Boolean): Boolean {
        return if (enable) {
            val started = liveLocationProvider.startLiveTracking()
            if (started) {
                stopTourSimulation()
            }
            started
        } else {
            liveLocationProvider.stopLiveTracking()
            false
        }
    }

    fun setLocationMode(mode: LocationMode) {
        _locationMode.value = mode
        prefs.edit().putString("location_mode", mode.name).apply()
        if (_isForegroundServiceActive.value) {
            LoreTourForegroundService.setMode(getApplication(), mode)
        }
    }

    fun startForegroundTourService(mode: LocationMode = _locationMode.value) {
        _locationMode.value = mode
        LoreTourForegroundService.start(getApplication(), mode)
        _isForegroundServiceActive.value = true
    }

    fun stopForegroundTourService() {
        LoreTourForegroundService.stop(getApplication())
        _isForegroundServiceActive.value = false
    }

    fun openBatteryGuide() {
        _showBatteryGuide.value = true
    }

    fun dismissBatteryGuide() {
        _showBatteryGuide.value = false
    }

    fun revalidatePack() {
        viewModelScope.launch {
            val result = repository.revalidatePack(getApplication())
            _packValidationResult.value = result
        }
    }

    fun reloadPackFromAssets() {
        viewModelScope.launch {
            val result = repository.syncAuthoritativePack(getApplication())
            _packValidationResult.value = result
        }
    }

    // --- Oral History Voice Memo Actions ---

    fun openTapeDeckRecorder(siteId: String? = null) {
        expeditionAudio.playBrassToggleSwitch()
        _activeRecordingSiteId.value = siteId
        _showTapeDeckRecorder.value = true
    }

    fun closeTapeDeckRecorder() {
        expeditionAudio.playTactileClick()
        if (audioRecordingManager.isRecording.value) {
            cancelRecordingVoiceMemo()
        }
        _showTapeDeckRecorder.value = false
    }

    fun startRecordingVoiceMemo(siteId: String? = _activeRecordingSiteId.value) {
        val memoId = UUID.randomUUID().toString()
        _activeRecordingDraftId.value = memoId
        _activeRecordingSiteId.value = siteId
        val file = audioRecordingManager.startRecording(memoId)
        _activeRecordingFile.value = file
        if (file != null) {
            adventureSoundEngine.playSonarPing()
        }
    }

    fun stopAndSaveVoiceMemo(
        title: String,
        informantName: String = "",
        fieldNotes: String = ""
    ) {
        val memoId = _activeRecordingDraftId.value ?: return
        val file = _activeRecordingFile.value ?: return
        val durationMs = audioRecordingManager.stopRecording()
        expeditionAudio.playTactileClick()

        val loc = liveLocationProvider.currentLocation.value

        val finalTitle = title.ifBlank {
            val site = _activeRecordingSiteId.value?.let { sid -> allSitesUnfiltered.value.find { it.id == sid } }
            if (site != null) "Interview: ${site.title}" else "Oral History Field Note"
        }

        viewModelScope.launch {
            val memo = LoreVoiceMemoEntity(
                id = memoId,
                siteId = _activeRecordingSiteId.value,
                title = finalTitle,
                informantName = informantName,
                audioFilePath = file.absolutePath,
                durationMs = durationMs,
                timestamp = System.currentTimeMillis(),
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                fieldNotes = fieldNotes
            )
            repository.insertVoiceMemo(memo)
            _activeRecordingDraftId.value = null
            _activeRecordingFile.value = null
        }
    }

    fun cancelRecordingVoiceMemo() {
        audioRecordingManager.cancelRecording()
        _activeRecordingDraftId.value = null
        _activeRecordingFile.value = null
        expeditionAudio.playTactileClick()
    }

    fun playVoiceMemo(memo: LoreVoiceMemoEntity) {
        expeditionAudio.playTactileClick()
        audioPlaybackManager.play(memo.id, memo.audioFilePath)
    }

    fun pauseVoiceMemo() {
        audioPlaybackManager.pause()
    }

    fun resumeVoiceMemo() {
        audioPlaybackManager.resume()
    }

    fun stopVoiceMemo() {
        audioPlaybackManager.stop()
    }

    fun seekVoiceMemo(positionMs: Long) {
        audioPlaybackManager.seekTo(positionMs)
    }

    fun deleteVoiceMemo(memo: LoreVoiceMemoEntity) {
        viewModelScope.launch {
            if (audioPlaybackManager.currentlyPlayingId.value == memo.id) {
                audioPlaybackManager.stop()
            }
            repository.deleteVoiceMemo(memo.id)
            val file = File(memo.audioFilePath)
            if (file.exists()) {
                file.delete()
            }
            expeditionAudio.playTactileClick()
        }
    }

    fun shareVoiceMemo(context: android.content.Context, memo: LoreVoiceMemoEntity) {
        val file = File(memo.audioFilePath)
        if (!file.exists()) return

        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Southern Indiana Lore: ${memo.title}")
                putExtra(android.content.Intent.EXTRA_TEXT, "Oral History Field Recording: ${memo.title}\nInformant: ${memo.informantName}\nNotes: ${memo.fieldNotes}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Field Recording").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        ambientSoundEngine.stopAmbient()
        narratorManager.shutdown()
        compassOrientationProvider.stopListening()
        liveLocationProvider.stopLiveTracking()
        tourJob?.cancel()
        audioRecordingManager.cancelRecording()
        audioPlaybackManager.stop()
    }
}
