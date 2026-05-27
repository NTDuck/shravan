# Implementation Plan for Shravan App

## Task 1: UI Foundation, Resources, and Audio Configuration
- **Description**: 
  - Move `@illuminati.png` to `app/src/main/res/mipmap-xxxhdpi/` and configure it as the app icon.
  - Download the "Inter" font and configure it as the default font in `Theme.kt`.
  - Externalize all hardcoded strings to `strings.xml` and `strings.xml (vi)` for English and Vietnamese support.
  - Modify TTS/Audio configuration to use speaker volume (like in phone calls) instead of standard media volume.
  - Add smooth fade-in/fade-out transitions for all screen navigations.

## Task 2: Core Navigation and First Run Setup
- **Description**: 
  - Implement a persistent bottom navigation bar across all main screens (Explore, Find, OCR, Currency, Settings, History) with 6 respective icons.
  - Implement horizontal swipe gestures on screens to transition left/right with haptic feedback.
  - Build the first startup setup flow: ask for permissions, dim screen, read welcome text, and listen for speech intent (partially blind vs totally blind). Use a local lightweight on-device approach for intent recognition (e.g. keywords or local small model). Persist state so it redirects to Explore next time.
  - Implement global voice-based navigation (listen for intent to redirect to a specific screen).

## Task 3: Explore and Find Screens
- **Description**: 
  - **Explore**: Configure camera feed length to 0.6x. Draw differently colored bounding boxes with labels for detected objects. Speak aloud label once per object visibility session (translate label to current language).
  - **Find**: Configure camera to 0.6x. Start in monotone color, listen for voice command for specific mode ("seatings & tables", "doors & windows", "person & vehicles"), then fade to normal color. Only detect objects of that mode. Trigger haptics based on proximity to the camera (closer = faster).

## Task 4: OCR, Currency, Settings, and History Screens
- **Description**: 
  - **OCR**: Camera feed 1.0x. Read scanned text aloud using ML Kit, prevent re-reading previously read text blocks.
  - **Currency**: Camera feed 1.0x. Use YOLO/TFLite model dedicated to Vietnamese currency (integrate provided `currency.zip` dataset/model).
  - **Settings**: Language dropdown (English/Tiếng Việt), Haptics toggle, Speech rate slider, Flash dropdown (Auto/On/Off), and Reset Factory Settings (press 7 times to reset, trigger haptic, clear state, exit app).
  - **History**: Display scanned objects/texts with time (hh:mm:ss). Ensure labels change dynamically with language change. Clear history via voice/action. Click to read aloud (interrupting ongoing speech).
