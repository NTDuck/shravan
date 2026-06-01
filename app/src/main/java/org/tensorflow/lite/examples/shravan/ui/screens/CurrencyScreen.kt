package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun CurrencyScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    isActive: Boolean = true,
    onProvideAnalyzer: (ImageAnalysis.Analyzer?) -> Unit = {}
) {
    val context = LocalContext.current
    val useVietnamese = settingsManager.useVietnamese
    
    val yoloAnalyzer = remember {
        YoloAnalyzer(
            context = context,
            ttsManager = ttsManager,
            settingsManager = settingsManager,
            historyManager = historyManager,
            modelName = "currency.tflite"
        )
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            onProvideAnalyzer(yoloAnalyzer)
        } else {
            onProvideAnalyzer(null)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            yoloAnalyzer.close()
        }
    }

    val scope = rememberCoroutineScope()
    var interactionsEnabled by remember { mutableStateOf(false) }
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    androidx.activity.compose.BackHandler {
        hapticManager.triggerHaptic()
        onBack()
    }

    val currencyGreeting = stringResource(R.string.currency_greeting)
    val backCommand = stringResource(R.string.back_command)

    LaunchedEffect(isActive) {
        if (isActive) {
            ttsManager.speak(
                currencyGreeting,
                isVietnamese = useVietnamese,
                onComplete = {
                    scope.launch {
                        delay(1000)
                        interactionsEnabled = true
                    }
                }
            )
            
            voiceSessionId.value = voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains(backCommand.lowercase())) {
                    ttsManager.speak(backCommand, isVietnamese = useVietnamese)
                    hapticManager.triggerHaptic()
                    onBack()
                }
            }
        } else {
            interactionsEnabled = false
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId.value = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Overlay UI if needed
    }
}
