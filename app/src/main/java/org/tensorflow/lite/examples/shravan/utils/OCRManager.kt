package org.tensorflow.lite.examples.shravan.utils

import android.content.Context

class OCRManager(context: Context) {
    private val spokenTextSet = mutableSetOf<String>()

    fun shouldSpeak(text: String): Boolean {
        val normalized = text.lowercase().trim()
        if (normalized.length <= 3) return false
        if (spokenTextSet.contains(normalized)) return false
        
        spokenTextSet.add(normalized)
        return true
    }

    fun clear() {
        spokenTextSet.clear()
    }
}
