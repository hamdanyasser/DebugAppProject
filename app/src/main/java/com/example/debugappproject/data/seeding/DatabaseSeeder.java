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
 * DatabaseSeeder handles loading initial data from assets/bugs.json
 * and populating the Room database with bugs, hints, learning paths, and achievements.
 */
public class DatabaseSeeder {

    /**
     * Seeds the database with initial data from JSON file.
     * This is called once on first app launch.
     */
    public static void seedDatabase(Context context, BugRepository repository) {
        // Check if database is already seeded
        int bugCount = repository.getBugCountSync();
        if (bugCount > 0) {
            return; // Already seeded
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

            // Extract bugs
            String bugsJson = gson.toJson(data.get("bugs"));
            Type bugListType = new TypeToken<List<Bug>>() {}.getType();
            List<Bug> bugs = gson.fromJson(bugsJson, bugListType);

            // Extract hints
            String hintsJson = gson.toJson(data.get("hints"));
            Type hintListType = new TypeToken<List<Hint>>() {}.getType();
            List<Hint> hints = gson.fromJson(hintsJson, hintListType);

            // Insert bugs and hints synchronously (we're already on a background thread)
            repository.insertBugsSync(bugs);
            repository.insertHintsSync(hints);
            repository.insertInitialProgressSync();

            // Seed learning paths
            seedLearningPathsSync(repository, bugs);

            // Seed achievements
            seedAchievementsSync(repository);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and seeds default learning paths based on bug categories and difficulties.
     * Synchronous version - called from background thread.
     */
    private static void seedLearningPathsSync(BugRepository repository, List<Bug> bugs) {
        List<LearningPath> paths = new ArrayList<>();
        List<BugInPath> bugInPathList = new ArrayList<>();

        // Path 1: Basics of Debugging (Easy bugs)
        LearningPath path1 = new LearningPath(
            "Basics of Debugging",
            "Start your debugging journey with simple, common mistakes",
            "🎯",
            "Easy",
            1,
            false
        );
        path1.setId(1); // Explicitly set ID to match BugInPath references
        paths.add(path1);

        // Path 2: Null & Exception Handling
        LearningPath path2 = new LearningPath(
            "Nulls & Crashes",
            "Master null pointer exceptions and error handling",
            "⚠️",
            "Easy-Medium",
            2,
            false
        );
        path2.setId(2); // Explicitly set ID to match BugInPath references
        paths.add(path2);

        // Path 3: Collections & Edge Cases
        LearningPath path3 = new LearningPath(
            "Collections & Arrays",
            "Tackle array indexing and collection modification bugs",
            "📚",
            "Medium",
            3,
            false
        );
        path3.setId(3); // Explicitly set ID to match BugInPath references
        paths.add(path3);

        // Path 4: Advanced Debugging
        LearningPath path4 = new LearningPath(
            "Advanced Challenges",
            "Hard bugs requiring deep understanding",
            "🏆",
            "Hard",
            4,
            false
        );
        path4.setId(4); // Explicitly set ID to match BugInPath references
        paths.add(path4);

        repository.insertLearningPathsSync(paths);

        // Assign bugs to paths (simplified mapping based on difficulty and category)
        int pathId = 1;
        int orderInPath = 1;
        for (Bug bug : bugs) {
            // Simplified logic: assign by difficulty
            if ("Easy".equals(bug.getDifficulty())) {
                bugInPathList.add(new BugInPath(bug.getId(), 1, orderInPath++));
            } else if ("Medium".equals(bug.getDifficulty())) {
                if ("Arrays".equals(bug.getCategory()) || "Collections".equals(bug.getCategory())) {
                    bugInPathList.add(new BugInPath(bug.getId(), 3, orderInPath++));
                } else if ("Exceptions".equals(bug.getCategory())) {
                    bugInPathList.add(new BugInPath(bug.getId(), 2, orderInPath++));
                }
            } else if ("Hard".equals(bug.getDifficulty())) {
                bugInPathList.add(new BugInPath(bug.getId(), 4, orderInPath++));
            }

            // Also add exception-related easy bugs to path 2
            if ("Easy".equals(bug.getDifficulty()) && "Exceptions".equals(bug.getCategory())) {
                bugInPathList.add(new BugInPath(bug.getId(), 2, orderInPath++));
            }
        }

        repository.insertBugInPathsSync(bugInPathList);
    }

    /**
     * Seeds predefined achievements synchronously.
     * Called from background thread.
     */
    private static void seedAchievementsSync(BugRepository repository) {
        List<AchievementDefinition> achievements = AchievementManager.getDefaultAchievements();
        repository.insertAchievementsSync(achievements);
    }
}
