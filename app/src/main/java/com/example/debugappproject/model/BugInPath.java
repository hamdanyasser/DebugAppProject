package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Junction entity for many-to-many relationship between Bugs and LearningPaths.
 * A bug can belong to multiple paths, and a path contains multiple bugs.
 */
@Entity(
    tableName = "bug_in_path",
    primaryKeys = {"bugId", "pathId"},
    foreignKeys = {
        @ForeignKey(entity = Bug.class, parentColumns = "id", childColumns = "bugId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = LearningPath.class, parentColumns = "id", childColumns = "pathId", onDelete = ForeignKey.CASCADE)
    },
    indices = {
        @Index("bugId"),
        @Index("pathId")
    }
)
public class BugInPath {

    private int bugId;
    private int pathId;
    private int orderInPath;    // Position within the path (for sequential ordering)

    public BugInPath(int bugId, int pathId, int orderInPath) {
        this.bugId = bugId;
        this.pathId = pathId;
        this.orderInPath = orderInPath;
    }

    // Getters and Setters
    public int getBugId() {
        return bugId;
    }

    public void setBugId(int bugId) {
        this.bugId = bugId;
    }

    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public int getOrderInPath() {
        return orderInPath;
    }

    public void setOrderInPath(int orderInPath) {
        this.orderInPath = orderInPath;
    }
}
