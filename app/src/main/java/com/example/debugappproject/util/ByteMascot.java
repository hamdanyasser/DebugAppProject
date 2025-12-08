package com.example.debugappproject.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - BYTE THE BUG MASCOT                               ║
 * ║           Your friendly debugging companion! 🐛                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Byte is a cute bug mascot who provides:
 * - Motivational messages
 * - Reactions to success/failure
 * - Tips and hints
 * - Personality to the game
 */
public class ByteMascot {

    private static final String PREFS_NAME = "byte_mascot_prefs";
    private static final String KEY_INTERACTION_COUNT = "interaction_count";
    private static final String KEY_LAST_GREETING = "last_greeting";

    private final Context context;
    private final SharedPreferences prefs;
    private final Random random = new Random();
    private MascotState currentState = MascotState.IDLE;

    // Mascot states
    public enum MascotState {
        IDLE,           // Default happy state
        EXCITED,        // On success or achievement
        ENCOURAGING,    // When player is struggling
        THINKING,       // When player is working on a bug
        CELEBRATING,    // On big achievements
        SLEEPY,         // Late night playing
        SURPRISED       // On combo or streak
    }

    // Greeting messages based on time of day
    private static final String[] MORNING_GREETINGS = {
        "🌅 Good morning, debugger! Ready to squash some bugs?",
        "☀️ Rise and shine! Today's code needs your magic!",
        "🌄 Morning! The bugs are awake, and so are we!",
        "☕ Coffee's ready, bugs are waiting! Let's go!"
    };

    private static final String[] AFTERNOON_GREETINGS = {
        "👋 Hey there! Time to debug!",
        "🔥 Afternoon debugging session! Let's do this!",
        "💪 Ready for some afternoon bug hunting?",
        "🎯 Perfect time to practice your skills!"
    };

    private static final String[] EVENING_GREETINGS = {
        "🌙 Evening, debugger! One more bug before bed?",
        "🌆 The night belongs to debuggers like us!",
        "✨ Evening session! Let's make it count!",
        "🦉 Night owl mode activated! Debug time!"
    };

    // Success messages (excited state)
    private static final String[] SUCCESS_MESSAGES = {
        "🎉 AMAZING! You crushed that bug!",
        "🚀 WOW! That was a clean fix!",
        "⭐ BRILLIANT! You're getting better!",
        "🏆 LEGENDARY! Bug didn't stand a chance!",
        "💎 PERFECT! That code is spotless now!",
        "🔥 ON FIRE! Keep this energy going!",
        "🎯 BULLSEYE! Right on target!",
        "✨ SPECTACULAR! You make it look easy!"
    };

    // Encouraging messages (when player fails)
    private static final String[] ENCOURAGEMENT_MESSAGES = {
        "🤔 Almost there! Try a different approach?",
        "💪 Don't give up! Every bug has a solution!",
        "🎯 You're close! Check your logic again?",
        "🌟 Mistakes help us learn! Try once more!",
        "🔍 Let's look at this from another angle!",
        "💡 Hint: Sometimes stepping back helps!",
        "🧩 Every puzzle has a solution! You got this!",
        "🌈 Even the best debuggers miss on first try!"
    };

    // Thinking messages (during puzzle solving)
    private static final String[] THINKING_MESSAGES = {
        "🤔 Hmm, interesting bug we have here...",
        "🔍 Let me see what's happening...",
        "💭 I wonder what's causing this...",
        "🧐 There's definitely something fishy...",
        "📚 This reminds me of a classic bug...",
        "🎓 Educational opportunity detected!"
    };

    // Celebration messages (big achievements)
    private static final String[] CELEBRATION_MESSAGES = {
        "🎊 INCREDIBLE ACHIEVEMENT UNLOCKED!",
        "🏅 You're becoming a debugging legend!",
        "🌟 Star performance! Absolutely stellar!",
        "👑 All hail the debugging champion!",
        "🎆 This calls for a celebration!",
        "🦸 Super debugger powers activated!"
    };

