package org.tensorflow.lite.examples.shravan.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToThemes: () -> Unit,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    val context = LocalContext.current
    val useVietnamese = settingsManager.useVietnamese
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var interactionsEnabled by remember { mutableStateOf(false) }
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        ttsManager.speak(
            if (useVietnamese) "Cài đặt" else "Settings",
            isVietnamese = useVietnamese,
            onComplete = {
                scope.launch {
                    delay(1000)
                    interactionsEnabled = true
                }
            }
        )
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled) {
            voiceSessionId.value = voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains("quay lại") || lowerResult.contains("back")) {
                    ttsManager.speak(if (useVietnamese) "Quay lại" else "Back", isVietnamese = useVietnamese)
                    if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                    onBack()
                }
            }
        } else {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId.value = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (useVietnamese) "Cài đặt" else "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Vibration Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (useVietnamese) "Cho phép rung" else "Enable vibration")
            Switch(
                checked = settingsManager.vibrationEnabled,
                enabled = interactionsEnabled,
                onCheckedChange = { 
                    settingsManager.updateVibrationEnabled(it)
                    if (it) hapticManager.triggerHaptic()
                }
            )
        }

        // Language Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (useVietnamese) "Tiếng Việt" else "English")
            Switch(
                checked = useVietnamese,
                enabled = interactionsEnabled,
                onCheckedChange = { settingsManager.updateUseVietnamese(it) }
            )
        }

        // Speech Rate Slider
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(if (useVietnamese) "Tốc độ nói" else "Speech rate")
            Slider(
                value = settingsManager.speechRate,
                enabled = interactionsEnabled,
                onValueChange = { 
                    settingsManager.updateSpeechRate(it)
                    ttsManager.setSpeechRate(it)
                },
                valueRange = 0.5f..2.0f
            )
        }

        // Themes Button
        Button(
            onClick = onNavigateToThemes,
            enabled = interactionsEnabled,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(if (useVietnamese) "Thay đổi màu sắc" else "Themes")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Music Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            IconButton(
                onClick = {
                    if (mediaPlayer == null) {
                        try {
                            val afd = context.assets.openFd("song.mp3")
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                prepare()
                                start()
                                isLooping = true
                            }
                        } catch (e: Exception) {
                            ttsManager.speak("Music file not found", isVietnamese = false)
                        }
                    } else {
                        if (mediaPlayer?.isPlaying == true) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                    }
                },
                enabled = interactionsEnabled,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (interactionsEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Play Music",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}
