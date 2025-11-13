# DebugMaster Privacy Policy

**Last Updated:** November 13, 2025
**Effective Date:** November 13, 2025

---

## Introduction

Welcome to DebugMaster! This Privacy Policy explains how we collect, use, and protect your information when you use our mobile application.

DebugMaster is an educational app designed to help users learn Java debugging through interactive exercises. We are committed to protecting your privacy and being transparent about our data practices.

---

## Information We Collect

### 1. Local Data (All Users)

DebugMaster stores the following data **locally on your device** using Room database:

- **Bug Progress:** Which bugs you've solved, completion status, timestamps
- **Hints Used:** Number of hints viewed globally and per bug
- **Experience Points (XP):** Total XP earned, current level, XP progress
- **Achievements:** Which achievements you've unlocked and when
- **Streak Data:** Current streak days, longest streak ever achieved
- **Learning Path Progress:** Which paths you're working on and completion percentage
- **User Notes:** Any personal notes you add to bugs
- **Settings Preferences:** Notification preferences, theme choice, font size

**This data never leaves your device unless you explicitly enable cloud sync (see below).**

### 2. Cloud Data (Authenticated Users Only)

If you choose to sign in with Google (optional), we additionally collect:

- **Firebase User ID:** Unique identifier from Google
- **Email Address:** From your Google account
- **Display Name:** From your Google account
- **Profile Photo URL:** From your Google account
- **Synced Progress Data:** A copy of your local progress data (bugs, XP, achievements, etc.) stored in Google Firestore for cross-device sync

**Cloud sync is entirely optional.** You can use DebugMaster 100% offline in Guest Mode without any account.

### 3. Data We Do NOT Collect

We do **NOT** collect:

- Location data
- Device identifiers (beyond Firebase auth)
- Usage analytics or tracking (unless Firebase Analytics is enabled - see below)
- Personal information beyond what's provided by Google Sign-In
- Payment information (app is free, no in-app purchases)
- Contacts, photos, or other device data

---

## How We Use Your Information

### Local Data

- **To provide app functionality:** Track your learning progress, award XP, unlock achievements
- **To personalize your experience:** Remember your settings, preferences, and progress
- **To calculate streaks:** Determine if you've solved bugs on consecutive days

### Cloud Data (If Authenticated)

- **To enable cross-device sync:** Access your progress on multiple devices
- **To restore progress:** Recover your data if you reinstall the app or switch devices
- **To provide account features:** Display your name and photo in the Profile screen

---

## Data Storage & Security

### Local Storage

- All local data is stored in an encrypted SQLite database (Room) on your device
- Data remains on your device and is not transmitted anywhere (unless you enable cloud sync)
- If you uninstall the app, all local data is deleted automatically

### Cloud Storage (If Authenticated)

- Cloud data is stored in **Google Firestore** (a Google Cloud service)
- Data is encrypted in transit (HTTPS) and at rest
- Access is restricted to authenticated users only (via Firebase Authentication)
- We use industry-standard security practices to protect your data

---

## Data Sharing & Third Parties

### We Do NOT Sell or Share Your Data

We do not sell, rent, or share your personal information with third parties for marketing purposes.

### Third-Party Services We Use

If you enable cloud sync, your data is processed by:

1. **Firebase Authentication (Google LLC)**
   - Used for Google Sign-In
   - Privacy Policy: https://policies.google.com/privacy
   - Data shared: Email, name, profile photo, user ID

2. **Google Firestore (Google LLC)**
   - Used for cloud data storage
   - Privacy Policy: https://policies.google.com/privacy
   - Data stored: User ID, bug progress, XP, achievements, streaks

### Optional: Firebase Analytics

**Note:** The current version of DebugMaster does NOT include Firebase Analytics. If we add it in the future:
- Usage analytics would be collected (screens viewed, features used, crashes)
- Data would be anonymous and aggregated
- You would be able to opt out in Settings
- We would update this policy and notify users

---

## Your Rights & Choices

### Guest Mode (No Account)

- You can use the app entirely offline without creating an account
- All data stays on your device
- No personal information is collected

### Delete Local Data

To delete your local progress data:
1. Open **Settings** in the app
2. Tap **Reset Progress**
3. Confirm deletion

