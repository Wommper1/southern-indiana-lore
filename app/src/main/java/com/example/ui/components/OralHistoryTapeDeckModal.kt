package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vintage 1970s analog field tape recorder modal for oral history interviews,
 * EVP acoustics, and eyewitness folklore accounts in Southern Indiana.
 */
@Composable
fun OralHistoryTapeDeckModal(
    siteTitle: String?,
    isRecording: Boolean,
    recordingDurationMs: Long,
    liveMicAmplitude: Float,
    onStartRecording: () -> Unit,
    onStopAndSave: (title: String, informant: String, notes: String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            onStartRecording()
        }
    }

    var memoTitle by remember(siteTitle) {
        mutableStateOf(if (!siteTitle.isNullOrBlank()) "Field Account: $siteTitle" else "Oral History Interview")
    }
    var informantName by remember { mutableStateOf("") }
    var fieldNotes by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "tape_reels")
    val reelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing)
        ),
        label = "reel_spin"
    )

    val animatedAmplitude by animateFloatAsState(
        targetValue = liveMicAmplitude,
        animationSpec = tween(durationMillis = 80),
        label = "vu_amplitude"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color(0xFFB89047), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1B1612),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Deck Header & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXPEDITION TAPE DECK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Model SL-74 Field Archivist",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9284)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Deck",
                            tint = Color(0xFFD4AF37)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Analog Twin Reel Visualizer
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF4A3828), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF120E0A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Tape Reel
                            TapeReelWheel(
                                rotationDegrees = if (isRecording) reelRotation else 0f,
                                isSpinning = isRecording
                            )

                            // Center LED Counter & Magnetic Head
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF332211), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    val seconds = (recordingDurationMs / 1000) % 60
                                    val minutes = (recordingDurationMs / 1000) / 60
                                    Text(
                                        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRecording) Color(0xFFFF4444) else Color(0xFF886633)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isRecording) Color(0xFFFF2222) else Color(0xFF442222))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRecording) "REC" else "STANDBY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isRecording) Color(0xFFFF6666) else Color(0xFF887766),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Right Tape Reel
                            TapeReelWheel(
                                rotationDegrees = if (isRecording) reelRotation else 0f,
                                isSpinning = isRecording
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vintage VU Meter & Dynamic Sound Waveform
                        AnalogVuMeter(amplitude = animatedAmplitude, isRecording = isRecording)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!hasMicPermission) {
                    // Permission Explanation Prompt
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2016)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Microphone Access Required",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFE2C48D),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Microphone access is strictly used to record on-device oral history interviews and field audio notes. Audio never leaves your phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC7B7A3)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB89047))
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Grant Microphone Access", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Metadata Inputs
                    OutlinedTextField(
                        value = memoTitle,
                        onValueChange = { memoTitle = it },
                        label = { Text("Interview / Memo Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2C48D),
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFF6A543A),
                            focusedLabelColor = Color(0xFFD4AF37),
                            unfocusedLabelColor = Color(0xFF9E8D77)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = informantName,
                            onValueChange = { informantName = it },
                            label = { Text("Informant / Speaker") },
                            placeholder = { Text("e.g. Local Historian") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE2C48D),
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color(0xFF6A543A),
                                focusedLabelColor = Color(0xFFD4AF37),
                                unfocusedLabelColor = Color(0xFF9E8D77)
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fieldNotes,
                        onValueChange = { fieldNotes = it },
                        label = { Text("Field Notes & Eyewitness Context") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2C48D),
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color(0xFF6A543A),
                            focusedLabelColor = Color(0xFFD4AF37),
                            unfocusedLabelColor = Color(0xFF9E8D77)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tactical Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = onStartRecording,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC2222))
                            ) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("START RECORDING", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onStopAndSave(memoTitle, informantName, fieldNotes)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE MEMO", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    onCancel()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(0.7f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB89047))
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CANCEL")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws an analog cassette wheel with rotating spokes.
 */
@Composable
private fun TapeReelWheel(
    rotationDegrees: Float,
    isSpinning: Boolean
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .border(2.dp, Color(0xFF6A543A), CircleShape)
            .background(Color(0xFF221A12), CircleShape)
            .rotate(rotationDegrees),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Center hub
            drawCircle(
                color = Color(0xFFB89047),
                radius = radius * 0.35f,
                center = center
            )
            drawCircle(
                color = Color.Black,
                radius = radius * 0.15f,
                center = center
            )

            // 3 Triangular tape reel windows / spokes
            for (i in 0 until 3) {
                val angle = Math.toRadians((i * 120.0))
                val spokeEnd = Offset(
                    (center.x + cos(angle) * radius * 0.85f).toFloat(),
                    (center.y + sin(angle) * radius * 0.85f).toFloat()
                )
                drawLine(
                    color = Color(0xFFD4AF37),
                    start = center,
                    end = spokeEnd,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

/**
 * Vintage analog illuminated VU meter with animated needle.
 */
@Composable
private fun AnalogVuMeter(
    amplitude: Float,
    isRecording: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2A2218))
            .border(1.dp, Color(0xFF5A442E), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
            val width = size.width
            val height = size.height

            // Background scale arc
            val pivot = Offset(width / 2f, height + 8.dp.toPx())
            val needleLength = height * 1.1f

            // Draw calibrated tick marks
            for (i in 0..10) {
                val fraction = i / 10f
                val angleDeg = -50f + (fraction * 100f)
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val tickStart = Offset(
                    (pivot.x + sin(angleRad) * (needleLength - 8.dp.toPx())).toFloat(),
                    (pivot.y - cos(angleRad) * (needleLength - 8.dp.toPx())).toFloat()
                )
                val tickEnd = Offset(
                    (pivot.x + sin(angleRad) * needleLength).toFloat(),
                    (pivot.y - cos(angleRad) * needleLength).toFloat()
                )

                val tickColor = if (i >= 8) Color(0xFFFF4444) else Color(0xFFD4AF37)
                drawLine(
                    color = tickColor,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Draw needle
            val targetAngleDeg = -50f + (amplitude.coerceIn(0f, 1f) * 100f)
            val needleRad = Math.toRadians(targetAngleDeg.toDouble())
            val needleTip = Offset(
                (pivot.x + sin(needleRad) * needleLength).toFloat(),
                (pivot.y - cos(needleRad) * needleLength).toFloat()
            )

            drawLine(
                color = if (isRecording) Color(0xFFFF3333) else Color(0xFF887766),
                start = pivot,
                end = needleTip,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // VU Label
        Text(
            text = "VU dB",
            fontSize = 9.sp,
            color = Color(0xFFB89047),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
