package com.example.debugappproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.data.local.HintDao;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.UserProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that provides a clean API for data access.
 * Single source of truth for all bug-related data operations.
 */
public class BugRepository {

    private final BugDao bugDao;
    private final HintDao hintDao;
    private final UserProgressDao userProgressDao;
    private final ExecutorService executorService;

    private final LiveData<List<Bug>> allBugs;
    private final LiveData<UserProgress> userProgress;

    public BugRepository(Application application) {
        DebugMasterDatabase database = DebugMasterDatabase.getInstance(application);
        bugDao = database.bugDao();
        hintDao = database.hintDao();
        userProgressDao = database.userProgressDao();
        executorService = Executors.newFixedThreadPool(2);

        allBugs = bugDao.getAllBugs();
        userProgress = userProgressDao.getUserProgress();
    }

    // Bug operations
    public LiveData<List<Bug>> getAllBugs() {
        return allBugs;
    }

    public LiveData<Bug> getBugById(int bugId) {
        return bugDao.getBugById(bugId);
    }

    public LiveData<List<Bug>> getBugsByDifficulty(String difficulty) {
        return bugDao.getBugsByDifficulty(difficulty);
    }

    public LiveData<List<Bug>> getBugsByCategory(String category) {
        return bugDao.getBugsByCategory(category);
    }

    public LiveData<List<Bug>> getBugsByDifficultyAndCategory(String difficulty, String category) {
        return bugDao.getBugsByDifficultyAndCategory(difficulty, category);
    }

    public LiveData<List<Bug>> getCompletedBugs() {
        return bugDao.getCompletedBugs();
    }

    public void markBugAsCompleted(int bugId, String difficulty) {
        executorService.execute(() -> {
            bugDao.markBugAsCompleted(bugId);
            userProgressDao.incrementTotalSolved();

            // Increment difficulty-specific counter
            switch (difficulty) {
                case "Easy":
                    userProgressDao.incrementEasySolved();
                    break;
                case "Medium":
                    userProgressDao.incrementMediumSolved();
                    break;
                case "Hard":
                    userProgressDao.incrementHardSolved();
                    break;
            }

            // Update last solved timestamp
            userProgressDao.updateLastSolvedTimestamp(System.currentTimeMillis());
        });
    }

    /**
     * Mark bug as completed with XP rewards.
     * Calculates XP based on difficulty and whether hints were used.
     *
     * @param bugId Bug ID to mark as completed
     * @param difficulty Difficulty level ("Easy", "Medium", "Hard")
     * @param solvedWithoutHints Whether the bug was solved without using hints
     */
    public void markBugAsCompletedWithXP(int bugId, String difficulty, boolean solvedWithoutHints) {
        executorService.execute(() -> {
            bugDao.markBugAsCompleted(bugId);
            userProgressDao.incrementTotalSolved();

            // Calculate XP based on difficulty
            int xpReward = 0;
            switch (difficulty) {
                case "Easy":
                    userProgressDao.incrementEasySolved();
                    xpReward = 10;
                    break;
                case "Medium":
                    userProgressDao.incrementMediumSolved();
                    xpReward = 20;
                    break;
                case "Hard":
                    userProgressDao.incrementHardSolved();
                    xpReward = 30;
                    break;
            }

            // Bonus XP for solving without hints
            if (solvedWithoutHints) {
                xpReward += 5;
                userProgressDao.incrementBugsSolvedWithoutHints();
            }

            // Award XP
            userProgressDao.addXp(xpReward);

            // Update last solved timestamp
            userProgressDao.updateLastSolvedTimestamp(System.currentTimeMillis());
        });
    }

    /**
     * Increment the global hints used counter.
     */
    public void incrementHintsUsed() {
        executorService.execute(() -> {
            userProgressDao.incrementHintsUsed();
        });
    }

    // Hint operations
    public LiveData<List<Hint>> getHintsForBug(int bugId) {
        return hintDao.getHintsForBug(bugId);
    }

    // User progress operations
    public LiveData<UserProgress> getUserProgress() {
        return userProgress;
    }

    public void updateLastOpenedTimestamp() {
        executorService.execute(() -> {
            userProgressDao.updateLastOpenedTimestamp(System.currentTimeMillis());
        });
    }

    public void updateStreak(int streak) {
        executorService.execute(() -> {
            userProgressDao.updateStreak(streak);
        });
    }

    public void resetProgress() {
        executorService.execute(() -> {
            bugDao.resetAllBugs();
            userProgressDao.resetProgress();
        });
    }

    // Database seeding (called once)
    public void insertBugs(List<Bug> bugs) {
        executorService.execute(() -> {
            bugDao.insertAll(bugs);
        });
    }

    public void insertHints(List<Hint> hints) {
        executorService.execute(() -> {
            hintDao.insertAll(hints);
        });
    }

    public void insertInitialProgress() {
        executorService.execute(() -> {
            UserProgress progress = userProgressDao.getUserProgressSync();
            if (progress == null) {
                userProgressDao.insert(new UserProgress());
            }
        });
    }

    /**
     * Get bug count synchronously (for seeding check).
     */
    public int getBugCountSync() {
        try {
            return executorService.submit(() -> bugDao.getBugCount()).get();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Update user notes for a bug.
     */
    public void updateBugNotes(int bugId, String notes) {
        executorService.execute(() -> {
            bugDao.updateBugNotes(bugId, notes);
        });
    }
}
