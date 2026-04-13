package org.tensorflow.lite.examples.shravan.ui.screens

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.theme.DimmedPalette
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager
) {
    val context = LocalContext.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }

    LaunchedEffect(Unit) {
        ttsManager.speak("Object Detection")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                imageAnalyzer = YoloAnalyzer(context, ttsManager) { results ->
                    recognitions = results
                }
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                recognitions.forEach { recognition ->
                    val rect = recognition.location
                    val scaleX = size.width / 416f
                    val scaleY = size.height / 416f
                    
                    // Use dimmed palette
                    val color = DimmedPalette[recognition.detectedClass % DimmedPalette.size]
                    
                    val left = rect.left * scaleX
                    val top = rect.top * scaleY
                    val width = rect.width() * scaleX
                    val height = rect.height() * scaleY

                    // Draw bounding box (thinner)
                    drawRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Draw label centered above the box
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
    }
}
