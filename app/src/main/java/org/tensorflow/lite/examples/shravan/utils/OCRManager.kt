package org.tensorflow.lite.examples.shravan.utils

import android.content.Context

class OCRManager(context: Context) {
    private val spokenTextTimes = mutableMapOf<String, Long>()
    private val DEBOUNCE_MS = 10000L // 10 seconds

    fun shouldSpeak(text: String): Boolean {
        val normalized = text.lowercase().trim()
        if (normalized.length <= 3) return false
        
        val currentTime = System.currentTimeMillis()
        val lastSpoken = spokenTextTimes[normalized] ?: 0L
        
        if (currentTime - lastSpoken < DEBOUNCE_MS) return false
        
        spokenTextTimes[normalized] = currentTime
        return true
    }

    fun clear() {
        spokenTextTimes.clear()
    }
}
