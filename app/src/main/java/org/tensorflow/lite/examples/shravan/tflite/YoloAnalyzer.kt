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

    private var currentDetector: YoloV5Classifier? = null
    private var currentLabels: List<String>? = null
    private var lastUsedLanguage: Boolean? = null

    private fun getDetector(): YoloV5Classifier {
        val useVi = settingsManager.useVietnamese
        if (currentDetector == null || lastUsedLanguage != useVi) {
            val labelFile = when {
                modelName == "currency.tflite" -> if (useVi) "file:///android_asset/currency_labels_vi.txt" else "file:///android_asset/currency_labels.txt"
                useVi -> "file:///android_asset/coco_vi.txt"
                else -> "file:///android_asset/coco.txt"
            }
            currentDetector?.close()
            currentDetector = DetectorFactory.getDetector(context.assets, modelName, labelFile)
            lastUsedLanguage = useVi
            // Refresh labels as well
            currentLabels = loadLabels()
        }
        return currentDetector!!
    }

    private fun loadLabels(): List<String> {
        val labelsList = mutableListOf<String>()
        val useVi = settingsManager.useVietnamese
        val filename = when {
            modelName == "currency.tflite" -> if (useVi) "currency_labels_vi.txt" else "currency_labels.txt"
            useVi -> "coco_vi.txt"
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

    private val labels: List<String> get() = currentLabels ?: loadLabels().also { currentLabels = it }

    private val lastSeenTime = mutableMapOf<String, Long>()
    private var isClosed = false
    private val lock = Any()
    
    // Persistence state
    private var persistentRecognitions = mutableListOf<Classifier.Recognition>()
    private val frameCountMap = mutableMapOf<String, Int>()
    private val PERSISTENCE_THRESHOLD = 5 // Stay detected for 5 frames if missing
    private var frameCounter = 0

    override fun analyze(image: ImageProxy) {
        synchronized(lock) {
            if (isClosed) {
                image.close()
                return
            }
            frameCounter++
            if (frameCounter % 5 != 0) {
                image.close()
                return
            }
            try {
                val detector = getDetector()
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
                val threshold = if (modelName == "currency.tflite") 0.15f else 0.25f
                val baseFiltered = results.filter { it.confidence > threshold }
                
                // Totally Blind Optimization: dynamic debounce
                val currentDebounce = if (settingsManager.impairmentLevel == org.tensorflow.lite.examples.shravan.utils.ImpairmentLevel.TotallyImpaired) {
                    1000L // Faster announcements for totally blind
                } else {
                    2000L
                }

                val currentLabelsList = labels
                val currentFiltered = if (allowedClasses != null) {
                    baseFiltered.filter { result ->
                        val title = if (result.detectedClass < currentLabelsList.size) currentLabelsList[result.detectedClass] else result.title
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
                    val title = if (detectedClass < currentLabelsList.size) currentLabelsList[detectedClass] else result.title
                    
                    var announcement = title
                    if (modelName == "currency.tflite") {
                        val amount = title.replace(".", "").replace(",", "").toLongOrNull()
                        if (amount != null) {
                            announcement = if (settingsManager.useVietnamese) {
                                when (amount) {
                                    1000L -> "một nghìn đồng"
                                    2000L -> "hai nghìn đồng"
                                    5000L -> "năm nghìn đồng"
                                    10000L -> "mười nghìn đồng"
                                    20000L -> "hai mươi nghìn đồng"
                                    50000L -> "năm mươi nghìn đồng"
                                    100000L -> "một trăm nghìn đồng"
                                    200000L -> "hai trăm nghìn đồng"
                                    500000L -> "năm trăm nghìn đồng"
                                    else -> announcement
                                }
                            } else {
                                when (amount) {
                                    1000L -> "one thousand vietnamese dong"
                                    2000L -> "two thousand vietnamese dong"
                                    5000L -> "five thousand vietnamese dong"
                                    10000L -> "ten thousand vietnamese dong"
                                    20000L -> "twenty thousand vietnamese dong"
                                    50000L -> "fifty thousand vietnamese dong"
                                    100000L -> "one hundred thousand vietnamese dong"
                                    200000L -> "two hundred thousand vietnamese dong"
                                    500000L -> "five hundred thousand vietnamese dong"
                                    else -> announcement
                                }
                            }
                        }
                    }

                    val lastSeen = lastSeenTime[announcement] ?: 0L
                    if (currentTime - lastSeen > currentDebounce) {
                        ttsManager.speak(announcement, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                        historyManager.addHistory(if (modelName == "currency.tflite") "Currency" else "Object", announcement)
                    }
                    lastSeenTime[announcement] = currentTime
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
                currentDetector?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
