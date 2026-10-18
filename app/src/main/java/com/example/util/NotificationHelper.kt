package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.LoreSite
import com.example.service.GeofenceBroadcastReceiver

object NotificationHelper {

    const val CHANNEL_ID_GEOFENCE = "channel_historic_geofence_v3"
    const val CHANNEL_ID_SERVICE = "channel_lore_tour_service_v3"

    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_GEOFENCE_BASE = 2000

    const val ACTION_PLAY_NARRATION = "com.example.ACTION_PLAY_NARRATION"
    const val ACTION_STOP_TOUR = "com.example.ACTION_STOP_TOUR"
    const val EXTRA_SITE_ID = "extra_site_id"
    const val EXTRA_SITE_TITLE = "extra_site_title"
    const val EXTRA_NARRATION_TEXT = "extra_narration_text"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // High importance channel for Geofence Proximity Alerts (Heads-up banner & vibration)
            val geofenceChannel = NotificationChannel(
                CHANNEL_ID_GEOFENCE,
                "Historic Proximity & Cave Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alert notifications when approaching or driving over verified historic sites, caves, and bridges."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Low importance channel for Persistent Driving Tour Foreground Service
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Expedition Tour Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background GPS proximity monitoring while touring."
            }

            notificationManager.createNotificationChannel(geofenceChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun buildTourForegroundNotification(
        context: Context,
        statusText: String = "Monitoring 10 Southern Indiana lore geofences"
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_STOP_TOUR
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
            .setContentTitle("Southern Indiana Lore Tour Active")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_stat_lore_beacon)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Tour", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateTourForegroundNotification(
        context: Context,
        statusText: String
    ) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = buildTourForegroundNotification(context, statusText)
            notificationManager.notify(NOTIFICATION_ID_SERVICE, notification)
        } catch (_: Exception) {}
    }

    fun showGeofenceAlertNotification(
        context: Context,
        site: LoreSite
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // In Android 13+, check POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS not granted; suppressing notification for ${site.id}")
                return
            }
        }

        // Ensure channel exists
        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SITE_ID, site.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, site.id.hashCode(), openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isUnderground = site.radarType.equals("UNDERGROUND", ignoreCase = true) ||
                site.whatMayBeUnderground.isNotBlank() ||
                site.title.contains("Cave", ignoreCase = true) ||
                site.title.contains("Tunnel", ignoreCase = true) ||
                site.title.contains("Cavern", ignoreCase = true)

        val isBridge = site.radarType.equals("BRIDGE", ignoreCase = true) ||
                site.title.contains("Bridge", ignoreCase = true) ||
                site.title.contains("Trestle", ignoreCase = true) ||
                site.title.contains("Viaduct", ignoreCase = true)

        val (radarBadge, defaultPrefix) = when {
            isUnderground -> Pair("🕳️ SUBTERRANEAN VOID BENEATH YOU", "Subterranean cavern / karst river directly beneath your vehicle")
            isBridge -> Pair("🌉 HISTORIC BRIDGE SPAN CROSSING", "Historic river bridge / railway viaduct crossing")
            site.radarType.equals("LOST_LANDSCAPE", ignoreCase = true) -> Pair("🚂 LOST LANDSCAPE", "Abandoned transportation corridor")
            site.radarType.equals("RIVER_MEMORY", ignoreCase = true) -> Pair("🌊 RIVER MEMORY", "Historic ferry or river crossing")
            site.radarType.equals("ROAD_DETECTIVE", ignoreCase = true) -> Pair("🛣️ ROAD DETECTIVE", "Historic route / trace alignment")
            site.radarType.equals("FOLKLORE", ignoreCase = true) -> Pair("👻 LOCAL LORE", "Documented local legend")
            site.radarType.equals("CROSSING", ignoreCase = true) -> Pair("⚡ YOU JUST CROSSED", "Historic corridor boundary")
            else -> Pair("📍 HISTORY NEARBY", "Hidden local history")
        }

        val displayTitle = "$radarBadge: ${site.title}"
        val driveSummary = when {
            site.youJustCrossedEvent.isNotBlank() -> site.youJustCrossedEvent
            site.whatMayBeUnderground.isNotBlank() -> "Directly beneath you: ${site.whatMayBeUnderground}"
            site.driveStory.isNotBlank() -> site.driveStory
            else -> site.subtitle
        }

        // Full rich narration content for playback
        val narrationContent = SpeechProsodyHelper.buildRichNarration(
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

        // Narration PendingIntent
        val narrateIntent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_PLAY_NARRATION
            putExtra(EXTRA_SITE_ID, site.id)
            putExtra(EXTRA_SITE_TITLE, site.title)
            putExtra(EXTRA_NARRATION_TEXT, narrationContent)
        }
        val narratePendingIntent = PendingIntent.getBroadcast(
            context, site.id.hashCode() + 1, narrateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val expandedBody = buildString {
            append(driveSummary)
            if (site.whatMayBeUnderground.isNotBlank() && !driveSummary.contains(site.whatMayBeUnderground)) {
                append("\n\n🕳️ Karst / Subterranean Passage: ").append(site.whatMayBeUnderground)
            }
            append("\n\n").append(site.fullStory.take(300)).append("...")
            if (site.address.isNotBlank()) {
                append("\n\n📍 ").append(site.address)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GEOFENCE)
            .setContentTitle(displayTitle)
            .setContentText(driveSummary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedBody)
            )
            .setSmallIcon(R.drawable.ic_stat_lore_beacon)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 350, 150, 350))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(android.R.drawable.ic_media_play, "Listen (Full Story)", narratePendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Inspect Site", openAppPendingIntent)
            .build()

        val notificationId = NOTIFICATION_ID_GEOFENCE_BASE + (site.id.hashCode() and 0x7FFFFFFF) % 10000
        notificationManager.notify(notificationId, notification)
    }
}
