package com.example.audio

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingFile = MutableStateFlow<String?>(null)
    val currentPlayingFile: StateFlow<String?> = _currentPlayingFile.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun playFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        if (_currentPlayingFile.value == filePath && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
            return
        }

        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                val dur = duration
                _durationMs.value = dur
                _currentPositionMs.value = 0
                _currentPlayingFile.value = filePath

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0
                    progressJob?.cancel()
                }

                applySpeed(_playbackSpeed.value)
                start()
            }
            _isPlaying.value = true
            startProgressTracker()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file", e)
            stop()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let {
                    try {
                        if (it.isPlaying) {
                            _currentPositionMs.value = it.currentPosition
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(200)
            }
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _currentPositionMs.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error seeking", e)
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = speed
                mediaPlayer?.playbackParams = params
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error setting speed", e)
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error pausing", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping", e)
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPlayingFile.value = null
            _currentPositionMs.value = 0
            _durationMs.value = 0
        }
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
