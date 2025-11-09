package com.example.debugappproject.ui.bugdetail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentBugDetailBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * BugDetailFragment - Interactive bug debugging practice screen with code editor.
 *
 * NEW Features (Interactive Mode):
 * - Editable code editor for students to write their fix
 * - "Run Tests" button to check if code matches the solution
 * - Code comparison with normalized whitespace/formatting
 * - "Reset Code" to restore original starter code
 * - Real-time test feedback (pass/fail)
 * - Hint tracking with XP penalties
 * - Automatic completion on test pass
 * - Confirmation dialog for manual completion without passing tests
 *
 * Original Features:
 * - Material 3 card-based layout with proper visual hierarchy
 * - Difficulty and category chips with dynamic coloring
 * - Progressive hint revelation system
 * - Solution with explanation and fixed code
 * - Completion tracking with visual indicator
 */
public class BugDetailFragment extends Fragment {

    private FragmentBugDetailBinding binding;
    private BugDetailViewModel viewModel;
    private int bugId;
    private Bug currentBug;
    private List<Hint> hints;

    // Track hints used for this bug (for XP penalty calculation)
    private int hintsUsedForThisBug = 0;

    // Track if tests have passed (for confirmation dialog)
    private boolean testsPassedForThisBug = false;

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

        // Get bug ID from arguments
        if (getArguments() != null) {
            bugId = getArguments().getInt("bugId", 1);
        }

        viewModel.loadBug(bugId);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        // Observe bug data
        viewModel.getCurrentBug().observe(getViewLifecycleOwner(), bug -> {
            if (bug != null) {
                currentBug = bug;
                displayBug(bug);
            }
        });

        // Observe hints
        viewModel.getHints().observe(getViewLifecycleOwner(), hintList -> {
            this.hints = hintList;
        });

