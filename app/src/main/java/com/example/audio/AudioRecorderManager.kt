package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0)
    val currentAmplitude: StateFlow<Int> = _currentAmplitude.asStateFlow()

    fun startRecording(surahNumber: Int, prefix: String = "rec"): File? {
        stopRecording()

        val outputDir = File(context.filesDir, "murajaah_recordings").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "murajaah_s${surahNumber}_${System.currentTimeMillis()}.m4a"
        val file = File(outputDir, fileName)
        currentOutputFile = file

        recorder = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording", e)
            null
        }

        if (recorder != null) {
            _isRecording.value = true
            _recordingDurationSeconds.value = 0
            startTimer()
            return file
        }
        return null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDurationSeconds.value += 1
                try {
                    val amp = recorder?.maxAmplitude ?: 0
                    _currentAmplitude.value = amp
                } catch (e: Exception) {
                    _currentAmplitude.value = 0
                }
            }
        }
    }

    fun stopRecording(): File? {
        if (!_isRecording.value) return null
        timerJob?.cancel()
        timerJob = null

        val resultFile = currentOutputFile
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recorder", e)
        } finally {
            recorder = null
            _isRecording.value = false
            _currentAmplitude.value = 0
        }
        return resultFile
    }

    fun release() {
        stopRecording()
        scope.cancel()
    }
}
