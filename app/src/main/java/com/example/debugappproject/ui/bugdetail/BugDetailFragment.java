package com.example.debugappproject.ui.bugdetail;

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
     */
    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());

        // Set difficulty chip
        binding.chipDifficulty.setText(bug.getDifficulty());
        setDifficultyChipColor(bug.getDifficulty());

        // Set category chip
        binding.chipCategory.setText(bug.getCategory());

        binding.textBugDescription.setText(bug.getDescription());
        binding.textBrokenCode.setText(bug.getBrokenCode());

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
        // Run Code button
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

        // Mark as Solved button
        binding.buttonMarkSolved.setOnClickListener(v -> {
            if (currentBug != null && !currentBug.isCompleted()) {
                viewModel.markBugAsCompleted(currentBug.getId(), currentBug.getDifficulty());
                binding.buttonMarkSolved.setText("Completed ✓");
                binding.buttonMarkSolved.setEnabled(false);
                Toast.makeText(requireContext(), "Bug marked as completed!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
