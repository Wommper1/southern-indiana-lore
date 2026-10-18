package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoreferenceStatus
import com.example.data.model.HistoricalLandscapeFeature
import com.example.data.model.HistoricalMapLayer
import com.example.data.model.LoreSite
import com.example.data.model.MapAlignmentConfidence
import com.example.data.model.MapType
import com.example.data.model.SanbornLegendGuide
import com.example.data.model.SanbornMicroAreaRepository
import com.example.data.model.SouthernIndianaLandscapeTimeline
import com.example.data.model.SouthernIndianaMapArchive
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
 * Historical Map Time Machine:
 * Allows user to peel back layers of time at their exact GPS location in Southern Indiana.
 * Synchronizes modern street position with authentic USGS, Sanborn, and Aerial surveys.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeMachineSheet(
    userLat: Double,
    userLng: Double,
    sites: List<LoreSite>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    onSiteSelected: (LoreSite) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val layers = remember { SouthernIndianaMapArchive.layers }
    val years = remember { layers.map { it.yearStart }.distinct().sorted() }

    // Match layer closest to selected year
    val activeLayer = layers.minByOrNull { kotlin.math.abs(it.yearStart - selectedYear) } ?: layers.first()
    val isWithinCoverage = activeLayer.contains(userLat, userLng)

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = TIMELINE & OPACITY, 1 = WHAT CHANGED HERE, 2 = MAP ARCHIVE PROVENANCE
    var selectedFeature by remember { mutableStateOf<HistoricalLandscapeFeature?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateNight,
        dragHandle = null,
        modifier = modifier.testTag("time_machine_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
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
                            .border(1.dp, LanternAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HISTORICAL TIME MACHINE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Text(
                            text = "Peel back Southern Indiana landscapes • Year ${activeLayer.displayYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Time Machine",
                        tint = ParchmentMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateNightElevated,
                contentColor = LanternAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LanternAmber
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "TIMELINE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) LanternAmber else ParchmentMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "WHAT CHANGED HERE?",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) LanternAmber else ParchmentMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "MAP PROVENANCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 2) LanternAmber else ParchmentMuted
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Timeline & Opacity Swipe Control
                    TimelineControlsView(
                        activeLayer = activeLayer,
                        isWithinCoverage = isWithinCoverage,
                        years = years,
                        selectedYear = selectedYear,
                        onYearSelected = onYearSelected,
                        opacity = opacity,
                        onOpacityChanged = onOpacityChanged
                    )
                }
                1 -> {
                    // What Changed Here Engine
                    WhatChangedHereView(
                        userLat = userLat,
                        userLng = userLng,
                        selectedYear = selectedYear,
                        sites = sites,
                        onFeatureClick = { selectedFeature = it },
                        onSiteSelected = onSiteSelected
                    )
                }
                2 -> {
                    // Map Provenance & Alignment Confidence
                    MapProvenanceView(activeLayer = activeLayer)
                }
            }
        }
    }

    // Detail dialog for landscape feature
    if (selectedFeature != null) {
        LandscapeFeatureDetailDialog(
            feature = selectedFeature!!,
            onDismiss = { selectedFeature = null }
        )
    }
}