    // Combo messages (surprised state)
    private static final String[] COMBO_MESSAGES = {
        "😲 WHOA! That combo though!",
        "🔥 YOU'RE UNSTOPPABLE!",
        "⚡ LIGHTNING FAST FIXES!",
        "💥 COMBO MASTER!",
        "🌪️ Nothing can stop you now!"
    };

    // Tips and hints
    private static final String[] TIPS = {
        "💡 Tip: Read error messages carefully - they're clues!",
        "💡 Tip: Off-by-one errors hide in loops!",
        "💡 Tip: Check your variable names for typos!",
        "💡 Tip: Null checks can save you from crashes!",
        "💡 Tip: Compare operators: == vs === matters!",
        "💡 Tip: Array indices start at 0, not 1!",
        "💡 Tip: Strings are immutable in many languages!",
        "💡 Tip: Don't forget to handle edge cases!",
        "💡 Tip: Infinite loops often have wrong conditions!",
        "💡 Tip: Recursion needs a base case to stop!"
    };

    // Mission intro messages
    private static final String[] MISSION_INTROS = {
        "🎯 Agent, we have a critical bug report!",
        "📡 Incoming transmission: Bug detected in sector 7G!",
        "🚨 Priority alert! Code malfunction detected!",
        "🔔 Your debugging skills are needed, agent!",
        "📋 New mission brief: Locate and eliminate the bug!",
        "🎖️ Command here: We need our best debugger on this!"
    };

    // Story snippets for missions
    private static final String[][] MISSION_STORIES = {
        {"Chapter 1: The Syntax Error", "A rogue semicolon threatens the entire system..."},
        {"Chapter 2: Loop of Doom", "The infinite loop is consuming all resources!"},
        {"Chapter 3: Null Island", "A mysterious null has crashed the expedition..."},
        {"Chapter 4: Off By One", "The index is wrong, and chaos ensues!"},
        {"Chapter 5: Type Wars", "String meets Integer in an epic battle!"},
        {"Chapter 6: Memory Leak", "The bytes are escaping! Stop them!"},
        {"Chapter 7: Race Condition", "Two threads race for the same resource..."},
        {"Chapter 8: Regex Riddle", "The pattern must be deciphered!"},
        {"Chapter 9: API Anomaly", "The server responses are all wrong!"},
        {"Chapter 10: Final Debug", "The ultimate bug awaits..."}
    };

    public ByteMascot(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get a greeting message based on time of day.
     */
    public String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String[] greetings;

        if (hour >= 5 && hour < 12) {
            greetings = MORNING_GREETINGS;
        } else if (hour >= 12 && hour < 18) {
            greetings = AFTERNOON_GREETINGS;
        } else {
            greetings = EVENING_GREETINGS;
        }

        recordInteraction();
        return greetings[random.nextInt(greetings.length)];
    }

    /**
     * Get a success message.
     */
    public String getSuccessMessage() {
        currentState = MascotState.EXCITED;
        return SUCCESS_MESSAGES[random.nextInt(SUCCESS_MESSAGES.length)];
    }

    /**
     * Get an encouragement message for failure.
     */
    public String getEncouragementMessage() {
        currentState = MascotState.ENCOURAGING;
        return ENCOURAGEMENT_MESSAGES[random.nextInt(ENCOURAGEMENT_MESSAGES.length)];
    }

    /**
     * Get a thinking message.
     */
    public String getThinkingMessage() {
        currentState = MascotState.THINKING;
        return THINKING_MESSAGES[random.nextInt(THINKING_MESSAGES.length)];
    }

    /**
     * Get a celebration message for achievements.
     */
    public String getCelebrationMessage() {
        currentState = MascotState.CELEBRATING;
        return CELEBRATION_MESSAGES[random.nextInt(CELEBRATION_MESSAGES.length)];
    }

    /**
     * Get a combo message.
     */
    public String getComboMessage(int comboCount) {
        currentState = MascotState.SURPRISED;
        String base = COMBO_MESSAGES[random.nextInt(COMBO_MESSAGES.length)];
        return base + " x" + comboCount + " COMBO!";
    }

    /**
     * Get a random tip.
     */
    public String getRandomTip() {
        return TIPS[random.nextInt(TIPS.length)];
    }

