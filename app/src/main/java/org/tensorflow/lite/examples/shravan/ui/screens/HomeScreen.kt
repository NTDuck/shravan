package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onOCRClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onHelpClick: () -> Unit,
    ttsManager: TTSManager,
    settingsManager: SettingsManager
) {
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(Unit) {
        if (settingsManager.isFirstLaunch) {
            ttsManager.speak(
                "Welcome to Shravan. Tap the top half to detect objects. Tap the middle half to read text. " +
                "The bottom bar contains Help, Settings, and History. Long press any button to hear its name.",
                isVietnamese = false
            )
            settingsManager.updateFirstLaunch(false)
        } else {
            ttsManager.speak("Home", isVietnamese = false)
        }
    }

    val backgroundColor = if (settingsManager.highContrastMode) Color.Black else MaterialTheme.colorScheme.background
    val primaryColor = if (settingsManager.highContrastMode) Color.Yellow else MaterialTheme.colorScheme.primary
    val secondaryColor = if (settingsManager.highContrastMode) Color.Cyan else MaterialTheme.colorScheme.secondary
    val onPrimaryColor = if (settingsManager.highContrastMode) Color.Black else MaterialTheme.colorScheme.onPrimary
    val onSecondaryColor = if (settingsManager.highContrastMode) Color.Black else MaterialTheme.colorScheme.onSecondary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Object Detection Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(primaryColor)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCameraClick()
                        },
                        onLongClick = {
                            ttsManager.speak("Detect Objects", isVietnamese = false)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Detect Objects",
                    color = onPrimaryColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            // OCR Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(secondaryColor)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOCRClick()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ttsManager.speak("Read Text", isVietnamese = false)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Read Text",
                    color = onSecondaryColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Layer: Help, Settings, History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Info,
                    label = "Help",
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = onHelpClick,
                    onLongClick = { ttsManager.speak("Help", isVietnamese = false) }
                )
                SmallActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onSettingsClick,
                    onLongClick = { ttsManager.speak("Settings", isVietnamese = false) }
                )
                SmallActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    label = "History",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onHistoryClick,
                    onLongClick = { ttsManager.speak("History", isVietnamese = false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmallActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
