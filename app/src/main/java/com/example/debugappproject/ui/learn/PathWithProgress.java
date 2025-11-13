package com.example.debugappproject.ui.learn;

import com.example.debugappproject.model.LearningPath;

/**
 * Helper class combining LearningPath with progress data.
 */
public class PathWithProgress {
    private final LearningPath path;
    private final int totalBugs;
    private final int completedBugs;

    public PathWithProgress(LearningPath path, int totalBugs, int completedBugs) {
        this.path = path;
        this.totalBugs = totalBugs;
        this.completedBugs = completedBugs;
    }

    public LearningPath getPath() {
        return path;
    }

    public int getTotalBugs() {
        return totalBugs;
    }

    public int getCompletedBugs() {
        return completedBugs;
    }

    public int getProgressPercentage() {
        if (totalBugs == 0) return 0;
        return (int) ((completedBugs * 100.0) / totalBugs);
    }
}
