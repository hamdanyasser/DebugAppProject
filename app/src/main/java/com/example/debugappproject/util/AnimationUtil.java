package com.example.debugappproject.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

/**
 * AnimationUtil - Utility class for common animations in DebugMaster.
 *
 * Provides:
 * - Spring animations for card interactions
 * - Scale animations for button presses
 * - Fade animations for visibility changes
 * - Progress bar animations with easing
 * - Shake animations for errors
 * - Pulse animations for attention
 *
 * All animations use Material Motion principles:
 * - Smooth, natural motion
 * - Appropriate duration (100-400ms for most interactions)
 * - Physics-based easing curves
 */
public class AnimationUtil {

    // Standard durations following Material Design guidelines
    private static final int DURATION_SHORT = 150;   // Quick interactions
    private static final int DURATION_MEDIUM = 250;  // Standard transitions
    private static final int DURATION_LONG = 350;    // Complex animations

    /**
     * Applies a spring-based press animation to a view.
     * Scales down slightly on press, then bounces back.
     *
     * Usage: Call this in an OnClickListener before handling the click.
     */
    public static void animatePress(View view, Runnable onComplete) {
        // Scale down
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(100);
        scaleDown.setInterpolator(new DecelerateInterpolator());

        // Scale up with overshoot
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(150);
        scaleUp.setInterpolator(new OvershootInterpolator(1.5f));

        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(scaleDown, scaleUp);
        fullAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        fullAnimation.start();
    }

    /**
     * Animates a card with a spring effect (scale + elevation).
     * Perfect for card clicks and selections.
     */
    public static void animateCardPress(View card) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.97f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.97f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(card, "translationZ", 0f, 8f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, elevation);
        animatorSet.setDuration(DURATION_MEDIUM);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Fades a view in with a scale animation.
     * Makes the view visible with a smooth entrance.
     */
    public static void fadeInWithScale(View view) {
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.setDuration(DURATION_LONG);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Fades a view out with a scale animation.
     * Hides the view with a smooth exit.
     */
    public static void fadeOutWithScale(View view, Runnable onComplete) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.setDuration(DURATION_MEDIUM);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        animatorSet.start();
    }

    /**
     * Animates a progress bar smoothly to a target value.
     * Uses easing for natural progression.
     *
     * @param progressBar The progress bar to animate
     * @param targetProgress Target progress (0-100)
     * @param duration Animation duration in milliseconds
     */
    public static void animateProgress(ProgressBar progressBar, int targetProgress, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(progressBar.getProgress(), targetProgress);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });
        animator.start();
    }

    /**
     * Shakes a view horizontally to indicate an error.
     * Perfect for invalid inputs or failed validations.
     */
    public static void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, -25, 25, -25, 25, -15, 15, -5, 5, 0);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * Creates a pulse animation to draw attention to a view.
     * Scales up and down continuously.
     */
    public static Animator pulseAnimation(View view) {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(400);
        scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(400);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet pulse = new AnimatorSet();
        pulse.playSequentially(scaleUp, scaleDown);
        pulse.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.start(); // Loop
            }
        });

        return pulse;
    }

    /**
     * Bounces a view using BounceInterpolator.
     * Great for success states and achievements.
     */
    public static void bounceView(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new BounceInterpolator());
        animatorSet.start();
    }

    /**
     * Slides a view in from the bottom.
     * Good for modals and bottom sheets.
     */
    public static void slideInFromBottom(View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0);
        animator.setDuration(DURATION_LONG);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * Slides a view out to the bottom.
     */
    public static void slideOutToBottom(View view, Runnable onComplete) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, view.getHeight());
        animator.setDuration(DURATION_LONG);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        animator.start();
    }

    /**
     * Rotates a view 360 degrees.
     * Can be used for refresh animations or success indicators.
     */
    public static void rotate360(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Creates a level-up celebration animation.
     * Combines scale, rotation, and fade effects.
     */
    public static void celebrateLevelUp(View view, Runnable onComplete) {
        view.setAlpha(0f);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setRotation(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f, 1f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f, 0.8f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, scaleX, scaleY, rotation);
        animatorSet.setDuration(1500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        animatorSet.start();
    }

    /**
     * Simple fade in animation.
     */
    public static void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                .setDuration(DURATION_MEDIUM)
                .start();
    }

    /**
     * Simple fade out animation.
     */
    public static void fadeOut(View view, Runnable onComplete) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(DURATION_MEDIUM);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        animator.start();
    }
}
