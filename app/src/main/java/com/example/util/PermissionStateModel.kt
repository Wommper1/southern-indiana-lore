package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class LocationPermissionStage {
    NO_PERMISSIONS,
    FOREGROUND_GRANTED,
    ALL_GRANTED
}

data class PermissionSnapshot(
    val hasForegroundLocation: Boolean,
    val hasBackgroundLocation: Boolean,
    val hasNotifications: Boolean,
    val stage: LocationPermissionStage
)

object PermissionStateModel {

    fun checkPermissions(context: Context): PermissionSnapshot {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasForeground = fineLocation || coarseLocation

        val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val stage = when {
            !hasForeground -> LocationPermissionStage.NO_PERMISSIONS
            !hasBackground -> LocationPermissionStage.FOREGROUND_GRANTED
            else -> LocationPermissionStage.ALL_GRANTED
        }

        return PermissionSnapshot(
            hasForegroundLocation = hasForeground,
            hasBackgroundLocation = hasBackground,
            hasNotifications = hasNotifications,
            stage = stage
        )
    }
}
