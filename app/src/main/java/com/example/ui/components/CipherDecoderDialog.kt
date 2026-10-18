package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CipherEngine
import com.example.data.model.CipherRiddle
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun CipherDecoderDialog(
    riddle: CipherRiddle,
    onDismiss: () -> Unit,
    onCipherSolved: (CipherRiddle) -> Unit,
    soundEngine: com.example.util.AdventureSoundEngine? = null
) {
    var currentShift by remember { mutableIntStateOf(0) }
    var showHint by remember { mutableStateOf(false) }

    val isSolved = currentShift == riddle.shiftKey || riddle.isSolved
    val liveDecodedText = remember(currentShift) {
        CipherEngine.decodeCaesar(riddle.ciphertext, currentShift)
    }

    androidx.compose.runtime.LaunchedEffect(isSolved) {
        if (isSolved && !riddle.isSolved) {
            soundEngine?.playVaultLatchRelease()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, if (isSolved) GildedGoldBright else SlateBorder, RoundedCornerShape(24.dp))
                .testTag("cipher_decoder_dialog"),
            color = SlateNight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSolved) GildedGold.copy(alpha = 0.2f) else SlateNightSurface)
                                .border(1.dp, if (isSolved) GildedGoldBright else SlateBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSolved) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isSolved) GildedGoldBright else LanternAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CIPHER WHEEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = GildedGoldBright
                            )
                            Text(
                                text = riddle.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Historical Premise
                Text(
                    text = riddle.historicalContext,
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Dual-Ring Brass Cipher Wheel
                BrassCipherWheelCanvas(
                    shift = currentShift,
                    onShiftChange = { newShift -> currentShift = (newShift % 26 + 26) % 26 },
                    isSolved = isSolved
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Shift Controller Stepper Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundEngine?.playCompassClick()
                            currentShift = (currentShift - 1 + 26) % 26
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SlateNightElevated)
                            .border(1.dp, SlateBorder, CircleShape)
                            .testTag("cipher_prev_shift_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Step Back", tint = LanternAmber)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        color = ShelfWoodDark,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SHIFT OFFSET",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                                color = GildedGold
                            )
                            val keyLetter = ('A'.code + currentShift).toChar()
                            Text(
                                text = "+$currentShift (A → $keyLetter)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isSolved) GildedGoldBright else ParchmentPaper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            soundEngine?.playCompassClick()
                            currentShift = (currentShift + 1) % 26
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SlateNightElevated)
                            .border(1.dp, SlateBorder, CircleShape)
                            .testTag("cipher_next_shift_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Step Forward", tint = LanternAmber)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Decryption Result Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSolved) Color(0xFF1B2E1D) else SlateNightElevated
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSolved) Color(0xFF10B981) else SlateBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CIPHERTEXT:",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = ParchmentMuted
                            )
                            Text(
                                text = if (isSolved) "DECRYPTED" else "ROTATING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSolved) Color(0xFF34D399) else LanternAmber
                            )
                        }

                        Text(
                            text = riddle.ciphertext,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "LIVE TRANSLATION:",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = if (isSolved) Color(0xFF34D399) else GildedGold
                        )

                        Text(
                            text = liveDecodedText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSolved) GildedGoldBright else ParchmentPaper
                        )
                    }
                }

                // Unlocked Secret Callout when Solved
                AnimatedVisibility(
                    visible = isSolved,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Surface(
                            color = Color(0x33D4AF37),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GildedGoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SECRET COORDINATE REVEALED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = GildedGoldBright
                                    )
                                    Text(
                                        text = riddle.unlockedSecret,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ParchmentPaper
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showHint = !showHint },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmber)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showHint) "Hide Clue" else "Inspector Clue")
                    }

                    if (isSolved) {
                        Button(
                            onClick = {
                                onCipherSolved(riddle.copy(isSolved = true))
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cipher_claim_reward_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GildedGold,
                                contentColor = Color(0xFF1E1100)
                            )
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Claim Secret", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showHint) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔎 FIELD CLUE: ${riddle.hint}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LanternAmberBright,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing concentric Jefferson brass cipher disks
 * Outer disk: Fixed Alphabet A-Z
 * Inner disk: Rotatable Alphabet with brass rivet accents and angle calculation
 */
@Composable
fun BrassCipherWheelCanvas(
    shift: Int,
    onShiftChange: (Int) -> Unit,
    isSolved: Boolean,
    modifier: Modifier = Modifier
) {
    var dragAngleOffset by remember { mutableFloatStateOf(0f) }
    val stepAngle = 360f / 26f

    // Animated rotation angle
    val targetRotation = shift * stepAngle
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        label = "cipher_wheel_rotation"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag along x/y updates the shift
                    dragAngleOffset += dragAmount.x * 0.4f
                    if (Math.abs(dragAngleOffset) >= stepAngle) {
                        val steps = (dragAngleOffset / stepAngle).toInt()
                        onShiftChange(shift + steps)
                        dragAngleOffset %= stepAngle
                    }
                }
            }
            .testTag("brass_cipher_wheel_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4.dp.toPx()
            val innerRadius = outerRadius * 0.72f
            val hubRadius = innerRadius * 0.42f

            // Outer Brass Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8D5B18), Color(0xFF4A2E0B), Color(0xFF1E1408)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // Outer Gilded Border
            drawCircle(
                color = if (isSolved) Color(0xFFF3C853) else Color(0xFFD4AF37),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner Rotating Disk
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isSolved) Color(0xFF2E4E30) else Color(0xFF6B4413),
                        if (isSolved) Color(0xFF18301B) else Color(0xFF3B2208)
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            drawCircle(
                color = if (isSolved) Color(0xFF34D399) else Color(0xFFF59E0B),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center Brass Hub & Pivot
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF1E1408),
                radius = hubRadius * 0.4f,
                center = center
            )

            // Outer 26 tick marks
            for (i in 0 until 26) {
                val angleRad = Math.toRadians((i * stepAngle - 90).toDouble())
                val startX = center.x + (outerRadius - 10.dp.toPx()) * Math.cos(angleRad).toFloat()
                val startY = center.y + (outerRadius - 10.dp.toPx()) * Math.sin(angleRad).toFloat()
                val endX = center.x + outerRadius * Math.cos(angleRad).toFloat()
                val endY = center.y + outerRadius * Math.sin(angleRad).toFloat()

                drawLine(
                    color = Color(0xFFFDE68A).copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Inner Rotatable Ring Indicator Needle
        Icon(
            imageVector = Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = "Rotate Dial",
            tint = if (isSolved) GildedGoldBright else Color(0xFFD4AF37),
            modifier = Modifier
                .size(32.dp)
                .rotate(-animatedRotation)
        )
    }
}
