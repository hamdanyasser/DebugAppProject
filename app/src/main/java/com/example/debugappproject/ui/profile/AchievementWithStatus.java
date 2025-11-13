package com.example.debugappproject.ui.profile;

import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.UserAchievement;

/**
 * Helper class combining AchievementDefinition with unlock status.
 * Used for displaying achievements in the Profile screen.
 */
public class AchievementWithStatus {
    private final AchievementDefinition definition;
    private final UserAchievement userAchievement; // null if locked

    public AchievementWithStatus(AchievementDefinition definition, UserAchievement userAchievement) {
        this.definition = definition;
        this.userAchievement = userAchievement;
    }

    public AchievementDefinition getDefinition() {
        return definition;
    }

    public UserAchievement getUserAchievement() {
        return userAchievement;
    }

    public boolean isUnlocked() {
        return userAchievement != null;
    }
}
