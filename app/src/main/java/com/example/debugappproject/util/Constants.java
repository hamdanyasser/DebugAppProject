package com.example.debugappproject.util;

/**
 * Constants used throughout the DebugMaster app.
 */
public class Constants {

    // Difficulty levels
    public static final String DIFFICULTY_ALL = "All";
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_HARD = "Hard";

    // Categories
    public static final String CATEGORY_ALL = "All";
    public static final String CATEGORY_LOOPS = "Loops";
    public static final String CATEGORY_ARRAYS = "Arrays";
    public static final String CATEGORY_OOP = "OOP";
    public static final String CATEGORY_STRINGS = "Strings";
    public static final String CATEGORY_CONDITIONALS = "Conditionals";
    public static final String CATEGORY_EXCEPTIONS = "Exceptions";
    public static final String CATEGORY_COLLECTIONS = "Collections";
    public static final String CATEGORY_METHODS = "Methods";

    // Navigation arguments
    public static final String ARG_BUG_ID = "bug_id";

    // SharedPreferences keys
    public static final String PREFS_NAME = "DebugMasterPrefs";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_DATABASE_SEEDED = "database_seeded";

    // UI constants
    public static final int MAX_HINT_LEVEL = 3;
    public static final int SPLASH_DELAY_MS = 5500;  // 5.5 seconds for premium game-like splash animation

    private Constants() {
        // Prevent instantiation
    }
}
