package com.example.debugappproject.util;

import com.example.debugappproject.data.local.AchievementDao;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Manager class for checking and unlocking achievements.
 * Call checkAchievements() after any significant user action (solving a bug, etc.)
 */
public class AchievementManager {

    private final AchievementDao achievementDao;
    private final UserProgressDao userProgressDao;
    private final BugDao bugDao;
    private final ExecutorService executorService;

    // Achievement IDs (constants)
    public static final String FIRST_FIX = "first_fix";
    public static final String NO_HINT_HERO = "no_hint_hero";
    public static final String ARRAY_ASSASSIN = "array_assassin";
    public static final String LOOP_MASTER = "loop_master";
    public static final String STREAK_MACHINE = "streak_machine";
    public static final String PERFECT_TEN = "perfect_ten";
    public static final String SPEED_DEMON = "speed_demon";
    public static final String COMPLETIONIST = "completionist";
    public static final String XP_COLLECTOR = "xp_collector";
    public static final String LEVEL_5 = "level_5";
    public static final String HARD_MODE = "hard_mode";
    public static final String NO_HINTS_5 = "no_hints_5";
    public static final String CATEGORY_MASTER = "category_master";
    public static final String STREAK_7 = "streak_7";
    public static final String STREAK_30 = "streak_30";

    public AchievementManager(AchievementDao achievementDao, UserProgressDao userProgressDao,
                             BugDao bugDao, ExecutorService executorService) {
        this.achievementDao = achievementDao;
        this.userProgressDao = userProgressDao;
        this.bugDao = bugDao;
        this.executorService = executorService;
    }

    /**
     * Check and unlock achievements based on current progress.
     * Returns list of newly unlocked achievement IDs.
     */
    public void checkAndUnlockAchievements(OnAchievementsUnlockedListener listener) {
        executorService.execute(() -> {
            List<String> newlyUnlocked = new ArrayList<>();
            UserProgress progress = userProgressDao.getUserProgressSync();
            if (progress == null) return;

            // Check each achievement condition
            checkFirstFix(progress, newlyUnlocked);
            checkNoHintHero(progress, newlyUnlocked);
            checkArrayAssassin(newlyUnlocked);
            checkLoopMaster(newlyUnlocked);
            checkStreakMachine(progress, newlyUnlocked);
            checkPerfectTen(progress, newlyUnlocked);
            checkCompletionist(progress, newlyUnlocked);
            checkXpCollector(progress, newlyUnlocked);
            checkLevel5(progress, newlyUnlocked);
            checkHardMode(progress, newlyUnlocked);
            checkNoHints5(progress, newlyUnlocked);
            checkStreak7(progress, newlyUnlocked);
            checkStreak30(progress, newlyUnlocked);

            if (listener != null && !newlyUnlocked.isEmpty()) {
                listener.onAchievementsUnlocked(newlyUnlocked);
            }
        });
    }

