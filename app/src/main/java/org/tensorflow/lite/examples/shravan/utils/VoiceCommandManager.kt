package org.tensorflow.lite.examples.shravan.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat

class VoiceCommandManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var onResult: ((String) -> Unit)? = null
    private var lastIsVietnamese = true
    private var shouldRetry = true

    init {
        createRecognizer()
    }

    private fun createRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("VoiceCommandManager", "Ready for speech")
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    Log.e("VoiceCommandManager", "Error: $error")
                    isListening = false
                    if (shouldRetry && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        startListeningInternal()
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult?.invoke(matches[0])
                    }
                    isListening = false
                    if (shouldRetry) {
                        startListeningInternal()
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening(isVietnamese: Boolean = true, retry: Boolean = true, callback: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("VoiceCommandManager", "RECORD_AUDIO permission not granted")
            return
        }
        onResult = callback
        lastIsVietnamese = isVietnamese
        shouldRetry = retry
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lastIsVietnamese) "vi-VN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        shouldRetry = false
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun destroy() {
        shouldRetry = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
