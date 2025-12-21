package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.animation.AnimationUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - DAILY REWARD MANAGER                                 ║
 * ║     Handles daily login rewards, streak bonuses, and claim state             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class DailyRewardManager {

    private static final String PREFS_NAME = "daily_reward_prefs";
    private static final String KEY_LAST_CLAIM_DATE = "last_claim_date";
    private static final String KEY_CONSECUTIVE_DAYS = "consecutive_days";
    private static final String KEY_TOTAL_GEMS_CLAIMED = "total_gems_claimed";
    private static final String KEY_LAST_REWARD_AMOUNT = "last_reward_amount";

    // Base reward + streak bonus
    private static final int BASE_GEM_REWARD = 15;
    private static final int STREAK_BONUS_PER_DAY = 2;
    private static final int MAX_STREAK_BONUS = 35; // Cap at day 10 bonus

    private final SharedPreferences prefs;
    private final Context context;

    public DailyRewardManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if user can claim today's reward.
     */
    public boolean canClaimToday() {
        String lastClaimDate = prefs.getString(KEY_LAST_CLAIM_DATE, "");
        String today = getTodayDateString();
        return !today.equals(lastClaimDate);
    }

    /**
     * Claim today's reward. Returns the gem amount, or 0 if already claimed.
     */
    public int claimDailyReward() {
        if (!canClaimToday()) {
            return 0;
        }

        String today = getTodayDateString();
        String lastClaim = prefs.getString(KEY_LAST_CLAIM_DATE, "");
        int consecutiveDays = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0);

        // Check if this is a consecutive day
        if (isYesterday(lastClaim)) {
            consecutiveDays++;
        } else if (!lastClaim.isEmpty()) {
            // Streak broken
            consecutiveDays = 1;
        } else {
            // First time claiming
            consecutiveDays = 1;
        }

        // Calculate reward with streak bonus
        int streakBonus = Math.min((consecutiveDays - 1) * STREAK_BONUS_PER_DAY, MAX_STREAK_BONUS);
        int reward = BASE_GEM_REWARD + streakBonus;

        // Save claim state
        prefs.edit()
                .putString(KEY_LAST_CLAIM_DATE, today)
                .putInt(KEY_CONSECUTIVE_DAYS, consecutiveDays)
                .putInt(KEY_TOTAL_GEMS_CLAIMED, prefs.getInt(KEY_TOTAL_GEMS_CLAIMED, 0) + reward)
                .putInt(KEY_LAST_REWARD_AMOUNT, reward)
                .apply();

        return reward;
    }

    /**
     * Get the current consecutive login days.
     */
    public int getConsecutiveDays() {
        String lastClaim = prefs.getString(KEY_LAST_CLAIM_DATE, "");
        int consecutive = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0);
        
        // If last claim was today or yesterday, streak is active
        if (isToday(lastClaim) || isYesterday(lastClaim)) {
            return consecutive;
        }
        return 0; // Streak broken
    }

    /**
     * Get what today's reward would be (for preview).
     */
    public int getTodayRewardAmount() {
        int consecutiveDays = getConsecutiveDays() + (canClaimToday() ? 1 : 0);
        int streakBonus = Math.min((consecutiveDays - 1) * STREAK_BONUS_PER_DAY, MAX_STREAK_BONUS);
        return BASE_GEM_REWARD + streakBonus;
    }

    /**
     * Get the last claimed reward amount.
     */
    public int getLastRewardAmount() {
        return prefs.getInt(KEY_LAST_REWARD_AMOUNT, BASE_GEM_REWARD);
    }

    /**
     * Check if animations should be reduced.
     */
    public boolean shouldReduceMotion() {
        float animatorScale = Settings.Global.getFloat(
                context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
        );
        return animatorScale == 0f;
    }

    /**
     * Trigger light haptic feedback for reward reveal.
     */
    public void triggerRewardHaptic() {
        if (!shouldReduceMotion()) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(30);
                }
            }
        }
    }

    /**
     * Trigger stronger haptic for play button.
     */
    public void triggerPlayHaptic() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                          DATE UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    private boolean isToday(String dateString) {
        return getTodayDateString().equals(dateString);
    }

    private boolean isYesterday(String dateString) {
        if (dateString == null || dateString.isEmpty()) return false;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayString = sdf.format(yesterday.getTime());
            return yesterdayString.equals(dateString);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get a fun greeting based on time of day.
     */
    public String getTimeBasedGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        if (hour < 21) return "Good evening";
        return "Night owl mode";
    }

    /**
     * Get streak status message.
     */
    public String getStreakMessage(int streakDays) {
        if (streakDays == 0) {
            return "🔥 Start a streak today!";
        } else if (streakDays == 1) {
            return "🔥 1 day streak started!";
        } else if (streakDays < 7) {
            return "🔥 " + streakDays + " day streak!";
        } else if (streakDays < 30) {
            return "🔥 " + streakDays + " days on fire!";
        } else {
            return "🔥 " + streakDays + " days! LEGENDARY!";
        }
    }
}
