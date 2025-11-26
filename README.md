# DebugMaster - Learn Debugging Through Play 🐛

A gamified Android app to learn debugging skills by finding and fixing bugs in code.

## 🎮 Features

### Free Features
- **30+ Debugging Challenges** - Real-world bugs to solve
- **2 Learning Paths** - Java Fundamentals & Loops/Control Flow
- **Daily Challenges** - New bug every day with bonus XP
- **Achievement System** - 20+ achievements to unlock
- **Progress Tracking** - XP, levels, streaks, stats

### Pro Features ($4.99/month or $39.99/year)
- **100+ Challenges** - All difficulty levels
- **6 Learning Paths** - Complete curriculum
- **Battle Arena** - Multiplayer competition
- **Unlimited Practice** - No daily limits
- **Ad-Free Experience**
- **Detailed Analytics**
- **Lifetime Option** - $99.99 one-time

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11+
- Android SDK 34
- A physical device or emulator (API 26+)

### Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device/emulator

### First Run
- The app seeds the database with 30 bugs on first launch
- Users start at Level 1 with 0 XP
- Daily challenges reset at midnight local time

## 📱 Play Store Publishing Checklist

### Before Publishing

1. **Change Package Name**
   - Update `applicationId` in `app/build.gradle.kts`
   - Example: `com.yourcompany.debugmaster`

2. **Create Signing Key**
   ```bash
   keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias debugmaster
   ```

3. **Configure Signing in Gradle**
   - Add to `gradle.properties`:
   ```
   RELEASE_STORE_FILE=../release-key.jks
   RELEASE_STORE_PASSWORD=your_password
   RELEASE_KEY_ALIAS=debugmaster
   RELEASE_KEY_PASSWORD=your_password
   ```

4. **Update App Icon**
   - Replace icons in `app/src/main/res/mipmap-*`
   - Use Android Studio Image Asset Studio

5. **Set Up Google Play Console**
   - Create developer account ($25 one-time)
   - Create new app listing
   - Set up internal testing track first

### Google Play Billing Setup

1. **In Google Play Console:**
   - Go to Monetize > Products > Subscriptions
   - Create products with these IDs:
     - `debugmaster_pro_monthly` - $4.99/month
     - `debugmaster_pro_yearly` - $39.99/year
   - Create in-app product:
     - `debugmaster_lifetime` - $99.99 one-time

2. **Testing:**
   - Add license testers in Play Console
   - Use internal testing track
   - Test purchase flows

### Required Play Store Assets

1. **Screenshots** (2-8 per device type)
   - Phone: 1080x1920 or 1920x1080
   - Tablet 7": 1200x1920
   - Tablet 10": 1600x2560

2. **Feature Graphic**
   - 1024x500 PNG/JPEG

3. **App Icon**
   - 512x512 PNG (32-bit with alpha)

4. **Store Listing**
   - Short description (80 chars)
   - Full description (4000 chars)
   - Category: Education
   - Tags: coding, programming, debugging, learning

### Privacy Policy
You must have a privacy policy. Template included at `docs/privacy-policy.md`

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/debugappproject/
│   │   ├── billing/          # Google Play Billing
│   │   ├── data/            
│   │   │   ├── local/        # Room database
│   │   │   ├── repository/   # Data repositories
│   │   │   └── seeding/      # Initial data
│   │   ├── model/            # Data classes
│   │   ├── ui/               # Fragments & ViewModels
│   │   │   ├── home/
│   │   │   ├── learn/
│   │   │   ├── profile/
│   │   │   ├── subscription/
│   │   │   └── ...
│   │   └── util/             # Utilities
│   ├── assets/
│   │   └── bugs.json         # Bug database
│   └── res/                  # Resources
└── build.gradle.kts
```

## 🎯 Technical Details

- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **DI**: Hilt
- **Navigation**: Jetpack Navigation
- **Billing**: Google Play Billing Library 6.1.0

## 📊 Analytics Events (Recommended)

Track these for optimization:
- `bug_started` - User begins challenge
- `bug_completed` - Challenge solved
- `hint_used` - Hint requested
- `path_completed` - Learning path finished
- `subscription_viewed` - Pro screen opened
- `subscription_purchased` - Successful purchase
- `subscription_cancelled` - Purchase cancelled

## 🔧 Customization

### Adding New Bugs
Edit `app/src/main/assets/bugs.json`:

```json
{
  "id": 31,
  "title": "Your Bug Title",
  "language": "Java",
  "difficulty": "Easy|Medium|Hard",
  "category": "Loops|Strings|etc",
  "description": "What's wrong?",
  "brokenCode": "code with bug",
  "expectedOutput": "what should happen",
  "actualOutput": "what actually happens",
  "explanation": "why the bug occurs",
  "fixedCode": "corrected code"
}
```

### Adding Hints
```json
{
  "bugId": 31,
  "level": 1,
  "text": "First hint (vague)"
}
```

## 📄 License

This project is proprietary. All rights reserved.

## 🤝 Support

For issues or feature requests, contact: your-email@example.com

---

Made with ❤️ for developers who want to master debugging!
