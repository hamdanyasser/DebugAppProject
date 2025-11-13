package com.example.debugappproject.ui.learn;

import com.example.debugappproject.model.BugChallenge;

/**
 * Helper class combining BugChallenge with completion status.
 * Used for displaying bugs within a learning path.
 */
public class BugInPathWithDetails {
    private final BugChallenge bug;
    private final boolean isCompleted;

    public BugInPathWithDetails(BugChallenge bug, boolean isCompleted) {
        this.bug = bug;
        this.isCompleted = isCompleted;
    }

    public BugChallenge getBug() {
        return bug;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
