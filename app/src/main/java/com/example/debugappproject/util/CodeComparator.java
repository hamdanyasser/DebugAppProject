package com.example.debugappproject.util;

import android.util.Log;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - INTELLIGENT CODE COMPARATOR                          ║
 * ║              Smart validation for debugging challenges                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Flexible code matching (handles whitespace, comments, formatting)
 * - Smart snippet comparison (extracts key fix from full code)
 * - Multi-language support (Java, Python, JavaScript, Kotlin, SQL, HTML)
 * - Tolerance for minor variations
 */
public class CodeComparator {

    private static final String TAG = "CodeComparator";

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
     * MAIN VALIDATION METHOD
     * 
     * Compares user code with fixed code using multiple strategies:
     * 1. Exact normalized match
     * 2. Core fix extraction match
     * 3. Key pattern detection
     * 4. Similarity scoring
     */
    public static boolean codesMatch(String userCode, String fixedCode) {
        if (userCode == null || fixedCode == null) {
            return false;
        }
        
        String normalizedUser = normalizeCode(userCode);
        String normalizedFixed = normalizeCode(fixedCode);
        
        // Strategy 1: Exact normalized match
        if (normalizedUser.equals(normalizedFixed)) {
            Log.d(TAG, "✅ Exact match");
            return true;
        }
        
        // Strategy 2: Core fix extraction match
        String coreUser = extractCoreFix(userCode);
        String coreFixed = extractCoreFix(fixedCode);
        
        if (coreUser.equals(coreFixed)) {
            Log.d(TAG, "✅ Core fix match");
            return true;
        }
        
        // Strategy 3: Check if user code CONTAINS the fixed code core
        if (normalizedUser.contains(coreFixed) || coreUser.contains(coreFixed)) {
            Log.d(TAG, "✅ Contains fix");
            return true;
        }
        
        // Strategy 4: Check for key fix patterns
        if (containsKeyFix(userCode, fixedCode)) {
            Log.d(TAG, "✅ Key fix pattern detected");
            return true;
        }
        
        // Strategy 5: High similarity score (90%+)
        double similarity = calculateSimilarity(coreUser, coreFixed);
        if (similarity >= 0.90) {
            Log.d(TAG, "✅ High similarity: " + (int)(similarity * 100) + "%");
            return true;
        }
        
        Log.d(TAG, "❌ No match. Similarity: " + (int)(similarity * 100) + "%");
        return false;
    }

