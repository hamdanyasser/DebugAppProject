package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.debugappproject.model.Bug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Debug Mentor - Smart debugging assistant that actually analyzes code
 * and provides genuinely helpful, specific feedback.
 */
public class AIMentor {

    private static final String PREFS_NAME = "ai_mentor_prefs";
    private static final String KEY_FREE_SESSIONS = "free_sessions";
    private static final String KEY_PURCHASED_SESSIONS = "purchased_sessions";
    private static final String KEY_LAST_FREE_RESET = "last_free_reset";

    private static final int FREE_DAILY_SESSIONS = 3;
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    private final Context context;
    private final SharedPreferences prefs;
    private final List<ChatMessage> chatHistory = new ArrayList<>();

    private Bug currentBug;
    private String currentUserCode;
    private int hintLevel = 0;

    // Cached analysis results
    private String bugLocation;
    private String bugType;
    private String exactFix;

    public AIMentor(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        resetFreeSessions();
    }

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

    public void setBugContext(Bug bug) {
        this.currentBug = bug;
        this.hintLevel = 0;
        this.chatHistory.clear();

        // Analyze the bug immediately
        if (bug != null) {
            analyzeBugDifference();
        }
    }

    public void setUserCode(String code) {
        this.currentUserCode = code;
    }

    /**
     * Analyze the difference between broken and fixed code to understand the bug
     */
    private void analyzeBugDifference() {
        if (currentBug == null) return;

        String broken = currentBug.getBrokenCode();
        String fixed = currentBug.getFixedCode();

        if (broken == null || fixed == null) return;

        // Split into lines and find differences
        String[] brokenLines = broken.split("\n");
        String[] fixedLines = fixed.split("\n");

        StringBuilder locationBuilder = new StringBuilder();
        StringBuilder fixBuilder = new StringBuilder();

        for (int i = 0; i < Math.max(brokenLines.length, fixedLines.length); i++) {
            String brokenLine = i < brokenLines.length ? brokenLines[i].trim() : "";
            String fixedLine = i < fixedLines.length ? fixedLines[i].trim() : "";

            if (!brokenLine.equals(fixedLine) && !brokenLine.isEmpty()) {
                locationBuilder.append("Line ").append(i + 1).append(": ").append(brokenLine);
                if (fixBuilder.length() == 0) {
                    fixBuilder.append(fixedLine);
                }
                break;
            }
        }

        this.bugLocation = locationBuilder.toString();
        this.exactFix = fixBuilder.toString();

        // Determine bug type from analysis
        this.bugType = detectBugType(broken, fixed);
    }

    private String detectBugType(String broken, String fixed) {
        // Compare to find what changed
        if (broken.contains("==") && fixed.contains("=") && !fixed.contains("==")) {
            return "WRONG_OPERATOR_COMPARISON";
        }
        if (broken.contains("=") && !broken.contains("==") && fixed.contains("==")) {
            return "ASSIGNMENT_VS_COMPARISON";
        }
        if (broken.contains("<=") && fixed.contains("<") || broken.contains("<") && fixed.contains("<=")) {
            return "OFF_BY_ONE_COMPARISON";
        }
        if (broken.contains(">=") && fixed.contains(">") || broken.contains(">") && fixed.contains(">=")) {
            return "OFF_BY_ONE_COMPARISON";
        }
        if (broken.contains("&&") && fixed.contains("||") || broken.contains("||") && fixed.contains("&&")) {
            return "WRONG_LOGICAL_OPERATOR";
        }
        if (broken.contains("++") && fixed.contains("--") || broken.contains("--") && fixed.contains("++")) {
            return "WRONG_INCREMENT";
        }
        if (broken.contains("+ ") && fixed.contains("- ") || broken.contains("- ") && fixed.contains("+ ")) {
            return "WRONG_ARITHMETIC";
        }
        if (broken.contains("* ") && fixed.contains("/ ") || broken.contains("/ ") && fixed.contains("* ")) {
            return "WRONG_ARITHMETIC";
        }
        if (broken.contains(".length") && fixed.contains(".length - 1") ||
            broken.contains(".length - 1") && fixed.contains(".length")) {
            return "ARRAY_BOUNDS";
        }
        if (broken.contains("null") || fixed.contains("null") ||
            broken.contains("!= null") || fixed.contains("!= null")) {
            return "NULL_CHECK";
        }
        if (broken.contains("return") && fixed.contains("return")) {
            return "WRONG_RETURN";
        }
        if (broken.contains("static") != fixed.contains("static")) {
            return "STATIC_CONTEXT";
        }
        if (broken.contains("this.") != fixed.contains("this.")) {
            return "INSTANCE_REFERENCE";
        }

        return "GENERAL";
    }

