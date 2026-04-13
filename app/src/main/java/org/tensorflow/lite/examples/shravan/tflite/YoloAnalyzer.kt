package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.examples.shravan.utils.ImageUtils
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class YoloAnalyzer(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val onResults: (List<Classifier.Recognition>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: YoloV5Classifier by lazy {
        DetectorFactory.getDetector(context.assets, "yolov5s-fp16.tflite")
    }

    private val labelsVi: List<String> by lazy {
        val labels = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open("coco_vi.txt")))
            var line: String? = reader.readLine()
            while (line != null) {
                labels.add(line)
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        labels
    }

    private val spokenObjects = mutableSetOf<String>()

    override fun analyze(image: ImageProxy) {
        val bitmap = ImageUtils.toBitmap(image)
        if (bitmap == null) {
            image.close()
            return
        }

        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        val scaledBitmap = Bitmap.createScaledBitmap(
            rotatedBitmap, detector.inputSize, detector.inputSize, true
        )

        val results = detector.recognizeImage(scaledBitmap)
        val filteredResults = results.filter { it.confidence > 0.5f }

        filteredResults.forEach { result ->
            val title = result.title
            if (!spokenObjects.contains(title)) {
                spokenObjects.add(title)
                
                // Use Vietnamese title if available
                val viTitle = if (result.detectedClass < labelsVi.size) labelsVi[result.detectedClass] else title
                ttsManager.speak(viTitle, isQueued = true, isVietnamese = true)
            }
        }

        onResults(filteredResults)
        image.close()
    }
}
