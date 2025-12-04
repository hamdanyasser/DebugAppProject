package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager - Handles app-wide theme switching (Dark/Light/System)
 * 
 * Features:
 * - Dark mode
 * - Light mode  
 * - Follow system setting
 * - Persists preference
 * - Smooth transitions
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    
    private static ThemeManager instance;
    private final SharedPreferences prefs;
    private final Context context;
    
    private ThemeManager(Context context) {
        this.context = context.getApplicationContext();
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
     */
    public void applyTheme() {
        int mode = getThemeMode();
        applyThemeMode(mode);
    }
    
    /**
     * Set and apply a new theme mode.
     */
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        applyThemeMode(mode);
    }
    
    /**
     * Get the current theme mode.
     */
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }
    
    /**
     * Check if dark mode is currently active.
     */
    public boolean isDarkMode() {
        int mode = getThemeMode();
        if (mode == THEME_DARK) {
            return true;
        } else if (mode == THEME_LIGHT) {
            return false;
        } else {
            // System mode - check actual system setting
            int nightModeFlags = context.getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
    }
    
    /**
     * Get theme mode name for display.
     */
    public String getThemeModeName() {
        int mode = getThemeMode();
        switch (mode) {
            case THEME_LIGHT:
                return "Light";
            case THEME_DARK:
                return "Dark";
            case THEME_SYSTEM:
            default:
                return "System";
        }
    }
    
    /**
     * Get theme mode name for display with emoji.
     */
    public String getThemeModeNameWithEmoji() {
        int mode = getThemeMode();
        switch (mode) {
            case THEME_LIGHT:
                return "Light";
            case THEME_DARK:
                return "Dark";
            case THEME_SYSTEM:
            default:
                return "System";
        }
    }
    
    /**
     * Cycle to next theme mode (Light -> Dark -> System -> Light).
     */
    public void cycleTheme() {
        int current = getThemeMode();
        int next = (current + 1) % 3;
        setThemeMode(next);
    }
    
    private void applyThemeMode(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
        }
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
