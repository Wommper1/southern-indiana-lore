package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Authentic acoustic sound effects engine for Southern Indiana field exploration:
 * - Low-latency SoundPool playback backed by cached WAV waveforms
 * - AudioTrack MODE_STREAM and ToneGenerator fail-safe fallbacks
 * - Sonar Ping, Cave Echo, Bridge Resonance, Discovery Chime, Shutter Clicks,
 *   Parchment Rustle, Leather Unclasp, Wax Seal Fracture, Coin Clinks, and Haptics
 */
class AdventureSoundEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs = context.getSharedPreferences("sound_fx_prefs", Context.MODE_PRIVATE)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
    } catch (_: Exception) {
        null
    }

    private val pcmCache = ConcurrentHashMap<String, ShortArray>()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_effects_enabled", true)
        set(value) {
            prefs.edit().putBoolean("sound_effects_enabled", value).apply()
        }

    init {
        preloadSounds()
    }

    private fun preloadSounds() {
        scope.launch {
            try {
                pcmCache["sonar_ping"] = generateSonarPing()
                pcmCache["cave_echo"] = generateCaveEcho()
                pcmCache["bridge_resonance"] = generateBridgeResonance()
                pcmCache["discovery_chime"] = generateDiscoveryChime()
                pcmCache["tactile_click"] = generateTactileClick()
                pcmCache["needle_drop"] = generatePhonographNeedleDrop()
                pcmCache["radio_sweep_up"] = generateRadioTuningSweep(1)
                pcmCache["radio_sweep_down"] = generateRadioTuningSweep(-1)
                pcmCache["spring_motor"] = generateSpringMotorWinding()
                pcmCache["vault_latch"] = generateVaultLatchRelease()
                pcmCache["gold_coin"] = generateGoldCoinClink()
                pcmCache["parchment_rustle"] = generateParchmentRustle()
                pcmCache["leather_unclasp"] = generateLeatherUnclasp()
                pcmCache["wax_seal"] = generateWaxSealBreak()
                pcmCache["cavern_drip"] = generateCavernDrip()
                pcmCache["camera_shutter"] = generateCameraShutter()
                pcmCache["compass_click"] = generateCompassClick()
                pcmCache["charcoal_scratch"] = generateCharcoalScratch()
                pcmCache["wax_stamp"] = generateWaxStampImpact()
                pcmCache["tape_latch"] = generateTapeDeckLatch()
                pcmCache["radio_burst"] = generateRadioStaticBurst()
                pcmCache["lantern_dial"] = generateLanternWickDial()
                pcmCache["paper_fold"] = generatePaperFold()
                pcmCache["paper_unfold"] = generatePaperUnfold()
                pcmCache["brass_switch"] = generateBrassToggleSwitch()
                pcmCache["winch_ratchet"] = generateWinchRatchetTick()
                pcmCache["cable_creak"] = generateCableCreak()
                pcmCache["hydrophone_splash"] = generateHydrophoneSplash()
            } catch (_: Exception) {}
        }
    }

    private fun playSound(key: String, generator: () -> ShortArray) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val pcm = pcmCache.getOrPut(key) { generator() }
                val sampleRate = 22050
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(2048)
                val allocSize = maxOf(pcm.size * 2, minBuf)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(allocSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()

                val durationMs = (pcm.size * 1000L) / sampleRate + 80L
                kotlinx.coroutines.delay(durationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                } catch (_: Exception) {}
            }
        }
    }

    fun vibrateTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10L)
            }
        } catch (_: Exception) {}
    }

    fun vibrateHeavyClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25L)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100L)
            }
        } catch (_: Exception) {}
    }

    fun playPhonographNeedleDrop() {
        vibrateHeavyClick()
        playSound("needle_drop") { generatePhonographNeedleDrop() }
    }

    fun playRadioTuningSweep(direction: Int = 1) {
        vibrateTick()
        val key = if (direction >= 0) "radio_sweep_up" else "radio_sweep_down"
        playSound(key) { generateRadioTuningSweep(direction) }
    }

    fun playSpringMotorWinding() {
        vibrateTick()
        playSound("spring_motor") { generateSpringMotorWinding() }
    }

    fun playVaultLatchRelease() {
        vibrateHeavyClick()
        playSound("vault_latch") { generateVaultLatchRelease() }
    }

    fun playGoldCoinClink() {
        vibrateTick()
        playSound("gold_coin") { generateGoldCoinClink() }
    }

    fun playParchmentRustle() {
        playSound("parchment_rustle") { generateParchmentRustle() }
    }

    fun playLeatherUnclasp() {
        vibrateHeavyClick()
        playSound("leather_unclasp") { generateLeatherUnclasp() }
    }

    fun playWaxSealBreak() {
        vibrateSuccess()
        playSound("wax_seal") { generateWaxSealBreak() }
    }

    fun playCavernDrip() {
        playSound("cavern_drip") { generateCavernDrip() }
    }

    fun playSonarPing() {
        playSound("sonar_ping") { generateSonarPing() }
    }

    fun playCaveEcho() {
        playSound("cave_echo") { generateCaveEcho() }
    }

    fun playBridgeResonance() {
        playSound("bridge_resonance") { generateBridgeResonance() }
    }

    fun playDiscoveryChime() {
        vibrateSuccess()
        playSound("discovery_chime") { generateDiscoveryChime() }
    }

    fun playTactileClick() {
        vibrateTick()
        playSound("tactile_click") { generateTactileClick() }
    }

    fun playCameraShutter() {
        vibrateHeavyClick()
        playSound("camera_shutter") { generateCameraShutter() }
    }

    fun playCompassClick() {
        vibrateTick()
        playSound("compass_click") { generateCompassClick() }
    }

    fun playCharcoalScratch(intensity: Float = 1.0f) {
        if (Random().nextFloat() < 0.35f) {
            vibrateTick()
        }
        playSound("charcoal_scratch") { generateCharcoalScratch() }
    }

    fun playWaxStampImpact() {
        vibrateSuccess()
        playSound("wax_stamp") { generateWaxStampImpact() }
    }

    fun playTapeDeckLatch() {
        vibrateHeavyClick()
        playSound("tape_latch") { generateTapeDeckLatch() }
    }

    fun playRadioStaticBurst() {
        vibrateTick()
        playSound("radio_burst") { generateRadioStaticBurst() }
    }

    fun playLanternWickDial() {
        vibrateTick()
        playSound("lantern_dial") { generateLanternWickDial() }
    }

    fun playPaperFold() {
        vibrateTick()
        playSound("paper_fold") { generatePaperFold() }
    }

    fun playPaperUnfold() {
        vibrateTick()
        playSound("paper_unfold") { generatePaperUnfold() }
    }

    fun playBrassToggleSwitch() {
        vibrateHeavyClick()
        playSound("brass_switch") { generateBrassToggleSwitch() }
    }

    fun playWinchRatchetTick() {
        vibrateTick()
        playSound("winch_ratchet") { generateWinchRatchetTick() }
    }

    fun playCableCreak() {
        vibrateTick()
        playSound("cable_creak") { generateCableCreak() }
    }

    fun playHydrophoneSplash() {
        vibrateSuccess()
        playSound("hydrophone_splash") { generateHydrophoneSplash() }
    }

    fun playCavernSonarPulse(depthRatio: Float = 0.5f) {
        vibrateHeavyClick()
        val key = "sonar_pulse_${(depthRatio * 10).toInt()}"
        playSound(key) { generateCavernSonarPulse(depthRatio) }
    }

    private fun generateCameraShutter(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.16
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val click1 = 0
        val click2 = (sampleRate * 0.07).toInt()
        val random = Random(123)
        for (i in 0 until numSamples) {
            val t1 = (i - click1).toDouble() / sampleRate
            val t2 = (i - click2).toDouble() / sampleRate
            var sample = 0.0
            if (t1 >= 0.0) {
                val env1 = exp(-60.0 * t1)
                sample += (sin(2.0 * PI * 1800.0 * t1) * 0.6 + (random.nextDouble() * 2.0 - 1.0) * 0.4) * env1
            }
            if (t2 >= 0.0) {
                val env2 = exp(-70.0 * t2)
                sample += (sin(2.0 * PI * 1200.0 * t2) * 0.7 + (random.nextDouble() * 2.0 - 1.0) * 0.3) * env2
            }
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCompassClick(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.06
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-80.0 * t)
            val sample = sin(2.0 * PI * 2400.0 * t) * env
            buffer[i] = (sample * 29000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSonarPing(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.8
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val freq1 = 1760.0 // A6
        val freq2 = 880.0  // A5 harmonic
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Exponential envelope
            val envelope = exp(-5.5 * t)
            val sample = (sin(2.0 * PI * freq1 * t) * 0.7 + sin(2.0 * PI * freq2 * t) * 0.3) * envelope
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCaveEcho(): ShortArray {
        val sampleRate = 22050
        val durationSec = 1.2
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val subBass = 65.41 // C2
        val caveLow = 98.0  // G2
        val hollow = 196.0  // G3
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Subterranean slow swelling rumble then long cave reverb decay
            val attack = (t / 0.15).coerceAtMost(1.0)
            val decay = exp(-2.8 * t)
            val reverbMod = 1.0 + 0.25 * sin(2.0 * PI * 6.0 * t) // Subterranean flutter
            val sample = (
                sin(2.0 * PI * subBass * t) * 0.45 +
                sin(2.0 * PI * caveLow * t) * 0.35 +
                sin(2.0 * PI * hollow * t) * 0.20
            ) * attack * decay * reverbMod
            buffer[i] = (sample * 29000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBridgeResonance(): ShortArray {
        val sampleRate = 22050
        val durationSec = 1.0
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val f1 = 293.66 // D4 (Iron Truss fundamental)
        val f2 = 440.00 // A4 (Girder harmonic)
        val f3 = 587.33 // D5 (Metallic chime)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-3.8 * t)
            val shimmer = 1.0 + 0.15 * sin(2.0 * PI * 18.0 * t)
            val sample = (
                sin(2.0 * PI * f1 * t) * 0.40 +
                sin(2.0 * PI * f2 * t) * 0.35 +
                sin(2.0 * PI * f3 * t) * 0.25
            ) * envelope * shimmer
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateDiscoveryChime(): ShortArray {
        val sampleRate = 22050
        val durationSec = 1.1
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val noteDuration = 0.22
        val c5 = 523.25
        val e5 = 659.25
        val g5 = 783.99
        val c6 = 1046.50

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val (currentFreq, noteStart) = when {
                t < noteDuration -> Pair(c5, 0.0)
                t < noteDuration * 2 -> Pair(e5, noteDuration)
                t < noteDuration * 3 -> Pair(g5, noteDuration * 2)
                else -> Pair(c6, noteDuration * 3)
            }
            val noteT = t - noteStart
            val noteEnv = exp(-4.0 * noteT).coerceAtMost(1.0)
            val sample = (sin(2.0 * PI * currentFreq * noteT) * 0.85 + sin(2.0 * PI * currentFreq * 2 * noteT) * 0.15) * noteEnv
            buffer[i] = (sample * 27000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateTactileClick(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.075
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-50.0 * t)
            val tone1 = sin(2.0 * PI * 1150.0 * t) * 0.70
            val tone2 = sin(2.0 * PI * 2300.0 * t) * 0.30
            val sample = (tone1 + tone2) * env
            buffer[i] = (sample * 29000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generatePhonographNeedleDrop(): ShortArray {
        val sampleRate = 22050
        val durationSec = 1.0
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Initial stylus impact at t = 0.05
            val impactT = t - 0.05
            val impact = if (impactT in 0.0..0.12) {
                sin(2.0 * PI * 85.0 * impactT) * exp(-28.0 * impactT)
            } else 0.0

            // Vinyl surface crackle and grooved hiss after drop
            val hissEnv = if (t > 0.08) (1.0 - exp(-6.0 * (t - 0.08))) * 0.12 else 0.0
            val whiteNoise = (random.nextDouble() * 2.0 - 1.0) * hissEnv

            // Periodic dust crackle clicks
            val crackle = if (t > 0.10 && random.nextDouble() < 0.003) {
                (random.nextDouble() * 2.0 - 1.0) * 0.45
            } else 0.0

            val sample = impact * 0.75 + whiteNoise + crackle
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateRadioTuningSweep(direction: Int): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.35
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(101)

        val startFreq = if (direction >= 0) 650.0 else 1450.0
        val endFreq = if (direction >= 0) 1450.0 else 650.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val envelope = sin(PI * progress) // smooth fade in and out

            // Heterodyne whistle
            val whistle = sin(2.0 * PI * currentFreq * t) * 0.45
            // Atmospheric static
            val staticNoise = (random.nextDouble() * 2.0 - 1.0) * 0.25

            val sample = (whistle + staticNoise) * envelope
            buffer[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSpringMotorWinding(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.25
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val clicks = 4
        val clickSpacing = numSamples / (clicks + 1)

        for (c in 0 until clicks) {
            val center = clickSpacing * (c + 1)
            for (offset in -120..120) {
                val idx = center + offset
                if (idx in 0 until numSamples) {
                    val t = offset.toDouble() / sampleRate
                    val env = exp(-200.0 * kotlin.math.abs(t))
                    val sample = sin(2.0 * PI * 2200.0 * t) * env * 0.7
                    buffer[idx] = (buffer[idx] + (sample * 26000.0).toInt()).coerceIn(-32767, 32767).toShort()
                }
            }
        }
        return buffer
    }

    private fun generateVaultLatchRelease(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.45
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Initial sharp metallic latch release
            val env1 = exp(-45.0 * t)
            val click1 = sin(2.0 * PI * 1850.0 * t) * env1

            // Heavy resonant iron clunk at 60ms
            val t2 = t - 0.06
            val clunk = if (t2 > 0) {
                (sin(2.0 * PI * 175.0 * t2) * 0.7 + sin(2.0 * PI * 350.0 * t2) * 0.3) * exp(-18.0 * t2)
            } else 0.0

            val sample = (click1 * 0.5 + clunk * 0.8)
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateGoldCoinClink(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.75
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val f1 = 2850.0 // Primary bell harmonic of gold
        val f2 = 4280.0 // Overtone
        val f3 = 5700.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Strike 1
            val env1 = exp(-6.5 * t)
            val tone1 = (sin(2.0 * PI * f1 * t) * 0.55 + sin(2.0 * PI * f2 * t) * 0.30 + sin(2.0 * PI * f3 * t) * 0.15) * env1

            // Secondary softer bounce clink at 0.12s
            val t2 = t - 0.12
            val tone2 = if (t2 > 0) {
                val env2 = exp(-8.0 * t2)
                (sin(2.0 * PI * (f1 * 1.05) * t2) * 0.40 + sin(2.0 * PI * (f2 * 1.05) * t2) * 0.20) * env2
            } else 0.0

            val sample = tone1 * 0.7 + tone2 * 0.5
            buffer[i] = (sample * 27000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateParchmentRustle(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.40
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(777)

        var lastSample = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val env = sin(PI * progress) * (1.0 + 0.3 * sin(2.0 * PI * 8.0 * progress))
            val raw = random.nextDouble() * 2.0 - 1.0
            // Low-pass filtered noise for paper-like texture
            lastSample = lastSample * 0.85 + raw * 0.15
            val sample = lastSample * env * 0.8
            buffer[i] = (sample * 25000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCavernDrip(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.65
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Quick dropping pitch (2400 Hz -> 1400 Hz)
            val freq = 1400.0 + 1000.0 * exp(-35.0 * t)
            val env = exp(-9.0 * t)
            val drop = sin(2.0 * PI * freq * t) * env

            // Reverb echo in subterranean pool at 0.18s
            val tEcho = t - 0.18
            val echo = if (tEcho > 0) {
                sin(2.0 * PI * 1350.0 * tEcho) * exp(-5.0 * tEcho) * 0.30
            } else 0.0

            val sample = drop * 0.8 + echo
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLeatherUnclasp(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.35
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(888)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Initial sharp metallic buckle snap (1200 Hz with fast decay)
            val metallic = sin(2.0 * PI * 1180.0 * t) * exp(-70.0 * t) * 0.7
            // Heavy leather friction friction noise (lowpass filtered noise)
            val leatherFriction = (random.nextDouble() * 2.0 - 1.0) * exp(-18.0 * t) * (1.0 - exp(-30.0 * t)) * 0.45
            // Low thud of thick leather flap releasing (90 Hz)
            val thud = sin(2.0 * PI * 92.0 * t) * exp(-22.0 * t) * 0.5

            val sample = metallic + leatherFriction + thud
            buffer[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWaxSealBreak(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.40
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(999)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // High frequency brittle crack (3200 Hz -> 2100 Hz sharp impulse)
            val crack = (sin(2.0 * PI * 2800.0 * t) + (random.nextDouble() * 2.0 - 1.0) * 0.6) * exp(-85.0 * t) * 0.8
            // Secondary wax fracture pop at 0.04s
            val t2 = t - 0.04
            val pop = if (t2 > 0) sin(2.0 * PI * 1450.0 * t2) * exp(-60.0 * t2) * 0.4 else 0.0
            // Crisp paper release tail
            val paperTail = (random.nextDouble() * 2.0 - 1.0) * exp(-15.0 * t) * 0.25

            val sample = crack + pop + paperTail
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCharcoalScratch(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.09
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()

        var grainFilter = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Granular friction on textured paper: filtered noise burst with rapid decay
            val white = random.nextDouble() * 2.0 - 1.0
            grainFilter = 0.65 * grainFilter + 0.35 * white
            val envelope = (1.0 - t / durationSec) * exp(-28.0 * t)
            val grit = sin(2.0 * PI * (2200.0 + random.nextInt(600)) * t) * 0.25
            val sample = (grainFilter * 0.75 + grit) * envelope
            buffer[i] = (sample * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWaxStampImpact(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.38
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Deep resonant brass stamp body (105 Hz)
            val body = sin(2.0 * PI * 105.0 * t) * exp(-18.0 * t) * 0.75
            // High metallic ringing edge (1850 Hz)
            val ring = sin(2.0 * PI * 1850.0 * t) * exp(-65.0 * t) * 0.35
            // Wax seal squish impulse (0.01 - 0.05s)
            val squish = (random.nextDouble() * 2.0 - 1.0) * exp(-45.0 * t) * 0.2
            val sample = body + ring + squish
            buffer[i] = (sample * 29000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateTapeDeckLatch(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.22
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random(101)

        val click1 = 0
        val click2 = (sampleRate * 0.06).toInt()

        for (i in 0 until numSamples) {
            val t1 = (i - click1).toDouble() / sampleRate
            val t2 = (i - click2).toDouble() / sampleRate
            var sample = 0.0

            // Heavy solenoid latch strike
            if (t1 >= 0.0) {
                val impact = sin(2.0 * PI * 340.0 * t1) * exp(-45.0 * t1) * 0.7
                val snap = (random.nextDouble() * 2.0 - 1.0) * exp(-80.0 * t1) * 0.5
                sample += impact + snap
            }
            // Spring rebound catch
            if (t2 >= 0.0) {
                val rebound = sin(2.0 * PI * 680.0 * t2) * exp(-55.0 * t2) * 0.5
                sample += rebound
            }

            buffer[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateRadioStaticBurst(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.25
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()

        var noiseState = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val white = random.nextDouble() * 2.0 - 1.0
            noiseState = 0.75 * noiseState + 0.25 * white
            val whistle = sin(2.0 * PI * 1150.0 * t) * 0.15
            val env = sin(PI * (t / durationSec)) // Smooth bell curve burst
            val sample = (noiseState * 0.7 + whistle) * env
            buffer[i] = (sample * 25000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLanternWickDial(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.05
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val clickImpulse = exp(-120.0 * t)
            val metallicPing = sin(2.0 * PI * 2850.0 * t) * exp(-75.0 * t)
            val toothFriction = (random.nextDouble() * 2.0 - 1.0) * exp(-160.0 * t) * 0.4
            val sample = (clickImpulse * 0.5 + metallicPing * 0.4 + toothFriction)
            buffer[i] = (sample * 27000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generatePaperFold(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.12
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        var lowPass = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val white = random.nextDouble() * 2.0 - 1.0
            lowPass = 0.65 * lowPass + 0.35 * white
            val creaseImpulse = if (t in 0.04..0.06) {
                sin(2.0 * PI * 1450.0 * (t - 0.04)) * exp(-110.0 * (t - 0.04))
            } else 0.0
            val env = exp(-28.0 * t)
            val sample = (lowPass * 0.7 * env + creaseImpulse * 0.6)
            buffer[i] = (sample * 25000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generatePaperUnfold(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.18
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        var bandState = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val white = random.nextDouble() * 2.0 - 1.0
            bandState = 0.55 * bandState + 0.45 * white
            val flapSnap = if (t in 0.08..0.11) {
                sin(2.0 * PI * 920.0 * (t - 0.08)) * exp(-85.0 * (t - 0.08))
            } else 0.0
            val env = sin(PI * (t / durationSec).coerceIn(0.0, 1.0)) * exp(-12.0 * t)
            val sample = (bandState * 0.8 * env + flapSnap * 0.5)
            buffer[i] = (sample * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBrassToggleSwitch(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.11
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val contactThud = sin(2.0 * PI * 780.0 * t) * exp(-65.0 * t)
            val brassChime = sin(2.0 * PI * 1680.0 * t) * exp(-45.0 * t) * 0.4
            val springCatch = if (t > 0.03) {
                sin(2.0 * PI * 2400.0 * (t - 0.03)) * exp(-95.0 * (t - 0.03)) * 0.5
            } else 0.0
            val scrape = (random.nextDouble() * 2.0 - 1.0) * exp(-140.0 * t) * 0.3
            val sample = contactThud + brassChime + springCatch + scrape
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWinchRatchetTick(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.045
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val pawlImpulse = exp(-140.0 * t)
            val brassTooth = sin(2.0 * PI * 3400.0 * t) * exp(-90.0 * t) * 0.6
            val springRebound = if (t > 0.015) {
                sin(2.0 * PI * 1850.0 * (t - 0.015)) * exp(-110.0 * (t - 0.015)) * 0.4
            } else 0.0
            val friction = (random.nextDouble() * 2.0 - 1.0) * exp(-180.0 * t) * 0.3
            val sample = pawlImpulse * 0.5 + brassTooth + springRebound + friction
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCableCreak(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.16
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val wireGroan = sin(2.0 * PI * 460.0 * t) * exp(-18.0 * t)
            val harmonic = sin(2.0 * PI * 920.0 * t) * exp(-24.0 * t) * 0.4
            val metallicFriction = (random.nextDouble() * 2.0 - 1.0) * exp(-45.0 * t) * 0.25
            val sample = (wireGroan + harmonic + metallicFriction) * sin(PI * (t / durationSec).coerceIn(0.0, 1.0))
            buffer[i] = (sample * 26000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateHydrophoneSplash(): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.22
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val random = Random()
        var waterNoise = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val white = random.nextDouble() * 2.0 - 1.0
            waterNoise = 0.7 * waterNoise + 0.3 * white
            val plungeThud = sin(2.0 * PI * 180.0 * t) * exp(-35.0 * t)
            val bubble = if (t in 0.05..0.12) {
                sin(2.0 * PI * 580.0 * (t - 0.05)) * exp(-50.0 * (t - 0.05)) * 0.5
            } else 0.0
            val env = exp(-16.0 * t)
            val sample = plungeThud * 0.7 + bubble + (waterNoise * 0.5 * env)
            buffer[i] = (sample * 27000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCavernSonarPulse(depthRatio: Float): ShortArray {
        val sampleRate = 22050
        val durationSec = 0.35
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val carrierFreq = 950.0 - (depthRatio.coerceIn(0f, 1f) * 550.0) // 950 Hz down to 400 Hz
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val primaryPulse = sin(2.0 * PI * carrierFreq * t) * exp(-30.0 * t)
            val echo1 = if (t > 0.08) sin(2.0 * PI * (carrierFreq * 0.95) * (t - 0.08)) * exp(-25.0 * (t - 0.08)) * 0.45 else 0.0
            val echo2 = if (t > 0.17) sin(2.0 * PI * (carrierFreq * 0.90) * (t - 0.17)) * exp(-20.0 * (t - 0.17)) * 0.25 else 0.0
            val sample = primaryPulse + echo1 + echo2
            buffer[i] = (sample * 28000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }
}
