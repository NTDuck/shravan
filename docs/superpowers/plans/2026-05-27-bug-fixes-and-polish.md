# Critical Bug Fixes and Final UI Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix crashes in Voice/Camera, cleanup debug icons, and polish the bottom navigation UI.

**Architecture:** Surgical refactoring of `VoiceCommandManager` and `CameraPreview` to handle concurrency and lifecycle safely. UI updates in `MainScreen` and `strings.xml`.

**Tech Stack:** Kotlin, Jetpack Compose, CameraX, Android Speech API.

---

### Task 1: App Icon (Debug Cleanup)

**Files:**
- Modify: `app/src/debug/res/mipmap*` (Delete)
- Verify: `app/src/main/res/mipmap*` (Confirm PNGs)

- [ ] **Step 1: Delete all ic_launcher files in debug res**

Run: `gci app/src/debug/res/mipmap* -filter "ic_launcher*" | remove-item -force`

- [ ] **Step 2: Confirm main icons exist**

Run: `ls app/src/main/res/mipmap*/ic_launcher.png` and `ls app/src/main/res/mipmap*/ic_launcher_round.png`

---

### Task 2: Crash Fix (VoiceCommandManager)

**Files:**
- Modify: `app/src/main/java/org/tensorflow/lite/examples/shravan/utils/VoiceCommandManager.kt`

- [ ] **Step 1: Refactor to create recognizer once and handle sessions safely**

```kotlin
class VoiceCommandManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var onResult: ((String) -> Unit)? = null
    private var lastIsVietnamese = true
    private var shouldRetry = true
    private val handler = Handler(Looper.getMainLooper())
    private var currentSessionId = 0

    init {
        handler.post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupListener()
            }
        }
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceCommandManager", "Ready for speech (Session: $currentSessionId)")
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                Log.e("VoiceCommandManager", "Error: $error (Session: $currentSessionId)")
                isListening = false
                if (shouldRetry && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    handler.post { startListeningInternal() }
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult?.invoke(matches[0])
                }
                isListening = false
                if (shouldRetry) {
                    handler.post { startListeningInternal() }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(isVietnamese: Boolean = true, retry: Boolean = true, callback: (String) -> Unit): Int {
        currentSessionId++
        handler.post {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return@post
            onResult = callback
            lastIsVietnamese = isVietnamese
            shouldRetry = retry
            
            speechRecognizer?.cancel() // Stop any ongoing session
            startListeningInternal()
        }
        return currentSessionId
    }

    private fun startListeningInternal() {
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lastIsVietnamese) "vi-VN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopCurrentSession() {
        handler.post {
            shouldRetry = false
            speechRecognizer?.cancel()
            isListening = false
        }
    }

    fun destroy() {
        handler.post {
            shouldRetry = false
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        }
    }
}
```

---

### Task 3: Crash Fix (CameraPreview)

**Files:**
- Modify: `app/src/main/java/org/tensorflow/lite/examples/shravan/ui/components/CameraPreview.kt`

- [ ] **Step 1: Refactor CameraPreview to avoid blocking UI thread**

```kotlin
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    zoomRatio: Float = 1.0f,
    imageAnalyzer: ImageAnalysis.Analyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            // Bind logic moved here or triggered by state
        }, ContextCompat.getMainExecutor(context))
    }
    
    // Better implementation using local state for cameraProvider
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    LaunchedEffect(context) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(cameraProvider, imageAnalyzer, zoomRatio, previewView) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val view = previewView ?: return@LaunchedEffect
        try {
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val camera = if (imageAnalyzer != null) {
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(cameraExecutor, imageAnalyzer)
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
            } else {
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
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

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        }
    }
}
```

---

### Task 4: Navbar Polish (MainScreen.kt)

**Files:**
- Modify: `app/src/main/java/org/tensorflow/lite/examples/shravan/ui/screens/MainScreen.kt`

- [ ] **Step 1: Set indicatorColor to Transparent and set selected colors**

```kotlin
NavigationBarItem(
    icon = { Icon(screen.second, contentDescription = stringResource(screen.first)) },
    label = { Text(stringResource(screen.first)) },
    selected = pagerState.currentPage == index,
    colors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = Color.White,
        selectedTextColor = Color.White,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
    ),
    onClick = { ... }
)
```

---

### Task 5: Rename Explore (strings.xml)

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Update nav_explore string**

```xml
<string name="nav_explore">Explore Screen</string>
```
