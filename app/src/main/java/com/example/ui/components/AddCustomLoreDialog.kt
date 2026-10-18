package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.LoreSiteEntity
import com.example.data.model.ConfidenceLevel
import com.example.data.model.LoreCategories
import com.example.data.model.LoreCounties
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCustomLoreDialog(
    currentGpsLat: Double,
    currentGpsLng: Double,
    onDismiss: () -> Unit,
    onSaveSite: (LoreSiteEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LoreCategories.HAUNTED) }
    var selectedCounty by remember { mutableStateOf(LoreCounties.FLOYD) }
    var latitudeText by remember { mutableStateOf(String.format("%.5f", currentGpsLat)) }
    var longitudeText by remember { mutableStateOf(String.format("%.5f", currentGpsLng)) }
    var address by remember { mutableStateOf("") }
    var era by remember { mutableStateOf("19th-Century / Frontier") }
    var fullStory by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("add_custom_lore_dialog"),
            color = SlateNightElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LOG FIELD DISCOVERY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LanternAmberBright
                        )
                        Text(
                            text = "Record an authentic regional landmark or oral lore",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text("Site / Landmark Title *") },
                    placeholder = { Text("e.g., Duncan Tunnel Air Shaft, Old French Settler Grave") },
                    modifier = Modifier.fillMaxWidth().testTag("custom_title_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        focusedBorderColor = LanternAmber,
                        unfocusedBorderColor = SlateBorder,
                        focusedLabelColor = LanternAmber,
                        unfocusedLabelColor = ParchmentMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Theme") },
                    placeholder = { Text("e.g., Hidden Hill Spring & Phantom Whistle Legend") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        focusedBorderColor = LanternAmber,
                        unfocusedBorderColor = SlateBorder,
                        focusedLabelColor = LanternAmber,
                        unfocusedLabelColor = ParchmentMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // County Selection
                Text(
                    text = "COUNTY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LanternAmber
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                ) {
                    LoreCounties.list.filter { it != LoreCounties.ALL }.forEach { county ->
                        FilterChip(
                            selected = selectedCounty == county,
                            onClick = { selectedCounty = county },
                            label = { Text(county, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateNightSurface,
                                labelColor = ParchmentMuted
                            )
                        )
                    }
                }

                // Category Selection
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LanternAmber
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                ) {
                    LoreCategories.list.filter { it != LoreCategories.ALL }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KnobstoneForest,
                                selectedLabelColor = ParchmentPaper,
                                containerColor = SlateNightSurface,
                                labelColor = ParchmentMuted
                            )
                        )
                    }
                }

                // Live S25 Ultra GPS Sync Bar
                Surface(
                    color = SlateNightSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = LanternAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Device Coordinates",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted
                                )
                                Text(
                                    text = "${String.format("%.4f", currentGpsLat)}, ${String.format("%.4f", currentGpsLng)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ParchmentPaper
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                latitudeText = String.format("%.5f", currentGpsLat)
                                longitudeText = String.format("%.5f", currentGpsLng)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use GPS Fix", fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ParchmentPaper,
                            unfocusedTextColor = ParchmentPaper,
                            focusedBorderColor = LanternAmber,
                            unfocusedBorderColor = SlateBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ParchmentPaper,
                            unfocusedTextColor = ParchmentPaper,
                            focusedBorderColor = LanternAmber,
                            unfocusedBorderColor = SlateBorder
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Landmark Description") },
                    placeholder = { Text("e.g., Knobstone Ridge Overlook off SR-111") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        focusedBorderColor = LanternAmber,
                        unfocusedBorderColor = SlateBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullStory,
                    onValueChange = { fullStory = it },
                    label = { Text("Folklore Tradition & Field Notes *") },
                    placeholder = { Text("Document the oral tale, personal encounter, or local historical records...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("custom_story_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        focusedBorderColor = LanternAmber,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field Photo Attachment Section
                Text(
                    text = "FIELD EVIDENCE PHOTO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LanternAmber
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (selectedPhotoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, LanternAmber, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Selected field photo",
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedPhotoUri = null },
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
                            tint = LanternAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Site Photo (Gallery)")
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = GhostCrimson,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please provide a site title."
                            return@Button
                        }
                        if (fullStory.isBlank()) {
                            errorMessage = "Please enter folklore or historical notes."
                            return@Button
                        }
                        val lat = latitudeText.toDoubleOrNull() ?: currentGpsLat
                        val lng = longitudeText.toDoubleOrNull() ?: currentGpsLng
                        val generatedId = "custom_${UUID.randomUUID().toString().take(8)}"

                        val newEntity = LoreSiteEntity(
                            id = generatedId,
                            title = title.trim(),
                            subtitle = if (subtitle.isNotBlank()) subtitle.trim() else "User Field Discovery",
                            category = selectedCategory,
                            county = selectedCounty,
                            latitude = lat,
                            longitude = lng,
                            address = if (address.isNotBlank()) address.trim() else "$selectedCounty, Indiana",
                            era = era,
                            accessStatus = "Public Field Discovery",
                            fullStory = fullStory.trim(),
                            verifiedHistory = "User submitted field entry; not independently verified against county deed books or state archives.",
                            folkloreTradition = fullStory.trim(),
                            confidenceLevel = ConfidenceLevel.UNVERIFIED.name,
                            sources = "User field discovery; pending independent archival verification",
                            sourceNotes = "User Field Journal Entry",
                            verificationGrade = "Not Independently Verified",
                            geofenceRadiusMeters = 80,
                            audioNarration = "You have arrived at ${title.trim()}, documented in $selectedCounty. ${fullStory.take(150)}",
                            isCustom = true,
                            photoUri = selectedPhotoUri?.toString()
                        )
                        onSaveSite(newEntity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_custom_site_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LanternAmber,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE SITE TO FIELD DATABASE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
