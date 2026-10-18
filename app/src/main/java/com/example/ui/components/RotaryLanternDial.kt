package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LanternLightingMode
import com.example.ui.theme.LanternLightingState
import com.example.util.AdventureSoundEngine
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mechanical brass rotary lantern dimmer & UV selector.
 * Features:
 * - Rotational thumb drag physics with 60-degree detent haptic ticks
 * - Steampunk knurled rim, center vacuum glass filament lens, and brass pointer
 * - Instant tap-to-cycle mode shortcut
 * - Smoothly morphing illumination glow
 */
@Composable
fun RotaryLanternDial(
    lightingState: LanternLightingState,
    onLightingModeChanged: (LanternLightingMode) -> Unit,
    soundEngine: AdventureSoundEngine?,
    modifier: Modifier = Modifier
) {
    val currentMode = lightingState.mode

    // Target angle based on mode: Kerosene=0°, Torch=120°, UV=240°
    val targetAngle = when (currentMode) {
        LanternLightingMode.KEROSENE -> 0f
        LanternLightingMode.TORCH -> 120f
        LanternLightingMode.BLACKLIGHT_UV -> 240f
    }

    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "dial_rot"
    )

    val glowColor by animateColorAsState(
        targetValue = currentMode.glowColor,
        animationSpec = tween(durationMillis = 280),
        label = "glow_color"
    )

    var lastDetentIndex by remember { mutableStateOf(currentMode.ordinal) }

    Surface(
        color = Color(0xFF160E08),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC89B3C).copy(alpha = 0.7f)),
        shadowElevation = 3.dp,
        modifier = modifier
            .testTag("rotary_lantern_dial_container")
            .clickable {
                soundEngine?.playLanternWickDial()
                val next = currentMode.next()
                onLightingModeChanged(next)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            // Physical Knurled Rotary Dial Wheel
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val angleRad = atan2(
                                (change.position.y - center.y).toDouble(),
                                (change.position.x - center.x).toDouble()
                            )
                            var deg = Math.toDegrees(angleRad).toFloat()
                            if (deg < 0) deg += 360f

                            val newMode = when {
                                deg < 90f || deg >= 330f -> LanternLightingMode.KEROSENE
                                deg in 90f..210f -> LanternLightingMode.TORCH
                                else -> LanternLightingMode.BLACKLIGHT_UV
                            }

                            if (newMode.ordinal != lastDetentIndex) {
                                lastDetentIndex = newMode.ordinal
                                soundEngine?.playLanternWickDial()
                                onLightingModeChanged(newMode)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Brass bezel, knurling notches, and center vacuum filament
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedAngle)
                ) {
                    val center = size.center
                    val radius = size.minDimension / 2f - 2.dp.toPx()

                    // Outer brass bezel gradient
                    val brassBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF3C853),
                            Color(0xFFC89B3C),
                            Color(0xFF784F17),
                            Color(0xFF3E2207)
                        ),
                        center = center,
                        radius = radius
                    )
                    drawCircle(brush = brassBrush, radius = radius, center = center)

                    // Knurled detent notches around rim
                    val notchCount = 12
                    for (i in 0 until notchCount) {
                        val angle = (i * (360f / notchCount)) * (PI.toFloat() / 180f)
                        val start = Offset(
                            center.x + (radius - 4.dp.toPx()) * cos(angle),
                            center.y + (radius - 4.dp.toPx()) * sin(angle)
                        )
                        val end = Offset(
                            center.x + radius * cos(angle),
                            center.y + radius * sin(angle)
                        )
                        drawLine(
                            color = Color(0xFF261505),
                            start = start,
                            end = end,
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Inner recessed filament well
                    val innerRadius = radius * 0.62f
                    drawCircle(
                        color = Color(0xFF110A05),
                        radius = innerRadius,
                        center = center
                    )

                    // Glowing vacuum filament bulb
                    val filamentGlow = Brush.radialGradient(
                        colors = listOf(
                            glowColor,
                            glowColor.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = innerRadius
                    )
                    drawCircle(brush = filamentGlow, radius = innerRadius, center = center)

                    // Brass Indicator Needle / Pip at top
                    val pipPath = Path().apply {
                        moveTo(center.x, center.y - radius + 2.dp.toPx())
                        lineTo(center.x - 3.dp.toPx(), center.y - innerRadius)
                        lineTo(center.x + 3.dp.toPx(), center.y - innerRadius)
                        close()
                    }
                    drawPath(pipPath, color = Color(0xFFFFF1C2))
                    drawPath(pipPath, color = Color(0xFF422104), style = Stroke(width = 0.8.dp.toPx()))
                }

                // Center glowing pip indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(glowColor)
                        .border(1.dp, Color(0xFFFFF8DC), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text descriptor with period nomenclature
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (currentMode) {
                            LanternLightingMode.KEROSENE -> "KEROSENE"
                            LanternLightingMode.TORCH -> "TORCH"
                            LanternLightingMode.BLACKLIGHT_UV -> "UV INK"
                        },
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = if (currentMode == LanternLightingMode.BLACKLIGHT_UV) Color(0xFFE9D5FF) else Color(0xFFFDE68A)
                    )
                    if (currentMode == LanternLightingMode.BLACKLIGHT_UV) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC084FC))
                        )
                    }
                }
                Text(
                    text = when (currentMode) {
                        LanternLightingMode.KEROSENE -> "2200K Glow"
                        LanternLightingMode.TORCH -> "Cavern Green"
                        LanternLightingMode.BLACKLIGHT_UV -> "Marginalia Revealed"
                    },
                    fontSize = 9.sp,
                    color = Color(0xFFB5A48B)
                )
            }
        }
    }
}