This will erase all local data (bugs, XP, achievements, streaks). Cannot be undone.

### Delete Cloud Data

If you signed in with Google and want to delete your cloud data:

**Option 1:** Use the app
1. Sign out of your account in Settings
2. Your local data remains, but cloud sync stops
3. Your cloud data will be automatically deleted after 30 days of inactivity

**Option 2:** Contact us
- Email: [YOUR_EMAIL_HERE]
- Request account and data deletion
- We will delete your cloud data within 30 days

### Data Portability

You can export your data:
- Local data: Use Android's backup feature or manually export database
- Cloud data: Contact us at [YOUR_EMAIL_HERE] to request a data export

---

## Children's Privacy

DebugMaster is intended for educational purposes and is suitable for users of all ages. However, we do not knowingly collect personal information from children under 13 without parental consent.

If you are under 13:
- Use Guest Mode (no account required)
- Do not sign in with Google without parental permission
- Ask a parent or guardian for help

If you are a parent and believe your child has provided us with personal information without your consent, please contact us at [YOUR_EMAIL_HERE], and we will delete the information.

---

## Changes to This Policy

We may update this Privacy Policy from time to time to reflect changes in:
- App features
- Data practices
- Legal requirements

When we update this policy:
- We will update the "Last Updated" date at the top
- If changes are significant, we will notify you via in-app message or email (if applicable)
- Continued use of the app after changes constitutes acceptance of the new policy

We encourage you to review this policy periodically.

---

## Data Retention

### Local Data

- Retained indefinitely on your device until you delete the app or reset progress
- Automatically deleted when you uninstall the app

### Cloud Data

- Retained as long as your account is active
- Deleted 30 days after you sign out or request deletion
- Backups may be retained for up to 90 days for disaster recovery

---

## Your Consent

By using DebugMaster, you consent to this Privacy Policy.

By signing in with Google (optional), you additionally consent to:
- Sharing your Google account information (email, name, photo)
- Storing your progress data in Google Firestore
- Syncing data across devices

You can withdraw consent at any time by signing out or deleting your account.

---

## International Data Transfers

If you use cloud sync, your data may be transferred to and stored on servers in countries other than your own. This is because we use Google Firestore, which stores data in Google's global data centers.

Google complies with international data protection laws, including GDPR (Europe) and CCPA (California). Your data is protected regardless of where it is stored.

---

## Contact Us

If you have questions, concerns, or requests regarding this Privacy Policy or your data, please contact us:

**Email:** [YOUR_EMAIL_HERE]

**Mailing Address (if applicable):**
[YOUR_NAME]
[YOUR_ADDRESS]

**Response Time:** We aim to respond to all inquiries within 7 business days.

---

## Compliance with Laws

We comply with applicable data protection laws, including:

- **GDPR (General Data Protection Regulation)** - Europe
- **CCPA (California Consumer Privacy Act)** - California, USA
- **COPPA (Children's Online Privacy Protection Act)** - USA

If you are in the European Union, you have additional rights under GDPR:
- Right to access your data
- Right to rectification (correct your data)
- Right to erasure ("right to be forgotten")
- Right to restrict processing
- Right to data portability
- Right to object to processing

To exercise these rights, contact us at [YOUR_EMAIL_HERE].

---

## Open Source & Transparency

DebugMaster is committed to transparency:
- Our database schema is documented in the app's architecture documentation
- We use open-source libraries (Room, Firebase, etc.)
- No hidden tracking or data collection

---

## Disclaimer

This app is provided for educational purposes. While we take reasonable measures to protect your data, no system is 100% secure. Use of the app is at your own risk.

We are not responsible for:
- Data loss due to device failure, user error, or other causes
- Unauthorized access to your device
- Third-party actions (e.g., Google service outages)

---

## Summary (TL;DR)

✅ **Guest Mode:** Use the app offline, no account needed, data stays on your device
✅ **Google Sign-In (Optional):** Sync progress across devices, stored in Google Firestore
✅ **No Tracking:** We don't track you or show ads
✅ **Your Control:** Delete your data anytime in Settings
✅ **Transparent:** We only collect what's needed for app functionality

---

**Last Updated:** November 13, 2025

**Version:** 1.0
