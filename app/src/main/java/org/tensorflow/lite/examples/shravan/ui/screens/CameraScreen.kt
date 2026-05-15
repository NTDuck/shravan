package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.components.ControlCircleButton
import org.tensorflow.lite.examples.shravan.ui.theme.DimmedPalette
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    val context = LocalContext.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }
    var isPaused by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Initializing...") }
    val useVietnamese = settingsManager.useVietnamese

    val scope = rememberCoroutineScope()
    var interactionsEnabled by remember { mutableStateOf(false) }

    BackHandler {
        if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
        onBack()
    }

    LaunchedEffect(Unit) {
        ttsManager.speak(
            if (useVietnamese) "Chụp ảnh" else "Camera",
            isVietnamese = useVietnamese,
            onComplete = {
                scope.launch {
                    delay(1000)
                    interactionsEnabled = true
                }
            }
        )
        statusText = "Scanning..."
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceCommandManager.stopListening()
        }
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled) {
            voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains("quay lại") || lowerResult.contains("back")) {
                    ttsManager.speak(if (useVietnamese) "Quay lại" else "Back", isVietnamese = useVietnamese)
                    if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                    onBack()
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isPaused) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageAnalyzer = YoloAnalyzer(context, ttsManager, settingsManager, historyManager) { results ->
                        recognitions = results
                    }
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (!isPaused) {
                    recognitions.forEach { recognition ->
                        val rect = recognition.location
                        val scaleX = size.width / 416f
                        val scaleY = size.height / 416f
                        val color = DimmedPalette[recognition.detectedClass % DimmedPalette.size]
                        
                        drawRect(
                            color = color,
                            topLeft = Offset(rect.left * scaleX, rect.top * scaleY),
                            size = Size(rect.width() * scaleX, rect.height() * scaleY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlCircleButton(
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    label = if (isPaused) "Resume" else "Pause",
                    enabled = interactionsEnabled,
                    onClick = { 
                        isPaused = !isPaused 
                        if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                    }
                )
                ControlCircleButton(
                    icon = Icons.Default.Refresh,
                    label = "Repeat",
                    enabled = interactionsEnabled,
                    onClick = { 
                        ttsManager.repeatLast() 
                        if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                    }
                )
                ControlCircleButton(
                    icon = Icons.Default.Stop,
                    label = "Stop",
                    enabled = interactionsEnabled,
                    onClick = { 
                        if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                        onBack() 
                    }
                )
            }
        }
    }
}
