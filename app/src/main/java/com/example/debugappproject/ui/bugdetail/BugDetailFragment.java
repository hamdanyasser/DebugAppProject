package com.example.debugappproject.ui.bugdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.debugappproject.databinding.FragmentBugDetailBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;

import java.util.List;

/**
 * BugDetailFragment - Shows detailed view of a single bug.
 * Displays:
 * - Bug description and broken code
 * - Run Code button (simulates execution)
 * - Hints button (reveals hints progressively)
 * - Solution button (shows explanation and fixed code)
 * - Mark as Solved button
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

    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());
        binding.textBugDifficulty.setText(bug.getDifficulty());
        binding.textBugCategory.setText(bug.getCategory());
        binding.textBugDescription.setText(bug.getDescription());
        binding.textBrokenCode.setText(bug.getBrokenCode());

        // Update button states based on completion
        if (bug.isCompleted()) {
            binding.buttonMarkSolved.setText("Completed ✓");
            binding.buttonMarkSolved.setEnabled(false);
        }
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

    private void showOutput() {
        // Show output comparison
        binding.layoutOutput.setVisibility(View.VISIBLE);
        binding.textExpectedOutput.setText("Expected:\n" + currentBug.getExpectedOutput());
        binding.textActualOutput.setText("Actual:\n" + currentBug.getActualOutput());
    }

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
            binding.layoutHints.setVisibility(View.VISIBLE);
            String currentHints = binding.textHints.getText().toString();
            String newHintText = currentHints + "\n\nHint " + (currentLevel + 1) + ": " + hint.getText();
            binding.textHints.setText(newHintText.trim());
            viewModel.revealNextHint();
        } else {
            Toast.makeText(requireContext(), "No more hints available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSolution() {
        binding.layoutSolution.setVisibility(View.VISIBLE);
        binding.textExplanation.setText(currentBug.getExplanation());
        binding.textFixedCode.setText(currentBug.getFixedCode());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
