package com.example.debugappproject.model;

/**
 * TestCase represents a single test case for a bug exercise.
 * Used to display expected inputs and outputs for testing the user's fix.
 *
 * Note: These are NOT executed - they are for display purposes only.
 * The actual validation is done by comparing the user's code to the fixedCode.
 */
public class TestCase {

    private String input;       // Input for the test case (e.g., "\"secret\"")
    private String expected;    // Expected output (e.g., "true")
    private String description; // Description of what this test checks
    private boolean passed;     // Whether this test passed (set at runtime)

    public TestCase() {
        this.passed = false;
    }

    public TestCase(String input, String expected, String description) {
        this.input = input;
        this.expected = expected;
        this.description = description;
        this.passed = false;
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

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
