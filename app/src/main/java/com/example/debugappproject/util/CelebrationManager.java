package com.example.debugappproject.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - CELEBRATION MANAGER                                ║
 * ║                 Dopamine Loop & Engagement System                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Manages celebratory messages, animations, and combo system for maximum engagement.
 */
public class CelebrationManager {

    private static final String PREFS_NAME = "celebration_prefs";
    private static final String KEY_COMBO_COUNT = "combo_count";
    private static final String KEY_LAST_SOLVE_TIME = "last_solve_time";
    private static final long COMBO_TIMEOUT_MS = 300000; // 5 minutes to maintain combo

    private final Context context;
    private final SharedPreferences prefs;
    private final Random random = new Random();
    private final SoundManager soundManager;

    // Celebration message arrays by tier
    private static final String[] PERFECT_MESSAGES = {
        "Perfect Fix! 🎯",
        "Flawless Victory! ⚡",
        "Bug Annihilated! 💥",
        "Clean Execution! ✨",
        "Master Debugger! 🏆",
        "Code Surgeon! 🔬",
        "Zero Errors! 🎪",
        "Precision Strike! 🎯"
    };

    private static final String[] GOOD_MESSAGES = {
        "Nice Fix! 👍",
        "Bug Squashed! 🐛",
        "Well Done! ⭐",
        "Solid Work! 💪",
        "Got It! ✓",
        "Success! 🌟",
        "Nailed It! 🔨"
    };

    private static final String[] COMBO_MESSAGES = {
        "🔥 COMBO x%d! 🔥",
        "⚡ %d IN A ROW! ⚡",
        "💥 %d STREAK! 💥",
        "🌟 %d COMBO! 🌟",
        "🎯 x%d MULTIPLIER! 🎯"
    };

    private static final String[] STREAK_MILESTONES = {
        "🔥 Day %d Streak!",
        "🌟 %d Days Strong!",
        "💪 %d Day Champion!",
        "🏆 %d Day Legend!"
    };

    private static final String[] DIFFICULTY_BONUSES = {
        "💎 Hard Mode Bonus!",
        "🏆 Expert Challenge!",
        "⭐ Difficulty Mastered!"
    };

