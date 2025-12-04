package com.example.debugappproject;

import android.app.Application;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.example.debugappproject.util.AchievementManager;
import com.example.debugappproject.util.ThemeManager;

import java.util.concurrent.Executors;

import dagger.hilt.android.HiltAndroidApp;

/**
 * DebugMasterApplication - Main application class for dependency injection.
 *
 * Initializes:
 * - Theme (Dark/Light/System)
 * - Database seeding
 * - Achievement tracking
 * - Daily login recording
 */
@HiltAndroidApp
public class DebugMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme preference FIRST (before any UI)
        ThemeManager.getInstance(this).applyTheme();
        
        // Initialize on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                android.util.Log.i("DebugMasterApp", "Starting database seeding...");
                BugRepository repository = new BugRepository(this);
                DatabaseSeeder.seedDatabase(this, repository);
                android.util.Log.i("DebugMasterApp", "Database seeding complete!");
                
                // Record daily login for achievements
                AchievementManager.getInstance(this).recordDailyLogin();
                
                // Check for any newly earned achievements
                AchievementManager.getInstance(this).checkAllAchievements();
                
            } catch (Exception e) {
                android.util.Log.e("DebugMasterApp", "Initialization failed", e);
            }
        });
    }
}
