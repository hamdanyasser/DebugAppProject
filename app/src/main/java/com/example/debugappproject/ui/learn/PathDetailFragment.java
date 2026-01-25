package com.example.debugappproject.ui.learn;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.debugmaster.app.R;
import com.debugmaster.app.databinding.FragmentPathDetailBinding;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - PATH DETAIL (ENHANCED)                               ║
 * ║         Interactive Learning with Animated Tutorials                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Displays path name, icon, description
 * - Shows progress (completed/total bugs)
 * - Lists all bugs in the path with animations
 * - Navigate to bug detail on click
 * - Interactive tutorial tips
 * - Engaging animations
 */
public class PathDetailFragment extends Fragment {

    private static final String TAG = "PathDetailFragment";
    
    private FragmentPathDetailBinding binding;
    private PathDetailViewModel viewModel;
    private BugInPathAdapter bugAdapter;
    private SoundManager soundManager;
    private int pathId;
    private Handler handler = new Handler(Looper.getMainLooper());

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
        soundManager = SoundManager.getInstance(requireContext());

        // Get path ID from arguments
        if (getArguments() != null) {
            pathId = getArguments().getInt("pathId", 1);
        }

        setupUI();
        setupBugsRecyclerView();
        setupObservers();
        playEntranceAnimations();

        // Load the path
        viewModel.loadPath(pathId);
        
        // Play entrance sound
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void setupUI() {
        // Back button
        if (binding.buttonBack != null) {
            binding.buttonBack.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Navigation.findNavController(requireView()).navigateUp();
            });
        }
        
        // Start learning button
        if (binding.buttonStartLearning != null) {
            binding.buttonStartLearning.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                startFirstIncompleteLesson();
            });
        }
    }
    
    private void startFirstIncompleteLesson() {
        if (bugAdapter != null) {
            java.util.List<BugInPathWithDetails> bugs = bugAdapter.getBugs();
            if (bugs != null && !bugs.isEmpty()) {
                // Find first incomplete bug
                for (BugInPathWithDetails bug : bugs) {
                    if (!bug.isCompleted()) {
                        Bundle args = new Bundle();
                        args.putInt("bugId", bug.getId());
                        Navigation.findNavController(requireView()).navigate(
                            R.id.action_pathDetail_to_bugDetail,
                            args
                        );
                        return;
                    }
                }
                // All completed - show message
                Toast.makeText(requireContext(), "🎉 All lessons completed!", Toast.LENGTH_SHORT).show();
                soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
            }
        }
    }
    
    private void playEntranceAnimations() {
        // Animate header
        if (binding.layoutHeader != null) {
            binding.layoutHeader.setAlpha(0f);
            binding.layoutHeader.setTranslationY(-50f);
            binding.layoutHeader.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        }
        
        // Animate progress card
        if (binding.cardProgress != null) {
            binding.cardProgress.setAlpha(0f);
            binding.cardProgress.setScaleX(0.9f);
            binding.cardProgress.setScaleY(0.9f);
            binding.cardProgress.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(200)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
        }
        
        // Animate recycler view
        if (binding.recyclerBugs != null) {
            binding.recyclerBugs.setAlpha(0f);
            binding.recyclerBugs.animate()
                .alpha(1f)
                .setStartDelay(400)
                .setDuration(300)
                .start();
        }
    }

    /**
     * Sets up the bugs RecyclerView.
     */
    private void setupBugsRecyclerView() {
        bugAdapter = new BugInPathAdapter(bug -> {
            // Play click sound
            soundManager.playButtonClick();
            
            // Navigate to bug detail with animation
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
                
                // Show tutorial tip for first-time users
                showTutorialTipIfNeeded(bugs);
            } else {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerBugs.setVisibility(View.GONE);
            }
        });
    }
    
    private void showTutorialTipIfNeeded(java.util.List<BugInPathWithDetails> bugs) {
        // Check if this is the user's first time
        long completed = bugs.stream().filter(BugInPathWithDetails::isCompleted).count();
        
        if (completed == 0 && binding.cardTutorialTip != null) {
            // Show tutorial tip
            binding.cardTutorialTip.setVisibility(View.VISIBLE);
            binding.cardTutorialTip.setAlpha(0f);
            binding.cardTutorialTip.animate()
                .alpha(1f)
                .setStartDelay(600)
                .setDuration(400)
                .start();
                
            // Set up dismiss
            if (binding.buttonDismissTip != null) {
                binding.buttonDismissTip.setOnClickListener(v -> {
                    soundManager.playButtonClick();
                    binding.cardTutorialTip.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> binding.cardTutorialTip.setVisibility(View.GONE))
                        .start();
                });
            }
        }
    }

    /**
     * Displays path information.
     */
    private void displayPathInfo(LearningPath path) {
        if (binding.textIcon != null) {
            binding.textIcon.setText(path.getIconEmoji());
        }
        if (binding.textPathName != null) {
            binding.textPathName.setText(path.getName());
        }
        if (binding.textDescription != null) {
            binding.textDescription.setText(path.getDescription());
        }
        
        // Show tutorial content if available
        if (binding.textTutorialContent != null && path.getTutorialContent() != null) {
            binding.textTutorialContent.setText("💡 " + path.getTutorialContent());
            binding.textTutorialContent.setVisibility(View.VISIBLE);
        }
        
        // Show difficulty
        if (binding.textDifficulty != null) {
            String diff = path.getDifficultyRange();
            binding.textDifficulty.setText(diff != null ? diff : "Beginner");
        }
        
        // Show XP reward
        if (binding.textXpReward != null) {
            binding.textXpReward.setText("⭐ " + path.getXpReward() + " XP");
        }
    }

    /**
     * Updates progress based on completed bugs.
     */
    private void updateProgress(java.util.List<BugInPathWithDetails> bugs) {
        int total = bugs.size();
        long completed = bugs.stream()
            .filter(BugInPathWithDetails::isCompleted)
            .count();

        int percentage = total > 0 ? (int) ((completed * 100.0) / total) : 0;

        if (binding.textProgress != null) {
            binding.textProgress.setText(completed + " of " + total + " completed");
        }
        if (binding.textPercentage != null) {
            binding.textPercentage.setText(percentage + "%");
        }
        if (binding.progressBar != null) {
            // Animate progress bar
            ObjectAnimator animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, percentage);
            animator.setDuration(800);
            animator.setInterpolator(new OvershootInterpolator(1.0f));
            animator.start();
        }
        
        // Update button text based on progress
        if (binding.buttonStartLearning != null) {
            if (percentage == 0) {
                binding.buttonStartLearning.setText("🚀 Start Learning");
            } else if (percentage >= 100) {
                binding.buttonStartLearning.setText("🔄 Review");
            } else {
                binding.buttonStartLearning.setText("▶️ Continue");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks
        handler.removeCallbacksAndMessages(null);
        if (binding != null && binding.recyclerBugs != null) {
            binding.recyclerBugs.setAdapter(null);
        }
        bugAdapter = null;
        binding = null;
    }
}
