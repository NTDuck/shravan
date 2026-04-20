package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onOCRClick: () -> Unit,
    ttsManager: TTSManager
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        ttsManager.speak("Home", isVietnamese = false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCameraClick()
                        },
                        onLongClick = {
                            ttsManager.speak("Object Detection", isVietnamese = false)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Object Detection",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondary)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOCRClick()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ttsManager.speak("OCR", isVietnamese = false)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OCR",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
