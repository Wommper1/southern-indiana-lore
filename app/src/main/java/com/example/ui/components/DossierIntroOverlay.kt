package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GildedGold
import com.example.ui.theme.GildedGoldBright
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.LeatherBurgundy
import com.example.ui.theme.ParchmentDossierBg
import com.example.ui.theme.ParchmentDossierCard
import com.example.ui.theme.ParchmentInkDark
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.ShelfWoodDark
import com.example.ui.theme.ShelfWoodPlank
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.StampInkRed
import com.example.ui.theme.StampWaxGold
import com.example.util.AdventureSoundEngine
import com.example.util.NarratorManager
import kotlinx.coroutines.delay

/**
 * Animated Leather Dossier Opening Sequence:
 * Presented on cold start. Displays an authentic 19th-century leather portfolio with
 * brass corner brackets, deep gold debossing, an interactive wax seal clasp, and
 * smoothly unfolds aged parchment folios accompanied by tactile haptics, parchment audio,
 * and high-fidelity spoken documentary narration.
 */
@Composable
fun DossierIntroOverlay(
    soundEngine: AdventureSoundEngine,
    narratorManager: NarratorManager,
    onBeginExpedition: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Sealed Leather Cover, 1 = Wax Seal Snapping, 2 = Parchment Unfolding, 3 = Briefing Ready
    BackHandler { onBeginExpedition() }
    var introPhase by remember { mutableIntStateOf(0) }
    // Narration is opt-in: the briefing stays silent until the user taps the speaker icon, so the
    // app never starts talking on its own the moment it opens.
    var isNarratorMuted by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "wax_seal_pulse")
    val sealGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "seal_glow"
    )

    // Progression Orchestration
    LaunchedEffect(Unit) {
        // Phase 0: Sealed leather portfolio with subtle ambient click
        soundEngine.playTactileClick()
        delay(700)

        // Phase 1: Heavy brass buckle unclasp and wax seal fracture
        introPhase = 1
        soundEngine.playLeatherUnclasp()
        delay(350)
        soundEngine.playWaxSealBreak()
        delay(600)

        // Phase 2: Parchment folios unfold
        introPhase = 2
        soundEngine.playParchmentRustle()
        delay(700)

        // Phase 3: Expedition Briefing active & Voice Narration begins
        introPhase = 3
        if (!isNarratorMuted) {
            narratorManager.speak(
                siteId = "intro_expedition_briefing",
                title = "Southern Indiana Expedition Briefing",
                text = "Welcome, explorer. Beyond the Ohio River bluffs lie forgotten subterranean tunnels, abolitionist belfry signals, and the unrecovered gold of the frontier. Your field dossier is unsealed. Let the expedition begin."
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            narratorManager.stop()
        }
    }

    // Unfolding folio 3D rotation animation
    val folioRotationY by animateFloatAsState(
        targetValue = if (introPhase >= 2) 0f else -85f,
        animationSpec = tween(700, easing = LinearOutSlowInEasing),
        label = "folio_rotation"
    )

    val folioAlpha by animateFloatAsState(
        targetValue = if (introPhase >= 2) 1f else 0f,
        animationSpec = tween(500),
        label = "folio_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateNight.copy(alpha = 0.96f))
            .testTag("dossier_intro_overlay"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding() // overlay sits above the whole app, so clear the system bars
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val isCompact = maxWidth < 400.dp

            if (introPhase < 2) {
                // --- PHASE 0 & 1: Sealed Leather Dossier Portfolio Cover ---
                SealedLeatherDossierCover(
                    isSnapping = introPhase == 1,
                    glowAlpha = sealGlowAlpha,
                    onBreakSealManually = {
                        if (introPhase == 0) {
                            introPhase = 1
                            soundEngine.playLeatherUnclasp()
                            soundEngine.playWaxSealBreak()
                        }
                    },
                    onSkip = {
                        soundEngine.playTactileClick()
                        onBeginExpedition()
                    }
                )
            } else {
                // --- PHASE 2 & 3: Opened Parchment Dossier Folio ---
                OpenedParchmentDossier(
                    rotationY = folioRotationY,
                    alpha = folioAlpha,
                    isNarratorMuted = isNarratorMuted,
                    isCompact = isCompact,
                    onToggleNarration = {
                        isNarratorMuted = !isNarratorMuted
                        soundEngine.playTactileClick()
                        if (isNarratorMuted) {
                            narratorManager.stop()
                        } else {
                            narratorManager.speak(
                                siteId = "intro_expedition_briefing",
                                title = "Southern Indiana Expedition Briefing",
                                text = "Welcome, explorer. Beyond the Ohio River bluffs lie forgotten subterranean tunnels, abolitionist belfry signals, and the unrecovered gold of the frontier. Your field dossier is unsealed. Let the expedition begin."
                            )
                        }
                    },
                    onBegin = {
                        soundEngine.playDiscoveryChime()
                        onBeginExpedition()
                    },
                    onSkip = {
                        soundEngine.playTactileClick()
                        onBeginExpedition()
                    }
                )
            }
        }
    }
}

