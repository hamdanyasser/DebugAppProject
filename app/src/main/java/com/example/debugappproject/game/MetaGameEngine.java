package com.example.debugappproject.game;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    META-GAME ENGINE                                          ║
 * ║         Extended Learning Features Beyond the Code Editor                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This engine provides fun learning activities that complement the core
 * debugging experience. Key systems:
 *
 * 1. PREDICTION ROUNDS - "What bug will this code have?" (builds intuition)
 * 2. BUG FORENSICS - "What caused this stack trace?" (teaches real debugging)
 * 3. DAILY CHALLENGES - Fun themed challenges each day
 * 4. STREAK TRACKING - Celebrate consistency with forgiveness
 * 5. FRIENDLY COMPETITION - Learn alongside other players
 * 6. POST-MORTEM ANALYSIS - Learn from mistakes constructively
 */
public class MetaGameEngine {

    private static final String TAG = "MetaGameEngine";
    private static final String PREFS_NAME = "meta_game";

    private final SharedPreferences prefs;
    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════════════════════
    //                         PREDICTION TYPES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum BugType {
        NULL_POINTER("NullPointerException", "Something is null that shouldn't be"),
        ARRAY_INDEX("ArrayIndexOutOfBounds", "Accessing invalid array index"),
        INFINITE_LOOP("Infinite Loop", "Loop never terminates"),
        OFF_BY_ONE("Off-by-One Error", "Loop boundary is wrong by 1"),
        TYPE_MISMATCH("Type Mismatch", "Wrong type used or cast"),
        LOGIC_ERROR("Logic Error", "Condition or calculation is wrong"),
        CONCURRENCY("Race Condition", "Timing-dependent bug"),
        MEMORY_LEAK("Memory Leak", "Resources not released"),
        STRING_ERROR("String Error", "String manipulation bug"),
        COMPARISON_ERROR("Comparison Error", "Wrong comparison operator");

        public final String name;
        public final String description;