    private void checkFirstFix(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getTotalSolved() >= 1 && !isUnlocked(FIRST_FIX)) {
            unlockAchievement(FIRST_FIX);
            newlyUnlocked.add(FIRST_FIX);
        }
    }

    private void checkNoHintHero(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getBugsSolvedWithoutHints() >= 3 && !isUnlocked(NO_HINT_HERO)) {
            unlockAchievement(NO_HINT_HERO);
            newlyUnlocked.add(NO_HINT_HERO);
        }
    }

    private void checkArrayAssassin(List<String> newlyUnlocked) {
        if (!isUnlocked(ARRAY_ASSASSIN)) {
            int arrayBugsSolved = bugDao.getCompletedBugsByCategory("Arrays");
            int totalArrayBugs = bugDao.getBugCountByCategory("Arrays");
            if (arrayBugsSolved > 0 && arrayBugsSolved == totalArrayBugs) {
                unlockAchievement(ARRAY_ASSASSIN);
                newlyUnlocked.add(ARRAY_ASSASSIN);
            }
        }
    }

    private void checkLoopMaster(List<String> newlyUnlocked) {
        if (!isUnlocked(LOOP_MASTER)) {
            int loopBugsSolved = bugDao.getCompletedBugsByCategory("Loops");
            int totalLoopBugs = bugDao.getBugCountByCategory("Loops");
            if (loopBugsSolved > 0 && loopBugsSolved == totalLoopBugs) {
                unlockAchievement(LOOP_MASTER);
                newlyUnlocked.add(LOOP_MASTER);
            }
        }
    }

    private void checkStreakMachine(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getLongestStreakDays() >= 7 && !isUnlocked(STREAK_MACHINE)) {
            unlockAchievement(STREAK_MACHINE);
            newlyUnlocked.add(STREAK_MACHINE);
        }
    }

    private void checkPerfectTen(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getTotalSolved() >= 10 && !isUnlocked(PERFECT_TEN)) {
            unlockAchievement(PERFECT_TEN);
            newlyUnlocked.add(PERFECT_TEN);
        }
    }

    private void checkCompletionist(UserProgress progress, List<String> newlyUnlocked) {
        if (!isUnlocked(COMPLETIONIST)) {
            int totalBugs = bugDao.getBugCount();
            if (progress.getTotalSolved() >= totalBugs && totalBugs > 0) {
                unlockAchievement(COMPLETIONIST);
                newlyUnlocked.add(COMPLETIONIST);
            }
        }
    }

    private void checkXpCollector(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getXp() >= 500 && !isUnlocked(XP_COLLECTOR)) {
            unlockAchievement(XP_COLLECTOR);
            newlyUnlocked.add(XP_COLLECTOR);
        }
    }

    private void checkLevel5(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getLevel() >= 5 && !isUnlocked(LEVEL_5)) {
            unlockAchievement(LEVEL_5);
            newlyUnlocked.add(LEVEL_5);
        }
    }

    private void checkHardMode(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getHardSolved() >= 5 && !isUnlocked(HARD_MODE)) {
            unlockAchievement(HARD_MODE);
            newlyUnlocked.add(HARD_MODE);
        }
    }

    private void checkNoHints5(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getBugsSolvedWithoutHints() >= 5 && !isUnlocked(NO_HINTS_5)) {
            unlockAchievement(NO_HINTS_5);
            newlyUnlocked.add(NO_HINTS_5);
        }
    }

    private void checkStreak7(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getStreakDays() >= 7 && !isUnlocked(STREAK_7)) {
            unlockAchievement(STREAK_7);
            newlyUnlocked.add(STREAK_7);
        }
    }

    private void checkStreak30(UserProgress progress, List<String> newlyUnlocked) {
        if (progress.getLongestStreakDays() >= 30 && !isUnlocked(STREAK_30)) {
            unlockAchievement(STREAK_30);
            newlyUnlocked.add(STREAK_30);
        }
    }

    private boolean isUnlocked(String achievementId) {
        return achievementDao.getUserAchievementSync(achievementId) != null;
    }

    private void unlockAchievement(String achievementId) {
        UserAchievement userAchievement = new UserAchievement(
            achievementId,
            System.currentTimeMillis(),
            false
        );
        achievementDao.insertUserAchievement(userAchievement);

        // Award XP for achievement
        AchievementDefinition def = achievementDao.getAchievementDefinitionById(achievementId).getValue();
        if (def != null && def.getXpReward() > 0) {
            userProgressDao.addXp(def.getXpReward());
        }
    }

    /**
     * Get predefined achievement definitions for seeding.
     */
    public static List<AchievementDefinition> getDefaultAchievements() {
        List<AchievementDefinition> achievements = new ArrayList<>();

        achievements.add(new AchievementDefinition(
            FIRST_FIX, "First Fix", "Solve your first bug", "🎉",
            10, "MILESTONE", 1
        ));

        achievements.add(new AchievementDefinition(
            NO_HINT_HERO, "No-Hint Hero", "Solve 3 bugs without using hints", "🦸",
            25, "SKILL", 2
        ));

        achievements.add(new AchievementDefinition(
            ARRAY_ASSASSIN, "Array Assassin", "Complete all array-related bugs", "🗡️",
            50, "CATEGORY", 3
        ));

        achievements.add(new AchievementDefinition(
            LOOP_MASTER, "Loop Master", "Complete all loop-related bugs", "🔄",
            50, "CATEGORY", 4
        ));

        achievements.add(new AchievementDefinition(
            STREAK_MACHINE, "Streak Machine", "Reach a 7-day streak", "🔥",
            30, "STREAK", 5
        ));

        achievements.add(new AchievementDefinition(
            PERFECT_TEN, "Perfect Ten", "Solve 10 bugs", "💯",
            30, "MILESTONE", 6
        ));

        achievements.add(new AchievementDefinition(
            COMPLETIONIST, "Completionist", "Solve all bugs", "👑",
            100, "MILESTONE", 7
        ));

        achievements.add(new AchievementDefinition(
            XP_COLLECTOR, "XP Collector", "Earn 500 XP", "⭐",
            20, "MILESTONE", 8
        ));

        achievements.add(new AchievementDefinition(
            LEVEL_5, "Level 5 Debugger", "Reach Level 5", "📈",
            50, "MILESTONE", 9
        ));

        achievements.add(new AchievementDefinition(
            HARD_MODE, "Hard Mode", "Solve 5 hard bugs", "💪",
            40, "SKILL", 10
        ));

        achievements.add(new AchievementDefinition(
            NO_HINTS_5, "Hint-Free Champion", "Solve 5 bugs without hints", "🏆",
            35, "SKILL", 11
        ));

        achievements.add(new AchievementDefinition(
            STREAK_7, "Week Warrior", "Maintain a 7-day streak", "📅",
            25, "STREAK", 12
        ));

        achievements.add(new AchievementDefinition(
            STREAK_30, "Month Master", "Achieve a 30-day streak", "🎯",
            75, "STREAK", 13
        ));

        return achievements;
    }

    /**
     * Callback interface for achievement unlocks.
     */
    public interface OnAchievementsUnlockedListener {
        void onAchievementsUnlocked(List<String> achievementIds);
    }
}
