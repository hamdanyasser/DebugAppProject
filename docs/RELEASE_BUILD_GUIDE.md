# DebugMaster - Release Build Guide

Complete guide for building and signing release APK/AAB files for Google Play Store submission.

---

## Overview

This guide covers:
1. **Versioning** - How to manage versionCode and versionName
2. **Keystore Creation** - Generate signing key for release builds
3. **Signing Configuration** - Set up local signing without committing secrets
4. **Building Release APK/AAB** - Command-line and Android Studio methods
5. **Testing Release Build** - Verify before submission
6. **Play Store Upload** - Final submission steps

---

## 1. Version Management

### Version Numbers

DebugMaster uses two version identifiers:

**versionCode (Integer):**
- Internal version number for Google Play
- Must increment for every release
- Never reuse a versionCode
- Current: `1` (in `app/build.gradle.kts`)

**versionName (String):**
- User-facing version string
- Uses semantic versioning: `MAJOR.MINOR.PATCH`
- Current: `1.0.0`

### Semantic Versioning Rules

```
MAJOR.MINOR.PATCH

MAJOR: Incompatible API changes or major redesigns
MINOR: New features, backward-compatible
PATCH: Bug fixes, backward-compatible
```

**Examples:**
- `1.0.0` → `1.0.1` - Bug fix release
- `1.0.1` → `1.1.0` - Added cloud sync feature
- `1.1.0` → `2.0.0` - Complete UI redesign

### How to Update Versions

**In `app/build.gradle.kts`:**

```kotlin
defaultConfig {
    // ...
    versionCode = 2  // Increment by 1
    versionName = "1.0.1"  // Update semantic version
}
```

**Workflow:**
1. Make changes to app
2. Test thoroughly
3. Update `versionCode` (increment by 1)
4. Update `versionName` (follow semantic versioning)
5. Commit changes: `git commit -m "Bump version to 1.0.1"`
6. Create git tag: `git tag v1.0.1`
7. Build release APK/AAB
8. Upload to Play Store

---

## 2. Generate Signing Keystore

### What is a Keystore?

A **keystore** is a file containing your app's signing key. Google Play requires all releases to be signed with the same key for updates.

**CRITICAL:** Never lose your keystore or forget your passwords! If you do, you cannot update your app on Play Store.

### Create Keystore (Command Line)

Run this command in your project root directory:

```bash
keytool -genkey -v \
  -keystore release-keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias debugmaster-release
```

