package org.tensorflow.lite.examples.shravan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                        CameraScreen(
                            onBack = { 
                                ttsManager.stop()
                                navController.popBackStack() 
                            },
                            ttsManager = ttsManager
                        )
                    }
                    composable("ocr") {
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
