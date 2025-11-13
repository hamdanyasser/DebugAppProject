# DebugMaster - Play Store Submission Checklist

Complete checklist for Google Play Store submission. Check off each item before submitting your app.

---

## Pre-Submission Checklist

### 1. App Development Complete

- [ ] All features from Phases 0-5 implemented and tested
- [ ] App runs smoothly in guest mode (100% offline)
- [ ] Optional Firebase integration documented but NOT required
- [ ] No critical bugs or crashes
- [ ] All unit tests passing: `./gradlew test`
- [ ] Manual QA completed (see `docs/QA_CHECKLIST.md`)

### 2. Version Management

- [ ] `versionCode` set in `app/build.gradle.kts` (e.g., `1` for first release)
- [ ] `versionName` set with semantic versioning (e.g., `"1.0.0"`)
- [ ] Version numbers documented in git commit
- [ ] Git tag created: `git tag v1.0.0`

### 3. Build Configuration

- [ ] Release build type configured in `app/build.gradle.kts`
- [ ] `minifyEnabled = true` and `isShrinkResources = true` enabled
- [ ] ProGuard rules complete in `proguard-rules.pro`
- [ ] Signing keystore generated (see `docs/RELEASE_BUILD_GUIDE.md`)
- [ ] Keystore backed up securely (password manager, encrypted drive)
- [ ] Signing configuration set up (environment variables or `local.properties`)

### 4. Build and Test Release

- [ ] Release AAB built successfully: `./gradlew bundleRelease`
- [ ] Release APK built for testing: `./gradlew assembleRelease`
- [ ] Release APK installed on physical device: `adb install ...`
- [ ] Tested all critical features on release build:
  - [ ] App launches without crashes
  - [ ] Onboarding displays correctly
  - [ ] Navigation works (all tabs)
  - [ ] Bug solving and XP system works
  - [ ] Achievements unlock correctly
  - [ ] Notifications work (if enabled)
  - [ ] Settings persist
  - [ ] Offline mode works perfectly
- [ ] No ProGuard-related crashes or issues
- [ ] App size is reasonable (< 20 MB for AAB)
- [ ] `mapping.txt` file saved from `app/build/outputs/mapping/release/`

---

## Play Console Setup

### 5. Developer Account

- [ ] Google Play Developer account created ($25 one-time fee)
- [ ] Account verified and active
- [ ] Payment profile set up (if needed for future paid apps)

### 6. App Creation

- [ ] New app created in Play Console
- [ ] App name: **DebugMaster** (or variant from `PLAY_STORE_LISTING.md`)
- [ ] Default language: **English (US)**
- [ ] App type: **App**
- [ ] Free or paid: **Free**
- [ ] Declarations accepted

---

## Store Listing

### 7. Store Listing Details

Use content from `docs/PLAY_STORE_LISTING.md`:

- [ ] **App name**: DebugMaster: Learn Java Bugs (max 30 chars)
- [ ] **Short description**: Entered (max 80 chars)
  - Example: "Master Java debugging by fixing real code. Earn XP, build streaks, unlock badges!"
- [ ] **Full description**: Entered (up to 4000 chars)
  - Includes sections: What it does, Key Features, Who it's for, Why it's different
- [ ] **App category**: Education
- [ ] **Tags** (if applicable): Programming, Coding, Learning, Education

### 8. Graphic Assets

- [ ] **App icon**: 512×512 PNG, < 1 MB (uploaded)
  - Design follows concept in `PLAY_STORE_LISTING.md`
  - Readable at small sizes
  - No rounded corners (Play applies masking automatically)
- [ ] **Feature graphic**: 1024×500 JPG or PNG, < 1 MB (uploaded)
  - Shows app name and mockup/visual
  - Professional appearance
- [ ] **Phone screenshots**: 2-8 screenshots (uploaded)
  - Framed with device mockups
  - Text overlays added
  - Readable code/UI elements
  - Shows: Onboarding, Learning Paths, Bug Detail, Bug of Day, Profile, Achievements
  - Order tells a story

### 9. Optional Assets

- [ ] Promo video (30-60 seconds on YouTube) - Optional but recommended
- [ ] TV banner (1280×720) - Only if supporting Android TV (N/A for DebugMaster)
- [ ] 7-inch tablet screenshots - Optional
- [ ] 10-inch tablet screenshots - Optional

---

## App Content & Compliance

### 10. Content Rating

- [ ] Content rating questionnaire completed
- [ ] Expected rating: **Everyone / PEGI 3 / ESRB E**
- [ ] No violence, gambling, adult content
- [ ] Safe for all ages

