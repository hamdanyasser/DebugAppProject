package com.example.debugappproject.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CODE REVIEW AI                                       ║
 * ║              Automated Code Quality Analysis & Suggestions                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class CodeReviewAI {

    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface ReviewCallback {
        void onReviewComplete(CodeReview review);
    }
    
    public static class CodeReview {
        public int qualityScore; // 0-100
        public int starRating;   // 1-5
        public List<Issue> issues = new ArrayList<>();
        public List<String> improvements = new ArrayList<>();
        public List<String> strengths = new ArrayList<>();
        public String summary;
        public boolean isCorrect;
        
        public static class Issue {
            public enum Severity { INFO, WARNING, ERROR, CRITICAL }
            public Severity severity;
            public int lineNumber;
            public String message;
            public String suggestion;
            
            public Issue(Severity severity, int line, String message, String suggestion) {
                this.severity = severity;
                this.lineNumber = line;
                this.message = message;
                this.suggestion = suggestion;
            }
        }
    }
    
    public CodeReviewAI() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Review submitted fix against expected solution
     */
    public void reviewFix(String submittedCode, String expectedCode, String language, ReviewCallback callback) {
        executor.execute(() -> {
            CodeReview review = analyzeCode(submittedCode, expectedCode, language);
            mainHandler.post(() -> callback.onReviewComplete(review));
        });
    }
    
    private CodeReview analyzeCode(String submitted, String expected, String language) {
        CodeReview review = new CodeReview();
        
        // Normalize code for comparison
        String normalizedSubmitted = normalizeCode(submitted);
        String normalizedExpected = normalizeCode(expected);
        
        // Check correctness
        review.isCorrect = normalizedSubmitted.equals(normalizedExpected);
        
        // Analyze code quality
        analyzeQuality(review, submitted, language);
        
        // Check for common issues
        checkCommonIssues(review, submitted, language);
        
        // Check best practices
        checkBestPractices(review, submitted, language);
        
        // Calculate scores
        calculateScores(review);
        
        // Generate summary
        generateSummary(review);
        
        return review;
    }
    
    private String normalizeCode(String code) {
        return code.replaceAll("\\s+", " ")
                   .replaceAll("//.*", "")
                   .replaceAll("/\\*.*?\\*/", "")
                   .trim()
                   .toLowerCase();
    }
    
    private void analyzeQuality(CodeReview review, String code, String language) {
        // Check indentation consistency
        if (hasConsistentIndentation(code)) {
            review.strengths.add("✓ Consistent indentation");
        } else {
            review.improvements.add("Improve indentation consistency");
        }
        
        // Check variable naming
        if (hasDescriptiveNames(code)) {
            review.strengths.add("✓ Descriptive variable names");
        } else {
            review.improvements.add("Use more descriptive variable names");
        }
        
        // Check line length
        for (String line : code.split("\n")) {
            if (line.length() > 120) {
                review.improvements.add("Consider breaking long lines (>120 chars)");
                break;
            }
        }
        
        // Check for comments
        if (code.contains("//") || code.contains("/*")) {
            review.strengths.add("✓ Code is commented");
        } else if (code.split("\n").length > 10) {
            review.improvements.add("Consider adding comments for complex logic");
        }
    }
    
    private void checkCommonIssues(CodeReview review, String code, String language) {
        String[] lines = code.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;
            
            // Check for common Java issues
            if (language.equalsIgnoreCase("Java")) {
                // Null pointer risks
                if (line.contains(".") && !line.contains("null") && 
                    (line.contains("get") || line.contains("find"))) {
                    if (!hasNullCheck(lines, i)) {
                        review.issues.add(new CodeReview.Issue(
                            CodeReview.Issue.Severity.WARNING,
                            lineNum,
                            "Potential null pointer exception",
                            "Add null check before accessing object methods"
                        ));
                    }
                }
                
                // Array index risks
                if (line.contains("[") && line.contains("]")) {
                    if (!hasBoundsCheck(lines, i)) {
                        review.issues.add(new CodeReview.Issue(
                            CodeReview.Issue.Severity.WARNING,
                            lineNum,
                            "Potential array index out of bounds",
                            "Verify array index is within bounds"
                        ));
                    }
                }
                
                // Empty catch blocks
                if (line.trim().equals("catch") || line.contains("catch (")) {
                    if (i + 1 < lines.length && lines[i + 1].trim().equals("}")) {
                        review.issues.add(new CodeReview.Issue(
                            CodeReview.Issue.Severity.ERROR,
                            lineNum,
                            "Empty catch block swallows exceptions",
                            "Log the exception or handle it appropriately"
                        ));
                    }
                }
                
                // Magic numbers
                Pattern magicNumber = Pattern.compile("\\b\\d{2,}\\b");
                Matcher matcher = magicNumber.matcher(line);
                if (matcher.find() && !line.contains("=") && !line.contains("final")) {
                    review.issues.add(new CodeReview.Issue(
                        CodeReview.Issue.Severity.INFO,
                        lineNum,
                        "Magic number detected: " + matcher.group(),
                        "Consider using a named constant"
                    ));
                }
            }
            
            // Check for Python issues
            if (language.equalsIgnoreCase("Python")) {
                // Bare except
                if (line.trim().equals("except:")) {
                    review.issues.add(new CodeReview.Issue(
                        CodeReview.Issue.Severity.WARNING,
                        lineNum,
                        "Bare except catches all exceptions",
                        "Specify the exception type: except ValueError:"
                    ));
                }
                
                // Mutable default argument
                if (line.contains("def ") && (line.contains("=[]") || line.contains("={}"))) {
                    review.issues.add(new CodeReview.Issue(
                        CodeReview.Issue.Severity.ERROR,
                        lineNum,
                        "Mutable default argument",
                        "Use None as default and initialize inside function"
                    ));
                }
            }
            
            // Check for JavaScript issues
            if (language.equalsIgnoreCase("JavaScript")) {
                // var usage
                if (line.contains("var ")) {
                    review.issues.add(new CodeReview.Issue(
                        CodeReview.Issue.Severity.INFO,
                        lineNum,
                        "Using 'var' instead of 'let' or 'const'",
                        "Prefer 'const' for constants, 'let' for variables"
                    ));
                }
                
                // == vs ===
                if (line.contains(" == ") && !line.contains(" === ")) {
                    review.issues.add(new CodeReview.Issue(
                        CodeReview.Issue.Severity.WARNING,
                        lineNum,
                        "Using == instead of ===",
                        "Use === for strict equality comparison"
                    ));
                }
            }
        }
    }
    
    private void checkBestPractices(CodeReview review, String code, String language) {
        // Check function length
        int functionLines = countFunctionLines(code);
        if (functionLines > 30) {
            review.improvements.add("Consider breaking down long functions (>30 lines)");
        } else if (functionLines > 0 && functionLines <= 15) {
            review.strengths.add("✓ Functions are concise");
        }
        
        // Check nesting depth
        int maxNesting = getMaxNestingDepth(code);
        if (maxNesting > 4) {
            review.issues.add(new CodeReview.Issue(
                CodeReview.Issue.Severity.WARNING,
                -1,
                "Deep nesting detected (depth: " + maxNesting + ")",
                "Refactor to reduce nesting using early returns or helper functions"
            ));
        } else if (maxNesting <= 2) {
            review.strengths.add("✓ Low nesting depth - good readability");
        }
        
        // Check for duplicate code (simplified)
        if (hasDuplicateCode(code)) {
            review.improvements.add("Consider extracting duplicate code into a function");
        }
    }
    
    private boolean hasConsistentIndentation(String code) {
        String[] lines = code.split("\n");
        boolean usesSpaces = false;
        boolean usesTabs = false;
        
        for (String line : lines) {
            if (line.startsWith(" ")) usesSpaces = true;
            if (line.startsWith("\t")) usesTabs = true;
        }
        
        return !(usesSpaces && usesTabs);
    }
    
    private boolean hasDescriptiveNames(String code) {
        // Check for single-letter variables (except i, j, k in loops)
        Pattern singleLetter = Pattern.compile("\\b(int|String|var|let|const)\\s+([a-z])\\b");
        Matcher matcher = singleLetter.matcher(code);
        int count = 0;
        while (matcher.find()) {
            String var = matcher.group(2);
            if (!var.equals("i") && !var.equals("j") && !var.equals("k")) {
                count++;
            }
        }
        return count < 2;
    }
    
    private boolean hasNullCheck(String[] lines, int lineIndex) {
        if (lineIndex > 0) {
            String prevLine = lines[lineIndex - 1];
            return prevLine.contains("!= null") || prevLine.contains("== null") || 
                   prevLine.contains("Optional") || prevLine.contains("if (");
        }
        return false;
    }
    
    private boolean hasBoundsCheck(String[] lines, int lineIndex) {
        for (int i = Math.max(0, lineIndex - 3); i < lineIndex; i++) {
            String line = lines[i];
            if (line.contains(".length") || line.contains(".size()") || 
                line.contains("< ") || line.contains("> ")) {
                return true;
            }
        }
        return false;
    }
    
    private int countFunctionLines(String code) {
        int count = 0;
        boolean inFunction = false;
        int braceCount = 0;
        
        for (String line : code.split("\n")) {
            if (line.contains("void ") || line.contains("def ") || 
                line.contains("function ") || line.matches(".*\\w+\\s*\\(.*\\)\\s*\\{?.*")) {
                inFunction = true;
            }
            
            if (inFunction) {
                count++;
                braceCount += countChar(line, '{') - countChar(line, '}');
                if (braceCount <= 0) {
                    inFunction = false;
                }
            }
        }
        return count;
    }
    
    private int getMaxNestingDepth(String code) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        for (char c : code.toCharArray()) {
            if (c == '{' || c == '(') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == '}' || c == ')') {
                currentDepth--;
            }
        }
        return maxDepth;
    }
    
    private boolean hasDuplicateCode(String code) {
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() > 20) {
                for (int j = i + 1; j < lines.length; j++) {
                    if (lines[j].trim().equals(line)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private int countChar(String s, char c) {
        int count = 0;
        for (char ch : s.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }
    
    private void calculateScores(CodeReview review) {
        int score = 100;
        
        // Deduct for issues
        for (CodeReview.Issue issue : review.issues) {
            switch (issue.severity) {
                case CRITICAL: score -= 25; break;
                case ERROR: score -= 15; break;
                case WARNING: score -= 8; break;
                case INFO: score -= 3; break;
            }
        }
        
        // Deduct for improvements needed
        score -= review.improvements.size() * 5;
        
        // Bonus for strengths
        score += review.strengths.size() * 3;
        
        // Bonus for correctness
        if (review.isCorrect) score += 20;
        
        // Clamp score
        review.qualityScore = Math.max(0, Math.min(100, score));
        
        // Calculate star rating
        if (review.qualityScore >= 90) review.starRating = 5;
        else if (review.qualityScore >= 75) review.starRating = 4;
        else if (review.qualityScore >= 60) review.starRating = 3;
        else if (review.qualityScore >= 40) review.starRating = 2;
        else review.starRating = 1;
    }
    
    private void generateSummary(CodeReview review) {
        StringBuilder sb = new StringBuilder();
        
        if (review.isCorrect) {
            sb.append("✅ **Your fix is correct!**\n\n");
        } else {
            sb.append("⚠️ **Your fix differs from the expected solution.**\n\n");
        }
        
        sb.append("**Quality Score:** ").append(review.qualityScore).append("/100 ");
        sb.append("(").append("⭐".repeat(review.starRating)).append(")\n\n");
        
        if (!review.strengths.isEmpty()) {
            sb.append("**Strengths:**\n");
            for (String s : review.strengths) {
                sb.append("• ").append(s).append("\n");
            }
            sb.append("\n");
        }
        
        if (!review.issues.isEmpty()) {
            sb.append("**Issues Found:** ").append(review.issues.size()).append("\n");
            for (CodeReview.Issue issue : review.issues) {
                sb.append("• [").append(issue.severity).append("] ");
                if (issue.lineNumber > 0) sb.append("Line ").append(issue.lineNumber).append(": ");
                sb.append(issue.message).append("\n");
            }
            sb.append("\n");
        }
        
        if (!review.improvements.isEmpty()) {
            sb.append("**Suggestions:**\n");
            for (String imp : review.improvements) {
                sb.append("• ").append(imp).append("\n");
            }
        }
        
        review.summary = sb.toString();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
