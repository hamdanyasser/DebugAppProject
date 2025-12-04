package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.debugappproject.data.local.AchievementDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           ACHIEVEMENT MANAGER - Global Achievement Tracking System           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Handles all achievement unlocking, progress tracking, and rewards.
 * Categories:
 * - MILESTONE: Progress-based (bugs solved, XP earned, paths completed)
 * - STREAK: Daily activity streaks
 * - SKILL: Specific accomplishments (no hints, speed runs, perfect scores)
 * - BATTLE: PvP victories and rankings
 * - CHALLENGE: Special events and challenges
 * - MASTERY: Expert-level achievements
 */
public class AchievementManager {
    
    private static final String TAG = "AchievementManager";
    private static final String PREFS_NAME = "achievement_prefs";
    
    private static AchievementManager instance;
    private final Context context;
    private final AchievementDao achievementDao;
    private final UserProgressDao userProgressDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor;
    
    // Listeners
    private OnAchievementUnlockedListener listener;
    
    public interface OnAchievementUnlockedListener {
        void onAchievementUnlocked(AchievementDefinition achievement);
    }
    
    private AchievementManager(Context context) {
        this.context = context.getApplicationContext();
        DebugMasterDatabase db = DebugMasterDatabase.getInstance(context);
        this.achievementDao = db.achievementDao();
        this.userProgressDao = db.userProgressDao();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized AchievementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AchievementManager(context);
        }
        return instance;
    }
    
    public void setOnAchievementUnlockedListener(OnAchievementUnlockedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Check all achievements and unlock any that have been earned.
     * Call this after any progress-related action.
     */
    public void checkAllAchievements() {
        executor.execute(() -> {
            try {
                UserProgress progress = userProgressDao.getUserProgressSync();
                if (progress == null) return;
                
                int bugsSolved = progress.getTotalSolved();
                int totalXp = progress.getTotalXp();
                int streakDays = progress.getStreakDays();
                int longestStreak = progress.getLongestStreakDays();
                int hintsUsed = progress.getHintsUsed();
                int bugsSolvedNoHints = progress.getBugsSolvedWithoutHints();
                
                // Battle stats
                int battleWins = prefs.getInt("battle_wins", 0);
                int battleStreak = prefs.getInt("battle_streak", 0);
                int trophies = prefs.getInt("trophies", 0);
                
                // MILESTONE ACHIEVEMENTS - Bugs Solved
                checkAndUnlock("first_fix", bugsSolved >= 1);
                checkAndUnlock("bug_squasher_10", bugsSolved >= 10);
                checkAndUnlock("bug_hunter_25", bugsSolved >= 25);
                checkAndUnlock("bug_slayer_50", bugsSolved >= 50);
                checkAndUnlock("bug_master_100", bugsSolved >= 100);
                checkAndUnlock("bug_legend_250", bugsSolved >= 250);
                checkAndUnlock("bug_god_500", bugsSolved >= 500);
                checkAndUnlock("bug_immortal_1000", bugsSolved >= 1000);
                
                // MILESTONE ACHIEVEMENTS - XP
                checkAndUnlock("xp_100", totalXp >= 100);
                checkAndUnlock("xp_500", totalXp >= 500);
                checkAndUnlock("xp_1000", totalXp >= 1000);
                checkAndUnlock("xp_5000", totalXp >= 5000);
                checkAndUnlock("xp_10000", totalXp >= 10000);
                checkAndUnlock("xp_50000", totalXp >= 50000);
                checkAndUnlock("xp_100000", totalXp >= 100000);
                
                // STREAK ACHIEVEMENTS
                checkAndUnlock("streak_3", streakDays >= 3);
                checkAndUnlock("streak_7", streakDays >= 7);
                checkAndUnlock("streak_14", streakDays >= 14);
                checkAndUnlock("streak_30", streakDays >= 30);
                checkAndUnlock("streak_60", streakDays >= 60);
                checkAndUnlock("streak_100", streakDays >= 100);
                checkAndUnlock("streak_365", streakDays >= 365);
                
                // LONGEST STREAK
                checkAndUnlock("longest_streak_30", longestStreak >= 30);
                checkAndUnlock("longest_streak_100", longestStreak >= 100);
                
                // SKILL ACHIEVEMENTS - No Hints
                checkAndUnlock("no_hint_1", bugsSolvedNoHints >= 1);
                checkAndUnlock("no_hint_10", bugsSolvedNoHints >= 10);
                checkAndUnlock("no_hint_50", bugsSolvedNoHints >= 50);
                checkAndUnlock("no_hint_100", bugsSolvedNoHints >= 100);
                
                // BATTLE ACHIEVEMENTS
                checkAndUnlock("first_battle_win", battleWins >= 1);
                checkAndUnlock("battle_wins_10", battleWins >= 10);
                checkAndUnlock("battle_wins_50", battleWins >= 50);
                checkAndUnlock("battle_wins_100", battleWins >= 100);
                checkAndUnlock("battle_wins_500", battleWins >= 500);
                
                // Battle Streaks
                checkAndUnlock("battle_streak_3", battleStreak >= 3);
                checkAndUnlock("battle_streak_5", battleStreak >= 5);
                checkAndUnlock("battle_streak_10", battleStreak >= 10);
                checkAndUnlock("battle_streak_20", battleStreak >= 20);
                
                // Trophy Achievements
                checkAndUnlock("trophy_100", trophies >= 100);
                checkAndUnlock("trophy_500", trophies >= 500);
                checkAndUnlock("trophy_1000", trophies >= 1000);
                checkAndUnlock("trophy_5000", trophies >= 5000);
                
                // TIME-BASED ACHIEVEMENTS
                checkTimeBasedAchievements();
                
                // SPECIAL ACHIEVEMENTS
                checkSpecialAchievements(progress);
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking achievements", e);
            }
        });
    }
    
    /**
     * Check and unlock a specific achievement if condition is met.
     */
    private void checkAndUnlock(String achievementId, boolean condition) {
        if (!condition) return;
        
        try {
            UserAchievement existing = achievementDao.getUserAchievementSync(achievementId);
            if (existing != null) return; // Already unlocked
            
            AchievementDefinition definition = findAchievementDefinition(achievementId);
            if (definition == null) return;
            
            // Unlock!
            UserAchievement newAchievement = new UserAchievement();
            newAchievement.setAchievementId(achievementId);
            newAchievement.setUnlockedTimestamp(System.currentTimeMillis());
            newAchievement.setNotificationShown(false);
            
            achievementDao.insertUserAchievement(newAchievement);
            
            // Award XP
            UserProgress progress = userProgressDao.getUserProgressSync();
            if (progress != null) {
                progress.setTotalXp(progress.getTotalXp() + definition.getXpReward());
                userProgressDao.update(progress);
            }
            
            Log.d(TAG, "Achievement unlocked: " + definition.getName());
            
            // Notify listener on main thread
            if (listener != null) {
                android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                handler.post(() -> listener.onAchievementUnlocked(definition));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error unlocking achievement: " + achievementId, e);
        }
    }
    
    private AchievementDefinition findAchievementDefinition(String id) {
        List<AchievementDefinition> all = achievementDao.getAllAchievementDefinitionsSync();
        for (AchievementDefinition def : all) {
            if (def.getId().equals(id)) {
                return def;
            }
        }
        return null;
    }
    
    private void checkTimeBasedAchievements() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // Early Bird (solve before 6am)
        if (hour < 6) {
            incrementCounter("early_bird_count");
            int count = prefs.getInt("early_bird_count", 0);
            checkAndUnlock("early_bird", count >= 1);
            checkAndUnlock("early_bird_5", count >= 5);
        }
        
        // Night Owl (solve after midnight)
        if (hour >= 0 && hour < 4) {
            incrementCounter("night_owl_count");
            int count = prefs.getInt("night_owl_count", 0);
            checkAndUnlock("night_owl", count >= 1);
            checkAndUnlock("night_owl_10", count >= 10);
        }
        
        // Weekend Warrior
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            incrementCounter("weekend_count");
            int count = prefs.getInt("weekend_count", 0);
            checkAndUnlock("weekend_warrior", count >= 5);
            checkAndUnlock("weekend_master", count >= 20);
        }
    }
    
    private void checkSpecialAchievements(UserProgress progress) {
        // Speed Demon - Check from prefs
        int fastSolves = prefs.getInt("fast_solves", 0);
        checkAndUnlock("speed_demon", fastSolves >= 10);
        checkAndUnlock("lightning_fast", fastSolves >= 50);
        
        // Language Master - Solved bugs in multiple languages
        int languagesUsed = prefs.getInt("languages_used", 0);
        checkAndUnlock("polyglot", languagesUsed >= 3);
        checkAndUnlock("language_master", languagesUsed >= 5);
        
        // Category Master - Completed all bugs in a category
        boolean categoryComplete = prefs.getBoolean("category_complete", false);
        checkAndUnlock("category_master", categoryComplete);
        
        // Perfect Run - Complete a path without hints
        boolean perfectPath = prefs.getBoolean("perfect_path", false);
        checkAndUnlock("perfect_path", perfectPath);
    }
    
    private void incrementCounter(String key) {
        int count = prefs.getInt(key, 0);
        prefs.edit().putInt(key, count + 1).apply();
    }
    
    /**
     * Record a bug solve for achievement tracking.
     */
    public void recordBugSolved(boolean usedHint, long solveTimeMs, String language, String category) {
        executor.execute(() -> {
            // Track fast solves (under 60 seconds)
            if (solveTimeMs < 60000) {
                incrementCounter("fast_solves");
            }
            
            // Track languages used
            String languagesKey = "languages_" + language.toLowerCase();
            if (!prefs.getBoolean(languagesKey, false)) {
                prefs.edit().putBoolean(languagesKey, true).apply();
                int count = prefs.getInt("languages_used", 0);
                prefs.edit().putInt("languages_used", count + 1).apply();
            }
            
            // Check all achievements
            checkAllAchievements();
        });
    }
    
    /**
     * Record a battle result.
     */
    public void recordBattleResult(boolean won, int trophyChange) {
        executor.execute(() -> {
            if (won) {
                int wins = prefs.getInt("battle_wins", 0) + 1;
                int streak = prefs.getInt("battle_streak", 0) + 1;
                int bestStreak = prefs.getInt("best_battle_streak", 0);
                
                prefs.edit()
                    .putInt("battle_wins", wins)
                    .putInt("battle_streak", streak)
                    .putInt("best_battle_streak", Math.max(streak, bestStreak))
                    .apply();
            } else {
                prefs.edit().putInt("battle_streak", 0).apply();
            }
            
            int trophies = prefs.getInt("trophies", 0) + trophyChange;
            prefs.edit().putInt("trophies", Math.max(0, trophies)).apply();
            
            checkAllAchievements();
        });
    }
    
    /**
     * Record a daily login for streak tracking.
     */
    public void recordDailyLogin() {
        executor.execute(() -> {
            long lastLogin = prefs.getLong("last_daily_login", 0);
            long now = System.currentTimeMillis();
            long dayMs = 24 * 60 * 60 * 1000;
            
            if (now - lastLogin > dayMs) {
                prefs.edit().putLong("last_daily_login", now).apply();
                
                // First login of the day achievement
                int totalDays = prefs.getInt("total_login_days", 0) + 1;
                prefs.edit().putInt("total_login_days", totalDays).apply();
                
                checkAndUnlock("first_login", true);
                checkAndUnlock("login_days_7", totalDays >= 7);
                checkAndUnlock("login_days_30", totalDays >= 30);
                checkAndUnlock("login_days_100", totalDays >= 100);
                checkAndUnlock("login_days_365", totalDays >= 365);
            }
            
            checkAllAchievements();
        });
    }
    
    /**
     * Get all achievement definitions.
     */
    public static List<AchievementDefinition> getAllAchievementDefinitions() {
        List<AchievementDefinition> achievements = new ArrayList<>();
        
        // ═══════════════════════════════════════════════════════════════════════
        // MILESTONE ACHIEVEMENTS - Bug Solving
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("first_fix", "First Fix", 
            "Solve your first bug", "🐛", 25, "MILESTONE", 1));
        achievements.add(new AchievementDefinition("bug_squasher_10", "Bug Squasher", 
            "Solve 10 bugs", "🔨", 50, "MILESTONE", 2));
        achievements.add(new AchievementDefinition("bug_hunter_25", "Bug Hunter", 
            "Solve 25 bugs", "🎯", 100, "MILESTONE", 3));
        achievements.add(new AchievementDefinition("bug_slayer_50", "Bug Slayer", 
            "Solve 50 bugs", "⚔️", 200, "MILESTONE", 4));
        achievements.add(new AchievementDefinition("bug_master_100", "Bug Master", 
            "Solve 100 bugs", "👑", 500, "MILESTONE", 5));
        achievements.add(new AchievementDefinition("bug_legend_250", "Bug Legend", 
            "Solve 250 bugs", "🏆", 1000, "MILESTONE", 6));
        achievements.add(new AchievementDefinition("bug_god_500", "Bug God", 
            "Solve 500 bugs", "🌟", 2000, "MILESTONE", 7));
        achievements.add(new AchievementDefinition("bug_immortal_1000", "Bug Immortal", 
            "Solve 1000 bugs", "💎", 5000, "MILESTONE", 8));
        
        // ═══════════════════════════════════════════════════════════════════════
        // MILESTONE ACHIEVEMENTS - XP
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("xp_100", "Getting Started", 
            "Earn 100 XP", "⭐", 10, "MILESTONE", 10));
        achievements.add(new AchievementDefinition("xp_500", "Rising Star", 
            "Earn 500 XP", "⭐", 25, "MILESTONE", 11));
        achievements.add(new AchievementDefinition("xp_1000", "Thousand Club", 
            "Earn 1,000 XP", "⭐", 50, "MILESTONE", 12));
        achievements.add(new AchievementDefinition("xp_5000", "XP Collector", 
            "Earn 5,000 XP", "🌟", 100, "MILESTONE", 13));
        achievements.add(new AchievementDefinition("xp_10000", "XP Master", 
            "Earn 10,000 XP", "🌟", 200, "MILESTONE", 14));
        achievements.add(new AchievementDefinition("xp_50000", "XP Legend", 
            "Earn 50,000 XP", "💫", 500, "MILESTONE", 15));
        achievements.add(new AchievementDefinition("xp_100000", "XP Immortal", 
            "Earn 100,000 XP", "✨", 1000, "MILESTONE", 16));
        
        // ═══════════════════════════════════════════════════════════════════════
        // STREAK ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("streak_3", "On a Roll", 
            "3 day streak", "🔥", 30, "STREAK", 20));
        achievements.add(new AchievementDefinition("streak_7", "Week Warrior", 
            "7 day streak", "🔥", 75, "STREAK", 21));
        achievements.add(new AchievementDefinition("streak_14", "Two Week Champion", 
            "14 day streak", "🔥", 150, "STREAK", 22));
        achievements.add(new AchievementDefinition("streak_30", "Monthly Master", 
            "30 day streak", "🔥", 300, "STREAK", 23));
        achievements.add(new AchievementDefinition("streak_60", "Dedicated Debugger", 
            "60 day streak", "🔥", 600, "STREAK", 24));
        achievements.add(new AchievementDefinition("streak_100", "Unstoppable", 
            "100 day streak", "💪", 1000, "STREAK", 25));
        achievements.add(new AchievementDefinition("streak_365", "Year of Code", 
            "365 day streak", "🏅", 5000, "STREAK", 26));
        
        // ═══════════════════════════════════════════════════════════════════════
        // SKILL ACHIEVEMENTS - No Hints
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("no_hint_1", "Independent Thinker", 
            "Solve 1 bug without hints", "🧠", 25, "SKILL", 30));
        achievements.add(new AchievementDefinition("no_hint_10", "Self Reliant", 
            "Solve 10 bugs without hints", "🧠", 100, "SKILL", 31));
        achievements.add(new AchievementDefinition("no_hint_50", "No Training Wheels", 
            "Solve 50 bugs without hints", "🧠", 250, "SKILL", 32));
        achievements.add(new AchievementDefinition("no_hint_100", "True Expert", 
            "Solve 100 bugs without hints", "🎓", 500, "SKILL", 33));
        
        // ═══════════════════════════════════════════════════════════════════════
        // SKILL ACHIEVEMENTS - Speed
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("speed_demon", "Speed Demon", 
            "Solve 10 bugs in under 60 seconds each", "⚡", 100, "SKILL", 35));
        achievements.add(new AchievementDefinition("lightning_fast", "Lightning Fast", 
            "Solve 50 bugs in under 60 seconds each", "⚡", 300, "SKILL", 36));
        
        // ═══════════════════════════════════════════════════════════════════════
        // SKILL ACHIEVEMENTS - Languages
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("polyglot", "Polyglot", 
            "Solve bugs in 3 different languages", "🌍", 100, "SKILL", 40));
        achievements.add(new AchievementDefinition("language_master", "Language Master", 
            "Solve bugs in 5 different languages", "🌍", 250, "SKILL", 41));
        
        // ═══════════════════════════════════════════════════════════════════════
        // BATTLE ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("first_battle_win", "First Victory", 
            "Win your first battle", "⚔️", 50, "BATTLE", 50));
        achievements.add(new AchievementDefinition("battle_wins_10", "Gladiator", 
            "Win 10 battles", "⚔️", 100, "BATTLE", 51));
        achievements.add(new AchievementDefinition("battle_wins_50", "Warrior", 
            "Win 50 battles", "⚔️", 300, "BATTLE", 52));
        achievements.add(new AchievementDefinition("battle_wins_100", "Champion", 
            "Win 100 battles", "🏆", 500, "BATTLE", 53));
        achievements.add(new AchievementDefinition("battle_wins_500", "Legendary Fighter", 
            "Win 500 battles", "👑", 1000, "BATTLE", 54));
        
        achievements.add(new AchievementDefinition("battle_streak_3", "Hat Trick", 
            "Win 3 battles in a row", "🎩", 50, "BATTLE", 55));
        achievements.add(new AchievementDefinition("battle_streak_5", "Dominator", 
            "Win 5 battles in a row", "💪", 100, "BATTLE", 56));
        achievements.add(new AchievementDefinition("battle_streak_10", "Unstoppable Force", 
            "Win 10 battles in a row", "🌟", 250, "BATTLE", 57));
        achievements.add(new AchievementDefinition("battle_streak_20", "Invincible", 
            "Win 20 battles in a row", "💎", 500, "BATTLE", 58));
        
        achievements.add(new AchievementDefinition("trophy_100", "Bronze League", 
            "Earn 100 trophies", "🥉", 50, "BATTLE", 60));
        achievements.add(new AchievementDefinition("trophy_500", "Silver League", 
            "Earn 500 trophies", "🥈", 100, "BATTLE", 61));
        achievements.add(new AchievementDefinition("trophy_1000", "Gold League", 
            "Earn 1,000 trophies", "🥇", 200, "BATTLE", 62));
        achievements.add(new AchievementDefinition("trophy_5000", "Diamond League", 
            "Earn 5,000 trophies", "💎", 500, "BATTLE", 63));
        
        // ═══════════════════════════════════════════════════════════════════════
        // TIME-BASED ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("early_bird", "Early Bird", 
            "Solve a bug before 6am", "🌅", 50, "CHALLENGE", 70));
        achievements.add(new AchievementDefinition("early_bird_5", "Dawn Patrol", 
            "Solve 5 bugs before 6am", "🌅", 150, "CHALLENGE", 71));
        achievements.add(new AchievementDefinition("night_owl", "Night Owl", 
            "Solve a bug after midnight", "🦉", 50, "CHALLENGE", 72));
        achievements.add(new AchievementDefinition("night_owl_10", "Night Master", 
            "Solve 10 bugs after midnight", "🦉", 200, "CHALLENGE", 73));
        achievements.add(new AchievementDefinition("weekend_warrior", "Weekend Warrior", 
            "Solve 5 bugs on weekends", "📅", 75, "CHALLENGE", 74));
        achievements.add(new AchievementDefinition("weekend_master", "Weekend Master", 
            "Solve 20 bugs on weekends", "📅", 200, "CHALLENGE", 75));
        
        // ═══════════════════════════════════════════════════════════════════════
        // LOGIN ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("first_login", "Welcome!", 
            "Open the app for the first time", "👋", 10, "MILESTONE", 0));
        achievements.add(new AchievementDefinition("login_days_7", "Regular", 
            "Log in 7 different days", "📱", 50, "MILESTONE", 80));
        achievements.add(new AchievementDefinition("login_days_30", "Committed", 
            "Log in 30 different days", "📱", 150, "MILESTONE", 81));
        achievements.add(new AchievementDefinition("login_days_100", "Dedicated", 
            "Log in 100 different days", "📱", 300, "MILESTONE", 82));
        achievements.add(new AchievementDefinition("login_days_365", "One Year Club", 
            "Log in 365 different days", "🎂", 1000, "MILESTONE", 83));
        
        // ═══════════════════════════════════════════════════════════════════════
        // MASTERY ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("category_master", "Category Master", 
            "Complete all bugs in a category", "📚", 500, "MASTERY", 90));
        achievements.add(new AchievementDefinition("perfect_path", "Perfect Path", 
            "Complete a learning path without hints", "✨", 750, "MASTERY", 91));
        achievements.add(new AchievementDefinition("all_paths", "Scholar", 
            "Complete all learning paths", "🎓", 2000, "MASTERY", 92));
        achievements.add(new AchievementDefinition("completionist", "Completionist", 
            "Solve all available bugs", "🏅", 5000, "MASTERY", 93));
        
        // ═══════════════════════════════════════════════════════════════════════
        // SECRET ACHIEVEMENTS
        // ═══════════════════════════════════════════════════════════════════════
        achievements.add(new AchievementDefinition("lucky_7", "Lucky Seven", 
            "Solve 7 bugs on the 7th day of a month", "🍀", 77, "SECRET", 100));
        achievements.add(new AchievementDefinition("triple_threat", "Triple Threat", 
            "Solve 3 hard bugs in a row without hints", "💪", 300, "SECRET", 101));
        achievements.add(new AchievementDefinition("comeback_kid", "Comeback Kid", 
            "Win a battle after losing 3 in a row", "🔄", 100, "SECRET", 102));
        
        return achievements;
    }
}