    public String getGreeting() {
        if (currentBug == null) {
            return "Hi! I'm your Debug Mentor. Load a bug challenge and I'll help you find and fix it!";
        }

        String title = currentBug.getTitle();
        String difficulty = currentBug.getDifficulty();

        return "Hi! I'm here to help you with \"" + title + "\" (" + difficulty + ").\n\n" +
               "I've analyzed this bug and I'm ready to guide you. You can:\n" +
               "• Ask me to explain the bug\n" +
               "• Request a hint (I'll give progressively more specific hints)\n" +
               "• Ask me to check your code\n" +
               "• Ask about the concept involved\n\n" +
               "What would you like to know?";
    }

    public String getResponse(String userQuestion) {
        if (currentBug == null) {
            return "Please load a bug challenge first!";
        }

        chatHistory.add(new ChatMessage(ChatMessage.Type.USER, userQuestion));

        String response = generateSmartResponse(userQuestion.toLowerCase());

        chatHistory.add(new ChatMessage(ChatMessage.Type.MENTOR, response));

        return response;
    }

    private String generateSmartResponse(String question) {
        // Explain the bug
        if (containsAny(question, "explain", "what", "bug", "problem", "wrong", "issue", "understand")) {
            return explainBugIntelligently();
        }

        // Give hint
        if (containsAny(question, "hint", "help", "stuck", "clue", "tip")) {
            return giveProgressiveHint();
        }

        // Check code
        if (containsAny(question, "check", "code", "my code", "analyze", "review", "look at")) {
            return analyzeUserCodeIntelligently();
        }

        // How to start / strategy
        if (containsAny(question, "start", "begin", "approach", "strategy", "how do i", "where")) {
            return giveDebuggingStrategy();
        }

        // Concept explanation
        if (containsAny(question, "concept", "learn", "teach", "how does", "why does", "what is")) {
            return explainRelevantConcept();
        }

        // Solution request
        if (containsAny(question, "solution", "answer", "fix", "tell me", "show me", "give me")) {
            return handleSolutionRequest();
        }

        // Encouragement
        if (containsAny(question, "can't", "hard", "difficult", "give up", "impossible", "frustrated")) {
            return giveEncouragement();
        }

        // Default - try to be helpful
        return giveContextualHelp();
    }