/**
 * Exterior of the 1800s leather-bound survey dossier with brass corner plates
 * and authentic red wax seal.
 */
@Composable
private fun SealedLeatherDossierCover(
    isSnapping: Boolean,
    glowAlpha: Float,
    onBreakSealManually: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .shadow(24.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onBreakSealManually)
            .testTag("sealed_dossier_cover"),
        shape = RoundedCornerShape(20.dp),
        color = LeatherBurgundy,
        border = BorderStroke(2.dp, GildedGold.copy(alpha = 0.7f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Antique Leather Surface Texture
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Inner embossed border line
                drawRoundRect(
                    color = Color(0xFF2B0B0E),
                    size = androidx.compose.ui.geometry.Size(w - 24.dp.toPx(), h - 24.dp.toPx()),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Gold foil inner accent thread
                drawRoundRect(
                    color = GildedGold.copy(alpha = 0.45f),
                    size = androidx.compose.ui.geometry.Size(w - 36.dp.toPx(), h - 36.dp.toPx()),
                    topLeft = Offset(18.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Brass Corner Braces (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
                val braceLen = 28.dp.toPx()
                val braceW = 3.dp.toPx()
                val brass = Color(0xFFE5C07B)

                // Top-Left
                drawLine(brass, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx() + braceLen, 16.dp.toPx()), braceW, StrokeCap.Round)
                drawLine(brass, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx() + braceLen), braceW, StrokeCap.Round)

                // Top-Right
                drawLine(brass, Offset(w - 16.dp.toPx(), 16.dp.toPx()), Offset(w - 16.dp.toPx() - braceLen, 16.dp.toPx()), braceW, StrokeCap.Round)
                drawLine(brass, Offset(w - 16.dp.toPx(), 16.dp.toPx()), Offset(w - 16.dp.toPx(), 16.dp.toPx() + braceLen), braceW, StrokeCap.Round)

                // Bottom-Left
                drawLine(brass, Offset(16.dp.toPx(), h - 16.dp.toPx()), Offset(16.dp.toPx() + braceLen, h - 16.dp.toPx()), braceW, StrokeCap.Round)
                drawLine(brass, Offset(16.dp.toPx(), h - 16.dp.toPx()), Offset(16.dp.toPx(), h - 16.dp.toPx() - braceLen), braceW, StrokeCap.Round)

                // Bottom-Right
                drawLine(brass, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx() - braceLen, h - 16.dp.toPx()), braceW, StrokeCap.Round)
                drawLine(brass, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx(), h - 16.dp.toPx() - braceLen), braceW, StrokeCap.Round)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Confidential Top Banner
                Surface(
                    color = Color(0xFF26080B),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "HISTORICAL SURVEY ARCHIVE • CONFIDENTIAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        ),
                        color = GildedGoldBright,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Embossed Title Header
                Text(
                    text = "SOUTHERN INDIANA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GildedGoldBright,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "LORE & EXPEDITION DOSSIER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GildedGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Territorial Karst, Rail Ciphers & Lost Frontier Accounts",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = Color(0xFFD4AF37).copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Wax Seal with Compass Rose Emblem
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFDC2626), StampInkRed, Color(0xFF450A0A))
                            )
                        )
                        .border(2.dp, GildedGold.copy(alpha = glowAlpha), CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val w = size.width
                        val h = size.height
                        val goldColor = GildedGoldBright

                        // Eight-point Compass Rose Star
                        val starPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            lineTo(w * 0.6f, h * 0.4f)
                            lineTo(w, h * 0.5f)
                            lineTo(w * 0.6f, h * 0.6f)
                            lineTo(w * 0.5f, h)
                            lineTo(w * 0.4f, h * 0.6f)
                            lineTo(0f, h * 0.5f)
                            lineTo(w * 0.4f, h * 0.4f)
                            close()
                        }
                        drawPath(starPath, goldColor, style = Stroke(width = 2.dp.toPx()))

                        // Center compass pip
                        drawCircle(goldColor, radius = 5.dp.toPx(), center = Offset(w * 0.5f, h * 0.5f))
                    }

                    if (isSnapping) {
                        Surface(
                            color = Color(0xAAFFFFFF),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isSnapping) "UNCLASPING WAX SEAL..." else "TAP TO UNSEAL DOSSIER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = GildedGoldBright
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Skip Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Skip Briefing ›",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFD4AF37).copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onSkip)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("skip_intro_btn")
                    )
                }
            }
        }
    }
}

