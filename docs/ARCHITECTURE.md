# Shravan Architecture

This document outlines the technical architecture and tech stack of the Shravan application.

## Tech Stack
*   **Language**: Kotlin (1.8+)
*   **UI Framework**: Jetpack Compose (Modern declarative UI)
*   **Asynchronous Programming**: Kotlin Coroutines
*   **Navigation**: Jetpack Compose Navigation
*   **Machine Learning**: 
    *   TensorFlow Lite (Object Detection)
    *   Google ML Kit (Text Recognition)
*   **Camera API**: CameraX (Analysis and Preview)
*   **Build System**: Gradle (Kotlin DSL)

## Core Components

### 1. UI Layer (Jetpack Compose)
The UI is built using a single-activity architecture with `MainActivity` serving as the entry point.
*   **Screens**: Modular Composable functions located in `ui/screens/` (Home, Camera, OCR, Settings, History, Splash).
*   **Theme**: `ShravanTheme` provides dynamic color schemes, supporting both standard Material 3 and a specialized High Contrast mode.

### 2. Machine Learning Engines
*   **YoloAnalyzer**: Implements CameraX's `ImageAnalysis.Analyzer`. It handles image preprocessing (rotation, scaling), executes the TFLite model, and processes results for proximity alerts and guidance.
*   **YoloV5Classifier**: A Java-based wrapper for the TFLite Interpreter, handling tensor mapping, Non-Maximum Suppression (NMS), and box de-normalization.
*   **OCR Engine**: Utilizes Google ML Kit's `TextRecognition` client within the `OCRScreen` to process camera frames.

### 3. Utility Managers
Managers are provided via `remember` blocks in `MainActivity` and passed down to screens:
*   **TTSManager**: Centralized wrapper for Android's `TextToSpeech` API. Handles queuing, language switching, and speech rate control.
*   **SettingsManager**: Manages user preferences using `SharedPreferences`.
*   **HistoryManager**: Handles local persistence of detection events.

## Data Flow
1.  **CameraX** captures frames and passes them to the `ImageAnalysis` use case.
2.  **Analyzers** (`YoloAnalyzer` or ML Kit) process the frame buffer.
3.  **Inference results** are filtered by confidence and passed to the UI for drawing overlays.
4.  **Logical triggers** (like proximity or new text) invoke the `TTSManager` for audio feedback.
5.  **Events** are logged via `HistoryManager` if applicable.

## Project Structure
```text
org.tensorflow.lite.examples.shravan
├── MainActivity.kt          # Entry point and NavHost
├── tflite/                  # ML inference logic and YOLO wrappers
├── ui/
│   ├── components/          # Reusable UI elements (CameraPreview)
│   ├── screens/             # Feature-specific screens
│   └── theme/               # Styling and Color definitions
└── utils/                   # Business logic and managers (TTS, History, Settings)
```
