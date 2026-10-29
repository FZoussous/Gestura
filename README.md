# Gestura
**Move beyond the mouse.** A JavaFX desktop vision playground with JavaCV/OpenCV webcam capture and a fading mouse-driven particle trail prototype.

## Run
```bash
mvn clean test
mvn javafx:run
```
The first build downloads native computer-vision dependencies and may take a while. On macOS, allow camera access to the app/terminal in **System Settings → Privacy & Security → Camera**.

## Current implementation status
- JavaFX desktop UI
- Start/stop webcam capture and live preview
- Fading particle trail driven by mouse movement
- **Hand landmark detection and actual finger gesture recognition are not implemented yet.** The trail is a visual prototype, not hand tracking.

## Roadmap
1. Validate camera access and native dependency behavior on Apple Silicon.
2. Integrate a Java-compatible hand-landmark model and verify model licensing.
3. Track landmarks, smooth motion, and classify gestures.
4. Map pinch/open palm/finger poses to effects.
5. Add performance metrics and gesture-classifier tests.

## Privacy
The MVP does not upload camera frames. Review future dependencies before introducing any network processing.
