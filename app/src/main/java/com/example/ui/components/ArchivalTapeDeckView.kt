package com.example.ui.components

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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RelicSpecimen
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Archival Magnetic Reel-to-Reel Tape Deck & AM Radio Dispatch Decoder:
 * Plays restored historical field recordings synthesized with magnetic tape hiss,
 * 60Hz hum, WOW/flutter modulation, and an analog VU meter.
 */
@Composable
fun ArchivalTapeDeckView(
    specimen: RelicSpecimen,
    narratorManager: NarratorManager?,
    ambientSoundEngine: AmbientSoundEngine?,
    adventureSoundEngine: AdventureSoundEngine?,
    modifier: Modifier = Modifier
) {
    var isPlayingTape by remember { mutableStateOf(false) }
    var currentFrequencyKhz by remember(specimen.id) { mutableFloatStateOf(specimen.archivalFrequencyKhz.toFloat()) }
    var isTapeHissEnabled by remember { mutableStateOf(true) }

    val isNarratorSpeaking = narratorManager?.isPlaying?.collectAsState()?.value ?: false

    // Reel spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "reel_spin")
    val reelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reel_angle"
    )

    // Tuning accuracy: within 20 kHz is locked on
    val isTunedIn = remember(currentFrequencyKhz, specimen.archivalFrequencyKhz) {
        abs(currentFrequencyKhz - specimen.archivalFrequencyKhz) <= 25f
    }

    // Stop playback if leaving
    DisposableEffect(specimen.id) {
        onDispose {
            if (isPlayingTape) {
                narratorManager?.stop()
                ambientSoundEngine?.stopAmbient()
            }
        }
    }

    LaunchedEffect(isNarratorSpeaking) {
        if (!isNarratorSpeaking && isPlayingTape) {
            isPlayingTape = false
            ambientSoundEngine?.stopAmbient()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("archival_tape_deck_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Antiquarian Bakelite & Brass Enclosure
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            color = Color(0xFF1B130B),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(GildedGold, Color(0xFF5A4418), GildedGold)))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Chassis Badge and Vacuum Tube Glow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Vacuum Tube Glow Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingTape) Color(0xFFFF9E3B) else Color(0xFF6B4515))
                                .border(1.dp, GildedGoldBright, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODEL 1874 ARCHIVAL REEL-TO-REEL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = GildedGoldBright
                        )
                    }

                    // AM Radio Dial Badge
                    Surface(
                        color = Color(0xFF2E1C0C),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isTunedIn) LanternAmberBright else ParchmentMuted.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${currentFrequencyKhz.toInt()} kHz AM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTunedIn) LanternAmberBright else ParchmentMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Reel Deck & Analog VU Meter Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF100A05))
                        .border(1.dp, Color(0xFF3E2812), RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawTapeDeckHardware(
                            isPlaying = isPlayingTape || isNarratorSpeaking,
                            reelAngle = if (isPlayingTape || isNarratorSpeaking) reelRotation else 0f,
                            isTunedIn = isTunedIn
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frequency Tuning Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Archival Radio Receiver Tuning:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                        Text(
                            text = if (isTunedIn) "SIGNAL LOCKED • ${specimen.archivalFrequencyKhz} kHz" else "SEARCHING FOR CARRIER...",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTunedIn) LanternAmberBright else Color(0xFFE57373)
                        )
                    }

                    Slider(
                        value = currentFrequencyKhz,
                        onValueChange = { newFreq ->
                            currentFrequencyKhz = newFreq
                            adventureSoundEngine?.playRadioTuningSweep(if (newFreq > currentFrequencyKhz) 1 else -1)
                        },
                        valueRange = 540f..1600f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = LanternAmberBright,
                            activeTrackColor = LanternAmber,
                            inactiveTrackColor = Color(0xFF3E2812)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("radio_frequency_slider")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Playback Transport Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause Button
                    Button(
                        onClick = {
                            if (isPlayingTape || isNarratorSpeaking) {
                                adventureSoundEngine?.playTapeDeckLatch()
                                isPlayingTape = false
                                narratorManager?.stop()
                                ambientSoundEngine?.stopAmbient()
                            } else {
                                adventureSoundEngine?.playTapeDeckLatch()
                                isPlayingTape = true
                                if (isTapeHissEnabled) {
                                    ambientSoundEngine?.startAmbient("TAPE_HISS")
                                }
                                narratorManager?.speak(
                                    siteId = specimen.id,
                                    title = specimen.archivalBroadcastTitle,
                                    text = specimen.archivalDispatchTranscript,
                                    forceRestart = true
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingTape) Color(0xFFC2410C) else LanternAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("play_tape_broadcast_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingTape) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingTape) "Halt Playback" else "Decoded Broadcast",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Tape Hiss Toggle
                    Surface(
                        color = if (isTapeHissEnabled) Color(0xFF382312) else Color(0xFF1D140D),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isTapeHissEnabled) LanternAmberBright else ParchmentMuted.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .clickable {
                                adventureSoundEngine?.playTactileClick()
                                isTapeHissEnabled = !isTapeHissEnabled
                                if (isPlayingTape) {
                                    if (isTapeHissEnabled) {
                                        ambientSoundEngine?.startAmbient("TAPE_HISS")
                                    } else {
                                        ambientSoundEngine?.stopAmbient()
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("toggle_tape_hiss_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isTapeHissEnabled) LanternAmberBright else ParchmentMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tape Hiss",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isTapeHissEnabled) LanternAmberBright else ParchmentMuted
                            )
                        }
                    }
                }
            }
        }

        // Restored Archival Dispatch Transcript Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF22180E)),
            border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = specimen.archivalBroadcastTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GildedGoldBright
                    )

                    Surface(
                        color = Color(0xFF3A2414),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = specimen.speakerRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = LanternAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Speaker: ${specimen.speakerName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\"${specimen.archivalDispatchTranscript}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    color = ParchmentPaper
                )
            }
        }
    }
}

