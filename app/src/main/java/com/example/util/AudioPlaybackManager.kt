package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
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

/**
 * Manages playback of recorded oral history field audio memos with position tracking.
 */
class AudioPlaybackManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    @Synchronized
    fun play(id: String, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.w("AudioPlaybackManager", "Audio file does not exist: $filePath")
            return
        }

        // If already playing this item, toggle pause
        if (_currentlyPlayingId.value == id && mediaPlayer != null) {
            if (_isPlaying.value) {
                pause()
                return
            } else {
                resume()
                return
            }
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
            }

            mediaPlayer = player
            _currentlyPlayingId.value = id
            _durationMs.value = player.duration.toLong().coerceAtLeast(0L)
            _currentPositionMs.value = 0L

            player.setOnCompletionListener {
                stop()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlaybackManager", "MediaPlayer error: what=$what, extra=$extra")
                stop()
                true
            }

            player.start()
            _isPlaying.value = true
            startProgressTracking()
        } catch (e: Exception) {
            Log.e("AudioPlaybackManager", "Failed to start playback for $filePath", e)
            stop()
        }
    }

    @Synchronized
    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            _isPlaying.value = false
            progressJob?.cancel()
            progressJob = null
        } catch (e: Exception) {
            Log.w("AudioPlaybackManager", "Error pausing playback", e)
        }
    }

    @Synchronized
    fun resume() {
        try {
            mediaPlayer?.let {
                it.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.w("AudioPlaybackManager", "Error resuming playback", e)
        }
    }

    @Synchronized
    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.let {
                it.seekTo(positionMs.toInt())
                _currentPositionMs.value = positionMs
            }
        } catch (e: Exception) {
            Log.w("AudioPlaybackManager", "Error seeking playback", e)
        }
    }

    @Synchronized
    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioPlaybackManager", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            _currentlyPlayingId.value = null
            _isPlaying.value = false
            _currentPositionMs.value = 0L
            _durationMs.value = 0L
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            _currentPositionMs.value = it.currentPosition.toLong()
                        }
                    }
                } catch (_: Exception) {
                }
                delay(100)
            }
        }
    }
}
