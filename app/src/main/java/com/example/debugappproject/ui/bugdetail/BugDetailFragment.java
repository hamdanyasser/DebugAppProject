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
import com.example.debugappproject.model.TestCase;
import com.example.debugappproject.util.CodeComparator;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * BugDetailFragment - Shows detailed view of a single bug with Material 3 design.
 *
 * Features:
 * - Material 3 card-based layout with proper visual hierarchy
 * - Difficulty and category chips with dynamic coloring
 * - Code execution simulation with output comparison
 * - Progressive hint revelation system
 * - Solution with explanation and fixed code
 * - Completion tracking
 *
 * Displays:
 * - Bug header with title and chips
 * - Description card
 * - Broken code card with syntax highlighting background
 * - Run Code and Show Hint buttons
 * - Output comparison card (shown after running code)
 * - Hints card (reveals hints one by one)
 * - Solution card (shows explanation and fixed code)
 * - Action buttons (Show Solution, Mark as Solved)
 */
public class BugDetailFragment extends Fragment {

    private FragmentBugDetailBinding binding;
    private BugDetailViewModel viewModel;
    private int bugId;
    private Bug currentBug;
    private List<Hint> hints;
    private List<TestCase> testCases;
    private String initialCode; // The starting code for the user's fix attempt

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
     * Initializes the code editor with starting code.
     */
    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());

        // Set difficulty chip
        binding.chipDifficulty.setText(bug.getDifficulty());
        setDifficultyChipColor(bug.getDifficulty());

        // Set category chip
        binding.chipCategory.setText(bug.getCategory());

        // Show completed chip if bug is completed
        if (bug.isCompleted()) {
            binding.chipCompleted.setVisibility(View.VISIBLE);
            binding.buttonMarkSolved.setText("Completed ✓");
            binding.buttonMarkSolved.setEnabled(false);
        }

        binding.textBugDescription.setText(bug.getDescription());
        binding.textBrokenCode.setText(bug.getBrokenCode());

        // Initialize code editor with starting code
        initialCode = bug.getInitialCode();
        binding.editUserCode.setText(initialCode);

        // Parse test cases from JSON if available
        parseTestCases(bug.getTestsJson());
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
        // Run Code button (old - now for demonstration purposes)
        binding.buttonRunCode.setOnClickListener(v -> {
            if (currentBug != null) {
                showOutput();
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

        // Check Fix / Run Tests button
        binding.buttonCheckFix.setOnClickListener(v -> {
            if (currentBug != null) {
                runTests();
            }
        });

        // Reset Code button
        binding.buttonResetCode.setOnClickListener(v -> {
            resetCode();
        });

        // Mark as Solved button
        binding.buttonMarkSolved.setOnClickListener(v -> {
            if (currentBug != null && !currentBug.isCompleted()) {
                // Check if user has passed tests
                String userCode = binding.editUserCode.getText().toString();
                boolean testsPassed = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());

                if (!testsPassed) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Mark as Solved?")
                            .setMessage("Are you sure? You haven't passed all tests yet.")
                            .setPositiveButton("Yes, Mark as Solved", (dialog, which) -> {
                                viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                                binding.chipCompleted.setVisibility(View.VISIBLE);
                                binding.buttonMarkSolved.setText("Completed ✓");
                                binding.buttonMarkSolved.setEnabled(false);
                                Toast.makeText(requireContext(), "Bug marked as completed!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    // Tests passed, mark as completed directly
                    viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                    binding.chipCompleted.setVisibility(View.VISIBLE);
                    binding.buttonMarkSolved.setText("Completed ✓");
                    binding.buttonMarkSolved.setEnabled(false);
                    Toast.makeText(requireContext(), "Bug marked as completed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Shows output comparison card with expected vs actual output.
     */
    private void showOutput() {
        binding.cardOutput.setVisibility(View.VISIBLE);
        binding.textExpectedOutput.setText(currentBug.getExpectedOutput());
        binding.textActualOutput.setText(currentBug.getActualOutput());
    }

    /**
     * Reveals the next hint and adds it to the hints container.
     * Each hint is displayed in a separate TextView for better readability.
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

    /**
     * Parses test cases from JSON string.
     * If JSON is null or empty, creates default test cases.
     */
    private void parseTestCases(String testsJson) {
        testCases = new ArrayList<>();

        if (testsJson != null && !testsJson.trim().isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TestCase>>(){}.getType();
                testCases = gson.fromJson(testsJson, listType);
            } catch (Exception e) {
                // If parsing fails, use empty list
                testCases = new ArrayList<>();
            }
        }

        // If no test cases, create a default one
        if (testCases.isEmpty()) {
            TestCase defaultTest = new TestCase();
            defaultTest.setInput("Various inputs");
            defaultTest.setExpected("Correct output");
            defaultTest.setDescription("Code should produce expected output");
            testCases.add(defaultTest);
        }
    }

    /**
     * Runs tests by comparing user's code with the fixed code.
     * Shows results in the test results card.
     */
    private void runTests() {
        String userCode = binding.editUserCode.getText().toString();
        String fixedCode = currentBug.getFixedCode();

        boolean testsPassed = CodeComparator.codesMatch(userCode, fixedCode);

        // Show test results card
        binding.cardTestResults.setVisibility(View.VISIBLE);

        if (testsPassed) {
            // All tests passed!
            binding.textTestResultTitle.setText("✅ All Tests Passed!");
            binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
            binding.textTestResultMessage.setText("Congratulations! Your code matches the expected solution. 🎉");

            // Mark all test cases as passed
            for (TestCase test : testCases) {
                test.setPassed(true);
            }

            // Display test cases
            displayTestResults();

            // Mark bug as completed with XP if not already completed
            if (!currentBug.isCompleted()) {
                viewModel.markBugAsCompletedWithXP(currentBug.getId(), currentBug.getDifficulty());
                currentBug.setCompleted(true);
                binding.chipCompleted.setVisibility(View.VISIBLE);
                binding.buttonMarkSolved.setText("Completed ✓");
                binding.buttonMarkSolved.setEnabled(false);

                // Show success snackbar
                Snackbar.make(binding.getRoot(), "Bug solved! XP awarded! 🏆", Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Tests passed! (Already completed)", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Tests failed
            binding.textTestResultTitle.setText("❌ Tests Failed");
            binding.textTestResultTitle.setTextColor(getResources().getColor(R.color.error_light, null));

            // Generate helpful error message
            String errorMessage = CodeComparator.generateErrorMessage(userCode, fixedCode);
            binding.textTestResultMessage.setText(errorMessage);

            // Mark all test cases as failed
            for (TestCase test : testCases) {
                test.setPassed(false);
            }

            // Display test cases
            displayTestResults();
        }
    }

    /**
     * Displays test cases with pass/fail indicators.
     */
    private void displayTestResults() {
        binding.layoutTestCases.removeAllViews();

        for (int i = 0; i < testCases.size(); i++) {
            TestCase test = testCases.get(i);

            TextView testView = new TextView(requireContext());
            testView.setTextAppearance(R.style.TextAppearance_DebugMaster_Body1);

            String statusIcon = test.isPassed() ? "✅" : "❌";
            String testInfo = statusIcon + " Test " + (i + 1) + ": " + test.getDescription() +
                    "\n   Input: " + test.getInput() +
                    "\n   Expected: " + test.getExpected();

            testView.setText(testInfo);
            testView.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);

            binding.layoutTestCases.addView(testView);
        }
    }

    /**
     * Resets the code editor to the initial code.
     */
    private void resetCode() {
        binding.editUserCode.setText(initialCode);
        Toast.makeText(requireContext(), "Code reset to starting point", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
