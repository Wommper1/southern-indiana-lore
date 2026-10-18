package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoreCategories
import com.example.data.model.LoreCounties
import com.example.data.model.LoreEras
import com.example.data.model.LoreSite
import com.example.ui.components.FoldedParchmentCard
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalLanternLighting
import com.example.util.AdventureSoundEngine
import java.util.Locale

/**
 * The Explorer's Field Desk (Worktable Hub).
 * Replaces generic scrolling card feeds with an overhead perspective of an 1880s
 * antiquarian surveyor's walnut workbench:
 * - Stacked manila dossiers with authentic fold physics and wax seals
 * - Folded Topographic Quadrangle map with quick unfold-to-radar mechanics
 * - Brass drawer pulls and quick shortcuts for Archivist, Add Site, and Relic Lab
 * - Secret Ultraviolet Cartography responsive to the Rotary Lantern Dimmer
 */
@Composable
fun ExplorerWorktableScreen(
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
    onOpenKarstHydrophone: (() -> Unit)? = null,
    onOpenRadar: (() -> Unit)? = null,
    isLiveGpsActive: Boolean = false,
    hardwareHeading: Float = 0f,
    soundEngine: AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    val lightingState = LocalLanternLighting.current
    val isUvActive = lightingState.isUvActive

    var showDrawerFilters by remember { mutableStateOf(false) }
    var isQuadrangleUnfolded by remember { mutableStateOf(false) }

    val deskSurfaceBg by animateColorAsState(
        targetValue = lightingState.deskSurfaceTint,
        animationSpec = tween(350),
        label = "desk_bg"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(deskSurfaceBg)
            .testTag("explorer_worktable_screen")
    ) {
        // Wooden planks & surveyor grid background texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle vertical wood plank joints
            val plankWidth = 140.dp.toPx()
            var x = plankWidth
            while (x < w) {
                drawLine(
                    color = Color(0x18000000),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.5.dp.toPx()
                )
                x += plankWidth
            }

            // Top surveyor ruler graduation line
            drawLine(
                color = if (isUvActive) Color(0x33A855F7) else Color(0x22C89B3C),
                start = Offset(0f, 2.dp.toPx()),
                end = Offset(w, 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section 1: Field Desk Title, Station Badges, and Quick Instrument Drawers
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "THE EXPLORER'S FIELD DESK",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.8.sp,
                                color = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFFFDE68A)
                            )
                            Text(
                                text = if (isUvActive) "✦ UV Cartography Mode Active • Secret Margins Exposed" else "Southern Indiana Historical Survey & Archives",
                                fontSize = 10.sp,
                                color = if (isUvActive) Color(0xFFC084FC) else Color(0xFFBCA685)
                            )
                        }

                        // Search Drawer Toggle
                        IconButton(
                            onClick = {
                                soundEngine?.playTactileClick()
                                showDrawerFilters = !showDrawerFilters
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF26170C))
                                .border(1.dp, Color(0xFFC89B3C), RoundedCornerShape(8.dp))
                                .testTag("btn_toggle_drawer_filters")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Toggle Drawer Filters",
                                tint = Color(0xFFF3C853),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Desk Pulls & Quick Stations Row (Relic Lab, Archivist, Add Site, Library)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Relic Lab Pull
                        if (onOpenRelicLab != null) {
                            DeskQuickPill(
                                icon = Icons.Default.Straighten,
                                label = "Relic Calipers",
                                onClick = {
                                    soundEngine?.playWaxStampImpact()
                                    onOpenRelicLab()
                                }
                            )
                        }

                        // Karst Hydrophone Probe Pull
                        if (onOpenKarstHydrophone != null) {
                            DeskQuickPill(
                                icon = Icons.Default.Sensors,
                                label = "Karst Hydrophone",
                                onClick = {
                                    soundEngine?.playBrassToggleSwitch()
                                    onOpenKarstHydrophone()
                                }
                            )
                        }

                        // Archivist Consultation
                        if (onOpenArchivist != null) {
                            DeskQuickPill(
                                icon = Icons.Default.AutoAwesome,
                                label = "Archivist Consultation",
                                onClick = {
                                    soundEngine?.playTactileClick()
                                    onOpenArchivist()
                                }
                            )
                        }

                        // Athenaeum Library
                        if (onOpenLibrary != null) {
                            DeskQuickPill(
                                icon = Icons.Default.LocalLibrary,
                                label = "Athenaeum Tomes",
                                onClick = {
                                    soundEngine?.playParchmentRustle()
                                    onOpenLibrary()
                                }
                            )
                        }

                        // Add Custom Site
                        if (onOpenAddCustomDialog != null) {
                            DeskQuickPill(
                                icon = Icons.Default.AddLocationAlt,
                                label = "Log Field Site",
                                onClick = {
                                    soundEngine?.playTactileClick()
                                    onOpenAddCustomDialog()
                                }
                            )
                        }
                    }
                }
            }

            // Section 2: Interactive Folded Topographic Quadrangle Map Segment
            item {
                Surface(
                    color = if (isUvActive) Color(0xFF1B132B) else Color(0xFF2A1C10),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.2.dp,
                        color = if (isUvActive) Color(0xFFC084FC) else Color(0xFFC89B3C)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folded_quadrangle_map_banner")
                        .clickable {
                            soundEngine?.playPaperUnfold()
                            if (onOpenRadar != null) {
                                onOpenRadar()
                            } else {
                                isQuadrangleUnfolded = !isQuadrangleUnfolded
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFFFDE68A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "CREASED LINEN QUADRANGLE MAP",
                                        fontFamily = CinzelFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        color = if (isUvActive) Color(0xFFFAF5FF) else Color(0xFFFDE68A)
                                    )
                                    Text(
                                        text = "Ohio River Karst & Escarpment Survey • ${sites.size} Documented Coordinates",
                                        fontSize = 10.sp,
                                        color = if (isUvActive) Color(0xFFD8B4FE) else Color(0xFFBCA685)
                                    )
                                }
                            }

                            // Unfold button pill
                            Surface(
                                color = if (isUvActive) Color(0xFF4C2A75) else Color(0xFF533516),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5B85C))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Unfold Radar",
                                        fontFamily = CinzelFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFF1C2)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFFFFF1C2),
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Folded Map Geometry & Crease Lines
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isUvActive) Color(0xFF24163B) else Color(0xFF382313))
                                .border(1.dp, if (isUvActive) Color(0xFF6B4895) else Color(0xFF6E4822), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // 4-segment folding creases (quadrangle folds)
                                drawLine(
                                    color = if (isUvActive) Color(0x66B388FF) else Color(0x55C89B3C),
                                    start = Offset(w / 2f, 0f),
                                    end = Offset(w / 2f, h),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawLine(
                                    color = if (isUvActive) Color(0x66B388FF) else Color(0x55C89B3C),
                                    start = Offset(0f, h / 2f),
                                    end = Offset(w, h / 2f),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                // Stylized topographical contour curves
                                for (i in 1..4) {
                                    val r = i * 22.dp.toPx()
                                    drawCircle(
                                        color = if (isUvActive) Color(0x22E879F9) else Color(0x22FDE68A),
                                        radius = r,
                                        center = Offset(w * 0.35f, h * 0.5f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isLiveGpsActive) "◉ LIVE SURVEY TRANSMITTER: ACTIVE" else "○ QUADRANGLE GRID: SECTION 31-S",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiveGpsActive) Color(0xFF34D399) else Color(0xFFE2D4BF)
                                )
                                Text(
                                    text = "TAP TO DEPLOY SEXTANT RADAR",
                                    fontFamily = CinzelFontFamily,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isUvActive) Color(0xFFF0ABFC) else Color(0xFFF3C853)
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Antiquarian Search & Drawer Filters Panel
            item {
                AnimatedVisibility(
                    visible = showDrawerFilters || searchQuery.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = Color(0xFF1E130A),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Search Text Input
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = {
                                    Text(
                                        "Search archives by site, town, or legend...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9E8B75)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFFF3C853),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchQueryChange("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = Color(0xFFF3C853),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF3C853),
                                    unfocusedBorderColor = Color(0xFF8B6428),
                                    focusedTextColor = Color(0xFFF5EEDC),
                                    unfocusedTextColor = Color(0xFFF5EEDC),
                                    focusedContainerColor = Color(0xFF160E07),
                                    unfocusedContainerColor = Color(0xFF160E07)
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("worktable_search_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Category Filter Chips
                            Text(
                                text = "HISTORICAL CATEGORY",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFFFDE68A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LoreCategories.list.forEach { category ->
                                    val isCatSelected = selectedCategory == category
                                    FilterChip(
                                        selected = isCatSelected,
                                        onClick = {
                                            soundEngine?.playTactileClick()
                                            onCategorySelected(category)
                                        },
                                        label = {
                                            Text(
                                                text = category,
                                                fontSize = 10.sp,
                                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFC89B3C),
                                            selectedLabelColor = Color(0xFF1A0E04),
                                            containerColor = Color(0xFF2A190C),
                                            labelColor = Color(0xFFD4C3A3)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isCatSelected,
                                            borderColor = if (isCatSelected) Color(0xFFFFF1C2) else Color(0xFF5A3916)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Favorites & Visited Quick Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = filterFavoritesOnly,
                                    onClick = {
                                        soundEngine?.playTactileClick()
                                        onToggleFavoritesFilter()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text("Starred Dossiers", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF991B1B),
                                        selectedLabelColor = Color(0xFFFFF1F2),
                                        containerColor = Color(0xFF2A190C),
                                        labelColor = Color(0xFFD4C3A3)
                                    )
                                )

                                FilterChip(
                                    selected = filterVisitedOnly,
                                    onClick = {
                                        soundEngine?.playWaxStampImpact()
                                        onToggleVisitedFilter()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text("Sealed / Visited", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF166534),
                                        selectedLabelColor = Color(0xFFF0FDF4),
                                        containerColor = Color(0xFF2A190C),
                                        labelColor = Color(0xFFD4C3A3)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Pinned Marginalia Sticky Note (Cartographer's Dispatch)
            item {
                Surface(
                    color = if (isUvActive) Color(0xFF26193D) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUvActive) Color(0xFFC084FC) else Color(0xFFD97706)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brass Thumbtack pin
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC89B3C))
                                .border(1.dp, Color(0xFFFFF1C2), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isUvActive) "✦ FIELD CARTOGRAPHER'S ULTRAVIOLET CIPHER" else "📌 ARCHIVIST'S FIELD MARGINALIA",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isUvActive) Color(0xFFF0ABFC) else Color(0xFF92400E)
                            )
                            Text(
                                text = if (isUvActive)
                                    "Rotate the brass lantern dial to modulate UV intensity. Hidden Underground Railroad waymarks and cave depths are revealed on each dossier card."
                                else
                                    "Tap any folder flap to peel open surveyor notes. Starred casefiles remain safe in your personal Athenaeum leather binder.",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFF78350F)
                            )
                        }
                    }
                }
            }

            // Section 5: The Stacked Field Dossier Cards
            if (sites.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = Color(0xFF8B6428),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Dossiers Match Current Filter",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFE2D4BF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Adjust the search drawer or category criteria above",
                            fontSize = 11.sp,
                            color = Color(0xFF9E8B75)
                        )
                    }
                }
            } else {
                items(sites, key = { it.id }) { site ->
                    FoldedParchmentCard(
                        site = site,
                        isNarrating = currentNarratingSiteId == site.id,
                        onCardClick = { onCardClick(site) },
                        onToggleFavorite = { onToggleFavorite(site) },
                        onToggleVisited = { onToggleVisited(site) },
                        onNarrateClick = { onNarrateClick(site) },
                        soundEngine = soundEngine
                    )
                }
            }
        }
    }
}

/**
 * Compact brass pull pill for the Explorer's Field Desk shortcuts.
 */
@Composable
private fun DeskQuickPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF26170C),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B6428)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFF3C853),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color(0xFFE2D4BF)
            )
        }
    }
}
