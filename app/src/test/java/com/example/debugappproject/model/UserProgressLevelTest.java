package com.example.debugappproject.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserProgressLevelTest {

    @Test
    public void levelStartsAtOne() {
        UserProgress p = new UserProgress();
        assertEquals(1, p.getLevel());
    }

    @Test
    public void levelIncreasesEvery100Xp() {
        UserProgress p = new UserProgress();

        p.setXp(0);
        assertEquals(1, p.getLevel());

        p.setXp(99);
        assertEquals(1, p.getLevel());

        p.setXp(100);
        assertEquals(2, p.getLevel());

        p.setXp(250);
        assertEquals(3, p.getLevel());
    }

    @Test
    public void xpProgressInLevelIsModulo100() {
        UserProgress p = new UserProgress();

        p.setXp(0);
        assertEquals(0, p.getXpProgressInLevel());

        p.setXp(45);
        assertEquals(45, p.getXpProgressInLevel());

        p.setXp(145);
        assertEquals(45, p.getXpProgressInLevel());
    }
}