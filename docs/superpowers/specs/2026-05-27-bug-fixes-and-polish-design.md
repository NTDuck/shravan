# Design Doc: Critical Bug Fixes and Final UI Polish

## 1. App Icon (Debug Cleanup)
### Goal
Remove debug launcher icons that override the main icons and ensure the main icons are correctly set.

### Strategy
- Use `run_shell_command` to delete all files starting with `ic_launcher` in `app/src/debug/res/mipmap*`.
- Confirm `ic_launcher.png` and `ic_launcher_round.png` are present in `app/src/main/res/mipmap*` (based on the PNG icon provided by the user).

## 2. Crash Fix (Voice & Camera)
### VoiceCommandManager.kt
#### Issues
- `speechRecognizer` is recreated on every `startListening`.
- No explicit `stopCurrentSession` to clean up properly.
- Race conditions with `currentSessionId`.

#### Design
- Create `speechRecognizer` once in `init`.
- Implement `stopCurrentSession()` to cancel and remove listeners.
- `startListening` will cancel any ongoing recognition before starting a new one.
- Use a single listener instance and manage state via session IDs more cleanly.

### CameraPreview.kt
#### Issues
- `cameraProviderFuture.get()` is called directly inside `remember`, which blocks the UI thread.
- Race conditions when binding/unbinding use cases.

#### Design
- Use `LaunchedEffect` to handle the asynchronous `ProcessCameraProvider`.
- Use `cameraProviderFuture.addListener` to safely get the provider without blocking.
- Ensure `unbindAll()` is called properly before binding new use cases.

## 3. Navbar Fix (MainScreen.kt)
### Goal
Remove the "pill" indicator from `NavigationBarItem` and ensure colors are visible.

### Design
- Explicitly set `indicatorColor = Color.Transparent` in `NavigationBarItemDefaults.colors`.
- Set `selectedIconColor = Color.White` and `selectedTextColor = Color.White` for clarity.

## 4. Rename Explore (strings.xml)
### Goal
Ensure `nav_explore` is set to "Explore Screen".

### Design
- Verify and update `app/src/main/res/values/strings.xml`.

## 5. Verification Plan
- **App Icon:** Check file existence.
- **Voice:** Test multiple `startListening` calls to ensure no crashes or overlaps.
- **Camera:** Verify the app opens and camera preview starts smoothly without UI freeze.
- **Navbar:** Visual inspection (if possible) or code verification of properties.
- **Strings:** Confirm `strings.xml` content.
