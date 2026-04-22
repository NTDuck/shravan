package org.tensorflow.lite.examples.shravan.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val localeVi = Locale("vi", "VN")
    private var lastSpokenText: String = ""
    private var lastIsVietnamese: Boolean = false
    private var currentSpeechRate: Float = 1.25f

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
                isInitialized = true
            }
        } else {
            Log.e("TTSManager", "Initialization Failed!")
        }
    }

    fun setLanguage(isVietnamese: Boolean) {
        if (isInitialized) {
            val locale = if (isVietnamese) localeVi else Locale.US
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language $locale not supported!")
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun speak(text: String, isQueued: Boolean = false, isVietnamese: Boolean = false) {
        if (isInitialized && text.isNotBlank()) {
            lastSpokenText = text
            lastIsVietnamese = isVietnamese
            setLanguage(isVietnamese)
            val queueMode = if (isQueued) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
            tts?.speak(text, queueMode, null, null)
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
