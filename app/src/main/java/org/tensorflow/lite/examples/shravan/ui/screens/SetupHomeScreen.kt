package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var isScreenDimmed by remember { mutableStateOf(true) } // Dimmed during welcome text
    val useVietnamese = settingsManager.useVietnamese

    val greetingVi = "Chào mừng bạn đến với Shravan. Vui lòng nói yêu cầu của bạn: bạn bị khiếm thị một phần hay hoàn toàn?"
    val greetingEn = "Welcome to Shravan. Please speak your intent: are you partially blind or totally blind?"
    val labelPartialVi = "một phần"
    val labelTotalVi = "hoàn toàn"
    val labelPartialEn = "partially"
    val labelTotalEn = "totally"

    val scope = rememberCoroutineScope()
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        ttsManager.speak(
            if (useVietnamese) greetingVi else greetingEn,
            isVietnamese = useVietnamese,
            onComplete = {
                scope.launch {
                    isScreenDimmed = false
                    delay(500)
                    interactionsEnabled = true
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
            }
        }
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled) {
            voiceSessionId.value = voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains(labelPartialVi.lowercase()) || lowerResult.contains(labelPartialEn.lowercase())) {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, useVietnamese)
                } else if (lowerResult.contains(labelTotalVi.lowercase()) || lowerResult.contains(labelTotalEn.lowercase())) {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, useVietnamese)
                }
            }
        } else {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId.value = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(if (isScreenDimmed) Color.Black.copy(alpha = 0.8f) else Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AccessibleButton(
                label = if (useVietnamese) "Khiếm thị một phần" else "Partially Blind",
                speakLabel = if (useVietnamese) "Khiếm thị một phần" else "Partially Blind",
                enabled = interactionsEnabled,
                onClick = {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, useVietnamese)
                },
                ttsManager = ttsManager,
                settingsManager = settingsManager,
                hapticManager = hapticManager,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            AccessibleButton(
                label = if (useVietnamese) "Khiếm thị hoàn toàn" else "Totally Blind",
                speakLabel = if (useVietnamese) "Khiếm thị hoàn toàn" else "Totally Blind",
                enabled = interactionsEnabled,
                onClick = {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, useVietnamese)
                },
                ttsManager = ttsManager,
                settingsManager = settingsManager,
                hapticManager = hapticManager,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

private fun handleSelection(
    level: ImpairmentLevel,
    settingsManager: SettingsManager,
    navController: NavController,
    ttsManager: TTSManager,
    useVietnamese: Boolean
) {
    settingsManager.updateImpairmentLevel(level)
    val labelVi = if (level == ImpairmentLevel.PartiallyImpaired) "Đã nhận: Khiếm thị một phần" else "Đã nhận: Khiếm thị hoàn toàn"
    val labelEn = if (level == ImpairmentLevel.PartiallyImpaired) "Received: Partially Blind" else "Received: Totally Blind"
    val textToSpeak = if (useVietnamese) labelVi else labelEn
    
    ttsManager.speak(textToSpeak, isVietnamese = useVietnamese) {
        navController.navigate("main") {
            popUpTo("setup") { inclusive = true }
        }
    }
}
