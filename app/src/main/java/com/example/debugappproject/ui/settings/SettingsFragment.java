package com.example.debugappproject.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.debugappproject.auth.AuthManager;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.databinding.FragmentSettingsBinding;
import com.example.debugappproject.sync.ProgressSyncManager;
import com.example.debugappproject.sync.SyncManagerFactory;
import com.example.debugappproject.util.NotificationHelper;
import com.example.debugappproject.util.NotificationScheduler;

/**
 * Settings Fragment - Manages app settings and preferences.
 *
 * Features:
 * - Notification preferences (daily reminders, achievements)
 * - Learning preferences (hints enabled)
 * - About section (version, privacy policy)
 * - Reset progress option
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private AuthManager authManager;
    private ProgressSyncManager syncManager;

    // Preference keys
    private static final String PREFS_NAME = "DebugMasterPrefs";
    private static final String KEY_DAILY_REMINDERS = "daily_reminders";
    private static final String KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications";
    private static final String KEY_HINTS_ENABLED = "hints_enabled";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register notification permission launcher (Android 13+)
        notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted - schedule notifications
                    NotificationScheduler.scheduleBugOfTheDayNotification(requireContext(), 9, 0);
                    Toast.makeText(requireContext(),
                        "Daily reminders enabled at 9:00 AM", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied - disable toggle
                    binding.switchDailyReminders.setChecked(false);
                    preferences.edit().putBoolean(KEY_DAILY_REMINDERS, false).apply();
                    Toast.makeText(requireContext(),
                        "Notification permission denied. Enable in app settings to receive daily reminders.",
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }

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

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        authManager = AuthManager.getInstance(requireContext());

        BugRepository repository = new BugRepository(requireActivity().getApplication());
        syncManager = SyncManagerFactory.createSyncManager(requireContext(), repository);

        // Create notification channels (required for Android O+)
        NotificationHelper.createNotificationChannels(requireContext());

        loadPreferences();
        setupListeners();
        displayAppVersion();
    }

    /**
     * Loads saved preferences and updates UI.
     */
    private void loadPreferences() {
        binding.switchDailyReminders.setChecked(
            preferences.getBoolean(KEY_DAILY_REMINDERS, true)
        );
        binding.switchAchievementNotifications.setChecked(
            preferences.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true)
        );
        binding.switchHintsEnabled.setChecked(
            preferences.getBoolean(KEY_HINTS_ENABLED, true)
        );
    }

    /**
     * Sets up listeners for switches and buttons.
     */
    private void setupListeners() {
        // Daily Reminders Switch
        binding.switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check notification permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted - schedule notifications
                        preferences.edit().putBoolean(KEY_DAILY_REMINDERS, true).apply();
                        NotificationScheduler.scheduleBugOfTheDayNotification(requireContext(), 9, 0);
                        Toast.makeText(requireContext(),
                            "Daily reminders enabled at 9:00 AM", Toast.LENGTH_SHORT).show();
                    } else {
                        // Request permission
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        preferences.edit().putBoolean(KEY_DAILY_REMINDERS, true).apply();
                    }
                } else {
                    // Android 12 and below - no permission needed
                    preferences.edit().putBoolean(KEY_DAILY_REMINDERS, true).apply();
                    NotificationScheduler.scheduleBugOfTheDayNotification(requireContext(), 9, 0);
                    Toast.makeText(requireContext(),
                        "Daily reminders enabled at 9:00 AM", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Cancel daily notification
                preferences.edit().putBoolean(KEY_DAILY_REMINDERS, false).apply();
                NotificationScheduler.cancelBugOfTheDayNotification(requireContext());
                Toast.makeText(requireContext(),
                    "Daily reminders disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Achievement Notifications Switch
        binding.switchAchievementNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit()
                .putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, isChecked)
                .apply();
        });

        // Hints Enabled Switch
        binding.switchHintsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit()
                .putBoolean(KEY_HINTS_ENABLED, isChecked)
                .apply();

            if (isChecked) {
                Toast.makeText(requireContext(),
                    "Hints enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                    "Hints disabled - challenge mode activated!", Toast.LENGTH_SHORT).show();
            }
        });

        // Privacy Policy Button
        binding.buttonPrivacyPolicy.setOnClickListener(v -> {
            // TODO: In Phase 3, link to actual privacy policy
            new AlertDialog.Builder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("DebugMaster is an offline-first learning app. All your data is stored locally on your device. We do not collect, transmit, or share any personal information.")
                .setPositiveButton("OK", null)
                .show();
        });

        // Reset Progress Button
        binding.buttonResetProgress.setOnClickListener(v -> {
            showResetConfirmationDialog();
        });

        // Sync Now Button
        binding.buttonSyncNow.setOnClickListener(v -> {
            handleSyncNow();
        });

        // Manage Account & Data Button
        binding.buttonManageAccount.setOnClickListener(v -> {
            showManageAccountDialog();
        });
    }

    /**
     * Displays the app version from PackageInfo.
     */
    private void displayAppVersion() {
        try {
            PackageInfo packageInfo = requireActivity().getPackageManager()
                .getPackageInfo(requireActivity().getPackageName(), 0);
            binding.textVersion.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            binding.textVersion.setText("Unknown");
        }
    }

    /**
     * Shows confirmation dialog for resetting all progress.
     */
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Reset All Progress")
            .setMessage("Are you sure you want to reset all your progress? This will delete:\n\n" +
                "• All completed bugs\n" +
                "• All XP and achievements\n" +
                "• All streak data\n" +
                "• All user notes\n\n" +
                "This action cannot be undone!")
            .setPositiveButton("Reset", (dialog, which) -> {
                resetAllProgress();
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    /**
     * Resets all user progress by clearing the database.
     */
    private void resetAllProgress() {
        BugRepository repository = new BugRepository(requireActivity().getApplication());

        repository.getExecutorService().execute(() -> {
            // Clear all user progress
            repository.getUserProgressDao().deleteAllProgress();

            // Clear all user achievements
            repository.getAchievementDao().deleteAllUserAchievements();

            // Reset all bugs to not completed
            repository.getBugDao().resetAllBugsToNotCompleted();

            // Clear all user notes
            repository.getBugDao().clearAllUserNotes();

            // Reseed the database with fresh data
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                        "All progress has been reset", Toast.LENGTH_LONG).show();

                    // Refresh the UI by restarting the activity
                    requireActivity().recreate();
                });
            }
        });
    }

    /**
     * Public method to check if hints are enabled.
     * Can be called from other fragments.
     */
    public static boolean areHintsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_HINTS_ENABLED, true);
    }

    /**
     * Public method to check if daily reminders are enabled.
     */
    public static boolean areDailyRemindersEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DAILY_REMINDERS, true);
    }

    /**
     * Public method to check if achievement notifications are enabled.
     */
    public static boolean areAchievementNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true);
    }

    /**
     * Handles manual sync request.
     */
    private void handleSyncNow() {
        if (!authManager.isSignedIn()) {
            Toast.makeText(requireContext(),
                "Sign in required to sync progress to the cloud",
                Toast.LENGTH_LONG).show();
            return;
        }

        if (!authManager.isFirebaseAvailable()) {
            Toast.makeText(requireContext(),
                "Firebase not configured. Add google-services.json to enable sync.",
                Toast.LENGTH_LONG).show();
            return;
        }

        // Show progress feedback
        Toast.makeText(requireContext(), "Syncing progress...", Toast.LENGTH_SHORT).show();

        syncManager.fullSync(new ProgressSyncManager.SyncCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                            "Sync complete! Your progress is backed up.",
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                            "Sync failed: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Shows dialog for managing account and cloud data.
     */
    private void showManageAccountDialog() {
        String message;
        if (authManager.isSignedIn()) {
            message = "Account: " + authManager.getUserEmail() + "\n\n" +
                "Cloud Data Deletion:\n" +
                "• Cloud-synced data will be deletable via a web dashboard (coming soon)\n" +
                "• For now, contact support to request cloud data deletion\n\n" +
                "Local Data:\n" +
                "• Use 'Reset Progress' button below to clear all local data\n" +
                "• Local reset does NOT affect cloud-synced data\n\n" +
                "Account Deletion:\n" +
                "• To delete your account and all associated data, contact support";
        } else {
            message = "You are currently in Guest mode.\n\n" +
                "Local Data:\n" +
                "• All your progress is stored locally on this device\n" +
                "• Use 'Reset Progress' button below to clear all local data\n\n" +
                "Cloud Sync:\n" +
                "• Sign in with Google to sync your progress to the cloud\n" +
                "• Your data will be backed up and accessible across devices";
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("Manage Account & Data")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
