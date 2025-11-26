package com.example.debugappproject.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentProfileBinding;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.DateUtils;

import android.widget.Toast;

/**
 * Profile Fragment - Displays user progress, stats, achievements, and Pro status.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementAdapter achievementAdapter;
    private BillingManager billingManager;
    private int previousLevel = -1;

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
        billingManager = new BillingManager(requireContext());

        setupUI();
        setupAchievementsRecyclerView();
        setupObservers();

        viewModel.loadAchievements();
    }

    private void setupUI() {
        // Account status
        if (binding.textAccountStatus != null) {
            binding.textAccountStatus.setText("Free Account");
        }

        // Pro button
        if (binding.buttonAuthAction != null) {
            binding.buttonAuthAction.setOnClickListener(v -> navigateToSubscription());
        }

        // Settings button
        if (binding.buttonSettings != null) {
            binding.buttonSettings.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.action_profile_to_settings);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Observe Pro status
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), this::updateProUI);
    }

    private void updateProUI(boolean isPro) {
        if (binding == null) return;
        
        if (isPro) {
            if (binding.textAccountStatus != null) {
                binding.textAccountStatus.setText("👑 Pro Member");
            }
            if (binding.buttonAuthAction != null) {
                binding.buttonAuthAction.setText("Manage");
                binding.buttonAuthAction.setOnClickListener(v -> showProMemberDialog());
            }
        } else {
            if (binding.textAccountStatus != null) {
                binding.textAccountStatus.setText("Free Account");
            }
            if (binding.buttonAuthAction != null) {
                binding.buttonAuthAction.setText("🚀 Go Pro");
                binding.buttonAuthAction.setOnClickListener(v -> navigateToSubscription());
            }
        }
    }

    private void navigateToSubscription() {
        try {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.action_profile_to_subscription);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation to subscription failed", e);
            // Show dialog as fallback
            showUpgradeDialog();
        }
    }

    private void showUpgradeDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("🚀 Upgrade to Pro")
                .setMessage("Unlock all features with DebugMaster Pro!\n\n" +
                        "✓ 100+ debugging challenges\n" +
                        "✓ All 6 learning paths\n" +
                        "✓ Battle Arena multiplayer\n" +
                        "✓ Unlimited practice\n" +
                        "✓ Ad-free experience\n" +
                        "✓ Detailed analytics\n\n" +
                        "Starting at just $4.99/month!")
                .setPositiveButton("See Plans", (dialog, which) -> {
                    // Try navigation again
                    navigateToSubscription();
                })
                .setNegativeButton("Maybe Later", null)
                .show();
    }

    private void showProMemberDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("👑 Pro Member")
                .setMessage("You're a DebugMaster Pro member!\n\n" +
                        "All premium features are unlocked:\n" +
                        "• 100+ debugging challenges\n" +
                        "• All learning paths\n" +
                        "• Battle Arena access\n" +
                        "• No ads\n" +
                        "• Priority support\n\n" +
                        "Thank you for your support! 💚")
                .setPositiveButton("Awesome!", null)
                .setNeutralButton("Manage in Play Store", (dialog, which) -> {
                    Toast.makeText(getContext(), 
                        "Open Google Play Store > Subscriptions to manage", 
                        Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void setupAchievementsRecyclerView() {
        if (binding == null || binding.recyclerAchievements == null) return;
        
        achievementAdapter = new AchievementAdapter();
        binding.recyclerAchievements.setAdapter(achievementAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.recyclerAchievements.setLayoutManager(layoutManager);
    }

    private void setupObservers() {
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                displayUserProgress(progress);
            }
        });

        viewModel.getAchievementsWithStatus().observe(getViewLifecycleOwner(), achievements -> {
            if (achievements == null || binding == null) return;
            
            if (!achievements.isEmpty()) {
                achievementAdapter.setAchievements(achievements);
                
                if (binding.layoutEmpty != null) {
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
                if (binding.recyclerAchievements != null) {
                    binding.recyclerAchievements.setVisibility(View.VISIBLE);
                }

                long unlockedCount = achievements.stream()
                    .filter(AchievementWithStatus::isUnlocked)
                    .count();
                    
                if (binding.textAchievementsCount != null) {
                    binding.textAchievementsCount.setText(
                        unlockedCount + " / " + achievements.size() + " unlocked"
                    );
                }
            } else {
                if (binding.layoutEmpty != null) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
                if (binding.recyclerAchievements != null) {
                    binding.recyclerAchievements.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayUserProgress(UserProgress progress) {
        if (binding == null) return;
        
        try {
            int level = viewModel.calculateLevel(progress.getTotalXp());

            // Level up celebration
            if (previousLevel > 0 && level > previousLevel) {
                celebrateLevelUp(level);
            }
            previousLevel = level;

            // Display level
            if (binding.textLevel != null) {
                binding.textLevel.setText(String.valueOf(level));
            }

            // XP progress
            int xpInLevel = viewModel.getXpProgressInLevel(progress.getTotalXp());
            if (binding.textXp != null) {
                binding.textXp.setText(xpInLevel + " / 100 XP");
            }

            if (binding.progressXp != null) {
                binding.progressXp.setMax(100);
                AnimationUtil.animateProgress(binding.progressXp, xpInLevel, 800);
            }

            // Stats
            if (binding.textPerfectFixes != null) {
                binding.textPerfectFixes.setText(String.valueOf(progress.getBugsSolvedWithoutHints()));
            }

            int currentStreak = DateUtils.calculateCurrentStreak(
                progress.getLastCompletionDate(),
                progress.getCurrentStreakDays()
            );
            if (binding.textStreakDays != null) {
                binding.textStreakDays.setText(String.valueOf(currentStreak));
            }

            // Bugs solved
            viewModel.getTotalBugsCompleted(count -> {
                if (getActivity() != null && binding != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding.textBugsSolved != null) {
                            binding.textBugsSolved.setText(String.valueOf(count));
                        }
                    });
                }
            });

        } catch (Exception e) {
            android.util.Log.e(TAG, "Error displaying user progress", e);
        }
    }

    private void celebrateLevelUp(int newLevel) {
        if (getContext() == null || binding == null) return;
        
        Toast.makeText(requireContext(),
                "🎉 Level Up! You reached Level " + newLevel + "!",
                Toast.LENGTH_LONG).show();

        if (binding.textLevel != null) {
            AnimationUtil.bounceView(binding.textLevel);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }
}
