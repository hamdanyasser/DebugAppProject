package com.example.debugappproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.debugappproject.auth.AuthManager;
import com.example.debugappproject.databinding.FragmentProfileBinding;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.sync.ProgressSyncManager;
import com.example.debugappproject.sync.SyncManagerFactory;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.DateUtils;

import android.widget.Toast;

/**
 * Profile Fragment - Displays user progress, stats, and achievements.
 *
 * Features:
 * - Level and XP display with progress bar
 * - Stats: bugs solved, perfect fixes, current streak
 * - Achievements grid showing locked/unlocked achievements
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementAdapter achievementAdapter;
    private AuthManager authManager;
    private ProgressSyncManager syncManager;
    private int previousLevel = -1; // Track level changes for celebration

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        authManager = AuthManager.getInstance(requireContext());

        BugRepository repository = new BugRepository(requireActivity().getApplication());
        syncManager = SyncManagerFactory.createSyncManager(requireContext(), repository);

        setupAuthUI();
        setupAchievementsRecyclerView();
        setupObservers();

        // Load achievements
        viewModel.loadAchievements();
    }

    /**
     * Sets up auth UI (account status and sign in/out button).
     */
    private void setupAuthUI() {
        updateAuthUI();

        binding.buttonAuthAction.setOnClickListener(v -> {
            AnimationUtil.animatePress(v, () -> {
                if (authManager.isSignedIn()) {
                    // Sign out
                    authManager.signOut();
                    Toast.makeText(requireContext(), "Signed out. Local data preserved.", Toast.LENGTH_SHORT).show();
                    updateAuthUI();
                } else {
                    // TODO: Implement Google Sign-In when Firebase is configured
                    // For now, show explanation
                    Toast.makeText(requireContext(),
                        "Firebase not configured. Add google-services.json to enable sign-in.",
                        Toast.LENGTH_LONG).show();

                    // Example implementation (uncomment when Firebase is ready):
                    // GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    //     .requestIdToken(getString(R.string.default_web_client_id))
                    //     .requestEmail()
                    //     .build();
                    // GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
                    // Intent signInIntent = googleSignInClient.getSignInIntent();
                    // startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
        });
    }

    /**
     * Updates auth UI based on current auth state.
     */
    private void updateAuthUI() {
        if (authManager.isSignedIn()) {
            // Signed in
            String userName = authManager.getUserName();
            String email = authManager.getUserEmail();
            binding.textAccountStatus.setText(userName != null ? userName : email);
            binding.buttonAuthAction.setText("Sign Out");

            // Show last sync time
            long lastSync = syncManager.getLastSyncTimestamp();
            if (lastSync > 0) {
                long minutesAgo = (System.currentTimeMillis() - lastSync) / (1000 * 60);
                String syncText;
                if (minutesAgo < 1) {
                    syncText = "Last synced: Just now";
                } else if (minutesAgo < 60) {
                    syncText = "Last synced: " + minutesAgo + " min ago";
                } else {
                    long hoursAgo = minutesAgo / 60;
                    syncText = "Last synced: " + hoursAgo + " hours ago";
                }
                binding.textLastSync.setText(syncText);
                binding.textLastSync.setVisibility(View.VISIBLE);
            } else {
                binding.textLastSync.setVisibility(View.GONE);
            }
        } else {
            // Guest mode
            binding.textAccountStatus.setText("Guest");
            binding.buttonAuthAction.setText("Sign In");
            binding.textLastSync.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the achievements RecyclerView with a grid layout.
     */
    private void setupAchievementsRecyclerView() {
        achievementAdapter = new AchievementAdapter();
        binding.recyclerAchievements.setAdapter(achievementAdapter);

        // Use 2-column grid for achievements
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.recyclerAchievements.setLayoutManager(layoutManager);
    }

    /**
     * Sets up LiveData observers for user progress and achievements.
     */
    private void setupObservers() {
        // Observe user progress
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                displayUserProgress(progress);
            }
        });

        // Observe achievements
        viewModel.getAchievementsWithStatus().observe(getViewLifecycleOwner(), achievements -> {
            if (achievements != null && !achievements.isEmpty()) {
                achievementAdapter.setAchievements(achievements);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerAchievements.setVisibility(View.VISIBLE);

                // Update achievements count
                long unlockedCount = achievements.stream()
                    .filter(AchievementWithStatus::isUnlocked)
                    .count();
                binding.textAchievementsCount.setText(
                    unlockedCount + " of " + achievements.size() + " unlocked"
                );
            } else {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.recyclerAchievements.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Displays user progress data in the UI with smooth animations.
     */
    private void displayUserProgress(UserProgress progress) {
        // Calculate and display level
        int level = viewModel.calculateLevel(progress.getTotalXp());

        // Check for level up
        if (previousLevel > 0 && level > previousLevel) {
            // Level up! Celebrate!
            celebrateLevelUp(level);
        }
        previousLevel = level;

        binding.textLevel.setText(String.valueOf(level));

        // Calculate XP progress within current level
        int xpInLevel = viewModel.getXpProgressInLevel(progress.getTotalXp());
        int xpForNextLevel = viewModel.getXpForNextLevel(progress.getTotalXp());
        binding.textXp.setText(xpInLevel + " / 100 XP");

        // Animate progress bar smoothly
        AnimationUtil.animateProgress(binding.progressXp, xpInLevel, 800);

        // Display perfect fixes (bugs solved without hints) with bounce
        binding.textPerfectFixes.setText(String.valueOf(progress.getBugsSolvedWithoutHints()));
        if (progress.getBugsSolvedWithoutHints() > 0) {
            AnimationUtil.bounceView(binding.textPerfectFixes);
        }

        // Calculate and display current streak with animation
        int currentStreak = DateUtils.calculateCurrentStreak(
            progress.getLastCompletionDate(),
            progress.getCurrentStreakDays()
        );
        binding.textStreakDays.setText(String.valueOf(currentStreak));

        // Bounce streak counter if streak > 0
        if (currentStreak > 0) {
            AnimationUtil.bounceView(binding.textStreakDays);
        }

        // Get total bugs solved with count-up animation
        viewModel.getTotalBugsCompleted(count -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.textBugsSolved.setText(String.valueOf(count));
                    // Bounce the stats card on first load
                    if (count > 0 && previousLevel == level) {
                        AnimationUtil.fadeInWithScale(binding.textBugsSolved);
                    }
                });
            }
        });
    }

    /**
     * Celebrates when user levels up with special animations.
     */
    private void celebrateLevelUp(int newLevel) {
        // Show level-up message
        Toast.makeText(requireContext(),
                "🎉 Level Up! You reached Level " + newLevel + "!",
                Toast.LENGTH_LONG).show();

        // Celebrate the level badge
        AnimationUtil.celebrateLevelUp(binding.textLevel, () -> {
            // Callback after animation
        });

        // Bounce the entire profile card
        AnimationUtil.bounceView(binding.textLevel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
