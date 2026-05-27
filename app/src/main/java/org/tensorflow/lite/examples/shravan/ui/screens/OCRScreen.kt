package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.components.ControlCircleButton
import org.tensorflow.lite.examples.shravan.utils.*
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    isActive: Boolean = true
) {
    val recognizer = remember(isActive) { 
        if (isActive) TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) else null 
    }
    
    DisposableEffect(recognizer) {
        onDispose {
            recognizer?.close()
        }
    }

    val spokenTextSet = remember { mutableStateOf(mutableSetOf<String>()) }
    var isPaused by remember { mutableStateOf(false) }
    val useVietnamese = settingsManager.useVietnamese

    val scope = rememberCoroutineScope()
    var interactionsEnabled by remember { mutableStateOf(false) }
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    BackHandler {
        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
        onBack()
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            ttsManager.speak(
                if (useVietnamese) "Đọc chữ" else "OCR",
                isVietnamese = useVietnamese,
                onComplete = {
                    scope.launch {
                        delay(1000)
                        interactionsEnabled = true
                    }
                }
            )
        } else {
            interactionsEnabled = false
        }
    }

    DisposableEffect(isActive) {
        onDispose {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
            }
        }
    }

    LaunchedEffect(interactionsEnabled, isActive) {
        if (interactionsEnabled && isActive) {
            voiceSessionId.value = voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains("quay lại") || lowerResult.contains("back")) {
                    ttsManager.speak(if (useVietnamese) "Quay lại" else "Back", isVietnamese = useVietnamese)
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    onBack()
                }
            }
        } else {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId.value = null
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isPaused && isActive) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    zoomRatio = 0.6f,
                    imageAnalyzer = { imageProxy ->
                        try {
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                val visionText = recognizer?.let { com.google.android.gms.tasks.Tasks.await(it.process(image)) }
                                visionText?.textBlocks?.forEach { block ->
                                    val originalText = block.text.trim()
                                    val normalizedText = originalText.lowercase()
                                    if (normalizedText.length > 3 && !spokenTextSet.value.contains(normalizedText)) {
                                        spokenTextSet.value.add(normalizedText)
                                        ttsManager.speak(originalText, isQueued = true, isVietnamese = containsVietnamese(originalText))
                                        historyManager.addHistory("OCR", originalText)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("OCRScreen", "Error during OCR processing", e)
                        } finally {
                            imageProxy.close()
                        }
                    }
                )
            }
        }
    }
}

private fun containsVietnamese(text: String): Boolean {
    val viChars = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ"
    return text.lowercase().any { it in viChars }
}
