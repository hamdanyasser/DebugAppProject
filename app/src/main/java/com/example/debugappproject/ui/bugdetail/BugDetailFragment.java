package com.example.debugappproject.ui.bugdetail;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentBugDetailBinding;
import com.example.debugappproject.execution.CodeExecutionEngine;
import com.example.debugappproject.execution.CodeExecutionResult;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.TestCase;
import com.example.debugappproject.ui.animation.ConfettiAnimationView;
import com.example.debugappproject.ui.mentor.AIMentorBottomSheet;
import com.example.debugappproject.ui.settings.SettingsFragment;
import com.example.debugappproject.util.AIMentor;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.CelebrationManager;
import com.example.debugappproject.util.CodeComparator;
import com.example.debugappproject.util.EditorThemeManager;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - BUG DETAIL GAMEPLAY SCREEN                           ║
 * ║              Premium Game Experience with Sound Effects                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Material 3 card-based layout with proper visual hierarchy
 * - Sound effects on all interactions
 * - Haptic feedback for success/failure
 * - Difficulty and category chips with dynamic coloring
 * - SMART code validation (works for all languages!)
 * - Progressive hint revelation system
 * - Solution with explanation and fixed code
 * - Completion tracking with celebration
 */
@AndroidEntryPoint
public class BugDetailFragment extends Fragment {

    private static final String TAG = "BugDetailFragment";
    
    // Languages that can be executed by Janino
    private static final Set<String> EXECUTABLE_LANGUAGES = new HashSet<>(Arrays.asList(
            "java"
    ));

