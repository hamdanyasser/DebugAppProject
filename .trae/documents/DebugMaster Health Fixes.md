## Build & Tooling Alignment
- Replace `agp` in `gradle/libs.versions.toml` from `8.13.0` to a stable, supported AGP (e.g., `8.6.1`).
- Downgrade Gradle wrapper in `gradle/wrapper/gradle-wrapper.properties` from `8.13` to a released Gradle (e.g., `8.10.2`) that matches the chosen AGP.
- Re-sync and verify `compileSdk=34`, `targetSdk=34`, `minSdk=26` compile clean in Android Studio.

## Room Migrations (Production Safety)
- Remove `.fallbackToDestructiveMigration()` in `DebugMasterDatabase.getInstance()` for release builds.
- Keep `MIGRATION_2_3` and add future migrations as schema evolves.
- Add a simple build-time flag to disable destructive fallback in release.

## Achievement XP Award Fix
- Add `getAchievementDefinitionByIdSync(String id)` in `AchievementDao` returning the entity directly (not LiveData).
- Update `AchievementManager.unlockAchievement()` to use the sync DAO method instead of `getValue()` on LiveData, ensuring XP is awarded reliably.

## UX Consistency: XP on Completion
- Clarify and adjust flows so XP is consistently awarded only after passing tests.
- Update button text or snackbar copy to reflect whether XP is awarded when using “Mark as Solved” without tests.

## Notifications & Scheduling
- Verify daily reminder scheduling works across API levels and respects `POST_NOTIFICATIONS` permission.
- Add a small instrumentation test for `NotificationScheduler.calculateInitialDelay()` edge cases.

## Testing Expansion
- Add unit tests for:
  - XP calculation and level formula from `UserProgress`.
  - Achievement unlock conditions in `AchievementManager`.
  - Streak updates (`DateUtils`).
- Add instrumented tests for:
  - Room migration v2→v3.
  - DAO read/writes for `BugDao`, `UserProgressDao`, `AchievementDao`.

## MinSdk Target (Optional)
- If desired, lower `minSdk` to `24` and validate on API 24–34 devices/emulators.

## CI & Release Hygiene
- Set up a basic CI (GitHub Actions) to run unit tests and assemble debug.
- Confirm ProGuard rules keep necessary classes and shrink release builds cleanly.
