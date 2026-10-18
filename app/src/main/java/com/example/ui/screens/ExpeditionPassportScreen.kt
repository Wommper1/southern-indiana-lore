package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoreSiteEntity
import com.example.data.model.CuratedExpeditionTrails
import com.example.data.model.ExpeditionTrail
import com.example.data.model.ExplorerBadge
import com.example.data.model.TrailProgress
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
fun ExpeditionPassportView(
    allSites: List<LoreSiteEntity>,
    onSelectSite: (LoreSiteEntity) -> Unit,
    onStartTrailTour: (ExpeditionTrail) -> Unit,
    modifier: Modifier = Modifier
) {
    val visitedSiteIds = allSites.filter { it.isVisited }.map { it.id }.toSet()
    val sitesMap = allSites.associateBy { it.id }

    val trailProgressList: List<TrailProgress> = CuratedExpeditionTrails.allTrails.map { trail ->
        CuratedExpeditionTrails.calculateProgress(trail, visitedSiteIds)
    }

    val unlockedBadgesCount = trailProgressList.count { it.isCompleted }
    val totalSitesCount = allSites.size
    val totalVisitedCount = visitedSiteIds.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- Vintage Explorer Passport Header Booklet ---
        Card(
            colors = CardDefaults.cardColors(containerColor = ShelfWoodDark),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "EXPEDITION PASSPORT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = GildedGoldBright
                        )
                        Text(
                            text = "State of Indiana Historical Frontier Register",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1D5DB)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GildedGold.copy(alpha = 0.2f), CircleShape)
                            .border(1.5.dp, GildedGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Explorer Seal",
                            tint = GildedGoldBright,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PassportStatItem(
                        value = "$totalVisitedCount / $totalSitesCount",
                        label = "Sites Stamped",
                        accentColor = StampInkRed
                    )
                    PassportStatItem(
                        value = "$unlockedBadgesCount / 4",
                        label = "Badges Earned",
                        accentColor = GildedGoldBright
                    )
                    PassportStatItem(
                        value = "${trailProgressList.count { it.isCompleted }} / 4",
                        label = "Trails Complete",
                        accentColor = KnobstoneForest
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Curated Trails Section Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CURATED EXPEDITION TRAILS",
                style = MaterialTheme.typography.titleSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ParchmentInkDark
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Trail Cards
        trailProgressList.forEach { progress ->
            TrailProgressCard(
                progress = progress,
                sitesMap = sitesMap,
                onSelectSite = onSelectSite,
                onStartTrail = { onStartTrailTour(progress.trail) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Stamped Passport Seals Gallery ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = StampInkRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "OFFICIAL INK PASSPORT SEALS",
                style = MaterialTheme.typography.titleSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ParchmentInkDark
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Rubber stamped seals acquired by visiting historic waypoints and caverns in person.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF78716C)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Inked Passport Seals
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            allSites.forEach { site ->
                PassportStampBadge(
                    site = site,
                    isStamped = visitedSiteIds.contains(site.id),
                    onClick = { onSelectSite(site) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PassportStatItem(
    value: String,
    label: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = accentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
fun TrailProgressCard(
    progress: TrailProgress,
    sitesMap: Map<String, LoreSiteEntity>,
    onSelectSite: (LoreSiteEntity) -> Unit,
    onStartTrail: () -> Unit
) {
    val trail = progress.trail
    val themeColor = Color(trail.themeColorHex)

    Card(
        colors = CardDefaults.cardColors(containerColor = ParchmentDossierCard),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trail.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = ParchmentInkDark
                    )
                    Text(
                        text = trail.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Color(0xFF57534E)
                    )
                }

                // Badge Emblem
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (progress.isCompleted) themeColor.copy(alpha = 0.2f) else Color(0xFFE5E7EB),
                            CircleShape
                        )
                        .border(
                            1.5.dp,
                            if (progress.isCompleted) themeColor else Color(0xFF9CA3AF),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (progress.isCompleted) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = trail.badgeTitle,
                        tint = if (progress.isCompleted) themeColor else Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = trail.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = Color(0xFF44403C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar & metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progress.visitedCount} of ${progress.totalCount} Waypoints Verified (${progress.progressPercent}%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (progress.isCompleted) KnobstoneForest else Color(0xFF78716C)
                )

                Text(
                    text = "~${trail.estimatedDistanceKm} km route",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF78716C)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = themeColor,
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trail Waypoints list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                trail.orderedSiteIds.forEachIndexed { index, siteId ->
                    val site = sitesMap[siteId]
                    if (site != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSite(site) }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (site.isVisited) Icons.Default.CheckCircle else Icons.Default.Place,
                                contentDescription = null,
                                tint = if (site.isVisited) StampInkRed else Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${index + 1}. ${site.title}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (site.isVisited) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (site.isVisited) ParchmentInkDark else Color(0xFF57534E),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = site.county.replace(" County", ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF78716C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStartTrail,
                colors = ButtonDefaults.buttonColors(containerColor = ShelfWoodDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = GildedGoldBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch Trail in Driving HUD",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PassportStampBadge(
    site: LoreSiteEntity,
    isStamped: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isStamped) StampInkRed.copy(alpha = 0.08f) else Color(0xFFF5F0E6),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isStamped) StampInkRed else Color(0xFFD6CEBF)
        ),
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .testTag("passport_stamp_${site.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isStamped) StampInkRed.copy(alpha = 0.15f) else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isStamped) StampInkRed else Color(0xFF9CA3AF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStamped) Icons.Default.Verified else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = if (isStamped) StampInkRed else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = site.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isStamped) ParchmentInkDark else Color(0xFF78716C),
                maxLines = 2,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isStamped) "STAMPED & VERIFIED" else "UNDISCOVERED",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp
                ),
                color = if (isStamped) StampInkRed else Color(0xFF9CA3AF)
            )
        }
    }
}