        BugType(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    public MetaGameEngine(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         PREDICTION ROUNDS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a prediction challenge - show code, player guesses what bug it has
     * Correct predictions earn bonus XP and improve "Error Intuition" skill
     */
    public PredictionChallenge createPredictionChallenge(String buggyCode, BugType actualBug,
                                                          String difficulty) {
        // Generate plausible decoy options based on code patterns
        List<BugType> options = generatePredictionOptions(actualBug, buggyCode);

        int baseXp = 50;
        if (difficulty.equalsIgnoreCase("hard")) baseXp = 100;
        if (difficulty.equalsIgnoreCase("expert")) baseXp = 150;

        return new PredictionChallenge(
                buggyCode,
                options,
                actualBug,
                baseXp,
                30, // 30 second time limit for bonus
                getPredictionStreak()
        );
    }

    private List<BugType> generatePredictionOptions(BugType correct, String code) {
        List<BugType> options = new ArrayList<>();
        options.add(correct);

        // Add related/confusing options based on actual bug
        switch (correct) {
            case NULL_POINTER:
                options.add(BugType.TYPE_MISMATCH);
                options.add(BugType.COMPARISON_ERROR);
                break;
            case ARRAY_INDEX:
                options.add(BugType.OFF_BY_ONE);
                options.add(BugType.INFINITE_LOOP);
                break;
            case INFINITE_LOOP:
                options.add(BugType.OFF_BY_ONE);
                options.add(BugType.LOGIC_ERROR);
                break;
            case OFF_BY_ONE:
                options.add(BugType.ARRAY_INDEX);
                options.add(BugType.LOGIC_ERROR);
                break;
            default:
                options.add(BugType.LOGIC_ERROR);
                options.add(BugType.NULL_POINTER);
        }

        // Add one random option if we have less than 4
        while (options.size() < 4) {
            BugType random = BugType.values()[this.random.nextInt(BugType.values().length)];
            if (!options.contains(random)) {
                options.add(random);
            }
        }

        // Shuffle options
        java.util.Collections.shuffle(options);
        return options;
    }

    public PredictionResult evaluatePrediction(PredictionChallenge challenge, BugType guess,
                                                int secondsTaken) {
        boolean correct = guess == challenge.correctAnswer;

        int xpEarned = 0;
        String message;
        int streakChange;

        if (correct) {
            xpEarned = challenge.baseXp;

            // Speed bonus
            if (secondsTaken <= challenge.timeLimitForBonus) {
                int speedBonus = (challenge.timeLimitForBonus - secondsTaken) * 2;
                xpEarned += speedBonus;
            }

            // Streak bonus
            int newStreak = challenge.currentStreak + 1;
            if (newStreak >= 3) {
                xpEarned += newStreak * 10;
            }

            savePredictionStreak(newStreak);
            streakChange = 1;

            if (secondsTaken <= 5) {
                message = "INSTANT READ! You saw it immediately! 🔥";
            } else if (secondsTaken <= 15) {
                message = "Sharp eye! Quick prediction!";
            } else {
                message = "Correct! Your intuition is improving.";
            }
        } else {
            savePredictionStreak(0);
            streakChange = -challenge.currentStreak;
            message = String.format("Not quite. The bug was: %s", challenge.correctAnswer.name);
        }

        return new PredictionResult(
                correct,
                xpEarned,
                message,
                streakChange,
                challenge.correctAnswer,
                guess
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         BUG FORENSICS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Forensics mode - Given only a stack trace, identify the bug
     * This is HARD mode - tests real debugging intuition
     */
    public ForensicsChallenge createForensicsChallenge(String stackTrace, String bugDescription,
                                                        List<String> codeSnippets,
                                                        int correctSnippetIndex) {
        int baseXp = 75; // Forensics is harder, more XP

        return new ForensicsChallenge(
                stackTrace,
                bugDescription,
                codeSnippets,
                correctSnippetIndex,
                baseXp,
                60 // 60 second time limit
        );
    }

    public ForensicsResult evaluateForensics(ForensicsChallenge challenge, int selectedIndex,
                                              int secondsTaken) {
        boolean correct = selectedIndex == challenge.correctIndex;

        int xpEarned = 0;
        String message;

        if (correct) {
            xpEarned = challenge.baseXp;

            // Time bonus
            if (secondsTaken <= 30) {
                xpEarned += 25;
            }

            message = "Excellent forensics work! You traced the bug.";
            incrementForensicsSolved();
        } else {
            message = "The stack trace pointed elsewhere. Study the trace again.";
        }

        return new ForensicsResult(
                correct,
                xpEarned,
                message,
                challenge.correctIndex
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         DAILY CHALLENGES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum ChallengeType {
        SPEED_RUN("Speed Run", "Solve 3 bugs in under 5 minutes"),
        PERFECT_DAY("Perfect Day", "Solve 5 bugs without any hints"),
        PREDICTION_MASTER("Prediction Master", "Get 5 predictions correct in a row"),
        FORENSICS_EXPERT("Forensics Expert", "Solve 3 forensics challenges"),
        STREAK_KEEPER("Streak Keeper", "Maintain your daily streak"),
        HARD_MODE("Hard Mode", "Solve 3 hard/expert bugs"),
        NO_MISTAKES("No Mistakes", "Submit 0 wrong answers today"),
        MARATHON("Marathon", "Solve 10 bugs in one session");

        public final String name;
        public final String description;

        ChallengeType(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    /**
     * Get today's daily challenges (regenerated each day)
     */
    public List<DailyChallenge> getTodaysChallenges() {
        long today = getTodayTimestamp();
        long lastChallengeDay = prefs.getLong("last_challenge_day", 0);

        if (today != lastChallengeDay) {
            // Generate new challenges for today
            generateDailyChallenges(today);
        }

        return loadDailyChallenges();
    }

    private void generateDailyChallenges(long timestamp) {
        // Use timestamp as seed for consistent daily challenges
        Random dailyRandom = new Random(timestamp);

        // Pick 3 random challenge types
        ChallengeType[] allTypes = ChallengeType.values();
        List<ChallengeType> selected = new ArrayList<>();

        while (selected.size() < 3) {
            ChallengeType type = allTypes[dailyRandom.nextInt(allTypes.length)];
            if (!selected.contains(type)) {
                selected.add(type);
            }
        }

        // Save challenges
        prefs.edit()
                .putLong("last_challenge_day", timestamp)
                .putString("daily_1_type", selected.get(0).name())
                .putString("daily_2_type", selected.get(1).name())
                .putString("daily_3_type", selected.get(2).name())
                .putBoolean("daily_1_complete", false)
                .putBoolean("daily_2_complete", false)
                .putBoolean("daily_3_complete", false)
                .apply();
    }

    private List<DailyChallenge> loadDailyChallenges() {
        List<DailyChallenge> challenges = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            String typeStr = prefs.getString("daily_" + i + "_type", "SPEED_RUN");
            boolean complete = prefs.getBoolean("daily_" + i + "_complete", false);

            ChallengeType type = ChallengeType.valueOf(typeStr);
            int xpReward = getXpForChallenge(type);

            challenges.add(new DailyChallenge(type, xpReward, complete, i));
        }

        return challenges;
    }

    private int getXpForChallenge(ChallengeType type) {
        switch (type) {
            case SPEED_RUN: return 150;
            case PERFECT_DAY: return 200;
            case PREDICTION_MASTER: return 175;
            case FORENSICS_EXPERT: return 200;
            case STREAK_KEEPER: return 100;
            case HARD_MODE: return 250;
            case NO_MISTAKES: return 300;
            case MARATHON: return 250;
            default: return 100;
        }
    }

    public void completeDailyChallenge(int challengeIndex) {
        prefs.edit()
                .putBoolean("daily_" + challengeIndex + "_complete", true)
                .apply();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         STREAK SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get current daily streak with freeze protection info
     */
    public StreakInfo getStreakInfo() {
        int currentStreak = prefs.getInt("daily_streak", 0);
        int longestStreak = prefs.getInt("longest_streak", 0);
        long lastActiveDay = prefs.getLong("last_active_day", 0);
        int freezesRemaining = prefs.getInt("streak_freezes", 0);
        boolean usedFreezeToday = prefs.getBoolean("used_freeze_today", false);

        long today = getTodayTimestamp();
        long yesterday = today - 86400000L; // 24 hours in ms

        boolean activeToday = lastActiveDay == today;
        boolean atRisk = !activeToday && lastActiveDay == yesterday;
        boolean broken = !activeToday && lastActiveDay < yesterday;

        if (broken && !usedFreezeToday && freezesRemaining > 0) {
            // Can use streak freeze
            atRisk = true;
            broken = false;
        }

        return new StreakInfo(
                currentStreak,
                longestStreak,
                activeToday,
                atRisk,
                broken,
                freezesRemaining,
                getStreakMilestoneProgress(currentStreak)
        );
    }

    public void recordDailyActivity() {
        long today = getTodayTimestamp();
        long lastActiveDay = prefs.getLong("last_active_day", 0);
        int currentStreak = prefs.getInt("daily_streak", 0);
        int longestStreak = prefs.getInt("longest_streak", 0);

        if (lastActiveDay == today) {
            return; // Already recorded today
        }

        long yesterday = today - 86400000L;

        if (lastActiveDay == yesterday) {
            // Continuing streak
            currentStreak++;
        } else if (lastActiveDay < yesterday) {
            // Streak broken - check for freeze
            int freezes = prefs.getInt("streak_freezes", 0);
            if (freezes > 0) {
                // Use freeze automatically
                prefs.edit()
                        .putInt("streak_freezes", freezes - 1)
                        .putBoolean("used_freeze_today", true)
                        .apply();
            } else {
                // Streak lost
                currentStreak = 1;
            }
        } else {
            // First day
            currentStreak = 1;
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        prefs.edit()
                .putLong("last_active_day", today)
                .putInt("daily_streak", currentStreak)
                .putInt("longest_streak", longestStreak)
                .putBoolean("used_freeze_today", false)
                .apply();
    }

    private int getStreakMilestoneProgress(int streak) {
        // Milestones at 7, 14, 30, 60, 100, 365 days
        int[] milestones = {7, 14, 30, 60, 100, 365};
        for (int milestone : milestones) {
            if (streak < milestone) {
                return (streak * 100) / milestone;
            }
        }
        return 100;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RIVAL SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Add a rival to track (from battle or leaderboard)
     */
    public void addRival(String odUserId, String displayName, int elo) {
        // In a real app, parse and add to JSON array
        // For now, just store basic info
        int rivalCount = prefs.getInt("rival_count", 0);
        prefs.edit()
                .putString("rival_" + rivalCount + "_id", odUserId)
                .putString("rival_" + rivalCount + "_name", displayName)
                .putInt("rival_" + rivalCount + "_elo", elo)
                .putInt("rival_count", rivalCount + 1)
                .apply();
    }

    /**
     * Check if user beat a rival's time/score
     */
    public RivalUpdate checkRivalBeaten(String odUserId, int userScore) {
        // In a real implementation, compare against rival's stored scores
        // Return notification if user beat their rival
        return null; // Placeholder
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         POST-MORTEM ANALYSIS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generate post-mortem analysis after a session
     */
    public PostMortemAnalysis generatePostMortem(List<BugAttempt> attempts) {
        int totalBugs = attempts.size();
        int perfectSolves = 0;
        int hintsUsed = 0;
        int wrongSubmissions = 0;
        long totalTimeMs = 0;
        List<String> weakSpots = new ArrayList<>();
        List<String> strengths = new ArrayList<>();

        // Bug type tracking
        int[] bugTypeFailures = new int[BugType.values().length];
        int[] bugTypeSuccesses = new int[BugType.values().length];

        for (BugAttempt attempt : attempts) {
            totalTimeMs += attempt.timeSpentMs;

            if (attempt.solved) {
                if (attempt.hintsUsed == 0 && attempt.wrongSubmissions == 0) {
                    perfectSolves++;
                }
                bugTypeSuccesses[attempt.bugType.ordinal()]++;
            } else {
                bugTypeFailures[attempt.bugType.ordinal()]++;
            }

            hintsUsed += attempt.hintsUsed;
            wrongSubmissions += attempt.wrongSubmissions;
        }

        // Analyze weak spots
        for (int i = 0; i < bugTypeFailures.length; i++) {
            if (bugTypeFailures[i] >= 2) {
                weakSpots.add(BugType.values()[i].name);
            }
            if (bugTypeSuccesses[i] >= 3 && bugTypeFailures[i] == 0) {
                strengths.add(BugType.values()[i].name);
            }
        }

        // Generate insights
        List<String> insights = new ArrayList<>();

        if (perfectSolves == totalBugs) {
            insights.add("FLAWLESS SESSION! You solved everything perfectly.");
        } else if (perfectSolves > totalBugs / 2) {
            insights.add("Strong performance. Over half were perfect solves.");
        }

        if (hintsUsed > totalBugs) {
            insights.add("You're relying heavily on hints. Try solving first.");
        }

        if (wrongSubmissions > totalBugs * 2) {
            insights.add("Lots of wrong attempts. Read the code more carefully.");
        }

        long avgTimeMs = totalBugs > 0 ? totalTimeMs / totalBugs : 0;
        if (avgTimeMs < 60000) { // Under 1 minute average
            insights.add("Speed demon! Your average solve time is impressive.");
        }

        return new PostMortemAnalysis(
                totalBugs,
                perfectSolves,
                hintsUsed,
                wrongSubmissions,
                totalTimeMs,
                weakSpots,
                strengths,
                insights
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private long getTodayTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private int getPredictionStreak() {
        return prefs.getInt("prediction_streak", 0);
    }

    private void savePredictionStreak(int streak) {
        prefs.edit().putInt("prediction_streak", streak).apply();
    }

    private void incrementForensicsSolved() {
        int current = prefs.getInt("forensics_solved", 0);
        prefs.edit().putInt("forensics_solved", current + 1).apply();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RESULT CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class PredictionChallenge {
        public final String buggyCode;
        public final List<BugType> options;
        public final BugType correctAnswer;
        public final int baseXp;
        public final int timeLimitForBonus;
        public final int currentStreak;

        public PredictionChallenge(String buggyCode, List<BugType> options, BugType correctAnswer,
                                    int baseXp, int timeLimitForBonus, int currentStreak) {
            this.buggyCode = buggyCode;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.baseXp = baseXp;
            this.timeLimitForBonus = timeLimitForBonus;
            this.currentStreak = currentStreak;
        }
    }

    public static class PredictionResult {
        public final boolean correct;
        public final int xpEarned;
        public final String message;
        public final int streakChange;
        public final BugType correctAnswer;
        public final BugType userGuess;

        public PredictionResult(boolean correct, int xpEarned, String message,
                                 int streakChange, BugType correctAnswer, BugType userGuess) {
            this.correct = correct;
            this.xpEarned = xpEarned;
            this.message = message;
            this.streakChange = streakChange;
            this.correctAnswer = correctAnswer;
            this.userGuess = userGuess;
        }
    }

    public static class ForensicsChallenge {
        public final String stackTrace;
        public final String bugDescription;
        public final List<String> codeSnippets;
        public final int correctIndex;
        public final int baseXp;
        public final int timeLimit;

        public ForensicsChallenge(String stackTrace, String bugDescription,
                                   List<String> codeSnippets, int correctIndex,
                                   int baseXp, int timeLimit) {
            this.stackTrace = stackTrace;
            this.bugDescription = bugDescription;
            this.codeSnippets = codeSnippets;
            this.correctIndex = correctIndex;
            this.baseXp = baseXp;
            this.timeLimit = timeLimit;
        }
    }

    public static class ForensicsResult {
        public final boolean correct;
        public final int xpEarned;
        public final String message;
        public final int correctIndex;

        public ForensicsResult(boolean correct, int xpEarned, String message, int correctIndex) {
            this.correct = correct;
            this.xpEarned = xpEarned;
            this.message = message;
            this.correctIndex = correctIndex;
        }
    }

    public static class DailyChallenge {
        public final ChallengeType type;
        public final int xpReward;
        public final boolean completed;
        public final int index;

        public DailyChallenge(ChallengeType type, int xpReward, boolean completed, int index) {
            this.type = type;
            this.xpReward = xpReward;
            this.completed = completed;
            this.index = index;
        }
    }

    public static class StreakInfo {
        public final int currentStreak;
        public final int longestStreak;
        public final boolean activeToday;
        public final boolean atRisk;
        public final boolean broken;
        public final int freezesRemaining;
        public final int milestoneProgress;

        public StreakInfo(int currentStreak, int longestStreak, boolean activeToday,
                          boolean atRisk, boolean broken, int freezesRemaining,
                          int milestoneProgress) {
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
            this.activeToday = activeToday;
            this.atRisk = atRisk;
            this.broken = broken;
            this.freezesRemaining = freezesRemaining;
            this.milestoneProgress = milestoneProgress;
        }

        public String getStreakMessage() {
            if (broken) {
                return "Ready for a fresh start? Let's go!";
            }
            if (atRisk) {
                return "🌟 Play today to continue your streak! (Or use a freeze)";
            }
            if (currentStreak >= 30) {
                return "🔥 " + currentStreak + " day streak! Amazing dedication!";
            }
            if (currentStreak >= 7) {
                return "🔥 " + currentStreak + " day streak! Great consistency!";
            }
            return currentStreak + " day streak - nice!";
        }
    }

    public static class RivalUpdate {
        public final String rivalName;
        public final String message;
        public final boolean beatRival;

        public RivalUpdate(String rivalName, String message, boolean beatRival) {
            this.rivalName = rivalName;
            this.message = message;
            this.beatRival = beatRival;
        }
    }

    public static class BugAttempt {
        public final BugType bugType;
        public final boolean solved;
        public final int hintsUsed;
        public final int wrongSubmissions;
        public final long timeSpentMs;

        public BugAttempt(BugType bugType, boolean solved, int hintsUsed,
                          int wrongSubmissions, long timeSpentMs) {
            this.bugType = bugType;
            this.solved = solved;
            this.hintsUsed = hintsUsed;
            this.wrongSubmissions = wrongSubmissions;
            this.timeSpentMs = timeSpentMs;
        }
    }

    public static class PostMortemAnalysis {
        public final int totalBugs;
        public final int perfectSolves;
        public final int hintsUsed;
        public final int wrongSubmissions;
        public final long totalTimeMs;
        public final List<String> weakSpots;
        public final List<String> strengths;
        public final List<String> insights;

        public PostMortemAnalysis(int totalBugs, int perfectSolves, int hintsUsed,
                                   int wrongSubmissions, long totalTimeMs,
                                   List<String> weakSpots, List<String> strengths,
                                   List<String> insights) {
            this.totalBugs = totalBugs;
            this.perfectSolves = perfectSolves;
            this.hintsUsed = hintsUsed;
            this.wrongSubmissions = wrongSubmissions;
            this.totalTimeMs = totalTimeMs;
            this.weakSpots = weakSpots;
            this.strengths = strengths;
            this.insights = insights;
        }

        public String getGrade() {
            if (totalBugs == 0) return "N/A";
            float perfectRatio = (float) perfectSolves / totalBugs;
            if (perfectRatio >= 0.9f) return "S";
            if (perfectRatio >= 0.7f) return "A";
            if (perfectRatio >= 0.5f) return "B";
            if (perfectRatio >= 0.3f) return "C";
            return "D";
        }
    }
}
