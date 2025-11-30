package com.example.debugappproject.ui.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
import com.example.debugappproject.util.SoundManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - LEARNING PATHS                                       ║
 * ║              Structured Learning with Sound Effects                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Fragment displaying learning paths with progress.
 * Main "Learn" tab in bottom navigation.
 */
public class LearningPathsFragment extends Fragment {

    private static final String TAG = "LearningPathsFragment";
    private FragmentLearningPathsBinding binding;
    private LearningPathsViewModel viewModel;
    private LearningPathAdapter adapter;
    private BillingManager billingManager;
    private SoundManager soundManager;

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
            billingManager = BillingManager.getInstance(requireContext());
            soundManager = SoundManager.getInstance(requireContext());
            
            // Play entrance sound
            soundManager.playSound(SoundManager.Sound.TRANSITION);
            
            setupUI();
            setupRecyclerView();
            observePaths();
            observeProStatus();
            playEntranceAnimations();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    /**
     * Premium entrance animations
     */
    private void playEntranceAnimations() {
        // Progress text fades in
        if (binding.textTotalProgress != null) {
            binding.textTotalProgress.setAlpha(0f);
            binding.textTotalProgress.animate()
                    .alpha(1f)
                    .setStartDelay(200)
                    .setDuration(400)
                    .start();
        }

        // Pro banner scales in with bounce
        if (binding.cardProBanner != null && binding.cardProBanner.getVisibility() == View.VISIBLE) {
            binding.cardProBanner.setAlpha(0f);
            binding.cardProBanner.setScaleX(0.8f);
            binding.cardProBanner.setScaleY(0.8f);
            binding.cardProBanner.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(300)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        // RecyclerView fades in
        if (binding.recyclerPaths != null) {
            binding.recyclerPaths.setAlpha(0f);
            binding.recyclerPaths.animate()
                    .alpha(1f)
                    .setStartDelay(400)
                    .setDuration(500)
                    .start();
        }
    }

    private void setupUI() {
        // Go Pro button with sound
        if (binding.buttonGoPro != null) {
            binding.buttonGoPro.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateButton(v);
                try {
                    Navigation.findNavController(v).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation to subscription failed", e);
                    Toast.makeText(getContext(), "Opening Pro subscription...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Pro banner card click with sound
        if (binding.cardProBanner != null) {
            binding.cardProBanner.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCard(v);
                try {
                    Navigation.findNavController(v).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation to subscription failed", e);
                }
            });
        }
    }

    /**
     * Animate button press
     */
    private void animateButton(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * Animate card press
     */
    private void animateCard(View card) {
        card.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(100)
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    private void observeProStatus() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (binding != null && binding.cardProBanner != null) {
                // Hide pro banner if user is already pro
                binding.cardProBanner.setVisibility(isPro ? View.GONE : View.VISIBLE);
            }
            // CRITICAL: Update adapter's pro status to unlock paths
            if (adapter != null) {
                adapter.setProStatus(isPro);
            }
        });
    }

    private void setupRecyclerView() {
        if (binding == null || binding.recyclerPaths == null) {
            android.util.Log.e(TAG, "binding or recyclerPaths is null");
            return;
        }

        adapter = new LearningPathAdapter();
        // Initialize adapter with current Pro status
        adapter.setProStatus(billingManager.isProUserSync());
        binding.recyclerPaths.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPaths.setAdapter(adapter);

        adapter.setOnPathClickListener(path -> {
            // Play click sound
            soundManager.playButtonClick();
            
            // Check if path is locked and user is not pro
            if (path.isLocked() && !billingManager.isProUserSync()) {
                // Show attractive Pro upgrade dialog
                showProUpgradeDialog(path);
            } else {
                // Play success sound for accessible path
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToPathDetail(path.getId());
            }
        });

        android.util.Log.d(TAG, "RecyclerView setup complete");
    }

    /**
     * Shows an attractive dialog encouraging Pro upgrade
     */
    private void showProUpgradeDialog(LearningPath path) {
        if (getContext() == null) return;
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        String pathName = path.getName() != null ? path.getName() : "this course";
        int xp = path.getXpReward() > 0 ? path.getXpReward() : 100;
        int lessons = path.getTotalLessons() > 0 ? path.getTotalLessons() : 10;
        
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("👑 Unlock " + pathName)
            .setMessage(
                "This premium course includes:\n\n" +
                "📚 " + lessons + " interactive lessons\n" +
                "⭐ " + xp + " XP reward\n" +
                "🏆 Completion certificate\n" +
                "🔓 Lifetime access\n\n" +
                "Upgrade to Pro to unlock ALL 14+ courses, Battle Arena, and more!\n\n" +
                "✨ Join 10,000+ developers mastering debugging!")
            .setPositiveButton("🚀 Go Pro", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_paths_to_subscription);
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Navigation error", e);
                }
            })
            .setNegativeButton("Maybe Later", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            })
            .show();
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
        if (billingManager != null) {
            billingManager.destroy();
        }
        binding = null;
        adapter = null;
    }
}
