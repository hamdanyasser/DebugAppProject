package com.example.debugappproject.ui.learn;

import com.example.debugappproject.model.Bug;

/**
 * Helper class combining Bug with completion status.
 * Used for displaying bugs within a learning path.
 */
public class BugInPathWithDetails {
    private final Bug bug;
    private final boolean isCompleted;

    public BugInPathWithDetails(Bug bug, boolean isCompleted) {
        this.bug = bug;
        this.isCompleted = isCompleted;
    }

    public Bug getBug() {
        return bug;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
