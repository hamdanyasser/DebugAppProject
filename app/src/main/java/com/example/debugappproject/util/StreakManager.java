package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - STREAK MANAGER                                    ║
 * ║           Streak Protection & Weekend XP System 🔥                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Streak Freeze: Protect your streak for 1 day
 * - Weekend XP Multiplier: 2x XP on Saturday/Sunday
 * - Streak Milestones: Special rewards at 7, 30, 100 days
 * - Grace Period: 24-hour grace period after missing a day
 */
public class StreakManager {

    private static final String PREFS_NAME = "streak_manager_prefs";
    private static final String KEY_STREAK_FREEZES = "streak_freezes";
    private static final String KEY_FREEZE_USED_DATE = "freeze_used_date";
    private static final String KEY_GRACE_PERIOD_START = "grace_period_start";
    private static final String KEY_LAST_ACTIVE_DATE = "last_active_date";

    // Streak freeze costs (in XP)
    private static final int FREEZE_COST_XP = 100;
    private static final int MAX_FREEZES = 5;
    private static final long GRACE_PERIOD_MS = 24 * 60 * 60 * 1000; // 24 hours

    private final Context context;
    private final SharedPreferences prefs;

    public StreakManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WEEKEND XP MULTIPLIER
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check if today is a weekend (Saturday or Sunday).
     */
    public boolean isWeekend() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Get the XP multiplier for today.
     * Returns 2.0 on weekends, 1.0 on weekdays.
     */
    public double getXpMultiplier() {
        return isWeekend() ? 2.0 : 1.0;
    }

    /**
     * Apply weekend multiplier to XP.
     */
    public int applyWeekendBonus(int baseXp) {
        return (int) (baseXp * getXpMultiplier());
    }

