package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.examples.shravan.utils.HistoryManager
import org.tensorflow.lite.examples.shravan.utils.ImageUtils
import org.tensorflow.lite.examples.shravan.utils.SettingsManager
import org.tensorflow.lite.examples.shravan.utils.TTSManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class YoloAnalyzer(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val settingsManager: SettingsManager,
    private val historyManager: HistoryManager,
    private val modelName: String = "yolov5s-fp16.tflite",
    private val onResults: (List<Classifier.Recognition>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: YoloV5Classifier by lazy {
        DetectorFactory.getDetector(context.assets, modelName)
    }

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val lastAreas = mutableMapOf<Int, Float>()
    private val lastAlertTime = mutableMapOf<Int, Long>()
    private val ALERT_COOLDOWN = 2000L
    private val GROWTH_THRESHOLD = 0.15f
    private val SIZE_THRESHOLD = 0.4f

    private val labels: List<String> by lazy {
        val labelsList = mutableListOf<String>()
        val filename = if (modelName == "currency.tflite") "currency_labels.txt" else "coco_vi.txt"
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))
            var line: String? = reader.readLine()
            while (line != null) {
                labelsList.add(line)
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        labelsList
    }

    private val lastSeenTime = mutableMapOf<String, Long>()
    private val DEBOUNCE_TIME = 2000L

    override fun analyze(image: ImageProxy) {
        try {
            val bitmap = ImageUtils.toBitmap(image)
            if (bitmap == null) {
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
            val filteredResults = results.filter { it.confidence > 0.25f }

            val currentTime = System.currentTimeMillis()
            val currentTitles = mutableSetOf<String>()

            filteredResults.forEach { result ->
                val detectedClass = result.detectedClass
                val title = if (detectedClass < labels.size) labels[detectedClass] else result.title
                currentTitles.add(title)
                
                // Proximity Detection logic
                val location = result.location
                val area = location.width() * location.height()
                val normalizedArea = area / (detector.inputSize * detector.inputSize)
                
                val prevArea = lastAreas[detectedClass] ?: 0f
                lastAreas[detectedClass] = normalizedArea
                
                val lastAlert = lastAlertTime[detectedClass] ?: 0L
                
                if (normalizedArea > SIZE_THRESHOLD && (normalizedArea - prevArea) > GROWTH_THRESHOLD) {
                    if (currentTime - lastAlert > ALERT_COOLDOWN) {
                        lastAlertTime[detectedClass] = currentTime
                        val alertTitle = if (settingsManager.useVietnamese) "$title quá gần!" else "$title too close!"
                        ttsManager.speak(alertTitle, isQueued = false, isVietnamese = settingsManager.useVietnamese)
                        
                        if (settingsManager.hapticsEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(500)
                            }
                        }
                    }
                }

                val lastSeen = lastSeenTime[title] ?: 0L
                if (currentTime - lastSeen > DEBOUNCE_TIME) {
                    val announcement = title
                    ttsManager.speak(announcement, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                    historyManager.addHistory("Object", announcement)
                }
                lastSeenTime[title] = currentTime
            }

            // Clean up old entries to prevent memory leak
            lastSeenTime.entries.removeIf { currentTime - it.value > 5000L }

            onResults(filteredResults)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}
