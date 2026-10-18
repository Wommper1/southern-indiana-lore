package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.local.LoreDatabase
import com.example.data.local.LoreSiteEntity
import com.example.data.repository.LoreRepository
import com.example.util.NarratorManager
import com.example.util.NarratorPersona
import com.example.util.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoreTourForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: LoreRepository
    private lateinit var narrator: NarratorManager
    private lateinit var hardwareGeofenceRegistrar: HardwareGeofenceRegistrar

    private val proximityEngine = LocalProximityEngine(debounceCooldownMs = 3 * 60 * 1000L)
    private lateinit var spatialCoordinator: SpatialProximityCoordinator
    private val soundEngine by lazy { com.example.util.AdventureSoundEngine(applicationContext) }
    private var allSites: List<LoreSiteEntity> = emptyList()

    private var isTourActive: Boolean = false
    private var currentMode: LocationMode = LocationMode.ROAD_TRIP
    private var currentSamplingInterval: Long = 10000L

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PLAY_NARRATION = "ACTION_PLAY_NARRATION"
        const val ACTION_STOP_NARRATION = "ACTION_STOP_NARRATION"
        const val ACTION_SET_MODE = "ACTION_SET_MODE"

        const val EXTRA_SITE_ID = "extra_site_id"
        const val EXTRA_SITE_TITLE = "extra_site_title"
        const val EXTRA_NARRATION_TEXT = "extra_narration_text"
        const val EXTRA_LOCATION_MODE = "extra_location_mode"

        var isRunning: Boolean = false
            private set

        var activeMode: LocationMode = LocationMode.ROAD_TRIP
            private set

        fun start(context: Context, mode: LocationMode = LocationMode.ROAD_TRIP) {
            val intent = Intent(context, LoreTourForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LOCATION_MODE, mode.name)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LoreTourForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setMode(context: Context, mode: LocationMode) {
            val intent = Intent(context, LoreTourForegroundService::class.java).apply {
                action = ACTION_SET_MODE
                putExtra(EXTRA_LOCATION_MODE, mode.name)
            }
            context.startService(intent)
        }

        fun playNarration(context: Context, siteId: String, title: String, text: String) {
            val intent = Intent(context, LoreTourForegroundService::class.java).apply {
                action = ACTION_PLAY_NARRATION
                putExtra(EXTRA_SITE_ID, siteId)
                putExtra(EXTRA_SITE_TITLE, title)
                putExtra(EXTRA_NARRATION_TEXT, text)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopNarration(context: Context) {
            val intent = Intent(context, LoreTourForegroundService::class.java).apply {
                action = ACTION_STOP_NARRATION
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        val db = LoreDatabase.getDatabase(applicationContext, serviceScope)
        repository = LoreRepository(db.loreDao())
        spatialCoordinator = SpatialProximityCoordinator(repository)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        hardwareGeofenceRegistrar = HardwareGeofenceRegistrar(this)

        narrator = NarratorManager(applicationContext).apply {
            onPlaybackFinished = {
                // If only playing a single narration without an active tour, gracefully stop service
                if (!isTourActive) {
                    stopSelf()
                }
            }
        }

        serviceScope.launch {
            allSites = repository.allSites.first()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                handleNewLocation(location)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val modeStr = intent?.getStringExtra(EXTRA_LOCATION_MODE)
        if (modeStr != null) {
            try {
                currentMode = LocationMode.valueOf(modeStr)
                activeMode = currentMode
            } catch (_: Exception) {}
        }

        when (intent?.action) {
            ACTION_STOP -> {
                isTourActive = false
                narrator.stop()
                stopTourService()
                return START_NOT_STICKY
            }
            ACTION_PLAY_NARRATION -> {
                val siteId = intent.getStringExtra(EXTRA_SITE_ID) ?: ""
                val title = intent.getStringExtra(EXTRA_SITE_TITLE) ?: ""
                val text = intent.getStringExtra(EXTRA_NARRATION_TEXT) ?: ""
                handleNarrationCommand(siteId, title, text)
            }
            ACTION_STOP_NARRATION -> {
                narrator.stop()
                if (!isTourActive) {
                    stopSelf()
                }
            }
            ACTION_SET_MODE -> {
                if (isTourActive) {
                    requestLocationUpdates()
                }
            }
            ACTION_START, null -> {
                startTourService()
            }
        }
        return START_STICKY
    }

    private fun handleNarrationCommand(siteId: String, title: String, text: String) {
        isRunning = true
        if (!isTourActive) {
            // Service was started specifically for audio narration from a notification
            startForeground(
                NotificationHelper.NOTIFICATION_ID_SERVICE,
                NotificationHelper.buildTourForegroundNotification(
                    this,
                    "Narrating: $title"
                )
            )
        }
        narrator.speak(siteId, title, text)
    }

    private fun startTourService() {
        isRunning = true
        isTourActive = true
        activeMode = currentMode
        val statusSubtitle = when (currentMode) {
            LocationMode.BROWSE -> "Browse Mode • Manual Exploration"
            LocationMode.EXPLORE -> "Explore Mode • On-Foot Walking Tour"
            LocationMode.ROAD_TRIP -> "Road Trip Mode • Movement-Adaptive Tour"
            LocationMode.PASSIVE -> "Passive Mode • Hardware Geofences Active"
        }

        startForeground(
            NotificationHelper.NOTIFICATION_ID_SERVICE,
            NotificationHelper.buildTourForegroundNotification(
                this,
                statusSubtitle
            )
        )
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        fusedLocationClient.removeLocationUpdates(locationCallback)

        val (priority, interval, minDistance) = when (currentMode) {
            LocationMode.BROWSE -> {
                // In browse mode, don't poll continuously in the background
                return
            }
            LocationMode.PASSIVE -> {
                // Passive geofence mode: low-power 2-minute anchor check
                Triple(Priority.PRIORITY_PASSIVE, 120000L, 100f)
            }
            LocationMode.EXPLORE -> {
                // Walking tour: 12 seconds, 10 meters
                Triple(Priority.PRIORITY_HIGH_ACCURACY, 12000L, 10f)
            }
            LocationMode.ROAD_TRIP -> {
                // Movement-aware road trip: initial baseline 10s / 20m
                Triple(Priority.PRIORITY_HIGH_ACCURACY, 10000L, 20f)
            }
        }

        currentSamplingInterval = interval
        val request = LocationRequest.Builder(priority, interval)
            .setMinUpdateIntervalMillis((interval / 2).coerceAtLeast(3000L))
            .setMinUpdateDistanceMeters(minDistance)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun handleNewLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val speedMps = if (location.hasSpeed()) location.speed else 0f

        serviceScope.launch {
            // 1. Spatial bounding box index evaluation and adaptive interval recommendation
            val (tierEvaluations, recommendedInterval) = spatialCoordinator.evaluateUserLocation(lat, lng, speedMps)

            // Dynamic polling interval adaptation:
            if (currentMode == LocationMode.ROAD_TRIP && recommendedInterval != currentSamplingInterval) {
                currentSamplingInterval = recommendedInterval
                adaptLocationSamplingRate(recommendedInterval)
            }

            // Dynamically maintain the 50 hardware geofence page around current anchor (> 5km movement)
            if (hardwareGeofenceRegistrar.shouldRefresh(lat, lng) && allSites.isNotEmpty()) {
                hardwareGeofenceRegistrar.registerNearbyGeofences(
                    anchorLat = lat,
                    anchorLng = lng,
                    sites = allSites.map { it.toDomain() }
                )
            }

            // 2. Multi-tier proximity geofence and media trigger dispatch
            for (evaluation in tierEvaluations) {
                val site = evaluation.site
                val domainSite = site.toDomain(evaluation.distanceMeters)

                when (evaluation.currentTier) {
                    ProximityTier.APPROACH_HORIZON -> {
                        // Horizon Tier (500m - 1.5km): Pre-buffer & subtle acoustic ping
                        soundEngine.playSonarPing()
                        NotificationHelper.updateTourForegroundNotification(
                            applicationContext,
                            "Approaching ${site.title} (~${evaluation.distanceMeters.toInt()}m)"
                        )
                    }

                    ProximityTier.APPROACH_NEAR -> {
                        // Near Approach Tier (150m - 500m): Pre-arrival radio static or chime
                        val asset = evaluation.linkedMediaAsset
                        if (asset != null && asset.mediaType == "RADIO_BROADCAST") {
                            soundEngine.playRadioStaticBurst()
                        } else {
                            soundEngine.playDiscoveryChime()
                        }
                        NotificationHelper.updateTourForegroundNotification(
                            applicationContext,
                            "Near ${site.title} • Spatial Envelope Active"
                        )
                    }

                    ProximityTier.IMMEDIATE_TRIGGER -> {
                        // Immediate Trigger Tier (< 100m or Corridor Crossing):
                        if (site.radarType.equals("UNDERGROUND", ignoreCase = true) || site.whatMayBeUnderground.isNotBlank()) {
                            soundEngine.playCaveEcho()
                        } else if (site.radarType.equals("BRIDGE", ignoreCase = true) ||
                                   site.title.contains("Bridge", ignoreCase = true) ||
                                   site.title.contains("Trestle", ignoreCase = true) ||
                                   site.title.contains("Viaduct", ignoreCase = true)) {
                            soundEngine.playBridgeResonance()
                        } else {
                            soundEngine.playDiscoveryChime()
                        }

                        NotificationHelper.showGeofenceAlertNotification(
                            applicationContext,
                            domainSite
                        )

                        // Record discovery encounter in permanent user journal
                        repository.recordEncounter(site.id)

                        // Rich media playback handoff
                        if (isTourActive) {
                            val mediaAsset = evaluation.linkedMediaAsset
                            val personaToUse = if (mediaAsset != null) {
                                NarratorPersona.fromId(mediaAsset.personaId)
                            } else {
                                narrator.selectedPersona.value
                            }
                            narrator.setPersona(personaToUse)

                            val depth = narrator.narrationDepth.value
                            val textToSpeak = if (mediaAsset != null && mediaAsset.description.isNotBlank()) {
                                mediaAsset.title + ". " + mediaAsset.description + " " + domainSite.audioNarration
                            } else if (depth == "QUICK_DRIVE") {
                                when {
                                    domainSite.youJustCrossedEvent.isNotBlank() -> domainSite.youJustCrossedEvent + ". " + domainSite.driveStory
                                    domainSite.driveStory.isNotBlank() -> domainSite.driveStory
                                    else -> domainSite.audioNarration
                                }
                            } else {
                                com.example.util.SpeechProsodyHelper.buildRichNarration(
                                    title = domainSite.title,
                                    subtitle = domainSite.subtitle,
                                    era = domainSite.era,
                                    county = domainSite.county,
                                    fullStory = domainSite.fullStory,
                                    verifiedHistory = domainSite.verifiedHistory,
                                    folkloreTradition = domainSite.folkloreTradition,
                                    whatMayBeUnderground = domainSite.whatMayBeUnderground,
                                    youJustCrossedEvent = domainSite.youJustCrossedEvent
                                )
                            }

                            narrator.speak(domainSite.id, domainSite.title, textToSpeak)
                            repository.recordListenedAudio(site.id)
                        }
                    }

                    ProximityTier.DEPARTED, ProximityTier.FAR -> {
                        // Leaving trigger zone, entering cooldown
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun adaptLocationSamplingRate(newInterval: Long) {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) return

        val minDistance = if (newInterval <= 6000L) 40f else 20f
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, newInterval)
            .setMinUpdateIntervalMillis(newInterval / 2)
            .setMinUpdateDistanceMeters(minDistance)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopTourService() {
        isRunning = false
        isTourActive = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        isTourActive = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        hardwareGeofenceRegistrar.removeGeofences()
        narrator.shutdown()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
