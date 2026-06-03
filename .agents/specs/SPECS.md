# SPECS.md

## 1. Functional Requirements

### Language Support
- Supported languages: English (default) & Vietnamese.
- Vietnamese uses different TTS & speech recognition (vi-VN locale).
- All texts (welcome text, labels, read-out-loud texts, etc.) MUST be configurable via Jetpack Compose resource files (`strings.xml`).
- Configuration values (thresholds, timings, etc.) MUST NOT be hard-coded.
- Texts should be concise and not verbose.

### Core Behaviors & Transitions
- LLMs/AI models MUST NOT require an API key; all must be download/integrate and use locally with no restrictions.
- Font: Inter MUST be used for all text.
- Volume: Use normal media volume instead of call volume for TTS.
- Transitions: Use proper animations for every transition, prioritizing fade in/out where appropriate.
- App Exit: TTS must stop immediately when the app exits or is backgrounded.
- Permissions: Ask for permissions AFTER the setup screen (or tutorial), not during the very first startup moment.

### Startup & Setup
- First startup:
  1. Speak language choice.
  2. Speak welcome text.
  3. Run inference on intent.
  4. Upon clear intent: Trigger haptics, fade screen to dimmed (not receiving input), and speak confirmation of received intent.
  5. Play a short tutorial describing the screens & their functions. Allow skip via screen click.
  6. Ask for necessary permissions (Camera, Record Audio, etc.).
  7. Redirect to Explore Screen.
- Subsequent startups: Open directly on Explore Screen.

### Navigation (Bottom Navbar)
- Persistent bar fixed to the bottom on Explore, Find, OCR, Currency, Settings, History screens.
- 6 Icons for each screen.
- Interaction:
  - Click Icon: Trigger haptics and redirect to screen.
  - Swipe Left/Right: Trigger haptics, redirect to left/right screen using swipe animations (clamped at ends).
  - Voice Control: Listen for user speech in each screen; upon clear intent for screen X, trigger haptics and redirect. Use relaxed matching (contains instead of exact equals) to lower recognition threshold.
  - Quick Status Voice Command: Support global commands for 'Time', 'Battery', or 'Status' to instantly read out current device time and/or battery level.
  - Global Help/Orientation Command: Support global commands for 'Help' or 'Where am I' to instantly read out the current screen/mode the user is in.

### Screen-Specific Requirements

#### Explore Screen
- Camera feed: 0.6x zoom/lens.
- Behavior: Surround detected objects with differently colored bounding boxes and labels (localized).
- TTS: Speak aloud the object's label (localized) once per presence in the camera feed.
- Totally Blind Mode: Enable auto-flash in dark environments. Enable spatial audio or proportional haptics.

#### Find Screen
- Camera feed: 0.6x zoom/lens.
- Initial state: Monotone camera feed, listening for user speech intent.
- Intents/Modes: "find seatings & tables", "find doors & windows", "find person & vehicles".
- Upon intent: Trigger haptics and fade camera feed to normal coloring.
- Behavior: Similar to Explore but only detects objects for the active mode.
- Proximity Feedback: If objects detected, trigger haptics at an interval; interval gets shorter proportionately with bounding box size. Selected button text becomes black.

#### OCR Screen
- Camera feed: 1.0x zoom/lens.
- Behavior: Read aloud scanned text.
- Optimization: Ensure text already read is not re-read.

#### Currency Screen
- Camera feed: 1.0x zoom/lens.
- Behavior: Detect Vietnamese currency/paper money specifically.
- Implementation: Use a technique combining a model with the provided dataset (`currency.zip`/`currency_data`).

#### Settings Screen (Visual Only, No Voice)
- Content Order:
  1. "Language": Dropdown ("English" (default), "Tiếng Việt").
  2. "Haptics": Pill option (defaults to ON; if OFF, all haptics disabled).
  3. "Speech rate": Slider bar.
  4. "Flash": Dropdown ("Auto" (default), "On", "Off").
  5. "Reset factory settings": Wide spanning button. If pressed 7 times, trigger haptics, reset all settings (including impairment level), and exit the app. Next launch behaves as first startup.

#### History Screen
- Content: List of scanned objects/texts with time (hh:mm:ss) and localized content.
- Language: Immediately update rendered names when language changes.
- Interaction:
  - Voice Control: Listen for intent to delete history; trigger haptics and clear all on success.
  - Click Item: Read aloud content (localized). Interruptible if exiting screen or clicking another item.

## 2. Technical Standards
- Adhere to `android-compose-best-practices.mdc`.
- Adhere to `ai-rules.mdc` (no API keys, local inference).
- Adhere to `feedback-standards.mdc` (speaker volume, haptics).
- Adhere to `resource-conventions.mdc` (localization, Inter font, animations).
- App Icon: Correct placement and conventional naming (`ic_launcher`).
- All state management must follow standard Android practices (MVVM/MVI).
