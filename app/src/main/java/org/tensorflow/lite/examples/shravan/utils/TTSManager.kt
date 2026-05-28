package org.tensorflow.lite.examples.shravan.utils

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val localeVi = Locale("vi", "VN")
    private var lastSpokenText: String = ""
    private var lastIsVietnamese: Boolean = false
    private var currentSpeechRate: Float = 1.0f
    private var onCompletionListener: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private data class PendingSpeakRequest(
        val text: String,
        val isQueued: Boolean,
        val isVietnamese: Boolean,
        val onComplete: (() -> Unit)?
    )
    private val pendingRequests = mutableListOf<PendingSpeakRequest>()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "The Language not supported!")
            } else {
                tts?.setSpeechRate(currentSpeechRate)
                setupProgressListener()
                isInitialized = true
                mainHandler.post {
                    val requestsToPlay = ArrayList(pendingRequests)
                    pendingRequests.clear()
                    for (request in requestsToPlay) {
                        speak(request.text, request.isQueued, request.isVietnamese, request.onComplete)
                    }
                }
            }
        } else {
            Log.e("TTSManager", "Initialization Failed!")
            mainHandler.post {
                val requestsToPlay = ArrayList(pendingRequests)
                pendingRequests.clear()
                for (request in requestsToPlay) {
                    request.onComplete?.invoke()
                }
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    onCompletionListener?.invoke()
                    onCompletionListener = null
                }
            }
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    onCompletionListener?.invoke()
                    onCompletionListener = null
                }
            }
        })
    }

    fun setLanguage(isVietnamese: Boolean) {
        lastIsVietnamese = isVietnamese
        val locale = if (isVietnamese) Locale("vi") else Locale.US
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTSManager", "Language not supported: $locale")
            if (isVietnamese) {
                // Fallback attempt
                tts?.setLanguage(Locale("vi", "VN"))
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun speak(text: String, isQueued: Boolean = false, isVietnamese: Boolean = false, onComplete: (() -> Unit)? = null) {
        if (text.isBlank()) {
            onComplete?.invoke()
            return
        }
        if (isInitialized) {
            this.onCompletionListener = onComplete
            lastSpokenText = text
            lastIsVietnamese = isVietnamese
            setLanguage(isVietnamese)
            val queueMode = if (isQueued) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
            
            val params = Bundle()
            try {
                // Set speakerphone on for VOICE_CALL stream
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                audioManager?.isSpeakerphoneOn = true
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id")
                params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_VOICE_CALL)
            } catch (e: Exception) {
                Log.e("TTSManager", "Error setting audio parameters", e)
            }
            
            try {
                tts?.speak(text, queueMode, params, "id")
            } catch (e: Exception) {
                Log.e("TTSManager", "Error in speak call", e)
                onComplete?.invoke()
                onCompletionListener = null
            }
        } else {
            pendingRequests.add(PendingSpeakRequest(text, isQueued, isVietnamese, onComplete))
        }
    }

    fun repeatLast() {
        if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText, isQueued = false, isVietnamese = lastIsVietnamese)
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    fun stop() {
        tts?.stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
    }
}
