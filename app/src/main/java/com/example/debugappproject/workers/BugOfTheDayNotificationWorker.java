package com.example.debugappproject.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.debugappproject.ui.settings.SettingsFragment;
import com.example.debugappproject.util.NotificationHelper;

/**
 * WorkManager Worker for Bug of the Day notifications.
 * Runs daily at the scheduled time to remind users about their daily bug.
 */
public class BugOfTheDayNotificationWorker extends Worker {

    public BugOfTheDayNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Check if notifications are enabled in settings
        if (SettingsFragment.areDailyRemindersEnabled(getApplicationContext())) {
            // Show the Bug of the Day notification
            NotificationHelper.showBugOfTheDayNotification(getApplicationContext());
            return Result.success();
        }

        // If notifications are disabled, still return success (job completed successfully)
        return Result.success();
    }
}
