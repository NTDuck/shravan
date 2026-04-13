package org.tensorflow.lite.examples.shravan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import org.tensorflow.lite.examples.shravan.R
import org.tensorflow.lite.examples.shravan.utils.TTSManager

@Composable
fun SplashScreen(onTimeout: () -> Unit, ttsManager: TTSManager) {
    LaunchedEffect(Unit) {
        ttsManager.speak("Home")
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sharvan_logo),
            contentDescription = "Logo"
        )
    }
}
