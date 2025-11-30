# DebugMaster - Setup Instructions

## 🔧 Required Dependencies (add to app/build.gradle)

Add these dependencies to your `app/build.gradle` file in the `dependencies` block:

```gradle
dependencies {
    // Existing dependencies...
    
    // Google Sign-In
    implementation 'com.google.android.gms:play-services-auth:21.0.0'
    
    // Glide for profile image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
}
```

## 🔐 Google Sign-In Setup

### Step 1: Create OAuth Credentials in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing one
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client ID**
5. Select **Web application** (Yes, Web - this is required for Android)
6. Give it a name like "DebugMaster Web Client"
7. Copy the **Client ID** - you'll need this!

### Step 2: Configure SHA-1 Fingerprint

1. Get your debug SHA-1:
```bash
cd android
./gradlew signingReport
```

Or manually:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

2. In Google Cloud Console > Credentials:
   - Click **Create Credentials** > **OAuth 2.0 Client ID**
   - Select **Android**
   - Package name: `com.example.debugappproject`
   - Add your SHA-1 fingerprint

### Step 3: Update the Code

Open `GoogleAuthManager.java` and replace the placeholder:

```java
// Replace this line (around line 53):
private static final String WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

// With your actual Web Client ID:
private static final String WEB_CLIENT_ID = "123456789-abcdefg.apps.googleusercontent.com";
```

### Step 4: (Optional) Add google-services.json

If you're using Firebase:
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Add your Android app
3. Download `google-services.json`
4. Place it in `app/` folder

## 📱 Features Added

### 1. Enhanced Learning Paths (15 Total)
- **1 FREE path** - "Getting Started" (complete intro)
- **14 PRO paths** including:
  - ☕ Java Mastery
  - 🐍 Python Power
  - ⚡ JavaScript Ninja
  - 📱 Kotlin & Android
  - 🤖 **Prompt Engineering** (AI course!)
  - 🧠 AI/ML Debugging
  - 🗄️ SQL & Databases
  - 🔌 API & Backend
  - 🎨 HTML & CSS Bugs
  - ⚛️ React Debugging
  - 📚 Data Structures
  - 🧮 Algorithm Bugs
  - ✨ Clean Code
  - 💼 Interview Prep

### 2. Google Sign-In
- One Tap Sign-In (modern, streamlined)
- Fallback to standard Sign-In
- Profile photo display
- Sign out functionality

### 3. 45+ Achievements
- Bug fixing milestones
- Streak achievements (up to 365 days!)
- Speed achievements
- Path completion
- Language mastery
- Battle Arena achievements
- Special fun achievements

### 4. Splash Screen Sounds
- AMBIENT_INTRO on start
- LOGO_WHOOSH when logo appears
- LOADING_COMPLETE when loading finishes
- BUTTON_APPEAR when PLAY button shows

## ⚠️ Important Notes

1. **First Run After Update**: The app will migrate the database to add new learning paths. This happens automatically.

2. **Clearing Data**: If you encounter issues, try clearing app data to get fresh database seeding.

3. **Pro Subscription**: Only 1 path is free. Subscribing to Pro unlocks all 14+ premium paths.

4. **Google Sign-In Without Setup**: The app works in guest mode if Google Sign-In is not configured. Users can still use all features locally.

## 🐛 Troubleshooting

### Build Error: "cannot find symbol BugDao"
The BattleArenaFragment has been updated to use the correct import. Make sure you have the latest version.

### Google Sign-In Not Working
1. Check that WEB_CLIENT_ID is correct
2. Verify SHA-1 fingerprint matches
3. Check that package name is correct in Google Cloud Console

### Database Issues
Reset the database by clearing app data in Android settings.

---

Happy debugging! 🚀
