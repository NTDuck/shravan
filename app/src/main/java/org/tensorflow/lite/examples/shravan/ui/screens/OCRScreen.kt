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
import kotlin.math.min

@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager
) {
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    
    // Set to keep track of unique text already enqueued/spoken in this session
    val spokenTextSet = remember { mutableStateOf(mutableSetOf<String>()) }

    LaunchedEffect(Unit) {
        ttsManager.speak("OCR")
    }

    // Clear buffer when exiting
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.stop()
            spokenTextSet.value.clear()
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
                                visionText.textBlocks.forEach { block ->
                                    val originalText = block.text.trim()
                                    // Normalize: lowercase and remove non-alphanumeric for comparison
                                    val normalizedText = originalText.lowercase().replace(Regex("[^a-z0-9]"), "")
                                    
                                    if (normalizedText.length > 3) {
                                        // Check for similarity with already spoken texts
                                        val alreadySpoken = spokenTextSet.value.any { spoken ->
                                            isSimilar(normalizedText, spoken)
                                        }
                                        
                                        if (!alreadySpoken) {
                                            spokenTextSet.value.add(normalizedText)
                                            ttsManager.speak(originalText, isQueued = true)
                                        }
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

/**
 * Simple similarity check: 
 * Returns true if one is a substring of another OR if Levenshtein distance is small.
 */
fun isSimilar(s1: String, s2: String): Boolean {
    if (s1.contains(s2) || s2.contains(s1)) return true
    
    // Levenshtein distance threshold: 20% of the length of the shorter string
    val maxDist = (min(s1.length, s2.length) * 0.2).toInt().coerceAtLeast(1)
    return levenshtein(s1, s2) <= maxDist
}

fun levenshtein(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j
    
    for (i in 1..s1.length) {
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = min(min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost)
        }
    }
    return dp[s1.length][s2.length]
}
