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
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    isActive: Boolean = true,
    onProvideAnalyzer: (ImageAnalysis.Analyzer?) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val recognizer = remember(isActive) {
        if (isActive) TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) else null
    }

    var isProcessing by remember { mutableStateOf(false) }
    val lastDetectedAmount = remember { mutableStateOf("") }
    val lastDetectionTime = remember { mutableLongStateOf(0L) }
    val DEBOUNCE_MS = 3000L

    fun formatCurrency(amountStr: String, isVietnamese: Boolean): String {
        val amount = amountStr.replace(".", "").replace(",", "").toLongOrNull() ?: return ""
        
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
                else -> {
                    // Generic fallback for other numbers
                    "$amount đồng"
                }
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
                else -> amount.toString()
            }
            return "$words vietnamese dong"
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
                            val fullText = visionText.text
                            // Regex to find potential currency numbers (e.g., 1000, 10.000, 500,000)
                            val numberRegex = Regex("\\b(\\d{1,3}([.,]\\d{3})*|\\d+)\\b")
                            val matches = numberRegex.findAll(fullText)
                            
                            for (match in matches) {
                                val found = match.value
                                val cleanNum = found.replace(".", "").replace(",", "")
                                
                                // Basic filter to avoid small numbers or non-currency numbers
                                if (cleanNum.length >= 4 && (cleanNum.startsWith("1") || cleanNum.startsWith("2") || cleanNum.startsWith("5"))) {
                                    val currentTime = System.currentTimeMillis()
                                    if (cleanNum != lastDetectedAmount.value || currentTime - lastDetectionTime.value > DEBOUNCE_MS) {
                                        val spokenText = formatCurrency(cleanNum, settingsManager.useVietnamese)
                                        if (spokenText.isNotEmpty()) {
                                            ttsManager.speak(spokenText, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                                            historyManager.addHistory("Currency", spokenText)
                                            lastDetectedAmount.value = cleanNum
                                            lastDetectionTime.value = currentTime
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

    val currencyGreeting = stringResource(R.string.currency_greeting)
    LaunchedEffect(isActive) {
        if (isActive) {
            ttsManager.speak(
                currencyGreeting,
                isVietnamese = settingsManager.useVietnamese
            )
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
