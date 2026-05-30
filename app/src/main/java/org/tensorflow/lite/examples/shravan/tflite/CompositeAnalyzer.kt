package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.examples.shravan.utils.DarknessDetector
import org.tensorflow.lite.examples.shravan.utils.SettingsManager

class CompositeAnalyzer(
    private val settingsManager: SettingsManager,
    private val onDarknessDetected: (Boolean) -> Unit,
    private val delegate: ImageAnalysis.Analyzer?
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        // 1. Darkness detection (always runs when camera is active)
        if (settingsManager.flashMode == "auto") {
            val isDark = DarknessDetector.isDark(image)
            onDarknessDetected(isDark)
        }
        
        // 2. Delegate to screen-specific analyzer
        if (delegate != null) {
            delegate.analyze(image)
        } else {
            image.close()
        }
    }
}
