package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.util.AdventureSoundEngine
import com.example.util.TopoElevationEngine
import kotlin.math.roundToInt

/**
 * Antique Field Dossier Topographic Elevation Cross-Section:
 * Interactive vector relief graph of Knobstone Escarpment ridges and Ohio River trench.
 *
 * Features:
 * - Real-time contour slicing and terrain profile cross-section
 * - Subterranean geological strata rendering (Siltstone, Limestone, Shale, Alluvium)
 * - Interactive scrub cursor with haptic detents and tactile clicks
 * - Altimeter telemetry with summit, valley, grade percentage and stratum classification
 */
@Composable
fun TopoCrossSectionProfile(
    telemetry: TopoElevationEngine.TrailProfileTelemetry,
    title: String = "KNOBSTONE ESCARPMENT RELIEF",
    trailDistanceMiles: Float = 6.4f,
    soundEngine: AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    val points = telemetry.points
    if (points.isEmpty()) return

    val minElev = 400
    val maxElev = 1050

    var scrubFraction by remember { mutableFloatStateOf(0.0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    val activeIndex = remember(scrubFraction, points.size) {
        val idx = (scrubFraction * (points.size - 1)).roundToInt()
        idx.coerceIn(0, points.lastIndex)
    }

    val activePoint = points[activeIndex]
    val activeStratum = activePoint.stratum

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14110E)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC8963E)), // Burnished brass rim
        modifier = modifier
            .fillMaxWidth()
            .testTag("topo_cross_section_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Dossier Header: Brass Instrument Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF332005), CircleShape)
                            .border(1.dp, Color(0xFFE5B358), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = Color(0xFFE5B358),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color(0xFFE5B358)
                        )
                        Text(
                            text = "Knobstone Ridge • 300+ ft Escarpment Slice",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Current Altimeter Readout
                Surface(
                    color = Color(0xFF24160D),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8963E).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePoint.elevationFt} FT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFF3C853)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MSL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Elevation Cross-Section Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1712))
                    .border(1.dp, Color(0xFF3E2C1E), RoundedCornerShape(10.dp))
                    .pointerInput(points.size) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            scrubFraction = fraction
                            soundEngine?.vibrateTick()
                            soundEngine?.playCompassClick()
                        }
                    }
                    .pointerInput(points.size) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isScrubbing = true
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                scrubFraction = fraction
                                soundEngine?.vibrateTick()
                            },
                            onDragEnd = {
                                isScrubbing = false
                            },
                            onDragCancel = {
                                isScrubbing = false
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                if (kotlin.math.abs(fraction - scrubFraction) > 0.03f) {
                                    soundEngine?.vibrateTick()
                                }
                                scrubFraction = fraction
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    val paddingBottom = 16f
                    val paddingTop = 12f
                    val usableHeight = h - paddingBottom - paddingTop

                    // 1. Draw horizontal elevation grid lines (400', 600', 800', 1000')
                    val gridElevations = listOf(450, 650, 850, 1000)
                    for (gridElev in gridElevations) {
                        val norm = (gridElev - minElev).toFloat() / (maxElev - minElev).toFloat()
                        val y = h - paddingBottom - (norm * usableHeight)

                        drawLine(
                            color = Color(0xFF3A2B1F),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // 2. Build Terrain Profile Path
                    val fillPath = Path()
                    val strokePath = Path()

                    val firstX = 0f
                    val firstNorm = (points.first().elevationFt - minElev).toFloat() / (maxElev - minElev).toFloat()
                    val firstY = h - paddingBottom - (firstNorm * usableHeight)

                    fillPath.moveTo(firstX, h)
                    fillPath.lineTo(firstX, firstY)
                    strokePath.moveTo(firstX, firstY)

                    for (i in points.indices) {
                        val fraction = i.toFloat() / (points.size - 1).toFloat()
                        val x = fraction * w
                        val elevNorm = (points[i].elevationFt - minElev).toFloat() / (maxElev - minElev).toFloat()
                        val y = h - paddingBottom - (elevNorm * usableHeight)

                        fillPath.lineTo(x, y)
                        strokePath.lineTo(x, y)
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Draw gradient fill representing geological depth
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC8963E).copy(alpha = 0.55f), // Summit Brass/Siltstone
                                Color(0xFF5D4037).copy(alpha = 0.65f), // Limestone Karst
                                Color(0xFF271A12).copy(alpha = 0.90f)  // Shale & Alluvium
                            ),
                            startY = paddingTop,
                            endY = h
                        )
                    )

                    // Draw ridge relief line
                    drawPath(
                        path = strokePath,
                        color = Color(0xFFF3C853),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // 3. Draw Waypoint Flags along path
                    for (i in points.indices) {
                        val pt = points[i]
                        if (pt.landmarkName != null) {
                            val frac = i.toFloat() / (points.size - 1).toFloat()
                            val x = frac * w
                            val elevNorm = (pt.elevationFt - minElev).toFloat() / (maxElev - minElev).toFloat()
                            val y = h - paddingBottom - (elevNorm * usableHeight)

                            // Marker pin
                            drawCircle(
                                color = Color(0xFF8B1E1E), // Crimson seal pin
                                radius = 5f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color(0xFFE5B358),
                                radius = 2.5f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // 4. Draw Active Scrub Cursor
                    val scrubX = scrubFraction * w
                    val scrubNorm = (activePoint.elevationFt - minElev).toFloat() / (maxElev - minElev).toFloat()
                    val scrubY = h - paddingBottom - (scrubNorm * usableHeight)

                    // Vertical hairline
                    drawLine(
                        color = Color(0xFFE5B358).copy(alpha = 0.8f),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, h),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Pulsing Altimeter Reticle
                    drawCircle(
                        color = Color(0xFFE5B358).copy(alpha = 0.35f),
                        radius = 12f,
                        center = Offset(scrubX, scrubY)
                    )
                    drawCircle(
                        color = Color(0xFFF3C853),
                        radius = 6f,
                        center = Offset(scrubX, scrubY)
                    )
                    drawCircle(
                        color = Color(0xFF14110E),
                        radius = 2.5f,
                        center = Offset(scrubX, scrubY)
                    )
                }

                // Instruction hint overlay (fades after interaction)
                if (!isScrubbing && scrubFraction == 0.0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                            .background(Color(0xFF14110E).copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "◄ Drag finger across profile to inspect elevation ◄",
                            color = Color(0xFFD4AF37),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subterranean Stratum & Waypoint Inspector Card
            Surface(
                color = Color(0xFF201610),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E2C1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeStratum.iconEmoji,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = activeStratum.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFF4EAD4)
                                )
                                Text(
                                    text = activeStratum.ageEra,
                                    fontSize = 10.sp,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                        }

                        // Distance along trail
                        Text(
                            text = "${String.format("%.1f", activePoint.distanceMiles)} mi",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFE5B358)
                        )
                    }

                    if (activePoint.landmarkName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🚩 Landmark: ",
                                fontSize = 10.sp,
                                color = Color(0xFF8B1E1E),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activePoint.landmarkName,
                                fontSize = 10.sp,
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expedition Relief Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReliefMetric(label = "PEAK SUMMIT", value = "${telemetry.maxSummitElevationFt} ft")
                ReliefMetric(label = "RIVER TRENCH", value = "${telemetry.minValleyElevationFt} ft")
                ReliefMetric(label = "TOTAL ASCENT", value = "+${telemetry.totalAscentFt} ft")
                ReliefMetric(
                    label = "MAX GRADE",
                    value = "${String.format("%.1f", kotlin.math.abs(telemetry.maxGradePercent))}%",
                    highlight = true
                )
            }
        }
    }
}

@Composable
private fun ReliefMetric(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFFFBBF24) else Color(0xFFF4EAD4)
        )
    }
}
