package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.utils.HistoryManager
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OCRScreen(
    onBack: () -> Unit,
    ttsManager: TTSManager,
    historyManager: HistoryManager
) {
    val haptic = LocalHapticFeedback.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val spokenTextSet = remember { mutableStateOf(mutableSetOf<String>()) }
    var isPaused by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Initializing...") }
    var guidanceText by remember { mutableStateOf("") }

    BackHandler {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onBack()
    }

    LaunchedEffect(Unit) {
        ttsManager.speak("OCR active", isVietnamese = false)
        statusText = "Scanning for text..."
    }

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
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isPaused) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageAnalyzer = { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    if (visionText.textBlocks.isEmpty()) {
                                        statusText = "No text found"
                                        guidanceText = "Move closer or hold steady"
                                    } else {
                                        statusText = "Text detected"
                                        guidanceText = ""
                                        
                                        visionText.textBlocks.forEach { block ->
                                            val originalText = block.text.trim()
                                            val normalizedText = originalText.lowercase().replace(Regex("[^a-z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]"), "")
                                            
                                            if (normalizedText.length > 3) {
                                                val alreadySpoken = spokenTextSet.value.any { spoken ->
                                                    isSimilar(normalizedText, spoken)
                                                }
                                                
                                                if (!alreadySpoken) {
                                                    spokenTextSet.value.add(normalizedText)
                                                    val isVietnamese = containsVietnamese(originalText)
                                                    
                                                    // Guidance based on block position
                                                    val rect = block.boundingBox
                                                    if (rect != null) {
                                                        val centerX = rect.centerX().toFloat() / image.width
                                                        val side = when {
                                                            centerX < 0.33f -> "on the left"
                                                            centerX > 0.66f -> "on the right"
                                                            else -> "in the center"
                                                        }
                                                        val announcement = "$originalText $side"
                                                        ttsManager.speak(announcement, isQueued = true, isVietnamese = isVietnamese)
                                                        historyManager.addHistory("OCR", originalText)
                                                    } else {
                                                        ttsManager.speak(originalText, isQueued = true, isVietnamese = isVietnamese)
                                                        historyManager.addHistory("OCR", originalText)
                                                    }
                                                }
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
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Paused", color = Color.White, fontSize = 32.sp)
                }
            }

            // Status and Guidance Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                if (guidanceText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = guidanceText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 14.sp
                        )
                    }
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

private fun containsVietnamese(text: String): Boolean {
    val viChars = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ"
    return text.lowercase().any { it in viChars }
}

fun isSimilar(s1: String, s2: String): Boolean {
    if (s1.contains(s2) || s2.contains(s1)) return true
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
