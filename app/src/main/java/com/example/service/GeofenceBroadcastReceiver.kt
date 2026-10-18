package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.LoreDatabase
import com.example.data.repository.LoreRepository
import com.example.util.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            NotificationHelper.ACTION_PLAY_NARRATION -> {
                val siteId = intent.getStringExtra(NotificationHelper.EXTRA_SITE_ID) ?: ""
                val title = intent.getStringExtra(NotificationHelper.EXTRA_SITE_TITLE) ?: ""
                val text = intent.getStringExtra(NotificationHelper.EXTRA_NARRATION_TEXT) ?: ""
                if (text.isNotBlank()) {
                    LoreTourForegroundService.playNarration(context, siteId, title, text)
                }
            }
            NotificationHelper.ACTION_STOP_TOUR -> {
                LoreTourForegroundService.stop(context)
            }
            else -> {
                // Hardware Geofence event from Google Play Services
                val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
                if (geofencingEvent.hasError()) {
                    val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                    return
                }

                val transition = geofencingEvent.geofenceTransition
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                    val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
                    val scope = CoroutineScope(Dispatchers.IO)
                    val db = LoreDatabase.getDatabase(context.applicationContext, scope)
                    val repository = LoreRepository(db.loreDao())

                    scope.launch {
                        for (geofence in triggeringGeofences) {
                            val siteId = geofence.requestId
                            val siteEntity = repository.getSiteById(siteId)
                            if (siteEntity != null) {
                                NotificationHelper.showGeofenceAlertNotification(
                                    context,
                                    siteEntity.toDomain()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
