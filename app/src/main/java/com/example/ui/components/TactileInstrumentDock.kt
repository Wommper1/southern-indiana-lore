package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LanternLightingState
import com.example.ui.theme.LocalLanternLighting
import com.example.util.AdventureSoundEngine

/**
 * Specification for an instrument token mounted on the brass field dock.
 */
data class InstrumentTokenSpec(
    val index: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String
)

val DEFAULT_INSTRUMENTS = listOf(
    InstrumentTokenSpec(
        index = 0,
        title = "Desk",
        subtitle = "Worktable",
        icon = Icons.Default.Explore,
        testTag = "nav_tab_desk"
    ),
    InstrumentTokenSpec(
        index = 1,
        title = "Radar",
        subtitle = "Acoustics",
        icon = Icons.Default.Radar,
        testTag = "nav_tab_radar"
    ),
    InstrumentTokenSpec(
        index = 2,
        title = "Tour",
        subtitle = "Trail",
        icon = Icons.Default.DirectionsCar,
        testTag = "nav_tab_tour"
    ),
    InstrumentTokenSpec(
        index = 3,
        title = "Library",
        subtitle = "Tomes",
        icon = Icons.Default.LocalLibrary,
        testTag = "nav_tab_library"
    ),
    InstrumentTokenSpec(
        index = 4,
        title = "Relics",
        subtitle = "Rubbings",
        icon = Icons.Default.Straighten,
        testTag = "nav_tab_relic_lab"
    ),
    InstrumentTokenSpec(
        index = 5,
        title = "Pack",
        subtitle = "Trunk",
        icon = Icons.Default.Description,
        testTag = "nav_tab_notes"
    )
)

/**
 * Tactile Antiquarian Brass Instrument Dock.
 * Replaces standard flat navigation bars with an authentic arched expedition console.
 * Features:
 * - Burnished brass bezels, rivet studs, and mahogany housing
 * - Illuminated vacuum-tube filament indicator tracking active tab with spring oscillation
 * - Mechanical switch click sound and haptic feedback
 * - Strict adherence to system navigation bar window insets for bezel-free devices
 */
@Composable
fun TactileInstrumentDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    soundEngine: AdventureSoundEngine?,
    modifier: Modifier = Modifier,
    instruments: List<InstrumentTokenSpec> = DEFAULT_INSTRUMENTS
) {
    val lightingState = LocalLanternLighting.current
    val accentGlow = lightingState.borderBezelHighlight

    Surface(
        color = Color(0xFF140D07),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC89B3C)),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("tactile_instrument_dock")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            // Brass Caliper Scale & Rivets Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Brass rail line
                    drawLine(
                        color = Color(0xFF8C6228),
                        start = Offset(0f, h / 2f),
                        end = Offset(w, h / 2f),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Small mechanical tick marks along the caliper rail
                    val divisions = 24
                    for (i in 0..divisions) {
                        val x = i * (w / divisions)
                        val tickHeight = if (i % 4 == 0) 4.dp.toPx() else 2.dp.toPx()
                        val tickColor = if (i % 4 == 0) Color(0xFFE5B85C) else Color(0xFF5A3B14)
                        drawLine(
                            color = tickColor,
                            start = Offset(x, (h - tickHeight) / 2f),
                            end = Offset(x, (h + tickHeight) / 2f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Interactive Instrument Tokens Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                instruments.forEach { token ->
                    val isSelected = selectedTab == token.index

                    val scaleAnim by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "token_scale"
                    )

                    val tokenGlowAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = 250),
                        label = "token_glow"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .scale(scaleAnim)
                            .testTag(token.testTag)
                            .clickable {
                                if (!isSelected) {
                                    soundEngine?.playBrassToggleSwitch()
                                    onTabSelected(token.index)
                                }
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        // Embossed Brass Bezel Coin Housing the Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFFF3C853),
                                                Color(0xFFC89B3C),
                                                Color(0xFF53310C)
                                            )
                                        )
                                    } else {
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFF2B1A0E),
                                                Color(0xFF1E1208),
                                                Color(0xFF130A04)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFF1C2) else Color(0xFF5A3E1B),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = token.icon,
                                contentDescription = token.title,
                                tint = if (isSelected) Color(0xFF1E1100) else Color(0xFFBCA685),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Period Nomenclature Label
                        Text(
                            text = token.title,
                            fontFamily = CinzelFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFFFDE68A) else Color(0xFF9E8B75),
                            maxLines = 1
                        )

                        // Glowing Vacuum Filament Indicator Pip
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .height(3.dp)
                                .width(if (isSelected) 16.dp else 4.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                accentGlow.copy(alpha = 0.4f),
                                                accentGlow,
                                                accentGlow.copy(alpha = 0.4f)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF2A1B0E),
                                                Color(0xFF3B2513)
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
