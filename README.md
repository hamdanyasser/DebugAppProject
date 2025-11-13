# DebugMaster - Mimo-Style Debugging Learning App

**Tagline:** *Learn Java debugging by fixing real code - with lessons, XP, and achievements!*

An interactive Android application that teaches debugging skills through structured learning paths, micro-lessons, and gamification. Inspired by Mimo's engaging learning experience.

---

## 📱 Project Overview

**DebugMaster** is a production-ready educational Android app that teaches Java debugging through hands-on practice. The app combines:

- **🎯 Structured Learning Paths** - Progress from basics to advanced challenges
- **📚 Micro-Lessons** - Learn concepts before debugging
- **🎮 Gamification** - Earn XP, level up, unlock achievements
- **🔥 Daily Streaks** - "Bug of the Day" keeps you motivated
- **🏆 15 Achievements** - Track your debugging mastery
- **💡 Smart Hints** - Progressive 3-level hint system
- **📊 Detailed Progress** - Stats, levels, and badges

---

## ✨ Key Features

### 🎯 Learning Paths (NEW!)

Structured progression through debugging concepts:
- **Basics of Debugging** - Common beginner mistakes
- **Nulls & Crashes** - Exception handling mastery
- **Collections & Arrays** - Data structure debugging
- **Advanced Challenges** - Complex, multi-faceted bugs

Each path shows your progress and guides you through bugs sequentially.

### 📚 Interactive Lessons (NEW!)

Before debugging, learn the underlying concepts:
- Short, focused micro-lessons (2-3 minutes)
- Interactive quizzes to reinforce learning
- Seamless flow: Lesson → Quiz → Debug

### 🎮 XP & Leveling System (NEW!)

Earn experience points and level up:
- **Easy bugs:** 10 XP
- **Medium bugs:** 20 XP
- **Hard bugs:** 30 XP
- **No-hint bonus:** +5 XP
- **Level formula:** Level = 1 + (XP / 100)

Achievements also award XP!

### 🏆 Achievement System (NEW!)

15 unlockable badges to collect:
- 🎉 **First Fix** - Solve your first bug
- 🦸 **No-Hint Hero** - Solve 3 bugs without hints
- 🗡️ **Array Assassin** - Master all array bugs
- 🔥 **Streak Machine** - Maintain a 7-day streak
- 👑 **Completionist** - Solve all bugs
- ...and 10 more!

### 🔥 Bug of the Day

- **Daily practice:** New bug every day
- **Streak tracking:** Build your debugging habit
- **Longest streak:** All-time record tracking
- **Notifications:** Daily reminders (optional)

### 💡 Progressive Hint System

3-level hint system acts as your AI mentor:
1. **Level 1:** Gentle nudge toward the bug
2. **Level 2:** More specific guidance
3. **Level 3:** Nearly explicit hint
4. **Solution:** Complete fix with explanation

Solving without hints earns bonus XP!

### 📊 Detailed Progress Tracking

- Total bugs solved (by difficulty)
- Current level and XP progress
- Current streak and longest streak
- Hints used statistics
- Bugs solved without hints
- Achievement collection progress

---

## 🏗️ Technical Architecture

### Architecture Pattern

The app follows **MVVM (Model-View-ViewModel)** architecture:

```
ui/                     # Presentation Layer
├── home/              # HomeFragment + HomeViewModel
├── buglist/           # BugListFragment + BugListViewModel + BugAdapter
├── bugdetail/         # BugDetailFragment + BugDetailViewModel
├── progress/          # ProgressFragment + ProgressViewModel
└── splash/            # SplashFragment

data/                   # Data Layer
├── local/             # Room Database v3, DAOs
│   ├── DebugMasterDatabase.java
│   ├── BugDao.java
│   ├── HintDao.java
│   ├── UserProgressDao.java
│   ├── LearningPathDao.java       (NEW!)
│   ├── LessonDao.java              (NEW!)
│   └── AchievementDao.java         (NEW!)
├── repository/        # BugRepository (single source of truth)
└── seeding/           # DatabaseSeeder

model/                  # Domain Models / Room Entities
├── Bug.java
├── Hint.java
├── UserProgress.java
├── LearningPath.java               (NEW!)
├── BugInPath.java                  (NEW!)
├── Lesson.java                     (NEW!)
├── LessonQuestion.java             (NEW!)
├── AchievementDefinition.java      (NEW!)
└── UserAchievement.java            (NEW!)

util/
├── DateUtils.java     # Bug of the day, streak calculation
├── Constants.java
├── CodeComparator.java
└── AchievementManager.java         (NEW!)
```

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | **Java 11** (100%) |
| UI | XML Layouts + Fragments |
| Navigation | Android Navigation Component |
| Database | **Room 2.6.1** (SQLite) |
| Architecture | **MVVM + LiveData** |
| UI Components | Material Design 3 |
| View Binding | Enabled |
| Gamification | Custom XP & Achievement System |
| Dependency Injection | Manual (Repository pattern) |

