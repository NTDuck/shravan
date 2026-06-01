package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    isActive: Boolean = true,
    ocrManager: OCRManager? = null,
    onProvideAnalyzer: (androidx.camera.core.ImageAnalysis.Analyzer?) -> Unit = {}
) {
    val recognizer = remember(isActive) { 
        if (isActive) TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) else null 
    }
    
    val useVietnamese = settingsManager.useVietnamese
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(isActive, recognizer) {
        if (isActive) {
            android.util.Log.d("OCRScreen", "Setting up OCR analyzer")
            onProvideAnalyzer { imageProxy ->
                if (isProcessing) {
                    imageProxy.close()
                    return@onProvideAnalyzer
                }

                val mediaImage = imageProxy.image
                val rec = recognizer
                if (mediaImage != null && rec != null) {
                    isProcessing = true
                    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    rec.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val text = visionText.text
                            if (text.isNotBlank()) {
                                android.util.Log.d("OCRScreen", "OCR Success: ${text.take(20)}...")
                                visionText.textBlocks.forEach { block ->
                                    val originalText = block.text.trim()
                                    if (originalText.length > 2 && ocrManager?.shouldSpeak(originalText) == true) {
                                        android.util.Log.d("OCRScreen", "Speaking OCR: $originalText")
                                        ttsManager.speak(originalText, isQueued = true, isVietnamese = containsVietnamese(originalText))
                                        historyManager.addHistory("OCR", originalText)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("OCRScreen", "OCR Failed", e)
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
            android.util.Log.d("OCRScreen", "Removing OCR analyzer")
            onProvideAnalyzer(null)
        }
    }

    DisposableEffect(recognizer) {
        onDispose {
            recognizer?.close()
        }
    }

    val scope = rememberCoroutineScope()
    var interactionsEnabled by remember { mutableStateOf(false) }
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    BackHandler {
        hapticManager.triggerHaptic()
        onBack()
    }

    val ocrGreeting = stringResource(R.string.ocr_greeting)
    val backCommand = stringResource(R.string.back_command)

    LaunchedEffect(isActive) {
        if (isActive) {
            ttsManager.speak(
                ocrGreeting,
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        }
    }
}

private fun containsVietnamese(text: String): Boolean {
    val viChars = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ"
    return text.lowercase().any { it in viChars }
}
