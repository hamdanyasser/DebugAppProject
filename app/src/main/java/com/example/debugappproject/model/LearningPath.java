package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * LearningPath entity representing a structured learning journey.
 * Groups bugs into themed paths (e.g., "Basics of Debugging", "Nulls & Crashes").
 */
@Entity(tableName = "learning_paths")
public class LearningPath {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;                    // Path name (e.g., "Basics of Debugging")
    private String description;             // Short description
    private String iconEmoji;               // Emoji icon for visual representation
    private String difficultyRange;         // "Easy", "Easy-Medium", "Medium-Hard", "All"
    private int sortOrder;                  // Display order (lower = shown first)
    private boolean isLocked;               // Whether path is locked (requires previous path completion)

    public LearningPath() {
    }

    public LearningPath(String name, String description, String iconEmoji, String difficultyRange, int sortOrder, boolean isLocked) {
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.difficultyRange = difficultyRange;
        this.sortOrder = sortOrder;
        this.isLocked = isLocked;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getDifficultyRange() {
        return difficultyRange;
    }

    public void setDifficultyRange(String difficultyRange) {
        this.difficultyRange = difficultyRange;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
