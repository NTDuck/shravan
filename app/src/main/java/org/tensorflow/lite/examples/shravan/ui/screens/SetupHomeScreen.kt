package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.ui.components.AccessibleButton
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun SetupHomeScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    var setupStep by remember { mutableIntStateOf(0) } // 0: Language, 1: Impairment
    var interactionsEnabled by remember { mutableStateOf(false) }
    var isScreenDimmed by remember { mutableStateOf(true) }
    var selectedLevel by remember { mutableStateOf<ImpairmentLevel?>(null) }
    
    val scope = rememberCoroutineScope()
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }

    // String resources
    val langPrompt = stringResource(R.string.setup_lang_prompt)
    val langEn = stringResource(R.string.lang_choice_en)
    val langVi = stringResource(R.string.lang_choice_vi)
    val kwEn = stringResource(R.string.voice_keyword_en).lowercase()
    val kwVi = stringResource(R.string.voice_keyword_vi).lowercase()

    val greeting = stringResource(R.string.welcome_text)
    val welcomePartial = stringResource(R.string.welcome_partial)
    val welcomeTotal = stringResource(R.string.welcome_total)
    val labelPartial = stringResource(R.string.label_partial)
    val labelTotal = stringResource(R.string.label_total)
    val keywordPartial = stringResource(R.string.voice_keyword_partial).lowercase()
    val keywordTotal = stringResource(R.string.voice_keyword_total).lowercase()

    LaunchedEffect(setupStep) {
        interactionsEnabled = false
        isScreenDimmed = true
        
        val textToSpeak = if (setupStep == 0) {
            // Speak in a mix or just English then Vietnamese?
            // Let's use the prompt which is localized but at step 0 it's usually English if not set.
            // Actually, we should speak both for clarity.
            "Please choose your language: English or Vietnamese? Vui lòng chọn ngôn ngữ: Tiếng Anh hay Tiếng Việt?"
        } else {
            greeting
        }

        ttsManager.speak(
            textToSpeak,
            isVietnamese = settingsManager.useVietnamese,
            onComplete = {
                scope.launch {
                    isScreenDimmed = false
                    delay(500)
                    interactionsEnabled = true
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceSessionId.value?.let { voiceCommandManager.stopListening(it) }
        }
    }

    LaunchedEffect(interactionsEnabled, setupStep) {
        if (interactionsEnabled) {
            voiceSessionId.value = voiceCommandManager.startListening(
                isVietnamese = settingsManager.useVietnamese,
                partialCallback = { partial ->
                    val lowerPartial = partial.trim().lowercase()
                    if (setupStep == 0) {
                        if (lowerPartial == kwEn || lowerPartial == "english") {
                            settingsManager.useVietnamese = false
                            ttsManager.setLanguage(false)
                            setupStep = 1
                        } else if (lowerPartial == kwVi || lowerPartial == "vietnamese" || lowerPartial == "tiếng việt") {
                            settingsManager.useVietnamese = true
                            ttsManager.setLanguage(true)
                            setupStep = 1
                        }
                    } else {
                        if (lowerPartial == keywordPartial) {
                            handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, welcomePartial, settingsManager.useVietnamese)
                        } else if (lowerPartial == keywordTotal) {
                            handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, welcomeTotal, settingsManager.useVietnamese)
                        }
                    }
                }
            ) { result ->
                val lowerResult = result.trim().lowercase()
                if (setupStep == 0) {
                    if (lowerResult == kwEn || lowerResult == "english") {
                        settingsManager.useVietnamese = false
                        ttsManager.setLanguage(false)
                        setupStep = 1
                    } else if (lowerResult == kwVi || lowerResult == "vietnamese" || lowerResult == "tiếng việt") {
                        settingsManager.useVietnamese = true
                        ttsManager.setLanguage(true)
                        setupStep = 1
                    }
                } else {
                    if (lowerResult == keywordPartial) {
                        handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, welcomePartial, settingsManager.useVietnamese)
                    } else if (lowerResult == keywordTotal) {
                        handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, welcomeTotal, settingsManager.useVietnamese)
                    }
                }
            }
        } else {
            voiceSessionId.value?.let {
                voiceCommandManager.stopListening(it)
                voiceSessionId.value = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(if (isScreenDimmed) Color.Black.copy(alpha = 0.8f) else Color(0xFF333333))) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (setupStep == 0) {
                AccessibleButton(
                    label = langEn,
                    speakLabel = langEn,
                    enabled = interactionsEnabled,
                    onClick = {
                        settingsManager.useVietnamese = false
                        ttsManager.setLanguage(false)
                        setupStep = 1
                    },
                    ttsManager = ttsManager,
                    settingsManager = settingsManager,
                    hapticManager = hapticManager,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    backgroundColor = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(12.dp))
                AccessibleButton(
                    label = langVi,
                    speakLabel = langVi,
                    enabled = interactionsEnabled,
                    onClick = {
                        settingsManager.useVietnamese = true
                        ttsManager.setLanguage(true)
                        setupStep = 1
                    },
                    ttsManager = ttsManager,
                    settingsManager = settingsManager,
                    hapticManager = hapticManager,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    backgroundColor = Color(0xFF222222)
                )
            } else {
                AccessibleButton(
                    label = labelPartial,
                    speakLabel = labelPartial,
                    enabled = interactionsEnabled,
                    onClick = {
                        handleSelection(ImpairmentLevel.PartiallyImpaired, settingsManager, navController, ttsManager, welcomePartial, settingsManager.useVietnamese)
                    },
                    ttsManager = ttsManager,
                    settingsManager = settingsManager,
                    hapticManager = hapticManager,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    backgroundColor = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(12.dp))
                AccessibleButton(
                    label = labelTotal,
                    speakLabel = labelTotal,
                    enabled = interactionsEnabled,
                    onClick = {
                        handleSelection(ImpairmentLevel.TotallyImpaired, settingsManager, navController, ttsManager, welcomeTotal, settingsManager.useVietnamese)
                    },
                    ttsManager = ttsManager,
                    settingsManager = settingsManager,
                    hapticManager = hapticManager,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    backgroundColor = Color(0xFF222222)
                )
            }
        }
    }
}

private fun handleSelection(
    level: ImpairmentLevel,
    settingsManager: SettingsManager,
    navController: NavController,
    ttsManager: TTSManager,
    confirmationText: String,
    useVietnamese: Boolean
) {
    settingsManager.updateImpairmentLevel(level)
    
    ttsManager.speak(confirmationText, isVietnamese = useVietnamese) {
        navController.navigate("main") {
            popUpTo("setup") { inclusive = true }
        }
    }
}
