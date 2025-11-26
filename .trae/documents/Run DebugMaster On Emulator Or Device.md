## Approach
- Verify available Android SDK emulator images or connected devices.
- If no device is connected, start the first available AVD emulator.
- Build and install the debug APK onto the device/emulator.
- Launch the app’s main activity.

## Steps
1. Check for connected devices and available AVDs.
2. If an AVD exists, start it and wait for boot completion; otherwise use any connected device.
3. Build and install: `./gradlew installDebug`.
4. Launch activity: `adb shell am start -n com.example.debugappproject.debug/com.example.debugappproject.MainActivity`.
5. Verify UI loads (Home hub, Bug of the Day, Bug Detail interactions).

## Notes
- Uses the SDK path already configured in `local.properties`.
- Package name for debug build is `com.example.debugappproject.debug` due to `applicationIdSuffix`.
- If no AVDs are available locally, we’ll report and suggest running from Android Studio’s AVD Manager to create one, then repeat the steps.