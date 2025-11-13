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
import com.example.debugappproject.databinding.FragmentLearningPathsBinding;
import com.example.debugappproject.model.LearningPath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment displaying learning paths with progress.
 * Main "Learn" tab in bottom navigation.
 */
public class LearningPathsFragment extends Fragment {

    private FragmentLearningPathsBinding binding;
    private LearningPathsViewModel viewModel;
    private LearningPathAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLearningPathsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LearningPathsViewModel.class);

        setupRecyclerView();
        observePaths();
    }

    private void setupRecyclerView() {
        adapter = new LearningPathAdapter();
        binding.recyclerPaths.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPaths.setAdapter(adapter);

        adapter.setOnPathClickListener(path -> {
            // Navigate to path detail
            Bundle args = new Bundle();
            args.putInt("pathId", path.getId());
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_paths_to_pathDetail, args);
        });
    }

    private void observePaths() {
        viewModel.getAllPaths().observe(getViewLifecycleOwner(), paths -> {
            if (paths == null || paths.isEmpty()) {
                binding.recyclerPaths.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerPaths.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
                loadPathsWithProgress(paths);
            }
        });
    }

    /**
     * Load paths with their progress data.
     * Observes bug counts for each path and combines into PathWithProgress objects.
     */
    private void loadPathsWithProgress(List<LearningPath> paths) {
        List<PathWithProgress> pathsWithProgress = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);

        for (LearningPath path : paths) {
            // Create placeholder with 0 progress
            PathWithProgress pathData = new PathWithProgress(path, 0, 0);
            pathsWithProgress.add(pathData);

            // Load actual progress data
            final int index = pathsWithProgress.size() - 1;

            viewModel.getBugCountInPath(path.getId()).observe(getViewLifecycleOwner(), total -> {
                if (total != null) {
                    viewModel.getCompletedBugCountInPath(path.getId()).observe(getViewLifecycleOwner(), completed -> {
                        if (completed != null) {
                            pathsWithProgress.set(index, new PathWithProgress(path, total, completed));

                            // Update adapter when all paths loaded
                            if (loadedCount.incrementAndGet() == paths.size()) {
                                adapter.setPaths(new ArrayList<>(pathsWithProgress));
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
