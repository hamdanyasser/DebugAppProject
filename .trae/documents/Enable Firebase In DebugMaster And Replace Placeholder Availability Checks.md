## Verification
- Check `app/google-services.json` exists and read its `client_info.android_client_info.package_name`.
- Confirm it matches `com.example.debugappproject.debug` (debug applicationId).
- If file is missing or package_name mismatched, report and pause Gradle plugin application to avoid build failure.

## Gradle Wiring (Kotlin DSL)
- Root `build.gradle.kts`:
  - Add `id("com.google.gms.google-services") version "4.4.2" apply false` under `plugins`.
- Module `app/build.gradle.kts`:
  - `plugins { alias(libs.plugins.android.application); id("com.google.gms.google-services") }`.
  - Keep existing Firebase BoM and `firebase-auth` dependencies.

## Availability Check
- Update `auth/AuthManager.java:isFirebaseAvailable()`:
  - Try `FirebaseApp.getInstance()`; if `IllegalStateException`, attempt `FirebaseApp.initializeApp(context)`.
  - Return true if an app exists after init; catch and log exceptions, return false otherwise.
- No changes to core business logic; signed-in state still controlled by saved prefs.

## UI Messaging
- `ui/profile/ProfileFragment.java`:
  - In the sign-in button click: if `!authManager.isFirebaseAvailable()`, show the "firebase not configured" toast; else show a placeholder toast like "Firebase available. Sign-in UI pending" (or proceed to GoogleSignIn if already scaffolded).
- `ui/settings/SettingsFragment.java`:
  - For Sync Now: keep existing checks, but they now depend on real `isFirebaseAvailable()`.

## Build & Run
- `./gradlew clean assembleDebug` then `./gradlew installDebug`.
- Launch debug app activity: `com.example.debugappproject.debug/com.example.debugappproject.MainActivity`.
- Verify:
  - With valid `google-services.json`, no more "firebase not configured" warning; profile shows sign-in action without warning and sync doesn’t crash.
  - If `google-services.json` is removed temporarily, app stays in guest mode and shows the clear message.

## Summary Output
- List Gradle files changed and plugin versions used.
- List classes/methods updated (AuthManager.isFirebaseAvailable, ProfileFragment sign-in click, SettingsFragment sync).
- Reproduction steps in Android Studio (Run, expected screens).