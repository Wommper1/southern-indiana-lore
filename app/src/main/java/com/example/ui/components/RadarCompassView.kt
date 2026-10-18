package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoreCategories
import com.example.data.model.LoreSite
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.util.LocationUtils
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarCompassView(
    userLat: Double,
    userLng: Double,
    sites: List<LoreSite>,
    selectedSite: LoreSite?,
    onSiteSelected: (LoreSite) -> Unit,
    hardwareHeadingDegrees: Float = 0f,
    isCompassAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Max radar radius represents 15 km (~9.3 miles) to encompass regional lore
    val maxRadiusMeters = 15000f

    // Animated radar sweep angle
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        ),
        label = "sweep_angle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SlateNightElevated)
            .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("radar_compass_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LanternAmberBright)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SOUTHERN INDIANA LORE RADAR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LanternAmberBright
                )
            }

            Surface(
                color = SlateNight,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                val headingText = if (isCompassAvailable) {
                    "🧭 ${hardwareHeadingDegrees.toInt()}°"
                } else {
                    "Range: 9.3 mi"
                }
                Text(
                    text = headingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentPaper,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circular Radar Screen
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0F1B2B),
                            Color(0xFF0A101C),
                            Color(0xFF06090F)
                        )
                    )
                )
                .border(2.dp, SlateBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(sites, userLat, userLng) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f

                            // Find tapped blip within 28dp tolerance
                            val tappedSite = sites.firstOrNull { site ->
                                val dist = LocationUtils.calculateDistanceMeters(
                                    userLat, userLng,
                                    site.latitude, site.longitude
                                )
                                val bearing = LocationUtils.calculateBearing(
                                    userLat, userLng,
                                    site.latitude, site.longitude
                                )
                                val scaledDist = (dist / maxRadiusMeters).coerceAtMost(1f) * radius
                                val rad = Math.toRadians(bearing.toDouble() - 90.0)
                                val blipX = center.x + (scaledDist * cos(rad)).toFloat()
                                val blipY = center.y + (scaledDist * sin(rad)).toFloat()

                                val dx = tapOffset.x - blipX
                                val dy = tapOffset.y - blipY
                                (dx * dx + dy * dy) <= (28f * 28f)
                            }
                            if (tappedSite != null) {
                                onSiteSelected(tappedSite)
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radarRadius = size.width / 2f

                // Concentric range rings
                val rings = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                rings.forEach { fraction ->
                    drawCircle(
                        color = Color(0x3338BDF8),
                        radius = radarRadius * fraction,
                        center = center,
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )
                }

                // Crosshair lines (North-South, East-West)
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x22FFFFFF),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1f
                )

                // Rotating radar sweep line
                val sweepRad = Math.toRadians(sweepAngle.toDouble() - 90.0)
                val sweepX = center.x + (radarRadius * cos(sweepRad)).toFloat()
                val sweepY = center.y + (radarRadius * sin(sweepRad)).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x00F59E0B), Color(0xCCF59E0B)),
                        start = center,
                        end = Offset(sweepX, sweepY)
                    ),
                    start = center,
                    end = Offset(sweepX, sweepY),
                    strokeWidth = 2.5f
                )

                // Plot sites as radar blips
                sites.forEach { site ->
                    val dist = LocationUtils.calculateDistanceMeters(
                        userLat, userLng,
                        site.latitude, site.longitude
                    )
                    val bearing = LocationUtils.calculateBearing(
                        userLat, userLng,
                        site.latitude, site.longitude
                    )
                    val scaledDist = (dist / maxRadiusMeters).coerceAtMost(0.95f) * radarRadius
                    val rad = Math.toRadians(bearing.toDouble() - 90.0)
                    val blipX = center.x + (scaledDist * cos(rad)).toFloat()
                    val blipY = center.y + (scaledDist * sin(rad)).toFloat()

                    val blipColor = when {
                        site.radarType.equals("BRIDGE", ignoreCase = true) || site.title.contains("Bridge", ignoreCase = true) || site.title.contains("Trestle", ignoreCase = true) || site.title.contains("Viaduct", ignoreCase = true) -> Color(0xFFF59E0B)
                        site.radarType.equals("UNDERGROUND", ignoreCase = true) -> Color(0xFF06B6D4)
                        site.radarType.equals("CROSSING", ignoreCase = true) -> Color(0xFF38BDF8)
                        site.radarType.equals("LOST_LANDSCAPE", ignoreCase = true) -> Color(0xFFF97316)
                        site.category == LoreCategories.HAUNTED -> GhostCrimson
                        site.category == LoreCategories.UNDERGROUND_RR -> RiverMistBlue
                        site.category == LoreCategories.STEAM_AND_RAIL -> LanternAmberBright
                        site.category == LoreCategories.CURIOSITIES -> KnobstoneForest
                        else -> LanternAmber
                    }

                    val isSelected = selectedSite?.id == site.id

                    // Subterranean karst ring if underground
                    if (site.radarType.equals("UNDERGROUND", ignoreCase = true)) {
                        drawCircle(
                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                            radius = if (isSelected) 18f else 10f,
                            center = Offset(blipX, blipY),
                            style = Stroke(width = 1.5f)
                        )
                    }

                    // Bridge span dashed ring
                    if (site.radarType.equals("BRIDGE", ignoreCase = true) ||
                        site.title.contains("Bridge", ignoreCase = true) ||
                        site.title.contains("Trestle", ignoreCase = true) ||
                        site.title.contains("Viaduct", ignoreCase = true)) {
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = 0.65f),
                            radius = if (isSelected) 20f else 12f,
                            center = Offset(blipX, blipY),
                            style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                        )
                    }

                    // Halo if selected
                    if (isSelected) {
                        drawCircle(
                            color = blipColor.copy(alpha = 0.35f),
                            radius = 16f,
                            center = Offset(blipX, blipY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = Offset(blipX, blipY),
                            style = Stroke(width = 2f)
                        )
                    }

                    // Dot
                    drawCircle(
                        color = blipColor,
                        radius = if (isSelected) 8f else 5.5f,
                        center = Offset(blipX, blipY)
                    )
                }

                // User Center Marker
                drawCircle(
                    color = LanternAmberBright,
                    radius = 6f,
                    center = center
                )
                drawCircle(
                    color = LanternAmberBright.copy(alpha = 0.4f),
                    radius = 12f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Cardinal Indicators on Edge
            Text(
                text = "N",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = LanternAmberBright,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
            )
            Text(
                text = "S",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
            Text(
                text = "E",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
            Text(
                text = "W",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Site Highlight Box
        if (selectedSite != null) {
            Surface(
                color = SlateNight,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = LanternAmberBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedSite.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        val typeTag = when (selectedSite.radarType) {
                            "UNDERGROUND" -> "🕳️ Karst / Subterranean"
                            "CROSSING" -> "〰️ Corridor Crossing"
                            "LOST_LANDSCAPE" -> "🏛️ Vanished Landscape"
                            else -> selectedSite.category
                        }
                        Text(
                            text = "${LocationUtils.formatDistance(selectedSite.distanceMeters)} • $typeTag",
                            style = MaterialTheme.typography.bodySmall,
                            color = LanternAmber
                        )
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tap any radar beacon to inspect bearing & story",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
