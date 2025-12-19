package com.example.debugappproject.github;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - GITHUB BUG SCRAPER V2                                ║
 * ║         Import Real Bugs + Built-in Curated Bug Library                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Now includes:
 * - Curated real-world bugs that ALWAYS work (no API needed)
 * - GitHub API integration for live bugs (optional)
 * - Better difficulty calibration
 * - Actual broken/fixed code pairs
 */
public class GitHubBugScraper {

    private static final String TAG = "GitHubBugScraper";
    private static final String GITHUB_API = "https://api.github.com";
    
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private String authToken;
    
    public interface ScraperCallback {
        void onBugsFound(List<GitHubBug> bugs);
        void onError(String error);
        void onProgress(int current, int total);
    }
    
    public static class GitHubBug {
        public String title;
        public String description;
        public String language;
        public String difficulty;
        public String category;
        public String brokenCode;
        public String fixedCode;
        public String sourceUrl;
        public String repository;
        public List<String> labels;
        public int stars;
        public String explanation;
        public int xpReward;
        
        public GitHubBug() {
            labels = new ArrayList<>();
        }
    }
    
    public GitHubBugScraper() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Search for bugs - uses curated library + optional GitHub API
     */
    public void searchBugs(String language, String category, int limit, ScraperCallback callback) {
        executor.execute(() -> {
            List<GitHubBug> bugs = new ArrayList<>();
            
            // First, add curated bugs that ALWAYS work
            List<GitHubBug> curatedBugs = getCuratedBugs(language);
            
            int added = 0;
            for (GitHubBug bug : curatedBugs) {
                if (added >= limit) break;
                bugs.add(bug);
                added++;
                int finalAdded = added;
                mainHandler.post(() -> callback.onProgress(finalAdded, limit));
            }
            
            // Try GitHub API for additional bugs (non-blocking)
            if (added < limit && authToken != null && !authToken.isEmpty()) {
                try {
                    List<GitHubBug> apiBugs = fetchFromGitHubAPI(language, limit - added);
                    for (GitHubBug bug : apiBugs) {
                        if (bugs.size() >= limit) break;
                        bugs.add(bug);
                        int current = bugs.size();
                        mainHandler.post(() -> callback.onProgress(current, limit));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "GitHub API unavailable, using curated bugs only");
                }
            }
            
            mainHandler.post(() -> callback.onBugsFound(bugs));
        });
    }
    
