package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoreSiteEntity
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.ParchmentDossierBg
import com.example.ui.theme.ParchmentDossierCard
import com.example.ui.theme.ParchmentInkDark
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.ui.theme.StampInkRed
import com.example.ui.theme.StampWaxGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TomeDossierReader(
    site: LoreSiteEntity,
    romanNumeral: String,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onPlayNarration: (LoreSiteEntity) -> Unit,
    onStopNarration: () -> Unit,
    isAudioPlaying: Boolean,
    narratingSiteId: String?,
    onOpenInCockpit: (LoreSiteEntity) -> Unit,
    onEditNotes: (LoreSiteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }
    val isNarratingThisSite = isAudioPlaying && narratingSiteId == site.id

    val sections = listOf(
        "Historical Dossier",
        "Witness Lore",
        "Subterranean & Maps",
        "Bibliography & Sources"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ParchmentDossierBg)
            .statusBarsPadding()
    ) {
        // --- Antique Library Leather Header Bar ---
        Surface(
            color = ShelfWoodDark,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tome_reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return to Athenaeum Shelves",
                            tint = GildedGoldBright
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ATHENAEUM ARCHIVES • $romanNumeral",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GildedGold
                        )
                        Text(
                            text = site.county.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFD1D5DB)
                        )
                    }

                    // Bookmark volume action
                    IconButton(
                        onClick = { onToggleBookmark(site.id) },
                        modifier = Modifier.testTag("tome_reader_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (site.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (site.isFavorite) "Bookmarked Volume" else "Bookmark Volume",
                            tint = if (site.isFavorite) GildedGoldBright else Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Stamp passport button
                    IconButton(
                        onClick = { onToggleVisited(site.id) },
                        modifier = Modifier.testTag("tome_reader_stamp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (site.isVisited) "Passport Stamped" else "Stamp Passport",
                            tint = if (site.isVisited) StampInkRed else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Gilded Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    GildedGold,
                                    GildedGoldBright,
                                    GildedGold,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // --- Manuscript Body Scrollable Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Title Header Box
            Card(
                colors = CardDefaults.cardColors(containerColor = ParchmentDossierCard),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ShelfWoodPlank,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GildedGold)
                        ) {
                            Text(
                                text = "ERA: ${site.era}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = GildedGoldBright,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (site.isVisited) {
                            Surface(
                                color = StampInkRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StampInkRed)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = StampInkRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PASSPORT STAMPED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = StampInkRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = site.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 28.sp
                        ),
                        color = ParchmentInkDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = site.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Color(0xFF57534E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verification Seal & Category Tags
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = KnobstoneForest.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KnobstoneForest.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = site.verificationGrade.ifBlank { "Not independently verified" },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = LanternAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = site.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Audio Narration Player Bar ---
                    Surface(
                        color = ShelfWoodDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (isNarratingThisSite) {
                                        onStopNarration()
                                    } else {
                                        onPlayNarration(site)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(GildedGold, CircleShape)
                                    .testTag("tome_reader_audio_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isNarratingThisSite) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isNarratingThisSite) "Stop Narration" else "Listen to Tome Narration",
                                    tint = ShelfWoodDark
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isNarratingThisSite) "Narrating Historical Dossier..." else "Spoken Archival Narration",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Hands-free audio transcription & field guide",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }

            // --- Section Tabs ---
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = ParchmentDossierBg,
                contentColor = ShelfWoodDark,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = StampInkRed,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selectedSection == index) ShelfWoodDark else Color(0xFF78716C)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Active Section Content ---
            Card(
                colors = CardDefaults.cardColors(containerColor = ParchmentDossierCard),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    when (selectedSection) {
                        0 -> {
                            // Section I: Historical Record
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = StampWaxGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CHRONICLED HISTORICAL RECORD",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ParchmentInkDark
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = site.verifiedHistory.ifBlank { site.fullStory },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 24.sp
                                ),
                                color = ParchmentInkDark
                            )

                            if (site.fullStory.isNotBlank() && site.verifiedHistory.isNotBlank() && site.fullStory != site.verifiedHistory) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFD6CEBF))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "ARCHIVAL EXPEDITION NOTES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF78716C)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = site.fullStory,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = Color(0xFF44403C)
                                )
                            }
                        }

                        1 -> {
                            // Section II: Witness Lore
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = RiverMistBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ORAL TRADITIONS & WITNESS LORE",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ParchmentInkDark
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (site.folkloreTradition.isNotBlank()) {
                                Text(
                                    text = site.folkloreTradition,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 24.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = ParchmentInkDark
                                )
                            } else {
                                Text(
                                    text = "No undocumented folkloric hearsay recorded. This site stands upon strictly verifiable municipal and territorial deeds.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF78716C)
                                )
                            }

                            if (site.driveStory.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFD6CEBF))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ROAD TRIP & FIELD PASSAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF78716C)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = site.driveStory,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = Color(0xFF44403C)
                                )
                            }
                        }

                        2 -> {
                            // Section III: Subterranean & Maps
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = KnobstoneForest,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SUBTERRANEAN ANATOMY & MAPS",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ParchmentInkDark
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "What Lies Beneath Ground Level:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentInkDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = site.whatMayBeUnderground.ifBlank { "Limestone bedrock foundation with karst sinkhole drainage network." },
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color(0xFF44403C)
                            )

                            if (site.visibleRemains.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Visible Remains Today:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentInkDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = site.visibleRemains,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = Color(0xFF44403C)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Access Level:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentInkDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = site.accessLevel.ifBlank { "Public Access & Roadside Landmark" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF065F46)
                            )
                        }

                        3 -> {
                            // Section IV: Bibliography & Sources
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = StampInkRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PRIMARY SOURCES & BIBLIOGRAPHY",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ParchmentInkDark
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = site.sources.ifBlank { "Indiana Historical Bureau Archives; County Historical Society Records." },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = Color(0xFF292524)
                            )

                            if (site.sourceNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Archivist Citation Index:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF78716C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = site.sourceNotes,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = Color(0xFF57534E)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Expedition Action Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenInCockpit(site) },
                    colors = ButtonDefaults.buttonColors(containerColor = ShelfWoodDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tome_action_cockpit")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = GildedGoldBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cockpit Tour",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = {
                        val navUri = Uri.parse("google.navigation:q=${site.latitude},${site.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val geoUri = Uri.parse("geo:${site.latitude},${site.longitude}?q=${Uri.encode(site.title)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tome_action_google_maps")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = ShelfWoodDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Directions",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ShelfWoodDark
                    )
                }

                Button(
                    onClick = { onEditNotes(site) },
                    colors = ButtonDefaults.buttonColors(containerColor = StampWaxGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("tome_action_notes")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Field Notes",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
