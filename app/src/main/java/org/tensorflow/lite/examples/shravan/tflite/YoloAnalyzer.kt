package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.examples.shravan.utils.ImageUtils
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import java.util.*

class YoloAnalyzer(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val onResults: (List<Classifier.Recognition>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: YoloV5Classifier by lazy {
        DetectorFactory.getDetector(context.assets, "yolov5s-fp16.tflite")
    }

    private val queue: Queue<String> = LinkedList()

    override fun analyze(image: ImageProxy) {
        val bitmap = ImageUtils.toBitmap(image)
        if (bitmap == null) {
            image.close()
            return
        }

        // Rotate and scale bitmap to match detector input
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        val scaledBitmap = Bitmap.createScaledBitmap(
            rotatedBitmap, detector.inputSize, detector.inputSize, true
        )

        val results = detector.recognizeImage(scaledBitmap)
        
        // Filter results with confidence > 0.5
        val filteredResults = results.filter { it.confidence > 0.5f }

        // TTS Logic: Speak the object name if it's new
        filteredResults.forEach { result ->
            if (!queue.contains(result.title)) {
                if (queue.size >= 2) {
                    queue.poll()
                }
                queue.add(result.title)
                ttsManager.speak(result.title, isQueued = true)
            }
        }

        onResults(filteredResults)
        
        image.close()
    }
}
