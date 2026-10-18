package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RelicRegistry
import com.example.data.model.RelicSpecimen
import com.example.data.model.SavedRubbingRecord
import com.example.ui.components.ArchivalTapeDeckView
import com.example.ui.components.TactileRubbingCanvas
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.util.AdventureSoundEngine
import com.example.util.AmbientSoundEngine
import com.example.util.NarratorManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen Standalone Research Station:
 * Southern Indiana Field Relic Examination & Tactile Rubbing Laboratory.
 */
@Composable
fun RelicLabScreen(
    soundEngine: AdventureSoundEngine?,
    ambientSoundEngine: AmbientSoundEngine?,
    narratorManager: NarratorManager?,
    onNavigateBack: () -> Unit,
    onNavigateToSite: ((String) -> Unit)? = null,
    onSaveRubbingRecord: ((SavedRubbingRecord) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    var selectedSpecimen by remember { mutableStateOf(RelicRegistry.STEAMBOAT_MEDALLION) }
    var selectedStationTab by remember { mutableIntStateOf(0) } // 0: Rubbing Studio, 1: 3D Calipers, 2: Tape Deck

    // Track authenticated specimens
    val authenticatedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put(RelicRegistry.CRINOID_FOSSIL.id, true) // 1 initial sample unlocked
        }
    }

    val authenticatedCount = authenticatedMap.count { it.value }
    val totalCount = RelicRegistry.allSpecimens.size

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF130D08)) // Dark antiquarian walnut
            .testTag("relic_lab_screen")
    ) {
        // Station Reachability Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1F140B),
            border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                soundEngine?.playLeatherUnclasp()
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("relic_lab_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return to Field Dossier",
                                tint = LanternAmberBright
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = GildedGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RELIC EXAMINATION LAB",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = GildedGoldBright
                                )
                            }
                            Text(
                                text = "Antique Field Research & Tactile Rubbing Station",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                        }
                    }

                    // Authenticated Counter Badge
                    Surface(
                        color = Color(0xFF2E1C0F),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$authenticatedCount / $totalCount SEALED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmberBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Specimen Drawer Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("specimen_drawer_carousel"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(RelicRegistry.allSpecimens) { specimen ->
                        val isSelected = specimen.id == selectedSpecimen.id
                        val isAuth = authenticatedMap[specimen.id] == true

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF382312) else Color(0xFF1E140C),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) LanternAmberBright else ParchmentMuted.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    soundEngine?.playTactileClick()
                                    selectedSpecimen = specimen
                                }
                                .width(150.dp)
                                .testTag("specimen_chip_${specimen.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(specimen.primaryColorHex))
                                    )
                                    if (isAuth) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Sealed",
                                            tint = GildedGoldBright,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = specimen.title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) LanternAmberBright else ParchmentPaper,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = specimen.siteName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Station Function Tabs
        TabRow(
            selectedTabIndex = selectedStationTab,
            containerColor = Color(0xFF181008),
            contentColor = LanternAmberBright,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStationTab]),
                    color = LanternAmberBright,
                    height = 2.5.dp
                )
            }
        ) {
            Tab(
                selected = selectedStationTab == 0,
                onClick = {
                    soundEngine?.playParchmentRustle()
                    selectedStationTab = 0
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Rubbing Lab", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                modifier = Modifier.testTag("tab_rubbing_studio")
            )
            Tab(
                selected = selectedStationTab == 1,
                onClick = {
                    soundEngine?.playTactileClick()
                    selectedStationTab = 1
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "3D Calipers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                modifier = Modifier.testTag("tab_calipers_3d")
            )
            Tab(
                selected = selectedStationTab == 2,
                onClick = {
                    soundEngine?.playTapeDeckLatch()
                    selectedStationTab = 2
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Archival Tape", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                modifier = Modifier.testTag("tab_tape_deck")
            )
        }

        // Station Body Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            when (selectedStationTab) {
                0 -> {
                    // Tactile Rubbing Studio
                    TactileRubbingCanvas(
                        specimen = selectedSpecimen,
                        soundEngine = soundEngine,
                        initialCompletionPercent = if (authenticatedMap[selectedSpecimen.id] == true) 85 else 0,
                        onAuthenticationAchieved = {
                            authenticatedMap[selectedSpecimen.id] = true
                        },
                        onSaveToJournal = { record ->
                            authenticatedMap[selectedSpecimen.id] = true
                            onSaveRubbingRecord?.invoke(record)
                        }
                    )
                }

                1 -> {
                    // 3D Calipers & Specimen Examination
                    RelicCalipersInspectionView(
                        specimen = selectedSpecimen,
                        soundEngine = soundEngine,
                        onNavigateToSite = onNavigateToSite
                    )
                }

                2 -> {
                    // Archival Tape Deck
                    ArchivalTapeDeckView(
                        specimen = selectedSpecimen,
                        narratorManager = narratorManager,
                        ambientSoundEngine = ambientSoundEngine,
                        adventureSoundEngine = soundEngine
                    )
                }
            }
        }
    }
}

