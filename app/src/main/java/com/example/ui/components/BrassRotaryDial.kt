package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AdventureSoundEngine
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Authentic 19th-Century Antique Brass Rotary Dial:
 * Physical expedition instrument with concentric burnished brass bezel, engraved tick detents,
 * knurled grip, mechanical ratchet clicks, and magnetic snap-to-detent physics.
 *
 * Used for selecting Historic Eras (Pioneer, Civil War, Victorian, Modern)
 * or Range Radius / Audio speeds.
 */
@Composable
fun BrassRotaryDial(
    options: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    title: String = "ERA SELECTOR",
    soundEngine: AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    val numDetents = options.size
    val sweepAngle = 240f // Arc from -120° to +120°
    val startAngle = -120f
    val stepAngle = if (numDetents > 1) sweepAngle / (numDetents - 1) else 0f

    val targetAngle = startAngle + (selectedIndex * stepAngle)
    val animatedAngle = remember { Animatable(targetAngle) }
    val scope = rememberCoroutineScope()

    var lastClickedIndex by remember { mutableIntStateOf(selectedIndex) }

    LaunchedEffect(selectedIndex) {
        animatedAngle.animateTo(
            targetValue = startAngle + (selectedIndex * stepAngle),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Surface(
        color = Color(0xFF18120C),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC8963E)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("brass_rotary_dial_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color(0xFFE5B358)
                )

                Surface(
                    color = Color(0xFF2C1E12),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8963E))
                ) {
                    Text(
                        text = options.getOrElse(selectedIndex) { "" },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFF3C853)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Antique Rotary Knob
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .pointerInput(options.size) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPos = change.position
                                val delta = touchPos - center
                                val angleRad = atan2(delta.y, delta.x)
                                var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() - 90f

                                // Normalize angle to range around startAngle to startAngle + sweepAngle
                                while (angleDeg < -180f) angleDeg += 360f
                                while (angleDeg > 180f) angleDeg -= 360f

                                val clamped = angleDeg.coerceIn(startAngle, startAngle + sweepAngle)
                                val nearestIndex = if (stepAngle > 0) {
                                    ((clamped - startAngle) / stepAngle).roundToInt().coerceIn(0, numDetents - 1)
                                } else 0

                                if (nearestIndex != lastClickedIndex) {
                                    lastClickedIndex = nearestIndex
                                    soundEngine?.vibrateTick()
                                    soundEngine?.playCompassClick()
                                    onIndexSelected(nearestIndex)
                                }
                            }
                        )
                    }
                    .pointerInput(options.size) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val delta = offset - center
                            val angleRad = atan2(delta.y, delta.x)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() - 90f
                            while (angleDeg < -180f) angleDeg += 360f
                            while (angleDeg > 180f) angleDeg -= 360f

                            val clamped = angleDeg.coerceIn(startAngle, startAngle + sweepAngle)
                            val nearestIndex = if (stepAngle > 0) {
                                ((clamped - startAngle) / stepAngle).roundToInt().coerceIn(0, numDetents - 1)
                            } else 0

                            soundEngine?.vibrateTick()
                            soundEngine?.playCompassClick()
                            onIndexSelected(nearestIndex)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width / 2f - 6f
                    val dialRadius = outerRadius - 16f
                    val knobRadius = dialRadius - 20f

                    // 1. Burnished Brass Outer Rim with Metallic Radial Gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF3C853), // Highlight gold
                                Color(0xFFC8963E), // Polished brass
                                Color(0xFF784D16), // Dark antique edge
                                Color(0xFF3E230B)  // Rivet shadow
                            ),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )

                    // 2. Brass Bezel Grooves & Engraved Rivets
                    drawCircle(
                        color = Color(0xFF1E1106),
                        radius = dialRadius + 12f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    // 8 Brass Screws / Rivets around outer rim
                    for (i in 0 until 8) {
                        val rivetAngle = (i * 45f) * (PI / 180.0)
                        val rivetDist = outerRadius - 7f
                        val rx = center.x + (rivetDist * cos(rivetAngle)).toFloat()
                        val ry = center.y + (rivetDist * sin(rivetAngle)).toFloat()

                        drawCircle(color = Color(0xFF2B1807), radius = 3f, center = Offset(rx, ry))
                        drawCircle(color = Color(0xFFF3C853), radius = 1.5f, center = Offset(rx - 0.5f, ry - 0.5f))
                    }

                    // 3. Aged Cartography Parchment Dial Face
                    drawCircle(
                        color = Color(0xFF26190E),
                        radius = dialRadius,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF4A331E),
                        radius = dialRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )

                    // 4. Detent Tick Marks and Era Labels
                    for (i in 0 until numDetents) {
                        val detentAngle = startAngle + (i * stepAngle)
                        val isSelected = i == selectedIndex
                        val angleRad = (detentAngle + 90f) * (PI / 180.0)

                        // Outer detent tick
                        val r1 = dialRadius - 4f
                        val r2 = dialRadius - (if (isSelected) 14f else 8f)
                        val p1 = Offset(center.x + (r1 * cos(angleRad)).toFloat(), center.y + (r1 * sin(angleRad)).toFloat())
                        val p2 = Offset(center.x + (r2 * cos(angleRad)).toFloat(), center.y + (r2 * sin(angleRad)).toFloat())

                        drawLine(
                            color = if (isSelected) Color(0xFFF3C853) else Color(0xFF8B6B40),
                            start = p1,
                            end = p2,
                            strokeWidth = if (isSelected) 3.5f else 1.8f,
                            cap = StrokeCap.Round
                        )

                        // Detent Jewel
                        if (isSelected) {
                            val rDot = dialRadius - 18f
                            val dotPos = Offset(center.x + (rDot * cos(angleRad)).toFloat(), center.y + (rDot * sin(angleRad)).toFloat())
                            drawCircle(color = Color(0xFF8B1E1E), radius = 3.5f, center = dotPos)
                            drawCircle(color = Color(0xFFF3C853), radius = 1.5f, center = dotPos)
                        }
                    }

                    // 5. Central Brass Rotary Knob (Physical knurled turn dial)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE5B358),
                                Color(0xFF9E6D24),
                                Color(0xFF4A2C0C)
                            ),
                            center = center,
                            radius = knobRadius
                        ),
                        radius = knobRadius,
                        center = center
                    )

                    // Knurled grip ridges
                    drawCircle(
                        color = Color(0xFF2E1906),
                        radius = knobRadius - 2f,
                        center = center,
                        style = Stroke(width = 3f)
                    )

                    // 6. Engraved Indicator Arrow / Pointer
                    rotate(degrees = animatedAngle.value, pivot = center) {
                        val pointerPath = Path().apply {
                            moveTo(center.x, center.y - knobRadius + 4f)
                            lineTo(center.x - 7f, center.y - 12f)
                            lineTo(center.x + 7f, center.y - 12f)
                            close()
                        }
                        drawPath(
                            path = pointerPath,
                            color = Color(0xFF8B1E1E) // Archival crimson arrow
                        )

                        // Center brass pivot hub
                        drawCircle(
                            color = Color(0xFFF3C853),
                            radius = 10f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF331D08),
                            radius = 4f,
                            center = center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rapid Selection Detent Chips Below Dial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed { idx, label ->
                    val isSelected = idx == selectedIndex
                    Surface(
                        onClick = {
                            soundEngine?.vibrateTick()
                            soundEngine?.playCompassClick()
                            onIndexSelected(idx)
                        },
                        color = if (isSelected) Color(0xFF382310) else Color(0xFF1E150D),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFE5B358) else Color(0xFF3E2C1E)
                        )
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFFDE68A) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