    private String explainBugIntelligently() {
        StringBuilder sb = new StringBuilder();

        sb.append("Here's what's happening with this bug:\n\n");

        // What the code should do
        sb.append("EXPECTED BEHAVIOR:\n");
        sb.append(currentBug.getExpectedOutput()).append("\n\n");

        // What it actually does
        sb.append("ACTUAL BEHAVIOR:\n");
        sb.append(currentBug.getActualOutput()).append("\n\n");

        // Type of bug
        sb.append("BUG TYPE: ").append(currentBug.getCategory()).append("\n\n");

        // Specific explanation based on bug type
        switch (bugType) {
            case "ASSIGNMENT_VS_COMPARISON":
                sb.append("This is a classic mistake: using = (assignment) instead of == (comparison).\n");
                sb.append("When you write 'if (x = 5)', you're setting x to 5, not checking if x equals 5!");
                break;
            case "OFF_BY_ONE_COMPARISON":
                sb.append("This is an 'off-by-one' error in a comparison.\n");
                sb.append("The difference between < and <= (or > and >=) determines if the boundary value is included.");
                break;
            case "WRONG_LOGICAL_OPERATOR":
                sb.append("The logical operator is wrong. Remember:\n");
                sb.append("• && (AND) = BOTH conditions must be true\n");
                sb.append("• || (OR) = AT LEAST ONE condition must be true");
                break;
            case "WRONG_INCREMENT":
                sb.append("The increment/decrement is going the wrong direction.\n");
                sb.append("• ++ increases by 1\n");
                sb.append("• -- decreases by 1");
                break;
            case "WRONG_ARITHMETIC":
                sb.append("There's a wrong arithmetic operator (+, -, *, /).\n");
                sb.append("Think about what mathematical operation should actually happen here.");
                break;
            case "ARRAY_BOUNDS":
                sb.append("There's an array indexing issue.\n");
                sb.append("Remember: arrays go from index 0 to length-1.\n");
                sb.append("Accessing array[length] is OUT OF BOUNDS!");
                break;
            case "NULL_CHECK":
                sb.append("There's a null reference issue.\n");
                sb.append("The code tries to use something that might be null without checking first.");
                break;
            case "STATIC_CONTEXT":
                sb.append("This is a static vs instance context error.\n");
                sb.append("Static methods belong to the class, not to instances.\n");
                sb.append("You can't use instance variables/methods from a static context without an object.");
                break;
            case "INSTANCE_REFERENCE":
                sb.append("There's an issue with 'this' reference.\n");
                sb.append("'this' refers to the current object instance.");
                break;
            default:
                sb.append("Look for the line that differs from what you'd expect.\n");
                sb.append("Compare what the code does vs what it should do.");
        }

        return sb.toString();
    }

    private String giveProgressiveHint() {
        hintLevel++;
        StringBuilder sb = new StringBuilder();

        sb.append("HINT #").append(hintLevel).append(":\n\n");

        if (hintLevel == 1) {
            // Very general hint
            sb.append("The bug is in the ").append(currentBug.getCategory().toLowerCase()).append(" category.\n\n");
            sb.append("Look at the expected vs actual output. What's the difference?\n");
            sb.append("Then find which line of code could cause that difference.");
        } else if (hintLevel == 2) {
            // More specific - what type of error
            switch (bugType) {
                case "ASSIGNMENT_VS_COMPARISON":
                    sb.append("Look at your if-statements. Are you comparing values or accidentally assigning them?");
                    break;
                case "OFF_BY_ONE_COMPARISON":
                    sb.append("Check your comparison operators (< vs <=, or > vs >=).\n");
                    sb.append("Is the boundary value being handled correctly?");
                    break;
                case "WRONG_LOGICAL_OPERATOR":
                    sb.append("Look at your && and || operators.\n");
                    sb.append("Should BOTH conditions be true, or just ONE?");
                    break;
                case "WRONG_INCREMENT":
                    sb.append("Check if values are increasing when they should decrease, or vice versa.");
                    break;
                case "ARRAY_BOUNDS":
                    sb.append("Look at array index calculations.\n");
                    sb.append("Are you accessing valid indices (0 to length-1)?");
                    break;
                case "STATIC_CONTEXT":
                    sb.append("Look for 'static' keywords.\n");
                    sb.append("Is something trying to access an instance member from a static context?");
                    break;
                default:
                    sb.append("Focus on: ").append(currentBug.getHintText() != null ? currentBug.getHintText() : "the main logic of the code");
            }
        } else if (hintLevel == 3) {
            // Tell them where the bug is
            if (bugLocation != null && !bugLocation.isEmpty()) {
                sb.append("The bug is here:\n");
                sb.append(bugLocation);
            } else {
                sb.append("The bug is in one of the core logic lines.\n");
                sb.append("Compare each operator and value carefully.");
            }
        } else {
            // Very strong hint - almost the answer
            sb.append("Here's exactly what needs to change:\n\n");

            String broken = currentBug.getBrokenCode();
            String fixed = currentBug.getFixedCode();

            // Find and show the specific difference
            String[] brokenLines = broken.split("\n");
            String[] fixedLines = fixed.split("\n");

            for (int i = 0; i < Math.min(brokenLines.length, fixedLines.length); i++) {
                if (!brokenLines[i].trim().equals(fixedLines[i].trim())) {
                    sb.append("WRONG: ").append(brokenLines[i].trim()).append("\n");
                    sb.append("SHOULD BE: ").append(fixedLines[i].trim()).append("\n\n");
                    sb.append("Can you see the difference?");
                    break;
                }
            }
        }

        return sb.toString();
    }

