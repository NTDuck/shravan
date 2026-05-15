# Navigation Specifications

## App Entry Flow
The application follows a conditional routing logic upon launch. The `SplashScreen` is removed.

1.  **Impairment Level Check**:
    *   On launch, the app retrieves the `ImpairmentLevel` enum from local storage.
2.  **Redirection Logic**:
    *   `null` / Not Set: Redirect to `SetupHomeScreen`.
    *   `PartiallyImpaired`: Redirect to `PartiallyImpairedHomeScreen`.
    *   `TotallyImpaired`: Redirect to `TotallyImpairedHomeScreen`.

## Screen Transitions
*   All screen transitions should be smooth.
*   Upon opening any screen, a TTS announcement is made (refer to [SCREENS.md](./SCREENS.md) for specific strings).
*   During TTS announcements and for 1 second afterwards, all interactive behaviors (onClick, etc.) are disabled.

## Back Navigation
*   `CameraScreen`, `OCRScreen`, `SettingsScreen`, and `ThemesScreen` support voice-activated "Back" commands.
*   Voice command "Quay lại" (Vietnamese) or "Back" (English) triggers:
    *   TTS confirmation ("Quay lại" / "Back").
    *   Haptic feedback.
    *   Navigation back to the respective previous screen.
