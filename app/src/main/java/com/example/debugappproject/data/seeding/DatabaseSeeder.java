package com.example.debugappproject.data.seeding;

import android.content.Context;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.BugInPath;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.LearningPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - COMPREHENSIVE LEARNING PLATFORM                      ║
 * ║         90+ Real Debugging Challenges Across 15 Learning Paths              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * FREE PATHS (4):
 * 1. Getting Started - Basic debugging fundamentals
 * 3. Python Power - Learn Python debugging  
 * 10. HTML & CSS - Web styling debug basics
 * 12. Data Structures - Core CS fundamentals
 * 
 * PRO PATHS (11): 
 * Everything else for serious learners
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";
    private static final int SEED_VERSION = 6; // Increment to force reseed (v6: Schema fix with DB v13)
    
    // Thread-safety: prevent concurrent seeding from multiple entry points
    private static final Object SEED_LOCK = new Object();
    private static volatile boolean seedingInProgress = false;

    public static void seedDatabase(Context context, BugRepository repository) {
        // Thread-safety guard: only one seeding operation at a time
        synchronized (SEED_LOCK) {
            if (seedingInProgress) {
                android.util.Log.w(TAG, "⚠️ Seeding already in progress, skipping duplicate call");
                return;
            }
            seedingInProgress = true;
        }
        
        try {
            seedDatabaseInternal(context, repository);
        } finally {
            synchronized (SEED_LOCK) {
                seedingInProgress = false;
            }
        }
    }
    
    private static void seedDatabaseInternal(Context context, BugRepository repository) {
        android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
        android.util.Log.i(TAG, "📊 DEBUGMASTER DATABASE SEEDER v" + SEED_VERSION);
        android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
        
        // Check seed version
        android.content.SharedPreferences prefs = context.getSharedPreferences("db_seed_prefs", Context.MODE_PRIVATE);
        int savedVersion = prefs.getInt("seed_version", 0);
        
        int bugCount = 0;
        int pathCount = 0;
        int bugInPathCount = 0;
        
        try {
            bugCount = repository.getBugCountSync();
            pathCount = repository.getPathCountSync();
            bugInPathCount = repository.getLearningPathDao().getBugInPathCountSync();
            android.util.Log.i(TAG, "📈 Current state: " + bugCount + " bugs, " + pathCount + " paths, " + bugInPathCount + " bug-path mappings");
        } catch (android.database.sqlite.SQLiteException sqlEx) {
            // Schema/migration error - DO NOT reseed, let migration fix it first
            android.util.Log.e(TAG, "❌ SQLite schema error - migration needed, skipping seed", sqlEx);
            return;
        } catch (IllegalStateException ise) {
            // Database in bad state (e.g., migration failed) - don't reseed
            android.util.Log.e(TAG, "❌ Database in invalid state - skipping seed", ise);
            return;
        } catch (Exception e) {
            // Other errors - check if it's a schema-related error
            String msg = e.getMessage();
            if (msg != null && (msg.contains("migration") || msg.contains("schema") || 
                msg.contains("no such column") || msg.contains("no such table") ||
                msg.contains("duplicate column"))) {
                android.util.Log.e(TAG, "❌ Schema-related error - skipping seed", e);
                return;
            }
            android.util.Log.e(TAG, "Error checking database, will attempt reseed", e);
        }

        // CRITICAL: Reseed if version changed, bug_in_path is empty, or paths need updating
        boolean versionChanged = savedVersion < SEED_VERSION;
        boolean needsReseed = bugCount < 80 || pathCount < 15 || bugInPathCount < 50 || versionChanged;
        
        if (versionChanged) {
            android.util.Log.i(TAG, "🔄 Seed version changed (" + savedVersion + " -> " + SEED_VERSION + "), forcing reseed...");
        }
        
        if (!needsReseed) {
            android.util.Log.i(TAG, "✅ Database already has sufficient content - skipping seed");
            return;
        }
        
        android.util.Log.i(TAG, "🔄 RESEEDING DATABASE (missing content or version update)...");

        try {
            // Clear existing data first to avoid conflicts
            android.util.Log.i(TAG, "🧹 Clearing existing path data...");
            repository.getLearningPathDao().clearAllBugInPath();
            repository.getLearningPathDao().clearAllPaths();
            
            InputStream inputStream = context.getAssets().open("bugs.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = gson.fromJson(jsonBuilder.toString(), type);

            String bugsJson = gson.toJson(data.get("bugs"));
            Type bugListType = new TypeToken<List<Bug>>() {}.getType();
            List<Bug> bugs = gson.fromJson(bugsJson, bugListType);

            // Parse hints (may be null)
            List<Hint> hints = new ArrayList<>();
            Object hintsObj = data.get("hints");
            if (hintsObj != null) {
                String hintsJson = gson.toJson(hintsObj);
                Type hintListType = new TypeToken<List<Hint>>() {}.getType();
                List<Hint> parsedHints = gson.fromJson(hintsJson, hintListType);
                if (parsedHints != null) {
                    hints = parsedHints;
                }
            }

            // Insert bugs
            repository.insertBugsSync(bugs);
            android.util.Log.i(TAG, "✅ Inserted " + bugs.size() + " bugs");
            
            // Insert hints only if we have them
            if (!hints.isEmpty()) {
                repository.insertHintsSync(hints);
                android.util.Log.i(TAG, "✅ Inserted " + hints.size() + " hints");
            } else {
                android.util.Log.w(TAG, "⚠️ No hints found in bugs.json - skipping hints");
            }
            
            repository.insertInitialProgressSync();

            // Create paths and assign bugs
            seedLearningPathsSync(repository, bugs);
            seedAchievementsSync(repository);

            android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
            android.util.Log.i(TAG, "🎉 DATABASE SEEDING COMPLETE!");
            android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
            
            // Save seed version to prevent re-seeding
            prefs.edit().putInt("seed_version", SEED_VERSION).apply();

        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Failed to seed database", e);
            e.printStackTrace();
        }
    }

    private static void seedLearningPathsSync(BugRepository repository, List<Bug> bugs) {
        List<LearningPath> paths = new ArrayList<>();
        List<BugInPath> bugInPathList = new ArrayList<>();

        android.util.Log.i(TAG, "📚 Creating 15 learning paths (4 FREE, 11 PRO)...");

        // ═══════════════════════════════════════════════════════════════════════
        // FREE PATHS - Available to all users
        // ═══════════════════════════════════════════════════════════════════════
        
        // PATH 1: FREE - Getting Started
        paths.add(createPath(1, "Getting Started", 
            "Your debugging journey begins here! Learn the fundamentals every developer needs. Perfect for beginners.",
            "🚀", "Beginner", 1, false, "Fundamentals", 45, 12, 150, true, false, "#10B981",
            "Interactive tutorials with step-by-step guidance. Learn to spot common mistakes."));

        // PATH 2: PRO - Java Mastery
        paths.add(createPath(2, "Java Mastery",
            "Master Java debugging from NullPointerException to Collections. The complete Java debug guide.",
            "☕", "Intermediate", 2, true, "Programming", 120, 30, 400, true, false, "#F59E0B",
            "Deep dive into Java's quirks with real-world examples from production code."));

        // PATH 3: FREE - Python Power  
        paths.add(createPath(3, "Python Power",
            "Debug Python like a pro! From indentation errors to decorators. Master Pythonic debugging.",
            "🐍", "Beginner", 3, false, "Programming", 60, 15, 250, true, true, "#3B82F6",
            "Interactive Python playground with instant feedback. Perfect for data scientists."));

        // PATH 4: PRO - JavaScript Ninja
        paths.add(createPath(4, "JavaScript Ninja",
            "Conquer JS quirks: hoisting, closures, async/await, and the infamous 'this' keyword.",
            "⚡", "Intermediate", 4, true, "Programming", 90, 20, 300, false, true, "#FBBF24",
            "Master the language that powers the web with animated explanations."));

        // PATH 5: PRO - Kotlin & Android
        paths.add(createPath(5, "Kotlin & Android",
            "Debug Android apps like a senior dev. Memory leaks, lifecycle bugs, null safety mastered.",
            "📱", "Advanced", 5, true, "Mobile", 100, 20, 350, false, false, "#A855F7",
            "Real Android Studio debugging techniques with memory profiler guides."));

        // PATH 6: PRO - Prompt Engineering
        paths.add(createPath(6, "Prompt Engineering",
            "Master AI prompts! Get better results from ChatGPT, Claude, and other LLMs. The future is here.",
            "🤖", "Beginner", 6, true, "AI/ML", 45, 10, 300, true, true, "#EC4899",
            "Learn prompt patterns, chain-of-thought, and system prompt optimization."));

        // PATH 7: PRO - Advanced Debugging
        paths.add(createPath(7, "Advanced Debugging",
            "Expert-level bugs: concurrency, performance, memory leaks. For senior developers only.",
            "🧠", "Advanced", 7, true, "Expert", 90, 15, 400, false, true, "#8B5CF6",
            "Race conditions, deadlocks, and heap analysis techniques explained."));

        // PATH 8: PRO - SQL & Databases
        paths.add(createPath(8, "SQL & Databases",
            "Fix queries, prevent SQL injection, optimize JOINs. Database debugging mastery.",
            "🗄️", "Intermediate", 8, true, "Database", 60, 12, 280, false, false, "#06B6D4",
            "Query plans, indexing strategies, and transaction isolation levels."));

        // PATH 9: PRO - API & Backend
        paths.add(createPath(9, "API & Backend",
            "Debug REST APIs, handle errors gracefully, fix CORS nightmares. Backend essentials.",
            "🔌", "Intermediate", 9, true, "Backend", 75, 15, 320, false, false, "#14B8A6",
            "HTTP status codes, authentication bugs, and rate limiting issues."));

        // PATH 10: FREE - HTML & CSS
        paths.add(createPath(10, "HTML & CSS",
            "Fix layouts, z-index nightmares, flexbox issues, and responsive design bugs.",
            "🎨", "Beginner", 10, false, "Web", 45, 10, 200, false, false, "#F97316",
            "Visual debugging with browser DevTools. See your fixes in real-time."));

        // PATH 11: PRO - React Debugging
        paths.add(createPath(11, "React Debugging",
            "useState batching, useEffect infinite loops, key prop errors. Master React's tricky behaviors.",
            "⚛️", "Intermediate", 11, true, "Web", 80, 15, 350, false, true, "#61DAFB",
            "React DevTools, component profiling, and hooks best practices."));

        // PATH 12: FREE - Data Structures
        paths.add(createPath(12, "Data Structures",
            "Arrays, linked lists, trees, stacks, queues. Debug the structures that power all software.",
            "📚", "Intermediate", 12, false, "CS Fundamentals", 90, 18, 380, true, false, "#EF4444",
            "Animated visualizations of data structure operations and common bugs."));

        // PATH 13: PRO - Algorithm Bugs
        paths.add(createPath(13, "Algorithm Bugs",
            "Binary search, recursion, sorting bugs. Fix the algorithms that interviewers love to test.",
            "🧮", "Advanced", 13, true, "CS Fundamentals", 80, 15, 350, false, false, "#F472B6",
            "Step-by-step algorithm execution with bug spotting exercises."));

        // PATH 14: PRO - Clean Code
        paths.add(createPath(14, "Clean Code",
            "Magic numbers, god methods, deep nesting. Learn to write bug-resistant code from the start.",
            "✨", "Intermediate", 14, true, "Best Practices", 50, 12, 280, false, false, "#84CC16",
            "Code smells detection and refactoring patterns with before/after examples."));

        // PATH 15: PRO - Interview Prep
        paths.add(createPath(15, "Interview Prep",
            "FizzBuzz to Two Sum. Debug the classic coding problems that appear in FAANG interviews.",
            "💼", "Intermediate", 15, true, "Career", 100, 20, 500, true, true, "#0EA5E9",
            "Mock interview questions with time pressure. Get job-ready!"));

        repository.insertLearningPathsSync(paths);
        android.util.Log.i(TAG, "✅ Created " + paths.size() + " learning paths (4 FREE: #1,3,10,12)");

        // ═══════════════════════════════════════════════════════════════════════
        // INTELLIGENT BUG ASSIGNMENT
        // ═══════════════════════════════════════════════════════════════════════
        
        int[] order = new int[16];
        for (int i = 0; i < 16; i++) order[i] = 1;
        Set<String> assigned = new HashSet<>();

        android.util.Log.i(TAG, "🔗 Assigning " + bugs.size() + " bugs to paths...");

        for (Bug bug : bugs) {
            String cat = safe(bug.getCategory());
            String diff = safe(bug.getDifficulty());
            String lang = safe(bug.getLanguage());

            // PATH 1: Getting Started (FREE) - All Easy Fundamentals
            if (diff.equals("Easy")) {
                assign(bugInPathList, assigned, bug.getId(), 1, order);
            }

            // PATH 2: Java Mastery - All Java bugs
            if (lang.equals("Java")) {
                assign(bugInPathList, assigned, bug.getId(), 2, order);
            }

            // PATH 3: Python Power (FREE) - All Python bugs
            if (lang.equals("Python")) {
                assign(bugInPathList, assigned, bug.getId(), 3, order);
            }

            // PATH 4: JavaScript Ninja - JS bugs (not React)
            if (lang.equals("JavaScript") && !cat.equals("React")) {
                assign(bugInPathList, assigned, bug.getId(), 4, order);
            }

            // PATH 5: Kotlin & Android
            if (lang.equals("Kotlin") || cat.equals("Android")) {
                assign(bugInPathList, assigned, bug.getId(), 5, order);
            }

            // PATH 6: Prompt Engineering
            if (lang.equals("Prompt") || cat.equals("PromptEngineering")) {
                assign(bugInPathList, assigned, bug.getId(), 6, order);
            }

            // PATH 7: Advanced Debugging - Hard bugs
            if (diff.equals("Hard")) {
                assign(bugInPathList, assigned, bug.getId(), 7, order);
            }

            // PATH 8: SQL & Databases
            if (lang.equals("SQL") || cat.equals("Database") || cat.equals("Security")) {
                assign(bugInPathList, assigned, bug.getId(), 8, order);
            }

            // PATH 9: API & Backend
            if (cat.equals("API") || cat.equals("Async") || cat.equals("Exceptions")) {
                assign(bugInPathList, assigned, bug.getId(), 9, order);
            }

            // PATH 10: HTML & CSS (FREE)
            if (lang.equals("HTML") || lang.equals("CSS") || cat.equals("Styling")) {
                assign(bugInPathList, assigned, bug.getId(), 10, order);
            }

            // PATH 11: React Debugging
            if (cat.equals("React") || (lang.equals("JavaScript") && cat.equals("React"))) {
                assign(bugInPathList, assigned, bug.getId(), 11, order);
            }

            // PATH 12: Data Structures (FREE)
            if (cat.equals("Arrays") || cat.equals("Collections") || cat.equals("DataStructures") ||
                cat.equals("Strings")) {
                assign(bugInPathList, assigned, bug.getId(), 12, order);
            }

            // PATH 13: Algorithm Bugs
            if (cat.equals("Algorithms") || cat.equals("Recursion") || cat.equals("Loops") ||
                cat.equals("Performance")) {
                assign(bugInPathList, assigned, bug.getId(), 13, order);
            }

            // PATH 14: Clean Code
            if (cat.equals("CleanCode") || cat.equals("OOP") || cat.equals("Methods") ||
                cat.equals("Variables") || cat.equals("VersionControl")) {
                assign(bugInPathList, assigned, bug.getId(), 14, order);
            }

            // PATH 15: Interview Prep
            if (cat.equals("Interview")) {
                assign(bugInPathList, assigned, bug.getId(), 15, order);
            }
        }

        // Ensure all paths have minimum content by adding fallbacks
        for (Bug bug : bugs) {
            String diff = safe(bug.getDifficulty());
            String lang = safe(bug.getLanguage());
            
            // Fill Kotlin/Android with Java OOP if too few
            if (order[5] <= 5 && lang.equals("Java") && safe(bug.getCategory()).equals("OOP")) {
                assign(bugInPathList, assigned, bug.getId(), 5, order);
            }
            // Fill Prompt Engineering with Medium bugs if too few
            if (order[6] <= 5 && diff.equals("Medium")) {
                assign(bugInPathList, assigned, bug.getId(), 6, order);
            }
            // Fill React with JavaScript bugs if too few
            if (order[11] <= 5 && lang.equals("JavaScript")) {
                assign(bugInPathList, assigned, bug.getId(), 11, order);
            }
            // Fill Interview Prep with Medium/Hard Java bugs
            if (order[15] <= 5 && (diff.equals("Medium") || diff.equals("Hard")) && lang.equals("Java")) {
                assign(bugInPathList, assigned, bug.getId(), 15, order);
            }
        }

        // Insert all bug-path mappings
        repository.insertBugInPathsSync(bugInPathList);
        
        // Log results
        StringBuilder log = new StringBuilder("\n╔════════════════════════════════════════╗\n");
        log.append("║       PATH CONTENT SUMMARY            ║\n");
        log.append("╠════════════════════════════════════════╣\n");
        String[] names = {"", "Getting Started ★", "Java Mastery", "Python Power ★", "JS Ninja", "Kotlin/Android",
                         "Prompt Eng", "Advanced", "SQL/DB", "API/Backend", "HTML/CSS ★", "React", 
                         "Data Structures ★", "Algorithms", "Clean Code", "Interview"};
        for (int i = 1; i <= 15; i++) {
            int count = order[i] - 1;
            String bar = "";
            for (int j = 0; j < Math.min(count, 20); j++) bar += "█";
            log.append(String.format("║ %2d. %-18s %3d %s\n", i, names[i], count, bar));
        }
        log.append("╠════════════════════════════════════════╣\n");
        log.append("║ ★ = FREE PATH                         ║\n");
        log.append("║ TOTAL ASSIGNMENTS: ").append(bugInPathList.size()).append("\n");
        log.append("╚════════════════════════════════════════╝");
        android.util.Log.i(TAG, log.toString());
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static void assign(List<BugInPath> list, Set<String> assigned, int bugId, int pathId, int[] order) {
        String key = bugId + "-" + pathId;
        if (!assigned.contains(key)) {
            assigned.add(key);
            list.add(new BugInPath(bugId, pathId, order[pathId]++));
        }
    }

    private static LearningPath createPath(int id, String name, String desc, String emoji,
                                           String diff, int sortOrder, boolean locked, String category,
                                           int mins, int lessons, int xp, boolean featured, 
                                           boolean isNew, String color, String tutorial) {
        LearningPath p = new LearningPath(name, desc, emoji, diff, sortOrder, locked,
                category, mins, lessons, xp, featured, isNew, color);
        p.setId(id);
        p.setTutorialContent(tutorial);
        
        // Set new fields for redesigned Learn tab
        p.setPopularityScore(calculatePopularity(featured, isNew, locked));
        p.setTags(generateTags(diff, mins, isNew, featured, locked));
        p.setPrimaryCategory(mapToPrimaryCategory(category));
        
        return p;
    }
    
    /**
     * Calculate popularity score (0-100)
     */
    private static int calculatePopularity(boolean featured, boolean isNew, boolean locked) {
        int score = 50; // Base score
        if (featured) score += 30;
        if (isNew) score += 20;
        if (!locked) score += 10; // Free paths slightly more "popular" for discoverability
        return Math.min(score, 100);
    }
    
    /**
     * Generate comma-separated tags for filtering
     */
    private static String generateTags(String difficulty, int minutes, boolean isNew, boolean featured, boolean locked) {
        List<String> tags = new ArrayList<>();
        
        // Difficulty tags
        if ("Beginner".equals(difficulty)) tags.add("beginner");
        else if ("Intermediate".equals(difficulty)) tags.add("intermediate");
        else if ("Advanced".equals(difficulty) || "Expert".equals(difficulty)) tags.add("advanced");
        
        // Duration tags
        if (minutes <= 45) tags.add("short");
        else if (minutes >= 90) tags.add("comprehensive");
        
        // Status tags
        if (isNew) tags.add("new");
        if (featured) tags.add("popular");
        if (!locked) tags.add("free");
        
        return String.join(",", tags);
    }
    
    /**
     * Map existing category to primary category for grouping
     */
    private static String mapToPrimaryCategory(String category) {
        if (category == null) return "Other";
        switch (category) {
            case "Programming":
                return "Languages";
            case "CS Fundamentals":
                return "Data Structures & Algorithms";
            case "Best Practices":
                return "Clean Code";
            case "Career":
                return "Interview Prep";
            case "Expert":
            case "Advanced":
                return "Advanced";
            case "AI/ML":
                return "AI & ML";
            case "Web":
            case "Backend":
            case "Database":
                return "Web";
            case "Mobile":
                return "Mobile";
            case "Fundamentals":
                return "Getting Started";
            default:
                return category;
        }
    }

    private static void seedAchievementsSync(BugRepository repository) {
        // Use comprehensive achievement list from AchievementManager
        List<AchievementDefinition> achievements = com.example.debugappproject.util.AchievementManager.getAllAchievementDefinitions();
        
        repository.insertAchievementsSync(achievements);
        android.util.Log.i(TAG, "Created " + achievements.size() + " achievements");
    }
}
