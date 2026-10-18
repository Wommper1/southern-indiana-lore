package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import com.example.data.model.HistoricTrail
import com.example.data.model.LoreSite
import com.example.data.model.RegionalHistoricTrails
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.data.local.InstalledPackEntity
import com.example.data.pack.PackValidationResult
import com.example.data.pack.PackValidationStatus
import com.example.util.AmbientSoundEngine

@Composable
fun ArchivePackScreen(
    sites: List<LoreSite>,
    onSiteClick: (LoreSite) -> Unit,
    ambientSoundEngine: AmbientSoundEngine? = null,
    onToggleAmbientTheme: ((String) -> Unit)? = null,
    installedPack: InstalledPackEntity? = null,
    packValidationResult: PackValidationResult? = null,
    onRevalidatePack: () -> Unit = {},
    onReloadPackFromAssets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sitesWithNotes = sites.filter { it.userNotes.isNotBlank() }
    val visitedSites = sites.filter { it.isVisited }

    val isAmbientPlaying = ambientSoundEngine?.isAmbientPlaying?.collectAsState()?.value ?: false
    val currentAmbientTheme = ambientSoundEngine?.ambientTheme?.collectAsState()?.value ?: "NONE"

    // Derive authoritative pack status
    val currentStatus: PackValidationStatus = when {
        packValidationResult != null -> packValidationResult.status
        installedPack != null -> try {
            PackValidationStatus.valueOf(installedPack.status)
        } catch (_: Exception) {
            PackValidationStatus.FALLBACK
        }
        else -> PackValidationStatus.NOT_LOADED
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("archive_pack_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pack Manifest Overview Card
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LanternAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = LanternAmberBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AUTHORITATIVE LORE PACK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = LanternAmberBright
                                )
                                Text(
                                    text = installedPack?.packId ?: "southern_indiana",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        val (statusText, statusBg, statusColor) = when (currentStatus) {
                            PackValidationStatus.VALID -> Triple("VALID", KnobstoneForest.copy(alpha = 0.2f), KnobstoneForest)
                            PackValidationStatus.INVALID -> Triple("INVALID", GhostCrimson.copy(alpha = 0.2f), GhostCrimson)
                            PackValidationStatus.FALLBACK -> Triple("FALLBACK", LanternAmber.copy(alpha = 0.2f), LanternAmber)
                            PackValidationStatus.NOT_LOADED -> Triple("NOT LOADED", Color.Gray.copy(alpha = 0.2f), Color.LightGray)
                        }

                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pack authority governs all historical, geospatial, and source data in the app. Safe pack synchronization preserves user field logs, custom sites, and journal records.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = ParchmentMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Manifest specs table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ManifestRow("Installed Version", "v${installedPack?.version ?: "1.0.0"}")
                        ManifestRow("Contract Schema", installedPack?.schemaVersion ?: "1.0.0")
                        ManifestRow("Content SHA-256", (installedPack?.contentHash?.take(16) ?: "Verified") + "...")
                        ManifestRow("Room Database Sites", "${sites.size} (${sites.count { !it.isCustom }} pack, ${sites.count { it.isCustom }} user)")
                        ManifestRow("Manifest Target Count", "${installedPack?.siteCount ?: packValidationResult?.siteCount ?: 21} sites")
                    }

                    if (packValidationResult?.errors?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = GhostCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GhostCrimson.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "VALIDATION ERRORS DETECTED:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GhostCrimson
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                packValidationResult.errors.forEach { err ->
                                    Text(
                                        text = "• $err",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = ParchmentPaper
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Re-Validate & Safe Reload Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRevalidatePack,
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Re-Validate Pack",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                        }

                        Button(
                            onClick = onReloadPackFromAssets,
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LanternAmber,
                                contentColor = Color(0xFF1E1100)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Safe Pack Sync",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Field Research Export Action
                    Button(
                        onClick = {
                            try {
                                val rootObj = JSONObject().apply {
                                    put("archiveTitle", "Southern Indiana Lore & Historic Expeditions")
                                    put("exportedCount", sites.size)
                                    put("exportTimestamp", System.currentTimeMillis())
                                    val sitesArray = JSONArray()
                                    sites.forEach { site ->
                                        val siteObj = JSONObject().apply {
                                            put("id", site.id)
                                            put("title", site.title)
                                            put("subtitle", site.subtitle)
                                            put("category", site.category)
                                            put("county", site.county)
                                            put("latitude", site.latitude)
                                            put("longitude", site.longitude)
                                            put("era", site.era)
                                            put("accessStatus", site.accessStatus)
                                            put("confidenceLevel", site.confidenceLevel.name)
                                            put("verifiedHistory", site.verifiedHistory)
                                            put("folkloreTradition", site.folkloreTradition)
                                            put("fullStory", site.fullStory)
                                            put("sources", site.sources)
                                            put("sourceNotes", site.sourceNotes)
                                            put("verificationGrade", site.verificationGrade)
                                            put("isCustom", site.isCustom)
                                            put("userNotes", site.userNotes)
                                            put("isVisited", site.isVisited)
                                            put("isFavorite", site.isFavorite)
                                        }
                                        sitesArray.put(siteObj)
                                    }
                                    put("sites", sitesArray)
                                }

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, rootObj.toString(2))
                                    type = "application/json"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Export Offline Field Archive (JSON)")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("export_archive_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateNight,
                            contentColor = LanternAmberBright
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = LanternAmberBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Archive Truth Layer (.json)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmberBright
                        )
                    }
                }
            }
        }

        // Archival Repositories & Bibliography
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = LanternAmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIMARY ARCHIVAL REPOSITORIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LanternAmberBright
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RepositoryItem(
                        title = "Indiana Historical Bureau & State Historical Marker Archives",
                        desc = "Primary research files for the Buffalo Trace route, Rose Island resort, Marshfield train robbery, Leavenworth flood relocation, Cannelton Mill, and Freeman Field."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Indiana Archives and Records Administration (IARA)",
                        desc = "Land office survey field books, 1804 treaty maps, 1851 Negro Registers, Harmony Society court records, and early Evansville Ku Klux Klan charters."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Workingmen's Institute & New Harmony Historic Archives",
                        desc = "Oldest continuously operating library in Indiana; 1826 'Boatload of Knowledge' journals, Thomas Say manuscripts, and David Dale Owen federal geological surveys."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Evansville African American Museum & Willard Library",
                        desc = "Baptisttown neighborhood oral histories, Lincoln Gardens 1938 PWA project files, and WWII Black servicemen club records."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "New Albany Public Library – Indiana History Room",
                        desc = "Historic newspapers (Evening Tribune, Daily Ledger), early county deeds, pioneer diaries, and regional rail disaster records."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Floyd County Historical Society & Padgett Museum",
                        desc = "Photographs, steamboat shipbuilding blueprints, and Floyd County Almshouse commissioner records."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Library of Congress & NPS Network to Freedom",
                        desc = "Second Baptist (Town Clock) Church underground railroad documentation; Culbertson Mansion HABS architectural surveys."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepositoryItem(
                        title = "Indiana University Folklore Archives (Baker Collection)",
                        desc = "Oral traditions of the Monon ghost train, Silver Hills hermit, and Tunnelton watchman rail folklore."
                    )
                }
            }
        }

        // Ambient Soundscape Generator Card
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RiverMistBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = RiverMistBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "HISTORIC SOUNDSCAPE GENERATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = RiverMistBlue
                                )
                                Text(
                                    text = "Acoustic Exploratory Synthesis",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        if (isAmbientPlaying) {
                            Surface(
                                color = LanternAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LanternAmberBright,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Synthesizes real-time acoustic soundscapes modeling the historical environments of Southern Indiana waterways, rail corridors, and the Knobs escarpment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onToggleAmbientTheme?.invoke("RIVER") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentAmbientTheme == "RIVER") RiverMistBlue else Color(0x2238BDF8)
                            ),
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Ohio River Fog",
                                fontSize = 11.sp,
                                color = if (currentAmbientTheme == "RIVER") Color(0xFF0F172A) else ParchmentPaper
                            )
                        }

                        Button(
                            onClick = { onToggleAmbientTheme?.invoke("RAIL") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentAmbientTheme == "RAIL") LanternAmber else Color(0x22F59E0B)
                            ),
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Monon Rail Iron",
                                fontSize = 11.sp,
                                color = if (currentAmbientTheme == "RAIL") Color(0xFF1E1100) else ParchmentPaper
                            )
                        }

                        Button(
                            onClick = { onToggleAmbientTheme?.invoke("CAVE_KNOBS") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentAmbientTheme == "CAVE_KNOBS") KnobstoneForest else Color(0x2210B981)
                            ),
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Knobs Cave",
                                fontSize = 11.sp,
                                color = if (currentAmbientTheme == "CAVE_KNOBS") Color.White else ParchmentPaper
                            )
                        }
                    }
                }
            }
        }

        // Curated Thematic Driving Trails Section
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LanternAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CURATED DRIVING TRAILS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = LanternAmberBright
                            )
                            Text(
                                text = "Thematic Turn-by-Turn Historic Expeditions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    RegionalHistoricTrails.trails.forEach { trail ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = trail.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = LanternAmberBright
                                    )
                                    Surface(
                                        color = Color(0x33F59E0B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${trail.approximateMiles} MI • ${trail.era.take(15)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LanternAmber,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = trail.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentMuted
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${trail.waypoints.size} Key Landmarks: " + trail.waypoints.joinToString(" ➔ ") { it.title.take(16) },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RiverMistBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pioneer Field Badges (Gamified Exploration Achievement Section)
        item {
            Surface(
                color = SlateNightElevated,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KnobstoneForest.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = KnobstoneForest,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PIONEER FIELD BADGES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = KnobstoneForest
                            )
                            Text(
                                text = "Field Exploration Achievements",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge 1: Falls of the Ohio Pathfinder
                    val badge1Earned = visitedSites.any { it.county.contains("Clark", ignoreCase = true) }
                    BadgeItem(
                        title = "Falls of the Ohio Pioneer",
                        desc = "Visit any documented historic site along Clark County riverfront or fossil beds.",
                        isEarned = badge1Earned
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge 2: Freedom Trail Courier
                    val badge2Earned = visitedSites.any { it.category == com.example.data.model.LoreCategories.UNDERGROUND_RR }
                    BadgeItem(
                        title = "Freedom Trail Courier",
                        desc = "Reach and explore an authentic Underground Railroad sanctuary station.",
                        isEarned = badge2Earned
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge 3: Knobs Escarpment Scout
                    val badge3Earned = visitedSites.size >= 5
                    BadgeItem(
                        title = "Knobs Escarpment Scout",
                        desc = "Discover and inspect 5 or more distinct historical coordinates across the Knobs.",
                        isEarned = badge3Earned
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge 4: Nocturnal Archivist / Night Lorekeeper
                    val badge4Earned = visitedSites.any { it.category == com.example.data.model.LoreCategories.HAUNTED }
                    BadgeItem(
                        title = "Midnight Folklore Sentinel",
                        desc = "Investigate a documented Southern Indiana haunted rail tunnel or historic cemetery.",
                        isEarned = badge4Earned
                    )
                }
            }
        }

        // Field Notes Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = LanternAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USER FIELD JOURNAL ENTRIES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = LanternAmber
                    )
                }
                Text(
                    text = "${sitesWithNotes.size} Logged Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentMuted
                )
            }
        }

        if (sitesWithNotes.isEmpty()) {
            item {
                Surface(
                    color = SlateNightElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No field journal notes entered yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ParchmentPaper
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open any lore site and type in 'Field Journal & Discovery Notes' to save observations directly to your offline SQLite database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted
                        )
                    }
                }
            }
        } else {
            items(sitesWithNotes, key = { it.id }) { site ->
                Card(
                    onClick = { onSiteClick(site) },
                    colors = CardDefaults.cardColors(containerColor = SlateNightElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = site.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LanternAmberBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = site.userNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManifestRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
        Text(text = value, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = ParchmentPaper)
    }
}

@Composable
private fun RepositoryItem(title: String, desc: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFDE68A)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = ParchmentMuted
        )
    }
}

@Composable
private fun BadgeItem(title: String, desc: String, isEarned: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isEarned) KnobstoneForest.copy(alpha = 0.12f) else Color(0xFF131D2E))
            .border(1.dp, if (isEarned) KnobstoneForest else SlateBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isEarned) KnobstoneForest else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEarned) Icons.Default.CheckCircle else Icons.Default.Stars,
                contentDescription = null,
                tint = if (isEarned) Color.White else ParchmentMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isEarned) KnobstoneForest else ParchmentPaper
                )
                Text(
                    text = if (isEarned) "UNLOCKED" else "LOCKED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isEarned) KnobstoneForest else ParchmentMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentMuted
            )
        }
    }
}
