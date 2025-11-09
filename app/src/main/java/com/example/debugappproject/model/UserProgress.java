package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * UserProgress entity tracking the user's overall progress and statistics.
 * Singleton entity (only one row with id=1).
 *
 * NEW in Part 3: XP and Level system with hint tracking
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

    // NEW: XP and Level System (Part 3)
    private int xp = 0;                      // Total experience points earned
    private int level = 1;                   // Current level (computed from XP: level = 1 + xp/100)
    private int hintsUsed = 0;               // Total number of hints used across all bugs
    private int bugsSolvedWithoutHints = 0;  // Count of bugs solved without using any hints

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
        this.level = 1;
        this.hintsUsed = 0;
        this.bugsSolvedWithoutHints = 0;
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

    // NEW: XP and Level getters/setters
    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
        // Auto-update level when XP changes
        this.level = 1 + (xp / 100);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    /**
     * Helper method to add XP and automatically update level.
     * Formula: level = 1 + (xp / 100)
     * Example: 0-99 XP = Level 1, 100-199 XP = Level 2, etc.
     */
    public void addXp(int xpToAdd) {
        this.xp += xpToAdd;
        this.level = 1 + (this.xp / 100);
    }

    /**
     * Get XP needed for next level.
     */
    public int getXpForNextLevel() {
        return level * 100;
    }

    /**
     * Get XP progress within current level (0-99).
     */
    public int getXpProgressInLevel() {
        return xp % 100;
    }
}
