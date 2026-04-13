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
import org.tensorflow.lite.examples.shravan.ui.screens.CameraScreen
import org.tensorflow.lite.examples.shravan.ui.screens.HomeScreen
import org.tensorflow.lite.examples.shravan.ui.screens.OCRScreen
import org.tensorflow.lite.examples.shravan.ui.screens.SplashScreen
import org.tensorflow.lite.examples.shravan.ui.theme.ShravanTheme
import org.tensorflow.lite.examples.shravan.utils.TTSManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShravanTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val ttsManager = remember { TTSManager(context) }

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
                            ttsManager = ttsManager
                        )
                    }
                    composable("camera") {
                        if (hasCameraPermission) {
                            CameraScreen(
                                onBack = { 
                                    ttsManager.stop()
                                    navController.popBackStack() 
                                },
                                ttsManager = ttsManager
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
                                ttsManager = ttsManager
                            )
                        }
                    }
                }
            }
        }
    }
}
