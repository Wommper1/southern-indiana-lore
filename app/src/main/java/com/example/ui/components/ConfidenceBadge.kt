package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConfidenceLevel

/**
 * Prominent, standardized Historical Confidence Badge.
 * Makes the archival epistemic status immediately visible on lore cards and detail sheets.
 */
@Composable
fun ConfidenceBadge(
    confidenceLevel: ConfidenceLevel,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (icon, bgAlpha, textColor) = when (confidenceLevel) {
        ConfidenceLevel.OFFICIAL_SOURCE -> Triple(
            Icons.Default.AccountBalance,
            0.18f,
            Color(0xFF22C55E) // Bright Forest Green
        )
        ConfidenceLevel.VERIFIED -> Triple(
            Icons.AutoMirrored.Filled.MenuBook,
            0.18f,
            Color(0xFF38BDF8) // Archival Sky Blue
        )
        ConfidenceLevel.WELL_SUPPORTED -> Triple(
            Icons.AutoMirrored.Filled.FactCheck,
            0.18f,
            Color(0xFF0284C7) // Deep Archival Blue
        )
        ConfidenceLevel.PLAUSIBLE, ConfidenceLevel.PROBABLE -> Triple(
            Icons.AutoMirrored.Filled.FactCheck,
            0.18f,
            Color(0xFF2DD4BF) // Cyan-Teal
        )
        ConfidenceLevel.ORAL_HISTORY -> Triple(
            Icons.Default.RecordVoiceOver,
            0.18f,
            Color(0xFFF59E0B) // Amber
        )
        ConfidenceLevel.FOLKLORE, ConfidenceLevel.LEGEND -> Triple(
            Icons.Default.AutoAwesome,
            0.18f,
            Color(0xFFA855F7) // Folkloric Purple
        )
        ConfidenceLevel.DISPUTED -> Triple(
            Icons.AutoMirrored.Filled.HelpOutline,
            0.18f,
            Color(0xFFC026D3) // Magenta
        )
        ConfidenceLevel.DEBUNKED -> Triple(
            Icons.AutoMirrored.Filled.HelpOutline,
            0.18f,
            Color(0xFFEF4444) // Red
        )
        ConfidenceLevel.UNVERIFIED -> Triple(
            Icons.AutoMirrored.Filled.HelpOutline,
            0.18f,
            Color(0xFF94A3B8) // Slate Grey
        )
    }

    Surface(
        color = textColor.copy(alpha = bgAlpha),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 6.dp else 8.dp,
                vertical = if (isCompact) 2.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
            )
            Text(
                text = confidenceLevel.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 9.sp else 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * Distinct Geographic / Location Confidence Badge.
 * Separates spatial/cartographic certainty (EXACT, GENERALIZED_PRESERVATION, CORRIDOR_APPROXIMATE, TRADITION_ONLY)
 * from historical evidentiary confidence.
 */
@Composable
fun LocationConfidenceBadge(
    siteType: String,
    isSensitiveLocation: Boolean,
    isExactCoordinateSafe: Boolean,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val (label, icon, color) = when {
        isSensitiveLocation || !isExactCoordinateSafe -> Triple(
            "GENERALIZED LOCATION",
            Icons.AutoMirrored.Filled.HelpOutline,
            Color(0xFFEF4444)
        )
        siteType.equals("LINE", ignoreCase = true) -> Triple(
            "CORRIDOR ALIGNMENT",
            Icons.AutoMirrored.Filled.FactCheck,
            Color(0xFF38BDF8)
        )
        siteType.equals("POLYGON", ignoreCase = true) -> Triple(
            "HISTORIC DISTRICT",
            Icons.AutoMirrored.Filled.FactCheck,
            Color(0xFFA855F7)
        )
        else -> Triple(
            "EXACT COORDINATE",
            Icons.Default.AccountBalance,
            Color(0xFF22C55E)
        )
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 6.dp else 8.dp,
                vertical = if (isCompact) 2.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (isCompact) 11.dp else 13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 9.sp else 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = color
            )
        }
    }
}

