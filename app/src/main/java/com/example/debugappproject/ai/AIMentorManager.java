package com.example.debugappproject.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.debugappproject.model.Bug;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
 * ║           DEBUGMASTER - AI DEBUGGING MENTOR (GPT-4 POWERED)                  ║
 * ║              Socratic Questioning & Conversational Debugging                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Socratic Error Analysis: Asks diagnostic questions instead of giving answers
 * - Progressive Hints: 5 levels from generic to specific
 * - Code Review: Reviews fix quality and suggests improvements
 * - Explanation Mode: After fixing, explains root cause and prevention
 * - Conversational Memory: Remembers context within a session
 */
public class AIMentorManager {
    
    private static final String TAG = "AIMentorManager";
    private static final String PREFS_NAME = "ai_mentor_prefs";
    private static final String API_KEY_PREF = "openai_api_key";
    
    // OpenAI API endpoint
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini"; // Cost-effective, fast
    
    private static AIMentorManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Gson gson;
    
    // Conversation history for context
    private final List<ConversationMessage> conversationHistory = new ArrayList<>();
    private Bug currentBug;
    private int hintLevel = 0;
    
    // Callbacks
    public interface MentorCallback {
        void onResponse(String response);
        void onError(String error);
        void onTyping();
    }
    
    public static class ConversationMessage {
        public String role; // "user", "assistant", "system"
        public String content;
        
        public ConversationMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
    
    private AIMentorManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public static synchronized AIMentorManager getInstance(Context context) {
        if (instance == null) {
            instance = new AIMentorManager(context);
        }
        return instance;
    }
    
    /**
     * Set API key for OpenAI
     */
    public void setApiKey(String apiKey) {
        prefs.edit().putString(API_KEY_PREF, apiKey).apply();
    }
    
    public boolean hasApiKey() {
        String key = prefs.getString(API_KEY_PREF, "");
        return key != null && !key.isEmpty();
    }
    
    /**
     * Start a new debugging session with a bug
     */
    public void startSession(Bug bug) {
        this.currentBug = bug;
        this.hintLevel = 0;
        this.conversationHistory.clear();
        
        // Add system prompt for Socratic debugging mentor
        String systemPrompt = createSystemPrompt(bug);
        conversationHistory.add(new ConversationMessage("system", systemPrompt));
    }
    
    /**
     * Ask the AI mentor a question about the current bug
     */
    public void askQuestion(String userQuestion, MentorCallback callback) {
        if (currentBug == null) {
            callback.onError("No bug session started. Please select a bug first.");
            return;
        }
        
        callback.onTyping();
        
        // Add user message to history
        conversationHistory.add(new ConversationMessage("user", userQuestion));
        
        executor.execute(() -> {
            try {
                String response = callOpenAI();
                
                // Add assistant response to history
                conversationHistory.add(new ConversationMessage("assistant", response));
                
                mainHandler.post(() -> callback.onResponse(response));
                
            } catch (Exception e) {
                Log.e(TAG, "Error calling AI API", e);
                String fallbackResponse = getFallbackResponse(userQuestion);
                mainHandler.post(() -> callback.onResponse(fallbackResponse));
            }
        });
    }
    
    /**
     * Get Socratic diagnostic questions (without revealing the answer)
     */
    public void getSocraticQuestions(MentorCallback callback) {
        String prompt = "The student is stuck. Ask 3 Socratic questions to guide them toward finding the bug themselves. " +
                "Do NOT reveal the answer. Focus on:\n" +
                "1. What they expect vs what happens\n" +
                "2. Which specific line might be suspicious\n" +
                "3. What debugging technique could help here";
        
        askQuestion(prompt, callback);
    }
    
    /**
     * Get progressive hint (increases hint level each call)
     */
    public void getNextHint(MentorCallback callback) {
        hintLevel = Math.min(hintLevel + 1, 5);
        
        String prompt;
        switch (hintLevel) {
            case 1:
                prompt = "Give a very generic hint about what TYPE of bug this might be (syntax, logic, runtime, etc). Don't be specific.";
                break;
            case 2:
                prompt = "Hint at which AREA of the code the bug is in (loop, condition, variable, method call). Still vague.";
                break;
            case 3:
                prompt = "Point to the specific LINE NUMBER where the bug is, but don't explain what's wrong with it.";
                break;
            case 4:
                prompt = "Explain exactly what's wrong on that line, but don't give the fix yet.";
                break;
            case 5:
            default:
                prompt = "Give the complete fix with explanation. The student has used all hints.";
                break;
        }
        
        callback.onTyping();
        conversationHistory.add(new ConversationMessage("user", prompt));
        
        executor.execute(() -> {
            try {
                String response = callOpenAI();
                conversationHistory.add(new ConversationMessage("assistant", response));
                
                String levelLabel = "💡 Hint Level " + hintLevel + "/5:\n\n";
                mainHandler.post(() -> callback.onResponse(levelLabel + response));
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResponse(getLocalHint(hintLevel)));
            }
        });
    }
    
    /**
     * Review the student's fix and provide feedback
     */
    public void reviewFix(String studentCode, MentorCallback callback) {
        String prompt = "The student submitted this fix:\n```\n" + studentCode + "\n```\n\n" +
                "Compare it to the correct solution. Provide:\n" +
                "1. ✅ What they did right\n" +
                "2. ⚠️ Any issues or edge cases they missed\n" +
                "3. 💡 How to improve code quality\n" +
                "4. 🎓 What principle they should remember\n" +
                "Be encouraging but thorough.";
        
        askQuestion(prompt, callback);
    }
    
