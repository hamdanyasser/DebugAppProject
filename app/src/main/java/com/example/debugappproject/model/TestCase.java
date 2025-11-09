package com.example.debugappproject.model;

/**
 * TestCase - Represents a single unit test for a bug exercise.
 * These are display-only and not actually executed.
 * They provide a unit-test feel to the learning experience.
 *
 * Example:
 * Input: "secret"
 * Expected: "true"
 * Description: "Correct password"
 */
public class TestCase {

    private String input;
    private String expected;
    private String description;

    // Constructor
    public TestCase(String input, String expected, String description) {
        this.input = input;
        this.expected = expected;
        this.description = description;
    }

    // Getters and Setters
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
