package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    settingsManager: SettingsManager,
    onReset: () -> Unit,
    isActive: Boolean = true
) {
    var languageExpanded by remember { mutableStateOf(false) }
    var flashExpanded by remember { mutableStateOf(false) }
    var resetClickCount by remember { mutableStateOf(0) }

    val languages = listOf("English" to false, "Tiếng Việt" to true)
    val flashes = listOf(
        stringResource(R.string.flash_auto) to "auto",
        stringResource(R.string.flash_on) to "on",
        stringResource(R.string.flash_off) to "off"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings), 
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Language
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (settingsManager.useVietnamese) "Tiếng Việt" else "English",
                onValueChange = {},
                readOnly = true,
                enabled = false, // Use Box for click
                label = { Text(stringResource(R.string.settings_language)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                modifier = Modifier.fillMaxWidth().clickable { languageExpanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color.White,
                    disabledLabelColor = Color.White,
                    disabledTrailingIconColor = Color.White
                )
            )
            DropdownMenu(
                expanded = languageExpanded,
                onDismissRequest = { languageExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF333333))
            ) {
                languages.forEach { (label, isVi) ->
                    DropdownMenuItem(
                        text = { Text(label, color = Color.White) },
                        onClick = {
                            settingsManager.useVietnamese = isVi
                            ttsManager.setLanguage(isVi)
                            languageExpanded = false
                        }
                    )
                }
            }
        }

        // Haptics
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.settings_haptics), color = Color.White, fontSize = 18.sp)
            Switch(
                checked = settingsManager.hapticsEnabled,
                onCheckedChange = { settingsManager.hapticsEnabled = it }
            )
        }

        // Speech Rate
        Column {
            Text(stringResource(R.string.settings_speech_rate) + ": ${"%.1f".format(settingsManager.speechRate)}x", color = Color.White, fontSize = 18.sp)
            Slider(
                value = settingsManager.speechRate,
                onValueChange = { 
                    settingsManager.speechRate = it
                    ttsManager.setSpeechRate(it)
                },
                valueRange = 0.5f..2.0f,
                steps = 14
            )
        }

        // Flash
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = flashes.find { it.second == settingsManager.flashMode }?.first ?: stringResource(R.string.flash_auto),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(stringResource(R.string.settings_flash)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flashExpanded) },
                modifier = Modifier.fillMaxWidth().clickable { flashExpanded = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color.White,
                    disabledLabelColor = Color.White,
                    disabledTrailingIconColor = Color.White
                )
            )
            DropdownMenu(
                expanded = flashExpanded,
                onDismissRequest = { flashExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF333333))
            ) {
                flashes.forEach { (label, mode) ->
                    DropdownMenuItem(
                        text = { Text(label, color = Color.White) },
                        onClick = {
                            settingsManager.flashMode = mode
                            flashExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Reset
        Button(
            onClick = {
                resetClickCount++
                if (resetClickCount >= 7) {
                    if (settingsManager.hapticsEnabled) hapticManager.triggerHaptic()
                    onReset()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.settings_reset) + if (resetClickCount > 0) " ($resetClickCount/7)" else "", fontSize = 18.sp, color = Color.White)
        }
    }
}
