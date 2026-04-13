package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager
) {
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var lastSpokenText by remember { mutableStateOf("") }
    var lastSpeakTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        ttsManager.speak("OCR Screen")
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
                imageAnalyzer = { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val text = visionText.text
                                val currentTime = System.currentTimeMillis()
                                
                                // Only speak if text is different AND either TTS is not speaking OR enough time has passed
                                if (text.isNotBlank() && text != lastSpokenText) {
                                    if (!ttsManager.isSpeaking() || (currentTime - lastSpeakTime > 5000)) {
                                        lastSpokenText = text
                                        lastSpeakTime = currentTime
                                        ttsManager.speak(text)
                                    }
                                }
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
    }
}
