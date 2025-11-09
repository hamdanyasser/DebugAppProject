package com.example.debugappproject.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

/**
 * Bug entity representing a debugging exercise.
 * Each bug contains broken code that students must debug.
 */
@Entity(tableName = "bugs")
public class Bug {

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
    private String starterCode;     // Optional: starter code for the editor (null = use brokenCode)
    private String userNotes;       // Optional: student's personal notes about this bug

    @Ignore
    private List<TestCase> tests;   // Optional: test cases for display (not persisted, read-only from JSON)

    // Constructor
    public Bug(int id, String title, String language, String difficulty, String category,
               String description, String brokenCode, String expectedOutput, String actualOutput,
               String explanation, String fixedCode, boolean isCompleted, String starterCode, String userNotes) {
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
        this.starterCode = starterCode;
        this.userNotes = userNotes;
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

    public List<TestCase> getTests() {
        return tests;
    }

    public void setTests(List<TestCase> tests) {
        this.tests = tests;
    }

    /**
     * Helper method to get the initial code for the editor.
     * Returns starterCode if available, otherwise returns brokenCode.
     */
    public String getInitialEditorCode() {
        return starterCode != null ? starterCode : brokenCode;
    }

    /**
     * Helper method to check if this bug has test cases defined.
     */
    public boolean hasTests() {
        return tests != null && !tests.isEmpty();
    }
}
