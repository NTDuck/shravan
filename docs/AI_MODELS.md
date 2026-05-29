# AI Models: Perception & Optimization

## Object Detection
Shravan employs the YOLOv5 (You Only Look Once) architecture for real-time object detection, optimized for mobile devices.

### Models
- **`yolov5s-fp16.tflite`**: A small-footprint YOLOv5 model quantized to 16-bit floating point. It is trained on the COCO dataset to recognize 80 classes of common objects.
- **`currency.tflite`**: A specialized model trained specifically for the recognition of Vietnamese Dong (VND) banknotes, supporting denominations from 1,000 to 500,000 VND.

### Model Execution Logic
The detection engine operates at the sensor's native frame rate, with several optimizations to ensure a smooth user experience:
- **Pre-allocated Memory**: To reduce Garbage Collection (GC) pressure and eliminate "stutter," Shravan uses pre-allocated 1D float arrays to capture TFLite inference outputs.
- **Dynamic Input Resizing**: The system automatically scales camera frames to the required input dimensions (416x416 for COCO, 640x640 for Currency) while maintaining aspect ratio via letterboxing.

## Advanced Logic & Optimizations

### Persistence Buffer (Anti-Flicker)
One of the biggest challenges in real-time detection is "flickering" (where an object disappears and reappears between frames). Shravan solves this with a **5-frame Persistence Buffer**:
- If an object is detected in frame N but missing in frame N+1, the system retains the detection for up to 5 consecutive frames.
- This ensures that tactile haptic feedback and voice announcements remain consistent even if the AI momentarily loses confidence.

### Dynamic Debounce
To prevent "information overload," Shravan dynamically adjusts its announcement frequency:
- **Totally Blind Mode**: Debounce interval is set to **1000ms**, prioritizing speed and immediate awareness.
- **Partially Blind Mode**: Debounce interval is set to **2000ms**, providing a calmer experience as visual cues are still available.

### Luminance-Based Auto-Flash
The `YoloAnalyzer` continuously monitors the average luminance of incoming frames. If the environment drops below a specific threshold (e.g., 40.0 lux), the system can automatically trigger the camera's torch if the user has set Flash to "Auto."

## Text Recognition (OCR)
Shravan uses **Google ML Kit** for its OCR capabilities. 
- **Bundled Version**: The model is bundled within the APK, ensuring that text recognition works entirely offline.
- **Real-Time Stream**: Unlike traditional "snap-and-read" OCR, Shravan processes the camera stream in real-time, providing immediate feedback as the user pans over text.

## Haptic Proximity Alerts
In "Find" mode, the app calculates the distance of the target object from the center of the frame. 
- **Feedback Interval**: Nearer objects trigger faster haptic pulses.
- **Implementation**: This is handled via the `HapticManager`, which translates distance/confidence scores into millisecond-accurate vibration patterns.
