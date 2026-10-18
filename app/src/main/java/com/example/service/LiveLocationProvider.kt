package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiveLocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isLiveGpsActive = MutableStateFlow(false)
    val isLiveGpsActive: StateFlow<Boolean> = _isLiveGpsActive.asStateFlow()

    private val _gpsAccuracyMeters = MutableStateFlow<Float?>(null)
    val gpsAccuracyMeters: StateFlow<Float?> = _gpsAccuracyMeters.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                _currentLocation.value = loc
                if (loc.hasAccuracy()) {
                    _gpsAccuracyMeters.value = loc.accuracy
                }
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun startLiveTracking(): Boolean {
        if (!hasLocationPermission()) {
            _isLiveGpsActive.value = false
            return false
        }

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val priority = if (hasFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val request = LocationRequest.Builder(priority, 2500L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(2f)
            .setWaitForAccurateLocation(false)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        _isLiveGpsActive.value = true

        // 1. Check last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                _currentLocation.value = loc
                if (loc.hasAccuracy()) {
                    _gpsAccuracyMeters.value = loc.accuracy
                }
            }
        }

        // 2. Also request an immediate fresh single location fix via getCurrentLocation
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(priority, cts.token).addOnSuccessListener { loc ->
            if (loc != null) {
                _currentLocation.value = loc
                if (loc.hasAccuracy()) {
                    _gpsAccuracyMeters.value = loc.accuracy
                }
            }
        }

        return true
    }

    fun stopLiveTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isLiveGpsActive.value = false
    }
}
