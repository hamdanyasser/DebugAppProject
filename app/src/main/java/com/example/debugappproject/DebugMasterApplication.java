package com.example.debugappproject;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.example.debugappproject.util.AchievementManager;
import com.example.debugappproject.util.SoundManager;
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

    private int activityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme preference FIRST (before any UI)
        ThemeManager.getInstance(this).applyTheme();

        // Register activity lifecycle callbacks to manage resources
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                activityCount++;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                activityCount--;
                if (activityCount == 0) {
                    // Last activity stopped - pause sounds
                    SoundManager.getInstance(DebugMasterApplication.this).pauseAll();
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });

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

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Release SoundManager resources
        SoundManager.getInstance(this).release();
        // Shutdown AchievementManager executor
        AchievementManager.getInstance(this).shutdown();
    }
}
