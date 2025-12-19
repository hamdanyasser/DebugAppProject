package com.example.debugappproject.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    PLAYER ADDICTION PROFILE                                  ║
 * ║         Identity Lock-In • Mental Stats • Streak Anxiety                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This system creates IDENTITY. Players must think:
 * "I am a Debugger. This is who I am."
 *
 * Features:
 * - Mental Evolution Stats (not generic XP)
 * - Streak System with real anxiety
 * - Titles that feel EARNED
 * - Visible mastery progression
 * - Fear of missing a day
 */
public class PlayerAddictionProfile {

    private static final String TAG = "PlayerAddictionProfile";
    private static final String PREFS_NAME = "addiction_profile";
    
    private static PlayerAddictionProfile instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         MENTAL STATS (The Core Identity)
    // ═══════════════════════════════════════════════════════════════════════════
    
    // These replace generic "XP" - they make players WATCH themselves evolve
    private int intuitionLevel = 1;           // Gut feeling for bugs
    private int patternSpeed = 1;             // How fast you spot patterns
    private int errorRecognition = 1;         // Identifying error types
    private int complexityTolerance = 1;      // Handling messy code
    private int focusEndurance = 1;           // Sustained attention
    private int debugIQ = 100;                // Overall "intelligence" score
    
    // Experience in each stat
    private int intuitionXP = 0;
    private int patternXP = 0;
    private int errorXP = 0;
    private int complexityXP = 0;
    private int focusXP = 0;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         STREAK SYSTEM (Anxiety Engine)
    // ═══════════════════════════════════════════════════════════════════════════
    
    private int currentStreak = 0;
    private int longestStreak = 0;
    private long lastPlayDate = 0;
    private int streakFreezes = 0;            // Paid streak protection
    private boolean streakAtRisk = false;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         TITLES & IDENTITY
    // ═══════════════════════════════════════════════════════════════════════════
    
    private String currentTitle = "Novice";
    private String currentBadge = "🔰";
    private List<String> unlockedTitles = new ArrayList<>();
    private List<String> unlockedBadges = new ArrayList<>();
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         PERFORMANCE TRACKING
    // ═══════════════════════════════════════════════════════════════════════════
    
    private int totalBugsFixed = 0;
    private int perfectSolves = 0;            // No hints used
    private int speedSolves = 0;              // Under 30 seconds
    private int clutchWins = 0;               // Won with <10 seconds left
    private int comebackWins = 0;             // Won after being behind
    private int dominationWins = 0;           // Won in under 60 seconds
    private float averageSolveTime = 0;
    private int fastestSolve = Integer.MAX_VALUE;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         ELO & RANKING
    // ═══════════════════════════════════════════════════════════════════════════
    
    private int elo = 1000;
    private int peakElo = 1000;
    private int seasonPeakElo = 1000;
    private int rankedWins = 0;
    private int rankedLosses = 0;
    private int winStreak = 0;
    private int lossStreak = 0;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    private PlayerAddictionProfile(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadProfile();
        checkStreakStatus();
    }
    
