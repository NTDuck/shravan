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
    
    // Set to keep track of unique text already enqueued/spoken in this session
    val spokenTextSet = remember { mutableSetOf<String>() }

    LaunchedEffect(Unit) {
        ttsManager.speak("OCR Screen")
    }

    // Clear buffer when exiting
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.stop()
        }
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
                                // Split into lines or blocks to enqueue
                                visionText.textBlocks.forEach { block ->
                                    val text = block.text.trim()
                                    if (text.isNotBlank() && !spokenTextSet.contains(text)) {
                                        spokenTextSet.add(text)
                                        // Enqueue text so it's not interrupted
                                        ttsManager.speak(text, isQueued = true)
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
