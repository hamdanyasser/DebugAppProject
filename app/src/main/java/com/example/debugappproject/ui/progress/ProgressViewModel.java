package com.example.debugappproject.ui.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Achievement;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for ProgressFragment.
 * Manages user progress statistics and completed bugs.
 */
public class ProgressViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<UserProgress> userProgress;
    private final LiveData<List<Bug>> completedBugs;
    private final LiveData<List<Bug>> allBugs;

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        userProgress = repository.getUserProgress();
        completedBugs = repository.getCompletedBugs();
        allBugs = repository.getAllBugs();
    }

    public LiveData<UserProgress> getUserProgress() {
        return userProgress;
    }

    public LiveData<List<Bug>> getCompletedBugs() {
        return completedBugs;
    }

    public LiveData<List<Bug>> getAllBugs() {
        return allBugs;
    }

    /**
     * Reset all progress.
     */
    public void resetProgress() {
        repository.resetProgress();
    }

    /**
     * Computes achievements based on user progress and completed bugs.
     *
     * Achievements include:
     * - First Fix: Solve your first bug
     * - Bug Hunter: Solve 5 bugs
     * - Bug Slayer: Solve 10 bugs
     * - Loop Tamer: Solve 3 loop-related bugs
     * - String Surgeon: Solve 3 string-related bugs
     * - Condition Master: Solve 3 condition-related bugs
     * - No Help Needed: Solve 5 bugs without using any hints
     * - Dedicated: Maintain a 3-day streak
     * - On Fire: Maintain a 7-day streak
     * - Level Up: Reach level 3
     * - Experienced: Reach level 5
     * - Easy Master: Complete all Easy bugs
     * - Medium Master: Complete all Medium bugs
     * - Hard Master: Complete all Hard bugs
     *
     * @param progress UserProgress containing stats
     * @param completedBugs List of completed bugs
     * @param allBugs List of all bugs for completion calculation
     * @return List of achievements with unlock status
     */
    public List<Achievement> computeAchievements(UserProgress progress, List<Bug> completedBugs, List<Bug> allBugs) {
        List<Achievement> achievements = new ArrayList<>();

        if (progress == null || completedBugs == null || allBugs == null) {
            return achievements;
        }

        // First Fix - Solve your first bug
        achievements.add(new Achievement(
                "First Fix",
                "Solve your first bug",
                "🎯",
                progress.getTotalSolved() >= 1
        ));

        // Bug Hunter - Solve 5 bugs
        achievements.add(new Achievement(
                "Bug Hunter",
                "Solve 5 bugs",
                "🔍",
                progress.getTotalSolved() >= 5
        ));

        // Bug Slayer - Solve 10 bugs
        achievements.add(new Achievement(
                "Bug Slayer",
                "Solve 10 bugs",
                "⚔️",
                progress.getTotalSolved() >= 10
        ));

        // Count category-specific completions
        int loopBugs = 0;
        int stringBugs = 0;
        int conditionBugs = 0;

        for (Bug bug : completedBugs) {
            String category = bug.getCategory().toLowerCase();
            if (category.contains("loop")) {
                loopBugs++;
            } else if (category.contains("string")) {
                stringBugs++;
            } else if (category.contains("condition")) {
                conditionBugs++;
            }
        }

        // Loop Tamer - Solve 3 loop-related bugs
        achievements.add(new Achievement(
                "Loop Tamer",
                "Solve 3 loop-related bugs",
                "🔄",
                loopBugs >= 3
        ));

        // String Surgeon - Solve 3 string-related bugs
        achievements.add(new Achievement(
                "String Surgeon",
                "Solve 3 string-related bugs",
                "✂️",
                stringBugs >= 3
        ));

        // Condition Master - Solve 3 condition-related bugs
        achievements.add(new Achievement(
                "Condition Master",
                "Solve 3 condition-related bugs",
                "🎲",
                conditionBugs >= 3
        ));

        // No Help Needed - Solve 5 bugs without using any hints
        achievements.add(new Achievement(
                "No Help Needed",
                "Solve 5 bugs without hints",
                "💪",
                progress.getBugsSolvedWithoutHints() >= 5
        ));

        // Dedicated - Maintain a 3-day streak
        achievements.add(new Achievement(
                "Dedicated",
                "Maintain a 3-day streak",
                "📅",
                progress.getStreakDays() >= 3
        ));

        // On Fire - Maintain a 7-day streak
        achievements.add(new Achievement(
                "On Fire",
                "Maintain a 7-day streak",
                "🔥",
                progress.getStreakDays() >= 7
        ));

        // Level Up - Reach level 3
        achievements.add(new Achievement(
                "Level Up",
                "Reach level 3",
                "📈",
                progress.getLevel() >= 3
        ));

        // Experienced - Reach level 5
        achievements.add(new Achievement(
                "Experienced",
                "Reach level 5",
                "🏆",
                progress.getLevel() >= 5
        ));

        // Count bugs by difficulty
        int easyTotal = 0;
        int mediumTotal = 0;
        int hardTotal = 0;

        for (Bug bug : allBugs) {
            String difficulty = bug.getDifficulty();
            if (difficulty.equals("Easy")) {
                easyTotal++;
            } else if (difficulty.equals("Medium")) {
                mediumTotal++;
            } else if (difficulty.equals("Hard")) {
                hardTotal++;
            }
        }

        // Easy Master - Complete all Easy bugs
        achievements.add(new Achievement(
                "Easy Master",
                "Complete all Easy bugs",
                "🟢",
                easyTotal > 0 && progress.getEasySolved() >= easyTotal
        ));

        // Medium Master - Complete all Medium bugs
        achievements.add(new Achievement(
                "Medium Master",
                "Complete all Medium bugs",
                "🟡",
                mediumTotal > 0 && progress.getMediumSolved() >= mediumTotal
        ));

        // Hard Master - Complete all Hard bugs
        achievements.add(new Achievement(
                "Hard Master",
                "Complete all Hard bugs",
                "🔴",
                hardTotal > 0 && progress.getHardSolved() >= hardTotal
        ));

        return achievements;
    }
}
