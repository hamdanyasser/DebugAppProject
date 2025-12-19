package com.example.debugappproject.game;

import android.content.Context;
import android.util.Log;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    NEAR-MISS ADDICTION ENGINE                                ║
 * ║           The Psychology of "Almost" - Keeps Players Coming Back             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This engine analyzes player solutions and generates "near-miss" feedback
 * that creates psychological tension and drives retry behavior.
 *
 * Key principles:
 * 1. "Almost correct" feels more frustrating than "completely wrong"
 * 2. Partial success activates reward circuitry without satisfaction
 * 3. Visible progress creates sunk cost fallacy
 * 4. Small remaining gaps feel achievable
 */
public class NearMissEngine {

    private static final String TAG = "NearMissEngine";

    // Thresholds for near-miss detection
    public static final float CLOSE_THRESHOLD = 0.85f;      // 85% match = "So close!"
    public static final float VERY_CLOSE_THRESHOLD = 0.92f; // 92% match = "One tiny mistake!"
    public static final float ALMOST_PERFECT = 0.98f;       // 98% match = "Almost perfect!"

    // Near-miss messages designed to create psychological tension
    private static final String[] CLOSE_MESSAGES = {
            "So close! Just one more change...",
            "You're 85% there. Don't give up now!",
            "Almost got it! The bug is hiding in plain sight.",
            "That's close! You can see it, can't you?",
            "Nearly there! Your instinct is right."
    };

    private static final String[] VERY_CLOSE_MESSAGES = {
            "ONE tiny mistake left. You see it?",
            "Incredible! Just a character or two off.",
            "You basically solved it. Look closer!",
            "92% match! The answer is right there.",
            "So frustratingly close. One more look?"
    };

    private static final String[] ALMOST_PERFECT_MESSAGES = {
            "ARE YOU KIDDING ME?! Check one character!",
            "98% match. ONE. THING. LEFT.",
            "This is basically correct. What's different?",
            "The fix is there. It's hiding in whitespace?",
            "You solved it... almost. Literally one change."
    };

    private static final String[] PARTIAL_SUCCESS_MESSAGES = {
            "Half the tests pass now! Keep going!",
            "You fixed part of it. The rest is similar.",
            "Getting warmer! That change helped.",
            "Some tests work now. Apply the same logic.",
            "Partial fix detected! You're onto something."
    };

    // Track near-miss streaks for escalating messages
    private int consecutiveNearMisses = 0;
    private float lastMatchPercentage = 0f;

    private final Random random = new Random();

    public NearMissEngine() {}

    // ═══════════════════════════════════════════════════════════════════════════
    //                         NEAR-MISS ANALYSIS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Analyze a solution attempt and generate near-miss feedback
     *
     * @param userCode The user's submitted code
     * @param correctCode The correct solution
     * @param testsPassedCount Number of tests that passed
     * @param totalTestCount Total number of tests
     * @return NearMissResult with feedback and psychological hooks
     */
    public NearMissResult analyze(String userCode, String correctCode,
                                   int testsPassedCount, int totalTestCount) {

        // Calculate similarity
        float similarity = calculateSimilarity(userCode, correctCode);
        lastMatchPercentage = similarity;

        // Check test pass ratio
        float testPassRatio = totalTestCount > 0 ?
                (float) testsPassedCount / totalTestCount : 0f;

        // Determine near-miss type
        NearMissType type = classifyNearMiss(similarity, testPassRatio);

        // Generate appropriate feedback
        String message = generateMessage(type, similarity, testsPassedCount, totalTestCount);
        String hint = generatePsychologicalHint(type, userCode, correctCode);

        // Track streaks
        if (type != NearMissType.NOT_CLOSE) {
            consecutiveNearMisses++;
        } else {
            consecutiveNearMisses = 0;
        }

        // Calculate "addiction score" - how compelling is this near-miss?
        int addictionScore = calculateAddictionScore(type, similarity, consecutiveNearMisses);

        return new NearMissResult(
                type,
                similarity,
                message,
                hint,
                testsPassedCount,
                totalTestCount,
                addictionScore,
                consecutiveNearMisses,
                shouldShowRetryButton(type),
                shouldShowDifferenceHighlight(type)
        );
    }

    /**
     * Calculate string similarity using Levenshtein distance
     */
    private float calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0f;

        // Normalize whitespace for fair comparison
        String n1 = normalizeCode(s1);
        String n2 = normalizeCode(s2);

        if (n1.equals(n2)) return 1.0f;

        int maxLen = Math.max(n1.length(), n2.length());
        if (maxLen == 0) return 1.0f;