### 11. Target Audience

- [ ] Target age group selected: **Ages 13+** (students, developers)
- [ ] Appeals to children: **No**

### 12. Privacy Policy

- [ ] Privacy policy created (see `docs/PRIVACY_POLICY_DRAFT.md`)
- [ ] Privacy policy hosted online (GitHub Pages, personal website, etc.)
- [ ] Privacy policy URL entered in Play Console
- [ ] URL is publicly accessible and loads correctly

**Recommended hosts:**
- GitHub Pages: `https://yourusername.github.io/debugmaster-privacy`
- Personal domain: `https://yourdomain.com/privacy`
- Google Sites (free)

### 13. Data Safety Form

- [ ] Data safety form completed
- [ ] **If Firebase NOT configured (default):**
  - [ ] Data collection: **NO**
  - [ ] Data sharing: **NO**
- [ ] **If Firebase configured (optional):**
  - [ ] Data types: Email, Name, App activity (progress)
  - [ ] Purpose: App functionality (sync)
  - [ ] Data sharing: **No third parties**
  - [ ] Data encrypted in transit: **YES**
  - [ ] User can request data deletion: **YES**
  - [ ] Data collection optional: **YES** (guest mode available)

### 14. Ads Declaration

- [ ] Ads in app: **NO**
- [ ] App is completely ad-free

### 15. App Access

- [ ] Special access requirements: **None**
- [ ] App does not require special permissions beyond standard (notifications, network)

---

## Technical Requirements

### 16. APK/AAB Upload

- [ ] **Release AAB** uploaded: `app-release.aab`
- [ ] Play Console processing complete (no errors)
- [ ] App analyzed successfully (security scan passed)
- [ ] APK/AAB signed with release keystore

### 17. Version Info

- [ ] Release name entered (e.g., "1.0.0 - Initial Release")
- [ ] Release notes entered (see `RELEASE_BUILD_GUIDE.md` for template)
- [ ] Languages supported: English (minimum)

### 18. Device Compatibility

- [ ] Minimum SDK: **API 26 (Android 8.0)** - Covers 95%+ devices
- [ ] Target SDK: **API 34 (Android 14)** - Latest stable
- [ ] Supported devices: **Phones and tablets**
- [ ] Excluded devices: None (unless specific compatibility issues found)

---

## Firebase Configuration (Optional)

### 19. Firebase Setup (ONLY if enabling cloud sync)

**If NOT using Firebase (default), skip this section entirely.**

- [ ] Firebase project created in Firebase Console
- [ ] `google-services.json` downloaded and added to `app/` directory
- [ ] SHA-1 fingerprint added to Firebase project (for Google Sign-In)
- [ ] Firebase Authentication enabled (Google provider)
- [ ] Firestore database created with security rules
- [ ] `com.google.gms.google-services` plugin applied in `build.gradle.kts`
- [ ] App tested with Firebase enabled (sign-in, sync work correctly)

**If using guest mode only (recommended for initial launch):**
- [ ] All Firebase items marked N/A or skipped
- [ ] App builds and runs without `google-services.json`
- [ ] "Firebase not configured" messages displayed appropriately

---

## Pre-Launch Testing

### 20. Internal Testing (Optional but Recommended)

- [ ] Create internal testing track in Play Console
- [ ] Upload AAB to internal track
- [ ] Add internal testers (your email + team members)
- [ ] Testers install and test app via Play Store
- [ ] Collect feedback and fix critical issues
- [ ] Update version and re-upload if needed

### 21. Closed Beta Testing (Optional)

- [ ] Create closed beta track
- [ ] Upload AAB
- [ ] Invite beta testers (friends, colleagues, community)
- [ ] Set up opt-in URL
- [ ] Monitor feedback, crashes, ANRs
- [ ] Iterate based on feedback

### 22. Open Beta Testing (Optional)

- [ ] Create open beta track
- [ ] Upload AAB
- [ ] Make beta publicly available
- [ ] Promote beta to community (Reddit, Twitter, etc.)
- [ ] Collect wide feedback before production launch

---

## Production Release

### 23. Final Review

- [ ] All above checklist items completed
- [ ] Store listing reviewed for typos and clarity
- [ ] Screenshots and graphics look professional
- [ ] Privacy policy is accurate and accessible
- [ ] Release AAB is the correct version
- [ ] All team members have reviewed submission

### 24. Rollout Plan

Choose rollout strategy:

**Option A: Staged Rollout (Recommended for First Release)**
- [ ] Start with 5% of users
- [ ] Monitor for 24-48 hours (check crashes, ratings)
- [ ] Increase to 10%, 20%, 50%, 100% gradually
- [ ] Pause rollout if critical issues found

