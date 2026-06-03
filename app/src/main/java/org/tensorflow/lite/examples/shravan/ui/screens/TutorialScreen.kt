package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.utils.HapticManager
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import org.tensorflow.lite.examples.shravan.utils.VoiceCommandManager

@Composable
fun TutorialScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    onRequestPermissions: () -> Unit,
    hasPermissions: Boolean
) {
    val useVietnamese = settingsManager.useVietnamese
    val scope = rememberCoroutineScope()
    var isTutorialFinished by remember { mutableStateOf(false) }
    var requestingPermissions by remember { mutableStateOf(false) }
    
    val tutorialText = stringResource(R.string.tutorial_text)
    val skipInstruction = stringResource(R.string.tutorial_skip_instruction)
    val skipKeyword = stringResource(R.string.tutorial_skip_keyword).lowercase()
    
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }
    var interactionsEnabled by remember { mutableStateOf(false) }

    fun finishTutorial() {
        if (!isTutorialFinished) {
            ttsManager.stopAll()
            isTutorialFinished = true
            requestingPermissions = true
            onRequestPermissions()
            hapticManager.triggerHaptic()
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions && requestingPermissions) {
            navController.navigate("main") {
                popUpTo("tutorial") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        interactionsEnabled = true
        delay(500) // Small safety delay for navigation transition
        ttsManager.speak(tutorialText, isVietnamese = useVietnamese) {
            finishTutorial()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceSessionId.value?.let { voiceCommandManager.stopListening(it) }
        }
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled && !isTutorialFinished) {
            voiceSessionId.value = voiceCommandManager.startListening(
                isVietnamese = useVietnamese,
                partialCallback = { partial ->
                    if (partial.lowercase().contains(skipKeyword)) {
                        finishTutorial()
                    }
                }
            ) { result ->
                if (result.lowercase().contains(skipKeyword)) {
                    finishTutorial()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222))
            .then(
                if (interactionsEnabled && !isTutorialFinished) {
                    Modifier.clickable { finishTutorial() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (requestingPermissions) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Text(
                text = skipInstruction,
                color = Color.White,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}