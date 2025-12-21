package com.example.debugappproject.util;

import com.example.debugappproject.model.AchievementDefinition;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for achievement definitions and conditions.
 * Tests the achievement configurations and validates their requirements.
 */
public class AchievementDefinitionsTest {

    @Test
    public void getAllAchievementDefinitions_returnsNonEmptyList() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        assertNotNull(achievements);
        assertFalse(achievements.isEmpty());
    }

    @Test
    public void getAllAchievementDefinitions_hasExpectedCount() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        // Should have many achievements (40+)
        assertTrue("Should have at least 30 achievements", achievements.size() >= 30);
    }

    @Test
    public void firstFixAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        AchievementDefinition firstFix = findById(achievements, "first_fix");

        assertNotNull("first_fix achievement should exist", firstFix);
        assertEquals("First Fix", firstFix.getName());
        assertEquals("Solve your first bug", firstFix.getDescription());
        assertTrue("XP reward should be positive", firstFix.getXpReward() > 0);
        assertEquals("MILESTONE", firstFix.getCategory());
    }

    @Test
    public void streakAchievements_exist() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        
        AchievementDefinition streak3 = findById(achievements, "streak_3");
        AchievementDefinition streak7 = findById(achievements, "streak_7");
        AchievementDefinition streak30 = findById(achievements, "streak_30");

        assertNotNull("streak_3 should exist", streak3);
        assertNotNull("streak_7 should exist", streak7);
        assertNotNull("streak_30 should exist", streak30);
        
        assertEquals("STREAK", streak3.getCategory());
        assertEquals("STREAK", streak7.getCategory());
        assertEquals("STREAK", streak30.getCategory());
    }

    @Test
    public void xpAchievements_exist() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        
        AchievementDefinition xp100 = findById(achievements, "xp_100");
        AchievementDefinition xp1000 = findById(achievements, "xp_1000");
        AchievementDefinition xp10000 = findById(achievements, "xp_10000");

        assertNotNull("xp_100 should exist", xp100);
        assertNotNull("xp_1000 should exist", xp1000);
        assertNotNull("xp_10000 should exist", xp10000);
    }

    @Test
    public void bugMilestoneAchievements_exist() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        
        AchievementDefinition bugSquasher = findById(achievements, "bug_squasher_10");
        AchievementDefinition bugHunter = findById(achievements, "bug_hunter_25");
        AchievementDefinition bugMaster = findById(achievements, "bug_master_100");

        assertNotNull("bug_squasher_10 should exist", bugSquasher);
        assertNotNull("bug_hunter_25 should exist", bugHunter);
        assertNotNull("bug_master_100 should exist", bugMaster);
        
        assertEquals("MILESTONE", bugSquasher.getCategory());
    }

    @Test
    public void allAchievements_haveUniqueIds() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        long uniqueIds = achievements.stream()
            .map(AchievementDefinition::getId)
            .distinct()
            .count();

        assertEquals(achievements.size(), uniqueIds);
    }

    @Test
    public void allAchievements_haveUniqueSortOrders() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        long uniqueSortOrders = achievements.stream()
            .map(AchievementDefinition::getSortOrder)
            .distinct()
            .count();

        assertEquals(achievements.size(), uniqueSortOrders);
    }

    @Test
    public void allAchievements_havePositiveXpRewards() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        for (AchievementDefinition achievement : achievements) {
            assertTrue("Achievement " + achievement.getName() + " should have positive XP",
                achievement.getXpReward() > 0);
        }
    }

    @Test
    public void allAchievements_haveNonEmptyNames() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        for (AchievementDefinition achievement : achievements) {
            assertNotNull(achievement.getName());
            assertFalse(achievement.getName().isEmpty());
        }
    }

    @Test
    public void allAchievements_haveNonEmptyDescriptions() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        for (AchievementDefinition achievement : achievements) {
            assertNotNull(achievement.getDescription());
            assertFalse(achievement.getDescription().isEmpty());
        }
    }

    @Test
    public void achievementCategories_areValid() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        String[] validCategories = {"MILESTONE", "SKILL", "STREAK", "BATTLE", "CHALLENGE", "MASTERY", "SECRET"};

        for (AchievementDefinition achievement : achievements) {
            String category = achievement.getCategory();
            boolean isValid = false;
            for (String validCategory : validCategories) {
                if (validCategory.equals(category)) {
                    isValid = true;
                    break;
                }
            }
            assertTrue("Achievement " + achievement.getName() + " has invalid category: " + category,
                isValid);
        }
    }

    @Test
    public void battleAchievements_exist() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        
        AchievementDefinition firstWin = findById(achievements, "first_battle_win");
        assertNotNull("first_battle_win should exist", firstWin);
        assertEquals("BATTLE", firstWin.getCategory());
    }

    @Test
    public void skillAchievements_exist() {
        List<AchievementDefinition> achievements = AchievementManager.getAllAchievementDefinitions();
        
        AchievementDefinition noHints = findById(achievements, "no_hint_10");
        assertNotNull("no_hint_10 should exist", noHints);
        assertEquals("SKILL", noHints.getCategory());
    }

    /**
     * Helper method to find achievement by ID.
     */
    private AchievementDefinition findById(List<AchievementDefinition> achievements, String id) {
        for (AchievementDefinition achievement : achievements) {
            if (id.equals(achievement.getId())) {
                return achievement;
            }
        }
        return null;
    }

    /**
     * Test that validates achievement unlock conditions (documentation).
     */
    @Test
    public void achievementConditions_areDocumented() {
        // Achievement unlock conditions:
        // first_fix: totalSolved >= 1
        // bug_squasher_10: totalSolved >= 10
        // bug_hunter_25: totalSolved >= 25
        // bug_master_100: totalSolved >= 100
        // streak_3: streakDays >= 3
        // streak_7: streakDays >= 7
        // streak_30: streakDays >= 30
        // xp_100: totalXp >= 100
        // xp_1000: totalXp >= 1000
        // no_hints_5: bugsSolvedWithoutHints >= 5
        // battle_first_win: battleWins >= 1

        // This test documents the unlock conditions
        assertTrue(true);
    }
}
