package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settingsManager: SettingsManager,
    ttsManager: TTSManager
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Accessibility Settings", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)

            // Speech Rate
            Column {
                Text("Speech Rate: ${"%.2f".format(settingsManager.speechRate)}", fontSize = 18.sp)
                Slider(
                    value = settingsManager.speechRate,
                    onValueChange = { 
                        settingsManager.updateSpeechRate(it)
                        ttsManager.setSpeechRate(it)
                    },
                    valueRange = 0.5f..2.5f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Confidence Threshold
            Column {
                Text("Confidence Threshold: ${(settingsManager.confidenceThreshold * 100).toInt()}%", fontSize = 18.sp)
                Slider(
                    value = settingsManager.confidenceThreshold,
                    onValueChange = { 
                        settingsManager.updateConfidenceThreshold(it)
                    },
                    valueRange = 0.1f..0.9f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Vibration Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibration Feedback", fontSize = 18.sp)
                Switch(
                    checked = settingsManager.vibrationEnabled,
                    onCheckedChange = { 
                        settingsManager.updateVibrationEnabled(it)
                    }
                )
            }

            // High Contrast Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("High Contrast Mode", fontSize = 18.sp)
                Switch(
                    checked = settingsManager.highContrastMode,
                    onCheckedChange = { 
                        settingsManager.updateHighContrastMode(it)
                    }
                )
            }

            // Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Language: ${if (settingsManager.useVietnamese) "Tiếng Việt" else "English"}", fontSize = 18.sp)
                Switch(
                    checked = settingsManager.useVietnamese,
                    onCheckedChange = { 
                        settingsManager.updateUseVietnamese(it)
                    }
                )
            }
            
            Button(
                onClick = { ttsManager.speak("Test speech at this rate", isVietnamese = false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Voice")
            }
        }
    }
}
