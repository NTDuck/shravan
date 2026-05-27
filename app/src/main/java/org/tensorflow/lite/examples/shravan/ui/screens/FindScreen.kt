package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.theme.DimmedPalette
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun FindScreen(
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    isActive: Boolean = true
) {
    val context = LocalContext.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }
    val textMeasurer = rememberTextMeasurer()
    
    var activeMode by remember { mutableStateOf<String?>(null) }
    val isMonotone = activeMode == null

    val scope = rememberCoroutineScope()
    var voiceSessionId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(activeMode, isActive) {
        if (isActive) {
            if (activeMode == null) {
                ttsManager.speak("What are you looking for? Seatings and tables, doors and windows, or person and vehicles?")
                voiceSessionId = voiceCommandManager.startListening(isVietnamese = settingsManager.useVietnamese) { result ->
                    val lowerResult = result.lowercase()
                    if (lowerResult.contains("seat") || lowerResult.contains("table") || lowerResult.contains("bàn") || lowerResult.contains("ghế")) {
                        activeMode = "seatings & tables"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    } else if (lowerResult.contains("door") || lowerResult.contains("window") || lowerResult.contains("cửa")) {
                        activeMode = "doors & windows"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    } else if (lowerResult.contains("person") || lowerResult.contains("vehicle") || lowerResult.contains("người") || lowerResult.contains("xe")) {
                        activeMode = "person & vehicles"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    }
                }
            } else {
                voiceSessionId?.let {
                    voiceCommandManager.stopListening(it)
                    voiceSessionId = null
                }
                ttsManager.speak("Looking for $activeMode")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceSessionId?.let { voiceCommandManager.stopListening(it) }
        }
    }

    LaunchedEffect(recognitions, activeMode, isActive) {
        if (isActive && activeMode != null && recognitions.isNotEmpty()) {
            val closest = recognitions.maxByOrNull { it.location.width() * it.location.height() }
            if (closest != null) {
                val area = closest.location.width() * closest.location.height()
                val maxArea = 416f * 416f
                val distanceRatio = 1f - (area / maxArea)
                val interval = (distanceRatio * 1000).toLong().coerceAtLeast(100L)
                
                if (settingsManager.hapticsEnabled) {
                    hapticManager.triggerHaptic()
                    delay(interval)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)) {
            if (isActive) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    zoomRatio = 0.6f,
                    imageAnalyzer = YoloAnalyzer(context, ttsManager, settingsManager, historyManager) { results ->
                        if (activeMode != null) {
                            recognitions = results.filter { 
                                when (activeMode) {
                                    "seatings & tables" -> it.title.lowercase() in listOf("chair", "dining table", "couch", "bàn", "ghế")
                                    "doors & windows" -> it.title.lowercase() in listOf("door", "window", "cửa")
                                    "person & vehicles" -> it.title.lowercase() in listOf("person", "car", "bus", "truck", "motorcycle", "người", "xe")
                                    else -> false
                                }
                            }
                        } else {
                            recognitions = emptyList()
                        }
                    }
                )

                if (isMonotone) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.8f)))
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    recognitions.forEach { recognition ->
                        val rect = recognition.location
                        val scaleX = size.width / 416f
                        val scaleY = size.height / 416f
                        val color = DimmedPalette[recognition.detectedClass % DimmedPalette.size]
                        
                        val topLeft = Offset(rect.left * scaleX, rect.top * scaleY)
                        val boxSize = Size(rect.width() * scaleX, rect.height() * scaleY)

                        drawRect(
                            color = color,
                            topLeft = topLeft,
                            size = boxSize,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        
                        drawText(
                            textMeasurer = textMeasurer,
                            text = recognition.title,
                            topLeft = Offset(topLeft.x, topLeft.y - 20.dp.toPx()),
                            style = TextStyle(color = color, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val mode1 = if (settingsManager.useVietnamese) "Bàn & Ghế" else "Seatings & Tables"
            val mode2 = if (settingsManager.useVietnamese) "Cửa & Cửa sổ" else "Doors & Windows"
            val mode3 = if (settingsManager.useVietnamese) "Người & Xe" else "Person & Vehicles"

            Button(
                onClick = { 
                    activeMode = "seatings & tables" 
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeMode == "seatings & tables") MaterialTheme.colorScheme.primary else Color.DarkGray
                )
            ) {
                Text(mode1, fontSize = 18.sp)
            }

            Button(
                onClick = { 
                    activeMode = "doors & windows" 
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeMode == "doors & windows") MaterialTheme.colorScheme.primary else Color.DarkGray
                )
            ) {
                Text(mode2, fontSize = 18.sp)
            }

            Button(
                onClick = { 
                    activeMode = "person & vehicles" 
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeMode == "person & vehicles") MaterialTheme.colorScheme.primary else Color.DarkGray
                )
            ) {
                Text(mode3, fontSize = 18.sp)
            }
        }
    }
}
