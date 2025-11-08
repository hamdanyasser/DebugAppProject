package com.example.debugappproject.ui.buglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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

/**
 * BugListFragment - Displays list of all bugs with filtering options.
 * Allows filtering by difficulty and category.
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

    private void setupFilters() {
        // Difficulty filter
        String[] difficulties = {
            Constants.DIFFICULTY_ALL,
            Constants.DIFFICULTY_EASY,
            Constants.DIFFICULTY_MEDIUM,
            Constants.DIFFICULTY_HARD
        };
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            difficulties
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDifficulty.setAdapter(difficultyAdapter);

        binding.spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setDifficultyFilter(difficulties[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Category filter
        String[] categories = {
            Constants.CATEGORY_ALL,
            Constants.CATEGORY_LOOPS,
            Constants.CATEGORY_ARRAYS,
            Constants.CATEGORY_OOP,
            Constants.CATEGORY_STRINGS,
            Constants.CATEGORY_CONDITIONALS,
            Constants.CATEGORY_EXCEPTIONS,
            Constants.CATEGORY_COLLECTIONS,
            Constants.CATEGORY_METHODS
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setCategoryFilter(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupObservers() {
        viewModel.getFilteredBugs().observe(getViewLifecycleOwner(), bugs -> {
            adapter.submitList(bugs);
            // Show empty state if no bugs
            if (bugs == null || bugs.isEmpty()) {
                binding.textEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewBugs.setVisibility(View.GONE);
            } else {
                binding.textEmptyState.setVisibility(View.GONE);
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
