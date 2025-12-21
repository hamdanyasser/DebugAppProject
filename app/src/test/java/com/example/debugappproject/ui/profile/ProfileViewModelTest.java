package com.example.debugappproject.ui.profile;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ProfileViewModel XP and level calculation logic.
 * These methods are static and can be tested without Android dependencies.
 */
public class ProfileViewModelTest {

    @Test
    public void calculateLevel_withZeroXp_returnsLevel1() {
        assertEquals(1, ProfileViewModel.calculateLevel(0));
    }

    @Test
    public void calculateLevel_with50Xp_returnsLevel1() {
        assertEquals(1, ProfileViewModel.calculateLevel(50));
    }

    @Test
    public void calculateLevel_with99Xp_returnsLevel1() {
        assertEquals(1, ProfileViewModel.calculateLevel(99));
    }

    @Test
    public void calculateLevel_with100Xp_returnsLevel2() {
        assertEquals(2, ProfileViewModel.calculateLevel(100));
    }

    @Test
    public void calculateLevel_with150Xp_returnsLevel2() {
        assertEquals(2, ProfileViewModel.calculateLevel(150));
    }

    @Test
    public void calculateLevel_with200Xp_returnsLevel3() {
        assertEquals(3, ProfileViewModel.calculateLevel(200));
    }

    @Test
    public void calculateLevel_with500Xp_returnsLevel6() {
        assertEquals(6, ProfileViewModel.calculateLevel(500));
    }

    @Test
    public void calculateLevel_with999Xp_returnsLevel10() {
        assertEquals(10, ProfileViewModel.calculateLevel(999));
    }

    @Test
    public void getXpProgressInLevel_withZeroXp_returns0() {
        assertEquals(0, ProfileViewModel.getXpProgressInLevel(0));
    }

    @Test
    public void getXpProgressInLevel_with50Xp_returns50() {
        assertEquals(50, ProfileViewModel.getXpProgressInLevel(50));
    }

    @Test
    public void getXpProgressInLevel_with99Xp_returns99() {
        assertEquals(99, ProfileViewModel.getXpProgressInLevel(99));
    }

    @Test
    public void getXpProgressInLevel_with100Xp_returns0() {
        assertEquals(0, ProfileViewModel.getXpProgressInLevel(100));
    }

    @Test
    public void getXpProgressInLevel_with150Xp_returns50() {
        assertEquals(50, ProfileViewModel.getXpProgressInLevel(150));
    }

    @Test
    public void getXpProgressInLevel_with250Xp_returns50() {
        assertEquals(50, ProfileViewModel.getXpProgressInLevel(250));
    }

    @Test
    public void getXpForNextLevel_withZeroXp_returns100() {
        // Level 1, next level is 2, which requires 100 XP total
        assertEquals(100, ProfileViewModel.getXpForNextLevel(0));
    }

    @Test
    public void getXpForNextLevel_with50Xp_returns100() {
        // Still level 1, next level is 2
        assertEquals(100, ProfileViewModel.getXpForNextLevel(50));
    }

    @Test
    public void getXpForNextLevel_with100Xp_returns200() {
        // Level 2, next level is 3, which requires 200 XP total
        assertEquals(200, ProfileViewModel.getXpForNextLevel(100));
    }

    @Test
    public void getXpForNextLevel_with250Xp_returns300() {
        // Level 3, next level is 4, which requires 300 XP total
        assertEquals(300, ProfileViewModel.getXpForNextLevel(250));
    }

    @Test
    public void getXpForNextLevel_with500Xp_returns600() {
        // Level 6, next level is 7, which requires 600 XP total
        assertEquals(600, ProfileViewModel.getXpForNextLevel(500));
    }
}
