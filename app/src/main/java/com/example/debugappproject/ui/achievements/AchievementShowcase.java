package com.example.debugappproject.ui.achievements;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.debugappproject.R;
import com.example.debugappproject.util.SoundManager;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ACHIEVEMENT UNLOCK SHOWCASE                          ║
 * ║              Epic Full-Screen Achievement Celebration!                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class AchievementShowcase {

    public enum Rarity {
        COMMON("#6B7280", "Common"),
        RARE("#3B82F6", "Rare"),
        EPIC("#8B5CF6", "Epic"),
        LEGENDARY("#F59E0B", "Legendary"),
        MYTHIC("#EC4899", "Mythic");

        public final String color;
        public final String label;

        Rarity(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }

    private final Context context;
    private final SoundManager soundManager;
    private Dialog dialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    public AchievementShowcase(Context context) {
        this.context = context;
        this.soundManager = SoundManager.getInstance(context);
    }

    public void showAchievement(String title, String description, String emoji, 
                                 int xpReward, Rarity rarity, Runnable onDismiss) {
        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_achievement_showcase, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        
        setupViews(view, title, description, emoji, xpReward, rarity);
        dialog.show();
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        startAnimations(view);
        createConfetti(view.findViewById(R.id.confetti_container));
        
        view.setOnClickListener(v -> {
            soundManager.playButtonClick();
            dismiss();
            if (onDismiss != null) onDismiss.run();
        });
        
        handler.postDelayed(() -> {
            if (dialog != null && dialog.isShowing()) {
                dismiss();
                if (onDismiss != null) onDismiss.run();
            }
        }, 5000);
    }
    
    private void setupViews(View view, String title, String description, String emoji, int xpReward, Rarity rarity) {
        TextView iconView = view.findViewById(R.id.text_achievement_icon);
        if (iconView != null) iconView.setText(emoji);
        
        TextView titleView = view.findViewById(R.id.text_achievement_title);
        if (titleView != null) titleView.setText(title);
        
        TextView descView = view.findViewById(R.id.text_achievement_description);
        if (descView != null) descView.setText(description);
        
        TextView xpView = view.findViewById(R.id.text_xp_reward);
        if (xpView != null) xpView.setText("+" + xpReward + " XP");
        
        TextView rarityView = view.findViewById(R.id.text_rarity);
        if (rarityView != null) {
            rarityView.setText(rarity.label);
            try { rarityView.setTextColor(Color.parseColor(rarity.color)); } catch (Exception e) {}
        }
    }
    
    private void startAnimations(View rootView) {
        rootView.setAlpha(0f);
        ObjectAnimator.ofFloat(rootView, "alpha", 0f, 1f).setDuration(300).start();
        
        View card = rootView.findViewById(R.id.card_achievement);
        if (card != null) {
            card.setScaleX(0.5f);
            card.setScaleY(0.5f);
            card.setAlpha(0f);
            
            AnimatorSet cardAnim = new AnimatorSet();
            cardAnim.playTogether(
                ObjectAnimator.ofFloat(card, "scaleX", 0.5f, 1.1f, 1f),
                ObjectAnimator.ofFloat(card, "scaleY", 0.5f, 1.1f, 1f),
                ObjectAnimator.ofFloat(card, "alpha", 0f, 1f)
            );
            cardAnim.setDuration(600);
            cardAnim.setInterpolator(new OvershootInterpolator(1.5f));
            cardAnim.setStartDelay(200);
            cardAnim.start();
        }
        
        View icon = rootView.findViewById(R.id.text_achievement_icon);
        if (icon != null) {
            handler.postDelayed(() -> {
                ObjectAnimator bounce = ObjectAnimator.ofPropertyValuesHolder(icon,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f));
                bounce.setDuration(500);
                bounce.setInterpolator(new BounceInterpolator());
                bounce.start();
            }, 500);
        }
    }
    
    private void createConfetti(FrameLayout container) {
        if (container == null) return;
        
        int[] colors = {Color.parseColor("#FFD54F"), Color.parseColor("#FF6B35"), 
                       Color.parseColor("#7C4DFF"), Color.parseColor("#00E676"), 
                       Color.parseColor("#EC4899"), Color.parseColor("#06B6D4")};
        
        container.post(() -> {
            int width = container.getWidth();
            int height = container.getHeight();
            
            for (int i = 0; i < 50; i++) {
                View particle = new View(context);
                int size = 8 + random.nextInt(12);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
                params.leftMargin = random.nextInt(Math.max(width, 1));
                params.topMargin = -50;
                particle.setLayoutParams(params);
                particle.setBackgroundColor(colors[random.nextInt(colors.length)]);
                particle.setRotation(random.nextFloat() * 360);
                
                container.addView(particle);
                
                int duration = 2000 + random.nextInt(1500);
                float endX = (random.nextFloat() - 0.5f) * 300;
                int delay = i * 40;
                
                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(
                    ObjectAnimator.ofFloat(particle, "translationY", 0f, height + 100).setDuration(duration),
                    ObjectAnimator.ofFloat(particle, "translationX", 0f, endX).setDuration(duration),
                    ObjectAnimator.ofFloat(particle, "rotation", 0f, 720f).setDuration(duration)
                );
                anim.setStartDelay(delay);
                anim.start();
            }
        });
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        handler.removeCallbacksAndMessages(null);
    }
    
    public static void show(Context context, String title, String description, String emoji, int xp, Rarity rarity) {
        new AchievementShowcase(context).showAchievement(title, description, emoji, xp, rarity, null);
    }
}
