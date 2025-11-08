package com.example.debugappproject.data.seeding;

import android.content.Context;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * DatabaseSeeder handles loading initial bug data from assets/bugs.json
 * and populating the Room database.
 */
public class DatabaseSeeder {

    /**
     * Seeds the database with initial bug and hint data from JSON file.
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

            // Insert into database
            repository.insertBugs(bugs);
            repository.insertHints(hints);
            repository.insertInitialProgress();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
