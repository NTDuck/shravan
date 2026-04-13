package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onOCRClick: () -> Unit,
    ttsManager: TTSManager
) {
    LaunchedEffect(Unit) {
        ttsManager.speak("Home Screen")
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
            Button(
                onClick = onCameraClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Object Detection",
                    fontSize = 48.sp, // x2 (was 24)
                    fontWeight = FontWeight.Bold,
                    lineHeight = 56.sp
                )
            }
            
            Button(
                onClick = onOCRClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "OCR", // Label changed simply to OCR
                    fontSize = 48.sp, // x2 (was 24)
                    fontWeight = FontWeight.Bold,
                    lineHeight = 56.sp
                )
            }
        }
    }
}
