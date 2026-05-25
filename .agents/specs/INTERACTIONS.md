# Interaction Specifications

## Text-to-Speech (TTS)
*   **Engine**: Integrated Android TTS.
*   **Languages**: Vietnamese (VI) and English (EN).
*   **Behavior**:
    *   TTS must stop when navigating back from `CameraScreen` or `OCRScreen`.
    *   All interactions are disabled during TTS announcements and for 1 second afterwards.
    *   Labels are spoken using the language defined in settings.

## Haptic Feedback
*   **Condition**: Enabled only if "Cho phép rung" (Enable vibration) is ON in settings.
*   **Trigger**: All button clicks and specific voice commands.

## Voice Command System (Voice/Mic)
*   **Engine**: Speech-to-Text (STT) listener.
*   **Activation**: Screens automatically start listening after initial TTS announcement finishes.
*   **Commands**:
    *   **Global**: "Quay lại" (VI) / "Back" (EN).
    *   **Setup**: "Khiếm thị một phần", "Khiếm thị hoàn toàn".
    *   **Home**: "Cài đặt", "Lịch sử".
*   **Feedback**: Recognized commands trigger TTS confirmation and haptics before execution.
