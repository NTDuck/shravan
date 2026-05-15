package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.tensorflow.lite.examples.shravan.ui.components.AccessibleButton
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun SetupHomeScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    var interactionsEnabled by remember { mutableStateOf(false) }
    val useVietnamese = settingsManager.useVietnamese

    val greetingVi = "Xin chào. Chọn chế độ: khiếm thị một phần hoặc khiếm thị hoàn toàn?"
    val greetingEn = "Hello! Choose mode: Partially Impaired or Totally Impaired?"
    val labelPartialVi = "Khiếm thị một phần"
    val labelTotalVi = "Khiếm thị hoàn toàn"
    val labelPartialEn = "Partially Impaired"
    val labelTotalEn = "Totally Impaired"

    LaunchedEffect(Unit) {
        ttsManager.speak(
            if (useVietnamese) greetingVi else greetingEn,
            isVietnamese = useVietnamese,
            onComplete = {
                // Delay 1s after TTS
                interactionsEnabled = true
            }
        )
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled) {
            delay(1000)
            voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains(labelPartialVi.lowercase()) || lowerResult.contains(labelPartialEn.lowercase())) {
                    handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, hapticManager)
                } else if (lowerResult.contains(labelTotalVi.lowercase()) || lowerResult.contains(labelTotalEn.lowercase())) {
                    handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, hapticManager)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AccessibleButton(
            label = if (useVietnamese) labelPartialVi else labelPartialEn,
            speakLabel = if (useVietnamese) labelPartialVi else labelPartialEn,
            enabled = interactionsEnabled,
            onClick = {
                handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, hapticManager)
            },
            ttsManager = ttsManager,
            settingsManager = settingsManager,
            hapticManager = hapticManager,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        AccessibleButton(
            label = if (useVietnamese) labelTotalVi else labelTotalEn,
            speakLabel = if (useVietnamese) labelTotalVi else labelTotalEn,
            enabled = interactionsEnabled,
            onClick = {
                handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, hapticManager)
            },
            ttsManager = ttsManager,
            settingsManager = settingsManager,
            hapticManager = hapticManager,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}

private fun handleSelection(
    level: ImpairmentLevel,
    settingsManager: SettingsManager,
    navController: NavController,
    ttsManager: TTSManager,
    hapticManager: HapticManager
) {
    settingsManager.updateImpairmentLevel(level)
    val route = when (level) {
        ImpairmentLevel.PartiallyImpaired -> "partially_home"
        ImpairmentLevel.TotallyImpaired -> "totally_home"
    }
    navController.navigate(route) {
        popUpTo("setup") { inclusive = true }
    }
}
