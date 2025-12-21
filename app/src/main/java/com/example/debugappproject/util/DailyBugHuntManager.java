package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - DAILY BUG HUNT MANAGER                               ║
 * ║        Manages daily challenges with streak tracking                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Deterministic daily challenge selection (same bug for all users on same day)
 * - Local streak tracking with SharedPreferences
 * - Bonus XP calculation based on streak
 * - No network required - works offline
 */
public class DailyBugHuntManager {

    private static final String PREFS_NAME = "daily_bug_hunt_prefs";
    private static final String KEY_LAST_COMPLETED_DATE = "last_completed_date";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_LONGEST_STREAK = "longest_streak";
    private static final String KEY_TOTAL_COMPLETED = "total_completed";

    // Challenge pool - indices of "good" daily challenge bugs (Easy/Medium difficulty)
    private static final int[] DAILY_BUG_POOL = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    };

    private static final String[] CHALLENGE_TITLES = {
        "Morning Debug ☀️",
        "Quick Fix Challenge 🔧",
        "Bug Squasher 🐛",
        "Code Detective 🔍",
        "Syntax Hunter 🎯",
        "Logic Master 🧠",
        "Error Eliminator ⚡",
        "Debug Sprint 🏃",
        "Fix & Win 🏆",
        "Daily Brain Teaser 💡"
    };

    private final Context context;
    private final SharedPreferences prefs;

    public DailyBugHuntManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get today's date key in format "yyyy-MM-dd"
     */
    public static String getTodayDateKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    /**
     * Get deterministic bug ID for a given date.
     * Same date = same bug for all users.
     */
    public int getDailyBugId(String dateKey) {
        // Use date string hash to get deterministic index
        int hash = dateKey.hashCode();
        int index = Math.abs(hash) % DAILY_BUG_POOL.length;
        return DAILY_BUG_POOL[index];
    }

    /**
     * Get today's challenge bug ID
     */
    public int getTodayBugId() {
        return getDailyBugId(getTodayDateKey());
    }

    /**
     * Get a fun title for the daily challenge
     */
    public String getDailyChallengeTitle(String dateKey) {
        int hash = dateKey.hashCode();
        int index = Math.abs(hash) % CHALLENGE_TITLES.length;
        return CHALLENGE_TITLES[index];
    }

    /**
     * Get today's challenge title
     */
    public String getTodayChallengeTitle() {
        return getDailyChallengeTitle(getTodayDateKey());
    }

    /**
     * Calculate XP reward with streak bonus
     * Base: 50 XP
     * Streak bonus: +10 XP per day (max +50)
     */
    public int calculateXpReward() {
        int baseXp = 50;
        int streak = getCurrentStreak();
        int streakBonus = Math.min(streak * 10, 50);
        return baseXp + streakBonus;
    }

    /**
     * Check if today's challenge is already completed
     */
    public boolean isTodayCompleted() {
        String lastCompleted = prefs.getString(KEY_LAST_COMPLETED_DATE, "");
        return getTodayDateKey().equals(lastCompleted);
    }

    /**
     * Mark today's challenge as completed
     */
    public void markTodayCompleted() {
        String today = getTodayDateKey();
        String lastCompleted = prefs.getString(KEY_LAST_COMPLETED_DATE, "");
        
        // Check if this continues the streak
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);
        
        if (isYesterday(lastCompleted)) {
            // Continuing streak
            currentStreak++;
        } else if (!today.equals(lastCompleted)) {
            // Starting new streak
            currentStreak = 1;
        }
        // If already completed today, don't change anything
        
        int longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0);
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }
        
        int totalCompleted = prefs.getInt(KEY_TOTAL_COMPLETED, 0) + 1;
        
        prefs.edit()
            .putString(KEY_LAST_COMPLETED_DATE, today)
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putInt(KEY_TOTAL_COMPLETED, totalCompleted)
            .apply();
    }

    /**
     * Check if a date string is yesterday
     */
    private boolean isYesterday(String dateKey) {
        if (dateKey == null || dateKey.isEmpty()) return false;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayKey = sdf.format(yesterday.getTime());
            return yesterdayKey.equals(dateKey);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current streak count
     */
    public int getCurrentStreak() {
        String lastCompleted = prefs.getString(KEY_LAST_COMPLETED_DATE, "");
        String today = getTodayDateKey();
        
        // If completed today, return streak
        if (today.equals(lastCompleted)) {
            return prefs.getInt(KEY_CURRENT_STREAK, 0);
        }
        
        // If completed yesterday, streak continues but not incremented yet
        if (isYesterday(lastCompleted)) {
            return prefs.getInt(KEY_CURRENT_STREAK, 0);
        }
        
        // Streak broken
        return 0;
    }

    /**
     * Get longest streak ever
     */
    public int getLongestStreak() {
        return prefs.getInt(KEY_LONGEST_STREAK, 0);
    }

    /**
     * Get total challenges completed
     */
    public int getTotalCompleted() {
        return prefs.getInt(KEY_TOTAL_COMPLETED, 0);
    }

    /**
     * Get time until next challenge (hours and minutes)
     */
    public String getTimeUntilReset() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.add(Calendar.DAY_OF_YEAR, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        
        long diff = midnight.getTimeInMillis() - now.getTimeInMillis();
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + " min";
        }
    }

    /**
     * Get estimated time for daily challenge (5-7 min)
     */
    public int getEstimatedMinutes() {
        // Varies slightly based on date for variety
        String dateKey = getTodayDateKey();
        int hash = Math.abs(dateKey.hashCode());
        return 5 + (hash % 3); // 5, 6, or 7 minutes
    }

    /**
     * Check if user should be reminded about streak at risk
     */
    public boolean isStreakAtRisk() {
        if (isTodayCompleted()) return false;
        
        int streak = getCurrentStreak();
        if (streak < 2) return false;
        
        // Check if it's late in the day
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        return hour >= 20; // After 8 PM
    }
}
