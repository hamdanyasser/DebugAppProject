package com.example.debugappproject.model;

/**
 * Enum representing different categories of debugging exercises.
 */
public enum BugCategory {
    ALL("All"),
    LOOPS("Loops"),
    ARRAYS("Arrays"),
    OOP("OOP"),
    STRINGS("Strings"),
    CONDITIONALS("Conditionals"),
    EXCEPTIONS("Exceptions"),
    COLLECTIONS("Collections"),
    METHODS("Methods");

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
