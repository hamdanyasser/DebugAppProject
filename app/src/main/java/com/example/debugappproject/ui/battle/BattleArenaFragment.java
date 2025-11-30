package com.example.debugappproject.ui.battle;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.databinding.FragmentBattleArenaBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.SoundManager;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - BATTLE ARENA (REAL GAMEPLAY)                         ║
 * ║              Actual Bug Challenges - Not Random Win/Lose!                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * FIXED: Now shows actual bugs that the user must solve to win.
 * Win = Correct fix submitted
 * Lose = Wrong fix or time runs out
 */
@AndroidEntryPoint
public class BattleArenaFragment extends Fragment {

    private static final String TAG = "BattleArenaFragment";
    private FragmentBattleArenaBinding binding;
    private BillingManager billingManager;
    private SoundManager soundManager;
    private BugDao bugDao;
    private ExecutorService executor;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMatchmaking = false;
    private boolean isInBattle = false;
    
    // Current battle state
    private Bug currentBug;
    private CountDownTimer battleTimer;
    private String opponentName;
    private int timeRemaining = 180; // 3 minutes
    
    // Stats (stored in SharedPreferences)
    private int wins = 0;
    private int losses = 0;
    private int trophies = 100;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBattleArenaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        bugDao = DebugMasterDatabase.getInstance(requireContext()).bugDao();
        executor = Executors.newSingleThreadExecutor();
        
        // Load saved stats
        loadSavedStats();
        
