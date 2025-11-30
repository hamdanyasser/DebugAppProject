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
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    public static void seedDatabase(Context context, BugRepository repository) {
        android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
        android.util.Log.i(TAG, "📊 DEBUGMASTER DATABASE SEEDER");
        android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
        
        int bugCount = 0;
        int pathCount = 0;
        int bugInPathCount = 0;
        
        try {
            bugCount = repository.getBugCountSync();
            pathCount = repository.getPathCountSync();
            bugInPathCount = repository.getLearningPathDao().getBugInPathCountSync();
            android.util.Log.i(TAG, "📈 Current state: " + bugCount + " bugs, " + pathCount + " paths, " + bugInPathCount + " bug-path mappings");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error checking database, will reseed", e);
        }

        // CRITICAL: Reseed if bug_in_path is empty (paths will appear empty in UI)
        boolean needsReseed = bugCount < 80 || pathCount < 15 || bugInPathCount < 50;
        
        if (!needsReseed) {
            android.util.Log.i(TAG, "✅ Database already has sufficient content - skipping seed");
            return;
        }
        
        android.util.Log.i(TAG, "🔄 RESEEDING DATABASE (missing content detected)...");

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

            String hintsJson = gson.toJson(data.get("hints"));
            Type hintListType = new TypeToken<List<Hint>>() {}.getType();
            List<Hint> hints = gson.fromJson(hintsJson, hintListType);

            // Insert bugs and hints
            repository.insertBugsSync(bugs);
            repository.insertHintsSync(hints);
            repository.insertInitialProgressSync();
            android.util.Log.i(TAG, "✅ Inserted " + bugs.size() + " bugs and " + hints.size() + " hints");

            // Create paths and assign bugs
            seedLearningPathsSync(repository, bugs);
            seedAchievementsSync(repository);

            android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");
            android.util.Log.i(TAG, "🎉 DATABASE SEEDING COMPLETE!");
            android.util.Log.i(TAG, "═══════════════════════════════════════════════════════");

        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Failed to seed database", e);
            e.printStackTrace();
        }
    }

    private static void seedLearningPathsSync(BugRepository repository, List<Bug> bugs) {
        List<LearningPath> paths = new ArrayList<>();
        List<BugInPath> bugInPathList = new ArrayList<>();

        android.util.Log.i(TAG, "📚 Creating 15 learning paths...");

        // ═══════════════════════════════════════════════════════════════════════
        // 15 COMPREHENSIVE LEARNING PATHS
        // ═══════════════════════════════════════════════════════════════════════
        
        paths.add(createPath(1, "🚀 Getting Started", 
            "Your debugging journey begins! Learn fundamentals that every developer needs.",
            "🚀", "Beginner", 1, false, "Fundamentals", 45, 12, 150, true, false, "#10B981"));

        paths.add(createPath(2, "☕ Java Mastery",
            "Master Java debugging from NullPointer to Collections. The complete Java debug guide.",
            "☕", "Intermediate", 2, true, "Programming", 120, 30, 400, true, false, "#F59E0B"));

        paths.add(createPath(3, "🐍 Python Power",
            "Debug Python like a pro! From indentation to decorators, master Pythonic debugging.",
            "🐍", "Beginner", 3, true, "Programming", 60, 15, 250, true, true, "#3B82F6"));

        paths.add(createPath(4, "⚡ JavaScript Ninja",
            "Conquer JS quirks: hoisting, closures, async/await, and the infamous 'this' keyword.",
            "⚡", "Intermediate", 4, true, "Programming", 90, 20, 300, false, true, "#FBBF24"));

        paths.add(createPath(5, "📱 Kotlin & Android",
            "Debug Android apps like a senior dev. Memory leaks, lifecycle, null safety mastered.",
            "📱", "Advanced", 5, true, "Mobile", 100, 20, 350, false, false, "#A855F7"));

        paths.add(createPath(6, "🤖 Prompt Engineering",
            "Master AI prompts! Get better results from ChatGPT, Claude, and other LLMs.",
            "🤖", "Beginner", 6, true, "AI/ML", 45, 10, 300, true, true, "#EC4899"));

        paths.add(createPath(7, "🧠 Advanced Debugging",
            "Expert-level bugs: concurrency, performance, memory issues. For senior developers.",
            "🧠", "Advanced", 7, true, "Expert", 90, 15, 400, false, true, "#8B5CF6"));

        paths.add(createPath(8, "🗄️ SQL & Databases",
            "Fix queries, prevent SQL injection, optimize JOINs. Database debugging mastery.",
            "🗄️", "Intermediate", 8, true, "Database", 60, 12, 280, false, false, "#06B6D4"));

        paths.add(createPath(9, "🔌 API & Backend",
            "Debug REST APIs, handle errors gracefully, fix CORS. Backend debugging essentials.",
            "🔌", "Intermediate", 9, true, "Backend", 75, 15, 320, false, false, "#14B8A6"));

        paths.add(createPath(10, "🎨 HTML & CSS",
            "Fix layouts, z-index nightmares, flexbox issues. Frontend styling debug guide.",
            "🎨", "Beginner", 10, true, "Web", 45, 10, 200, false, false, "#F97316"));

        paths.add(createPath(11, "⚛️ React Debugging",
            "useState batching, useEffect loops, key props. Master React's tricky behaviors.",
            "⚛️", "Intermediate", 11, true, "Web", 80, 15, 350, false, true, "#61DAFB"));

        paths.add(createPath(12, "📚 Data Structures",
            "Arrays, linked lists, trees, stacks. Debug the structures that power all software.",
            "📚", "Intermediate", 12, true, "CS Fundamentals", 90, 18, 380, true, false, "#EF4444"));

        paths.add(createPath(13, "🧮 Algorithm Bugs",
            "Binary search, recursion, sorting bugs. Fix the algorithms that interviewers love.",
            "🧮", "Advanced", 13, true, "CS Fundamentals", 80, 15, 350, false, false, "#F472B6"));

        paths.add(createPath(14, "✨ Clean Code",
            "Magic numbers, god methods, deep nesting. Write bug-resistant code from the start.",
            "✨", "Intermediate", 14, true, "Best Practices", 50, 12, 280, false, false, "#84CC16"));

        paths.add(createPath(15, "💼 Interview Prep",
            "FizzBuzz to Two Sum. Debug the classics that appear in every coding interview.",
            "💼", "Intermediate", 15, true, "Career", 100, 20, 500, true, true, "#0EA5E9"));

        repository.insertLearningPathsSync(paths);
        android.util.Log.i(TAG, "✅ Created " + paths.size() + " learning paths");

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

            // PATH 3: Python Power - All Python bugs
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

            // PATH 10: HTML & CSS
            if (lang.equals("HTML") || lang.equals("CSS") || cat.equals("Styling")) {
                assign(bugInPathList, assigned, bug.getId(), 10, order);
            }

            // PATH 11: React Debugging
            if (cat.equals("React") || (lang.equals("JavaScript") && cat.equals("React"))) {
                assign(bugInPathList, assigned, bug.getId(), 11, order);
            }

            // PATH 12: Data Structures
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
        String[] names = {"", "Getting Started", "Java Mastery", "Python Power", "JS Ninja", "Kotlin/Android",
                         "Prompt Eng", "Advanced", "SQL/DB", "API/Backend", "HTML/CSS", "React", 
                         "Data Structures", "Algorithms", "Clean Code", "Interview"};
        for (int i = 1; i <= 15; i++) {
            int count = order[i] - 1;
            String bar = "";
            for (int j = 0; j < Math.min(count, 20); j++) bar += "█";
            log.append(String.format("║ %2d. %-14s %3d %s\n", i, names[i], count, bar));
        }
        log.append("╠════════════════════════════════════════╣\n");
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
                                           boolean isNew, String color) {
        LearningPath p = new LearningPath(name, desc, emoji, diff, sortOrder, locked,
                category, mins, lessons, xp, featured, isNew, color);
        p.setId(id);
        return p;
    }

    private static void seedAchievementsSync(BugRepository repository) {
        List<AchievementDefinition> achievements = new ArrayList<>();

        // Bug Achievements
        achievements.add(new AchievementDefinition("first_bug", "First Bug Squashed", "Complete your first challenge", "🐛", 10, "bugs_fixed", 1));
        achievements.add(new AchievementDefinition("bug_hunter_10", "Bug Hunter", "Fix 10 bugs", "🎯", 50, "bugs_fixed", 10));
        achievements.add(new AchievementDefinition("bug_slayer_25", "Bug Slayer", "Fix 25 bugs", "⚔️", 100, "bugs_fixed", 25));
        achievements.add(new AchievementDefinition("bug_master_50", "Bug Master", "Fix 50 bugs", "🏆", 200, "bugs_fixed", 50));
        achievements.add(new AchievementDefinition("bug_legend_100", "Bug Legend", "Fix 100 bugs!", "👑", 500, "bugs_fixed", 100));

        // Streaks
        achievements.add(new AchievementDefinition("streak_3", "Warming Up", "3-day streak", "🔥", 30, "streak_days", 3));
        achievements.add(new AchievementDefinition("streak_7", "On Fire!", "7-day streak", "🔥", 75, "streak_days", 7));
        achievements.add(new AchievementDefinition("streak_14", "Unstoppable", "14-day streak", "💥", 150, "streak_days", 14));
        achievements.add(new AchievementDefinition("streak_30", "Dedicated", "30-day streak", "💎", 300, "streak_days", 30));

        // Perfect Scores
        achievements.add(new AchievementDefinition("perfect_5", "Sharp Eye", "5 perfect scores", "👁️", 50, "perfect_scores", 5));
        achievements.add(new AchievementDefinition("perfect_20", "Eagle Eye", "20 perfect scores", "🦅", 150, "perfect_scores", 20));

        // Speed
        achievements.add(new AchievementDefinition("speed_demon", "Speed Demon", "Fix in under 30s", "⚡", 25, "speed_fix", 1));
        achievements.add(new AchievementDefinition("lightning_fast", "Lightning Fast", "10 fast fixes", "⚡", 100, "speed_fix", 10));

        // Paths
        achievements.add(new AchievementDefinition("path_complete_1", "Path Pioneer", "Complete 1 path", "🛤️", 100, "paths_completed", 1));
        achievements.add(new AchievementDefinition("path_complete_3", "Path Explorer", "Complete 3 paths", "🗺️", 250, "paths_completed", 3));
        achievements.add(new AchievementDefinition("path_complete_5", "Path Master", "Complete 5 paths", "🧭", 400, "paths_completed", 5));
        achievements.add(new AchievementDefinition("path_complete_all", "Path Legend", "Complete ALL paths", "🏅", 2000, "paths_completed", 15));

        // Languages
        achievements.add(new AchievementDefinition("java_expert", "Java Expert", "20 Java bugs fixed", "☕", 100, "java_bugs", 20));
        achievements.add(new AchievementDefinition("python_master", "Python Master", "15 Python bugs fixed", "🐍", 100, "python_bugs", 15));
        achievements.add(new AchievementDefinition("js_ninja", "JS Ninja", "15 JavaScript bugs fixed", "⚡", 100, "js_bugs", 15));
        achievements.add(new AchievementDefinition("polyglot", "Polyglot", "Fix bugs in 3 languages", "🌍", 75, "languages_used", 3));

        // Battle
        achievements.add(new AchievementDefinition("first_victory", "First Victory", "Win a battle", "⚔️", 50, "battles_won", 1));
        achievements.add(new AchievementDefinition("battle_veteran", "Battle Veteran", "Win 10 battles", "🛡️", 150, "battles_won", 10));
        achievements.add(new AchievementDefinition("arena_champion", "Arena Champion", "Win 50 battles", "🏆", 500, "battles_won", 50));

        // Levels
        achievements.add(new AchievementDefinition("level_5", "Rising Star", "Reach Level 5", "⭐", 50, "level", 5));
        achievements.add(new AchievementDefinition("level_10", "Skilled", "Reach Level 10", "🌟", 100, "level", 10));
        achievements.add(new AchievementDefinition("level_25", "Expert", "Reach Level 25", "💫", 250, "level", 25));

        // Special
        achievements.add(new AchievementDefinition("night_owl", "Night Owl", "Debug after midnight", "🦉", 25, "special", 1));
        achievements.add(new AchievementDefinition("early_bird", "Early Bird", "Debug before 6 AM", "🐦", 25, "special", 1));
        achievements.add(new AchievementDefinition("weekend_warrior", "Weekend Warrior", "Debug on weekend", "💪", 50, "special", 1));
        achievements.add(new AchievementDefinition("pro_member", "Pro Member", "Subscribe to Pro", "👑", 100, "special", 1));

        repository.insertAchievementsSync(achievements);
        android.util.Log.i(TAG, "✅ Created " + achievements.size() + " achievements");
    }
}
