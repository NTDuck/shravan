package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.theme.DimmedPalette
import org.tensorflow.lite.examples.shravan.utils.HistoryManager
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    settingsManager: SettingsManager,
    historyManager: HistoryManager
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }
    var isPaused by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Initializing...") }

    BackHandler {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onBack()
    }

    LaunchedEffect(Unit) {
        ttsManager.speak("Object Detection active", isVietnamese = false)
        statusText = "Scanning for objects..."
    }

    LaunchedEffect(recognitions) {
        if (recognitions.isEmpty() && !isPaused) {
            statusText = "Scanning..."
        } else if (!isPaused) {
            statusText = "${recognitions.size} objects detected"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isPaused) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageAnalyzer = YoloAnalyzer(context, ttsManager, settingsManager, historyManager) { results ->
                        recognitions = results
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Paused", color = Color.White, fontSize = 32.sp)
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (!isPaused) {
                    recognitions.forEach { recognition ->
                        val rect = recognition.location
                        val scaleX = size.width / 416f
                        val scaleY = size.height / 416f
                        
                        val color = DimmedPalette[recognition.detectedClass % DimmedPalette.size]
                        
                        val left = rect.left * scaleX
                        val top = rect.top * scaleY
                        val width = rect.width() * scaleX
                        val height = rect.height() * scaleY

                        drawRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        drawContext.canvas.nativeCanvas.apply {
                            val labelText = "${recognition.title} ${(recognition.confidence * 100).toInt()}%"
                            val paint = android.graphics.Paint().apply {
                                this.color = android.graphics.Color.argb(
                                    (color.alpha * 255).toInt(),
                                    (color.red * 255).toInt(),
                                    (color.green * 255).toInt(),
                                    (color.blue * 255).toInt()
                                )
                                this.textSize = 16.sp.toPx()
                                this.isFakeBoldText = true
                                this.textAlign = android.graphics.Paint.Align.CENTER
                            }
                            
                            val xPos = left + width / 2
                            val yPos = if (top > 30f) top - 10f else top + 30f
                            
                            drawText(labelText, xPos, yPos, paint)
                        }
                    }
                }
            }

            // Status Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 18.sp
                    )
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlCircleButton(
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    label = if (isPaused) "Resume" else "Pause",
                    onClick = { 
                        isPaused = !isPaused 
                        ttsManager.speak(if (isPaused) "Paused" else "Resumed", isVietnamese = false)
                    }
                )
                ControlCircleButton(
                    icon = Icons.Default.Refresh,
                    label = "Repeat",
                    onClick = { ttsManager.repeatLast() }
                )
                ControlCircleButton(
                    icon = Icons.Default.Stop,
                    label = "Stop",
                    onClick = { 
                        ttsManager.stop()
                        onBack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier
            .size(80.dp)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(40.dp))
        }
    }
}
