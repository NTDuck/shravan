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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.examples.shravan.tflite.Classifier
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.components.ShravanTopAppBar
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager
) {
    val context = LocalContext.current
    var recognitions by remember { mutableStateOf(emptyList<Classifier.Recognition>()) }

    LaunchedEffect(Unit) {
        ttsManager.speak("Camera Screen")
    }

    Scaffold(
        topBar = { ShravanTopAppBar("Object Detection") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                    // YOLO input is 416x416, we need to scale to canvas size
                    // This is a simplification; a proper Matrix transform would be better
                    val scaleX = size.width / 416f
                    val scaleY = size.height / 416f

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(rect.left * scaleX, rect.top * scaleY),
                        size = Size(rect.width() * scaleX, rect.height() * scaleY),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }
    }
}
