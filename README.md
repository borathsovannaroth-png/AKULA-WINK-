# AKULA WINK PRO

An independent Android photo/video editing starter project with an original dark-purple interface. It is **not affiliated with, endorsed by, or a modified version of Wink**.

## Current MVP features
- Import a photo from the device.
- Preview the selected photo.
- Apply a simple brightness adjustment and save an edited copy to the device gallery.
- Open a video from the device in Android's built-in video player.
- Dark purple/black interface and GitHub Actions APK build workflow.

## Important limitations
This is a starter MVP, not a full AI editor or a copy of Wink Premium. AI face restoration, 4K upscaling, advanced retouching, timelines, transitions, and video rendering are not implemented yet. The video action currently opens the selected clip for playback; it does not re-encode or enhance it.

## Build with GitHub
1. Create a new GitHub repository.
2. Upload the contents of this ZIP to the repository root.
3. Open **Actions** and enable workflows if GitHub asks.
4. Run **Build Android APK** (or push to `main`).
5. Download `akula-wink-pro-debug-apk` from the workflow run's Artifacts section.

## Build locally
Install Android Studio and JDK 17, then run:

```bash
gradle assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## App identity
- App name: AKULA WINK PRO
- Package: `com.akula.winkpro`
- Minimum Android version: Android 7.0 (API 24)
- Target SDK: 35

## Next development steps
1. Add a proper editing canvas with crop, rotate, filters, and undo/redo.
2. Add a video timeline and safe export pipeline.
3. Integrate a licensed on-device or cloud AI enhancement model.
4. Add tests for image processing and export.