/**
 * Draws the spinning tape spools, magnetic tape loop, and backlit analog VU meter.
 */
private fun DrawScope.drawTapeDeckHardware(
    isPlaying: Boolean,
    reelAngle: Float,
    isTunedIn: Boolean
) {
    val reelRadius = size.height * 0.38f
    val leftReelCenter = Offset(size.width * 0.22f, size.height * 0.50f)
    val rightReelCenter = Offset(size.width * 0.78f, size.height * 0.50f)

    // Draw Magnetic Tape Span between reels
    val tapeY = size.height * 0.76f
    drawLine(
        color = Color(0xFF382315),
        start = Offset(leftReelCenter.x, tapeY),
        end = Offset(rightReelCenter.x, tapeY),
        strokeWidth = 6f
    )

    // Center Headblock (Magnetic playback pickup)
    val headBlockX = size.width * 0.5f
    drawRect(
        color = Color(0xFF6B4515),
        topLeft = Offset(headBlockX - 18f, tapeY - 8f),
        size = Size(36f, 16f)
    )
    drawRect(
        color = GildedGoldBright,
        topLeft = Offset(headBlockX - 3f, tapeY - 6f),
        size = Size(6f, 12f)
    )

    // Draw Left Supply Reel
    drawSpoolReel(leftReelCenter, reelRadius, reelAngle)

    // Draw Right Take-Up Reel
    drawSpoolReel(rightReelCenter, reelRadius, -reelAngle)

    // Central Analog VU Meter (Glowing Amber Gauge)
    drawAnalogVuMeter(
        center = Offset(size.width * 0.5f, size.height * 0.35f),
        width = size.width * 0.28f,
        height = size.height * 0.45f,
        isPlaying = isPlaying,
        isTunedIn = isTunedIn
    )
}

/**
 * Draws an individual 3-spoke tape reel spool.
 */
private fun DrawScope.drawSpoolReel(center: Offset, radius: Float, angleDegrees: Float) {
    // Outer acrylic reel rim
    drawCircle(
        color = Color(0xFF26190D),
        radius = radius,
        center = center
    )
    drawCircle(
        color = GildedGold.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = Stroke(width = 3f)
    )

    // Inner tape pack (dark brown magnetic ribbon)
    drawCircle(
        color = Color(0xFF4A2A12),
        radius = radius * 0.75f,
        center = center
    )

    // Center metal hub
    drawCircle(
        color = Color(0xFF9E7B35),
        radius = radius * 0.32f,
        center = center
    )
    drawCircle(
        color = Color(0xFF1B1107),
        radius = radius * 0.12f,
        center = center
    )

    // 3 Cutout Spokes
    val rad = angleDegrees * (PI / 180.0)
    for (i in 0 until 3) {
        val spAngle = rad + (i * 2.0 * PI / 3.0)
        val spX = center.x + (cos(spAngle) * radius * 0.6f).toFloat()
        val spY = center.y + (sin(spAngle) * radius * 0.6f).toFloat()
        drawLine(
            color = Color(0xFF1A1006),
            start = center,
            end = Offset(spX, spY),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws an analog VU needle meter with warm backlit dial.
 */
private fun DrawScope.drawAnalogVuMeter(
    center: Offset,
    width: Float,
    height: Float,
    isPlaying: Boolean,
    isTunedIn: Boolean
) {
    val meterRect = TapeDeckRectF(center.x - width * 0.5f, center.y - height * 0.5f, center.x + width * 0.5f, center.y + height * 0.5f)

    // Backlit Warm Amber Face
    drawRoundRect(
        color = Color(0xFF332010),
        topLeft = Offset(meterRect.left, meterRect.top),
        size = Size(meterRect.width, meterRect.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = GildedGold.copy(alpha = 0.4f),
        topLeft = Offset(meterRect.left, meterRect.top),
        size = Size(meterRect.width, meterRect.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    // Arc Scale
    drawArc(
        color = LanternAmberBright.copy(alpha = 0.6f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(meterRect.left + 8f, meterRect.top + 8f),
        size = Size(meterRect.width - 16f, meterRect.height * 1.2f),
        style = Stroke(width = 2f)
    )

    // Animated Needle Deflection
    val needleDeflection = if (isPlaying && isTunedIn) 0.65f else if (isPlaying) 0.25f else 0.08f
    val needleAngle = 200f + (needleDeflection * 140f)
    val rad = needleAngle * (PI / 180.0)
    val pivot = Offset(center.x, meterRect.bottom - 4f)
    val needleLen = height * 0.75f
    val tipX = pivot.x + (cos(rad) * needleLen).toFloat()
    val tipY = pivot.y + (sin(rad) * needleLen).toFloat()

    // Shadow
    drawLine(
        color = Color(0x66000000),
        start = pivot,
        end = Offset(tipX + 2f, tipY + 2f),
        strokeWidth = 2.5f
    )
    // Needle
    drawLine(
        color = if (needleDeflection > 0.5f) Color(0xFFFF5252) else LanternAmberBright,
        start = pivot,
        end = Offset(tipX, tipY),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )

    // Pivot cap
    drawCircle(
        color = Color(0xFF1B1107),
        radius = 5f,
        center = pivot
    )
}

private data class TapeDeckRectF(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}
