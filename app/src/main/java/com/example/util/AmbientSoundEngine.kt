package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Ambient Synthesizer generator for historical exploration atmosphere:
 * - River Foghorn / Steam Calliope ambient drone
 * - Rail Whistle & Iron Resonance
 * - Knobs Ridge Wind & Cave Echo
 */
class AmbientSoundEngine(private val context: Context) {

    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying: StateFlow<Boolean> = _isAmbientPlaying.asStateFlow()

    private val _ambientTheme = MutableStateFlow("NONE")
    val ambientTheme: StateFlow<String> = _ambientTheme.asStateFlow()

    private val _isDucked = MutableStateFlow(false)
    val isDucked: StateFlow<Boolean> = _isDucked.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setDucked(ducked: Boolean) {
        _isDucked.value = ducked
    }

    fun startAmbient(theme: String) {
        if (_isAmbientPlaying.value && _ambientTheme.value == theme) return
        stopAmbient()

        _ambientTheme.value = theme
        _isAmbientPlaying.value = true

        synthJob = scope.launch {
            generateSyntheticAmbient(theme)
        }
    }

    fun stopAmbient() {
        _isAmbientPlaying.value = false
        _ambientTheme.value = "NONE"
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private suspend fun generateSyntheticAmbient(theme: String) {
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            val buffer = ShortArray(bufferSize / 2)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0

            // Base frequencies for atmospheric tone
            val isTapeMode = theme == "TAPE_HISS"
            val isRadioMode = theme == "ARCHIVAL_RADIO"

            val (baseFreq, modFreq, subFreq) = when (theme) {
                "RIVER" -> Triple(110.0, 165.0, 55.0) // Deep steam horn chord (A2 + E3)
                "RAIL" -> Triple(146.83, 220.0, 73.4) // D3 + A3 train horn interval
                "CAVE_KNOBS" -> Triple(92.5, 138.59, 46.25) // Low subterranean drone & echo
                "FOREST_WIND" -> Triple(174.61, 261.63, 87.31) // F3 + C4 whistling ridge wind
                "VINTAGE_RADIO" -> Triple(220.0, 330.0, 110.0) // AM radio resonance & carrier drone
                "TAPE_HISS" -> Triple(60.0, 120.0, 5.5) // 60Hz transformer hum + 120Hz harmonic + 5.5Hz flutter
                "ARCHIVAL_RADIO" -> Triple(880.0, 60.0, 7.2) // Carrier heterodyne whistle + hum
                else -> Triple(100.0, 150.0, 50.0)
            }

            var pinkNoiseState = 0.0
            val random = java.util.Random()

            while (synthJob?.isActive == true && _isAmbientPlaying.value) {
                // Duck ambient volume to 20% if narration or speech is active
                val duckingFactor = if (_isDucked.value) 0.20 else 1.0

                for (i in buffer.indices) {
                    val combined: Double
                    if (isTapeMode) {
                        // Magnetic Tape Hiss with 5.5Hz WOW flutter & 60Hz hum
                        val white = (random.nextDouble() * 2.0 - 1.0)
                        pinkNoiseState = 0.88 * pinkNoiseState + 0.12 * white // Low-pass filtered ferric hiss
                        val hum60 = sin(phase1) * 0.06 // 60Hz transformer hum
                        val hum120 = sin(phase2) * 0.03 // 120Hz secondary harmonic
                        val wowFlutter = 1.0 + 0.18 * sin(phase3) // 5.5Hz reel flutter modulation
                        combined = (pinkNoiseState * 0.18 * wowFlutter + hum60 + hum120) * duckingFactor
                    } else if (isRadioMode) {
                        // Archival AM Radio: Static burst pops + heterodyne whistle + hum
                        val white = (random.nextDouble() * 2.0 - 1.0)
                        pinkNoiseState = 0.82 * pinkNoiseState + 0.18 * white
                        val carrierWhistle = sin(phase1) * 0.04 * (1.0 + 0.2 * sin(phase3))
                        val hum = sin(phase2) * 0.05
                        val pop = if (random.nextInt(1200) < 3) (random.nextDouble() * 0.5 - 0.25) else 0.0
                        combined = (pinkNoiseState * 0.14 + carrierWhistle + hum + pop) * duckingFactor
                    } else {
                        // Gentle undulating modulation
                        val lfo = (1.0 + 0.3 * sin(phase3)) * 0.15
                        val sample1 = sin(phase1) * 0.4
                        val sample2 = sin(phase2) * 0.25
                        val sampleSub = sin(phase3 * 0.5) * 0.35
                        combined = (sample1 + sample2 + sampleSub) * lfo * 0.25 * duckingFactor
                    }

                    buffer[i] = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()

                    phase1 += 2.0 * Math.PI * baseFreq / sampleRate
                    phase2 += 2.0 * Math.PI * modFreq / sampleRate
                    phase3 += 2.0 * Math.PI * (if (isTapeMode || isRadioMode) subFreq else baseFreq * 0.08) / sampleRate

                    if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                    if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                    if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI
                }

                audioTrack?.write(buffer, 0, buffer.size)
                delay(5)
            }
        } catch (_: Exception) {
            stopAmbient()
        }
    }
}
