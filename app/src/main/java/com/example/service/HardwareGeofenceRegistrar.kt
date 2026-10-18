package com.example.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.model.LoreSite
import com.example.util.LocationUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class HardwareGeofenceRegistrar(private val context: Context) {

    companion object {
        const val MAX_ACTIVE_GEOFENCES = 50
        const val RE_REGISTRATION_DISTANCE_THRESHOLD_METERS = 5000f // 5 km
    }

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private var lastRegisteredAnchor: Pair<Double, Double>? = null
    var activeGeofenceCount: Int = 0
        private set

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registerNearbyGeofences(
        anchorLat: Double,
        anchorLng: Double,
        sites: List<LoreSite>,
        onSuccess: (registeredCount: Int) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation || sites.isEmpty()) return

        // Sort sites by proximity to the anchor coordinate and cap at MAX_ACTIVE_GEOFENCES
        val candidateSites = sites
            .sortedBy { site ->
                LocationUtils.calculateDistanceMeters(
                    anchorLat, anchorLng, site.latitude, site.longitude
                )
            }
            .take(MAX_ACTIVE_GEOFENCES)

        val geofenceList = candidateSites.map { site ->
            Geofence.Builder()
                .setRequestId(site.id)
                .setCircularRegion(
                    site.latitude,
                    site.longitude,
                    site.geofenceRadiusMeters.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(10000) // 10 seconds dwell
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        // Clear existing geofences before adding the updated spatial page
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnCompleteListener {
                geofencingClient.addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener {
                        lastRegisteredAnchor = Pair(anchorLat, anchorLng)
                        activeGeofenceCount = candidateSites.size
                        onSuccess(candidateSites.size)
                    }
                    .addOnFailureListener { e -> onError(e) }
            }
    }

    fun shouldRefresh(currentLat: Double, currentLng: Double): Boolean {
        val anchor = lastRegisteredAnchor ?: return true
        val distanceMoved = LocationUtils.calculateDistanceMeters(
            anchor.first, anchor.second, currentLat, currentLng
        )
        return distanceMoved >= RE_REGISTRATION_DISTANCE_THRESHOLD_METERS
    }

    fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
        lastRegisteredAnchor = null
        activeGeofenceCount = 0
    }
}
