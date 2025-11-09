package com.example.debugappproject.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentHomeBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;

/**
 * HomeFragment - Dashboard/home screen of the app.
 *
 * Displays:
 * - Bug of the Day with difficulty/category chips
 * - Quick stats (bugs solved, total, streak)
 * - Motivational messages based on progress
 * - Navigation buttons to other screens
 *
 * Features:
 * - Material 3 design with cards and chips
 * - Dynamic difficulty chip coloring
 * - Progress-based motivational text
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        // Observe Bug of the Day
        viewModel.getBugOfTheDay().observe(getViewLifecycleOwner(), bug -> {
            if (bug != null) {
                updateBugOfTheDay(bug);
            }
        });

        // Observe User Progress for stats
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                updateStats(progress);
            }
        });

        // Observe all bugs for total count
        viewModel.getAllBugs().observe(getViewLifecycleOwner(), bugs -> {
            if (bugs != null && binding.textTotalBugs != null) {
                binding.textTotalBugs.setText(String.valueOf(bugs.size()));
            }
        });
    }

    /**
     * Updates Bug of the Day card with bug information.
     * Sets chip colors based on difficulty level.
     */
    private void updateBugOfTheDay(Bug bug) {
        binding.textBugOfDayTitle.setText(bug.getTitle());

        // Set difficulty chip
        binding.chipDifficulty.setText(bug.getDifficulty());
        setDifficultyChipColor(bug.getDifficulty());

        // Set category chip
        binding.chipCategory.setText(bug.getCategory());

        // Store bug ID for navigation
        binding.cardBugOfDay.setTag(bug.getId());
        binding.buttonSolveNow.setTag(bug.getId());
    }

    /**
     * Sets chip background color based on difficulty level.
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

    /**
     * Updates stats display with user progress.
     * Shows motivational message based on progress.
     * NEW: Updates XP and Level display (Part 4).
     */
    private void updateStats(UserProgress progress) {
        binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
        binding.textStreakDays.setText(String.valueOf(progress.getStreakDays()));

        // Set motivational message based on progress
        String motivation = getMotivationalMessage(progress);
        binding.textMotivation.setText(motivation);

        // NEW: Update XP and Level display (Part 4)
        updateXpAndLevel(progress);
    }

    /**
     * Updates the XP and Level card with current progress.
     * Shows level badge, total XP, and progress bar toward next level.
     */
    private void updateXpAndLevel(UserProgress progress) {
        // Update level badge
        binding.textCurrentLevel.setText(String.valueOf(progress.getLevel()));

        // Update total XP
        binding.textTotalXp.setText(progress.getXp() + " XP");

        // Update progress bar and XP to next level text
        int xpInLevel = progress.getXpProgressInLevel();
        int xpForNextLevel = progress.getXpForNextLevel();

        binding.progressBarXp.setProgress(xpInLevel);
        binding.textXpToNextLevel.setText(
            xpInLevel + "/100 XP to Level " + (progress.getLevel() + 1)
        );
    }

    /**
     * Returns motivational message based on user progress.
     */
    private String getMotivationalMessage(UserProgress progress) {
        int solved = progress.getTotalSolved();
        int streak = progress.getStreakDays();

        if (streak >= 7) {
            return "Amazing! 7-day streak! Keep it up! 🔥";
        } else if (streak >= 3) {
            return "Great streak! You're on fire! 🔥";
        } else if (solved >= 10) {
            return "Double digits! You're a debugging pro! 🎯";
        } else if (solved >= 5) {
            return "Halfway there! Keep going! 💪";
        } else if (solved >= 1) {
            return "Great start! Keep practicing! 🚀";
        } else {
            return getString(R.string.keep_going);
        }
    }

    /**
     * Sets up click listeners for navigation.
     */
    private void setupClickListeners() {
        // Bug of the Day - "Solve Now" button click
        binding.buttonSolveNow.setOnClickListener(v -> {
            Integer bugId = (Integer) v.getTag();
            if (bugId != null) {
                navigateToBugDetail(bugId, v);
            }
        });

        // Bug of the Day card click (for convenience)
        binding.cardBugOfDay.setOnClickListener(v -> {
            Integer bugId = (Integer) v.getTag();
            if (bugId != null) {
                navigateToBugDetail(bugId, v);
            }
        });

        // All Bugs button
        binding.buttonAllBugs.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_home_to_bugList)
        );

        // My Progress button
        binding.buttonMyProgress.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_home_to_progress)
        );
    }

    /**
     * Navigates to bug detail screen with the given bug ID.
     */
    private void navigateToBugDetail(int bugId, View view) {
        Bundle args = new Bundle();
        args.putInt("bugId", bugId);
        Navigation.findNavController(view).navigate(
            R.id.action_home_to_bugDetail, args
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
