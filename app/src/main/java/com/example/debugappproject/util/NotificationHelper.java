package com.example.debugappproject.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.debugappproject.MainActivity;
import com.example.debugappproject.R;
import com.example.debugappproject.ui.settings.SettingsFragment;

/**
 * Helper class for creating and managing notifications.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID_BUG_OF_DAY = "bug_of_day_channel";
    public static final String CHANNEL_ID_ACHIEVEMENTS = "achievements_channel";
    public static final int NOTIFICATION_ID_BUG_OF_DAY = 1001;
    public static final int NOTIFICATION_ID_ACHIEVEMENT = 1002;

    // Extra keys for deep linking
    public static final String EXTRA_NAVIGATE_TO = "navigate_to";
    public static final String DESTINATION_BUG_OF_DAY = "bug_of_day";

    /**
     * Creates notification channels (required for Android O+).
     * Should be called once at app startup.
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // Bug of the Day channel
            NotificationChannel bugOfDayChannel = new NotificationChannel(
                CHANNEL_ID_BUG_OF_DAY,
                "Bug of the Day",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            bugOfDayChannel.setDescription("Daily reminders for Bug of the Day");
            manager.createNotificationChannel(bugOfDayChannel);

            // Achievements channel
            NotificationChannel achievementsChannel = new NotificationChannel(
                CHANNEL_ID_ACHIEVEMENTS,
                "Achievements",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            achievementsChannel.setDescription("Notifications when you unlock achievements");
            manager.createNotificationChannel(achievementsChannel);
        }
    }

    /**
     * Shows the Bug of the Day notification.
     */
    public static void showBugOfTheDayNotification(Context context) {
        // Create intent that deep links to Bug of the Day screen
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_NAVIGATE_TO, DESTINATION_BUG_OF_DAY);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_BUG_OF_DAY)
            .setSmallIcon(R.drawable.ic_notification_bug) // We'll create this
            .setContentTitle("Bug of the Day is Ready!")
            .setContentText("Your daily debugging challenge awaits – don't break your streak!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID_BUG_OF_DAY, builder.build());
    }

    /**
     * Shows an achievement unlock notification.
     * Respects the "Achievement Notifications" toggle from Settings.
     */
    public static void showAchievementNotification(Context context, String achievementName, String achievementDescription) {
        // Check if achievement notifications are enabled in settings
        if (!SettingsFragment.areAchievementNotificationsEnabled(context)) {
            return; // User has disabled achievement notifications
        }

        // Create intent to open main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_notification_trophy)
            .setContentTitle("Achievement Unlocked!")
            .setContentText(achievementName + ": " + achievementDescription)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID_ACHIEVEMENT, builder.build());
    }
}
