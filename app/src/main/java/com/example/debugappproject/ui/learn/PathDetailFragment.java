package com.example.debugappproject.ui.learn;

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
import com.example.debugappproject.databinding.FragmentPathDetailBinding;
import com.example.debugappproject.model.LearningPath;

/**
 * Path Detail Fragment - Shows details of a learning path and its bugs.
 *
 * Features:
 * - Displays path name, icon, description
 * - Shows progress (completed/total bugs)
 * - Lists all bugs in the path
 * - Navigate to bug detail on click
 */
public class PathDetailFragment extends Fragment {

    private FragmentPathDetailBinding binding;
    private PathDetailViewModel viewModel;
    private BugInPathAdapter bugAdapter;
    private int pathId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPathDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PathDetailViewModel.class);

        // Get path ID from arguments
        if (getArguments() != null) {
            pathId = getArguments().getInt("pathId", 1);
        }

        setupBugsRecyclerView();
        setupObservers();

        // Load the path
        viewModel.loadPath(pathId);
    }

    /**
     * Sets up the bugs RecyclerView.
     */
    private void setupBugsRecyclerView() {
        bugAdapter = new BugInPathAdapter(bug -> {
            // Navigate to bug detail
            Bundle args = new Bundle();
            args.putInt("bugId", bug.getId());
            Navigation.findNavController(requireView()).navigate(
                R.id.action_pathDetail_to_bugDetail,
                args
            );
        });

        binding.recyclerBugs.setAdapter(bugAdapter);
        binding.recyclerBugs.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Sets up LiveData observers.
     */
    private void setupObservers() {
        // Observe current path
        viewModel.getCurrentPath().observe(getViewLifecycleOwner(), path -> {
            if (path != null) {
                displayPathInfo(path);
            }
        });

        // Observe bugs in path
        viewModel.getBugsInPath().observe(getViewLifecycleOwner(), bugs -> {
            if (bugs != null && !bugs.isEmpty()) {
                bugAdapter.setBugs(bugs);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerBugs.setVisibility(View.VISIBLE);

                // Update progress
                updateProgress(bugs);
            } else {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerBugs.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Displays path information.
     */
    private void displayPathInfo(LearningPath path) {
        binding.textIcon.setText(path.getIconEmoji());
        binding.textPathName.setText(path.getName());
        binding.textDescription.setText(path.getDescription());
    }

    /**
     * Updates progress based on completed bugs.
     */
    private void updateProgress(java.util.List<BugInPathWithDetails> bugs) {
        int total = bugs.size();
        long completed = bugs.stream()
            .filter(BugInPathWithDetails::isCompleted)
            .count();

        int percentage = (int) ((completed * 100.0) / total);

        binding.textProgress.setText(completed + " of " + total + " completed");
        binding.textPercentage.setText(percentage + "%");
        binding.progressBar.setProgress(percentage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
