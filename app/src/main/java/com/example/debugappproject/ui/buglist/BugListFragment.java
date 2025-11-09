package com.example.debugappproject.ui.buglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentBugListBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.Constants;
import com.google.android.material.chip.Chip;

/**
 * BugListFragment - Displays list of all bugs with Material 3 chip-based filtering.
 *
 * Features:
 * - Chip group filters for difficulty and category
 * - Real-time filtering with visual feedback
 * - Enhanced empty state with helpful message
 * - Material 3 design with cards and proper spacing
 */
public class BugListFragment extends Fragment {

    private FragmentBugListBinding binding;
    private BugListViewModel viewModel;
    private BugAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBugListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BugListViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new BugAdapter(bug -> {
            // Navigate to bug detail
            Bundle args = new Bundle();
            args.putInt("bugId", bug.getId());
            Navigation.findNavController(requireView()).navigate(
                R.id.action_bugList_to_bugDetail, args
            );
        });

        binding.recyclerViewBugs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewBugs.setAdapter(adapter);
    }

    /**
     * Sets up chip-based filters for difficulty and category.
     */
    private void setupFilters() {
        // Difficulty filter chips
        binding.chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            String filter;

            if (checkedId == R.id.chip_all_difficulty) {
                filter = Constants.DIFFICULTY_ALL;
            } else if (checkedId == R.id.chip_difficulty_easy) {
                filter = Constants.DIFFICULTY_EASY;
            } else if (checkedId == R.id.chip_difficulty_medium) {
                filter = Constants.DIFFICULTY_MEDIUM;
            } else if (checkedId == R.id.chip_difficulty_hard) {
                filter = Constants.DIFFICULTY_HARD;
            } else {
                filter = Constants.DIFFICULTY_ALL;
            }

            viewModel.setDifficultyFilter(filter);
        });

        // Category filter chips
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            String filter;

            if (checkedId == R.id.chip_all_category) {
                filter = Constants.CATEGORY_ALL;
            } else if (checkedId == R.id.chip_category_loops) {
                filter = Constants.CATEGORY_LOOPS;
            } else if (checkedId == R.id.chip_category_arrays) {
                filter = Constants.CATEGORY_ARRAYS;
            } else if (checkedId == R.id.chip_category_oop) {
                filter = Constants.CATEGORY_OOP;
            } else if (checkedId == R.id.chip_category_strings) {
                filter = Constants.CATEGORY_STRINGS;
            } else if (checkedId == R.id.chip_category_conditionals) {
                filter = Constants.CATEGORY_CONDITIONALS;
            } else if (checkedId == R.id.chip_category_exceptions) {
                filter = Constants.CATEGORY_EXCEPTIONS;
            } else if (checkedId == R.id.chip_category_collections) {
                filter = Constants.CATEGORY_COLLECTIONS;
            } else if (checkedId == R.id.chip_category_methods) {
                filter = Constants.CATEGORY_METHODS;
            } else {
                filter = Constants.CATEGORY_ALL;
            }

            viewModel.setCategoryFilter(filter);
        });
    }

    /**
     * Sets up observers for filtered bugs list.
     * Shows/hides empty state based on results.
     */
    private void setupObservers() {
        viewModel.getFilteredBugs().observe(getViewLifecycleOwner(), bugs -> {
            adapter.submitList(bugs);

            // Show empty state if no bugs match filters
            if (bugs == null || bugs.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewBugs.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.recyclerViewBugs.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