        // Observe solution visibility
        viewModel.isShowingSolution().observe(getViewLifecycleOwner(), showing -> {
            if (showing != null && showing && currentBug != null) {
                showSolution();
            }
        });
    }

    /**
     * Displays bug details in the UI.
     * Sets chip colors based on difficulty level.
     * Initializes the code editor with starter code.
     */
    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());

        // Show completed chip if bug is completed
        if (bug.isCompleted()) {
            binding.chipCompleted.setVisibility(View.VISIBLE);
            testsPassedForThisBug = true; // Already completed, so tests are considered passed
        }

        // Set difficulty chip
        binding.chipDifficulty.setText(bug.getDifficulty());
        setDifficultyChipColor(bug.getDifficulty());

        // Set category chip
        binding.chipCategory.setText(bug.getCategory());

        binding.textBugDescription.setText(bug.getDescription());
        binding.textBrokenCode.setText(bug.getBrokenCode());

        // Initialize code editor with starter code
        String initialCode = bug.getInitialEditorCode();
        binding.editYourFix.setText(initialCode);

        // Update button states based on completion
        if (bug.isCompleted()) {
            binding.buttonMarkSolved.setText("Completed ✓");
            binding.buttonMarkSolved.setEnabled(false);
        }
    }

    /**
     * Sets difficulty chip background color based on difficulty level.
     */
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
        // Run Tests button - check if user's code matches the solution
        binding.buttonRunTests.setOnClickListener(v -> {
            if (currentBug != null) {
                runTests();
            }
        });

        // Reset Code button - restore initial starter code
        binding.buttonResetCode.setOnClickListener(v -> {
            if (currentBug != null) {
                resetCode();
            }
        });

        // Show Hint button
        binding.buttonShowHint.setOnClickListener(v -> {
            showNextHint();
        });

        // Show Solution button
        binding.buttonShowSolution.setOnClickListener(v -> {
            viewModel.showSolution();
        });

        // Mark as Solved button
        binding.buttonMarkSolved.setOnClickListener(v -> {
            if (currentBug != null && !currentBug.isCompleted()) {
                handleMarkAsSolved();
            }
        });
    }

    /**
     * Runs tests by comparing user's code with the fixed code.
     * Uses normalized comparison (trimmed, collapsed whitespace).
     * If tests pass, automatically marks bug as completed.
     */
    private void runTests() {
        String userCode = binding.editYourFix.getText().toString();
        String fixedCode = currentBug.getFixedCode();

        // Normalize both strings for comparison
        String normalizedUserCode = normalizeCode(userCode);
        String normalizedFixedCode = normalizeCode(fixedCode);

        // Show test results card
        binding.cardTestResults.setVisibility(View.VISIBLE);

        if (normalizedUserCode.equals(normalizedFixedCode)) {
            // Tests passed!
            testsPassedForThisBug = true;
            binding.textTestResultTitle.setText("✅ All Tests Passed!");
            binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.success, null));
            binding.textTestResultMessage.setText(getString(R.string.all_tests_passed));
            binding.cardTestResults.setCardBackgroundColor(getResources().getColor(R.color.success_background, null));

            // Automatically mark as completed if not already
            if (!currentBug.isCompleted()) {
                viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty(), hintsUsedForThisBug);
                binding.buttonMarkSolved.setText("Completed ✓");
                binding.buttonMarkSolved.setEnabled(false);
                binding.chipCompleted.setVisibility(View.VISIBLE);

                // Show celebration message with XP info
                int xp = calculateXpReward(currentBug.getDifficulty(), hintsUsedForThisBug);
                String message = "🎉 Bug solved! +" + xp + " XP!";
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Tests failed
            testsPassedForThisBug = false;
            binding.textTestResultTitle.setText("❌ Tests Failed");
            binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.error_light, null));
            binding.textTestResultMessage.setText(getString(R.string.tests_failed));
            binding.cardTestResults.setCardBackgroundColor(getResources().getColor(R.color.error_background, null));

            // Optional: Try to find which line differs
            String diffHint = findCodeDifference(userCode, fixedCode);
            if (diffHint != null) {
                binding.textTestResultMessage.setText(getString(R.string.tests_failed) + "\n\n" + diffHint);
            }
        }
    }

    /**
     * Normalizes code for comparison by:
     * - Trimming whitespace
     * - Removing blank lines
     * - Collapsing multiple spaces to single space
     * - Converting to lowercase for case-insensitive comparison (optional)
     */
    private String normalizeCode(String code) {
        if (code == null) return "";

        // Split into lines, trim each, remove empty lines
        String[] lines = code.split("\n");
        StringBuilder normalized = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Collapse multiple spaces to single space
                trimmed = trimmed.replaceAll("\\s+", " ");
                normalized.append(trimmed).append("\n");
            }
        }

        return normalized.toString().trim();
    }

    /**
     * Attempts to find the first line where user code differs from fixed code.
     * Returns a helpful hint message, or null if no specific difference found.
     */
    private String findCodeDifference(String userCode, String fixedCode) {
        String[] userLines = userCode.split("\n");
        String[] fixedLines = fixedCode.split("\n");

        int minLength = Math.min(userLines.length, fixedLines.length);

        for (int i = 0; i < minLength; i++) {
            String userLine = userLines[i].trim();
            String fixedLine = fixedLines[i].trim();

            if (!userLine.equals(fixedLine)) {
                return "💡 Hint: Check line " + (i + 1) + " – something looks different.";
            }
        }

        if (userLines.length != fixedLines.length) {
            return "💡 Hint: Your code has a different number of lines than expected.";
        }

        return null;
    }

    /**
     * Resets the code editor to the initial starter code.
     */
    private void resetCode() {
        String initialCode = currentBug.getInitialEditorCode();
        binding.editYourFix.setText(initialCode);
        Toast.makeText(requireContext(), "Code reset to original", Toast.LENGTH_SHORT).show();

        // Hide test results when code is reset
        binding.cardTestResults.setVisibility(View.GONE);
        testsPassedForThisBug = false;
    }

    /**
     * Handles the "Mark as Solved" button click.
     * Shows confirmation dialog if tests haven't passed yet.
     */
    private void handleMarkAsSolved() {
        if (testsPassedForThisBug) {
            // Tests passed, so just mark as solved
            markBugAsSolved();
        } else {
            // Tests haven't passed, show confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_mark_solved_title)
                    .setMessage(R.string.confirm_mark_solved_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> markBugAsSolved())
                    .setNegativeButton(R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    /**
     * Marks the bug as solved and updates the UI.
     */
    private void markBugAsSolved() {
        viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty(), hintsUsedForThisBug);
        binding.buttonMarkSolved.setText("Completed ✓");
        binding.buttonMarkSolved.setEnabled(false);
        binding.chipCompleted.setVisibility(View.VISIBLE);
        testsPassedForThisBug = true;

        // Show XP reward
        int xp = calculateXpReward(currentBug.getDifficulty(), hintsUsedForThisBug);
        Toast.makeText(requireContext(), "Bug completed! +" + xp + " XP", Toast.LENGTH_SHORT).show();
    }

    /**
     * Calculates XP reward based on difficulty and hints used.
     * Same formula as BugRepository for consistency.
     */
    private int calculateXpReward(String difficulty, int hintsUsed) {
        int xp;
        switch (difficulty) {
            case "Easy":
                xp = 10;
                break;
            case "Medium":
                xp = 20;
                break;
            case "Hard":
                xp = 30;
                break;
            default:
                xp = 10;
        }

        // Bonus if no hints used
        if (hintsUsed == 0) {
            xp += 5;
        }

        return xp;
    }

    /**
     * Shows output comparison card with expected vs actual output.
     * (Kept for backward compatibility, though now superseded by test results)
     */
    private void showOutput() {
        binding.cardOutput.setVisibility(View.VISIBLE);
        binding.textExpectedOutput.setText(currentBug.getExpectedOutput());
        binding.textActualOutput.setText(currentBug.getActualOutput());
    }

    /**
     * Reveals the next hint and adds it to the hints container.
     * Each hint is displayed in a separate TextView for better readability.
     * Tracks hints used for XP penalty calculation (Part 3).
     */
    private void showNextHint() {
        if (hints == null || hints.isEmpty()) {
            Toast.makeText(requireContext(), "No hints available", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer currentLevel = viewModel.getCurrentHintLevel().getValue();
        if (currentLevel == null) {
            currentLevel = 0;
        }

        if (currentLevel < hints.size()) {
            Hint hint = hints.get(currentLevel);

            // Show hints card if first hint
            if (currentLevel == 0) {
                binding.cardHints.setVisibility(View.VISIBLE);
            }

            // Create a new TextView for this hint
            TextView hintView = new TextView(requireContext());
            hintView.setTextAppearance(R.style.TextAppearance_DebugMaster_Body1);
            hintView.setText("💡 Hint " + (currentLevel + 1) + ": " + hint.getText());

            // Add spacing between hints
            if (currentLevel > 0) {
                hintView.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
            }

            binding.layoutHintsContainer.addView(hintView);
            viewModel.revealNextHint();

            // Track hints used for this bug (for XP calculation in Part 3)
            hintsUsedForThisBug++;
        } else {
            Toast.makeText(requireContext(), "No more hints available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows the solution card with explanation and fixed code.
     */
    private void showSolution() {
        binding.cardSolution.setVisibility(View.VISIBLE);
        binding.textExplanation.setText(currentBug.getExplanation());
        binding.textFixedCode.setText(currentBug.getFixedCode());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
