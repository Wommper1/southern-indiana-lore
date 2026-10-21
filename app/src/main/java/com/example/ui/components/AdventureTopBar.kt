package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.LanternLightingMode
import com.example.ui.theme.LanternLightingState
import com.example.ui.theme.LocalLanternLighting
import com.example.util.AdventureSoundEngine

/**
 * Compact top bar. The bar itself only carries what a driver needs at a glance - the title/stats
 * and the live GPS status. Everything else lives in the "More" overflow menu.
 *
 * Layout rule: nothing here may depend on the screen being wide. Phones with a larger display-size
 * or font-scale setting are ~360dp wide, and an over-full Row squeezes any text beside fixed-size
 * icons down to a one-letter column, which makes the bar hundreds of dp tall. So: the title takes
 * the leftover width (weight) and ellipsizes, and the secondary controls are in a menu.
 */
@Composable
fun AdventureTopBar(
    totalSitesCount: Int,
    visitedCount: Int,
    favoriteCount: Int,
    isTourActive: Boolean,
    isLiveGpsActive: Boolean,
    isSoundFxEnabled: Boolean,
    onToggleSoundFx: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onOpenFieldJournal: (() -> Unit)? = null,
    onOpenViewfinder: (() -> Unit)? = null,
    onOpenPhonographRadio: (() -> Unit)? = null,
    onOpenRelicLab: (() -> Unit)? = null,
    lightingState: LanternLightingState = LocalLanternLighting.current,
    onLightingModeChanged: (LanternLightingMode) -> Unit = {},
    isPhonographPlaying: Boolean = false,
    soundEngine: AdventureSoundEngine? = null,
    modifier: Modifier = Modifier
) {
    var isStatsExpanded by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF14110E),
        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFC8963E)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("adventure_top_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .animateContentSize()
        ) {
            // Main compact row: [emblem + title (takes leftover width)] [GPS pill] [More]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            soundEngine?.playTactileClick()
                            isStatsExpanded = !isStatsExpanded
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LanternAmber.copy(alpha = 0.2f))
                            .border(1.dp, LanternAmberBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SOUTHERN INDIANA LORE",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = ParchmentPaper,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = if (isStatsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle stats",
                                tint = ParchmentMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isTourActive) "Active Driving Tour • Proximity" else "$totalSitesCount Sites • $visitedCount Visited",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isTourActive) KnobstoneForest else LanternAmberBright
                        )
                    }
                }

                // Live GPS status pill
                Surface(
                    color = if (isLiveGpsActive) KnobstoneForest.copy(alpha = 0.15f) else LanternAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLiveGpsActive) KnobstoneForest else LanternAmber.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiveGpsActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                            contentDescription = null,
                            tint = if (isLiveGpsActive) KnobstoneForest else LanternAmberBright,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isLiveGpsActive) "GPS" else "SIM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            maxLines = 1,
                            color = if (isLiveGpsActive) KnobstoneForest else LanternAmberBright
                        )
                    }
                }

                // More menu: everything that used to crowd the bar
                Box {
                    IconButton(
                        onClick = {
                            soundEngine?.playTactileClick()
                            isMenuOpen = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("topbar_more_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = LanternAmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                        containerColor = Color(0xFF1B1611),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8963E).copy(alpha = 0.6f))
                    ) {
                        TopBarMenuItem(
                            icon = Icons.Default.RecordVoiceOver,
                            label = "Narrator voice & speed",
                            tint = LanternAmber,
                            tag = "topbar_voice_btn"
                        ) {
                            isMenuOpen = false
                            soundEngine?.playTactileClick()
                            onOpenVoiceSettings()
                        }
                        TopBarMenuItem(
                            icon = if (isSoundFxEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            label = if (isSoundFxEnabled) "Sound effects: on" else "Sound effects: off",
                            tint = if (isSoundFxEnabled) LanternAmberBright else ParchmentMuted,
                            tag = "topbar_sound_btn"
                        ) {
                            soundEngine?.playSonarPing()
                            onToggleSoundFx()
                        }
                        if (onOpenFieldJournal != null) {
                            TopBarMenuItem(
                                icon = Icons.Default.Book,
                                label = "Field journal",
                                tint = LanternAmberBright,
                                tag = "topbar_journal_btn"
                            ) {
                                isMenuOpen = false
                                soundEngine?.playParchmentRustle()
                                onOpenFieldJournal()
                            }
                        }
                        if (onOpenViewfinder != null) {
                            TopBarMenuItem(
                                icon = Icons.Default.PhotoCamera,
                                label = "Viewfinder",
                                tint = Color(0xFF38BDF8),
                                tag = "topbar_viewfinder_btn"
                            ) {
                                isMenuOpen = false
                                soundEngine?.playTactileClick()
                                onOpenViewfinder()
                            }
                        }
                        if (onOpenPhonographRadio != null) {
                            TopBarMenuItem(
                                icon = Icons.Default.Radio,
                                label = "Phonograph radio",
                                tint = if (isPhonographPlaying) LanternAmberBright else GildedGold,
                                tag = "topbar_phonograph_btn"
                            ) {
                                isMenuOpen = false
                                soundEngine?.playTactileClick()
                                onOpenPhonographRadio()
                            }
                        }
                        if (onOpenRelicLab != null) {
                            TopBarMenuItem(
                                icon = Icons.Default.Straighten,
                                label = "Relic lab",
                                tint = GildedGoldBright,
                                tag = "topbar_relic_lab_btn"
                            ) {
                                isMenuOpen = false
                                soundEngine?.playWaxStampImpact()
                                onOpenRelicLab()
                            }
                        }
                        // Lantern dial keeps its own tap/drag handling inside the menu.
                        DropdownMenuItem(
                            text = {
                                RotaryLanternDial(
                                    lightingState = lightingState,
                                    onLightingModeChanged = onLightingModeChanged,
                                    soundEngine = soundEngine
                                )
                            },
                            onClick = {}
                        )
                    }
                }
            }

            // Collapsible stats row (only shown when expanded)
            AnimatedVisibility(
                visible = isStatsExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadge(
                            icon = Icons.Default.Explore,
                            label = "$totalSitesCount Total",
                            tint = LanternAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            icon = Icons.Default.CheckCircle,
                            label = "$visitedCount Visited",
                            tint = KnobstoneForest,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            icon = Icons.Default.Favorite,
                            label = "$favoriteCount Saved",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            icon = Icons.Default.CheckCircle,
                            label = "v1.0.1",
                            tint = GildedGoldBright,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = label, color = ParchmentPaper, fontSize = 14.sp, maxLines = 1) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        },
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    )
}

@Composable
private fun StatBadge(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                color = ParchmentPaper
            )
        }
    }
}