    /**
     * Get weekend bonus description for UI.
     */
    public String getWeekendBonusText() {
        if (isWeekend()) {
            return "🎉 WEEKEND 2X XP ACTIVE!";
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STREAK FREEZE SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get number of available streak freezes.
     */
    public int getAvailableFreezes() {
        return prefs.getInt(KEY_STREAK_FREEZES, 1); // Start with 1 free freeze
    }

    /**
     * Add a streak freeze (e.g., purchased or earned).
     */
    public void addFreeze() {
        int current = getAvailableFreezes();
        if (current < MAX_FREEZES) {
            prefs.edit().putInt(KEY_STREAK_FREEZES, current + 1).apply();
        }
    }

    /**
     * Purchase a streak freeze with XP.
     * Returns true if purchase successful.
     */
    public boolean purchaseFreezeWithXp(int currentXp, XpDeductionCallback callback) {
        if (currentXp < FREEZE_COST_XP) {
            return false;
        }
        if (getAvailableFreezes() >= MAX_FREEZES) {
            return false;
        }
        if (callback != null) {
            callback.deductXp(FREEZE_COST_XP);
        }
        addFreeze();
        return true;
    }

    /**
     * Use a streak freeze to protect today's streak.
     * Returns true if freeze was used successfully.
     */
    public boolean useFreeze() {
        int freezes = getAvailableFreezes();
        if (freezes <= 0) {
            return false;
        }

        String today = getTodayDateString();
        String lastUsed = prefs.getString(KEY_FREEZE_USED_DATE, "");

        // Can only use one freeze per day
        if (today.equals(lastUsed)) {
            return false;
        }

        prefs.edit()
            .putInt(KEY_STREAK_FREEZES, freezes - 1)
            .putString(KEY_FREEZE_USED_DATE, today)
            .apply();

        return true;
    }

    /**
     * Check if a freeze was used today.
     */
    public boolean isFreezeActiveToday() {
        String today = getTodayDateString();
        String lastUsed = prefs.getString(KEY_FREEZE_USED_DATE, "");
        return today.equals(lastUsed);
    }

    /**
     * Get the cost of a streak freeze in XP.
     */
    public int getFreezeCostXp() {
        return FREEZE_COST_XP;
    }

    /**
     * Get max freezes that can be held.
     */
    public int getMaxFreezes() {
        return MAX_FREEZES;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GRACE PERIOD SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Start a grace period (when streak would normally break).
     */
    public void startGracePeriod() {
        prefs.edit()
            .putLong(KEY_GRACE_PERIOD_START, System.currentTimeMillis())
            .apply();
    }

    /**
     * Check if we're currently in a grace period.
     */
    public boolean isInGracePeriod() {
        long graceStart = prefs.getLong(KEY_GRACE_PERIOD_START, 0);
        if (graceStart == 0) return false;

        long elapsed = System.currentTimeMillis() - graceStart;
        return elapsed < GRACE_PERIOD_MS;
    }

    /**
     * Get remaining grace period time in milliseconds.
     */
    public long getGracePeriodRemaining() {
        long graceStart = prefs.getLong(KEY_GRACE_PERIOD_START, 0);
        if (graceStart == 0) return 0;

        long elapsed = System.currentTimeMillis() - graceStart;
        long remaining = GRACE_PERIOD_MS - elapsed;
        return Math.max(0, remaining);
    }

    /**
     * Clear grace period (streak was recovered).
     */
    public void clearGracePeriod() {
        prefs.edit().remove(KEY_GRACE_PERIOD_START).apply();
    }

    /**
     * Format grace period remaining for display.
     */
    public String getGracePeriodText() {
        long remaining = getGracePeriodRemaining();
        if (remaining <= 0) return null;

        long hours = remaining / (60 * 60 * 1000);
        long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);

        return String.format("⏰ %dh %dm left to save your streak!", hours, minutes);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STREAK MILESTONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get streak milestone reward if applicable.
     */
    public StreakMilestone checkMilestone(int streakDays) {
        switch (streakDays) {
            case 7:
                return new StreakMilestone(7, "🔥 Week Warrior!", 50, 1);
            case 14:
                return new StreakMilestone(14, "💪 Two Week Champion!", 100, 1);
            case 30:
                return new StreakMilestone(30, "🏆 Monthly Master!", 200, 2);
            case 60:
                return new StreakMilestone(60, "⭐ Two Month Legend!", 400, 2);
            case 100:
                return new StreakMilestone(100, "👑 100 Day King!", 1000, 3);
            case 365:
                return new StreakMilestone(365, "🌟 YEARLY LEGEND!", 5000, 5);
            default:
                return null;
        }
    }

    /**
     * Get streak tier for bonus calculation.
     */
    public StreakTier getStreakTier(int streakDays) {
        if (streakDays >= 100) return StreakTier.LEGENDARY;
        if (streakDays >= 30) return StreakTier.MASTER;
        if (streakDays >= 14) return StreakTier.EXPERT;
        if (streakDays >= 7) return StreakTier.ADVANCED;
        if (streakDays >= 3) return StreakTier.BEGINNER;
        return StreakTier.NONE;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════════════════════

    private String getTodayDateString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
    }

    public void recordActivity() {
        prefs.edit()
            .putString(KEY_LAST_ACTIVE_DATE, getTodayDateString())
            .apply();
    }

    public String getLastActiveDate() {
        return prefs.getString(KEY_LAST_ACTIVE_DATE, "");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum StreakTier {
        NONE(0, "No Streak", 1.0),
        BEGINNER(3, "Beginner", 1.05),
        ADVANCED(7, "Advanced", 1.10),
        EXPERT(14, "Expert", 1.20),
        MASTER(30, "Master", 1.30),
        LEGENDARY(100, "Legendary", 1.50);

        private final int minDays;
        private final String name;
        private final double multiplier;

        StreakTier(int minDays, String name, double multiplier) {
            this.minDays = minDays;
            this.name = name;
            this.multiplier = multiplier;
        }

        public int getMinDays() { return minDays; }
        public String getName() { return name; }
        public double getMultiplier() { return multiplier; }

        public String getEmoji() {
            switch (this) {
                case LEGENDARY: return "👑";
                case MASTER: return "🏆";
                case EXPERT: return "⭐";
                case ADVANCED: return "🔥";
                case BEGINNER: return "💪";
                default: return "🌱";
            }
        }
    }

    public static class StreakMilestone {
        public final int days;
        public final String title;
        public final int xpReward;
        public final int freezeReward;

        public StreakMilestone(int days, String title, int xpReward, int freezeReward) {
            this.days = days;
            this.title = title;
            this.xpReward = xpReward;
            this.freezeReward = freezeReward;
        }
    }

    public interface XpDeductionCallback {
        void deductXp(int amount);
    }
}