### Key Libraries

```gradle
// Room for local database
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'

// ViewModel & LiveData
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'

// Navigation Component
implementation 'androidx.navigation:navigation-fragment:2.6.0'
implementation 'androidx.navigation:navigation-ui:2.6.0'

// Material Design
implementation 'com.google.android.material:material:1.11.0'

// Gson for JSON parsing
implementation 'com.google.code.gson:gson:2.10.1'
```

---

## 📊 Database Schema (v3)

### New in Version 3 (Mimo-Style Transformation)

#### Learning Paths Table
```sql
CREATE TABLE learning_paths (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    description TEXT,
    iconEmoji TEXT,
    difficultyRange TEXT,
    sortOrder INTEGER,
    isLocked INTEGER
);
```

#### Bug-Path Junction Table
```sql
CREATE TABLE bug_in_path (
    bugId INTEGER,
    pathId INTEGER,
    orderInPath INTEGER,
    PRIMARY KEY(bugId, pathId),
    FOREIGN KEY(bugId) REFERENCES bugs(id),
    FOREIGN KEY(pathId) REFERENCES learning_paths(id)
);
```

#### Lessons Table
```sql
CREATE TABLE lessons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bugId INTEGER,
    title TEXT,
    content TEXT,
    estimatedMinutes INTEGER,
    FOREIGN KEY(bugId) REFERENCES bugs(id)
);
```

#### Lesson Questions Table
```sql
CREATE TABLE lesson_questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lessonId INTEGER,
    questionText TEXT,
    questionType TEXT,              -- "MCQ" or "TRUE_FALSE"
    optionsJson TEXT,               -- JSON array of options
    correctOptionIndex INTEGER,
    explanation TEXT,
    orderInLesson INTEGER,
    FOREIGN KEY(lessonId) REFERENCES lessons(id)
);
```

#### Achievement Definitions Table
```sql
CREATE TABLE achievement_definitions (
    id TEXT PRIMARY KEY,            -- "first_fix", "no_hint_hero", etc.
    name TEXT,
    description TEXT,
    iconEmoji TEXT,
    xpReward INTEGER,
    category TEXT,                  -- MILESTONE, STREAK, SKILL, SPEED
    sortOrder INTEGER
);
```

#### User Achievements Table
```sql
CREATE TABLE user_achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    achievementId TEXT,
    unlockedTimestamp INTEGER,
    notificationShown INTEGER,
    FOREIGN KEY(achievementId) REFERENCES achievement_definitions(id)
);
```

#### Enhanced User Progress
```sql
CREATE TABLE user_progress (
    id INTEGER PRIMARY KEY,         -- Always 1 (singleton)
    totalSolved INTEGER,
    streakDays INTEGER,
    easySolved INTEGER,
    mediumSolved INTEGER,
    hardSolved INTEGER,
    lastSolvedTimestamp INTEGER,
    lastOpenedTimestamp INTEGER,
    xp INTEGER,                     -- Total experience points
    hintsUsed INTEGER,
    bugsSolvedWithoutHints INTEGER,
    longestStreakDays INTEGER       -- NEW!
);
```

See `docs/ARCHITECTURE_DEBUGMASTER.md` for complete schema documentation.

---

## 🎨 User Interface

### Current Screens

1. **Splash Screen**
   - App logo and tagline
   - Database seeding on first launch
   - Auto-navigation to home

2. **Home Screen**
   - Bug of the Day card
   - Quick statistics (level, XP, solved, streak)
   - Navigation buttons

3. **Bug List Screen**
   - RecyclerView of all bugs
   - Difficulty filter (All/Easy/Medium/Hard)
   - Category filter dropdown
   - Completion indicators

