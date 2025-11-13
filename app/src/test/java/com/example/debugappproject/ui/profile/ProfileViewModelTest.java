package com.example.debugappproject.ui.profile;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ProfileViewModel XP and level calculation logic.
 */
public class ProfileViewModelTest {

    @Test
    public void calculateLevel_withZeroXp_returnsLevel1() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(1, viewModel.calculateLevel(0));
    }

    @Test
    public void calculateLevel_with50Xp_returnsLevel1() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(1, viewModel.calculateLevel(50));
    }

    @Test
    public void calculateLevel_with99Xp_returnsLevel1() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(1, viewModel.calculateLevel(99));
    }

    @Test
    public void calculateLevel_with100Xp_returnsLevel2() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(2, viewModel.calculateLevel(100));
    }

    @Test
    public void calculateLevel_with150Xp_returnsLevel2() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(2, viewModel.calculateLevel(150));
    }

    @Test
    public void calculateLevel_with200Xp_returnsLevel3() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(3, viewModel.calculateLevel(200));
    }

    @Test
    public void calculateLevel_with500Xp_returnsLevel6() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(6, viewModel.calculateLevel(500));
    }

    @Test
    public void calculateLevel_with999Xp_returnsLevel10() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(10, viewModel.calculateLevel(999));
    }

    @Test
    public void getXpProgressInLevel_withZeroXp_returns0() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(0, viewModel.getXpProgressInLevel(0));
    }

    @Test
    public void getXpProgressInLevel_with50Xp_returns50() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(50, viewModel.getXpProgressInLevel(50));
    }

    @Test
    public void getXpProgressInLevel_with99Xp_returns99() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(99, viewModel.getXpProgressInLevel(99));
    }

    @Test
    public void getXpProgressInLevel_with100Xp_returns0() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(0, viewModel.getXpProgressInLevel(100));
    }

    @Test
    public void getXpProgressInLevel_with150Xp_returns50() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(50, viewModel.getXpProgressInLevel(150));
    }

    @Test
    public void getXpProgressInLevel_with250Xp_returns50() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        assertEquals(50, viewModel.getXpProgressInLevel(250));
    }

    @Test
    public void getXpForNextLevel_withZeroXp_returns100() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        // Level 1, next level is 2, which requires 100 XP total
        assertEquals(100, viewModel.getXpForNextLevel(0));
    }

    @Test
    public void getXpForNextLevel_with50Xp_returns100() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        // Still level 1, next level is 2
        assertEquals(100, viewModel.getXpForNextLevel(50));
    }

    @Test
    public void getXpForNextLevel_with100Xp_returns200() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        // Level 2, next level is 3, which requires 200 XP total
        assertEquals(200, viewModel.getXpForNextLevel(100));
    }

    @Test
    public void getXpForNextLevel_with250Xp_returns300() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        // Level 3, next level is 4, which requires 300 XP total
        assertEquals(300, viewModel.getXpForNextLevel(250));
    }

    @Test
    public void getXpForNextLevel_with500Xp_returns600() {
        ProfileViewModel viewModel = new TestProfileViewModel();
        // Level 6, next level is 7, which requires 600 XP total
        assertEquals(600, viewModel.getXpForNextLevel(500));
    }

    /**
     * Test subclass that doesn't require Application context.
     * This allows us to test the pure calculation methods without mocking Android dependencies.
     */
    private static class TestProfileViewModel extends ProfileViewModel {
        public TestProfileViewModel() {
            super(null);
        }
    }
}
