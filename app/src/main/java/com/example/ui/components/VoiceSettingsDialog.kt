package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import kotlinx.coroutines.flow.MutableStateFlow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.util.AdventureSoundEngine
import com.example.util.NarratorManager
import com.example.util.NarratorPersona

/**
 * Modern dialog enabling real-time audio & voice personalization:
 * - Switch narrator personas with instant audible sample feedback
 * - Fine-tune pitch and cadence independently
 * - Select from device-installed voices
 * - Sound FX toggle & multi-cue acoustic testing
 */
@Composable
fun VoiceSettingsDialog(
    narratorManager: NarratorManager,
    soundEngine: AdventureSoundEngine? = null,
    ambientSoundEngine: com.example.util.AmbientSoundEngine? = null,
    onReplayIntro: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val currentPersona by narratorManager.selectedPersona.collectAsStateWithLifecycle()
    val speechRateState by narratorManager.speechRate.collectAsStateWithLifecycle()
    val pitchState by narratorManager.pitch.collectAsStateWithLifecycle()
    val availableVoices by narratorManager.availableVoices.collectAsStateWithLifecycle()
    val customVoiceName by narratorManager.customVoiceName.collectAsStateWithLifecycle()
    val isNeuralActive by narratorManager.isNeuralPipelineActive.collectAsStateWithLifecycle()
    val isAmbientPlaying by (ambientSoundEngine?.isAmbientPlaying ?: MutableStateFlow(false)).collectAsStateWithLifecycle()
    val currentAmbientTheme by (ambientSoundEngine?.ambientTheme ?: MutableStateFlow("NONE")).collectAsStateWithLifecycle()
    val isAmbientDucked by (ambientSoundEngine?.isDucked ?: MutableStateFlow(false)).collectAsStateWithLifecycle()

    var selectedPersona by remember(currentPersona) { mutableStateOf(currentPersona) }
    var speechRate by remember(speechRateState) { mutableFloatStateOf(speechRateState) }
    var pitch by remember(pitchState) { mutableFloatStateOf(pitchState) }
    var narrationDepth by remember { mutableStateOf(narratorManager.narrationDepth.value) }
    var isSoundFxOn by remember { mutableStateOf(soundEngine?.isSoundEnabled ?: true) }
    var showDeviceVoices by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14110E),
        titleContentColor = ParchmentPaper,
        textContentColor = ParchmentPaper,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF332005), CircleShape)
                        .border(1.dp, Color(0xFFE5B358), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = Color(0xFFE5B358),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Acoustic & Voice Dossier",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ParchmentPaper
                    )
                    Text(
                        text = "High-Fidelity Neural Storytelling",
                        fontSize = 11.sp,
                        color = Color(0xFFE5B358)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 0: High-Fidelity Neural Pipeline Status Banner
                Surface(
                    color = Color(0xFF22160C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isNeuralActive) Color(0xFFC8963E) else SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFF10B981), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", color = Color(0xFF10B981), fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HIGH-FIDELITY NEURAL PIPELINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "Robotic & low-quality voices filtered out. Pure natural cadence with auto-ducking.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = ParchmentMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Narration Detail Level
                Text(
                    text = "STORYTELLING DEPTH",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color(0xFFE5B358)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = narrationDepth == "FULL_STORY",
                        onClick = {
                            narrationDepth = "FULL_STORY"
                            soundEngine?.playTactileClick()
                            narratorManager.setNarrationDepth("FULL_STORY")
                        },
                        label = { Text("Deep Full Story", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LanternAmber,
                            selectedLabelColor = Color(0xFF1E1100),
                            containerColor = Color(0xFF1E293B),
                            labelColor = ParchmentPaper
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = narrationDepth == "QUICK_DRIVE",
                        onClick = {
                            narrationDepth = "QUICK_DRIVE"
                            soundEngine?.playTactileClick()
                            narratorManager.setNarrationDepth("QUICK_DRIVE")
                        },
                        label = { Text("Quick Drive Alert", fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LanternAmber,
                            selectedLabelColor = Color(0xFF1E1100),
                            containerColor = Color(0xFF1E293B),
                            labelColor = ParchmentPaper
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Personas
                Text(
                    text = "NARRATOR CADENCE & VOICE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = LanternAmberBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap a persona to hear live vocal sample:",
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                NarratorPersona.entries.forEach { persona ->
                    val isSelected = persona == selectedPersona && customVoiceName == null
                    Surface(
                        onClick = {
                            selectedPersona = persona
                            speechRate = persona.baseSpeechRate
                            pitch = persona.basePitch
                            narratorManager.previewPersona(persona, soundEngine)
                        },
                        color = if (isSelected) Color(0xFF261D12) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) LanternAmberBright else SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .testTag("voice_persona_${persona.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) LanternAmber.copy(alpha = 0.25f) else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = persona.iconEmoji,
                                    fontSize = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = if (isSelected) LanternAmberBright else ParchmentPaper
                                )
                                Text(
                                    text = persona.titleSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Device Voices Browser (Optional dropdown if TTS engine has multiple voices)
                if (availableVoices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        onClick = { showDeviceVoices = !showDeviceVoices },
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = LanternAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (customVoiceName != null) "Voice: $customVoiceName" else "Installed Voices (${availableVoices.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (customVoiceName != null) LanternAmberBright else ParchmentPaper
                                )
                            }
                            Icon(
                                imageVector = if (showDeviceVoices) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ParchmentMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (showDeviceVoices) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableVoices.take(12).forEach { voice ->
                                val isVoiceSelected = voice.name == customVoiceName
                                Surface(
                                    onClick = {
                                        narratorManager.setCustomVoice(voice.name)
                                        soundEngine?.playTactileClick()
                                        narratorManager.speak(
                                            siteId = "preview_custom_voice",
                                            title = voice.name,
                                            text = "Testing voice: ${voice.name.substringAfterLast('-')}. Welcome to Southern Indiana."
                                        )
                                    },
                                    color = if (isVoiceSelected) LanternAmber.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isVoiceSelected) LanternAmberBright else SlateBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = voice.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = if (isVoiceSelected) LanternAmberBright else ParchmentPaper,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Pitch & Speed Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vocal Pitch (Tone)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentPaper
                    )
                    Text(
                        text = "${"%.2f".format(pitch)}x",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmberBright
                    )
                }

                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        narratorManager.setPitch(it)
                    },
                    onValueChangeFinished = {
                        soundEngine?.playTactileClick()
                        narratorManager.speak(
                            siteId = "preview_pitch_${System.currentTimeMillis()}",
                            title = "Pitch Test",
                            text = "Tone adjusted to ${"%.2f".format(pitch)}.",
                            forceRestart = true
                        )
                    },
                    valueRange = 0.60f..1.40f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = LanternAmberBright,
                        activeTrackColor = LanternAmber,
                        inactiveTrackColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Narration Pace (Cadence)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ParchmentPaper
                    )
                    Text(
                        text = "${"%.2f".format(speechRate)}x",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmberBright
                    )
                }

                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        narratorManager.setSpeechRate(it)
                    },
                    onValueChangeFinished = {
                        soundEngine?.playTactileClick()
                        narratorManager.speak(
                            siteId = "preview_rate_${System.currentTimeMillis()}",
                            title = "Cadence Test",
                            text = "Pacing adjusted to ${"%.2f".format(speechRate)}.",
                            forceRestart = true
                        )
                    },
                    valueRange = 0.75f..1.30f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = LanternAmberBright,
                        activeTrackColor = LanternAmber,
                        inactiveTrackColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Sound FX Controls & Testing
                if (soundEngine != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = LanternAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Adventure Sound Effects",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ParchmentPaper
                            )
                        }
                        Switch(
                            checked = isSoundFxOn,
                            onCheckedChange = {
                                isSoundFxOn = it
                                soundEngine.isSoundEnabled = it
                                if (it) soundEngine.playSonarPing() else soundEngine.playTactileClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LanternAmber,
                                uncheckedThumbColor = ParchmentMuted,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Test Acoustic Cues:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { soundEngine.playSonarPing() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Radar Ping", fontSize = 10.sp, color = LanternAmberBright)
                        }
                        OutlinedButton(
                            onClick = { soundEngine.playCaveEcho() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cave Echo", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }
                        OutlinedButton(
                            onClick = { soundEngine.playBridgeResonance() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Bridge Chime", fontSize = 10.sp, color = LanternAmber)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { soundEngine.playCompassClick() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Brass Compass", fontSize = 10.sp, color = Color(0xFFF3C853))
                        }
                        OutlinedButton(
                            onClick = { soundEngine.playCameraShutter() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Shutter Click", fontSize = 10.sp, color = ParchmentPaper)
                        }
                        OutlinedButton(
                            onClick = { soundEngine.playPhonographNeedleDrop() },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Phonograph", fontSize = 10.sp, color = LanternAmberBright)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Section 5: Atmospheric Soundscape Mixer
                if (ambientSoundEngine != null) {
                    Text(
                        text = "ATMOSPHERIC SOUNDSCAPE MIXER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color(0xFFE5B358)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAmbientDucked) "Layered drone active (gracefully ducked for narration)" else "Continuous vintage acoustic drone behind exploration:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (isAmbientDucked) Color(0xFFFDE68A) else ParchmentMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val ambientThemes = listOf(
                        Pair("NONE", "Off"),
                        Pair("RIVER", "River Horn"),
                        Pair("RAIL", "Rail Whistle"),
                        Pair("CAVE_KNOBS", "Cave Drone"),
                        Pair("FOREST_WIND", "Forest Wind"),
                        Pair("VINTAGE_RADIO", "Vintage AM")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ambientThemes.take(3).forEach { (themeId, label) ->
                                val isSelected = currentAmbientTheme == themeId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        soundEngine?.playTactileClick()
                                        if (themeId == "NONE") {
                                            ambientSoundEngine.stopAmbient()
                                        } else {
                                            ambientSoundEngine.startAmbient(themeId)
                                        }
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFC8963E),
                                        selectedLabelColor = Color(0xFF1E1100),
                                        containerColor = Color(0xFF1E150D),
                                        labelColor = ParchmentPaper
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ambientThemes.drop(3).forEach { (themeId, label) ->
                                val isSelected = currentAmbientTheme == themeId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        soundEngine?.playTactileClick()
                                        if (themeId == "NONE") {
                                            ambientSoundEngine.stopAmbient()
                                        } else {
                                            ambientSoundEngine.startAmbient(themeId)
                                        }
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFC8963E),
                                        selectedLabelColor = Color(0xFF1E1100),
                                        containerColor = Color(0xFF1E150D),
                                        labelColor = ParchmentPaper
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Sample Voice button
                Button(
                    onClick = {
                        soundEngine?.playTactileClick()
                        narratorManager.speak(
                            siteId = "preview_sample",
                            title = "Preview Voice",
                            text = "You are listening to ${selectedPersona.displayName}. Beneath the Knobs and along the Ohio River bluffs, the rich history and subterranean caverns of Southern Indiana await."
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = LanternAmber
                    ),
                    border = BorderStroke(1.dp, LanternAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = LanternAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Full Narration Sample",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmber
                    )
                }

                if (onReplayIntro != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onReplayIntro()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternAmberBright),
                        modifier = Modifier.fillMaxWidth().testTag("replay_dossier_btn")
                    ) {
                        Text(
                            text = "Replay Dossier Opening Briefing",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LanternAmber,
                    contentColor = Color(0xFF1E1100)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
