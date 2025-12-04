package com.example.debugappproject.ui.gamemodes;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

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
import com.example.debugappproject.util.SoundManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Game Session Fragment - Handles all game modes with real mechanics
 */
public class GameSessionFragment extends Fragment {

    public enum GameMode {
        QUICK_FIX("Quick Fix", 60000, 3, 1.5f, true, true),
        PUZZLE_MODE("Puzzle Mode", 0, 3, 3.0f, false, false),
        DAILY_CHALLENGE("Daily Challenge", 120000, 1, 2.0f, true, true),
        MYSTERY_BUG("Mystery Bug", 90000, 2, 2.5f, true, true),
        SPEED_RUN("Speed Run", 45000, 5, 2.0f, true, true),
        SURVIVAL("Survival", 30000, 1, 1.0f, true, false);

        public final String displayName;
        public final long timeLimitMs;
        public final int lives;
        public final float xpMultiplier;
        public final boolean hasTimer;
        public final boolean showHints;

        GameMode(String displayName, long timeLimitMs, int lives, float xpMultiplier, 
                 boolean hasTimer, boolean showHints) {
            this.displayName = displayName;
            this.timeLimitMs = timeLimitMs;
            this.lives = lives;
            this.xpMultiplier = xpMultiplier;
            this.hasTimer = hasTimer;
            this.showHints = showHints;
        }
    }

    private FragmentGameSessionBinding binding;
    private BugListViewModel viewModel;
    private SoundManager soundManager;
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

    // Timer
    private CountDownTimer bugTimer;
    private long timeRemaining;

    // Answer tracking
    private int selectedOptionIndex = -1;
    private Bug currentBug;
    private String[] currentOptions;

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
        prefs = requireContext().getSharedPreferences("game_session_prefs", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            String modeName = getArguments().getString("gameMode", "quick_fix");
            currentMode = parseGameMode(modeName);
        }

        setupUI();
        setupObservers();
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