**Option B: Full Rollout**
- [ ] Release to 100% of users immediately
- [ ] Monitor closely for first 24 hours
- [ ] Be ready to halt rollout or push hotfix if needed

### 25. Submit for Review

- [ ] Click **"Review release"** in Play Console
- [ ] Review all sections one final time
- [ ] Click **"Start rollout to Production"** (or chosen track)
- [ ] Confirm submission

### 26. Post-Submission

- [ ] Submission confirmed (email from Google Play)
- [ ] Review status: **In review** (typically 1-3 days)
- [ ] Check email daily for approval or rejection
- [ ] Monitor Play Console dashboard

---

## Post-Launch Monitoring

### 27. First 24 Hours After Launch

- [ ] App approved and live on Play Store
- [ ] Install app from Play Store (verify it's live)
- [ ] Check Play Console dashboard hourly:
  - [ ] Install count
  - [ ] Crash rate (target: < 1%)
  - [ ] ANR rate (target: < 0.5%)
  - [ ] User ratings
- [ ] Respond to early reviews (thank positive reviews, address issues)
- [ ] Fix critical bugs immediately if found

### 28. First Week After Launch

- [ ] Monitor crashes and ANRs daily
- [ ] Upload ProGuard mapping file to Play Console for crash deobfuscation
- [ ] Track user acquisition sources
- [ ] Monitor keyword rankings
- [ ] Collect user feedback and feature requests
- [ ] Plan first update based on feedback

### 29. Ongoing Maintenance

- [ ] Set up crash reporting (Play Console or Crashlytics)
- [ ] Respond to user reviews within 48 hours
- [ ] Release bug fix updates as needed
- [ ] Plan feature updates (every 1-2 months)
- [ ] Monitor competitor apps
- [ ] Update screenshots/listing based on new features

---

## Rejection Troubleshooting

### If App is Rejected

- [ ] Read rejection email carefully
- [ ] Review Play Console rejection details
- [ ] Fix issues mentioned by reviewer
- [ ] Common rejection reasons:
  - Incomplete store listing
  - Missing privacy policy
  - Inaccurate content rating
  - Broken functionality in app
  - Policy violations (crashes, deceptive behavior)
- [ ] Re-submit after fixes
- [ ] Contact Google Play Support if rejection unclear

---

## Resources

**Official Links:**
- [Play Console](https://play.google.com/console)
- [Google Play Policies](https://play.google.com/about/developer-content-policy/)
- [Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [Play Academy](https://playacademy.exceedlms.com/student/catalog) - Free courses

**DebugMaster Documentation:**
- `docs/PLAY_STORE_LISTING.md` - Store listing content
- `docs/RELEASE_BUILD_GUIDE.md` - Build and signing guide
- `docs/QA_CHECKLIST.md` - Manual testing checklist
- `docs/PRIVACY_POLICY_DRAFT.md` - Privacy policy template
- `docs/IMPLEMENTATION_STATUS.md` - Development status

---

## Quick Submission Summary

**Minimum Required:**

1. ✅ Release AAB built and signed
2. ✅ App icon (512×512 PNG)
3. ✅ Feature graphic (1024×500)
4. ✅ 2+ phone screenshots
5. ✅ Short description (80 chars)
6. ✅ Full description (up to 4000 chars)
7. ✅ Privacy policy URL
8. ✅ Content rating completed
9. ✅ Data safety form filled
10. ✅ Release notes written

**Recommended (But Optional):**

- Internal/beta testing phase
- Promo video
- 8 screenshots (instead of minimum 2)
- Tablet screenshots
- Multiple language support

---

## Estimated Timeline

**First-time submission:**
- Setup and documentation: 2-4 hours
- Graphic assets creation: 4-8 hours
- Store listing writing: 1-2 hours
- Release build and testing: 2-3 hours
- Play Console setup: 1-2 hours
- **Total: 10-19 hours**

**Google Play review:**
- Typical: 1-3 days
- Can be as quick as a few hours
- Rarely: up to 7 days

**Subsequent updates:**
- Version bump and build: 30 minutes
- Testing: 1-2 hours
- Release notes and upload: 30 minutes
- **Total: 2-3 hours per update**

---

## Contact & Support

**Questions about submission?**
- [Google Play Support](https://support.google.com/googleplay/android-developer)
- [r/androiddev community](https://reddit.com/r/androiddev)
- Review DebugMaster docs in `docs/` folder

**Good luck with your launch!** 🚀

---

**Last Updated:** 2025-11-13
