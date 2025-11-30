package com.example.debugappproject.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - PROFILE & ACHIEVEMENTS                               ║
 * ║              Track Progress with Sound Effects                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Note: Google Sign-In is disabled for this demo version.
 * All features work in local/guest mode.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private AchievementAdapter achievementAdapter;
    private BillingManager billingManager;
    private SoundManager soundManager;
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
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());

        // Play entrance sound
        soundManager.playSound(SoundManager.Sound.TRANSITION);

        setupUI();
        setupAchievementsRecyclerView();
        setupObservers();

        viewModel.loadAchievements();
    }

    private void setupUI() {
        boolean isPro = billingManager.isProUserSync();
        android.util.Log.d(TAG, "setupUI: Pro status = " + isPro);

        updateAccountUI();

        if (binding.buttonSettings != null) {
            binding.buttonSettings.setOnClickListener(v -> {
                soundManager.playButtonClick();
                try {
                    Navigation.findNavController(v).navigate(R.id.action_profile_to_settings);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Google Sign-In button (demo - just show message)
        if (binding.buttonGoogleSignIn != null) {
            binding.buttonGoogleSignIn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showSignInDemoMessage();
            });
        }

        // Sign Out button (hidden by default in demo)
        if (binding.buttonSignOut != null) {
            binding.buttonSignOut.setVisibility(View.GONE);
        }

        billingManager.getIsProUser().observe(getViewLifecycleOwner(), this::updateProUI);
    }

    /**
     * Update account section UI
     */
    private void updateAccountUI() {
        if (binding == null) return;
        
        boolean isPro = billingManager.isProUserSync();
        
        // Guest mode display
        if (binding.textUserName != null) {
            binding.textUserName.setText("Debug Master");
            binding.textUserName.setVisibility(View.VISIBLE);
        }
        
        if (binding.textUserEmail != null) {
            binding.textUserEmail.setText("Local Progress Mode");
            binding.textUserEmail.setVisibility(View.VISIBLE);
        }
        
        if (binding.textAccountStatus != null) {
            binding.textAccountStatus.setText(isPro ? "👑 Pro Member" : "Guest Mode");
        }
        
        // Show sign in button
        if (binding.buttonGoogleSignIn != null) {
            binding.buttonGoogleSignIn.setVisibility(View.VISIBLE);
        }
        if (binding.buttonSignOut != null) {
            binding.buttonSignOut.setVisibility(View.GONE);
        }
        
        // Update auth action button
        if (binding.buttonAuthAction != null) {
            if (isPro) {
                binding.buttonAuthAction.setText("Manage");
                binding.buttonAuthAction.setOnClickListener(v -> {
                    soundManager.playButtonClick();
                    showProMemberDialog();
                });
            } else {
                binding.buttonAuthAction.setText("🚀 Go Pro");
                binding.buttonAuthAction.setOnClickListener(v -> {
                    soundManager.playSound(SoundManager.Sound.POWER_UP);
                    navigateToSubscription();
                });
            }
        }
    }

    /**
     * Show message when Google Sign-In is tapped (demo mode)
     */
    private void showSignInDemoMessage() {
        if (getContext() == null) return;
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🔐 Google Sign-In")
            .setMessage("Google Sign-In is available in the full release!\n\n" +
                    "Benefits of signing in:\n" +
                    "• Sync progress across devices\n" +
                    "• Cloud backup of achievements\n" +
                    "• Compete on global leaderboards\n" +
                    "• Restore progress on new device\n\n" +
                    "For now, all your progress is saved locally on this device.")
            .setPositiveButton("Got it!", (dialog, which) -> {
                soundManager.playButtonClick();
            })
            .show();
    }

    private void updateProUI(boolean isPro) {
        if (binding == null) return;

        android.util.Log.d(TAG, "updateProUI: isPro=" + isPro);
        updateAccountUI();
    }

    private void navigateToSubscription() {
        try {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.action_profile_to_subscription);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation to subscription failed", e);
            showUpgradeDialog();
        }
    }

    private void showUpgradeDialog() {
        if (getContext() == null) return;

        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        new AlertDialog.Builder(requireContext())
                .setTitle("🚀 Upgrade to Pro")
                .setMessage("Unlock all features with DebugMaster Pro!\n\n" +
                        "✓ 100+ debugging challenges\n" +
                        "✓ All 15 learning paths\n" +
                        "✓ Battle Arena multiplayer\n" +
                        "✓ Unlimited practice\n" +
                        "✓ Ad-free experience\n" +
                        "✓ Detailed analytics\n\n" +
                        "Starting at just $4.99/month!")
                .setPositiveButton("See Plans", (dialog, which) -> {
                    soundManager.playButtonClick();
                    navigateToSubscription();
                })
                .setNegativeButton("Maybe Later", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                })
                .show();
    }

    private void showProMemberDialog() {
        if (getContext() == null) return;

        soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
        new AlertDialog.Builder(requireContext())
                .setTitle("👑 Pro Member")
                .setMessage("You're a DebugMaster Pro member!\n\n" +
                        "All premium features are unlocked:\n" +
                        "• 100+ debugging challenges\n" +
                        "• All 15 learning paths\n" +
                        "• Battle Arena access\n" +
                        "• No ads\n" +
                        "• Priority support\n\n" +
                        "Thank you for your support! 💚")
                .setPositiveButton("Awesome!", (dialog, which) -> {
                    soundManager.playButtonClick();
                })
                .setNeutralButton("Manage in Play Store", (dialog, which) -> {
                    soundManager.playButtonClick();
                    Toast.makeText(getContext(), 
                        "Open Google Play Store > Subscriptions to manage", 
                        Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void setupAchievementsRecyclerView() {
        if (binding == null || binding.recyclerAchievements == null) return;
        
        achievementAdapter = new AchievementAdapter(achievement -> {
            // Achievement click handler with sound
            soundManager.playSound(SoundManager.Sound.BLIP);
        });
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

            // Level up celebration with SOUND!
            if (previousLevel > 0 && level > previousLevel) {
                celebrateLevelUp(level);
            }
            previousLevel = level;

            if (binding.textLevel != null) {
                binding.textLevel.setText(String.valueOf(level));
            }

            int xpInLevel = viewModel.getXpProgressInLevel(progress.getTotalXp());
            if (binding.textXp != null) {
                binding.textXp.setText(xpInLevel + " / 100 XP");
            }

            if (binding.progressXp != null) {
                binding.progressXp.setMax(100);
                AnimationUtil.animateProgress(binding.progressXp, xpInLevel, 800);
            }

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

    /**
     * Celebrate level up with EPIC sound and animation!
     */
    private void celebrateLevelUp(int newLevel) {
        if (getContext() == null || binding == null) return;
        
        // Play LEVEL UP sound!
        soundManager.playLevelUp();
        
        Toast.makeText(requireContext(),
                "🎉 Level Up! You reached Level " + newLevel + "!",
                Toast.LENGTH_LONG).show();

        if (binding.textLevel != null) {
            AnimationUtil.bounceView(binding.textLevel);
        }
        
        // Play star sound after delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            soundManager.playSound(SoundManager.Sound.STAR_EARNED);
        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume: checking Pro status");
        if (billingManager != null) {
            billingManager.refreshPurchases();
            android.util.Log.d(TAG, "onResume: Pro status after refresh = " + billingManager.isProUserSync());
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
            billingManager.clearCallback();
        }
        achievementAdapter = null;
        binding = null;
    }
}
