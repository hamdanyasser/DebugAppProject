package com.example.debugappproject.model;

/**
 * Enum representing different categories of debugging exercises.
 * Updated with new categories: Logic, Infinite Loop, Off-by-One,
 * API, Race Condition, Memory Leak, and Regex.
 */
public enum BugCategory {
    // General categories
    ALL("All"),
    LOOPS("Loops"),
    ARRAYS("Arrays"),
    OOP("OOP"),
    STRINGS("Strings"),
    CONDITIONALS("Conditionals"),
    EXCEPTIONS("Exceptions"),
    COLLECTIONS("Collections"),
    METHODS("Methods"),

    // New advanced categories
    LOGIC("Logic"),
    INFINITE_LOOP("Infinite Loop"),
    OFF_BY_ONE("Off-by-One"),
    API("API"),
    RACE_CONDITION("Race Condition"),
    MEMORY_LEAK("Memory Leak"),
    REGEX("Regex"),
    NULL_REFERENCE("Null Reference"),
    TYPE_ERROR("Type Error"),
    RECURSION("Recursion"),
    SYNTAX("Syntax"),
    ANDROID("Android");

    private final String displayName;

    BugCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to BugCategory, defaults to ALL if not found.
     */
    public static BugCategory fromString(String text) {
        for (BugCategory category : BugCategory.values()) {
            if (category.displayName.equalsIgnoreCase(text)) {
                return category;
            }
        }
        return ALL;
    }
}
