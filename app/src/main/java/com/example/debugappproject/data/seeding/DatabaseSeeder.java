package com.example.debugappproject.data.seeding;

import android.content.Context;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.BugInPath;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.util.AchievementManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DatabaseSeeder loads initial content from bugs.json and creates learning paths.
 * This creates a complete learning experience with 30+ bugs across 6 learning paths.
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    public static void seedDatabase(Context context, BugRepository repository) {
        try {
            // Check if already seeded
            int bugCount = repository.getBugCountSync();
            if (bugCount > 0) {
                int pathCount = repository.getPathCountSync();
                if (pathCount > 0) {
                    android.util.Log.i(TAG, "Database already seeded: " + bugCount + " bugs, " + pathCount + " paths");
                    return;
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error checking database, will reseed", e);
        }

        try {
            // Read JSON from assets
            InputStream inputStream = context.getAssets().open("bugs.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            // Parse JSON
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = gson.fromJson(jsonBuilder.toString(), type);

            // Extract and insert bugs
            String bugsJson = gson.toJson(data.get("bugs"));
            Type bugListType = new TypeToken<List<Bug>>() {}.getType();
            List<Bug> bugs = gson.fromJson(bugsJson, bugListType);

            // Extract and insert hints
            String hintsJson = gson.toJson(data.get("hints"));
            Type hintListType = new TypeToken<List<Hint>>() {}.getType();
            List<Hint> hints = gson.fromJson(hintsJson, hintListType);

            repository.insertBugsSync(bugs);
            repository.insertHintsSync(hints);
            repository.insertInitialProgressSync();

            // Create learning paths
            seedLearningPathsSync(repository, bugs);

            // Create achievements
            seedAchievementsSync(repository);

            android.util.Log.i(TAG, "Database seeded successfully with " + bugs.size() + " bugs");

        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to seed database", e);
        }
    }

    private static void seedLearningPathsSync(BugRepository repository, List<Bug> bugs) {
        List<LearningPath> paths = new ArrayList<>();
        List<BugInPath> bugInPathList = new ArrayList<>();

        // Path 1: Java Fundamentals (Free)
        LearningPath path1 = new LearningPath(
            "Java Fundamentals",
            "Start here! Learn to spot common Java mistakes that every developer makes.",
            "☕",
            "Beginner",
            1,
            false // Not locked - free content
        );
        path1.setId(1);
        paths.add(path1);

        // Path 2: Loop & Control Flow (Free)
        LearningPath path2 = new LearningPath(
            "Loops & Control Flow",
            "Master loops, conditionals, and control flow debugging.",
            "🔄",
            "Beginner",
            2,
            false
        );
        path2.setId(2);
        paths.add(path2);

        // Path 3: Strings & Data (Pro)
        LearningPath path3 = new LearningPath(
            "Strings & Data Types",
            "Debug string manipulation and data type conversion issues.",
            "📝",
            "Intermediate",
            3,
            true // Pro content
        );
        path3.setId(3);
        paths.add(path3);

        // Path 4: Collections & Arrays (Pro)
        LearningPath path4 = new LearningPath(
            "Collections & Arrays",
            "Tackle array indexing, list operations, and map pitfalls.",
            "📚",
            "Intermediate",
            4,
            true
        );
        path4.setId(4);
        paths.add(path4);

        // Path 5: OOP & Design (Pro)
        LearningPath path5 = new LearningPath(
            "OOP & Design Patterns",
            "Debug object-oriented code and common design issues.",
            "🏗️",
            "Advanced",
            5,
            true
        );
        path5.setId(5);
        paths.add(path5);

        // Path 6: Multi-Language (Pro)
        LearningPath path6 = new LearningPath(
            "Multi-Language Mastery",
            "Debug Kotlin, Python, JavaScript bugs and more!",
            "🌍",
            "Advanced",
            6,
            true
        );
        path6.setId(6);
        paths.add(path6);

        repository.insertLearningPathsSync(paths);

        // Assign bugs to paths intelligently
        int orderPath1 = 1, orderPath2 = 1, orderPath3 = 1;
        int orderPath4 = 1, orderPath5 = 1, orderPath6 = 1;

        for (Bug bug : bugs) {
            String category = bug.getCategory();
            String difficulty = bug.getDifficulty();
            String language = bug.getLanguage();

            // Path 1: Java Fundamentals - Easy bugs, various categories
            if ("Easy".equals(difficulty) && "Java".equals(language) && 
                ("Methods".equals(category) || "Exceptions".equals(category))) {
                bugInPathList.add(new BugInPath(bug.getId(), 1, orderPath1++));
            }

            // Path 2: Loops & Control Flow
            if (("Loops".equals(category) || "Conditionals".equals(category)) && "Java".equals(language)) {
                bugInPathList.add(new BugInPath(bug.getId(), 2, orderPath2++));
            }

            // Path 3: Strings & Data Types
            if ("Strings".equals(category) || 
                (category != null && category.contains("String"))) {
                bugInPathList.add(new BugInPath(bug.getId(), 3, orderPath3++));
            }

            // Path 4: Collections & Arrays
            if ("Collections".equals(category) || "Arrays".equals(category)) {
                bugInPathList.add(new BugInPath(bug.getId(), 4, orderPath4++));
            }

            // Path 5: OOP & Design
            if ("OOP".equals(category) || "Recursion".equals(category) || "Algorithms".equals(category)) {
                bugInPathList.add(new BugInPath(bug.getId(), 5, orderPath5++));
            }

            // Path 6: Multi-Language
            if (!"Java".equals(language)) {
                bugInPathList.add(new BugInPath(bug.getId(), 6, orderPath6++));
            }
        }

        // Ensure each path has at least some content
        // Add any unassigned Easy bugs to Path 1
        for (Bug bug : bugs) {
            if ("Easy".equals(bug.getDifficulty()) && "Java".equals(bug.getLanguage())) {
                boolean alreadyAssigned = false;
                for (BugInPath bip : bugInPathList) {
                    if (bip.getBugId() == bug.getId()) {
                        alreadyAssigned = true;
                        break;
                    }
                }
                if (!alreadyAssigned) {
                    bugInPathList.add(new BugInPath(bug.getId(), 1, orderPath1++));
                }
            }
        }

        repository.insertBugInPathsSync(bugInPathList);
        android.util.Log.i(TAG, "Created " + paths.size() + " learning paths with " + bugInPathList.size() + " bug assignments");
    }

    private static void seedAchievementsSync(BugRepository repository) {
        List<AchievementDefinition> achievements = new ArrayList<>();

        // Beginner Achievements
        achievements.add(new AchievementDefinition(
            "first_bug", "First Bug Squashed", "Complete your first debugging challenge",
            "🐛", 10, "bugs_fixed", 1
        ));
        
        achievements.add(new AchievementDefinition(
            "bug_hunter_10", "Bug Hunter", "Fix 10 bugs",
            "🎯", 50, "bugs_fixed", 10
        ));

        achievements.add(new AchievementDefinition(
            "bug_slayer_25", "Bug Slayer", "Fix 25 bugs",
            "⚔️", 100, "bugs_fixed", 25
        ));

        achievements.add(new AchievementDefinition(
            "bug_master_50", "Bug Master", "Fix 50 bugs",
            "🏆", 200, "bugs_fixed", 50
        ));

        achievements.add(new AchievementDefinition(
            "bug_legend_100", "Bug Legend", "Fix 100 bugs",
            "👑", 500, "bugs_fixed", 100
        ));

        // Streak Achievements
        achievements.add(new AchievementDefinition(
            "streak_3", "Getting Started", "Maintain a 3-day streak",
            "🔥", 30, "streak_days", 3
        ));

        achievements.add(new AchievementDefinition(
            "streak_7", "On Fire", "Maintain a 7-day streak",
            "🔥", 75, "streak_days", 7
        ));

        achievements.add(new AchievementDefinition(
            "streak_30", "Dedicated Debugger", "Maintain a 30-day streak",
            "💎", 300, "streak_days", 30
        ));

        // Perfect Score Achievements
        achievements.add(new AchievementDefinition(
            "perfect_5", "Sharp Eye", "Get 5 perfect scores (no hints)",
            "👁️", 50, "perfect_scores", 5
        ));

        achievements.add(new AchievementDefinition(
            "perfect_20", "Eagle Eye", "Get 20 perfect scores",
            "🦅", 150, "perfect_scores", 20
        ));

        // Speed Achievements
        achievements.add(new AchievementDefinition(
            "speed_demon", "Speed Demon", "Fix a bug in under 30 seconds",
            "⚡", 25, "speed_fix", 1
        ));

        achievements.add(new AchievementDefinition(
            "lightning_fast", "Lightning Fast", "Fix 10 bugs in under 30 seconds each",
            "⚡", 100, "speed_fix", 10
        ));

        // Path Completion
        achievements.add(new AchievementDefinition(
            "path_complete_1", "Path Pioneer", "Complete your first learning path",
            "🛤️", 100, "paths_completed", 1
        ));

        achievements.add(new AchievementDefinition(
            "path_complete_3", "Path Explorer", "Complete 3 learning paths",
            "🗺️", 250, "paths_completed", 3
        ));

        achievements.add(new AchievementDefinition(
            "path_complete_all", "Path Master", "Complete all learning paths",
            "🏅", 1000, "paths_completed", 6
        ));

        // Language Achievements
        achievements.add(new AchievementDefinition(
            "java_expert", "Java Expert", "Fix 20 Java bugs",
            "☕", 100, "java_bugs", 20
        ));

        achievements.add(new AchievementDefinition(
            "polyglot", "Polyglot", "Fix bugs in 3 different languages",
            "🌍", 75, "languages_used", 3
        ));

        // Special Achievements
        achievements.add(new AchievementDefinition(
            "night_owl", "Night Owl", "Debug after midnight",
            "🦉", 25, "special", 1
        ));

        achievements.add(new AchievementDefinition(
            "early_bird", "Early Bird", "Debug before 6 AM",
            "🐦", 25, "special", 1
        ));

        achievements.add(new AchievementDefinition(
            "weekend_warrior", "Weekend Warrior", "Debug on Saturday and Sunday",
            "💪", 50, "special", 1
        ));

        repository.insertAchievementsSync(achievements);
        android.util.Log.i(TAG, "Created " + achievements.size() + " achievements");
    }
}
