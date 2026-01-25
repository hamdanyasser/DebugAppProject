package com.example.debugappproject.ui.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.debugmaster.app.R;
import com.debugmaster.app.databinding.FragmentPracticeBinding;

import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * PracticeFragment - Random bug practice mode.
 * Serves random bugs for users to solve without following a specific path.
 */
@AndroidEntryPoint
public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private Random random = new Random();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUI();
        loadRandomBug();
    }

    private void setupUI() {
        // Back button
        if (binding.buttonBack != null) {
            binding.buttonBack.setOnClickListener(v -> {
                Navigation.findNavController(v).navigateUp();
            });
        }

        // Start Practice button
        if (binding.buttonStartPractice != null) {
            binding.buttonStartPractice.setOnClickListener(v -> {
                navigateToRandomBug();
            });
        }

        // Skip button
        if (binding.buttonSkip != null) {
            binding.buttonSkip.setOnClickListener(v -> {
                loadRandomBug();
            });
        }

        // Difficulty filter chips
        if (binding.chipEasy != null) {
            binding.chipEasy.setOnClickListener(v -> filterByDifficulty("easy"));
        }
        if (binding.chipMedium != null) {
            binding.chipMedium.setOnClickListener(v -> filterByDifficulty("medium"));
        }
        if (binding.chipHard != null) {
            binding.chipHard.setOnClickListener(v -> filterByDifficulty("hard"));
        }
    }

    private void loadRandomBug() {
        // In production, this would fetch a random bug from the database
        // For now, we show a placeholder
        if (binding.textBugTitle != null) {
            String[] bugTitles = {
                "Fix the Null Pointer",
                "Array Index Out of Bounds",
                "Memory Leak Detection",
                "Race Condition Fix",
                "Infinite Loop Escape"
            };
            binding.textBugTitle.setText(bugTitles[random.nextInt(bugTitles.length)]);
        }

        if (binding.textBugCategory != null) {
            String[] categories = {"Java", "Python", "JavaScript", "C++", "Swift"};
            binding.textBugCategory.setText(categories[random.nextInt(categories.length)]);
        }
    }

    private void filterByDifficulty(String difficulty) {
        // Update UI to show selected difficulty
        if (binding.chipEasy != null) binding.chipEasy.setChecked("easy".equals(difficulty));
        if (binding.chipMedium != null) binding.chipMedium.setChecked("medium".equals(difficulty));
        if (binding.chipHard != null) binding.chipHard.setChecked("hard".equals(difficulty));
        
        // Load new bug with filter
        loadRandomBug();
    }

    private void navigateToRandomBug() {
        try {
            // Navigate to bug detail with a random bug ID
            int randomBugId = random.nextInt(10) + 1; // Random ID from 1-10
            Bundle args = new Bundle();
            args.putInt("bugId", randomBugId);
            Navigation.findNavController(requireView())
                .navigate(R.id.action_practice_to_bugDetail, args);
        } catch (Exception e) {
            android.util.Log.e("PracticeFragment", "Navigation error", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
