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

import com.debugmaster.app.R;
import com.example.debugappproject.billing.BillingManager;
import com.debugmaster.app.databinding.FragmentProfileBinding;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.DateUtils;
import com.example.debugappproject.util.SoundManager;
import com.example.debugappproject.ui.shop.ShopFragment;
import com.example.debugappproject.ui.profile.CosmeticChooserDialog;

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
    private AuthManager authManager;
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
        authManager = AuthManager.getInstance(requireContext());

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
     * Update account section UI with AuthManager integration
     */
    private void updateAccountUI() {
        if (binding == null) return;
        
        boolean isPro = billingManager.isProUserSync();
        boolean isGuest = authManager.isGuest();
        String displayName = authManager.getDisplayName();
        String email = authManager.getEmail();
        
        // Determine which avatar to show and if it's premium
        boolean hasPremiumAvatars = ShopFragment.hasUnlockedAvatars(requireContext());
        String selectedPremiumAvatar = ShopFragment.getSelectedAvatar(requireContext());
        boolean isUsingPremiumAvatar = hasPremiumAvatars && selectedPremiumAvatar != null && !selectedPremiumAvatar.isEmpty();
        
        // Use premium avatar if selected, otherwise use free avatar
        String avatar = isUsingPremiumAvatar 
            ? selectedPremiumAvatar
            : authManager.getAvatarEmoji();
        
        // Display user avatar emoji (clickable to change)
        if (binding.textUserAvatar != null) {
            binding.textUserAvatar.setText(avatar);
        }
        
        // Update collection badge visibility
        updateCollectionBadge(isUsingPremiumAvatar, hasPremiumAvatars);
        
        // Set up avatar container click listener (larger touch target)
        if (binding.containerAvatar != null) {
            binding.containerAvatar.setOnClickListener(v -> {
                soundManager.playButtonClick();
                // Animate the tap
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();
                // If user has premium avatars, show collection chooser first
                if (ShopFragment.hasUnlockedAvatars(requireContext())) {
                    showAvatarCollectionChooser();
                } else {
                    showAvatarSelector();
                }
            });
            
            // Apply premium glow effect if user has premium avatars
            if (hasPremiumAvatars && binding.viewPremiumGlow != null) {
                binding.viewPremiumGlow.setVisibility(View.VISIBLE);
                binding.containerAvatar.setBackgroundResource(R.drawable.bg_avatar_premium_circle);
            }
            
            // Show tap hint for first-time users
            if (binding.textTapHint != null) {
                binding.textTapHint.setVisibility(View.VISIBLE);
            }
        } else if (binding.textUserAvatar != null) {
            // Fallback: Use the avatar text view directly
            binding.textUserAvatar.setOnClickListener(v -> {
                soundManager.playButtonClick();
                if (ShopFragment.hasUnlockedAvatars(requireContext())) {
                    showAvatarCollectionChooser();
                } else {
                    showAvatarSelector();
                }
            });
        }
        
        // User name display with custom title
        if (binding.textUserName != null) {
            String title = ShopFragment.hasUnlockedTitles(requireContext()) 
                ? ShopFragment.getSelectedTitle(requireContext()) 
                : "";
            if (title != null && !title.isEmpty()) {
                binding.textUserName.setText(displayName + " • " + title);
            } else {
                binding.textUserName.setText(displayName);
            }
            binding.textUserName.setVisibility(View.VISIBLE);
            
            // Make name clickable to change title if unlocked
            if (ShopFragment.hasUnlockedTitles(requireContext())) {
                binding.textUserName.setOnClickListener(v -> {
                    soundManager.playButtonClick();
                    showTitleCollectionChooser();
                });
            }
        }
        
        // Email or guest mode display
        if (binding.textUserEmail != null) {
            if (isGuest) {
                binding.textUserEmail.setText("Guest Mode - Progress saved locally");
            } else {
                binding.textUserEmail.setText(maskEmail(email));
            }
            binding.textUserEmail.setVisibility(View.VISIBLE);
        }
        
        // Account status
        if (binding.textAccountStatus != null) {
            String status = "";
            if (isPro) {
                status = "👑 Pro Member";
            } else if (isGuest) {
                status = "🎮 Guest";
            } else {
                status = "✅ Registered";
            }
            binding.textAccountStatus.setText(status);
        }
        
        // Sign in / Sign out buttons
        if (binding.buttonGoogleSignIn != null) {
            binding.buttonGoogleSignIn.setVisibility(isGuest ? View.VISIBLE : View.GONE);
        }
        if (binding.buttonEmailLogin != null) {
            binding.buttonEmailLogin.setVisibility(isGuest ? View.VISIBLE : View.GONE);
            binding.buttonEmailLogin.setOnClickListener(v -> {
                soundManager.playButtonClick();
                navigateToAuth();
            });
        }
        if (binding.buttonSignOut != null) {
            binding.buttonSignOut.setVisibility(isGuest ? View.GONE : View.VISIBLE);
            binding.buttonSignOut.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showLogoutConfirmation();
            });
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
     * Navigate to auth screen for email/password login
     */
    private void navigateToAuth() {
        try {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.action_profile_to_auth);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation to auth failed", e);
            Toast.makeText(getContext(), "Login screen coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show premium avatar selector dialog (for users who purchased)
     * Beautiful custom dialog with premium UI/UX
     */
    private void showPremiumAvatarSelector() {
        String currentAvatar = ShopFragment.getSelectedAvatar(requireContext());
        
        PremiumAvatarSelectorDialog dialog = PremiumAvatarSelectorDialog.newInstance(currentAvatar);
        dialog.setOnPremiumAvatarSelectedListener(emoji -> {
            // Update UI immediately with animation
            if (binding != null && binding.textUserAvatar != null) {
                binding.textUserAvatar.setText(emoji);
                // Celebration animation
                binding.textUserAvatar.animate()
                    .scaleX(1.3f).scaleY(1.3f).setDuration(200)
                    .withEndAction(() -> binding.textUserAvatar.animate()
                        .scaleX(1f).scaleY(1f).setDuration(200).start())
                    .start();
            }
        });
        dialog.show(getChildFragmentManager(), "premium_avatar_selector");
    }
    
    /**
     * Update the collection badge to show whether using premium or free cosmetics
     */
    private void updateCollectionBadge(boolean isUsingPremiumAvatar, boolean hasPremiumAvatars) {
        if (binding == null) return;
        
        // Try to get badge view
        try {
            android.widget.TextView badgeView = binding.getRoot().findViewById(R.id.badge_collection_type);
            if (badgeView != null) {
                if (hasPremiumAvatars) {
                    badgeView.setVisibility(android.view.View.VISIBLE);
                    if (isUsingPremiumAvatar) {
                        badgeView.setText("👑");  // Crown for premium
                        // Add subtle pulse animation
                        badgeView.animate()
                            .scaleX(1.2f).scaleY(1.2f)
                            .setDuration(500)
                            .withEndAction(() -> badgeView.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(500)
                                .start())
                            .start();
                    } else {
                        badgeView.setText("🎨");  // Palette for free with premium owned
                    }
                } else {
                    badgeView.setVisibility(android.view.View.GONE);
                }
            }
        } catch (Exception e) {
            android.util.Log.d(TAG, "Badge view not found (may not be in layout): " + e.getMessage());
        }
    }
    
    /**
     * Show premium title selector dialog (for users who purchased)
     * Beautiful custom dialog with premium UI/UX
     */
    private void showPremiumTitleSelector() {
        String currentTitle = ShopFragment.getSelectedTitle(requireContext());
        
        PremiumTitleSelectorDialog dialog = PremiumTitleSelectorDialog.newInstance(currentTitle);
        dialog.setOnPremiumTitleSelectedListener(title -> {
            // Update UI immediately
            updateAccountUI();
            
            // Show toast confirmation
            if (title != null && !title.isEmpty()) {
                Toast.makeText(getContext(), "Title equipped: " + title, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Title removed", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getChildFragmentManager(), "premium_title_selector");
    }

    /**
     * Show avatar selector dialog
     */
    private void showAvatarSelector() {
        AvatarSelectorDialog dialog = AvatarSelectorDialog.newInstance(authManager.getAvatarEmoji());
        dialog.setOnAvatarSelectedListener(emoji -> {
            // Update UI immediately with animation
            if (binding != null && binding.textUserAvatar != null) {
                binding.textUserAvatar.setText(emoji);
                // Celebration animation
                binding.textUserAvatar.animate()
                    .scaleX(1.3f).scaleY(1.3f).setDuration(200)
                    .withEndAction(() -> binding.textUserAvatar.animate()
                        .scaleX(1f).scaleY(1f).setDuration(200).start())
                    .start();
            }
            // Update full UI to refresh badge
            updateAccountUI();
        });
        dialog.show(getChildFragmentManager(), "avatar_selector");
    }
    
    /**
     * Show the beautiful collection chooser dialog for avatars
     * Allows user to choose between free and premium avatar collections
     */
    private void showAvatarCollectionChooser() {
        String currentFree = authManager.getAvatarEmoji();
        String currentPremium = ShopFragment.getSelectedAvatar(requireContext());
        boolean isPremiumEquipped = CosmeticChooserDialog.isCurrentAvatarPremium(requireContext());
        
        CosmeticChooserDialog dialog = CosmeticChooserDialog.newInstance(
            CosmeticChooserDialog.ChooserType.AVATAR,
            currentFree,
            currentPremium,
            isPremiumEquipped
        );
        
        dialog.setOnCollectionChosenListener(new CosmeticChooserDialog.OnCollectionChosenListener() {
            @Override
            public void onFreeCollectionChosen() {
                // User wants free avatars - show free selector
                showAvatarSelector();
            }
            
            @Override
            public void onPremiumCollectionChosen() {
                // User wants premium avatars - show premium selector
                showPremiumAvatarSelector();
            }
        });
        
        dialog.show(getChildFragmentManager(), "avatar_collection_chooser");
    }
    
    /**
     * Show the beautiful collection chooser dialog for titles
     * Allows user to choose to keep or remove premium titles
     */
    private void showTitleCollectionChooser() {
        String currentPremium = ShopFragment.getSelectedTitle(requireContext());
        boolean isPremiumEquipped = currentPremium != null && !currentPremium.isEmpty();
        
        CosmeticChooserDialog dialog = CosmeticChooserDialog.newInstance(
            CosmeticChooserDialog.ChooserType.TITLE,
            "",  // No free titles
            currentPremium,
            isPremiumEquipped
        );
        
        dialog.setOnCollectionChosenListener(new CosmeticChooserDialog.OnCollectionChosenListener() {
            @Override
            public void onFreeCollectionChosen() {
                // User wants no title - clear it
                ShopFragment.setSelectedTitle(requireContext(), "");
                updateAccountUI();
                Toast.makeText(getContext(), "Title removed", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPremiumCollectionChosen() {
                // User wants to pick a premium title
                showPremiumTitleSelector();
            }
        });
        
        dialog.show(getChildFragmentManager(), "title_collection_chooser");
    }
    
    /**
     * Masks email for privacy (e.g., hamdan****@gmail.com)
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email != null ? email : "";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***@" + domain;
        } else {
            String visible = localPart.substring(0, 3);
            return visible + "****@" + domain;
        }
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmation() {
        if (getContext() == null) return;
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("👋 Sign Out")
            .setMessage("Are you sure you want to sign out?\n\n" +
                    "Your progress is saved and will be here when you return!")
            .setPositiveButton("Sign Out", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                authManager.logout();
                // Navigate to auth screen
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_auth);
                } catch (Exception e) {
                    updateAccountUI();
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                soundManager.playButtonClick();
            })
            .show();
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
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.recyclerAchievements.setLayoutManager(layoutManager);
        binding.recyclerAchievements.setAdapter(achievementAdapter);
    }

    private void setupObservers() {
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            // Display progress even if null (will use defaults)
            displayUserProgress(progress);
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
            // Handle null progress with defaults
            int totalXp = progress != null ? progress.getTotalXp() : 0;
            int bugsSolvedWithoutHints = progress != null ? progress.getBugsSolvedWithoutHints() : 0;
            long lastCompletionDate = progress != null ? progress.getLastCompletionDate() : 0L;
            int currentStreakDays = progress != null ? progress.getCurrentStreakDays() : 0;
            
            int level = viewModel.calculateLevel(totalXp);

            // Level up celebration with SOUND!
            if (previousLevel > 0 && level > previousLevel) {
                celebrateLevelUp(level);
            }
            previousLevel = level;

            if (binding.textLevel != null) {
                binding.textLevel.setText(String.valueOf(level));
            }

            int xpInLevel = viewModel.getXpProgressInLevel(totalXp);
            if (binding.textXp != null) {
                binding.textXp.setText(xpInLevel + " / 100 XP");
            }

            if (binding.progressXp != null) {
                binding.progressXp.setMax(100);
                AnimationUtil.animateProgress(binding.progressXp, xpInLevel, 800);
            }

            if (binding.textPerfectFixes != null) {
                binding.textPerfectFixes.setText(String.valueOf(bugsSolvedWithoutHints));
            }

            int currentStreak = DateUtils.calculateCurrentStreak(
                lastCompletionDate,
                currentStreakDays
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
            // Always update UI on resume to reflect any demo purchases or changes
            updateAccountUI();
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
        // Clear all field references to prevent memory leaks
        achievementAdapter = null;
        viewModel = null;
        soundManager = null;
        authManager = null;
        billingManager = null;
        binding = null;
    }
}
