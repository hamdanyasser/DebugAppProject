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
import android.view.MotionEvent;
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
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.navigation.Navigation;

import com.debugmaster.app.R;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.debugmaster.app.databinding.FragmentSplashBinding;
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

    // Letter views for staggered title animation
    private TextView[] letterViews;

    // Gem sparkle scheduler runnable
    private Runnable gemSparkleRunnable;
    private boolean sparkleSchedulerActive = false;

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

        initializeLetterViews();
        setupStartButton();
        setupSkipFunctionality();
        setupTouchParallax();
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
        binding.imageLogo.setScaleX(1f);
        binding.imageLogo.setScaleY(1f);
        
        // Show all letters instantly
        for (TextView letter : letterViews) {
            if (letter != null) {
                letter.setAlpha(1f);
                letter.setTranslationY(0);
                letter.setScaleX(1f);
                letter.setScaleY(1f);
            }
        }
        
        binding.textTagline.setAlpha(1f);
        binding.textVersion.setAlpha(0.5f);
        binding.textCopyright.setAlpha(0.3f);
        
        // Show reward card and button immediately
        showRewardCard();
        showStartButtonStatic();
    }

    /**
     * Initializes letter views for staggered title animation
     */
    private void initializeLetterViews() {
        if (binding == null) return;
        letterViews = new TextView[] {
            binding.letterD,
            binding.letterE,
            binding.letterB,
            binding.letterU,
            binding.letterG,
            binding.letterM,
            binding.letterA,
            binding.letterS,
            binding.letterT,
            binding.letterE2,
            binding.letterR
        };
    }

    /**
     * Sets up touch parallax for hero orb and energy waves
     */
    private void setupTouchParallax() {
        if (binding == null || reduceMotion) return;

        View parallaxTarget = binding.splashRoot;
        parallaxTarget.setOnTouchListener((v, event) -> {
            if (reduceMotion || userTappedStart) return false;

            float centerX = v.getWidth() / 2f;
            float centerY = v.getHeight() / 2f;
            float deltaX = (event.getX() - centerX) * 0.03f;
            float deltaY = (event.getY() - centerY) * 0.03f;

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    // Logo moves opposite
                    binding.imageLogo.setTranslationX(-deltaX);
                    binding.imageLogo.setTranslationY(-deltaY);
                    binding.logoGlowPulse.setTranslationX(-deltaX);
                    binding.logoGlowPulse.setTranslationY(-deltaY);

                    // Energy waves move with touch at different rates
                    binding.energyWave1.setTranslationX(deltaX * 0.5f);
                    binding.energyWave1.setTranslationY(deltaY * 0.5f);
                    binding.energyWave2.setTranslationX(deltaX * 0.3f);
                    binding.energyWave3.setTranslationX(deltaX * 0.2f);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Spring back to center
                    binding.imageLogo.animate().translationX(0).translationY(0).setDuration(300).start();
                    binding.logoGlowPulse.animate().translationX(0).translationY(0).setDuration(300).start();
                    binding.energyWave1.animate().translationX(0).translationY(0).setDuration(300).start();
                    binding.energyWave2.animate().translationX(0).setDuration(300).start();
                    binding.energyWave3.animate().translationX(0).setDuration(300).start();
                    break;
            }
            // Return false so clicks still work on children
            return false;
        });
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
        
        // Stop sparkle scheduler
        sparkleSchedulerActive = false;
        
        // Cancel all running animations (use copy to avoid ConcurrentModificationException)
        List<Animator> animatorsCopy = new ArrayList<>(runningAnimators);
        for (Animator animator : animatorsCopy) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        runningAnimators.clear();
        animationHandler.removeCallbacksAndMessages(null);
        
        // Show everything immediately
        showStaticWelcome();
        
        // Hide skip button (with null check in case fragment was destroyed)
        if (binding != null) {
            binding.btnSkip.setVisibility(View.GONE);
            binding.overlayTapSkip.setVisibility(View.GONE);
        }
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
     * Epic button press animation with enhanced effects + sound
     */
    private void animateButtonPress(View button, Runnable onComplete) {
        // Phase 1: Press down (80ms, Decelerate)
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.96f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.96f);
        ObjectAnimator glowAlphaUp = ObjectAnimator.ofFloat(binding.btnStartGlow, "alpha", 0.3f, 0.9f);
        ObjectAnimator glowScaleUp = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleX", 1f, 1.3f);
        ObjectAnimator glowScaleUpY = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleY", 1f, 1.3f);

        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleDownX, scaleDownY, glowAlphaUp, glowScaleUp, glowScaleUpY);
        pressDown.setDuration(80);
        pressDown.setInterpolator(new DecelerateInterpolator());

        // Phase 2: Release with ripple (300ms, Overshoot)
        ObjectAnimator glowRippleScale = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleX", 1.3f, 2.5f);
        ObjectAnimator glowRippleScaleY = ObjectAnimator.ofFloat(binding.btnStartGlow, "scaleY", 1.3f, 2.5f);
        ObjectAnimator glowFadeOut = ObjectAnimator.ofFloat(binding.btnStartGlow, "alpha", 0.9f, 0f);
        ObjectAnimator buttonBounce = ObjectAnimator.ofFloat(button, "scaleX", 0.96f, 1.08f, 1f);
        ObjectAnimator buttonBounceY = ObjectAnimator.ofFloat(button, "scaleY", 0.96f, 1.08f, 1f);

        AnimatorSet release = new AnimatorSet();
        release.playTogether(glowRippleScale, glowRippleScaleY, glowFadeOut, buttonBounce, buttonBounceY);
        release.setDuration(300);
        release.setInterpolator(new OvershootInterpolator(1.5f));

        // Phase 3: Button fade out + screen fade
        ObjectAnimator buttonFade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f);
        buttonFade.setDuration(200);

        ObjectAnimator screenFade = ObjectAnimator.ofFloat(binding.splashRoot, "alpha", 1f, 0f);
        screenFade.setDuration(300);

        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(pressDown, release, buttonFade, screenFade);
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
                // Don't set databaseSeeded = true on error - prevents user from proceeding with broken DB
                mainHandler.post(() -> {
                    if (binding != null) {
                        binding.textTapHint.setText("[ ERROR - RESTART APP ]");
                        binding.textTapHint.setTextColor(0xFFFF5252);
                    }
                });
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
        animationHandler.postDelayed(this::animateBackgroundGradient, 0);

        // Energy wave rings (outer to inner) - silent elegant entrance
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave3, 20000, false), WAVE_3_DELAY);
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave2, 15000, true), WAVE_2_DELAY);
        animationHandler.postDelayed(() -> animateEnergyWave(binding.energyWave1, 12000, false), WAVE_1_DELAY);

        // Logo section
        animationHandler.postDelayed(this::animateLogoGlow, LOGO_GLOW_DELAY);
        animationHandler.postDelayed(this::animateLogo, LOGO_DELAY);

        // Staggered title letters
        animationHandler.postDelayed(this::animateStaggeredTitle, DEBUG_TEXT_DELAY);
        animationHandler.postDelayed(this::animateTagline, TAGLINE_DELAY);

        // Loading section
        animationHandler.postDelayed(this::animateLoadingSection, LOADING_DELAY);

        // Start button - NO AUTO NAVIGATION
        animationHandler.postDelayed(this::showStartButton, START_BUTTON_DELAY);
    }

    /**
     * Animates background gradient decorations with slow drift
     */
    private void animateBackgroundGradient() {
        if (binding == null || !isAdded() || reduceMotion) return;

        // Animate top-left decoration
        ObjectAnimator topLeftX = ObjectAnimator.ofFloat(binding.decorationTopLeft, "translationX", 0f, 20f, 0f);
        topLeftX.setDuration(12000);
        topLeftX.setRepeatCount(ValueAnimator.INFINITE);
        topLeftX.setInterpolator(new LinearInterpolator());
        topLeftX.start();
        runningAnimators.add(topLeftX);

        ObjectAnimator topLeftY = ObjectAnimator.ofFloat(binding.decorationTopLeft, "translationY", 0f, 15f, 0f);
        topLeftY.setDuration(12000);
        topLeftY.setRepeatCount(ValueAnimator.INFINITE);
        topLeftY.setInterpolator(new LinearInterpolator());
        topLeftY.start();
        runningAnimators.add(topLeftY);

        // Animate bottom-right decoration
        ObjectAnimator bottomRightX = ObjectAnimator.ofFloat(binding.decorationBottomRight, "translationX", 0f, -20f, 0f);
        bottomRightX.setDuration(12000);
        bottomRightX.setRepeatCount(ValueAnimator.INFINITE);
        bottomRightX.setInterpolator(new LinearInterpolator());
        bottomRightX.start();
        runningAnimators.add(bottomRightX);

        ObjectAnimator bottomRightY = ObjectAnimator.ofFloat(binding.decorationBottomRight, "translationY", 0f, -15f, 0f);
        bottomRightY.setDuration(12000);
        bottomRightY.setRepeatCount(ValueAnimator.INFINITE);
        bottomRightY.setInterpolator(new LinearInterpolator());
        bottomRightY.start();
        runningAnimators.add(bottomRightY);
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
     * Creates floating energy particles with depth layers
     */
    private void createFloatingParticles() {
        if (binding == null || !isAdded()) return;

        // Get depth layer containers
        FrameLayout[] depthLayers = {
            binding.particlesLayerFar,
            binding.particlesLayerMid,
            binding.particlesLayerNear
        };

        FrameLayout referenceContainer = binding.particlesContainer;
        referenceContainer.post(() -> {
            if (binding == null || !isAdded()) return;

            int containerWidth = referenceContainer.getWidth() > 0 ? referenceContainer.getWidth() : 1080;
            int containerHeight = referenceContainer.getHeight() > 0 ? referenceContainer.getHeight() : 1920;
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

                // Determine depth layer (0=far, 1=mid, 2=near)
                int depthLayer = i % 3;

                int sizeDp = 6 + random.nextInt(14);
                int sizePx = dpToPx(sizeDp);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
                params.leftMargin = random.nextInt(containerWidth);
                params.topMargin = random.nextInt(containerHeight);
                particle.setLayoutParams(params);
                particle.setAlpha(0f);
                particle.setColorFilter(colors[random.nextInt(colors.length)]);

                // Add to appropriate depth layer
                depthLayers[depthLayer].addView(particle);
                particles.add(particle);

                final int index = i;
                final int depth = depthLayer;
                animationHandler.postDelayed(() -> animateParticle(particle, containerHeight, depth), index * 120);
            }
        });
    }

    /**
     * Animates a single particle floating up with depth-based properties
     */
    private void animateParticle(View particle, int containerHeight, int depthLayer) {
        if (binding == null || !isAdded() || reduceMotion) return;

        // Depth-based properties
        float speedMultiplier = 0.5f + depthLayer * 0.5f; // 0.5, 1.0, 1.5
        float alphaMax = 0.2f + depthLayer * 0.2f; // 0.2, 0.4, 0.6
        float scale = 0.5f + depthLayer * 0.3f; // 0.5, 0.8, 1.1

        particle.setScaleX(scale);
        particle.setScaleY(scale);

        float driftX = (random.nextFloat() - 0.5f) * 150f;
        int duration = (int) (7000 / speedMultiplier);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(particle, "alpha", 0f, alphaMax);
        fadeIn.setDuration(1500);

        ObjectAnimator moveY = ObjectAnimator.ofFloat(particle, "translationY", 0f, -containerHeight * 0.6f);
        moveY.setDuration(duration);
        moveY.setInterpolator(new LinearInterpolator());

        ObjectAnimator moveX = ObjectAnimator.ofFloat(particle, "translationX", 0f, driftX);
        moveX.setDuration(duration);

        // Alpha flicker effect during float
        ObjectAnimator flicker = ObjectAnimator.ofFloat(particle, "alpha", 
                alphaMax, alphaMax * 0.6f, alphaMax, alphaMax * 0.8f, alphaMax);
        flicker.setDuration(duration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(particle, "alpha", alphaMax, 0f);
        fadeOut.setDuration(1500);
        fadeOut.setStartDelay(duration - 1500);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, moveY, moveX, flicker, fadeOut);
        final int finalDepthLayer = depthLayer;
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding != null && isAdded() && !reduceMotion) {
                    particle.setTranslationX(0);
                    particle.setTranslationY(0);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) particle.getLayoutParams();
                    int containerWidth = binding.particlesContainer.getWidth();
                    params.leftMargin = random.nextInt(containerWidth > 0 ? containerWidth : 1080);
                    params.topMargin = containerHeight;
                    particle.setLayoutParams(params);
                    animateParticle(particle, containerHeight, finalDepthLayer);
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
        
        // Add epic screen shake when logo lands!
        entrance.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding != null && isAdded()) {
                    shakeScreen();
                }
            }
        });
        entrance.start();
        runningAnimators.add(entrance);

        // Start subtle breathing effect + synced glow pulse after entrance
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;

            // Subtle breathing: 0.98 -> 1.02 loop, 3000ms
            ObjectAnimator breathe = ObjectAnimator.ofPropertyValuesHolder(logo,
                    PropertyValuesHolder.ofFloat("scaleX", 0.98f, 1.02f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.98f, 1.02f));
            breathe.setDuration(3000);
            breathe.setRepeatCount(ValueAnimator.INFINITE);
            breathe.setRepeatMode(ValueAnimator.REVERSE);
            breathe.setInterpolator(new FastOutSlowInInterpolator());
            if (!reduceMotion) {
                breathe.start();
                runningAnimators.add(breathe);
            }

            // Start synced glow pulse
            startSyncedGlowPulse();
        }, 1200);
    }

    /**
     * Synced glow pulse animation for logo glow ring
     */
    private void startSyncedGlowPulse() {
        if (binding == null || !isAdded() || reduceMotion) return;

        View glow = binding.logoGlowPulse;

        ObjectAnimator glowPulse = ObjectAnimator.ofPropertyValuesHolder(glow,
                PropertyValuesHolder.ofFloat("alpha", 0.5f, 0.8f),
                PropertyValuesHolder.ofFloat("scaleX", 0.95f, 1.05f),
                PropertyValuesHolder.ofFloat("scaleY", 0.95f, 1.05f));
        glowPulse.setDuration(3000);
        glowPulse.setRepeatCount(ValueAnimator.INFINITE);
        glowPulse.setRepeatMode(ValueAnimator.REVERSE);
        glowPulse.setInterpolator(new FastOutSlowInInterpolator());
        glowPulse.start();
        runningAnimators.add(glowPulse);
    }

    /**
     * Animates staggered title letters with typing effect
     */
    private void animateStaggeredTitle() {
        if (binding == null || !isAdded()) return;

        final int STAGGER_DELAY = 55; // 55ms per letter
        final int LETTER_DURATION = 250;

        for (int i = 0; i < letterViews.length; i++) {
            final TextView letter = letterViews[i];
            if (letter == null) continue;

            final int index = i;
            animationHandler.postDelayed(() -> {
                if (binding == null || !isAdded()) return;

                // Play typing sound every 3rd letter
                if (index % 3 == 0) {
                    soundManager.playSound(SoundManager.Sound.TYPING);
                }

                // Animate: fade 0->1, translationY 20->0, scale 0.5->1.1->1
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(letter, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(letter, "translationY", 
                        dpToPx(20), 0f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(letter, "scaleX", 0.5f, 1.1f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(letter, "scaleY", 0.5f, 1.1f, 1f);

                AnimatorSet letterAnim = new AnimatorSet();
                letterAnim.playTogether(fadeIn, slideUp, scaleX, scaleY);
                letterAnim.setDuration(LETTER_DURATION);
                letterAnim.setInterpolator(new FastOutSlowInInterpolator());
                letterAnim.start();
                runningAnimators.add(letterAnim);
            }, i * STAGGER_DELAY);
        }

        // Play title shimmer after all letters are done
        int totalLetterTime = letterViews.length * STAGGER_DELAY + LETTER_DURATION;
        animationHandler.postDelayed(this::playTitleShimmer, totalLetterTime + 200);
    }

    /**
     * Plays one-time shimmer highlight across title
     */
    private void playTitleShimmer() {
        if (binding == null || !isAdded() || reduceMotion) return;

        View shimmer = binding.shimmerHighlight;

        // Move from left (-200) to right (400)
        ObjectAnimator moveX = ObjectAnimator.ofFloat(shimmer, "translationX", 
                dpToPx(-200), dpToPx(400));
        moveX.setDuration(600);
        moveX.setInterpolator(new FastOutSlowInInterpolator());

        // Alpha: 0 -> 0.8 (quick fade in), then 0.8 -> 0 (fade out starts at 400ms)
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(shimmer, "alpha", 0f, 0.8f);
        alphaIn.setDuration(100);

        ObjectAnimator alphaOut = ObjectAnimator.ofFloat(shimmer, "alpha", 0.8f, 0f);
        alphaOut.setDuration(200);
        alphaOut.setStartDelay(400);

        AnimatorSet shimmerAnim = new AnimatorSet();
        shimmerAnim.playTogether(moveX, alphaIn, alphaOut);
        shimmerAnim.start();
        runningAnimators.add(shimmerAnim);
    }

    /**
     * Animates "DEBUG" text with glitch effect + SOUND (LEGACY - kept for compatibility)
     */
    private void animateDebugText() {
        // Now handled by animateStaggeredTitle()
    }

    /**
     * Animates "MASTER" text with glitch effect (LEGACY - kept for compatibility)
     */
    private void animateMasterText() {
        // Now handled by animateStaggeredTitle()
    }

    /**
     * Subtle screen shake for impactful moments
     */
    private void shakeScreen() {
        if (binding == null || !isAdded()) return;
        
        View rootView = binding.getRoot();
        
        // Subtle shake - not too aggressive
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(rootView, "translationX", 0f, 6f, -5f, 4f, -3f, 2f, 0f);
        ObjectAnimator shakeY = ObjectAnimator.ofFloat(rootView, "translationY", 0f, 3f, -2f, 1f, 0f);
        
        AnimatorSet shake = new AnimatorSet();
        shake.playTogether(shakeX, shakeY);
        shake.setDuration(250);
        shake.setInterpolator(new DecelerateInterpolator());
        shake.start();
        runningAnimators.add(shake);
        // No sound - just visual impact
    }

    /**
     * Plays a subtle glitch effect on text
     */
    private void playGlitchEffect(View view) {
        if (binding == null || !isAdded()) return;

        // Subtle horizontal glitch
        ObjectAnimator glitch = ObjectAnimator.ofFloat(view, "translationX", 0f, 3f, -3f, 2f, -1f, 0f);
        glitch.setDuration(120);
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

        // Type out text with occasional click sounds
        final int[] charIndex = {0};
        final Runnable typeRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null || !isAdded()) return;
                if (charIndex[0] < fullText.length()) {
                    tagline.setText(fullText.substring(0, charIndex[0] + 1));
                    tagline.setAlpha(1f);
                    
                    // Play typing sound occasionally (not every char)
                    if (charIndex[0] % 8 == 0) {
                        soundManager.playSound(SoundManager.Sound.TYPING);
                    }
                    
                    charIndex[0]++;
                    animationHandler.postDelayed(this, 35);
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
        final int[] lastMilestone = {0};
        progressAnimator.addUpdateListener(animation -> {
            if (binding == null || !isAdded()) return;

            int progress = (int) animation.getAnimatedValue();
            binding.progressLoading.setProgress(progress);
            binding.textProgressPercent.setText(String.format("[ %d%% ]", progress));

            // Play subtle tick at 50% and 100% only
            if (progress == 50 || progress == 100) {
                if (lastMilestone[0] != progress) {
                    lastMilestone[0] = progress;
                    soundManager.playSound(SoundManager.Sound.TICK);
                }
            }

            // Update loading message based on progress (no sound)
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
        
        // Set welcome message (without crown emoji - already in layout)
        String greeting = rewardManager.getTimeBasedGreeting();
        binding.textWelcomeUser.setText(greeting + ", " + username + "!");
        
        // Check if reward can be claimed
        boolean canClaim = rewardManager.canClaimToday();
        int rewardAmount = rewardManager.getTodayRewardAmount();
        int streakDays = rewardManager.getConsecutiveDays();
        
        // Set streak number display (compact)
        if (streakDays == 0) {
            binding.textStreakNumber.setText("🔥 1");
            binding.textStreakChip.setText("START!");
        } else {
            binding.textStreakNumber.setText("🔥 " + streakDays);
            binding.textStreakChip.setText("STREAK");
        }
        
        // Set today's mission XP (compact)
        binding.textTodaysMission.setText("XP");
        
        // Show card
        binding.cardReward.setVisibility(View.VISIBLE);
        
        if (reduceMotion) {
            // Show everything instantly for reduce motion
            binding.cardReward.setAlpha(1f);
            binding.cardReward.setTranslationY(0);
            binding.layoutGemReward.setAlpha(1f);
            binding.layoutGemReward.setScaleX(1f);
            binding.layoutGemReward.setScaleY(1f);
            binding.layoutStreakBadge.setAlpha(1f);
            binding.layoutMissionBadge.setAlpha(1f);
            
            if (canClaim) {
                int claimed = rewardManager.claimDailyReward();
                binding.textGemReward.setText("+" + claimed);
            } else {
                binding.layoutGemReward.setVisibility(View.GONE);
                binding.layoutAlreadyClaimed.setVisibility(View.VISIBLE);
            }
            return;
        }
        
        // Spring-based card entrance
        animateCardSpringEntrance();
        
        // Animate gem reward after card appears
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            if (canClaim) {
                animateGemReward(rewardAmount);
            } else {
                // Show already claimed state with celebration
                binding.layoutGemReward.setVisibility(View.GONE);
                binding.layoutAlreadyClaimed.setVisibility(View.VISIBLE);
                
                ObjectAnimator claimedScale = ObjectAnimator.ofPropertyValuesHolder(
                        binding.layoutAlreadyClaimed,
                        PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.1f, 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.1f, 1f),
                        PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
                claimedScale.setDuration(500);
                claimedScale.setInterpolator(new OvershootInterpolator(2f));
                claimedScale.start();
            }
        }, 500);
        
        // Animate streak badge with bounce
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            ObjectAnimator streakAnim = ObjectAnimator.ofPropertyValuesHolder(
                    binding.layoutStreakBadge,
                    PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.1f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.1f, 1f),
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
            streakAnim.setDuration(500);
            streakAnim.setInterpolator(new OvershootInterpolator(2f));
            streakAnim.start();
            runningAnimators.add(streakAnim);
            
            // Light haptic for streak reveal
            rewardManager.triggerRewardHaptic();
        }, 900);
        
        // Animate mission badge
        animationHandler.postDelayed(() -> {
            if (binding == null || !isAdded()) return;
            
            ObjectAnimator missionAnim = ObjectAnimator.ofPropertyValuesHolder(
                    binding.layoutMissionBadge,
                    PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.1f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.1f, 1f),
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
            missionAnim.setDuration(500);
            missionAnim.setInterpolator(new OvershootInterpolator(2f));
            missionAnim.start();
            runningAnimators.add(missionAnim);
        }, 1100);
    }

    /**
     * Animates card entrance with spring physics
     */
    private void animateCardSpringEntrance() {
        if (binding == null || !isAdded()) return;

        View card = binding.cardReward;
        card.setTranslationY(dpToPx(100));
        card.setScaleX(0.9f);
        card.setScaleY(0.9f);
        card.setAlpha(0f);

        // Fade in (standard ObjectAnimator)
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setInterpolator(new FastOutSlowInInterpolator());
        fadeIn.start();
        runningAnimators.add(fadeIn);

        // Spring translation Y
        SpringAnimation springY = new SpringAnimation(card, DynamicAnimation.TRANSLATION_Y, 0f);
        springY.getSpring()
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        springY.start();

        // Spring scale X
        SpringAnimation springScaleX = new SpringAnimation(card, DynamicAnimation.SCALE_X, 1f);
        springScaleX.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springScaleX.start();

        // Spring scale Y
        SpringAnimation springScaleY = new SpringAnimation(card, DynamicAnimation.SCALE_Y, 1f);
        springScaleY.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springScaleY.start();
    }
    
    /**
     * Animates gem reward with EPIC elastic scale + sparkle scheduler
     */
    private void animateGemReward(int rewardAmount) {
        if (binding == null || !isAdded()) return;
        
        // Claim the reward
        int actualReward = rewardManager.claimDailyReward();
        if (actualReward == 0) actualReward = rewardAmount; // Fallback
        final int finalReward = actualReward;
        
        // ELASTIC scale animation: 0.3 -> 1.3 -> 0.95 -> 1.05 -> 1
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.layoutGemReward, "scaleX", 
                0.3f, 1.3f, 0.95f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.layoutGemReward, "scaleY", 
                0.3f, 1.3f, 0.95f, 1.05f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.layoutGemReward, "alpha", 0f, 1f);
        
        AnimatorSet gemEntrance = new AnimatorSet();
        gemEntrance.playTogether(scaleX, scaleY, fadeIn);
        gemEntrance.setDuration(800);
        gemEntrance.setInterpolator(new OvershootInterpolator(3f));
        gemEntrance.start();
        runningAnimators.add(gemEntrance);
        
        // Start INFINITE pulsing glow ring animation!
        startInfiniteGlowPulse();
        
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
                // BURST CONFETTI PARTICLES! 🎉
                burstRewardConfetti();
                // Start gem sparkle scheduler
                startGemSparkleScheduler();
            }
        });
        countUp.start();
        runningAnimators.add(countUp);
    }

    /**
     * Starts gem sparkle scheduler that fires randomly every 2-4 seconds
     */
    private void startGemSparkleScheduler() {
        if (binding == null || !isAdded() || reduceMotion || userTappedStart) return;
        
        sparkleSchedulerActive = true;
        
        gemSparkleRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null || !isAdded() || !sparkleSchedulerActive || userTappedStart) {
                    sparkleSchedulerActive = false;
                    return;
                }
                
                // Play sparkle animation
                playGemSparkle();
                
                // Schedule next sparkle with random delay (2000-4000ms)
                int nextDelay = 2000 + random.nextInt(2000);
                animationHandler.postDelayed(this, nextDelay);
            }
        };
        
        // Start first sparkle after random delay
        int initialDelay = 2000 + random.nextInt(2000);
        animationHandler.postDelayed(gemSparkleRunnable, initialDelay);
    }

    /**
     * Plays a single gem sparkle animation
     */
    private void playGemSparkle() {
        if (binding == null || !isAdded()) return;
        
        ImageView sparkle = binding.gemSparkle;
        
        // Random position offset
        float offsetX = dpToPx(-36 + random.nextInt(20));
        float offsetY = dpToPx(-8 + random.nextInt(16));
        sparkle.setTranslationX(offsetX);
        sparkle.setTranslationY(offsetY);
        
        // Sparkle animation: scale + alpha burst
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sparkle, "scaleX", 0f, 1.2f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sparkle, "scaleY", 0f, 1.2f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(sparkle, "alpha", 0f, 1f, 0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(sparkle, "rotation", 0f, 45f);
        
        AnimatorSet sparkleAnim = new AnimatorSet();
        sparkleAnim.playTogether(scaleX, scaleY, alpha, rotation);
        sparkleAnim.setDuration(400);
        sparkleAnim.setInterpolator(new FastOutSlowInInterpolator());
        sparkleAnim.start();
    }
    
    /**
     * Infinite pulsing glow ring for mesmerizing dopamine effect
     */
    private AnimatorSet glowPulseAnimator;
    
    private void startInfiniteGlowPulse() {
        if (binding == null || !isAdded() || reduceMotion) return;
        
        // Cancel any existing glow animation
        if (glowPulseAnimator != null) {
            glowPulseAnimator.cancel();
        }
        
        ObjectAnimator glowPulseX = ObjectAnimator.ofFloat(binding.gemGlowRing, "scaleX", 0.85f, 1.15f);
        ObjectAnimator glowPulseY = ObjectAnimator.ofFloat(binding.gemGlowRing, "scaleY", 0.85f, 1.15f);
        ObjectAnimator glowPulseAlpha = ObjectAnimator.ofFloat(binding.gemGlowRing, "alpha", 0.4f, 0.9f);
        
        glowPulseAnimator = new AnimatorSet();
        glowPulseAnimator.playTogether(glowPulseX, glowPulseY, glowPulseAlpha);
        glowPulseAnimator.setDuration(1000);
        glowPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowPulseAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean reversed = false;
            
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding == null || !isAdded()) return;
                
                // Toggle direction
                reversed = !reversed;
                if (reversed) {
                    // Pulse back down
                    ObjectAnimator reverseX = ObjectAnimator.ofFloat(binding.gemGlowRing, "scaleX", 1.15f, 0.85f);
                    ObjectAnimator reverseY = ObjectAnimator.ofFloat(binding.gemGlowRing, "scaleY", 1.15f, 0.85f);
                    ObjectAnimator reverseAlpha = ObjectAnimator.ofFloat(binding.gemGlowRing, "alpha", 0.9f, 0.4f);
                    
                    glowPulseAnimator = new AnimatorSet();
                    glowPulseAnimator.playTogether(reverseX, reverseY, reverseAlpha);
                    glowPulseAnimator.setDuration(1000);
                    glowPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    glowPulseAnimator.addListener(this);
                    glowPulseAnimator.start();
                } else {
                    // Pulse back up - restart the cycle
                    startInfiniteGlowPulse();
                }
            }
        });
        glowPulseAnimator.start();
        runningAnimators.add(glowPulseAnimator);
    }
    
    /**
     * Bursts colorful confetti particles when reward is claimed! 🎊
     */
    private void burstRewardConfetti() {
        if (binding == null || !isAdded() || reduceMotion) return;
        
        // Get center of gem reward for burst origin
        int[] location = new int[2];
        binding.layoutGemReward.getLocationInWindow(location);
        float centerX = location[0] + binding.layoutGemReward.getWidth() / 2f;
        float centerY = location[1] + binding.layoutGemReward.getHeight() / 2f;
        
        // Confetti colors: gold, purple, cyan, pink, green
        int[] confettiColors = {
            Color.parseColor("#FFD700"), // Gold
            Color.parseColor("#A78BFA"), // Purple
            Color.parseColor("#22D3EE"), // Cyan
            Color.parseColor("#F472B6"), // Pink
            Color.parseColor("#34D399"), // Green
            Color.parseColor("#FBBF24"), // Amber
        };
        
        // Create 20 confetti particles
        for (int i = 0; i < 20; i++) {
            createConfettiParticle(centerX, centerY, confettiColors[i % confettiColors.length]);
        }
    }
    
    /**
     * Creates a single animated confetti particle
     */
    private void createConfettiParticle(float originX, float originY, int color) {
        if (binding == null || !isAdded()) return;
        
        // Create confetti view (small rectangle or square)
        View confetti = new View(requireContext());
        int size = 8 + random.nextInt(8); // 8-16dp
        int sizePx = (int) (size * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        confetti.setLayoutParams(params);
        confetti.setBackgroundColor(color);
        confetti.setRotation(random.nextFloat() * 360);
        
        // Add to particle container
        binding.particlesContainer.addView(confetti);
        
        // Random trajectory
        float angle = random.nextFloat() * 360;
        float distance = 150 + random.nextFloat() * 200; // 150-350dp travel
        float distancePx = distance * getResources().getDisplayMetrics().density;
        
        float endX = originX + (float) Math.cos(Math.toRadians(angle)) * distancePx;
        float endY = originY + (float) Math.sin(Math.toRadians(angle)) * distancePx + 100; // Gravity bias
        
        // Set initial position
        confetti.setX(originX - sizePx / 2f);
        confetti.setY(originY - sizePx / 2f);
        
        // Animate outward with spin and fade
        ObjectAnimator moveX = ObjectAnimator.ofFloat(confetti, "x", originX, endX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(confetti, "y", originY, endY);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(confetti, "rotation", 0, 360 + random.nextFloat() * 720);
        ObjectAnimator fade = ObjectAnimator.ofFloat(confetti, "alpha", 1f, 0f);
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(confetti, "scaleX", 1f, 0.3f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(confetti, "scaleY", 1f, 0.3f);
        
        AnimatorSet confettiAnim = new AnimatorSet();
        confettiAnim.playTogether(moveX, moveY, rotate, fade, scaleDown, scaleDownY);
        confettiAnim.setDuration(800 + random.nextInt(400)); // 800-1200ms
        confettiAnim.setInterpolator(new DecelerateInterpolator(1.5f));
        confettiAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (binding != null && binding.particlesContainer != null) {
                    binding.particlesContainer.removeView(confetti);
                }
            }
        });
        confettiAnim.start();
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
     * Glow pulse animation for play button - enhanced
     */
    private void startGlowPulse() {
        if (binding == null || !isAdded() || binding.btnStartGlow.getVisibility() != View.VISIBLE) return;
        if (reduceMotion) return;

        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(binding.btnStartGlow,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.08f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.08f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0.3f, 0.5f, 0.3f));
        pulse.setDuration(2500);
        pulse.setInterpolator(new FastOutSlowInInterpolator());
        pulse.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!userTappedStart && !reduceMotion) {
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

        // Stop sparkle scheduler
        sparkleSchedulerActive = false;
        if (gemSparkleRunnable != null && animationHandler != null) {
            animationHandler.removeCallbacks(gemSparkleRunnable);
        }

        if (mainHandler != null) mainHandler.removeCallbacksAndMessages(null);
        if (animationHandler != null) animationHandler.removeCallbacksAndMessages(null);
        
        // Stop infinite glow pulse
        if (glowPulseAnimator != null) {
            glowPulseAnimator.cancel();
            glowPulseAnimator = null;
        }

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
