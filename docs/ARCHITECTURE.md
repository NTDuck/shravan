# System Architecture

## Design Pattern
Shravan follows a modern **MVVM/MVI** hybrid pattern. State is managed centrally via `SettingsManager` and `HistoryManager`, while Jetpack Compose handles the unidirectional data flow to the UI.

## Component Breakdown

### 1. `MainActivity` (The Orchestrator)
The entry point of the application responsible for:
- **Locale Management**: Dynamically switching the app's internal locale between English and Vietnamese.
- **Permission Handling**: Managing the critical CAMERA and RECORD_AUDIO permissions required for operation.
- **Global Lifecycle**: Initializing all singleton Managers and passing them down the Compose tree.

### 2. `MainScreen` (The Navigation Hub)
The `MainScreen` is the architectural heart of Shravan.
- **Shared Camera Preview**: It hosts the persistent `CameraPreview` component, preventing expensive camera restarts during navigation.
- **Global Voice Monitoring**: It registers a global listener with `VoiceCommandManager` to respond to navigation intents (e.g., "Settings", "Quay lại") regardless of the active sub-screen.
- **State Synchronization**: Orchestrates the active `ImageAnalysis.Analyzer` based on the current page index.

### 3. `YoloAnalyzer` (The Processing Pipeline)
A thread-safe, synchronized analyzer that pipes raw image buffers into the TFLite inference engine.
- **Lock Management**: Uses a synchronized monitor (`synchronized(lock)`) to ensure that frames are dropped gracefully if the AI engine is still processing the previous frame, preventing memory backpressure.
- **Result Piping**: Pushes detection results to a callback (`onResults`) which the UI screens observe to update bounding boxes or trigger haptics.

### 4. `TTSManager` (The Voice Engine)
A specialized wrapper around the Android TextToSpeech API.
- **Local Voice Selection**: Automatically chooses high-quality localized voices based on the user's language setting.
- **Interference Prevention**: Implements a **500ms post-speech buffer**. The `isSpeaking()` method returns `true` for half a second after the actual speech ends to prevent the `VoiceCommandManager` from triggering on the app's own voice.

### 5. `VoiceCommandManager` (The Listening Ear)
Optimized speech recognition for high-stress scenarios.
- **Aggressive Polling**: In "Totally Blind" mode, the manager uses shorter silence thresholds to restart recognition faster, ensuring the app is always "listening."
- **Intent Dispatcher**: Uses a priority-based intent system where global navigation commands are checked before screen-specific context commands.

## State Management
- **`SettingsManager`**: Uses `mutableStateOf` to provide reactive access to user preferences (Theme, Flash, Language).
- **`HistoryManager`**: Records detection events into a persistent JSON-backed store, allowing users to review what the app "saw" during their session.