/**
 * 3D Calipers & Metallurgical Light Angle Inspection View:
 * Allows user to dynamically sweep illumination angle across the artifact to cast relief shadows,
 * inspect provenance geography, and read geological notes.
 */
@Composable
private fun RelicCalipersInspectionView(
    specimen: RelicSpecimen,
    soundEngine: AdventureSoundEngine?,
    onNavigateToSite: ((String) -> Unit)?
) {
    var lightAngleDegrees by remember { mutableFloatStateOf(45f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("relic_calipers_inspection_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Specimen 3D Shadow Viewport
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            color = Color(0xFF140D07),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, GildedGold.copy(alpha = 0.45f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.5f
                    val r = size.height * 0.38f

                    val rad = lightAngleDegrees * (PI / 180.0)
                    val shadowOffsetX = (cos(rad) * 18f).toFloat()
                    val shadowOffsetY = (sin(rad) * 18f).toFloat()

                    // Deep simulated cast shadow reacting to light angle
                    drawCircle(
                        color = Color(0x99000000),
                        radius = r + 6f,
                        center = Offset(cx + shadowOffsetX, cy + shadowOffsetY)
                    )

                    // Specimen plate material
                    val plateColor = Color(specimen.primaryColorHex)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                plateColor.copy(alpha = 0.95f),
                                plateColor.copy(alpha = 0.65f),
                                Color(0xFF181008)
                            ),
                            center = Offset(cx - shadowOffsetX, cy - shadowOffsetY),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )

                    // Engraved edge relief
                    drawCircle(
                        color = GildedGoldBright.copy(alpha = 0.5f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 5f)
                    )

                    // Highlights on the rim facing the light
                    drawArc(
                        color = Color.White.copy(alpha = 0.45f),
                        startAngle = lightAngleDegrees + 130f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(cx - r, cy - r),
                        size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                        style = Stroke(width = 3f)
                    )
                }

                // Light source direction badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        tint = LanternAmberBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sun Angle: ${lightAngleDegrees.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmberBright
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulated Light Angle Dial Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shadow Sweep:",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = lightAngleDegrees,
                onValueChange = {
                    lightAngleDegrees = it
                    soundEngine?.playCompassClick()
                },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = LanternAmberBright,
                    activeTrackColor = LanternAmber,
                    inactiveTrackColor = Color(0xFF2C1E12)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("shadow_sweep_slider")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Curator Provenance & Geological Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E150D)),
            border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SPECIMEN PROVENANCE & METALLURGY",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GildedGoldBright
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = specimen.curatorNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentPaper
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Provenance Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Discovery Site:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                        Text(
                            text = specimen.siteName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LanternAmberBright
                        )
                        Text(
                            text = "${specimen.lat}, ${specimen.lng}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }

                    if (onNavigateToSite != null) {
                        Button(
                            onClick = {
                                soundEngine?.playCompassClick()
                                onNavigateToSite(specimen.siteId)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF382312),
                                contentColor = LanternAmberBright
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "View Site", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Period Inscriptions Revealed by Rubbing
                Text(
                    text = "Historical Inscriptions on Relief:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                specimen.inscriptions.forEach { inscription ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0xFF140D07),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, LanternAmber.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "• $inscription",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = ParchmentPaper,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
