package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.ui.components.AccessibleButton
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun PartiallyImpairedHomeScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    BaseHomeScreen(
        showBottomBar = true,
        navController = navController,
        settingsManager = settingsManager,
        ttsManager = ttsManager,
        hapticManager = hapticManager,
        voiceCommandManager = voiceCommandManager
    )
}

@Composable
fun TotallyImpairedHomeScreen(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    BaseHomeScreen(
        showBottomBar = false,
        navController = navController,
        settingsManager = settingsManager,
        ttsManager = ttsManager,
        hapticManager = hapticManager,
        voiceCommandManager = voiceCommandManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHomeScreen(
    showBottomBar: Boolean,
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    var interactionsEnabled by remember { mutableStateOf(false) }
    val useVietnamese = settingsManager.useVietnamese
    val scope = rememberCoroutineScope()

    val greetingVi = "màn hình chính"
    val greetingEn = "home screen"

    LaunchedEffect(Unit) {
        ttsManager.speak(
            if (useVietnamese) greetingVi else greetingEn,
            isVietnamese = useVietnamese,
            onComplete = {
                scope.launch {
                    delay(1000)
                    interactionsEnabled = true
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceCommandManager.stopListening()
        }
    }

    LaunchedEffect(interactionsEnabled) {
        if (interactionsEnabled) {
            voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
                val lowerResult = result.lowercase()
                if (lowerResult.contains("cài đặt") || lowerResult.contains("settings")) {
                    navController.navigate("settings")
                } else if (lowerResult.contains("lịch sử") || lowerResult.contains("history")) {
                    navController.navigate("history")
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    settingsManager = settingsManager,
                    ttsManager = ttsManager,
                    hapticManager = hapticManager,
                    enabled = interactionsEnabled
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AccessibleButton(
                label = "Camera",
                speakLabel = if (useVietnamese) "Chụp ảnh" else "Camera",
                enabled = interactionsEnabled,
                onClick = { navController.navigate("camera") },
                ttsManager = ttsManager,
                settingsManager = settingsManager,
                hapticManager = hapticManager,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            AccessibleButton(
                label = "OCR",
                speakLabel = if (useVietnamese) "Đọc chữ" else "OCR",
                enabled = interactionsEnabled,
                onClick = { navController.navigate("ocr") },
                ttsManager = ttsManager,
                settingsManager = settingsManager,
                hapticManager = hapticManager,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    enabled: Boolean
) {
    val useVietnamese = settingsManager.useVietnamese
    val items = listOf(
        NavigationItem("Home", if (useVietnamese) "Màn hình chính" else "Home", Icons.Default.Home, null),
        NavigationItem("Settings", if (useVietnamese) "Cài đặt" else "Settings", Icons.Default.Settings, "settings"),
        NavigationItem("History", if (useVietnamese) "Lịch sử" else "History", Icons.Default.History, "history")
    )

    NavigationBar {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(enabled) {
                        if (enabled) {
                            detectTapGestures(
                                onTap = {
                                    ttsManager.speak(item.speakLabel, isVietnamese = useVietnamese)
                                    if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                                    item.route?.let { navController.navigate(it) }
                                },
                                onLongPress = {
                                    ttsManager.speak(item.speakLabel, isVietnamese = useVietnamese)
                                    if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(item.icon, contentDescription = item.label)
                    Text(item.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val speakLabel: String,
    val icon: ImageVector,
    val route: String?
)
