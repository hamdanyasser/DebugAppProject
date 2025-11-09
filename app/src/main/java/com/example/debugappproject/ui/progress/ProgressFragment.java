package com.example.debugappproject.ui.progress;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.debugappproject.databinding.FragmentProgressBinding;
import com.example.debugappproject.model.UserProgress;

/**
 * ProgressFragment - Stats dashboard showing user's learning progress.
 *
 * Features:
 * - Overall progress with visual progress bar
 * - Current streak counter to encourage daily practice
 * - Difficulty breakdown (Easy, Medium, Hard) with individual progress bars
 * - Reset progress functionality with confirmation dialog
 * - Material 3 card-based design with proper visual hierarchy
 *
 * Displays:
 * - Total bugs solved out of total available
 * - Percentage completion
 * - Streak days counter
 * - Progress by difficulty level
 */
public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private ProgressViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        // Observe user progress
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                updateProgressDisplay(progress);
            }
        });

        // Observe total bug count
        viewModel.getAllBugs().observe(getViewLifecycleOwner(), bugs -> {
            if (bugs != null) {
                binding.textTotalBugs.setText(String.valueOf(bugs.size()));

                // Calculate percentages
                UserProgress progress = viewModel.getUserProgress().getValue();
                if (progress != null) {
                    updateProgressBars(progress, bugs.size());
                }
            }
        });
    }

    private void updateProgressDisplay(UserProgress progress) {
        binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
        binding.textEasySolved.setText(String.valueOf(progress.getEasySolved()));
        binding.textMediumSolved.setText(String.valueOf(progress.getMediumSolved()));
        binding.textHardSolved.setText(String.valueOf(progress.getHardSolved()));
        binding.textStreak.setText(String.valueOf(progress.getStreakDays()));
    }

    private void updateProgressBars(UserProgress progress, int totalBugs) {
        if (totalBugs == 0) return;

        // Overall progress
        int overallPercent = (progress.getTotalSolved() * 100) / totalBugs;
        binding.progressBarOverall.setProgress(overallPercent);
        binding.textOverallPercent.setText(overallPercent + "%");

        // Count bugs by difficulty (simplified - assumes equal distribution)
        int bugsPerDifficulty = totalBugs / 3;
        if (bugsPerDifficulty > 0) {
            int easyPercent = Math.min(100, (progress.getEasySolved() * 100) / bugsPerDifficulty);
            binding.progressBarEasy.setProgress(easyPercent);

            int mediumPercent = Math.min(100, (progress.getMediumSolved() * 100) / bugsPerDifficulty);
            binding.progressBarMedium.setProgress(mediumPercent);

            int hardPercent = Math.min(100, (progress.getHardSolved() * 100) / bugsPerDifficulty);
            binding.progressBarHard.setProgress(hardPercent);
        }
    }

    private void setupClickListeners() {
        binding.buttonResetProgress.setOnClickListener(v -> showResetConfirmation());
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Reset Progress")
            .setMessage("Are you sure you want to reset all your progress? This cannot be undone.")
            .setPositiveButton("Reset", (dialog, which) -> {
                viewModel.resetProgress();
                Toast.makeText(requireContext(), "Progress reset!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
