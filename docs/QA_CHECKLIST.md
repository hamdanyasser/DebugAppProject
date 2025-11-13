# QA Checklist - DebugMaster

Manual testing checklist for DebugMaster app functionality.

## Phase 3: Onboarding & Notifications

### Onboarding Flow
- [ ] Fresh install shows onboarding on first launch
- [ ] Can swipe through all 4 onboarding screens
- [ ] Page indicators (dots) update correctly as you swipe
- [ ] "Skip" button navigates to main app
- [ ] "Next" buttons advance to next screen
- [ ] "Get Started" on final screen navigates to main app
- [ ] Onboarding does NOT show on subsequent app launches
- [ ] All text and images are visible and properly formatted

### Daily Notifications
- [ ] Settings > Daily Reminders toggle is OFF by default
- [ ] Turning ON daily reminders requests notification permission (Android 13+)
- [ ] Granting permission shows success toast "Daily reminders enabled at 9:00 AM"
- [ ] Denying permission disables toggle and shows error toast
- [ ] Turning OFF daily reminders shows "Daily reminders disabled" toast
- [ ] Daily reminder notification appears at scheduled time (9:00 AM)
- [ ] Tapping notification navigates to Bug of the Day screen
- [ ] Notification has correct title and text
- [ ] Notification auto-dismisses when tapped

### Achievement Notifications
- [ ] Settings > Achievement Notifications toggle is ON by default
- [ ] Unlocking achievement shows notification when toggle is ON
- [ ] Achievement notification has correct title and description
- [ ] Tapping achievement notification opens the app
- [ ] Disabling toggle prevents achievement notifications
- [ ] Achievement is still unlocked even if notifications are off

### Hints Toggle
- [ ] Settings > Hints Enabled toggle is ON by default
- [ ] When hints are enabled, "Show Hint" button works in Bug Detail
- [ ] When hints are disabled, "Show Hint" shows "Hints Disabled" dialog
- [ ] Dialog offers option to "Enable Hints" which navigates to Settings
- [ ] Dialog "Keep Disabled" dismisses and maintains challenge mode
- [ ] Enabling hints shows "Hints enabled" toast
- [ ] Disabling hints shows "Hints disabled - challenge mode activated!" toast

## Phase 4: Firebase Auth & Sync

### Guest Mode (Default)
- [ ] Fresh install starts in Guest mode
- [ ] Profile screen shows "Account Status: Guest"
- [ ] Profile screen shows "Sign In" button
- [ ] Settings > Sync section explains that sync requires sign-in
- [ ] "Sync Now" in guest mode shows "Sign in required" toast
- [ ] All progress is saved locally in guest mode
- [ ] Level, XP, achievements work normally in guest mode

### Sign In Flow
- [ ] Tapping "Sign In" in Profile shows Firebase not configured message (expected without google-services.json)
- [ ] When Firebase IS configured:
  - [ ] "Sign In" opens Google Sign-In sheet
  - [ ] Selecting Google account completes sign-in
  - [ ] Profile updates to show user name/email
  - [ ] Button changes to "Sign Out"
  - [ ] Last sync time appears (if synced)

### Sign Out
- [ ] Tapping "Sign Out" signs user out
- [ ] Toast shows "Signed out. Local data preserved."
- [ ] Profile returns to "Guest" status
- [ ] Button changes to "Sign In"
- [ ] All local data remains intact after sign-out
- [ ] Can still solve bugs and earn XP while signed out

### Cloud Sync
- [ ] When signed in and Firebase configured:
  - [ ] "Sync Now" button triggers sync
  - [ ] Shows "Syncing progress..." toast
  - [ ] On success: "Sync complete! Your progress is backed up."
  - [ ] On error: Shows appropriate error message
  - [ ] Last sync time updates after successful sync
- [ ] Sync without sign-in shows appropriate error
- [ ] Sync without Firebase shows "Firebase not configured" message

### Account Management
- [ ] "Manage Account & Data" shows different messages for Guest vs Signed In
- [ ] Guest mode dialog explains local data storage
- [ ] Signed-in dialog shows account email
- [ ] Dialog explains cloud data deletion process (coming soon)
- [ ] Dialog differentiates between local and cloud data reset
- [ ] Dialog mentions account deletion requires contacting support

## Core Functionality

### Bug Browsing
- [ ] Learn tab shows list of all bugs
- [ ] Bugs show title, category, difficulty
- [ ] Completed bugs show green checkmark
- [ ] Can filter by difficulty (Easy, Medium, Hard)
- [ ] Can filter by category (Arrays, Loops, etc.)
- [ ] Can search bugs by title
- [ ] Tapping bug opens Bug Detail screen

### Bug Detail
- [ ] Bug detail shows full code with bug
- [ ] Shows description of what's wrong
- [ ] Shows expected vs actual output
- [ ] "Show Hint" button reveals hints one by one
- [ ] "Mark as Solved" marks bug as complete
- [ ] Completing bug awards XP
- [ ] XP amount shown in toast
- [ ] Can add personal notes
- [ ] Notes persist after closing screen
- [ ] Back button returns to bug list

