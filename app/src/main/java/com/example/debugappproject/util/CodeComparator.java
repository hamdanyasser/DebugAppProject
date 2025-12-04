package com.example.debugappproject.util;

import android.util.Log;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - INTELLIGENT CODE COMPARATOR                          ║
 * ║              STRICT validation for debugging challenges                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Strict code matching to prevent false positives
 * - Flexible whitespace/comment handling
 * - Multi-language support
 * - Helpful similarity feedback
 * 
 * IMPORTANT: This comparator is intentionally STRICT to ensure users
 * actually fix the bug correctly before getting credit.
 */
public class CodeComparator {

    private static final String TAG = "CodeComparator";
    
    // Minimum similarity required for a "match" (98% = very strict)
    private static final double STRICT_SIMILARITY_THRESHOLD = 0.98;
    
    // Minimum similarity for key-fix based validation (95%)
    private static final double KEY_FIX_SIMILARITY_THRESHOLD = 0.95;

    /**
     * Normalizes code for comparison by:
     * - Trimming leading/trailing whitespace
     * - Removing blank lines
     * - Collapsing multiple spaces to single space
     * - Removing comments (// and /* *\/)
     * - Normalizing line endings
     */
    public static String normalizeCode(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        // Remove single-line comments (Java/JS/Kotlin style)
        code = code.replaceAll("//.*?(\r?\n|$)", "\n");
        
        // Remove Python-style comments
        code = code.replaceAll("#.*?(\r?\n|$)", "\n");

        // Remove multi-line comments
        code = code.replaceAll("/\\*.*?\\*/", "");

        // Split into lines and process each
        String[] lines = code.split("\\r?\\n");
        StringBuilder normalized = new StringBuilder();

        for (String line : lines) {
            // Trim each line
            String trimmed = line.trim();

            // Skip blank lines
            if (trimmed.isEmpty()) {
                continue;
            }

            // Collapse multiple spaces to single space
            trimmed = trimmed.replaceAll("\\s+", " ");

            normalized.append(trimmed).append("\n");
        }

        return normalized.toString().trim().toLowerCase();
    }

    /**
     * Extracts the "core fix" from code - the key part that's different.
     * This helps compare user's snippet-style code with full class code.
     */
    public static String extractCoreFix(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        
        String normalized = normalizeCode(code);
        
        // Remove class wrapper if present
        normalized = normalized.replaceAll("public class \\w+ \\{", "");
        normalized = normalized.replaceAll("class \\w+ \\{", "");
        
        // Remove main method wrapper if present
        normalized = normalized.replaceAll("public static void main\\(string\\[\\] args\\) \\{", "");
        
        // Remove closing braces at end
        normalized = normalized.replaceAll("\\}\\s*$", "");
        normalized = normalized.replaceAll("\\}\\s*\\}\\s*$", "");
        
        return normalized.trim();
    }

