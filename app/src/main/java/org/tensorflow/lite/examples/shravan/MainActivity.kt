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
import org.tensorflow.lite.examples.shravan.utils.HistoryManager
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val historyManager = remember { HistoryManager(context) }
            val ttsManager = remember { TTSManager(context) }
            
            ShravanTheme(darkTheme = settingsManager.highContrastMode) {
                val navController = rememberNavController()

                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        hasCameraPermission = granted
                    }
                )

                LaunchedEffect(Unit) {
                    if (!hasCameraPermission) {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                    ttsManager.setSpeechRate(settingsManager.speechRate)
                }

                DisposableEffect(Unit) {
                    onDispose {
                        ttsManager.destroy()
                    }
                }

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }, ttsManager = ttsManager)
                    }
                    composable("home") {
                        HomeScreen(
                            onCameraClick = { navController.navigate("camera") },
                            onOCRClick = { navController.navigate("ocr") },
                            onSettingsClick = { navController.navigate("settings") },
                            onHistoryClick = { navController.navigate("history") },
                            onHelpClick = { ttsManager.speak("Help screen coming soon", isVietnamese = false) },
                            ttsManager = ttsManager,
                            settingsManager = settingsManager
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            settingsManager = settingsManager,
                            ttsManager = ttsManager
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            onBack = { navController.popBackStack() },
                            historyManager = historyManager
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
                                historyManager = historyManager
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
                                historyManager = historyManager
                            )
                        }
                    }
                }
            }
        }
    }
}
