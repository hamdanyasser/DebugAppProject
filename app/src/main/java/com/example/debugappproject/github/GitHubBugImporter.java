package com.example.debugappproject.github;

import android.content.Context;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - GITHUB BUG IMPORTER                                  ║
 * ║         Import Real Bugs from GitHub Issues & Pull Requests                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Features:
 * - Search GitHub for bug-related issues
 * - Extract code snippets from PRs
 * - Categorize by language and difficulty
 * - Import curated bug collections
 */
public class GitHubBugImporter {

    private static final String TAG = "GitHubBugImporter";
    private static final String GITHUB_API_URL = "https://api.github.com";
    
    private final Context context;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    private String githubToken; // Optional for higher rate limits
    
    public interface ImportCallback {
        void onBugsImported(List<GitHubBug> bugs);
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
        public String explanation;
        public String sourceUrl;
        public String repoName;
        public int issueNumber;
        public List<String> labels;
        
        public Bug toBug(int id) {
            Bug bug = new Bug(
                    id,
                    title,
                    language,
                    difficulty,
                    category,
                    description,
                    brokenCode,
                    "Error or incorrect output",
                    "See issue description",
                    explanation,
                    fixedCode,
                    false
            );
            return bug;
        }
    }
    
    public GitHubBugImporter(Context context) {
        this.context = context;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Set GitHub token for higher API rate limits
     */
    public void setGitHubToken(String token) {
        this.githubToken = token;
    }
    
    /**
     * Search for bug-related issues in popular repositories
     */
    public void searchBugIssues(String language, String query, ImportCallback callback) {
        executor.execute(() -> {
            try {
                String searchQuery = buildSearchQuery(language, query);
                String url = GITHUB_API_URL + "/search/issues?q=" + searchQuery + "&per_page=20";
                
                String response = makeGitHubRequest(url);
                List<GitHubBug> bugs = parseIssuesResponse(response, language);
                
                mainHandler.post(() -> callback.onBugsImported(bugs));
                
            } catch (Exception e) {
                Log.e(TAG, "Error searching bugs", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Import bugs from curated repositories
     */
    public void importFromCuratedRepos(String language, ImportCallback callback) {
        executor.execute(() -> {
            try {
                List<GitHubBug> allBugs = new ArrayList<>();
                String[] repos = getCuratedRepos(language);
                
                for (int i = 0; i < repos.length; i++) {
                    final int progress = i;
                    mainHandler.post(() -> callback.onProgress(progress, repos.length));
                    
                    String repo = repos[i];
                    List<GitHubBug> bugs = fetchBugsFromRepo(repo, language);
                    allBugs.addAll(bugs);
                    
                    // Respect rate limits
                    Thread.sleep(1000);
                }
                
                mainHandler.post(() -> callback.onBugsImported(allBugs));
                
            } catch (Exception e) {
                Log.e(TAG, "Error importing bugs", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Fetch bug fix PRs from a specific repository
     */
    public void fetchBugFixPRs(String owner, String repo, ImportCallback callback) {
        executor.execute(() -> {
            try {
                String url = GITHUB_API_URL + "/repos/" + owner + "/" + repo + 
                            "/pulls?state=closed&per_page=30";
                
                String response = makeGitHubRequest(url);
                List<GitHubBug> bugs = parsePRsResponse(response, owner + "/" + repo);
                
                mainHandler.post(() -> callback.onBugsImported(bugs));
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching PRs", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    private String buildSearchQuery(String language, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("label:bug");
        
        if (language != null && !language.isEmpty()) {
            sb.append("+language:").append(language);
        }
        
        if (query != null && !query.isEmpty()) {
            sb.append("+").append(query.replace(" ", "+"));
        }
        
        sb.append("+is:closed"); // Only closed issues (fixed bugs)
        
        return sb.toString();
    }
    
    private String makeGitHubRequest(String url) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "DebugMaster-App");
        
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.addHeader("Authorization", "token " + githubToken);
        }
        
        try (Response response = httpClient.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API error: " + response.code());
            }
            return response.body().string();
        }
    }
    
    private List<GitHubBug> parseIssuesResponse(String json, String language) throws JSONException {
        List<GitHubBug> bugs = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray items = root.getJSONArray("items");
        
        for (int i = 0; i < items.length(); i++) {
            JSONObject issue = items.getJSONObject(i);
            
            GitHubBug bug = new GitHubBug();
            bug.title = issue.getString("title");
            bug.description = issue.optString("body", "No description provided");
            bug.language = language != null ? language : "Java";
            bug.sourceUrl = issue.getString("html_url");
            bug.issueNumber = issue.getInt("number");
            
            // Extract labels
            bug.labels = new ArrayList<>();
            JSONArray labels = issue.getJSONArray("labels");
            for (int j = 0; j < labels.length(); j++) {
                bug.labels.add(labels.getJSONObject(j).getString("name"));
            }
            
            // Determine difficulty from labels or title
            bug.difficulty = determineDifficulty(bug.title, bug.labels);
            bug.category = determineCategory(bug.title, bug.description, bug.labels);
            
            // Extract code from description if present
            extractCodeFromDescription(bug);
            
            bugs.add(bug);
        }
        
        return bugs;
    }
    
    private List<GitHubBug> parsePRsResponse(String json, String repoName) throws JSONException {
        List<GitHubBug> bugs = new ArrayList<>();
        JSONArray prs = new JSONArray(json);
        
        for (int i = 0; i < prs.length(); i++) {
            JSONObject pr = prs.getJSONObject(i);
            
            String title = pr.getString("title").toLowerCase();
            
            // Filter for bug fixes
            if (!title.contains("fix") && !title.contains("bug") && 
                !title.contains("resolve") && !title.contains("patch")) {
                continue;
            }
            
            GitHubBug bug = new GitHubBug();
            bug.title = pr.getString("title");
            bug.description = pr.optString("body", "Bug fix");
            bug.repoName = repoName;
            bug.sourceUrl = pr.getString("html_url");
            bug.language = detectLanguageFromRepo(repoName);
            bug.difficulty = "Medium";
            bug.category = determineCategory(bug.title, bug.description, new ArrayList<>());
            
            // Extract code snippets from PR body
            extractCodeFromDescription(bug);
            
            bugs.add(bug);
        }
        
        return bugs;
    }
    
    private void extractCodeFromDescription(GitHubBug bug) {
        String body = bug.description;
        if (body == null || body.isEmpty()) {
            bug.brokenCode = "// No code snippet available\n// Check the GitHub issue for details";
            bug.fixedCode = "// See the fix in the GitHub PR";
            bug.explanation = "See GitHub issue for full details.";
            return;
        }
        
        // Look for code blocks
        int codeStart = body.indexOf("```");
        if (codeStart != -1) {
            int codeEnd = body.indexOf("```", codeStart + 3);
            if (codeEnd != -1) {
                String codeBlock = body.substring(codeStart + 3, codeEnd);
                
                // Remove language identifier if present
                int newlineIndex = codeBlock.indexOf("\n");
                if (newlineIndex != -1 && newlineIndex < 20) {
                    codeBlock = codeBlock.substring(newlineIndex + 1);
                }
                
                bug.brokenCode = codeBlock.trim();
                
                // Look for second code block (the fix)
                int secondCodeStart = body.indexOf("```", codeEnd + 3);
                if (secondCodeStart != -1) {
                    int secondCodeEnd = body.indexOf("```", secondCodeStart + 3);
                    if (secondCodeEnd != -1) {
                        String fixBlock = body.substring(secondCodeStart + 3, secondCodeEnd);
                        int nl = fixBlock.indexOf("\n");
                        if (nl != -1 && nl < 20) {
                            fixBlock = fixBlock.substring(nl + 1);
                        }
                        bug.fixedCode = fixBlock.trim();
                    }
                }
            }
        }
        
        // If no code found, provide placeholder
        if (bug.brokenCode == null || bug.brokenCode.isEmpty()) {
            bug.brokenCode = "// Code snippet not available in issue\n// Visit: " + bug.sourceUrl;
            bug.fixedCode = "// Check the linked PR for the fix";
        }
        
        if (bug.fixedCode == null || bug.fixedCode.isEmpty()) {
            bug.fixedCode = "// Fix not extracted - see GitHub PR";
        }
        
        bug.explanation = "This bug was found in a real GitHub repository. " +
                         "Check the source link for full context and discussion.";
    }
    
    private String determineDifficulty(String title, List<String> labels) {
        String combined = title.toLowerCase();
        for (String label : labels) {
            combined += " " + label.toLowerCase();
        }
        
        if (combined.contains("beginner") || combined.contains("easy") || 
            combined.contains("good first issue") || combined.contains("trivial")) {
            return "Easy";
        }
        
        if (combined.contains("hard") || combined.contains("complex") || 
            combined.contains("advanced") || combined.contains("difficult")) {
            return "Hard";
        }
        
        return "Medium";
    }
    
    private String determineCategory(String title, String description, List<String> labels) {
        String combined = (title + " " + description).toLowerCase();
        for (String label : labels) {
            combined += " " + label.toLowerCase();
        }
        
        if (combined.contains("null") || combined.contains("nullpointer")) return "Null Handling";
        if (combined.contains("loop") || combined.contains("iteration")) return "Loops";
        if (combined.contains("array") || combined.contains("index")) return "Arrays";
        if (combined.contains("string")) return "Strings";
        if (combined.contains("recursion") || combined.contains("recursive")) return "Recursion";
        if (combined.contains("memory") || combined.contains("leak")) return "Memory";
        if (combined.contains("thread") || combined.contains("concurrent")) return "Concurrency";
        if (combined.contains("exception") || combined.contains("error")) return "Error Handling";
        if (combined.contains("logic") || combined.contains("condition")) return "Logic Errors";
        if (combined.contains("syntax")) return "Syntax";
        if (combined.contains("api") || combined.contains("network")) return "API";
        if (combined.contains("database") || combined.contains("sql")) return "Database";
        
        return "General";
    }
    
    private String detectLanguageFromRepo(String repoName) {
        String lower = repoName.toLowerCase();
        if (lower.contains("python") || lower.contains("py")) return "Python";
        if (lower.contains("javascript") || lower.contains("js") || lower.contains("node")) return "JavaScript";
        if (lower.contains("kotlin") || lower.contains("android")) return "Kotlin";
        if (lower.contains("swift") || lower.contains("ios")) return "Swift";
        if (lower.contains("go") || lower.contains("golang")) return "Go";
        if (lower.contains("rust")) return "Rust";
        if (lower.contains("cpp") || lower.contains("c++")) return "C++";
        return "Java";
    }
    
    private String[] getCuratedRepos(String language) {
        switch (language.toLowerCase()) {
            case "python":
                return new String[]{
                    "python/cpython",
                    "django/django",
                    "pallets/flask",
                    "numpy/numpy"
                };
            case "javascript":
                return new String[]{
                    "facebook/react",
                    "nodejs/node",
                    "expressjs/express",
                    "vuejs/vue"
                };
            case "java":
            default:
                return new String[]{
                    "spring-projects/spring-framework",
                    "google/guava",
                    "apache/commons-lang",
                    "junit-team/junit5"
                };
        }
    }
    
    private List<GitHubBug> fetchBugsFromRepo(String repo, String language) {
        try {
            String url = GITHUB_API_URL + "/repos/" + repo + "/issues?labels=bug&state=closed&per_page=10";
            String response = makeGitHubRequest(url);
            return parseIssuesResponse(response, language);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching from " + repo, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get pre-built bug exercises (offline fallback)
     */
    public List<GitHubBug> getOfflineBugs(String language) {
        List<GitHubBug> bugs = new ArrayList<>();
        
        if ("Python".equalsIgnoreCase(language)) {
            bugs.addAll(getPythonBugs());
        } else if ("JavaScript".equalsIgnoreCase(language)) {
            bugs.addAll(getJavaScriptBugs());
        } else {
            bugs.addAll(getJavaBugs());
        }
        
        return bugs;
    }
    
    private List<GitHubBug> getPythonBugs() {
        List<GitHubBug> bugs = new ArrayList<>();
        
        // Bug 1: Off-by-one in range
        GitHubBug bug1 = new GitHubBug();
        bug1.title = "Sum of Numbers - Off by One";
        bug1.description = "Calculate sum of numbers from 1 to n, but missing the last number";
        bug1.language = "Python";
        bug1.difficulty = "Easy";
        bug1.category = "Loops";
        bug1.brokenCode = "def sum_to_n(n):\n    total = 0\n    for i in range(n):  # Bug here\n        total += i\n    return total\n\nprint(sum_to_n(5))  # Should be 15";
        bug1.fixedCode = "def sum_to_n(n):\n    total = 0\n    for i in range(1, n + 1):  # Fixed\n        total += i\n    return total\n\nprint(sum_to_n(5))  # Returns 15";
        bug1.explanation = "range(n) goes from 0 to n-1. To sum 1 to n, use range(1, n+1).";
        bugs.add(bug1);
        
        // Bug 2: Mutable default argument
        GitHubBug bug2 = new GitHubBug();
        bug2.title = "List Accumulation Bug";
        bug2.description = "Function keeps appending to same list across calls";
        bug2.language = "Python";
        bug2.difficulty = "Medium";
        bug2.category = "Functions";
        bug2.brokenCode = "def add_item(item, items=[]):\n    items.append(item)\n    return items\n\nprint(add_item('a'))  # ['a']\nprint(add_item('b'))  # Expected ['b'], got ['a', 'b']";
        bug2.fixedCode = "def add_item(item, items=None):\n    if items is None:\n        items = []\n    items.append(item)\n    return items\n\nprint(add_item('a'))  # ['a']\nprint(add_item('b'))  # ['b']";
        bug2.explanation = "Mutable default arguments are shared between calls. Use None as default.";
        bugs.add(bug2);
        
        // Bug 3: String comparison
        GitHubBug bug3 = new GitHubBug();
        bug3.title = "Case-Sensitive Comparison";
        bug3.description = "User login fails due to case sensitivity";
        bug3.language = "Python";
        bug3.difficulty = "Easy";
        bug3.category = "Strings";
        bug3.brokenCode = "def check_username(input_name, stored_name):\n    if input_name == stored_name:\n        return True\n    return False\n\nprint(check_username('Admin', 'admin'))  # Should be True";
        bug3.fixedCode = "def check_username(input_name, stored_name):\n    if input_name.lower() == stored_name.lower():\n        return True\n    return False\n\nprint(check_username('Admin', 'admin'))  # True";
        bug3.explanation = "String comparison is case-sensitive. Use .lower() for case-insensitive comparison.";
        bugs.add(bug3);
        
        return bugs;
    }
    
    private List<GitHubBug> getJavaScriptBugs() {
        List<GitHubBug> bugs = new ArrayList<>();
        
        // Bug 1: var vs let in loop
        GitHubBug bug1 = new GitHubBug();
        bug1.title = "Closure in Loop Problem";
        bug1.description = "setTimeout prints wrong values in loop";
        bug1.language = "JavaScript";
        bug1.difficulty = "Medium";
        bug1.category = "Closures";
        bug1.brokenCode = "for (var i = 0; i < 3; i++) {\n  setTimeout(function() {\n    console.log(i);\n  }, 100);\n}\n// Prints: 3, 3, 3 (not 0, 1, 2)";
        bug1.fixedCode = "for (let i = 0; i < 3; i++) {\n  setTimeout(function() {\n    console.log(i);\n  }, 100);\n}\n// Prints: 0, 1, 2";
        bug1.explanation = "var is function-scoped, so all callbacks share the same i. let is block-scoped, creating a new i for each iteration.";
        bugs.add(bug1);
        
        // Bug 2: == vs ===
        GitHubBug bug2 = new GitHubBug();
        bug2.title = "Type Coercion Bug";
        bug2.description = "Comparison returns unexpected true";
        bug2.language = "JavaScript";
        bug2.difficulty = "Easy";
        bug2.category = "Comparisons";
        bug2.brokenCode = "function isZero(value) {\n  if (value == false) {\n    return true;\n  }\n  return false;\n}\n\nconsole.log(isZero('0'));  // true (unexpected!)\nconsole.log(isZero(0));    // true";
        bug2.fixedCode = "function isZero(value) {\n  if (value === 0) {\n    return true;\n  }\n  return false;\n}\n\nconsole.log(isZero('0'));  // false\nconsole.log(isZero(0));    // true";
        bug2.explanation = "== performs type coercion. Use === for strict equality without coercion.";
        bugs.add(bug2);
        
        // Bug 3: async/await missing
        GitHubBug bug3 = new GitHubBug();
        bug3.title = "Async Function Returns Promise";
        bug3.description = "Function returns Promise instead of value";
        bug3.language = "JavaScript";
        bug3.difficulty = "Medium";
        bug3.category = "Async";
        bug3.brokenCode = "async function fetchData() {\n  return await fetch('/api/data');\n}\n\nconst data = fetchData();\nconsole.log(data); // Promise { <pending> }";
        bug3.fixedCode = "async function fetchData() {\n  return await fetch('/api/data');\n}\n\nconst data = await fetchData();\nconsole.log(data); // Actual data\n\n// Or use .then()\nfetchData().then(data => console.log(data));";
        bug3.explanation = "Async functions always return a Promise. Use await or .then() to get the actual value.";
        bugs.add(bug3);
        
        return bugs;
    }
    
    private List<GitHubBug> getJavaBugs() {
        List<GitHubBug> bugs = new ArrayList<>();
        
        // Bug 1: String comparison
        GitHubBug bug1 = new GitHubBug();
        bug1.title = "String Equality Check";
        bug1.description = "String comparison using == fails";
        bug1.language = "Java";
        bug1.difficulty = "Easy";
        bug1.category = "Strings";
        bug1.brokenCode = "String s1 = new String(\"hello\");\nString s2 = new String(\"hello\");\n\nif (s1 == s2) {\n    System.out.println(\"Equal\");\n} else {\n    System.out.println(\"Not equal\"); // This prints!\n}";
        bug1.fixedCode = "String s1 = new String(\"hello\");\nString s2 = new String(\"hello\");\n\nif (s1.equals(s2)) {\n    System.out.println(\"Equal\"); // This prints!\n} else {\n    System.out.println(\"Not equal\");\n}";
        bug1.explanation = "== compares object references, not content. Use .equals() for String content comparison.";
        bugs.add(bug1);
        
        // Bug 2: Integer caching
        GitHubBug bug2 = new GitHubBug();
        bug2.title = "Integer Comparison Surprise";
        bug2.description = "Integer comparison works for small numbers but fails for large";
        bug2.language = "Java";
        bug2.difficulty = "Hard";
        bug2.category = "Objects";
        bug2.brokenCode = "Integer a = 127;\nInteger b = 127;\nSystem.out.println(a == b); // true\n\nInteger c = 128;\nInteger d = 128;\nSystem.out.println(c == d); // false (unexpected!)";
        bug2.fixedCode = "Integer a = 127;\nInteger b = 127;\nSystem.out.println(a.equals(b)); // true\n\nInteger c = 128;\nInteger d = 128;\nSystem.out.println(c.equals(d)); // true";
        bug2.explanation = "Java caches Integer objects from -128 to 127. Beyond this range, == compares references. Always use .equals() for object comparison.";
        bugs.add(bug2);
        
        return bugs;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
