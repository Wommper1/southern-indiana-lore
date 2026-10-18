package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CuratedOralHistoryArchive
import com.example.data.model.WaxCylinderRecording
import com.example.ui.components.RelicVaultView
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentDossierBg
import com.example.ui.theme.ParchmentInkDark
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.util.AdventureSoundEngine
import kotlin.math.roundToInt

@Composable
fun PhonographRadioScreen(
    visitedSiteIds: Set<String>,
    solvedCipherIds: Set<String>,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPlayingId: String?,
    onPlayCylinder: (WaxCylinderRecording) -> Unit,
    onStopPlayback: () -> Unit,
    onBack: () -> Unit,
    soundEngine: AdventureSoundEngine?,
    onNavigateToSite: ((String) -> Unit)? = null,
    onOpenLab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Phonograph Radio, 1 = Relic Vault
    val cylinders = CuratedOralHistoryArchive.allCylinders

    var selectedCylinderIndex by remember { mutableIntStateOf(0) }
    val currentCylinder = cylinders.getOrElse(selectedCylinderIndex) { cylinders.first() }

    var radioFrequencyKhz by remember { mutableFloatStateOf(currentCylinder.frequencyKhz.toFloat()) }

    // Wax Cylinder Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "cylinder_rotation")
    val cylinderRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cylinder_rotation"
    )

    // Vacuum Tube Filament Glow Animation
    val tubeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tube_glow"
    )

    val isCurrentCylinderUnlocked = remember(currentCylinder, visitedSiteIds, solvedCipherIds) {
        CuratedOralHistoryArchive.isCylinderUnlocked(currentCylinder, visitedSiteIds, solvedCipherIds)
    }

    val isCurrentlyActiveTrack = isPlaying && currentPlayingId == currentCylinder.id

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1008)) // Dark mahogany phonograph cabinet
            .statusBarsPadding()
            .testTag("phonograph_radio_screen")
    ) {
        // --- Top Gramophone Brass Header ---
        Surface(
            color = ShelfWoodDark,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GildedGoldBright
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = GildedGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EDISON & ZENITH FIELD RADIO",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = GildedGoldBright
                                )
                            }
                            Text(
                                text = "1890s Wax Cylinders • Eyewitness Oral History Archive",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD1D5DB)
                            )
                        }
                    }

                    // Mode indicator badge
                    Surface(
                        color = ShelfWoodPlank,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${currentCylinder.frequencyKhz} kHz",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = GildedGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Top Tab Switcher: Phonograph Radio vs Relic Vault
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShelfWoodDark,
                    contentColor = GildedGoldBright,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GildedGoldBright,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            soundEngine?.playTactileClick()
                            selectedTab = 0
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) GildedGoldBright else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Wax Cylinders",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == 0) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_phonograph_cylinders")
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            soundEngine?.playVaultLatchRelease()
                            selectedTab = 1
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) GildedGoldBright else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tactile Relic Vault",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTab == 1) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_phonograph_relics")
                    )
                }
            }
        }

        if (selectedTab == 1) {
            // Relic Vault View
            RelicVaultView(
                visitedSiteIds = visitedSiteIds,
                solvedCipherIds = solvedCipherIds,
                soundEngine = soundEngine,
                onNavigateToSite = onNavigateToSite,
                onOpenLab = onOpenLab
            )
        } else {
            // Phonograph & Shortwave Radio Playback View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // --- Gramophone Acoustic Visualizer & Rotating Cylinder ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140B05)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF854D0E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Vacuum Tube & Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vacuum Tube Indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp, 24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isCurrentlyActiveTrack) {
                                                Color(0xFFF59E0B).copy(alpha = tubeGlowAlpha)
                                            } else Color(0x334B5563)
                                        )
                                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCurrentlyActiveTrack) "TUBE WARM • AMPLIFIED" else "STANDBY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isCurrentlyActiveTrack) Color(0xFFFBBF24) else Color(0xFF9CA3AF)
                                )
                            }

                            // Buffering / Studio Voice Tag
                            if (isBuffering) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = LanternAmberBright
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Tuning High-Fidelity Voice...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = LanternAmberBright
                                    )
                                }
                            } else {
                                Surface(
                                    color = Color(currentCylinder.cylinderColorHex).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = currentCylinder.recordedDate,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = Color(currentCylinder.cylinderColorHex),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mechanical Phonograph Turntable with Grooved Wax Cylinder Canvas
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(currentCylinder.cylinderColorHex),
                                            Color(0xFF381502),
                                            Color(0xFF0F0601)
                                        )
                                    )
                                )
                                .border(3.dp, Color(0xFFD4AF37), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = size.minDimension / 2f

                                // Draw grooved concentric acoustic tracks
                                val grooveSteps = 8
                                for (g in 1..grooveSteps) {
                                    val r = radius * (g / (grooveSteps + 1f))
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.25f),
                                        radius = r,
                                        center = center,
                                        style = Stroke(width = 1.5f)
                                    )
                                }

                                // Center brass spindle & spindle ring
                                drawCircle(
                                    color = Color(0xFFD4AF37),
                                    radius = radius * 0.22f,
                                    center = center
                                )
                                drawCircle(
                                    color = Color(0xFF1E1008),
                                    radius = radius * 0.08f,
                                    center = center
                                )

                                // Stylus pickup needle arm
                                val needleAngle = if (isCurrentlyActiveTrack) 25f else 55f
                                val needleLength = radius * 0.85f
                                val rad = Math.toRadians(needleAngle.toDouble())
                                val needleEnd = Offset(
                                    (center.x + needleLength * kotlin.math.cos(rad)).toFloat(),
                                    (center.y + needleLength * kotlin.math.sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(center.x + radius * 0.9f, center.y - radius * 0.9f),
                                    end = needleEnd,
                                    strokeWidth = 4f,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Rotating amber center decal
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .rotate(if (isCurrentlyActiveTrack) cylinderRotation else 0f)
                                    .clip(CircleShape)
                                    .background(Color(0xFF78350F))
                                    .border(1.dp, Color(0xFFF59E0B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "EDISON\nRECORD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = Color(0xFFFDE68A),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Track Title & Speaker Role
                        Text(
                            text = currentCylinder.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GildedGoldBright,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${currentCylinder.eyewitnessName} • ${currentCylinder.speakerRole}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LanternAmber,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Analog Radio Frequency Tuning Slider ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "FREQUENCY TUNING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = ParchmentMuted
                                )
                                Text(
                                    text = "${radioFrequencyKhz.roundToInt()} kHz",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = LanternAmberBright
                                )
                            }

                            Slider(
                                value = radioFrequencyKhz,
                                onValueChange = { newFreq ->
                                    val oldInt = radioFrequencyKhz.toInt() / 20
                                    val newInt = newFreq.toInt() / 20
                                    if (oldInt != newInt) {
                                        soundEngine?.playRadioTuningSweep(if (newFreq > radioFrequencyKhz) 1 else -1)
                                    }
                                    radioFrequencyKhz = newFreq

                                    // Auto-snap to nearest cylinder station within 30 kHz
                                    val nearest = cylinders.minByOrNull { kotlin.math.abs(it.frequencyKhz - newFreq) }
                                    if (nearest != null && kotlin.math.abs(nearest.frequencyKhz - newFreq) < 25) {
                                        val newIdx = cylinders.indexOf(nearest)
                                        if (newIdx != selectedCylinderIndex) {
                                            selectedCylinderIndex = newIdx
                                        }
                                    }
                                },
                                valueRange = 500f..1200f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF97316),
                                    activeTrackColor = Color(0xFFD4AF37),
                                    inactiveTrackColor = Color(0xFF332014)
                                ),
                                modifier = Modifier.testTag("radio_frequency_slider")
                            )

                            // Quick Frequency Dial Scale Markings
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("550", "700", "840", "990", "1120").forEach { mark ->
                                    Text(
                                        text = mark,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Playback Controls Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Cylinder
                            IconButton(
                                onClick = {
                                    soundEngine?.playSpringMotorWinding()
                                    selectedCylinderIndex = if (selectedCylinderIndex > 0) selectedCylinderIndex - 1 else cylinders.size - 1
                                    radioFrequencyKhz = cylinders[selectedCylinderIndex].frequencyKhz.toFloat()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E170A))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Cylinder", tint = ParchmentPaper)
                            }

                            // Main Needle Drop / Play Button
                            Button(
                                onClick = {
                                    if (!isCurrentCylinderUnlocked) {
                                        soundEngine?.vibrateHeavyClick()
                                        return@Button
                                    }
                                    if (isCurrentlyActiveTrack) {
                                        onStopPlayback()
                                        soundEngine?.vibrateTick()
                                    } else {
                                        soundEngine?.playPhonographNeedleDrop()
                                        onPlayCylinder(currentCylinder)
                                    }
                                },
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("phonograph_play_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrentlyActiveTrack) Color(0xFF78350F) else LanternAmber,
                                    contentColor = if (isCurrentlyActiveTrack) Color.White else Color(0xFF1E1008)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFD4AF37))
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlyActiveTrack) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isCurrentlyActiveTrack) "Pause" else "Drop Needle & Play",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (!isCurrentCylinderUnlocked) "LOCKED CYLINDER" else if (isCurrentlyActiveTrack) "LIFT STYLUS" else "DROP NEEDLE",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Next Cylinder
                            IconButton(
                                onClick = {
                                    soundEngine?.playSpringMotorWinding()
                                    selectedCylinderIndex = (selectedCylinderIndex + 1) % cylinders.size
                                    radioFrequencyKhz = cylinders[selectedCylinderIndex].frequencyKhz.toFloat()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E170A))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Cylinder", tint = ParchmentPaper)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- Discovered Wax Cylinder Rack (Horizontal Selector) ---
                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text(
                        text = "FIELD CYLINDER RACK (${cylinders.count { CuratedOralHistoryArchive.isCylinderUnlocked(it, visitedSiteIds, solvedCipherIds) }}/${cylinders.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GildedGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        cylinders.forEachIndexed { index, cyl ->
                            val isUnlocked = CuratedOralHistoryArchive.isCylinderUnlocked(cyl, visitedSiteIds, solvedCipherIds)
                            val isSelected = index == selectedCylinderIndex

                            Surface(
                                onClick = {
                                    soundEngine?.playSpringMotorWinding()
                                    selectedCylinderIndex = index
                                    radioFrequencyKhz = cyl.frequencyKhz.toFloat()
                                },
                                color = if (isSelected) Color(0xFF451A03) else Color(0xFF1E1008),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFFF59E0B) else Color(0x336B7280)
                                ),
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("cylinder_pill_$index")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(cyl.cylinderColorHex))
                                        )
                                        if (!isUnlocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFF6B7280),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = cyl.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUnlocked) ParchmentPaper else Color(0xFF9CA3AF),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${cyl.frequencyKhz} kHz",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = LanternAmberBright
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Synchronized Historical Typewriter Transcript ---
                Surface(
                    color = ParchmentDossierBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFICIAL RECORDING TRANSCRIPT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFF78350F)
                            )

                            Surface(
                                color = Color(0xFF451A03),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "EDISON 1890 ARCHIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    ),
                                    color = Color(0xFFFDE68A),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isCurrentCylinderUnlocked) currentCylinder.transcript else "Encrypted/Locked wax cylinder. Visit ${currentCylinder.siteId.replace("_", " ").uppercase()} or decode the linked mystery cipher to unlock the acoustic playback and read the official transcript.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = ParchmentInkDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Historical Context: ${currentCylinder.historicalContext}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = Color(0xFF57534E)
                        )

                        if (!isCurrentCylinderUnlocked && onNavigateToSite != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onNavigateToSite(currentCylinder.siteId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78350F)),
                                border = BorderStroke(1.dp, Color(0xFF78350F))
                            ) {
                                Text("Inspect Discovery Coordinates", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
