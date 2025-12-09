package com.example.debugappproject.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.local.AchievementDao;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.data.local.HintDao;
import com.example.debugappproject.data.local.LearningPathDao;
import com.example.debugappproject.data.local.LessonDao;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.BugInPath;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.model.Lesson;
import com.example.debugappproject.model.LessonQuestion;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that provides a clean API for data access.
 * Single source of truth for all data operations.
 */
public class BugRepository {

    private final BugDao bugDao;
    private final HintDao hintDao;
    private final UserProgressDao userProgressDao;
    private final LearningPathDao learningPathDao;
    private final LessonDao lessonDao;
    private final AchievementDao achievementDao;
    private final ExecutorService executorService;

    private final LiveData<List<Bug>> allBugs;
    private final LiveData<UserProgress> userProgress;

    public BugRepository(Application application) {
        DebugMasterDatabase database = DebugMasterDatabase.getInstance(application);
        bugDao = database.bugDao();
        hintDao = database.hintDao();
        userProgressDao = database.userProgressDao();
        learningPathDao = database.learningPathDao();
        lessonDao = database.lessonDao();
        achievementDao = database.achievementDao();
        executorService = Executors.newFixedThreadPool(4); // Increased for better performance

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

            // Increment difficulty-specific counter (case-insensitive)
            String diffLower = difficulty != null ? difficulty.toLowerCase() : "easy";
            switch (diffLower) {
                case "easy":
                    userProgressDao.incrementEasySolved();
                    break;
                case "medium":
                    userProgressDao.incrementMediumSolved();
                    break;
                case "hard":
                case "expert":
                    userProgressDao.incrementHardSolved();
                    break;
            }

            // Update last solved timestamp
            userProgressDao.updateLastSolvedTimestamp(System.currentTimeMillis());
        });
    }

    /**
     * Mark bug as completed with XP and gem rewards.
     * Calculates XP based on difficulty and whether hints were used.
     * XP values aligned with GameManager: Easy=10, Medium=25, Hard=50, Expert=100
     * Gem rewards: Easy=5, Medium=10, Hard=20, Expert=40
     *
     * @param bugId Bug ID to mark as completed
     * @param difficulty Difficulty level ("Easy", "Medium", "Hard", "Expert")
     * @param solvedWithoutHints Whether the bug was solved without using hints
     */
    public void markBugAsCompletedWithXP(int bugId, String difficulty, boolean solvedWithoutHints) {
        executorService.execute(() -> {
            bugDao.markBugAsCompleted(bugId);
            userProgressDao.incrementTotalSolved();

            // Calculate XP and gems based on difficulty
            int xpReward = 10; // Default for easy
            int gemReward = 5; // Default for easy
            String diffLower = difficulty != null ? difficulty.toLowerCase() : "easy";

            switch (diffLower) {
                case "easy":
                    userProgressDao.incrementEasySolved();
                    xpReward = 10;
                    gemReward = 5;
                    break;
                case "medium":
                    userProgressDao.incrementMediumSolved();
                    xpReward = 25;
                    gemReward = 10;
                    break;
                case "hard":
                    userProgressDao.incrementHardSolved();
                    xpReward = 50;
                    gemReward = 20;
                    break;
                case "expert":
                    userProgressDao.incrementHardSolved(); // Count expert as hard for stats
                    xpReward = 100;
                    gemReward = 40;
                    break;
            }

            // Bonus for solving without hints (2x XP, 1.5x gems)
            if (solvedWithoutHints) {
                xpReward *= 2;
                gemReward = (int)(gemReward * 1.5);
                userProgressDao.incrementBugsSolvedWithoutHints();
            }

            // Award XP and gems
            userProgressDao.addXp(xpReward);
            userProgressDao.addGems(gemReward);

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

    /**
     * Add gems to user's balance.
     */
    public void addGems(int amount) {
        executorService.execute(() -> {
            userProgressDao.addGems(amount);
        });
    }

    /**
     * Spend gems from user's balance.
     * @return true if successful (had enough gems), false otherwise
     */
    public boolean spendGems(int amount) {
        try {
            return executorService.submit(() -> {
                int rowsUpdated = userProgressDao.spendGems(amount);
                return rowsUpdated > 0;
            }).get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current gems count synchronously.
     */
    public int getGemsSync() {
        try {
            return executorService.submit(() -> userProgressDao.getGemsSync()).get();
        } catch (Exception e) {
            return 0;
        }
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
     * Get learning path count synchronously (for integrity check).
     */
    public int getPathCountSync() {
        try {
            return executorService.submit(() -> learningPathDao.getPathCountSync()).get();
        } catch (Exception e) {
            return 0;
        }
    }

    // Synchronous seeding methods - called from background thread during database initialization
    // These bypass the executor service to ensure completion before returning

    /**
     * Insert bugs synchronously. MUST be called from background thread.
     */
    public void insertBugsSync(List<Bug> bugs) {
        bugDao.insertAll(bugs);
    }

    /**
     * Insert hints synchronously. MUST be called from background thread.
     */
    public void insertHintsSync(List<Hint> hints) {
        hintDao.insertAll(hints);
    }

    /**
     * Insert initial progress synchronously. MUST be called from background thread.
     */
    public void insertInitialProgressSync() {
        UserProgress progress = userProgressDao.getUserProgressSync();
        if (progress == null) {
            userProgressDao.insert(new UserProgress());
        }
    }

    /**
     * Insert learning paths synchronously. MUST be called from background thread.
     */
    public void insertLearningPathsSync(List<LearningPath> paths) {
        learningPathDao.insertAllPaths(paths);
    }

    /**
     * Insert bug-in-path mappings synchronously. MUST be called from background thread.
     */
    public void insertBugInPathsSync(List<BugInPath> bugInPaths) {
        learningPathDao.insertAllBugInPath(bugInPaths);
    }

    /**
     * Insert achievements synchronously. MUST be called from background thread.
     */
    public void insertAchievementsSync(List<AchievementDefinition> achievements) {
        achievementDao.insertAllAchievementDefinitions(achievements);
    }

    /**
     * Update user notes for a bug.
     */
    public void updateBugNotes(int bugId, String notes) {
        executorService.execute(() -> {
            bugDao.updateBugNotes(bugId, notes);
        });
    }

    // Learning Path operations
    public LiveData<List<LearningPath>> getAllLearningPaths() {
        return learningPathDao.getAllPaths();
    }

    public LiveData<LearningPath> getLearningPathById(int pathId) {
        return learningPathDao.getPathById(pathId);
    }

    public LiveData<List<Integer>> getBugIdsInPath(int pathId) {
        return learningPathDao.getBugIdsInPath(pathId);
    }

    public LiveData<Integer> getBugCountInPath(int pathId) {
        return learningPathDao.getBugCountInPath(pathId);
    }

    public LiveData<Integer> getCompletedBugCountInPath(int pathId) {
        return learningPathDao.getCompletedBugCountInPath(pathId);
    }

    public void insertLearningPaths(List<LearningPath> paths) {
        executorService.execute(() -> {
            learningPathDao.insertAllPaths(paths);
        });
    }

    public void insertBugInPaths(List<BugInPath> bugInPaths) {
        executorService.execute(() -> {
            learningPathDao.insertAllBugInPath(bugInPaths);
        });
    }

    // Lesson operations
    public LiveData<Lesson> getLessonForBug(int bugId) {
        return lessonDao.getLessonForBug(bugId);
    }

    public LiveData<List<LessonQuestion>> getQuestionsForLesson(int lessonId) {
        return lessonDao.getQuestionsForLesson(lessonId);
    }

    public void insertLessons(List<Lesson> lessons) {
        executorService.execute(() -> {
            lessonDao.insertAllLessons(lessons);
        });
    }

    public void insertLessonQuestions(List<LessonQuestion> questions) {
        executorService.execute(() -> {
            lessonDao.insertAllQuestions(questions);
        });
    }

    // Achievement operations
    public LiveData<List<AchievementDefinition>> getAllAchievementDefinitions() {
        return achievementDao.getAllAchievementDefinitions();
    }

    public LiveData<List<UserAchievement>> getAllUnlockedAchievements() {
        return achievementDao.getAllUnlockedAchievements();
    }

    public LiveData<Integer> getUnlockedAchievementCount() {
        return achievementDao.getUnlockedAchievementCount();
    }

    public void insertAchievements(List<AchievementDefinition> achievements) {
        executorService.execute(() -> {
            achievementDao.insertAllAchievementDefinitions(achievements);
        });
    }

    public AchievementDao getAchievementDao() {
        return achievementDao;
    }

    public BugDao getBugDao() {
        return bugDao;
    }

    public UserProgressDao getUserProgressDao() {
        return userProgressDao;
    }

    public LearningPathDao getLearningPathDao() {
        return learningPathDao;
    }

    public LessonDao getLessonDao() {
        return lessonDao;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
