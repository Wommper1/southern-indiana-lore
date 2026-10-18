package com.example.ui.components

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoreVoiceMemoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OralHistoryMemoItemCard(
    memo: LoreVoiceMemoEntity,
    isPlaying: Boolean,
    playbackProgressMs: Long,
    playbackDurationMs: Long,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US).format(Date(memo.timestamp))

    val effectiveDuration = if (memo.durationMs > 0) memo.durationMs else playbackDurationMs
    val effectiveDurationSec = (effectiveDuration / 1000) % 60
    val effectiveDurationMin = (effectiveDuration / 1000) / 60
    val durationFormatted = String.format(Locale.US, "%d:%02d", effectiveDurationMin, effectiveDurationSec)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isPlaying) Color(0xFFD4AF37) else Color(0xFF4A3828), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isPlaying) Color(0xFF261D15) else Color(0xFF1B1612)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF332418))
                            .border(1.dp, Color(0xFFB89047), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isPlaying) Color(0xFFFFCC44) else Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = memo.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFF3E5AB),
                            fontWeight = FontWeight.Bold
                        )
                        if (memo.informantName.isNotBlank()) {
                            Text(
                                text = "Informant: ${memo.informantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC7B7A3)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Audio",
                            tint = Color(0xFFB89047),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memo",
                            tint = Color(0xFFAA4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (memo.fieldNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = memo.fieldNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E8D77),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = if (isPlaying) onPause else onPlay,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isPlaying) Color(0xFFB89047) else Color(0xFF5A442E),
                        contentColor = if (isPlaying) Color.Black else Color(0xFFF3E5AB)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isPlaying && playbackDurationMs > 0) {
                        Slider(
                            value = (playbackProgressMs.toFloat() / playbackDurationMs.toFloat()).coerceIn(0f, 1f),
                            onValueChange = { fraction ->
                                onSeek((fraction * playbackDurationMs).toLong())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD4AF37),
                                activeTrackColor = Color(0xFFB89047),
                                inactiveTrackColor = Color(0xFF3A2B1D)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFB89047),
                            trackColor = Color(0xFF3A2B1D)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentMs = if (isPlaying) playbackProgressMs else 0L
                        val curSec = (currentMs / 1000) % 60
                        val curMin = (currentMs / 1000) / 60
                        Text(
                            text = String.format(Locale.US, "%d:%02d / %s", curMin, curSec, durationFormatted),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFD4AF37)
                        )

                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7A6A58)
                        )
                    }
                }
            }
        }
    }
}
