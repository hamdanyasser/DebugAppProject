package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.debugappproject.model.Bug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Debug Mentor - Provides intelligent, contextual debugging assistance.
 *
 * This mentor analyzes the user's code and the bug they're working on to provide
 * helpful, educational guidance rather than just showing static hints.
 */
public class AIMentor {

    private static final String PREFS_NAME = "ai_mentor_prefs";
    private static final String KEY_SESSIONS_USED = "sessions_used";
    private static final String KEY_FREE_SESSIONS = "free_sessions";
    private static final String KEY_PURCHASED_SESSIONS = "purchased_sessions";
    private static final String KEY_LAST_FREE_RESET = "last_free_reset";

    // Free sessions reset daily
    private static final int FREE_DAILY_SESSIONS = 3;
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    private final Context context;
    private final SharedPreferences prefs;
    private final Random random = new Random();

    // Chat history for the current session
    private final List<ChatMessage> chatHistory = new ArrayList<>();

    // Current bug context
    private Bug currentBug;
    private String currentUserCode;
    private int attemptCount = 0;

    public AIMentor(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        resetFreeSessions();
    }

    /**
     * Chat message class for conversation history
     */
    public static class ChatMessage {
        public enum Type { USER, MENTOR }

        public final Type type;
        public final String message;
        public final long timestamp;

