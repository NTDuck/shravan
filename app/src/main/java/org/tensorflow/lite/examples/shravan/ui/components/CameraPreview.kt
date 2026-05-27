package org.tensorflow.lite.examples.shravan.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object CameraExecutorManager {
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    zoomRatio: Float = 1.0f,
    imageAnalyzer: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = CameraExecutorManager.executor

    
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to get camera provider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    var analysisUseCase by remember { mutableStateOf<ImageAnalysis?>(null) }

    LaunchedEffect(cameraProvider, imageAnalyzer, zoomRatio, previewView) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val view = previewView ?: return@LaunchedEffect
        try {
            // Unbind previous specific use cases for this instance if any
            previewUseCase?.let { provider.unbind(it) }
            analysisUseCase?.let { provider.unbind(it) }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }
            previewUseCase = preview

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            Log.d("CameraPreview", "Binding use cases to lifecycle")
            val camera = if (imageAnalyzer != null) {
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(cameraExecutor, imageAnalyzer)
                analysisUseCase = analysis
                
                // Unbind all before binding just in case to clear any zombie bindings, 
                // but wait, if another screen bound it, unbindAll will kill theirs.
                // It's safer to just bind ours. However, CameraX allows binding multiple if they fit,
                // or fails. Usually unbindAll() is called before bindToLifecycle in single-screen apps.
                // Let's rely on unbindAll() only if we know we are the active screen, OR just unbind our own.
                // Actually, if we use HorizontalPager, both screens might try to bind to the SAME lifecycleOwner.
                // It's best to unbindAll() just before binding new ones in LaunchedEffect because we are taking over.
                provider.unbindAll() 
                
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )
            } else {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            }
            camera.cameraControl.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
        modifier = modifier
    )

    DisposableEffect(cameraProvider) {
        onDispose {
            try {
                analysisUseCase?.clearAnalyzer()
                previewUseCase?.let { cameraProvider?.unbind(it) }
                analysisUseCase?.let { cameraProvider?.unbind(it) }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error unbinding on dispose", e)
            }
        }
    }
}