    /**
     * Get a mission intro message.
     */
    public String getMissionIntro() {
        return MISSION_INTROS[random.nextInt(MISSION_INTROS.length)];
    }

    /**
     * Get a mission story snippet.
     */
    public String[] getMissionStory(int missionNumber) {
        int index = (missionNumber - 1) % MISSION_STORIES.length;
        return MISSION_STORIES[index];
    }

    /**
     * Get the current mascot state.
     */
    public MascotState getCurrentState() {
        return currentState;
    }

    /**
     * Set the mascot state.
     */
    public void setState(MascotState state) {
        this.currentState = state;
    }

    /**
     * Get emoji representation of current state.
     */
    public String getStateEmoji() {
        switch (currentState) {
            case EXCITED:
                return "🎉";
            case ENCOURAGING:
                return "💪";
            case THINKING:
                return "🤔";
            case CELEBRATING:
                return "🏆";
            case SLEEPY:
                return "😴";
            case SURPRISED:
                return "😲";
            case IDLE:
            default:
                return "🐛";
        }
    }

    /**
     * Get the mascot's name.
     */
    public String getName() {
        return "Byte";
    }

    /**
     * Get the mascot's full introduction.
     */
    public String getIntroduction() {
        return "👋 Hi! I'm Byte, your debugging buddy! " +
               "I'm here to help you become a debugging master! " +
               "Let's squash some bugs together! 🐛";
    }

    /**
     * Record an interaction for analytics.
     */
    private void recordInteraction() {
        int count = prefs.getInt(KEY_INTERACTION_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_INTERACTION_COUNT, count).apply();
    }

    /**
     * Get interaction count.
     */
    public int getInteractionCount() {
        return prefs.getInt(KEY_INTERACTION_COUNT, 0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Animate mascot image with bounce effect.
     */
    public static void animateBounce(View mascotView) {
        if (mascotView == null) return;

        ObjectAnimator bounceY = ObjectAnimator.ofFloat(mascotView, "translationY", 0f, -30f, 0f);
        bounceY.setDuration(600);
        bounceY.setInterpolator(new BounceInterpolator());
        bounceY.start();
    }

    /**
     * Animate mascot with excited wiggle.
     */
    public static void animateExcited(View mascotView) {
        if (mascotView == null) return;

        ObjectAnimator rotation = ObjectAnimator.ofFloat(mascotView, "rotation", -15f, 15f, -10f, 10f, -5f, 5f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mascotView, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mascotView, "scaleY", 1f, 1.2f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(rotation, scaleX, scaleY);
        set.setDuration(800);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Animate mascot thinking (subtle side-to-side).
     */
    public static void animateThinking(View mascotView) {
        if (mascotView == null) return;

        ObjectAnimator tilt = ObjectAnimator.ofFloat(mascotView, "rotation", 0f, 10f, 0f, -10f, 0f);
        tilt.setDuration(2000);
        tilt.setRepeatCount(ObjectAnimator.INFINITE);
        tilt.start();
    }

    /**
     * Animate mascot celebration (spinning).
     */
    public static void animateCelebration(View mascotView) {
        if (mascotView == null) return;

        ObjectAnimator spin = ObjectAnimator.ofFloat(mascotView, "rotation", 0f, 360f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mascotView, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mascotView, "scaleY", 1f, 1.5f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(spin, scaleX, scaleY);
        set.setDuration(1000);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Show mascot speech bubble with message.
     */
    public static void showSpeechBubble(TextView bubbleView, String message) {
        if (bubbleView == null) return;

        bubbleView.setText(message);
        bubbleView.setAlpha(0f);
        bubbleView.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(bubbleView, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 0.5f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 0.5f, 1.1f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleX, scaleY);
        set.setDuration(400);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    /**
     * Hide speech bubble with fade out.
     */
    public static void hideSpeechBubble(TextView bubbleView) {
        if (bubbleView == null) return;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(bubbleView, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                bubbleView.setVisibility(View.GONE);
            }
        });
        fadeOut.start();
    }
}
