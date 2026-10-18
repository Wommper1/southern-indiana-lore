package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharcoalPigment
import com.example.data.model.RelicReliefFeature
import com.example.data.model.RelicSpecimen
import com.example.data.model.RubbingPaperType
import com.example.data.model.RubbingPoint
import com.example.data.model.SavedRubbingRecord
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.util.AdventureSoundEngine
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Interactive Antiquarian Tactile Charcoal Rubbing Canvas:
 * Allows explorers to rub charcoal across a sheet of vellum paper resting over a 3D relief artifact.
 * Features realistic graphite grain accumulation, relief heightmap detection, velocity-based audio,
 * and authentic wax seal authentication upon reaching completion.
 */
@Composable
fun TactileRubbingCanvas(
    specimen: RelicSpecimen,
    soundEngine: AdventureSoundEngine?,
    onSaveToJournal: (SavedRubbingRecord) -> Unit,
    modifier: Modifier = Modifier,
    initialCompletionPercent: Int = 0,
    onAuthenticationAchieved: (() -> Unit)? = null
) {
    var selectedPigment by remember { mutableStateOf(CharcoalPigment.GRAPHITE_EBONY) }
    var selectedPaper by remember { mutableStateOf(RubbingPaperType.AGED_PARCHMENT) }
    val rubbingPoints = remember(specimen.id) { mutableStateListOf<RubbingPoint>() }
    var completionPercent by remember(specimen.id) { mutableIntStateOf(initialCompletionPercent) }
    var isAuthenticated by remember(specimen.id) { mutableStateOf(initialCompletionPercent >= 75) }
    var isSavedToJournal by remember(specimen.id) { mutableStateOf(false) }

    // Seal pop-in scale animation
    val sealScale = remember { Animatable(if (isAuthenticated) 1f else 0f) }
    val scope = rememberCoroutineScope()

    // Grid tracker for coverage (16x16 grid = 256 cells)
    val gridCoverage = remember(specimen.id) { BooleanArray(256) }

    // Last audio trigger timestamp to avoid audio flood
    var lastSoundTriggerTime by remember { mutableStateOf(0L) }

    fun registerRubbingPoint(relX: Float, relY: Float, canvasWidth: Float, canvasHeight: Float) {
        val normX = (relX / canvasWidth).coerceIn(0f, 1f)
        val normY = (relY / canvasHeight).coerceIn(0f, 1f)

        // Check if pointer is near any relief feature
        var reliefIntensity = 0.35f
        for (feat in specimen.reliefFeatures) {
            val dist = hypot(normX - feat.x, normY - feat.y)
            if (dist <= feat.radius) {
                // High relief captures more charcoal pigment
                reliefIntensity = 1.0f
                break
            }
        }

        rubbingPoints.add(
            RubbingPoint(
                x = relX,
                y = relY,
                pressure = reliefIntensity,
                pigment = selectedPigment
            )
        )

        // Update grid coverage
        val col = (normX * 16).toInt().coerceIn(0, 15)
        val row = (normY * 16).toInt().coerceIn(0, 15)
        val cellIndex = row * 16 + col
        if (!gridCoverage[cellIndex]) {
            gridCoverage[cellIndex] = true
            val coveredCount = gridCoverage.count { it }
            // Coverage within the central active circle (~70% of cells max reachable)
            val computedPercent = ((coveredCount.toFloat() / 150f) * 100).toInt().coerceIn(0, 100)
            if (computedPercent > completionPercent) {
                completionPercent = computedPercent

                // Check for authentication trigger at 75%
                if (completionPercent >= 75 && !isAuthenticated) {
                    isAuthenticated = true
                    soundEngine?.playWaxStampImpact()
                    onAuthenticationAchieved?.invoke()
                    scope.launch {
                        sealScale.snapTo(0f)
                        sealScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            }
        }

        // Throttle scratch sound every 70ms
        val now = System.currentTimeMillis()
        if (now - lastSoundTriggerTime > 70L) {
            lastSoundTriggerTime = now
            soundEngine?.playCharcoalScratch(intensity = reliefIntensity)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactile_rubbing_canvas_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Specimen Workbench Banner & Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E150F)),
            border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = LanternAmberBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = specimen.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GildedGoldBright
                        )
                        Text(
                            text = "${specimen.material} • ${specimen.era}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }
                }

                // Completion Badge
                Surface(
                    color = if (isAuthenticated) Color(0xFF781A1A) else Color(0xFF2E2218),
                    border = BorderStroke(1.dp, if (isAuthenticated) GildedGold else LanternAmber.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAuthenticated) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Authenticated",
                                tint = GildedGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AUTHENTICATED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GildedGoldBright
                            )
                        } else {
                            Text(
                                text = "RUBBING: $completionPercent%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmber
                            )
                        }
                    }
                }
            }
        }

        // Main Tactile Rubbing Surface
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .aspectRatio(1.05f)
                .shadow(12.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF140D07)) // Deep walnut workbench
                .border(2.dp, Brush.linearGradient(listOf(GildedGold, Color(0xFF5A4418), GildedGold)), RoundedCornerShape(14.dp))
                .testTag("rubbing_touch_surface")
        ) {
            val canvasW = constraints.maxWidth.toFloat()
            val canvasH = constraints.maxHeight.toFloat()

            // 1. Interactive Drawing Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(specimen.id) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                registerRubbingPoint(offset.x, offset.y, canvasW, canvasH)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                registerRubbingPoint(change.position.x, change.position.y, canvasW, canvasH)
                            }
                        )
                    }
            ) {
                // Draw Base Relief Plate (Metal or Stone under parchment)
                drawReliefBasePlate(specimen)

                // Draw Translucent Parchment Overlay Sheet
                drawParchmentSheet(selectedPaper)

                // Draw Accumulated Charcoal Pigment Strokes
                for (pt in rubbingPoints) {
                    val alpha = (0.28f * pt.pressure).coerceIn(0.1f, 0.75f)
                    val radius = if (pt.pressure > 0.6f) 16f else 11f
                    drawCircle(
                        color = pt.pigment.color.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(pt.x, pt.y)
                    )
                }

                // Render Revealed Embossed Details Under the Charcoal
                drawRevealedReliefDetails(
                    specimen = specimen,
                    rubbingPoints = rubbingPoints,
                    selectedPigment = selectedPigment,
                    canvasW = canvasW,
                    canvasH = canvasH
                )

                // Draw Brass Corner Paper Clamps
                drawBrassCornerClamps()
            }

            // 2. Authentication Wax Seal Stamp (Animated when >= 75%)
            if (isAuthenticated) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .size(86.dp)
                        .testTag("wax_authentication_seal")
                ) {
                    WaxSealMonogramStamp(scale = sealScale.value)
                }
            }

            // Touch Prompt Overlay when fresh
            if (rubbingPoints.isEmpty()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Drag finger to rub charcoal & reveal relic relief",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = ParchmentPaper,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Toolbar: Pigments, Paper Textures, & Actions
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF1A120B),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Pigment Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Charcoal Pigment:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ParchmentMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CharcoalPigment.values().forEach { pigment ->
                            val isSelected = selectedPigment == pigment
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(pigment.color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) LanternAmberBright else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        soundEngine?.playTactileClick()
                                        selectedPigment = pigment
                                    }
                                    .testTag("pigment_${pigment.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = pigment.displayName,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Paper stock row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vellum Stock:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ParchmentMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RubbingPaperType.values().forEach { paper ->
                            val isSelected = selectedPaper == paper
                            Surface(
                                color = if (isSelected) LanternAmber.copy(alpha = 0.25f) else Color(0xFF261C13),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) LanternAmberBright else ParchmentMuted.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.clickable {
                                    soundEngine?.playParchmentRustle()
                                    selectedPaper = paper
                                }
                            ) {
                                Text(
                                    text = paper.displayName.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) LanternAmberBright else ParchmentMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Clear & Save to Dossier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            soundEngine?.playParchmentRustle()
                            rubbingPoints.clear()
                            gridCoverage.fill(false)
                            completionPercent = 0
                            isAuthenticated = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentMuted),
                        border = BorderStroke(1.dp, ParchmentMuted.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_rubbing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Fresh Vellum", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            if (!isSavedToJournal) {
                                soundEngine?.playWaxSealBreak()
                                isSavedToJournal = true
                                onSaveToJournal(
                                    SavedRubbingRecord(
                                        relicId = specimen.id,
                                        timestamp = System.currentTimeMillis(),
                                        completionPercent = completionPercent,
                                        pigmentUsed = selectedPigment,
                                        paperType = selectedPaper,
                                        notes = "${specimen.title} rubbed on ${selectedPaper.displayName}"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedToJournal) Color(0xFF1D4ED8) else LanternAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = completionPercent > 10,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_rubbing_dossier_button")
                    ) {
                        Icon(
                            imageVector = if (isSavedToJournal) Icons.Default.BookmarkAdded else Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSavedToJournal) "Logged in Dossier" else "Catalog Rubbing",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the specimen relief plate resting beneath the tracing vellum.
 */
private fun DrawScope.drawReliefBasePlate(specimen: RelicSpecimen) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val baseRadius = size.width * 0.42f

    // Heavy cast shadow beneath the metal/stone plate
    drawCircle(
        color = Color(0x77000000),
        radius = baseRadius + 14f,
        center = Offset(cx + 8f, cy + 12f)
    )

    // Primary Plate Material Disk (Polished brass, dark iron, fossil limestone, etc.)
    val plateColor = Color(specimen.primaryColorHex)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                plateColor.copy(alpha = 0.85f),
                plateColor.copy(alpha = 0.55f),
                Color(0xFF1E1712)
            ),
            center = Offset(cx - 30f, cy - 30f),
            radius = baseRadius
        ),
        radius = baseRadius,
        center = Offset(cx, cy)
    )

    // Outer milled rim
    drawCircle(
        color = Color(0xFFD4AF37).copy(alpha = 0.35f),
        radius = baseRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 6f)
    )
}

