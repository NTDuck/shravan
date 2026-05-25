# Technical Specifications

## Data Models

### ImpairmentLevel (Enum)
```kotlin
enum class ImpairmentLevel {
    PartiallyImpaired,
    TotallyImpaired
}
```

### Theme (Data Class)
```kotlin
data class AppTheme(
    val name: String,
    val colors: List<Color> // 5 hex colors
)
```

## Local Storage
*   **Storage Type**: `SharedPreferences` (managed via `SettingsManager`).
*   **Keys**:
    *   `impairment_level`: String (serialized enum or null).
    *   `vibration_enabled`: Boolean (Default: true).
    *   `language`: String ("vi" or "en").
    *   `speech_rate`: Float.
    *   `active_theme_index`: Int (Default: 3).

## Audio Management
*   **Resource**: `https://youtu.be/HGguPt27Pzg`.
*   **Implementation**: The audio must be downloaded and placed in `assets/` or `res/raw/`.
*   **Playback**: Handled via `MediaPlayer` or `ExoPlayer` in `SettingsScreen`. Playback stops on `onDispose` or navigating away.

## Haptic Feedback
*   **Implementation**: Use `Vibrator` service.
*   **Constraint**: Check `SettingsManager.vibrationEnabled` before triggering.

## Voice Recognition
*   **Implementation**: Use `SpeechRecognizer` with `RecognitionListener`.
*   **Flow**:
    1.  Screen opens.
    2.  TTS speaks greeting/label.
    3.  Wait for TTS to finish + 1s.
    4.  Start `SpeechRecognizer`.
    5.  Handle results and map to navigation/actions.
    6.  Restart listening if no match or error occurs, unless navigating away.
