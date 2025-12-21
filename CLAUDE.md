# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project (Windows)
./gradlew.bat assembleDebug

# Build release APK
./gradlew.bat assembleRelease

# Run unit tests
./gradlew.bat test

# Run a specific test class
./gradlew.bat test --tests "com.example.debugappproject.util.DateUtilsTest"

# Clean build
./gradlew.bat clean assembleDebug

# Lint check
./gradlew.bat lint
```

## Architecture Overview

This is an Android app using **MVVM with Repository pattern** and **Hilt for dependency injection**.

### Core Data Flow
```
UI (Fragments) → ViewModels → BugRepository → Room DAOs → SQLite Database
```

### Key Components

**DebugMasterApplication** - Entry point that initializes theme, seeds database on first run, and sets up achievement tracking.

**BugRepository** (`data/repository/`) - Single source of truth for all data operations. Provides access to all DAOs and handles background threading via ExecutorService.

**DebugMasterDatabase** (`data/local/`) - Room database (version 10) with entities: Bug, Hint, UserProgress, LearningPath, BugInPath, Lesson, LessonQuestion, AchievementDefinition, UserAchievement, MentalProfile. Contains migration logic for schema evolution.

**DatabaseSeeder** (`data/seeding/`) - Populates initial data from `app/src/main/assets/bugs.json` on first launch. Seeds bugs, hints, learning paths, and achievements.

### Navigation
Uses Jetpack Navigation with a single `nav_graph.xml`. Main destinations:
- Bottom nav: Home, Learn (LearningPaths), Leaderboard, Profile
- Features: BugDetail, PathDetail, BattleArena, MentorChat, Debugger, GameModes

### Dependency Injection
Hilt modules in `di/` package. `RepositoryModule` provides singleton BugRepository.

### Feature Packages
- `ai/` - AI mentor, code reviewer, certificate generation
- `auth/` - Firebase authentication (AuthManager, GoogleAuthManager)
- `billing/` - Google Play Billing integration
- `game/` - Game mechanics (GameManager, RankedBattleSystem, NearMissEngine)
- `multiplayer/` - Real-time battles via Firebase Realtime Database
- `sync/` - Progress sync (Firebase vs local-only strategies)
- `execution/` - Janino-based code execution engine for running user code

## Bug Data Format

Bugs are stored in `app/src/main/assets/bugs.json` with this structure:
```json
{
  "id": 1,
  "title": "Bug Title",
  "language": "Java",
  "difficulty": "Easy|Medium|Hard|Expert",
  "category": "Fundamentals|Loops|Arrays|Strings|etc",
  "brokenCode": "code with bug",
  "fixedCode": "corrected code",
  "hint": "helpful hint",
  "explanation": "why the bug occurs"
}
```

## XP and Rewards System

XP rewards by difficulty: Easy=10, Medium=25, Hard=50, Expert=100. Solving without hints doubles XP. Gems earned: Easy=5, Medium=10, Hard=20, Expert=40 (1.5x without hints).

## Testing

Unit tests are in `app/src/test/`. Key test files:
- `CodeExecutionEngineTest` - Tests code execution sandbox
- `AchievementManagerXpTest` - Tests XP/achievement calculations
- `UserProgressLevelTest` - Tests leveling system

Instrumented tests in `app/src/androidTest/`.

## Database Migrations

When modifying entity schemas, add a new migration in `DebugMasterDatabase`. Current version is 10. The app uses `fallbackToDestructiveMigration()` for dev builds but proper migrations should be added for production.

## Firebase Configuration

The app uses Firebase for:
- Authentication (Firebase Auth)
- Cloud sync (Firestore)
- Multiplayer battles (Realtime Database)

Config file location: `app/google-services.json` (not in repo, must be added for Firebase features).
