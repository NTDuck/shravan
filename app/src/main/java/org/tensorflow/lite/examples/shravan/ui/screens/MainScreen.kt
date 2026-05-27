package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.shravan.R
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
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val screens = listOf(
        Triple(R.string.nav_explore, Icons.Default.CameraAlt, "explore"),
        Triple(R.string.nav_find, Icons.Default.Search, "find"),
        Triple(R.string.nav_ocr, Icons.Default.TextFields, "ocr"),
        Triple(R.string.nav_currency, Icons.Default.MonetizationOn, "currency"),
        Triple(R.string.nav_settings, Icons.Default.Settings, "settings"),
        Triple(R.string.nav_history, Icons.Default.History, "history")
    )

    LaunchedEffect(pagerState.currentPage) {
        if (settingsManager.hapticsEnabled) {
            hapticManager.triggerHaptic()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.second, contentDescription = stringResource(screen.first)) },
                        label = { Text(stringResource(screen.first)) },
                        selected = pagerState.currentPage == index,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        onClick = {
                            if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Use a Box to allow HorizontalPager to take full screen if needed, 
        // but for now we follow the user's request for "whole screen" 
        // by removing the innerPadding from the pager itself if we want it to go under.
        // However, standard behavior is to respect innerPadding.
        // The user said "camera feed should take up whole screen", so I'll remove padding.
        
        HorizontalPager(
            pageCount = 6,
            state = pagerState,
            modifier = Modifier.fillMaxSize() // Removed padding(innerPadding)
        ) { page ->
            val isActive = pagerState.currentPage == page
            when (page) {
                0 -> ExploreScreen(ttsManager, hapticManager, voiceCommandManager, settingsManager, historyManager, isActive = isActive)
                1 -> FindScreen(ttsManager, hapticManager, voiceCommandManager, settingsManager, historyManager, isActive = isActive)
                2 -> OCRScreen(onBack = {}, ttsManager = ttsManager, settingsManager = settingsManager, historyManager = historyManager, hapticManager = hapticManager, voiceCommandManager = voiceCommandManager, isActive = isActive)
                3 -> CurrencyScreen(ttsManager, hapticManager, voiceCommandManager, settingsManager, historyManager, isActive = isActive)
                4 -> Box(modifier = Modifier.padding(innerPadding)) {
                    SettingsScreen(ttsManager, hapticManager, settingsManager, onReset, isActive = isActive)
                }
                5 -> Box(modifier = Modifier.padding(innerPadding)) {
                    HistoryScreen(onBack = {}, historyManager = historyManager, settingsManager = settingsManager, ttsManager = ttsManager, hapticManager = hapticManager, voiceCommandManager = voiceCommandManager, isActive = isActive)
                }
            }
        }
    }
}
