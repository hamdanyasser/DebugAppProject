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
}
