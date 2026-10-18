package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.util.ExpeditionAudioEngine
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tactile Brass Subterranean Cable Winch Console.
 * Features:
 * - Rotational circular drag crank with cable spool inertia
 * - Dynamic cable tension physics based on suspended wire weight and water resistance
 * - Acoustic mechanical pawl ratchet ticks & braided wire groans
 * - Precision dual vernier gauges for depth in feet and tension in pounds
 */
@Composable
fun HydrophoneWinchView(
    currentDepthFt: Float,
    maxDepthFt: Float,
    waterTableDepthFt: Float,
    isWinchLocked: Boolean,
    onDepthChanged: (Float) -> Unit,
    onToggleLock: () -> Unit,
    audioEngine: ExpeditionAudioEngine?,
    modifier: Modifier = Modifier
) {
    // Crank handle rotation angle in degrees
    var crankAngleDegrees by remember { mutableFloatStateOf(0f) }
    var lastToothIndex by remember { mutableStateOf(0) }

    // Dynamic cable tension calculation: dead cable weight + water drag
    val isSubmerged = currentDepthFt >= waterTableDepthFt
    val suspendedCableWeightLbs = (currentDepthFt * 0.045f) + 1.2f
    val waterResistanceLbs = if (isSubmerged) 2.8f else 0f
    val cableTensionLbs = (suspendedCableWeightLbs + waterResistanceLbs).coerceIn(0f, 25f)

    Surface(
        color = Color(0xFF140D07),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFC89B3C)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hydrophone_winch_console")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Console Header & Gauges Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BRASS SOUNDING WINCH",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.6.sp,
                        color = Color(0xFFFDE68A)
                    )
                    Text(
                        text = "Braided Phosphor-Bronze Cable • 500-ft Drum",
                        fontSize = 10.sp,
                        color = Color(0xFFB5A48B)
                    )
                }

                // Mechanical Pawl Lock Toggle
                Surface(
                    color = if (isWinchLocked) Color(0xFF451A03) else Color(0xFF1F2937),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isWinchLocked) Color(0xFFF59E0B) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.clickable {
                        audioEngine?.playBrassToggleSwitch()
                        onToggleLock()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isWinchLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isWinchLocked) "Unlock winch" else "Lock winch",
                            tint = if (isWinchLocked) Color(0xFFFBBF24) else Color(0xFFD1D5DB),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWinchLocked) "PAWL LOCKED" else "WINCH FREE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isWinchLocked) Color(0xFFFBBF24) else Color(0xFFE5E7EB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Analog Telemetry Dials: Depth & Cable Tension
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Dial 1: Depth in Feet
                Surface(
                    color = Color(0xFF1E140B),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SENSOR DEPTH",
                            fontFamily = CinzelFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBCA685)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f FT", currentDepthFt),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isSubmerged) Color(0xFF06B6D4) else Color(0xFFFDE68A)
                        )
                        Text(
                            text = if (isSubmerged) "AQUIFER SUBMERGED" else "VADOSE AIR SHAFT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubmerged) Color(0xFF22D3EE) else Color(0xFF9E8B75)
                        )
                    }
                }

                // Dial 2: Cable Spool Tension (lbs)
                Surface(
                    color = Color(0xFF1E140B),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CABLE TENSION",
                            fontFamily = CinzelFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBCA685)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f LBS", cableTensionLbs),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (cableTensionLbs > 15f) Color(0xFFF43F5E) else Color(0xFF34D399)
                        )
                        Text(
                            text = "LOAD STATUS: NORMAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9E8B75)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // THE PHYSICAL ROTARY CABLE WINCH DRUM & CRANK
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .pointerInput(isWinchLocked) {
                        if (!isWinchLocked) {
                            var previousAngleRad = 0.0
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    previousAngleRad = atan2(
                                        (offset.y - center.y).toDouble(),
                                        (offset.x - center.x).toDouble()
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val currentAngleRad = atan2(
                                        (change.position.y - center.y).toDouble(),
                                        (change.position.x - center.x).toDouble()
                                    )
                                    var deltaRad = currentAngleRad - previousAngleRad
                                    // Normalize wrap around [-PI, PI]
                                    if (deltaRad > PI) deltaRad -= 2 * PI
                                    if (deltaRad < -PI) deltaRad += 2 * PI
                                    previousAngleRad = currentAngleRad

                                    val deltaDeg = Math.toDegrees(deltaRad).toFloat()
                                    crankAngleDegrees += deltaDeg

                                    // Each 18° of clockwise turn drops 1.0 ft; counter-clockwise raises
                                    val depthDelta = (deltaDeg / 18f) * 1.0f
                                    val newDepth = (currentDepthFt + depthDelta).coerceIn(0f, maxDepthFt)

                                    val toothIndex = (crankAngleDegrees / 18f).toInt()
                                    if (toothIndex != lastToothIndex) {
                                        lastToothIndex = toothIndex
                                        audioEngine?.playWinchRatchetTick()

                                        // Submergence splash check
                                        if (currentDepthFt < waterTableDepthFt && newDepth >= waterTableDepthFt) {
                                            audioEngine?.playHydrophoneSplash()
                                        }

                                        // Heavy cable creak on tension
                                        if (cableTensionLbs > 12f && (toothIndex % 8 == 0)) {
                                            audioEngine?.playCableCreak()
                                        }
                                    }

                                    onDepthChanged(newDepth)
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Cog & Steel Cable Drum Graphics
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = size.center
                    val outerRadius = size.minDimension / 2f - 8.dp.toPx()

                    // Outer Bronze Winch Housing Flange
                    val bronzeBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE5B85C),
                            Color(0xFFC89B3C),
                            Color(0xFF6B481A),
                            Color(0xFF2E1906)
                        ),
                        center = center,
                        radius = outerRadius
                    )
                    drawCircle(brush = bronzeBrush, radius = outerRadius, center = center)

                    // Ratchet gear teeth around the outer rim (24 teeth)
                    val toothCount = 24
                    for (i in 0 until toothCount) {
                        val angle = (i * (360f / toothCount)) * (PI.toFloat() / 180f)
                        val r1 = outerRadius - 6.dp.toPx()
                        val r2 = outerRadius
                        drawLine(
                            color = Color(0xFF1E1004),
                            start = Offset(center.x + r1 * cos(angle), center.y + r1 * sin(angle)),
                            end = Offset(center.x + r2 * cos(angle), center.y + r2 * sin(angle)),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Inner recessed cable well
                    val drumRadius = outerRadius * 0.72f
                    drawCircle(
                        color = Color(0xFF170E07),
                        radius = drumRadius,
                        center = center
                    )

                    // Layered Spooled Braided Steel Wire Coils (Density diminishes as cable unspools)
                    val unspoolRatio = (currentDepthFt / maxDepthFt).coerceIn(0f, 1f)
                    val activeCoilRadius = drumRadius * (1f - unspoolRatio * 0.45f)
                    val wireColor = Color(0xFF94A3B8)
                    for (step in 1..6) {
                        val r = (drumRadius * 0.35f) + (step * (activeCoilRadius - drumRadius * 0.35f) / 6f)
                        drawCircle(
                            color = wireColor.copy(alpha = 0.5f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Center Axle Hub
                    drawCircle(
                        color = Color(0xFFC89B3C),
                        radius = 16.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF3E2207),
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }

                // Rotating Brass Crank Handle with Turned Wood Knob
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(crankAngleDegrees)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = size.center
                        val crankArmLength = size.minDimension / 2f - 24.dp.toPx()

                        // Brass Crank Arm Bar
                        drawLine(
                            color = Color(0xFFF3C853),
                            start = center,
                            end = Offset(center.x, center.y - crankArmLength),
                            strokeWidth = 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF784F17),
                            start = center,
                            end = Offset(center.x, center.y - crankArmLength),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Turned Hardwood Grip Knob
                        drawCircle(
                            color = Color(0xFF5A2A18),
                            radius = 12.dp.toPx(),
                            center = Offset(center.x, center.y - crankArmLength)
                        )
                        drawCircle(
                            color = Color(0xFFFFF1C2),
                            radius = 4.dp.toPx(),
                            center = Offset(center.x, center.y - crankArmLength)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isWinchLocked) "PAWL ENGAGED • TAP UNLOCK TO CRANK" else "DRAG CRANK IN A CIRCLE TO UNWIND / WIND CABLE",
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                color = if (isWinchLocked) Color(0xFFFBBF24) else Color(0xFFFDE68A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Step Controls: Lower 10ft, Raise 10ft, Reset Surface
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isWinchLocked) {
                            val newDepth = (currentDepthFt - 10f).coerceAtLeast(0f)
                            audioEngine?.playWinchRatchetTick()
                            onDepthChanged(newDepth)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
                    enabled = !isWinchLocked && currentDepthFt > 0f,
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wind 10'", fontSize = 11.sp, fontFamily = CinzelFontFamily)
                }

                OutlinedButton(
                    onClick = {
                        if (!isWinchLocked) {
                            val newDepth = (currentDepthFt + 10f).coerceAtMost(maxDepthFt)
                            audioEngine?.playWinchRatchetTick()
                            if (currentDepthFt < waterTableDepthFt && newDepth >= waterTableDepthFt) {
                                audioEngine?.playHydrophoneSplash()
                            }
                            onDepthChanged(newDepth)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
                    enabled = !isWinchLocked && currentDepthFt < maxDepthFt,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Spool 10'", fontSize = 11.sp, fontFamily = CinzelFontFamily)
                }
            }
        }
    }
}
