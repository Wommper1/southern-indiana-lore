package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NarratorManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                tts?.stop()
                _isPlaying.value = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Duck volume if needed
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
            }
        }
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: Triple<String, String, String>? = null
    var onPlaybackFinished: (() -> Unit)? = null

    private val prefs = context.getSharedPreferences("narrator_prefs", Context.MODE_PRIVATE)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentNarrationSiteId = MutableStateFlow<String?>(null)
    val currentNarrationSiteId: StateFlow<String?> = _currentNarrationSiteId.asStateFlow()

    private val _currentNarrationTitle = MutableStateFlow("")
    val currentNarrationTitle: StateFlow<String> = _currentNarrationTitle.asStateFlow()

    private val _selectedPersona = MutableStateFlow(
        NarratorPersona.fromId(prefs.getString("persona_id", NarratorPersona.ADVENTURE_CHRONICLER.id))
    )
    val selectedPersona: StateFlow<NarratorPersona> = _selectedPersona.asStateFlow()

    private val _speechRate = MutableStateFlow(
        prefs.getFloat("speech_rate", _selectedPersona.value.baseSpeechRate)
    )
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(
        prefs.getFloat("pitch", _selectedPersona.value.basePitch)
    )
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()

    private val _customVoiceName = MutableStateFlow<String?>(
        prefs.getString("custom_voice_name", null)
    )
    val customVoiceName: StateFlow<String?> = _customVoiceName.asStateFlow()

    // Narration Detail Mode: "FULL_STORY" (deep archival & folklore journey) or "QUICK_DRIVE" (concise road summary)
    private val _narrationDepth = MutableStateFlow(
        prefs.getString("narration_depth", "FULL_STORY") ?: "FULL_STORY"
    )
    val narrationDepth: StateFlow<String> = _narrationDepth.asStateFlow()

    // Buffering & Acoustic Calibration state
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _tuningStatus = MutableStateFlow("Calibrating Storyteller Acoustics...")
    val tuningStatus: StateFlow<String> = _tuningStatus.asStateFlow()

    // Sleep Timer (0 = Off, >0 = minutes remaining)
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val application = context.applicationContext
        // Prefer Google TTS engine for highest quality natural neural voices
        var preferredEngine: String? = null
        try {
            val dummyTts = TextToSpeech(application, null)
            val engines = dummyTts.engines
            dummyTts.shutdown()
            if (engines.any { it.name == "com.google.android.tts" }) {
                preferredEngine = "com.google.android.tts"
            }
        } catch (_: Exception) {}

        val initListener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                refreshAvailableVoices()
                applyVoiceAndPitch()

                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isBuffering.value = false
                        _isPlaying.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        abandonAudioFocus()
                        _isBuffering.value = false
                        _isPlaying.value = false
                        _currentNarrationSiteId.value = null
                        onPlaybackFinished?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        abandonAudioFocus()
                        _isBuffering.value = false
                        _isPlaying.value = false
                        _currentNarrationSiteId.value = null
                        onPlaybackFinished?.invoke()
                    }
                })

                pendingSpeech?.let { (id, title, text) ->
                    pendingSpeech = null
                    speak(id, title, text)
                }
            }
        }

        tts = if (preferredEngine != null) {
            TextToSpeech(application, initListener, preferredEngine)
        } else {
            TextToSpeech(application, initListener)
        }
    }

    private val _isNeuralPipelineActive = MutableStateFlow(true)
    val isNeuralPipelineActive: StateFlow<Boolean> = _isNeuralPipelineActive.asStateFlow()

    fun refreshAvailableVoices() {
        try {
            val all = tts?.voices ?: emptySet()
            // Strictly filter out low quality / robotic synthetic voices:
            // 1. Exclude legacy engines or flat synthesizer names (e.g., "pico", "espeak", "compact", "low-quality")
            // 2. Reject Voice.QUALITY_VERY_LOW and Voice.QUALITY_LOW if normal/high quality voices exist
            // 3. Keep English voices
            val rawEnglish = all.filter { it.locale.language.startsWith("en", ignoreCase = true) }
            val higherQuality = rawEnglish.filter { voice ->
                val nameLower = voice.name.lowercase()
                val isLowQualityName = nameLower.contains("pico") || nameLower.contains("espeak") || nameLower.contains("compact")
                val isVeryLowQuality = voice.quality == Voice.QUALITY_VERY_LOW || voice.quality == Voice.QUALITY_LOW
                !isLowQualityName && !isVeryLowQuality
            }
            val filtered = if (higherQuality.isNotEmpty()) higherQuality else rawEnglish
            val sorted = filtered.sortedWith(
                compareByDescending<Voice> { voice ->
                    val n = voice.name.lowercase()
                    when {
                        n.contains("neural") || n.contains("wavenet") || n.contains("natural") -> 4
                        voice.quality >= Voice.QUALITY_HIGH -> 3
                        voice.quality == Voice.QUALITY_NORMAL -> 2
                        !voice.isNetworkConnectionRequired -> 1
                        else -> 0
                    }
                }.thenBy { it.name }
            )
            _availableVoices.value = sorted

            val hasNeuralOrWavenet = sorted.any {
                val n = it.name.lowercase()
                n.contains("neural") || n.contains("wavenet") || n.contains("natural") || it.quality >= Voice.QUALITY_HIGH
            }
            _isNeuralPipelineActive.value = hasNeuralOrWavenet || sorted.isNotEmpty()
        } catch (_: Exception) {}
    }

    fun setPersona(persona: NarratorPersona) {
        _customVoiceName.value = null
        _selectedPersona.value = persona
        _speechRate.value = persona.baseSpeechRate
        _pitch.value = persona.basePitch
        prefs.edit()
            .remove("custom_voice_name")
            .putString("persona_id", persona.id)
            .putFloat("speech_rate", persona.baseSpeechRate)
            .putFloat("pitch", persona.basePitch)
            .apply()
        applyVoiceAndPitch()
    }

    fun setCustomVoice(voiceName: String?) {
        _customVoiceName.value = voiceName
        if (voiceName != null) {
            prefs.edit().putString("custom_voice_name", voiceName).apply()
        } else {
            prefs.edit().remove("custom_voice_name").apply()
        }
        applyVoiceAndPitch()
    }

    fun setPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.6f, 1.6f)
        _pitch.value = clamped
        prefs.edit().putFloat("pitch", clamped).apply()
        tts?.setPitch(clamped)
    }

    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.70f, 1.5f)
        _speechRate.value = clampedRate
        prefs.edit().putFloat("speech_rate", clampedRate).apply()
        tts?.setSpeechRate(clampedRate)
    }

    fun setNarrationDepth(depth: String) {
        _narrationDepth.value = depth
        prefs.edit().putString("narration_depth", depth).apply()
    }

    fun applyVoiceAndPitch() {
        val persona = _selectedPersona.value
        val pitchToUse = _pitch.value
        val rateToUse = _speechRate.value

        try {
            val allVoices = tts?.voices
            if (!allVoices.isNullOrEmpty()) {
                val customName = _customVoiceName.value
                if (customName != null) {
                    val customVoice = allVoices.firstOrNull { it.name == customName }
                    if (customVoice != null) {
                        tts?.voice = customVoice
                    }
                } else {
                    val bestVoice = findBestVoiceForPersona(persona, allVoices)
                    if (bestVoice != null) {
                        tts?.voice = bestVoice
                    }
                }
            }
        } catch (_: Exception) {}

        // CRITICAL: Must be invoked AFTER setting voice because setVoice resets pitch and rate
        tts?.setPitch(pitchToUse)
        tts?.setSpeechRate(rateToUse)
    }

    private fun findBestVoiceForPersona(persona: NarratorPersona, allVoices: Set<Voice>): Voice? {
        val englishVoices = allVoices.filter { it.locale.language.startsWith("en", ignoreCase = true) }
            .sortedBy { it.name }
        if (englishVoices.isEmpty()) return allVoices.firstOrNull()
        if (englishVoices.size == 1) return englishVoices.first()

        val femaleVoices = englishVoices.filter {
            it.name.contains("female", true) || it.name.contains("f0", true) || it.name.contains("sfg", true)
        }
        val maleVoices = englishVoices.filter {
            it.name.contains("male", true) || it.name.contains("m0", true) || it.name.contains("iol", true) || it.name.contains("iom", true)
        }
        val gbVoices = englishVoices.filter {
            it.locale.country.equals("GB", true) || it.name.contains("gb", true) || it.name.contains("rjs", true)
        }

        return when (persona) {
            NarratorPersona.HISTORIAN -> {
                gbVoices.firstOrNull()
                    ?: femaleVoices.firstOrNull()
                    ?: englishVoices[0 % englishVoices.size]
            }
            NarratorPersona.FOLKLORE_SAGE -> {
                maleVoices.firstOrNull()
                    ?: englishVoices[1 % englishVoices.size]
            }
            NarratorPersona.FIELD_EXPLORER -> {
                femaleVoices.lastOrNull()
                    ?: englishVoices[2 % englishVoices.size]
            }
            NarratorPersona.ADVENTURE_CHRONICLER -> {
                maleVoices.lastOrNull()
                    ?: englishVoices[3 % englishVoices.size]
            }
            NarratorPersona.WIZARD_NARRATOR -> {
                maleVoices.firstOrNull { it.name.contains("iom", true) }
                    ?: maleVoices.firstOrNull()
                    ?: englishVoices[1 % englishVoices.size]
            }
            NarratorPersona.RETRO_RADIO -> {
                femaleVoices.firstOrNull { it.name.contains("tpd", true) }
                    ?: maleVoices.lastOrNull()
                    ?: englishVoices[0 % englishVoices.size]
            }
        }
    }

    fun previewPersona(persona: NarratorPersona, soundEngine: AdventureSoundEngine? = null) {
        setPersona(persona)
        when (persona) {
            NarratorPersona.HISTORIAN -> soundEngine?.playParchmentRustle()
            NarratorPersona.FOLKLORE_SAGE -> soundEngine?.playCaveEcho()
            NarratorPersona.FIELD_EXPLORER -> soundEngine?.playSonarPing()
            NarratorPersona.ADVENTURE_CHRONICLER -> soundEngine?.playPhonographNeedleDrop()
            NarratorPersona.WIZARD_NARRATOR -> soundEngine?.playCavernSonarPulse()
            NarratorPersona.RETRO_RADIO -> soundEngine?.playRadioStaticBurst()
        }
        speak(
            siteId = "preview_${persona.id}",
            title = persona.displayName,
            text = persona.sampleAuditionLine,
            forceRestart = true
        )
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = request
            val res = audioManager.requestAudioFocus(request)
            hasAudioFocus = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            hasAudioFocus
        } else {
            @Suppress("DEPRECATION")
            val res = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            hasAudioFocus = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            hasAudioFocus
        }
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus || audioManager == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
        hasAudioFocus = false
    }

    fun speak(siteId: String, title: String, text: String, forceRestart: Boolean = false) {
        if (!isInitialized) {
            pendingSpeech = Triple(siteId, title, text)
            _currentNarrationSiteId.value = siteId
            _currentNarrationTitle.value = title
            _isPlaying.value = true
            return
        }

        if (!forceRestart && _currentNarrationSiteId.value == siteId && _isPlaying.value) {
            stop()
            return
        }

        requestAudioFocus()

        _currentNarrationSiteId.value = siteId
        _currentNarrationTitle.value = title
        _isPlaying.value = true

        applyVoiceAndPitch()
        val speechText = SpeechProsodyHelper.formatForHumanNarration(text)
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LORE_NARRATION_${System.currentTimeMillis()}")
        }
        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, "LORE_NARRATION_${System.currentTimeMillis()}")
    }

    fun stop() {
        pendingSpeech = null
        tts?.stop()
        abandonAudioFocus()
        _isPlaying.value = false
        _currentNarrationSiteId.value = null
        _currentNarrationTitle.value = ""
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes > 0) {
            sleepTimerJob = scope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    delay(60_000L) // 1 minute ticks
                    remaining--
                    _sleepTimerMinutes.value = remaining
                }
                stop()
            }
        }
    }

    fun shutdown() {
        sleepTimerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