    /**
     * Checks if user code contains the KEY FIX for common bug patterns.
     * This is more lenient and focuses on whether the actual bug was fixed.
     */
    private static boolean containsKeyFix(String userCode, String fixedCode) {
        String userLower = userCode.toLowerCase();
        String fixedLower = fixedCode.toLowerCase();
        
        // === Common Java Fixes ===
        
        // println vs printIn fix (Bug #1)
        if (fixedLower.contains("println") && !fixedLower.contains("printin")) {
            return userLower.contains("println") && !userLower.contains("printin");
        }
        
        // .equals() for string comparison (Bug #11)
        if (fixedLower.contains(".equals(")) {
            return userLower.contains(".equals(");
        }
        
        // null check fix (Bug #12)
        if (fixedLower.contains("!= null") || fixedLower.contains("!= null")) {
            return userLower.contains("!= null") || userLower.contains("? ");
        }
        
        // Integer division fix - 2.0 instead of 2 (Bug #13)
        if (fixedLower.contains("/ 2.0") || fixedLower.contains("/2.0")) {
            return userLower.contains("2.0") || userLower.contains("(double)");
        }
        
        // && vs || logic fix (Bug #14)
        if (fixedLower.contains("&&") && !fixedLower.contains("||")) {
            if (userLower.contains("&&") && !userLower.contains("||")) {
                return true;
            }
        }
        
        // Array index fix: length - 1 (Bug #16)
        if (fixedLower.contains("length - 1") || fixedLower.contains("length-1")) {
            return userLower.contains("length - 1") || userLower.contains("length-1");
        }
        
        // Arrays.copyOf fix (Bug #17)
        if (fixedLower.contains("arrays.copyof") || fixedLower.contains("clone()")) {
            return userLower.contains("arrays.copyof") || userLower.contains("clone()") || userLower.contains("arraycopy");
        }
        
        // getOrDefault fix (Bug #19)
        if (fixedLower.contains("getordefault")) {
            return userLower.contains("getordefault");
        }
        
        // removeIf fix (Bug #20)
        if (fixedLower.contains("removeif") || fixedLower.contains("iterator")) {
            return userLower.contains("removeif") || userLower.contains("iterator");
        }
        
        // break in switch fix (Bug #21)
        if (fixedLower.contains("break;") && fixedLower.contains("case")) {
            // Count breaks in user vs fixed
            int userBreaks = countOccurrences(userLower, "break;");
            int fixedBreaks = countOccurrences(fixedLower, "break;");
            return userBreaks >= fixedBreaks;
        }
        
        // Base case in recursion fix (Bug #25)
        if (fixedLower.contains("if (n <= 1)") || fixedLower.contains("if (n == 0)") || fixedLower.contains("if (n < 2)")) {
            return userLower.contains("if (n") || userLower.contains("if(n");
        }
        
        // try-with-resources fix (Bug #26)
        if (fixedLower.contains("try (") || fixedLower.contains("try(")) {
            return userLower.contains("try (") || userLower.contains("try(");
        }
        
        // StringBuilder fix (Bug #27)
        if (fixedLower.contains("stringbuilder")) {
            return userLower.contains("stringbuilder");
        }
        
        // Math.min for substring fix (Bug #28)
        if (fixedLower.contains("math.min")) {
            return userLower.contains("math.min") || userLower.contains("length()");
        }
        
        // === JavaScript Fixes ===
        
        // === for strict equality (Bug #41)
        if (fixedLower.contains("===")) {
            return userLower.contains("===");
        }
        
        // let vs var (Bugs #42, #43)
        if (fixedLower.contains("let ") && !fixedLower.contains("var ")) {
            return userLower.contains("let ") && !userLower.contains("var ");
        }
        
        // Arrow function fix (Bug #44)
        if (fixedLower.contains("=>")) {
            return userLower.contains("=>");
        }
        
        // async/await fix (Bug #45)
        if (fixedLower.contains("async") && fixedLower.contains("await")) {
            return userLower.contains("async") && userLower.contains("await");
        }
        
        // .find() fix (Bug #46)
        if (fixedLower.contains(".find(")) {
            return userLower.contains(".find(") || userLower.contains("for (");
        }
        
        // Spread operator fix (Bug #47)
        if (fixedLower.contains("...")) {
            return userLower.contains("...") || userLower.contains("object.assign");
        }
        
        // === Python Fixes ===
        
        // Indentation (Bug #31) - hard to check, be lenient
        if (fixedLower.contains("def ") && fixedLower.contains("print")) {
            return userLower.contains("def ") && userLower.contains("print");
        }
        
        // range() fix (Bug #32)
        if (fixedLower.contains("range(11)") || fixedLower.contains("range(1, 11)")) {
            return userLower.contains("range(11)") || userLower.contains("range(1, 11)") || userLower.contains("<= 10");
        }
        
        // None default argument (Bug #33)
        if (fixedLower.contains("= none") || fixedLower.contains("=none")) {
            return userLower.contains("none");
        }
        
        // Integer division // (Bug #34)
        if (fixedLower.contains("//")) {
            return userLower.contains("//") || userLower.contains("int(");
        }
        
        // .get() for dict (Bug #36)
        if (fixedLower.contains(".get(")) {
            return userLower.contains(".get(");
        }
        
        // global keyword (Bug #37)
        if (fixedLower.contains("global ")) {
            return userLower.contains("global ");
        }
        
        // self parameter (Bug #39)
        if (fixedLower.contains("def __init__(self") || fixedLower.contains("def bark(self")) {
            return userLower.contains("(self");
        }
        
        // === Kotlin Fixes ===
        
        // Safe call ?. and elvis ?: (Bug #48)
        if (fixedLower.contains("?.") || fixedLower.contains("?:")) {
            return userLower.contains("?.") || userLower.contains("?:");
        }
        
        // .copy() for data class (Bug #49)
        if (fixedLower.contains(".copy(")) {
            return userLower.contains(".copy(");
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
        
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        
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

        if (similarityPercent >= 80) {
            return "🔥 So close! " + similarityPercent + "% there. Check around line " + diffLine + ".";
        } else if (similarityPercent >= 60) {
            return "👍 Good progress! " + similarityPercent + "% similar. Focus on the key fix.";
        } else if (similarityPercent >= 40) {
            return "🤔 Getting there! " + similarityPercent + "% similar. Review the hint.";
        } else {
            return "💡 Need a hint? Focus on understanding the bug first.";
        }
    }
}
