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
    var onGlobalIntent: ((String) -> Boolean)? = null
    private var onResult: ((String) -> Unit)? = null
    private var onPartialResult: ((String) -> Unit)? = null
    private var lastIsVietnamese = true
    private var shouldRetry = true
    private val handler = Handler(Looper.getMainLooper())
    private var currentSessionId = 0

    init {
        handler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                    if (speechRecognizer != null) {
                        setupListener()
                    } else {
                        Log.e("VoiceCommandManager", "SpeechRecognizer creation failed")
                    }
                } else {
                    Log.e("VoiceCommandManager", "SpeechRecognition not available")
                }
            } catch (e: Throwable) {
                Log.e("VoiceCommandManager", "Initialization error", e)
            }
        }
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceCommandManager", "Ready for speech (Session: $currentSessionId)")
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                Log.e("VoiceCommandManager", "Error: $error (Session: $currentSessionId)")
                isListening = false
                if (shouldRetry && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    handler.post { startListeningInternal() }
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    var handled = false
                    for (match in matches) {
                        if (onGlobalIntent?.invoke(match) == true) {
                            handled = true
                            break
                        }
                    }
                    if (!handled) {
                        onResult?.invoke(matches[0])
                    }
                }
                isListening = false
                if (shouldRetry) {
                    handler.post { startListeningInternal() }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    var handled = false
                    for (match in matches) {
                        if (onGlobalIntent?.invoke(match) == true) {
                            handled = true
                            break
                        }
                    }
                    if (!handled) {
                        onPartialResult?.invoke(matches[0])
                    }
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(isVietnamese: Boolean = true, retry: Boolean = true, partialCallback: ((String) -> Unit)? = null, callback: (String) -> Unit): Int {
        currentSessionId++
        handler.post {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("VoiceCommandManager", "RECORD_AUDIO permission not granted")
                    return@post
                }
                onResult = callback
                onPartialResult = partialCallback
                lastIsVietnamese = isVietnamese
                shouldRetry = retry

                if (speechRecognizer != null) {
                    speechRecognizer?.cancel()
                    startListeningInternal()
                } else {
                    Log.e("VoiceCommandManager", "Recognizer is null, cannot start")
                }
            } catch (e: Throwable) {
                Log.e("VoiceCommandManager", "Error starting listener", e)
            }
        }
        return currentSessionId
    }

    private fun startListeningInternal() {
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lastIsVietnamese) "vi-VN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopCurrentSession() {
        handler.post {
            shouldRetry = false
            speechRecognizer?.cancel()
            isListening = false
        }
    }

    fun stopListening(sessionId: Int) {
        handler.post {
            if (sessionId == currentSessionId) {
                stopCurrentSession()
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
