package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Bug entity representing a debugging exercise.
 * Each bug contains broken code that students must debug.
 */
@Entity(tableName = "bugs")
public class Bug implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey
    private int id;

    private String title;
    private String language;        // Programming language (e.g., "Java")
    private String difficulty;      // "Easy", "Medium", "Hard"
    private String category;        // "Loops", "Arrays", "OOP", etc.
    private String description;     // What the program is supposed to do
    private String brokenCode;      // Code snippet with the bug
    private String expectedOutput;  // What the correct output should be
    private String actualOutput;    // What the buggy code outputs/error message
    private String explanation;     // Root cause explanation of the bug
    private String fixedCode;       // The corrected version of the code
    private boolean isCompleted;    // Whether user has solved this bug
    private String starterCode;     // Starting code for user's attempt (defaults to brokenCode if null)
    private String userNotes;       // User's personal notes for this bug
    private String testsJson;       // JSON string containing test cases
    private String hint;            // Helpful hint without giving away the answer

    // Constructor
    public Bug(int id, String title, String language, String difficulty, String category,
               String description, String brokenCode, String expectedOutput, String actualOutput,
               String explanation, String fixedCode, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.difficulty = difficulty;
        this.category = category;
        this.description = description;
        this.brokenCode = brokenCode;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.explanation = explanation;
        this.fixedCode = fixedCode;
        this.isCompleted = isCompleted;
        this.starterCode = null;
        this.userNotes = "";
        this.testsJson = null;
        this.hint = null;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrokenCode() {
        return brokenCode;
    }

    public void setBrokenCode(String brokenCode) {
        this.brokenCode = brokenCode;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getFixedCode() {
        return fixedCode;
    }

    public void setFixedCode(String fixedCode) {
        this.fixedCode = fixedCode;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public String getTestsJson() {
        return testsJson;
    }

    public void setTestsJson(String testsJson) {
        this.testsJson = testsJson;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    /**
     * Gets the starting code for the user's fix attempt.
     * Returns starterCode if set, otherwise falls back to brokenCode.
     */
    public String getInitialCode() {
        return (starterCode != null && !starterCode.isEmpty()) ? starterCode : brokenCode;
    }
    
    /**
     * Gets a hint for this bug. If no hint is set, generates one based on the error type.
     */
    public String getHintText() {
        if (hint != null && !hint.isEmpty()) {
            return hint;
        }
        // Generate a generic hint based on the actual output/error
        if (actualOutput != null) {
            if (actualOutput.contains("cannot find symbol")) {
                return "Check for typos in method names, variable names, or class names. Java is case-sensitive!";
            } else if (actualOutput.contains("';' expected")) {
                return "Look for missing semicolons at the end of statements.";
            } else if (actualOutput.contains("ArrayIndexOutOfBounds")) {
                return "Arrays start at index 0 and end at length-1. Check your loop boundaries!";
            } else if (actualOutput.contains("NullPointer")) {
                return "Something is null that shouldn't be. Add null checks before using objects.";
            } else if (actualOutput.contains("infinite")) {
                return "Check your loop condition and make sure the loop variable changes correctly.";
            } else if (actualOutput.contains("Syntax")) {
                return "There's a syntax error. Check brackets, parentheses, and quotes.";
            }
        }
        return "Read the error message carefully. Compare your code character by character with what should work.";
    }
}
