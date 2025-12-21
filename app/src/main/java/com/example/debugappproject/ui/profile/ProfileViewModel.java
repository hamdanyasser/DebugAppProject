package com.example.debugappproject.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for Profile screen.
 * Provides user progress data and achievements.
 */
public class ProfileViewModel extends AndroidViewModel {
    private final BugRepository repository;
    private final MutableLiveData<List<AchievementWithStatus>> achievementsWithStatus = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
    }

    /**
     * Gets the user's progress data.
     */
    public LiveData<UserProgress> getUserProgress() {
        return repository.getUserProgress();
    }

    /**
     * Gets all achievements with their unlock status.
     */
    public LiveData<List<AchievementWithStatus>> getAchievementsWithStatus() {
        return achievementsWithStatus;
    }

    /**
     * Loads achievements and combines them with unlock status.
     * This runs on a background thread.
     */
    public void loadAchievements() {
        repository.getExecutorService().execute(() -> {
            List<AchievementDefinition> allAchievements =
                repository.getAchievementDao().getAllAchievementDefinitionsSync();
            List<UserAchievement> unlockedAchievements =
                repository.getAchievementDao().getAllUnlockedAchievementsSync();

            // Create a map of unlocked achievements by achievement ID
            Map<String, UserAchievement> unlockedMap = new HashMap<>();
            for (UserAchievement ua : unlockedAchievements) {
                unlockedMap.put(ua.getAchievementId(), ua);
            }

            // Combine definitions with unlock status
            List<AchievementWithStatus> combined = new ArrayList<>();
            for (AchievementDefinition def : allAchievements) {
                UserAchievement unlocked = unlockedMap.get(def.getId());
                combined.add(new AchievementWithStatus(def, unlocked));
            }

            // Sort: unlocked first, then by sort order
            combined.sort((a, b) -> {
                if (a.isUnlocked() != b.isUnlocked()) {
                    return a.isUnlocked() ? -1 : 1;
                }
                return Integer.compare(
                    a.getDefinition().getSortOrder(),
                    b.getDefinition().getSortOrder()
                );
            });

            achievementsWithStatus.postValue(combined);
        });
    }

    /**
     * Calculates the user's level from their XP.
     * Formula: level = 1 + (xp / 100)
     */
    public static int calculateLevel(int xp) {
        return 1 + (xp / 100);
    }

    /**
     * Calculates XP progress within current level.
     * Returns percentage (0-100).
     */
    public static int getXpProgressInLevel(int xp) {
        return xp % 100;
    }

    /**
     * Gets XP needed for next level.
     */
    public static int getXpForNextLevel(int xp) {
        int currentLevel = calculateLevel(xp);
        return currentLevel * 100;
    }

    /**
     * Gets total number of bugs completed.
     */
    public void getTotalBugsCompleted(TotalBugsCallback callback) {
        repository.getExecutorService().execute(() -> {
            int total = repository.getBugDao().getCompletedBugsCount();
            callback.onResult(total);
        });
    }

    public interface TotalBugsCallback {
        void onResult(int count);
    }
}
