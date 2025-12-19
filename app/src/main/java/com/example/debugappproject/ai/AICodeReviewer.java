package com.example.debugappproject.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.debugappproject.model.Bug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AI CODE REVIEWER                                     ║
 * ║         Intelligent Code Review with Suggestions & Best Practices            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Fix quality assessment
 * - Style suggestions
 * - Best practice recommendations
 * - Performance tips
 * - Security checks
 * - Alternative solutions
 */
public class AICodeReviewer {

    private static final String TAG = "AICodeReviewer";
    private static AICodeReviewer instance;
    private final Context context;
    private final Handler mainHandler;
    private final Random random;

    // Review categories
    public enum ReviewCategory {
        CORRECTNESS,
        STYLE,
        PERFORMANCE,
        SECURITY,
        BEST_PRACTICE,
        READABILITY
    }

    // Review result
    public static class CodeReview {
        public int overallScore; // 0-100
        public boolean isCorrect;
        public List<ReviewItem> items;
        public String summary;
        public List<String> improvements;
        public String alternativeSolution;

        public CodeReview() {
            items = new ArrayList<>();
            improvements = new ArrayList<>();
        }
    }

    // Individual review item
    public static class ReviewItem {
        public ReviewCategory category;
        public String severity; // "info", "warning", "error"
        public String message;
        public int lineNumber;
        public String suggestion;

        public ReviewItem(ReviewCategory category, String severity, String message) {
            this.category = category;
            this.severity = severity;
            this.message = message;
            this.lineNumber = -1;
        }

        public ReviewItem withLine(int line) {
            this.lineNumber = line;
            return this;
        }