    /**
     * MAIN VALIDATION METHOD - STRICT VERSION
     * 
     * Compares user code with fixed code using strict validation:
     * 1. Exact normalized match
     * 2. Core fix extraction match  
     * 3. Key pattern + high similarity (95%+)
     * 4. Very high similarity score (98%+)
     * 
     * This is intentionally strict to prevent false positives.
     */
    public static boolean codesMatch(String userCode, String fixedCode) {
        if (userCode == null || fixedCode == null) {
            return false;
        }
        
        if (userCode.trim().isEmpty()) {
            return false;
        }
        
        String normalizedUser = normalizeCode(userCode);
        String normalizedFixed = normalizeCode(fixedCode);
        
        Log.d(TAG, "=== Code Comparison ===");
        Log.d(TAG, "User (normalized): " + normalizedUser.substring(0, Math.min(100, normalizedUser.length())));
        Log.d(TAG, "Fixed (normalized): " + normalizedFixed.substring(0, Math.min(100, normalizedFixed.length())));
        
        // Strategy 1: Exact normalized match
        if (normalizedUser.equals(normalizedFixed)) {
            Log.d(TAG, "✅ Exact match");
            return true;
        }
        
        // Strategy 2: Core fix extraction match
        String coreUser = extractCoreFix(userCode);
        String coreFixed = extractCoreFix(fixedCode);
        
        if (!coreUser.isEmpty() && !coreFixed.isEmpty() && coreUser.equals(coreFixed)) {
            Log.d(TAG, "✅ Core fix exact match");
            return true;
        }
        
        // Calculate similarity for further checks
        double similarity = calculateSimilarity(normalizedUser, normalizedFixed);
        double coreSimilarity = calculateSimilarity(coreUser, coreFixed);
        
        Log.d(TAG, "Similarity: " + (int)(similarity * 100) + "%, Core similarity: " + (int)(coreSimilarity * 100) + "%");
        
        // Strategy 3: Key fix detected AND high similarity (95%+)
        // This prevents marking as correct when user has the pattern but other bugs
        if (containsKeyFix(userCode, fixedCode)) {
            if (coreSimilarity >= KEY_FIX_SIMILARITY_THRESHOLD) {
                Log.d(TAG, "✅ Key fix + high similarity: " + (int)(coreSimilarity * 100) + "%");
                return true;
            } else {
                Log.d(TAG, "⚠️ Key fix found but similarity too low: " + (int)(coreSimilarity * 100) + "%");
            }
        }
        
        // Strategy 4: Very high similarity (98%+) - catches minor formatting differences
        if (similarity >= STRICT_SIMILARITY_THRESHOLD || coreSimilarity >= STRICT_SIMILARITY_THRESHOLD) {
            Log.d(TAG, "✅ Very high similarity match");
            return true;
        }
        
        Log.d(TAG, "❌ No match. Best similarity: " + (int)(Math.max(similarity, coreSimilarity) * 100) + "%");
        return false;
    }

