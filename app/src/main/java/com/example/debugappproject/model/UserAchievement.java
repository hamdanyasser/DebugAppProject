package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * UserAchievement entity tracking which achievements the user has unlocked.
 */
@Entity(
    tableName = "user_achievements",
    foreignKeys = {
        @ForeignKey(entity = AchievementDefinition.class, parentColumns = "id", childColumns = "achievementId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("achievementId")}
)
public class UserAchievement {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String achievementId;       // Foreign key to achievement_definitions.id
    private long unlockedTimestamp;     // When the achievement was unlocked
    private boolean notificationShown;  // Whether we've shown the unlock notification

    public UserAchievement() {
    }

    @Ignore
    public UserAchievement(String achievementId, long unlockedTimestamp, boolean notificationShown) {
        this.achievementId = achievementId;
        this.unlockedTimestamp = unlockedTimestamp;
        this.notificationShown = notificationShown;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(String achievementId) {
        this.achievementId = achievementId;
    }

    public long getUnlockedTimestamp() {
        return unlockedTimestamp;
    }

    public void setUnlockedTimestamp(long unlockedTimestamp) {
        this.unlockedTimestamp = unlockedTimestamp;
    }

    public boolean isNotificationShown() {
        return notificationShown;
    }

    public void setNotificationShown(boolean notificationShown) {
        this.notificationShown = notificationShown;
    }
}
