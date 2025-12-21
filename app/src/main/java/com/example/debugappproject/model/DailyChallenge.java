package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Daily Bug Hunt challenge - a lightweight daily challenge
 * that gives bonus XP and maintains streaks.
 */
@Entity(tableName = "daily_challenges")
public class DailyChallenge {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String dateKey;              // Format: "2025-12-20"
    private int bugId;                   // The bug to solve
    private String title;                // Challenge title
    private String description;          // Brief description
    private int xpReward;                // Bonus XP for completion
    private int estimatedMinutes;        // Time estimate (5-7 min typically)
    private String difficulty;           // Easy, Medium, Hard
    private boolean isCompleted;         // Whether user completed today's challenge
    private long completedAt;            // Timestamp of completion

    public DailyChallenge() {}

    @Ignore
    public DailyChallenge(String dateKey, int bugId, String title, String description,
                          int xpReward, int estimatedMinutes, String difficulty) {
        this.dateKey = dateKey;
        this.bugId = bugId;
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.estimatedMinutes = estimatedMinutes;
        this.difficulty = difficulty;
        this.isCompleted = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public int getBugId() { return bugId; }
    public void setBugId(int bugId) { this.bugId = bugId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}
