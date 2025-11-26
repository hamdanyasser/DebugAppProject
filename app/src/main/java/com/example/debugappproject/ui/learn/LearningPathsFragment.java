package com.example.debugappproject.ui.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentLearningPathsBinding;
import com.example.debugappproject.model.LearningPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying learning paths with progress.
 * Main "Learn" tab in bottom navigation.
 */
public class LearningPathsFragment extends Fragment {

    private static final String TAG = "LearningPathsFragment";
    private FragmentLearningPathsBinding binding;
    private LearningPathsViewModel viewModel;
    private LearningPathAdapter adapter;
    private BillingManager billingManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        android.util.Log.d(TAG, "onCreateView");
        binding = FragmentLearningPathsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d(TAG, "onViewCreated");

        try {
            viewModel = new ViewModelProvider(this).get(LearningPathsViewModel.class);
            billingManager = new BillingManager(requireContext());
            
            setupUI();
            setupRecyclerView();
            observePaths();
            observeProStatus();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    private void setupUI() {
        // Go Pro button
        if (binding.buttonGoPro != null) {
            binding.buttonGoPro.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation to subscription failed", e);
                    Toast.makeText(getContext(), "Opening Pro subscription...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Pro banner card click
        if (binding.cardProBanner != null) {
            binding.cardProBanner.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation to subscription failed", e);
                }
            });
        }
    }

    private void observeProStatus() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (binding != null && binding.cardProBanner != null) {
                // Hide pro banner if user is already pro
                binding.cardProBanner.setVisibility(isPro ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setupRecyclerView() {
        if (binding == null || binding.recyclerPaths == null) {
            android.util.Log.e(TAG, "binding or recyclerPaths is null");
            return;
        }

        adapter = new LearningPathAdapter();
        binding.recyclerPaths.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPaths.setAdapter(adapter);

        adapter.setOnPathClickListener(path -> {
            // Check if path is locked and user is not pro
            if (path.isLocked() && !billingManager.isProUserSync()) {
                // Show upgrade prompt
                Toast.makeText(getContext(), "🔒 This path requires Pro. Upgrade to unlock!", Toast.LENGTH_SHORT).show();
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation error", e);
                }
            } else {
                // Navigate to path detail
                navigateToPathDetail(path.getId());
            }
        });

        android.util.Log.d(TAG, "RecyclerView setup complete");
    }

    private void navigateToPathDetail(int pathId) {
        try {
            if (getView() == null) {
                android.util.Log.e(TAG, "View is null");
                return;
            }

            Bundle args = new Bundle();
            args.putInt("pathId", pathId);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_paths_to_pathDetail, args);

        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Could not open path", Toast.LENGTH_SHORT).show();
        }
    }

    private void observePaths() {
        if (viewModel == null) {
            android.util.Log.e(TAG, "viewModel is null");
            return;
        }

        viewModel.getAllPaths().observe(getViewLifecycleOwner(), paths -> {
            android.util.Log.d(TAG, "Paths received: " + (paths != null ? paths.size() : "null"));

            if (binding == null) return;

            if (paths == null || paths.isEmpty()) {
                if (binding.recyclerPaths != null) {
                    binding.recyclerPaths.setVisibility(View.GONE);
                }
                if (binding.layoutEmpty != null) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                if (binding.recyclerPaths != null) {
                    binding.recyclerPaths.setVisibility(View.VISIBLE);
                }
                if (binding.layoutEmpty != null) {
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
                loadPathsWithProgress(paths);
            }
        });
    }

    private void loadPathsWithProgress(List<LearningPath> paths) {
        if (paths == null || paths.isEmpty() || adapter == null) {
            return;
        }

        // Create list with 0 progress initially
        List<PathWithProgress> pathsWithProgress = new ArrayList<>();
        for (LearningPath path : paths) {
            pathsWithProgress.add(new PathWithProgress(path, 0, 0));
        }
        
        // Update adapter immediately
        adapter.setPaths(pathsWithProgress);

        // Calculate total progress
        int totalCompleted = 0;
        int totalBugs = 0;

        // Load actual progress for each path
        for (int i = 0; i < paths.size(); i++) {
            final int index = i;
            LearningPath path = paths.get(i);

            try {
                viewModel.getBugCountInPath(path.getId()).observe(getViewLifecycleOwner(), total -> {
                    if (total != null && total > 0) {
                        viewModel.getCompletedBugCountInPath(path.getId()).observe(getViewLifecycleOwner(), completed -> {
                            if (completed != null && index < pathsWithProgress.size()) {
                                pathsWithProgress.set(index, new PathWithProgress(path, total, completed));
                                if (adapter != null) {
                                    adapter.notifyItemChanged(index);
                                }
                                
                                // Update total progress
                                updateTotalProgress(pathsWithProgress);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error loading progress for path " + path.getId(), e);
            }
        }
    }

    private void updateTotalProgress(List<PathWithProgress> paths) {
        int totalBugs = 0;
        int completedBugs = 0;
        
        for (PathWithProgress p : paths) {
            totalBugs += p.getTotalBugs();
            completedBugs += p.getCompletedBugs();
        }
        
        int percent = totalBugs > 0 ? (completedBugs * 100) / totalBugs : 0;
        
        if (binding != null && binding.textTotalProgress != null) {
            binding.textTotalProgress.setText(percent + "% Complete");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh pro status when returning to this screen
        if (billingManager != null) {
            billingManager.refreshPurchases();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.destroy();
        }
        binding = null;
        adapter = null;
    }
}
