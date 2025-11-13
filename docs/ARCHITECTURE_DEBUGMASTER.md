# DebugMaster Architecture Documentation

**Internal development documentation - for developers only**

## Overview

DebugMaster is an Android debugging education app built with Java, following MVVM architecture with Room database for persistence. This document describes the current architecture state as of the Mimo-style transformation project.

---

## Project Structure

```
app/src/main/java/com/example/debugappproject/
├── MainActivity.java                    # Single activity container
├── ui/                                  # Presentation layer (Fragments + ViewModels)
│   ├── splash/
│   │   └── SplashFragment.java         # Initial splash screen with DB seeding
│   ├── home/
│   │   ├── HomeFragment.java           # Dashboard with Bug of the Day
│   │   └── HomeViewModel.java
│   ├── buglist/
│   │   ├── BugListFragment.java        # List of all bugs with filters
│   │   ├── BugListViewModel.java
│   │   └── BugAdapter.java             # RecyclerView adapter
│   ├── bugdetail/
│   │   ├── BugDetailFragment.java      # Bug detail with hints & solution
│   │   └── BugDetailViewModel.java
│   └── progress/
│       ├── ProgressFragment.java       # User stats and progress
│       └── ProgressViewModel.java
├── data/                                # Data layer
│   ├── local/
│   │   ├── DebugMasterDatabase.java   # Room database singleton
│   │   ├── BugDao.java                # Bug queries
│   │   ├── HintDao.java               # Hint queries
│   │   └── UserProgressDao.java       # Progress queries
│   ├── repository/
│   │   └── BugRepository.java         # Single source of truth, coordinates DAOs
│   └── seeding/
│       └── DatabaseSeeder.java        # Seeds DB from assets/bugs.json
├── model/                               # Domain models / Room entities
│   ├── Bug.java                       # Bug entity
│   ├── Hint.java                      # Hint entity
│   ├── UserProgress.java              # User progress entity
│   ├── Achievement.java               # Achievement model (computed, not stored)
│   ├── BugCategory.java               # Category enum
│   └── TestCase.java                  # Test case model
└── util/
    ├── DateUtils.java                 # Bug of the Day logic, streak calculation
    ├── Constants.java                 # App constants
    └── CodeComparator.java            # Code comparison utility
```

---

## Current Room Schema

### Database Version: 2

#### Table: `bugs`
```sql
CREATE TABLE bugs (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    language TEXT,              -- Programming language (e.g., "Java")
    difficulty TEXT,            -- "Easy", "Medium", "Hard"
    category TEXT,              -- "Loops", "Arrays", "OOP", etc.
    description TEXT,           -- What the program should do
    brokenCode TEXT,            -- Buggy code snippet
    expectedOutput TEXT,        -- Correct output
    actualOutput TEXT,          -- Buggy output / error message
    explanation TEXT,           -- Root cause explanation
    fixedCode TEXT,             -- Corrected code
    isCompleted INTEGER,        -- 0/1 boolean
    starterCode TEXT,           -- Optional starter code (defaults to brokenCode)
    userNotes TEXT,             -- User's personal notes
    testsJson TEXT              -- JSON string with test cases
);
```

**Fields added in v2:**
- `starterCode`: Allows customizing the initial code shown to users
- `userNotes`: Enables users to add personal notes per bug
- `testsJson`: Stores test cases as JSON for validation

#### Table: `hints`
```sql
CREATE TABLE hints (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bugId INTEGER,              -- Foreign key to bugs.id
    level INTEGER,              -- Progressive hint level (1-5, higher = more specific)
    text TEXT                   -- Hint content
);
```

**Relationship:** Each bug can have multiple hints with increasing specificity levels.

#### Table: `user_progress`
```sql
CREATE TABLE user_progress (
    id INTEGER PRIMARY KEY,                -- Always 1 (singleton)
    totalSolved INTEGER,                   -- Total bugs solved
    streakDays INTEGER,                    -- Current streak
    easySolved INTEGER,                    -- Easy bugs solved
    mediumSolved INTEGER,                  -- Medium bugs solved
    hardSolved INTEGER,                    -- Hard bugs solved
    lastSolvedTimestamp INTEGER,           -- Last solve time (millis)
    lastOpenedTimestamp INTEGER,           -- Last app open (millis)
    xp INTEGER,                            -- Total XP earned
    hintsUsed INTEGER,                     -- Global hints used counter
    bugsSolvedWithoutHints INTEGER         -- Bugs solved with zero hints
);
```

**Fields added in v2:**
- `xp`: Total experience points
- `hintsUsed`: Tracks how many hints user has viewed
- `bugsSolvedWithoutHints`: Achievement tracking

**Computed fields (not stored, calculated in `UserProgress.java`):**
- `getLevel()`: Returns `1 + (xp / 100)`
- `getXpToNextLevel()`: XP needed for next level
- `getXpProgressInLevel()`: Progress within current level (0-100)