        setupUI();
        loadStats();
        playEntranceAnimations();
    }

    private void loadSavedStats() {
        android.content.SharedPreferences prefs = requireContext()
            .getSharedPreferences("battle_stats", android.content.Context.MODE_PRIVATE);
        wins = prefs.getInt("wins", 0);
        losses = prefs.getInt("losses", 0);
        trophies = prefs.getInt("trophies", 100);
    }
    
    private void saveStats() {
        android.content.SharedPreferences prefs = requireContext()
            .getSharedPreferences("battle_stats", android.content.Context.MODE_PRIVATE);
        prefs.edit()
            .putInt("wins", wins)
            .putInt("losses", losses)
            .putInt("trophies", trophies)
            .apply();
    }

    private void playEntranceAnimations() {
        View[] buttons = {binding.buttonQuickMatch, binding.buttonChallengeFriend, 
                          binding.buttonCreateRoom, binding.buttonJoinRoom};
        for (int i = 0; i < buttons.length; i++) {
            View button = buttons[i];
            if (button != null) {
                button.setAlpha(0f);
                button.setTranslationX(-100f);
                button.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setStartDelay(200 + (i * 100L))
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    private void setupUI() {
        // Back button
        if (binding.buttonBack != null) {
            binding.buttonBack.setOnClickListener(v -> {
                soundManager.playButtonClick();
                if (isInBattle) {
                    confirmExitBattle();
                } else if (isMatchmaking) {
                    cancelMatchmaking();
                } else {
                    Navigation.findNavController(v).navigateUp();
                }
            });
        }

        // Quick Match button
        if (binding.buttonQuickMatch != null) {
            binding.buttonQuickMatch.setOnClickListener(v -> {
                soundManager.playButtonClick();
                AnimationUtil.animatePress(v, this::startMatchmaking);
            });
        }

        // Challenge Friend button
        if (binding.buttonChallengeFriend != null) {
            binding.buttonChallengeFriend.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Toast.makeText(requireContext(), "Coming soon! Use Quick Match for now.", Toast.LENGTH_SHORT).show();
            });
        }

        // Create Room button
        if (binding.buttonCreateRoom != null) {
            binding.buttonCreateRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Toast.makeText(requireContext(), "Coming soon! Use Quick Match for now.", Toast.LENGTH_SHORT).show();
            });
        }

        // Join Room button
        if (binding.buttonJoinRoom != null) {
            binding.buttonJoinRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Toast.makeText(requireContext(), "Coming soon! Use Quick Match for now.", Toast.LENGTH_SHORT).show();
            });
        }

        // Cancel matchmaking button
        if (binding.buttonCancelMatchmaking != null) {
            binding.buttonCancelMatchmaking.setOnClickListener(v -> {
                soundManager.playButtonClick();
                cancelMatchmaking();
            });
        }
    }

    private void loadStats() {
        if (binding.textWins != null) {
            binding.textWins.setText(String.valueOf(wins));
        }
        if (binding.textLosses != null) {
            binding.textLosses.setText(String.valueOf(losses));
        }
        if (binding.textWinRate != null) {
            int total = wins + losses;
            int winRate = total > 0 ? (wins * 100) / total : 0;
            binding.textWinRate.setText(winRate + "%");
        }
        if (binding.textTrophies != null) {
            binding.textTrophies.setText(String.format("%,d", trophies));
        }
    }

    private void startMatchmaking() {
        isMatchmaking = true;
        
        // Show matchmaking overlay
        if (binding.layoutMatchmaking != null) {
            binding.layoutMatchmaking.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(binding.layoutMatchmaking);
        }
        if (binding.layoutMainMenu != null) {
            AnimationUtil.fadeOut(binding.layoutMainMenu);
        }
        
        // Load a random bug from database
        loadRandomBugForBattle();
    }
    
    private void loadRandomBugForBattle() {
        executor.execute(() -> {
            try {
                int bugCount = bugDao.getBugCount();
                if (bugCount > 0) {
                    // Pick a random bug ID (1 to bugCount)
                    Random random = new Random();
                    int randomId = random.nextInt(bugCount) + 1;
                    currentBug = bugDao.getBugByIdSync(randomId);
                    
                    // If null, try another one
                    if (currentBug == null) {
                        for (int i = 1; i <= bugCount && currentBug == null; i++) {
                            currentBug = bugDao.getBugByIdSync(i);
                        }
                    }
                    
                    handler.post(() -> {
                        if (isMatchmaking && currentBug != null) {
                            simulateMatchmaking();
                        } else {
                            Toast.makeText(requireContext(), "No bugs available.", Toast.LENGTH_SHORT).show();
                            cancelMatchmaking();
                        }
                    });
                } else {
                    handler.post(() -> {
                        Toast.makeText(requireContext(), "No bugs available. Please try again later.", Toast.LENGTH_SHORT).show();
                        cancelMatchmaking();
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    Toast.makeText(requireContext(), "Error loading bug: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cancelMatchmaking();
                });
            }
        });
    }

    private void simulateMatchmaking() {
        String[] statusMessages = {
            "Searching for opponent...",
            "Found match!",
            "Loading challenge..."
        };

        for (int i = 0; i < statusMessages.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (isMatchmaking && binding != null && binding.textMatchmakingStatus != null) {
                    binding.textMatchmakingStatus.setText(statusMessages[index]);
                    
                    if (index == statusMessages.length - 1) {
                        handler.postDelayed(this::showBattleChallenge, 1000);
                    }
                }
            }, (i + 1) * 1000L);
        }
    }

    private void showBattleChallenge() {
        if (getContext() == null || !isMatchmaking || currentBug == null) return;

        // Generate opponent
        String[] opponents = {"CodeNinja", "BugSlayer", "DebugQueen", "JavaMaster", "ByteHunter"};
        opponentName = opponents[new Random().nextInt(opponents.length)] + new Random().nextInt(100);

        new AlertDialog.Builder(requireContext())
            .setTitle("⚔️ Battle Found!")
            .setMessage("Opponent: " + opponentName + "\n\n" +
                "Challenge: " + currentBug.getTitle() + "\n" +
                "Difficulty: " + currentBug.getDifficulty() + "\n" +
                "Category: " + currentBug.getCategory() + "\n\n" +
                "Time Limit: 3 minutes\n\n" +
                "Fix the bug before time runs out to WIN!")
            .setPositiveButton("Start Battle!", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                startActualBattle();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                cancelMatchmaking();
            })
            .setCancelable(false)
            .show();
    }

    private void startActualBattle() {
        isMatchmaking = false;
        isInBattle = true;
        
        // Hide matchmaking, show battle UI
        if (binding.layoutMatchmaking != null) {
            binding.layoutMatchmaking.setVisibility(View.GONE);
        }
        
        // Show the battle dialog with the actual bug
        showBugChallengeDialog();
    }
    
    private void showBugChallengeDialog() {
        if (getContext() == null || currentBug == null) return;
        
        // Create input for solution
        EditText inputFix = new EditText(requireContext());
        inputFix.setHint("Type your fixed code here...");
        inputFix.setMinLines(5);
        inputFix.setGravity(android.view.Gravity.TOP);
        inputFix.setText(currentBug.getBrokenCode());
        inputFix.setTypeface(android.graphics.Typeface.MONOSPACE);
        inputFix.setTextSize(12);
        
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        inputFix.setPadding(padding, padding, padding, padding);
        
        // Build the challenge text
        String challengeText = "👤 VS " + opponentName + "\n" +
            "⏱️ Time: 3:00\n\n" +
            "📋 " + currentBug.getTitle() + "\n\n" +
            currentBug.getDescription() + "\n\n" +
            "🔴 BUGGY CODE:\n" + currentBug.getBrokenCode() + "\n\n" +
            "Fix the code below:";
        
        AlertDialog battleDialog = new AlertDialog.Builder(requireContext())
            .setTitle("⚔️ BATTLE: Fix The Bug!")
            .setMessage(challengeText)
            .setView(inputFix)
            .setPositiveButton("Submit Fix", null) // Set later to prevent auto-dismiss
            .setNegativeButton("Give Up", (dialog, which) -> {
                endBattle(false, "You gave up!");
            })
            .setCancelable(false)
            .create();
        
        battleDialog.setOnShowListener(dialog -> {
            // Start the countdown timer
            startBattleTimer(battleDialog);
            
            // Override submit button to check answer
            battleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String userFix = inputFix.getText().toString().trim();
                checkSolution(userFix, battleDialog);
            });
        });
        
        battleDialog.show();
    }
    
    private void startBattleTimer(AlertDialog dialog) {
        timeRemaining = 180; // 3 minutes
        
        battleTimer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                
                // Update dialog title with timer
                if (dialog.isShowing()) {
                    dialog.setTitle(String.format("⚔️ BATTLE: %d:%02d remaining", minutes, seconds));
                }
            }
            
            @Override
            public void onFinish() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                endBattle(false, "Time ran out!");
            }
        }.start();
    }
    
    private void checkSolution(String userFix, AlertDialog battleDialog) {
        if (currentBug == null) return;
        
        String correctSolution = currentBug.getFixedCode().trim();
        String normalizedUserFix = normalizeCode(userFix);
        String normalizedCorrect = normalizeCode(correctSolution);
        
        // Cancel timer
        if (battleTimer != null) {
            battleTimer.cancel();
        }
        
        battleDialog.dismiss();
        
        // Check if solution is correct (flexible matching)
        boolean isCorrect = checkCodeMatch(normalizedUserFix, normalizedCorrect);
        
        if (isCorrect) {
            endBattle(true, "Your fix was correct!");
        } else {
            // Show correct answer and mark as loss
            showCorrectAnswerAndLose(correctSolution);
        }
    }
    
    private String normalizeCode(String code) {
        // Remove extra whitespace, normalize line endings
        return code.replaceAll("\\s+", " ")
                   .replaceAll("\\s*;\\s*", ";")
                   .replaceAll("\\s*\\{\\s*", "{")
                   .replaceAll("\\s*\\}\\s*", "}")
                   .replaceAll("\\s*\\(\\s*", "(")
                   .replaceAll("\\s*\\)\\s*", ")")
                   .trim()
                   .toLowerCase();
    }
    
    private boolean checkCodeMatch(String userCode, String correctCode) {
        // Exact match
        if (userCode.equals(correctCode)) return true;
        
        // Check similarity (at least 75% similar)
        int maxLen = Math.max(userCode.length(), correctCode.length());
        if (maxLen == 0) return false;
        
        int distance = levenshteinDistance(userCode, correctCode);
        double similarity = 1.0 - ((double) distance / maxLen);
        
        return similarity >= 0.75; // 75% similarity threshold
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
    
    private void showCorrectAnswerAndLose(String correctSolution) {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("❌ Incorrect!")
            .setMessage("Your fix was wrong.\n\n" +
                "✅ CORRECT SOLUTION:\n" + correctSolution + "\n\n" +
                "📖 Explanation:\n" + (currentBug != null ? currentBug.getExplanation() : ""))
            .setPositiveButton("Got it", (dialog, which) -> {
                endBattle(false, "Wrong answer");
            })
            .setCancelable(false)
            .show();
    }
    
    private void endBattle(boolean won, String reason) {
        isInBattle = false;
        
        if (battleTimer != null) {
            battleTimer.cancel();
            battleTimer = null;
        }
        
        int trophyChange = won ? (25 + new Random().nextInt(15)) : -(10 + new Random().nextInt(10));
        int xpEarned = won ? 50 : 10;
        
        if (won) {
            wins++;
            soundManager.playSound(SoundManager.Sound.VICTORY);
        } else {
            losses++;
            soundManager.playSound(SoundManager.Sound.DEFEAT);
        }
        trophies = Math.max(0, trophies + trophyChange);
        saveStats();
        loadStats();
        
        // Show result
        if (getContext() != null) {
            String title = won ? "🎉 VICTORY!" : "😢 Defeat";
            String message = won ? 
                "You beat " + opponentName + "!\n" + reason + "\n\n" +
                "🏆 +" + trophyChange + " Trophies\n" +
                "⭐ +" + xpEarned + " XP" :
                "You lost to " + opponentName + ".\n" + reason + "\n\n" +
                "🏆 " + trophyChange + " Trophies\n" +
                "⭐ +" + xpEarned + " XP (participation)";
            
            new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    startMatchmaking();
                })
                .setNegativeButton("Done", (dialog, which) -> {
                    showMainMenu();
                })
                .show();
        }
        
        currentBug = null;
        opponentName = null;
    }
    
    private void confirmExitBattle() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Exit Battle?")
            .setMessage("If you leave now, you will lose this battle.")
            .setPositiveButton("Leave (Lose)", (dialog, which) -> {
                if (battleTimer != null) {
                    battleTimer.cancel();
                }
                endBattle(false, "You left the battle");
            })
            .setNegativeButton("Stay", null)
            .show();
    }
    
    private void showMainMenu() {
        if (binding.layoutMatchmaking != null) {
            binding.layoutMatchmaking.setVisibility(View.GONE);
        }
        if (binding.layoutMainMenu != null) {
            binding.layoutMainMenu.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(binding.layoutMainMenu);
        }
    }

    private void cancelMatchmaking() {
        isMatchmaking = false;
        handler.removeCallbacksAndMessages(null);
        showMainMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (battleTimer != null) {
            battleTimer.cancel();
        }
        if (executor != null) {
            executor.shutdown();
        }
        binding = null;
    }
}
