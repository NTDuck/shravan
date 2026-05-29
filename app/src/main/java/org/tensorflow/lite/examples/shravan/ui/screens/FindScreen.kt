package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import org.tensorflow.lite.examples.shravan.ui.theme.InterFontFamily
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

    val analyzer = remember(isActive, settingsManager.useVietnamese) {
        if (isActive) {
            YoloAnalyzer(context, ttsManager, settingsManager, historyManager) { results ->
                recognitions = results
            }.apply {
                this.allowedClasses = when (activeMode) {
                    "seatings & tables" -> listOf("chair", "dining table", "sofa", "couch", "bàn ăn", "ghế", "ghế sofa")
                    "doors & windows" -> listOf("door", "window", "cửa")
                    "person & vehicles" -> listOf("person", "car", "bus", "truck", "motorcycle", "bicycle", "người", "xe hơi", "xe buýt", "xe tải", "xe máy", "xe đạp")
                    else -> emptyList()
                }
            }
        } else null
    }

    LaunchedEffect(activeMode) {
        analyzer?.allowedClasses = when (activeMode) {
            "seatings & tables" -> listOf("chair", "dining table", "sofa", "couch", "bàn ăn", "ghế", "ghế sofa")
            "doors & windows" -> listOf("door", "window", "cửa")
            "person & vehicles" -> listOf("person", "car", "bus", "truck", "motorcycle", "bicycle", "người", "xe hơi", "xe buýt", "xe tải", "xe máy", "xe đạp")
            else -> emptyList()
        }
    }

    val findPrompt = stringResource(org.tensorflow.lite.examples.shravan.R.string.find_prompt)
    val modeSeatingsTables = stringResource(org.tensorflow.lite.examples.shravan.R.string.mode_seatings_tables)
    val modeDoorsWindows = stringResource(org.tensorflow.lite.examples.shravan.R.string.mode_doors_windows)
    val modePersonVehicles = stringResource(org.tensorflow.lite.examples.shravan.R.string.mode_person_vehicles)
    
    LaunchedEffect(activeMode, isActive) {
        if (isActive) {
            if (activeMode == null) {
                ttsManager.speak(findPrompt, isVietnamese = settingsManager.useVietnamese)
                voiceSessionId = voiceCommandManager.startListening(isVietnamese = settingsManager.useVietnamese) { result ->
                    val lowerResult = result.lowercase()
                    if (lowerResult.contains(modeSeatingsTables.lowercase()) || lowerResult.contains("seat") || lowerResult.contains("table") || lowerResult.contains("bàn") || lowerResult.contains("ghế")) {
                        activeMode = "seatings & tables"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    } else if (lowerResult.contains(modeDoorsWindows.lowercase()) || lowerResult.contains("door") || lowerResult.contains("window") || lowerResult.contains("cửa")) {
                        activeMode = "doors & windows"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    } else if (lowerResult.contains(modePersonVehicles.lowercase()) || lowerResult.contains("person") || lowerResult.contains("vehicle") || lowerResult.contains("người") || lowerResult.contains("xe")) {
                        activeMode = "person & vehicles"
                        if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    }
                }
            } else {
                voiceSessionId?.let {
                    voiceCommandManager.stopListening(it)
                    voiceSessionId = null
                }
                val modeLabel = when (activeMode) {
                    "seatings & tables" -> modeSeatingsTables
                    "doors & windows" -> modeDoorsWindows
                    "person & vehicles" -> modePersonVehicles
                    else -> ""
                }
                val lookingForText = context.getString(org.tensorflow.lite.examples.shravan.R.string.find_looking_for, modeLabel)
                ttsManager.speak(lookingForText, isVietnamese = settingsManager.useVietnamese)
            }
        } else {
            voiceSessionId?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId = null
            }
        }
    }

    DisposableEffect(isActive) {
        onDispose {
            voiceSessionId?.let { voiceCommandManager.stopListening(it) }
        }
    }

    LaunchedEffect(recognitions, activeMode, isActive) {
        if (isActive && activeMode != null && recognitions.isNotEmpty()) {
            while (true) {
                val currentRecognitions = recognitions
                if (currentRecognitions.isEmpty()) break
                
                val closest = currentRecognitions.maxByOrNull { it.location.width() * it.location.height() }
                if (closest != null) {
                    val area = closest.location.width() * closest.location.height()
                    val maxArea = 416f * 416f
                    val distanceRatio = (1f - (area / maxArea)).coerceIn(0f, 1f)
                    val interval = (distanceRatio * 1000).toLong().coerceAtLeast(100L)
                    
                    if (settingsManager.hapticsEnabled) {
                        hapticManager.triggerHaptic()
                    }
                    delay(interval)
                } else {
                    break
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isActive) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                zoomRatio = 0.6f,
                imageAnalyzer = analyzer
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
                        style = TextStyle(
                            color = color, 
                            fontSize = 16.sp,
                            fontFamily = InterFontFamily
                        )
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 120.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1
            Box(
                modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (activeMode == "seatings & tables") MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)).clickable { activeMode = "seatings & tables"; if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Default.EventSeat, contentDescription = null, tint = if (activeMode == "seatings & tables") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                    Text("/", color = if (activeMode == "seatings & tables") Color.White else Color.LightGray, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    Icon(androidx.compose.material.icons.Icons.Default.TableRestaurant, contentDescription = null, tint = if (activeMode == "seatings & tables") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
            // Button 2
            Box(
                modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (activeMode == "doors & windows") MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)).clickable { activeMode = "doors & windows"; if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Default.Window, contentDescription = null, tint = if (activeMode == "doors & windows") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                    Text("/", color = if (activeMode == "doors & windows") Color.White else Color.LightGray, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    Icon(androidx.compose.material.icons.Icons.Default.MeetingRoom, contentDescription = null, tint = if (activeMode == "doors & windows") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
            // Button 3
            Box(
                modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (activeMode == "person & vehicles") MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)).clickable { activeMode = "person & vehicles"; if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null, tint = if (activeMode == "person & vehicles") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                    Text("/", color = if (activeMode == "person & vehicles") Color.White else Color.LightGray, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    Icon(androidx.compose.material.icons.Icons.Default.DirectionsCar, contentDescription = null, tint = if (activeMode == "person & vehicles") Color.White else Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