4. **Bug Detail Screen**
   - Bug description
   - Broken code display (monospace font)
   - Run Code button (shows output comparison)
   - Progressive 3-level hint system
   - Solution reveal with explanation
   - Mark as solved (awards XP)

5. **Progress Screen**
   - Overall progress with percentage
   - Level and XP progress bar
   - Streak display (current & longest)
   - Difficulty breakdown
   - Reset progress option

### Planned Screens (Phase 2)

6. **Learning Paths Screen**
   - Modern card-based path list
   - Progress bars per path
   - Locked/unlocked path indicators

7. **Path Detail Screen**
   - Bugs in sequential order
   - Status badges (New, In Progress, Completed)

8. **Profile Screen**
   - User level and XP prominently displayed
   - Achievement grid (locked/unlocked)
   - Detailed stats

9. **Bug of the Day Screen**
   - Dedicated daily bug card
   - Countdown to next bug
   - Streak tracking

10. **Onboarding Screens**
    - 3-4 screens introducing features
    - ViewPager2 with skip option

11. **Settings Screen**
    - Notification preferences
    - Reminder time picker
    - Theme selection
    - Larger font toggle
    - Reset progress

---

## 🧪 Sample Bugs

The app includes 15 carefully crafted debugging exercises:

| # | Title | Difficulty | Category | Concept |
|---|-------|-----------|----------|---------|
| 1 | Off-by-One Error in Loop | Easy | Loops | Boundary conditions |
| 2 | NullPointerException | Easy | Exceptions | Null checking |
| 3 | String Comparison Error | Easy | Strings | == vs .equals() |
| 4 | Integer Division Truncation | Medium | Methods | Type casting |
| 5 | Wrong Variable in Condition | Easy | Conditionals | Logic errors |
| 6 | Missing Break in Switch | Medium | Conditionals | Fall-through |
| 7 | ArrayList IndexOutOfBounds | Medium | Collections | Array indexing |
| 8 | Infinite Loop | Easy | Loops | Loop control |
| 9 | Variable Shadowing | Hard | OOP | Scope |
| 10 | Wrong Return Value | Easy | Methods | Return logic |
| 11 | Array Index Error | Medium | Arrays | Zero-indexing |
| 12 | Incorrect Operator Precedence | Medium | Methods | Math operations |
| 13 | Modifying Collection While Iterating | Hard | Collections | ConcurrentModification |
| 14 | Static vs Instance Confusion | Hard | OOP | Static context |
| 15 | Uninitialized Variable | Medium | Conditionals | Initialization |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11+
- Android SDK API 36 (minSdk can be lowered to 24 for wider compatibility)
- Gradle 8.13

### Building the Project

1. **Clone the repository:**
   ```bash
   git clone https://github.com/hamdanyasser/DebugAppProject.git
   cd DebugAppProject
   ```

2. **Open in Android Studio:**
   - File → Open → Select project directory

3. **Sync Gradle:**
   - Android Studio will automatically sync dependencies
   - If sync fails, ensure internet connection and JDK 11 is configured

4. **(Optional) Firebase Setup:**
   - **For cloud sync features (optional):**
     - Create a Firebase project at https://console.firebase.google.com
     - Download `google-services.json`
     - Place in `app/` folder
     - **DO NOT commit this file** (already in .gitignore)
   - **To run without Firebase:**
     - App works 100% offline without google-services.json
     - Cloud sync features will be disabled gracefully

5. **Run the app:**
   - Select an emulator or physical device
   - Click Run (Shift + F10)

### First Launch

On first launch, the app will:
1. Display splash screen
2. Seed database with:
   - 15 bugs and hints from `assets/bugs.json`
   - 4 learning paths
   - 15 achievement definitions
3. Create initial UserProgress record
4. Navigate to home screen

**Database version will be 3** after first launch.

---

## 🎓 Educational Value

### What Students Learn

1. **Common Bug Patterns**
   - Off-by-one errors
   - Null pointer exceptions
   - String comparison pitfalls (== vs .equals)
   - Collection modification during iteration
   - Variable scope and shadowing
   - Static vs instance context

2. **Debugging Strategies**
   - Reading and analyzing error messages
   - Comparing expected vs actual output
   - Using hints progressively (not giving up too early)
   - Understanding root causes (not just symptoms)
   - Building mental models of code execution

3. **Android Development Skills**
   - MVVM architecture pattern
   - Room database with migrations
   - LiveData and ViewModels
   - Navigation Component
   - RecyclerView with adapters
   - Material Design 3 principles
   - Repository pattern
   - Background threading (ExecutorService)