    private String analyzeUserCodeIntelligently() {
        if (currentUserCode == null || currentUserCode.trim().isEmpty()) {
            return "I don't see your code yet. Make some changes to the code editor first, then ask me to check it!";
        }

        StringBuilder sb = new StringBuilder();

        String broken = currentBug.getBrokenCode();
        String fixed = currentBug.getFixedCode();
        String user = currentUserCode;

        // Normalize for comparison
        String normUser = normalizeCode(user);
        String normBroken = normalizeCode(broken);
        String normFixed = normalizeCode(fixed);

        if (normUser.equals(normBroken)) {
            sb.append("You haven't made any changes yet!\n\n");
            sb.append("The code is still the original buggy version.\n");
            sb.append("Try to find and fix the bug - would you like a hint?");
            return sb.toString();
        }

        if (normUser.equals(normFixed)) {
            sb.append("PERFECT! Your code matches the correct solution!\n\n");
            sb.append("Click the 'Check Fix' button to verify and earn your XP!");
            return sb.toString();
        }

        // Check similarity to solution
        double similarity = calculateSimilarity(normUser, normFixed);

        if (similarity >= 0.95) {
            sb.append("ALMOST THERE! Your code is 95%+ correct!\n\n");
            sb.append("There might be a tiny difference - check for:\n");
            sb.append("• Extra or missing spaces\n");
            sb.append("• Slightly different variable names\n");
            sb.append("• Minor punctuation differences");
        } else if (similarity >= 0.80) {
            sb.append("You're very close! About ").append((int)(similarity * 100)).append("% there.\n\n");
            sb.append("Your approach is right, but something small is off.\n");
            findAndShowDifference(sb, user, fixed);
        } else if (similarity >= 0.60) {
            sb.append("Good progress! You're on the right track.\n\n");
            sb.append("But the fix isn't quite right yet.\n");
            findAndShowDifference(sb, user, fixed);
        } else {
            sb.append("Hmm, your changes have gone in a different direction.\n\n");
            sb.append("The fix should be simpler - usually just one small change.\n");
            sb.append("Try resetting the code and focusing on just the buggy line.");
        }

        // Check for common mistakes in user code
        checkForCommonMistakes(sb, user);

        return sb.toString();
    }

    private void findAndShowDifference(StringBuilder sb, String user, String fixed) {
        String[] userLines = user.split("\n");
        String[] fixedLines = fixed.split("\n");

        for (int i = 0; i < Math.min(userLines.length, fixedLines.length); i++) {
            String userLine = userLines[i].trim();
            String fixedLine = fixedLines[i].trim();

            if (!userLine.equals(fixedLine) && !userLine.isEmpty() && !fixedLine.isEmpty()) {
                // Check if this is likely the key line
                if (fixedLine.contains("if") || fixedLine.contains("for") ||
                    fixedLine.contains("while") || fixedLine.contains("return")) {
                    sb.append("\nLook at line ").append(i + 1).append(":\n");
                    sb.append("Your code: ").append(userLine).append("\n");
                    sb.append("Compare the operators and values carefully.");
                    return;
                }
            }
        }
    }

    private void checkForCommonMistakes(StringBuilder sb, String code) {
        List<String> issues = new ArrayList<>();

        // Check for = instead of == in conditions
        if (Pattern.compile("if\\s*\\([^)]*[^!=<>]=[^=]").matcher(code).find()) {
            issues.add("• Possible = instead of == in a condition");
        }

        // Check for common off-by-one patterns
        if (code.contains("<= ") && code.contains(".length") && !code.contains(".length - 1")) {
            issues.add("• Using <= with .length can cause index out of bounds");
        }

        // Check for missing semicolons (basic check)
        if (Pattern.compile("\\)[^;{]*$", Pattern.MULTILINE).matcher(code).find()) {
            issues.add("• Possible missing semicolon");
        }

        if (!issues.isEmpty()) {
            sb.append("\n\nPOTENTIAL ISSUES I NOTICED:\n");
            for (String issue : issues) {
                sb.append(issue).append("\n");
            }
        }
    }

