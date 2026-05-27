package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    ttsManager: TTSManager,
    hapticManager: HapticManager,
    settingsManager: SettingsManager,
    onReset: () -> Unit
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineMedium)

        // Language
        ExposedDropdownMenuBox(
            expanded = languageExpanded,
            onExpandedChange = { languageExpanded = !languageExpanded }
        ) {
            OutlinedTextField(
                value = if (settingsManager.useVietnamese) "Tiếng Việt" else "English",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_language)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = languageExpanded,
                onDismissRequest = { languageExpanded = false }
            ) {
                languages.forEach { (label, isVi) ->
                    DropdownMenuItem(
                        text = { Text(label) },
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
            Text(stringResource(R.string.settings_haptics))
            Switch(
                checked = settingsManager.hapticsEnabled,
                onCheckedChange = { settingsManager.hapticsEnabled = it }
            )
        }

        // Speech Rate
        Column {
            Text(stringResource(R.string.settings_speech_rate) + ": ${"%.1f".format(settingsManager.speechRate)}x")
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
        ExposedDropdownMenuBox(
            expanded = flashExpanded,
            onExpandedChange = { flashExpanded = !flashExpanded }
        ) {
            OutlinedTextField(
                value = flashes.find { it.second == settingsManager.flashMode }?.first ?: stringResource(R.string.flash_auto),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_flash)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flashExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = flashExpanded,
                onDismissRequest = { flashExpanded = false }
            ) {
                flashes.forEach { (label, mode) ->
                    DropdownMenuItem(
                        text = { Text(label) },
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.settings_reset) + if (resetClickCount > 0) " ($resetClickCount/7)" else "")
        }
    }
}
