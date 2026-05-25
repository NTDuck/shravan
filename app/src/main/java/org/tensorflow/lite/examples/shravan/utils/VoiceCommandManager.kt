package org.tensorflow.lite.examples.shravan.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var currentSessionId = 0

    init {
        handler.post {
            createRecognizer(0)
        }
    }

    private fun createRecognizer(sessionId: Int) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (sessionId != currentSessionId) return
                    Log.d("VoiceCommandManager", "Ready for speech (Session: $sessionId)")
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    if (sessionId != currentSessionId) return
                    isListening = false
                }
                override fun onError(error: Int) {
                    if (sessionId != currentSessionId) return
                    Log.e("VoiceCommandManager", "Error: $error (Session: $sessionId)")
                    isListening = false
                    if (shouldRetry && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        handler.post {
                            startListeningInternal(sessionId)
                        }
                    }
                }
                override fun onResults(results: Bundle?) {
                    if (sessionId != currentSessionId) return
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        handler.post {
                            if (sessionId == currentSessionId) {
                                onResult?.invoke(matches[0])
                            }
                        }
                    }
                    isListening = false
                    if (shouldRetry) {
                        handler.post {
                            startListeningInternal(sessionId)
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening(isVietnamese: Boolean = true, retry: Boolean = true, callback: (String) -> Unit): Int {
        currentSessionId++
        val sessionId = currentSessionId
        handler.post {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.e("VoiceCommandManager", "RECORD_AUDIO permission not granted")
                return@post
            }
            onResult = callback
            lastIsVietnamese = isVietnamese
            shouldRetry = retry

            destroyRecognizerInternal()
            createRecognizer(sessionId)

            startListeningInternal(sessionId)
        }
        return sessionId
    }

    private fun startListeningInternal(sessionId: Int) {
        if (sessionId != currentSessionId) return
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lastIsVietnamese) "vi-VN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening(sessionId: Int) {
        handler.post {
            if (sessionId == currentSessionId) {
                shouldRetry = false
                speechRecognizer?.stopListening()
                isListening = false
            }
        }
    }

    private fun destroyRecognizerInternal() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    fun destroy() {
        handler.post {
            shouldRetry = false
            destroyRecognizerInternal()
        }
    }
}
