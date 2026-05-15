# Shravan App Features

Shravan is an accessibility-focused Android application designed to assist visually impaired individuals by providing real-time environmental awareness through object detection and text recognition.

## 1. Real-time Object Detection
*   **YOLOv5 Integration**: Powered by a TensorFlow Lite YOLOv5s model for fast and accurate object detection.
*   **Audio Guidance**: Automatically announces detected objects and their relative positions (e.g., "Chair on the left", "Person in the center").
*   **Smart Filtering**: Only announces new objects or significant position changes to avoid audio clutter.
*   **Proximity Alerts**: Detects when an object is dangerously close and moving towards the user, triggering high-priority audio warnings and haptic (vibration) feedback.

## 2. Text Recognition (OCR)
*   **Live Text Reading**: Uses Google ML Kit to recognize and read text from the camera stream in real-time.
*   **Vietnamese Support**: Full support for Vietnamese characters and tones, with automatic language detection.
*   **Positional Context**: Informs the user where the text is located (left, right, or center).
*   **Similarity Filtering**: Employs Levenshtein distance algorithms to avoid repeating the same text block multiple times.

## 3. Accessibility-First Design
*   **Large-Scale UI**: High-touch targets and oversized buttons optimized for users with low vision.
*   **High Contrast Mode**: Toggleable high-contrast color scheme (Yellow/Cyan on Black) for better visibility.
*   **Haptic Feedback**: Distinct vibration patterns for button interactions and critical alerts.
*   **Comprehensive TTS**: Every UI element and action is backed by Text-to-Speech feedback.
*   **Speech Rate Control**: Users can adjust the speed of the audio guidance to their preference.

## 4. History and Persistence
*   **Detection Log**: Maintains a chronological history of detected objects and read text.
*   **Persistent Settings**: Saves user preferences for speech rate, confidence thresholds, and accessibility modes.

## 5. Multilingual Support
*   **Bilingual Interface**: Seamless switching between English and Vietnamese for both the UI and the machine learning inference labels.
