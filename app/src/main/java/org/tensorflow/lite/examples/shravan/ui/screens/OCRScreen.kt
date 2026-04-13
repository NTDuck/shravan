package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.ui.components.ShravanTopAppBar
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager
) {
    var detectedText by remember { mutableStateOf("") }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    LaunchedEffect(Unit) {
        ttsManager.speak("OCR Screen")
    }

    Scaffold(
        topBar = { ShravanTopAppBar("OCR / Text Reader") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageAnalyzer = { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    detectedText = visionText.text
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = if (detectedText.isEmpty()) "No text detected" else detectedText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (detectedText.isNotEmpty()) {
                        ttsManager.speak(detectedText)
                    } else {
                        ttsManager.speak("No text to read")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Read Aloud", fontWeight = FontWeight.Bold)
            }
        }
    }
}
