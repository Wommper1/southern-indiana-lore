package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.LoreSite
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface

@Composable
fun SiteDetailDialog(
    site: LoreSite,
    isCurrentlyNarrating: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVisited: () -> Unit,
    onNarrateClick: () -> Unit,
    onSaveUserNotes: (String) -> Unit,
    onAttachPhoto: ((String?) -> Unit)? = null,
    onAskArchivist: ((String) -> Unit)? = null,
    onDeleteCustomSite: (() -> Unit)? = null,
    onOpenInLibrary: (() -> Unit)? = null,
    onOpenViewfinder: (() -> Unit)? = null,
    onRecordVoiceMemo: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var userNotesText by remember(site.id) { mutableStateOf(site.userNotes) }
    var hasNotesChanged by remember(site.id) { mutableStateOf(false) }
    var currentPhotoUri by remember(site.id, site.photoUri) { mutableStateOf(site.photoUri) }
    var thenNowSplitRatio by remember(site.id) { mutableStateOf(0.5f) }
    var isComparingThenNow by remember(site.id) { mutableStateOf(false) }
    var selected3DLayer by remember(site.id) { mutableStateOf("SURFACE") }
    var selectedTimeLayer by remember(site.id) { mutableStateOf("TODAY") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val uriString = uri?.toString()
        currentPhotoUri = uriString
        onAttachPhoto?.invoke(uriString)
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                val updated = if (userNotesText.isBlank()) recognizedText else "$userNotesText\n$recognizedText"
                userNotesText = updated
                hasNotesChanged = true
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 750.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("site_detail_dialog"),
            color = SlateNightElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar: Close & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Proximity Historian Radar Badge
                        val (radarBadge, badgeColor) = when (site.radarType.uppercase()) {
                            "UNDERGROUND" -> Pair("🕳️ BENEATH YOU", Color(0xFF06B6D4))
                            "BRIDGE" -> Pair("🌉 HISTORIC BRIDGE SPAN", Color(0xFFF59E0B))
                            "LOST_LANDSCAPE" -> Pair("🚂 LOST LANDSCAPE", Color(0xFFF97316))
                            "RIVER_MEMORY" -> Pair("🌊 RIVER MEMORY", Color(0xFF3B82F6))
                            "ROAD_DETECTIVE" -> Pair("🛣️ ROAD DETECTIVE", Color(0xFFEAB308))
                            "FOLKLORE" -> Pair("👻 LOCAL LORE", Color(0xFFA855F7))
                            "CROSSING" -> Pair("⚡ CROSSING", Color(0xFFEC4899))
                            else -> Pair("📍 HISTORY NEARBY", LanternAmber)
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
                                    fontSize = 10.sp
                                ),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        ConfidenceBadge(confidenceLevel = site.confidenceLevel)

                        LocationConfidenceBadge(
                            siteType = site.siteType,
                            isSensitiveLocation = site.isSensitiveLocation,
                            isExactCoordinateSafe = site.isExactCoordinateSafe
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close detail view",
                            tint = ParchmentMuted
                        )
                    }
                }

                // Sensitive Location Protection Banner
                if (site.isSensitiveLocation || !site.isExactCoordinateSafe) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0x22DC2626),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SENSITIVE LOCATION PROTECTION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Coordinates are generalized to preserve fragile karst caves, archaeological deposits, and landowner privacy. Preservation takes precedence over curiosity.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = ParchmentPaper
                                )
                            }
                        }
                    }
                }

                // Proximity Historian: "You Just Crossed..." Line Corridor Alert
                if (site.youJustCrossedEvent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0x2638BDF8),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛣️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "YOU JUST CROSSED...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = site.youJustCrossedEvent,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = ParchmentPaper
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = site.title,
                    fontFamily = com.example.ui.theme.CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = ParchmentPaper
                )

                Text(
                    text = site.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LanternAmberBright
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location & Era Meta Box
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LanternAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = site.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentPaper
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Era: ${site.era}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                            val geomLabel = when (site.siteType.uppercase()) {
                                "LINE" -> "〰️ Linear Corridor"
                                "POLYGON" -> "⬡ Historic Area"
                                else -> "📍 Point Landmark"
                            }
                            Text(
                                text = geomLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = LanternAmberBright
                            )
                        }
                        if (site.encounterCount > 0 || site.hasListenedAudio) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🧭 Field Encounters: ${site.encounterCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF34D399)
                                )
                                Text(
                                    text = if (site.hasListenedAudio) "🎧 Audio Logged" else "Geofence: ${site.geofenceRadiusMeters}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (site.hasListenedAudio) Color(0xFF60A5FA) else Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Narrator & Driving Navigation Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNarrateClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dialog_narrate_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentlyNarrating) Color(0xFFDC2626) else LanternAmber,
                            contentColor = if (isCurrentlyNarrating) Color.White else Color(0xFF1E1100)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyNarrating) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrentlyNarrating) "Stop Audio" else "Listen to Audio",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${site.latitude},${site.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${site.latitude},${site.longitude}")
                                    )
                                    context.startActivity(webIntent)
                                }
                            } catch (_: Exception) {
                                try {
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${site.latitude},${site.longitude}")
                                    )
                                    context.startActivity(webIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open map navigation", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmberBright),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmberBright)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (onOpenInLibrary != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenInLibrary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_open_in_library_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF3C853)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalLibrary,
                            contentDescription = null,
                            tint = Color(0xFFF3C853),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Read Leather Tome in Athenaeum Library",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (onOpenViewfinder != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenViewfinder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_open_viewfinder_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F2B3B),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inspect with AR Viewfinder HUD",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (onRecordVoiceMemo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRecordVoiceMemo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_record_voice_memo_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF261D15),
                            contentColor = Color(0xFFD4AF37)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB89047))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Oral History / EVP at this Site",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Epistemic Status & Historical Confidence Card
                val confidenceColor = when (site.confidenceLevel) {
                    com.example.data.model.ConfidenceLevel.OFFICIAL_SOURCE -> Color(0xFF22C55E)
                    com.example.data.model.ConfidenceLevel.VERIFIED -> Color(0xFF38BDF8)
                    com.example.data.model.ConfidenceLevel.WELL_SUPPORTED -> Color(0xFF0284C7)
                    com.example.data.model.ConfidenceLevel.PLAUSIBLE, com.example.data.model.ConfidenceLevel.PROBABLE -> Color(0xFF2DD4BF)
                    com.example.data.model.ConfidenceLevel.ORAL_HISTORY -> Color(0xFFF59E0B)
                    com.example.data.model.ConfidenceLevel.FOLKLORE, com.example.data.model.ConfidenceLevel.LEGEND -> Color(0xFFA855F7)
                    com.example.data.model.ConfidenceLevel.DISPUTED -> Color(0xFFC026D3)
                    com.example.data.model.ConfidenceLevel.DEBUNKED -> Color(0xFFEF4444)
                    com.example.data.model.ConfidenceLevel.UNVERIFIED -> Color(0xFF94A3B8)
                }

                Surface(
                    color = confidenceColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, confidenceColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = when (site.confidenceLevel) {
                                com.example.data.model.ConfidenceLevel.OFFICIAL_SOURCE -> Icons.Default.CheckCircle
                                com.example.data.model.ConfidenceLevel.VERIFIED -> Icons.AutoMirrored.Filled.MenuBook
                                com.example.data.model.ConfidenceLevel.WELL_SUPPORTED -> Icons.AutoMirrored.Filled.MenuBook
                                com.example.data.model.ConfidenceLevel.PLAUSIBLE, com.example.data.model.ConfidenceLevel.PROBABLE -> Icons.Default.CheckCircle
                                com.example.data.model.ConfidenceLevel.ORAL_HISTORY -> Icons.Default.Mic
                                com.example.data.model.ConfidenceLevel.FOLKLORE, com.example.data.model.ConfidenceLevel.LEGEND -> Icons.Default.AutoAwesome
                                com.example.data.model.ConfidenceLevel.DISPUTED -> Icons.Default.Close
                                com.example.data.model.ConfidenceLevel.DEBUNKED -> Icons.Default.Close
                                com.example.data.model.ConfidenceLevel.UNVERIFIED -> Icons.Default.Close
                            },
                            contentDescription = null,
                            tint = confidenceColor,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "EPISTEMIC CONFIDENCE: ${site.confidenceLevel.label.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = confidenceColor
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = site.confidenceLevel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentPaper
                            )
                            if (site.isCustom) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Origin: User Field Discovery (Not independently corroborated by county deed or landmark archive)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = LanternAmberBright
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Proximity Historian: Drive-By History (1-2 Sentences)
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DRIVE-BY HISTORY (1–2 SENTENCES)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = LanternAmberBright
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (site.driveStory.isNotBlank()) site.driveStory else site.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                lineHeight = 21.sp
                            ),
                            color = ParchmentPaper
                        )
                    }
                }

                // Proximity Historian: Why Did This Exist Here?
                if (site.whyHere.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WHY DID THIS EXIST HERE?",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = site.whyHere,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                        }
                    }
                }

                // Proximity Historian: Three-Dimensional Landscape (Above / Surface / Below)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = KnobstoneForest,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "THREE-DIMENSIONAL LANDSCAPE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = KnobstoneForest
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ABOVE", "SURFACE", "BELOW").forEach { layer ->
                                val isSelected = selected3DLayer == layer
                                Surface(
                                    color = if (isSelected) KnobstoneForest else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selected3DLayer = layer }
                                ) {
                                    Text(
                                        text = layer,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else ParchmentMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val layerContent = when (selected3DLayer) {
                            "ABOVE" -> site.aboveLayer.ifBlank { "Historic structures, former bridges, telegraph lines, or original forest canopy." }
                            "BELOW" -> site.belowLayer.ifBlank { site.whatMayBeUnderground.ifBlank { "Limestone bedrock formations, buried conduits, aquifers, or foundation stone." } }
                            else -> site.surfaceLayer.ifBlank { site.address }
                        }
                        Text(
                            text = layerContent,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = ParchmentPaper
                        )
                    }
                }

                // Proximity Historian: Time Layers (1800, 1850, 1900, 1950, Today)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TIME LAYERS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = LanternAmberBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("1800", "1850", "1900", "1950", "TODAY").forEach { era ->
                                val isSelected = selectedTimeLayer == era
                                Surface(
                                    color = if (isSelected) LanternAmber else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTimeLayer = era }
                                ) {
                                    Text(
                                        text = era,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isSelected) Color(0xFF1E1100) else ParchmentMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val timeDescription = when (selectedTimeLayer) {
                            "1800" -> site.time1800.ifBlank { "Pioneer frontier era: virgin hardwood forest, indigenous traces, and early territorial settlements." }
                            "1850" -> site.time1850.ifBlank { "Antebellum riverboat expansion, plank turnpikes, and timber and grist mills." }
                            "1900" -> site.time1900.ifBlank { "Industrial boom: steam railroads, stone quarries, interurban lines, and bustling county seats." }
                            "1950" -> site.time1950.ifBlank { "Post-war highways, suburban growth, and preservation of historic landmarks." }
                            else -> site.timeToday.ifBlank { "Modern road network and cultural heritage landscape." }
                        }
                        Text(
                            text = timeDescription,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = ParchmentPaper
                        )
                    }
                }

                // Proximity Historian: Under Your Feet (Karst & Subterranean)
                if (site.radarType.equals("UNDERGROUND", ignoreCase = true) || site.whatMayBeUnderground.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0x1806B6D4),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🕳️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UNDER YOUR FEET (SUBTERRANEAN & KARST)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF22D3EE)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val subterraneanText = site.whatMayBeUnderground.ifBlank {
                                "This area sits above highly cavernous limestone terrain with documented subterranean karst conduits and drainage networks."
                            }
                            Text(
                                text = subterraneanText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                            if (site.undergroundConfidence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0x3306B6D4),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SURVEY CONFIDENCE: ${site.undergroundConfidence}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFF67E8F9),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Karst Survey Disclosure: Karst conduit pathways and subterranean hydrology are mapped from geological surveys and spring dye traces. Cave protection regulations apply.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF67E8F9)
                            )
                        }
                    }
                }

                // Proximity Historian: History Nerd Discovery
                if (site.historyNerdDiscovery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0x1DF59E0B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmberBright.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🧠", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HISTORY NERD DISCOVERY",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = LanternAmberBright
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = site.historyNerdDiscovery,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                        }
                    }
                }

                // Proximity Historian: Historical Chain & Connections
                if (site.relatedLocations.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFF131C2E),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HISTORICAL CHAIN & CONNECTIONS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF60A5FA)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = site.relatedLocations,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Documented / Verified History
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DOCUMENTED ARCHIVAL HISTORY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val verifiedText = site.verifiedHistory.ifBlank {
                            if (site.confidenceLevel == com.example.data.model.ConfidenceLevel.LEGEND ||
                                site.confidenceLevel == com.example.data.model.ConfidenceLevel.UNVERIFIED) {
                                "No independent archival record or public deed established for this site. Historical claims remain unverified."
                            } else {
                                site.fullStory
                            }
                        }
                        Text(
                            text = verifiedText,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = ParchmentPaper
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Folklore / Oral Tradition / Supernatural Claims
                if (site.folkloreTradition.isNotBlank() ||
                    site.confidenceLevel == com.example.data.model.ConfidenceLevel.LEGEND ||
                    site.confidenceLevel == com.example.data.model.ConfidenceLevel.ORAL_HISTORY) {

                    Surface(
                        color = Color(0x18A855F7),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44A855F7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FOLKLORE & ORAL TRADITION",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFA855F7)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Preserved as regional cultural heritage, community memory, or folklore. Not established as archival fact.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = ParchmentMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val folkText = site.folkloreTradition.ifBlank { site.fullStory }
                            Text(
                                text = folkText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Section 3: Archival Sources & Historical Citations
                Surface(
                    color = SlateNight,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRIMARY SOURCES & CITATIONS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val sourcesText = buildString {
                            if (site.sources.isNotBlank()) {
                                append(site.sources)
                            }
                            if (site.sourceNotes.isNotBlank() && site.sourceNotes != site.sources) {
                                if (isNotEmpty()) append("\n\nNotes: ")
                                append(site.sourceNotes)
                            }
                        }.ifBlank {
                            "No primary sources or archival deeds cited. Documented from oral or field testimony."
                        }
                        Text(
                            text = sourcesText,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field Journal & Personal Notes (Persisted to Room)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FIELD JOURNAL & DISCOVERY NOTES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LanternAmber
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LanternAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate field observation memo...")
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Voice dictation not available on device", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Dictate Note",
                                tint = LanternAmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice Memo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LanternAmberBright
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = userNotesText,
                    onValueChange = {
                        userNotesText = it
                        hasNotesChanged = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_notes_field"),
                    placeholder = {
                        Text(
                            "Record personal observations, timestamps, EVP captures, or field visit notes...",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted.copy(alpha = 0.6f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LanternAmberBright,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        cursorColor = LanternAmber
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                if (hasNotesChanged) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSaveUserNotes(userNotesText)
                            hasNotesChanged = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KnobstoneForest),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_notes_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Note", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field Photo & Then & Now Comparison Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FIELD EVIDENCE & THEN/NOW ARCHIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LanternAmber
                    )

                    if (!currentPhotoUri.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isComparingThenNow) LanternAmber.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isComparingThenNow) LanternAmber else SlateBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isComparingThenNow = !isComparingThenNow }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = null,
                                    tint = if (isComparingThenNow) LanternAmberBright else ParchmentMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isComparingThenNow) "Split View ON" else "Then vs Now",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isComparingThenNow) LanternAmberBright else ParchmentPaper
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (!currentPhotoUri.isNullOrBlank()) {
                    if (isComparingThenNow) {
                        // Interactive Then & Now Slider View
                        Surface(
                            color = SlateNight,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // "Then" historical representation (archival rendering)
                                    Box(
                                        modifier = Modifier
                                            .weight(thenNowSplitRatio.coerceIn(0.15f, 0.85f))
                                            .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                                            .background(Color(0xFF2C2416))
                                            .border(1.dp, Color(0xFF785C37)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "THEN (${site.era.take(12)})",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFFDE68A)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Historic Record: ${site.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFD7CCC8),
                                                maxLines = 3
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Archival Plate",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFA1887F)
                                            )
                                        }
                                    }

                                    // "Now" user field photo
                                    Box(
                                        modifier = Modifier
                                            .weight((1f - thenNowSplitRatio).coerceIn(0.15f, 0.85f))
                                            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                                            .border(1.dp, LanternAmber.copy(alpha = 0.5f))
                                    ) {
                                        AsyncImage(
                                            model = currentPhotoUri,
                                            contentDescription = "Current evidence photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(bottomStart = 6.dp),
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Text(
                                                text = "NOW (FIELD)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = LanternAmberBright,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Slider controller
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "THEN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFDE68A)
                                    )
                                    Slider(
                                        value = thenNowSplitRatio,
                                        onValueChange = { thenNowSplitRatio = it },
                                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = LanternAmberBright,
                                            activeTrackColor = LanternAmber,
                                            inactiveTrackColor = SlateBorder
                                        )
                                    )
                                    Text(
                                        text = "NOW",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LanternAmberBright
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard single photo view with action buttons
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, LanternAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = currentPhotoUri,
                                contentDescription = "Site photo",
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    currentPhotoUri = null
                                    onAttachPhoto?.invoke(null)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove photo",
                                    tint = GhostCrimson
                                )
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentPaper)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = LanternAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Field Photo (Gallery)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Ask Archivist & Share Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onAskArchivist != null) {
                        Button(
                            onClick = {
                                onAskArchivist("Tell me more about the history, folklore, and primary sources behind ${site.title}.")
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LanternAmber,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ask Archivist",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val shareText = """
                                📜 Southern Indiana Lore Field Report
                                Site: ${site.title} (${site.county})
                                Era: ${site.era}
                                Coordinates: ${site.latitude}, ${site.longitude}
                                Access: ${site.accessStatus}

                                Historical Lore:
                                ${site.fullStory}

                                Verified History:
                                ${site.verifiedHistory}

                                User Notes:
                                ${if (userNotesText.isNotBlank()) userNotesText else "None recorded"}
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Field Report"))
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Report",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (site.isCustom && onDeleteCustomSite != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onDeleteCustomSite()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhostCrimson.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Custom Discovery", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Quick Action Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Visited Toggle
                    Surface(
                        onClick = onToggleVisited,
                        shape = RoundedCornerShape(12.dp),
                        color = if (site.isVisited) KnobstoneForest.copy(alpha = 0.2f) else SlateNight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (site.isVisited) KnobstoneForest else SlateBorder
                        ),
                        modifier = Modifier.testTag("dialog_visited_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (site.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (site.isVisited) KnobstoneForest else ParchmentMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (site.isVisited) "Visited in Field" else "Mark as Visited",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (site.isVisited) KnobstoneForest else ParchmentPaper
                            )
                        }
                    }

                    // Favorite Toggle
                    Surface(
                        onClick = onToggleFavorite,
                        shape = RoundedCornerShape(12.dp),
                        color = if (site.isFavorite) GhostCrimson.copy(alpha = 0.2f) else SlateNight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (site.isFavorite) GhostCrimson else SlateBorder
                        ),
                        modifier = Modifier.testTag("dialog_fav_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (site.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (site.isFavorite) GhostCrimson else ParchmentMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (site.isFavorite) "Saved" else "Save Site",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (site.isFavorite) GhostCrimson else ParchmentPaper
                            )
                        }
                    }
                }
            }
        }
    }
}