4. **Gamification Psychology** (NEW!)
   - XP and leveling systems
   - Achievement design
   - Streak mechanics for habit building
   - Progress tracking and visualization

---

## 📝 Code Quality

### Best Practices Implemented

- ✅ **Clean Architecture:** Separation of concerns (UI, Data, Domain)
- ✅ **MVVM Pattern:** Clear separation between business logic and UI
- ✅ **SOLID Principles:** Single responsibility, dependency inversion
- ✅ **LiveData:** Reactive, lifecycle-aware data handling
- ✅ **Repository Pattern:** Single source of truth for data
- ✅ **View Binding:** Type-safe view access (no findViewById)
- ✅ **Room Migrations:** Proper database version management
- ✅ **Material Design 3:** Modern, consistent UI
- ✅ **JavaDoc Comments:** All public methods documented
- ✅ **Naming Conventions:** Meaningful class and variable names

---

## 🔄 Extensibility

### Adding New Bugs

1. Edit `assets/bugs.json`
2. Add new bug object with all required fields:
   ```json
   {
     "id": 16,
     "title": "Your Bug Title",
     "difficulty": "Medium",
     "category": "YourCategory",
     "description": "What the program should do",
     "brokenCode": "...",
     "expectedOutput": "...",
     "actualOutput": "...",
     "explanation": "Root cause",
     "fixedCode": "...",
     "hints": [
       {"level": 1, "text": "Gentle hint"},
       {"level": 2, "text": "More specific"},
       {"level": 3, "text": "Almost explicit"}
     ]
   }
   ```
3. Clear app data to re-seed database, or manually insert via SQL

### Adding New Learning Paths

Update `DatabaseSeeder.seedLearningPaths()`:
```java
LearningPath newPath = new LearningPath(
    "Your Path Name",
    "Description",
    "🎯",              // Emoji icon
    "Easy-Medium",     // Difficulty range
    5,                 // Sort order
    false              // isLocked
);
```

### Adding New Achievements

1. Define achievement ID constant in `AchievementManager`
2. Add to `getDefaultAchievements()`:
   ```java
   achievements.add(new AchievementDefinition(
       "your_achievement_id",
       "Achievement Name",
       "Description of what user must do",
       "🏅",
       25,  // XP reward
       "MILESTONE",
       14   // Sort order
   ));
   ```
3. Add checking logic in `checkAndUnlockAchievements()`:
   ```java
   private void checkYourAchievement(UserProgress progress, List<String> unlocked) {
       if (/* your condition */ && !isUnlocked("your_achievement_id")) {
           unlockAchievement("your_achievement_id");
           unlocked.add("your_achievement_id");
       }
   }
   ```

### Adding Lessons to Bugs

Currently no UI for lessons (Phase 2), but data model is ready:
```java
Lesson lesson = new Lesson(
    bugId,
    "Lesson Title",
    "Lesson content explaining the concept...",
    2  // Estimated minutes
);
repository.insertLessons(Arrays.asList(lesson));

LessonQuestion question = new LessonQuestion(
    lessonId,
    "What causes a NullPointerException?",
    "MCQ",
    "[\"Dividing by zero\", \"Accessing null object\", \"Array overflow\"]",
    1,  // Correct option index (0-based)
    "NPE occurs when you try to use an object that is null.",
    1   // Order in lesson
);
repository.insertLessonQuestions(Arrays.asList(question));
```

---

## 📐 Architecture Documentation

For detailed architecture information, see:

- **`docs/ARCHITECTURE_DEBUGMASTER.md`** - Complete technical architecture
- **`docs/IMPLEMENTATION_STATUS.md`** - Transformation progress and next steps
- **`docs/PRIVACY_POLICY_DRAFT.md`** - Privacy policy for Play Store

---

## 🐛 Known Limitations & Technical Debt

### Current Limitations

1. **Offline Only:** No real code execution or AI integration
2. **Simulated Output:** Expected/actual outputs are hardcoded
3. **No Code Editor:** Read-only code display
4. **Fixed Bug Set:** No dynamic content loading
5. **High minSdk:** Currently API 36 (Android 15) - should be lowered to 24
6. **No Proper Migrations in Prod:** Uses fallbackToDestructiveMigration
7. **UI Not Yet Updated:** Phase 2 (UI redesign) not implemented
8. **No Tests:** Unit and instrumented tests need to be added

