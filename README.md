# 🐛 DebugMaster - Production-Ready Android Learning Platform

<div align="center">

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java%2011-orange.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-purple.svg)
![DI](https://img.shields.io/badge/DI-Hilt-red.svg)
![Test Coverage](https://img.shields.io/badge/Test%20Coverage-70%25-brightgreen.svg)

**An innovative mobile learning platform for mastering Java debugging through interactive code execution**

[Features](#-key-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [Testing](#-testing)

</div>

---

## 📖 Overview

DebugMaster is a portfolio-worthy, production-ready Android application that revolutionizes programming education by allowing users to **actually compile and execute Java code** on their mobile devices. Unlike traditional learning apps, DebugMaster provides real-time feedback with actual compilation errors, runtime exceptions, and output validation.

---

## ✨ Key Features

### 🚀 Interactive Code Execution (Killer Feature)
- **Real Java Compiler**: Uses Janino to compile and execute user code at runtime
- **Compilation Errors**: Shows actual errors with line numbers and helpful hints
- **Runtime Errors**: Catches exceptions (NPE, ArrayIndexOutOfBounds, etc.) with formatted stack traces
- **Output Comparison**: Compares actual output vs. expected output
- **Timeout Protection**: 5-second timeout prevents infinite loops
- **Execution Time**: Displays performance metrics for each run

### 🎨 Exceptional UI/UX
- **Confetti Celebrations**: 200-particle animation on bug completion
- **Spring Animations**: Physics-based animations for all card interactions
- **Animated XP Bar**: Smooth progress animation with easing functions
- **Level-Up Celebrations**: Multi-layer animations with rotation and scaling
- **Button Micro-interactions**: Press animations for every button
- **Card Reveal Animations**: Fade-in with scale for hints, solutions, and results
- **Shake Animations**: Error states draw attention with shake effect

### 🏗️ Production-Ready Architecture
- **MVVM Pattern**: Clear separation between UI, logic, and data
- **Hilt Dependency Injection**: Professional DI setup with modules
- **Repository Pattern**: Single source of truth for data operations
- **Room Database v3**: Proper migrations, relationships, and indexes
- **Offline-First Design**: 100% functional without internet
- **LiveData + ViewModels**: Reactive data flow with lifecycle awareness

### 🎮 Gamification System
- **XP & Leveling**: Earn XP for solving bugs (Easy: 10, Medium: 20, Hard: 30)
- **Bonus XP**: +5 XP for solving without hints
- **Streak System**: Daily streak tracking with longest streak record
- **15 Achievements**: Unlock achievements for milestones
- **Perfect Fixes**: Track bugs solved without hints
- **Progress Analytics**: Detailed stats by difficulty and category

### 📚 Learning Features
- **20+ Debugging Challenges**: Bugs across categories (Loops, Arrays, OOP, etc.)
- **4 Learning Paths**: Structured progression (Basics → Advanced)
- **Progressive Hints**: 3-level hint system (subtle → explicit)
- **Explanations**: Detailed explanations with fixed code
- **User Notes**: Save observations and learnings
- **Bug of the Day**: Daily challenge with notifications

---

## 🏛️ Architecture

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Fragments   │  │  Activities  │  │   Adapters   │      │
│  │ @AndroidEP   │  │ @AndroidEP   │  │ RecyclerView │      │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘      │
│         │                  │                                 │
│         └──────────────────┴─── ViewModels (@HiltViewModel) │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴─────────────────────────────┐
│                     Business Logic Layer                    │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │  BugRepository   │  │ CodeExecution    │               │
│  │  (Singleton)     │  │     Engine       │               │
│  └────────┬─────────┘  └──────────────────┘               │
└───────────┴─────────────────────────────────────────────────┘
            │
┌───────────┴─────────────────────────────────────────────────┐
│                     Data Layer                               │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  Room Database   │  │      DAOs        │                │
│  │   (Singleton)    │  │  Bug, Hint, XP   │                │
│  └──────────────────┘  └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Java 11
- **Min SDK**: 26 (Android 8.0 - covers 95%+ devices)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle Kotlin DSL 8.13.0

### Architecture & DI
- **Pattern**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt 2.48.1
- **Lifecycle**: AndroidX Lifecycle 2.7.0
- **Navigation**: Navigation Component 2.6.0

### Database & Data
- **Local Database**: Room 2.6.1
- **Reactive Data**: LiveData + ViewModel
- **JSON Parsing**: Gson 2.10.1

### UI & Animations
- **Design System**: Material Design 3 (1.13.0)
- **Animations**: Lottie 6.1.0 + Custom AnimatorSet
- **Shimmer Effects**: Facebook Shimmer 0.5.0
- **Layout**: ConstraintLayout 2.2.1

### Code Execution
- **Java Compiler**: Janino 3.1.10
- **Thread Management**: ExecutorService
- **Timeout Handling**: Future with TimeoutException

### Testing
- **Unit Testing**: JUnit 4.13.2
- **Mocking**: Mockito 5.7.0
- **UI Testing**: Espresso 3.7.0
- **Memory Leaks**: LeakCanary 2.12

---

## 🚀 Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android SDK 34

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/hamdanyasser/DebugAppProject.git
   cd DebugAppProject
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync

3. **Build the project**
   ```bash
   ./gradlew clean build
   ```

4. **Run on device/emulator**
   - Connect Android device OR start an emulator
   - Click "Run" in Android Studio
   - App will install and launch

---

## 🧪 Testing

### Running Tests

**Unit Tests**
```bash
./gradlew test
./gradlew test --tests CodeExecutionEngineTest
```

**Instrumented Tests**
```bash
./gradlew connectedAndroidTest
```

### Test Coverage

| Component | Coverage | Tests |
|-----------|----------|-------|
| CodeExecutionEngine | 85% | 22 |
| DateUtils | 90% | 13 |
| ProfileViewModel | 80% | 15 |
| **Overall** | **70%** | **58** |

---

## 📊 Performance

- **APK Size**: ~8 MB (ProGuard enabled)
- **Startup Time**: < 1 second
- **Animation Frame Rate**: 60 FPS
- **Memory Usage**: ~50 MB average

---

## 👨‍💻 Author

**Hamdan Yasser**
- GitHub: [@hamdanyasser](https://github.com/hamdanyasser)

---

<div align="center">

**Built with ❤️ for learning and portfolio purposes**

⭐ Star this repo if you find it helpful!

</div>
