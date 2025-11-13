package com.example.debugappproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.debugappproject.databinding.FragmentProfileBinding;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.DateUtils;

/**
 * Profile Fragment - Displays user progress, stats, and achievements.
 *
 * Features:
 * - Level and XP display with progress bar
 * - Stats: bugs solved, perfect fixes, current streak
 * - Achievements grid showing locked/unlocked achievements
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementAdapter achievementAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupAchievementsRecyclerView();
        setupObservers();

        // Load achievements
        viewModel.loadAchievements();
    }

    /**
     * Sets up the achievements RecyclerView with a grid layout.
     */
    private void setupAchievementsRecyclerView() {
        achievementAdapter = new AchievementAdapter();
        binding.recyclerAchievements.setAdapter(achievementAdapter);

        // Use 2-column grid for achievements
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.recyclerAchievements.setLayoutManager(layoutManager);
    }

    /**
     * Sets up LiveData observers for user progress and achievements.
     */
    private void setupObservers() {
        // Observe user progress
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                displayUserProgress(progress);
            }
        });

        // Observe achievements
        viewModel.getAchievementsWithStatus().observe(getViewLifecycleOwner(), achievements -> {
            if (achievements != null && !achievements.isEmpty()) {
                achievementAdapter.setAchievements(achievements);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.VISIBLE);

                // Update achievements count
                long unlockedCount = achievements.stream()
                    .filter(AchievementWithStatus::isUnlocked)
                    .count();
                binding.textAchievementsCount.setText(
                    unlockedCount + " of " + achievements.size() + " unlocked"
                );
            } else {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Displays user progress data in the UI.
     */
    private void displayUserProgress(UserProgress progress) {
        // Calculate and display level
        int level = viewModel.calculateLevel(progress.getTotalXp());
        binding.textLevel.setText(String.valueOf(level));

        // Calculate XP progress within current level
        int xpInLevel = viewModel.getXpProgressInLevel(progress.getTotalXp());
        int xpForNextLevel = viewModel.getXpForNextLevel(progress.getTotalXp());
        binding.textXp.setText(xpInLevel + " / 100 XP");
        binding.progressXp.setProgress(xpInLevel);

        // Display perfect fixes (bugs solved without hints)
        binding.textPerfectFixes.setText(String.valueOf(progress.getBugsSolvedWithoutHints()));

        // Calculate and display current streak
        int currentStreak = DateUtils.calculateCurrentStreak(
            progress.getLastCompletionDate(),
            progress.getCurrentStreakDays()
        );
        binding.textStreakDays.setText(String.valueOf(currentStreak));

        // Get total bugs solved
        viewModel.getTotalBugsCompleted(count -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.textBugsSolved.setText(String.valueOf(count));
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
