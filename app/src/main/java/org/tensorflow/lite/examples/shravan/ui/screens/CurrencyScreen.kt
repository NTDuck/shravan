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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
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
    val recognizer = remember(isActive) {
        if (isActive) TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) else null
    }

    val useVietnamese = settingsManager.useVietnamese
    var isProcessing by remember { mutableStateOf(false) }
    val spokenTextTimes = remember { mutableMapOf<String, Long>() }
    val DEBOUNCE_MS = 10000L // 10 seconds like OCR

    val allowedDenominations = remember {
        setOf(1000L, 2000L, 5000L, 10000L, 20000L, 50000L, 100000L, 200000L, 500000L)
    }

    fun formatCurrency(amountStr: String, isVietnamese: Boolean): String {
        val amount = amountStr.replace(".", "").replace(",", "").toLongOrNull() ?: return ""
        if (amount !in allowedDenominations) return ""
        
        if (isVietnamese) {
            return when (amount) {
                1000L -> "một nghìn đồng"
                2000L -> "hai nghìn đồng"
                5000L -> "năm nghìn đồng"
                10000L -> "mười nghìn đồng"
                20000L -> "hai mươi nghìn đồng"
                50000L -> "năm mươi nghìn đồng"
                100000L -> "một trăm nghìn đồng"
                200000L -> "hai trăm nghìn đồng"
                500000L -> "năm trăm nghìn đồng"
                else -> ""
            }
        } else {
            val words = when (amount) {
                1000L -> "one thousand"
                2000L -> "two thousand"
                5000L -> "five thousand"
                10000L -> "ten thousand"
                20000L -> "twenty thousand"
                50000L -> "fifty thousand"
                100000L -> "one hundred thousand"
                200000L -> "two hundred thousand"
                500000L -> "five hundred thousand"
                else -> ""
            }
            return if (words.isNotEmpty()) "$words vietnamese dong" else ""
        }
    }

    LaunchedEffect(isActive, recognizer) {
        if (isActive && recognizer != null) {
            onProvideAnalyzer { imageProxy ->
                if (isProcessing) {
                    imageProxy.close()
                    return@onProvideAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    isProcessing = true
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            // Regex to find potential currency numbers (e.g., 1000, 10.000, 500,000)
                            val numberRegex = Regex("(\\d{1,3}([.,]\\d{3})*|\\d+)")
                            
                            visionText.textBlocks.forEach { block ->
                                val matches = numberRegex.findAll(block.text)
                                for (match in matches) {
                                    val cleanNum = match.value.replace(".", "").replace(",", "")
                                    val spokenText = formatCurrency(cleanNum, settingsManager.useVietnamese)
                                    
                                    if (spokenText.isNotEmpty()) {
                                        val currentTime = System.currentTimeMillis()
                                        val lastSpoken = spokenTextTimes[cleanNum] ?: 0L
                                        
                                        if (currentTime - lastSpoken >= DEBOUNCE_MS) {
                                            ttsManager.speak(spokenText, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                                            historyManager.addHistory("Currency", spokenText)
                                            spokenTextTimes[cleanNum] = currentTime
                                            if (settingsManager.hapticsEnabled) {
                                                hapticManager.triggerHaptic()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .addOnCompleteListener {
                            isProcessing = false
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }
        } else {
            onProvideAnalyzer(null)
        }
    }

    DisposableEffect(isActive) {
        onDispose {
            recognizer?.close()
        }
    }

    val scope = rememberCoroutineScope()
    var interactionsEnabled by remember { mutableStateOf(false) }
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    androidx.activity.compose.BackHandler {
        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
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
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
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
