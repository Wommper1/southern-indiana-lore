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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.ConfidenceLevel
import com.example.data.model.HistoricalBuildingFootprint
import com.example.data.model.HistoricalChangeEvent
import com.example.data.model.MapAlignmentConfidence
import com.example.data.model.SanbornMicroAreaRepository
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated

/**
 * Street-Level "What Stood Here?" Modal Dialog:
 * Shows authentic Sanborn footprint evidence, construction materials, stories, and change lineage.
 * Includes "SANBORN PROVENANCE" button to inspect raw archive metadata, GCPs, and RMS error.
 */
@Composable
fun WhatStoodHereDialog(
    footprint: HistoricalBuildingFootprint,
    changeEvent: HistoricalChangeEvent?,
    onDismiss: () -> Unit
) {
    var showProvenanceModal by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SlateNight,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("what_stood_here_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(footprint.material.colorHex).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(footprint.material.colorHex),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WHAT STOOD HERE? (${footprint.sourceYear})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LanternAmberBright
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ParchmentMuted)
                    }
                }

                // Building Title & Map Label
                Text(
                    text = footprint.originalMapLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ParchmentPaper
                )

                Text(
                    text = footprint.modernAddressOrLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted
                )

                // Construction & Story Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(footprint.material.colorHex).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(footprint.material.colorHex))
                    ) {
                        Text(
                            text = footprint.material.description,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(footprint.material.colorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = SlateNightElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text(
                            text = "${footprint.storiesNotation} Stories",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = ParchmentPaper,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Strict Location Evidence Language
                val accuracyStatement = when (footprint.geometryConfidence) {
                    MapAlignmentConfidence.SURVEY_GRADE, MapAlignmentConfidence.HIGH ->
                        "This building stood here. (High geodetic alignment, RMS < 4m)"
                    MapAlignmentConfidence.GOOD ->
                        "This building stood approximately here. (Good alignment, 5-10m variance)"
                    MapAlignmentConfidence.APPROXIMATE ->
                        "This structure was located somewhere near this area. (Approximate alignment)"
                    else ->
                        "Map symbol interpretation uncertain."
                }

                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = KnobstoneForest,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = accuracyStatement,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = KnobstoneForest
                        )
                    }
                }

                // Historical Use & Map Interpretation
                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SANBORN MAP EVIDENCE (${footprint.sourceYear}):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmber
                        )
                        Text(
                            text = "Primary Use: ${footprint.primaryUse}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Text(
                            text = footprint.interpretationNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }

                // Then -> Then -> Now lineage if available
                if (changeEvent != null) {
                    Surface(
                        color = Color(0x22F59E0B),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "THEN → THEN → NOW EVOLUTION:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmberBright
                            )
                            Text(
                                text = changeEvent.whatStoodBefore,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = ParchmentPaper
                            )
                            Text(
                                text = changeEvent.whatStoodAfter,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = RiverMistBlue
                            )
                            Text(
                                text = "TODAY: ${changeEvent.whatExistsToday}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = KnobstoneForest
                            )
                        }
                    }
                }

                // Action Buttons: Sanborn Provenance ("Prove It") & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showProvenanceModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateNightElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = LanternAmberBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sanborn Provenance", color = LanternAmberBright, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = LanternAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = Color(0xFF1E1100), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Provenance / "Prove It" Details Dialog
    if (showProvenanceModal) {
        val sheetRecord = SanbornMicroAreaRepository.sheets.firstOrNull {
            it.editionYear == footprint.sourceYear && it.sheetNumber == footprint.sourceSheetNumber
        }

        Dialog(onDismissRequest = { showProvenanceModal = false }) {
            Surface(
                color = SlateNight,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SANBORN PROVENANCE AUDIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmberBright
                        )
                        IconButton(onClick = { showProvenanceModal = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = ParchmentMuted)
                        }
                    }

                    if (sheetRecord != null) {
                        Text(
                            text = "Source: ${sheetRecord.sourceInstitution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                        Text(
                            text = "Item: ${sheetRecord.archiveIdentifier}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ParchmentMuted
                        )
                        Text(
                            text = "Sheet: #${sheetRecord.sheetNumber} (${sheetRecord.sheetTitle})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Text(
                            text = "Original Scale: ${sheetRecord.originalScale}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ParchmentPaper
                        )
                        Text(
                            text = "Source SHA-256: ${sheetRecord.sourceHashSha256.take(24)}…",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = RiverMistBlue
                        )
                        Text(
                            text = "Transformation: ${sheetRecord.transformationType} • RMS: ${sheetRecord.rmsErrorMeters}m",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = KnobstoneForest
                        )
                        Text(
                            text = "Max Residual: ${sheetRecord.maxResidualMeters}m • Median: ${sheetRecord.medianResidualMeters}m",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = ParchmentMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GROUND CONTROL POINTS (${sheetRecord.gcpList.size} TIED):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmber
                        )
                        sheetRecord.gcpList.take(3).forEach { gcp ->
                            Surface(
                                color = SlateNightElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = "${gcp.id}: ${gcp.featureDescription}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = ParchmentPaper
                                    )
                                    Text(
                                        text = "Pixel (${gcp.pixelX.toInt()}, ${gcp.pixelY.toInt()}) -> Lat ${String.format("%.5f", gcp.latitude)}, Lon ${String.format("%.5f", gcp.longitude)} (Residual: ${gcp.residualErrorMeters}m)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                        color = ParchmentMuted
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showProvenanceModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = LanternAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss Audit", color = Color(0xFF1E1100), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
