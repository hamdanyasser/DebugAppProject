package com.example.debugappproject.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentSettingsBinding;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.ProManager;
import com.example.debugappproject.util.SoundManager;
import com.example.debugappproject.util.ThemeManager;

/**
 * Settings & Preferences Screen
 * 
 * Sections:
 * - Appearance: Theme (Light/Dark/System), sounds, haptic feedback
 * - Gameplay: Hints, timer, auto-submit
 * - Notifications: Daily reminders, streak alerts
 * - Subscription: Pro status management
 * - Data: Reset progress
 * - About: Version, privacy, terms
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "debugmaster_settings";
    
    // Appearance settings
    private static final String KEY_SOUNDS = "sounds_enabled";
    private static final String KEY_HAPTIC = "haptic_enabled";
    
    // Gameplay settings
    private static final String KEY_HINTS = "hints_enabled";
    private static final String KEY_TIMER = "timer_enabled";
    private static final String KEY_AUTO_SUBMIT = "auto_submit_enabled";
    
    // Notification settings
    private static final String KEY_REMINDERS = "reminders_enabled";
    private static final String KEY_STREAK_ALERTS = "streak_alerts_enabled";

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;
    private BillingManager billingManager;
    private SoundManager soundManager;
    private ThemeManager themeManager;
    private ProManager proManager;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        themeManager = ThemeManager.getInstance(requireContext());
        proManager = ProManager.getInstance(requireContext());
        authManager = AuthManager.getInstance(requireContext());

        setupAccountSection();
        loadSettings();
        setupListeners();
        observeProStatus();
    }

    private void loadSettings() {
        // Load theme preference
        boolean isDarkMode = themeManager.getThemeMode() == ThemeManager.THEME_DARK ||
                (themeManager.getThemeMode() == ThemeManager.THEME_SYSTEM && themeManager.isDarkMode());
        binding.switchDarkMode.setChecked(isDarkMode);
        
        // Load appearance preferences
        binding.switchSounds.setChecked(soundManager.isSoundEnabled());
        binding.switchHaptic.setChecked(soundManager.isHapticEnabled());
        
        // Load gameplay preferences
        if (binding.switchHints != null) {
            binding.switchHints.setChecked(prefs.getBoolean(KEY_HINTS, true));
        }
        if (binding.switchTimer != null) {
            binding.switchTimer.setChecked(prefs.getBoolean(KEY_TIMER, true));
        }
        if (binding.switchAutoSubmit != null) {
            binding.switchAutoSubmit.setChecked(prefs.getBoolean(KEY_AUTO_SUBMIT, true));
        }
        
        // Load notification preferences
        binding.switchReminders.setChecked(prefs.getBoolean(KEY_REMINDERS, true));
        binding.switchStreakAlerts.setChecked(prefs.getBoolean(KEY_STREAK_ALERTS, true));
    }

    private void setupAccountSection() {
        // Always show account section
        binding.sectionAccount.setVisibility(View.VISIBLE);
        
        boolean isGuest = authManager.isGuest();
        
        if (isGuest) {
            // Show sign-in option for guests
            binding.layoutSignIn.setVisibility(View.VISIBLE);
            binding.layoutLogout.setVisibility(View.GONE);
            binding.textAccountTitle.setText("Sign In");
            binding.textAccountDesc.setText("Create account or login to save progress");
            binding.textAccountIcon.setText("👤");
            
            binding.buttonSignIn.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                navigateToAuth();
            });
            
            binding.layoutSignIn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                navigateToAuth();
            });
        } else {
            // Show logged-in user info with logout option
            binding.layoutSignIn.setVisibility(View.GONE);
            binding.layoutLogout.setVisibility(View.VISIBLE);
            
            String email = authManager.getEmail();
            String displayName = authManager.getDisplayName();
            binding.textLoggedInEmail.setText(email.isEmpty() ? displayName : email);
            
            binding.layoutLogout.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.WARNING);
                showLogoutConfirmation();
            });
        }
    }
    
    private void navigateToAuth() {
        try {
            // First logout to clear guest session
            authManager.logout();
            // Navigate to auth screen
            Navigation.findNavController(requireView()).navigate(R.id.authFragment);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Navigate to Sign In from profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                    authManager.logout();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to auth screen
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.authFragment);
                    } catch (Exception e) {
                        // Just refresh the settings UI
                        setupAccountSection();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                })
                .show();
    }

    private void setupListeners() {
        // Back button with sound
        binding.buttonBack.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            Navigation.findNavController(v).navigateUp();
        });

        // ==================== APPEARANCE ====================
        
        // Dark mode toggle - now uses ThemeManager
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.playButtonClick();
            
            if (isChecked) {
                themeManager.setThemeMode(ThemeManager.THEME_DARK);
            } else {
                themeManager.setThemeMode(ThemeManager.THEME_LIGHT);
            }
        });
        
        // Dark mode switch can also be long-pressed to show options
        binding.switchDarkMode.setOnLongClickListener(v -> {
            showThemeOptionsDialog();
            return true;
        });

        // Sound effects toggle - controls SoundManager
        binding.switchSounds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Play sound BEFORE disabling if turning off
            if (!isChecked) {
                soundManager.playButtonClick();
            }
            
            soundManager.setSoundEnabled(isChecked);
            prefs.edit().putBoolean(KEY_SOUNDS, isChecked).apply();
            
            if (isChecked) {
                // Play sound AFTER enabling
                soundManager.playSound(SoundManager.Sound.SUCCESS);
                Toast.makeText(getContext(), "Sound effects enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sound effects disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Haptic feedback toggle - controls SoundManager
        binding.switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.playButtonClick();
            soundManager.setHapticEnabled(isChecked);
            prefs.edit().putBoolean(KEY_HAPTIC, isChecked).apply();
            
            if (isChecked) {
                // Give immediate haptic feedback to show it's working
                soundManager.vibrate(SoundManager.Haptic.SUCCESS);
                Toast.makeText(getContext(), "Haptic feedback enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Haptic feedback disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // ==================== GAMEPLAY ====================
        
        // Hints toggle
        if (binding.switchHints != null) {
            binding.switchHints.setOnCheckedChangeListener((buttonView, isChecked) -> {
                soundManager.playButtonClick();
                prefs.edit().putBoolean(KEY_HINTS, isChecked).apply();
                if (!isChecked) {
                    soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                    Toast.makeText(getContext(), "Challenge mode: Hints disabled!", Toast.LENGTH_SHORT).show();
                } else {
                    soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
                    Toast.makeText(getContext(), "Hints enabled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Timer toggle
        if (binding.switchTimer != null) {
            binding.switchTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
                soundManager.playButtonClick();
                prefs.edit().putBoolean(KEY_TIMER, isChecked).apply();
            });
        }
        
        // Auto-submit toggle
        if (binding.switchAutoSubmit != null) {
            binding.switchAutoSubmit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                soundManager.playButtonClick();
                prefs.edit().putBoolean(KEY_AUTO_SUBMIT, isChecked).apply();
            });
        }

        // ==================== NOTIFICATIONS ====================

        // Daily reminders toggle
        binding.switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.playButtonClick();
            prefs.edit().putBoolean(KEY_REMINDERS, isChecked).apply();
            if (isChecked) {
                soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                Toast.makeText(getContext(), "You'll get daily reminders", Toast.LENGTH_SHORT).show();
            }
        });

        // Streak alerts toggle
        binding.switchStreakAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.playButtonClick();
            prefs.edit().putBoolean(KEY_STREAK_ALERTS, isChecked).apply();
        });

        // ==================== SUBSCRIPTION ====================

        // Set initial state based on current Pro status
        updateSubscriptionUI(proManager.isPro());

        // ==================== DATA ====================

        // Reset progress
        binding.layoutResetProgress.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.WARNING);
            showResetDialog();
        });

        // ==================== ABOUT ====================

        // Privacy policy
        binding.layoutPrivacy.setOnClickListener(v -> {
            soundManager.playButtonClick();
            Toast.makeText(getContext(), "Opening Privacy Policy...", Toast.LENGTH_SHORT).show();
        });

        // Terms of service
        binding.layoutTerms.setOnClickListener(v -> {
            soundManager.playButtonClick();
            Toast.makeText(getContext(), "Opening Terms of Service...", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void showThemeOptionsDialog() {
        soundManager.playButtonClick();
        
        String[] options = ThemeManager.getThemeOptions();
        int currentMode = themeManager.getThemeMode();
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, currentMode, (dialog, which) -> {
                    soundManager.playButtonClick();
                    themeManager.setThemeMode(which);
                    
                    // Update switch to reflect new state
                    boolean isDark = (which == ThemeManager.THEME_DARK) ||
                            (which == ThemeManager.THEME_SYSTEM && themeManager.isDarkMode());
                    binding.switchDarkMode.setChecked(isDark);
                    
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                })
                .show();
    }

    private void observeProStatus() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (binding == null) return;
            updateSubscriptionUI(isPro);
        });
    }

    private void showQuickDemoActivateDialog() {
        soundManager.playSound(SoundManager.Sound.POWER_UP);
        new AlertDialog.Builder(requireContext())
                .setTitle("Quick Pro Activation")
                .setMessage("Instantly activate Pro to test premium features:\n\n" +
                        "- All 90+ debugging challenges\n" +
                        "- All 15 learning paths\n" +
                        "- Unlimited Battle Arena\n" +
                        "- Algorithm Arena\n" +
                        "- Code Review Mode\n" +
                        "- Ad-free experience\n" +
                        "- 2x XP weekends\n\n" +
                        "This is Demo Mode - no real payment.")
                .setPositiveButton("Activate Pro", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
                    proManager.enableDemoMode();
                    billingManager.demoPurchase(BillingManager.PRODUCT_YEARLY);
                    updateSubscriptionUI(true);
                    Toast.makeText(getContext(), "Pro activated!", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("See Plans", (dialog, which) -> {
                    soundManager.playButtonClick();
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.proSubscriptionFragment);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                })
                .show();
    }
    
    private void updateSubscriptionUI(boolean isPro) {
        if (binding == null) return;
        
        boolean isDemoMode = proManager.isDemoMode();
        
        if (isPro) {
            binding.textSubscriptionStatus.setText("Pro Member");
            binding.textSubscriptionDesc.setText(isDemoMode ? 
                "Demo Mode - All features unlocked!" : "All features unlocked!");
            binding.buttonUpgrade.setText("Manage");
            binding.buttonUpgrade.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showProMemberInfo();
            });
            binding.cardSubscription.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showProMemberInfo();
            });
            
            // Show 2x XP indicator if active
            if (proManager.isDoubleXPActive()) {
                binding.textSubscriptionDesc.setText("2x XP Weekend Active!");
            }
        } else {
            binding.textSubscriptionStatus.setText("Free Plan");
            binding.textSubscriptionDesc.setText("Upgrade to unlock all features");
            binding.buttonUpgrade.setText("Upgrade");
            
            binding.buttonUpgrade.setOnClickListener(v -> showQuickDemoActivateDialog());
            binding.cardSubscription.setOnClickListener(v -> showQuickDemoActivateDialog());
        }
    }

    private void showProMemberInfo() {
        boolean isDemoMode = proManager.isDemoMode();
        soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
        
        StringBuilder features = new StringBuilder();
        features.append("You have full access to all features!\n\n");
        
        String[] proFeatures = ProManager.getProFeaturesList();
        for (int i = 0; i < Math.min(10, proFeatures.length); i++) {
            features.append(proFeatures[i]).append("\n");
        }
        features.append("...and more!");
        
        if (isDemoMode) {
            long remaining = proManager.getDemoTimeRemaining();
            long hours = remaining / (60 * 60 * 1000);
            features.append("\n\nDemo Mode: ").append(hours).append("h remaining");
        }
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Pro Member")
                .setMessage(features.toString())
                .setPositiveButton("Awesome!", (dialog, which) -> {
                    soundManager.playButtonClick();
                })
                .setNeutralButton(isDemoMode ? "Deactivate" : "Manage", (dialog, which) -> {
                    if (isDemoMode) {
                        soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                        proManager.disableDemoMode();
                        billingManager.demoDeactivate();
                        updateSubscriptionUI(false);
                        Toast.makeText(getContext(), "Pro deactivated. Back to free plan.", Toast.LENGTH_SHORT).show();
                    } else {
                        soundManager.playButtonClick();
                        Toast.makeText(getContext(), 
                            "Open Google Play Store > Subscriptions to manage", 
                            Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void showResetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Progress")
                .setMessage("This will delete all your progress, XP, achievements, and stats.\n\n" +
                        "This action cannot be undone!")
                .setPositiveButton("Reset", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.DEFEAT);
                    // TODO: Implement actual reset logic with database
                    Toast.makeText(getContext(), "Progress reset!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                })
                .show();
    }
    
    // ==================== STATIC HELPER METHODS ====================
    
    /**
     * Check if hints are enabled in settings.
     */
    public static boolean areHintsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_HINTS, true);
    }
    
    /**
     * Check if timer is enabled in settings.
     */
    public static boolean isTimerEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_TIMER, true);
    }
    
    /**
     * Check if auto-submit is enabled in settings.
     */
    public static boolean isAutoSubmitEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_AUTO_SUBMIT, true);
    }
    
    /**
     * Check if sounds are enabled in settings.
     */
    public static boolean areSoundsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_SOUNDS, true);
    }
    
    /**
     * Check if haptic feedback is enabled in settings.
     */
    public static boolean isHapticEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_HAPTIC, true);
    }
    
    /**
     * Check if daily reminders are enabled in settings.
     */
    public static boolean areDailyRemindersEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_REMINDERS, true);
    }
    
    /**
     * Check if streak alerts are enabled in settings.
     */
    public static boolean areStreakAlertsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_STREAK_ALERTS, true);
    }
    
    /**
     * Check if achievement notifications are enabled.
     */
    public static boolean areAchievementNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(KEY_STREAK_ALERTS, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.clearCallback();
        }
        binding = null;
    }
}
