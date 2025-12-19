package com.example.debugappproject.ui.gamemodes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentGameSessionBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.ui.buglist.BugListViewModel;
import com.example.debugappproject.util.CelebrationManager;
import com.example.debugappproject.util.SoundManager;
import com.example.debugappproject.ui.shop.ShopFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - SUPER ADDICTIVE GAME SESSION                         ║
 * ║                    Maximum Fun & Engagement System                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Combo system with multipliers
 * - Power-ups (Time Freeze, Double XP, Shield, Hint Reveal)
 * - Satisfying feedback animations
 * - Progressive difficulty
 * - Streak bonuses
 * - Achievement triggers
 * - Mode-specific mechanics
 */
public class GameSessionFragment extends Fragment {

    // ═══════════════════════════════════════════════════════════════════════════
    //                              GAME MODES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum GameMode {
        QUICK_FIX("Quick Fix", 60000, 3, 1.5f, true, true, "🎯"),
        PUZZLE_MODE("Puzzle Mode", 0, 3, 3.0f, false, false, "🧩"),
        DAILY_CHALLENGE("Daily Challenge", 120000, 1, 2.0f, true, true, "🏆"),
        MYSTERY_BUG("Mystery Bug", 90000, 2, 2.5f, true, true, "🎰"),
        SPEED_RUN("Speed Run", 45000, 5, 2.0f, true, true, "🏃"),
        SURVIVAL("Survival", 30000, 1, 1.0f, true, false, "💀");

        public final String displayName;
        public final long timeLimitMs;
        public final int lives;
        public final float xpMultiplier;
        public final boolean hasTimer;
        public final boolean showHints;
        public final String emoji;

        GameMode(String displayName, long timeLimitMs, int lives, float xpMultiplier,
                 boolean hasTimer, boolean showHints, String emoji) {
            this.displayName = displayName;
            this.timeLimitMs = timeLimitMs;
            this.lives = lives;
            this.xpMultiplier = xpMultiplier;
            this.hasTimer = hasTimer;
            this.showHints = showHints;
            this.emoji = emoji;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              POWER-UPS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum PowerUp {
        TIME_FREEZE("⏸️ Time Freeze", "Freezes timer for 10 seconds", 10000),
        DOUBLE_XP("2️⃣ Double XP", "2x XP for next 3 bugs", 3),
        SHIELD("🛡️ Shield", "Protects from one wrong answer", 1),
        HINT_REVEAL("💡 Super Hint", "Eliminates 2 wrong options", 2),
        EXTRA_LIFE("❤️ Extra Life", "Gain an extra life", 1),
        SCORE_BOOST("⚡ Score Boost", "+500 instant points", 500);

        public final String name;
        public final String description;
        public final int value;

        PowerUp(String name, String description, int value) {
            this.name = name;
            this.description = description;
            this.value = value;
        }
    }

    private FragmentGameSessionBinding binding;
    private BugListViewModel viewModel;
    private SoundManager soundManager;
    private CelebrationManager celebrationManager;
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    // Game state
    private GameMode currentMode = GameMode.QUICK_FIX;
    private List<Bug> sessionBugs = new ArrayList<>();
    private int currentBugIndex = 0;
    private int lives;
    private int score = 0;
    private int bugsCompleted = 0;
    private int totalXpEarned = 0;
    private long sessionStartTime;
    private boolean gameEnded = false;
    private boolean isPaused = false;

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ADDICTIVE MECHANICS
    // ═══════════════════════════════════════════════════════════════════════════

    // Combo system
    private int comboCount = 0;
    private int maxCombo = 0;
    private long lastCorrectTime = 0;
    private static final long COMBO_TIMEOUT = 15000; // 15 seconds to maintain combo

    // Multipliers
    private float currentMultiplier = 1.0f;
    private int perfectStreak = 0; // No hints used streak
    private boolean usedHintThisBug = false;

    // Power-ups
    private boolean hasShield = false;
    private int doubleXpBugsRemaining = 0;
    private boolean timeFrozen = false;
    private long freezeEndTime = 0;
    private int hintsRevealed = 0;

    // Special effects
    private boolean isFeverMode = false; // Activated at 10+ combo
    private int totalPerfects = 0;
    private int fastSolves = 0; // Solved in < 50% of time

    // Daily challenge specifics
    private String dailyTheme = "";
    private int bonusObjectivesCompleted = 0;
    private List<String> bonusObjectives = new ArrayList<>();

    // Mystery bug modifiers
    private String mysteryModifier = "";
    private float mysteryMultiplier = 1.0f;

    // Timer
    private CountDownTimer bugTimer;
    private long timeRemaining;
    private long originalTimeLimit;

    // Answer tracking
    private int selectedOptionIndex = -1;
    private Bug currentBug;
    private String[] currentOptions;
    private int correctAnswerIndex = -1;
    
    // Shop items integration
    private int shopHintsAvailable = 0;
    private int shopShieldsAvailable = 0;
    private int shopTimeFreezeAvailable = 0;
    private int shopSkipTokensAvailable = 0;
    private int shopComboSaversAvailable = 0;
    private int shopXpBoostsAvailable = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGameSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BugListViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());
        celebrationManager = new CelebrationManager(requireContext());
        prefs = requireContext().getSharedPreferences("game_session_prefs", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            String modeName = getArguments().getString("gameMode", "quick_fix");
            currentMode = parseGameMode(modeName);
        }

