package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CuratedOralHistoryArchive
import com.example.data.model.HistoricalRelic
import com.example.data.model.RelicRarity
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.ShelfWoodDark
import com.example.util.AdventureSoundEngine

@Composable
fun RelicVaultView(
    visitedSiteIds: Set<String>,
    solvedCipherIds: Set<String>,
    soundEngine: AdventureSoundEngine?,
    onNavigateToSite: ((String) -> Unit)? = null,
    onOpenLab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedRelic by remember { mutableStateOf<HistoricalRelic?>(null) }
    val relics = CuratedOralHistoryArchive.allRelics

    val unlockedCount = remember(visitedSiteIds, solvedCipherIds) {
        relics.count { CuratedOralHistoryArchive.isRelicUnlocked(it, visitedSiteIds, solvedCipherIds) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2A0808)) // Deep antique velvet ruby
            .testTag("relic_vault_view")
    ) {
        // Vault Header Banner
        Surface(
            color = ShelfWoodDark,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EXPEDITION RELIC VAULT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = GildedGoldBright
                        )
                    }
                    Text(
                        text = "Authentic artifacts collected from field explorations",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD1D5DB)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenLab != null) {
                        Surface(
                            color = Color(0xFFC89B3C),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    soundEngine?.playWaxStampImpact()
                                    onOpenLab()
                                }
                                .testTag("relic_vault_open_lab_btn")
                        ) {
                            Text(
                                text = "EXAMINATION LAB",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E1100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF1E1008),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "$unlockedCount / ${relics.size} UNLOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GildedGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Velvet Grid of Relics
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(relics) { relic ->
                val isUnlocked = CuratedOralHistoryArchive.isRelicUnlocked(relic, visitedSiteIds, solvedCipherIds)
                RelicGridCard(
                    relic = relic,
                    isUnlocked = isUnlocked,
                    onClick = {
                        soundEngine?.playGoldCoinClink()
                        selectedRelic = relic
                    }
                )
            }
        }
    }

    // Relic Inspection Dialog
    selectedRelic?.let { relic ->
        val isUnlocked = CuratedOralHistoryArchive.isRelicUnlocked(relic, visitedSiteIds, solvedCipherIds)
        RelicInspectionDialog(
            relic = relic,
            isUnlocked = isUnlocked,
            onDismiss = { selectedRelic = null },
            onNavigateToSite = {
                selectedRelic = null
                onNavigateToSite?.invoke(relic.linkedSiteId)
            },
            onOpenLab = if (onOpenLab != null) {
                {
                    selectedRelic = null
                    onOpenLab()
                }
            } else null
        )
    }
}

@Composable
fun RelicGridCard(
    relic: HistoricalRelic,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.5.dp,
                if (isUnlocked) Color(relic.primaryColorHex).copy(alpha = 0.6f) else Color(0x336B7280),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("relic_card_${relic.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF380E0E) else Color(0xFF1F0B0B)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Rarity / Lock Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isUnlocked) Color(relic.primaryColorHex).copy(alpha = 0.2f) else Color(0x334B5563),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isUnlocked) relic.rarity.name else "LOCKED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                        color = if (isUnlocked) Color(relic.primaryColorHex) else Color(0xFF9CA3AF),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Central Relic Icon / Emblem
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(
                        if (isUnlocked) {
                            Modifier.background(
                                Brush.radialGradient(
                                    colors = listOf(Color(relic.primaryColorHex).copy(alpha = 0.4f), Color(0xFF220808))
                                )
                            )
                        } else {
                            Modifier.background(Color(0x22111827))
                        }
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) Color(relic.primaryColorHex) else Color(0x334B5563),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val vector = getRelicIcon(relic.badgeIconName)
                Icon(
                    imageVector = vector,
                    contentDescription = relic.title,
                    tint = if (isUnlocked) Color(relic.primaryColorHex) else Color(0x446B7280),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Title & Era
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isUnlocked) relic.title else "Hidden Artifact",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = if (isUnlocked) ParchmentPaper else Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isUnlocked) relic.material else "Explore sites or ciphers",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isUnlocked) LanternAmberBright else Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun RelicInspectionDialog(
    relic: HistoricalRelic,
    isUnlocked: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSite: () -> Unit,
    onOpenLab: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Color(relic.primaryColorHex), RoundedCornerShape(20.dp))
                .testTag("relic_inspect_dialog"),
            color = Color(0xFF1F0909),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(relic.primaryColorHex).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = relic.rarity.name + " HISTORICAL RELIC",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(relic.primaryColorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ParchmentMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Relic Emblem Showcase
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(relic.primaryColorHex).copy(alpha = 0.25f))
                        .border(2.dp, Color(relic.primaryColorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getRelicIcon(relic.badgeIconName),
                        contentDescription = relic.title,
                        tint = Color(relic.primaryColorHex),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = relic.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GildedGoldBright,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${relic.era} • ${relic.material}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = LanternAmber,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = Color(0xFF381010),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(relic.primaryColorHex).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PROVENANCE & DISCOVERY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GildedGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = relic.provenance,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "EXPEDITION LORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GildedGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = relic.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenLab != null) {
                        Button(
                            onClick = { onOpenLab() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("relic_open_lab_dialog_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC89B3C),
                                contentColor = Color(0xFF1E1100)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Examine in Lab", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onNavigateToSite,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("relic_navigate_site_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LanternAmber,
                            contentColor = Color(0xFF1E1100)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View Site", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getRelicIcon(name: String): ImageVector {
    return when (name) {
        "MonetizationOn" -> Icons.Default.MonetizationOn
        "OfflineBolt" -> Icons.Default.OfflineBolt
        "Explore" -> Icons.Default.Explore
        "Build" -> Icons.Default.Build
        "DirectionsBoat" -> Icons.Default.DirectionsBoat
        "Spa" -> Icons.Default.Spa
        "Architecture" -> Icons.Default.Architecture
        "Straighten" -> Icons.Default.Straighten
        "VerifiedUser" -> Icons.Default.VerifiedUser
        "Key" -> Icons.Default.Key
        else -> Icons.Default.Explore
    }
}
