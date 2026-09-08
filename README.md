# Vibe Forge

> AI agent Android IDE: on-device app builder (MODE_A) and AOSP SystemUI design assist (MODE_B).

## Modes

- **MODE_A - App Builder**: natural language -> real APK compiled and installed on-device (Java only, single-Activity, no external dependencies).
- **MODE_B - AOSP Design Assist**: edit AOSP SystemUI sources (Kotlin+Java+XML) with on-device mockup preview, then commit and push to a real Git repo. Builds happen off-device (PC/CI) - never on-device.

## Status

- Phase 1: Foundation & Chat UI (dark Material 3, 5-tab navigation, static chat bubbles)

## Build

CI builds the debug APK on every push (artifact: `vibeforge-debug-apk`).

```bash
./gradlew :app:assembleDebug
```
