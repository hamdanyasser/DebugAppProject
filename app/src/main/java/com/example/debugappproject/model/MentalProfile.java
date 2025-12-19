package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    MENTAL EVOLUTION PROFILE                                  ║
 * ║         The Core Identity System - Players Watch Themselves Transform        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This is NOT a level system. This is a COGNITIVE FINGERPRINT.
 * Players see exactly which mental abilities they're developing.
 *
 * Each skill:
 * - Starts at 0, caps at 1000 (mastery)
 * - Has visible milestones (100, 250, 500, 750, 1000)
 * - Affects matchmaking & challenge difficulty
 * - Creates unique player identity signatures
 */
@Entity(tableName = "mental_profile")
public class MentalProfile {

    @PrimaryKey
    private int id = 1; // Singleton

    // ═══════════════════════════════════════════════════════════════════════════
    //                         CORE COGNITIVE SKILLS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Pattern Recognition - Ability to spot recurring code patterns */
    private int patternRecognition = 0;

    /** Error Intuition - "Gut feeling" for where bugs hide */
    private int errorIntuition = 0;

    /** Logic Flow - Understanding control flow & state */
    private int logicFlow = 0;

    /** Speed Debugging - How fast you diagnose under pressure */
    private int speedDebugging = 0;

    /** Complexity Tolerance - Handling nested/messy code */
    private int complexityTolerance = 0;

    /** Focus Endurance - Maintaining attention on long problems */
    private int focusEndurance = 0;

    /** Risk Assessment - Predicting side effects of changes */
    private int riskAssessment = 0;

    /** Code Memory - Remembering code structure while solving */
    private int codeMemory = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SPECIALTY SKILLS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Null Hunter - Expertise in null reference bugs */
    private int nullHunter = 0;

    /** Loop Master - Expertise in iteration/recursion bugs */
    private int loopMaster = 0;

    /** Type Wrangler - Expertise in type casting/conversion bugs */
    private int typeWrangler = 0;

    /** Boundary Expert - Off-by-one and edge case mastery */
    private int boundaryExpert = 0;

    /** Concurrency Sage - Thread safety and race condition mastery */
    private int concurrencySage = 0;

    /** Memory Architect - Memory leak and allocation mastery */
    private int memoryArchitect = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    //                         META STATISTICS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Total experience points (for backwards compatibility) */
    private int totalXp = 0;

    /** Bugs solved lifetime */
    private int bugsSolved = 0;

    /** Perfect solves (no hints, first try) */
    private int perfectSolves = 0;

    /** Average solve time in seconds */
    private int avgSolveTimeSeconds = 0;

    /** Current win streak in battles */
    private int battleWinStreak = 0;

    /** Longest battle win streak ever */
    private int longestBattleStreak = 0;

    /** Elo rating for matchmaking (starts at 1000) */
    private int eloRating = 1000;

    /** Peak Elo ever achieved */
    private int peakElo = 1000;

    /** Total battles played */
    private int battlesPlayed = 0;

    /** Battle wins */
    private int battleWins = 0;

    /** Current ranked tier (0=Unranked, 1=Bronze, 2=Silver, 3=Gold, 4=Diamond, 5=Master, 6=Legend) */
    private int rankedTier = 0;

    /** Ranked points within current tier (0-100) */
    private int rankedPoints = 0;

    /** Number of near-misses (almost solved) - feeds addiction */
    private int nearMisses = 0;

    /** Last skill improved - for UI highlighting */
    private String lastSkillImproved = "";

    /** Last skill improvement amount */
    private int lastSkillGain = 0;

    /** Timestamp of last activity */
    private long lastActivityTimestamp = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    //                         CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════════════════

    public MentalProfile() {}

