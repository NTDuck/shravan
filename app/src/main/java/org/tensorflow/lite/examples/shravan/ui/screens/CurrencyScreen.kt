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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import org.tensorflow.lite.examples.shravan.R
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.core.ImageAnalysis
import org.tensorflow.lite.examples.shravan.tflite.RoboflowAnalyzer
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.theme.DimmedPalette
import org.tensorflow.lite.examples.shravan.ui.theme.InterFontFamily
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun CurrencyScreen(
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager,
    isActive: Boolean = true,
    yoloAnalyzer: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }
    val textMeasurer = rememberTextMeasurer()

    val currencyGreeting = stringResource(R.string.currency_greeting)

    LaunchedEffect(isActive) {
        if (isActive) {
            ttsManager.speak(
                currencyGreeting,
                isVietnamese = settingsManager.useVietnamese
            )
        }
    }

    LaunchedEffect(isActive, yoloAnalyzer) {
        if (isActive) {
            when (yoloAnalyzer) {
                is YoloAnalyzer -> {
                    yoloAnalyzer.onResults = { results ->
                        recognitions = results
                    }
                }
                is RoboflowAnalyzer -> {
                    yoloAnalyzer.onResults = { results ->
                        recognitions = results
                    }
                }
            }
        }
    }

    DisposableEffect(isActive, settingsManager.useVietnamese) {
        var sessionId: Int? = null
        if (isActive) {
            sessionId = voiceCommandManager.startListening(isVietnamese = settingsManager.useVietnamese) {}
        }
        onDispose {
            sessionId?.let { voiceCommandManager.stopListening(it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        if (isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                recognitions.forEach { recognition ->
                    val rect = recognition.location
                    val color = DimmedPalette[recognition.detectedClass % DimmedPalette.size]

                    if (rect != null) {
                        // Draw Bounding Box (YOLO)
                        val scaleX = size.width / 640f
                        val scaleY = size.height / 640f
                        
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
                    } else if (recognition.confidence > 0.6f && recognition.title != "000000" && recognition.title != "Background") {
                        // Draw prominent label for Classification
                        drawText(
                            textMeasurer = textMeasurer,
                            text = recognition.title,
                            topLeft = Offset(size.width / 2f - 50.dp.toPx(), size.height * 0.8f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontFamily = InterFontFamily,
                                background = Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}
