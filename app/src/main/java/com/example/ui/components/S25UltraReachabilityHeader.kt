package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun S25UltraReachabilityHeader(
    totalSitesCount: Int,
    visitedCount: Int,
    favoriteCount: Int,
    isTourActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF080C14)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .testTag("s25_ultra_header")
    ) {
        // Hero Background subtle art
        Image(
            painter = painterResource(id = R.drawable.hero_southern_indiana),
            contentDescription = "Southern Indiana Knobstone Hills and Ohio River",
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.28f
        )

        // Gradient overlay to ensure high contrast text on S25 Ultra Quad HD+ AMOLED
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x770B0F19),
                            Color(0xEE0B0F19),
                            Color(0xFF090D16)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LanternAmber.copy(alpha = 0.2f))
                            .border(1.dp, LanternAmberBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SOUTHERN INDIANA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = LanternAmberBright
                        )
                        Text(
                            text = "Floyd County & Regional Lore",
                            fontFamily = com.example.ui.theme.CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = ParchmentPaper
                        )
                    }
                }

                // S25 Ultra Screen Optimization Indicator
                Surface(
                    color = Color(0x332563EB),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6638BDF8))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isTourActive) Color(0xFF10B981) else LanternAmber)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isTourActive) "TOUR ACTIVE" else "OFFLINE PACK",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reachability Stat Chips (One-UI ergonomic glanceable cards)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatPill(
                    icon = Icons.Default.Explore,
                    label = "$totalSitesCount Sites",
                    tint = LanternAmber
                )
                StatPill(
                    icon = Icons.Default.CheckCircle,
                    label = "$visitedCount Visited",
                    tint = Color(0xFF10B981)
                )
                StatPill(
                    icon = Icons.Default.Favorite,
                    label = "$favoriteCount Saved",
                    tint = Color(0xFFF43F5E)
                )
                StatPill(
                    icon = Icons.Default.MyLocation,
                    label = "Geofence Ready",
                    tint = Color(0xFF38BDF8)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SlateNightElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = ParchmentPaper
            )
        }
    }
}
