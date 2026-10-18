package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Stop
import com.example.data.model.ExpeditionTrail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CrossingCategory
import com.example.data.model.CrossingCountdown
import com.example.data.model.LoreSite
import com.example.service.LocationMode
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.LocationConfidenceBadge
import com.example.ui.components.TopoCrossSectionProfile
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.util.LocationUtils
import com.example.util.TopoElevationEngine

@Composable
fun TourModeScreen(
    isTourActive: Boolean,
    onStartTour: () -> Unit,
    onStopTour: () -> Unit,
    isForegroundTourActive: Boolean,
    onToggleForegroundTour: (Boolean) -> Unit,
    currentLocationMode: LocationMode = LocationMode.ROAD_TRIP,
    onLocationModeChanged: (LocationMode) -> Unit = {},
    onOpenBatteryGuide: () -> Unit,
    sites: List<LoreSite>,
    onSiteClick: (LoreSite) -> Unit,
    userLat: Double,
    userLng: Double,
    upcomingSubterraneanCrossing: CrossingCountdown? = null,
    upcomingBridgeCrossing: CrossingCountdown? = null,
    activeExpeditionTrail: ExpeditionTrail? = null,
    onClearExpeditionTrail: () -> Unit = {},
    onOpenViewfinder: ((LoreSite) -> Unit)? = null,
    onOpenFieldJournal: (() -> Unit)? = null,
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCarModeEnabled by remember { mutableStateOf(false) }
    var turnByTurnAudioCues by remember { mutableStateOf(true) }

    // Identify closest upcoming site along the drive
    val nearestSite = remember(sites, userLat, userLng) {
        sites.minByOrNull { LocationUtils.calculateDistanceMeters(userLat, userLng, it.latitude, it.longitude) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tour_mode_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Curated Expedition Trail Banner (if launched from Athenaeum/Passport)
        if (activeExpeditionTrail != null) {
            item {
                Surface(
                    color = Color(0xFF1E1100),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LanternAmberBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(LanternAmberBright.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, LanternAmberBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "ACTIVE EXPEDITION TRAIL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = LanternAmberBright
                                    )
                                    Text(
                                        text = activeExpeditionTrail.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onClearExpeditionTrail,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Active Trail",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${activeExpeditionTrail.orderedSiteIds.size} Curated Trail Waypoints • ${activeExpeditionTrail.thematicEra}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }
        }

        // Topographic Knobstone Escarpment Relief & Elevation Profile HUD
        item(key = "topo_elevation_profile_item") {
            val trailSites = remember(activeExpeditionTrail, sites) {
                if (activeExpeditionTrail != null) {
                    val siteMap = sites.associateBy { it.id }
                    activeExpeditionTrail.orderedSiteIds.mapNotNull { siteMap[it] }
                } else {
                    emptyList()
                }
            }

            val topoTelemetry = remember(userLat, userLng, trailSites, sites) {
                if (trailSites.isNotEmpty()) {
                    TopoElevationEngine.generateTrailProfile(
                        startLat = trailSites.first().latitude,
                        startLng = trailSites.first().longitude,
                        endLat = trailSites.last().latitude,
                        endLng = trailSites.last().longitude,
                        waypoints = trailSites
                    )
                } else {
                    val dest = nearestSite ?: sites.firstOrNull()
                    val destLat = dest?.latitude ?: 38.3180
                    val destLng = dest?.longitude ?: -85.9080
                    TopoElevationEngine.generateTrailProfile(
                        startLat = userLat,
                        startLng = userLng,
                        endLat = destLat,
                        endLng = destLng,
                        waypoints = sites.take(4)
                    )
                }
            }

            TopoCrossSectionProfile(
                telemetry = topoTelemetry,
                title = if (activeExpeditionTrail != null) "${activeExpeditionTrail.title} Topo Profile" else "Knobstone Escarpment Relief Profile",
                soundEngine = soundEngine
            )
        }

        // Car Mode Banner Switch (High-contrast, Large Touch Targets, Turn-by-Turn Cues)
        item {
            Surface(
                color = if (isCarModeEnabled) Color(0xFF0F172A) else SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isCarModeEnabled) LanternAmberBright else SlateBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isCarModeEnabled) LanternAmber else Color(0x33F59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = if (isCarModeEnabled) Color(0xFF1E1100) else LanternAmberBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "COCKPIT CAR MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = LanternAmberBright
                                )
                                Text(
                                    text = if (isCarModeEnabled) "High-Contrast HUD Active" else "Standard Driver Layout",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        Switch(
                            checked = isCarModeEnabled,
                            onCheckedChange = { isCarModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LanternAmberBright,
                                checkedTrackColor = LanternAmber.copy(alpha = 0.5f),
                                uncheckedThumbColor = ParchmentMuted,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("car_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (turnByTurnAudioCues) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                tint = if (turnByTurnAudioCues) KnobstoneForest else ParchmentMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Turn-by-turn voice cue chime",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentPaper
                            )
                        }

                        Switch(
                            checked = turnByTurnAudioCues,
                            onCheckedChange = { turnByTurnAudioCues = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KnobstoneForest,
                                checkedTrackColor = KnobstoneForest.copy(alpha = 0.4f),
                                uncheckedThumbColor = ParchmentMuted,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    // Cockpit Car HUD Quick Target if Car Mode is ON
                    if (isCarModeEnabled && nearestSite != null) {
                        val distNearest = LocationUtils.calculateDistanceMeters(userLat, userLng, nearestSite.latitude, nearestSite.longitude)
                        val bearingNearest = LocationUtils.calculateBearing(userLat, userLng, nearestSite.latitude, nearestSite.longitude)

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color(0xFF090D16),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmberBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                val (radarBadge, badgeColor) = when (nearestSite.radarType.uppercase()) {
                                    "UNDERGROUND" -> Pair("🕳️ BENEATH YOU", Color(0xFF06B6D4))
                                    "BRIDGE" -> Pair("🌉 HISTORIC BRIDGE SPAN", Color(0xFFF59E0B))
                                    "LOST_LANDSCAPE" -> Pair("🚂 LOST LANDSCAPE", Color(0xFFF97316))
                                    "RIVER_MEMORY" -> Pair("🌊 RIVER MEMORY", Color(0xFF3B82F6))
                                    "ROAD_DETECTIVE" -> Pair("🛣️ ROAD DETECTIVE", Color(0xFFEAB308))
                                    "FOLKLORE" -> Pair("👻 LOCAL LORE", Color(0xFFA855F7))
                                    "CROSSING" -> Pair("⚡ CROSSING", Color(0xFFEC4899))
                                    else -> Pair("NEXT APPROACH BEACON", LanternAmberBright)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = radarBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = badgeColor
                                    )
                                    if (nearestSite.isSensitiveLocation || !nearestSite.isExactCoordinateSafe) {
                                        Text(
                                            text = "🛡️ SENSITIVE SITE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ConfidenceBadge(
                                        confidenceLevel = nearestSite.confidenceLevel,
                                        isCompact = true
                                    )
                                    LocationConfidenceBadge(
                                        siteType = nearestSite.siteType,
                                        isSensitiveLocation = nearestSite.isSensitiveLocation,
                                        isExactCoordinateSafe = nearestSite.isExactCoordinateSafe,
                                        isCompact = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = nearestSite.title,
                                    fontFamily = com.example.ui.theme.CinzelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    lineHeight = 26.sp,
                                    color = ParchmentPaper
                                )

                                val driveText = when {
                                    nearestSite.whatMayBeUnderground.isNotBlank() && nearestSite.radarType.equals("UNDERGROUND", ignoreCase = true) ->
                                        "🕳️ SUBTERRANEAN VOID: ${nearestSite.whatMayBeUnderground}"
                                    nearestSite.driveStory.isNotBlank() -> nearestSite.driveStory
                                    else -> nearestSite.subtitle
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = driveText,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = Color(0xFFE2E8F0)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${LocationUtils.formatDistance(distNearest)} • Bearing ${bearingNearest.toInt()}°",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = KnobstoneForest
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (onOpenViewfinder != null) {
                                            OutlinedButton(
                                                onClick = { onOpenViewfinder(nearestSite) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Viewfinder", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { onSiteClick(nearestSite) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmberBright),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Dossier", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                        onClick = {
                                            try {
                                                val gmmIntentUri = Uri.parse("google.navigation:q=${nearestSite.latitude},${nearestSite.longitude}")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                    setPackage("com.google.android.apps.maps")
                                                }
                                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(mapIntent)
                                                } else {
                                                    val webIntent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${nearestSite.latitude},${nearestSite.longitude}")
                                                    )
                                                    context.startActivity(webIntent)
                                                }
                                            } catch (_: Exception) {
                                                try {
                                                    val webIntent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${nearestSite.latitude},${nearestSite.longitude}")
                                                    )
                                                    context.startActivity(webIntent)
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LanternAmber,
                                            contentColor = Color(0xFF1E1100)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Drive in Maps", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Real Highway GPS Driving Tour Card (Foreground Service & Push Notifications)
    item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isForegroundTourActive) KnobstoneForest else LanternAmberBright
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isForegroundTourActive) KnobstoneForest.copy(alpha = 0.2f)
                                        else LanternAmber.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isForegroundTourActive) KnobstoneForest else LanternAmberBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LIVE S25 ULTRA BACKGROUND TOUR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = if (isForegroundTourActive) KnobstoneForest else LanternAmberBright
                                )
                                Text(
                                    text = "Hands-Free Drive Alerts",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        Surface(
                            color = if (isForegroundTourActive) KnobstoneForest.copy(alpha = 0.2f) else Color(0x3364748B),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isForegroundTourActive) KnobstoneForest else SlateBorder
                            )
                        ) {
                            Text(
                                text = if (isForegroundTourActive) "ACTIVE" else "READY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isForegroundTourActive) KnobstoneForest else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Keeps your Galaxy S25 Ultra GPS active with the screen off or running navigation. As you enter historic site geofences, it sends heads-up push notifications and speaks the lore story through your speakers.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = ParchmentMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "LOCATION SAMPLING MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = LanternAmberBright
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = currentLocationMode == LocationMode.ROAD_TRIP,
                            onClick = { onLocationModeChanged(LocationMode.ROAD_TRIP) },
                            label = { Text("Driving", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color(0xFF1E1100),
                                containerColor = SlateNightElevated,
                                labelColor = ParchmentPaper
                            ),
                            modifier = Modifier.testTag("mode_road_trip")
                        )
                        FilterChip(
                            selected = currentLocationMode == LocationMode.EXPLORE,
                            onClick = { onLocationModeChanged(LocationMode.EXPLORE) },
                            label = { Text("Walking", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color(0xFF1E1100),
                                containerColor = SlateNightElevated,
                                labelColor = ParchmentPaper
                            ),
                            modifier = Modifier.testTag("mode_explore")
                        )
                        FilterChip(
                            selected = currentLocationMode == LocationMode.PASSIVE,
                            onClick = { onLocationModeChanged(LocationMode.PASSIVE) },
                            label = { Text("Passive", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color(0xFF1E1100),
                                containerColor = SlateNightElevated,
                                labelColor = ParchmentPaper
                            ),
                            modifier = Modifier.testTag("mode_passive")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentLocationMode.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isForegroundTourActive) soundEngine?.playTactileClick()
                                else soundEngine?.playSpringMotorWinding()
                                onToggleForegroundTour(!isForegroundTourActive)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("foreground_tour_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isForegroundTourActive) Color(0xFFDC2626) else KnobstoneForest,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isForegroundTourActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isForegroundTourActive) "Stop Live Tour" else "Start Live Drive",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenBatteryGuide,
                            modifier = Modifier.height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                        ) {
                            Icon(
                                Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Battery Tips",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentPaper
                            )
                        }
                    }
                }
            }
        }

        // Virtual Tour Simulation Banner
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = LanternAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DESK & TABLETOP MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = LanternAmber
                                )
                                Text(
                                    text = "Virtual Scenic Byway Simulation",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isTourActive) {
                                    soundEngine?.playTactileClick()
                                    onStopTour()
                                } else {
                                    soundEngine?.playSpringMotorWinding()
                                    onStartTour()
                                }
                            },
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("tour_toggle_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTourActive) Color(0xFFDC2626) else LanternAmber,
                                contentColor = if (isTourActive) Color.White else Color(0xFF1E1100)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isTourActive) "Stop Sim" else "Simulate Route",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Subterranean & Bridge Crossings Proximity Countdown
        if (upcomingSubterraneanCrossing != null || upcomingBridgeCrossing != null) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CROSSING RADAR & COUNTDOWN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LanternAmberBright
                        )
                        Text(
                            text = "Live GPS Telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KnobstoneForest
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    upcomingSubterraneanCrossing?.let { countdown ->
                        CrossingCountdownCard(
                            countdown = countdown,
                            onSiteClick = onSiteClick,
                            accentColor = Color(0xFF06B6D4),
                            modifier = Modifier.testTag("subterranean_countdown_card")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    upcomingBridgeCrossing?.let { countdown ->
                        CrossingCountdownCard(
                            countdown = countdown,
                            onSiteClick = onSiteClick,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.testTag("bridge_countdown_card")
                        )
                    }
                }
            }
        }

        // Waypoints Route Header

        item {
            val totalDistanceMeters = remember(sites, userLat, userLng) {
                var sum = 0.0
                for (i in 0 until sites.size - 1) {
                    sum += LocationUtils.calculateDistanceMeters(
                        sites[i].latitude, sites[i].longitude,
                        sites[i + 1].latitude, sites[i + 1].longitude
                    )
                }
                sum
            }
            val formattedTotalDist = LocationUtils.formatDistance(totalDistanceMeters.toFloat())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOUR WAYPOINT ITINERARY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LanternAmber
                )
                Text(
                    text = "${sites.size} Waypoints • ~$formattedTotalDist trail",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentMuted
                )
            }
        }

        // Tour Route Steps
        itemsIndexed(sites, key = { _, s -> s.id }) { index, site ->
            val dist = LocationUtils.calculateDistanceMeters(userLat, userLng, site.latitude, site.longitude)
            val isWithinGeofence = dist <= site.geofenceRadiusMeters

            Card(
                onClick = { onSiteClick(site) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isWithinGeofence) Color(0xFF1C2D24) else SlateNightElevated
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isWithinGeofence) KnobstoneForest else SlateBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isWithinGeofence) KnobstoneForest else Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isWithinGeofence) Color(0xFF062312) else LanternAmber
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = site.title,
                                fontFamily = com.example.ui.theme.CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ParchmentPaper
                            )
                            if (isWithinGeofence) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KnobstoneForest,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "NEARBY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${site.era} • ${site.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ConfidenceBadge(
                                confidenceLevel = site.confidenceLevel,
                                isCompact = true
                            )
                            LocationConfidenceBadge(
                                siteType = site.siteType,
                                isSensitiveLocation = site.isSensitiveLocation,
                                isExactCoordinateSafe = site.isExactCoordinateSafe,
                                isCompact = true
                            )
                            if (site.radarType.equals("UNDERGROUND", ignoreCase = true) || site.whatMayBeUnderground.isNotBlank()) {
                                Surface(
                                    color = Color(0x2206B6D4),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🕳️ SUBTERRANEAN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF22D3EE),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (site.radarType.equals("BRIDGE", ignoreCase = true) ||
                                site.title.contains("Bridge", ignoreCase = true) ||
                                site.title.contains("Trestle", ignoreCase = true) ||
                                site.title.contains("Viaduct", ignoreCase = true)) {
                                Surface(
                                    color = Color(0x22F59E0B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🌉 HISTORIC BRIDGE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = LanternAmberBright,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (isWithinGeofence) KnobstoneForest.copy(alpha = 0.2f) else LanternAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = LocationUtils.formatDistance(dist),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isWithinGeofence) KnobstoneForest else LanternAmberBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        IconButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${site.latitude},${site.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${site.latitude},${site.longitude}")
                                    )
                                    context.startActivity(webIntent)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Navigate in Maps",
                                tint = LanternAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrossingCountdownCard(
    countdown: CrossingCountdown,
    onSiteClick: (LoreSite) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (countdown.isImminent) accentColor else SlateBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${countdown.category.icon} ${countdown.category.label.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = accentColor
                    )
                }

                Surface(
                    color = if (countdown.isImminent) accentColor.copy(alpha = 0.2f) else Color(0x3364748B),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (countdown.isImminent) accentColor else SlateBorder
                    )
                ) {
                    Text(
                        text = if (countdown.distanceMeters <= countdown.site.geofenceRadiusMeters) "CROSSING NOW" else countdown.state.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = if (countdown.isImminent) accentColor else ParchmentMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = countdown.site.title,
                fontFamily = com.example.ui.theme.CinzelFontFamily,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ParchmentPaper
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Real-Time Distance & ETA Countdown Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = countdown.formattedDistance,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = accentColor
                    )
                    Text(
                        text = "${countdown.formattedEta} • Bearing ${countdown.bearingDegrees.toInt()}°",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ParchmentMuted
                    )
                }

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "${(countdown.progressPercent * 100).toInt()}% Approach",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentPaper,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { countdown.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = Color(0xFF1E293B)
            )

            // Geologic / Structural details
            val detailSnippet = when {
                countdown.category == CrossingCategory.SUBTERRANEAN_VOID && countdown.site.whatMayBeUnderground.isNotBlank() ->
                    "🕳️ Karst Strata: ${countdown.site.whatMayBeUnderground}"
                countdown.site.driveStory.isNotBlank() -> countdown.site.driveStory
                else -> countdown.site.subtitle
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detailSnippet,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = ParchmentMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onSiteClick(countdown.site) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmberBright),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Dossier", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        try {
                            val uri = Uri.parse("google.navigation:q=${countdown.site.latitude},${countdown.site.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${countdown.site.latitude},${countdown.site.longitude}")
                                )
                                context.startActivity(webIntent)
                            }
                        } catch (_: Exception) {
                            try {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${countdown.site.latitude},${countdown.site.longitude}")
                                )
                                context.startActivity(webIntent)
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Drive in Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
