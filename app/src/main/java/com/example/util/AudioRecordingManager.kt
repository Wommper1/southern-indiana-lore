package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

/**
 * Manages low-latency field audio recording with live decibel amplitude polling
 * for the Southern Indiana Lore vintage tape deck UI.
 */
class AudioRecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0L
    private var amplitudePollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _liveNormalizedAmplitude = MutableStateFlow(0f)
    val liveNormalizedAmplitude: StateFlow<Float> = _liveNormalizedAmplitude.asStateFlow()

    fun getVoiceMemoDirectory(): File {
        val dir = File(context.filesDir, "voice_memos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    @Synchronized
    fun startRecording(fileId: String): File? {
        if (_isRecording.value) {
            stopRecording()
        }

        return try {
            val dir = getVoiceMemoDirectory()
            val outputFile = File(dir, "memo_${fileId}.m4a")
            currentOutputFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
            _recordingDurationMs.value = 0L
            _liveNormalizedAmplitude.value = 0f

            startAmplitudePolling()
            outputFile
        } catch (e: Exception) {
            Log.e("AudioRecordingManager", "Failed to start audio recording", e)
            cleanup()
            null
        }
    }

    private fun startAmplitudePolling() {
        amplitudePollJob?.cancel()
        amplitudePollJob = scope.launch {
            while (isActive && _isRecording.value) {
                try {
                    val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                    // maxAmplitude ranges from 0 to ~32767
                    val normalized = min(1.0f, (maxAmp.toFloat() / 24000f))
                    _liveNormalizedAmplitude.value = normalized

                    val elapsed = System.currentTimeMillis() - recordingStartTime
                    _recordingDurationMs.value = elapsed
                } catch (e: Exception) {
                    _liveNormalizedAmplitude.value = 0f
                }
                delay(80)
            }
        }
    }

    @Synchronized
    fun stopRecording(): Long {
        val duration = System.currentTimeMillis() - recordingStartTime
        amplitudePollJob?.cancel()
        amplitudePollJob = null

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioRecordingManager", "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            _liveNormalizedAmplitude.value = 0f
        }
        return duration.coerceAtLeast(0L)
    }

    @Synchronized
    fun cancelRecording() {
        amplitudePollJob?.cancel()
        amplitudePollJob = null

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            _liveNormalizedAmplitude.value = 0f
        }

        currentOutputFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
        currentOutputFile = null
    }

    private fun cleanup() {
        amplitudePollJob?.cancel()
        amplitudePollJob = null
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null
        _isRecording.value = false
        _liveNormalizedAmplitude.value = 0f
    }
}
