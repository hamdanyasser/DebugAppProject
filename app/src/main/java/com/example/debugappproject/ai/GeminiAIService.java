package com.example.debugappproject.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
 * ║           DEBUGMASTER - GEMINI AI SERVICE (FREE & POWERFUL)                  ║
 * ║                    Google's Gemini 1.5 Flash - FREE Tier                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * FREE LIMITS (as of 2024):
 * - 15 requests per minute
 * - 1,500 requests per day
 * - 1 million tokens per minute
 *
 * Get your FREE API key at: https://aistudio.google.com/apikey
 */
public class GeminiAIService {

    private static final String TAG = "GeminiAIService";
    private static final String PREFS_NAME = "gemini_ai_prefs";
    private static final String API_KEY_PREF = "gemini_api_key";

    // Default API key (embedded for all users)
    private static final String DEFAULT_API_KEY = "AIzaSyDFgykmcJYOTjLtfV0IB5tegU1dcifvLmk";

    // Gemini API endpoint (FREE tier)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private static GeminiAIService instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // Conversation history for context
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    public interface AICallback {
        void onResponse(String response);
        void onError(String error);
        void onTyping();
    }

    public static class ChatMessage {
        public String role; // "user" or "model"
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private GeminiAIService(Context context) {
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

    public static synchronized GeminiAIService getInstance(Context context) {
        if (instance == null) {
            instance = new GeminiAIService(context);
        }
        return instance;
    }

    /**
     * Set API key for Gemini (FREE from Google AI Studio)
     */
    public void setApiKey(String apiKey) {
        prefs.edit().putString(API_KEY_PREF, apiKey).apply();
    }

    public String getApiKey() {
        String customKey = prefs.getString(API_KEY_PREF, "");
        // Return custom key if set, otherwise use embedded default key
        if (customKey != null && !customKey.isEmpty()) {
            return customKey;
        }
        return DEFAULT_API_KEY;
    }

    public boolean hasApiKey() {
        // Always true since we have a default embedded key
        return true;
    }

    /**
     * Set system context for the conversation (like bug details)
     */
    public void setSystemContext(String systemPrompt) {
        conversationHistory.clear();
        // Gemini doesn't have a "system" role, so we add it as first user message
        conversationHistory.add(new ChatMessage("user", "CONTEXT: " + systemPrompt));
        conversationHistory.add(new ChatMessage("model", "I understand. I'm ready to help you debug this code using the Socratic method. I'll guide you with questions rather than giving direct answers. What would you like to explore?"));
    }

    /**
     * Send a message and get AI response
     */
    public void sendMessage(String userMessage, AICallback callback) {
        if (!hasApiKey()) {
            callback.onError("No API key. Get your FREE key at: aistudio.google.com/apikey");
            return;
        }

        callback.onTyping();

        // Add user message to history
        conversationHistory.add(new ChatMessage("user", userMessage));

        executor.execute(() -> {
            try {
                String response = callGeminiAPI();

                // Add assistant response to history
                conversationHistory.add(new ChatMessage("model", response));

                mainHandler.post(() -> callback.onResponse(response));

            } catch (Exception e) {
                Log.e(TAG, "Gemini API Error", e);
                // Remove failed message from history
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                mainHandler.post(() -> callback.onError("AI Error: " + e.getMessage()));
            }
        });
    }

    /**
     * Call Gemini API
     */
    private String callGeminiAPI() throws IOException, JSONException {
        String apiKey = getApiKey();
        String url = GEMINI_API_URL + "?key=" + apiKey;

        // Build the request body
        JSONObject requestBody = new JSONObject();

        // Build contents array with conversation history
        JSONArray contents = new JSONArray();
        for (ChatMessage msg : conversationHistory) {
            JSONObject content = new JSONObject();
            content.put("role", msg.role);

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", msg.content);
            parts.put(textPart);

            content.put("parts", parts);
            contents.put(content);
        }
        requestBody.put("contents", contents);

        // Add generation config
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1024);
        generationConfig.put("topP", 0.95);
        requestBody.put("generationConfig", generationConfig);

        // Add safety settings (allow educational content)
        JSONArray safetySettings = new JSONArray();
        String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH",
                              "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
        for (String category : categories) {
            JSONObject setting = new JSONObject();
            setting.put("category", category);
            setting.put("threshold", "BLOCK_ONLY_HIGH");
            safetySettings.put(setting);
        }
        requestBody.put("safetySettings", safetySettings);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                Log.e(TAG, "API Error Response: " + responseBody);
                if (response.code() == 400) {
                    throw new IOException("Invalid API key. Get a free key at aistudio.google.com/apikey");
                } else if (response.code() == 429) {
                    throw new IOException("Rate limit reached. Try again in a minute.");
                }
                throw new IOException("API error: " + response.code());
            }

            JSONObject json = new JSONObject(responseBody);

            // Check for errors in response
            if (json.has("error")) {
                String errorMsg = json.getJSONObject("error").optString("message", "Unknown error");
                throw new IOException(errorMsg);
            }

            // Extract text from response
            JSONArray candidates = json.getJSONArray("candidates");
            if (candidates.length() == 0) {
                throw new IOException("No response generated");
            }

            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");

            StringBuilder responseText = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                responseText.append(parts.getJSONObject(i).getString("text"));
            }

            return responseText.toString();
        }
    }

    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * Get conversation history
     */
    public List<ChatMessage> getHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * Quick helper to ask a single question without history
     */
    public void askSingleQuestion(String question, String context, AICallback callback) {
        if (!hasApiKey()) {
            callback.onError("No API key configured");
            return;
        }

        callback.onTyping();

        executor.execute(() -> {
            try {
                // Build single request
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();

                JSONObject content = new JSONObject();
                content.put("role", "user");

                JSONArray parts = new JSONArray();
                JSONObject textPart = new JSONObject();
                textPart.put("text", context + "\n\nQuestion: " + question);
                parts.put(textPart);

                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                // Generation config
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 1024);
                requestBody.put("generationConfig", generationConfig);

                String apiKey = getApiKey();
                String url = GEMINI_API_URL + "?key=" + apiKey;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("API error: " + response.code());
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);

                    String text = json.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    mainHandler.post(() -> callback.onResponse(text));
                }

            } catch (Exception e) {
                Log.e(TAG, "Single question error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
