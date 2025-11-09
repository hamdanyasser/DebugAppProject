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

    /**
     * Marks a bug as completed (legacy method without XP - kept for compatibility).
     */
    public void markBugAsCompleted(int bugId, String difficulty) {
        markBugAsCompleted(bugId, difficulty, 0);
    }

    /**
     * Marks a bug as completed with XP rewards based on difficulty and hints used.
     *
     * XP Rewards:
     * - Easy: 10 XP
     * - Medium: 20 XP
     * - Hard: 30 XP
     * - Bonus +5 XP if solved without hints
     *
     * @param bugId The ID of the bug
     * @param difficulty The difficulty level (Easy, Medium, Hard)
     * @param hintsUsed The number of hints used for this bug (0 = no hints)
     */
    public void markBugAsCompleted(int bugId, String difficulty, int hintsUsed) {
        executorService.execute(() -> {
            bugDao.markBugAsCompleted(bugId);
            userProgressDao.incrementTotalSolved();

            // Calculate XP based on difficulty
            int xpReward;
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
                default:
                    xpReward = 10;
            }

            // Add bonus XP if solved without hints
            if (hintsUsed == 0) {
                xpReward += 5;
                userProgressDao.incrementBugsSolvedWithoutHints();
            }

            // Award XP
            userProgressDao.addXp(xpReward);

            // Update hints used count (if any hints were used)
            if (hintsUsed > 0) {
                for (int i = 0; i < hintsUsed; i++) {
                    userProgressDao.incrementHintsUsed();
                }
            }

            // Update last solved timestamp
            userProgressDao.updateLastSolvedTimestamp(System.currentTimeMillis());
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
}