    private String giveDebuggingStrategy() {
        StringBuilder sb = new StringBuilder();

        sb.append("Here's how to debug this systematically:\n\n");

        sb.append("STEP 1 - UNDERSTAND THE GOAL\n");
        sb.append("Expected: ").append(currentBug.getExpectedOutput()).append("\n\n");

        sb.append("STEP 2 - SEE THE PROBLEM\n");
        sb.append("Currently getting: ").append(currentBug.getActualOutput()).append("\n\n");

        sb.append("STEP 3 - FIND THE CAUSE\n");
        sb.append("Ask yourself: \"Which line could produce '" + currentBug.getActualOutput() + "' instead of '" + currentBug.getExpectedOutput() + "'?\"\n\n");

        sb.append("STEP 4 - MAKE A MINIMAL FIX\n");
        sb.append("Change only what's necessary. Don't rewrite the whole thing!\n\n");

        sb.append("STEP 5 - TEST\n");
        sb.append("Click 'Check Fix' to see if your change worked.\n\n");

        sb.append("Would you like me to help you with any of these steps?");

        return sb.toString();
    }

    private String explainRelevantConcept() {
        StringBuilder sb = new StringBuilder();
        String category = currentBug.getCategory().toLowerCase();
        String code = currentBug.getBrokenCode();

        sb.append("Let me explain the concept:\n\n");

        if (bugType.equals("STATIC_CONTEXT") || category.contains("oop") || category.contains("static")) {
            sb.append("STATIC VS INSTANCE MEMBERS:\n\n");
            sb.append("• STATIC members belong to the CLASS itself\n");
            sb.append("  - Shared by all objects\n");
            sb.append("  - Access with: ClassName.member\n");
            sb.append("  - Can't use 'this' or instance members directly\n\n");
            sb.append("• INSTANCE members belong to each OBJECT\n");
            sb.append("  - Each object has its own copy\n");
            sb.append("  - Access with: objectName.member or this.member\n");
            sb.append("  - Need an object to exist first\n\n");
            sb.append("ERROR: If a static method tries to use an instance variable,\n");
            sb.append("you get 'non-static variable cannot be referenced from static context'");
        } else if (code.contains("for") || code.contains("while") || category.contains("loop")) {
            sb.append("LOOPS:\n\n");
            sb.append("for (initialization; condition; update)\n\n");
            sb.append("• initialization: runs once at start (int i = 0)\n");
            sb.append("• condition: checked before each iteration (i < 10)\n");
            sb.append("• update: runs after each iteration (i++)\n\n");
            sb.append("COMMON BUGS:\n");
            sb.append("• Off-by-one: using <= instead of < (or vice versa)\n");
            sb.append("• Wrong start: starting at 1 instead of 0\n");
            sb.append("• Wrong update: incrementing instead of decrementing");
        } else if (code.contains("if") || category.contains("condition") || category.contains("logic")) {
            sb.append("CONDITIONS & COMPARISONS:\n\n");
            sb.append("COMPARISON OPERATORS:\n");
            sb.append("  == equals (checks if same)\n");
            sb.append("  != not equals\n");
            sb.append("  <  less than\n");
            sb.append("  >  greater than\n");
            sb.append("  <= less than or equal\n");
            sb.append("  >= greater than or equal\n\n");
            sb.append("LOGICAL OPERATORS:\n");
            sb.append("  && AND (both must be true)\n");
            sb.append("  || OR (at least one true)\n");
            sb.append("  !  NOT (inverts true/false)\n\n");
            sb.append("COMMON BUG: Using = (assignment) instead of == (comparison)!");
        } else if (code.contains("[") || category.contains("array")) {
            sb.append("ARRAYS:\n\n");
            sb.append("Arrays are indexed starting at 0!\n\n");
            sb.append("int[] arr = {10, 20, 30};\n");
            sb.append("arr[0] = 10  (first element)\n");
            sb.append("arr[1] = 20  (second element)\n");
            sb.append("arr[2] = 30  (third element)\n");
            sb.append("arr[3] = ERROR! (out of bounds)\n\n");
            sb.append("arr.length = 3 (number of elements)\n");
            sb.append("Last valid index = length - 1 = 2");
        } else {
            sb.append("DEBUGGING BASICS:\n\n");
            sb.append("1. Read the error message carefully\n");
            sb.append("2. Identify expected vs actual behavior\n");
            sb.append("3. Trace through the code step by step\n");
            sb.append("4. Check operators, boundaries, and edge cases\n");
            sb.append("5. Make small, targeted changes");
        }

        return sb.toString();
    }

