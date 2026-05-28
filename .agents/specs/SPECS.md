# SPECS.md

## 1. Functional Requirements

### Language Support
- Supported languages: English (default) & Vietnamese.
- Uses different TTS & speech recognition for each language.
- All texts (welcome text, labels, read-out-loud texts) MUST be configurable via Jetpack Compose resource files (`strings.xml`).
- Configuration values MUST NOT be hard-coded.
- Texts should be concise, not verbose.

### Core Behaviors & Transitions
- LLMs MUST NOT require an API key (on-device or local integration).
- Font: Inter.
- Volume: Use speaker volume (like in calls) instead of normal audio volume.
- Transitions: Use proper animations (fade in/out prioritized).

### Startup & Setup
- First startup:
  1. Ask for all necessary permissions.
  2. Speak welcome text (screen is dimmed).
  3. Fade screen to normal and listen for user speech.
  4. Run LLM inference on speech stream to clarify intent (threshold-based).
  5. Upon clear intent: trigger haptics, fade screen to dimmed (stop listening), and speak confirmation.
  6. Intent defines impairment level: Partially Blind or Totally Blind.
  7. Redirect to Explore Screen.
- Subsequent startups: Open directly on Explore Screen.

### Navigation (Bottom Navbar)
- Persistent bar on Explore, Find, OCR, Currency, Settings, History screens.
- 6 Icons corresponding to these screens.
- Click: Trigger haptics and redirect.
- Swipe Left/Right: Trigger haptics and redirect with swipe animation (clamped at ends).
- Voice Control: Listen for speech; upon clear intent (target screen), trigger haptics and redirect.

### Screen-Specific Requirements

#### Explore Screen
- Camera feed (0.6x zoom/scale).
- Object detection with colored bounding boxes and labels (localized).
- Speak aloud detected object label (once per presence in feed).

#### Find Screen
- Camera feed (0.6x zoom/scale).
- Initial state: Monotone feed, listening for speech intent.
- Intents/Modes: "find seatings & tables", "find doors & windows", "find person & vehicles".
- Upon intent: Trigger haptics, fade feed to normal colors.
- Behavior: Similar to Explore but filtered by mode.
- Interval Haptics: Trigger every 1s (default) if objects detected, interval scales with distance to nearest object (nearer = faster).

#### OCR Screen
- Camera feed (1.0x zoom/scale).
- Read aloud scanned text.
- Prevent re-reading of already read text.

#### Currency Screen
- Camera feed (1.0x zoom/scale).
- Detect Vietnamese currency using custom model/dataset (`currency.zip`).
- Behavior similar to Explore.

#### Settings Screen (Visual Only, No Voice)
- Order:
  1. Language: Dropdown ("English", "Tiếng Việt").
  2. Haptics: Pill option (On/Off).
  3. Speech Rate: Slider.
  4. Flash: Dropdown ("Auto", "On", "Off").
  5. Reset factory settings: Wide spanning button. Press 7 times to trigger haptics, reset all settings, and exit app.
- Reset leads to setup menu on next launch.

#### History Screen
- List of scanned objects/texts with time (hh:mm:ss) and localized content.
- Update rendering immediately on language change.
- Voice Control: listening for clear intent to clear history, trigger haptics on success.
- Click item: Read aloud content (interruptible by exiting screen or clicking another item).

## 2. Technical Standards
- Adhere to `android-compose-best-practices.mdc`.
- Adhere to `rules-management.mdc`.
- App icon: Ensure correct placement and conventional naming.
