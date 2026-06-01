package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
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
    
    val tutorialEn = "Welcome to the tutorial. This app has 5 main screens: Explore to detect objects around you, Find to locate specific items, OCR to read text, Settings to configure preferences, and History to view past detections. You can swipe left and right, or say the screen name to navigate. Say 'Skip' to end this tutorial and grant necessary permissions."
    val tutorialVi = "Chào mừng bạn đến với hướng dẫn. Ứng dụng này có 5 màn hình chính: Khám phá để nhận diện vật thể xung quanh, Tìm kiếm để tìm vật thể cụ thể, Đọc văn bản để đọc chữ, Cài đặt để tùy chỉnh, và Lịch sử để xem lại. Bạn có thể vuốt trái phải, hoặc đọc tên màn hình để chuyển. Nói 'Bỏ qua' để kết thúc và cấp quyền."
    
    val textToSpeak = if (useVietnamese) tutorialVi else tutorialEn
    val skipKeyword = if (useVietnamese) "bỏ qua" else "skip"
    
    val voiceSessionId = remember { mutableStateOf<Int?>(null) }
    var interactionsEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions && requestingPermissions) {
            navController.navigate("main") {
                popUpTo("tutorial") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        ttsManager.speak(textToSpeak, isVietnamese = useVietnamese) {
            if (!isTutorialFinished) {
                isTutorialFinished = true
                requestingPermissions = true
                onRequestPermissions()
            }
        }
        interactionsEnabled = true
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
                        ttsManager.stopAll()
                        isTutorialFinished = true
                        requestingPermissions = true
                        onRequestPermissions()
                        hapticManager.triggerHaptic()
                    }
                }
            ) { result ->
                if (result.lowercase().contains(skipKeyword)) {
                    ttsManager.stopAll()
                    isTutorialFinished = true
                    requestingPermissions = true
                    onRequestPermissions()
                    hapticManager.triggerHaptic()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        if (requestingPermissions) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Text(
                text = if (useVietnamese) "Đang phát hướng dẫn...\nNói 'Bỏ qua' để chuyển tiếp." else "Playing tutorial...\nSay 'Skip' to continue.",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}