@Composable
private fun TimelineControlsView(
    activeLayer: HistoricalMapLayer,
    isWithinCoverage: Boolean,
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    opacity: Float,
    onOpacityChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Active Map Banner
        Surface(
            color = SlateNightElevated,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeLayer.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Text(
                            text = "${activeLayer.sourceInstitution} • Edition ${activeLayer.displayYear}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ParchmentMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MapAlignmentBadge(confidence = activeLayer.alignmentConfidence)
                        GeoreferenceStatusBadge(status = activeLayer.georeferenceStatus)
                    }
                }

                if (!isWithinCoverage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0x33EF4444),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x88EF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "HISTORICAL MAP COVERAGE ENDS HERE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = Color(0xFFF87171)
                                )
                                Text(
                                    text = "Your GPS position is outside the surveyed bounds (${activeLayer.coverageDescription}). Imagery is not artificially stretched or repeated.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = ParchmentPaper
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stepped Year Selector Buttons
        Column {
            Text(
                text = "AUTHENTIC SURVEY YEAR (TAP TO SNAP):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ParchmentMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                years.forEach { yr ->
                    val isSelected = (yr == activeLayer.yearStart)
                    Surface(
                        color = if (isSelected) LanternAmber else SlateNightElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) LanternAmberBright else SlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onYearSelected(yr) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = yr.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color(0xFF1E1100) else ParchmentPaper
                            )
                        }
                    }
                }
            }
        }

        // Opacity Slider (Then <-> Now comparison)
        Surface(
            color = SlateNightElevated,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HISTORICAL MAP OVERLAY OPACITY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentMuted
                    )
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmberBright
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = LanternAmberBright,
                        activeTrackColor = LanternAmber,
                        inactiveTrackColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0% (Modern Satellite/Streets)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = ParchmentMuted
                    )
                    Text(
                        text = "100% (${activeLayer.displayYear} Survey)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = ParchmentMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatChangedHereView(
    userLat: Double,
    userLng: Double,
    selectedYear: Int,
    sites: List<LoreSite>,
    onFeatureClick: (HistoricalLandscapeFeature) -> Unit,
    onSiteSelected: (LoreSite) -> Unit
) {
    val features = remember { SouthernIndianaLandscapeTimeline.features }
    val changeEvents = remember { SanbornMicroAreaRepository.changeEvents }
    val lostStreets = remember { SanbornMicroAreaRepository.lostStreets }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "DOCUMENTED STREET-LEVEL CHANGE EVENTS (SANBORN & DEEDS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = LanternAmberBright
            )
        }

        // Real Historical Change Events
        items(changeEvents) { chg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNightElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chg.locationDescription,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Surface(
                            color = Color(0x33F59E0B),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${chg.startYear} → ${chg.endYear}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmberBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "BEFORE: ${chg.whatStoodBefore}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = RiverMistBlue
                    )
                    Text(
                        text = "AFTER: ${chg.whatStoodAfter}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentPaper
                    )
                    Text(
                        text = "TODAY: ${chg.whatExistsToday}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = KnobstoneForest
                    )
                    Text(
                        text = "Sources: ${chg.sourceBefore} • ${chg.sourceAfter}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = ParchmentMuted
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "HISTORICAL STREET LINEAGE (RENAMED / FLOODWALL VACATED)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = LanternAmberBright
            )
        }

        // Lost Streets
        items(lostStreets) { st ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNightElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Historic Name: ${st.formerHistoricName}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmber
                    )
                    Text(
                        text = "Current Name: ${st.currentStreetName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentPaper
                    )
                    Text(
                        text = "Status: ${st.status} • Source: ${st.primarySource}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = ParchmentMuted
                    )
                    Text(
                        text = st.historicalSignificance,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentPaper
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SURROUNDING REGIONAL LANDSCAPE TRANSFORMATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ParchmentMuted
            )
        }

        items(features) { feat ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNightElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFeatureClick(feat) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = feat.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Surface(
                            color = LanternAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = feat.displayEra,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmberBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAST: ${feat.whatStoodHere}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = RiverMistBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TODAY: ${feat.whatExistsToday}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MapProvenanceView(activeLayer: HistoricalMapLayer) {
    Column(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = SlateNightElevated,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ARCHIVAL PROVENANCE & RIGHTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LanternAmberBright
                )

                Text(
                    text = "Source Institution: ${activeLayer.sourceInstitution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentPaper
                )
                Text(
                    text = "Catalog Identifier: ${activeLayer.sourceIdentifier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted
                )
                if (activeLayer.sheetCount != null) {
                    Text(
                        text = "Sheet Count: ${activeLayer.sheetCount} original sheets",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentPaper
                    )
                }
                if (activeLayer.locDigitalId != null) {
                    Text(
                        text = "Digital ID: ${activeLayer.locDigitalId}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                }
                if (activeLayer.iiifManifestUrl != null) {
                    Text(
                        text = "IIIF Manifest: ${activeLayer.iiifManifestUrl}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = RiverMistBlue
                    )
                }
                Text(
                    text = "Rights Status: ${activeLayer.rightsStatus}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = KnobstoneForest
                )
                Text(
                    text = "Citation: ${activeLayer.citation}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ParchmentPaper
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Georeference Status:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentPaper
                    )
                    GeoreferenceStatusBadge(status = activeLayer.georeferenceStatus)
                }
                Text(
                    text = "Georeferencing Details: ${activeLayer.georeferencingMethod}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ParchmentMuted
                )
                Text(
                    text = "Curator Notes: ${activeLayer.curatorialNotes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ParchmentPaper
                )

                if (activeLayer.mapType == MapType.SANBORN_FIRE_INSURANCE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SANBORN SYMBOL & MATERIAL INTERPRETATION GUIDE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = LanternAmberBright
                    )
                    Text(
                        text = "Authentic 19th/20th-century fire insurance cartographic convention:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                    SanbornLegendGuide.materials.forEach { mat ->
                        Surface(
                            color = SlateNightElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(mat.colorHex).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(mat.colorHex),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.size(16.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${mat.materialName} (${mat.description})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = ParchmentPaper
                                    )
                                    Text(
                                        text = mat.significance,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = ParchmentMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "MICRO-AREA GEOREFERENCING AUDIT (GCP CONTROL):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = LanternAmberBright
                    )
                    SanbornMicroAreaRepository.sheets.filter { it.editionYear == activeLayer.yearStart }.forEach { sh ->
                        Surface(
                            color = Color(0x2210B981),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KnobstoneForest.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Sheet #${sh.sheetNumber}: ${sh.sheetTitle}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                                Text(
                                    text = "Scale: ${sh.originalScale} • Transform: ${sh.transformationType} • RMS: ${sh.rmsErrorMeters}m (Max Residual: ${sh.maxResidualMeters}m)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = KnobstoneForest
                                )
                                Text(
                                    text = "LOC Item: ${sh.archiveIdentifier} • SHA-256: ${sh.sourceHashSha256.take(16)}…",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                    color = ParchmentMuted
                                )
                            }
                        }
                    }
                }

                if (activeLayer.editionSeries != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "HISTORICAL EDITION CHAIN (LIBRARY OF CONGRESS):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = LanternAmberBright
                    )
                    SouthernIndianaMapArchive.newAlbanySanbornLineage.forEach { ed ->
                        val isThisYear = ed.year == activeLayer.yearStart
                        Surface(
                            color = if (isThisYear) Color(0x33F59E0B) else SlateNightElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isThisYear) LanternAmber else SlateBorder),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ed.year} Edition (${ed.month}) • ${ed.sheetCount} sheets",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isThisYear) LanternAmberBright else ParchmentPaper
                                    )
                                    Text(
                                        text = "${ed.locItemId} • ${ed.notes}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = ParchmentMuted
                                    )
                                }
                                if (isThisYear) {
                                    Surface(
                                        color = LanternAmber,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = Color(0xFF1E1100),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapAlignmentBadge(confidence: MapAlignmentConfidence, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (confidence) {
        MapAlignmentConfidence.SURVEY_GRADE -> Triple("SURVEY-GRADE (<10M)", Color(0x3310B981), Color(0xFF34D399))
        MapAlignmentConfidence.GOOD -> Triple("GOOD ALIGNMENT", Color(0x333B82F6), Color(0xFF60A5FA))
        MapAlignmentConfidence.APPROXIMATE -> Triple("APPROXIMATE (30-100M)", Color(0x33F59E0B), Color(0xFFFBBF24))
        MapAlignmentConfidence.LOW -> Triple("LOW ALIGNMENT", Color(0x33EF4444), Color(0xFFF87171))
        MapAlignmentConfidence.UNREFERENCED -> Triple("UNREFERENCED", Color(0x3364748B), Color(0xFF94A3B8))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GeoreferenceStatusBadge(status: GeoreferenceStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        GeoreferenceStatus.VERIFIED_GEOREFERENCED -> Triple("VERIFIED GEOREFERENCED", Color(0x3310B981), Color(0xFF34D399))
        GeoreferenceStatus.GEOREFERENCING_PLANNED -> Triple("GEOREFERENCING PLANNED", Color(0x33F59E0B), Color(0xFFFBBF24))
        GeoreferenceStatus.APPROXIMATE_GEOMETRY -> Triple("APPROXIMATE GEOMETRY", Color(0x333B82F6), Color(0xFF60A5FA))
        GeoreferenceStatus.UNVERIFIED -> Triple("UNVERIFIED", Color(0x33EF4444), Color(0xFFF87171))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun LandscapeFeatureDetailDialog(
    feature: HistoricalLandscapeFeature,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SlateNight,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHAT WAS HERE?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = LanternAmberBright
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ParchmentMuted)
                    }
                }

                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ParchmentPaper
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Active Era: ${feature.displayEra}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = RiverMistBlue
                    )
                    if (feature.isDemonstrationData) {
                        Surface(
                            color = Color(0x33EF4444),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "DEMO DATA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFFF87171),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0x3310B981),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "VERIFIED SOURCE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hist. Confidence: ${feature.historicalConfidence.name}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                    Text(
                        text = "Location: ${feature.locationConfidence.name}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = ParchmentMuted
                    )
                }

                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HISTORIC LANDSCAPE (${feature.displayEra}):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmber
                        )
                        Text(
                            text = feature.whatStoodHere,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }

                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "WHAT CHANGED:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GhostCrimson
                        )
                        Text(
                            text = feature.whatChanged,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }

                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "TODAY ON THIS GROUND:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KnobstoneForest
                        )
                        Text(
                            text = feature.whatExistsToday,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }

                Text(
                    text = "Primary Map Source: ${feature.primaryMapSource}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ParchmentMuted
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LanternAmber),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Map", color = Color(0xFF1E1100), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