    /**
     * Checks if user code contains the KEY FIX for the specific bug.
     * Returns true only if the specific fix pattern is present.
     */
    private static boolean containsKeyFix(String userCode, String fixedCode) {
        String userLower = userCode.toLowerCase().replaceAll("\\s+", " ");
        String fixedLower = fixedCode.toLowerCase().replaceAll("\\s+", " ");
        
        // === Bug #1: println vs printIn ===
        if (fixedLower.contains("println") && !fixedLower.contains("printin")) {
            boolean userHasPrintln = userLower.contains("println");
            boolean userHasPrintin = userLower.contains("printin");
            if (userHasPrintln && !userHasPrintin) {
                return true;
            }
        }
        
        // === Bug #11: .equals() for string comparison ===
        if (fixedLower.contains(".equals(") && !fixedLower.contains("==")) {
            // User must use .equals() and NOT use == for string comparison
            if (userLower.contains(".equals(")) {
                // Check they don't still have == for strings
                if (!userLower.matches(".*\".*\"\\s*==.*") && !userLower.matches(".*==\\s*\".*\".*")) {
                    return true;
                }
            }
        }
        
        // === Bug #12: null check ===
        if (fixedLower.contains("!= null") || fixedLower.contains("== null")) {
            if (userLower.contains("!= null") || userLower.contains("== null") || 
                userLower.contains("optional") || userLower.contains("?.")) {
                return true;
            }
        }
        
        // === Bug #13: Integer division fix ===
        if (fixedLower.contains("/ 2.0") || fixedLower.contains("/2.0") || 
            fixedLower.contains("(double)") || fixedLower.contains("(float)")) {
            if (userLower.contains("2.0") || userLower.contains("(double)") || 
                userLower.contains("(float)") || userLower.contains("1.0 *")) {
                return true;
            }
        }
        
        // === Bug #14: && vs || logic ===
        // Only match if fixed uses && and user switches from || to &&
        if (fixedLower.contains("&&") && !fixedLower.contains("||")) {
            if (userLower.contains("&&") && !userLower.contains("||")) {
                return true;
            }
        }
        
        // === Bug #16: Array length - 1 ===
        if (fixedLower.contains("length - 1") || fixedLower.contains("length-1") ||
            fixedLower.contains(".length - 1")) {
            if (userLower.contains("length - 1") || userLower.contains("length-1") ||
                userLower.contains(".length - 1")) {
                return true;
            }
        }
        
        // === Bug #21: break in switch ===
        if (fixedLower.contains("break;") && fixedLower.contains("case")) {
            int fixedBreaks = countOccurrences(fixedLower, "break;");
            int userBreaks = countOccurrences(userLower, "break;");
            // User must have at least as many breaks as the fixed code
            if (userBreaks >= fixedBreaks && userBreaks > 0) {
                return true;
            }
        }
        
        // === Bug #41: === strict equality (JavaScript) ===
        if (fixedLower.contains("===") && !fixedLower.contains("==") || 
            (fixedLower.contains("===") && countOccurrences(fixedLower, "===") > countOccurrences(fixedLower, "=="))) {
            if (userLower.contains("===")) {
                return true;
            }
        }
        
        // === Bug #42/43: let vs var ===
        if (fixedLower.contains("let ") && !fixedLower.contains("var ")) {
            if (userLower.contains("let ") && !userLower.contains("var ")) {
                return true;
            }
        }
        
        // === Python: range() off-by-one ===
        if (fixedLower.contains("range(") && (fixedLower.contains("range(11)") || 
            fixedLower.contains("range(1, 11)") || fixedLower.contains("range(0, 11)"))) {
            if (userLower.contains("range(11)") || userLower.contains("range(1, 11)") || 
                userLower.contains("range(0, 11)")) {
                return true;
            }
        }
        
        // === Python: .get() for dictionary ===
        if (fixedLower.contains(".get(") && !fixedLower.contains("[\"")) {
            if (userLower.contains(".get(")) {
                return true;
            }
        }
        
        // === Kotlin: safe call ?. ===
        if (fixedLower.contains("?.") && !fixedLower.matches(".*[^?]\\..*")) {
            if (userLower.contains("?.")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Counts occurrences of a substring in a string.
     */
    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * Calculates similarity between two strings using Levenshtein distance.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Calculates Levenshtein edit distance between two strings.
     */
    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        // Limit for performance
        if (m > 500 || n > 500) {
            // For very long strings, use simpler comparison
            return Math.abs(m - n) + (s1.equals(s2) ? 0 : Math.max(m, n) / 2);
        }
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[m][n];
    }

    /**
     * Finds the first line number where user code differs from fixed code.
     * Returns -1 if codes match or line number (1-indexed) if they differ.
     */
    public static int findFirstDifference(String userCode, String fixedCode) {
        String normalizedUser = normalizeCode(userCode);
        String normalizedFixed = normalizeCode(fixedCode);

        String[] userLines = normalizedUser.split("\\n");
        String[] fixedLines = normalizedFixed.split("\\n");

        int maxLines = Math.max(userLines.length, fixedLines.length);

        for (int i = 0; i < maxLines; i++) {
            String userLine = i < userLines.length ? userLines[i] : "";
            String fixedLine = i < fixedLines.length ? fixedLines[i] : "";

            if (!userLine.equals(fixedLine)) {
                return i + 1;
            }
        }

        return -1;
    }

    /**
     * Generates a helpful, encouraging error message when codes don't match.
     */
    public static String generateErrorMessage(String userCode, String fixedCode) {
        int diffLine = findFirstDifference(userCode, fixedCode);
        double similarity = calculateSimilarity(
            extractCoreFix(userCode), 
            extractCoreFix(fixedCode)
        );
        
        int similarityPercent = (int)(similarity * 100);

        if (similarityPercent >= 90) {
            return "🔥 Almost there! " + similarityPercent + "% correct. Check line " + diffLine + " carefully.";
        } else if (similarityPercent >= 70) {
            return "👍 Good progress! " + similarityPercent + "% similar. Focus on the key fix.";
        } else if (similarityPercent >= 50) {
            return "🤔 Getting there! " + similarityPercent + "% similar. Review the hint.";
        } else {
            return "💡 Need a hint? Focus on understanding the bug first.";
        }
    }
    
    /**
     * Check if the user's fix addresses the specific bug type.
     * This is a stricter check that looks for the exact fix pattern.
     */
    public static boolean hasCorrectFix(String userCode, String fixedCode, String bugType) {
        String userNorm = normalizeCode(userCode);
        String fixedNorm = normalizeCode(fixedCode);
        
        // First check - must be very similar overall
        double similarity = calculateSimilarity(userNorm, fixedNorm);
        if (similarity < 0.85) {
            return false;
        }
        
        // Then check the specific fix is present
        return containsKeyFix(userCode, fixedCode);
    }
}
