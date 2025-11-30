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
import com.example.debugappproject.ui.settings.SettingsFragment;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.CodeComparator;
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
    private int bugId;
    private Bug currentBug;
    private List<Hint> hints;
    private List<TestCase> testCases;
    private String initialCode;
    private ConfettiAnimationView confettiView;
    private CodeExecutionEngine codeExecutionEngine;

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

        // Play challenge start sound
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);

        // Initialize confetti view
        confettiView = binding.confettiView;

        // Initialize code execution engine
        codeExecutionEngine = new CodeExecutionEngine();
        codeExecutionEngine.setTimeout(5000);

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

        binding.chipDifficulty.setText(bug.getDifficulty());
        setDifficultyChipColor(bug.getDifficulty());

        binding.chipCategory.setText(bug.getCategory());

        if (bug.isCompleted()) {
            binding.chipCompleted.setVisibility(View.VISIBLE);
            binding.buttonMarkSolved.setText("Completed ✓");
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
    }

    private void setDifficultyChipColor(String difficulty) {
        int colorRes;
        switch (difficulty.toLowerCase()) {
            case "easy":
                colorRes = R.color.difficulty_easy;
                break;
            case "medium":
                colorRes = R.color.difficulty_medium;
                break;
            case "hard":
                colorRes = R.color.difficulty_hard;
                break;
            default:
                colorRes = R.color.difficulty_easy;
        }
        binding.chipDifficulty.setChipBackgroundColorResource(colorRes);
    }

    private void setupClickListeners() {
        // Run Code button
        binding.buttonRunCode.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CODE_RUN);
            AnimationUtil.animatePress(v, () -> {
                if (currentBug != null) {
                    showOutput();
                }
            });
        });

        // Show Hint button
        binding.buttonShowHint.setOnClickListener(v -> {
            soundManager.playButtonClick();
            AnimationUtil.animatePress(v, this::showNextHint);
        });

        // Show Solution button
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
                boolean testsPassed = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());

                if (!testsPassed) {
                    soundManager.playSound(SoundManager.Sound.WARNING);
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Mark as Solved?")
                            .setMessage("Are you sure? Your code doesn't quite match the solution yet.")
                            .setPositiveButton("Yes, Mark as Solved", (dialog, which) -> {
                                soundManager.playSuccess();
                                viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                                binding.chipCompleted.setVisibility(View.VISIBLE);
                                binding.buttonMarkSolved.setText("Completed ✓");
                                binding.buttonMarkSolved.setEnabled(false);
                                Toast.makeText(requireContext(), "Bug marked as completed!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep Trying", (dialog, which) -> {
                                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                            })
                            .show();
                } else {
                    soundManager.playSuccess();
                    viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                    binding.chipCompleted.setVisibility(View.VISIBLE);
                    binding.buttonMarkSolved.setText("Completed ✓");
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

        if (hints == null || hints.isEmpty()) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "No hints available", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer currentLevel = viewModel.getCurrentHintLevel().getValue();
        if (currentLevel == null) {
            currentLevel = 0;
        }

        if (currentLevel < hints.size()) {
            soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
            
            Hint hint = hints.get(currentLevel);

            if (currentLevel == 0) {
                AnimationUtil.fadeInWithScale(binding.cardHints);
            }

            TextView hintView = new TextView(requireContext());
            hintView.setTextAppearance(R.style.TextAppearance_DebugMaster_Body1);
            hintView.setText("💡 Hint " + (currentLevel + 1) + ": " + hint.getText());
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
     * SMART CODE VALIDATION
     * ═══════════════════════════════════════════════════════════════════════════
     * 
     * This method intelligently validates user code:
     * 1. For Java: Tries to execute, but also accepts code comparison
     * 2. For Python/JS/Kotlin/etc: Uses smart code comparison only
     * 3. Provides helpful feedback regardless of language
     */
    private void validateUserCode() {
        String userCode = binding.editUserCode.getText().toString().trim();

        if (userCode.isEmpty()) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "Please enter your code fix first", Toast.LENGTH_SHORT).show();
            return;
        }

        soundManager.playSound(SoundManager.Sound.CODE_COMPILE);

        binding.buttonCheckFix.setEnabled(false);
        binding.buttonCheckFix.setText("Checking...");

        new Thread(() -> {
            // First, check if the code matches the fix pattern (works for ALL languages)
            boolean codeMatches = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());
            
            Log.d(TAG, "Code comparison result: " + codeMatches);
            Log.d(TAG, "Language: " + currentBug.getLanguage());
            
            // Calculate similarity for feedback
            double similarity = CodeComparator.calculateSimilarity(
                CodeComparator.extractCoreFix(userCode),
                CodeComparator.extractCoreFix(currentBug.getFixedCode())
            );
            
            // For Java, also try execution if code doesn't match exactly
            CodeExecutionResult executionResult = null;
            boolean canExecute = isExecutableLanguage(currentBug.getLanguage());
            
            if (canExecute && !codeMatches) {
                executionResult = codeExecutionEngine.execute(userCode);
            }

            final boolean finalCodeMatches = codeMatches;
            final CodeExecutionResult finalExecutionResult = executionResult;
            final double finalSimilarity = similarity;

            requireActivity().runOnUiThread(() -> {
                binding.buttonCheckFix.setEnabled(true);
                binding.buttonCheckFix.setText("Check Fix");

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
     * Process the validation result and show appropriate UI
     */
    private void processValidationResult(boolean codeMatches, CodeExecutionResult executionResult, double similarity) {
        if (binding.cardTestResults.getVisibility() != View.VISIBLE) {
            AnimationUtil.fadeInWithScale(binding.cardTestResults);
        }

        // SUCCESS: Code matches the fix pattern!
        if (codeMatches) {
            handleSuccess(similarity);
            return;
        }

        // For Java: Check if execution was successful AND output matches
        if (executionResult != null) {
            if (executionResult.isSuccess()) {
                String actualOutput = executionResult.getOutput().trim();
                String expectedOutput = currentBug.getExpectedOutput().trim();
                
                // Check if output looks like it matches (be lenient)
                if (outputsMatch(actualOutput, expectedOutput)) {
                    handleSuccess(similarity);
                    return;
                }
            }
            
            // Show execution error if there was one
            if (!executionResult.isSuccess()) {
                handleExecutionError(executionResult);
                return;
            }
        }

        // FAILURE: Code doesn't match
        handleFailure(similarity, executionResult);
    }

    /**
     * Lenient output comparison
     */
    private boolean outputsMatch(String actual, String expected) {
        if (actual == null || expected == null) return false;
        
        // Exact match
        if (actual.equals(expected)) return true;
        
        // Case-insensitive match
        if (actual.equalsIgnoreCase(expected)) return true;
        
        // Trim and compare
        if (actual.trim().equals(expected.trim())) return true;
        
        // Check if actual contains expected (for multi-line outputs)
        if (actual.contains(expected) || expected.contains(actual)) return true;
        
        // Normalize whitespace and compare
        String normalizedActual = actual.replaceAll("\\s+", " ").trim();
        String normalizedExpected = expected.replaceAll("\\s+", " ").trim();
        return normalizedActual.equals(normalizedExpected);
    }

    /**
     * Handle successful code fix
     */
    private void handleSuccess(double similarity) {
        soundManager.playSound(SoundManager.Sound.SUCCESS);
        
        binding.textTestResultTitle.setText("✅ Correct! Bug Fixed!");
        binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
        
        // Animate title
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(binding.textTestResultTitle,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f));
        pulse.setDuration(500);
        pulse.setInterpolator(new OvershootInterpolator(2f));
        pulse.start();

        String message = "🎉 Congratulations! You found the bug and fixed it correctly!\n\n" +
                "Your solution matches the expected fix. Great debugging skills!";
        binding.textTestResultMessage.setText(message);

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
            binding.buttonMarkSolved.setText("Completed ✓");
            binding.buttonMarkSolved.setEnabled(false);

            Snackbar.make(binding.getRoot(), "🏆 Bug solved! XP awarded!", Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Correct! (Already completed)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle code that doesn't match the fix
     */
    private void handleFailure(double similarity, CodeExecutionResult executionResult) {
        soundManager.playFailure();
        
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