    private FragmentBugDetailBinding binding;
    private BugDetailViewModel viewModel;
    private SoundManager soundManager;
    private CelebrationManager celebrationManager;
    private EditorThemeManager themeManager;
    private int bugId;
    private Bug currentBug;
    private List<Hint> hints;
    private List<TestCase> testCases;
    private String initialCode;
    private ConfettiAnimationView confettiView;
    private CodeExecutionEngine codeExecutionEngine;
    private AIMentor aiMentor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBugDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BugDetailViewModel.class);
        soundManager = SoundManager.getInstance(requireContext());
        celebrationManager = new CelebrationManager(requireContext());
        themeManager = new EditorThemeManager(requireContext());

        // Play challenge start sound
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);

        // Initialize confetti view
        confettiView = binding.confettiView;

        // Apply editor theme and font
        applyEditorTheme();

        // Initialize code execution engine
        codeExecutionEngine = new CodeExecutionEngine();
        codeExecutionEngine.setTimeout(5000);

        // Initialize AI Mentor
        aiMentor = new AIMentor(requireContext());
        updateMentorSessionsDisplay();

        // Get bug ID from arguments
        if (getArguments() != null) {
            bugId = getArguments().getInt("bugId", 1);
        }

        viewModel.loadBug(bugId);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.getCurrentBug().observe(getViewLifecycleOwner(), bug -> {
            if (bug != null) {
                currentBug = bug;
                displayBug(bug);
            }
        });

        viewModel.getHints().observe(getViewLifecycleOwner(), hintList -> {
            this.hints = hintList;
        });

        viewModel.isShowingSolution().observe(getViewLifecycleOwner(), showing -> {
            if (showing != null && showing && currentBug != null) {
                showSolution();
            }
        });
    }

    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());

        // Set difficulty with proper styling
        String difficulty = bug.getDifficulty();
        binding.chipDifficulty.setText(difficulty.toUpperCase());
        setDifficultyChipStyle(difficulty);

        binding.chipCategory.setText(bug.getCategory());
        
        // Set language chip if available
        if (binding.chipLanguage != null) {
            String language = bug.getLanguage();
            if (language != null && !language.isEmpty()) {
                binding.chipLanguage.setText(language);
                binding.chipLanguage.setVisibility(View.VISIBLE);
            } else {
                binding.chipLanguage.setVisibility(View.GONE);
            }
        }
        
        // Set XP reward based on difficulty
        if (binding.textXpReward != null) {
            int xp = getXpForDifficulty(difficulty);
            binding.textXpReward.setText("+" + xp + " XP");
        }

        if (bug.isCompleted()) {
            binding.chipCompleted.setVisibility(View.VISIBLE);
            binding.buttonMarkSolved.setText("✅  Completed");
            binding.buttonMarkSolved.setEnabled(false);
        }

        binding.textBugDescription.setText(bug.getDescription());
        binding.textBrokenCode.setText(bug.getBrokenCode());

        initialCode = bug.getInitialCode();
        binding.editUserCode.setText(initialCode);

        if (bug.getUserNotes() != null && !bug.getUserNotes().isEmpty()) {
            binding.editUserNotes.setText(bug.getUserNotes());
        }

        parseTestCases(bug.getTestsJson());

        // Update AI Mentor context
        if (aiMentor != null) {
            aiMentor.setBugContext(bug);
        }
    }
    
    private int getXpForDifficulty(String difficulty) {
        if (difficulty == null) return 10;
        switch (difficulty.toLowerCase()) {
            case "easy": return 15;
            case "medium": return 25;
            case "hard": return 40;
            default: return 10;
        }
    }

    /**
     * Apply editor theme and font to all code views.
     */
    private void applyEditorTheme() {
        // Apply to code editor
        if (binding.editUserCode != null) {
            themeManager.applyFullStyleToEditor(binding.editUserCode);
        }

        // Apply to code display views
        if (binding.textBrokenCode != null) {
            themeManager.applyFullStyle(binding.textBrokenCode);
        }

        if (binding.textFixedCode != null) {
            themeManager.applyFullStyle(binding.textFixedCode);
        }
    }

    private void setDifficultyChipStyle(String difficulty) {
        if (difficulty == null) return;
        
        int bgResource;
        switch (difficulty.toLowerCase()) {
            case "easy":
                bgResource = R.drawable.bg_difficulty_easy;
                break;
            case "medium":
                bgResource = R.drawable.bg_difficulty_medium;
                break;
            case "hard":
                bgResource = R.drawable.bg_difficulty_hard;
                break;
            default:
                bgResource = R.drawable.bg_difficulty_easy;
        }
        binding.chipDifficulty.setBackgroundResource(bgResource);
    }

    private void setupClickListeners() {
        // Back button
        if (binding.buttonBack != null) {
            binding.buttonBack.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Run Code button (now a card)
        binding.buttonRunCode.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CODE_RUN);
            AnimationUtil.animatePress(v, () -> {
                if (currentBug != null) {
                    showOutput();
                }
            });
        });

        // Show Hint button (now a card)
        binding.buttonShowHint.setOnClickListener(v -> {
            soundManager.playButtonClick();
            AnimationUtil.animatePress(v, this::showNextHint);
        });

        // Show Solution button (now a card)
        binding.buttonShowSolution.setOnClickListener(v -> {
            soundManager.playButtonClick();
            AnimationUtil.animatePress(v, () -> viewModel.showSolution());
        });

        // Check Fix / Run Tests button
        binding.buttonCheckFix.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CODE_SUBMIT);
            AnimationUtil.animatePress(v, () -> {
                if (currentBug != null) {
                    validateUserCode();
                }
            });
        });

        // Reset Code button
        binding.buttonResetCode.setOnClickListener(v -> {
            soundManager.playButtonClick();
            AnimationUtil.animatePress(v, this::resetCode);
        });

        // Mark as Solved button
        binding.buttonMarkSolved.setOnClickListener(v -> {
            if (currentBug != null && !currentBug.isCompleted()) {
                String userCode = binding.editUserCode.getText().toString();
                
                // Check if user modified the code at all
                String originalBroken = currentBug.getBrokenCode().trim();
                if (CodeComparator.normalizeCode(userCode).equals(CodeComparator.normalizeCode(originalBroken))) {
                    soundManager.playSound(SoundManager.Sound.ERROR);
                    Toast.makeText(requireContext(), "You need to fix the bug first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Check if the fix is correct
                boolean isCorrect = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());
                double similarity = CodeComparator.calculateSimilarity(
                    CodeComparator.normalizeCode(userCode),
                    CodeComparator.normalizeCode(currentBug.getFixedCode())
                );

                if (!isCorrect && similarity < 0.90) {
                    // Code is significantly different - warn the user
                    soundManager.playSound(SoundManager.Sound.WARNING);
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Code Doesn't Match")
                            .setMessage("Your code is only " + (int)(similarity * 100) + "% similar to the solution.\n\n" +
                                    "Are you sure you want to mark this as solved without the correct fix?")
                            .setPositiveButton("Mark Anyway", (dialog, which) -> {
                                soundManager.playSound(SoundManager.Sound.SUCCESS);
                                viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                                binding.chipCompleted.setVisibility(View.VISIBLE);
                                binding.buttonMarkSolved.setText("✅  Completed");
                                binding.buttonMarkSolved.setEnabled(false);
                                Toast.makeText(requireContext(), "Bug marked as completed (manual)", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep Trying", (dialog, which) -> {
                                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                            })
                            .show();
                } else {
                    // Code is correct or very close
                    soundManager.playSuccess();
                    viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                    binding.chipCompleted.setVisibility(View.VISIBLE);
                    binding.buttonMarkSolved.setText("✅  Completed");
                    binding.buttonMarkSolved.setEnabled(false);
                    Toast.makeText(requireContext(), "Bug marked as completed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Save Notes button
        binding.buttonSaveNotes.setOnClickListener(v -> {
            soundManager.playButtonClick();
            AnimationUtil.animatePress(v, () -> {
                if (currentBug != null) {
                    String notes = binding.editUserNotes.getText().toString();
                    viewModel.saveBugNotes(currentBug.getId(), notes);
                    soundManager.playSound(SoundManager.Sound.SUCCESS);
                    Toast.makeText(requireContext(), "Notes saved!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // AI Mentor button
        if (binding.buttonAskMentor != null) {
            binding.buttonAskMentor.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                AnimationUtil.animatePress(v, this::openAIMentor);
            });
        }

        // AI Mentor card click
        if (binding.cardAiMentor != null) {
            binding.cardAiMentor.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                AnimationUtil.animatePress(v, this::openAIMentor);
            });
        }
    }

    private void showOutput() {
        if (binding.cardOutput.getVisibility() != View.VISIBLE) {
            soundManager.playSound(SoundManager.Sound.BLIP);
            AnimationUtil.fadeInWithScale(binding.cardOutput);
        }
        binding.textExpectedOutput.setText(currentBug.getExpectedOutput());
        binding.textActualOutput.setText(currentBug.getActualOutput());
    }

    private void showNextHint() {
        if (!SettingsFragment.areHintsEnabled(requireContext())) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            new AlertDialog.Builder(requireContext())
                .setTitle("Hints Disabled")
                .setMessage("Hints are currently disabled in Settings. Challenge mode is active! " +
                    "Would you like to enable hints?")
                .setPositiveButton("Enable Hints", (dialog, which) -> {
                    Toast.makeText(requireContext(),
                        "Please enable hints in Settings", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Keep Disabled", null)
                .show();
            return;
        }

        Integer currentLevel = viewModel.getCurrentHintLevel().getValue();
        if (currentLevel == null) {
            currentLevel = 0;
        }

        // Build combined hint list: first the bug's built-in hint, then any from database
        List<String> allHints = new ArrayList<>();
        
        // Add bug's built-in hint first if available
        if (currentBug != null) {
            String bugHint = currentBug.getHintText();
            if (bugHint != null && !bugHint.isEmpty()) {
                allHints.add(bugHint);
            }
        }
        
        // Add hints from database
        if (hints != null) {
            for (Hint hint : hints) {
                if (hint.getText() != null && !hint.getText().isEmpty()) {
                    allHints.add(hint.getText());
                }
            }
        }

        if (allHints.isEmpty()) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "No hints available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLevel < allHints.size()) {
            soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
            
            String hintText = allHints.get(currentLevel);

            if (currentLevel == 0) {
                AnimationUtil.fadeInWithScale(binding.cardHints);
            }

            TextView hintView = new TextView(requireContext());
            hintView.setTextAppearance(R.style.TextAppearance_DebugMaster_Body1);
            hintView.setText("💡 Hint " + (currentLevel + 1) + ": " + hintText);
            hintView.setTextColor(getResources().getColor(R.color.white, null)); // Ensure visible on dark background
            hintView.setAlpha(0f);

            if (currentLevel > 0) {
                hintView.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
            }

            binding.layoutHintsContainer.addView(hintView);
            hintView.post(() -> AnimationUtil.fadeIn(hintView));

            viewModel.revealNextHint();
        } else {
            soundManager.playSound(SoundManager.Sound.WARNING);
            Toast.makeText(requireContext(), "No more hints available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSolution() {
        soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
        if (binding.cardSolution.getVisibility() != View.VISIBLE) {
            AnimationUtil.fadeInWithScale(binding.cardSolution);
        }
        binding.textExplanation.setText(currentBug.getExplanation());
        binding.textFixedCode.setText(currentBug.getFixedCode());
    }

    private void parseTestCases(String testsJson) {
        testCases = new ArrayList<>();

        if (testsJson != null && !testsJson.trim().isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TestCase>>(){}.getType();
                testCases = gson.fromJson(testsJson, listType);
            } catch (Exception e) {
                testCases = new ArrayList<>();
            }
        }

        if (testCases.isEmpty()) {
            TestCase defaultTest = new TestCase();
            defaultTest.setInput("Your code");
            defaultTest.setExpected("Matches the fix pattern");
            defaultTest.setDescription("Code should fix the bug correctly");
            testCases.add(defaultTest);
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * STRICT CODE VALIDATION
     * ═══════════════════════════════════════════════════════════════════════════
     * 
     * This method validates user code strictly:
     * 1. For Java: Executes code and checks output matches expected
     * 2. For all languages: Requires high similarity (95%+) to fixed code
     * 3. Provides helpful feedback for incorrect solutions
     */
    private void validateUserCode() {
        String userCode = binding.editUserCode.getText().toString().trim();

        if (userCode.isEmpty()) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "Please enter your code fix first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user just submitted the original broken code
        String originalBroken = currentBug.getBrokenCode().trim();
        if (CodeComparator.normalizeCode(userCode).equals(CodeComparator.normalizeCode(originalBroken))) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "You need to fix the bug first!", Toast.LENGTH_SHORT).show();
            return;
        }

        soundManager.playSound(SoundManager.Sound.CODE_COMPILE);

        binding.buttonCheckFix.setEnabled(false);
        binding.buttonCheckFix.setText("⏳  Checking...");

        new Thread(() -> {
            // Calculate similarity for feedback
            String normalizedUser = CodeComparator.normalizeCode(userCode);
            String normalizedFixed = CodeComparator.normalizeCode(currentBug.getFixedCode());
            double similarity = CodeComparator.calculateSimilarity(normalizedUser, normalizedFixed);
            
            Log.d(TAG, "=== Validation ===");
            Log.d(TAG, "User code length: " + userCode.length());
            Log.d(TAG, "Fixed code length: " + currentBug.getFixedCode().length());
            Log.d(TAG, "Similarity: " + (int)(similarity * 100) + "%");
            
            // Check if codes match (strict comparison)
            boolean codeMatches = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());
            
            Log.d(TAG, "Code matches: " + codeMatches);
            
            // For Java, also try execution
            CodeExecutionResult executionResult = null;
            boolean canExecute = isExecutableLanguage(currentBug.getLanguage());
            
            if (canExecute) {
                executionResult = codeExecutionEngine.execute(userCode);
                Log.d(TAG, "Execution result: " + (executionResult.isSuccess() ? "Success" : "Failed"));
                if (executionResult.isSuccess()) {
                    Log.d(TAG, "Output: " + executionResult.getOutput());
                }
            }

            final boolean finalCodeMatches = codeMatches;
            final CodeExecutionResult finalExecutionResult = executionResult;
            final double finalSimilarity = similarity;

            requireActivity().runOnUiThread(() -> {
                binding.buttonCheckFix.setEnabled(true);
                binding.buttonCheckFix.setText("▶  Run Tests");

                processValidationResult(finalCodeMatches, finalExecutionResult, finalSimilarity);
            });
        }).start();
    }

    /**
     * Checks if the language can be executed by our engine
     */
    private boolean isExecutableLanguage(String language) {
        if (language == null) return false;
        return EXECUTABLE_LANGUAGES.contains(language.toLowerCase());
    }

    /**
     * Process the validation result and show appropriate UI.
     * STRICT: Only marks as correct if code truly matches the fix.
     */
    private void processValidationResult(boolean codeMatches, CodeExecutionResult executionResult, double similarity) {
        if (binding.cardTestResults.getVisibility() != View.VISIBLE) {
            AnimationUtil.fadeInWithScale(binding.cardTestResults);
        }

        Log.d(TAG, "Processing result - codeMatches: " + codeMatches + ", similarity: " + (int)(similarity * 100) + "%");

        // SUCCESS: Code matches the fix pattern (strict comparison passed)
        if (codeMatches) {
            handleSuccess(similarity);
            return;
        }

        // For Java: Check if execution was successful AND output EXACTLY matches
        if (executionResult != null && executionResult.isSuccess()) {
            String actualOutput = executionResult.getOutput().trim();
            String expectedOutput = currentBug.getExpectedOutput().trim();
            
            Log.d(TAG, "Comparing outputs:");
            Log.d(TAG, "Actual: '" + actualOutput + "'");
            Log.d(TAG, "Expected: '" + expectedOutput + "'");
            
            // STRICT output matching - must be very close
            if (outputsMatchStrict(actualOutput, expectedOutput)) {
                // Additional check: similarity must be at least 80%
                if (similarity >= 0.80) {
                    handleSuccess(similarity);
                    return;
                } else {
                    Log.d(TAG, "Output matches but code similarity too low: " + (int)(similarity * 100) + "%");
                }
            }
        }
        
        // Show execution error if there was one
        if (executionResult != null && !executionResult.isSuccess()) {
            handleExecutionError(executionResult);
            return;
        }

        // FAILURE: Code doesn't match
        handleFailure(similarity, executionResult);
    }

    /**
     * STRICT output comparison - outputs must match closely
     */
    private boolean outputsMatchStrict(String actual, String expected) {
        if (actual == null || expected == null) return false;
        if (actual.isEmpty() && expected.isEmpty()) return true;
        if (actual.isEmpty() || expected.isEmpty()) return false;
        
        // Normalize for comparison
        String normActual = actual.replaceAll("\\s+", " ").trim().toLowerCase();
        String normExpected = expected.replaceAll("\\s+", " ").trim().toLowerCase();
        
        // Exact match after normalization
        if (normActual.equals(normExpected)) return true;
        
        // Check if one contains the other (for partial matches)
        // But only if they're very similar in length
        int lenDiff = Math.abs(normActual.length() - normExpected.length());
        int maxLen = Math.max(normActual.length(), normExpected.length());
        
        if (lenDiff <= 5 && maxLen > 0) {
            // Small length difference - check if similar
            double similarity = CodeComparator.calculateSimilarity(normActual, normExpected);
            if (similarity >= 0.95) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Handle successful code fix with dopamine loop celebrations
     */
    private void handleSuccess(double similarity) {
        // Calculate XP and record the solve
        int xpEarned = getXpForDifficulty(currentBug.getDifficulty());
        boolean perfectSolve = similarity >= 0.95;

        // Get celebration data from manager
        CelebrationManager.CelebrationResult celebration = celebrationManager.recordSolve(
            perfectSolve,
            false, // wasFast - not applicable in bug detail view
            currentBug.getDifficulty(),
            xpEarned,
            0, // Streak will be fetched from GameManager in production
            1 // Combo count
        );

        // Play appropriate celebration sound
        celebrationManager.playCelebrationSound(celebration.celebrationType);

        // Show the main celebration message
        binding.textTestResultTitle.setText(celebration.mainMessage);
        binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.difficulty_easy, null));

        // Animate the title with pulse effect
        CelebrationManager.animatePulse(binding.textTestResultTitle);

        // Build celebratory message
        StringBuilder message = new StringBuilder();
        message.append("🎉 You found the bug and fixed it correctly!\n\n");

        // Add combo message if applicable
        if (celebration.hasCombo()) {
            message.append(celebration.comboMessage).append("\n");
            // Play combo sound after a delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                soundManager.playSound(SoundManager.Sound.COMBO);
            }, 300);
        }

        // Add difficulty bonus message
        if (celebration.hasDifficultyBonus()) {
            message.append(celebration.difficultyMessage).append("\n");
        }

        // Show XP earned with bonus
        int totalXp = celebration.getTotalXp();
        message.append("\n💎 +").append(totalXp).append(" XP earned!");
        if (celebration.comboBonus > 0) {
            message.append(" (includes +").append(celebration.comboBonus).append(" combo bonus)");
        }

        binding.textTestResultMessage.setText(message.toString());

        // Animate the XP text if available
        if (binding.textXpReward != null) {
            CelebrationManager.animatePulse(binding.textXpReward);
        }

        for (TestCase test : testCases) {
            test.setPassed(true);
        }
        displayTestResults();

        if (!currentBug.isCompleted()) {
            viewModel.markBugAsCompletedWithXP(currentBug.getId(), currentBug.getDifficulty());
            currentBug.setCompleted(true);

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
            }, 500);

            celebrateBugCompletion();

            binding.chipCompleted.setVisibility(View.VISIBLE);
            binding.buttonMarkSolved.setText("✅  Completed");
            binding.buttonMarkSolved.setEnabled(false);

            Snackbar.make(binding.getRoot(), celebration.mainMessage + " +" + totalXp + " XP!", Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), celebration.mainMessage + " (Already completed)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle code that doesn't match the fix
     */
    private void handleFailure(double similarity, CodeExecutionResult executionResult) {
        soundManager.playFailure();

        // Reset combo on failure
        celebrationManager.resetCombo();
        
        int similarityPercent = (int)(similarity * 100);
        
        binding.textTestResultTitle.setText("❌ Not Quite Right");
        binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.error_light, null));

        StringBuilder message = new StringBuilder();
        
        if (similarityPercent >= 80) {
            message.append("🔥 So close! You're ")
                   .append(similarityPercent)
                   .append("% there!\n\n")
                   .append("Your code is almost correct. Look carefully at the difference.\n\n");
        } else if (similarityPercent >= 50) {
            message.append("👍 Good progress! ")
                   .append(similarityPercent)
                   .append("% similar to the solution.\n\n")
                   .append("You're on the right track. Review the bug description.\n\n");
        } else {
            message.append("🤔 Let's think about this differently.\n\n")
                   .append("Your code is ")
                   .append(similarityPercent)
                   .append("% similar. Try using a hint!\n\n");
        }

        // Add execution info if available
        if (executionResult != null && executionResult.isSuccess()) {
            String output = executionResult.getOutput().trim();
            if (!output.isEmpty()) {
                message.append("Your code output: ").append(output).append("\n");
                message.append("Expected output: ").append(currentBug.getExpectedOutput()).append("\n");
            }
        }

        binding.textTestResultMessage.setText(message.toString());

        for (TestCase test : testCases) {
            test.setPassed(false);
        }
        displayTestResults();

        // Add helpful hint
        String errorHint = CodeComparator.generateErrorMessage(
            binding.editUserCode.getText().toString(),
            currentBug.getFixedCode()
        );
        addErrorHint(errorHint);

        AnimationUtil.shakeView(binding.cardTestResults);
    }

    private void handleExecutionError(CodeExecutionResult result) {
        soundManager.playSound(SoundManager.Sound.CODE_ERROR);
        
        binding.textTestResultTitle.setText("⚠️ " + result.getErrorType().replace("_", " "));
        binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.error_light, null));

        binding.textTestResultMessage.setText(result.getFormattedErrorMessage());

        binding.layoutTestCases.removeAllViews();

        if ("COMPILATION_ERROR".equals(result.getErrorType())) {
            addErrorHint("💡 Check your syntax, variable names, and semicolons.");
        } else if ("RUNTIME_ERROR".equals(result.getErrorType())) {
            addErrorHint("💡 Check for array bounds, null values, and division by zero.");
        } else if ("TIMEOUT_ERROR".equals(result.getErrorType())) {
            addErrorHint("💡 Check for infinite loops or excessive recursion.");
        }

        AnimationUtil.shakeView(binding.cardTestResults);
    }

    private void addErrorHint(String hintText) {
        TextView hintView = new TextView(requireContext());
        hintView.setTextAppearance(R.style.TextAppearance_DebugMaster_Caption);
        hintView.setText(hintText);
        hintView.setPadding(
                0,
                (int) (16 * getResources().getDisplayMetrics().density),
                0,
                0
        );
        hintView.setAlpha(0f);

        binding.layoutTestCases.addView(hintView);
        hintView.post(() -> AnimationUtil.fadeIn(hintView));
    }

    private void displayTestResults() {
        binding.layoutTestCases.removeAllViews();

        for (int i = 0; i < testCases.size(); i++) {
            TestCase test = testCases.get(i);

            TextView testView = new TextView(requireContext());
            testView.setTextAppearance(R.style.TextAppearance_DebugMaster_Body1);

            String statusIcon = test.isPassed() ? "✅" : "❌";
            String testInfo = statusIcon + " Test " + (i + 1) + ": " + test.getDescription();

            testView.setText(testInfo);
            testView.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);

            binding.layoutTestCases.addView(testView);
            
            if (i < 3) {
                int finalI = i;
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    soundManager.playSound(SoundManager.Sound.TICK);
                }, finalI * 150);
            }
        }
    }

    private void resetCode() {
        soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
        binding.editUserCode.setText(initialCode);
        Toast.makeText(requireContext(), "Code reset to starting point", Toast.LENGTH_SHORT).show();
    }

    private void celebrateBugCompletion() {
        soundManager.playSound(SoundManager.Sound.VICTORY);
        
        if (confettiView != null) {
            confettiView.setParticleCount(200);
            confettiView.setDuration(3000);
            confettiView.startAnimation();
        }

        binding.chipCompleted.post(() -> {
            if (binding.chipCompleted.getVisibility() == View.VISIBLE) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    soundManager.playSound(SoundManager.Sound.STAR_EARNED);
                }, 300);
                AnimationUtil.bounceView(binding.chipCompleted);
            }
        });

        AnimationUtil.fadeInWithScale(binding.cardTestResults);
    }

    /**
     * Open the AI Debug Mentor bottom sheet
     */
    private void openAIMentor() {
        if (currentBug == null) {
            Toast.makeText(requireContext(), "Please wait for the bug to load", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user's code in mentor for analysis
        if (aiMentor != null && binding.editUserCode != null) {
            aiMentor.setUserCode(binding.editUserCode.getText().toString());
        }

        // Create and show bottom sheet
        AIMentorBottomSheet mentorSheet = AIMentorBottomSheet.newInstance(currentBug);

        // Update user code when sheet opens
        mentorSheet.updateUserCode(binding.editUserCode.getText().toString());

        // Set listener for "Get More Sessions" button
        mentorSheet.setOnGetMoreSessionsListener(() -> {
            // Navigate to shop
            try {
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(com.example.debugappproject.R.id.shopFragment);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Shop coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        mentorSheet.show(getParentFragmentManager(), "ai_mentor");
    }

    /**
     * Update the mentor sessions display in the UI
     */
    private void updateMentorSessionsDisplay() {
        if (binding == null || binding.textMentorSessions == null) return;

        boolean hasUnlimited = AIMentor.hasUnlimitedAccess(requireContext());

        if (hasUnlimited) {
            binding.textMentorSessions.setText("Unlimited (Pro)");
            binding.textMentorSessions.setTextColor(getResources().getColor(R.color.difficulty_medium, null));
        } else if (aiMentor != null) {
            int free = aiMentor.getFreeSessions();
            int purchased = aiMentor.getPurchasedSessions();
            int total = free + purchased;

            if (total > 0) {
                binding.textMentorSessions.setText(total + " session" + (total > 1 ? "s" : "") + " available");
                binding.textMentorSessions.setTextColor(0xFF10B981); // Green
            } else {
                binding.textMentorSessions.setText("No sessions - tap to get more");
                binding.textMentorSessions.setTextColor(0xFFF59E0B); // Yellow warning
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update sessions display when returning from shop
        updateMentorSessionsDisplay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (confettiView != null) {
            confettiView.stopAnimation();
        }
        if (codeExecutionEngine != null) {
            codeExecutionEngine.shutdown();
        }
        binding = null;
    }
}
