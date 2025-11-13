package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Lesson entity representing a micro-lesson attached to a bug.
 * Each bug can have an optional lesson that teaches the underlying concept
 * before the user attempts to debug the code.
 */
@Entity(
    tableName = "lessons",
    foreignKeys = {
        @ForeignKey(entity = Bug.class, parentColumns = "id", childColumns = "bugId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("bugId")}
)
public class Lesson {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int bugId;                  // Associated bug
    private String title;               // Lesson title (e.g., "Understanding Null Pointers")
    private String content;             // Lesson content (Markdown or plain text)
    private int estimatedMinutes;       // Estimated reading time in minutes

    public Lesson() {
    }

    public Lesson(int bugId, String title, String content, int estimatedMinutes) {
        this.bugId = bugId;
        this.title = title;
        this.content = content;
        this.estimatedMinutes = estimatedMinutes;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }
}