    private String handleSolutionRequest() {
        StringBuilder sb = new StringBuilder();

        if (hintLevel < 2) {
            sb.append("I want to help you learn, not just give answers!\n\n");
            sb.append("Let me give you a hint instead:\n\n");
            return sb.toString() + giveProgressiveHint();
        }

        sb.append("You've been working hard on this!\n\n");
        sb.append("Here's my suggestion:\n");
        sb.append("• If you want to learn more: ask for another hint\n");
        sb.append("• If you're ready to see the answer: use the 'Show Solution' button below the code\n\n");
        sb.append("The solution includes an explanation of WHY it works, which is valuable for learning!");

        return sb.toString();
    }

    private String giveEncouragement() {
        StringBuilder sb = new StringBuilder();

        sb.append("Hey, don't give up! Every programmer gets stuck on bugs.\n\n");
        sb.append("The fact that you're working through this means you're learning.\n\n");
        sb.append("Let's try a different approach:\n");
        sb.append("1. Take a breath\n");
        sb.append("2. Read the expected vs actual output again\n");
        sb.append("3. Ask me for a specific hint\n\n");
        sb.append("You've got this! Would you like a hint?");

        return sb.toString();
    }

    private String giveContextualHelp() {
        StringBuilder sb = new StringBuilder();

        sb.append("I'm here to help! You can ask me:\n\n");
        sb.append("• \"Explain this bug\" - I'll break down what's wrong\n");
        sb.append("• \"Give me a hint\" - Progressive hints from general to specific\n");
        sb.append("• \"Check my code\" - I'll analyze your changes\n");
        sb.append("• \"How do I start?\" - Debugging strategy\n");
        sb.append("• \"Explain the concept\" - Learn the underlying topic\n\n");
        sb.append("What would help you most right now?");

        return sb.toString();
    }

    // ============ Utility Methods ============

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
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
        return 1.0 - ((double) levenshteinDistance(a, b) / maxLen);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    // ============ Session Management ============

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

    public int getFreeSessions() {
        resetFreeSessions();
        return prefs.getInt(KEY_FREE_SESSIONS, FREE_DAILY_SESSIONS);
    }

    public int getPurchasedSessions() {
        return prefs.getInt(KEY_PURCHASED_SESSIONS, 0);
    }

    public int getTotalSessions() {
        return getFreeSessions() + getPurchasedSessions();
    }

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

    public void addPurchasedSessions(int count) {
        int current = getPurchasedSessions();
        prefs.edit().putInt(KEY_PURCHASED_SESSIONS, current + count).apply();
    }

    public static boolean hasUnlimitedAccess(Context context) {
        try {
            return com.example.debugappproject.billing.BillingManager.getInstance(context).isProUserSync();
        } catch (Exception e) {
            return false;
        }
    }

    public static String[] getQuickActions() {
        return new String[]{"Explain the bug", "Give me a hint", "Check my code", "How do I start?", "I'm stuck!"};
    }

    public List<ChatMessage> getChatHistory() {
        return new ArrayList<>(chatHistory);
    }

    public void clearChatHistory() {
        chatHistory.clear();
    }
}
