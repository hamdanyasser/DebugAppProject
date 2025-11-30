package com.example.debugappproject;

import android.app.Application;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;

import java.util.concurrent.Executors;

import dagger.hilt.android.HiltAndroidApp;

/**
 * DebugMasterApplication - Main application class for dependency injection.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 *
 * Initializes the database with seed data on first run.
 */
@HiltAndroidApp
public class DebugMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Seed database on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                android.util.Log.i("DebugMasterApp", "🚀 Starting database seeding...");
                BugRepository repository = new BugRepository(this);
                DatabaseSeeder.seedDatabase(this, repository);
                android.util.Log.i("DebugMasterApp", "✅ Database seeding complete!");
            } catch (Exception e) {
                android.util.Log.e("DebugMasterApp", "❌ Database seeding failed", e);
            }
        });
    }
}