**You will be prompted for:**
1. **Keystore password** - Choose a strong password (you'll need this for every release)
2. **Key password** - Can be same as keystore password or different
3. **Your name and organization details** - Can use defaults or actual info
4. **Alias** - Use `debugmaster-release` or any memorable name

**Example session:**
```
Enter keystore password: [your-password-here]
Re-enter new password: [your-password-here]
What is your first and last name? [Unknown]: Your Name
What is the name of your organizational unit? [Unknown]: Development
What is the name of your organization? [Unknown]: YourCompany
What is the name of your City or Locality? [Unknown]: San Francisco
What is the name of your State or Province? [Unknown]: CA
What is the two-letter country code for this unit? [Unknown]: US
Is CN=Your Name, OU=Development, O=YourCompany, L=San Francisco, ST=CA, C=US correct? [no]: yes

Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA)
    with a validity of 10,000 days for: CN=Your Name, ...
```

**Result:** A file named `release-keystore.jks` is created in your project root.

### Backup Your Keystore

**IMPORTANT:** Backup your keystore file and passwords securely!

1. **Copy keystore to secure location:**
   - External hard drive
   - Encrypted cloud storage (1Password, Bitwarden vault)
   - USB drive in a safe

2. **Store passwords securely:**
   - Use a password manager (1Password, Bitwarden, LastPass)
   - Write down and store in a safe
   - **Never commit passwords to git!**

3. **Share with team (if applicable):**
   - Encrypted file sharing (Keybase, PGP-encrypted email)
   - Secure company vault

---

## 3. Signing Configuration

### Option A: Environment Variables (Recommended for CI/CD)

Set environment variables before building:

**Linux/macOS:**
```bash
export KEYSTORE_FILE="/path/to/release-keystore.jks"
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="debugmaster-release"
export KEY_PASSWORD="your-key-password"
```

**Windows (PowerShell):**
```powershell
$env:KEYSTORE_FILE="C:\path\to\release-keystore.jks"
$env:KEYSTORE_PASSWORD="your-keystore-password"
$env:KEY_ALIAS="debugmaster-release"
$env:KEY_PASSWORD="your-key-password"
```

**Then uncomment signing config in `app/build.gradle.kts`:**

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release-keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("KEY_ALIAS") ?: ""
        keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... other config
    }
}
```

### Option B: local.properties (Recommended for Local Development)

**Create `local.properties` in project root** (already gitignored):

```properties
# Keystore configuration (DO NOT COMMIT)
keystore.file=release-keystore.jks
keystore.password=your-keystore-password
key.alias=debugmaster-release
key.password=your-key-password
```

**Update `app/build.gradle.kts` to read from local.properties:**

```kotlin
// At the top of the file, after plugins block
val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ...

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["keystore.file"] ?: "release-keystore.jks")
            storePassword = keystoreProperties["keystore.password"] as String? ?: ""
            keyAlias = keystoreProperties["key.alias"] as String? ?: ""
            keyPassword = keystoreProperties["key.password"] as String? ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other config
        }
    }
}
```

### Option C: Android Studio (Manual Signing)

Don't configure signing in Gradle. Instead, sign manually after building unsigned APK.

See "Manual Signing" section below.

---

## 4. Building Release APK/AAB

### Build Types

**APK (Android Package):**
- Traditional Android app format
- Can be installed directly on devices
- Larger file size (contains all resources)
- Good for testing and direct distribution

**AAB (Android App Bundle):**
- Modern format required by Google Play
- Google Play generates optimized APKs for each device
- Smaller download size for users
- **Required for Play Store submission** (since August 2021)

### Command Line Build

**Build Release AAB (recommended for Play Store):**
```bash
./gradlew bundleRelease
```

**Output location:**
```
app/build/outputs/bundle/release/app-release.aab
```

**Build Release APK (for testing):**
```bash
./gradlew assembleRelease
```

**Output location:**
```
app/build/outputs/apk/release/app-release.apk
```

### Android Studio Build

**Method 1: Build Menu**

1. Open Android Studio
2. Click **Build** → **Generate Signed Bundle / APK**
3. Select **Android App Bundle** (for Play Store) or **APK** (for testing)
4. Click **Next**
5. Choose **Create new keystore** (first time) or **Choose existing**
6. Enter keystore path, passwords, and alias
7. Click **Next**
8. Select **release** build variant
9. Choose signature versions: **V1** and **V2** (both)
10. Click **Finish**

**Method 2: Build Variants**

1. In Android Studio, open **Build Variants** tab (bottom left)
2. Select **release** variant
3. Click **Build** → **Build Bundle(s) / APK(s)** → **Build Bundle(s)**

### Manual Signing (if building unsigned)

If you built an unsigned APK, sign it manually:

```bash
jarsigner -verbose \
  -sigalg SHA256withRSA \
  -digestalg SHA-256 \
  -keystore release-keystore.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  debugmaster-release