    public CelebrationManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.soundManager = SoundManager.getInstance(context);
    }

    /**
     * Record a successful bug solve and get celebration data.
     */
    public CelebrationResult recordSolve(boolean perfect, String difficulty, int xpEarned, int streak) {
        long now = System.currentTimeMillis();
        long lastSolve = prefs.getLong(KEY_LAST_SOLVE_TIME, 0);
        int combo = prefs.getInt(KEY_COMBO_COUNT, 0);

        // Check if combo is still valid
        if (now - lastSolve > COMBO_TIMEOUT_MS) {
            combo = 0;
        }

        combo++;

        // Save new combo state
        prefs.edit()
            .putInt(KEY_COMBO_COUNT, combo)
            .putLong(KEY_LAST_SOLVE_TIME, now)
            .apply();

        // Build celebration result
        CelebrationResult result = new CelebrationResult();
        result.combo = combo;
        result.xpEarned = xpEarned;
        result.streak = streak;

        // Calculate combo bonus XP (5% per combo level, max 50%)
        double comboMultiplier = Math.min(1.0 + (combo * 0.05), 1.5);
        result.comboBonus = (int) ((xpEarned * comboMultiplier) - xpEarned);

        // Choose appropriate message
        if (perfect) {
            result.mainMessage = PERFECT_MESSAGES[random.nextInt(PERFECT_MESSAGES.length)];
            result.celebrationType = CelebrationType.PERFECT;
        } else {
            result.mainMessage = GOOD_MESSAGES[random.nextInt(GOOD_MESSAGES.length)];
            result.celebrationType = CelebrationType.GOOD;
        }

        // Add combo message if combo > 1
        if (combo > 1) {
            String comboTemplate = COMBO_MESSAGES[random.nextInt(COMBO_MESSAGES.length)];
            result.comboMessage = String.format(comboTemplate, combo);
        }

        // Add difficulty bonus message for hard/expert
        if ("Hard".equalsIgnoreCase(difficulty) || "Expert".equalsIgnoreCase(difficulty)) {
            result.difficultyMessage = DIFFICULTY_BONUSES[random.nextInt(DIFFICULTY_BONUSES.length)];
        }

        // Add streak milestone message at intervals
        if (streak > 0 && streak % 5 == 0) {
            String streakTemplate = STREAK_MILESTONES[random.nextInt(STREAK_MILESTONES.length)];
            result.streakMessage = String.format(streakTemplate, streak);
        }

        return result;
    }

    /**
     * Reset combo (e.g., on failure or timeout).
     */
    public void resetCombo() {
        prefs.edit().putInt(KEY_COMBO_COUNT, 0).apply();
    }

    /**
     * Get current combo count.
     */
    public int getCurrentCombo() {
        long now = System.currentTimeMillis();
        long lastSolve = prefs.getLong(KEY_LAST_SOLVE_TIME, 0);

        if (now - lastSolve > COMBO_TIMEOUT_MS) {
            return 0;
        }
        return prefs.getInt(KEY_COMBO_COUNT, 0);
    }

    /**
     * Animate a view with a celebratory pulse effect.
     */
    public static void animatePulse(View view) {
        if (view == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Animate a view with a bounce effect.
     */
    public static void animateBounce(View view) {
        if (view == null) return;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0f, -20f, 0f);
        translationY.setDuration(400);
        translationY.setInterpolator(new OvershootInterpolator());
        translationY.start();
    }

    /**
     * Animate XP gain with a count-up effect.
     */
    public static void animateXpGain(TextView textView, int startXp, int endXp, long duration) {
        if (textView == null) return;

        ObjectAnimator animator = ObjectAnimator.ofInt(startXp, endXp);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.format("+%d XP", value));
        });
        animator.start();
    }

    /**
     * Animate celebration message appearing with scale and fade.
     */
    public static void animateMessageAppear(View view, Runnable onComplete) {
        if (view == null) return;

        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.1f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());

        if (onComplete != null) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onComplete.run();
                }
            });
        }

        set.start();
    }

    /**
     * Animate combo counter with fire effect.
     */
    public static void animateCombo(View view) {
        if (view == null) return;

        // Rapid pulse effect for combo
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", -5f, 5f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.start();
    }

    /**
     * Play celebration sound based on type.
     */
    public void playCelebrationSound(CelebrationType type) {
        switch (type) {
            case PERFECT:
                soundManager.playSound(SoundManager.Sound.LEVEL_UP);
                break;
            case GOOD:
                soundManager.playSound(SoundManager.Sound.SUCCESS);
                break;
            case COMBO:
                soundManager.playSound(SoundManager.Sound.COMBO);
                break;
        }
    }

    /**
     * Celebration types for different feedback levels.
     */
    public enum CelebrationType {
        PERFECT,
        GOOD,
        COMBO
    }

    /**
     * Result container for celebration data.
     */
    public static class CelebrationResult {
        public String mainMessage = "";
        public String comboMessage = "";
        public String difficultyMessage = "";
        public String streakMessage = "";
        public int combo = 0;
        public int xpEarned = 0;
        public int comboBonus = 0;
        public int streak = 0;
        public CelebrationType celebrationType = CelebrationType.GOOD;

        public int getTotalXp() {
            return xpEarned + comboBonus;
        }

        public boolean hasCombo() {
            return combo > 1 && comboMessage != null && !comboMessage.isEmpty();
        }

        public boolean hasDifficultyBonus() {
            return difficultyMessage != null && !difficultyMessage.isEmpty();
        }

        public boolean hasStreakMilestone() {
            return streakMessage != null && !streakMessage.isEmpty();
        }
    }
}
