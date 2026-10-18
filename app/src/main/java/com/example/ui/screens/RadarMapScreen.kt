package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoreSite
import com.example.ui.components.InteractiveLoreMap
import com.example.ui.components.RadarCompassView
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
fun RadarMapScreen(
    userLat: Double,
    userLng: Double,
    sites: List<LoreSite>,
    selectedSite: LoreSite?,
    isLiveGpsActive: Boolean,
    gpsAccuracyMeters: Float? = null,
    hardwareHeadingDegrees: Float = 0f,
    isCompassAvailable: Boolean = false,
    onToggleLiveGps: (Boolean) -> Unit,
    onSiteSelected: (LoreSite) -> Unit,
    onSetLocation: (Double, Double) -> Unit,
    onResetCenter: () -> Unit,
    onOpenBatteryGuide: () -> Unit,
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf("STREET_MAP") } // "STREET_MAP" or "RADAR"
    var isTelemetryExpanded by remember { mutableStateOf(false) }
    var deepScanMode by remember { mutableStateOf("ALL") } // "ALL", "BRIDGES", "CAVES"

    val displayedSites = remember(sites, deepScanMode) {
        when (deepScanMode) {
            "BRIDGES" -> sites.filter {
                it.radarType.equals("BRIDGE", ignoreCase = true) ||
                it.title.contains("Bridge", ignoreCase = true) ||
                it.title.contains("Trestle", ignoreCase = true) ||
                it.title.contains("Viaduct", ignoreCase = true)
            }
            "CAVES" -> sites.filter {
                it.radarType.equals("UNDERGROUND", ignoreCase = true) ||
                it.whatMayBeUnderground.isNotBlank() ||
                it.title.contains("Cave", ignoreCase = true) ||
                it.title.contains("Cavern", ignoreCase = true) ||
                it.title.contains("Tunnel", ignoreCase = true)
            }
            else -> sites
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateNight)
            .testTag("radar_map_screen")
    ) {
        // Top Header Control Bar: Segmented Switch & Live Telemetry Pill
        Surface(
            color = SlateNightElevated,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .animateContentSize()
            ) {
                // Row 1: Mode Switcher & Expandable Telemetry Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segmented Mode Toggle (Street & Topo Map vs Tactical Radar)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewMode = "STREET_MAP" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewMode == "STREET_MAP") LanternAmber else Color.Transparent
                            ),
                            shape = RoundedCornerShape(9.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                tint = if (viewMode == "STREET_MAP") Color(0xFF1E1100) else ParchmentMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Street Map",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (viewMode == "STREET_MAP") Color(0xFF1E1100) else ParchmentPaper
                            )
                        }

                        Button(
                            onClick = { viewMode = "RADAR" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewMode == "RADAR") LanternAmber else Color.Transparent
                            ),
                            shape = RoundedCornerShape(9.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                Icons.Default.Radar,
                                contentDescription = null,
                                tint = if (viewMode == "RADAR") Color(0xFF1E1100) else ParchmentMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Radar",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (viewMode == "RADAR") Color(0xFF1E1100) else ParchmentPaper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // GPS / Simulation Indicator Pill (Clickable to toggle settings panel)
                    Surface(
                        onClick = { isTelemetryExpanded = !isTelemetryExpanded },
                        color = if (isLiveGpsActive) KnobstoneForest.copy(alpha = 0.15f) else LanternAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLiveGpsActive) KnobstoneForest else LanternAmber.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLiveGpsActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                contentDescription = null,
                                tint = if (isLiveGpsActive) KnobstoneForest else LanternAmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveGpsActive) "GPS" else "SIM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isLiveGpsActive) KnobstoneForest else LanternAmberBright
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = if (isTelemetryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ParchmentMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Tactical Deep Scan Filter Chips
                // Chips keep their natural single-line width and the row scrolls sideways when the
                // screen is narrow (weighted chips used to chop "Special Bridges" in half).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.FilterChip(
                        selected = deepScanMode == "ALL",
                        onClick = {
                            deepScanMode = "ALL"
                            soundEngine?.playSonarPing()
                        },
                        label = { Text("All Scans", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LanternAmber,
                            selectedLabelColor = Color(0xFF1E1100),
                            containerColor = Color(0xFF0F172A),
                            labelColor = ParchmentPaper
                        )
                    )

                    androidx.compose.material3.FilterChip(
                        selected = deepScanMode == "BRIDGES",
                        onClick = {
                            deepScanMode = "BRIDGES"
                            soundEngine?.playBridgeResonance()
                        },
                        label = { Text("🌉 Special Bridges", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LanternAmber,
                            selectedLabelColor = Color(0xFF1E1100),
                            containerColor = Color(0xFF0F172A),
                            labelColor = ParchmentPaper
                        )
                    )

                    androidx.compose.material3.FilterChip(
                        selected = deepScanMode == "CAVES",
                        onClick = {
                            deepScanMode = "CAVES"
                            soundEngine?.playCaveEcho()
                        },
                        label = { Text("🕳️ Karst & Caves", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF06B6D4),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF0F172A),
                            labelColor = ParchmentPaper
                        )
                    )
                }

                // Expandable Telemetry & Jump Coordinates Tray
                if (isTelemetryExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isLiveGpsActive) "S25 ULTRA HARDWARE GPS ACTIVE" else "GPS TELEMETRY (SIMULATION / PRESET)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = if (isLiveGpsActive) KnobstoneForest else LanternAmberBright
                                )
                                val telemetrySubtitle = buildString {
                                    append("${String.format("%.4f", userLat)}° N, ${String.format("%.4f", userLng)}° W")
                                    if (isLiveGpsActive && gpsAccuracyMeters != null) {
                                        append(" • Acc: ±${gpsAccuracyMeters.toInt()}m")
                                    }
                                }
                                Text(
                                    text = telemetrySubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentPaper
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isLiveGpsActive,
                                    onCheckedChange = { onToggleLiveGps(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = KnobstoneForest,
                                        uncheckedThumbColor = ParchmentMuted,
                                        uncheckedTrackColor = Color(0xFF1E293B)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = onOpenBatteryGuide,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Battery",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ParchmentPaper
                                    )
                                }
                            }
                        }

                        // Topographic Elevation Altimeter & Geological Stratum HUD
                        val currentElev = remember(userLat, userLng) {
                            TopoElevationEngine.getElevationFeet(userLat, userLng)
                        }
                        val currentStratum = remember(currentElev) {
                            TopoElevationEngine.getStratum(currentElev)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF1E150D),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8963E).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(currentStratum.iconEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "KNOBSTONE ALTIMETER",
                                            fontSize = 9.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE5B358)
                                        )
                                        Text(
                                            text = "${currentStratum.displayName} (${currentStratum.ageEra})",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFDE68A)
                                        )
                                    }
                                }
                                Text(
                                    text = "$currentElev FT MSL",
                                    fontSize = 13.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF3C853)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Jump Coordinates Preset:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item { PresetLocationChip("New Albany Civic Core", 38.2856, -85.8241, onSetLocation) }
                            item { PresetLocationChip("High & State (Sheet 3)", 38.2863, -85.8228, onSetLocation) }
                            item { PresetLocationChip("Spring & Pearl (Sheet 4)", 38.2872, -85.8219, onSetLocation) }
                            item { PresetLocationChip("Main & Bank (Sheet 6)", 38.2851, -85.8239, onSetLocation) }
                            item { PresetLocationChip("Market & Upper 3rd", 38.2858, -85.8205, onSetLocation) }
                            item { PresetLocationChip("Rose Island Ruins", 38.4239, -85.6268, onSetLocation) }
                            item { PresetLocationChip("Duncan Tunnel", 38.3619, -85.9080, onSetLocation) }
                            item { PresetLocationChip("Squire Boone Caverns", 38.0772, -86.1770, onSetLocation) }
                            item { PresetLocationChip("Wyandotte Cave", 38.2275, -86.2977, onSetLocation) }
                            item { PresetLocationChip("Battle of Corydon", 38.2122, -86.1264, onSetLocation) }
                            item { PresetLocationChip("Clark's Point Overlook", 38.2831, -85.7876, onSetLocation) }
                            item { PresetLocationChip("Floyds Knobs", 38.3315, -85.8755, onSetLocation) }
                            item { PresetLocationChip("Silver Hills", 38.2910, -85.8450, onSetLocation) }
                            item { PresetLocationChip("Greenville Springs", 38.3718, -85.9860, onSetLocation) }
                        }
                    }
                }
            }
        }

        // Dedicated Primary Viewport: Smooth Full Fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Crossfade(targetState = viewMode, label = "map_radar_crossfade") { mode ->
                if (mode == "STREET_MAP") {
                    InteractiveLoreMap(
                        userLat = userLat,
                        userLng = userLng,
                        sites = displayedSites,
                        selectedSite = selectedSite,
                        onSiteSelected = onSiteSelected,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    RadarCompassView(
                        userLat = userLat,
                        userLng = userLng,
                        sites = displayedSites,
                        selectedSite = selectedSite,
                        onSiteSelected = onSiteSelected,
                        hardwareHeadingDegrees = hardwareHeadingDegrees,
                        isCompassAvailable = isCompassAvailable,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bottom Horizon Proximity Drawer: Quick Access to Nearest Beacons
        Surface(
            color = SlateNightElevated,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LanternAmberBright)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (deepScanMode == "BRIDGES") "HISTORIC BRIDGES & TRESTLES" else if (deepScanMode == "CAVES") "KARST CAVERNS & SUBTERRANEAN VOIDS" else "NEAREST LORE BEACONS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (deepScanMode == "CAVES") Color(0xFF06B6D4) else LanternAmber
                        )
                    }
                    Text(
                        text = "${displayedSites.size} detected",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayedSites.take(8), key = { it.id }) { site ->
                        val bearing = LocationUtils.calculateBearing(userLat, userLng, site.latitude, site.longitude)
                        val isSelected = selectedSite?.id == site.id

                        Card(
                            onClick = { onSiteSelected(site) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF272115) else Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) LanternAmberBright else SlateBorder
                            ),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(LanternAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ParchmentPaper,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${LocationUtils.formatDistance(site.distanceMeters)} • ${bearing.toInt()}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LanternAmberBright
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetLocationChip(
    name: String,
    lat: Double,
    lng: Double,
    onSetLocation: (Double, Double) -> Unit
) {
    Surface(
        onClick = { onSetLocation(lat, lng) },
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = LanternAmber,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = ParchmentPaper,
                fontSize = 11.sp
            )
        }
    }
}
