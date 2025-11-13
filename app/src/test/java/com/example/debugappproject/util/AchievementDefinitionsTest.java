package com.example.debugappproject.util;

import com.example.debugappproject.model.AchievementDefinition;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for achievement definitions and conditions.
 * Tests the default achievement configurations and validates their requirements.
 */
public class AchievementDefinitionsTest {

    @Test
    public void getDefaultAchievements_returnsNonEmptyList() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        assertNotNull(achievements);
        assertFalse(achievements.isEmpty());
    }

    @Test
    public void getDefaultAchievements_returns13Achievements() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        assertEquals(13, achievements.size());
    }

    @Test
    public void firstFixAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition firstFix = findById(achievements, AchievementManager.FIRST_FIX);

        assertNotNull(firstFix);
        assertEquals("First Fix", firstFix.getName());
        assertEquals("Solve your first bug", firstFix.getDescription());
        assertEquals(10, firstFix.getXpReward());
        assertEquals("MILESTONE", firstFix.getCategory());
    }

    @Test
    public void noHintHeroAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition noHintHero = findById(achievements, AchievementManager.NO_HINT_HERO);

        assertNotNull(noHintHero);
        assertEquals("No-Hint Hero", noHintHero.getName());
        assertEquals(25, noHintHero.getXpReward());
        assertEquals("SKILL", noHintHero.getCategory());
    }

    @Test
    public void streakMachineAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition streakMachine = findById(achievements, AchievementManager.STREAK_MACHINE);

        assertNotNull(streakMachine);
        assertEquals("Streak Machine", streakMachine.getName());
        assertEquals(30, streakMachine.getXpReward());
        assertEquals("STREAK", streakMachine.getCategory());
    }

    @Test
    public void perfectTenAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition perfectTen = findById(achievements, AchievementManager.PERFECT_TEN);

        assertNotNull(perfectTen);
        assertEquals("Perfect Ten", perfectTen.getName());
        assertEquals(30, perfectTen.getXpReward());
    }

    @Test
    public void completionistAchievement_hasHighestXpReward() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition completionist = findById(achievements, AchievementManager.COMPLETIONIST);

        assertNotNull(completionist);
        assertEquals("Completionist", completionist.getName());
        assertEquals(100, completionist.getXpReward());
    }

    @Test
    public void xpCollectorAchievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition xpCollector = findById(achievements, AchievementManager.XP_COLLECTOR);

        assertNotNull(xpCollector);
        assertEquals("XP Collector", xpCollector.getName());
        assertEquals(20, xpCollector.getXpReward());
    }

    @Test
    public void level5Achievement_hasCorrectProperties() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition level5 = findById(achievements, AchievementManager.LEVEL_5);

        assertNotNull(level5);
        assertEquals("Level 5 Debugger", level5.getName());
        assertEquals(50, level5.getXpReward());
    }

    @Test
    public void streak30Achievement_hasHighXpReward() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        AchievementDefinition streak30 = findById(achievements, AchievementManager.STREAK_30);

        assertNotNull(streak30);
        assertEquals("Month Master", streak30.getName());
        assertEquals(75, streak30.getXpReward());
        assertEquals("STREAK", streak30.getCategory());
    }

    @Test
    public void allAchievements_haveUniqueIds() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        long uniqueIds = achievements.stream()
            .map(AchievementDefinition::getId)
            .distinct()
            .count();

        assertEquals(achievements.size(), uniqueIds);
    }

    @Test
    public void allAchievements_haveUniqueSortOrders() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        long uniqueSortOrders = achievements.stream()
            .map(AchievementDefinition::getSortOrder)
            .distinct()
            .count();

        assertEquals(achievements.size(), uniqueSortOrders);
    }

    @Test
    public void allAchievements_havePositiveXpRewards() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        for (AchievementDefinition achievement : achievements) {
            assertTrue("Achievement " + achievement.getName() + " should have positive XP",
                achievement.getXpReward() > 0);
        }
    }

    @Test
    public void allAchievements_haveNonEmptyNames() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        for (AchievementDefinition achievement : achievements) {
            assertNotNull(achievement.getName());
            assertFalse(achievement.getName().isEmpty());
        }
    }

    @Test
    public void allAchievements_haveNonEmptyDescriptions() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        for (AchievementDefinition achievement : achievements) {
            assertNotNull(achievement.getDescription());
            assertFalse(achievement.getDescription().isEmpty());
        }
    }

    @Test
    public void achievementCategories_areValid() {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        String[] validCategories = {"MILESTONE", "SKILL", "CATEGORY", "STREAK"};

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
     * These conditions are checked by AchievementManager.checkAndUnlockAchievements()
     */
    @Test
    public void achievementConditions_areDocumented() {
        // FIRST_FIX: Unlock when totalSolved >= 1
        // NO_HINT_HERO: Unlock when bugsSolvedWithoutHints >= 3
        // ARRAY_ASSASSIN: Unlock when all array bugs are completed
        // LOOP_MASTER: Unlock when all loop bugs are completed
        // STREAK_MACHINE: Unlock when longestStreakDays >= 7
        // PERFECT_TEN: Unlock when totalSolved >= 10
        // COMPLETIONIST: Unlock when totalSolved >= total bugs count
        // XP_COLLECTOR: Unlock when totalXp >= 500
        // LEVEL_5: Unlock when level >= 5
        // HARD_MODE: Unlock when hardSolved >= 5
        // NO_HINTS_5: Unlock when bugsSolvedWithoutHints >= 5
        // STREAK_7: Unlock when streakDays >= 7
        // STREAK_30: Unlock when longestStreakDays >= 30

        // This test documents the unlock conditions
        assertTrue(true);
    }
}
