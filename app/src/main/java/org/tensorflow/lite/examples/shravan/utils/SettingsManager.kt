package org.tensorflow.lite.examples.shravan.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shravan_prefs", Context.MODE_PRIVATE)

    var speechRate by mutableStateOf(prefs.getFloat("speech_rate", 1.25f))
        private set

    fun updateSpeechRate(value: Float) {
        speechRate = value
        prefs.edit().putFloat("speech_rate", value).apply()
    }

    var vibrationEnabled by mutableStateOf(prefs.getBoolean("vibration_enabled", true))
        private set

    fun updateVibrationEnabled(value: Boolean) {
        vibrationEnabled = value
        prefs.edit().putBoolean("vibration_enabled", value).apply()
    }

    var confidenceThreshold by mutableStateOf(prefs.getFloat("confidence_threshold", 0.5f))
        private set

    fun updateConfidenceThreshold(value: Float) {
        confidenceThreshold = value
        prefs.edit().putFloat("confidence_threshold", value).apply()
    }

    var isContinuousScan by mutableStateOf(prefs.getBoolean("continuous_scan", true))
        private set

    fun updateContinuousScan(value: Boolean) {
        isContinuousScan = value
        prefs.edit().putBoolean("continuous_scan", value).apply()
    }

    var isFirstLaunch by mutableStateOf(prefs.getBoolean("is_first_launch", true))
        private set

    fun updateFirstLaunch(value: Boolean) {
        isFirstLaunch = value
        prefs.edit().putBoolean("is_first_launch", value).apply()
    }

    var highContrastMode by mutableStateOf(prefs.getBoolean("high_contrast", false))
        private set

    fun updateHighContrastMode(value: Boolean) {
        highContrastMode = value
        prefs.edit().putBoolean("high_contrast", value).apply()
    }

    var useVietnamese by mutableStateOf(prefs.getBoolean("use_vietnamese", false))
        private set

    fun updateUseVietnamese(value: Boolean) {
        useVietnamese = value
        prefs.edit().putBoolean("use_vietnamese", value).apply()
    }
}