    private void setupUI() {
        binding.textGameMode.setText(currentMode.displayName);
        lives = currentMode.lives;
        updateLivesDisplay();
        binding.layoutTimer.setVisibility(currentMode.hasTimer ? View.VISIBLE : View.GONE);

        binding.buttonBack.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showExitConfirmation();
        });

        binding.buttonPause.setOnClickListener(v -> {
            soundManager.playButtonClick();
            togglePause();
        });

        if (currentMode.showHints) {
            binding.buttonHint.setVisibility(View.VISIBLE);
            binding.buttonHint.setOnClickListener(v -> showHint());
        } else {
            binding.buttonHint.setVisibility(View.GONE);
        }

        binding.buttonSkip.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.ERROR);
            loseLife();
            if (!gameEnded) {
                currentBugIndex++;
                loadCurrentBug();
            }
        });

        setupOptionButtons();
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
                
                int count = currentMode == GameMode.DAILY_CHALLENGE ? 1 : 
                           currentMode == GameMode.MYSTERY_BUG ? 1 : 10;
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

    private void startGame() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutGame.setVisibility(View.VISIBLE);
        
        sessionStartTime = System.currentTimeMillis();
        currentBugIndex = 0;
        score = 0;
        bugsCompleted = 0;
        totalXpEarned = 0;
        gameEnded = false;

        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        showCountdown(this::loadCurrentBug);
    }

    private void showCountdown(Runnable onComplete) {
        binding.layoutCountdown.setVisibility(View.VISIBLE);
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
                    soundManager.playSound(SoundManager.Sound.SUCCESS);
                    handler.postDelayed(() -> {
                        binding.layoutCountdown.setVisibility(View.GONE);
                        binding.layoutGameContent.setAlpha(1f);
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

    private void loadCurrentBug() {
        if (currentBugIndex >= sessionBugs.size()) {
            endGame(true);
            return;
        }

        currentBug = sessionBugs.get(currentBugIndex);
        selectedOptionIndex = -1;

        binding.textProgress.setText(String.format("Bug %d/%d", 
                currentBugIndex + 1, sessionBugs.size()));
        binding.progressSession.setMax(sessionBugs.size());
        binding.progressSession.setProgress(currentBugIndex);

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

        if (currentMode.hasTimer) {
            startBugTimer();
        }

        animateBugEntrance();
    }

    private void generateOptions() {
        currentOptions = new String[4];
        
        int correctIndex = random.nextInt(4);
        currentOptions[correctIndex] = currentBug.getFixedCode();

        String[] wrongAnswers = generateWrongAnswers(currentBug);
        int wrongIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex && wrongIndex < wrongAnswers.length) {
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
        
        variations[0] = buggy;
        
        variations[1] = correct.replace("==", "=")
                              .replace("!=", "==")
                              .replace("<=", "<")
                              .replace(">=", ">");
        if (variations[1].equals(correct)) {
            variations[1] = correct.replace(";", "")
                                  .replace("}", "");
        }
        
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

        options[index].animate()
                .scaleX(1.05f).scaleY(1.05f)
                .setDuration(100)
                .withEndAction(() ->
                    options[index].animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start()
                )
                .start();

        binding.buttonSubmit.setEnabled(true);
        binding.buttonSubmit.setAlpha(1f);
    }

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

        long timeBonus = currentMode.hasTimer ? (timeRemaining / 1000) * 2 : 0;
        int baseXp = getBaseXp(currentBug.getDifficulty());
        int earnedXp = (int) ((baseXp + timeBonus) * currentMode.xpMultiplier);
        
        totalXpEarned += earnedXp;
        bugsCompleted++;
        score += 100 + (int) timeBonus;

        animateScoreIncrease(earnedXp);

        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_correct);
        
        binding.textFeedback.setText("✅ Correct! +" + earnedXp + " XP");
        binding.textFeedback.setTextColor(getResources().getColor(R.color.success_green, null));
        binding.textFeedback.setVisibility(View.VISIBLE);
        binding.textFeedback.animate().alpha(1f).setDuration(200).start();

        handler.postDelayed(() -> {
            binding.textFeedback.setVisibility(View.GONE);
            currentBugIndex++;
            loadCurrentBug();
        }, 1500);
    }

    private void handleWrongAnswer() {
        soundManager.playSound(SoundManager.Sound.ERROR);

        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        options[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);

        for (int i = 0; i < 4; i++) {
            if (currentOptions[i].equals(currentBug.getFixedCode())) {
                options[i].setBackgroundResource(R.drawable.bg_option_correct);
                break;
            }
        }

        ObjectAnimator shake = ObjectAnimator.ofFloat(options[selectedOptionIndex], 
                "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(400);
        shake.start();

        binding.textFeedback.setText("❌ Wrong! The correct fix was shown.");
        binding.textFeedback.setTextColor(getResources().getColor(R.color.error_red, null));
        binding.textFeedback.setVisibility(View.VISIBLE);

        loseLife();

        if (!gameEnded) {
            handler.postDelayed(() -> {
                binding.textFeedback.setVisibility(View.GONE);
                currentBugIndex++;
                loadCurrentBug();
            }, 2000);
        }
    }

    private void loseLife() {
        lives--;
        updateLivesDisplay();

        binding.textLives.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(100)
                .withEndAction(() ->
                    binding.textLives.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start()
                )
                .start();

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

    private void startBugTimer() {
        timeRemaining = currentMode.timeLimitMs;
        
        if (bugTimer != null) {
            bugTimer.cancel();
        }

        binding.progressTimer.setMax((int) (currentMode.timeLimitMs / 1000));
        
        bugTimer = new CountDownTimer(currentMode.timeLimitMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                binding.textTimer.setText(String.format("%02d", seconds));
                binding.progressTimer.setProgress(seconds);

                if (seconds <= 10) {
                    binding.textTimer.setTextColor(
                            getResources().getColor(R.color.error_red, null));
                    if (seconds <= 5 && seconds > 0) {
                        soundManager.playSound(SoundManager.Sound.BLIP);
                    }
                } else {
                    binding.textTimer.setTextColor(
                            getResources().getColor(R.color.white, null));
                }
            }

            @Override
            public void onFinish() {
                soundManager.playSound(SoundManager.Sound.ERROR);
                binding.textFeedback.setText("⏱️ Time's up!");
                binding.textFeedback.setTextColor(
                        getResources().getColor(R.color.error_red, null));
                binding.textFeedback.setVisibility(View.VISIBLE);
                
                loseLife();
                
                if (!gameEnded) {
                    handler.postDelayed(() -> {
                        binding.textFeedback.setVisibility(View.GONE);
                        currentBugIndex++;
                        loadCurrentBug();
                    }, 1500);
                }
            }
        };

        bugTimer.start();
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
            if (currentMode.hasTimer) {
                startBugTimer();
            }
        }
    }

    private void showHint() {
        if (currentBug == null) return;
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        String hint = currentBug.getHint();
        if (hint == null || hint.isEmpty()) {
            hint = "Look carefully at the code logic and variable usage.";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("💡 Hint")
                .setMessage(hint)
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Exit Game?")
                .setMessage("Your progress will be lost. Are you sure?")
                .setPositiveButton("Exit", (d, w) -> {
                    if (bugTimer != null) bugTimer.cancel();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    private void endGame(boolean completed) {
        gameEnded = true;
        if (bugTimer != null) bugTimer.cancel();

        long sessionTime = System.currentTimeMillis() - sessionStartTime;
        int minutes = (int) (sessionTime / 60000);
        int seconds = (int) ((sessionTime % 60000) / 1000);

        saveGameStats();

        String title = completed ? "🎉 Session Complete!" : "💀 Game Over";
        String message = String.format(
                "Mode: %s\n\n" +
                "Bugs Fixed: %d/%d\n" +
                "Score: %d\n" +
                "XP Earned: +%d\n" +
                "Time: %d:%02d\n\n" +
                "%s",
                currentMode.displayName,
                bugsCompleted, sessionBugs.size(),
                score,
                totalXpEarned,
                minutes, seconds,
                completed ? "Great job! 🌟" : "Better luck next time!"
        );

        soundManager.playSound(completed ? SoundManager.Sound.LEVEL_UP : SoundManager.Sound.ERROR);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Play Again", (d, w) -> {
                    currentBugIndex = 0;
                    lives = currentMode.lives;
                    loadBugsForSession();
                })
                .setNegativeButton("Exit", (d, w) -> {
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setCancelable(false)
                .show();
    }

    private void saveGameStats() {
        int totalGames = prefs.getInt("total_games_" + currentMode.name(), 0);
        int totalScore = prefs.getInt("total_score_" + currentMode.name(), 0);
        int bestScore = prefs.getInt("best_score_" + currentMode.name(), 0);

        prefs.edit()
                .putInt("total_games_" + currentMode.name(), totalGames + 1)
                .putInt("total_score_" + currentMode.name(), totalScore + score)
                .putInt("best_score_" + currentMode.name(), Math.max(bestScore, score))
                .apply();
    }

    private void animateScoreIncrease(int xpGained) {
        binding.textScore.setText(String.format("Score: %d", score));
        
        binding.textScore.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() ->
                    binding.textScore.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(150)
                            .start()
                )
                .start();
    }

    private void animateBugEntrance() {
        binding.cardBug.setTranslationX(300f);
        binding.cardBug.setAlpha(0f);
        binding.cardBug.animate()
                .translationX(0f).alpha(1f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        View[] options = {binding.option1, binding.option2, binding.option3, binding.option4};
        for (int i = 0; i < options.length; i++) {
            options[i].setTranslationY(50f);
            options[i].setAlpha(0f);
            int delay = 200 + (i * 100);
            options[i].animate()
                    .translationY(0f).alpha(1f)
                    .setStartDelay(delay)
                    .setDuration(300)
                    .start();
        }
    }

    private int getBaseXp(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return 10;
            case "medium": return 25;
            case "hard": return 50;
            default: return 15;
        }
    }

    private int getDifficultyColor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return 0xFF10B981;
            case "medium": return 0xFFF59E0B;
            case "hard": return 0xFFEF4444;
            default: return 0xFF6B7280;
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
