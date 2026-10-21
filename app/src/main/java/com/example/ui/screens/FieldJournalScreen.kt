package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import com.example.data.local.LoreVoiceMemoEntity
import com.example.ui.components.OralHistoryMemoItemCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.ExpeditionDossierExporter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoreSiteEntity
import com.example.data.model.CipherRiddle
import com.example.data.model.CuratedMysteries
import com.example.data.model.JournalClueEntry
import com.example.data.model.MysteryStoryline
import com.example.data.model.SketchType
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentDossierBg
import com.example.ui.theme.ParchmentDossierCard
import com.example.ui.theme.ParchmentInkDark
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.ui.theme.StampInkRed
import com.example.ui.theme.StampWaxGold

@Composable
fun FieldJournalScreen(
    allSites: List<LoreSiteEntity>,
    onBack: () -> Unit,
    onOpenViewfinder: (String?) -> Unit,
    onOpenCipher: (CipherRiddle) -> Unit,
    solvedCipherIds: Set<String> = emptySet(),
    soundEngine: com.example.util.AdventureSoundEngine? = null,
    voiceMemos: List<LoreVoiceMemoEntity> = emptyList(),
    currentlyPlayingMemoId: String? = null,
    isPlayingMemo: Boolean = false,
    playbackProgressMs: Long = 0L,
    playbackDurationMs: Long = 0L,
    onOpenTapeDeck: () -> Unit = {},
    onPlayMemo: (LoreVoiceMemoEntity) -> Unit = {},
    onPauseMemo: () -> Unit = {},
    onSeekMemo: (Long) -> Unit = {},
    onDeleteMemo: (LoreVoiceMemoEntity) -> Unit = {},
    onShareMemo: (LoreVoiceMemoEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        soundEngine?.playLeatherUnclasp()
        onBack()
    }
    var selectedMysteryIndex by remember { mutableIntStateOf(0) }
    val mysteries = CuratedMysteries.allMysteries
    val currentMystery = mysteries.getOrElse(selectedMysteryIndex) { mysteries.first() }

    val cluesForMystery = remember(currentMystery) {
        CuratedMysteries.allClues.filter { it.mysteryId == currentMystery.id }
    }

    val cipherForMystery = remember(currentMystery) {
        CuratedMysteries.allCiphers.firstOrNull { it.id == currentMystery.cipherRiddleId }
            ?: CuratedMysteries.RENO_CIPHER
    }

    val visitedSiteIds = remember(allSites) {
        allSites.filter { it.isVisited }.map { it.id }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentDossierBg)
            .testTag("field_journal_screen")
    ) {
        // --- Leather Bound Journal Header ---
        Surface(
            color = ShelfWoodDark,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundEngine?.playLeatherUnclasp()
                            onBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ShelfWoodPlank)
                            .border(1.dp, GildedGold.copy(alpha = 0.5f), CircleShape)
                            .testTag("journal_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GildedGoldBright
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EXPEDITION FIELD JOURNAL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = GildedGoldBright
                        )
                        Text(
                            text = "Sketches, Cipher Dispatches & Field Marginalia",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1D5DB)
                        )
                    }

                    Surface(
                        color = Color(0xFF4A151B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "FIELD LOG",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = GildedGoldBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Mystery Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedMysteryIndex,
                    containerColor = ShelfWoodDark,
                    contentColor = GildedGoldBright,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMysteryIndex]),
                            color = GildedGoldBright,
                            height = 3.dp
                        )
                    }
                ) {
                    mysteries.forEachIndexed { index, mystery ->
                        Tab(
                            selected = selectedMysteryIndex == index,
                            onClick = {
                                soundEngine?.playParchmentRustle()
                                selectedMysteryIndex = index
                            },
                            text = {
                                Text(
                                    text = mystery.title.take(24) + "...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedMysteryIndex == index) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selectedMysteryIndex == index) GildedGoldBright else Color(0xFF9CA3AF)
                                )
                            },
                            modifier = Modifier.testTag("journal_tab_$index")
                        )
                    }
                }
            }
        }

        // --- Journal Pages Body (Parchment Paper Layout) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mystery Storyline Overview Parchment Folio
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ParchmentDossierCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6CEBF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentMystery.historicalEra.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = LanternAmber
                            )

                            Surface(
                                color = StampWaxGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StampWaxGold)
                            ) {
                                Text(
                                    text = "EXPEDITION FILE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentMystery.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ParchmentInkDark
                        )

                        Text(
                            text = currentMystery.tagline,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF78350F)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentMystery.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val isCipherSolved = solvedCipherIds.contains(cipherForMystery.id)

                        if (isCipherSolved) {
                            // Unlocked Historic Artifact Claim Card
                            Surface(
                                color = Color(0xFF1E2E1E),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, KnobstoneForest),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GildedGoldBright,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ARTIFACT UNLOCKED & SECURED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            ),
                                            color = KnobstoneForest
                                        )
                                        Text(
                                            text = currentMystery.artifactTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Cipher solved. Placed in Field Relic Vault.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Cipher Decoder Launcher Button
                        Button(
                            onClick = {
                                soundEngine?.playWaxSealBreak()
                                onOpenCipher(cipherForMystery)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_cipher_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCipherSolved) Color(0xFF1B281B) else ShelfWoodDark,
                                contentColor = GildedGoldBright
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCipherSolved) KnobstoneForest else GildedGold
                            )
                        ) {
                            Icon(
                                imageVector = if (isCipherSolved) Icons.Default.CheckCircle else Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isCipherSolved) KnobstoneForest else GildedGoldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCipherSolved) "REVIEW CIPHER WHEEL (SOLVED)" else "DECODE HISTORICAL CIPHER WHEEL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }

            // Section Header: Field Clues & Sketches
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ARCHIVAL FIELD SKETCHES & NOTES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF57534E)
                    )

                    Text(
                        text = "${cluesForMystery.size} CLUES LOGGED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmber
                    )
                }
            }

            // Clue Entries with Pencil Sketches & Polaroids
            items(cluesForMystery) { clue ->
                val isSiteVisited = visitedSiteIds.contains(clue.siteId)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ParchmentDossierCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSiteVisited) GildedGold else Color(0xFFD6CEBF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSiteVisited) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isSiteVisited) Color(0xFF15803D) else Color(0xFF78716C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = clue.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ParchmentInkDark
                                )
                            }

                            if (isSiteVisited) {
                                Surface(
                                    color = StampInkRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StampInkRed)
                                ) {
                                    Text(
                                        text = "VISITED & STAMPED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        ),
                                        color = StampInkRed,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Architectural Pencil Sketch
                        PencilSketchCanvas(sketchType = clue.sketchType)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Archival Field Note
                        Surface(
                            color = Color(0xFFF9F5EC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DAC8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "“${clue.handwrittenNote}”",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 18.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "📌 MARGINALIA: ${clue.historicalMarginalia}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF78350F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Viewfinder HUD Inspector Launcher
                        OutlinedButton(
                            onClick = { onOpenViewfinder(clue.siteId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShelfWoodDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ShelfWoodDark)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inspect with Viewfinder HUD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // --- Version 1.0 Field Expedition Dossier & GPX Export ---
            item {
                val context = LocalContext.current
                val domainSites = remember(allSites) { allSites.map { it.toDomain() } }
                val visitedCount = remember(allSites) { allSites.count { it.isVisited || it.encounterCount > 0 } }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 24.dp)
                        .testTag("expedition_dossier_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ShelfWoodDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GildedGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = GildedGoldBright,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "EXPEDITION DOSSIER EXPORT",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = GildedGoldBright
                                    )
                                    Text(
                                        text = "Version 1.0.1 Production Release by Alex Shade Productions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD1D5DB)
                                    )
                                }
                            }

                            Surface(
                                color = KnobstoneForest,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "v1.0.1",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Export your collected field notes, custom discoveries, and geospatial waypoints into standard formats for archival research, GIS mapping, or sharing with expedition colleagues.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = ShelfWoodPlank,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${allSites.size}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GildedGoldBright
                                    )
                                    Text(
                                        text = "Mapped POIs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                color = ShelfWoodPlank,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$visitedCount",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LanternAmberBright
                                    )
                                    Text(
                                        text = "Encountered",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                color = ShelfWoodPlank,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "WGS84",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Datum",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action 1: Export Markdown Dossier
                        Button(
                            onClick = {
                                soundEngine?.playParchmentRustle()
                                val dossierMd = ExpeditionDossierExporter.generateMarkdownDossier(
                                    sites = domainSites,
                                    title = "Southern Indiana Lore Expedition Dossier"
                                )
                                ExpeditionDossierExporter.shareContent(
                                    context = context,
                                    content = dossierMd,
                                    subject = "Southern Indiana Lore - Expedition Dossier v1.0.1",
                                    mimeType = "text/markdown"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("export_dossier_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GildedGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXPORT EXPEDITION DOSSIER (.MD)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action 2: Export GPX 1.1 Waypoints
                        OutlinedButton(
                            onClick = {
                                soundEngine?.playWaxSealBreak()
                                val gpxXml = ExpeditionDossierExporter.generateGpxTrack(
                                    sites = domainSites,
                                    trackName = "Southern Indiana Lore Waypoints v1.0.1"
                                )
                                ExpeditionDossierExporter.shareContent(
                                    context = context,
                                    content = gpxXml,
                                    subject = "Southern Indiana Lore - GPX 1.1 Waypoints",
                                    mimeType = "application/gpx+xml"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("export_gpx_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GildedGoldBright),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GildedGoldBright)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXPORT GPX 1.1 WAYPOINTS (GARMIN / QGIS)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // --- Oral History Archive & Vintage Tape Deck Memos ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.5.dp, GildedGold, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = ShelfWoodDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = GildedGoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ORAL HISTORY ARCHIVE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = GildedGoldBright
                                    )
                                    Text(
                                        text = "Eyewitness Interviews & Field Memos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC7B7A3)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    soundEngine?.playBrassToggleSwitch()
                                    onOpenTapeDeck()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB89047)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RECORD", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (voiceMemos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ShelfWoodPlank, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF4A3828), RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No oral folklore memos logged yet. Tap RECORD above to document firsthand accounts, elder folklore, or nocturnal EVP recordings on site.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9E8D77),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                voiceMemos.forEach { memo ->
                                    val isThisPlaying = currentlyPlayingMemoId == memo.id && isPlayingMemo
                                    OralHistoryMemoItemCard(
                                        memo = memo,
                                        isPlaying = isThisPlaying,
                                        playbackProgressMs = if (currentlyPlayingMemoId == memo.id) playbackProgressMs else 0L,
                                        playbackDurationMs = if (currentlyPlayingMemoId == memo.id) playbackDurationMs else memo.durationMs,
                                        onPlay = { onPlayMemo(memo) },
                                        onPause = onPauseMemo,
                                        onSeek = onSeekMemo,
                                        onDelete = { onDeleteMemo(memo) },
                                        onShare = { onShareMemo(memo) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Procedural pencil / charcoal sketches for landmarks drawn on aged canvas
 */
@Composable
private fun PencilSketchCanvas(sketchType: SketchType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFE6D5))
            .border(1.dp, Color(0xFFD3C8B4), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width
            val h = size.height
            val pencilColor = Color(0xFF475569)
            val strokeW = 1.5.dp.toPx()

            when (sketchType) {
                SketchType.CLOCK_TOWER -> {
                    // Tower shaft
                    drawRect(pencilColor, Offset(w * 0.42f, h * 0.25f), size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.75f), style = Stroke(strokeW))
                    // Belfry roof spire
                    val spire = Path().apply {
                        moveTo(w * 0.38f, h * 0.25f)
                        lineTo(w * 0.5f, h * 0.05f)
                        lineTo(w * 0.62f, h * 0.25f)
                        close()
                    }
                    drawPath(spire, pencilColor, style = Stroke(strokeW))
                    // Clock circle
                    drawCircle(pencilColor, radius = h * 0.08f, center = Offset(w * 0.5f, h * 0.45f), style = Stroke(strokeW))
                }
                SketchType.RAIL_TUNNEL -> {
                    // Tunnel Arch
                    val arch = Path().apply {
                        moveTo(w * 0.25f, h * 0.9f)
                        lineTo(w * 0.25f, h * 0.45f)
                        cubicTo(w * 0.25f, h * 0.15f, w * 0.75f, h * 0.15f, w * 0.75f, h * 0.45f)
                        lineTo(w * 0.75f, h * 0.9f)
                    }
                    drawPath(arch, pencilColor, style = Stroke(strokeW))
                    // Rail lines converging
                    drawLine(pencilColor, Offset(w * 0.35f, h * 0.9f), Offset(w * 0.48f, h * 0.5f), strokeW)
                    drawLine(pencilColor, Offset(w * 0.65f, h * 0.9f), Offset(w * 0.52f, h * 0.5f), strokeW)
                }
                SketchType.CAVE_MOUTH -> {
                    // Irregular cavern contour
                    val cave = Path().apply {
                        moveTo(w * 0.15f, h * 0.85f)
                        lineTo(w * 0.25f, h * 0.35f)
                        lineTo(w * 0.45f, h * 0.2f)
                        lineTo(w * 0.75f, h * 0.3f)
                        lineTo(w * 0.85f, h * 0.85f)
                    }
                    drawPath(cave, pencilColor, style = Stroke(strokeW))
                    // Stalactite spikes
                    drawLine(pencilColor, Offset(w * 0.35f, h * 0.27f), Offset(w * 0.35f, h * 0.45f), strokeW)
                    drawLine(pencilColor, Offset(w * 0.55f, h * 0.24f), Offset(w * 0.55f, h * 0.5f), strokeW)
                }
                else -> {
                    // Trestle / Bridge truss
                    drawLine(pencilColor, Offset(w * 0.1f, h * 0.4f), Offset(w * 0.9f, h * 0.4f), strokeW * 2)
                    drawLine(pencilColor, Offset(w * 0.2f, h * 0.4f), Offset(w * 0.3f, h * 0.9f), strokeW)
                    drawLine(pencilColor, Offset(w * 0.4f, h * 0.4f), Offset(w * 0.5f, h * 0.9f), strokeW)
                    drawLine(pencilColor, Offset(w * 0.6f, h * 0.4f), Offset(w * 0.7f, h * 0.9f), strokeW)
                    drawLine(pencilColor, Offset(w * 0.8f, h * 0.4f), Offset(w * 0.9f, h * 0.9f), strokeW)
                }
            }
        }

        // Taped Corner Graphic
        Text(
            text = "ARCHIVAL FIELD SKETCH",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            ),
            color = Color(0xFF64748B),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
        )
    }
}
