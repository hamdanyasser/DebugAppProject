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
import com.example.debugappproject.model.Achievement;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;

import java.util.List;

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
                computeAndDisplayAchievements();
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

                computeAndDisplayAchievements();
            }
        });

        // Observe completed bugs for achievements
        viewModel.getCompletedBugs().observe(getViewLifecycleOwner(), completedBugs -> {
            computeAndDisplayAchievements();
        });
    }

    /**
     * Updates progress display with user statistics.
     * Shows overall progress, streak, XP, level, hints used, and difficulty breakdown.
     */
    private void updateProgressDisplay(UserProgress progress) {
        binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
        binding.textEasySolved.setText(String.valueOf(progress.getEasySolved()));
        binding.textMediumSolved.setText(String.valueOf(progress.getMediumSolved()));
        binding.textHardSolved.setText(String.valueOf(progress.getHardSolved()));
        binding.textStreak.setText(String.valueOf(progress.getStreakDays()));

        // Update XP & Level section
        binding.textLevel.setText(String.valueOf(progress.getLevel()));
        binding.textTotalXp.setText(String.valueOf(progress.getXp()));
        binding.textHintsUsed.setText(String.valueOf(progress.getHintsUsed()));
        binding.textNoHints.setText(String.valueOf(progress.getBugsSolvedWithoutHints()));
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

    /**
     * Computes achievements and displays them in the achievements container.
     * Shows unlocked achievements in full color and locked ones in gray.
     */
    private void computeAndDisplayAchievements() {
        UserProgress progress = viewModel.getUserProgress().getValue();
        List<Bug> completedBugs = viewModel.getCompletedBugs().getValue();
        List<Bug> allBugs = viewModel.getAllBugs().getValue();

        if (progress == null || completedBugs == null || allBugs == null) {
            return;
        }

        List<Achievement> achievements = viewModel.computeAchievements(progress, completedBugs, allBugs);

        // Count unlocked achievements
        int unlockedCount = 0;
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }

        // Update achievement count
        binding.textAchievementsCount.setText(unlockedCount + " / " + achievements.size() + " unlocked");

        // Clear existing achievement views
        binding.layoutAchievementsContainer.removeAllViews();

        // Display each achievement
        for (Achievement achievement : achievements) {
            View achievementView = createAchievementView(achievement);
            binding.layoutAchievementsContainer.addView(achievementView);
        }
    }

    /**
     * Creates a view for a single achievement.
     * Shows icon, name, and description with appropriate styling for locked/unlocked state.
     */
    private View createAchievementView(Achievement achievement) {
        // Create a horizontal LinearLayout for the achievement
        android.widget.LinearLayout achievementLayout = new android.widget.LinearLayout(requireContext());
        achievementLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        achievementLayout.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (8 * getResources().getDisplayMetrics().density));

        // Icon TextView
        android.widget.TextView iconView = new android.widget.TextView(requireContext());
        iconView.setText(achievement.getIcon());
        iconView.setTextSize(24);
        iconView.setAlpha(achievement.isUnlocked() ? 1.0f : 0.3f);
        android.widget.LinearLayout.LayoutParams iconParams = new android.widget.LinearLayout.LayoutParams(
                (int) (40 * getResources().getDisplayMetrics().density),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        iconView.setLayoutParams(iconParams);

        // Text container (name + description)
        android.widget.LinearLayout textContainer = new android.widget.LinearLayout(requireContext());
        textContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams textContainerParams = new android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textContainer.setLayoutParams(textContainerParams);

        // Name TextView
        android.widget.TextView nameView = new android.widget.TextView(requireContext());
        nameView.setText(achievement.getName());
        nameView.setTextSize(16);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        nameView.setAlpha(achievement.isUnlocked() ? 1.0f : 0.5f);

        // Description TextView
        android.widget.TextView descView = new android.widget.TextView(requireContext());
        descView.setText(achievement.getDescription());
        descView.setTextSize(14);
        descView.setAlpha(achievement.isUnlocked() ? 0.7f : 0.4f);

        textContainer.addView(nameView);
        textContainer.addView(descView);

        // Status indicator
        android.widget.TextView statusView = new android.widget.TextView(requireContext());
        if (achievement.isUnlocked()) {
            statusView.setText("✓");
            statusView.setTextColor(getResources().getColor(com.example.debugappproject.R.color.difficulty_easy, null));
        } else {
            statusView.setText("🔒");
            statusView.setAlpha(0.3f);
        }
        statusView.setTextSize(18);
        android.widget.LinearLayout.LayoutParams statusParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMarginStart((int) (8 * getResources().getDisplayMetrics().density));
        statusView.setLayoutParams(statusParams);

        achievementLayout.addView(iconView);
        achievementLayout.addView(textContainer);
        achievementLayout.addView(statusView);

        return achievementLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