    public static synchronized PlayerAddictionProfile getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerAddictionProfile(context.getApplicationContext());
        }
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         MENTAL STAT PROGRESSION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Award XP to mental stats based on performance
     */
    public MentalGrowthResult recordPerformance(PerformanceData data) {
        MentalGrowthResult result = new MentalGrowthResult();
        
        // Intuition XP - Awarded for solving without hints
        if (!data.usedHint) {
            int intuitionGain = 10 + (data.difficulty * 5);
            intuitionXP += intuitionGain;
            result.intuitionGain = intuitionGain;
        }
        
        // Pattern Speed XP - Awarded for fast solves
        if (data.solveTimeSeconds < 60) {
            int patternGain = (60 - data.solveTimeSeconds) / 2;
            patternXP += patternGain;
            result.patternGain = patternGain;
        }
        
        // Error Recognition XP - Awarded for first-try correct
        if (data.attemptsBeforeCorrect == 1) {
            int errorGain = 15 + (data.difficulty * 3);
            errorXP += errorGain;
            result.errorGain = errorGain;
        }
        
        // Complexity Tolerance XP - Awarded for hard bugs
        if (data.difficulty >= 3) {
            int complexityGain = data.difficulty * 10;
            complexityXP += complexityGain;
            result.complexityGain = complexityGain;
        }
        
        // Focus Endurance XP - Awarded for long sessions
        if (data.sessionMinutes >= 10) {
            int focusGain = data.sessionMinutes;
            focusXP += focusGain;
            result.focusGain = focusGain;
        }
        
        // Check for level ups
        result.intuitionLevelUp = checkLevelUp("intuition");
        result.patternLevelUp = checkLevelUp("pattern");
        result.errorLevelUp = checkLevelUp("error");
        result.complexityLevelUp = checkLevelUp("complexity");
        result.focusLevelUp = checkLevelUp("focus");
        
        // Recalculate Debug IQ
        int oldIQ = debugIQ;
        calculateDebugIQ();
        result.iqChange = debugIQ - oldIQ;
        
        // Update totals
        totalBugsFixed++;
        if (!data.usedHint) perfectSolves++;
        if (data.solveTimeSeconds < 30) speedSolves++;
        if (data.clutchWin) clutchWins++;
        if (data.comebackWin) comebackWins++;
        if (data.solveTimeSeconds < 60 && data.won) dominationWins++;
        
        // Update average solve time
        averageSolveTime = ((averageSolveTime * (totalBugsFixed - 1)) + data.solveTimeSeconds) / totalBugsFixed;
        if (data.solveTimeSeconds < fastestSolve) {
            fastestSolve = data.solveTimeSeconds;
            result.newRecord = true;
        }
        
        // Check for new titles
        result.newTitle = checkTitleUnlock();
        
        saveProfile();
        return result;
    }
    
    private boolean checkLevelUp(String stat) {
        int xpNeeded = 0;
        int currentXP = 0;
        int currentLevel = 0;
        
        switch (stat) {
            case "intuition":
                currentXP = intuitionXP;
                currentLevel = intuitionLevel;
                break;
            case "pattern":
                currentXP = patternXP;
                currentLevel = patternSpeed;
                break;
            case "error":
                currentXP = errorXP;
                currentLevel = errorRecognition;
                break;
            case "complexity":
                currentXP = complexityXP;
                currentLevel = complexityTolerance;
                break;
            case "focus":
                currentXP = focusXP;
                currentLevel = focusEndurance;
                break;
        }
        
        // XP needed = level * 100
        xpNeeded = currentLevel * 100;
        
        if (currentXP >= xpNeeded) {
            // Level up!
            switch (stat) {
                case "intuition":
                    intuitionLevel++;
                    intuitionXP -= xpNeeded;
                    break;
                case "pattern":
                    patternSpeed++;
                    patternXP -= xpNeeded;
                    break;
                case "error":
                    errorRecognition++;
                    errorXP -= xpNeeded;
                    break;
                case "complexity":
                    complexityTolerance++;
                    complexityXP -= xpNeeded;
                    break;
                case "focus":
                    focusEndurance++;
                    focusXP -= xpNeeded;
                    break;
            }
            return true;
        }
        return false;
    }
    
    private void calculateDebugIQ() {
        // Debug IQ = weighted average of all mental stats, scaled to 100-200 range
        float rawScore = (intuitionLevel * 1.2f) + 
                         (patternSpeed * 1.3f) + 
                         (errorRecognition * 1.1f) + 
                         (complexityTolerance * 1.4f) + 
                         (focusEndurance * 1.0f);
        
        // Base 100, max ~200 at all level 20
        debugIQ = 100 + (int)(rawScore * 2);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         STREAK SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Record a play session - updates streak
     */
    public StreakResult recordSession() {
        long today = getStartOfDay(System.currentTimeMillis());
        long lastPlay = getStartOfDay(lastPlayDate);
        
        StreakResult result = new StreakResult();
        result.previousStreak = currentStreak;
        
        if (lastPlayDate == 0) {
            // First ever session
            currentStreak = 1;
            result.isFirstSession = true;
        } else if (today == lastPlay) {
            // Already played today
            result.alreadyPlayedToday = true;
        } else if (today - lastPlay == 24 * 60 * 60 * 1000L) {
            // Played yesterday - streak continues!
            currentStreak++;
            result.streakContinued = true;
        } else if (today - lastPlay > 24 * 60 * 60 * 1000L) {
            // Missed a day - streak broken!
            if (streakFreezes > 0) {
                streakFreezes--;
                result.usedFreeze = true;
                result.freezesRemaining = streakFreezes;
            } else {
                result.streakLost = true;
                result.lostStreakValue = currentStreak;
                currentStreak = 1;
            }
        }
        
        lastPlayDate = System.currentTimeMillis();
        
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
            result.newRecord = true;
        }
        
        result.currentStreak = currentStreak;
        result.streakReward = calculateStreakReward(currentStreak);
        result.milestone = getStreakMilestone(currentStreak);
        
        saveProfile();
        return result;
    }
    
    private void checkStreakStatus() {
        long today = getStartOfDay(System.currentTimeMillis());
        long lastPlay = getStartOfDay(lastPlayDate);
        
        // If it's been more than 1 day, streak is at risk
        if (lastPlayDate > 0 && today - lastPlay >= 24 * 60 * 60 * 1000L) {
            streakAtRisk = true;
        }
    }
    
    private long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private int calculateStreakReward(int streak) {
        // Escalating rewards to create FOMO
        if (streak >= 365) return 1000;
        if (streak >= 100) return 500;
        if (streak >= 30) return 200;
        if (streak >= 14) return 100;
        if (streak >= 7) return 50;
        if (streak >= 3) return 25;
        return 10;
    }
    
    private String getStreakMilestone(int streak) {
        if (streak == 365) return "🏆 ONE YEAR STREAK!";
        if (streak == 100) return "💯 100 DAYS!";
        if (streak == 50) return "🔥 50 DAY INFERNO!";
        if (streak == 30) return "📅 30 DAYS STRONG!";
        if (streak == 14) return "⚡ 2 WEEK WARRIOR!";
        if (streak == 7) return "🌟 WEEK ONE COMPLETE!";
        if (streak == 3) return "🚀 3 DAY STREAK!";
        return null;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         TITLES & IDENTITY
    // ═══════════════════════════════════════════════════════════════════════════
    
    private String checkTitleUnlock() {
        String newTitle = null;
        
        // Performance-based titles
        if (totalBugsFixed >= 1000 && !unlockedTitles.contains("Bug Slayer")) {
            newTitle = "Bug Slayer";
            unlockedTitles.add(newTitle);
        }
        if (perfectSolves >= 100 && !unlockedTitles.contains("Perfect Mind")) {
            newTitle = "Perfect Mind";
            unlockedTitles.add(newTitle);
        }
        if (speedSolves >= 50 && !unlockedTitles.contains("Speed Demon")) {
            newTitle = "Speed Demon";
            unlockedTitles.add(newTitle);
        }
        if (clutchWins >= 25 && !unlockedTitles.contains("Clutch Master")) {
            newTitle = "Clutch Master";
            unlockedTitles.add(newTitle);
        }
        if (comebackWins >= 10 && !unlockedTitles.contains("Comeback King")) {
            newTitle = "Comeback King";
            unlockedTitles.add(newTitle);
        }
        
        // Streak-based titles
        if (currentStreak >= 30 && !unlockedTitles.contains("Dedicated")) {
            newTitle = "Dedicated";
            unlockedTitles.add(newTitle);
        }
        if (currentStreak >= 100 && !unlockedTitles.contains("Obsessed")) {
            newTitle = "Obsessed";
            unlockedTitles.add(newTitle);
        }
        
        // Skill-based titles
        if (debugIQ >= 150 && !unlockedTitles.contains("Genius")) {
            newTitle = "Genius";
            unlockedTitles.add(newTitle);
        }
        if (intuitionLevel >= 10 && !unlockedTitles.contains("Bug Whisperer")) {
            newTitle = "Bug Whisperer";
            unlockedTitles.add(newTitle);
        }
        if (patternSpeed >= 10 && !unlockedTitles.contains("Pattern Master")) {
            newTitle = "Pattern Master";
            unlockedTitles.add(newTitle);
        }
        
        // Rank-based titles
        if (elo >= 2000 && !unlockedTitles.contains("Legend")) {
            newTitle = "Legend";
            unlockedTitles.add(newTitle);
        }
        if (elo >= 1800 && !unlockedTitles.contains("Master")) {
            newTitle = "Master";
            unlockedTitles.add(newTitle);
        }
        
        return newTitle;
    }
    
    public void setTitle(String title) {
        if (unlockedTitles.contains(title)) {
            currentTitle = title;
            currentBadge = getTitleBadge(title);
            saveProfile();
        }
    }
    
    private String getTitleBadge(String title) {
        switch (title) {
            case "Bug Slayer": return "🗡️";
            case "Perfect Mind": return "🧠";
            case "Speed Demon": return "⚡";
            case "Clutch Master": return "🎯";
            case "Comeback King": return "👑";
            case "Dedicated": return "💪";
            case "Obsessed": return "🔥";
            case "Genius": return "🎓";
            case "Bug Whisperer": return "🔮";
            case "Pattern Master": return "🔍";
            case "Legend": return "🏆";
            case "Master": return "⭐";
            default: return "🔰";
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         RANKED BATTLE UPDATES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public RankedUpdateResult updateRankedResult(boolean won, int opponentElo, 
                                                  boolean perfectWin, float timeAdvantage) {
        RankedBattleSystem.EloChangeResult eloResult = RankedBattleSystem.calculateEloChange(
            elo, opponentElo, won, perfectWin, timeAdvantage);
        
        RankedBattleSystem.RankTier oldRank = RankedBattleSystem.RankTier.fromElo(elo);
        
        elo += eloResult.totalChange;
        if (elo < 0) elo = 0;
        
        RankedBattleSystem.RankTier newRank = RankedBattleSystem.RankTier.fromElo(elo);
        
        if (elo > peakElo) peakElo = elo;
        if (elo > seasonPeakElo) seasonPeakElo = elo;
        
        if (won) {
            rankedWins++;
            winStreak++;
            lossStreak = 0;
        } else {
            rankedLosses++;
            lossStreak++;
            winStreak = 0;
        }
        
        saveProfile();
        
        return new RankedUpdateResult(
            elo,
            eloResult.totalChange,
            oldRank,
            newRank,
            oldRank != newRank,
            newRank.tier > oldRank.tier,
            winStreak,
            lossStreak,
            eloResult.bonusReason
        );
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         VIRAL MOMENTS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public ShareableVictory generateShareableVictory(PerformanceData data) {
        ShareableVictory victory = new ShareableVictory();
        
        victory.headline = generateHeadline(data);
        victory.stats = generateStatBlock(data);
        victory.rarity = calculateRarity(data);
        victory.visualStyle = getVisualStyle(data);
        
        return victory;
    }
    
    private String generateHeadline(PerformanceData data) {
        if (data.solveTimeSeconds < 10) return "⚡ INSTANT FIX IN " + data.solveTimeSeconds + "s!";
        if (data.clutchWin) return "🎯 CLUTCH WIN WITH " + data.timeRemaining + "s LEFT!";
        if (data.comebackWin) return "👑 IMPOSSIBLE COMEBACK!";
        if (!data.usedHint && data.attemptsBeforeCorrect == 1) return "🧠 FIRST TRY PERFECT!";
        if (data.difficulty >= 4) return "🔥 CRUSHED A LEGENDARY BUG!";
        return "✅ BUG DESTROYED!";
    }
    
    private String generateStatBlock(PerformanceData data) {
        return String.format(
            "Time: %ds | Rank: %s | IQ: %d | Streak: 🔥%d",
            data.solveTimeSeconds,
            RankedBattleSystem.RankTier.fromElo(elo).emoji + RankedBattleSystem.RankTier.fromElo(elo).name,
            debugIQ,
            currentStreak
        );
    }
    
    private String calculateRarity(PerformanceData data) {
        if (data.solveTimeSeconds < 10 && !data.usedHint) return "Only 0.1% solve this fast!";
        if (data.clutchWin) return "Only 2% win this close!";
        if (data.difficulty >= 4 && !data.usedHint) return "Only 5% solve this without hints!";
        if (data.attemptsBeforeCorrect == 1) return "Only 15% get it first try!";
        return "Top solver!";
    }
    
    private String getVisualStyle(PerformanceData data) {
        if (data.solveTimeSeconds < 10) return "LIGHTNING";
        if (data.clutchWin) return "DRAMATIC";
        if (data.comebackWin) return "EPIC";
        if (elo >= 1800) return "MASTER";
        return "STANDARD";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         PERSISTENCE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void loadProfile() {
        intuitionLevel = prefs.getInt("intuitionLevel", 1);
        patternSpeed = prefs.getInt("patternSpeed", 1);
        errorRecognition = prefs.getInt("errorRecognition", 1);
        complexityTolerance = prefs.getInt("complexityTolerance", 1);
        focusEndurance = prefs.getInt("focusEndurance", 1);
        debugIQ = prefs.getInt("debugIQ", 100);
        
        intuitionXP = prefs.getInt("intuitionXP", 0);
        patternXP = prefs.getInt("patternXP", 0);
        errorXP = prefs.getInt("errorXP", 0);
        complexityXP = prefs.getInt("complexityXP", 0);
        focusXP = prefs.getInt("focusXP", 0);
        
        currentStreak = prefs.getInt("currentStreak", 0);
        longestStreak = prefs.getInt("longestStreak", 0);
        lastPlayDate = prefs.getLong("lastPlayDate", 0);
        streakFreezes = prefs.getInt("streakFreezes", 0);
        
        currentTitle = prefs.getString("currentTitle", "Novice");
        currentBadge = prefs.getString("currentBadge", "🔰");
        
        String titlesJson = prefs.getString("unlockedTitles", "[]");
        Type listType = new TypeToken<List<String>>(){}.getType();
        unlockedTitles = gson.fromJson(titlesJson, listType);
        if (unlockedTitles == null) unlockedTitles = new ArrayList<>();
        
        totalBugsFixed = prefs.getInt("totalBugsFixed", 0);
        perfectSolves = prefs.getInt("perfectSolves", 0);
        speedSolves = prefs.getInt("speedSolves", 0);
        clutchWins = prefs.getInt("clutchWins", 0);
        comebackWins = prefs.getInt("comebackWins", 0);
        dominationWins = prefs.getInt("dominationWins", 0);
        averageSolveTime = prefs.getFloat("averageSolveTime", 0);
        fastestSolve = prefs.getInt("fastestSolve", Integer.MAX_VALUE);
        
        elo = prefs.getInt("elo", 1000);
        peakElo = prefs.getInt("peakElo", 1000);
        seasonPeakElo = prefs.getInt("seasonPeakElo", 1000);
        rankedWins = prefs.getInt("rankedWins", 0);
        rankedLosses = prefs.getInt("rankedLosses", 0);
        winStreak = prefs.getInt("winStreak", 0);
        lossStreak = prefs.getInt("lossStreak", 0);
    }
    
    private void saveProfile() {
        prefs.edit()
            .putInt("intuitionLevel", intuitionLevel)
            .putInt("patternSpeed", patternSpeed)
            .putInt("errorRecognition", errorRecognition)
            .putInt("complexityTolerance", complexityTolerance)
            .putInt("focusEndurance", focusEndurance)
            .putInt("debugIQ", debugIQ)
            .putInt("intuitionXP", intuitionXP)
            .putInt("patternXP", patternXP)
            .putInt("errorXP", errorXP)
            .putInt("complexityXP", complexityXP)
            .putInt("focusXP", focusXP)
            .putInt("currentStreak", currentStreak)
            .putInt("longestStreak", longestStreak)
            .putLong("lastPlayDate", lastPlayDate)
            .putInt("streakFreezes", streakFreezes)
            .putString("currentTitle", currentTitle)
            .putString("currentBadge", currentBadge)
            .putString("unlockedTitles", gson.toJson(unlockedTitles))
            .putInt("totalBugsFixed", totalBugsFixed)
            .putInt("perfectSolves", perfectSolves)
            .putInt("speedSolves", speedSolves)
            .putInt("clutchWins", clutchWins)
            .putInt("comebackWins", comebackWins)
            .putInt("dominationWins", dominationWins)
            .putFloat("averageSolveTime", averageSolveTime)
            .putInt("fastestSolve", fastestSolve)
            .putInt("elo", elo)
            .putInt("peakElo", peakElo)
            .putInt("seasonPeakElo", seasonPeakElo)
            .putInt("rankedWins", rankedWins)
            .putInt("rankedLosses", rankedLosses)
            .putInt("winStreak", winStreak)
            .putInt("lossStreak", lossStreak)
            .apply();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         GETTERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public int getIntuitionLevel() { return intuitionLevel; }
    public int getPatternSpeed() { return patternSpeed; }
    public int getErrorRecognition() { return errorRecognition; }
    public int getComplexityTolerance() { return complexityTolerance; }
    public int getFocusEndurance() { return focusEndurance; }
    public int getDebugIQ() { return debugIQ; }
    public int getCurrentStreak() { return currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public boolean isStreakAtRisk() { return streakAtRisk; }
    public String getCurrentTitle() { return currentTitle; }
    public String getCurrentBadge() { return currentBadge; }
    public List<String> getUnlockedTitles() { return unlockedTitles; }
    public int getTotalBugsFixed() { return totalBugsFixed; }
    public int getElo() { return elo; }
    public int getPeakElo() { return peakElo; }
    public int getRankedWins() { return rankedWins; }
    public int getRankedLosses() { return rankedLosses; }
    public int getWinStreak() { return winStreak; }
    public int getLossStreak() { return lossStreak; }
    
    public int getXPForStat(String stat) {
        switch (stat) {
            case "intuition": return intuitionXP;
            case "pattern": return patternXP;
            case "error": return errorXP;
            case "complexity": return complexityXP;
            case "focus": return focusXP;
            default: return 0;
        }
    }
    
    public int getXPNeededForStat(String stat) {
        switch (stat) {
            case "intuition": return intuitionLevel * 100;
            case "pattern": return patternSpeed * 100;
            case "error": return errorRecognition * 100;
            case "complexity": return complexityTolerance * 100;
            case "focus": return focusEndurance * 100;
            default: return 100;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static class PerformanceData {
        public int solveTimeSeconds;
        public int difficulty; // 1-5
        public boolean usedHint;
        public int attemptsBeforeCorrect;
        public int sessionMinutes;
        public boolean won;
        public boolean clutchWin;
        public boolean comebackWin;
        public int timeRemaining;
    }
    
    public static class MentalGrowthResult {
        public int intuitionGain = 0;
        public int patternGain = 0;
        public int errorGain = 0;
        public int complexityGain = 0;
        public int focusGain = 0;
        public boolean intuitionLevelUp = false;
        public boolean patternLevelUp = false;
        public boolean errorLevelUp = false;
        public boolean complexityLevelUp = false;
        public boolean focusLevelUp = false;
        public int iqChange = 0;
        public boolean newRecord = false;
        public String newTitle = null;
        
        public boolean hasLevelUp() {
            return intuitionLevelUp || patternLevelUp || errorLevelUp || 
                   complexityLevelUp || focusLevelUp;
        }
    }
    
    public static class StreakResult {
        public int currentStreak;
        public int previousStreak;
        public boolean isFirstSession = false;
        public boolean alreadyPlayedToday = false;
        public boolean streakContinued = false;
        public boolean streakLost = false;
        public int lostStreakValue = 0;
        public boolean usedFreeze = false;
        public int freezesRemaining = 0;
        public boolean newRecord = false;
        public int streakReward = 0;
        public String milestone = null;
    }
    
    public static class RankedUpdateResult {
        public int newElo;
        public int eloChange;
        public RankedBattleSystem.RankTier oldRank;
        public RankedBattleSystem.RankTier newRank;
        public boolean rankChanged;
        public boolean promoted;
        public int winStreak;
        public int lossStreak;
        public String bonusReason;
        
        public RankedUpdateResult(int newElo, int eloChange, 
                                   RankedBattleSystem.RankTier oldRank,
                                   RankedBattleSystem.RankTier newRank,
                                   boolean rankChanged, boolean promoted,
                                   int winStreak, int lossStreak,
                                   String bonusReason) {
            this.newElo = newElo;
            this.eloChange = eloChange;
            this.oldRank = oldRank;
            this.newRank = newRank;
            this.rankChanged = rankChanged;
            this.promoted = promoted;
            this.winStreak = winStreak;
            this.lossStreak = lossStreak;
            this.bonusReason = bonusReason;
        }
    }
    
    public static class ShareableVictory {
        public String headline;
        public String stats;
        public String rarity;
        public String visualStyle;
    }
}
