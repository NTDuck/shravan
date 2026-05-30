package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.examples.shravan.utils.*
import java.io.IOException

class RoboflowAnalyzer(
    private val context: Context,
    private val ttsManager: TTSManager,
    private val settingsManager: SettingsManager,
    private val historyManager: HistoryManager,
    var onResults: ((List<Classifier.Recognition>) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
        
    private val apiKey = "cCunQhOIWzChQHaRKPia"
    private val modelId = "vietnamese-currency-lgi9i"
    private val version = "5"
    private val apiUrl = "https://serverless.roboflow.com/$modelId/$version?api_key=$apiKey"
    private val mainHandler = Handler(Looper.getMainLooper())

    private val lastSeenTime = mutableMapOf<String, Long>()
    private val DEBOUNCE_TIME = 3000L
    private var isClosed = false
    private var isProcessing = false

    override fun analyze(image: ImageProxy) {
        if (isClosed || isProcessing) {
            image.close()
            return
        }

        isProcessing = true
        Log.d("RoboflowAnalyzer", "Analyzing frame...")
        
        // 1. Convert to rotated bitmap (Portrait)
        val rotatedBitmap = ImageUtils.toRotatedBitmap(image)
        image.close()
        
        if (rotatedBitmap == null) {
            isProcessing = false
            return
        }

        // 2. Downscale for API performance
        val maxDim = 640
        val scale = maxDim.toFloat() / Math.max(rotatedBitmap.width, rotatedBitmap.height)
        val scaledBitmap = if (scale < 1f) {
            android.graphics.Bitmap.createScaledBitmap(
                rotatedBitmap,
                (rotatedBitmap.width * scale).toInt(),
                (rotatedBitmap.height * scale).toInt(),
                true
            )
        } else {
            rotatedBitmap
        }

        val out = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
        val jpegBytes = out.toByteArray()
        val imageWidth = rotatedBitmap.width.toFloat()
        val imageHeight = rotatedBitmap.height.toFloat()
        
        val base64String = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        
        // Roboflow Serverless v2 payload
        val jsonBody = JsonObject().apply {
            val imageObj = JsonObject().apply {
                addProperty("type", "base64")
                addProperty("value", base64String)
            }
            add("image", imageObj)
        }
        
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(apiUrl).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isProcessing = false
                Log.e("RoboflowAnalyzer", "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                isProcessing = false
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val roboflowResponse = Gson().fromJson(body, RoboflowResponse::class.java)
                        if (roboflowResponse?.predictions != null) {
                            val recognitions = roboflowResponse.predictions.map { pred ->
                                // Detect if it's a valid detection or just classification
                                val hasBox = pred.width > 0 && pred.height > 0
                                
                                val location = if (hasBox) {
                                    val scaleW = imageWidth / scaledBitmap.width
                                    val scaleH = imageHeight / scaledBitmap.height
                                    val left = (pred.x - pred.width / 2f) * scaleW / imageWidth
                                    val top = (pred.y - pred.height / 2f) * scaleH / imageHeight
                                    val right = (pred.x + pred.width / 2f) * scaleW / imageWidth
                                    val bottom = (pred.y + pred.height / 2f) * scaleH / imageHeight
                                    RectF(left, top, right, bottom)
                                } else {
                                    null
                                }
                                
                                Classifier.Recognition(
                                    pred.classId.toString(),
                                    pred.className,
                                    pred.confidence,
                                    location,
                                    pred.classId
                                )
                            }

                            mainHandler.post {
                                val topResult = recognitions.firstOrNull { it.confidence > 0.4f }
                                if (topResult != null) {
                                    val currentTime = System.currentTimeMillis()
                                    val lastSeen = lastSeenTime[topResult.title] ?: 0L
                                    if (currentTime - lastSeen > DEBOUNCE_TIME) {
                                        Log.d("RoboflowAnalyzer", "Detected: ${topResult.title} (${topResult.confidence})")
                                        ttsManager.speak(topResult.title, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                                        historyManager.addHistory("Currency", topResult.title)
                                        lastSeenTime[topResult.title] = currentTime
                                    }
                                }
                                onResults?.invoke(recognitions)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RoboflowAnalyzer", "JSON parse error: ${e.message}\nBody: $body")
                    }
                } else {
                    Log.e("RoboflowAnalyzer", "API Error ${response.code}: $body")
                    // If nested JSON failed, log it clearly so we can fix in next turn
                }
            }
        })
    }

    fun close() {
        isClosed = true
        client.dispatcher.executorService.shutdown()
    }

    data class RoboflowResponse(val predictions: List<RoboflowPrediction>)
    data class RoboflowPrediction(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val confidence: Float,
        @SerializedName("class") val className: String,
        @SerializedName("class_id") val classId: Int
    )
}