```

Then align the APK:

```bash
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release-signed.apk
```

---

## 5. Testing Release Build

**Before uploading to Play Store, test the release build thoroughly!**

### Install Release APK on Device

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### Test Checklist

Run through the manual QA checklist:

- [ ] App launches successfully
- [ ] Onboarding shows on first launch
- [ ] Navigate through all screens (Learn, Bug of Day, Profile, Settings)
- [ ] Solve a bug, verify XP awarded
- [ ] Check achievement unlocking
- [ ] Test daily reminders toggle
- [ ] Verify settings persistence
- [ ] Test offline mode (airplane mode on)
- [ ] Check ProGuard didn't break anything:
  - [ ] Room database queries work
  - [ ] Achievements unlock correctly
  - [ ] WorkManager notifications work
  - [ ] Navigation works
  - [ ] ViewBinding inflates correctly
- [ ] Check app size (should be smaller than debug build)
- [ ] Verify no crashes or ANRs

**Check app size:**
```bash
ls -lh app/build/outputs/bundle/release/app-release.aab
```

Target: < 20 MB for AAB

### Verify ProGuard Mapping

After building, a mapping file is created:

```
app/build/outputs/mapping/release/mapping.txt
```

**SAVE THIS FILE!** You need it to deobfuscate crash reports from Play Console.

---

## 6. Play Store Upload

### Google Play Console Setup

**First-time setup (one-time):**

1. Go to [Google Play Console](https://play.google.com/console)
2. Create developer account ($25 one-time fee)
3. Click **Create app**
4. Fill in app details:
   - App name: DebugMaster
   - Default language: English (US)
   - App type: App
   - Free or paid: Free
5. Accept declarations
6. Click **Create app**

### Complete App Setup

**Before uploading APK/AAB, complete these sections:**

1. **App content:**
   - Privacy policy (host `PRIVACY_POLICY_DRAFT.md` online)
   - Ads declaration: No ads
   - Target audience: Ages 13+
   - App category: Education

2. **Store listing:**
   - App name, descriptions (use `docs/PLAY_STORE_LISTING.md`)
   - Screenshots (see `PLAY_STORE_LISTING.md`)
   - Feature graphic
   - App icon

3. **Content rating:**
   - Complete questionnaire
   - Expected rating: Everyone / PEGI 3

4. **Data safety:**
   - Fill out data safety form
   - Guest mode: No data collected
   - Signed-in mode: Email, name, progress (optional)

### Upload Release AAB

1. Go to **Release** → **Production** → **Create new release**
2. Click **Upload** and select `app-release.aab`
3. Wait for upload and processing
4. Enter **Release name**: `1.0.0 - Initial Release`
5. Enter **Release notes:**
   ```
   🎉 Welcome to DebugMaster!

   Master Java debugging with:
   • 15+ real code challenges
   • Structured learning paths
   • XP, levels, and achievements
   • Daily Bug of the Day
   • Smart 3-level hint system
   • 100% offline capable

   Start fixing bugs today!
   ```
6. Click **Next** → **Save**
7. Click **Review release**
8. Review all details
9. Click **Start rollout to Production**

### Rollout Options

- **Full rollout:** Release to 100% of users immediately
- **Staged rollout:** Release to 5%, 10%, 20%, 50%, 100% gradually (recommended for first release)

### After Submission

- **Review time:** 1-3 days typically
- **Check email** for approval or rejection
- **Monitor Play Console** for crashes, ANRs, ratings
- **Respond to reviews** to build community

---

## 7. Troubleshooting

### Build Errors

**Error: "Keystore not found"**
- Check path in signing configuration
- Verify keystore file exists
- Use absolute path if relative path fails

**Error: "Incorrect keystore password"**
- Double-check password
- Try re-entering in Android Studio dialog
- Regenerate keystore if password truly lost (but can't update existing app!)

**Error: "Duplicate class found"**
- Check for conflicting dependencies
- Run `./gradlew dependencies` to see dependency tree
- Exclude duplicate dependencies

**Error: "ProGuard error"**
- Check ProGuard rules in `proguard-rules.pro`
- Add `-keep` rules for classes causing issues
- Review R8 build logs in `app/build/outputs/logs/`

### Runtime Issues in Release

**App crashes in release but not debug:**
- ProGuard/R8 may have removed necessary classes
- Check crash logs for `ClassNotFoundException` or `MethodNotFoundException`
- Add `-keep` rules for affected classes
- Upload `mapping.txt` to Play Console to deobfuscate crash reports

**Features broken in release:**
- Check ProGuard rules for Room, Gson, WorkManager
- Test with `./gradlew assembleRelease` locally before uploading
- Enable verbose ProGuard logging: `-verbose` in `proguard-rules.pro`

---

## 8. Version Bump Workflow

**For each new release:**

1. **Make changes** to app
2. **Test thoroughly** using `debug` build
3. **Run unit tests:** `./gradlew test`
4. **Update version** in `app/build.gradle.kts`:
   ```kotlin
   versionCode = 2  // Increment
   versionName = "1.0.1"  // Update
   ```
5. **Commit version bump:**
   ```bash
   git add app/build.gradle.kts
   git commit -m "Bump version to 1.0.1"
   git tag v1.0.1
   git push origin main --tags
   ```
6. **Build release:** `./gradlew bundleRelease`
7. **Test release APK** on device
8. **Upload to Play Console**
9. **Add release notes** describing changes
10. **Roll out** (staged or full)
11. **Monitor** Play Console for crashes

---

## 9. Security Best Practices

### Never Commit These Files

**Add to `.gitignore`:**
```
# Keystore files
*.jks
*.keystore

