package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KarstHydrophoneRegistry
import com.example.data.model.KarstStratum
import com.example.data.model.KarstWellSpecimen
import com.example.data.model.LoggedSoundingEntry
import com.example.ui.components.HydrophoneWinchView
import com.example.ui.theme.CinzelFontFamily
import com.example.util.ExpeditionAudioEngine
import java.util.Locale

/**
 * Subterranean Karst Hydrophone & Sonar Sounding Station.
 * Full-screen standalone field instrument station for probing Southern Indiana's
 * sunken aquifers, limestone siphons, and subterranean river channels.
 */
@Composable
fun KarstHydrophoneScreen(
    audioEngine: ExpeditionAudioEngine?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var selectedWell by remember { mutableStateOf(KarstHydrophoneRegistry.LOST_RIVER_SIPHON) }
    var currentDepthFt by remember { mutableFloatStateOf(0f) }
    var isWinchLocked by remember { mutableStateOf(false) }
    var isHydrophoneListening by remember { mutableStateOf(false) }
    var logToastMessage by remember { mutableStateOf<String?>(null) }

    val loggedSoundings = remember { mutableStateListOf<LoggedSoundingEntry>() }

    // Start/Stop hydrophone subterranean acoustic stream on toggle
    DisposableEffect(isHydrophoneListening) {
        if (isHydrophoneListening) {
            audioEngine?.startSubterraneanHydrophoneAudio()
        } else {
            audioEngine?.stopSubterraneanHydrophoneAudio()
        }
        onDispose {
            audioEngine?.stopSubterraneanHydrophoneAudio()
        }
    }

    val activeStratum = remember(selectedWell, currentDepthFt) {
        KarstHydrophoneRegistry.findStratumAtDepth(selectedWell, currentDepthFt)
    }

    val depthRatio = (currentDepthFt / selectedWell.maxSoundingDepthFt).coerceIn(0f, 1f)
    val isSubmerged = currentDepthFt >= selectedWell.waterTableDepthFt

    Surface(
        color = Color(0xFF0A0704),
        modifier = modifier
            .fillMaxSize()
            .testTag("karst_hydrophone_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Station Navigation Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                audioEngine?.playTactileClick()
                                onNavigateBack()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return to Field Desk",
                                tint = Color(0xFFF3C853)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "KARST HYDROPHONE PROBE",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFFDE68A)
                            )
                            Text(
                                text = "Subterranean Karst Sounding Station • Lost River Aquifer",
                                fontSize = 10.sp,
                                color = Color(0xFFB5A48B)
                            )
                        }
                    }

                    // Hydrophone Listening Toggle
                    Surface(
                        color = if (isHydrophoneListening) Color(0xFF0E3A4A) else Color(0xFF1E140B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isHydrophoneListening) Color(0xFF06B6D4) else Color(0xFF8B6428)
                        ),
                        modifier = Modifier.clickable {
                            isHydrophoneListening = !isHydrophoneListening
                            if (isHydrophoneListening) {
                                audioEngine?.playSonarPing()
                            } else {
                                audioEngine?.playTactileClick()
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Toggle Hydrophone Stream",
                                tint = if (isHydrophoneListening) Color(0xFF22D3EE) else Color(0xFFD4C3A3),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHydrophoneListening) "MONITOR ON" else "MONITOR OFF",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = if (isHydrophoneListening) Color(0xFF67E8F9) else Color(0xFFB5A48B)
                            )
                        }
                    }
                }
            }

            // Historical Well & Sinkhole Selector Drawer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KarstHydrophoneRegistry.allWells.forEach { well ->
                        val isSelected = selectedWell.id == well.id
                        Surface(
                            color = if (isSelected) Color(0xFF382312) else Color(0xFF1C130A),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) Color(0xFFF3C853) else Color(0xFF6E4822)
                            ),
                            modifier = Modifier.clickable {
                                if (!isSelected) {
                                    audioEngine?.playBrassToggleSwitch()
                                    selectedWell = well
                                    currentDepthFt = 0f
                                }
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = well.title,
                                    fontFamily = CinzelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFFFFF1C2) else Color(0xFFD4C3A3)
                                )
                                Text(
                                    text = "${well.county} • Waterline: ${well.waterTableDepthFt.toInt()} ft • Max: ${well.maxSoundingDepthFt.toInt()} ft",
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color(0xFFE5B85C) else Color(0xFF8B7763)
                                )
                            }
                        }
                    }
                }
            }

            // Historical Well Significance Banner
            item {
                Surface(
                    color = Color(0xFF181008),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A3E1B))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF3C853),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedWell.historicalSignificance,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 15.sp,
                            color = Color(0xFFE2D4BF)
                        )
                    }
                }
            }

            // Geological Aquifer Strata Cross-Section & Depth Scale
            item {
                Surface(
                    color = Color(0xFF130E0A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF8B6428)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GEOLOGICAL STRATUM CROSS-SECTION",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFFFDE68A)
                            )
                            Text(
                                text = activeStratum.geologicalEra,
                                fontSize = 9.sp,
                                color = Color(0xFFB5A48B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual Subterranean Shaft Column
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0A06))
                                .border(1.dp, Color(0xFF422810), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw geological stratum strata layers proportionally
                                selectedWell.strata.forEach { stratum ->
                                    val topRatio = (stratum.depthRangeFeet.start / selectedWell.maxSoundingDepthFt).coerceIn(0f, 1f)
                                    val bottomRatio = (stratum.depthRangeFeet.endInclusive / selectedWell.maxSoundingDepthFt).coerceIn(0f, 1f)
                                    val yTop = topRatio * h
                                    val yBottom = bottomRatio * h

                                    drawRect(
                                        color = stratum.stratumColor,
                                        topLeft = Offset(40.dp.toPx(), yTop),
                                        size = Size(w - 40.dp.toPx(), yBottom - yTop)
                                    )
                                }

                                // Water table surface line
                                val waterRatio = (selectedWell.waterTableDepthFt / selectedWell.maxSoundingDepthFt).coerceIn(0f, 1f)
                                val waterY = waterRatio * h
                                drawLine(
                                    color = Color(0xFF06B6D4),
                                    start = Offset(40.dp.toPx(), waterY),
                                    end = Offset(w, waterY),
                                    strokeWidth = 2.dp.toPx()
                                )

                                // Suspended cable wire descending from surface
                                val probeY = (depthRatio * h).coerceIn(4.dp.toPx(), h - 8.dp.toPx())
                                drawLine(
                                    color = Color(0xFF94A3B8),
                                    start = Offset(w * 0.5f, 0f),
                                    end = Offset(w * 0.5f, probeY),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                // Brass Hydrophone Sensor Capsule at bottom of wire
                                drawCircle(
                                    color = if (isSubmerged) Color(0xFF22D3EE) else Color(0xFFF3C853),
                                    radius = 6.dp.toPx(),
                                    center = Offset(w * 0.5f, probeY)
                                )
                                drawCircle(
                                    color = Color(0xFF1E1004),
                                    radius = 2.dp.toPx(),
                                    center = Offset(w * 0.5f, probeY)
                                )
                            }

                            // Left Margin Depth Callouts
                            Column(
                                modifier = Modifier
                                    .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0 FT", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB5A48B))
                                Text("${selectedWell.waterTableDepthFt.toInt()} FT ♒", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF22D3EE))
                                Text("${selectedWell.maxSoundingDepthFt.toInt()} FT", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB5A48B))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Current Stratum Description
                        Surface(
                            color = Color(0xFF1F150B),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6E4822)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = activeStratum.name,
                                    fontFamily = CinzelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFF1C2)
                                )
                                Text(
                                    text = activeStratum.lithologyDescription,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFFD4C3A3)
                                )
                                if (activeStratum.discoveredArtifactOrAnomaly != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✦ ${activeStratum.discoveredArtifactOrAnomaly}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // THE PHYSICAL HYDROPHONE CABLE WINCH
            item {
                HydrophoneWinchView(
                    currentDepthFt = currentDepthFt,
                    maxDepthFt = selectedWell.maxSoundingDepthFt,
                    waterTableDepthFt = selectedWell.waterTableDepthFt,
                    isWinchLocked = isWinchLocked,
                    onDepthChanged = { currentDepthFt = it },
                    onToggleLock = { isWinchLocked = !isWinchLocked },
                    audioEngine = audioEngine
                )
            }

            // Subterranean Sonar Telemetry & Sounding Actions
            item {
                Surface(
                    color = Color(0xFF160E07),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF8B6428)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACOUSTIC SONAR TELEMETRY",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFDE68A)
                            )
                            Text(
                                text = if (isSubmerged) "PHREATIC HYDROPHONE: ACTIVE" else "AIR CAVITY TRANSDUCER",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = if (isSubmerged) Color(0xFF34D399) else Color(0xFFB5A48B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sonar Ping & Log Sounding Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    audioEngine?.playCavernSonarPulse(depthRatio)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0E7490),
                                    contentColor = Color(0xFFE0F2FE)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fire Sonar Ping", fontSize = 11.sp, fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    audioEngine?.playWaxStampImpact()
                                    val entry = LoggedSoundingEntry(
                                        id = "log_${System.currentTimeMillis()}",
                                        wellId = selectedWell.id,
                                        depthFt = currentDepthFt,
                                        stratumName = activeStratum.name,
                                        waterTempF = activeStratum.waterTemperatureF,
                                        note = activeStratum.discoveredArtifactOrAnomaly ?: "Normal bedrock sounding."
                                    )
                                    loggedSoundings.add(entry)
                                    logToastMessage = "Recorded ${String.format(Locale.US, "%.1f", currentDepthFt)} ft sounding into Dossier!"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC89B3C),
                                    contentColor = Color(0xFF1E1100)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Sounding", fontSize = 11.sp, fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Success Log Toast Banner
                        AnimatedVisibility(visible = logToastMessage != null) {
                            logToastMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0xFF14532D),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✓ $msg",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDCFCE7)
                                        )
                                    }
                                }
                            }
                        }

                        // Recent Logged Soundings in this session
                        if (loggedSoundings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LOGGED FIELD SOUNDINGS (${loggedSoundings.size}):",
                                fontFamily = CinzelFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB5A48B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            loggedSoundings.takeLast(3).reversed().forEach { log ->
                                Text(
                                    text = "• ${String.format(Locale.US, "%.1f", log.depthFt)} FT: ${log.stratumName} (${log.note})",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE2D4BF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
