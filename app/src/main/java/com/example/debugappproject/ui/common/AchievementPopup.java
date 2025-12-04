package com.example.debugappproject.ui.common;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.debugappproject.R;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.util.SoundManager;

/**
 * AchievementPopup - Displays celebratory achievement unlock notifications
 * 
 * Features:
 * - Animated entrance/exit
 * - Auto-dismiss after delay
 * - Sound effects and haptics
 * - XP reward display
 */
public class AchievementPopup {
    
    private static final long DISPLAY_DURATION_MS = 4000;
    private static final long ANIMATION_DURATION_MS = 500;
    
    private final Context context;
    private final SoundManager soundManager;
    private Dialog dialog;
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    public AchievementPopup(Context context) {
        this.context = context;
        this.soundManager = SoundManager.getInstance(context);
    }
    
    /**
     * Show achievement unlock popup.
     */
    public void show(AchievementDefinition achievement) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        
        // Create dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_achievement_unlock, null);
        dialog.setContentView(view);
        
        // Configure window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = 100;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            window.setAttributes(params);
        }
        
        // Bind views
        CardView card = view.findViewById(R.id.cardAchievement);
        TextView textIcon = view.findViewById(R.id.textAchievementIcon);
        TextView textTitle = view.findViewById(R.id.textAchievementTitle);
        TextView textName = view.findViewById(R.id.textAchievementName);
        TextView textXp = view.findViewById(R.id.textXpReward);
        
        // Set content
        textIcon.setText(achievement.getIconEmoji());
        textTitle.setText("Achievement Unlocked!");
        textName.setText(achievement.getName());
        textXp.setText("+" + achievement.getXpReward() + " XP");
        
        // Play sound and haptic
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        soundManager.vibrate(SoundManager.Haptic.SUCCESS);
        
        // Show dialog
        dialog.show();
        
        // Animate entrance
        animateEntrance(card);
        
        // Schedule dismissal
        handler.postDelayed(() -> {
            if (dialog != null && dialog.isShowing()) {
                animateExit(card, () -> {
                    dialog.dismiss();
                    dialog = null;
                });
            }
        }, DISPLAY_DURATION_MS);
        
        // Tap to dismiss
        card.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);
            animateExit(card, () -> {
                dialog.dismiss();
                dialog = null;
            });
        });
    }
    
    private void animateEntrance(View view) {
        view.setTranslationY(-200f);
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", -200f, 0f),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f),
            ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f)
        );
        animatorSet.setDuration(ANIMATION_DURATION_MS);
        animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
        animatorSet.start();
    }
    
    private void animateExit(View view, Runnable onComplete) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", 0f, -200f),
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f),
            ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f)
        );
        animatorSet.setDuration(300);
        animatorSet.start();
        
        handler.postDelayed(onComplete, 300);
    }
    
    /**
     * Dismiss the popup if showing.
     */
    public void dismiss() {
        handler.removeCallbacksAndMessages(null);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
