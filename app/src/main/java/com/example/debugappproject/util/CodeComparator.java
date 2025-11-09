package com.example.debugappproject.util;

/**
 * Utility class for comparing user code with fixed code.
 * Provides code normalization and comparison logic for the "Run Tests" feature.
 */
public class CodeComparator {

    /**
     * Normalizes code for comparison by:
     * - Trimming leading/trailing whitespace
     * - Removing blank lines
     * - Collapsing multiple spaces to single space
     * - Removing comments (// and /* *\/)
     */
    public static String normalizeCode(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        // Remove single-line comments
        code = code.replaceAll("//.*?(\r?\n|$)", "\n");

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

        return normalized.toString().trim();
    }

    /**
     * Compares user code with fixed code.
     * Returns true if they match after normalization.
     */
    public static boolean codesMatch(String userCode, String fixedCode) {
        String normalizedUser = normalizeCode(userCode);
        String normalizedFixed = normalizeCode(fixedCode);
        return normalizedUser.equals(normalizedFixed);
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
                return i + 1; // 1-indexed line number
            }
        }

        return -1; // No difference found
    }

    /**
     * Generates a helpful error message when codes don't match.
     */
    public static String generateErrorMessage(String userCode, String fixedCode) {
        int diffLine = findFirstDifference(userCode, fixedCode);

        if (diffLine == -1) {
            return "Not quite there yet – double-check your code!";
        } else {
            return "Not quite there yet – something around line " + diffLine + " looks off. " +
                   "Double-check the condition or return value.";
        }
    }
}
