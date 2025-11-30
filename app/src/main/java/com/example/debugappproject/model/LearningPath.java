package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * LearningPath entity representing a structured learning journey.
 * Groups bugs into themed paths with gamification elements inspired by 
 * Duolingo, Mimo, and Datacamp.
 */
@Entity(tableName = "learning_paths")
public class LearningPath {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;                    // Path name (e.g., "Python Fundamentals")
    private String description;             // Short description
    private String iconEmoji;               // Emoji icon for visual representation
    private String difficultyRange;         // "Beginner", "Intermediate", "Advanced", "Expert"
    private int sortOrder;                  // Display order (lower = shown first)
    private boolean isLocked;               // Whether path requires Pro subscription
    
    // New fields for enhanced learning experience
    private String category;                // "Programming", "AI/ML", "Web", "Mobile", "Database", "Soft Skills"
    private int estimatedMinutes;           // Estimated time to complete
    private int totalLessons;               // Number of lessons in path
    private int xpReward;                   // XP earned for completing path
    private String prerequisites;           // Comma-separated path IDs required before this
    private boolean isFeatured;             // Show in featured section
    private boolean isNew;                  // Show "NEW" badge
    private String colorHex;                // Accent color for the path card
    private String skillTags;               // Comma-separated skills learned (e.g., "loops,arrays,debugging")

    public LearningPath() {
    }

    @Ignore
    public LearningPath(String name, String description, String iconEmoji, String difficultyRange, int sortOrder, boolean isLocked) {
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.difficultyRange = difficultyRange;
        this.sortOrder = sortOrder;
        this.isLocked = isLocked;
        this.category = "Programming";
        this.estimatedMinutes = 30;
        this.totalLessons = 10;
        this.xpReward = 100;
        this.isFeatured = false;
        this.isNew = false;
        this.colorHex = "#6366F1";
    }
    
    @Ignore
    public LearningPath(String name, String description, String iconEmoji, String difficultyRange, 
                        int sortOrder, boolean isLocked, String category, int estimatedMinutes,
                        int totalLessons, int xpReward, boolean isFeatured, boolean isNew, String colorHex) {
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.difficultyRange = difficultyRange;
        this.sortOrder = sortOrder;
        this.isLocked = isLocked;
        this.category = category;
        this.estimatedMinutes = estimatedMinutes;
        this.totalLessons = totalLessons;
        this.xpReward = xpReward;
        this.isFeatured = isFeatured;
        this.isNew = isNew;
        this.colorHex = colorHex;
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
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getSkillTags() {
        return skillTags;
    }

    public void setSkillTags(String skillTags) {
        this.skillTags = skillTags;
    }
}
