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

    LaunchedEffect(isActive, recognizer) {
        if (isActive) {
            onProvideAnalyzer { imageProxy ->
                try {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        recognizer?.process(inputImage)
                            ?.addOnSuccessListener { visionText ->
                                visionText.textBlocks.forEach { block ->
                                    val originalText = block.text.trim()
                                    if (ocrManager?.shouldSpeak(originalText) == true) {
                                        ttsManager.speak(originalText, isQueued = true, isVietnamese = containsVietnamese(originalText))
                                        historyManager.addHistory("OCR", originalText)
                                    }
                                }
                            }
                            ?.addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OCRScreen", "Error during OCR processing", e)
                    imageProxy.close()
                }
            }
        } else {
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
        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
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