    @Ignore
    public MentalProfile(int initialElo) {
        this.eloRating = initialElo;
        this.peakElo = initialElo;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SKILL CALCULATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate overall "Mental Level" from all skills
     * This is the visible number players identify with
     */
    @Ignore
    public int getMentalLevel() {
        int totalSkillPoints = patternRecognition + errorIntuition + logicFlow +
                speedDebugging + complexityTolerance + focusEndurance +
                riskAssessment + codeMemory + nullHunter + loopMaster +
                typeWrangler + boundaryExpert + concurrencySage + memoryArchitect;

        // Level formula: Exponential curve so early gains feel fast
        // Level 1 = 0-99, Level 2 = 100-299, Level 3 = 300-599, etc.
        if (totalSkillPoints < 100) return 1;
        if (totalSkillPoints < 300) return 2;
        if (totalSkillPoints < 600) return 3;
        if (totalSkillPoints < 1000) return 4;
        if (totalSkillPoints < 1500) return 5;
        if (totalSkillPoints < 2200) return 6;
        if (totalSkillPoints < 3000) return 7;
        if (totalSkillPoints < 4000) return 8;
        if (totalSkillPoints < 5200) return 9;
        if (totalSkillPoints < 6600) return 10;
        if (totalSkillPoints < 8200) return 11;
        if (totalSkillPoints < 10000) return 12;
        return 13 + (totalSkillPoints - 10000) / 2000; // Slower after 12
    }

    /**
     * Get total skill points across all skills
     */
    @Ignore
    public int getTotalSkillPoints() {
        return patternRecognition + errorIntuition + logicFlow +
                speedDebugging + complexityTolerance + focusEndurance +
                riskAssessment + codeMemory + nullHunter + loopMaster +
                typeWrangler + boundaryExpert + concurrencySage + memoryArchitect;
    }

    /**
     * Get the player's strongest skill name
     */
    @Ignore
    public String getStrongestSkill() {
        int max = 0;
        String strongest = "Pattern Recognition";

        if (patternRecognition > max) { max = patternRecognition; strongest = "Pattern Recognition"; }
        if (errorIntuition > max) { max = errorIntuition; strongest = "Error Intuition"; }
        if (logicFlow > max) { max = logicFlow; strongest = "Logic Flow"; }
        if (speedDebugging > max) { max = speedDebugging; strongest = "Speed Debugging"; }
        if (complexityTolerance > max) { max = complexityTolerance; strongest = "Complexity Tolerance"; }
        if (focusEndurance > max) { max = focusEndurance; strongest = "Focus Endurance"; }
        if (riskAssessment > max) { max = riskAssessment; strongest = "Risk Assessment"; }
        if (codeMemory > max) { max = codeMemory; strongest = "Code Memory"; }
        if (nullHunter > max) { max = nullHunter; strongest = "Null Hunter"; }
        if (loopMaster > max) { max = loopMaster; strongest = "Loop Master"; }
        if (typeWrangler > max) { max = typeWrangler; strongest = "Type Wrangler"; }
        if (boundaryExpert > max) { max = boundaryExpert; strongest = "Boundary Expert"; }
        if (concurrencySage > max) { max = concurrencySage; strongest = "Concurrency Sage"; }
        if (memoryArchitect > max) { max = memoryArchitect; strongest = "Memory Architect"; }

        return strongest;
    }

    /**
     * Get the player's weakest skill name (for training recommendations)
     */
    @Ignore
    public String getWeakestSkill() {
        int min = Integer.MAX_VALUE;
        String weakest = "Pattern Recognition";

        if (patternRecognition < min) { min = patternRecognition; weakest = "Pattern Recognition"; }
        if (errorIntuition < min) { min = errorIntuition; weakest = "Error Intuition"; }
        if (logicFlow < min) { min = logicFlow; weakest = "Logic Flow"; }
        if (speedDebugging < min) { min = speedDebugging; weakest = "Speed Debugging"; }
        if (complexityTolerance < min) { min = complexityTolerance; weakest = "Complexity Tolerance"; }
        if (focusEndurance < min) { min = focusEndurance; weakest = "Focus Endurance"; }
        if (riskAssessment < min) { min = riskAssessment; weakest = "Risk Assessment"; }
        if (codeMemory < min) { min = codeMemory; weakest = "Code Memory"; }

        return weakest;
    }

    /**
     * Get skill milestone name (Novice, Apprentice, Journeyman, Expert, Master)
     */
    @Ignore
    public static String getSkillMilestone(int skillValue) {
        if (skillValue < 100) return "Novice";
        if (skillValue < 250) return "Apprentice";
        if (skillValue < 500) return "Journeyman";
        if (skillValue < 750) return "Expert";
        if (skillValue < 1000) return "Master";
        return "Grandmaster";
    }

    /**
     * Get progress to next milestone (0-100%)
     */
    @Ignore
    public static int getSkillMilestoneProgress(int skillValue) {
        if (skillValue < 100) return (skillValue * 100) / 100;
        if (skillValue < 250) return ((skillValue - 100) * 100) / 150;
        if (skillValue < 500) return ((skillValue - 250) * 100) / 250;
        if (skillValue < 750) return ((skillValue - 500) * 100) / 250;
        if (skillValue < 1000) return ((skillValue - 750) * 100) / 250;
        return 100;
    }

    /**
     * Get ranked tier name
     */
    @Ignore
    public String getRankedTierName() {
        switch (rankedTier) {
            case 0: return "Unranked";
            case 1: return "Bronze";
            case 2: return "Silver";
            case 3: return "Gold";
            case 4: return "Diamond";
            case 5: return "Master";
            case 6: return "Legend";
            default: return "Unknown";
        }
    }

    /**
     * Calculate Elo change for a match result
     * @param won Whether player won
     * @param opponentElo Opponent's Elo rating
     * @return Change in Elo (positive or negative)
     */
    @Ignore
    public int calculateEloChange(boolean won, int opponentElo) {
        // K-factor varies by rating (more volatile at lower ratings)
        int kFactor = eloRating < 1200 ? 40 : (eloRating < 1600 ? 32 : 24);

        // Expected score
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentElo - eloRating) / 400.0));

        // Actual score
        double actual = won ? 1.0 : 0.0;

        // Elo change
        return (int) Math.round(kFactor * (actual - expected));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         SKILL IMPROVEMENT METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Improve a skill with diminishing returns at high levels
     * @param currentValue Current skill value
     * @param baseGain Base amount to gain
     * @return New skill value (capped at 1000)
     */
    @Ignore
    private int improveSkill(int currentValue, int baseGain) {
        // Diminishing returns formula
        double multiplier = 1.0 - (currentValue / 1200.0); // Slower as you approach 1000
        int actualGain = Math.max(1, (int)(baseGain * multiplier));
        return Math.min(1000, currentValue + actualGain);
    }

    public void improvePatternRecognition(int gain) {
        int oldValue = patternRecognition;
        patternRecognition = improveSkill(patternRecognition, gain);
        if (patternRecognition > oldValue) {
            lastSkillImproved = "Pattern Recognition";
            lastSkillGain = patternRecognition - oldValue;
        }
    }

    public void improveErrorIntuition(int gain) {
        int oldValue = errorIntuition;
        errorIntuition = improveSkill(errorIntuition, gain);
        if (errorIntuition > oldValue) {
            lastSkillImproved = "Error Intuition";
            lastSkillGain = errorIntuition - oldValue;
        }
    }

    public void improveLogicFlow(int gain) {
        int oldValue = logicFlow;
        logicFlow = improveSkill(logicFlow, gain);
        if (logicFlow > oldValue) {
            lastSkillImproved = "Logic Flow";
            lastSkillGain = logicFlow - oldValue;
        }
    }

    public void improveSpeedDebugging(int gain) {
        int oldValue = speedDebugging;
        speedDebugging = improveSkill(speedDebugging, gain);
        if (speedDebugging > oldValue) {
            lastSkillImproved = "Speed Debugging";
            lastSkillGain = speedDebugging - oldValue;
        }
    }

    public void improveComplexityTolerance(int gain) {
        int oldValue = complexityTolerance;
        complexityTolerance = improveSkill(complexityTolerance, gain);
        if (complexityTolerance > oldValue) {
            lastSkillImproved = "Complexity Tolerance";
            lastSkillGain = complexityTolerance - oldValue;
        }
    }

    public void improveFocusEndurance(int gain) {
        int oldValue = focusEndurance;
        focusEndurance = improveSkill(focusEndurance, gain);
        if (focusEndurance > oldValue) {
            lastSkillImproved = "Focus Endurance";
            lastSkillGain = focusEndurance - oldValue;
        }
    }

    public void improveRiskAssessment(int gain) {
        int oldValue = riskAssessment;
        riskAssessment = improveSkill(riskAssessment, gain);
        if (riskAssessment > oldValue) {
            lastSkillImproved = "Risk Assessment";
            lastSkillGain = riskAssessment - oldValue;
        }
    }

    public void improveCodeMemory(int gain) {
        int oldValue = codeMemory;
        codeMemory = improveSkill(codeMemory, gain);
        if (codeMemory > oldValue) {
            lastSkillImproved = "Code Memory";
            lastSkillGain = codeMemory - oldValue;
        }
    }

    // Specialty skill improvements
    public void improveNullHunter(int gain) {
        int oldValue = nullHunter;
        nullHunter = improveSkill(nullHunter, gain);
        if (nullHunter > oldValue) {
            lastSkillImproved = "Null Hunter";
            lastSkillGain = nullHunter - oldValue;
        }
    }

    public void improveLoopMaster(int gain) {
        int oldValue = loopMaster;
        loopMaster = improveSkill(loopMaster, gain);
        if (loopMaster > oldValue) {
            lastSkillImproved = "Loop Master";
            lastSkillGain = loopMaster - oldValue;
        }
    }

    public void improveTypeWrangler(int gain) {
        int oldValue = typeWrangler;
        typeWrangler = improveSkill(typeWrangler, gain);
        if (typeWrangler > oldValue) {
            lastSkillImproved = "Type Wrangler";
            lastSkillGain = typeWrangler - oldValue;
        }
    }

    public void improveBoundaryExpert(int gain) {
        int oldValue = boundaryExpert;
        boundaryExpert = improveSkill(boundaryExpert, gain);
        if (boundaryExpert > oldValue) {
            lastSkillImproved = "Boundary Expert";
            lastSkillGain = boundaryExpert - oldValue;
        }
    }

    public void improveConcurrencySage(int gain) {
        int oldValue = concurrencySage;
        concurrencySage = improveSkill(concurrencySage, gain);
        if (concurrencySage > oldValue) {
            lastSkillImproved = "Concurrency Sage";
            lastSkillGain = concurrencySage - oldValue;
        }
    }

    public void improveMemoryArchitect(int gain) {
        int oldValue = memoryArchitect;
        memoryArchitect = improveSkill(memoryArchitect, gain);
        if (memoryArchitect > oldValue) {
            lastSkillImproved = "Memory Architect";
            lastSkillGain = memoryArchitect - oldValue;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════════════

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatternRecognition() { return patternRecognition; }
    public void setPatternRecognition(int patternRecognition) { this.patternRecognition = patternRecognition; }

    public int getErrorIntuition() { return errorIntuition; }
    public void setErrorIntuition(int errorIntuition) { this.errorIntuition = errorIntuition; }

    public int getLogicFlow() { return logicFlow; }
    public void setLogicFlow(int logicFlow) { this.logicFlow = logicFlow; }

    public int getSpeedDebugging() { return speedDebugging; }
    public void setSpeedDebugging(int speedDebugging) { this.speedDebugging = speedDebugging; }

    public int getComplexityTolerance() { return complexityTolerance; }
    public void setComplexityTolerance(int complexityTolerance) { this.complexityTolerance = complexityTolerance; }

    public int getFocusEndurance() { return focusEndurance; }
    public void setFocusEndurance(int focusEndurance) { this.focusEndurance = focusEndurance; }

    public int getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(int riskAssessment) { this.riskAssessment = riskAssessment; }

    public int getCodeMemory() { return codeMemory; }
    public void setCodeMemory(int codeMemory) { this.codeMemory = codeMemory; }

    public int getNullHunter() { return nullHunter; }
    public void setNullHunter(int nullHunter) { this.nullHunter = nullHunter; }

    public int getLoopMaster() { return loopMaster; }
    public void setLoopMaster(int loopMaster) { this.loopMaster = loopMaster; }

    public int getTypeWrangler() { return typeWrangler; }
    public void setTypeWrangler(int typeWrangler) { this.typeWrangler = typeWrangler; }

    public int getBoundaryExpert() { return boundaryExpert; }
    public void setBoundaryExpert(int boundaryExpert) { this.boundaryExpert = boundaryExpert; }

    public int getConcurrencySage() { return concurrencySage; }
    public void setConcurrencySage(int concurrencySage) { this.concurrencySage = concurrencySage; }

    public int getMemoryArchitect() { return memoryArchitect; }
    public void setMemoryArchitect(int memoryArchitect) { this.memoryArchitect = memoryArchitect; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getBugsSolved() { return bugsSolved; }
    public void setBugsSolved(int bugsSolved) { this.bugsSolved = bugsSolved; }

    public int getPerfectSolves() { return perfectSolves; }
    public void setPerfectSolves(int perfectSolves) { this.perfectSolves = perfectSolves; }

    public int getAvgSolveTimeSeconds() { return avgSolveTimeSeconds; }
    public void setAvgSolveTimeSeconds(int avgSolveTimeSeconds) { this.avgSolveTimeSeconds = avgSolveTimeSeconds; }

    public int getBattleWinStreak() { return battleWinStreak; }
    public void setBattleWinStreak(int battleWinStreak) { this.battleWinStreak = battleWinStreak; }

    public int getLongestBattleStreak() { return longestBattleStreak; }
    public void setLongestBattleStreak(int longestBattleStreak) { this.longestBattleStreak = longestBattleStreak; }

    public int getEloRating() { return eloRating; }
    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
        if (eloRating > peakElo) peakElo = eloRating;
    }

    public int getPeakElo() { return peakElo; }
    public void setPeakElo(int peakElo) { this.peakElo = peakElo; }

    public int getBattlesPlayed() { return battlesPlayed; }
    public void setBattlesPlayed(int battlesPlayed) { this.battlesPlayed = battlesPlayed; }

    public int getBattleWins() { return battleWins; }
    public void setBattleWins(int battleWins) { this.battleWins = battleWins; }

    public int getRankedTier() { return rankedTier; }
    public void setRankedTier(int rankedTier) { this.rankedTier = rankedTier; }

    public int getRankedPoints() { return rankedPoints; }
    public void setRankedPoints(int rankedPoints) { this.rankedPoints = rankedPoints; }

    public int getNearMisses() { return nearMisses; }
    public void setNearMisses(int nearMisses) { this.nearMisses = nearMisses; }

    public String getLastSkillImproved() { return lastSkillImproved; }
    public void setLastSkillImproved(String lastSkillImproved) { this.lastSkillImproved = lastSkillImproved; }

    public int getLastSkillGain() { return lastSkillGain; }
    public void setLastSkillGain(int lastSkillGain) { this.lastSkillGain = lastSkillGain; }

    public long getLastActivityTimestamp() { return lastActivityTimestamp; }
    public void setLastActivityTimestamp(long lastActivityTimestamp) { this.lastActivityTimestamp = lastActivityTimestamp; }
}