/**
 * Draws the semi-translucent vellum paper with aged fibers and deckle edges.
 */
private fun DrawScope.drawParchmentSheet(paperType: RubbingPaperType) {
    val pad = 18f
    val sheetRect = RubbingRectF(pad, pad, size.width - pad, size.height - pad)

    // Translucent vellum tint
    drawRoundRect(
        color = paperType.tintColor.copy(alpha = 0.82f),
        topLeft = Offset(sheetRect.left, sheetRect.top),
        size = Size(sheetRect.width, sheetRect.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )

    // Subtle deckle parchment border
    drawRoundRect(
        color = Color(0x334A3828),
        topLeft = Offset(sheetRect.left, sheetRect.top),
        size = Size(sheetRect.width, sheetRect.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1.5f)
    )
}

/**
 * Renders revealed relief contours, inscriptions, and fossil rings when touched.
 */
private fun DrawScope.drawRevealedReliefDetails(
    specimen: RelicSpecimen,
    rubbingPoints: List<RubbingPoint>,
    selectedPigment: CharcoalPigment,
    canvasW: Float,
    canvasH: Float
) {
    if (rubbingPoints.isEmpty()) return

    val cx = canvasW * 0.5f
    val cy = canvasH * 0.5f

    for (feat in specimen.reliefFeatures) {
        val fx = feat.x * canvasW
        val fy = feat.y * canvasH
        val fr = feat.radius * canvasW

        // Check rubbing stroke density around this feature
        val strokeNearCount = rubbingPoints.count { pt ->
            hypot(pt.x - fx, pt.y - fy) <= fr * 1.25f
        }

        if (strokeNearCount > 3) {
            val revealOpacity = (strokeNearCount.toFloat() / 24f).coerceIn(0.15f, 0.92f)
            val reliefColor = selectedPigment.color.copy(alpha = revealOpacity)

            when (feat.featureType) {
                RelicReliefFeature.FeatureType.CONCENTRIC_RINGS -> {
                    drawCircle(
                        color = reliefColor,
                        radius = fr,
                        center = Offset(fx, fy),
                        style = Stroke(width = feat.strokeWidth)
                    )
                    // Inner bevel highlight
                    drawCircle(
                        color = Color.White.copy(alpha = revealOpacity * 0.25f),
                        radius = fr - feat.strokeWidth * 0.5f,
                        center = Offset(fx - 2f, fy - 2f),
                        style = Stroke(width = 1.5f)
                    )
                }

                RelicReliefFeature.FeatureType.STAR_EMBOSS -> {
                    drawStarEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.COMPASS_ROSE -> {
                    drawCompassRoseEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.GEAR_TEETH -> {
                    drawGearTeethEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.CHISEL_BLADE -> {
                    drawChiselEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.HORN_OUTLINE -> {
                    drawHornEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.KEY_BIT -> {
                    drawKeyBitEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.FLORAL_FILIGREE -> {
                    drawFloralFiligreeEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.CROSS_HATCH -> {
                    drawCrossHatchEmboss(center = Offset(fx, fy), radius = fr, color = reliefColor)
                }

                RelicReliefFeature.FeatureType.INSCRIPTION_ARC -> {
                    // Dashed arc simulating raised lettering bas-relief
                    drawCircle(
                        color = reliefColor,
                        radius = fr,
                        center = Offset(fx, fy),
                        style = Stroke(
                            width = feat.strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Star relief emboss renderer
 */
private fun DrawScope.drawStarEmboss(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.42f
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * PI / points - PI / 2.0
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = 4f))
}

/**
 * 8-Point Compass Rose relief emboss
 */
private fun DrawScope.drawCompassRoseEmboss(center: Offset, radius: Float, color: Color) {
    val points = 8
    val innerRadius = radius * 0.3f
    val path = Path()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * PI / points - PI / 2.0
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = 3.5f))
}

/**
 * Gear/Paddlewheel teeth relief
 */
private fun DrawScope.drawGearTeethEmboss(center: Offset, radius: Float, color: Color) {
    val teeth = 16
    for (i in 0 until teeth) {
        val angle = (i * 2.0 * PI / teeth).toFloat()
        val x1 = center.x + cos(angle) * (radius - 8f)
        val y1 = center.y + sin(angle) * (radius - 8f)
        val x2 = center.x + cos(angle) * (radius + 8f)
        val y2 = center.y + sin(angle) * (radius + 8f)
        drawLine(color = color, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 4f)
    }
}

private fun DrawScope.drawChiselEmboss(center: Offset, radius: Float, color: Color) {
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 6f
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 6f
    )
}

private fun DrawScope.drawHornEmboss(center: Offset, radius: Float, color: Color) {
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius * 0.6f),
        size = Size(radius * 2f, radius * 1.2f),
        style = Stroke(width = 5f)
    )
}

private fun DrawScope.drawKeyBitEmboss(center: Offset, radius: Float, color: Color) {
    drawRect(
        color = color,
        topLeft = Offset(center.x - radius * 0.5f, center.y - radius * 0.3f),
        size = Size(radius, radius * 0.6f),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawFloralFiligreeEmboss(center: Offset, radius: Float, color: Color) {
    for (i in 0 until 4) {
        val angle = (i * PI * 0.5).toFloat()
        val petX = center.x + cos(angle) * (radius * 0.5f)
        val petY = center.y + sin(angle) * (radius * 0.5f)
        drawCircle(
            color = color,
            radius = radius * 0.35f,
            center = Offset(petX, petY),
            style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawCrossHatchEmboss(center: Offset, radius: Float, color: Color) {
    val step = radius * 0.4f
    var d = -radius
    while (d <= radius) {
        drawLine(
            color = color,
            start = Offset(center.x + d, center.y - radius),
            end = Offset(center.x + d, center.y + radius),
            strokeWidth = 2.5f
        )
        drawLine(
            color = color,
            start = Offset(center.x - radius, center.y + d),
            end = Offset(center.x + radius, center.y + d),
            strokeWidth = 2.5f
        )
        d += step
    }
}

/**
 * Brass corner paper clips/clamps to anchor the parchment sheet to the board.
 */
private fun DrawScope.drawBrassCornerClamps() {
    val brassColor = Color(0xFFC89B3C)
    val clampSize = 22f

    // 4 Corner brackets
    val corners = listOf(
        Offset(24f, 24f),
        Offset(size.width - 24f, 24f),
        Offset(24f, size.height - 24f),
        Offset(size.width - 24f, size.height - 24f)
    )

    for (c in corners) {
        drawCircle(color = Color(0x66000000), radius = 10f, center = Offset(c.x + 2f, c.y + 2f))
        drawCircle(color = brassColor, radius = 9f, center = c)
        drawCircle(color = Color(0xFF4A3410), radius = 3.5f, center = c)
    }
}

/**
 * Visual Monogram Stamp in antique red sealing wax with gold lettering.
 */
@Composable
private fun WaxSealMonogramStamp(scale: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wax_monogram_stamp")
    ) {
        if (scale <= 0.01f) return@Canvas

        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val baseR = (size.width * 0.44f) * scale

        // Wax drip edges
        drawCircle(
            color = Color(0xFF8B1E1E), // Crimson wax
            radius = baseR,
            center = Offset(cx, cy)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC53030), Color(0xFF741212)),
                center = Offset(cx - 8f, cy - 8f),
                radius = baseR
            ),
            radius = baseR - 4f,
            center = Offset(cx, cy)
        )

        // Embossed gold foil ring
        drawCircle(
            color = GildedGoldBright,
            radius = baseR * 0.72f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )

        // Central Authenticated Star Monogram
        drawStarEmboss(
            center = Offset(cx, cy),
            radius = baseR * 0.45f,
            color = GildedGoldBright
        )
    }
}

private data class RubbingRectF(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}
