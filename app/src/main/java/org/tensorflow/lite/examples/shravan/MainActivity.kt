package org.tensorflow.lite.examples.shravan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import org.tensorflow.lite.examples.shravan.ui.screens.*
import org.tensorflow.lite.examples.shravan.ui.theme.ShravanTheme
import org.tensorflow.lite.examples.shravan.utils.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent {
            val context = LocalContext.current
            
            val settingsManager = remember { 
                try { SettingsManager(context) } catch (e: Exception) { null }
            }
            val historyManager = remember { 
                try { HistoryManager(context) } catch (e: Exception) { null }
            }
            val ttsManager = remember { 
                try { TTSManager(context) } catch (e: Exception) { null }
            }
            val hapticManager = remember { 
                try { HapticManager(context) } catch (e: Exception) { null }
            }
            val voiceCommandManager = remember { 
                try { VoiceCommandManager(context) } catch (e: Exception) { null }
            }

            if (settingsManager == null || historyManager == null || ttsManager == null || 
                hapticManager == null || voiceCommandManager == null) {
                // Critical failure, show simple error
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    androidx.compose.material3.Text("Failed to initialize components")
                }
                return@setContent
            }
            
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

                val startDestination = if (settingsManager.impairmentLevel == null) "setup" else "main"

                NavHost(
                    navController = navController, 
                    startDestination = startDestination
                ) {
                    composable("setup") {
                        SetupHomeScreen(
                            navController = navController,
                            settingsManager = settingsManager,
                            ttsManager = ttsManager,
                            hapticManager = hapticManager,
                            voiceCommandManager = voiceCommandManager
                        )
                    }
                    composable("main") {
                        if (hasCameraPermission && hasRecordAudioPermission) {
                            MainScreen(
                                settingsManager = settingsManager,
                                ttsManager = ttsManager,
                                hapticManager = hapticManager,
                                voiceCommandManager = voiceCommandManager,
                                historyManager = historyManager,
                                onReset = {
                                    settingsManager.clearAll()
                                    historyManager.clearHistory()
                                    navController.navigate("setup") {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        } else {
                            // Show loading or permission request state
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
