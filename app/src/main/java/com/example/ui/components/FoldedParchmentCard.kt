package com.example.ui.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoreSite
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalLanternLighting
import com.example.util.AdventureSoundEngine
import java.util.Locale

/**
 * Physical folded parchment dossier card.
 * Features:
 * - Aged manila file jacket with red twine closure and wax seal
 * - Interactive folded corner / marginalia flap that flips open to reveal secret field notes
 * - Dynamic Blacklight UV reaction: under UV illumination, regular ink recedes and
 *   fluorescent cartographer runes and subterranean data ignite in glowing violet/cyan!
 * - Haptic crease acoustics on fold/unfold transitions
 */
@Composable
fun FoldedParchmentCard(
    site: LoreSite,
    isNarrating: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVisited: () -> Unit,
    onNarrateClick: () -> Unit,
    soundEngine: AdventureSoundEngine?,
    modifier: Modifier = Modifier
) {
    val lightingState = LocalLanternLighting.current
    val isUvActive = lightingState.isUvActive

    var isCornerUnfolded by remember { mutableStateOf(false) }

    val cornerPeelAngle by animateFloatAsState(
        targetValue = if (isCornerUnfolded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "corner_peel"
    )

    // Animated colors reacting to the Lantern Dimmer & UV Mode
    val paperBgColor by animateColorAsState(
        targetValue = lightingState.paperSurfaceColor,
        animationSpec = tween(300),
        label = "paper_bg"
    )

    val inkPrimaryColor by animateColorAsState(
        targetValue = lightingState.standardInkColor,
        animationSpec = tween(300),
        label = "ink_primary"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isUvActive) Color(0xFFC084FC) else Color(0xFFC89B3C),
        animationSpec = tween(300),
        label = "border_col"
    )

    Surface(
        color = paperBgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUvActive) 1.5.dp else 1.2.dp,
            color = borderColor
        ),
        shadowElevation = if (isUvActive) 8.dp else 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("dossier_card_${site.id}")
            .clickable {
                soundEngine?.playParchmentRustle()
                onCardClick()
            }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle paper creasing and corner peel graphics
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Top left folded binder tab shadow
                val tabPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(32.dp.toPx(), 0f)
                    lineTo(24.dp.toPx(), 14.dp.toPx())
                    lineTo(0f, 14.dp.toPx())
                    close()
                }
                drawPath(tabPath, color = if (isUvActive) Color(0xFF2C2245) else Color(0xFFE2D3B8))

                // Manila folder stitched binder line down left margin
                drawLine(
                    color = if (isUvActive) Color(0xFF4C3672) else Color(0xFF9E7B4C),
                    start = Offset(14.dp.toPx(), 16.dp.toPx()),
                    end = Offset(14.dp.toPx(), h - 16.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Top-right corner fold triangle (interactive peel)
                val cornerSize = (18 + 18 * cornerPeelAngle).dp.toPx()
                val foldPath = Path().apply {
                    moveTo(w - cornerSize, 0f)
                    lineTo(w, cornerSize)
                    lineTo(w, 0f)
                    close()
                }
                drawPath(
                    foldPath,
                    color = if (isUvActive) Color(0xFF381E57) else Color(0xFFD3BE9C)
                )

                // Diagonal crease line for corner
                drawLine(
                    color = if (isUvActive) Color(0xFFB388FF) else Color(0xFF785528),
                    start = Offset(w - cornerSize, 0f),
                    end = Offset(w, cornerSize),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)
            ) {
                // Top Meta: Reference Number, Category Badge, and Interactive Corner Peel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Brass eyelet icon
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isUvActive) Color(0xFFC084FC) else Color(0xFFB45309))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DOSSIER NO. ${site.id.takeLast(4).uppercase(Locale.ROOT)} • ${site.era.uppercase(Locale.ROOT)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUvActive) Color(0xFFC4B5FD) else Color(0xFF854D0E)
                        )
                    }

                    // Interactive Corner Peel / Flap Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUvActive) Color(0xFF2E1A47) else Color(0xFFEDE0C7))
                            .clickable {
                                if (isCornerUnfolded) {
                                    soundEngine?.playPaperFold()
                                } else {
                                    soundEngine?.playPaperUnfold()
                                }
                                isCornerUnfolded = !isCornerUnfolded
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCornerUnfolded) "Folded" else "Unfold Flap",
                            fontSize = 10.sp,
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFF78350F)
                        )
                        Icon(
                            imageVector = if (isCornerUnfolded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle marginalia flap",
                            tint = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFF78350F),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dossier Title & Subtitle
                Text(
                    text = site.title,
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 0.3.sp,
                    color = if (isUvActive) Color(0xFFFAF5FF) else Color(0xFF1C1917)
                )

                if (site.subtitle.isNotBlank()) {
                    Text(
                        text = site.subtitle,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = if (isUvActive) Color(0xFFD8B4FE) else Color(0xFF713F12)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Public Story Preview
                val previewText = site.driveStory.ifBlank { site.fullStory }
                Text(
                    text = previewText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = if (isCornerUnfolded) 5 else 2,
                    overflow = TextOverflow.Ellipsis,
                    color = inkPrimaryColor
                )

                // SECRET ULTRAVIOLET CARTOGRAPHER MARGINALIA LAYER
                // Fluoresces when Blacklight UV is active or when the corner is peeled open!
                if (isUvActive || isCornerUnfolded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isUvActive) {
                                        listOf(
                                            Color(0xFF3B1E63),
                                            Color(0xFF281145)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFEDE0C7),
                                            Color(0xFFEDE0C7)
                                        )
                                    }
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = if (isUvActive) Color(0xFFC084FC) else Color(0xFF9E7B4C),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isUvActive) "✦ UV REVEAL: CARTOGRAPHER CIPHER" else "✍️ CURATOR FIELD MARGINALIA",
                                    fontFamily = CinzelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isUvActive) Color(0xFFF0ABFC) else Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (site.isVisited) {
                                    Text(
                                        text = "• [SURVEYED & SEALED]",
                                        fontSize = 9.sp,
                                        color = if (isUvActive) Color(0xFF34D399) else Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Secret notes from the dossier
                            val secretNote = when {
                                site.historyNerdDiscovery.isNotBlank() -> site.historyNerdDiscovery
                                site.whatMayBeUnderground.isNotBlank() -> "Subterranean Scan: ${site.whatMayBeUnderground}"
                                site.folkloreTradition.isNotBlank() -> site.folkloreTradition
                                else -> "Survey Record: Limestone caverns and seasonal springs intersect this coordinate quadrant."
                            }

                            Text(
                                text = secretNote,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 15.sp,
                                color = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFF451A03)
                            )

                            // Historical Coordinates & County Tag
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Station: ${site.county} • GPS: ${String.format(Locale.US, "%.4f°N, %.4f°W", site.latitude, site.longitude)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = if (isUvActive) Color(0xFFA78BFA) else Color(0xFF9A3412)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Tools Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & County pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isUvActive) Color(0xFF2C1945) else Color(0xFFEDE0C7),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, if (isUvActive) Color(0xFFA855F7) else Color(0xFFB45309))
                        ) {
                            Text(
                                text = site.category.uppercase(Locale.ROOT),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUvActive) Color(0xFFE9D5FF) else Color(0xFF78350F),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (site.isVisited) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isUvActive) Color(0xFF0F3926) else Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "VISITED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUvActive) Color(0xFF34D399) else Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Action Icons: Audio Playback, Favorite, Visited Stamp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Audio Narration
                        IconButton(
                            onClick = {
                                soundEngine?.playTactileClick()
                                onNarrateClick()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_narrate_${site.id}")
                        ) {
                            Icon(
                                imageVector = if (isNarrating) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                                contentDescription = if (isNarrating) "Stop Narration" else "Listen to Archival Narration",
                                tint = if (isNarrating) {
                                    if (isUvActive) Color(0xFFF43F5E) else Color(0xFFDC2626)
                                } else {
                                    if (isUvActive) Color(0xFFC084FC) else Color(0xFFB45309)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Visited Stamp
                        IconButton(
                            onClick = {
                                soundEngine?.playWaxStampImpact()
                                onToggleVisited()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_visited_${site.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark as Visited",
                                tint = if (site.isVisited) {
                                    if (isUvActive) Color(0xFF34D399) else Color(0xFF16A34A)
                                } else {
                                    if (isUvActive) Color(0xFF6B5885) else Color(0xFFB7A58E)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Favorite Heart
                        IconButton(
                            onClick = {
                                soundEngine?.playTactileClick()
                                onToggleFavorite()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_favorite_${site.id}")
                        ) {
                            Icon(
                                imageVector = if (site.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (site.isFavorite) {
                                    if (isUvActive) Color(0xFFF43F5E) else Color(0xFFDC2626)
                                } else {
                                    if (isUvActive) Color(0xFF6B5885) else Color(0xFFB7A58E)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
