package com.example.debugappproject.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.example.debugappproject.databinding.FragmentSplashBinding;
import com.example.debugappproject.ui.onboarding.OnboardingActivity;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.DailyRewardManager;
import com.example.debugappproject.util.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ULTRA PREMIUM GAME SPLASH SCREEN                     ║
 * ║                 AAA Mobile Game Quality Experience                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * FEATURES:
 * ─────────────────────────────────────────────────────────────────────────────────
 * • Matrix-style falling code rain effect
 * • Pulsating energy wave rings around logo
 * • Dramatic logo entrance with rotation & scale
 * • Split text animation (DEBUG + MASTER) with glitch effects
 * • Typing cursor animation
 * • Cyber-styled loading bar with status messages
 * • Floating energy particles with parallax
 * • Premium "PLAY" button with glow pulse - WAITS FOR USER INPUT
 * • Hexagon grid pattern background animation
 * • Sound effects for immersive experience
 */
public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;
    private Handler mainHandler;
    private Handler animationHandler;
    private SoundManager soundManager;
    private DailyRewardManager rewardManager;
    private boolean reduceMotion = false;

    private volatile boolean databaseSeeded = false;
    private volatile boolean loadingAnimationComplete = false;
    private volatile boolean userTappedStart = false;
    private volatile boolean buttonReady = false;

    private List<View> particles = new ArrayList<>();
    private List<View> codeRainColumns = new ArrayList<>();
    private List<Animator> runningAnimators = new ArrayList<>();
    private Random random = new Random();

    // Loading status messages for terminal effect
    private static final String[] LOADING_MESSAGES = {
            "> Initializing core systems...",
            "> Loading bug database...",
            "> Preparing challenges...",
            "> Syncing achievements...",
            "> Optimizing experience...",
            "> Finalizing setup...",
            "> Ready to debug!"
    };

    // Animation timing constants (in ms)
    private static final int PATTERN_DELAY = 0;
    private static final int PARTICLES_DELAY = 0;
    private static final int CODE_RAIN_DELAY = 100;
    private static final int DECORATIONS_DELAY = 100;
    private static final int WAVE_3_DELAY = 200;
    private static final int WAVE_2_DELAY = 400;
    private static final int WAVE_1_DELAY = 600;
    private static final int LOGO_GLOW_DELAY = 800;
    private static final int LOGO_DELAY = 1000;
    private static final int DEBUG_TEXT_DELAY = 1400;
    private static final int MASTER_TEXT_DELAY = 1700;
    private static final int TAGLINE_DELAY = 2000;
    private static final int LOADING_DELAY = 2400;
    private static final int PROGRESS_DURATION = 1800;
    private static final int START_BUTTON_DELAY = 4200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());
        animationHandler = new Handler(Looper.getMainLooper());
        
        // Initialize managers
        soundManager = SoundManager.getInstance(requireContext());
        rewardManager = new DailyRewardManager(requireContext());
        reduceMotion = rewardManager.shouldReduceMotion();

        setupStartButton();
        setupSkipFunctionality();
        seedDatabase();
        
        // If reduce motion is enabled, show static screen immediately
        if (reduceMotion) {
            showStaticWelcome();
        } else {
            startAnimationSequence();
        }
    }

    /**
     * Shows static welcome screen for reduce motion / accessibility
     */
    private void showStaticWelcome() {
        if (binding == null || !isAdded()) return;
        
        // Show all elements immediately without animation
        binding.hexPattern1.setAlpha(0.08f);
        binding.hexPattern2.setAlpha(0.08f);
        binding.energyWave1.setAlpha(0.5f);
        binding.energyWave2.setAlpha(0.5f);
        binding.energyWave3.setAlpha(0.5f);
        binding.logoGlowPulse.setAlpha(0.6f);
        binding.imageLogo.setAlpha(1f);
        binding.textDebug.setAlpha(1f);
        binding.textDebug.setTranslationX(0);
        binding.textMaster.setAlpha(1f);
        binding.textMaster.setTranslationX(0);
        binding.textTagline.setAlpha(1f);
        binding.textVersion.setAlpha(0.5f);
        binding.textCopyright.setAlpha(0.3f);
        
        // Show reward card and button immediately
        showRewardCard();
        showStartButtonStatic();
    }
    
    /**
     * Shows start button without animation for reduce motion
     */
    private void showStartButtonStatic() {
        if (binding == null || !isAdded()) return;
        
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.btnStart.setAlpha(1f);
        binding.btnStart.setScaleX(1f);
        binding.btnStart.setScaleY(1f);
        binding.btnStartGlow.setVisibility(View.VISIBLE);
        binding.btnStartGlow.setAlpha(0.4f);
        binding.textTapHint.setVisibility(View.VISIBLE);
        binding.textTapHint.setAlpha(0.7f);
        binding.textTapHint.setText("[ TAP TO START ]");
        buttonReady = true;
    }

    /**
     * Sets up skip functionality - tap anywhere or skip button
     */
    private void setupSkipFunctionality() {
        // Skip button in top-right
        binding.btnSkip.setOnClickListener(v -> skipToReady());
        
        // Tap anywhere overlay to skip
        binding.overlayTapSkip.setOnClickListener(v -> skipToReady());
        
        // Show skip button after a short delay
        animationHandler.postDelayed(() -> {
            if (binding != null && isAdded() && !reduceMotion) {
                binding.btnSkip.setVisibility(View.VISIBLE);
                binding.overlayTapSkip.setVisibility(View.VISIBLE);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.btnSkip, "alpha", 0f, 0.6f);
                fadeIn.setDuration(300);
                fadeIn.start();
            }
        }, 500);
    }
    
    /**
     * Skips animation and shows ready state immediately
     */
    private void skipToReady() {
        if (buttonReady) return; // Already at ready state
        
        // Cancel all running animations
        for (Animator animator : runningAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        runningAnimators.clear();
        animationHandler.removeCallbacksAndMessages(null);
        
        // Show everything immediately
        showStaticWelcome();
        
        // Hide skip button
        binding.btnSkip.setVisibility(View.GONE);
        binding.overlayTapSkip.setVisibility(View.GONE);
    }

    /**
     * Sets up the PLAY button click listener - NO AUTO NAVIGATION!
     * User MUST tap to proceed
     */
    private void setupStartButton() {
        binding.btnStart.setOnClickListener(v -> {
            if (!userTappedStart && buttonReady && databaseSeeded) {
                userTappedStart = true;
                // Haptic feedback on play tap
                rewardManager.triggerPlayHaptic();
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                animateButtonPress(v, this::navigateToNextScreen);
            } else if (!databaseSeeded) {
                // Show loading message if database not ready
                binding.textTapHint.setText("[ LOADING... PLEASE WAIT ]");
                soundManager.playSound(SoundManager.Sound.ERROR);
            }
        });
        
        // Also handle tap hint area
        binding.textTapHint.setOnClickListener(v -> binding.btnStart.performClick());
    }

    /**
     * Epic button press animation with sound
     */
    private void animateButtonPress(View button, Runnable onComplete) {
        // Flash effect
        ObjectAnimator flash = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.5f, 1f);
        flash.setDuration(100);

        // Scale down with haptic feedback feel
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.9f);

        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleDownX, scaleDownY, flash);
        pressDown.setDuration(100);

        // Scale up with overshoot then fade out
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1.3f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1.3f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f);
        
        // Also fade out glow
        ObjectAnimator glowFade = ObjectAnimator.ofFloat(binding.btnStartGlow, "alpha", 0.4f, 0f);
        ObjectAnimator glowScale = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleX", 1f, 2f);
        ObjectAnimator glowScaleY = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleY", 1f, 2f);

        AnimatorSet release = new AnimatorSet();
        release.playTogether(scaleUpX, scaleUpY, fadeOut, glowFade, glowScale, glowScaleY);
        release.setDuration(400);
        release.setInterpolator(new AccelerateInterpolator());

        // Fade out entire screen
        ObjectAnimator screenFade = ObjectAnimator.ofFloat(binding.splashRoot, "alpha", 1f, 0f);
        screenFade.setDuration(300);

        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(pressDown, release, screenFade);
        fullAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) onComplete.run();
            }
        });
        fullAnimation.start();
        runningAnimators.add(fullAnimation);
    }

    /**
     * Seeds the database on background thread
     */
    private void seedDatabase() {
        new Thread(() -> {
            try {
                BugRepository repository = new BugRepository(requireActivity().getApplication());
                DatabaseSeeder.seedDatabase(requireContext(), repository);
                databaseSeeded = true;
                
                // Update UI on main thread if button is waiting
                mainHandler.post(() -> {
                    if (binding != null && buttonReady) {
                        binding.textTapHint.setText("[ TAP TO START ]");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                databaseSeeded = true;
            }
        }).start();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                    MAIN ANIMATION SEQUENCE ORCHESTRATOR
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void startAnimationSequence() {
        // Background effects
        animationHandler.postDelayed(this::animateHexPatterns, PATTERN_DELAY);
        animationHandler.postDelayed(this::createFloatingParticles, PARTICLES_DELAY);
        animationHandler.postDelayed(this::createCodeRain, CODE_RAIN_DELAY);
        animationHandler.postDelayed(this::animateDecorations, DECORATIONS_DELAY);

        // Energy wave rings (outer to inner)
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave3, 20000, false), WAVE_3_DELAY);
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave2, 15000, true), WAVE_2_DELAY);
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave1, 12000, false), WAVE_1_DELAY);

        // Logo section
        animationHandler.postDelayed(this::animateLogoGlow, LOGO_GLOW_DELAY);
        animationHandler.postDelayed(this::animateLogo, LOGO_DELAY);

        // Text section with glitch effects
        animationHandler.postDelayed(this::animateDebugText, DEBUG_TEXT_DELAY);
        animationHandler.postDelayed(this::animateMasterText, MASTER_TEXT_DELAY);
        animationHandler.postDelayed(this::animateTagline, TAGLINE_DELAY);

        // Loading section
        animationHandler.postDelayed(this::animateLoadingSection, LOADING_DELAY);

        // Start button - NO AUTO NAVIGATION
        animationHandler.postDelayed(this::showStartButton, START_BUTTON_DELAY);
    }

    /**
     * Animates hexagon grid patterns
     */
    private void animateHexPatterns() {
        if (binding == null || !isAdded()) return;

        View[] patterns = {binding.hexPattern1, binding.hexPattern2};

        for (int i = 0; i < patterns.length; i++) {
            View pattern = patterns[i];
            if (pattern == null) continue;

            // Fade in
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pattern, "alpha", 0f, 0.08f);
            fadeIn.setDuration(2000);
            fadeIn.setStartDelay(i * 500);
            fadeIn.start();
            runningAnimators.add(fadeIn);

            // Subtle rotation
            float startRotation = pattern.getRotation();
            ObjectAnimator rotate = ObjectAnimator.ofFloat(pattern, "rotation", 
                    startRotation, startRotation + (i == 0 ? 5f : -5f));
            rotate.setDuration(30000);
            rotate.setRepeatCount(ValueAnimator.INFINITE);
            rotate.setRepeatMode(ValueAnimator.REVERSE);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.start();
            runningAnimators.add(rotate);
        }
    }

    /**
     * Creates Matrix-style falling code rain effect
     */
    private void createCodeRain() {
        if (binding == null || !isAdded()) return;

        FrameLayout container = binding.codeRainContainer;
        container.post(() -> {
            if (binding == null || !isAdded()) return;

            int containerWidth = container.getWidth() > 0 ? container.getWidth() : 1080;
            int columnCount = 15;
            int columnWidth = containerWidth / columnCount;

            String[] codeChars = {"0", "1", "{", "}", ";", "=", "+", "-", "*", "/", 
                    "if", "for", "int", "}", "{", "null", "==", "!=", "&&", "||"};

            for (int i = 0; i < columnCount; i++) {
                final int columnIndex = i;
                animationHandler.postDelayed(() -> {
                    if (binding == null || !isAdded()) return;
                    createCodeColumn(container, columnIndex * columnWidth, codeChars);
                }, i * 100);
            }
        });
    }

    /**
     * Creates a single column of falling code
     */
    private void createCodeColumn(FrameLayout container, int xPosition, String[] chars) {
        if (binding == null || !isAdded()) return;

        TextView codeText = new TextView(requireContext());
        codeText.setTextColor(Color.parseColor("#308B5CF6"));
        codeText.setTextSize(10);
        codeText.setAlpha(0f);

        StringBuilder code = new StringBuilder();
        int charCount = 8 + random.nextInt(8);
        for (int j = 0; j < charCount; j++) {
            code.append(chars[random.nextInt(chars.length)]).append("\n");
        }
        codeText.setText(code.toString());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = xPosition;
        params.topMargin = -500;
        codeText.setLayoutParams(params);

        container.addView(codeText);
        codeRainColumns.add(codeText);

        animateCodeFall(codeText, container.getHeight());
    }

    /**
     * Animates code falling down
     */
    private void animateCodeFall(View codeView, int containerHeight) {
        if (binding == null || !isAdded()) return;

        int duration = 8000 + random.nextInt(4000);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(codeView, "alpha", 0f, 0.4f);
        fadeIn.setDuration(1000);

        ObjectAnimator fall = ObjectAnimator.ofFloat(codeView, "translationY", 0f, containerHeight + 500);
        fall.setDuration(duration);
        fall.setInterpolator(new LinearInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(codeView, "alpha", 0.4f, 0f);
        fadeOut.setDuration(1000);
        fadeOut.setStartDelay(duration - 1000);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, fall, fadeOut);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding != null && isAdded()) {
                    codeView.setTranslationY(0);
                    animateCodeFall(codeView, containerHeight);
                }
            }
        });
        animSet.start();
        runningAnimators.add(animSet);
    }

    /**
     * Creates floating energy particles
     */
    private void createFloatingParticles() {
        if (binding == null || !isAdded()) return;

        FrameLayout container = binding.particlesContainer;
        container.post(() -> {
            if (binding == null || !isAdded()) return;

            int containerWidth = container.getWidth() > 0 ? container.getWidth() : 1080;
            int containerHeight = container.getHeight() > 0 ? container.getHeight() : 1920;
            int particleCount = 25;

            int[] colors = {
                    Color.parseColor("#A78BFA"),
                    Color.parseColor("#EC4899"),
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#8B5CF6"),
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#06B6D4")
            };

            for (int i = 0; i < particleCount; i++) {
                ImageView particle = new ImageView(requireContext());
                particle.setImageResource(R.drawable.particle_energy);

                int sizeDp = 6 + random.nextInt(14);
                int sizePx = dpToPx(sizeDp);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
                params.leftMargin = random.nextInt(containerWidth);
                params.topMargin = random.nextInt(containerHeight);
                particle.setLayoutParams(params);
                particle.setAlpha(0f);
                particle.setColorFilter(colors[random.nextInt(colors.length)]);

                container.addView(particle);
                particles.add(particle);

                final int index = i;
                animationHandler.postDelayed(() -> animateParticle(particle, containerHeight), index * 120);
            }
        });
    }

    /**
     * Animates a single particle floating up
     */
    private void animateParticle(View particle, int containerHeight) {
        if (binding == null || !isAdded()) return;

        float maxAlpha = 0.2f + random.nextFloat() * 0.5f;
        float driftX = (random.nextFloat() - 0.5f) * 150f;
        int duration = 5000 + random.nextInt(4000);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(particle, "alpha", 0f, maxAlpha);
        fadeIn.setDuration(1500);

        ObjectAnimator moveY = ObjectAnimator.ofFloat(particle, "translationY", 0f, -containerHeight * 0.6f);
        moveY.setDuration(duration);
        moveY.setInterpolator(new LinearInterpolator());

        ObjectAnimator moveX = ObjectAnimator.ofFloat(particle, "translationX", 0f, driftX);
        moveX.setDuration(duration);

        ObjectAnimator scale = ObjectAnimator.ofFloat(particle, "scaleX", 1f, 0.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(particle, "scaleY", 1f, 0.3f);
        scale.setDuration(duration);
        scaleY.setDuration(duration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(particle, "alpha", maxAlpha, 0f);
        fadeOut.setDuration(1500);
        fadeOut.setStartDelay(duration - 1500);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, moveY, moveX, scale, scaleY, fadeOut);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding != null && isAdded()) {
                    particle.setTranslationX(0);
                    particle.setTranslationY(0);
                    particle.setScaleX(1f);
                    particle.setScaleY(1f);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) particle.getLayoutParams();
                    int containerWidth = binding.particlesContainer.getWidth();
                    params.leftMargin = random.nextInt(containerWidth > 0 ? containerWidth : 1080);
                    params.topMargin = containerHeight;
                    particle.setLayoutParams(params);
                    animateParticle(particle, containerHeight);
                }
            }
        });
        animSet.start();
        runningAnimators.add(animSet);
    }

    /**
     * Animates decorative blobs
     */
    private void animateDecorations() {
        if (binding == null || !isAdded()) return;

        View[] decorations = {
                binding.decorationTopLeft,
                binding.decorationTopRight,
                binding.decorationBottomLeft,
                binding.decorationBottomRight
        };

        for (int i = 0; i < decorations.length; i++) {
            View decoration = decorations[i];
            if (decoration == null) continue;

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(decoration, "alpha", 0f, 0.12f);
            fadeIn.setDuration(2000);
            fadeIn.setStartDelay(i * 150);
            fadeIn.start();
            runningAnimators.add(fadeIn);

            // Floating animation
            ObjectAnimator floatAnim = ObjectAnimator.ofFloat(decoration, "translationY", 0f, -25f, 0f);
            floatAnim.setDuration(5000 + i * 500);
            floatAnim.setRepeatCount(ValueAnimator.INFINITE);
            floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            floatAnim.start();
            runningAnimators.add(floatAnim);

            // Subtle scale pulse
            ObjectAnimator scalePulse = ObjectAnimator.ofPropertyValuesHolder(decoration,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f));
            scalePulse.setDuration(4000 + i * 300);
            scalePulse.setRepeatCount(ValueAnimator.INFINITE);
            scalePulse.start();
            runningAnimators.add(scalePulse);
        }
    }

    /**
     * Animates energy wave rings
     */
    private void animateEnergyWave(View wave, int rotationDuration, boolean counterClockwise) {
        if (binding == null || !isAdded() || wave == null) return;

        // Fade in + scale entrance
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(wave, "alpha", 0f, 0.7f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(wave, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(wave, "scaleY", 0.3f, 1f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(fadeIn, scaleX, scaleY);
        entrance.setDuration(800);
        entrance.setInterpolator(new DecelerateInterpolator(2f));
        entrance.start();
        runningAnimators.add(entrance);

        // Continuous rotation
        float endRotation = counterClockwise ? -360f : 360f;
        ObjectAnimator rotation = ObjectAnimator.ofFloat(wave, "rotation", 0f, endRotation);
        rotation.setDuration(rotationDuration);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());
        rotation.start();
        runningAnimators.add(rotation);

        // Pulsing effect
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(wave,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.08f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.08f, 1f));
        pulse.setDuration(2500);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.setStartDelay(800);
        pulse.start();
        runningAnimators.add(pulse);
    }

    /**
     * Animates logo glow pulse
     */
    private void animateLogoGlow() {
        if (binding == null || !isAdded()) return;

        View glow = binding.logoGlowPulse;

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(glow, "alpha", 0f, 0.8f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(glow, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(glow, "scaleY", 0.5f, 1f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(fadeIn, scaleX, scaleY);
        entrance.setDuration(600);
        entrance.setInterpolator(new OvershootInterpolator(1.5f));
        entrance.start();
        runningAnimators.add(entrance);

        // Counter-rotation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(glow, "rotation", 0f, -360f);
        rotation.setDuration(10000);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());
        rotation.start();
        runningAnimators.add(rotation);
    }

    /**
     * Animates the main logo with epic entrance + SOUND
     */
    private void animateLogo() {
        if (binding == null || !isAdded()) return;

        ImageView logo = binding.imageLogo;
        
        // Play logo whoosh sound for epic entrance
        soundManager.playSound(SoundManager.Sound.LOGO_WHOOSH);

        // Epic entrance
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1.15f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logo, "rotation", -180f, 0f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(scaleX, scaleY, fadeIn, rotation);
        entrance.setDuration(1200);
        entrance.setInterpolator(new AnticipateOvershootInterpolator(0.8f));
        entrance.start();
        runningAnimators.add(entrance);

        // Breathing effect
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;

            ObjectAnimator breathe = ObjectAnimator.ofPropertyValuesHolder(logo,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.06f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.06f, 1f));
            breathe.setDuration(2500);
            breathe.setRepeatCount(ValueAnimator.INFINITE);
            breathe.setInterpolator(new AccelerateDecelerateInterpolator());
            breathe.start();
            runningAnimators.add(breathe);
        }, 1200);
    }

    /**
     * Animates "DEBUG" text with glitch effect + SOUND
     */
    private void animateDebugText() {
        if (binding == null || !isAdded()) return;

        TextView text = binding.textDebug;

        // Slide in from left
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(text, "translationX", -100f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(slideIn, fadeIn);
        entrance.setDuration(500);
        entrance.setInterpolator(new DecelerateInterpolator(2f));
        entrance.start();
        runningAnimators.add(entrance);

        // Glitch effect (no sound)
        animationHandler.postDelayed(() -> playGlitchEffect(text), 500);
    }

    /**
     * Animates "MASTER" text with glitch effect
     */
    private void animateMasterText() {
        if (binding == null || !isAdded()) return;

        TextView text = binding.textMaster;

        // Slide in from right
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(text, "translationX", 100f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f);

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(slideIn, fadeIn);
        entrance.setDuration(500);
        entrance.setInterpolator(new DecelerateInterpolator(2f));
        entrance.start();
        runningAnimators.add(entrance);

        // Glitch effect (no sound)
        animationHandler.postDelayed(() -> playGlitchEffect(text), 500);
    }

    /**
     * Plays a glitch effect on text
     */
    private void playGlitchEffect(View view) {
        if (binding == null || !isAdded()) return;

        // Quick horizontal shake
        ObjectAnimator glitch = ObjectAnimator.ofFloat(view, "translationX", 0f, 3f, -3f, 2f, -2f, 0f);
        glitch.setDuration(150);
        glitch.start();
        runningAnimators.add(glitch);
    }

    /**
     * Animates tagline with typing effect + SOUND
     */
    private void animateTagline() {
        if (binding == null || !isAdded()) return;

        TextView tagline = binding.textTagline;
        View cursor = binding.typingCursor;

        String fullText = "[ LEARN • DEBUG • MASTER ]";
        tagline.setText("");

        // Show cursor first
        cursor.setAlpha(1f);
        startCursorBlink(cursor);

        // Type out text (no sounds - simplified)
        final int[] charIndex = {0};
        final Runnable typeRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null || !isAdded()) return;
                if (charIndex[0] < fullText.length()) {
                    tagline.setText(fullText.substring(0, charIndex[0] + 1));
                    tagline.setAlpha(1f);
                    charIndex[0]++;
                    animationHandler.postDelayed(this, 40);
                }
            }
        };
        animationHandler.postDelayed(typeRunnable, 200);
    }

    /**
     * Starts cursor blinking animation
     */
    private void startCursorBlink(View cursor) {
        if (binding == null || !isAdded()) return;

        ObjectAnimator blink = ObjectAnimator.ofFloat(cursor, "alpha", 1f, 0f, 1f);
        blink.setDuration(800);
        blink.setRepeatCount(ValueAnimator.INFINITE);
        blink.start();
        runningAnimators.add(blink);
    }

    /**
     * Animates loading section with terminal styling
     */
    private void animateLoadingSection() {
        if (binding == null || !isAdded()) return;

        // Show loading text
        ObjectAnimator loadingFade = ObjectAnimator.ofFloat(binding.textLoading, "alpha", 0f, 1f);
        loadingFade.setDuration(400);
        loadingFade.start();
        runningAnimators.add(loadingFade);

        // Show progress bar
        ObjectAnimator progressFade = ObjectAnimator.ofFloat(binding.progressLoading, "alpha", 0f, 1f);
        progressFade.setDuration(400);
        progressFade.start();
        runningAnimators.add(progressFade);

        // Show percentage
        ObjectAnimator percentFade = ObjectAnimator.ofFloat(binding.textProgressPercent, "alpha", 0f, 1f);
        percentFade.setDuration(400);
        percentFade.start();
        runningAnimators.add(percentFade);

        // Show loading item text
        ObjectAnimator itemFade = ObjectAnimator.ofFloat(binding.textLoadingItem, "alpha", 0f, 1f);
        itemFade.setDuration(400);
        itemFade.start();
        runningAnimators.add(itemFade);

        // Animate progress with status messages and sounds
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(PROGRESS_DURATION);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        final int[] lastMessageIndex = {-1};
        progressAnimator.addUpdateListener(animation -> {
            if (binding == null || !isAdded()) return;

            int progress = (int) animation.getAnimatedValue();
            binding.progressLoading.setProgress(progress);
            binding.textProgressPercent.setText(String.format("[ %d%% ]", progress));

            // Update loading message based on progress
            int messageIndex = Math.min((progress * LOADING_MESSAGES.length) / 100, 
                    LOADING_MESSAGES.length - 1);
            
            if (messageIndex != lastMessageIndex[0]) {
                lastMessageIndex[0] = messageIndex;
                binding.textLoadingItem.setText(LOADING_MESSAGES[messageIndex]);
            }
        });
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingAnimationComplete = true;
                // Play loading complete sound
                soundManager.playSound(SoundManager.Sound.LOADING_COMPLETE);
            }
        });
        progressAnimator.start();
        runningAnimators.add(progressAnimator);

        // Show footer
        animationHandler.postDelayed(this::showFooter, 600);
        
        // Show reward card after loading
        animationHandler.postDelayed(this::showRewardCard, 1400);
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                    REWARD CARD ANIMATION (DOPAMINE RUSH!)
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private void showRewardCard() {
        if (binding == null || !isAdded()) return;
        
        // Get user info
        AuthManager authManager = AuthManager.getInstance(requireContext());
        String username = authManager.getDisplayName();
        if (username == null || username.isEmpty()) {
            username = "Debugger";
        }
        
        // Set welcome message
        String greeting = rewardManager.getTimeBasedGreeting();
        binding.textWelcomeUser.setText(greeting + ", " + username + "! 👑");
        
        // Check if reward can be claimed
        boolean canClaim = rewardManager.canClaimToday();
        int rewardAmount = rewardManager.getTodayRewardAmount();
        int streakDays = rewardManager.getConsecutiveDays();
        
        // Set streak message
        binding.textStreakChip.setText(rewardManager.getStreakMessage(streakDays));
        
        // Set today's mission (placeholder - would come from MetaGameEngine)
        binding.textTodaysMission.setText("🎯 Today: Find your first bug (+25 XP)");
        
        // Show card
        binding.cardReward.setVisibility(View.VISIBLE);
        
        if (reduceMotion) {
            // Show everything instantly for reduce motion
            binding.cardReward.setAlpha(1f);
            binding.cardReward.setTranslationY(0);
            binding.layoutGemReward.setAlpha(1f);
            binding.layoutGemReward.setScaleX(1f);
            binding.layoutGemReward.setScaleY(1f);
            binding.textStreakChip.setAlpha(1f);
            binding.textTodaysMission.setAlpha(1f);
            
            if (canClaim) {
                int claimed = rewardManager.claimDailyReward();
                binding.textGemReward.setText("+" + claimed);
            } else {
                binding.layoutGemReward.setVisibility(View.GONE);
                binding.textAlreadyClaimed.setVisibility(View.VISIBLE);
            }
            return;
        }
        
        // Animated card entrance
        ObjectAnimator cardFade = ObjectAnimator.ofFloat(binding.cardReward, "alpha", 0f, 1f);
        ObjectAnimator cardSlide = ObjectAnimator.ofFloat(binding.cardReward, "translationY", 40f, 0f);
        
        AnimatorSet cardEntrance = new AnimatorSet();
        cardEntrance.playTogether(cardFade, cardSlide);
        cardEntrance.setDuration(500);
        cardEntrance.setInterpolator(new DecelerateInterpolator());
        cardEntrance.start();
        runningAnimators.add(cardEntrance);
        
        // Animate gem reward after card appears
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            if (canClaim) {
                animateGemReward(rewardAmount);
            } else {
                // Show already claimed state
                binding.layoutGemReward.setVisibility(View.GONE);
                binding.textAlreadyClaimed.setVisibility(View.VISIBLE);
                ObjectAnimator claimedFade = ObjectAnimator.ofFloat(binding.textAlreadyClaimed, "alpha", 0f, 1f);
                claimedFade.setDuration(300);
                claimedFade.start();
            }
        }, 400);
        
        // Animate streak chip
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            ObjectAnimator streakFade = ObjectAnimator.ofFloat(binding.textStreakChip, "alpha", 0f, 1f);
            streakFade.setDuration(400);
            streakFade.start();
        }, 700);
        
        // Animate mission text
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            ObjectAnimator missionFade = ObjectAnimator.ofFloat(binding.textTodaysMission, "alpha", 0f, 1f);
            missionFade.setDuration(400);
            missionFade.start();
        }, 900);
    }
    
    /**
     * Animates gem reward with count-up and haptic feedback
     */
    private void animateGemReward(int rewardAmount) {
        if (binding == null || !isAdded()) return;
        
        // Claim the reward
        int actualReward = rewardManager.claimDailyReward();
        if (actualReward == 0) actualReward = rewardAmount; // Fallback
        final int finalReward = actualReward;
        
        // Scale + fade in animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.layoutGemReward, "scaleX", 0.5f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.layoutGemReward, "scaleY", 0.5f, 1.1f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.layoutGemReward, "alpha", 0f, 1f);
        
        AnimatorSet gemEntrance = new AnimatorSet();
        gemEntrance.playTogether(scaleX, scaleY, fadeIn);
        gemEntrance.setDuration(600);
        gemEntrance.setInterpolator(new OvershootInterpolator(2f));
        gemEntrance.start();
        runningAnimators.add(gemEntrance);
        
        // Count-up animation with haptic
        ValueAnimator countUp = ValueAnimator.ofInt(0, finalReward);
        countUp.setDuration(800);
        countUp.setStartDelay(200);
        countUp.setInterpolator(new DecelerateInterpolator());
        countUp.addUpdateListener(animation -> {
            if (binding == null || !isAdded()) return;
            int value = (int) animation.getAnimatedValue();
            binding.textGemReward.setText("+" + value);
        });
        countUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Haptic feedback on reward reveal complete
                rewardManager.triggerRewardHaptic();
                // Play coin sound
                soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
            }
        });
        countUp.start();
        runningAnimators.add(countUp);
    }

    /**
     * Shows footer section
     */
    private void showFooter() {
        if (binding == null || !isAdded()) return;

        ObjectAnimator versionFade = ObjectAnimator.ofFloat(binding.textVersion, "alpha", 0f, 0.5f);
        ObjectAnimator copyrightFade = ObjectAnimator.ofFloat(binding.textCopyright, "alpha", 0f, 0.3f);

        AnimatorSet footerAnim = new AnimatorSet();
        footerAnim.playTogether(versionFade, copyrightFade);
        footerAnim.setDuration(800);
        footerAnim.start();
        runningAnimators.add(footerAnim);
    }

    /**
     * Shows the PLAY button with epic entrance + SOUND
     * NO AUTO-NAVIGATION - User must tap!
     */
    private void showStartButton() {
        if (binding == null || !isAdded()) return;

        // Hide loading elements
        ObjectAnimator hideLoading = ObjectAnimator.ofFloat(binding.textLoading, "alpha", 1f, 0f);
        ObjectAnimator hideProgress = ObjectAnimator.ofFloat(binding.progressLoading, "alpha", 1f, 0f);
        ObjectAnimator hidePercent = ObjectAnimator.ofFloat(binding.textProgressPercent, "alpha", 1f, 0f);
        ObjectAnimator hideItem = ObjectAnimator.ofFloat(binding.textLoadingItem, "alpha", 1f, 0f);

        AnimatorSet hideSet = new AnimatorSet();
        hideSet.playTogether(hideLoading, hideProgress, hidePercent, hideItem);
        hideSet.setDuration(300);
        hideSet.start();
        runningAnimators.add(hideSet);

        // Show button after delay
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            // Play button appear sound
            soundManager.playSound(SoundManager.Sound.BUTTON_APPEAR);

            binding.btnStart.setVisibility(View.VISIBLE);
            binding.btnStartGlow.setVisibility(View.VISIBLE);
            binding.textTapHint.setVisibility(View.VISIBLE);
            
            // Update hint text based on database status
            if (databaseSeeded) {
                binding.textTapHint.setText("[ TAP TO START ]");
            } else {
                binding.textTapHint.setText("[ LOADING... ]");
            }

            // Button entrance
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.btnStart, "scaleX", 0.5f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.btnStart, "scaleY", 0.5f, 1.1f, 1f);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.btnStart, "alpha", 0f, 1f);

            AnimatorSet buttonEntrance = new AnimatorSet();
            buttonEntrance.playTogether(scaleX, scaleY, fadeIn);
            buttonEntrance.setDuration(700);
            buttonEntrance.setInterpolator(new BounceInterpolator());
            buttonEntrance.start();
            runningAnimators.add(buttonEntrance);

            // Glow entrance
            ObjectAnimator glowFade = ObjectAnimator.ofFloat(binding.btnStartGlow, "alpha", 0f, 0.4f);
            glowFade.setDuration(700);
            glowFade.start();
            runningAnimators.add(glowFade);

            // Tap hint with pulse
            ObjectAnimator hintFade = ObjectAnimator.ofFloat(binding.textTapHint, "alpha", 0f, 0.7f);
            hintFade.setDuration(700);
            hintFade.setStartDelay(500);
            hintFade.start();
            runningAnimators.add(hintFade);

            // Start button pulse
            animationHandler.postDelayed(() -> {
                buttonReady = true;
                startButtonPulse();
                startGlowPulse();
                startTapHintPulse();
            }, 700);

        }, 400);
    }

    /**
     * Button pulse animation
     */
    private void startButtonPulse() {
        if (binding == null || !isAdded() || binding.btnStart.getVisibility() != View.VISIBLE) return;

        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(binding.btnStart,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.04f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.04f, 1f));
        pulse.setDuration(1500);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!userTappedStart) {
                    startButtonPulse();
                }
            }
        });
        pulse.start();
        runningAnimators.add(pulse);
    }

    /**
     * Glow pulse animation
     */
    private void startGlowPulse() {
        if (binding == null || !isAdded() || binding.btnStartGlow.getVisibility() != View.VISIBLE) return;

        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(binding.btnStartGlow,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.15f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.15f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0.4f, 0.2f, 0.4f));
        pulse.setDuration(2000);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!userTappedStart) {
                    startGlowPulse();
                }
            }
        });
        pulse.start();
        runningAnimators.add(pulse);
    }
    
    /**
     * Tap hint pulse animation - draws attention to the button
     */
    private void startTapHintPulse() {
        if (binding == null || !isAdded() || binding.textTapHint.getVisibility() != View.VISIBLE) return;

        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(binding.textTapHint,
                PropertyValuesHolder.ofFloat("alpha", 0.7f, 0.3f, 0.7f));
        pulse.setDuration(1200);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!userTappedStart) {
                    startTapHintPulse();
                }
            }
        });
        pulse.start();
        runningAnimators.add(pulse);
    }

    /**
     * Navigates to next screen with TRANSITION SOUND
     */
    private void navigateToNextScreen() {
        if (!isAdded() || binding == null) return;
        
        soundManager.playSound(SoundManager.Sound.TRANSITION);
        performNavigation();
    }

    /**
     * Performs actual navigation based on auth and onboarding status
     */
    private void performNavigation() {
        if (!isAdded() || binding == null) return;

        try {
            AuthManager authManager = AuthManager.getInstance(requireContext());
            
            // Check if user has seen onboarding OR has any existing progress
            boolean hasSeenOnboarding = OnboardingActivity.hasSeenOnboarding(requireContext());
            boolean hasExistingProgress = checkForExistingProgress();
            
            if (!hasSeenOnboarding && !hasExistingProgress) {
                // First time user - show onboarding
                Navigation.findNavController(binding.getRoot()).navigate(
                        R.id.action_splash_to_onboarding
                );
            } else {
                // Mark onboarding as seen if user has progress but flag wasn't set
                if (!hasSeenOnboarding && hasExistingProgress) {
                    markOnboardingAsSeen();
                }
                
                if (!authManager.isLoggedInSync()) {
                    // Not logged in - show auth screen
                    Navigation.findNavController(binding.getRoot()).navigate(
                            R.id.action_splash_to_auth
                    );
                } else {
                    // Logged in - go to home
                    Navigation.findNavController(binding.getRoot()).navigate(
                            R.id.action_splash_to_home
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to home
            try {
                Navigation.findNavController(binding.getRoot()).navigate(
                        R.id.action_splash_to_home
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Check if user has any existing progress (returning user)
     */
    private boolean checkForExistingProgress() {
        try {
            // Check home_prefs for visit count
            android.content.SharedPreferences homePrefs = requireContext()
                .getSharedPreferences("home_prefs", android.content.Context.MODE_PRIVATE);
            int visitCount = homePrefs.getInt("visit_count", 0);
            if (visitCount > 0) return true;
            
            // Check shop_prefs for any purchases
            android.content.SharedPreferences shopPrefs = requireContext()
                .getSharedPreferences("shop_prefs", android.content.Context.MODE_PRIVATE);
            int hints = shopPrefs.getInt("hints_owned", 0);
            boolean avatars = shopPrefs.getBoolean("avatars_unlocked", false);
            boolean titles = shopPrefs.getBoolean("titles_unlocked", false);
            if (hints > 0 || avatars || titles) return true;
            
            // Check if user has any XP or completed bugs
            android.content.SharedPreferences gamePrefs = requireContext()
                .getSharedPreferences("game_session_prefs", android.content.Context.MODE_PRIVATE);
            // Check if any game mode has been played
            if (gamePrefs.getAll().size() > 0) return true;
            
            // Check AuthManager for returning user
            AuthManager authManager = AuthManager.getInstance(requireContext());
            if (authManager.isLoggedInSync()) return true;
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Mark onboarding as seen for returning users
     */
    private void markOnboardingAsSeen() {
        try {
            android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("DebugMasterPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("has_seen_onboarding", true).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mainHandler != null) mainHandler.removeCallbacksAndMessages(null);
        if (animationHandler != null) animationHandler.removeCallbacksAndMessages(null);

        // Create a copy to avoid ConcurrentModificationException
        List<Animator> animatorsCopy = new ArrayList<>(runningAnimators);
        for (Animator animator : animatorsCopy) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        runningAnimators.clear();
        particles.clear();
        codeRainColumns.clear();

        binding = null;
    }
}
