package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.examples.shravan.ui.theme.ThemeCatalog
import org.tensorflow.lite.examples.shravan.utils.*

@Composable
fun ThemesScreen(
    onBack: () -> Unit,
    settingsManager: SettingsManager,
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    voiceCommandManager: VoiceCommandManager
) {
    val useVietnamese = settingsManager.useVietnamese

    LaunchedEffect(Unit) {
        voiceCommandManager.startListening(isVietnamese = useVietnamese) { result ->
            val lowerResult = result.lowercase()
            if (lowerResult.contains("quay lại") || lowerResult.contains("back")) {
                ttsManager.speak(if (useVietnamese) "Quay lại" else "Back", isVietnamese = useVietnamese)
                if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                onBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (useVietnamese) "Thay đổi màu sắc" else "Themes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(ThemeCatalog.themes) { index, theme ->
                ThemeItem(
                    theme = theme,
                    isSelected = settingsManager.activeThemeIndex == index,
                    onClick = {
                        settingsManager.updateActiveThemeIndex(index)
                        ttsManager.speak(theme.name, isVietnamese = false)
                        if (settingsManager.vibrationEnabled) hapticManager.triggerHaptic()
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeItem(
    theme: org.tensorflow.lite.examples.shravan.ui.theme.AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = theme.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 8.dp)
            ) {
                theme.colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }
    }
}