        public ChatMessage(Type type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Set the current bug context for the mentor
     */
    public void setBugContext(Bug bug) {
        this.currentBug = bug;
        this.attemptCount = 0;
        this.chatHistory.clear();
    }

    /**
     * Update the user's current code
     */
    public void setUserCode(String code) {
        this.currentUserCode = code;
    }

    /**
     * Record an attempt (for tracking when to give more detailed hints)
     */
    public void recordAttempt() {
        this.attemptCount++;
    }

    /**
     * Get greeting message when mentor is opened
     */
    public String getGreeting() {
        if (currentBug == null) {
            return "Hi! I'm your AI Debug Mentor. Load a bug challenge and I'll help you debug it!";
        }

        String[] greetings = {
            "Hey! I see you're working on \"" + currentBug.getTitle() + "\". How can I help?",
            "Welcome! This is a " + currentBug.getDifficulty() + " " + currentBug.getCategory() + " bug. What would you like to know?",
            "Ready to squash some bugs! I'm here to guide you through \"" + currentBug.getTitle() + "\".",
            "Hi there! Let's tackle this " + currentBug.getDifficulty() + " challenge together. What's on your mind?"
        };

        return greetings[random.nextInt(greetings.length)];
    }

    /**
     * Main method to get mentor response based on user question
     */
    public String getResponse(String userQuestion) {
        if (currentBug == null) {
            return "Please load a bug challenge first, then I can help you debug it!";
        }

        // Add to chat history
        chatHistory.add(new ChatMessage(ChatMessage.Type.USER, userQuestion));

        String response = analyzeAndRespond(userQuestion.toLowerCase());

        chatHistory.add(new ChatMessage(ChatMessage.Type.MENTOR, response));

        return response;
    }

    /**
     * Analyze user question and generate contextual response
     */
    private String analyzeAndRespond(String question) {
        // Detect what the user is asking about

        // Help understanding the bug
        if (containsAny(question, "what", "explain", "understand", "mean", "bug", "problem", "issue")) {
            return explainBug();
        }

        // Looking for hints
        if (containsAny(question, "hint", "clue", "help", "stuck", "idea", "tip")) {
            return provideContextualHint();
        }

        // Asking about their code
        if (containsAny(question, "code", "wrong", "fix", "error", "mistake", "check")) {
            return analyzeUserCode();
        }

        // Asking about concepts
        if (containsAny(question, "how", "why", "what is", "concept", "learn", "teach")) {
            return explainConcept();
        }

        // Looking for the solution
        if (containsAny(question, "solution", "answer", "show", "give", "tell me")) {
            return handleSolutionRequest();
        }

        // Encouragement
        if (containsAny(question, "can't", "cannot", "impossible", "hard", "difficult", "give up")) {
            return provideEncouragement();
        }

        // Strategy
        if (containsAny(question, "approach", "strategy", "start", "begin", "where", "first")) {
            return suggestStrategy();
        }

        // Default helpful response
        return getDefaultResponse();
    }

    /**
     * Explain the current bug in detail
     */
    private String explainBug() {
        StringBuilder sb = new StringBuilder();

        sb.append("Let me break down this bug for you:\n\n");
        sb.append("**Category:** ").append(currentBug.getCategory()).append("\n");
        sb.append("**Difficulty:** ").append(currentBug.getDifficulty()).append("\n");
        sb.append("**Language:** ").append(currentBug.getLanguage()).append("\n\n");

        // Provide category-specific explanation
        String category = currentBug.getCategory().toLowerCase();

        if (category.contains("syntax")) {
            sb.append("This is a **syntax error** - the code has incorrect grammar that prevents it from running.\n\n");
            sb.append("Common syntax issues include:\n");
            sb.append("- Missing semicolons or brackets\n");
            sb.append("- Typos in keywords\n");
            sb.append("- Incorrect operator usage\n");
        } else if (category.contains("logic")) {
            sb.append("This is a **logic error** - the code runs but produces wrong results.\n\n");
            sb.append("Logic errors often involve:\n");
            sb.append("- Wrong conditions in if/while statements\n");
            sb.append("- Off-by-one errors in loops\n");
            sb.append("- Incorrect mathematical operations\n");
        } else if (category.contains("runtime") || category.contains("exception")) {
            sb.append("This is a **runtime error** - the code crashes while running.\n\n");
            sb.append("Common runtime issues:\n");
            sb.append("- NullPointerException from uninitialized variables\n");
            sb.append("- ArrayIndexOutOfBoundsException\n");
            sb.append("- Division by zero\n");
        } else if (category.contains("type")) {
            sb.append("This is a **type error** - there's a mismatch between data types.\n\n");
            sb.append("Type issues include:\n");
            sb.append("- Comparing incompatible types\n");
            sb.append("- Wrong variable declarations\n");
            sb.append("- Missing type conversions\n");
        }

        sb.append("\n**What the code should do:** ").append(currentBug.getExpectedOutput());
        sb.append("\n**What it currently does:** ").append(currentBug.getActualOutput());

        return sb.toString();
    }

    /**
     * Provide contextual hints based on attempts and bug type
     */
    private String provideContextualHint() {
        StringBuilder sb = new StringBuilder();

        // More specific hints as attempts increase
        if (attemptCount < 2) {
            sb.append("Here's a starting hint:\n\n");
            sb.append(getGeneralHint());
        } else if (attemptCount < 5) {
            sb.append("Let me give you a more specific hint:\n\n");
            sb.append(getSpecificHint());
        } else {
            sb.append("You've been working hard! Here's a detailed hint:\n\n");
            sb.append(getDetailedHint());
        }

        return sb.toString();
    }

    private String getGeneralHint() {
        String category = currentBug.getCategory().toLowerCase();

        Map<String, String[]> categoryHints = new HashMap<>();
        categoryHints.put("syntax", new String[]{
            "Look carefully at each line - is there something missing or misplaced?",
            "Check your brackets, parentheses, and semicolons.",
            "Compare the structure of your code with valid examples."
        });
        categoryHints.put("logic", new String[]{
            "Trace through the code mentally - what happens step by step?",
            "Check your conditions - are they checking what you think they're checking?",
            "Consider edge cases - what happens with 0, negative numbers, or empty values?"
        });
        categoryHints.put("runtime", new String[]{
            "What could cause the code to crash? Think about uninitialized values.",
            "Check array/list accesses - are indices always valid?",
            "Consider what happens if a variable is null."
        });
        categoryHints.put("type", new String[]{
            "Look at variable types - do they match what you're trying to do?",
            "Check if you need to convert between types.",
            "Make sure comparisons are between compatible types."
        });

        for (Map.Entry<String, String[]> entry : categoryHints.entrySet()) {
            if (category.contains(entry.getKey())) {
                return entry.getValue()[random.nextInt(entry.getValue().length)];
            }
        }

        // Default hints
        String[] defaults = {
            "Read the code line by line and question each part.",
            "Compare the expected vs actual output - what's different?",
            "Think about what each variable contains at each step."
        };
        return defaults[random.nextInt(defaults.length)];
    }

    private String getSpecificHint() {
        StringBuilder sb = new StringBuilder();

        // Analyze the broken code for common patterns
        String brokenCode = currentBug.getBrokenCode();

        if (brokenCode.contains("for") || brokenCode.contains("while")) {
            sb.append("Focus on the **loop**: \n");
            sb.append("- Is the starting value correct?\n");
            sb.append("- Is the condition right?\n");
            sb.append("- Is the increment/update correct?\n");
        } else if (brokenCode.contains("if") || brokenCode.contains("else")) {
            sb.append("Focus on the **conditions**: \n");
            sb.append("- Are you using the right comparison operator? (==, !=, <, >, <=, >=)\n");
            sb.append("- Should it be && (AND) or || (OR)?\n");
            sb.append("- Is the logic inverted?\n");
        } else if (brokenCode.contains("[")) {
            sb.append("Focus on **array access**: \n");
            sb.append("- Are indices within bounds (0 to length-1)?\n");
            sb.append("- Is there an off-by-one error?\n");
        } else if (brokenCode.contains("return")) {
            sb.append("Focus on the **return statement**: \n");
            sb.append("- Is it returning the right value?\n");
            sb.append("- Is it in the right place?\n");
            sb.append("- Should there be multiple return paths?\n");
        }

        if (currentBug.getHintText() != null && !currentBug.getHintText().isEmpty()) {
            sb.append("\n**Bug-specific hint:** ").append(currentBug.getHintText());
        }

        return sb.toString();
    }

    private String getDetailedHint() {
        StringBuilder sb = new StringBuilder();

        sb.append("Let me walk you through the debugging process:\n\n");
        sb.append("1. **The expected behavior:** ").append(currentBug.getExpectedOutput()).append("\n\n");
        sb.append("2. **The current behavior:** ").append(currentBug.getActualOutput()).append("\n\n");
        sb.append("3. **The gap to bridge:** You need to change the code so it produces the expected output.\n\n");

        // Give a strong hint about where the bug is
        String explanation = currentBug.getExplanation();
        if (explanation != null && !explanation.isEmpty()) {
            // Extract first sentence as a hint
            int endIndex = explanation.indexOf('.');
            if (endIndex > 0 && endIndex < 100) {
                sb.append("4. **Key insight:** ").append(explanation.substring(0, endIndex + 1)).append("\n\n");
            }
        }

        sb.append("Look at your code and find where this behavior could be caused!");

        return sb.toString();
    }

    /**
     * Analyze the user's current code and provide feedback
     */
    private String analyzeUserCode() {
        if (currentUserCode == null || currentUserCode.isEmpty()) {
            return "I don't see any code to analyze yet. Try writing a fix first!";
        }

        StringBuilder sb = new StringBuilder();
        String fixedCode = currentBug.getFixedCode();
        String brokenCode = currentBug.getBrokenCode();

        // Normalize for comparison
        String normalizedUser = normalizeCode(currentUserCode);
        String normalizedFixed = normalizeCode(fixedCode);
        String normalizedBroken = normalizeCode(brokenCode);

        // Check if still original broken code
        if (normalizedUser.equals(normalizedBroken)) {
            sb.append("Your code is still the original broken version. You need to make changes to fix the bug!\n\n");
            sb.append("**Suggestion:** Look for the part that's causing the wrong output and modify it.");
            return sb.toString();
        }

        // Check similarity to correct solution
        double similarity = calculateSimilarity(normalizedUser, normalizedFixed);

        if (similarity >= 0.95) {
            sb.append("Your code looks **very close** to the correct solution! ");
            sb.append("Check for any minor differences like spacing or variable names.\n\n");
            sb.append("Try running the tests to verify!");
        } else if (similarity >= 0.80) {
            sb.append("You're **almost there**! Your code is about ").append((int)(similarity * 100)).append("% similar to the solution.\n\n");
            sb.append("There might be a small detail you're missing. Double-check:\n");
            sb.append("- The exact operators used\n");
            sb.append("- The order of operations\n");
            sb.append("- Any missing or extra characters\n");
        } else if (similarity >= 0.50) {
            sb.append("You're making progress! Your approach is on the right track.\n\n");
            sb.append("Your code is about ").append((int)(similarity * 100)).append("% similar to the solution.\n\n");
            sb.append("Consider:\n");
            sb.append("- Is your fix targeting the right part of the code?\n");
            sb.append("- Are you using the correct approach for this type of bug?\n");
        } else {
            sb.append("Your code has diverged quite a bit from the expected solution.\n\n");
            sb.append("Let me suggest:\n");
            sb.append("1. Reset to the original code and start fresh\n");
            sb.append("2. Focus on understanding what the bug is first\n");
            sb.append("3. Make only the minimum change needed\n\n");
            sb.append("Would you like a hint about where the bug is?");
        }

        // Detect common issues
        detectCommonIssues(sb, currentUserCode);

        return sb.toString();
    }

    private void detectCommonIssues(StringBuilder sb, String code) {
        List<String> issues = new ArrayList<>();

        // Check for common mistakes
        if (code.contains("= ") && code.contains("==") == false && code.contains("if")) {
            if (containsAssignmentInCondition(code)) {
                issues.add("- You might be using assignment (=) instead of comparison (==) in a condition");
            }
        }

        if (code.contains("i <= ") && code.contains(".length")) {
            issues.add("- Check your loop bounds - using <= with .length can cause out-of-bounds errors");
        }

        if (code.contains("null") == false && currentBug.getCategory().toLowerCase().contains("null")) {
            issues.add("- This bug involves null values - make sure you handle that case");
        }

        if (!issues.isEmpty()) {
            sb.append("\n\n**Potential issues I noticed:**\n");
            for (String issue : issues) {
                sb.append(issue).append("\n");
            }
        }
    }

    private boolean containsAssignmentInCondition(String code) {
        // Simple heuristic: look for "if" followed by "=" but not "==" or "!="
        Pattern pattern = Pattern.compile("if\\s*\\([^)]*[^!=<>]=[^=][^)]*\\)");
        return pattern.matcher(code).find();
    }

    /**
     * Explain the concept behind the bug
     */
    private String explainConcept() {
        StringBuilder sb = new StringBuilder();
        String category = currentBug.getCategory().toLowerCase();

        sb.append("Let me explain the concept behind this bug:\n\n");

        if (category.contains("loop") || currentBug.getBrokenCode().contains("for") || currentBug.getBrokenCode().contains("while")) {
            sb.append("**Loops in Programming:**\n\n");
            sb.append("Loops repeat code multiple times. Common issues:\n");
            sb.append("- **Off-by-one errors**: Starting at wrong index or wrong ending condition\n");
            sb.append("- **Infinite loops**: Condition never becomes false\n");
            sb.append("- **Wrong increment**: Not updating loop variable correctly\n\n");
            sb.append("```\nfor (int i = 0; i < array.length; i++)\n");
            sb.append("    ^          ^                    ^\n");
            sb.append("  start     condition            update\n```");
        } else if (category.contains("condition") || category.contains("logic")) {
            sb.append("**Conditional Logic:**\n\n");
            sb.append("Conditionals control which code runs. Key operators:\n");
            sb.append("- `==` equals (comparison)\n");
            sb.append("- `!=` not equals\n");
            sb.append("- `&&` AND (both must be true)\n");
            sb.append("- `||` OR (at least one must be true)\n");
            sb.append("- `!` NOT (inverts true/false)\n\n");
            sb.append("**Common mistake:** Using `=` (assignment) instead of `==` (comparison)");
        } else if (category.contains("null")) {
            sb.append("**Null References:**\n\n");
            sb.append("Null means \"nothing\" or \"no value\". Trying to use a null value causes crashes.\n\n");
            sb.append("**Prevention:**\n");
            sb.append("- Always check `if (variable != null)` before using\n");
            sb.append("- Initialize variables when you declare them\n");
            sb.append("- Use default values when possible");
        } else if (category.contains("array") || category.contains("index")) {
            sb.append("**Arrays and Indices:**\n\n");
            sb.append("Arrays start at index 0, not 1!\n\n");
            sb.append("```\narray[0] = first element\n");
            sb.append("array[length-1] = last element\n");
            sb.append("array[length] = OUT OF BOUNDS!\n```\n\n");
            sb.append("**Common off-by-one errors:**\n");
            sb.append("- Starting loop at 1 instead of 0\n");
            sb.append("- Using <= instead of < in loop condition");
        } else {
            sb.append("**Debugging Fundamentals:**\n\n");
            sb.append("1. **Understand the expected behavior** - What should happen?\n");
            sb.append("2. **Identify the actual behavior** - What is happening?\n");
            sb.append("3. **Find the gap** - What's causing the difference?\n");
            sb.append("4. **Fix the minimum** - Change only what's necessary\n");
        }

        return sb.toString();
    }

    /**
     * Handle requests for the solution
     */
    private String handleSolutionRequest() {
        StringBuilder sb = new StringBuilder();

        sb.append("I want to help you learn, not just give you the answer!\n\n");

        if (attemptCount < 3) {
            sb.append("You haven't tried many approaches yet. Let me give you a strong hint instead:\n\n");
            sb.append(getDetailedHint());
            sb.append("\n\nTry a few more times, then if you're still stuck, you can use the 'Show Solution' button.");
        } else {
            sb.append("You've been working hard on this! Here's what I suggest:\n\n");
            sb.append("1. If you want to learn the most: Try one more approach with my hints\n");
            sb.append("2. If you're ready to move on: Use the 'Show Solution' button below the code\n\n");
            sb.append("The solution will show you both the fix AND an explanation of why it works.");
        }

        return sb.toString();
    }

    /**
     * Provide encouragement when user is struggling
     */
    private String provideEncouragement() {
        String[] encouragements = {
            "Every great programmer has been stuck on bugs before - this is how we learn!\n\n" +
            "Let's break it down into smaller steps. What specifically is confusing you?",

            "Don't give up! Debugging is literally the process of being stuck and then figuring it out.\n\n" +
            "Take a breath, look at the code fresh, and focus on one thing at a time.",

            "I believe in you! This bug CAN be solved.\n\n" +
            "Sometimes stepping away for a moment helps. When you come back, try reading the code out loud.",

            "Struggling means you're learning! The bugs that challenge us the most teach us the most.\n\n" +
            "Let's approach this differently - what do you think the bug might be?",

            "You've got this! Remember: even 'easy' bugs can trip up experienced programmers.\n\n" +
            "Would you like me to explain the concept behind this bug type?"
        };

        return encouragements[random.nextInt(encouragements.length)];
    }

    /**
     * Suggest a debugging strategy
     */
    private String suggestStrategy() {
        StringBuilder sb = new StringBuilder();

        sb.append("Here's how I'd approach this bug:\n\n");

        sb.append("**Step 1: Understand the Goal**\n");
        sb.append("Expected output: ").append(currentBug.getExpectedOutput()).append("\n\n");

        sb.append("**Step 2: Identify the Problem**\n");
        sb.append("Actual output: ").append(currentBug.getActualOutput()).append("\n\n");

        sb.append("**Step 3: Find the Culprit**\n");
        sb.append("Look through the code and ask: \"Which line could cause this wrong behavior?\"\n\n");

        sb.append("**Step 4: Make a Targeted Fix**\n");
        sb.append("Change only what's necessary - don't rewrite everything!\n\n");

        sb.append("**Step 5: Test**\n");
        sb.append("Run your fix and see if the output matches the expected result.\n\n");

        sb.append("Start with Step 3 - can you identify which line looks suspicious?");

        return sb.toString();
    }

    /**
     * Default response when question isn't recognized
     */
    private String getDefaultResponse() {
        String[] responses = {
            "I'm not sure what you're asking. Try asking about:\n" +
            "- The bug explanation ('What is this bug?')\n" +
            "- Hints ('Give me a hint')\n" +
            "- Code analysis ('Check my code')\n" +
            "- Concepts ('How do loops work?')",

            "Could you rephrase that? I can help with:\n" +
            "- Explaining the bug\n" +
            "- Providing hints\n" +
            "- Analyzing your code\n" +
            "- Teaching concepts",

            "Let me help you another way. What would be most useful?\n" +
            "1. An explanation of the bug\n" +
            "2. A hint to guide you\n" +
            "3. Feedback on your code\n" +
            "4. Learning about the concept"
        };

        return responses[random.nextInt(responses.length)];
    }

    // ============ Session Management ============

    /**
     * Check and reset free sessions if a new day has started
     */
    private void resetFreeSessions() {
        long lastReset = prefs.getLong(KEY_LAST_FREE_RESET, 0);
        long now = System.currentTimeMillis();

        if (now - lastReset >= DAY_MILLIS) {
            prefs.edit()
                .putInt(KEY_FREE_SESSIONS, FREE_DAILY_SESSIONS)
                .putLong(KEY_LAST_FREE_RESET, now)
                .apply();
        }
    }

    /**
     * Get remaining free sessions today
     */
    public int getFreeSessions() {
        resetFreeSessions();
        return prefs.getInt(KEY_FREE_SESSIONS, FREE_DAILY_SESSIONS);
    }

    /**
     * Get purchased sessions
     */
    public int getPurchasedSessions() {
        return prefs.getInt(KEY_PURCHASED_SESSIONS, 0);
    }

    /**
     * Get total available sessions
     */
    public int getTotalSessions() {
        return getFreeSessions() + getPurchasedSessions();
    }

    /**
     * Use one session (returns false if none available)
     */
    public boolean useSession() {
        int free = getFreeSessions();
        int purchased = getPurchasedSessions();

        if (free > 0) {
            prefs.edit().putInt(KEY_FREE_SESSIONS, free - 1).apply();
            return true;
        } else if (purchased > 0) {
            prefs.edit().putInt(KEY_PURCHASED_SESSIONS, purchased - 1).apply();
            return true;
        }

        return false;
    }

    /**
     * Add purchased sessions
     */
    public void addPurchasedSessions(int count) {
        int current = getPurchasedSessions();
        prefs.edit().putInt(KEY_PURCHASED_SESSIONS, current + count).apply();
    }

    /**
     * Check if user has unlimited mentor access (Pro subscription)
     */
    public static boolean hasUnlimitedAccess(Context context) {
        // Check Pro subscription status
        try {
            return com.example.debugappproject.billing.BillingManager.getInstance(context).isProUserSync();
        } catch (Exception e) {
            return false;
        }
    }

    // ============ Utility Methods ============

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeCode(String code) {
        return code.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private double calculateSimilarity(String a, String b) {
        if (a == null || b == null) return 0;
        if (a.equals(b)) return 1.0;

        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(a, b);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    /**
     * Get quick action buttons for the mentor UI
     */
    public static String[] getQuickActions() {
        return new String[]{
            "Explain the bug",
            "Give me a hint",
            "Check my code",
            "How do I start?",
            "I'm stuck!"
        };
    }

    /**
     * Get the chat history
     */
    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    /**
     * Clear chat history
     */
    public void clearChatHistory() {
        chatHistory.clear();
    }
}
