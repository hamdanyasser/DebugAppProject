package com.example.debugappproject.util;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.debugappproject.workers.BugOfTheDayNotificationWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for scheduling and cancelling notifications using WorkManager.
 */
public class NotificationScheduler {

    private static final String WORK_TAG_BUG_OF_DAY = "bug_of_day_notification";

    /**
     * Schedules the Bug of the Day notification at the specified hour and minute.
     *
     * @param context Application context
     * @param hour    Hour in 24-hour format (0-23)
     * @param minute  Minute (0-59)
     */
    public static void scheduleBugOfTheDayNotification(Context context, int hour, int minute) {
        // Calculate initial delay until the specified time
        long initialDelay = calculateInitialDelay(hour, minute);

        // Create periodic work request (runs once per day)
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            BugOfTheDayNotificationWorker.class,
            24, TimeUnit.HOURS  // Repeat every 24 hours
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG_BUG_OF_DAY)
            .build();

        // Schedule the work (replace existing if any)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG_BUG_OF_DAY,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        );
    }

    /**
     * Cancels the Bug of the Day notification.
     */
    public static void cancelBugOfTheDayNotification(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_BUG_OF_DAY);
    }

    /**
     * Calculates the initial delay in milliseconds until the specified time today.
     * If the time has already passed today, schedules for tomorrow.
     *
     * @param hour   Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     * @return Delay in milliseconds
     */
    private static long calculateInitialDelay(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // If target time has passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}