/**
 * Interior of the unfolded parchment portfolio with mission briefing,
 * sketch artwork, and the call-to-action button.
 */
@Composable
private fun OpenedParchmentDossier(
    rotationY: Float,
    alpha: Float,
    isNarratorMuted: Boolean,
    isCompact: Boolean,
    onToggleNarration: () -> Unit,
    onBegin: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 540.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                this.alpha = alpha
                cameraDistance = 12f * density
            }
            .shadow(28.dp, RoundedCornerShape(18.dp))
            .testTag("opened_dossier_folio"),
        shape = RoundedCornerShape(18.dp),
        color = ParchmentDossierBg,
        border = BorderStroke(2.dp, Color(0xFFD6CEBF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(if (isCompact) 18.dp else 26.dp)
        ) {
            // Header Bar: Field Dispatch Label + Narration Voice Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = StampWaxGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, StampWaxGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FIELD DISPATCH • EXPEDITION ORDERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp
                            ),
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleNarration,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isNarratorMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isNarratorMuted) "Unmute Audio Briefing" else "Mute Audio Briefing",
                            tint = LanternAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Folio Document Title
            Text(
                text = "EXPEDITION BRIEFING",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = ParchmentInkDark
            )

            Text(
                text = "Surveying the Historical Mysteries of the Ohio River Valley",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF78350F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Archival Sketch Canvas: Ohio River Bend & Frontier Trails
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = Color(0xFFEFE8D8),
                border = BorderStroke(1.dp, Color(0xFFD6CEBF))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        val w = size.width
                        val h = size.height
                        val ink = Color(0xFF334155)
                        val stroke = 1.6.dp.toPx()

                        // River bend wavy line
                        val river = Path().apply {
                            moveTo(0f, h * 0.75f)
                            cubicTo(w * 0.3f, h * 0.9f, w * 0.6f, h * 0.4f, w, h * 0.65f)
                        }
                        drawPath(river, Color(0xFF0284C7).copy(alpha = 0.5f), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

                        // Historic Knobstone Ridge mountain peaks
                        val ridge = Path().apply {
                            moveTo(w * 0.1f, h * 0.45f)
                            lineTo(w * 0.22f, h * 0.15f)
                            lineTo(w * 0.35f, h * 0.45f)
                            lineTo(w * 0.48f, h * 0.18f)
                            lineTo(w * 0.60f, h * 0.45f)
                        }
                        drawPath(ridge, ink, style = Stroke(width = stroke))

                        // Buffalo Trace dashed line
                        drawLine(
                            color = Color(0xFF92400E),
                            start = Offset(w * 0.15f, h * 0.85f),
                            end = Offset(w * 0.85f, h * 0.25f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    Text(
                        text = "FIG. 1: TERRAIN RECONNAISSANCE MAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Handwritten-Style Field Narrative
            Surface(
                color = Color(0xFFF6F0E2),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2DAC8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "“Beyond the Ohio River bluffs lie forgotten subterranean tunnels, abolitionist belfry signals, and the unrecovered gold of the frontier. Your field dossier is unsealed. Let the expedition begin.”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // The label takes whatever width is left and may wrap; the stamp below keeps
                        // its natural width on a single line (narrow phones used to squeeze the
                        // stamp into a one-letter column).
                        Text(
                            text = "OFFICIAL RECORD • DEPT. OF FIELD EXPLORATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF78350F),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        // Red Archival Stamped Mark
                        Surface(
                            color = StampInkRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, StampInkRed)
                        ) {
                            Text(
                                text = "STAMPED & VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp
                                ),
                                maxLines = 1,
                                softWrap = false,
                                color = StampInkRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action: Begin Expedition
            Button(
                onClick = onBegin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("begin_expedition_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShelfWoodDark,
                    contentColor = GildedGoldBright
                ),
                border = BorderStroke(1.5.dp, GildedGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GildedGoldBright
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BEGIN EXPEDITION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Skip Button
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("dismiss_intro_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF9CA3AF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563))
            ) {
                Text(
                    text = "Enter Field Directory Directly",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
