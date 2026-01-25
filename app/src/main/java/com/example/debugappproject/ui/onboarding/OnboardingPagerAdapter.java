package com.example.debugappproject.ui.onboarding;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.debugmaster.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ANIMATED ONBOARDING PAGER                            ║
 * ║              World-Class Onboarding Experience                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final Context context;
    private final int[] layouts = {
        R.layout.onboarding_screen_1,
        R.layout.onboarding_screen_2,
        R.layout.onboarding_screen_3,
        R.layout.onboarding_screen_4
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Start animations when page is bound
        holder.startAnimations();
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        
        private Handler handler = new Handler(Looper.getMainLooper());
        private List<ObjectAnimator> runningAnimators = new ArrayList<>();
        
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        
        public void startAnimations() {
            // Stop any running animations
            stopAnimations();
            
            // Find views
            View glowRing = itemView.findViewById(R.id.glow_ring);
            View iconContainer = itemView.findViewById(R.id.icon_container);
            View textIcon = itemView.findViewById(R.id.text_icon);
            View textTitle = itemView.findViewById(R.id.text_title);
            View textSubtitle = itemView.findViewById(R.id.text_subtitle);
            View textDescription = itemView.findViewById(R.id.text_description);
            
            // Reset views
            if (glowRing != null) {
                glowRing.setAlpha(0f);
                glowRing.setScaleX(0.5f);
                glowRing.setScaleY(0.5f);
            }
            if (iconContainer != null) {
                iconContainer.setAlpha(0f);
                iconContainer.setScaleX(0.5f);
                iconContainer.setScaleY(0.5f);
            }
            if (textTitle != null) {
                textTitle.setAlpha(0f);
                textTitle.setTranslationY(30f);
            }
            if (textSubtitle != null) {
                textSubtitle.setAlpha(0f);
                textSubtitle.setTranslationY(20f);
            }
            if (textDescription != null) {
                textDescription.setAlpha(0f);
                textDescription.setTranslationY(20f);
            }
            
            // Animate glow ring
            if (glowRing != null) {
                handler.postDelayed(() -> {
                    animateGlowRing(glowRing);
                }, 100);
            }
            
            // Animate icon container
            if (iconContainer != null) {
                handler.postDelayed(() -> {
                    animateIconContainer(iconContainer);
                }, 200);
            }
            
            // Bounce icon
            if (textIcon != null) {
                handler.postDelayed(() -> {
                    animateIcon(textIcon);
                }, 500);
            }
            
            // Animate title
            if (textTitle != null) {
                handler.postDelayed(() -> {
                    animateTextIn(textTitle);
                }, 400);
            }
            
            // Animate subtitle
            if (textSubtitle != null) {
                handler.postDelayed(() -> {
                    animateTextIn(textSubtitle);
                }, 550);
            }
            
            // Animate description
            if (textDescription != null) {
                handler.postDelayed(() -> {
                    animateTextIn(textDescription);
                }, 700);
            }
        }
        
        private void animateGlowRing(View view) {
            // Entrance
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 0.8f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);
            
            AnimatorSet entrance = new AnimatorSet();
            entrance.playTogether(fadeIn, scaleX, scaleY);
            entrance.setDuration(600);
            entrance.setInterpolator(new OvershootInterpolator(1.2f));
            entrance.start();
            
            // Continuous rotation
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
            rotation.setDuration(20000);
            rotation.setRepeatCount(ValueAnimator.INFINITE);
            rotation.setInterpolator(new LinearInterpolator());
            rotation.start();
            runningAnimators.add(rotation);
            
            // Pulse
            ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0.8f, 0.5f, 0.8f));
            pulse.setDuration(3000);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setInterpolator(new AccelerateDecelerateInterpolator());
            pulse.setStartDelay(600);
            pulse.start();
            runningAnimators.add(pulse);
        }
        
        private void animateIconContainer(View view) {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.1f, 1f);
            
            AnimatorSet entrance = new AnimatorSet();
            entrance.playTogether(fadeIn, scaleX, scaleY);
            entrance.setDuration(500);
            entrance.setInterpolator(new OvershootInterpolator(1.5f));
            entrance.start();
        }
        
        private void animateIcon(View view) {
            // Bounce animation
            ObjectAnimator bounce = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("translationY", 0f, -15f, 0f));
            bounce.setDuration(1500);
            bounce.setRepeatCount(ValueAnimator.INFINITE);
            bounce.setInterpolator(new AccelerateDecelerateInterpolator());
            bounce.start();
            runningAnimators.add(bounce);
        }
        
        private void animateTextIn(View view) {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", 
                view.getTranslationY(), 0f);
            
            AnimatorSet anim = new AnimatorSet();
            anim.playTogether(fadeIn, slideUp);
            anim.setDuration(400);
            anim.setInterpolator(new DecelerateInterpolator(2f));
            anim.start();
        }
        
        public void stopAnimations() {
            handler.removeCallbacksAndMessages(null);
            for (ObjectAnimator animator : runningAnimators) {
                if (animator != null && animator.isRunning()) {
                    animator.cancel();
                }
            }
            runningAnimators.clear();
        }
    }
}