**Migration Strategy:** Currently uses `fallbackToDestructiveMigration()` - database is recreated on schema changes. For production, proper migrations will be implemented.

---

## Main User Flows

### 1. App Launch & Database Seeding

```
User launches app
    ↓
MainActivity created
    ↓
SplashFragment displayed
    ↓
DatabaseSeeder checks bug count
    ↓
If count == 0:
    - Read assets/bugs.json
    - Parse bug & hint data
    - Insert into Room DB
    ↓
Navigate to HomeFragment
```

**Key file:** `DatabaseSeeder.java`
**Data source:** `app/src/main/assets/bugs.json` (15 curated bugs with hints)

### 2. Bug of the Day

```
HomeFragment loads
    ↓
HomeViewModel.loadBugOfTheDay()
    ↓
DateUtils.getBugOfTheDayId(totalBugCount)
    - Uses deterministic formula: ((year * 1000) + dayOfYear) % totalBugCount + 1
    - Same bug appears globally for everyone on the same date
    ↓
Display bug in prominent card
    ↓
User clicks → Navigate to BugDetailFragment
```

**Key files:**
- `DateUtils.java:19` - Bug of the Day calculation
- `HomeViewModel.java` - Loads today's bug

### 3. Viewing Bug List with Filters

```
User navigates to BugListFragment
    ↓
BugListViewModel.getAllBugs() → LiveData<List<Bug>>
    ↓
User applies filters:
    - Difficulty: All / Easy / Medium / Hard
    - Category: All / Loops / Arrays / OOP / Strings / etc.
    ↓
ViewModel queries filtered bugs from BugDao
    ↓
BugAdapter displays bugs in RecyclerView
    - Shows: title, difficulty, category, completion status
    ↓
User clicks bug → Navigate to BugDetailFragment(bugId)
```

**Key files:**
- `BugListFragment.java` - UI with filter spinners
- `BugListViewModel.java` - Manages filters & data
- `BugAdapter.java` - RecyclerView adapter with ViewHolder

### 4. Solving a Bug (Core Flow)

```
BugDetailFragment loads with bugId argument
    ↓
BugDetailViewModel.loadBug(bugId)
    ↓
Display:
    - Bug description
    - Broken code (monospace font)
    - "Run Code" button
    - Progressive hints (initially collapsed)
    - Solution (initially hidden)
    ↓
User clicks "Run Code":
    - Show expected vs actual output comparison
    ↓
User clicks "Show Hint [Level N]":
    - Reveal hint at level N
    - Increment global hintsUsed counter
    - Track if user used any hints for this bug
    ↓
User clicks "Show Solution":
    - Reveal fixedCode + explanation
    ↓
User clicks "Mark as Solved":
    - BugRepository.markBugAsCompletedWithXP(bugId, difficulty, solvedWithoutHints)
    - Calculate XP:
        * Easy: 10 XP
        * Medium: 20 XP
        * Hard: 30 XP
        * Bonus: +5 XP if solved without hints
    - Update UserProgress:
        * totalSolved++
        * {difficulty}Solved++
        * xp += xpReward
        * lastSolvedTimestamp = now
        * If no hints used: bugsSolvedWithoutHints++
    - Update streak (if consecutive day)
    - Show success feedback
```

**XP Reward Formula:**
- Easy: 10 XP
- Medium: 20 XP
- Hard: 30 XP
- No-hint bonus: +5 XP

**Key files:**
- `BugDetailFragment.java` - Bug detail UI
- `BugDetailViewModel.java` - Manages bug state
- `BugRepository.java:100` - `markBugAsCompletedWithXP()`

### 5. Progress Tracking

```
User navigates to ProgressFragment
    ↓
ProgressViewModel.getUserProgress() → LiveData<UserProgress>
    ↓
Display:
    - Total bugs solved / total bugs
    - Progress percentage
    - Current streak (days)
    - Level & XP progress bar
    - Breakdown by difficulty (Easy/Medium/Hard)
    - Stats: hints used, bugs solved without hints
    ↓
User can reset progress (dialog confirmation)
    ↓
BugRepository.resetProgress()
    - Reset all bugs to incomplete
    - Reset UserProgress to defaults
```

**Key files:**
- `ProgressFragment.java` - Stats UI
- `ProgressViewModel.java` - Progress data

### 6. Streak Calculation

```
Every time user solves a bug:
    ↓
DateUtils.calculateStreak(lastSolvedTimestamp, currentStreak)
    ↓
Logic:
    - If lastSolvedTimestamp == 0: streak = 0
    - If daysSince == 0 (same day): maintain current streak
    - If daysSince == 1 (next day): streak++
    - If daysSince > 1: streak = 0 (broken)
    ↓
Update UserProgress.streakDays
```

**Key file:** `DateUtils.java:43` - Streak calculation logic

---

## Architecture Patterns

### MVVM (Model-View-ViewModel)

**Model:**
- Room entities: `Bug`, `Hint`, `UserProgress`
- Repository: `BugRepository` (single source of truth)

