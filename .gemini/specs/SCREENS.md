# Screen Specifications

## 1. SetupHomeScreen
*   **Purpose**: Initial configuration for the user's impairment level.
*   **UI**: Two large buttons dividing the screen vertically.
    *   Padding: 2dp.
    *   Corners: Rounded.
    *   Labels: "Khiếm thị một phần" (Partially Impaired) and "Khiếm thị hoàn toàn" (Totally Impaired).
*   **Behavior**:
    *   **On Open**: Speaks "Xin chào. Chọn chế độ: khiếm thị một phần hoặc khiếm thị hoàn toàn?" (VI) or "Hello! Choose mode: Partially Impaired or Totally Impaired?" (EN).
    *   **OnClick**: Speaks label, triggers haptics, stores value, redirects to respective screen.
    *   **On Long Click**: Speaks label, triggers haptics, does nothing else.
    *   **Voice Command**: Listens for labels. If matched, speaks label, stores value, and redirects.

## 2. PartiallyImpairedHomeScreen
*   **Purpose**: Main hub for partially impaired users.
*   **UI**:
    *   Two vertically stacked buttons: "Camera" and "OCR".
    *   Bottom Bar: 3 icons (Home, Settings, History).
*   **Behavior**:
    *   **On Open**: Speaks "màn hình chính" (VI/EN based on settings).
    *   **Buttons (Camera/OCR)**:
        *   OnClick: Speaks "Chụp ảnh"/"Đọc chữ" (VI) or "Camera"/"OCR" (EN), triggers haptics, redirects.
        *   On Long Click: Same as OnClick but no redirect.
    *   **Bottom Bar**:
        *   OnClick: Speaks name, triggers haptics. Redirects to Settings/History only. Home icon does not redirect.
        *   On Long Click: Speaks name, triggers haptics, no redirect.
    *   **Voice Command**: "Cài đặt" or "Lịch sử" (VI) redirects to respective screen.

## 3. TotallyImpairedHomeScreen
*   **Purpose**: Main hub for totally impaired users.
*   **UI/Behavior**: Identical to `PartiallyImpairedHomeScreen` but **without** the Bottom Bar.

## 4. CameraScreen & OCRScreen
*   **Behavior**:
    *   Existing functionality remains.
    *   **On Open**: Speaks "Chụp ảnh" (Camera) or "Đọc chữ" (OCR).
    *   **Voice Command**: "Quay lại" (Back) triggers back navigation.

## 5. SettingsScreen
*   **UI Order**:
    1.  Title: "Cài đặt" (Settings).
    2.  Vibration Toggle: "Cho phép rung" (Enable vibration). Defaults to ON.
    3.  Language Toggle: "Tiếng Việt" (English). Defaults to "Tiếng Việt".
    4.  Speech Rate: "Tốc độ nói" (Speech rate) with a slider bar.
    5.  Themes Button: "Thay đổi màu sắc" (Themes). Redirects to `ThemesScreen`.
    6.  Music Button: Circle, right-aligned, "{music note icon}". Different color from other components.
*   **Behavior**:
    *   Music Button: Plays "https://youtu.be/HGguPt27Pzg" (stored locally). Exiting screen stops playback.
    *   Voice Command: "Quay lại" (Back) triggers back navigation.

## 6. ThemesScreen
*   **UI**:
    *   Title: "Thay đổi màu sắc" (Themes).
    *   Scrollable list of 20 themes.
    *   Theme Component: Wide-spanning, name in middle, 5 colors horizontally stacked.
*   **Behavior**:
    *   **Selection**: Updates app colors, speaks theme name in English, triggers haptics.
    *   Voice Command: "Quay lại" (Back) triggers back navigation.

## 7. HistoryScreen
*   **UI**:
    *   Title: "Lịch sử" (History).
    *   Scrollable list of previous results from OCR/Camera.
