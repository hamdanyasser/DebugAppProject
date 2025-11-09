package com.example.debugappproject.model;

/**
 * Achievement - Represents a badge/achievement that can be unlocked.
 *
 * Achievements are computed in real-time based on UserProgress and Bug data.
 * Not stored in database - calculated on-the-fly for display.
 */
public class Achievement {
    private String name;
    private String description;
    private String icon;        // Emoji or icon identifier
    private boolean unlocked;

    public Achievement(String name, String description, String icon, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.unlocked = unlocked;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
