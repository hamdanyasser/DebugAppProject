## Design Goals
- Create a modern game dashboard feel with energetic but focused visuals.
- Keep MVVM + Room flows intact; no schema or business-logic changes.
- Add playful, lightweight animations for feedback and motivation without affecting performance.

## Visual Design System
- Colors: Update to a vibrant but academic palette.
  - Primary: Indigo → Purple gradient accents for headers/cards.
  - Secondary: Teal/Lime accents for success and interactive elements.
  - Difficulty tags: Keep existing `difficulty_*` colors; improve contrast.
- Typography: Use existing Material 3 text styles; refine sizes/weights.
  - HeadlineLarge for hero titles, HeadlineMedium for card headers.
  - Monospace for code blocks via `TextAppearance.DebugMaster.Code`.
- Spacing & Cards:
  - Consistent `padding_*` and `spacing_*` scale; larger spacing for hero sections.
  - Elevated cards with subtle gradients and strokes for hierarchy.
- Components:
  - Chips for categories/difficulty; outlined chips for filters.
  - Progress bars styled for XP; badges as emoji + label in grid.

## Motion & Micro-Animations
- Use `ViewPropertyAnimator` and `ValueAnimator` for lightweight effects (no new libs).
- Card entrance: scale(0.98→1.0) + alpha fade on first display.
- Success feedback: checkmark pulse + short confetti stream (emoji overlay using `FrameLayout` and `ValueAnimator`).
- Error feedback: gentle shake on incorrect (`translationX` oscillation with decaying amplitude).
- Level-up: overlay banner slide-down with XP bar animate-to-next-level.
- Streak milestone: fire emoji pulse + streak number count-up.

## Information Architecture
- Bottom nav remains: Learn, Bug of Day, Profile, Settings (`MainActivity.java`).
- StartDestination: Splash → Learn (paths) via existing navigation; Onboarding shown only on true first launch.
- Legacy Home/Bug List/Progress kept for compatibility but visually refreshed.

## Screen-by-Screen Changes
### Splash / Onboarding
- `fragment_splash.xml`: logo and tagline fade/slide; short 800ms sequence.
- First-run gate to `OnboardingActivity` (already present) with 2–3 concise slides.
- Slides copy: “Fix bugs, gain XP, level up your Java skills.”

### Home (Dashboard)
- File: `res/layout/fragment_home.xml`
- Convert to a game hub:
  - Header with avatar initials, current level, XP bar, and streak.
  - “Bug of the Day” prominent hero card (title, difficulty chip, reward XP, Start).
  - “Daily Quest” mini-card (solve any bug without hints; shows +5 XP bonus).
  - Quick shortcuts: Bug Library, Progress.
- Animations: entrance for cards, XP bar animate on data change.

### Bug of the Day
- Files: `res/layout/fragment_bug_of_day.xml`, `ui/bugofday/BugOfTheDayFragment.java`
- Big hero card with:
  - Bug icon, title, difficulty, category.
  - Code snippet preview (monospace block) and reward XP.
  - “Solve” interaction → navigates to Bug Detail.
- Feedback:
  - On success: checkmark pulse + brief confetti overlay.
  - On incorrect: shake animation; hint area nudges.
- Add smooth navigation between items via next/prev buttons (within fragment header).

### Bug Library
- Files: `res/layout/fragment_bug_list.xml`, `res/layout/item_bug.xml`, `ui/buglist/BugListFragment.java`, `ui/buglist/BugAdapter.java`
- UI upgrades:
  - Search bar at top; `ChipGroup` filters for difficulty and topic.
  - Cards show tags (topic icon), difficulty chip, small progress indicator.
  - Completed state: subtle overlay badge with check emoji.
- Performance: use `DiffUtil` in adapter for quick filter updates.

### Bug Detail
- Files: `res/layout/fragment_bug_detail.xml`, `ui/bugdetail/BugDetailFragment.java`
- Layout:
  - Hero header with title, difficulty, XP reward.
  - Code block style with line-number look (simple left stripe + monospace).
  - Hint steps: progressive reveal with subtle expand/collapse animation.
  - “Run Code” → shows expected vs actual; “Mark as Solved” → success micro-animations.
- Gamification hooks: call `AchievementManager.checkAndUnlockAchievements()` on success and show toast/snackbar with XP/achievement.

### Progress / Stats
- Files: `res/layout/fragment_progress.xml`, `res/layout/item_achievement.xml`, `ui/progress/ProgressFragment.java`
- Turn into dashboard:
  - XP section: current level, bar with “XP to next level”.
  - Solved per category: simple horizontal bars (standard views).
  - Streak calendar: 7×N grid using `RecyclerView` with day chips (filled/empty).
  - Badges grid: achievement icons + names with locked state dimmed.

### Settings / Profile
- Files: `res/layout/fragment_settings.xml`, `res/layout/fragment_profile.xml`, `ui/settings/SettingsFragment.java`, `ui/profile/ProfileFragment.java`
- Profile card: avatar initials, username, level, XP.
- Toggles: “Reduce motion” and “Sound & haptics” (sound toggles as placeholders; no new audio libs).
- Settings keep notification time and reset progress; polish layout and hierarchy.

## Implementation Outline
### Phase 1: Foundations
- Update `themes.xml` styles: card, button, chip refinements; ensure dark-mode consistency.
- Update `colors.xml` and `dimens.xml` to support gradients/spacing.
- Add reusable drawables (rounded backgrounds, outlines, gradient overlays).

### Phase 2: Splash + Home
- Animate splash.
- Redesign `fragment_home.xml` into hub; bind data in `HomeFragment.java` to animate XP bar and streak.

### Phase 3: Bug of Day + Library
- Redesign `fragment_bug_of_day.xml`, add simple next/prev navigation.
- Upgrade library list UI; implement search/filter logic and adapter `DiffUtil`.

### Phase 4: Bug Detail Microinteractions
- Enhance code block styling and hint animations.
- Add success/error feedback on solve/check actions.

### Phase 5: Progress + Profile + Settings
- Build stats dashboard views and achievements grid.
- Profile card, toggles, and layout polish in settings.

### Phase 6: Polish & Performance
- Ensure animations are gated behind a “Reduce motion” toggle.
- Defer heavy animations; keep 60fps target.
- Accessibility: content descriptions on interactive elements.

## Non-Breaking Constraints
- No Room schema changes.
- Keep existing repositories, DAOs, and ViewModels.
- Use only Java + XML, Material components, and Android animations.

## Files to Touch (Primary)
- `res/values/themes.xml`, `res/values/colors.xml`, `res/values/dimens.xml`
- `res/drawable/*` (gradients, rounded backgrounds)
- `res/layout/fragment_splash.xml`, `fragment_home.xml`, `fragment_bug_of_day.xml`, `fragment_bug_list.xml`, `item_bug.xml`, `fragment_bug_detail.xml`, `fragment_progress.xml`, `item_achievement.xml`, `fragment_profile.xml`, `fragment_settings.xml`
- `ui/*/*.java` to wire animations and data-binding where needed (no logic changes)

## Verification Plan
- Build: `./gradlew assembleDebug` and fix any compile issues.
- Manual flows: Splash → Learn → Bug of Day → Bug Detail → Solve; Library search/filter; Progress dashboard; Settings toggles.
- Performance check: animation jank on mid-range emulator; gate via “Reduce motion”.

## README Update
- Add a short “Game-Style UI & Gamification” section describing the new design language and how to navigate the updated screens.

## Deliverables
- Updated layouts, styles, themes, and Java wiring for animations.
- Final summary including visual style, changed screens, and run/test instructions.