**View:**
- Fragments: UI logic, user interaction
- XML layouts: View structure
- View Binding: Type-safe view access

**ViewModel:**
- Holds UI state
- Exposes LiveData to Fragments
- Survives configuration changes
- No Android framework dependencies (except AndroidViewModel)

### Repository Pattern

`BugRepository` acts as the single source of truth:
- Coordinates DAOs (BugDao, HintDao, UserProgressDao)
- Manages background threading (ExecutorService)
- Exposes clean API to ViewModels
- Handles complex multi-table operations (e.g., solving a bug updates both Bug and UserProgress)

### Navigation Architecture Component

Single Activity (`MainActivity`) with Fragment destinations:
- Navigation graph: `res/navigation/nav_graph.xml`
- Start destination: `SplashFragment`
- Safe Args for type-safe argument passing (bugId)
- AppBar integration with NavigationUI

---

## Data Seeding

**Source:** `app/src/main/assets/bugs.json`

**Structure:**
```json
{
  "bugs": [
    {
      "id": 1,
      "title": "Bug title",
      "difficulty": "Easy|Medium|Hard",
      "category": "Loops|Arrays|OOP|...",
      "description": "...",
      "brokenCode": "...",
      "expectedOutput": "...",
      "actualOutput": "...",
      "explanation": "...",
      "fixedCode": "...",
      "hints": [
        {"level": 1, "text": "..."},
        {"level": 2, "text": "..."},
        {"level": 3, "text": "..."}
      ]
    }
  ]
}
```

**Seeding Logic (DatabaseSeeder.java):**
1. Check if bugs exist in DB (getBugCountSync())
2. If count == 0:
   - Read `bugs.json` from assets
   - Parse JSON with Gson
   - Insert bugs via `BugRepository.insertBugs()`
   - Insert hints via `BugRepository.insertHints()`
   - Insert initial UserProgress

**Current Bug Set:** 15 bugs covering common Java debugging scenarios (off-by-one, NPE, string comparison, etc.)

---

## Current Limitations & Technical Debt

1. **No proper Room migrations:** Uses `fallbackToDestructiveMigration()` - destroys user data on schema changes
2. **No cloud sync:** All data is local only
3. **No authentication:** Single offline user
4. **Simulated code execution:** Expected/actual outputs are hardcoded
5. **No real-time code validation:** Can't run user's code
6. **No learning paths:** Flat bug list, no structured progression
7. **Minimal gamification:** Basic XP, no levels displayed prominently, no achievements system
8. **No notifications:** No daily reminders for Bug of the Day
9. **No onboarding:** Jumps straight to home after splash
10. **FAB unused:** Floating Action Button has placeholder behavior

---

## Technology Stack Summary

| Component | Technology |
|-----------|-----------|
| Language | Java 11 |
| Architecture | MVVM |
| Database | Room 2.6.1 |
| UI | Fragments + XML Layouts |
| View Binding | Enabled |
| Navigation | Navigation Component 2.6.0 |
| Async | LiveData + ExecutorService |
| JSON | Gson 2.10.1 |
| Design | Material Design 3 |
| Min SDK | 36 (Android 15) |
| Target SDK | 36 |

---

## Next Steps (Mimo-Style Transformation)

This architecture will be extended with:

### Phase 1: Data Model
- Add `LearningPath` entity with bug associations
- Add `Lesson` and `LessonQuestion` entities for micro-lessons
- Add `Achievement` and `UserAchievement` entities
- Implement proper Room migrations (v2 → v3)
- Create `AchievementManager` for unlocking logic

### Phase 2: UI/Navigation
- Redesign with bottom navigation (Learn, Bug of Day, Profile)
- Create `LearningPathsFragment` with modern cards
- Enhance `BugDetailFragment` with lesson → quiz → debug flow
- Create dedicated `BugOfTheDayFragment`
- Create `ProfileFragment` with level, XP, achievements
- Modern Material 3 styling (cards, rounded corners, consistent palette)

### Phase 3: Features
- Onboarding flow (ViewPager2, 3-4 screens)
- Settings screen (notifications, reminder time, accessibility)
- WorkManager for daily Bug of the Day notifications
- Deep linking from notifications

### Phase 4: Cloud Sync
- Firebase Auth (Google Sign-In + Guest mode)
- Firestore sync layer (`ProgressSyncManager`)
- Graceful degradation if google-services.json missing

### Phase 5: Quality
- Unit tests for XP calculation, achievement logic
- Instrumented tests for Room DAOs
- Performance optimization (ensure background threading)
- Crash-proofing (null checks, error states)

### Phase 6: Play Store Readiness
- Privacy policy document
- Updated README with build instructions
- App screenshots / promotional assets
- ProGuard rules for release builds
- Version bumps and release process docs

---

**Document Status:** Created for Mimo-style transformation project
**Last Updated:** 2025-11-13
**Database Version:** 2
**App Version:** 1.0 (pre-transformation)
