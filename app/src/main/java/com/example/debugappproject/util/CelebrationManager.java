package com.example.debugappproject.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - SUPER CELEBRATION MANAGER                          ║
 * ║                 Maximum Dopamine & Engagement System                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Combo tracking with multipliers
 * - Dynamic celebration messages
 * - Screen shake effects
 * - Particle-like floating text
 * - Color flash effects
 * - Victory animations
 * - Streak milestone celebrations
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
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ═══════════════════════════════════════════════════════════════════════════
    //                         CELEBRATION MESSAGES
    // ═══════════════════════════════════════════════════════════════════════════

    // Perfect solve messages (no hints, fast time)
    private static final String[] LEGENDARY_MESSAGES = {
        "⚡ LEGENDARY! ⚡",
        "🌟 GODLIKE! 🌟",
        "💎 FLAWLESS! 💎",
        "🔥 UNSTOPPABLE! 🔥",
        "⭐ SUPERSTAR! ⭐",
        "🏆 CHAMPION! 🏆",
        "👑 ROYALTY! 👑",
        "🎯 BULLSEYE! 🎯"
    };

    private static final String[] PERFECT_MESSAGES = {
        "Perfect Fix! 🎯",
        "Flawless Victory! ⚡",
        "Bug Annihilated! 💥",
        "Clean Execution! ✨",
        "Master Debugger! 🏆",
        "Code Surgeon! 🔬",
        "Zero Errors! 🎪",
        "Precision Strike! 🎯",
        "Surgical Precision! 🔪",
        "Pixel Perfect! ✅"
    };

    private static final String[] GOOD_MESSAGES = {
        "Nice Fix! 👍",
        "Bug Squashed! 🐛",
        "Well Done! ⭐",
        "Solid Work! 💪",
        "Got It! ✓",
        "Success! 🌟",
        "Nailed It! 🔨",
        "Good Job! 👏",
        "Sweet! 🍬",
        "Boom! 💥"
    };

    private static final String[] FAST_MESSAGES = {
        "⚡ LIGHTNING FAST! ⚡",
        "🏃 SPEED DEMON! 🏃",
        "⏱️ QUICK DRAW! ⏱️",
        "🚀 BLAZING! 🚀",
        "💨 ZOOM! 💨",
        "🏎️ TURBO MODE! 🏎️"
    };

    // Combo messages with intensity levels
    private static final String[] COMBO_2_5 = {
        "🔥 %dx COMBO! 🔥",
        "⚡ %d IN A ROW! ⚡",
        "💪 %d STREAK! 💪"
    };

    private static final String[] COMBO_6_9 = {
        "🔥🔥 %dx COMBO! 🔥🔥",
        "⚡⚡ %d STREAK! ⚡⚡",
        "💥 %dx MULTIPLIER! 💥",
        "🌟 %d AND COUNTING! 🌟"
    };

    private static final String[] COMBO_10_PLUS = {
        "🔥🔥🔥 %dx MEGA COMBO! 🔥🔥🔥",
        "⚡⚡⚡ FEVER MODE: %dx! ⚡⚡⚡",
        "💥💥💥 %dx UNSTOPPABLE! 💥💥💥",
        "🌟🌟🌟 %dx LEGENDARY! 🌟🌟🌟",
        "👑👑👑 %dx GODLIKE! 👑👑👑"
    };

    private static final String[] STREAK_MILESTONES = {
        "🔥 Day %d Streak!",
        "🌟 %d Days Strong!",
        "💪 %d Day Champion!",
        "🏆 %d Day Legend!",
        "👑 %d Day King!"
    };

    private static final String[] DIFFICULTY_BONUSES = {
        "💎 Hard Mode Mastered!",
        "🏆 Expert Challenge Complete!",
        "⭐ Difficulty Conquered!",
        "🔥 Hard Mode Bonus!",
        "💪 Tough Nut Cracked!"
    };

    // Encouragement messages for wrong answers
    private static final String[] ENCOURAGEMENT = {
        "Keep trying! 💪",
        "Almost there! 🎯",
        "Don't give up! 🔥",
        "You've got this! ⭐",
        "Next one's yours! 🏆"
    };

    public CelebrationManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.soundManager = SoundManager.getInstance(context);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         COMBO TRACKING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Record a successful bug solve and get celebration data.
     */
    public CelebrationResult recordSolve(boolean perfect, boolean fast, String difficulty,
                                         int xpEarned, int streak, int combo) {
        long now = System.currentTimeMillis();

        // Build celebration result
        CelebrationResult result = new CelebrationResult();
        result.combo = combo;
        result.xpEarned = xpEarned;
        result.streak = streak;
        result.wasPerfect = perfect;
        result.wasFast = fast;

        // Calculate combo bonus XP (5% per combo level, max 100%)
        double comboMultiplier = Math.min(1.0 + (combo * 0.05), 2.0);
        result.comboBonus = (int) ((xpEarned * comboMultiplier) - xpEarned);

        // Determine celebration type and message
        if (perfect && fast && combo >= 10) {
            result.mainMessage = LEGENDARY_MESSAGES[random.nextInt(LEGENDARY_MESSAGES.length)];
            result.celebrationType = CelebrationType.LEGENDARY;
        } else if (perfect && fast) {
            result.mainMessage = FAST_MESSAGES[random.nextInt(FAST_MESSAGES.length)];
            result.celebrationType = CelebrationType.FAST;
        } else if (perfect) {
            result.mainMessage = PERFECT_MESSAGES[random.nextInt(PERFECT_MESSAGES.length)];
            result.celebrationType = CelebrationType.PERFECT;
        } else if (fast) {
            result.mainMessage = FAST_MESSAGES[random.nextInt(FAST_MESSAGES.length)];
            result.celebrationType = CelebrationType.FAST;
        } else {
            result.mainMessage = GOOD_MESSAGES[random.nextInt(GOOD_MESSAGES.length)];
            result.celebrationType = CelebrationType.GOOD;
        }

        // Add combo message based on intensity
        if (combo >= 10) {
            String template = COMBO_10_PLUS[random.nextInt(COMBO_10_PLUS.length)];
            result.comboMessage = String.format(template, combo);
            result.isFeverMode = true;
        } else if (combo >= 6) {
            String template = COMBO_6_9[random.nextInt(COMBO_6_9.length)];
            result.comboMessage = String.format(template, combo);
        } else if (combo >= 2) {
            String template = COMBO_2_5[random.nextInt(COMBO_2_5.length)];
            result.comboMessage = String.format(template, combo);
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
     * Get an encouragement message after a wrong answer.
     */
    public String getEncouragementMessage() {
        return ENCOURAGEMENT[random.nextInt(ENCOURAGEMENT.length)];
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

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Animate a view with a celebratory pulse effect.
     */
    public static void animatePulse(View view) {
        if (view == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(350);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.start();
    }

    /**
     * Animate a view with a super bounce effect.
     */
    public static void animateSuperBounce(View view) {
        if (view == null) return;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0f, -40f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 0.9f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 0.9f, 1.1f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translationY, scaleX, scaleY);
        set.setDuration(600);
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }

    /**
     * Animate a view with a shake effect (for wrong answers).
     */
    public static void animateShake(View view) {
        if (view == null) return;

        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
            0, 15, -15, 15, -15, 10, -10, 5, -5, 0);
        shake.setDuration(500);
        shake.start();
    }

    /**
     * Animate XP gain with a count-up effect.
     */
    public static void animateXpGain(TextView textView, int startXp, int endXp, long duration) {
        if (textView == null) return;

        ValueAnimator animator = ValueAnimator.ofInt(startXp, endXp);
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

        view.setScaleX(0.3f);
        view.setScaleY(0.3f);
        view.setAlpha(0f);
        view.setRotation(-15f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", -15f, 5f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha, rotation);
        set.setDuration(500);
        set.setInterpolator(new OvershootInterpolator(1.5f));

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
    public static void animateCombo(View view, int comboLevel) {
        if (view == null) return;

        // More intense animation for higher combos
        float scale = 1f + (comboLevel * 0.03f);
        scale = Math.min(scale, 1.5f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale, 1f);

        // Add rotation for high combos
        float rotationAmount = comboLevel >= 5 ? 5f : 0f;
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", -rotationAmount, rotationAmount, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.start();
    }

    /**
     * Create a floating "+XP" text animation.
     */
    public static void animateFloatingText(ViewGroup parent, String text, int color, float startX, float startY) {
        if (parent == null) return;

        TextView floatingText = new TextView(parent.getContext());
        floatingText.setText(text);
        floatingText.setTextColor(color);
        floatingText.setTextSize(20);
        floatingText.setAlpha(1f);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        floatingText.setLayoutParams(params);
        floatingText.setX(startX);
        floatingText.setY(startY);

        parent.addView(floatingText);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(floatingText, "alpha", 1f, 0f);
        ObjectAnimator floatUp = ObjectAnimator.ofFloat(floatingText, "translationY", 0f, -150f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(floatingText, "scaleX", 1f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(floatingText, "scaleY", 1f, 1.3f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeOut, floatUp, scaleX, scaleY);
        set.setDuration(1000);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                parent.removeView(floatingText);
            }
        });
        set.start();
    }

    /**
     * Screen flash effect for victories.
     */
    public static void flashScreen(View rootView, int color, long duration) {
        if (rootView == null) return;

        int originalColor = Color.TRANSPARENT;

        ObjectAnimator flash = ObjectAnimator.ofArgb(rootView, "backgroundColor",
            originalColor, color, originalColor);
        flash.setDuration(duration);
        flash.start();
    }

    /**
     * Victory celebration animation sequence.
     */
    public void playVictoryCelebration(View view, CelebrationResult result) {
        // Play appropriate sound
        playCelebrationSound(result.celebrationType);

        // Animate the view
        if (result.celebrationType == CelebrationType.LEGENDARY) {
            animateSuperBounce(view);
        } else {
            animatePulse(view);
        }
    }

    /**
     * Play celebration sound based on type.
     */
    public void playCelebrationSound(CelebrationType type) {
        switch (type) {
            case LEGENDARY:
                soundManager.playSound(SoundManager.Sound.VICTORY);
                break;
            case PERFECT:
            case FAST:
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

    // ═══════════════════════════════════════════════════════════════════════════
    //                         DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Celebration types for different feedback levels.
     */
    public enum CelebrationType {
        LEGENDARY,  // Perfect + Fast + High Combo
        PERFECT,    // No hints used
        FAST,       // Under half time
        GOOD,       // Normal correct
        COMBO       // Combo bonus
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
        public boolean wasPerfect = false;
        public boolean wasFast = false;
        public boolean isFeverMode = false;
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

        public String getFullMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(mainMessage);

            if (comboMessage != null && !comboMessage.isEmpty()) {
                sb.append("\n").append(comboMessage);
            }

            if (isFeverMode) {
                sb.append("\n🌟 FEVER MODE ACTIVE! 🌟");
            }

            return sb.toString();
        }
    }
}
