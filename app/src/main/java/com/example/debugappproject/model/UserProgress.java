package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * UserProgress entity tracking the user's overall progress and statistics.
 * Singleton entity (only one row with id=1).
 */
@Entity(tableName = "user_progress")
public class UserProgress {

    @PrimaryKey
    private int id = 1; // Singleton - only one progress record

    private int totalSolved;            // Total number of bugs solved
    private int streakDays;             // Current streak in days
    private int easySolved;             // Number of easy bugs solved
    private int mediumSolved;           // Number of medium bugs solved
    private int hardSolved;             // Number of hard bugs solved
    private long lastSolvedTimestamp;   // Timestamp of last solved bug (for streak calculation)
    private long lastOpenedTimestamp;   // Timestamp of last app open (for streak calculation)
    private int xp;                     // Total experience points earned
    private int hintsUsed;              // Total number of hints used across all bugs
    private int bugsSolvedWithoutHints; // Number of bugs solved without using any hints
    private int longestStreakDays;      // Longest streak ever achieved

    // Constructor
    public UserProgress() {
        this.id = 1;
        this.totalSolved = 0;
        this.streakDays = 0;
        this.easySolved = 0;
        this.mediumSolved = 0;
        this.hardSolved = 0;
        this.lastSolvedTimestamp = 0;
        this.lastOpenedTimestamp = 0;
        this.xp = 0;
        this.hintsUsed = 0;
        this.bugsSolvedWithoutHints = 0;
        this.longestStreakDays = 0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(int totalSolved) {
        this.totalSolved = totalSolved;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }

    public int getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(int easySolved) {
        this.easySolved = easySolved;
    }

    public int getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(int mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public int getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(int hardSolved) {
        this.hardSolved = hardSolved;
    }

    public long getLastSolvedTimestamp() {
        return lastSolvedTimestamp;
    }

    public void setLastSolvedTimestamp(long lastSolvedTimestamp) {
        this.lastSolvedTimestamp = lastSolvedTimestamp;
    }

    public long getLastOpenedTimestamp() {
        return lastOpenedTimestamp;
    }

    public void setLastOpenedTimestamp(long lastOpenedTimestamp) {
        this.lastOpenedTimestamp = lastOpenedTimestamp;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public int getBugsSolvedWithoutHints() {
        return bugsSolvedWithoutHints;
    }

    public void setBugsSolvedWithoutHints(int bugsSolvedWithoutHints) {
        this.bugsSolvedWithoutHints = bugsSolvedWithoutHints;
    }

    public int getLongestStreakDays() {
        return longestStreakDays;
    }

    public void setLongestStreakDays(int longestStreakDays) {
        this.longestStreakDays = longestStreakDays;
    }

    /**
     * Calculates the user's level based on XP.
     * Formula: level = 1 + xp / 100
     */
    public int getLevel() {
        return 1 + (xp / 100);
    }

    /**
     * Calculates XP needed to reach the next level.
     */
    public int getXpToNextLevel() {
        int currentLevel = getLevel();
        int xpForNextLevel = currentLevel * 100;
        return xpForNextLevel - xp;
    }

    /**
     * Calculates XP progress within the current level (0-100).
     */
    public int getXpProgressInLevel() {
        return xp % 100;
    }
}
