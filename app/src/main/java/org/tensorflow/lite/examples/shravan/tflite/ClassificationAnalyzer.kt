package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.examples.shravan.utils.HistoryManager
import org.tensorflow.lite.examples.shravan.utils.ImageUtils
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import java.io.BufferedReader
import java.io.InputStreamReader

class ClassificationAnalyzer(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val settingsManager: SettingsManager,
    private val historyManager: HistoryManager,
    private val modelName: String = "currency.tflite",
    private val labelFile: String = "currency_labels.txt",
    private val inputSize: Int = 224,
    var onResults: ((List<Classifier.Recognition>) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    private val classifier: TFLiteImageClassifier by lazy {
        TFLiteImageClassifier.create(
            context.assets,
            modelName,
            if (settingsManager.useVietnamese) labelFile.replace(".txt", "_vi.txt") else labelFile,
            inputSize,
            false // Assuming float model for now
        )
    }

    private val lastSeenTime = mutableMapOf<String, Long>()
    private val DEBOUNCE_TIME = 3000L
    private var isClosed = false

    override fun analyze(image: ImageProxy) {
        if (isClosed) {
            image.close()
            return
        }

        try {
            val bitmap = ImageUtils.toBitmap(image) ?: return
            
            val matrix = Matrix()
            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            val scaledBitmap = Bitmap.createScaledBitmap(
                rotatedBitmap, inputSize, inputSize, true
            )

            val results = classifier.recognizeImage(scaledBitmap)
            val topResult = results.firstOrNull()

            if (topResult != null && topResult.confidence > 0.7f) {
                // Ignore "000000" or background class if it's the top result
                if (topResult.title != "000000" && topResult.title != "Background") {
                    val currentTime = System.currentTimeMillis()
                    val lastSeen = lastSeenTime[topResult.title] ?: 0L
                    
                    if (currentTime - lastSeen > DEBOUNCE_TIME) {
                        ttsManager.speak(topResult.title, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                        historyManager.addHistory("Currency", topResult.title)
                        lastSeenTime[topResult.title] = currentTime
                    }
                }
            }

            onResults?.invoke(results)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    fun close() {
        isClosed = true
        classifier.close()
    }
}