    /**
     * Explain the root cause and prevention after solving
     */
    public void explainRootCause(MentorCallback callback) {
        String prompt = "The student has now fixed the bug. Provide a comprehensive explanation:\n" +
                "1. 🔍 ROOT CAUSE: Why did this bug happen?\n" +
                "2. 🛡️ PREVENTION: How to avoid this bug in future code?\n" +
                "3. 🌍 REAL-WORLD: Where might this bug appear in production systems?\n" +
                "4. 📚 RELATED CONCEPTS: What other bugs are similar to this?\n" +
                "Keep it educational and memorable.";
        
        askQuestion(prompt, callback);
    }
    
    /**
     * Analyze stack trace and explain error
     */
    public void analyzeStackTrace(String stackTrace, MentorCallback callback) {
        String prompt = "Analyze this stack trace:\n```\n" + stackTrace + "\n```\n\n" +
                "Explain:\n" +
                "1. What error occurred (in simple terms)\n" +
                "2. Where exactly in the code it happened\n" +
                "3. What the call chain was that led here\n" +
                "4. Most likely causes of this error\n" +
                "Use simple language suitable for a learner.";
        
        askQuestion(prompt, callback);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE METHODS
    // ═══════════════════════════════════════════════════════════════════════
    
    private String createSystemPrompt(Bug bug) {
        return "You are a Socratic debugging mentor for DebugMaster, an educational app that teaches debugging.\n\n" +
                "CURRENT BUG CONTEXT:\n" +
                "- Title: " + bug.getTitle() + "\n" +
                "- Language: " + bug.getLanguage() + "\n" +
                "- Difficulty: " + bug.getDifficulty() + "\n" +
                "- Category: " + bug.getCategory() + "\n" +
                "- Description: " + bug.getDescription() + "\n\n" +
                "BROKEN CODE:\n```\n" + bug.getBrokenCode() + "\n```\n\n" +
                "EXPECTED OUTPUT: " + bug.getExpectedOutput() + "\n" +
                "ACTUAL OUTPUT/ERROR: " + bug.getActualOutput() + "\n\n" +
                "CORRECT SOLUTION (for your reference, don't reveal unless asked):\n```\n" + bug.getFixedCode() + "\n```\n\n" +
                "YOUR ROLE:\n" +
                "1. Use SOCRATIC QUESTIONING - ask questions that guide students to discover the bug themselves\n" +
                "2. NEVER immediately reveal the answer unless explicitly asked after multiple hints\n" +
                "3. Be encouraging, patient, and educational\n" +
                "4. Explain concepts clearly with examples\n" +
                "5. Relate bugs to real-world scenarios when possible\n" +
                "6. Use emojis to make responses engaging\n" +
                "7. Keep responses concise (under 200 words usually)\n\n" +
                "Remember: The goal is LEARNING, not just solving. Help them become better debuggers!";
    }
    
    private String callOpenAI() throws IOException {
        String apiKey = prefs.getString(API_KEY_PREF, "");
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("No API key configured");
        }
        
        // Build messages array
        JsonArray messages = new JsonArray();
        for (ConversationMessage msg : conversationHistory) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.role);
            msgObj.addProperty("content", msg.content);
            messages.add(msgObj);
        }
        
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.add("messages", messages);
        requestBody.addProperty("max_tokens", 500);
        requestBody.addProperty("temperature", 0.7);
        
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            
            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }
    
    /**
     * Fallback responses when API is unavailable
     */
    private String getFallbackResponse(String question) {
        if (currentBug == null) {
            return "🤖 Let me help you debug! Please select a bug first.";
        }
        
        String q = question.toLowerCase();
        
        if (q.contains("hint") || q.contains("help") || q.contains("stuck")) {
            return getLocalHint(Math.min(hintLevel + 1, 5));
        }
        
        if (q.contains("what") && q.contains("wrong")) {
            return "🤔 Great question! Let me ask you this instead:\n\n" +
                    "1. What output do you EXPECT this code to produce?\n" +
                    "2. What is it ACTUALLY producing?\n" +
                    "3. At which line do you think the behavior differs from your expectation?\n\n" +
                    "Think through these questions - they'll lead you to the bug!";
        }
        
        if (q.contains("explain") || q.contains("why")) {
            return "📚 " + currentBug.getExplanation() + "\n\n" +
                    "💡 Remember: Understanding WHY bugs happen helps prevent them in the future!";
        }
        
        return "🤖 I'm here to help you debug!\n\n" +
                "Try asking me:\n" +
                "• \"Give me a hint\"\n" +
                "• \"What's wrong with this code?\"\n" +
                "• \"Explain the error\"\n" +
                "• \"Review my fix\"\n\n" +
                "Or describe what you're confused about!";
    }
    
    private String getLocalHint(int level) {
        if (currentBug == null) return "No bug selected.";
        
        String hint = currentBug.getHintText();
        String explanation = currentBug.getExplanation();
        
        switch (level) {
            case 1:
                return "💡 Hint 1/5: Look carefully at the " + currentBug.getCategory() + " in this code. There's a common mistake here.";
            case 2:
                return "💡 Hint 2/5: The bug is related to: " + hint;
            case 3:
                return "💡 Hint 3/5: Check the logic where the main operation happens. Compare what you expect vs what actually occurs.";
            case 4:
                return "💡 Hint 4/5: " + explanation;
            case 5:
            default:
                return "💡 Hint 5/5 (Solution): \n\n" + currentBug.getFixedCode() + "\n\n📚 " + explanation;
        }
    }
    
    public int getCurrentHintLevel() {
        return hintLevel;
    }
    
    public void resetHintLevel() {
        this.hintLevel = 0;
    }
    
    public List<ConversationMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    public void clearConversation() {
        conversationHistory.clear();
        if (currentBug != null) {
            conversationHistory.add(new ConversationMessage("system", createSystemPrompt(currentBug)));
        }
        hintLevel = 0;
    }
}