        setupUI();
        setupObservers();
        initializeModeSpecifics();
        loadBugsForSession();
    }

    private GameMode parseGameMode(String modeName) {
        switch (modeName.toLowerCase()) {
            case "puzzle": return GameMode.PUZZLE_MODE;
            case "daily": return GameMode.DAILY_CHALLENGE;
            case "mystery": return GameMode.MYSTERY_BUG;
            case "speed_run": return GameMode.SPEED_RUN;
            case "survival": return GameMode.SURVIVAL;
            default: return GameMode.QUICK_FIX;
        }
    }

    private void initializeModeSpecifics() {
        switch (currentMode) {
            case DAILY_CHALLENGE:
                initializeDailyChallenge();
                break;
            case MYSTERY_BUG:
                initializeMysteryBug();
                break;
            case SPEED_RUN:
                initializeSpeedRun();
                break;
            case SURVIVAL:
                initializeSurvival();
                break;
            case PUZZLE_MODE:
                initializePuzzleMode();
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                       MODE-SPECIFIC INITIALIZERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void initializeDailyChallenge() {
        // Theme based on day of week
        String[] themes = {"Memory Monday", "Type Tuesday", "Wild Wednesday",
                          "Throwback Thursday", "Fast Friday", "Skill Saturday", "Super Sunday"};
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        dailyTheme = themes[dayOfWeek];

        // Generate bonus objectives
        bonusObjectives.clear();
        bonusObjectives.add("🎯 Complete without hints");
        bonusObjectives.add("⚡ Solve 3 bugs under 30 seconds");
        bonusObjectives.add("🔥 Get a 5+ combo");

        // Weekend bonus
        if (dayOfWeek == 0 || dayOfWeek == 6) {
            currentMultiplier = 2.0f; // Weekend 2x XP
        }
    }

    private void initializeMysteryBug() {
        // Random modifier
        String[] modifiers = {
            "JACKPOT", "DOUBLE_TIME", "HARD_MODE", "BONUS_XP", "SPEED_DEMON",
            "LUCKY_STREAK", "MEGA_COMBO", "TREASURE_HUNT"
        };
        mysteryModifier = modifiers[random.nextInt(modifiers.length)];

        switch (mysteryModifier) {
            case "JACKPOT":
                mysteryMultiplier = 5.0f;
                break;
            case "DOUBLE_TIME":
                originalTimeLimit = currentMode.timeLimitMs * 2;
                mysteryMultiplier = 1.5f;
                break;
            case "HARD_MODE":
                mysteryMultiplier = 3.0f;
                break;
            case "BONUS_XP":
                mysteryMultiplier = 2.5f;
                break;
            case "SPEED_DEMON":
                originalTimeLimit = currentMode.timeLimitMs / 2;
                mysteryMultiplier = 4.0f;
                break;
            case "LUCKY_STREAK":
                comboCount = 3; // Start with combo
                mysteryMultiplier = 2.0f;
                break;
            case "MEGA_COMBO":
                mysteryMultiplier = 1.0f; // Combo multiplier is doubled
                break;
            case "TREASURE_HUNT":
                hasShield = true;
                mysteryMultiplier = 2.0f;
                break;
        }
    }

    private void initializeSpeedRun() {
        // Speed run bonuses
        currentMultiplier = 1.0f;
        // Every bug solved under half time gives bonus multiplier
    }

    private void initializeSurvival() {
        // Survival: Time decreases each round
        originalTimeLimit = currentMode.timeLimitMs;
    }

    private void initializePuzzleMode() {
        // Puzzle mode: No time pressure, but hints disabled
        // 3x XP for solving without any assistance
        currentMultiplier = 3.0f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              UI SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupUI() {
        binding.textGameMode.setText(currentMode.emoji + " " + currentMode.displayName);
        lives = currentMode.lives;
        updateLivesDisplay();
        binding.layoutTimer.setVisibility(currentMode.hasTimer ? View.VISIBLE : View.GONE);
        
        // Load shop items
        loadShopItems();

        binding.buttonBack.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showExitConfirmation();
        });

        binding.buttonPause.setOnClickListener(v -> {
            soundManager.playButtonClick();
            togglePause();
        });

        if (currentMode.showHints || shopHintsAvailable > 0) {
            binding.buttonHint.setVisibility(View.VISIBLE);
            binding.buttonHint.setOnClickListener(v -> showHint());
            updateHintButtonText();
        } else {
            binding.buttonHint.setVisibility(View.GONE);
        }
        
        // Setup time freeze button if available
        setupPowerUpButtons();

        binding.buttonSkip.setOnClickListener(v -> {
            // Check if user has skip tokens from shop
            if (shopSkipTokensAvailable > 0 && ShopFragment.useSkipToken(requireContext())) {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                shopSkipTokensAvailable--;
                showPowerUpUsedMessage("⏭️ Skip Token used!", "Bug skipped without penalty");
                // Skip without losing life or combo!
                currentBugIndex++;
                loadCurrentBug();
                return;
            }
            
            soundManager.playSound(SoundManager.Sound.ERROR);
            if (hasShield || shopShieldsAvailable > 0) {
                if (shopShieldsAvailable > 0 && ShopFragment.useGameShield(requireContext())) {
                    shopShieldsAvailable--;
                    showPowerUpUsedMessage("🛡️ Shield used!", "Protected from penalty");
                } else {
                    hasShield = false;
                }
                showShieldUsedAnimation();
            } else {
                loseLife();
            }
            if (!gameEnded) {
                resetCombo();
                currentBugIndex++;
                loadCurrentBug();
            }
        });

        setupOptionButtons();
        updateComboDisplay();
        updateMultiplierDisplay();
        updatePowerUpDisplay();
    }
    
    /**
     * Load available shop items for this game session
     */
    private void loadShopItems() {
        shopHintsAvailable = ShopFragment.getOwnedHints(requireContext());
        shopShieldsAvailable = ShopFragment.getGameShields(requireContext());
        shopTimeFreezeAvailable = ShopFragment.getTimeFreezes(requireContext());
        shopSkipTokensAvailable = ShopFragment.getSkipTokens(requireContext());
        shopComboSaversAvailable = ShopFragment.getComboSavers(requireContext());
        shopXpBoostsAvailable = ShopFragment.getXpBoostCount(requireContext());
        
        // Also check if XP boost is active from shop
        if (shopXpBoostsAvailable > 0) {
            doubleXpBugsRemaining = shopXpBoostsAvailable;
        }
    }
    
    /**
     * Setup power-up buttons based on shop inventory
     */
    private void setupPowerUpButtons() {
        if (binding == null) return;
        
        // Time Freeze button
        if (binding.buttonTimeFreeze != null) {
            binding.buttonTimeFreeze.setVisibility(shopTimeFreezeAvailable > 0 ? View.VISIBLE : View.GONE);
            binding.buttonTimeFreeze.setText("⏸️ " + shopTimeFreezeAvailable);
            binding.buttonTimeFreeze.setOnClickListener(v -> activateShopTimeFreeze());
        }
        
        // Update skip button text if user has skip tokens
        if (binding.buttonSkip != null && shopSkipTokensAvailable > 0) {
            binding.buttonSkip.setText("⏭️ Skip (" + shopSkipTokensAvailable + ")");
            binding.buttonSkip.setTextColor(Color.parseColor("#10B981")); // Green when has tokens
            binding.buttonSkip.setStrokeColorResource(android.R.color.transparent);
        }
    }
    
    /**
     * Update hint button text to show owned count
     */
    private void updateHintButtonText() {
        if (binding.buttonHint != null && shopHintsAvailable > 0) {
            binding.buttonHint.setText("💡 " + shopHintsAvailable);
        }
    }
    
    /**
     * Update power-up display counts
     */
    private void updatePowerUpDisplay() {
        if (binding == null) return;
        
        if (binding.buttonHint != null) {
            updateHintButtonText();
        }
        if (binding.buttonTimeFreeze != null) {
            binding.buttonTimeFreeze.setText("⏸️ " + shopTimeFreezeAvailable);
            binding.buttonTimeFreeze.setVisibility(shopTimeFreezeAvailable > 0 ? View.VISIBLE : View.GONE);
        }
        
        // Update skip button
        if (binding.buttonSkip != null) {
            if (shopSkipTokensAvailable > 0) {
                binding.buttonSkip.setText("⏭️ Skip (" + shopSkipTokensAvailable + ")");
                binding.buttonSkip.setTextColor(Color.parseColor("#10B981"));
            } else {
                binding.buttonSkip.setText("⏭️ Skip");
                binding.buttonSkip.setTextColor(Color.parseColor("#EF4444"));
            }
        }
    }
    
    /**
     * Activate time freeze from shop
     */
    private void activateShopTimeFreeze() {
        if (shopTimeFreezeAvailable > 0 && ShopFragment.useTimeFreeze(requireContext())) {
            shopTimeFreezeAvailable--;
            updatePowerUpDisplay();
            activateTimeFreezeEffect();
            showPowerUpUsedMessage("⏸️ Time Frozen!", "15 seconds added");
        }
    }
    
    /**
     * Show power-up used message with animation
     */
    private void showPowerUpUsedMessage(String title, String subtitle) {
        binding.textFeedback.setText(title + "\n" + subtitle);
        binding.textFeedback.setTextColor(Color.parseColor("#3B82F6"));
        binding.textFeedback.setVisibility(View.VISIBLE);
        
        binding.textFeedback.setScaleX(0.5f);
        binding.textFeedback.setScaleY(0.5f);
        binding.textFeedback.setAlpha(0f);
        binding.textFeedback.animate()
            .scaleX(1.1f).scaleY(1.1f).alpha(1f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator())
            .withEndAction(() -> {
                handler.postDelayed(() -> {
                    if (binding != null) {
                        binding.textFeedback.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> {
                                if (binding != null) binding.textFeedback.setVisibility(View.GONE);
                            }).start();
                    }
                }, 1500);
            })
            .start();
    }

    private void setupOptionButtons() {
        View.OnClickListener optionClickListener = v -> {
            if (isPaused || gameEnded) return;

            int clickedIndex = -1;
            if (v == binding.option1) clickedIndex = 0;
            else if (v == binding.option2) clickedIndex = 1;
            else if (v == binding.option3) clickedIndex = 2;
            else if (v == binding.option4) clickedIndex = 3;

            if (clickedIndex >= 0) {
                selectOption(clickedIndex);
            }
        };

        binding.option1.setOnClickListener(optionClickListener);
        binding.option2.setOnClickListener(optionClickListener);
        binding.option3.setOnClickListener(optionClickListener);
        binding.option4.setOnClickListener(optionClickListener);

        binding.buttonSubmit.setOnClickListener(v -> submitAnswer());
    }

    private void setupObservers() {
        viewModel.getBugs().observe(getViewLifecycleOwner(), bugs -> {
            if (bugs != null && !bugs.isEmpty()) {
                List<Bug> shuffled = new ArrayList<>(bugs);
                Collections.shuffle(shuffled, random);

                int count;
                switch (currentMode) {
                    case DAILY_CHALLENGE:
                        count = 5; // Daily has 5 themed bugs
                        break;
                    case MYSTERY_BUG:
                        count = 3; // Mystery has 3 random bugs
                        break;
                    case SURVIVAL:
                        count = 20; // Survival goes as long as you can
                        break;
                    case SPEED_RUN:
                        count = 15; // Speed run has 15 bugs
                        break;
                    default:
                        count = 10;
                }
                sessionBugs = shuffled.subList(0, Math.min(count, shuffled.size()));

                startGame();
            }
        });
    }

    private void loadBugsForSession() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutGame.setVisibility(View.GONE);
        viewModel.loadAllBugs();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              GAME FLOW
    // ═══════════════════════════════════════════════════════════════════════════

    private void startGame() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutGame.setVisibility(View.VISIBLE);

        sessionStartTime = System.currentTimeMillis();
        currentBugIndex = 0;
        score = 0;
        bugsCompleted = 0;
        totalXpEarned = 0;
        gameEnded = false;
        comboCount = 0;
        maxCombo = 0;
        perfectStreak = 0;
        totalPerfects = 0;
        fastSolves = 0;

        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);

        // Show mode-specific intro
        showModeIntro(() -> showCountdown(this::loadCurrentBug));
    }

    private void showModeIntro(Runnable onComplete) {
        String introMessage = "";

        switch (currentMode) {
            case DAILY_CHALLENGE:
                introMessage = "🏆 " + dailyTheme + "\n\n" +
                              "Bonus Objectives:\n" +
                              String.join("\n", bonusObjectives);
                break;
            case MYSTERY_BUG:
                introMessage = "🎰 MYSTERY MODIFIER:\n\n" +
                              getMysteryModifierDescription() + "\n\n" +
                              "Multiplier: " + mysteryMultiplier + "x";
                break;
            case SPEED_RUN:
                introMessage = "🏃 SPEED RUN\n\n" +
                              "Beat your best time!\n" +
                              "Fast solves = Bonus XP";
                break;
            case SURVIVAL:
                introMessage = "💀 SURVIVAL MODE\n\n" +
                              "One life. Timer gets shorter.\n" +
                              "How far can you go?";
                break;
            case PUZZLE_MODE:
                introMessage = "🧩 PUZZLE MODE\n\n" +
                              "No hints. No timer.\n" +
                              "Pure debugging skill.\n" +
                              "3x XP Reward!";
                break;
            default:
                onComplete.run();
                return;
        }

        showAnimatedMessage(introMessage, 2500, onComplete);
    }

    private String getMysteryModifierDescription() {
        switch (mysteryModifier) {
            case "JACKPOT": return "🎰 JACKPOT!\n5x XP on everything!";
            case "DOUBLE_TIME": return "⏰ DOUBLE TIME!\nTwice the time, 1.5x XP";
            case "HARD_MODE": return "🔥 HARD MODE!\nLess time, 3x XP";
            case "BONUS_XP": return "💰 BONUS XP!\n2.5x XP multiplier";
            case "SPEED_DEMON": return "⚡ SPEED DEMON!\nHalf time, 4x XP!";
            case "LUCKY_STREAK": return "🍀 LUCKY STREAK!\nStart with 3 combo!";
            case "MEGA_COMBO": return "🔥 MEGA COMBO!\nDouble combo multiplier!";
            case "TREASURE_HUNT": return "🛡️ TREASURE HUNT!\nFree shield + 2x XP";
            default: return "Mystery awaits...";
        }
    }

    private void showAnimatedMessage(String message, long duration, Runnable onComplete) {
        binding.layoutCountdown.setVisibility(View.VISIBLE);
        binding.textCountdown.setText(message);
        binding.textCountdown.setTextSize(18);
        binding.layoutGameContent.setAlpha(0.3f);

        binding.textCountdown.setScaleX(0.5f);
        binding.textCountdown.setScaleY(0.5f);
        binding.textCountdown.setAlpha(0f);

        binding.textCountdown.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(500)
            .setInterpolator(new OvershootInterpolator())
            .withEndAction(() -> {
                handler.postDelayed(() -> {
                    binding.textCountdown.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            binding.layoutCountdown.setVisibility(View.GONE);
                            binding.layoutGameContent.setAlpha(1f);
                            binding.textCountdown.setTextSize(120);
                            onComplete.run();
                        })
                        .start();
                }, duration - 800);
            })
            .start();
    }

    private void showCountdown(Runnable onComplete) {
        binding.layoutCountdown.setVisibility(View.VISIBLE);
        binding.textCountdown.setTextSize(120);
        binding.layoutGameContent.setAlpha(0.3f);

        final int[] count = {3};

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (count[0] > 0) {
                    binding.textCountdown.setText(String.valueOf(count[0]));
                    animateCountdownNumber();
                    soundManager.playSound(SoundManager.Sound.BLIP);
                    count[0]--;
                    handler.postDelayed(this, 1000);
                } else {
                    binding.textCountdown.setText("GO!");
                    binding.textCountdown.setTextColor(Color.parseColor("#10B981"));
                    soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                    handler.postDelayed(() -> {
                        binding.layoutCountdown.setVisibility(View.GONE);
                        binding.layoutGameContent.setAlpha(1f);
                        binding.textCountdown.setTextColor(Color.WHITE);
                        onComplete.run();
                    }, 500);
                }
            }
        });
    }

    private void animateCountdownNumber() {
        binding.textCountdown.setScaleX(0.5f);
        binding.textCountdown.setScaleY(0.5f);
        binding.textCountdown.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(2f))
                .withEndAction(() ->
                    binding.textCountdown.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(200)
                            .start()
                )
                .start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           BUG LOADING & DISPLAY
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadCurrentBug() {
        if (currentBugIndex >= sessionBugs.size()) {
            endGame(true);
            return;
        }

        currentBug = sessionBugs.get(currentBugIndex);
        selectedOptionIndex = -1;
        usedHintThisBug = false;
        hintsRevealed = 0;

        // Update progress
        binding.textProgress.setText(String.format("Bug %d/%d",
                currentBugIndex + 1, sessionBugs.size()));
        binding.progressSession.setMax(sessionBugs.size());
        binding.progressSession.setProgress(currentBugIndex);

        // Display bug info
        binding.textBugTitle.setText(currentBug.getTitle());
        binding.textBugDescription.setText(currentBug.getDescription());
        binding.textBugCode.setText(currentBug.getBrokenCode());

        String difficulty = currentBug.getDifficulty();
        binding.badgeDifficulty.setText(difficulty.toUpperCase());
        int badgeColor = getDifficultyColor(difficulty);
        binding.badgeDifficulty.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(badgeColor));

        generateOptions();
        resetOptionButtons();

        // Mode-specific timer adjustments
        if (currentMode.hasTimer) {
            long timeLimit = getAdjustedTimeLimit();
            startBugTimer(timeLimit);
        }

        animateBugEntrance();

        // Check for fever mode
        if (comboCount >= 10 && !isFeverMode) {
            activateFeverMode();
        }
    }

    private long getAdjustedTimeLimit() {
        long baseTime = currentMode.timeLimitMs;

        // Mystery bug time modifications
        if (currentMode == GameMode.MYSTERY_BUG && originalTimeLimit > 0) {
            baseTime = originalTimeLimit;
        }

        // Survival mode: time decreases each round
        if (currentMode == GameMode.SURVIVAL) {
            baseTime = Math.max(15000, 30000 - (currentBugIndex * 1500));
        }

        // Speed run: consistent but tight
        if (currentMode == GameMode.SPEED_RUN) {
            baseTime = 45000;
        }

        return baseTime;
    }

    private void generateOptions() {
        currentOptions = new String[4];

        correctAnswerIndex = random.nextInt(4);
        currentOptions[correctAnswerIndex] = currentBug.getFixedCode();

        String[] wrongAnswers = generateWrongAnswers(currentBug);
        int wrongIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (i != correctAnswerIndex && wrongIndex < wrongAnswers.length) {
                currentOptions[i] = wrongAnswers[wrongIndex++];
            }
        }

        binding.option1Text.setText(truncateCode(currentOptions[0]));
        binding.option2Text.setText(truncateCode(currentOptions[1]));
        binding.option3Text.setText(truncateCode(currentOptions[2]));
        binding.option4Text.setText(truncateCode(currentOptions[3]));
    }

    private String[] generateWrongAnswers(Bug bug) {
        String correct = bug.getFixedCode();
        String buggy = bug.getBrokenCode();

        String[] variations = new String[3];

        // Variation 1: Original buggy code
        variations[0] = buggy;

        // Variation 2: Subtly wrong fix
        variations[1] = correct.replace("==", "=")
                              .replace("!=", "==")
                              .replace("<=", "<")
                              .replace(">=", ">");
        if (variations[1].equals(correct)) {
            variations[1] = correct.replace(";", "")
                                  .replace("}", "");
        }

        // Variation 3: Another wrong variation
        variations[2] = correct.replace("true", "false")
                              .replace("null", "undefined")
                              .replace("0", "1");
        if (variations[2].equals(correct)) {
            variations[2] = buggy.replace("i++", "++i")
                                .replace("++", "--");
        }

        return variations;
    }

    private String truncateCode(String code) {
        if (code == null) return "// Error";
        if (code.length() > 100) {
            return code.substring(0, 97) + "...";
        }
        return code;
    }

    private void resetOptionButtons() {
        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        for (View option : options) {
            option.setSelected(false);
            option.setBackgroundResource(R.drawable.bg_option_default);
            option.setAlpha(1f);
            option.setEnabled(true);
        }
        binding.buttonSubmit.setEnabled(false);
        binding.buttonSubmit.setAlpha(0.5f);
    }

    private void selectOption(int index) {
        soundManager.playSound(SoundManager.Sound.BLIP);

        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        for (View option : options) {
            option.setSelected(false);
            option.setBackgroundResource(R.drawable.bg_option_default);
        }

        selectedOptionIndex = index;
        options[index].setSelected(true);
        options[index].setBackgroundResource(R.drawable.bg_option_selected);

        // Satisfying selection animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(options[index], "scaleX", 1f, 1.08f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(options[index], "scaleY", 1f, 1.08f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.start();

        binding.buttonSubmit.setEnabled(true);
        binding.buttonSubmit.setAlpha(1f);

        // Animate submit button
        binding.buttonSubmit.animate()
            .scaleX(1.05f).scaleY(1.05f)
            .setDuration(150)
            .withEndAction(() ->
                binding.buttonSubmit.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(150)
                    .start()
            )
            .start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           ANSWER HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    private void submitAnswer() {
        if (selectedOptionIndex < 0 || currentBug == null) return;

        if (bugTimer != null) {
            bugTimer.cancel();
        }

        String selectedAnswer = currentOptions[selectedOptionIndex];
        boolean isCorrect = selectedAnswer.equals(currentBug.getFixedCode());

        if (isCorrect) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer();
        }
    }

    private void handleCorrectAnswer() {
        soundManager.playSound(SoundManager.Sound.SUCCESS);

        // Update combo
        long now = System.currentTimeMillis();
        if (now - lastCorrectTime < COMBO_TIMEOUT || lastCorrectTime == 0) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        lastCorrectTime = now;
        maxCombo = Math.max(maxCombo, comboCount);

        // Calculate XP with all multipliers
        long usedTime = currentMode.hasTimer ? (currentMode.timeLimitMs - timeRemaining) : 0;
        boolean wasFast = currentMode.hasTimer && usedTime < (currentMode.timeLimitMs / 2);
        boolean wasPerfect = !usedHintThisBug;

        if (wasFast) fastSolves++;
        if (wasPerfect) {
            perfectStreak++;
            totalPerfects++;
        } else {
            perfectStreak = 0;
        }

        int baseXp = getBaseXp(currentBug.getDifficulty());

        // Apply multipliers
        float totalMultiplier = currentMode.xpMultiplier;
        totalMultiplier *= currentMultiplier; // Mode-specific multiplier
        totalMultiplier *= mysteryMultiplier; // Mystery bug multiplier

        // Combo multiplier (5% per combo, max 100%)
        float comboMult = currentMode == GameMode.MYSTERY_BUG && mysteryModifier.equals("MEGA_COMBO")
            ? 1 + (comboCount * 0.10f) // Double combo bonus
            : 1 + (comboCount * 0.05f);
        comboMult = Math.min(comboMult, 2.0f);
        totalMultiplier *= comboMult;

        // Perfect streak bonus
        if (perfectStreak >= 3) {
            totalMultiplier *= (1 + perfectStreak * 0.05f);
        }

        // Double XP power-up
        if (doubleXpBugsRemaining > 0) {
            totalMultiplier *= 2;
            doubleXpBugsRemaining--;
        }

        // Fever mode bonus
        if (isFeverMode) {
            totalMultiplier *= 1.5f;
        }

        // Time bonus
        long timeBonus = currentMode.hasTimer ? (timeRemaining / 1000) * 3 : 0;
        if (wasFast) timeBonus *= 2; // Double time bonus for fast solve

        int earnedXp = (int) ((baseXp + timeBonus) * totalMultiplier);

        totalXpEarned += earnedXp;
        bugsCompleted++;
        score += 100 + (comboCount * 25) + (int) timeBonus;

        // Show celebration
        showCorrectAnswerCelebration(earnedXp, wasPerfect, wasFast);

        // Update displays
        updateComboDisplay();
        updateMultiplierDisplay();
        animateScoreIncrease(earnedXp);

        // Show correct option
        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_correct);

        // Check bonus objectives for daily challenge
        if (currentMode == GameMode.DAILY_CHALLENGE) {
            checkBonusObjectives(wasPerfect, wasFast);
        }

        // Proceed to next bug
        handler.postDelayed(() -> {
            binding.textFeedback.setVisibility(View.GONE);
            currentBugIndex++;
            loadCurrentBug();
        }, 1800);
    }

    private void showCorrectAnswerCelebration(int xpEarned, boolean perfect, boolean fast) {
        StringBuilder feedback = new StringBuilder();

        // Main message
        if (perfect && fast) {
            feedback.append("⚡ LIGHTNING FAST! ⚡\n");
            soundManager.playSound(SoundManager.Sound.LEVEL_UP);
        } else if (perfect) {
            feedback.append("✨ PERFECT! ✨\n");
        } else if (fast) {
            feedback.append("🏃 QUICK FIX!\n");
        } else {
            feedback.append("✅ Correct!\n");
        }

        // XP earned
        feedback.append("+").append(xpEarned).append(" XP");

        // Combo message
        if (comboCount > 1) {
            feedback.append("\n🔥 ").append(comboCount).append("x COMBO!");
        }

        // Fever mode
        if (isFeverMode) {
            feedback.append("\n🌟 FEVER BONUS!");
        }

        binding.textFeedback.setText(feedback.toString());
        binding.textFeedback.setTextColor(Color.parseColor("#10B981"));
        binding.textFeedback.setVisibility(View.VISIBLE);

        // Animate feedback
        binding.textFeedback.setScaleX(0.5f);
        binding.textFeedback.setScaleY(0.5f);
        binding.textFeedback.setAlpha(0f);
        binding.textFeedback.animate()
            .scaleX(1.1f).scaleY(1.1f).alpha(1f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator())
            .withEndAction(() ->
                binding.textFeedback.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .start()
            )
            .start();
    }

    private void handleWrongAnswer() {
        soundManager.playSound(SoundManager.Sound.ERROR);

        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};

        // Check for shop game shield first
        if (shopShieldsAvailable > 0 && ShopFragment.useGameShield(requireContext())) {
            shopShieldsAvailable--;
            soundManager.playSound(SoundManager.Sound.POWER_UP);
            showShieldUsedAnimation();
            binding.textFeedback.setText("🛡️ Shop Shield Protected You!\nTry again! (" + shopShieldsAvailable + " left)");
            binding.textFeedback.setTextColor(Color.parseColor("#3B82F6"));
            binding.textFeedback.setVisibility(View.VISIBLE);

            // Disable wrong option
            options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);
            options[selectedOptionIndex].setEnabled(false);
            options[selectedOptionIndex].setAlpha(0.5f);
            selectedOptionIndex = -1;
            binding.buttonSubmit.setEnabled(false);
            binding.buttonSubmit.setAlpha(0.5f);

            handler.postDelayed(() -> binding.textFeedback.setVisibility(View.GONE), 1500);
            return;
        }
        
        // Check for built-in shield
        if (hasShield) {
            hasShield = false;
            showShieldUsedAnimation();
            binding.textFeedback.setText("🛡️ Shield Protected You!\nTry again!");
            binding.textFeedback.setTextColor(Color.parseColor("#3B82F6"));
            binding.textFeedback.setVisibility(View.VISIBLE);

            // Disable wrong option
            options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);
            options[selectedOptionIndex].setEnabled(false);
            options[selectedOptionIndex].setAlpha(0.5f);
            selectedOptionIndex = -1;
            binding.buttonSubmit.setEnabled(false);
            binding.buttonSubmit.setAlpha(0.5f);

            handler.postDelayed(() -> binding.textFeedback.setVisibility(View.GONE), 1500);
            return;
        }

        // Show wrong answer
        options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);

        // Show correct answer
        for (int i = 0; i < 4; i++) {
            if (currentOptions[i].equals(currentBug.getFixedCode())) {
                options[i].setBackgroundResource(R.drawable.bg_option_correct);
                break;
            }
        }

        // Shake animation
        ObjectAnimator shake = ObjectAnimator.ofFloat(options[selectedOptionIndex],
                "translationX", 0, 15, -15, 15, -15, 8, -8, 0);
        shake.setDuration(500);
        shake.start();

        // Check for combo saver
        boolean comboSaved = false;
        if (comboCount > 0 && shopComboSaversAvailable > 0 && ShopFragment.useComboSaver(requireContext())) {
            shopComboSaversAvailable--;
            comboSaved = true;
            binding.textFeedback.setText("❌ Wrong!\n🔥 Combo Saver activated! Combo preserved!");
            binding.textFeedback.setTextColor(Color.parseColor("#F59E0B"));
        } else {
            binding.textFeedback.setText("❌ Wrong! Combo broken.");
            binding.textFeedback.setTextColor(Color.parseColor("#EF4444"));
        }
        binding.textFeedback.setVisibility(View.VISIBLE);

        // Reset combo only if not saved
        if (!comboSaved) {
            resetCombo();
        }

        loseLife();

        if (!gameEnded) {
            handler.postDelayed(() -> {
                binding.textFeedback.setVisibility(View.GONE);
                currentBugIndex++;
                loadCurrentBug();
            }, 2000);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                          COMBO & MULTIPLIER
    // ═══════════════════════════════════════════════════════════════════════════

    private void resetCombo() {
        comboCount = 0;
        perfectStreak = 0;
        isFeverMode = false;
        updateComboDisplay();
        updateMultiplierDisplay();
    }

    private void updateComboDisplay() {
        if (binding.textScore != null) {
            String comboText = comboCount > 1 ?
                String.format("Score: %d | 🔥%dx", score, comboCount) :
                String.format("Score: %d", score);
            binding.textScore.setText(comboText);

            // Pulse animation for high combos
            if (comboCount >= 5) {
                binding.textScore.animate()
                    .scaleX(1.1f).scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() ->
                        binding.textScore.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(150)
                            .start()
                    )
                    .start();
            }
        }
    }

    private void updateMultiplierDisplay() {
        // Calculate current effective multiplier
        float effectiveMultiplier = currentMode.xpMultiplier * currentMultiplier * mysteryMultiplier;
        if (comboCount > 1) {
            effectiveMultiplier *= (1 + comboCount * 0.05f);
        }
        if (doubleXpBugsRemaining > 0) {
            effectiveMultiplier *= 2;
        }
        if (isFeverMode) {
            effectiveMultiplier *= 1.5f;
        }
    }

    private void activateFeverMode() {
        isFeverMode = true;
        soundManager.playSound(SoundManager.Sound.POWER_UP);

        // Flash the screen
        binding.getRoot().setBackgroundColor(Color.parseColor("#1A10B981"));
        handler.postDelayed(() -> {
            binding.getRoot().setBackgroundResource(R.drawable.bg_cyber_main);
        }, 500);

        showAnimatedMessage("🔥 FEVER MODE! 🔥\n1.5x Bonus Active!", 1500, () -> {});
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              POWER-UPS
    // ═══════════════════════════════════════════════════════════════════════════

    private void showShieldUsedAnimation() {
        soundManager.playSound(SoundManager.Sound.POWER_UP);

        // Flash blue
        View root = binding.getRoot();
        root.setBackgroundColor(Color.parseColor("#1A3B82F6"));
        handler.postDelayed(() -> root.setBackgroundResource(R.drawable.bg_cyber_main), 300);
    }

    private void activateTimeFreezeEffect() {
        if (!currentMode.hasTimer || bugTimer == null) return;

        timeFrozen = true;
        freezeEndTime = System.currentTimeMillis() + 10000;
        bugTimer.cancel();

        soundManager.playSound(SoundManager.Sound.POWER_UP);
        binding.textTimer.setTextColor(Color.parseColor("#3B82F6"));
        binding.textTimer.setText("⏸️");

        handler.postDelayed(() -> {
            timeFrozen = false;
            binding.textTimer.setTextColor(Color.WHITE);
            if (!gameEnded && !isPaused) {
                startBugTimer(timeRemaining);
            }
        }, 10000);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              TIMER
    // ═══════════════════════════════════════════════════════════════════════════

    private void startBugTimer(long duration) {
        timeRemaining = duration;

        if (bugTimer != null) {
            bugTimer.cancel();
        }

        binding.progressTimer.setMax((int) (duration / 1000));

        bugTimer = new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timeFrozen) return;

                timeRemaining = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                binding.textTimer.setText(String.format("%02d", seconds));
                binding.progressTimer.setProgress(seconds);

                // Color coding with urgency animations
                if (seconds <= 5) {
                    binding.textTimer.setTextColor(Color.parseColor("#EF4444"));
                    // Pulse animation
                    binding.textTimer.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(100)
                        .withEndAction(() ->
                            binding.textTimer.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(100)
                                .start()
                        )
                        .start();
                    soundManager.playSound(SoundManager.Sound.BLIP);
                } else if (seconds <= 15) {
                    binding.textTimer.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    binding.textTimer.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onFinish() {
                soundManager.playSound(SoundManager.Sound.ERROR);
                binding.textFeedback.setText("⏱️ TIME'S UP!");
                binding.textFeedback.setTextColor(Color.parseColor("#EF4444"));
                binding.textFeedback.setVisibility(View.VISIBLE);

                resetCombo();

                if (hasShield) {
                    hasShield = false;
                    showShieldUsedAnimation();
                    handler.postDelayed(() -> {
                        binding.textFeedback.setVisibility(View.GONE);
                        currentBugIndex++;
                        loadCurrentBug();
                    }, 1500);
                } else {
                    loseLife();

                    if (!gameEnded) {
                        handler.postDelayed(() -> {
                            binding.textFeedback.setVisibility(View.GONE);
                            currentBugIndex++;
                            loadCurrentBug();
                        }, 1500);
                    }
                }
            }
        };

        bugTimer.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              LIVES
    // ═══════════════════════════════════════════════════════════════════════════

    private void loseLife() {
        lives--;
        updateLivesDisplay();

        // Dramatic life loss animation
        binding.textLives.animate()
                .scaleX(1.5f).scaleY(1.5f)
                .setDuration(100)
                .withEndAction(() ->
                    binding.textLives.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new BounceInterpolator())
                            .start()
                )
                .start();

        // Screen flash red
        View root = binding.getRoot();
        root.setBackgroundColor(Color.parseColor("#1AEF4444"));
        handler.postDelayed(() -> root.setBackgroundResource(R.drawable.bg_cyber_main), 200);

        if (lives <= 0) {
            endGame(false);
        }
    }

    private void updateLivesDisplay() {
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < currentMode.lives; i++) {
            hearts.append(i < lives ? "❤️" : "🖤");
        }
        binding.textLives.setText(hearts.toString());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           HINTS & PAUSE
    // ═══════════════════════════════════════════════════════════════════════════

    private void showHint() {
        if (currentBug == null) return;
        
        // Check if user has shop hints available
        if (shopHintsAvailable > 0) {
            if (ShopFragment.useHint(requireContext())) {
                shopHintsAvailable--;
                updateHintButtonText();
                soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
                
                String hint = currentBug.getHint();
                if (hint == null || hint.isEmpty()) {
                    hint = "Look carefully at the code logic and variable usage.";
                }
                
                // Shop hints don't penalize XP!
                new AlertDialog.Builder(requireContext())
                        .setTitle("💡 Shop Hint (" + shopHintsAvailable + " left)")
                        .setMessage(hint + "\n\n✨ Shop hints have NO XP penalty!")
                        .setPositiveButton("Got it!", null)
                        .show();
                return;
            }
        }
        
        // Fall back to free hint with XP penalty
        usedHintThisBug = true;
        soundManager.playSound(SoundManager.Sound.HINT_REVEAL);

        String hint = currentBug.getHint();
        if (hint == null || hint.isEmpty()) {
            hint = "Look carefully at the code logic and variable usage.";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("💡 Hint (-50% XP)")
                .setMessage(hint)
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            if (bugTimer != null) bugTimer.cancel();
            binding.layoutPaused.setVisibility(View.VISIBLE);
            binding.buttonPause.setText("▶️");
        } else {
            binding.layoutPaused.setVisibility(View.GONE);
            binding.buttonPause.setText("⏸️");
            if (currentMode.hasTimer && !timeFrozen) {
                startBugTimer(timeRemaining);
            }
        }
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit Game?")
                .setMessage("Your progress will be lost!\n\n" +
                           "Current Score: " + score + "\n" +
                           "XP Earned: " + totalXpEarned + "\n" +
                           "Max Combo: " + maxCombo + "x")
                .setPositiveButton("Exit", (d, w) -> {
                    if (bugTimer != null) bugTimer.cancel();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           DAILY CHALLENGE
    // ═══════════════════════════════════════════════════════════════════════════

    private void checkBonusObjectives(boolean wasPerfect, boolean wasFast) {
        // Check "Complete without hints"
        if (wasPerfect && bugsCompleted == sessionBugs.size()) {
            bonusObjectivesCompleted++;
        }

        // Check "Solve 3 bugs under 30 seconds"
        if (fastSolves >= 3 && !bonusObjectives.get(1).contains("✅")) {
            bonusObjectives.set(1, "✅ " + bonusObjectives.get(1));
            bonusObjectivesCompleted++;
            showBonusObjectiveComplete("⚡ Fast Fixer!");
        }

        // Check "Get a 5+ combo"
        if (maxCombo >= 5 && !bonusObjectives.get(2).contains("✅")) {
            bonusObjectives.set(2, "✅ " + bonusObjectives.get(2));
            bonusObjectivesCompleted++;
            showBonusObjectiveComplete("🔥 Combo Master!");
        }
    }

    private void showBonusObjectiveComplete(String message) {
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        showAnimatedMessage("🎯 BONUS COMPLETE!\n" + message + "\n+100 XP", 1500, () -> {});
        totalXpEarned += 100;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              END GAME
    // ═══════════════════════════════════════════════════════════════════════════

    private void endGame(boolean completed) {
        gameEnded = true;
        if (bugTimer != null) bugTimer.cancel();

        long sessionTime = System.currentTimeMillis() - sessionStartTime;
        int minutes = (int) (sessionTime / 60000);
        int seconds = (int) ((sessionTime % 60000) / 1000);

        // Calculate final bonuses
        int completionBonus = completed ? 200 : 0;
        int perfectBonus = totalPerfects * 25;
        int comboBonus = maxCombo * 15;
        int speedBonus = fastSolves * 20;

        int finalXp = totalXpEarned + completionBonus + perfectBonus + comboBonus + speedBonus;

        saveGameStats(finalXp, completed);

        // Build result message
        String title = completed ? "🎉 VICTORY!" : "💀 GAME OVER";

        StringBuilder message = new StringBuilder();
        message.append(currentMode.emoji).append(" ").append(currentMode.displayName).append("\n\n");

        message.append("═══════════════════\n");
        message.append("📊 STATS\n");
        message.append("═══════════════════\n");
        message.append("🐛 Bugs Fixed: ").append(bugsCompleted).append("/").append(sessionBugs.size()).append("\n");
        message.append("🎯 Score: ").append(score).append("\n");
        message.append("⏱️ Time: ").append(minutes).append(":").append(String.format("%02d", seconds)).append("\n");
        message.append("🔥 Max Combo: ").append(maxCombo).append("x\n");
        message.append("✨ Perfect Solves: ").append(totalPerfects).append("\n");
        message.append("⚡ Fast Solves: ").append(fastSolves).append("\n\n");

        message.append("═══════════════════\n");
        message.append("💰 XP BREAKDOWN\n");
        message.append("═══════════════════\n");
        message.append("Base XP: ").append(totalXpEarned).append("\n");
        if (completionBonus > 0) message.append("✅ Completion: +").append(completionBonus).append("\n");
        if (perfectBonus > 0) message.append("✨ Perfect: +").append(perfectBonus).append("\n");
        if (comboBonus > 0) message.append("🔥 Combo: +").append(comboBonus).append("\n");
        if (speedBonus > 0) message.append("⚡ Speed: +").append(speedBonus).append("\n");
        message.append("\n🏆 TOTAL: +").append(finalXp).append(" XP");

        // Daily challenge bonus objectives
        if (currentMode == GameMode.DAILY_CHALLENGE && bonusObjectivesCompleted > 0) {
            message.append("\n\n🎯 Bonus Objectives: ").append(bonusObjectivesCompleted).append("/3");
        }

        soundManager.playSound(completed ? SoundManager.Sound.VICTORY : SoundManager.Sound.DEFEAT);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message.toString())
                .setPositiveButton("🔄 Play Again", (d, w) -> {
                    resetGameState();
                    loadBugsForSession();
                })
                .setNegativeButton("🏠 Exit", (d, w) -> {
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setCancelable(false)
                .show();
    }

    private void resetGameState() {
        currentBugIndex = 0;
        lives = currentMode.lives;
        score = 0;
        bugsCompleted = 0;
        totalXpEarned = 0;
        comboCount = 0;
        maxCombo = 0;
        perfectStreak = 0;
        totalPerfects = 0;
        fastSolves = 0;
        hasShield = false;
        doubleXpBugsRemaining = 0;
        isFeverMode = false;
        bonusObjectivesCompleted = 0;

        initializeModeSpecifics();
        updateLivesDisplay();
        updateComboDisplay();
    }

    private void saveGameStats(int finalXp, boolean completed) {
        int totalGames = prefs.getInt("total_games_" + currentMode.name(), 0);
        int totalScore = prefs.getInt("total_score_" + currentMode.name(), 0);
        int bestScore = prefs.getInt("best_score_" + currentMode.name(), 0);
        int bestCombo = prefs.getInt("best_combo_" + currentMode.name(), 0);
        int totalXp = prefs.getInt("total_xp_" + currentMode.name(), 0);

        prefs.edit()
                .putInt("total_games_" + currentMode.name(), totalGames + 1)
                .putInt("total_score_" + currentMode.name(), totalScore + score)
                .putInt("best_score_" + currentMode.name(), Math.max(bestScore, score))
                .putInt("best_combo_" + currentMode.name(), Math.max(bestCombo, maxCombo))
                .putInt("total_xp_" + currentMode.name(), totalXp + finalXp)
                .apply();

        // Update daily challenge status
        if (currentMode == GameMode.DAILY_CHALLENGE && completed) {
            long today = System.currentTimeMillis() / 86400000;
            long lastDay = prefs.getLong("daily_challenge_last", 0) / 86400000;
            int streak = prefs.getInt("daily_streak", 0);

            if (today != lastDay) {
                if (today - lastDay == 1) {
                    streak++; // Consecutive day
                } else {
                    streak = 1; // Reset streak
                }

                prefs.edit()
                    .putLong("daily_challenge_last", System.currentTimeMillis())
                    .putInt("daily_streak", streak)
                    .apply();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private void animateScoreIncrease(int xpGained) {
        binding.textScore.setText(String.format("Score: %d", score));

        // Pop animation
        binding.textScore.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() ->
                    binding.textScore.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(200)
                            .start()
                )
                .start();
    }

    private void animateBugEntrance() {
        // Card slides in from right
        binding.cardBug.setTranslationX(400f);
        binding.cardBug.setAlpha(0f);
        binding.cardBug.setRotation(5f);
        binding.cardBug.animate()
                .translationX(0f).alpha(1f).rotation(0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        // Options cascade in
        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        for (int i = 0; i < options.length; i++) {
            options[i].setTranslationY(80f);
            options[i].setAlpha(0f);
            options[i].setScaleX(0.9f);
            options[i].setScaleY(0.9f);
            int delay = 300 + (i * 100);
            options[i].animate()
                    .translationY(0f).alpha(1f).scaleX(1f).scaleY(1f)
                    .setStartDelay(delay)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        // Submit button bounces in
        binding.buttonSubmit.setTranslationY(50f);
        binding.buttonSubmit.setAlpha(0f);
        binding.buttonSubmit.animate()
            .translationY(0f).alpha(0.5f)
            .setStartDelay(700)
            .setDuration(400)
            .setInterpolator(new BounceInterpolator())
            .start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    private int getBaseXp(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return 15;
            case "medium": return 30;
            case "hard": return 60;
            case "expert": return 100;
            default: return 20;
        }
    }

    private int getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return 0xFF10B981;    // Green
            case "medium": return 0xFFF59E0B;  // Orange
            case "hard": return 0xFFEF4444;    // Red
            case "expert": return 0xFF8B5CF6;  // Purple
            default: return 0xFF6B7280;        // Gray
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bugTimer != null) bugTimer.cancel();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
