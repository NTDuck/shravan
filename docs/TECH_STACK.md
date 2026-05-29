# Tech Stack: Architecture & Infrastructure

## Platform & Compatibility
- **OS**: Android
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.0.2 with Kotlin DSL

## Language
- **Kotlin**: 100% Kotlin codebase, utilizing modern features like Coroutines, Flows, and Sealed Classes for robust, expressive logic.

## UI Framework
- **Jetpack Compose**: Shravan uses a modern declarative UI approach. This choice allows for:
    - **Dynamic Accessibility**: UI elements that react instantly to state changes (e.g., theme switching or language updates).
    - **Semantics**: Integrated accessibility semantics that provide clear hints to system tools like TalkBack.
    - **Theme System**: A custom high-contrast theme engine (accessible via `ShravanTheme`) supporting multiple visual profiles for partially impaired users.

## Camera & Processing
- **CameraX**: Shravan utilizes Google's CameraX library for its camera implementation.
    - **Shared Preview**: To eliminate the lag associated with restarting the camera when switching screens, Shravan implements a "Shared Camera Layer" hosted in the `MainScreen`.
    - **Centralized Analyzer**: The `YoloAnalyzer` is attached once to the `ImageAnalysis` use case, piping results to whichever screen is currently active.
    - **Zoom Controls**: Fixed zoom ratios (0.6x for wide exploration, 1.0x for reading/currency) optimized for sensor clarity.

## Navigation
- **Compose Navigation**: Centralized routing in `MainActivity`.
- **AnimatedContent**: Custom transitions between screens. Shravan uses horizontal slide animations for most navigation to give users a sense of spatial continuity, while using crossfades for the high-priority "Find" mode to minimize visual distraction.

## Core Libraries & Dependencies
- **TensorFlow Lite (TFLite)**: The engine behind on-device AI.
- **ML Kit**: Bundled Google ML Kit for offline Text Recognition (OCR).
- **Compose BOM**: Ensures version compatibility across all Jetpack Compose libraries.
- **MultiDex**: Enabled to support the large method counts required by AI and Camera libraries.
- **DataStore/SharedPreferences**: Used via `SettingsManager` for lightweight, reactive persistence of user preferences.
