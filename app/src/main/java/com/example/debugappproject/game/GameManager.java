package com.example.debugappproject.game;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;

/**
 * GameManager - Core game logic for DebugMaster! 🎮
 * 
 * XP System:
 * - Easy bug: 10 XP | Medium: 25 XP | Hard: 50 XP | Expert: 100 XP
 * - Perfect solve (no hints): 2x multiplier
 * - Streak bonus: +5% per day (max 50%)
 * - Daily challenge bonus: +25 XP
 */
public class GameManager {

    private static final String PREFS_NAME = "debugmaster_game";
    private static final String KEY_DAILY_SOLVES = "daily_solves";
    private static final String KEY_LAST_SOLVE_DATE = "last_solve_date";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_TOTAL_XP = "total_xp";
    private static final String KEY_TOTAL_SOLVED = "total_solved";
    private static final String KEY_PERFECT_SOLVES = "perfect_solves";
    private static final String KEY_DAILY_CHALLENGE_DONE = "daily_challenge_done";
    
    public static final int FREE_DAILY_LIMIT = 5;
    public static final int XP_EASY = 10;
    public static final int XP_MEDIUM = 25;
    public static final int XP_HARD = 50;
    public static final int XP_EXPERT = 100;
    public static final int XP_DAILY_BONUS = 25;

    private final SharedPreferences prefs;
    private final MutableLiveData<Integer> dailySolvesRemaining = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentStreak = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalXp = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentLevel = new MutableLiveData<>();

    private boolean isProUser = false;

    public GameManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkAndResetDaily();
        loadStats();
    }

    private void checkAndResetDaily() {
        String today = getTodayString();
        String lastDate = prefs.getString(KEY_LAST_SOLVE_DATE, "");
        
        if (!today.equals(lastDate)) {
            prefs.edit()
                .putInt(KEY_DAILY_SOLVES, 0)
                .putBoolean(KEY_DAILY_CHALLENGE_DONE, false)
                .putString(KEY_LAST_SOLVE_DATE, today)
                .apply();
            
            if (!isYesterday(lastDate) && !lastDate.isEmpty()) {
                prefs.edit().putInt(KEY_STREAK, 0).apply();
            }
        }
    }

    private void loadStats() {
        int dailySolves = prefs.getInt(KEY_DAILY_SOLVES, 0);
        int remaining = isProUser ? 999 : Math.max(0, FREE_DAILY_LIMIT - dailySolves);
        dailySolvesRemaining.postValue(remaining);
        currentStreak.postValue(prefs.getInt(KEY_STREAK, 0));
        int xp = prefs.getInt(KEY_TOTAL_XP, 0);
        totalXp.postValue(xp);
        currentLevel.postValue(calculateLevel(xp));
    }

    public int recordBugSolved(String difficulty, boolean usedHints, boolean isDailyChallenge) {
        checkAndResetDaily();
        
        int dailySolves = prefs.getInt(KEY_DAILY_SOLVES, 0) + 1;
        prefs.edit().putInt(KEY_DAILY_SOLVES, dailySolves).apply();
        
        int remaining = isProUser ? 999 : Math.max(0, FREE_DAILY_LIMIT - dailySolves);
        dailySolvesRemaining.postValue(remaining);
        
        int baseXp = getXpForDifficulty(difficulty);
        double multiplier = 1.0;
        
        if (!usedHints) {
            multiplier *= 2.0;
            int perfectSolves = prefs.getInt(KEY_PERFECT_SOLVES, 0) + 1;
            prefs.edit().putInt(KEY_PERFECT_SOLVES, perfectSolves).apply();
        }
        
        int streak = prefs.getInt(KEY_STREAK, 0);
        double streakBonus = Math.min(streak * 0.05, 0.50);
        multiplier += streakBonus;
        
        int earnedXp = (int) (baseXp * multiplier);
        
        if (isDailyChallenge && !prefs.getBoolean(KEY_DAILY_CHALLENGE_DONE, false)) {
            earnedXp += XP_DAILY_BONUS;
            prefs.edit().putBoolean(KEY_DAILY_CHALLENGE_DONE, true).apply();
            updateStreak();
        }
        
        int newTotalXp = prefs.getInt(KEY_TOTAL_XP, 0) + earnedXp;
        int newTotalSolved = prefs.getInt(KEY_TOTAL_SOLVED, 0) + 1;
        
        prefs.edit()
            .putInt(KEY_TOTAL_XP, newTotalXp)
            .putInt(KEY_TOTAL_SOLVED, newTotalSolved)
            .apply();
        
        totalXp.postValue(newTotalXp);
        currentLevel.postValue(calculateLevel(newTotalXp));
        
        return earnedXp;
    }

    private void updateStreak() {
        int streak = prefs.getInt(KEY_STREAK, 0) + 1;
        prefs.edit().putInt(KEY_STREAK, streak).apply();
        currentStreak.postValue(streak);
    }

    private int getXpForDifficulty(String difficulty) {
        if (difficulty == null) return XP_EASY;
        switch (difficulty.toLowerCase()) {
            case "medium": return XP_MEDIUM;
            case "hard": return XP_HARD;
            case "expert": return XP_EXPERT;
            default: return XP_EASY;
        }
    }

    public static int calculateLevel(int totalXp) {
        int level = 1;
        int xpNeeded = 0;
        while (true) {
            int xpForLevel = getXpForLevel(level);
            if (xpNeeded + xpForLevel > totalXp) break;
            xpNeeded += xpForLevel;
            level++;
        }
        return level;
    }

    public static int getXpForLevel(int level) {
        if (level <= 10) return 100;
        if (level <= 25) return 150;
        if (level <= 50) return 200;
        return 300;
    }

    public boolean canSolveMoreBugs() {
        if (isProUser) return true;
        return prefs.getInt(KEY_DAILY_SOLVES, 0) < FREE_DAILY_LIMIT;
    }

    public void setProUser(boolean isPro) {
        this.isProUser = isPro;
        loadStats();
    }

    public LiveData<Integer> getDailySolvesRemaining() { return dailySolvesRemaining; }
    public LiveData<Integer> getCurrentStreak() { return currentStreak; }
    public LiveData<Integer> getTotalXp() { return totalXp; }
    public LiveData<Integer> getCurrentLevel() { return currentLevel; }
    public int getTotalSolved() { return prefs.getInt(KEY_TOTAL_SOLVED, 0); }
    public int getPerfectSolves() { return prefs.getInt(KEY_PERFECT_SOLVES, 0); }

    private String getTodayString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(String dateString) {
        if (dateString == null || dateString.isEmpty()) return false;
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayString = yesterday.get(Calendar.YEAR) + "-" + yesterday.get(Calendar.DAY_OF_YEAR);
        return dateString.equals(yesterdayString);
    }
}
