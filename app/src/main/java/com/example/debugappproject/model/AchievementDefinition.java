package com.example.debugappproject.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * AchievementDefinition entity representing an achievement/badge definition.
 * Defines all possible achievements that can be unlocked.
 */
@Entity(tableName = "achievement_definitions")
public class AchievementDefinition {

    @PrimaryKey
    @NonNull
    private String id;                  // Unique achievement ID (e.g., "first_fix", "no_hint_hero")

    private String name;                // Display name (e.g., "First Fix")
    private String description;         // Achievement description
    private String iconEmoji;           // Emoji icon
    private int xpReward;               // XP awarded when unlocked
    private String category;            // Category: "MILESTONE", "STREAK", "SKILL", "SPEED"
    private int sortOrder;              // Display order

    public AchievementDefinition() {
    }

    public AchievementDefinition(String id, String name, String description, String iconEmoji,
                                int xpReward, String category, int sortOrder) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.xpReward = xpReward;
        this.category = category;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
