package com.example.debugappproject.execution;

import java.util.ArrayList;
import java.util.List;

/**
 * CodeExecutionResult - Represents the result of executing Java code.
 *
 * Contains:
 * - Success/failure status
 * - Standard output captured from execution
 * - Error messages (compilation or runtime)
 * - Execution time in milliseconds
 * - Line number for errors (if applicable)
 *
 * Usage:
 * <pre>
 *     CodeExecutionResult result = engine.execute(code);
 *     if (result.isSuccess()) {
 *         String output = result.getOutput();
 *     } else {
 *         String error = result.getErrorMessage();
 *     }
 * </pre>
 */
public class CodeExecutionResult {

    private boolean success;
    private String output;
    private String errorMessage;
    private String errorType; // "COMPILATION_ERROR", "RUNTIME_ERROR", "TIMEOUT_ERROR"
    private int errorLineNumber; // -1 if not applicable
    private long executionTimeMs;
    private List<String> compilerWarnings;

    public CodeExecutionResult() {
        this.compilerWarnings = new ArrayList<>();
        this.errorLineNumber = -1;
    }

    /**
     * Creates a successful execution result.
     */
    public static CodeExecutionResult success(String output, long executionTimeMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.success = true;
        result.output = output != null ? output : "";
        result.executionTimeMs = executionTimeMs;
        result.errorType = null;
        return result;
    }

    /**
     * Creates a compilation error result.
     */
    public static CodeExecutionResult compilationError(String errorMessage, int lineNumber) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.errorType = "COMPILATION_ERROR";
        result.errorLineNumber = lineNumber;
        return result;
    }

    /**
     * Creates a runtime error result.
     */
    public static CodeExecutionResult runtimeError(String errorMessage, String output) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.success = false;
        result.errorMessage = errorMessage;
        result.errorType = "RUNTIME_ERROR";
        result.output = output != null ? output : "";
        return result;
    }

    /**
     * Creates a timeout error result.
     */
    public static CodeExecutionResult timeoutError(long timeoutMs) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.success = false;
        result.errorMessage = "Code execution timed out after " + timeoutMs + "ms. " +
                "Check for infinite loops or excessive computation.";
        result.errorType = "TIMEOUT_ERROR";
        return result;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public int getErrorLineNumber() {
        return errorLineNumber;
    }

    public void setErrorLineNumber(int errorLineNumber) {
        this.errorLineNumber = errorLineNumber;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<String> getCompilerWarnings() {
        return compilerWarnings;
    }

    public void addCompilerWarning(String warning) {
        this.compilerWarnings.add(warning);
    }

    /**
     * Returns a user-friendly error message with formatting.
     */
    public String getFormattedErrorMessage() {
        if (success) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        switch (errorType) {
            case "COMPILATION_ERROR":
                sb.append("❌ Compilation Error");
                if (errorLineNumber > 0) {
                    sb.append(" (Line ").append(errorLineNumber).append(")");
                }
                sb.append("\n\n").append(errorMessage);
                break;

            case "RUNTIME_ERROR":
                sb.append("❌ Runtime Error\n\n").append(errorMessage);
                if (output != null && !output.isEmpty()) {
                    sb.append("\n\nOutput before error:\n").append(output);
                }
                break;

            case "TIMEOUT_ERROR":
                sb.append("⏱️ Timeout Error\n\n").append(errorMessage);
                break;

            default:
                sb.append("❌ Error\n\n").append(errorMessage);
        }

        return sb.toString();
    }

    /**
     * Returns a summary for logging.
     */
    public String getSummary() {
        if (success) {
            return String.format("Success (%.2f ms) - Output length: %d chars",
                    executionTimeMs, output != null ? output.length() : 0);
        } else {
            return String.format("Failed (%s) - %s",
                    errorType, errorMessage);
        }
    }
}
