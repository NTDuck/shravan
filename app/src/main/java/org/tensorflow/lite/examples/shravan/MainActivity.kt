package org.tensorflow.lite.examples.shravan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.tensorflow.lite.examples.shravan.ui.screens.*
import org.tensorflow.lite.examples.shravan.ui.theme.ShravanTheme
import org.tensorflow.lite.examples.shravan.utils.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val historyManager = remember { HistoryManager(context) }
            val ttsManager = remember { TTSManager(context) }
            val hapticManager = remember { HapticManager(context) }
            val voiceCommandManager = remember { VoiceCommandManager(context) }
            
            ShravanTheme(themeIndex = settingsManager.activeThemeIndex) {
                val navController = rememberNavController()

                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }
                
                var hasRecordAudioPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { permissions ->
                        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
                        hasRecordAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasRecordAudioPermission
                    }
                )

                LaunchedEffect(Unit) {
                    val permissionsNeeded = mutableListOf<String>()
                    if (!hasCameraPermission) permissionsNeeded.add(Manifest.permission.CAMERA)
                    if (!hasRecordAudioPermission) permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
                    
                    if (permissionsNeeded.isNotEmpty()) {
                        launcher.launch(permissionsNeeded.toTypedArray())
                    }
                    ttsManager.setSpeechRate(settingsManager.speechRate)
                }

                DisposableEffect(Unit) {
                    onDispose {
                        ttsManager.destroy()
                        voiceCommandManager.destroy()
                    }
                }

                val startDestination = when (settingsManager.impairmentLevel) {
                    ImpairmentLevel.PartiallyImpaired -> "partially_home"
                    ImpairmentLevel.TotallyImpaired -> "totally_home"
                    null -> "setup"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("setup") {
                        SetupHomeScreen(
                            navController = navController,
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("partially_home") {
                        PartiallyImpairedHomeScreen(
                            navController = navController,
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("totally_home") {
                        TotallyImpairedHomeScreen(
                            navController = navController,
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToThemes = { navController.navigate("themes") },
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("themes") {
                        ThemesScreen(
                            onBack = { navController.popBackStack() },
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            onBack = { navController.popBackStack() },
                            historyManager = historyManager,
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("camera") {
                        if (hasCameraPermission) {
                            CameraScreen(
                                onBack = { 
                                    ttsManager.stop()
                                    navController.popBackStack() 
                                },
                                ttsManager = ttsManager,
                                settingsManager = settingsManager,
                                historyManager = historyManager,
                                hapticManager = hapticManager,
                                voiceCommandManager = voiceCommandManager
                            )
                        }
                    }
                    composable("ocr") {
                        if (hasCameraPermission) {
                            OCRScreen(
                                onBack = { 
                                    ttsManager.stop()
                                    navController.popBackStack() 
                                },
                                ttsManager = ttsManager,
                                settingsManager = settingsManager,
                                historyManager = historyManager,
                                hapticManager = hapticManager,
                                voiceCommandManager = voiceCommandManager
                            )
                        }
                    }
                }
            }
        }
    }
}
