package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.debugappproject.model.UserProgress;

/**
 * Data Access Object for UserProgress entity.
 * Provides methods to interact with the user_progress table.
 * This is a singleton table (only one row with id=1).
 */
@Dao
public interface UserProgressDao {

    /**
     * Insert initial user progress record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProgress userProgress);

    /**
     * Update user progress.
     */
    @Update
    void update(UserProgress userProgress);

    /**
     * Get user progress as LiveData.
     */
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    LiveData<UserProgress> getUserProgress();

    /**
     * Get user progress synchronously.
     */
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    UserProgress getUserProgressSync();

    /**
     * Increment total solved count.
     */
    @Query("UPDATE user_progress SET totalSolved = totalSolved + 1 WHERE id = 1")
    void incrementTotalSolved();

    /**
     * Increment easy solved count.
     */
    @Query("UPDATE user_progress SET easySolved = easySolved + 1 WHERE id = 1")
    void incrementEasySolved();

    /**
     * Increment medium solved count.
     */
    @Query("UPDATE user_progress SET mediumSolved = mediumSolved + 1 WHERE id = 1")
    void incrementMediumSolved();

    /**
     * Increment hard solved count.
     */
    @Query("UPDATE user_progress SET hardSolved = hardSolved + 1 WHERE id = 1")
    void incrementHardSolved();

    /**
     * Update last solved timestamp.
     */
    @Query("UPDATE user_progress SET lastSolvedTimestamp = :timestamp WHERE id = 1")
    void updateLastSolvedTimestamp(long timestamp);

    /**
     * Update last opened timestamp.
     */
    @Query("UPDATE user_progress SET lastOpenedTimestamp = :timestamp WHERE id = 1")
    void updateLastOpenedTimestamp(long timestamp);

    /**
     * Update streak days.
     */
    @Query("UPDATE user_progress SET streakDays = :streak WHERE id = 1")
    void updateStreak(int streak);

    /**
     * Update longest streak if current streak is higher.
     */
    @Query("UPDATE user_progress SET longestStreakDays = :longestStreak WHERE id = 1 AND :longestStreak > longestStreakDays")
    void updateLongestStreak(int longestStreak);

    /**
     * Add XP to user's total.
     */
    @Query("UPDATE user_progress SET xp = xp + :xpAmount WHERE id = 1")
    void addXp(int xpAmount);

    /**
     * Increment hints used count.
     */
    @Query("UPDATE user_progress SET hintsUsed = hintsUsed + 1 WHERE id = 1")
    void incrementHintsUsed();

    /**
     * Increment bugs solved without hints count.
     */
    @Query("UPDATE user_progress SET bugsSolvedWithoutHints = bugsSolvedWithoutHints + 1 WHERE id = 1")
    void incrementBugsSolvedWithoutHints();

    /**
     * Reset all progress (for reset feature).
     */
    @Query("UPDATE user_progress SET totalSolved = 0, easySolved = 0, mediumSolved = 0, " +
            "hardSolved = 0, streakDays = 0, longestStreakDays = 0, lastSolvedTimestamp = 0, xp = 0, hintsUsed = 0, " +
            "bugsSolvedWithoutHints = 0 WHERE id = 1")
    void resetProgress();

    /**
     * Alias for resetProgress() for consistency.
     */
    @Query("UPDATE user_progress SET totalSolved = 0, easySolved = 0, mediumSolved = 0, " +
            "hardSolved = 0, streakDays = 0, longestStreakDays = 0, lastSolvedTimestamp = 0, xp = 0, hintsUsed = 0, " +
            "bugsSolvedWithoutHints = 0 WHERE id = 1")
    void deleteAllProgress();
}
