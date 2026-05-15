package org.tensorflow.lite.examples.shravan.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ImpairmentLevel {
    PartiallyImpaired,
    TotallyImpaired
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shravan_prefs", Context.MODE_PRIVATE)

    var impairmentLevel by mutableStateOf(
        prefs.getString("impairment_level", null)?.let { ImpairmentLevel.valueOf(it) }
    )
        private set

    fun updateImpairmentLevel(level: ImpairmentLevel?) {
        impairmentLevel = level
        prefs.edit().putString("impairment_level", level?.name).apply()
    }

    var activeThemeIndex by mutableStateOf(prefs.getInt("active_theme_index", 3)) // Default: Neon Hazard
        private set

    fun updateActiveThemeIndex(index: Int) {
        activeThemeIndex = index
        prefs.edit().putInt("active_theme_index", index).apply()
    }

    var speechRate by mutableStateOf(prefs.getFloat("speech_rate", 1.0f))
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

    var useVietnamese by mutableStateOf(prefs.getBoolean("use_vietnamese", true))
        private set

    fun updateUseVietnamese(value: Boolean) {
        useVietnamese = value
        prefs.edit().putBoolean("use_vietnamese", value).apply()
    }

    // Removed highContrastMode, confidenceThreshold, isContinuousScan, isFirstLaunch as they are not in the new specs
}
