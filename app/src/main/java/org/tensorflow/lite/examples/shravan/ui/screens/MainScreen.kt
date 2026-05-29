package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.tflite.YoloAnalyzer
import org.tensorflow.lite.examples.shravan.ui.components.CameraPreview
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager,
    historyManager: HistoryManager,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeThreshold = remember { with(density) { 50.dp.toPx() } }
    var totalDrag by remember { mutableStateOf(0f) }
    var isCameraReady by remember { mutableStateOf(false) }

    // Cached Analyzers to avoid re-loading models
    val yoloAnalyzer = remember { YoloAnalyzer(context, ttsManager, settingsManager, historyManager) }
    val currencyAnalyzer = remember { YoloAnalyzer(context, ttsManager, settingsManager, historyManager, modelName = "currency.tflite") }
    
    var activeAnalyzer by remember { mutableStateOf<ImageAnalysis.Analyzer?>(null) }
    var ocrAnalyzer by remember { mutableStateOf<ImageAnalysis.Analyzer?>(null) }

    val screens = listOf(
        Triple(R.string.nav_explore, Icons.Default.CameraAlt, "explore"),
        Triple(R.string.nav_find, Icons.Default.Search, "find"),
        Triple(R.string.nav_ocr, Icons.Default.TextFields, "ocr"),
        Triple(R.string.nav_currency, Icons.Default.MonetizationOn, "currency"),
        Triple(R.string.nav_settings, Icons.Default.Settings, "settings"),
        Triple(R.string.nav_history, Icons.Default.History, "history")
    )

    val kExplore = stringResource(R.string.voice_keyword_explore).lowercase()
    val kFind = stringResource(R.string.voice_keyword_find).lowercase()
    val kOcr = stringResource(R.string.voice_keyword_ocr).lowercase()
    val kCurrency = stringResource(R.string.voice_keyword_currency).lowercase()
    val kSettings = stringResource(R.string.voice_keyword_settings).lowercase()
    val kHistory = stringResource(R.string.voice_keyword_history).lowercase()

    val navKeywords = remember(kExplore, kFind, kOcr, kCurrency, kSettings, kHistory) {
        listOf(
            kExplore to 0,
            kFind to 1,
            kOcr to 2,
            kCurrency to 3,
            kSettings to 4,
            kHistory to 5
        )
    }

    // Single Haptic Trigger
    LaunchedEffect(currentPage) {
        if (settingsManager.hapticsEnabled) {
            hapticManager.triggerHaptic()
        }
        // Prepare analyzer for the new page
        activeAnalyzer = when (currentPage) {
            0 -> {
                yoloAnalyzer.allowedClasses = null
                yoloAnalyzer
            }
            1 -> yoloAnalyzer // FindScreen sets allowedClasses
            2 -> ocrAnalyzer
            3 -> currencyAnalyzer
            else -> null
        }
        
        // If moving away from camera pages, reset camera ready
        if (currentPage >= 4) {
            isCameraReady = false
        }
    }

    // Centralized Voice Navigation Listener
    DisposableEffect(settingsManager.useVietnamese) {
        voiceCommandManager.onGlobalIntent = { result ->
            if (ttsManager.isSpeaking()) {
                false
            } else {
                val lowerResult = result.lowercase()
                val matched = navKeywords.find { lowerResult.contains(it.first) }
                if (matched != null) {
                    if (currentPage != matched.second) {
                        currentPage = matched.second
                    }
                    true // handled
                } else {
                    false // not handled
                }
            }
        }
        onDispose {
            voiceCommandManager.onGlobalIntent = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            yoloAnalyzer.close()
            currencyAnalyzer.close()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.second, contentDescription = stringResource(screen.first), modifier = Modifier.size(32.dp)) },
                        label = null,
                        selected = currentPage == index,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        onClick = {
                            currentPage = index
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { totalDrag = 0f },
                        onDragCancel = { totalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                            if (java.lang.Math.abs(totalDrag) > swipeThreshold) {
                                val targetPage = if (totalDrag < 0) {
                                    (currentPage + 1).coerceAtMost(5)
                                } else {
                                    (currentPage - 1).coerceAtLeast(0)
                                }
                                if (targetPage != currentPage) {
                                    currentPage = targetPage
                                }
                                totalDrag = 0f
                            }
                        }
                    )
                }
        ) {
            // Shared CameraPreview layer
            if (currentPage < 4) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    zoomRatio = 0.6f,
                    imageAnalyzer = activeAnalyzer,
                    onReady = { isCameraReady = true }
                )
            }

            // Crossfade between Spinner and Camera
            if (currentPage < 4) {
                AnimatedVisibility(
                    visible = !isCameraReady,
                    enter = fadeIn(),
                    exit = fadeOut(animationSpec = tween(durationMillis = 500))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF222222)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Page Content with slide transitions
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    val isFindTransition = targetState == 1 || initialState == 1
                    if (isFindTransition) {
                        // Fade only for Find Screen transitions
                        fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(500))
                    } else {
                        // Slide for everything else
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() with slideOutHorizontally { it } + fadeOut()
                        }
                    }.using(SizeTransform(clip = false))
                }
            ) { targetPage ->
                Box(modifier = Modifier.fillMaxSize()) {
                    val isActive = currentPage == targetPage
                    when (targetPage) {
                        0 -> ExploreScreen(
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager,
                            settingsManager = settingsManager,
                            historyManager = historyManager,
                            isActive = isActive,
                            yoloAnalyzer = yoloAnalyzer
                        )
                        1 -> FindScreen(
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager,
                            settingsManager = settingsManager,
                            historyManager = historyManager,
                            isActive = isActive,
                            yoloAnalyzer = yoloAnalyzer
                        )
                        2 -> OCRScreen(
                            onBack = { currentPage = 0 },
                            ttsManager = ttsManager,
                            settingsManager = settingsManager,
                            historyManager = historyManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager,
                            isActive = isActive,
                            onProvideAnalyzer = { ocrAnalyzer = it }
                        )
                        3 -> CurrencyScreen(
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager,
                            settingsManager = settingsManager,
                            historyManager = historyManager,
                            isActive = isActive,
                            yoloAnalyzer = currencyAnalyzer
                        )
                        4 -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF222222)).padding(innerPadding)) {
                            SettingsScreen(ttsManager, hapticManager, voiceCommandManager, settingsManager, onReset, isActive = isActive)
                        }
                        5 -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF222222)).padding(innerPadding)) {
                            HistoryScreen(onBack = { currentPage = 0 }, historyManager = historyManager, settingsManager = settingsManager, ttsManager = ttsManager, hapticManager = hapticManager, voiceCommandManager = voiceCommandManager, isActive = isActive)
                        }
                    }
                }
            }
        }
    }
}