        public ReviewItem withSuggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }
    }

    // Callback interface
    public interface ReviewCallback {
        void onReviewComplete(CodeReview review);
        void onProgress(String status);
    }

    private AICodeReviewer(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public static synchronized AICodeReviewer getInstance(Context context) {
        if (instance == null) {
            instance = new AICodeReviewer(context);
        }
        return instance;
    }

    /**
     * Review user's code fix
     */
    public void reviewCode(String userCode, Bug bug, ReviewCallback callback) {
        mainHandler.post(() -> callback.onProgress("Analyzing code structure..."));

        // Simulate processing delay
        mainHandler.postDelayed(() -> {
            callback.onProgress("Checking correctness...");

            mainHandler.postDelayed(() -> {
                callback.onProgress("Evaluating style and best practices...");

                mainHandler.postDelayed(() -> {
                    CodeReview review = performReview(userCode, bug);
                    callback.onReviewComplete(review);
                }, 400);
            }, 400);
        }, 400);
    }

    /**
     * Perform the actual code review
     */
    private CodeReview performReview(String userCode, Bug bug) {
        CodeReview review = new CodeReview();
        String language = bug.getLanguage().toLowerCase();
        String fixedCode = bug.getFixedCode();

        // Check correctness
        review.isCorrect = checkCorrectness(userCode, fixedCode);

        // Run various checks based on language
        switch (language) {
            case "java":
                reviewJavaCode(userCode, bug, review);
                break;
            case "python":
            case "py":
                reviewPythonCode(userCode, bug, review);
                break;
            case "javascript":
            case "js":
                reviewJavaScriptCode(userCode, bug, review);
                break;
            default:
                reviewGenericCode(userCode, bug, review);
        }

        // Calculate overall score
        review.overallScore = calculateScore(review);

        // Generate summary
        review.summary = generateSummary(review);

        // Suggest alternative solution if different from user's
        review.alternativeSolution = suggestAlternative(userCode, bug);

        return review;
    }

    /**
     * Check if user's fix is correct
     */
    private boolean checkCorrectness(String userCode, String fixedCode) {
        // Normalize both codes
        String normalizedUser = normalizeCode(userCode);
        String normalizedFixed = normalizeCode(fixedCode);

        // Exact match
        if (normalizedUser.equals(normalizedFixed)) {
            return true;
        }

        // Similarity check (allow minor differences)
        int similarity = calculateSimilarity(normalizedUser, normalizedFixed);
        return similarity >= 85; // 85% similarity threshold
    }

    /**
     * Review Java-specific code
     */
    private void reviewJavaCode(String code, Bug bug, CodeReview review) {
        String[] lines = code.split("\n");

        // Check naming conventions
        Pattern varPattern = Pattern.compile("\\b(int|String|double|boolean|float)\\s+(\\w+)");
        Matcher varMatcher = varPattern.matcher(code);
        while (varMatcher.find()) {
            String varName = varMatcher.group(2);
            if (!varName.matches("[a-z][a-zA-Z0-9]*")) {
                review.items.add(new ReviewItem(
                        ReviewCategory.STYLE,
                        "warning",
                        "Variable '" + varName + "' should use camelCase naming"
                ).withSuggestion("Rename to follow Java naming conventions"));
            }
        }

        // Check for magic numbers
        Pattern magicPattern = Pattern.compile("(?<![\\w\"])\\b(\\d{2,})\\b(?![\\w\"])");
        Matcher magicMatcher = magicPattern.matcher(code);
        while (magicMatcher.find()) {
            String number = magicMatcher.group(1);
            if (!number.equals("100") && !number.equals("10")) { // Common acceptable numbers
                review.items.add(new ReviewItem(
                        ReviewCategory.BEST_PRACTICE,
                        "info",
                        "Magic number " + number + " should be a named constant"
                ).withSuggestion("Extract to a constant: private static final int CONSTANT_NAME = " + number + ";"));
            }
        }

        // Check for proper exception handling
        if (code.contains("catch") && code.contains("Exception")) {
            if (code.contains("catch (Exception")) {
                review.items.add(new ReviewItem(
                        ReviewCategory.BEST_PRACTICE,
                        "warning",
                        "Catching generic Exception is discouraged"
                ).withSuggestion("Catch specific exceptions instead"));
            }
        }

        // Check for null safety
        if (code.contains("null") && !code.contains("!= null") && !code.contains("== null")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.SECURITY,
                    "info",
                    "Consider adding null checks to prevent NullPointerException"
            ));
        }

        // Check for StringBuilder vs String concatenation in loops
        if (code.contains("for") && code.contains("+= ") && code.contains("\"")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.PERFORMANCE,
                    "warning",
                    "String concatenation in loop is inefficient"
            ).withSuggestion("Use StringBuilder for better performance"));
        }

        // Check proper equals usage
        if (code.contains("==") && code.contains("\"")) {
            Pattern stringComparePattern = Pattern.compile("\\w+\\s*==\\s*\"[^\"]*\"");
            if (stringComparePattern.matcher(code).find()) {
                review.items.add(new ReviewItem(
                        ReviewCategory.CORRECTNESS,
                        "error",
                        "Use .equals() for String comparison, not =="
                ).withSuggestion("Replace == with .equals() for string comparison"));
            }
        }

        // Check for unused imports (simplified)
        if (code.contains("import")) {
            review.improvements.add("Consider removing unused imports");
        }

        // Positive feedback
        if (code.contains("final ")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "Good use of 'final' keyword! 👍"
            ));
        }

        // Check array bounds handling
        if (code.contains("[") && code.contains("length")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.CORRECTNESS,
                    "info",
                    "Good: Array bounds are being considered 👍"
            ));
        }
    }

    /**
     * Review Python-specific code
     */
    private void reviewPythonCode(String code, Bug bug, CodeReview review) {
        String[] lines = code.split("\n");

        // Check PEP 8 naming conventions
        Pattern funcPattern = Pattern.compile("def\\s+(\\w+)");
        Matcher funcMatcher = funcPattern.matcher(code);
        while (funcMatcher.find()) {
            String funcName = funcMatcher.group(1);
            if (!funcName.matches("[a-z_][a-z0-9_]*")) {
                review.items.add(new ReviewItem(
                        ReviewCategory.STYLE,
                        "warning",
                        "Function '" + funcName + "' should use snake_case (PEP 8)"
                ));
            }
        }

        // Check for bare except
        if (code.contains("except:") && !code.contains("except Exception")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "warning",
                    "Avoid bare 'except:' - catch specific exceptions"
            ).withSuggestion("Use 'except Exception as e:' or specific exception types"));
        }

        // Check for mutable default arguments
        Pattern defaultArgPattern = Pattern.compile("def\\s+\\w+\\([^)]*=\\s*\\[\\]");
        if (defaultArgPattern.matcher(code).find()) {
            review.items.add(new ReviewItem(
                    ReviewCategory.CORRECTNESS,
                    "error",
                    "Mutable default argument detected! This is a common Python bug."
            ).withSuggestion("Use None as default and initialize inside the function"));
        }

        // Check for proper indentation
        boolean hasTabsAndSpaces = code.contains("\t") &&
                Pattern.compile("^    ", Pattern.MULTILINE).matcher(code).find();
        if (hasTabsAndSpaces) {
            review.items.add(new ReviewItem(
                    ReviewCategory.STYLE,
                    "error",
                    "Mixed tabs and spaces - use consistent indentation"
            ));
        }

        // Check for f-string usage (modern Python)
        if (code.contains("format(") || code.contains("% ")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.STYLE,
                    "info",
                    "Consider using f-strings for cleaner string formatting (Python 3.6+)"
            ).withSuggestion("Example: f\"Hello {name}\" instead of \"Hello {}\".format(name)"));
        }

        // Check for list comprehension opportunities
        if (code.contains("for") && code.contains("append")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.STYLE,
                    "info",
                    "Consider using list comprehension for more Pythonic code"
            ).withSuggestion("[item for item in iterable if condition]"));
        }

        // Positive feedback for good practices
        if (code.contains("if __name__ == \"__main__\"") || code.contains("if __name__ == '__main__'")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "Good: Using if __name__ == '__main__' guard 👍"
            ));
        }
    }

    /**
     * Review JavaScript-specific code
     */
    private void reviewJavaScriptCode(String code, Bug bug, CodeReview review) {
        // Check for var vs let/const
        if (code.contains("var ")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "warning",
                    "Consider using 'let' or 'const' instead of 'var'"
            ).withSuggestion("'const' for values that don't change, 'let' for variables"));
        }

        // Check for === vs ==
        if (code.contains("==") && !code.contains("===")) {
            Pattern eqPattern = Pattern.compile("[^=!]==[^=]");
            if (eqPattern.matcher(code).find()) {
                review.items.add(new ReviewItem(
                        ReviewCategory.CORRECTNESS,
                        "warning",
                        "Use === for strict equality comparison"
                ).withSuggestion("=== checks both value and type, preventing unexpected coercion"));
            }
        }

        // Check for callback hell
        int callbackDepth = countNestingDepth(code, "function");
        if (callbackDepth > 2) {
            review.items.add(new ReviewItem(
                    ReviewCategory.READABILITY,
                    "warning",
                    "Deep callback nesting detected"
            ).withSuggestion("Consider using async/await or Promises for cleaner code"));
        }

        // Check for console.log in production code
        if (code.contains("console.log")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "Remember to remove console.log statements in production"
            ));
        }

        // Check for proper error handling in promises
        if (code.contains(".then(") && !code.contains(".catch(")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.CORRECTNESS,
                    "warning",
                    "Promise chain without .catch() - unhandled rejections possible"
            ).withSuggestion("Add .catch(error => ...) to handle errors"));
        }

        // Check for arrow functions
        if (code.contains("function()") && !code.contains("=>")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.STYLE,
                    "info",
                    "Consider using arrow functions for shorter syntax"
            ).withSuggestion("() => {} instead of function() {}"));
        }

        // Check for template literals
        if (code.contains("+ \"") || code.contains("\" +")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.STYLE,
                    "info",
                    "Consider using template literals for string concatenation"
            ).withSuggestion("Use `Hello ${name}` instead of \"Hello \" + name"));
        }

        // Positive feedback for modern JS
        if (code.contains("const ") || code.contains("let ")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "Good: Using modern variable declarations 👍"
            ));
        }

        if (code.contains("async") && code.contains("await")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "Good: Using async/await for cleaner async code 👍"
            ));
        }
    }

    /**
     * Generic code review for other languages
     */
    private void reviewGenericCode(String code, Bug bug, CodeReview review) {
        // Check for common issues across languages

        // Very long lines
        for (String line : code.split("\n")) {
            if (line.length() > 120) {
                review.items.add(new ReviewItem(
                        ReviewCategory.READABILITY,
                        "info",
                        "Line exceeds 120 characters"
                ).withSuggestion("Consider breaking into multiple lines"));
                break;
            }
        }

        // Check for commented out code
        if (code.contains("//") || code.contains("/*") || code.contains("#")) {
            Pattern commentedCodePattern = Pattern.compile("//.*[;{}()]|#.*[;{}()]");
            if (commentedCodePattern.matcher(code).find()) {
                review.items.add(new ReviewItem(
                        ReviewCategory.READABILITY,
                        "info",
                        "Commented-out code detected"
                ).withSuggestion("Remove unused code instead of commenting it out"));
            }
        }

        // Check for TODO/FIXME comments
        if (code.toUpperCase().contains("TODO") || code.toUpperCase().contains("FIXME")) {
            review.items.add(new ReviewItem(
                    ReviewCategory.BEST_PRACTICE,
                    "info",
                    "TODO/FIXME comment found - consider addressing it"
            ));
        }
    }

    /**
     * Count nesting depth for a keyword
     */
    private int countNestingDepth(String code, String keyword) {
        int maxDepth = 0;
        int currentDepth = 0;

        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '{') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (code.charAt(i) == '}') {
                currentDepth = Math.max(0, currentDepth - 1);
            }
        }

        return maxDepth;
    }

    /**
     * Calculate overall score
     */
    private int calculateScore(CodeReview review) {
        int score = 100;

        // Start with correctness
        if (!review.isCorrect) {
            score -= 40;
        }

        // Deduct for issues
        for (ReviewItem item : review.items) {
            switch (item.severity) {
                case "error":
                    score -= 15;
                    break;
                case "warning":
                    score -= 7;
                    break;
                case "info":
                    // Info items don't reduce score, might even add points
                    if (item.message.contains("👍") || item.message.contains("Good")) {
                        score += 2;
                    }
                    break;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Generate summary based on review
     */
    private String generateSummary(CodeReview review) {
        if (review.overallScore >= 90) {
            return "🌟 Excellent fix! Your code is clean, correct, and follows best practices.";
        } else if (review.overallScore >= 75) {
            return "👍 Good job! Your fix works correctly with minor style improvements possible.";
        } else if (review.overallScore >= 60) {
            return "✅ Your fix is functional, but there are several areas for improvement.";
        } else if (review.overallScore >= 40) {
            return "⚠️ Your fix partially addresses the bug. Review the suggestions carefully.";
        } else if (review.isCorrect) {
            return "🔧 The fix works, but the code quality needs significant improvement.";
        } else {
            return "❌ The fix doesn't fully resolve the bug. Check the expected behavior again.";
        }
    }

    /**
     * Suggest alternative solution
     */
    private String suggestAlternative(String userCode, Bug bug) {
        String fixedCode = bug.getFixedCode();

        // Only suggest if user's approach is different
        if (normalizeCode(userCode).equals(normalizeCode(fixedCode))) {
            return null;
        }

        return "Here's an alternative approach:\n\n" + fixedCode +
                "\n\n💡 " + bug.getExplanation();
    }

    /**
     * Normalize code for comparison
     */
    private String normalizeCode(String code) {
        return code.replaceAll("\\s+", " ")
                .replaceAll("\\s*([{}();,])\\s*", "$1")
                .trim()
                .toLowerCase();
    }

    /**
     * Calculate similarity percentage
     */
    private int calculateSimilarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 100;

        int matches = 0;
        int minLen = Math.min(s1.length(), s2.length());

        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                matches++;
            }
        }

        return (matches * 100) / maxLen;
    }

    /**
     * Format review as string for display
     */
    public static String formatReview(CodeReview review) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║          CODE REVIEW REPORT          ║\n");
        sb.append("╠══════════════════════════════════════╣\n");

        // Score
        String scoreEmoji = review.overallScore >= 80 ? "🌟" : review.overallScore >= 60 ? "👍" : "📝";
        sb.append(String.format("║ Score: %s %d/100                    ║\n", scoreEmoji, review.overallScore));
        sb.append("╠══════════════════════════════════════╣\n");

        // Summary
        sb.append("║ ").append(review.summary).append("\n");
        sb.append("╠══════════════════════════════════════╣\n");

        // Items
        if (!review.items.isEmpty()) {
            sb.append("║ FINDINGS:\n");
            for (ReviewItem item : review.items) {
                String icon = item.severity.equals("error") ? "❌" :
                        item.severity.equals("warning") ? "⚠️" : "ℹ️";
                sb.append("║ ").append(icon).append(" ").append(item.message).append("\n");
                if (item.suggestion != null) {
                    sb.append("║   → ").append(item.suggestion).append("\n");
                }
            }
        }

        // Improvements
        if (!review.improvements.isEmpty()) {
            sb.append("╠══════════════════════════════════════╣\n");
            sb.append("║ SUGGESTIONS:\n");
            for (String improvement : review.improvements) {
                sb.append("║ • ").append(improvement).append("\n");
            }
        }

        sb.append("╚══════════════════════════════════════╝");

        return sb.toString();
    }
}
