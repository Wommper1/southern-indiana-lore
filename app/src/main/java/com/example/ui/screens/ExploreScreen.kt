package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.util.Calendar
import com.example.data.model.LoreCategories
import com.example.data.model.LoreCounties
import com.example.data.model.LoreEras
import com.example.data.model.LoreSite
import com.example.ui.components.BrassRotaryDial
import com.example.ui.components.ExpeditionRangeDialog
import com.example.ui.components.LoreCard
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface

@Composable
fun ExploreScreen(
    sites: List<LoreSite>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedCounty: String = LoreCounties.ALL,
    onCountySelected: (String) -> Unit = {},
    selectedEra: String = LoreEras.ALL,
    onEraSelected: (String) -> Unit = {},
    proximityRadiusKm: Float = 0f,
    onProximityRadiusKmChanged: (Float) -> Unit = {},
    filterFavoritesOnly: Boolean,
    onToggleFavoritesFilter: () -> Unit,
    filterVisitedOnly: Boolean,
    onToggleVisitedFilter: () -> Unit,
    currentNarratingSiteId: String?,
    onCardClick: (LoreSite) -> Unit,
    onToggleFavorite: (LoreSite) -> Unit,
    onToggleVisited: (LoreSite) -> Unit,
    onNarrateClick: (LoreSite) -> Unit,
    onOpenAddCustomDialog: (() -> Unit)? = null,
    onOpenArchivist: (() -> Unit)? = null,
    onOpenVoiceSettings: (() -> Unit)? = null,
    onOpenLibrary: (() -> Unit)? = null,
    onOpenFieldJournal: (() -> Unit)? = null,
    onOpenRelicLab: (() -> Unit)? = null,
    isLiveGpsActive: Boolean = false,
    hardwareHeading: Float = 0f,
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNaturallyNight = currentHour >= 19 || currentHour < 6
    var isNightModeForced by remember { mutableStateOf(false) }
    val isNightModeActive = isNaturallyNight || isNightModeForced
    var showRangeDialog by remember { mutableStateOf(false) }
    var showDetailedFilters by remember { mutableStateOf(false) }

    if (showRangeDialog) {
        ExpeditionRangeDialog(
            currentRadiusKm = proximityRadiusKm,
            onRadiusApplied = onProximityRadiusKmChanged,
            onDismiss = { showRangeDialog = false }
        )
    }

    val displayedSites = remember(sites, isNightModeActive) {
        if (isNightModeActive) {
            // Prioritize Haunted, Curiosities, and Nocturnal Folklore first
            sites.sortedByDescending { site ->
                site.category == LoreCategories.HAUNTED ||
                site.folkloreTradition.contains("ghost", ignoreCase = true) ||
                site.folkloreTradition.contains("haunt", ignoreCase = true) ||
                site.folkloreTradition.contains("specter", ignoreCase = true) ||
                site.fullStory.contains("ghost", ignoreCase = true) ||
                site.fullStory.contains("night", ignoreCase = true)
            }
        } else {
            sites
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen")
    ) {
        // Discovery Stream with smooth scrolling filters and expansive lore cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Item 0: Compact Integrated Search Bar
            item(key = "search_bar_item") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    placeholder = {
                        Text(
                            "Search sites, legends, Wyandotte...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = LanternAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    soundEngine?.playTactileClick()
                                    onSearchQueryChange("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = ParchmentMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // Filter Expander Toggle inside search bar
                            IconButton(onClick = {
                                soundEngine?.playTactileClick()
                                showDetailedFilters = !showDetailedFilters
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filters",
                                    tint = if (showDetailedFilters || selectedCounty != LoreCounties.ALL || selectedEra != LoreEras.ALL) LanternAmberBright else ParchmentMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LanternAmberBright,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        cursorColor = LanternAmber,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            // Item 1: Primary Category Pills (Single smooth horizontal rail)
            item(key = "category_pills_rail") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoreCategories.list.forEach { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                soundEngine?.playTactileClick()
                                onCategorySelected(category)
                            },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color(0xFF1E1100),
                                containerColor = Color(0xFF1E293B),
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
            }

            // Item 2: Quick Status Badges: Range, Saved, Visited & Expedition Actions
            item(key = "quick_status_badges") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick state toggles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isRangeActive = proximityRadiusKm > 0f
                        Surface(
                            onClick = {
                                soundEngine?.playTactileClick()
                                showRangeDialog = true
                            },
                            color = if (isRangeActive) LanternAmber.copy(alpha = 0.2f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isRangeActive) LanternAmberBright else SlateBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = if (isRangeActive) LanternAmberBright else ParchmentMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (!isRangeActive) "All Distances" else "${proximityRadiusKm.toInt()}km",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isRangeActive) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isRangeActive) LanternAmberBright else ParchmentPaper,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Saved Filter Pill
                        Surface(
                            onClick = {
                                soundEngine?.playGoldCoinClink()
                                onToggleFavoritesFilter()
                            },
                            color = if (filterFavoritesOnly) GhostCrimson.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (filterFavoritesOnly) GhostCrimson else SlateBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (filterFavoritesOnly) GhostCrimson else ParchmentMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (filterFavoritesOnly) Color.White else ParchmentPaper,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Visited Filter Pill
                        Surface(
                            onClick = {
                                soundEngine?.playDiscoveryChime()
                                onToggleVisitedFilter()
                            },
                            color = if (filterVisitedOnly) KnobstoneForest.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (filterVisitedOnly) KnobstoneForest else SlateBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (filterVisitedOnly) KnobstoneForest else ParchmentMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Visited",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (filterVisitedOnly) Color.White else ParchmentPaper,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Action buttons: Compact Archivist & Log Field
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onOpenArchivist != null) {
                            Surface(
                                onClick = {
                                    soundEngine?.playTactileClick()
                                    onOpenArchivist()
                                },
                                color = LanternAmber.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "AI",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LanternAmberBright,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (onOpenAddCustomDialog != null) {
                            Surface(
                                onClick = {
                                    soundEngine?.playLeatherUnclasp()
                                    onOpenAddCustomDialog()
                                },
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AddLocationAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+ Log",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable County & Era Deep Exploration Tray
            item(key = "detailed_filters_tray") {
                AnimatedVisibility(
                    visible = showDetailedFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "COUNTY BOUNDARIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = KnobstoneForest,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(LoreCounties.list) { county ->
                                val isCountySelected = county == selectedCounty
                                FilterChip(
                                    selected = isCountySelected,
                                    onClick = { onCountySelected(county) },
                                    label = {
                                        Text(
                                            text = if (county == LoreCounties.ALL) "All Counties" else "$county Co.",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isCountySelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KnobstoneForest,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF0F172A),
                                        labelColor = ParchmentPaper
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val eraOptions = LoreEras.list
                        val currentEraIdx = eraOptions.indexOf(selectedEra).coerceAtLeast(0)
                        BrassRotaryDial(
                            options = eraOptions,
                            selectedIndex = currentEraIdx,
                            onIndexSelected = { idx ->
                                onEraSelected(eraOptions[idx])
                            },
                            title = "Era Rotary Selector",
                            soundEngine = soundEngine
                        )
                    }
                }
            }

            // Active Filter Summary Feedback
            val hasActiveFilters = searchQuery.isNotBlank() ||
                    selectedCategory != LoreCategories.ALL ||
                    selectedCounty != LoreCounties.ALL ||
                    selectedEra != LoreEras.ALL ||
                    proximityRadiusKm > 0f ||
                    filterFavoritesOnly ||
                    filterVisitedOnly ||
                    isNightModeForced

            if (hasActiveFilters) {
                item(key = "active_filter_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${displayedSites.size} sites found",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LanternAmberBright
                        )
                        TextButton(
                            onClick = {
                                soundEngine?.playTactileClick()
                                onSearchQueryChange("")
                                onCategorySelected(LoreCategories.ALL)
                                onCountySelected(LoreCounties.ALL)
                                onEraSelected(LoreEras.ALL)
                                onProximityRadiusKmChanged(0f)
                                if (filterFavoritesOnly) onToggleFavoritesFilter()
                                if (filterVisitedOnly) onToggleVisitedFilter()
                                isNightModeForced = false
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = LanternAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset filters",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmber
                            )
                        }
                    }
                }
            }

            // Relic Examination Lab Standalone Station Banner
            if (onOpenRelicLab != null) {
                item(key = "relic_lab_discovery_card") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                soundEngine?.playWaxStampImpact()
                                onOpenRelicLab()
                            }
                            .testTag("explore_relic_lab_banner"),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1E140C)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF382312),
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GildedGoldBright)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = LanternAmberBright,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FIELD RELIC EXAMINATION LAB",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = GildedGoldBright
                                    )
                                    Text(
                                        text = "Tactile charcoal rubbing canvas & archival tape broadcasts",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ParchmentPaper
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Sites Discovery Stream
            if (displayedSites.isEmpty()) {
                item(key = "empty_state_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = ParchmentMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No lore sites match your criteria",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Clear filters or expand your proximity range to discover more",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentMuted
                            )
                        }
                    }
                }
            } else {
                if (onOpenLibrary != null && searchQuery.isBlank()) {
                    item(key = "athenaeum_library_banner") {
                        Surface(
                            onClick = onOpenLibrary,
                            color = Color(0xFF1E140C),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag("explore_library_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2B1C11))
                                        .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFFF3C853),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "SOUTHERN INDIANA ARCHIVES",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color(0xFFF3C853)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF4A151B),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "LIBRARY & PASSPORT",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp),
                                                color = Color(0xFFFDE68A),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Leather-bound Ohio Valley volumes, witness lore archives, and trail passport stamps.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }

                if (onOpenFieldJournal != null && searchQuery.isBlank()) {
                    item(key = "drake_field_journal_banner") {
                        Surface(
                            onClick = {
                                soundEngine?.playParchmentRustle()
                                onOpenFieldJournal()
                            },
                            color = Color(0xFF1B1713),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .testTag("explore_journal_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2A2016))
                                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "EXPEDITION FIELD JOURNAL",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color(0xFFFBBF24)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF78350F),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "CIPHERS & SKETCHES",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 8.sp),
                                                color = Color(0xFFFEF3C7),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Architectural pencil sketches, Reno Gold cipher wheels, and optical viewfinder alignment targets.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }

                items(displayedSites, key = { it.id }) { site ->
                    LoreCard(
                        site = site,
                        isCurrentlyNarrating = currentNarratingSiteId == site.id,
                        onCardClick = {
                            soundEngine?.playParchmentRustle()
                            onCardClick(site)
                        },
                        onToggleFavorite = {
                            soundEngine?.playGoldCoinClink()
                            onToggleFavorite(site)
                        },
                        onToggleVisited = {
                            soundEngine?.playWaxSealBreak()
                            onToggleVisited(site)
                        },
                        onNarrateClick = {
                            soundEngine?.playPhonographNeedleDrop()
                            onNarrateClick(site)
                        }
                    )
                }
            }
        }
    }
}
