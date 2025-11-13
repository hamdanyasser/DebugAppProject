package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;

import java.util.List;

/**
 * DAO for Achievement and UserAchievement operations.
 */
@Dao
public interface AchievementDao {

    // Achievement Definition queries
    @Query("SELECT * FROM achievement_definitions ORDER BY sortOrder ASC")
    LiveData<List<AchievementDefinition>> getAllAchievementDefinitions();

    @Query("SELECT * FROM achievement_definitions WHERE id = :achievementId")
    LiveData<AchievementDefinition> getAchievementDefinitionById(String achievementId);

    @Query("SELECT * FROM achievement_definitions WHERE category = :category ORDER BY sortOrder ASC")
    LiveData<List<AchievementDefinition>> getAchievementsByCategory(String category);

    @Insert
    void insertAchievementDefinition(AchievementDefinition achievement);

    @Insert
    void insertAllAchievementDefinitions(List<AchievementDefinition> achievements);

    // User Achievement queries
    @Query("SELECT * FROM user_achievements")
    LiveData<List<UserAchievement>> getAllUnlockedAchievements();

    @Query("SELECT * FROM user_achievements WHERE achievementId = :achievementId LIMIT 1")
    LiveData<UserAchievement> getUserAchievement(String achievementId);

    @Query("SELECT * FROM user_achievements WHERE achievementId = :achievementId LIMIT 1")
    UserAchievement getUserAchievementSync(String achievementId);

    @Query("SELECT COUNT(*) FROM user_achievements")
    LiveData<Integer> getUnlockedAchievementCount();

    @Insert
    void insertUserAchievement(UserAchievement userAchievement);

    @Query("UPDATE user_achievements SET notificationShown = 1 WHERE achievementId = :achievementId")
    void markNotificationShown(String achievementId);

    @Query("DELETE FROM user_achievements")
    void clearAllUserAchievements();
}