### Bug of the Day
- [ ] Bug of Day tab shows today's featured bug
- [ ] Same bug appears for all users on same day
- [ ] Shows current streak and longest streak
- [ ] Completing Bug of Day increments streak
- [ ] Completing multiple bugs same day doesn't break streak
- [ ] Missing a day resets streak to 0
- [ ] Streak survives app restart

### Profile & Progress
- [ ] Profile shows current level
- [ ] Profile shows XP with progress bar
- [ ] Progress bar accurately reflects XP in current level
- [ ] Shows "X / 100 XP" text
- [ ] Shows total bugs solved
- [ ] Shows perfect fixes (bugs solved without hints)
- [ ] Shows current streak
- [ ] Level increases every 100 XP (Level 1 = 0-99 XP, Level 2 = 100-199 XP, etc.)

### Achievements
- [ ] Profile shows achievements grid (2 columns)
- [ ] Locked achievements appear grayed out with lock icon
- [ ] Unlocked achievements appear in full color
- [ ] Shows "X of Y unlocked" count
- [ ] Achievements unlock based on correct conditions:
  - [ ] First Fix: Solve 1 bug
  - [ ] Perfect Ten: Solve 10 bugs
  - [ ] No-Hint Hero: Solve 3 bugs without hints
  - [ ] Streak Machine: 7-day streak
  - [ ] XP Collector: Earn 500 XP
  - [ ] Level 5 Debugger: Reach level 5
  - [ ] Completionist: Solve all bugs
  - [ ] (Test other achievements as applicable)
- [ ] Unlocking achievement awards bonus XP
- [ ] Achievement notification appears (if enabled)

### Settings
- [ ] All toggles persist after app restart
- [ ] App version displays correctly
- [ ] Privacy Policy shows privacy explanation dialog
- [ ] Reset Progress shows confirmation dialog
- [ ] Confirming reset clears all progress
- [ ] After reset, app restarts with fresh data
- [ ] Reset does NOT re-show onboarding

## XP & Leveling System

### XP Awards
- [ ] Completing easy bug awards 10 XP
- [ ] Completing medium bug awards 15 XP
- [ ] Completing hard bug awards 25 XP
- [ ] Completing bug without hints awards +5 bonus XP
- [ ] Achievement unlocks award bonus XP
- [ ] XP awards shown in toast notifications
- [ ] Total XP increases correctly

### Level Calculation
- [ ] Level 1: 0-99 XP
- [ ] Level 2: 100-199 XP
- [ ] Level 3: 200-299 XP
- [ ] Level increases automatically when XP threshold crossed
- [ ] Progress bar resets at each level
- [ ] Level display updates in real-time

### Streak System
- [ ] Completing bug today when last completion was yesterday: streak +1
- [ ] Completing bug today when last completion was today: streak unchanged
- [ ] Not completing bug for 2+ days: streak resets to 0
- [ ] Longest streak tracks all-time maximum
- [ ] Current streak shows accurate active streak or 0 if broken

## Edge Cases & Error Handling

### Network Issues
- [ ] App works offline (no Firebase)
- [ ] Sync fails gracefully when offline
- [ ] Shows appropriate error messages
- [ ] Local data never lost due to network issues

### Data Persistence
- [ ] All progress persists after app restart
- [ ] Notes on bugs persist
- [ ] Achievements remain unlocked
- [ ] Streak continues correctly across app restarts
- [ ] Settings preferences persist

### Permissions
- [ ] App works without notification permission
- [ ] Can still use app if permission denied
- [ ] Clear messaging about permission requirements
- [ ] Can enable permissions later from system settings

### App Lifecycle
- [ ] App resumes correctly from background
- [ ] No data loss when app is killed
- [ ] Navigation state preserved during rotation
- [ ] Splash screen appears on cold start

## Performance

- [ ] App launches in < 2 seconds
- [ ] Navigation between screens is smooth
- [ ] No visible lag when scrolling bug list
- [ ] Achievement checks don't block UI
- [ ] Sync operations happen in background
- [ ] No ANR (Application Not Responding) errors
- [ ] No crashes during normal usage

## UI/UX

- [ ] All text is readable and properly sized
- [ ] Colors follow Mimo-inspired design
- [ ] Material Design components used throughout
- [ ] Proper spacing and padding
- [ ] Icons are clear and appropriate
- [ ] Animations are smooth (progress bars, navigation)
- [ ] Dark theme support (if implemented)
- [ ] Accessibility: content descriptions on images/buttons

## Notes for QA Testers

1. **Testing Daily Notifications**: To test without waiting 24 hours, temporarily modify `NotificationScheduler.java` to use a shorter interval (e.g., 1 minute instead of 24 hours).

2. **Testing Streaks**: To test streak logic without waiting days, temporarily modify `DateUtils.calculateStreak()` to use hours instead of days.

3. **Testing Firebase**: Requires adding `google-services.json` to `app/` directory and uncommenting Firebase code in relevant classes.

4. **Resetting for Fresh Test**: Use Settings > Reset Progress, or clear app data from system settings.

5. **Checking Logs**: Use `adb logcat | grep DebugMaster` to see app logs during testing.
