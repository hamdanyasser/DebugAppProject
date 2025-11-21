package com.example.debugappproject.execution;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CodeExecutionEngine.
 *
 * Tests the core functionality of compiling and executing Java code:
 * - Successful code execution with output capture
 * - Compilation error detection with line numbers
 * - Runtime error handling
 * - Timeout enforcement for infinite loops
 * - Output comparison and validation
 *
 * These tests ensure the KILLER FEATURE works reliably.
 */
public class CodeExecutionEngineTest {

    private CodeExecutionEngine engine;

    @Before
    public void setUp() {
        engine = new CodeExecutionEngine();
        engine.setTimeout(2000); // 2 second timeout for tests
    }

    @After
    public void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }

    // ========== Successful Execution Tests ==========

    @Test
    public void testSimplePrint_Success() {
        String code = "System.out.println(\"Hello, World!\");";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Code should execute successfully", result.isSuccess());
        assertTrue("Output should contain 'Hello, World!'",
                result.getOutput().contains("Hello, World!"));
        assertTrue("Execution time should be recorded",
                result.getExecutionTimeMs() >= 0);
    }

    @Test
    public void testMultipleStatements_Success() {
        String code = "int x = 5;\n" +
                     "int y = 10;\n" +
                     "System.out.println(x + y);";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Code should execute successfully", result.isSuccess());
        assertTrue("Output should contain '15'",
                result.getOutput().contains("15"));
    }

    @Test
    public void testLoopExecution_Success() {
        String code = "for (int i = 1; i <= 5; i++) {\n" +
                     "    System.out.println(i);\n" +
                     "}";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Loop should execute successfully", result.isSuccess());
        String output = result.getOutput();
        assertTrue("Output should contain all numbers 1-5",
                output.contains("1") && output.contains("2") &&
                output.contains("3") && output.contains("4") &&
                output.contains("5"));
    }

    @Test
    public void testArrayOperations_Success() {
        String code = "int[] arr = {1, 2, 3, 4, 5};\n" +
                     "int sum = 0;\n" +
                     "for (int num : arr) {\n" +
                     "    sum += num;\n" +
                     "}\n" +
                     "System.out.println(sum);";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Array operations should succeed", result.isSuccess());
        assertTrue("Sum should be 15", result.getOutput().contains("15"));
    }

    // ========== Compilation Error Tests ==========

    @Test
    public void testSyntaxError_MissingSemicolon() {
        String code = "System.out.println(\"Hello\")"; // Missing semicolon

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Code with syntax error should fail", result.isSuccess());
        assertEquals("Should be compilation error",
                "COMPILATION_ERROR", result.getErrorType());
        assertNotNull("Error message should exist", result.getErrorMessage());
    }

    @Test
    public void testUndeclaredVariable_CompilationError() {
        String code = "System.out.println(unknownVariable);";

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Code with undeclared variable should fail", result.isSuccess());
        assertEquals("Should be compilation error",
                "COMPILATION_ERROR", result.getErrorType());
    }

    @Test
    public void testTypeMismatch_CompilationError() {
        String code = "String text = 123;"; // Type mismatch

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Code with type mismatch should fail", result.isSuccess());
        assertEquals("Should be compilation error",
                "COMPILATION_ERROR", result.getErrorType());
    }

    // ========== Runtime Error Tests ==========

    @Test
    public void testArrayIndexOutOfBounds_RuntimeError() {
        String code = "int[] arr = {1, 2, 3};\n" +
                     "System.out.println(arr[5]);"; // Index 5 out of bounds

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Code with array out of bounds should fail", result.isSuccess());
        assertEquals("Should be runtime error",
                "RUNTIME_ERROR", result.getErrorType());
        assertTrue("Error message should mention array bounds",
                result.getErrorMessage().toLowerCase().contains("bound") ||
                result.getErrorMessage().toLowerCase().contains("index"));
    }

    @Test
    public void testDivisionByZero_RuntimeError() {
        String code = "int x = 10;\n" +
                     "int y = 0;\n" +
                     "System.out.println(x / y);"; // Division by zero

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Division by zero should fail", result.isSuccess());
        assertEquals("Should be runtime error",
                "RUNTIME_ERROR", result.getErrorType());
    }

    @Test
    public void testNullPointerException_RuntimeError() {
        String code = "String text = null;\n" +
                     "System.out.println(text.length());"; // NPE

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Null pointer should fail", result.isSuccess());
        assertEquals("Should be runtime error",
                "RUNTIME_ERROR", result.getErrorType());
        assertTrue("Error should mention null",
                result.getErrorMessage().toLowerCase().contains("null"));
    }

    // ========== Timeout Tests ==========

    @Test
    public void testInfiniteLoop_Timeout() {
        String code = "while (true) {\n" +
                     "    // Infinite loop\n" +
                     "}";

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Infinite loop should timeout", result.isSuccess());
        assertEquals("Should be timeout error",
                "TIMEOUT_ERROR", result.getErrorType());
        assertTrue("Error message should mention timeout",
                result.getErrorMessage().toLowerCase().contains("timeout"));
    }

    @Test
    public void testLongRunningLoop_Timeout() {
        String code = "for (long i = 0; i < 999999999999L; i++) {\n" +
                     "    // Very long loop\n" +
                     "}";

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Long running code should timeout", result.isSuccess());
        assertEquals("Should be timeout error",
                "TIMEOUT_ERROR", result.getErrorType());
    }

    // ========== Edge Cases and Validation Tests ==========

    @Test
    public void testEmptyCode_CompilationError() {
        String code = "";

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Empty code should fail", result.isSuccess());
        assertEquals("Should be compilation error",
                "COMPILATION_ERROR", result.getErrorType());
    }

    @Test
    public void testOnlyWhitespace_CompilationError() {
        String code = "   \n  \t  \n  ";

        CodeExecutionResult result = engine.execute(code);

        assertFalse("Whitespace only should fail", result.isSuccess());
        assertEquals("Should be compilation error",
                "COMPILATION_ERROR", result.getErrorType());
    }

    @Test
    public void testOutputWithSpecialCharacters_Success() {
        String code = "System.out.println(\"Hello\\nWorld\\t!\");";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Special characters should work", result.isSuccess());
        assertTrue("Output should have newline",
                result.getOutput().contains("\n"));
    }

    @Test
    public void testMathOperations_Success() {
        String code = "double x = Math.sqrt(16);\n" +
                     "System.out.println(x);";

        CodeExecutionResult result = engine.execute(code);

        assertTrue("Math operations should work", result.isSuccess());
        assertTrue("Square root of 16 should be 4",
                result.getOutput().contains("4"));
    }

    // ========== Self-Test ==========

    @Test
    public void testEngine_SelfTest() {
        boolean selfTestPassed = engine.runSelfTest();

        assertTrue("Engine self-test should pass", selfTestPassed);
    }

    // ========== Result Object Tests ==========

    @Test
    public void testSuccessResult_FormattedMessage() {
        CodeExecutionResult result = CodeExecutionResult.success("Test output", 100);

        assertTrue("Should be success", result.isSuccess());
        assertEquals("Output should match", "Test output", result.getOutput());
        assertEquals("Execution time should match", 100, result.getExecutionTimeMs());
        assertNull("Formatted error message should be null for success",
                result.getFormattedErrorMessage());
    }

    @Test
    public void testCompilationErrorResult_FormattedMessage() {
        CodeExecutionResult result = CodeExecutionResult.compilationError(
                "Syntax error", 5);

        assertFalse("Should be failure", result.isSuccess());
        assertEquals("Error line should be 5", 5, result.getErrorLineNumber());
        assertNotNull("Formatted message should exist",
                result.getFormattedErrorMessage());
        assertTrue("Formatted message should mention line number",
                result.getFormattedErrorMessage().contains("5"));
    }

    @Test
    public void testRuntimeErrorResult_Summary() {
        CodeExecutionResult result = CodeExecutionResult.runtimeError(
                "NullPointerException", "Partial output");

        assertFalse("Should be failure", result.isSuccess());
        assertEquals("Output should be preserved", "Partial output",
                result.getOutput());
        String summary = result.getSummary();
        assertNotNull("Summary should exist", summary);
        assertTrue("Summary should mention error",
                summary.contains("Failed"));
    }

    @Test
    public void testTimeoutResult_Message() {
        CodeExecutionResult result = CodeExecutionResult.timeoutError(5000);

        assertFalse("Should be failure", result.isSuccess());
        assertEquals("Should be timeout error", "TIMEOUT_ERROR",
                result.getErrorType());
        assertTrue("Message should mention timeout",
                result.getErrorMessage().contains("5000"));
    }
}