    /**
     * Fetch from specific repository
     */
    public void fetchFromRepository(String owner, String repo, ScraperCallback callback) {
        executor.execute(() -> {
            List<GitHubBug> bugs = new ArrayList<>();
            
            try {
                // Try actual GitHub API first
                String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/pulls?state=closed&per_page=10";
                
                Request.Builder requestBuilder = new Request.Builder().url(url);
                requestBuilder.addHeader("Accept", "application/vnd.github.v3+json");
                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + authToken);
                }
                
                try (Response response = client.newCall(requestBuilder.build()).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        JSONArray prs = new JSONArray(body);
                        
                        for (int i = 0; i < Math.min(prs.length(), 10); i++) {
                            JSONObject pr = prs.getJSONObject(i);
                            String title = pr.optString("title", "").toLowerCase();
                            
                            if (title.contains("fix") || title.contains("bug") || 
                                title.contains("resolve") || title.contains("error")) {
                                
                                GitHubBug bug = createBugFromPR(pr, owner, repo);
                                if (bug != null) {
                                    bugs.add(bug);
                                    int size = bugs.size();
                                    mainHandler.post(() -> callback.onProgress(size, 10));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "API Error: " + e.getMessage());
            }
            
            // If no bugs found from API, provide curated bugs as fallback
            if (bugs.isEmpty()) {
                bugs.addAll(getCuratedBugs("Java"));
                mainHandler.post(() -> callback.onProgress(bugs.size(), bugs.size()));
            }
            
            mainHandler.post(() -> callback.onBugsFound(bugs));
        });
    }
    
    private GitHubBug createBugFromPR(JSONObject pr, String owner, String repo) {
        try {
            GitHubBug bug = new GitHubBug();
            bug.title = pr.optString("title", "Bug Fix");
            bug.description = truncate(pr.optString("body", "Fix from " + repo), 300);
            bug.sourceUrl = pr.optString("html_url", "https://github.com/" + owner + "/" + repo);
            bug.repository = owner + "/" + repo;
            bug.language = detectLanguage(repo);
            bug.difficulty = "Medium";
            bug.category = "Real-World";
            bug.xpReward = 75;
            
            // Generate sample code based on title
            generateCodeFromTitle(bug);
            
            return bug;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void generateCodeFromTitle(GitHubBug bug) {
        String title = bug.title.toLowerCase();
        
        if (title.contains("null")) {
            bug.brokenCode = "public String process(User user) {\n    return user.getName().toUpperCase();\n}";
            bug.fixedCode = "public String process(User user) {\n    if (user == null || user.getName() == null) {\n        return \"\";\n    }\n    return user.getName().toUpperCase();\n}";
            bug.explanation = "Missing null check causes NullPointerException";
            bug.category = "Null Handling";
        } else if (title.contains("index") || title.contains("bound")) {
            bug.brokenCode = "for (int i = 0; i <= array.length; i++) {\n    sum += array[i];\n}";
            bug.fixedCode = "for (int i = 0; i < array.length; i++) {\n    sum += array[i];\n}";
            bug.explanation = "Off-by-one error: <= should be <";
            bug.category = "Arrays";
        } else if (title.contains("loop") || title.contains("infinite")) {
            bug.brokenCode = "while (i < 10) {\n    result += i;\n    // missing i++\n}";
            bug.fixedCode = "while (i < 10) {\n    result += i;\n    i++;\n}";
            bug.explanation = "Missing increment causes infinite loop";
            bug.category = "Loops";
        } else {
            bug.brokenCode = "// Bug from: " + bug.repository + "\n// See GitHub for details:\n// " + bug.sourceUrl;
            bug.fixedCode = "// Fixed version at:\n// " + bug.sourceUrl;
            bug.explanation = "Visit GitHub to see the full fix";
        }
    }
    
    private List<GitHubBug> fetchFromGitHubAPI(String language, int limit) {
        List<GitHubBug> bugs = new ArrayList<>();
        
        try {
            String query = URLEncoder.encode("bug fix language:" + language + " is:pr is:merged", "UTF-8");
            String url = GITHUB_API + "/search/issues?q=" + query + "&per_page=" + limit + "&sort=reactions";
            
            Request.Builder builder = new Request.Builder().url(url);
            builder.addHeader("Accept", "application/vnd.github.v3+json");
            if (authToken != null) {
                builder.addHeader("Authorization", "Bearer " + authToken);
            }
            
            try (Response response = client.newCall(builder.build()).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray items = json.optJSONArray("items");
                    
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            GitHubBug bug = new GitHubBug();
                            bug.title = item.optString("title", "GitHub Bug");
                            bug.description = truncate(item.optString("body", ""), 200);
                            bug.sourceUrl = item.optString("html_url", "");
                            bug.language = language;
                            bug.difficulty = "Medium";
                            bug.category = "GitHub Import";
                            bug.xpReward = 50;
                            generateCodeFromTitle(bug);
                            bugs.add(bug);
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "fetchFromGitHubAPI error: " + e.getMessage());
        }
        
        return bugs;
    }
    
    /**
     * CURATED BUG LIBRARY - These ALWAYS work, no API needed!
     */
    private List<GitHubBug> getCuratedBugs(String language) {
        List<GitHubBug> bugs = new ArrayList<>();
        
        if (language.equalsIgnoreCase("Java") || language.equalsIgnoreCase("Kotlin") || language.equalsIgnoreCase("All")) {
            // Java/Kotlin Bugs
            bugs.add(createCuratedBug(
                "NullPointerException in User Service",
                "java",
                "Null Handling",
                "Easy",
                "User.getName() is called without null check",
                "public String getDisplayName(User user) {\n" +
                "    return user.getName().toUpperCase();\n" +
                "}",
                "public String getDisplayName(User user) {\n" +
                "    if (user == null || user.getName() == null) {\n" +
                "        return \"Guest\";\n" +
                "    }\n" +
                "    return user.getName().toUpperCase();\n" +
                "}",
                "Always check for null before calling methods on objects",
                50
            ));
            
            bugs.add(createCuratedBug(
                "ArrayIndexOutOfBounds in Loop",
                "java",
                "Arrays",
                "Easy",
                "Loop iterates one element past the array end",
                "int[] scores = {90, 85, 78, 92};\nint sum = 0;\nfor (int i = 0; i <= scores.length; i++) {\n    sum += scores[i];\n}",
                "int[] scores = {90, 85, 78, 92};\nint sum = 0;\nfor (int i = 0; i < scores.length; i++) {\n    sum += scores[i];\n}",
                "Array indices go from 0 to length-1. Use < not <=",
                40
            ));
            
            bugs.add(createCuratedBug(
                "Infinite Loop Bug",
                "java",
                "Loops",
                "Medium",
                "Counter variable never increments",
                "int count = 0;\nwhile (count < 10) {\n    System.out.println(count);\n    // count++ missing!\n}",
                "int count = 0;\nwhile (count < 10) {\n    System.out.println(count);\n    count++;\n}",
                "While loops need their condition to eventually become false",
                60
            ));
            
            bugs.add(createCuratedBug(
                "String Comparison Bug",
                "java",
                "Strings",
                "Medium",
                "Using == instead of .equals() for String comparison",
                "String input = scanner.nextLine();\nif (input == \"yes\") {\n    System.out.println(\"Confirmed!\");\n}",
                "String input = scanner.nextLine();\nif (input.equals(\"yes\")) {\n    System.out.println(\"Confirmed!\");\n}",
                "In Java, == compares references, .equals() compares content",
                55
            ));
            
            bugs.add(createCuratedBug(
                "Integer Division Precision Loss",
                "java",
                "Math",
                "Medium",
                "Integer division truncates decimal part",
                "int total = 7;\nint count = 2;\ndouble average = total / count;\n// Returns 3.0, not 3.5!",
                "int total = 7;\nint count = 2;\ndouble average = (double) total / count;\n// Returns 3.5 correctly",
                "Cast to double before division to preserve decimals",
                50
            ));
            
            bugs.add(createCuratedBug(
                "ConcurrentModificationException",
                "java",
                "Collections",
                "Hard",
                "Modifying list while iterating",
                "List<String> items = new ArrayList<>();\nitems.add(\"a\"); items.add(\"b\");\nfor (String item : items) {\n    if (item.equals(\"a\")) {\n        items.remove(item);\n    }\n}",
                "List<String> items = new ArrayList<>();\nitems.add(\"a\"); items.add(\"b\");\nIterator<String> it = items.iterator();\nwhile (it.hasNext()) {\n    if (it.next().equals(\"a\")) {\n        it.remove();\n    }\n}",
                "Use Iterator.remove() when removing during iteration",
                85
            ));
            
            bugs.add(createCuratedBug(
                "StringBuilder vs String Concatenation",
                "java",
                "Performance",
                "Medium",
                "Inefficient string building in loop",
                "String result = \"\";\nfor (int i = 0; i < 1000; i++) {\n    result += i + \", \"; // Creates 1000 String objects!\n}",
                "StringBuilder sb = new StringBuilder();\nfor (int i = 0; i < 1000; i++) {\n    sb.append(i).append(\", \");\n}\nString result = sb.toString();",
                "StringBuilder is much faster for repeated concatenation",
                70
            ));
            
            bugs.add(createCuratedBug(
                "Resource Leak - Unclosed Stream",
                "java",
                "Resources",
                "Hard",
                "FileInputStream never closed",
                "FileInputStream fis = new FileInputStream(\"data.txt\");\nbyte[] data = fis.readAllBytes();\n// fis.close() missing - resource leak!",
                "try (FileInputStream fis = new FileInputStream(\"data.txt\")) {\n    byte[] data = fis.readAllBytes();\n} // Auto-closed by try-with-resources",
                "Always use try-with-resources for auto-closeable resources",
                80
            ));
        }
        
        if (language.equalsIgnoreCase("Python") || language.equalsIgnoreCase("All")) {
            bugs.add(createCuratedBug(
                "Mutable Default Argument",
                "python",
                "Functions",
                "Hard",
                "List default argument is shared between calls",
                "def add_item(item, items=[]):\n    items.append(item)\n    return items\n\n# add_item('a') then add_item('b') returns ['a', 'b']!",
                "def add_item(item, items=None):\n    if items is None:\n        items = []\n    items.append(item)\n    return items",
                "Default mutable arguments are shared - use None instead",
                75
            ));
            
            bugs.add(createCuratedBug(
                "IndentationError",
                "python",
                "Syntax",
                "Easy",
                "Mixed tabs and spaces",
                "def calculate():\n    total = 0\n\tfor i in range(10):  # Tab instead of spaces!\n        total += i",
                "def calculate():\n    total = 0\n    for i in range(10):\n        total += i",
                "Never mix tabs and spaces - use 4 spaces consistently",
                35
            ));
            
            bugs.add(createCuratedBug(
                "Off-by-One in Range",
                "python",
                "Loops",
                "Easy",
                "Range excludes the end value",
                "# Print 1 to 10\nfor i in range(1, 10):\n    print(i)  # Only prints 1-9!",
                "# Print 1 to 10\nfor i in range(1, 11):\n    print(i)  # Now prints 1-10",
                "range(start, end) excludes 'end' - add 1 if needed",
                40
            ));
        }
        
        if (language.equalsIgnoreCase("JavaScript") || language.equalsIgnoreCase("All")) {
            bugs.add(createCuratedBug(
                "Equality vs Strict Equality",
                "javascript",
                "Comparisons",
                "Easy",
                "Using == instead of ===",
                "if (userInput == 0) {\n    // This is true for '', null, false too!\n    showError();\n}",
                "if (userInput === 0) {\n    // Only true for actual 0\n    showError();\n}",
                "Always use === for strict type comparison",
                45
            ));
            
            bugs.add(createCuratedBug(
                "Async/Await Missing",
                "javascript",
                "Async",
                "Medium",
                "Forgot await on async function",
                "async function loadData() {\n    const data = fetchFromAPI();  // Missing await!\n    console.log(data);  // Logs Promise, not data\n}",
                "async function loadData() {\n    const data = await fetchFromAPI();\n    console.log(data);  // Now logs actual data\n}",
                "Async functions return Promises - use await to get the value",
                65
            ));
            
            bugs.add(createCuratedBug(
                "Closure in Loop",
                "javascript",
                "Closures",
                "Hard",
                "var captures wrong value in loop",
                "for (var i = 0; i < 3; i++) {\n    setTimeout(() => console.log(i), 100);\n}\n// Prints: 3, 3, 3",
                "for (let i = 0; i < 3; i++) {\n    setTimeout(() => console.log(i), 100);\n}\n// Prints: 0, 1, 2",
                "Use 'let' instead of 'var' for proper block scoping",
                80
            ));
        }
        
        // Shuffle for variety
        java.util.Collections.shuffle(bugs, new Random());
        
        return bugs;
    }
    
    private GitHubBug createCuratedBug(String title, String language, String category, 
            String difficulty, String description, String brokenCode, String fixedCode, 
            String explanation, int xpReward) {
        GitHubBug bug = new GitHubBug();
        bug.title = title;
        bug.language = language;
        bug.category = category;
        bug.difficulty = difficulty;
        bug.description = description;
        bug.brokenCode = brokenCode;
        bug.fixedCode = fixedCode;
        bug.explanation = explanation;
        bug.xpReward = xpReward;
        bug.sourceUrl = "curated://debugmaster/" + title.toLowerCase().replace(" ", "-");
        bug.repository = "DebugMaster/CuratedBugs";
        return bug;
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private String detectLanguage(String repo) {
        repo = repo.toLowerCase();
        if (repo.contains("java") || repo.contains("android") || repo.contains("spring")) return "Java";
        if (repo.contains("python") || repo.contains("django") || repo.contains("flask")) return "Python";
        if (repo.contains("js") || repo.contains("node") || repo.contains("react")) return "JavaScript";
        if (repo.contains("kotlin")) return "Kotlin";
        return "Java";
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
