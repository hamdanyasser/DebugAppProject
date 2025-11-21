package com.example.debugappproject;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * DebugMasterApplication - Main application class for dependency injection.
 *
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 *
 * Hilt will generate:
 * - Application-level component
 * - Component managers
 * - Base classes for injection
 *
 * This serves as the root of the dependency graph and initializes
 * Hilt's code generation.
 */
@HiltAndroidApp
public class DebugMasterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Hilt automatically initializes components here
        // Additional app-wide initialization can go here
    }
}
