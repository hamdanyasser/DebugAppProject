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
 * Displays:
 * - Bug of the Day
 * - Quick stats (bugs solved, streak)
 * - Navigation buttons to other screens
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

    private void updateBugOfTheDay(Bug bug) {
        binding.textBugOfDayTitle.setText(bug.getTitle());
        binding.textBugOfDayDifficulty.setText(bug.getDifficulty());
        binding.textBugOfDayCategory.setText(bug.getCategory());

        // Store bug ID for navigation
        binding.cardBugOfDay.setTag(bug.getId());
    }

    private void updateStats(UserProgress progress) {
        binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
        binding.textStreakDays.setText(String.valueOf(progress.getStreakDays()));
    }

    private void setupClickListeners() {
        // Bug of the Day card click
        binding.cardBugOfDay.setOnClickListener(v -> {
            Integer bugId = (Integer) v.getTag();
            if (bugId != null) {
                Bundle args = new Bundle();
                args.putInt("bugId", bugId);
                Navigation.findNavController(v).navigate(
                    R.id.action_home_to_bugDetail, args
                );
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
