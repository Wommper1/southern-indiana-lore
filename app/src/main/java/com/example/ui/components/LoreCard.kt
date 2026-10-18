package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LoreCategories
import com.example.data.model.LoreSite
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.util.LocationUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoreCard(
    site: LoreSite,
    isCurrentlyNarrating: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVisited: () -> Unit,
    onNarrateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (site.category) {
        LoreCategories.HAUNTED -> GhostCrimson
        LoreCategories.UNDERGROUND_RR -> RiverMistBlue
        LoreCategories.STEAM_AND_RAIL -> LanternAmberBright
        LoreCategories.PIONEER -> Color(0xFFEAB308)
        LoreCategories.CURIOSITIES -> KnobstoneForest
        else -> LanternAmber
    }

    val siteElevation = remember(site.latitude, site.longitude) {
        com.example.util.TopoElevationEngine.getElevationFeet(site.latitude, site.longitude)
    }
    val siteStratum = remember(siteElevation) {
        com.example.util.TopoElevationEngine.getStratum(siteElevation)
    }

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCurrentlyNarrating) Color(0xFFE5B358) else if (site.isVisited) Color(0xFF8B1E1E).copy(alpha = 0.6f) else Color(0xFF382516),
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.2.dp, cardBorderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .testTag("lore_card_${site.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16110D) // Antique dark dossier parchment
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category & Status Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip & Radar Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (radarBadge, badgeColor) = when (site.radarType.uppercase()) {
                        "UNDERGROUND" -> Pair("🕳️ BENEATH YOU", Color(0xFF06B6D4))
                        "BRIDGE" -> Pair("🌉 HISTORIC BRIDGE SPAN", Color(0xFFF59E0B))
                        "LOST_LANDSCAPE" -> Pair("🚂 LOST LANDSCAPE", Color(0xFFF97316))
                        "RIVER_MEMORY" -> Pair("🌊 RIVER MEMORY", Color(0xFF3B82F6))
                        "ROAD_DETECTIVE" -> Pair("🛣️ ROAD DETECTIVE", Color(0xFFEAB308))
                        "FOLKLORE" -> Pair("👻 LOCAL LORE", Color(0xFFA855F7))
                        "CROSSING" -> Pair("⚡ CROSSING", Color(0xFFEC4899))
                        else -> Pair(site.category.uppercase(), categoryColor)
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = radarBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                fontSize = 10.sp
                            ),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    ConfidenceBadge(
                        confidenceLevel = site.confidenceLevel,
                        isCompact = true
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    LocationConfidenceBadge(
                        siteType = site.siteType,
                        isSensitiveLocation = site.isSensitiveLocation,
                        isExactCoordinateSafe = site.isExactCoordinateSafe,
                        isCompact = true
                    )

                    if (site.isSensitiveLocation || !site.isExactCoordinateSafe) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0x33DC2626),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🛡️ SENSITIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (site.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = LanternAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "USER REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = LanternAmberBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Distance & Geofence Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LanternAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = LocationUtils.formatDistance(site.distanceMeters),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ParchmentPaper
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proximity Historian: Crossing Banner if crossing event present
            if (site.youJustCrossedEvent.isNotBlank()) {
                Surface(
                    color = Color(0x2638BDF8),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6638BDF8)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛣️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOU JUST CROSSED: ${site.youJustCrossedEvent}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF38BDF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Title & Subtitle / Drive-by Story
            Text(
                text = site.title,
                fontFamily = com.example.ui.theme.CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                color = ParchmentPaper,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            val displaySnippet = if (site.driveStory.isNotBlank()) site.driveStory else site.subtitle
            Text(
                text = displaySnippet,
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!site.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = site.photoUri,
                        contentDescription = "Site photo",
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips (Era, Altimeter, Wax Seal & Verification)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Topographic Altimeter & Geological Stratum Chip
                Surface(
                    color = Color(0xFF26190E),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8963E).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${siteStratum.iconEmoji} ${siteElevation} FT",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFFF3C853)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "• ${siteStratum.displayName.substringBefore(' ')}",
                            fontSize = 9.sp,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }

                // Archival Wax Seal (if visited)
                if (site.isVisited) {
                    Surface(
                        color = Color(0xFF8B1E1E), // Crimson archival wax
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5B358).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💮", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ARCHIVE SEALED",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0x221E293B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = site.era,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color(0x2210B981),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (site.accessLevel.isNotBlank()) site.accessLevel else site.accessStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = KnobstoneForest,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (site.historyNerdDiscovery.isNotBlank()) {
                    Surface(
                        color = Color(0x22F59E0B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🧠 DISCOVERY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = LanternAmberBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (site.siteType.equals("LINE", ignoreCase = true)) {
                    Surface(
                        color = Color(0x2238BDF8),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "〰️ CORRIDOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (site.radarType.equals("UNDERGROUND", ignoreCase = true) || site.whatMayBeUnderground.isNotBlank()) {
                    Surface(
                        color = Color(0x2206B6D4),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🕳️ SUBTERRANEAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF22D3EE),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (site.encounterCount > 0) {
                    Surface(
                        color = Color(0x2210B981),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🧭 ${site.encounterCount}x ENCOUNTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (site.hasListenedAudio) {
                    Surface(
                        color = Color(0x228B5CF6),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🎧 AUDIO HEARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFFA78BFA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Narrator, Visited, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Narrator Button (48dp touch target)
                Surface(
                    onClick = onNarrateClick,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrentlyNarrating) LanternAmber else Color(0x33F59E0B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrentlyNarrating) LanternAmberBright else LanternAmber.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("narrate_btn_${site.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyNarrating) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isCurrentlyNarrating) "Stop Audio Narration" else "Play Audio Narration",
                            tint = if (isCurrentlyNarrating) Color(0xFF1E1100) else LanternAmberBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCurrentlyNarrating) "Listening..." else "Audio Guide",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrentlyNarrating) Color(0xFF1E1100) else LanternAmberBright
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visited Toggle
                    IconButton(
                        onClick = onToggleVisited,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("visited_btn_${site.id}")
                    ) {
                        Icon(
                            imageVector = if (site.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (site.isVisited) "Marked as visited" else "Mark as visited",
                            tint = if (site.isVisited) KnobstoneForest else ParchmentMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Favorite Toggle
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("favorite_btn_${site.id}")
                    ) {
                        Icon(
                            imageVector = if (site.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (site.isFavorite) "Favorited" else "Add to favorites",
                            tint = if (site.isFavorite) GhostCrimson else ParchmentMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
