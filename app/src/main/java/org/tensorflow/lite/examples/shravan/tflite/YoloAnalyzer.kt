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
    var onResults: ((List<Classifier.Recognition>) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    var allowedClasses: List<String>? = null

    private val detector: YoloV5Classifier by lazy {
        val labelFile = when {
            modelName == "currency.tflite" -> if (settingsManager.useVietnamese) "file:///android_asset/currency_labels_vi.txt" else "file:///android_asset/currency_labels.txt"
            settingsManager.useVietnamese -> "file:///android_asset/coco_vi.txt"
            else -> "file:///android_asset/coco.txt"
        }
        DetectorFactory.getDetector(context.assets, modelName, labelFile)
    }

    private val labels: List<String> get() {
        val labelsList = mutableListOf<String>()
        val filename = when {
            modelName == "currency.tflite" -> if (settingsManager.useVietnamese) "currency_labels_vi.txt" else "currency_labels.txt"
            settingsManager.useVietnamese -> "coco_vi.txt"
            else -> "coco.txt"
        }
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
        return labelsList
    }

    private val lastSeenTime = mutableMapOf<String, Long>()
    private var isClosed = false
    private val lock = Any()
    
    // Persistence state
    private var persistentRecognitions = mutableListOf<Classifier.Recognition>()
    private val frameCountMap = mutableMapOf<String, Int>()
    private val PERSISTENCE_THRESHOLD = 5 // Stay detected for 5 frames if missing

    override fun analyze(image: ImageProxy) {
        synchronized(lock) {
            if (isClosed) {
                image.close()
                return
            }
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
                val baseFiltered = results.filter { it.confidence > 0.25f }
                
                // Totally Blind Optimization: dynamic debounce
                val currentDebounce = if (settingsManager.impairmentLevel == org.tensorflow.lite.examples.shravan.utils.ImpairmentLevel.TotallyImpaired) {
                    1000L // Faster announcements for totally blind
                } else {
                    2000L
                }

                val currentFiltered = if (allowedClasses != null) {
                    baseFiltered.filter { result ->
                        val title = if (result.detectedClass < labels.size) labels[result.detectedClass] else result.title
                        allowedClasses!!.contains(title.lowercase())
                    }
                } else {
                    baseFiltered
                }

                // Apply Persistence Logic
                val newPersistentList = mutableListOf<Classifier.Recognition>()
                val activeTitles = currentFiltered.map { it.title }.toSet()
                
                // 1. Add current frame results
                currentFiltered.forEach { recognition ->
                    newPersistentList.add(recognition)
                    frameCountMap[recognition.title] = PERSISTENCE_THRESHOLD
                }
                
                // 2. Carry over missing objects that are still within the threshold
                persistentRecognitions.forEach { old ->
                    if (!activeTitles.contains(old.title)) {
                        val count = frameCountMap[old.title] ?: 0
                        if (count > 0) {
                            newPersistentList.add(old)
                            frameCountMap[old.title] = count - 1
                        }
                    }
                }
                
                persistentRecognitions = newPersistentList

                val currentTime = System.currentTimeMillis()
                persistentRecognitions.forEach { result ->
                    val detectedClass = result.detectedClass
                    val title = if (detectedClass < labels.size) labels[detectedClass] else result.title
                    
                    val lastSeen = lastSeenTime[title] ?: 0L
                    if (currentTime - lastSeen > currentDebounce) {
                        val announcement = title
                        ttsManager.speak(announcement, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                        historyManager.addHistory("Object", announcement)
                    }
                    lastSeenTime[title] = currentTime
                }

                // Clean up old entries to prevent memory leak
                lastSeenTime.entries.removeIf { currentTime - it.value > 5000L }

                onResults?.invoke(persistentRecognitions)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    fun close() {
        synchronized(lock) {
            isClosed = true
            try {
                detector.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
