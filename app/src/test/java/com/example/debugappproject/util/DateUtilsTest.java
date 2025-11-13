package com.example.debugappproject.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for DateUtils streak calculation logic.
 */
public class DateUtilsTest {

    private static final long ONE_DAY_MS = TimeUnit.DAYS.toMillis(1);
    private static final long ONE_HOUR_MS = TimeUnit.HOURS.toMillis(1);

    @Test
    public void calculateStreak_withZeroTimestamp_returnsZero() {
        int streak = DateUtils.calculateStreak(0, 5);
        assertEquals(0, streak);
    }

    @Test
    public void calculateStreak_sameDayCompletion_maintainsStreak() {
        long now = System.currentTimeMillis();
        long oneHourAgo = now - ONE_HOUR_MS;

        int streak = DateUtils.calculateStreak(oneHourAgo, 5);
        assertEquals(5, streak);
    }

    @Test
    public void calculateStreak_nextDayCompletion_incrementsStreak() {
        long now = System.currentTimeMillis();
        long oneDayAgo = now - ONE_DAY_MS;

        int streak = DateUtils.calculateStreak(oneDayAgo, 5);
        assertEquals(6, streak);
    }

    @Test
    public void calculateStreak_twoDaysLater_resetsStreak() {
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - (2 * ONE_DAY_MS);

        int streak = DateUtils.calculateStreak(twoDaysAgo, 5);
        assertEquals(0, streak);
    }

    @Test
    public void calculateStreak_threeDaysLater_resetsStreak() {
        long now = System.currentTimeMillis();
        long threeDaysAgo = now - (3 * ONE_DAY_MS);

        int streak = DateUtils.calculateStreak(threeDaysAgo, 10);
        assertEquals(0, streak);
    }

    @Test
    public void calculateStreak_firstCompletion_incrementsFromZero() {
        long now = System.currentTimeMillis();
        long oneDayAgo = now - ONE_DAY_MS;

        int streak = DateUtils.calculateStreak(oneDayAgo, 0);
        assertEquals(1, streak);
    }

    @Test
    public void calculateCurrentStreak_withZeroTimestamp_returnsZero() {
        int streak = DateUtils.calculateCurrentStreak(0, 5);
        assertEquals(0, streak);
    }

    @Test
    public void calculateCurrentStreak_completedToday_returnsCurrentStreak() {
        long now = System.currentTimeMillis();
        long twoHoursAgo = now - (2 * ONE_HOUR_MS);

        int streak = DateUtils.calculateCurrentStreak(twoHoursAgo, 7);
        assertEquals(7, streak);
    }

    @Test
    public void calculateCurrentStreak_completedYesterday_returnsCurrentStreak() {
        long now = System.currentTimeMillis();
        long oneDayAgo = now - ONE_DAY_MS;

        int streak = DateUtils.calculateCurrentStreak(oneDayAgo, 5);
        assertEquals(5, streak);
    }

    @Test
    public void calculateCurrentStreak_completedTwoDaysAgo_returnsZero() {
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - (2 * ONE_DAY_MS);

        int streak = DateUtils.calculateCurrentStreak(twoDaysAgo, 10);
        assertEquals(0, streak);
    }

    @Test
    public void calculateCurrentStreak_completedThreeDaysAgo_returnsZero() {
        long now = System.currentTimeMillis();
        long threeDaysAgo = now - (3 * ONE_DAY_MS);

        int streak = DateUtils.calculateCurrentStreak(threeDaysAgo, 15);
        assertEquals(0, streak);
    }

    @Test
    public void calculateCurrentStreak_streakBroken_returnsZero() {
        long now = System.currentTimeMillis();
        long weekAgo = now - (7 * ONE_DAY_MS);

        int streak = DateUtils.calculateCurrentStreak(weekAgo, 20);
        assertEquals(0, streak);
    }

    @Test
    public void isSameDay_sameTimestamp_returnsTrue() {
        long now = System.currentTimeMillis();
        assertTrue(DateUtils.isSameDay(now, now));
    }

    @Test
    public void isSameDay_sameDay_returnsTrue() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        long morning = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 21);
        long evening = cal.getTimeInMillis();

        assertTrue(DateUtils.isSameDay(morning, evening));
    }

    @Test
    public void isSameDay_differentDays_returnsFalse() {
        long now = System.currentTimeMillis();
        long yesterday = now - ONE_DAY_MS;
        assertFalse(DateUtils.isSameDay(now, yesterday));
    }

    @Test
    public void isToday_currentTime_returnsTrue() {
        long now = System.currentTimeMillis();
        assertTrue(DateUtils.isToday(now));
    }

    @Test
    public void isToday_oneHourAgo_returnsTrue() {
        long oneHourAgo = System.currentTimeMillis() - ONE_HOUR_MS;
        assertTrue(DateUtils.isToday(oneHourAgo));
    }

    @Test
    public void isToday_yesterday_returnsFalse() {
        long yesterday = System.currentTimeMillis() - ONE_DAY_MS;
        assertFalse(DateUtils.isToday(yesterday));
    }

    @Test
    public void getBugOfTheDayId_withZeroBugs_returns1() {
        int bugId = DateUtils.getBugOfTheDayId(0);
        assertEquals(1, bugId);
    }

    @Test
    public void getBugOfTheDayId_withOneBug_returns1() {
        int bugId = DateUtils.getBugOfTheDayId(1);
        assertEquals(1, bugId);
    }

    @Test
    public void getBugOfTheDayId_with10Bugs_returnsBetween1And10() {
        int bugId = DateUtils.getBugOfTheDayId(10);
        assertTrue(bugId >= 1 && bugId <= 10);
    }

    @Test
    public void getBugOfTheDayId_with50Bugs_returnsBetween1And50() {
        int bugId = DateUtils.getBugOfTheDayId(50);
        assertTrue(bugId >= 1 && bugId <= 50);
    }

    @Test
    public void getBugOfTheDayId_sameDayCallsReturnSameId() {
        // Calling twice on the same day should return the same bug ID
        int bugId1 = DateUtils.getBugOfTheDayId(20);
        int bugId2 = DateUtils.getBugOfTheDayId(20);
        assertEquals(bugId1, bugId2);
    }
}