        int distance = levenshteinDistance(n1, n2);
        return 1.0f - ((float) distance / maxLen);
    }

    private String normalizeCode(String code) {
        return code
                .replaceAll("\\s+", " ")  // Normalize whitespace
                .replaceAll("//.*", "")   // Remove single-line comments
                .replaceAll("/\\*.*?\\*/", "") // Remove multi-line comments
                .trim();
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(
                        prev[j] + 1,      // deletion
                        curr[j - 1] + 1), // insertion
                        prev[j - 1] + cost); // substitution
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[s2.length()];
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         CLASSIFICATION
    // ═══════════════════════════════════════════════════════════════════════════

    private NearMissType classifyNearMiss(float similarity, float testPassRatio) {
        // Almost perfect
        if (similarity >= ALMOST_PERFECT) {
            return NearMissType.ALMOST_PERFECT;
        }

        // Very close
        if (similarity >= VERY_CLOSE_THRESHOLD) {
            return NearMissType.VERY_CLOSE;
        }

        // Close
        if (similarity >= CLOSE_THRESHOLD) {
            return NearMissType.CLOSE;
        }

        // Partial success (some tests pass)
        if (testPassRatio >= 0.3f && testPassRatio < 1.0f) {
            return NearMissType.PARTIAL_SUCCESS;
        }

        // Showing improvement (better than last attempt)
        if (similarity > lastMatchPercentage * 1.1f) {
            return NearMissType.IMPROVING;
        }

        return NearMissType.NOT_CLOSE;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         MESSAGE GENERATION
    // ═══════════════════════════════════════════════════════════════════════════

    private String generateMessage(NearMissType type, float similarity,
                                    int testsPassed, int totalTests) {
        switch (type) {
            case ALMOST_PERFECT:
                return ALMOST_PERFECT_MESSAGES[random.nextInt(ALMOST_PERFECT_MESSAGES.length)];

            case VERY_CLOSE:
                return VERY_CLOSE_MESSAGES[random.nextInt(VERY_CLOSE_MESSAGES.length)];

            case CLOSE:
                return CLOSE_MESSAGES[random.nextInt(CLOSE_MESSAGES.length)];

            case PARTIAL_SUCCESS:
                return String.format("%d/%d tests passing. %s",
                        testsPassed, totalTests,
                        PARTIAL_SUCCESS_MESSAGES[random.nextInt(PARTIAL_SUCCESS_MESSAGES.length)]);

            case IMPROVING:
                return String.format("Getting better! Now at %.0f%% match.", similarity * 100);

            default:
                return "Keep trying! Debug like a detective.";
        }
    }

    private String generatePsychologicalHint(NearMissType type, String userCode, String correctCode) {
        switch (type) {
            case ALMOST_PERFECT:
                // Super specific hint for maximum frustration/motivation
                return findSingleDifference(userCode, correctCode);

            case VERY_CLOSE:
                return "The fix is in your solution. Something small is wrong.";

            case CLOSE:
                return "You understand the problem. Focus on the details.";

            case PARTIAL_SUCCESS:
                return "Apply what's working to the rest of the code.";

            case IMPROVING:
                return "Your changes are helping. Keep that direction.";

            default:
                return "Read the expected output carefully.";
        }
    }

    private String findSingleDifference(String userCode, String correctCode) {
        String n1 = normalizeCode(userCode);
        String n2 = normalizeCode(correctCode);

        // Find first difference position
        int firstDiff = -1;
        for (int i = 0; i < Math.min(n1.length(), n2.length()); i++) {
            if (n1.charAt(i) != n2.charAt(i)) {
                firstDiff = i;
                break;
            }
        }

        if (firstDiff == -1 && n1.length() != n2.length()) {
            return "Check the length of your solution. Something's missing or extra.";
        }

        if (firstDiff >= 0) {
            int lineNum = countNewlines(n1.substring(0, firstDiff)) + 1;
            return String.format("Look around line %d. That's where it differs.", lineNum);
        }

        return "The answer is right in front of you.";
    }

    private int countNewlines(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '\n') count++;
        }
        return count;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ADDICTION SCORING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate how "addictive" this near-miss is (0-100)
     * Higher = more likely to retry immediately
     */
    private int calculateAddictionScore(NearMissType type, float similarity, int streak) {
        int base;
        switch (type) {
            case ALMOST_PERFECT: base = 95; break;
            case VERY_CLOSE: base = 85; break;
            case CLOSE: base = 70; break;
            case PARTIAL_SUCCESS: base = 60; break;
            case IMPROVING: base = 55; break;
            default: base = 20;
        }

        // Streak bonus - consecutive near-misses increase compulsion
        int streakBonus = Math.min(streak * 3, 15);

        // Similarity bonus
        int simBonus = (int) ((similarity - 0.5f) * 20);

        return Math.min(100, base + streakBonus + simBonus);
    }

    private boolean shouldShowRetryButton(NearMissType type) {
        return type != NearMissType.NOT_CLOSE;
    }

    private boolean shouldShowDifferenceHighlight(NearMissType type) {
        return type == NearMissType.ALMOST_PERFECT || type == NearMissType.VERY_CLOSE;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         RESULT CLASS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum NearMissType {
        ALMOST_PERFECT,     // 98%+ match - Maximum frustration
        VERY_CLOSE,         // 92-98% match - High tension
        CLOSE,              // 85-92% match - Visible progress
        PARTIAL_SUCCESS,    // Some tests pass - Tangible win
        IMPROVING,          // Better than last attempt
        NOT_CLOSE           // Needs more work
    }

    public static class NearMissResult {
        public final NearMissType type;
        public final float similarity;
        public final String message;
        public final String hint;
        public final int testsPassed;
        public final int totalTests;
        public final int addictionScore;
        public final int nearMissStreak;
        public final boolean showRetryButton;
        public final boolean showDifferenceHighlight;

        public NearMissResult(NearMissType type, float similarity, String message,
                              String hint, int testsPassed, int totalTests,
                              int addictionScore, int nearMissStreak,
                              boolean showRetryButton, boolean showDifferenceHighlight) {
            this.type = type;
            this.similarity = similarity;
            this.message = message;
            this.hint = hint;
            this.testsPassed = testsPassed;
            this.totalTests = totalTests;
            this.addictionScore = addictionScore;
            this.nearMissStreak = nearMissStreak;
            this.showRetryButton = showRetryButton;
            this.showDifferenceHighlight = showDifferenceHighlight;
        }

        public boolean isNearMiss() {
            return type != NearMissType.NOT_CLOSE;
        }

        public String getPercentageText() {
            return String.format("%.0f%% match", similarity * 100);
        }
    }
}