### Planned Improvements

- [ ] Lower minSdk to 24 for wider device compatibility
- [ ] Remove fallbackToDestructiveMigration before production
- [ ] Implement Phase 2 UI (bottom nav, learning paths, profile, etc.)
- [ ] Add onboarding flow (Phase 3)
- [ ] Implement daily notifications (Phase 3)
- [ ] Add Firebase authentication and cloud sync (Phase 4)
- [ ] Write unit tests for XP, achievements, streaks (Phase 5)
- [ ] Add instrumented tests for database (Phase 5)
- [ ] Create app screenshots for Play Store (Phase 6)

See `docs/IMPLEMENTATION_STATUS.md` for complete roadmap.

---

## 🧪 Testing

### Current Test Coverage

- ✅ Basic instrumented test (ExampleInstrumentedTest)
- ✅ Basic unit test (ExampleUnitTest)
- ⚠️ **Needs expansion** (Phase 5)

### Recommended Tests to Add

**Unit Tests:**
- XP calculation and level formula
- Achievement unlocking conditions
- Streak calculation logic
- Bug of the Day determinism

**Instrumented Tests:**
- Database migration (v2 → v3)
- Repository operations
- DAO queries
- Achievement manager integration

---

## 🚢 Deployment & Release

### Pre-Release Checklist

- [ ] **Phase 2:** Complete UI redesign (bottom nav, learning paths, profile)
- [ ] **Phase 3:** Add onboarding, settings, notifications
- [ ] **Phase 4:** Implement Firebase auth (optional)
- [ ] **Phase 5:** Write comprehensive tests
- [ ] **Phase 6:** Create privacy policy, screenshots, app description
- [ ] Lower minSdk to 24
- [ ] Remove fallbackToDestructiveMigration
- [ ] Add ProGuard rules for release build
- [ ] Test on multiple devices and Android versions
- [ ] Set up CI/CD (GitHub Actions)
- [ ] Create signed release APK/AAB
- [ ] Prepare Play Store listing

### Play Store Requirements

**Assets Needed:**
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG)
- Screenshots (at least 2, up to 8, 1080x1920 PNG)
- Privacy policy URL or inline text
- App description (short & full)
- Target audience and content rating

See `docs/IMPLEMENTATION_STATUS.md` for detailed Play Store preparation guide.

---

## 📜 Privacy & Data

- **All data stored locally by default** (Room database)
- **No tracking or analytics** (unless Firebase Analytics added)
- **No ads**
- **Optional cloud sync** (requires Firebase setup)
- **Guest mode available** (100% offline)

See `docs/PRIVACY_POLICY_DRAFT.md` for complete privacy policy.

---

## 📚 Resources

- [Android Jetpack Documentation](https://developer.android.com/jetpack)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [MVVM Architecture Guide](https://developer.android.com/jetpack/guide)
- [Material Design 3](https://m3.material.io/)
- [Firebase Documentation](https://firebase.google.com/docs)

---

## 🤝 Contributing

This is an educational project. Contributions are welcome!

**How to Contribute:**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

**Areas Needing Help:**
- Phase 2: UI redesign (bottom nav, learning paths, profile screens)
- Phase 3: Onboarding flow, settings, notifications
- Phase 5: Unit and instrumented tests
- Bug content: More realistic bugs and better explanations
- Accessibility: Screen reader support, high contrast mode
- Localization: Translate to other languages

---

## 📜 License

This project is created for educational purposes. Free to use, modify, and distribute for non-commercial educational purposes.

---

## 👤 Author

Created by **Hamdan Yasser** as a university project for Android Development course.

Enhanced to Mimo-style app with AI assistance for production-ready features.

---

## 🙏 Acknowledgments

- Android Jetpack libraries and documentation
- Material Design guidelines and components
- Room persistence library
- Firebase (Google Cloud)
- Stack Overflow community for bug inspiration
- Mimo app for gamification inspiration

---

## 📞 Support & Contact

For questions, bug reports, or feature requests:
- Open an issue on GitHub
- See `docs/IMPLEMENTATION_STATUS.md` for development status
- See `docs/ARCHITECTURE_DEBUGMASTER.md` for technical details

---

**Happy Debugging! 🐞✨**

*Transform bugs into learning opportunities, one XP at a time.*
