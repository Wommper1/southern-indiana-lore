package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalClueEntry
import com.example.data.model.LoreSite
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper

enum class OpticalFilterMode {
    PRECISION_CLEAR,
    DAGUERREOTYPE_SEPIA,
    UV_SPECTRAL
}

@Composable
fun CameraViewfinderScreen(
    currentSite: LoreSite?,
    clueEntry: JournalClueEntry?,
    headingDegrees: Float,
    onBack: () -> Unit,
    onCaptureSnapshot: (String) -> Unit,
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedFilter by remember { mutableStateOf(OpticalFilterMode.PRECISION_CLEAR) }
    var isTargetLocked by remember { mutableStateOf(false) }
    var captureSuccessMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val shutterFlashAlpha = remember { Animatable(0f) }

    // Auto-dismiss capture notification toast after 3.5 seconds
    LaunchedEffect(captureSuccessMessage) {
        if (captureSuccessMessage != null) {
            delay(3500)
            captureSuccessMessage = null
        }
    }

    // Simulated spirit level inclinometer
    val spiritPitch = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        spiritPitch.animateTo(
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Auto-lock simulation when aligning
    LaunchedEffect(selectedFilter) {
        if (selectedFilter == OpticalFilterMode.UV_SPECTRAL) {
            isTargetLocked = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_viewfinder_screen")
    ) {
        // --- 1. Background Simulated Live Field Lens / Camera Background ---
        ViewfinderBackground(
            filterMode = selectedFilter,
            siteTitle = currentSite?.title ?: "Expedition Field Sector"
        )

        // --- 2. Antique Brass HUD Reticle Overlay ---
        ViewfinderReticleCanvas(
            filterMode = selectedFilter,
            isLocked = isTargetLocked,
            headingDegrees = headingDegrees,
            pitchOffset = spiritPitch.value
        )

        // --- 3. Top Instrument Panel & Compass Heading ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, GildedGold.copy(alpha = 0.6f), CircleShape)
                        .testTag("viewfinder_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Exploration",
                        tint = GildedGoldBright
                    )
                }

                // Compass Azimuth Banner
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val headingInt = headingDegrees.toInt()
                        val cardinal = when (headingInt) {
                            in 338..360, in 0..22 -> "N"
                            in 23..67 -> "NE"
                            in 68..112 -> "E"
                            in 113..157 -> "SE"
                            in 158..202 -> "S"
                            in 203..247 -> "SW"
                            in 248..292 -> "W"
                            else -> "NW"
                        }
                        Text(
                            text = "BEARING: $headingInt° $cardinal • OPTICAL RETICLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = GildedGoldBright
                        )
                    }
                }

                // Shutter / Flash indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isTargetLocked) Color(0x3310B981) else Color.Black.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            if (isTargetLocked) Color(0xFF10B981) else GildedGold.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = null,
                        tint = if (isTargetLocked) Color(0xFF34D399) else GildedGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Target Alignment Guidance Box
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTargetLocked) Color(0xFF34D399) else LanternAmber.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTargetLocked) "🎯" else "🔭",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTargetLocked) "ARCHITECTURAL TARGET LOCKED" else "OPTICAL SIGHT ALIGNMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = if (isTargetLocked) Color(0xFF34D399) else LanternAmberBright
                        )
                        Text(
                            text = clueEntry?.alignmentTargetDescription
                                ?: "Frame historical landmark within the brass viewfinder crosshairs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }
            }
        }

        // --- 4. Bottom Controls: Optical Filter Switcher & Shutter Button ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Feedback Toast
            AnimatedVisibility(
                visible = captureSuccessMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xFF14532D),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = captureSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Lens Optical Filter Switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(1.dp, GildedGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == OpticalFilterMode.PRECISION_CLEAR,
                    onClick = {
                        soundEngine?.playTactileClick()
                        selectedFilter = OpticalFilterMode.PRECISION_CLEAR
                    },
                    label = { Text("Precision Lens", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GildedGold,
                        selectedLabelColor = Color(0xFF1E1100),
                        containerColor = Color.Transparent,
                        labelColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("filter_precision_lens")
                )

                FilterChip(
                    selected = selectedFilter == OpticalFilterMode.DAGUERREOTYPE_SEPIA,
                    onClick = {
                        soundEngine?.playTactileClick()
                        selectedFilter = OpticalFilterMode.DAGUERREOTYPE_SEPIA
                    },
                    label = { Text("1850 Daguerreotype", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF78350F),
                        selectedLabelColor = Color(0xFFFDE68A),
                        containerColor = Color.Transparent,
                        labelColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("filter_daguerreotype")
                )

                FilterChip(
                    selected = selectedFilter == OpticalFilterMode.UV_SPECTRAL,
                    onClick = {
                        soundEngine?.playTactileClick()
                        selectedFilter = OpticalFilterMode.UV_SPECTRAL
                    },
                    label = { Text("UV Spectral", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0E7490),
                        selectedLabelColor = Color(0xFFCFFAFE),
                        containerColor = Color.Transparent,
                        labelColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("filter_uv_spectral")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shutter Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(GildedGold.copy(alpha = 0.2f))
                        .border(3.dp, GildedGoldBright, CircleShape)
                        .clickable {
                            soundEngine?.playCameraShutter()
                            val snapshotLabel = currentSite?.title ?: "Expedition Field Alignment"
                            onCaptureSnapshot(snapshotLabel)
                            captureSuccessMessage = "Snapshot Captured & Logged in Expedition Journal!"
                            coroutineScope.launch {
                                shutterFlashAlpha.snapTo(0.85f)
                                shutterFlashAlpha.animateTo(0f, tween(320, easing = LinearEasing))
                            }
                        }
                        .testTag("viewfinder_shutter_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFDE68A), GildedGold, Color(0xFFB45309))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture Snapshot",
                            tint = Color(0xFF1E1100),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // --- 5. Shutter Flash Curtain Overlay ---
        if (shutterFlashAlpha.value > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Color.White.copy(alpha = shutterFlashAlpha.value))
            }
        }
    }
}

@Composable
private fun ViewfinderBackground(
    filterMode: OpticalFilterMode,
    siteTitle: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "uv_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "uv_pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when (filterMode) {
            OpticalFilterMode.PRECISION_CLEAR -> {
                // Subtle dark ambient gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
            }
            OpticalFilterMode.DAGUERREOTYPE_SEPIA -> {
                // Warm antique sepia patina with vignette
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF451A03).copy(alpha = 0.85f),
                            Color(0xFF1E0A01)
                        ),
                        center = Offset(width / 2f, height / 2f),
                        radius = width
                    )
                )
            }
            OpticalFilterMode.UV_SPECTRAL -> {
                // High-contrast deep navy with glowing cyan UV field
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF083344),
                            Color(0xFF020617)
                        ),
                        center = Offset(width / 2f, height / 2f),
                        radius = width
                    )
                )

                // Glowing hidden UV symbols / arrows on the canvas
                val glowAlpha = (0.5f + 0.4f * Math.sin(waveOffset * 2 * Math.PI).toFloat()).coerceIn(0.2f, 1f)
                drawCircle(
                    color = Color(0xFF22D3EE).copy(alpha = 0.15f * glowAlpha),
                    radius = width * 0.35f,
                    center = Offset(width / 2f, height * 0.45f)
                )

                // Secret UV historical alignment arrow
                drawLine(
                    color = Color(0xFF06B6D4).copy(alpha = glowAlpha),
                    start = Offset(width / 2f, height * 0.35f),
                    end = Offset(width / 2f, height * 0.55f),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun ViewfinderReticleCanvas(
    filterMode: OpticalFilterMode,
    isLocked: Boolean,
    headingDegrees: Float,
    pitchOffset: Float
) {
    val reticleColor = when (filterMode) {
        OpticalFilterMode.UV_SPECTRAL -> Color(0xFF22D3EE)
        OpticalFilterMode.DAGUERREOTYPE_SEPIA -> Color(0xFFFDE68A)
        else -> if (isLocked) Color(0xFF34D399) else Color(0xFFD4AF37)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.46f
        val cornerMargin = 40.dp.toPx()
        val bracketLen = 32.dp.toPx()
        val strokeW = 2.dp.toPx()

        // 1. Four Antique Corner Brackets
        // Top-Left
        drawLine(reticleColor, Offset(cornerMargin, cornerMargin), Offset(cornerMargin + bracketLen, cornerMargin), strokeW)
        drawLine(reticleColor, Offset(cornerMargin, cornerMargin), Offset(cornerMargin, cornerMargin + bracketLen), strokeW)

        // Top-Right
        drawLine(reticleColor, Offset(w - cornerMargin, cornerMargin), Offset(w - cornerMargin - bracketLen, cornerMargin), strokeW)
        drawLine(reticleColor, Offset(w - cornerMargin, cornerMargin), Offset(w - cornerMargin, cornerMargin + bracketLen), strokeW)

        // Bottom-Left
        drawLine(reticleColor, Offset(cornerMargin, h - cornerMargin * 2), Offset(cornerMargin + bracketLen, h - cornerMargin * 2), strokeW)
        drawLine(reticleColor, Offset(cornerMargin, h - cornerMargin * 2), Offset(cornerMargin, h - cornerMargin * 2 - bracketLen), strokeW)

        // Bottom-Right
        drawLine(reticleColor, Offset(w - cornerMargin, h - cornerMargin * 2), Offset(w - cornerMargin - bracketLen, h - cornerMargin * 2), strokeW)
        drawLine(reticleColor, Offset(w - cornerMargin, h - cornerMargin * 2), Offset(w - cornerMargin, h - cornerMargin * 2 - bracketLen), strokeW)

        // 2. Central Crosshair & Circle
        val ringR = 64.dp.toPx()
        drawCircle(
            color = reticleColor.copy(alpha = 0.7f),
            radius = ringR,
            center = Offset(cx, cy),
            style = Stroke(width = strokeW)
        )

        // Crosshairs with gap
        val gap = 16.dp.toPx()
        val armLen = 36.dp.toPx()
        drawLine(reticleColor, Offset(cx - ringR - armLen, cy), Offset(cx - gap, cy), strokeW)
        drawLine(reticleColor, Offset(cx + gap, cy), Offset(cx + ringR + armLen, cy), strokeW)
        drawLine(reticleColor, Offset(cx, cy - ringR - armLen), Offset(cx, cy - gap), strokeW)
        drawLine(reticleColor, Offset(cx, cy + gap), Offset(cx, cy + ringR + armLen), strokeW)

        // 3. Spirit Inclinometer Level line
        val levelY = cy + (pitchOffset * 10f)
        drawLine(
            color = if (isLocked) Color(0xFF34D399) else Color(0xFFF59E0B).copy(alpha = 0.6f),
            start = Offset(cx - ringR * 0.7f, levelY),
            end = Offset(cx + ringR * 0.7f, levelY),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}