# Signing configuration
local.properties

# Release outputs (optional - can commit if needed)
app/build/outputs/
```

**Check `.gitignore` is working:**
```bash
git status
# Should NOT show release-keystore.jks or local.properties
```

### Keystore Security

- **Store in password manager:** Use 1Password, Bitwarden, etc.
- **Encrypt backups:** Use encrypted storage (VeraCrypt, Cryptomator)
- **Limit access:** Only authorized team members should have keystore
- **Use strong passwords:** 16+ characters, mix of letters, numbers, symbols
- **Never share via email:** Use secure file sharing (Keybase, encrypted Dropbox)

### CI/CD Security

**If using GitHub Actions or similar:**
- Store keystore as **encrypted secret**
- Use GitHub Secrets for passwords
- Never log passwords in CI output
- Use ephemeral build environments

---

## 10. Useful Commands Cheat Sheet

**Build commands:**
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build release AAB (for Play Store)
./gradlew bundleRelease

# Clean build
./gradlew clean

# Run unit tests
./gradlew test

# Check dependencies
./gradlew dependencies
```

**ADB commands:**
```bash
# Install APK
adb install app/build/outputs/apk/release/app-release.apk

# Install AAB (requires bundletool)
java -jar bundletool.jar install-apks --apks=app.apks

# Uninstall app
adb uninstall com.example.debugappproject

# View logs
adb logcat | grep DebugMaster
```

**Keystore commands:**
```bash
# Generate keystore
keytool -genkey -v -keystore release-keystore.jks ...

# List keystore entries
keytool -list -v -keystore release-keystore.jks

# Change keystore password
keytool -storepasswd -keystore release-keystore.jks

# Verify APK signature
jarsigner -verify -verbose -certs app-release.apk
```

---

## 11. Resources

**Official Documentation:**
- [Android: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android: Build and test your app](https://developer.android.com/studio/build)
- [Google Play: Publish your app](https://developer.android.com/distribute/best-practices/launch)

**Tools:**
- [Bundletool](https://github.com/google/bundletool) - Test AAB files locally
- [APK Analyzer](https://developer.android.com/studio/build/apk-analyzer) - Inspect APK contents
- [Play Console](https://play.google.com/console) - Upload and manage releases

**Community:**
- [r/androiddev](https://reddit.com/r/androiddev) - Android developer community
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android) - Technical Q&A

---

## Summary

**Quick Start (First Release):**

1. Generate keystore: `keytool -genkey ...`
2. Update versions in `build.gradle.kts`
3. Configure signing (use `local.properties`)
4. Build: `./gradlew bundleRelease`
5. Test release APK thoroughly
6. Upload AAB to Play Console
7. Complete store listing (use `PLAY_STORE_LISTING.md`)
8. Submit for review
9. Monitor approval and launch!

**For Subsequent Releases:**

1. Update `versionCode` and `versionName`
2. `./gradlew bundleRelease`
3. Test release build
4. Upload to Play Console (Production or Beta track)
5. Add release notes
6. Roll out

Good luck with your release! 🚀
