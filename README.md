# DebugMaster

**Tagline:** *Learn to debug by fixing real-world broken code*

An Android application designed for students learning Android Development. DebugMaster provides a comprehensive debugging practice environment where students solve real-world code bugs through guided exercises.

---

## 📱 Project Overview

**DebugMaster** is an educational Android app that teaches debugging skills through interactive exercises. Each bug represents a common programming mistake that students must identify, understand, and fix. The app provides:

- **15 curated debugging exercises** covering various difficulty levels
- **Progressive hint system** acting as an "AI mentor"
- **Simulated code execution** showing expected vs actual output
- **Progress tracking** with streaks and statistics
- **"Bug of the Day"** feature for daily practice

---

## 🎯 Features

### Core Functionality

1. **Bug of the Day**
   - Deterministic daily bug selection
   - Encourages daily practice
   - Displayed prominently on home screen

2. **Bug Library**
   - 15 debugging exercises covering:
     - Loops
     - Arrays
     - Object-Oriented Programming
     - Strings
     - Conditionals
     - Exceptions
     - Collections
     - Methods
   - Filter by difficulty (Easy, Medium, Hard)
   - Filter by category

3. **Interactive Debugging**
   - View broken code with description
   - Run code to see output comparison
   - Progressive 3-level hint system
   - Complete solution with explanation
   - Mark bugs as solved

4. **Progress Tracking**
   - Total bugs solved
   - Breakdown by difficulty level
   - Daily streak counter
   - Progress percentages
   - Reset progress option

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
├── local/             # Room Database, DAOs
├── repository/        # BugRepository (single source of truth)
└── seeding/           # DatabaseSeeder

model/                  # Domain Models
├── Bug.java           # Bug entity
├── Hint.java          # Hint entity
├── UserProgress.java  # Progress tracking entity
└── BugCategory.java   # Category enum

util/                   # Utilities
├── DateUtils.java     # Bug of the day logic, streak calculation
└── Constants.java     # App-wide constants
```

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | **Java** (100%) |
| UI | XML Layouts + Fragments |
| Navigation | Android Navigation Component |
| Database | **Room** (SQLite) |
| Architecture | **ViewModel + LiveData** |
| UI Components | Material Design 3 |
| View Binding | Enabled |
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

// RecyclerView
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// Gson for JSON parsing
implementation 'com.google.code.gson:gson:2.10.1'
```

---

## 📊 Database Schema

### Bugs Table
```sql
CREATE TABLE bugs (
    id INTEGER PRIMARY KEY,
    title TEXT,
    language TEXT,
    difficulty TEXT,
    category TEXT,
    description TEXT,
    brokenCode TEXT,
    expectedOutput TEXT,
    actualOutput TEXT,
    explanation TEXT,
    fixedCode TEXT,
    isCompleted INTEGER
);
```

### Hints Table
```sql
CREATE TABLE hints (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bugId INTEGER,
    level INTEGER,
    text TEXT
);
```

### User Progress Table
```sql
CREATE TABLE user_progress (
    id INTEGER PRIMARY KEY,
    totalSolved INTEGER,
    streakDays INTEGER,
    easySolved INTEGER,
    mediumSolved INTEGER,
    hardSolved INTEGER,
    lastSolvedTimestamp INTEGER,
    lastOpenedTimestamp INTEGER
);
```

---

## 🎨 User Interface

### Screens

1. **Splash Screen**
   - App logo and tagline
   - Database seeding on first launch
   - Auto-navigation to home

2. **Home Screen**
   - Bug of the Day card
   - Quick statistics (solved, total, streak)
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
   - Progressive hint system
   - Solution reveal
   - Mark as solved

5. **Progress Screen**
   - Overall progress with percentage
   - Streak display
   - Difficulty breakdown with progress bars
   - Reset progress option

---

## 💾 Data Flow

1. **App Launch:**
   ```
   SplashFragment → DatabaseSeeder → BugRepository → Room Database
   ```

2. **Viewing Bugs:**
   ```
   BugListFragment → BugListViewModel → BugRepository → BugDao → LiveData<List<Bug>>
   ```

3. **Solving a Bug:**
   ```
   BugDetailFragment → BugDetailViewModel → BugRepository →
   [Update Bug + UserProgress] → LiveData updates UI
   ```

4. **Bug of the Day:**
   ```
   HomeViewModel → DateUtils.getBugOfTheDayId() →
   (currentDayOfYear % totalBugs) → Display bug
   ```

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

- Android Studio Arctic Fox or later
- JDK 11
- Android SDK API 36
- Gradle 8.13

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/hamdanyasser/DebugAppProject.git
   cd DebugAppProject
   ```

2. Open in Android Studio:
   - File → Open → Select project directory

3. Sync Gradle:
   - Android Studio will automatically sync dependencies

4. Run the app:
   - Select an emulator or physical device
   - Click Run (Shift + F10)

### First Launch

On first launch, the app will:
1. Display splash screen
2. Seed database with 15 bugs and hints from `assets/bugs.json`
3. Create initial UserProgress record
4. Navigate to home screen

---

## 🎓 Educational Value

### What Students Learn

1. **Common Bug Patterns**
   - Off-by-one errors
   - Null pointer exceptions
   - String comparison pitfalls
   - Collection modification issues
   - Scope and shadowing

2. **Debugging Strategies**
   - Reading error messages
   - Analyzing expected vs actual output
   - Using hints progressively
   - Understanding root causes

3. **Android Development**
   - MVVM architecture
   - Room database
   - LiveData and ViewModels
   - Navigation Component
   - RecyclerView
   - Material Design

---

## 📝 Code Quality

### Best Practices Implemented

- ✅ **Clean Architecture:** Separation of concerns (UI, Data, Domain)
- ✅ **SOLID Principles:** Single responsibility, dependency inversion
- ✅ **LiveData:** Reactive, lifecycle-aware data handling
- ✅ **Repository Pattern:** Single source of truth
- ✅ **View Binding:** Type-safe view access
- ✅ **Material Design:** Modern, consistent UI
- ✅ **JavaDoc Comments:** Documented public methods
- ✅ **Naming Conventions:** Meaningful class and variable names

---

## 🔄 Extensibility

### Adding New Bugs

1. Edit `assets/bugs.json`
2. Add new bug object with all required fields
3. Add corresponding hints
4. Increment bug ID

### Adding New Categories

1. Update `BugCategory.java` enum
2. Update `Constants.java` with new category
3. Update filter spinner in `BugListFragment.java`

### Customizing Difficulty Levels

- Modify difficulty strings in `Constants.java`
- Update UI colors in `BugAdapter.java`

---

## 🐛 Known Limitations

1. **Offline Only:** No real code execution or AI integration
2. **Simulated Output:** Predefined expected/actual outputs
3. **No Code Editor:** Read-only code display
4. **Fixed Bug Set:** No dynamic content loading

---

## 📜 License

This project is created for educational purposes as part of an Android Development course.

---

## 👤 Author

Created as a university project for Android Development course.

---

## 🙏 Acknowledgments

- Android Jetpack libraries
- Material Design guidelines
- Stack Overflow community for bug inspiration

---

**Happy Debugging! 🐞**
