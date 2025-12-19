package com.example.debugappproject.ui.buglist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

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
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.chip.Chip;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - BUG LIST FRAGMENT                                    ║
 * ║              Browse Challenges with Sound Effects                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Chip group filters for difficulty and category with sounds
 * - Real-time filtering with visual feedback
 * - Enhanced empty state with helpful message
 * - Premium entrance animations
 * - Sound effects for all interactions
 */
public class BugListFragment extends Fragment {

    private FragmentBugListBinding binding;
    private BugListViewModel viewModel;
    private BugAdapter adapter;
    private SoundManager soundManager;

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
        soundManager = SoundManager.getInstance(requireContext());

        // Play entrance sound
        soundManager.playSound(SoundManager.Sound.TRANSITION);

        setupRecyclerView();
        setupSearchBar();
        setupFilters();
        setupObservers();
        playEntranceAnimations();
    }

    /**
     * Premium entrance animations for the bug list
     */
    private void playEntranceAnimations() {
        // Search bar slides down
        binding.searchBarLayout.setAlpha(0f);
        binding.searchBarLayout.setTranslationY(-50f);
        binding.searchBarLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Difficulty chips slide in from left
        binding.chipGroupDifficulty.setAlpha(0f);
        binding.chipGroupDifficulty.setTranslationX(-100f);
        binding.chipGroupDifficulty.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(150)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Category chips slide in from right
        binding.chipGroupCategory.setAlpha(0f);
        binding.chipGroupCategory.setTranslationX(100f);
        binding.chipGroupCategory.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(250)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // RecyclerView fades in
        binding.recyclerViewBugs.setAlpha(0f);
        binding.recyclerViewBugs.animate()
                .alpha(1f)
                .setStartDelay(350)
                .setDuration(500)
                .start();
    }

    private void setupRecyclerView() {
        adapter = new BugAdapter(bug -> {
            // Play sound on bug selection
            soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            
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
     * Sets up the search bar with real-time text filtering and sounds.
     */
    private void setupSearchBar() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Play subtle typing sound for significant changes
                if (count > 0 && s.length() > 0) {
                    soundManager.playSound(SoundManager.Sound.TICK);
                }
                // Update search query in ViewModel
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    /**
     * Sets up chip-based filters with sound effects.
     */
    private void setupFilters() {
        // Difficulty filter chips with sound
        binding.chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            // Play chip selection sound
            soundManager.playSound(SoundManager.Sound.BLIP);

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
            
            // Animate the selected chip
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                animateChipSelection(selectedChip);
            }
        });

        // Category filter chips with sound
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            // Play chip selection sound
            soundManager.playSound(SoundManager.Sound.BLIP);

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
            
            // Animate the selected chip
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                animateChipSelection(selectedChip);
            }
        });
    }

    /**
     * Animates chip selection with a bounce effect
     */
    private void animateChipSelection(Chip chip) {
        chip.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    chip.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * Sets up observers for filtered bugs list with animations.
     */
    private void setupObservers() {
        viewModel.getFilteredBugs().observe(getViewLifecycleOwner(), bugs -> {
            adapter.submitList(bugs);

            // Show empty state if no bugs match filters
            if (bugs == null || bugs.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewBugs.setVisibility(View.GONE);
                
                // Play notification sound for empty state
                soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                
                // Animate empty state
                binding.layoutEmptyState.setAlpha(0f);
                binding.layoutEmptyState.setScaleX(0.8f);
                binding.layoutEmptyState.setScaleY(0.8f);
                binding.layoutEmptyState.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.recyclerViewBugs.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (soundManager != null) {
            soundManager.resumeAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pauseAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear RecyclerView adapter to prevent memory leaks
        if (binding != null && binding.recyclerViewBugs != null) {
            binding.recyclerViewBugs.setAdapter(null);
        }
        adapter = null;
        binding = null;
    }
}
