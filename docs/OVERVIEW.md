# Shravan: Environmental Awareness for the Visually Impaired

## Introduction
Shravan is a cutting-edge Android application specifically designed to empower individuals with visual impairments. Named after the Sanskrit word for "hearing," the app acts as a digital pair of eyes, translating visual information into auditory and tactile feedback. By leveraging on-device Artificial Intelligence, Shravan provides real-time object detection, text recognition, and currency identification without requiring a constant internet connection.

## Target Audience
Shravan is built with two distinct user profiles in mind:
1.  **Partially Blind Individuals**: Users who can still perceive high-contrast shapes or colors. For these users, the app provides a high-contrast UI and visual bounding boxes for detected objects.
2.  **Totally Blind Individuals**: Users who rely entirely on non-visual cues. Shravan prioritizes voice commands, high-fidelity Text-to-Speech (TTS), and sophisticated haptic patterns to navigate the world.

## Core Mission
The mission of Shravan is to foster independence. We believe that environmental awareness—knowing that a chair is three feet away, reading a medicine label, or identifying a banknote—is a fundamental right. Shravan bridges the accessibility gap by providing a seamless, low-latency interface that feels like an extension of the user's senses.

## Key User Flows

### First-Time Setup
Upon the first launch, Shravan guides users through an "Intent Clarification" phase. Users select their level of impairment, which dynamically adjusts the app's behavior (e.g., faster speech rates and more frequent haptic pulses for totally blind users).

### Real-Time Exploration (Explore Mode)
As the user moves their camera, Shravan continuously scans the environment. Objects are announced via TTS with a priority-based queue, ensuring that the most relevant information (like an "Obstable" or "Person") is delivered first.

### Targeted Search (Find Mode)
Users can ask the app to find specific objects (e.g., "Find a chair"). As the camera points closer to the target, the frequency of haptic vibrations increases, providing a tactile "hot/cold" game mechanism to guide the user.

### Information Retrieval (OCR & Currency)
- **OCR**: Real-time reading of documents, street signs, or product labels.
- **Currency**: Specialized recognition for Vietnamese Dong (VND), helping users manage their finances independently.
