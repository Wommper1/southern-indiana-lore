package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import com.example.data.model.CipherRiddle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoreSiteEntity
import com.example.data.model.ExpeditionTrail
import com.example.data.model.LoreTome
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.ParchmentDossierBg
import com.example.ui.theme.ParchmentInkDark
import com.example.ui.theme.ShelfWoodBevel
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.ui.theme.StampInkRed

@Composable
fun LibraryBookcaseScreen(
    allSites: List<LoreSiteEntity>,
    onSelectSite: (LoreSiteEntity, String) -> Unit,
    onStartTrailTour: (ExpeditionTrail) -> Unit,
    onOpenCipher: ((CipherRiddle) -> Unit)? = null,
    onOpenViewfinder: ((String?) -> Unit)? = null,
    solvedCipherIds: Set<String> = emptySet(),
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    var selectedTopTab by remember { mutableIntStateOf(0) } // 0 = Bookcase, 1 = Passport, 2 = Journal
    var searchQuery by remember { mutableStateOf("") }
    var selectedShelfFilter by remember { mutableStateOf("All Shelves") }

    val tomes = remember(allSites) {
        allSites.mapIndexed { index, site ->
            LoreTome.fromSite(site, index)
        }
    }

    val filteredTomes = remember(tomes, searchQuery, selectedShelfFilter) {
        tomes.filter { tome ->
            val matchesSearch = searchQuery.isBlank() ||
                tome.title.contains(searchQuery, ignoreCase = true) ||
                tome.subtitle.contains(searchQuery, ignoreCase = true) ||
                tome.county.contains(searchQuery, ignoreCase = true) ||
                tome.era.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedShelfFilter) {
                "Pioneer & Frontier" -> tome.category.contains("Pioneer", ignoreCase = true) || tome.era.contains("1808", ignoreCase = true) || tome.era.contains("Territorial", ignoreCase = true)
                "Underground Railroad" -> tome.category.contains("Underground", ignoreCase = true) || tome.title.contains("Underground", ignoreCase = true) || tome.subtitle.contains("Abolition", ignoreCase = true)
                "Riverboat & Rail" -> tome.category.contains("Steam", ignoreCase = true) || tome.title.contains("Rail", ignoreCase = true) || tome.title.contains("Depot", ignoreCase = true) || tome.title.contains("Bridge", ignoreCase = true)
                "Subterranean Karst" -> tome.category.contains("Cave", ignoreCase = true) || tome.title.contains("Cave", ignoreCase = true) || tome.site.whatMayBeUnderground.isNotBlank()
                "Bookmarked Only" -> tome.isFavorite
                "Stamped Only" -> tome.isVisited
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    // Split tomes into shelves of 5 books each for natural bookshelf presentation
    val shelfRows = remember(filteredTomes) {
        filteredTomes.chunked(5)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentDossierBg)
    ) {
        // --- Antique Library Header Banner ---
        Surface(
            color = ShelfWoodDark,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalLibrary,
                                contentDescription = null,
                                tint = GildedGoldBright,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SOUTHERN INDIANA ARCHIVES",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = GildedGoldBright
                            )
                        }
                        Text(
                            text = "Historic Dossiers, Karst & River Valley Chronicles",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1D5DB)
                        )
                    }

                    Surface(
                        color = ShelfWoodPlank,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${allSites.size} VOLUMES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GildedGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Top Mode Tab Row: Bookcase Shelves vs Expedition Passport
                TabRow(
                    selectedTabIndex = selectedTopTab,
                    containerColor = ShelfWoodDark,
                    contentColor = GildedGoldBright,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTopTab]),
                            color = GildedGoldBright,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTopTab == 0,
                        onClick = {
                            soundEngine?.playParchmentRustle()
                            selectedTopTab = 0
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalLibrary,
                                    contentDescription = null,
                                    tint = if (selectedTopTab == 0) GildedGoldBright else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ohio Valley Archive",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTopTab == 0) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTopTab == 0) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_bookcase")
                    )

                    Tab(
                        selected = selectedTopTab == 1,
                        onClick = {
                            soundEngine?.playGoldCoinClink()
                            selectedTopTab = 1
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = if (selectedTopTab == 1) GildedGoldBright else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Expedition Passport",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTopTab == 1) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTopTab == 1) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_passport")
                    )

                    Tab(
                        selected = selectedTopTab == 2,
                        onClick = {
                            soundEngine?.playLeatherUnclasp()
                            selectedTopTab = 2
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (selectedTopTab == 2) GildedGoldBright else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Expedition Journal",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTopTab == 2) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedTopTab == 2) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_field_journal")
                    )
                }
            }
        }

        when (selectedTopTab) {
            1 -> {
                // Expedition Passport & Trails View
                ExpeditionPassportView(
                    allSites = allSites,
                    onSelectSite = { site -> onSelectSite(site, "VOL. X") },
                    onStartTrailTour = onStartTrailTour
                )
            }
            2 -> {
                // Expedition Field Journal View
                FieldJournalScreen(
                    allSites = allSites,
                    onBack = { selectedTopTab = 0 },
                    onOpenViewfinder = { siteId -> onOpenViewfinder?.invoke(siteId) },
                    onOpenCipher = { riddle -> onOpenCipher?.invoke(riddle) },
                    solvedCipherIds = solvedCipherIds
                )
            }
            else -> {
                // Bookcase Shelves View
                Column(modifier = Modifier.fillMaxSize()) {
                // Search and Filter Bar
                Surface(
                    color = ParchmentDossierBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search volumes by name, county, or era...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78716C)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ShelfWoodDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = ShelfWoodDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedBorderColor = ShelfWoodDark,
                                unfocusedBorderColor = Color(0xFFD6CEBF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("athenaeum_search_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Filter chips row
                        val filterOptions = listOf(
                            "All Shelves",
                            "Pioneer & Frontier",
                            "Underground Railroad",
                            "Riverboat & Rail",
                            "Subterranean Karst",
                            "Bookmarked Only",
                            "Stamped Only"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(filterOptions) { filter ->
                                FilterChip(
                                    selected = selectedShelfFilter == filter,
                                    onClick = {
                                        soundEngine?.playTactileClick()
                                        selectedShelfFilter = filter
                                    },
                                    label = {
                                        Text(
                                            text = filter,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (selectedShelfFilter == filter) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ShelfWoodDark,
                                        selectedLabelColor = GildedGoldBright,
                                        containerColor = Color.White,
                                        labelColor = ParchmentInkDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedShelfFilter == filter,
                                        borderColor = if (selectedShelfFilter == filter) GildedGold else Color(0xFFD6CEBF)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }
                    }
                }

                // --- Wooden Shelves with Books ---
                if (shelfRows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No leather-bound volumes match your search. Clear filters to explore all 52 archives.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF78716C),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                    ) {
                        items(shelfRows) { tomesOnShelf ->
                            BookcaseShelfRow(
                                tomes = tomesOnShelf,
                                onSelectTome = { tome ->
                                    soundEngine?.playLeatherUnclasp()
                                    onSelectSite(tome.site, tome.romanNumeral)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun BookcaseShelfRow(
    tomes: List<LoreTome>,
    onSelectTome: (LoreTome) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Books standing on the shelf
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            tomes.forEach { tome ->
                LeatherTomeSpine(
                    tome = tome,
                    onClick = { onSelectTome(tome) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Fill remaining shelf space if fewer than 5 books
            repeat(5 - tomes.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Solid Wooden Shelf Plank
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .shadow(6.dp, RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ShelfWoodBevel,
                            ShelfWoodPlank,
                            ShelfWoodDark
                        )
                    ),
                    RoundedCornerShape(2.dp)
                )
        )

        // Wood grain shelf shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun LeatherTomeSpine(
    tome: LoreTome,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spineHeightDp = (175 * tome.heightFraction).dp

    Column(
        modifier = modifier
            .height(spineHeightDp)
            .shadow(4.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        tome.leatherColor.copy(alpha = 0.85f),
                        tome.leatherColor,
                        tome.leatherColor.copy(alpha = 0.95f),
                        tome.leatherColor.copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .border(
                1.dp,
                GildedGold.copy(alpha = 0.4f),
                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("tome_spine_${tome.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Gilded Roman Numeral
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tome.romanNumeral,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = GildedGoldBright,
                textAlign = TextAlign.Center
            )

            // Top gold spine rib line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(GildedGold.copy(alpha = 0.6f))
            )
        }

        // Bookmark ribbon or visited wax stamp indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (tome.isVisited) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Stamped",
                    tint = StampInkRed,
                    modifier = Modifier.size(12.dp)
                )
            } else if (tome.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = GildedGoldBright,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Vertical Tome Title
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tome.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 11.sp
                ),
                color = Color(0xFFFDE68A),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Spine Ribs and County Seal
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(GildedGold.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tome.county.replace(" County", "").take(4).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
