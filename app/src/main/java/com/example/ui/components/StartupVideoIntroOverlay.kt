package com.example.ui.components

import android.content.res.AssetFileDescriptor
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateNight
import kotlinx.coroutines.delay

/**
 * High-performance, fail-safe Startup Video Intro Experience.
 * Renders the atmospheric cinematic MP4 with hardware-accelerated TextureView.
 * If playback fails or is delayed on emulated environments, seamlessly transitions to an
 * elegant animated cinematic prologue without clipping or freezing.
 */
@Composable
fun StartupVideoIntroOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var isVideoPlaying by remember { mutableStateOf(false) }
    var hasVideoFailed by remember { mutableStateOf(false) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceRef by remember { mutableStateOf<Surface?>(null) }

    // Fallback timer: if the hardware video player is silent/stuck for 8s, allow instant continue
    LaunchedEffect(Unit) {
        delay(7000L)
        if (!isVideoPlaying && !hasVideoFailed) {
            hasVideoFailed = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateNight)
                .testTag("startup_video_intro_container")
        ) {
            // Layer 1: Atmospheric hero backdrop with pulse animation
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.hero_southern_indiana),
                    contentDescription = "Southern Indiana Wilderness Backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xD90B0F19),
                                    Color(0x990B0F19),
                                    Color(0xFA090D16)
                                )
                            )
                        )
                )
            }

            // Layer 2: TextureView MediaPlayer for the actual MP4
            if (!hasVideoFailed) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("startup_video_view"),
                    factory = { ctx ->
                        val textureView = TextureView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                                val surface = Surface(surfaceTexture)
                                surfaceRef = surface

                                try {
                                    val mp = MediaPlayer().apply {
                                        setSurface(surface)
                                        setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .build()
                                        )

                                        val afd: AssetFileDescriptor = ctx.resources.openRawResourceFd(R.raw.startup_intro)
                                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                        afd.close()

                                        setOnPreparedListener { player ->
                                            player.isLooping = false
                                            player.start()
                                            isVideoPlaying = true
                                        }
                                        setOnCompletionListener {
                                            onDismiss()
                                        }
                                        setOnErrorListener { _, what, extra ->
                                            Log.w("VideoIntro", "MediaPlayer error: what=$what, extra=$extra")
                                            hasVideoFailed = true
                                            true
                                        }
                                        prepareAsync()
                                    }
                                    mediaPlayerRef = mp
                                } catch (e: Exception) {
                                    Log.w("VideoIntro", "Exception initializing MediaPlayer", e)
                                    hasVideoFailed = true
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                try {
                                    mediaPlayerRef?.let {
                                        if (it.isPlaying) it.stop()
                                        it.release()
                                    }
                                    mediaPlayerRef = null
                                    surfaceRef?.release()
                                    surfaceRef = null
                                } catch (_: Exception) {}
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                        }

                        textureView
                    }
                )
            }

            // Layer 3: If video failed or hardware codec is missing, show cinematic title card overlay
            if (hasVideoFailed || !isVideoPlaying) {
                CinematicPrologueCard(
                    onEnterApp = onDismiss,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Top action bar: Skip button
            Button(
                onClick = {
                    try {
                        mediaPlayerRef?.let {
                            if (it.isPlaying) it.stop()
                            it.release()
                        }
                        mediaPlayerRef = null
                    } catch (_: Exception) {}
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .testTag("startup_video_skip_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC1E293B),
                    contentColor = ParchmentPaper
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Skip Intro",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmber
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip Intro Video",
                        tint = LanternAmber
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayerRef?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
                mediaPlayerRef = null
                surfaceRef?.release()
                surfaceRef = null
            } catch (_: Exception) {}
        }
    }
}

@Composable
private fun CinematicPrologueCard(
    onEnterApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_ring"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Radar Beacon
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension / 2f * 0.7f
                drawCircle(
                    color = LanternAmberBright.copy(alpha = 0.15f),
                    radius = baseRadius * ringPulse,
                    center = center
                )
                drawCircle(
                    color = LanternAmber.copy(alpha = 0.4f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(LanternAmberBright.copy(alpha = 0.25f))
                    .border(2.dp, LanternAmberBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📜",
                    fontSize = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SOUTHERN INDIANA",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = LanternAmberBright,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Lost Landscapes & Local Lore",
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = ParchmentPaper,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Step beyond the highway into the secret history of Floyd County, the Ohio River, underground railroads, and forgotten frontiers.",
            style = MaterialTheme.typography.bodyMedium,
            color = ParchmentMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEnterApp,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LanternAmber,
                contentColor = Color(0xFF1E1100)
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("enter_lore_app_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF1E1100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Begin Exploration",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E1100)
                )
            }
        }
    }
}
