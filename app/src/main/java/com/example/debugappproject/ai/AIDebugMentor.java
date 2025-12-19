package com.example.debugappproject.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.debugappproject.model.Bug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AI DEBUGGING MENTOR                                  ║
 * ║         Socratic Method Debugging Companion powered by GPT-4                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Socratic questioning (guides without giving answers)
 * - 5 progressive hint levels
 * - Code review and fix quality assessment
 * - Root cause explanation after solving
 * - Conversational debugging guidance
 */
public class AIDebugMentor {

    private static final String TAG = "AIDebugMentor";
    private static final String PREFS_NAME = "ai_mentor_prefs";
    private static final String KEY_API_KEY = "openai_api_key";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    private static AIDebugMentor instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final OkHttpClient httpClient;
    
    // Conversation history for context
    private List<ChatMessage> conversationHistory = new ArrayList<>();
    private Bug currentBug;
    private int currentHintLevel = 0;
    
    public interface MentorCallback {
        void onResponse(String response);
        void onError(String error);
        void onTyping();
    }
    
    public static class ChatMessage {
        public String role; // "user", "assistant", "system"
        public String content;
        public long timestamp;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private AIDebugMentor(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public static synchronized AIDebugMentor getInstance(Context context) {
        if (instance == null) {
            instance = new AIDebugMentor(context);
        }
        return instance;
    }
    
    /**
     * Set API key for OpenAI
     */
    public void setApiKey(String apiKey) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply();
    }
    
    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }
    
    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }
    
    /**
     * Start a new debugging session with a bug
     */
    public void startSession(Bug bug) {
        this.currentBug = bug;
        this.currentHintLevel = 0;
        this.conversationHistory.clear();
        
        // Add system prompt
        String systemPrompt = buildSystemPrompt(bug);
        conversationHistory.add(new ChatMessage("system", systemPrompt));
    }
    
    /**
     * Build the system prompt for Socratic debugging
     */
    private String buildSystemPrompt(Bug bug) {
        return "You are an expert debugging mentor using the Socratic method. " +
               "Your goal is to help students learn to debug code by asking guiding questions, " +
               "NOT by giving direct answers.\n\n" +
               "RULES:\n" +
               "1. Never reveal the bug directly unless asked for maximum hint\n" +
               "2. Ask diagnostic questions like:\n" +
               "   - 'What do you expect this line to do?'\n" +
               "   - 'What's the actual output vs expected?'\n" +
               "   - 'Have you traced the variable values?'\n" +
               "   - 'What happens if the input is empty/null/negative?'\n" +
               "3. Guide students to discover bugs themselves\n" +
               "4. Be encouraging and patient\n" +
               "5. Use code examples sparingly\n" +
               "6. After they solve it, explain the root cause and prevention\n\n" +
               "CURRENT BUG CONTEXT:\n" +
               "Title: " + bug.getTitle() + "\n" +
               "Language: " + bug.getLanguage() + "\n" +
               "Difficulty: " + bug.getDifficulty() + "\n" +
               "Category: " + bug.getCategory() + "\n" +
               "Description: " + bug.getDescription() + "\n\n" +
               "BROKEN CODE:\n```" + bug.getLanguage().toLowerCase() + "\n" + 
               bug.getBrokenCode() + "\n```\n\n" +
               "EXPECTED OUTPUT: " + bug.getExpectedOutput() + "\n" +
               "ACTUAL OUTPUT/ERROR: " + bug.getActualOutput() + "\n\n" +
               "(For your reference only - DO NOT REVEAL unless student gives up)\n" +
               "FIXED CODE:\n```" + bug.getLanguage().toLowerCase() + "\n" + 
               bug.getFixedCode() + "\n```\n" +
               "ROOT CAUSE: " + bug.getExplanation();
    }
    
    /**
     * Send a message to the AI mentor
     */
    public void sendMessage(String userMessage, MentorCallback callback) {
        if (!hasApiKey()) {
            // Use offline fallback
            handleOffline(userMessage, callback);
            return;
        }
        
        callback.onTyping();
        
        // Add user message to history
        conversationHistory.add(new ChatMessage("user", userMessage));
        
        executor.execute(() -> {
            try {
                String response = callOpenAI();
                
                // Add assistant response to history
                conversationHistory.add(new ChatMessage("assistant", response));
                
                mainHandler.post(() -> callback.onResponse(response));
                
            } catch (Exception e) {
                Log.e(TAG, "API Error", e);
                mainHandler.post(() -> {
                    // Fallback to offline hints
                    handleOffline(userMessage, callback);
                });
            }
        });
    }
    
    /**
     * Call OpenAI API
     */
    private String callOpenAI() throws IOException, JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4");
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        
        JSONArray messages = new JSONArray();
        for (ChatMessage msg : conversationHistory) {
            JSONObject msgObj = new JSONObject();
            msgObj.put("role", msg.role);
            msgObj.put("content", msg.content);
            messages.put(msgObj);
        }
        requestBody.put("messages", messages);
        
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
    
    /**
     * Offline fallback using pre-built Socratic responses
     */
    private void handleOffline(String userMessage, MentorCallback callback) {
        String response = generateOfflineResponse(userMessage);
        conversationHistory.add(new ChatMessage("assistant", response));
        callback.onResponse(response);
    }
    
    /**
     * Generate offline Socratic responses
     */
    private String generateOfflineResponse(String userMessage) {
        if (currentBug == null) {
            return "Let's start debugging! What code are you working on?";
        }
        
        String lowerMessage = userMessage.toLowerCase();
        
        // Check for hint requests
        if (lowerMessage.contains("hint") || lowerMessage.contains("help") || 
            lowerMessage.contains("stuck") || lowerMessage.contains("don't know")) {
            return getProgressiveHint();
        }
        
        // Check for give up
        if (lowerMessage.contains("give up") || lowerMessage.contains("answer") || 
            lowerMessage.contains("solution") || lowerMessage.contains("tell me")) {
            return getFullSolution();
        }
        
        // Check for code submission
        if (lowerMessage.contains("fixed") || lowerMessage.contains("try this") ||
            lowerMessage.contains("is this right")) {
            return reviewAttempt(userMessage);
        }
        
        // Default Socratic questions based on bug type
        return getSocraticQuestion();
    }
    
    /**
     * Get progressive hint (levels 1-5)
     */
    public String getProgressiveHint() {
        currentHintLevel = Math.min(currentHintLevel + 1, 5);
        
        if (currentBug == null) {
            return "No bug loaded. Start a debugging session first!";
        }
        
        switch (currentHintLevel) {
            case 1:
                return "🤔 **Hint Level 1 - Generic**\n\n" +
                       "Let's think about this step by step.\n" +
                       "• What is the code supposed to do?\n" +
                       "• What is it actually doing?\n" +
                       "• Where does the behavior differ from expectations?";
                       
            case 2:
                return "💡 **Hint Level 2 - Direction**\n\n" +
                       "Focus on the '" + currentBug.getCategory() + "' aspect.\n" +
                       "• Have you checked the " + getCategoryHint() + "?\n" +
                       "• Try tracing through with a sample input.";
                       
            case 3:
                return "🔍 **Hint Level 3 - Specific Area**\n\n" +
                       "The bug is likely in the " + getSpecificArea() + ".\n" +
                       "• Look at line numbers carefully\n" +
                       "• Check for common " + currentBug.getCategory() + " mistakes";
                       
            case 4:
                return "🎯 **Hint Level 4 - Almost There**\n\n" +
                       getBugTypeHint() + "\n" +
                       "• The error message says: " + currentBug.getActualOutput();
                       
            case 5:
                return "✅ **Hint Level 5 - Solution**\n\n" +
                       "Here's what's wrong:\n" +
                       currentBug.getExplanation() + "\n\n" +
                       "The fix:\n```" + currentBug.getLanguage().toLowerCase() + "\n" +
                       currentBug.getFixedCode() + "\n```";
                       
            default:
                return getFullSolution();
        }
    }
    
    private String getCategoryHint() {
        String category = currentBug.getCategory().toLowerCase();
        if (category.contains("loop")) return "loop conditions and boundaries";
        if (category.contains("array")) return "array indices and bounds";
        if (category.contains("string")) return "string operations and null checks";
        if (category.contains("condition")) return "comparison operators and logic";
        if (category.contains("method")) return "method parameters and return values";
        if (category.contains("oop")) return "object initialization and references";
        return "variable values and control flow";
    }
    
    private String getSpecificArea() {
        String error = currentBug.getActualOutput().toLowerCase();
        if (error.contains("nullpointer")) return "object initialization section";
        if (error.contains("arrayindex")) return "array access statements";
        if (error.contains("syntax")) return "syntax around brackets/semicolons";
        if (error.contains("infinite")) return "loop termination condition";
        return "core logic section";
    }
    
    private String getBugTypeHint() {
        String error = currentBug.getActualOutput().toLowerCase();
        if (error.contains("nullpointer")) 
            return "Something is null that shouldn't be. Check object creation.";
        if (error.contains("arrayindex")) 
            return "Array index is wrong. Remember: arrays start at 0!";
        if (error.contains("';' expected")) 
            return "Missing semicolon somewhere. Check line endings.";
        if (error.contains("infinite")) 
            return "Loop never ends. Check the condition and increment.";
        if (error.contains("cannot find symbol")) 
            return "Typo in a name. Check spelling and case sensitivity.";
        return "Compare expected vs actual output line by line.";
    }
    
    /**
     * Get full solution (when student gives up)
     */
    private String getFullSolution() {
        if (currentBug == null) return "No bug loaded.";
        
        return "📚 **Complete Solution**\n\n" +
               "**The Problem:**\n" + currentBug.getExplanation() + "\n\n" +
               "**The Fix:**\n```" + currentBug.getLanguage().toLowerCase() + "\n" +
               currentBug.getFixedCode() + "\n```\n\n" +
               "**How to Prevent This:**\n" + getPreventionTips() + "\n\n" +
               "Don't worry! Learning from mistakes is part of debugging. 💪";
    }
    
    private String getPreventionTips() {
        String category = currentBug.getCategory().toLowerCase();
        if (category.contains("loop")) 
            return "• Always check loop bounds before running\n• Use debugger to step through iterations";
        if (category.contains("array")) 
            return "• Validate array indices before access\n• Remember arrays are 0-indexed";
        if (category.contains("null")) 
            return "• Initialize objects before use\n• Add null checks for external data";
        return "• Test with edge cases\n• Use print statements to trace values";
    }
    
    /**
     * Review student's fix attempt
     */
    private String reviewAttempt(String attempt) {
        // In offline mode, give encouraging feedback
        return "🔍 **Reviewing your attempt...**\n\n" +
               "I can see you're working on a solution! Let me ask:\n" +
               "• Does your fix address the root cause?\n" +
               "• Have you tested with the expected output: " + currentBug.getExpectedOutput() + "?\n" +
               "• Does it handle edge cases?\n\n" +
               "Run your code and tell me the result!";
    }
    
    /**
     * Get a Socratic question based on current bug
     */
    private String getSocraticQuestion() {
        String[] questions = {
            "🤔 Let's think about this. What do you expect line by line?",
            "📝 Can you walk me through what the code does step by step?",
            "🔍 What happens when you trace through with input: " + getExampleInput() + "?",
            "💭 If you were the computer, what would you do at each step?",
            "🎯 The error says '" + getShortError() + "'. What could cause that?",
            "⚡ Have you checked the " + getCategoryHint() + "?",
            "🧪 What test case would help isolate the problem?",
            "📊 What are the variable values at each point in the code?"
        };
        
        int index = (int) (System.currentTimeMillis() % questions.length);
        return questions[index];
    }
    
    private String getExampleInput() {
        String expected = currentBug.getExpectedOutput();
        if (expected.contains("5")) return "5";
        if (expected.contains("10")) return "10";
        if (expected.contains("hello")) return "\"hello\"";
        return "a sample value";
    }
    
    private String getShortError() {
        String error = currentBug.getActualOutput();
        if (error.length() > 50) {
            return error.substring(0, 47) + "...";
        }
        return error;
    }
    
    /**
     * Review the quality of a fix
     */
    public void reviewFix(String fixedCode, MentorCallback callback) {
        if (!hasApiKey()) {
            // Offline review
            String review = performOfflineReview(fixedCode);
            callback.onResponse(review);
            return;
        }
        
        callback.onTyping();
        
        String reviewPrompt = "The student submitted this fix:\n```" + 
                currentBug.getLanguage().toLowerCase() + "\n" + fixedCode + "\n```\n\n" +
                "Please review:\n" +
                "1. Is the fix correct?\n" +
                "2. Is it clean and efficient?\n" +
                "3. Any improvements suggested?\n" +
                "4. Rate it 1-5 stars with explanation.";
        
        conversationHistory.add(new ChatMessage("user", reviewPrompt));
        
        executor.execute(() -> {
            try {
                String response = callOpenAI();
                conversationHistory.add(new ChatMessage("assistant", response));
                mainHandler.post(() -> callback.onResponse(response));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(performOfflineReview(fixedCode)));
            }
        });
    }
    
    private String performOfflineReview(String fixedCode) {
        if (currentBug == null) return "No bug context available.";
        
        String expectedFix = currentBug.getFixedCode().trim()
                .replaceAll("\\s+", " ");
        String studentFix = fixedCode.trim()
                .replaceAll("\\s+", " ");
        
        boolean isCorrect = expectedFix.equals(studentFix);
        
        if (isCorrect) {
            return "⭐⭐⭐⭐⭐ **Excellent Fix!**\n\n" +
                   "✅ Your solution is correct!\n" +
                   "✅ Code is clean and follows best practices\n\n" +
                   "**What you learned:**\n" + currentBug.getExplanation() + "\n\n" +
                   "Great job! You've mastered this bug type. 🎉";
        } else {
            return "⭐⭐⭐ **Good Attempt!**\n\n" +
                   "Your fix is different from the expected solution.\n\n" +
                   "**Your code:**\n```\n" + fixedCode + "\n```\n\n" +
                   "**Questions to consider:**\n" +
                   "• Does it produce the expected output?\n" +
                   "• Does it handle all edge cases?\n" +
                   "• Is there a simpler approach?\n\n" +
                   "Keep trying! Would you like a hint?";
        }
    }
    
    /**
     * Get conversation history
     */
    public List<ChatMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Clear conversation
     */
    public void clearConversation() {
        conversationHistory.clear();
        currentHintLevel = 0;
    }
    
    /**
     * Get current hint level
     */
    public int getCurrentHintLevel() {
        return currentHintLevel;
    }
    
    /**
     * Reset hint level
     */
    public void resetHintLevel() {
        currentHintLevel = 0;
    }
}
