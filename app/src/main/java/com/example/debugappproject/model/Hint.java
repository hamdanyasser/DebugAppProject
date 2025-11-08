package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Hint entity representing progressive hints for debugging exercises.
 * Each bug can have multiple hints with increasing levels of specificity.
 */
@Entity(tableName = "hints")
public class Hint {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int bugId;      // Foreign key to Bug
    private int level;      // Hint level (1-5), higher = more specific
    private String text;    // Hint content

    // Constructor
    public Hint(int bugId, int level, String text) {
        this.bugId = bugId;
        this.level = level;
        this.text = text;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBugId() {
        return bugId;
    }

    public void setBugId(int bugId) {
        this.bugId = bugId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
