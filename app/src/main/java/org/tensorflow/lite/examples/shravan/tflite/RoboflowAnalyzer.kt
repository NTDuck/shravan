package org.tensorflow.lite.examples.shravan.tflite

import android.content.Context
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

    private val client = OkHttpClient()
    private val apiKey = "cCunQhOIWzChQHaRKPia"
    private val modelId = "vietnamese-currency-lgi9i"
    private val version = "5"
    private val apiUrl = "https://serverless.roboflow.com/$modelId/$version?api_key=$apiKey"

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
        Log.d("RoboflowAnalyzer", "Starting analysis...")
        val jpegBytes = ImageUtils.toJpegBytes(image)
        image.close()

        val base64String = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        
        // v2 Serverless API expects JSON body
        val jsonBody = JsonObject().apply {
            addProperty("image", base64String)
        }
        
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isProcessing = false
                Log.e("RoboflowAnalyzer", "API call failed: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                isProcessing = false
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    Log.d("RoboflowAnalyzer", "API Success: $body")
                    try {
                        val roboflowResponse = Gson().fromJson(body, RoboflowResponse::class.java)
                        if (roboflowResponse?.predictions != null) {
                            val recognitions = roboflowResponse.predictions.map { pred ->
                                Classifier.Recognition(
                                    pred.classId.toString(),
                                    pred.className,
                                    pred.confidence,
                                    null,
                                    pred.classId
                                )
                            }

                            val topResult = recognitions.firstOrNull()
                            if (topResult != null && topResult.confidence > 0.4f) {
                                val currentTime = System.currentTimeMillis()
                                val lastSeen = lastSeenTime[topResult.title] ?: 0L
                                if (currentTime - lastSeen > DEBOUNCE_TIME) {
                                    Log.d("RoboflowAnalyzer", "Speaking currency: ${topResult.title}")
                                    ttsManager.speak(topResult.title, isQueued = true, isVietnamese = settingsManager.useVietnamese)
                                    historyManager.addHistory("Currency", topResult.title)
                                    lastSeenTime[topResult.title] = currentTime
                                }
                            }
                            onResults?.invoke(recognitions)
                        } else {
                            Log.w("RoboflowAnalyzer", "No predictions in response")
                            onResults?.invoke(emptyList())
                        }
                    } catch (e: Exception) {
                        Log.e("RoboflowAnalyzer", "Error parsing response", e)
                    }
                } else {
                    Log.e("RoboflowAnalyzer", "API Error: ${response.code} - $body")
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
