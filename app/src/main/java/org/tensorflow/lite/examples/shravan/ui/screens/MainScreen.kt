package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.camera.core.ImageAnalysis
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeThreshold = remember { with(density) { 50.dp.toPx() } }
    var totalDrag by remember { mutableStateOf(0f) }

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

    LaunchedEffect(pagerState.currentPage) {
        if (settingsManager.hapticsEnabled) {
            hapticManager.triggerHaptic()
        }
    }

    LaunchedEffect(pagerState.currentPage, ocrAnalyzer) {
        // Update active analyzer based on page
        activeAnalyzer = when (pagerState.currentPage) {
            0 -> {
                yoloAnalyzer.allowedClasses = null
                yoloAnalyzer
            }
            1 -> yoloAnalyzer // FindScreen will set allowedClasses
            2 -> ocrAnalyzer
            3 -> currencyAnalyzer
            else -> null
        }
    }

    // Centralized Voice Navigation Listener
    DisposableEffect(settingsManager.useVietnamese) {
        voiceCommandManager.onGlobalIntent = { result ->
            val isSpeaking = ttsManager.isSpeaking()
            android.util.Log.d("SHRAVAN_NAV", "Global intent check: result=$result, isSpeaking=$isSpeaking")
            
            if (isSpeaking) {
                false
            } else {
                val lowerResult = result.lowercase()
                
                val matched = navKeywords.find { lowerResult.contains(it.first) }
                if (matched != null) {
                    if (pagerState.currentPage != matched.second) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(matched.second)
                        }
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
                tonalElevation = 0.dp,
                modifier = Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { totalDrag = 0f },
                        onDragCancel = { totalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                            if (java.lang.Math.abs(totalDrag) > swipeThreshold) {
                                val targetPage = if (totalDrag < 0) {
                                    (pagerState.currentPage + 1).coerceAtMost(5)
                                } else {
                                    (pagerState.currentPage - 1).coerceAtLeast(0)
                                }
                                if (targetPage != pagerState.currentPage) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetPage)
                                    }
                                }
                                totalDrag = 0f
                            }
                        }
                    )
                }
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.second, contentDescription = stringResource(screen.first), modifier = Modifier.size(32.dp)) },
                        label = null,
                        selected = pagerState.currentPage == index,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Shared CameraPreview behind the pager
            if (pagerState.currentPage < 4) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    zoomRatio = 0.6f,
                    imageAnalyzer = activeAnalyzer
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val isFindScreenTransition = page == 1 || pagerState.currentPage == 1
                
                val alpha = if (isFindScreenTransition) {
                    1f - Math.abs(pageOffset).coerceIn(0f, 1f)
                } else 1f

                Box(modifier = Modifier.graphicsLayer { 
                    this.alpha = alpha
                    if (isFindScreenTransition) {
                        // Counteract the sliding motion to create a stationary fade effect
                        translationX = pageOffset * size.width
                    }
                }) {
                    val isActive = pagerState.currentPage == page
                    when (page) {
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
                            onBack = {}, ttsManager = ttsManager, settingsManager = settingsManager, 
                            historyManager = historyManager, hapticManager = hapticManager, 
                            voiceCommandManager = voiceCommandManager, isActive = isActive,
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
                            HistoryScreen(onBack = {}, historyManager = historyManager, settingsManager = settingsManager, ttsManager = ttsManager, hapticManager = hapticManager, voiceCommandManager = voiceCommandManager, isActive = isActive)
                        }
                    }
                }
            }
        }
    }
}
