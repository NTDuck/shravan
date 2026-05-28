package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
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

    val greeting = stringResource(R.string.welcome_text)
    val welcomePartial = stringResource(R.string.welcome_partial)
    val welcomeTotal = stringResource(R.string.welcome_total)
    
    val labelPartial = stringResource(R.string.label_partial)
    val labelTotal = stringResource(R.string.label_total)
    
    val keywordPartial = stringResource(R.string.voice_keyword_partial).lowercase()
    val keywordTotal = stringResource(R.string.voice_keyword_total).lowercase()

    val scope = rememberCoroutineScope()
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        ttsManager.speak(
            greeting,
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
            val onIntentClarified = { matchedKeyword: String, level: ImpairmentLevel, welcomeText: String ->
                interactionsEnabled = false
                isScreenDimmed = true
                if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                handleSelection(level, settingsManager, navController, ttsManager, welcomeText, useVietnamese)
            }

            voiceSessionId.value = voiceCommandManager.startListening(
                isVietnamese = useVietnamese,
                partialCallback = { partial ->
                    val lowerPartial = partial.lowercase()
                    if (lowerPartial.contains(keywordPartial)) {
                        onIntentClarified(keywordPartial, ImpairmentLevel.PartiallyImpaired, welcomePartial)
                    } else if (lowerPartial.contains(keywordTotal)) {
                        onIntentClarified(keywordTotal, ImpairmentLevel.TotallyImpaired, welcomeTotal)
                    }
                }
            ) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains(keywordPartial)) {
                    onIntentClarified(keywordPartial, ImpairmentLevel.PartiallyImpaired, welcomePartial)
                } else if (lowerResult.contains(keywordTotal)) {
                    onIntentClarified(keywordTotal, ImpairmentLevel.TotallyImpaired, welcomeTotal)
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
                label = labelPartial,
                speakLabel = labelPartial,
                enabled = interactionsEnabled,
                onClick = {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, welcomePartial, useVietnamese)
                },
                ttsManager = ttsManager,
                settingsManager = settingsManager,
                hapticManager = hapticManager,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            AccessibleButton(
                label = labelTotal,
                speakLabel = labelTotal,
                enabled = interactionsEnabled,
                onClick = {
                    interactionsEnabled = false
                    isScreenDimmed = true
                    handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, welcomeTotal, useVietnamese)
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
    confirmationText: String,
    useVietnamese: Boolean
) {
    settingsManager.updateImpairmentLevel(level)
    
    ttsManager.speak(confirmationText, isVietnamese = useVietnamese) {
        navController.navigate("main") {
            popUpTo("setup") { inclusive = true }
        }
    }
}
