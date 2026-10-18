package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface

/**
 * Dedicated Expedition Survey Radius Dialog.
 * Keeps the proximity adjustment away from the primary directory scroll area,
 * eliminating accidental touches on large screens like the Galaxy S25 Ultra.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpeditionRangeDialog(
    currentRadiusKm: Float,
    onRadiusApplied: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempRadius by remember(currentRadiusKm) { mutableFloatStateOf(currentRadiusKm) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                .testTag("expedition_range_dialog"),
            color = SlateNightElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .background(LanternAmber.copy(alpha = 0.2f))
                                .border(1.dp, LanternAmberBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EXPEDITION RANGE SURVEY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = LanternAmberBright
                            )
                            Text(
                                text = "Search Radius",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ParchmentPaper
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Filter historical sites by driving or walking distance from your current position in Southern Indiana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Radius Display Card
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (tempRadius <= 0f) "State-Wide Exploration" else "Within ${tempRadius.toInt()} km",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (tempRadius <= 0f) ParchmentPaper else LanternAmberBright
                            )
                            Text(
                                text = if (tempRadius <= 0f) {
                                    "Showing all sites across Indiana"
                                } else {
                                    "Approx. ${(tempRadius * 0.621371f).toInt()} miles radius"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = ParchmentMuted
                            )
                        }

                        Surface(
                            color = if (tempRadius <= 0f) SlateBorder.copy(alpha = 0.5f) else LanternAmber,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (tempRadius <= 0f) "ALL" else "${tempRadius.toInt()} KM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (tempRadius <= 0f) ParchmentPaper else Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Preset Chips
                Text(
                    text = "HISTORICAL SURVEY PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ParchmentMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        0f to "All Indiana",
                        5f to "5 km (Walking)",
                        15f to "15 km (Local)",
                        30f to "30 km (County)",
                        50f to "50 km (Regional)"
                    )

                    presets.forEach { (km, label) ->
                        val isSelected = (km == 0f && tempRadius <= 0f) || (km > 0f && tempRadius == km)
                        FilterChip(
                            selected = isSelected,
                            onClick = { tempRadius = km },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (km == 0f) KnobstoneForest else LanternAmber,
                                selectedLabelColor = if (km == 0f) Color.White else Color.Black,
                                containerColor = SlateNightSurface,
                                labelColor = ParchmentPaper
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = SlateBorder,
                                selectedBorderColor = LanternAmberBright
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive Slider
                Text(
                    text = "CUSTOM DISTANCE (SLIDER)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ParchmentMuted
                )

                Slider(
                    value = tempRadius,
                    onValueChange = { tempRadius = it },
                    valueRange = 0f..50f,
                    steps = 9, // 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                    colors = SliderDefaults.colors(
                        thumbColor = LanternAmberBright,
                        activeTrackColor = LanternAmber,
                        inactiveTrackColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_proximity_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0 km (All)", style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
                    Text(text = "25 km", style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
                    Text(text = "50 km", style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempRadius = 0f
                            onRadiusApplied(0f)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        border = BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = ParchmentPaper,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset All",
                            color = ParchmentPaper,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            onRadiusApplied(tempRadius)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("apply_range_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LanternAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Apply Range",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}
