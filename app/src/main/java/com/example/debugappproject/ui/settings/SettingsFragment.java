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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentSettingsBinding;

/**
 * Settings Fragment - App configuration and preferences
 * 
 * Sections:
 * - Appearance: Dark mode, sounds, haptic feedback
 * - Gameplay: Hints, timer, auto-submit
 * - Notifications: Daily reminders, streak alerts
 * - Subscription: Pro status management
 * - Data: Reset progress
 * - About: Version, privacy, terms
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "debugmaster_settings";
    
    // Appearance settings
    private static final String KEY_DARK_MODE = "dark_mode";
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
        billingManager = new BillingManager(requireContext());

        loadSettings();
        setupListeners();
        observeProStatus();
    }

    private void loadSettings() {
        // Load appearance preferences
        binding.switchDarkMode.setChecked(prefs.getBoolean(KEY_DARK_MODE, false));
        binding.switchSounds.setChecked(prefs.getBoolean(KEY_SOUNDS, true));
        binding.switchHaptic.setChecked(prefs.getBoolean(KEY_HAPTIC, true));
        
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

    private void setupListeners() {
        // Back button
        binding.buttonBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // ==================== APPEARANCE ====================
        
        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Sound effects toggle
        binding.switchSounds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SOUNDS, isChecked).apply();
            if (isChecked) {
                Toast.makeText(getContext(), "🔊 Sounds enabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Haptic feedback toggle
        binding.switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_HAPTIC, isChecked).apply();
        });

        // ==================== GAMEPLAY ====================
        
        // Hints toggle
        if (binding.switchHints != null) {
            binding.switchHints.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_HINTS, isChecked).apply();
                if (!isChecked) {
                    Toast.makeText(getContext(), "🎯 Challenge mode: Hints disabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "💡 Hints enabled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Timer toggle
        if (binding.switchTimer != null) {
            binding.switchTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_TIMER, isChecked).apply();
            });
        }
        
        // Auto-submit toggle
        if (binding.switchAutoSubmit != null) {
            binding.switchAutoSubmit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_AUTO_SUBMIT, isChecked).apply();
            });
        }

        // ==================== NOTIFICATIONS ====================

        // Daily reminders toggle
        binding.switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_REMINDERS, isChecked).apply();
            if (isChecked) {
                Toast.makeText(getContext(), "🔔 You'll get daily reminders", Toast.LENGTH_SHORT).show();
            }
        });

        // Streak alerts toggle
        binding.switchStreakAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_STREAK_ALERTS, isChecked).apply();
        });

        // ==================== SUBSCRIPTION ====================

        // Set initial state based on current Pro status
        updateSubscriptionUI(billingManager.isProUserSync());

        // ==================== DATA ====================

        // Reset progress
        binding.layoutResetProgress.setOnClickListener(v -> showResetDialog());

        // ==================== ABOUT ====================

        // Privacy policy
        binding.layoutPrivacy.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening Privacy Policy...", Toast.LENGTH_SHORT).show();
            // In production, open a WebView or browser with privacy policy
        });

        // Terms of service
        binding.layoutTerms.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening Terms of Service...", Toast.LENGTH_SHORT).show();
            // In production, open a WebView or browser with terms
        });
    }

    private void observeProStatus() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (binding == null) return;
            updateSubscriptionUI(isPro);
        });
    }

    private void showQuickDemoActivateDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("🧪 Quick Pro Activation")
                .setMessage("Instantly activate Pro to test premium features:\n\n" +
                        "✓ All 100+ debugging challenges\n" +
                        "✓ All 6 learning paths\n" +
                        "✓ Battle Arena multiplayer\n" +
                        "✓ Unlimited practice mode\n" +
                        "✓ Ad-free experience\n\n" +
                        "This is Demo Mode - no real payment.")
                .setPositiveButton("🚀 Activate Pro", (dialog, which) -> {
                    // Use BillingManager to activate demo
                    billingManager.demoPurchase(BillingManager.PRODUCT_YEARLY);
                    
                    // Manually update UI immediately
                    updateSubscriptionUI(true);
                    
                    Toast.makeText(getContext(), "🎉 Pro activated!", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("See Plans", (dialog, which) -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.proSubscriptionFragment);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updateSubscriptionUI(boolean isPro) {
        if (binding == null) return;
        
        boolean isDemoMode = BillingManager.isDemoMode();
        
        if (isPro) {
            binding.textSubscriptionStatus.setText("👑 Pro Member");
            binding.textSubscriptionDesc.setText(isDemoMode ? 
                "Demo Mode - All features unlocked!" : "All features unlocked!");
            binding.buttonUpgrade.setText("Manage");
            binding.buttonUpgrade.setOnClickListener(v -> showProMemberInfo());
            binding.cardSubscription.setOnClickListener(v -> showProMemberInfo());
        } else {
            binding.textSubscriptionStatus.setText("Free Plan");
            binding.textSubscriptionDesc.setText(isDemoMode ? 
                "Demo Mode - Tap to test Pro features" : "Upgrade to unlock all features");
            binding.buttonUpgrade.setText(isDemoMode ? "🧪 Try Pro" : "Upgrade");
            
            if (isDemoMode) {
                binding.buttonUpgrade.setOnClickListener(v -> showQuickDemoActivateDialog());
                binding.cardSubscription.setOnClickListener(v -> showQuickDemoActivateDialog());
            } else {
                binding.buttonUpgrade.setOnClickListener(v -> {
                    try {
                        Navigation.findNavController(v).navigate(R.id.proSubscriptionFragment);
                    } catch (Exception e) {
                        showDemoUpgradeDialog();
                    }
                });
                binding.cardSubscription.setOnClickListener(v -> {
                    try {
                        Navigation.findNavController(v).navigate(R.id.proSubscriptionFragment);
                    } catch (Exception e) {
                        showDemoUpgradeDialog();
                    }
                });
            }
        }
    }

    private void showDemoUpgradeDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("🚀 Demo Mode")
                .setMessage("This is a demo purchase. In production, this would connect to Google Play Billing.\n\n" +
                        "Would you like to simulate a Pro upgrade?")
                .setPositiveButton("Activate Pro", (dialog, which) -> {
                    // Use BillingManager to activate demo
                    billingManager.demoPurchase(BillingManager.PRODUCT_YEARLY);
                    
                    // Manually update UI immediately
                    updateSubscriptionUI(true);
                    
                    Toast.makeText(getContext(), "🎉 Pro activated! (Demo)", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProMemberInfo() {
        boolean isDemoMode = BillingManager.isDemoMode();
        
        new AlertDialog.Builder(requireContext())
                .setTitle("👑 Pro Member")
                .setMessage("You have full access to all features!\n\n" +
                        "• 100+ debugging challenges\n" +
                        "• All 6 learning paths\n" +
                        "• Battle Arena multiplayer\n" +
                        "• Ad-free experience\n\n" +
                        (isDemoMode ? "🧪 Demo Mode Active" : "Thank you for your support!"))
                .setPositiveButton("Awesome!", null)
                .setNeutralButton(isDemoMode ? "Deactivate Pro" : "Manage in Play Store", (dialog, which) -> {
                    if (isDemoMode) {
                        // Deactivate demo pro
                        billingManager.demoDeactivate();
                        
                        // Manually update UI immediately
                        updateSubscriptionUI(false);
                        
                        Toast.makeText(getContext(), "Pro deactivated. Back to free plan.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), 
                            "Open Google Play Store > Subscriptions to manage", 
                            Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void showResetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Reset Progress")
                .setMessage("This will delete all your progress, XP, achievements, and stats.\n\n" +
                        "This action cannot be undone!")
                .setPositiveButton("Reset", (dialog, which) -> {
                    // TODO: Implement actual reset logic with database
                    Toast.makeText(getContext(), "Progress reset!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    // ==================== STATIC HELPER METHODS ====================
    
    /**
     * Check if hints are enabled in settings.
     * Can be called from other fragments.
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
     * Check if achievement notifications are enabled in settings.
     * Uses streak alerts setting as achievement notifications.
     */
    public static boolean areAchievementNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        // Use streak alerts as achievement notifications setting
        return prefs.getBoolean(KEY_STREAK_ALERTS, true);
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
