package com.example.debugappproject.util;

import com.example.debugappproject.data.local.AchievementDao;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class AchievementManagerXpTest {

    static class FakeAchievementDao implements AchievementDao {
        List<UserAchievement> inserted = new ArrayList<>();
        AchievementDefinition def;

        FakeAchievementDao(int xpReward) {
            def = new AchievementDefinition(AchievementManager.FIRST_FIX, "First Fix", "", "🎉", xpReward, "MILESTONE", 1);
        }

        @Override public androidx.lifecycle.LiveData<List<AchievementDefinition>> getAllAchievementDefinitions() { return null; }
        @Override public List<AchievementDefinition> getAllAchievementDefinitionsSync() { return null; }
        @Override public androidx.lifecycle.LiveData<AchievementDefinition> getAchievementDefinitionById(String achievementId) { return null; }
        @Override public AchievementDefinition getAchievementDefinitionByIdSync(String achievementId) { return def; }
        @Override public androidx.lifecycle.LiveData<List<AchievementDefinition>> getAchievementsByCategory(String category) { return null; }
        @Override public void insertAchievementDefinition(AchievementDefinition achievement) { }
        @Override public void insertAllAchievementDefinitions(List<AchievementDefinition> achievements) { }
        @Override public androidx.lifecycle.LiveData<List<UserAchievement>> getAllUnlockedAchievements() { return null; }
        @Override public List<UserAchievement> getAllUnlockedAchievementsSync() { return inserted; }
        @Override public androidx.lifecycle.LiveData<UserAchievement> getUserAchievement(String achievementId) { return null; }
        @Override public UserAchievement getUserAchievementSync(String achievementId) { return null; }
        @Override public androidx.lifecycle.LiveData<Integer> getUnlockedAchievementCount() { return null; }
        @Override public void insertUserAchievement(UserAchievement userAchievement) { inserted.add(userAchievement); }
        @Override public void markNotificationShown(String achievementId) { }
        @Override public void clearAllUserAchievements() { inserted.clear(); }
        @Override public void deleteAllUserAchievements() { inserted.clear(); }
    }

    static class FakeUserProgressDao implements UserProgressDao {
        UserProgress progress = new UserProgress();
        @Override public void insert(UserProgress userProgress) { this.progress = userProgress; }
        @Override public void update(UserProgress userProgress) { this.progress = userProgress; }
        @Override public androidx.lifecycle.LiveData<UserProgress> getUserProgress() { return null; }
        @Override public UserProgress getUserProgressSync() { return progress; }
        @Override public void incrementTotalSolved() { progress.setTotalSolved(progress.getTotalSolved() + 1); }
        @Override public void incrementEasySolved() { }
        @Override public void incrementMediumSolved() { }
        @Override public void incrementHardSolved() { }
        @Override public void updateLastSolvedTimestamp(long timestamp) { progress.setLastSolvedTimestamp(timestamp); }
        @Override public void updateLastOpenedTimestamp(long timestamp) { progress.setLastOpenedTimestamp(timestamp); }
        @Override public void updateStreak(int streak) { progress.setStreakDays(streak); }
        @Override public void updateLongestStreak(int longestStreak) { progress.setLongestStreakDays(longestStreak); }
        @Override public void addXp(int xpAmount) { progress.setXp(progress.getXp() + xpAmount); }
        @Override public void incrementHintsUsed() { progress.setHintsUsed(progress.getHintsUsed() + 1); }
        @Override public void incrementBugsSolvedWithoutHints() { progress.setBugsSolvedWithoutHints(progress.getBugsSolvedWithoutHints() + 1); }
        @Override public void resetProgress() { progress = new UserProgress(); }
        @Override public void deleteAllProgress() { progress = new UserProgress(); }
    }

    static class FakeBugDao implements BugDao {
        @Override public void insertAll(List<com.example.debugappproject.model.Bug> bugs) { }
        @Override public void insert(com.example.debugappproject.model.Bug bug) { }
        @Override public void update(com.example.debugappproject.model.Bug bug) { }
        @Override public androidx.lifecycle.LiveData<List<com.example.debugappproject.model.Bug>> getAllBugs() { return null; }
        @Override public androidx.lifecycle.LiveData<com.example.debugappproject.model.Bug> getBugById(int bugId) { return null; }
        @Override public com.example.debugappproject.model.Bug getBugByIdSync(int bugId) { return null; }
        @Override public androidx.lifecycle.LiveData<List<com.example.debugappproject.model.Bug>> getBugsByDifficulty(String difficulty) { return null; }
        @Override public androidx.lifecycle.LiveData<List<com.example.debugappproject.model.Bug>> getBugsByCategory(String category) { return null; }
        @Override public androidx.lifecycle.LiveData<List<com.example.debugappproject.model.Bug>> getBugsByDifficultyAndCategory(String difficulty, String category) { return null; }
        @Override public androidx.lifecycle.LiveData<List<com.example.debugappproject.model.Bug>> getCompletedBugs() { return null; }
        @Override public int getBugCount() { return 15; }
        @Override public int getCompletedBugCount() { return 0; }
        @Override public int getCompletedBugsCount() { return 0; }
        @Override public int getBugCountByDifficulty(String difficulty) { return 0; }
        @Override public int getCompletedBugCountByDifficulty(String difficulty) { return 0; }
        @Override public int getBugCountByCategory(String category) { return 0; }
        @Override public int getCompletedBugsByCategory(String category) { return 0; }
        @Override public void markBugAsCompleted(int bugId) { }
        @Override public boolean isBugCompleted(int bugId) { return false; }
        @Override public void resetAllBugs() { }
        @Override public void resetAllBugsToNotCompleted() { }
        @Override public void updateBugNotes(int bugId, String notes) { }
        @Override public void clearAllUserNotes() { }
    }

    @Test
    public void unlockingFirstFixAwardsXp() throws InterruptedException {
        FakeAchievementDao achievementDao = new FakeAchievementDao(10);
        FakeUserProgressDao userProgressDao = new FakeUserProgressDao();
        FakeBugDao bugDao = new FakeBugDao();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Progress: simulate that at least one bug is solved
        UserProgress p = userProgressDao.getUserProgressSync();
        p.setTotalSolved(1);
        userProgressDao.update(p);

        AchievementManager manager = new AchievementManager(achievementDao, userProgressDao, bugDao, executor);

        CountDownLatch latch = new CountDownLatch(1);
        manager.checkAndUnlockAchievements(ids -> latch.countDown());
        latch.await();

        assertTrue(userProgressDao.getUserProgressSync().getXp() >= 10);
    }
}