package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager - Handles app-wide theme (Dark mode only)
 *
 * Note: This app is designed for dark mode only to match the gaming aesthetic.
 * The UI uses dark backgrounds, neon colors, and glassmorphic effects that
 * are optimized for dark mode.
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    
    private static ThemeManager instance;
    private final SharedPreferences prefs;

    private ThemeManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }
    
    /**
     * Apply the saved theme preference.
     * Call this in Application.onCreate() or Activity.onCreate()
     * Note: This app is designed for dark mode only to match the gaming aesthetic.
     */
    public void applyTheme() {
        // Force dark mode - app is designed for dark theme only
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    /**
     * Set and apply a new theme mode.
     * Note: This app is designed for dark mode only, so this always applies dark mode.
     */
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        // Force dark mode - app is designed for dark theme only
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    /**
     * Get the current theme mode.
     */
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }
    
    /**
     * Check if dark mode is currently active.
     * Note: This app is designed for dark mode only, so this always returns true.
     */
    public boolean isDarkMode() {
        return true; // App is dark mode only
    }
    
    /**
     * Get theme mode name for display.
     * Note: This app is dark mode only.
     */
    public String getThemeModeName() {
        return "Dark"; // App is dark mode only
    }

    /**
     * Get theme mode name for display with emoji.
     * Note: This app is dark mode only.
     */
    public String getThemeModeNameWithEmoji() {
        return "Dark"; // App is dark mode only
    }
    
    /**
     * Cycle to next theme mode.
     * Note: This app is dark mode only, so this is a no-op.
     */
    public void cycleTheme() {
        // App is dark mode only - no cycling
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    
    /**
     * Get all available theme options.
     */
    public static String[] getThemeOptions() {
        return new String[]{"Light", "Dark", "System Default"};
    }
    
    /**
     * Get theme descriptions.
     */
    public static String[] getThemeDescriptions() {
        return new String[]{
            "Always use light theme",
            "Always use dark theme", 
            "Follow your device's theme setting"
        };
    }
}
