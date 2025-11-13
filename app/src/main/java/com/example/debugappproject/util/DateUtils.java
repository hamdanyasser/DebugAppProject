package com.example.debugappproject.util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date-related operations.
 * Handles bug of the day calculation and streak tracking.
 */
public class DateUtils {

    /**
     * Calculate Bug of the Day ID based on current date.
     * Uses a deterministic algorithm so the same bug appears each day.
     *
     * @param totalBugCount Total number of bugs in database
     * @return Bug ID for today (1-based)
     */
    public static int getBugOfTheDayId(int totalBugCount) {
        if (totalBugCount == 0) {
            return 1;
        }

        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        // Combine year and day for variety across years
        int seed = (year * 1000) + dayOfYear;

        // Return bug ID (1-indexed)
        return (seed % totalBugCount) + 1;
    }

    /**
     * Calculate streak days based on last solved timestamp.
     * Returns 0 if more than 1 day has passed without solving.
     *
     * @param lastSolvedTimestamp Last time a bug was solved
     * @param currentStreak Current streak value
     * @return Updated streak days
     */
    public static int calculateStreak(long lastSolvedTimestamp, int currentStreak) {
        if (lastSolvedTimestamp == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long daysSinceLastSolved = TimeUnit.MILLISECONDS.toDays(currentTime - lastSolvedTimestamp);

        if (daysSinceLastSolved == 0) {
            // Same day - maintain current streak
            return currentStreak;
        } else if (daysSinceLastSolved == 1) {
            // Next day - increment streak
            return currentStreak + 1;
        } else {
            // More than 1 day - reset streak
            return 0;
        }
    }

    /**
     * Check if two timestamps are on the same day.
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is from today.
     */
    public static boolean isToday(long timestamp) {
        return isSameDay(timestamp, System.currentTimeMillis());
    }

    /**
     * Calculate current streak based on last completion date.
     * Returns 0 if streak is broken (more than 1 day since last completion).
     * Returns currentStreakDays if completed today or yesterday.
     *
     * @param lastCompletionDate Last completion timestamp
     * @param currentStreakDays Stored current streak value
     * @return Actual current streak (0 if broken)
     */
    public static int calculateCurrentStreak(long lastCompletionDate, int currentStreakDays) {
        if (lastCompletionDate == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long daysSinceLastCompletion = TimeUnit.MILLISECONDS.toDays(currentTime - lastCompletionDate);

        if (daysSinceLastCompletion <= 1) {
            // Completed today or yesterday - streak is active
            return currentStreakDays;
        } else {
            // More than 1 day - streak is broken
            return 0;
        }
    }
}
