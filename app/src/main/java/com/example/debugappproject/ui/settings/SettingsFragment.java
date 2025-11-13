package com.example.debugappproject.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.databinding.FragmentSettingsBinding;

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

    // Preference keys
    private static final String PREFS_NAME = "DebugMasterPrefs";
    private static final String KEY_DAILY_REMINDERS = "daily_reminders";
    private static final String KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications";
    private static final String KEY_HINTS_ENABLED = "hints_enabled";

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
            preferences.edit()
                .putBoolean(KEY_DAILY_REMINDERS, isChecked)
                .apply();

            if (isChecked) {
                Toast.makeText(requireContext(),
                    "Daily reminders enabled", Toast.LENGTH_SHORT).show();
                // TODO: Schedule daily notification in Phase 3
            } else {
                Toast.makeText(requireContext(),
                    "Daily reminders disabled", Toast.LENGTH_SHORT).show();
                // TODO: Cancel daily notification in Phase 3
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
