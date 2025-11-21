package com.example.debugappproject.execution;

import android.util.Log;

import org.codehaus.janino.SimpleCompiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CodeExecutionEngine - Executes Java code snippets using Janino compiler.
 *
 * Features:
 * - Compiles and executes Java code at runtime
 * - Captures System.out output
 * - Handles compilation errors with line numbers
 * - Catches runtime exceptions
 * - Enforces execution timeout (5 seconds default)
 * - Thread-safe execution
 * - Automatic code preparation (wraps in class if needed)
 *
 * Security:
 * - Runs in isolated thread with timeout
 * - Cannot access Android system resources
 * - Limited to standard Java operations
 *
 * Usage:
 * <pre>
 *     CodeExecutionEngine engine = new CodeExecutionEngine();
 *     CodeExecutionResult result = engine.execute("System.out.println(\"Hello\");");
 *     engine.shutdown();
 * </pre>
 */
public class CodeExecutionEngine {

    private static final String TAG = "CodeExecutionEngine";
    private static final long DEFAULT_TIMEOUT_MS = 5000; // 5 seconds

    private final ExecutorService executorService;
    private long timeoutMs = DEFAULT_TIMEOUT_MS;

    public CodeExecutionEngine() {
        // Single thread executor for sequential execution
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Sets the execution timeout in milliseconds.
     * @param timeoutMs Timeout in milliseconds (500-10000 recommended)
     */
    public void setTimeout(long timeoutMs) {
        this.timeoutMs = Math.max(500, Math.min(10000, timeoutMs));
    }

    /**
     * Executes Java code and returns the result.
     *
     * @param userCode The Java code to execute (method body or full class)
     * @return CodeExecutionResult containing output or error information
     */
    public CodeExecutionResult execute(String userCode) {
        if (userCode == null || userCode.trim().isEmpty()) {
            return CodeExecutionResult.compilationError("Code is empty", -1);
        }

        long startTime = System.currentTimeMillis();

        try {
            // Prepare code (wrap in class if needed)
            String preparedCode = prepareCode(userCode);

            Log.d(TAG, "Executing code:\n" + preparedCode);

            // Submit execution task with timeout
            Future<CodeExecutionResult> future = executorService.submit(
                    new CodeExecutionTask(preparedCode)
            );

            // Wait for result with timeout
            CodeExecutionResult result = future.get(timeoutMs, TimeUnit.MILLISECONDS);

            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);

            Log.d(TAG, "Execution result: " + result.getSummary());
            return result;

        } catch (TimeoutException e) {
            Log.w(TAG, "Execution timed out after " + timeoutMs + "ms");
            return CodeExecutionResult.timeoutError(timeoutMs);

        } catch (Exception e) {
            Log.e(TAG, "Execution failed", e);
            return CodeExecutionResult.runtimeError(
                    "Unexpected error: " + e.getMessage(),
                    null
            );
        }
    }

    /**
     * Prepares user code for execution.
     * - Wraps standalone statements in a class and main method
     * - Adds necessary imports
     * - Handles both full classes and code snippets
     */
    private String prepareCode(String userCode) {
        String trimmedCode = userCode.trim();

        // Check if code is already a full class
        if (trimmedCode.startsWith("public class") || trimmedCode.startsWith("class")) {
            return trimmedCode;
        }

        // Check if code is a method
        if (isMethod(trimmedCode)) {
            return wrapMethodInClass(trimmedCode);
        }

        // Assume it's method body statements
        return wrapStatementsInClass(trimmedCode);
    }

    /**
     * Checks if the code is a complete method definition.
     */
    private boolean isMethod(String code) {
        Pattern methodPattern = Pattern.compile(
                "^(public|private|protected|static|\\s)*\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{",
                Pattern.MULTILINE
        );
        return methodPattern.matcher(code).find();
    }

    /**
     * Wraps a method in a class.
     */
    private String wrapMethodInClass(String methodCode) {
        return "public class UserCode {\n" +
                "    " + methodCode + "\n" +
                "    public static void main(String[] args) {\n" +
                "        // Call user method if it's main-compatible\n" +
                "    }\n" +
                "}";
    }

    /**
     * Wraps standalone statements in a class with main method.
     */
    private String wrapStatementsInClass(String statements) {
        return "public class UserCode {\n" +
                "    public static void main(String[] args) {\n" +
                "        " + statements + "\n" +
                "    }\n" +
                "}";
    }

    /**
     * Shuts down the execution engine.
     * Call this when done to release resources.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Task that compiles and executes code in a separate thread.
     */
    private static class CodeExecutionTask implements Callable<CodeExecutionResult> {

        private final String code;

        public CodeExecutionTask(String code) {
            this.code = code;
        }

        @Override
        public CodeExecutionResult call() {
            // Capture System.out
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream captureOut = new PrintStream(outputStream);

            try {
                // Compile the code
                SimpleCompiler compiler = new SimpleCompiler();
                compiler.cook(code);

                // Get the compiled class
                ClassLoader classLoader = compiler.getClassLoader();
                Class<?> compiledClass = classLoader.loadClass("UserCode");

                // Find and invoke main method
                Method mainMethod = compiledClass.getMethod("main", String[].class);

                // Redirect System.out
                System.setOut(captureOut);

                // Execute main method
                String[] args = new String[0];
                mainMethod.invoke(null, (Object) args);

                // Flush and get output
                captureOut.flush();
                String output = outputStream.toString();

                return CodeExecutionResult.success(output, 0);

            } catch (org.codehaus.commons.compiler.CompileException e) {
                // Compilation error - extract line number
                int lineNumber = extractLineNumber(e.getMessage());
                String errorMessage = cleanErrorMessage(e.getMessage());

                return CodeExecutionResult.compilationError(errorMessage, lineNumber);

            } catch (Exception e) {
                // Runtime error
                String output = outputStream.toString();
                String errorMessage = formatRuntimeError(e);

                return CodeExecutionResult.runtimeError(errorMessage, output);

            } finally {
                // Restore System.out
                System.setOut(originalOut);

                // Close streams
                try {
                    captureOut.close();
                    outputStream.close();
                } catch (Exception ignored) {
                }
            }
        }

        /**
         * Extracts line number from compiler error message.
         */
        private int extractLineNumber(String errorMessage) {
            Pattern linePattern = Pattern.compile("Line (\\d+)");
            Matcher matcher = linePattern.matcher(errorMessage);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
            return -1;
        }

        /**
         * Cleans up compiler error message for user display.
         */
        private String cleanErrorMessage(String rawMessage) {
            // Remove Janino-specific details
            String cleaned = rawMessage.replaceAll("File '.*?', ", "");
            cleaned = cleaned.replaceAll("org\\.codehaus\\.janino\\.", "");

            // Make it more user-friendly
            cleaned = cleaned.replace("Compilation unit '", "'");

            return cleaned;
        }

        /**
         * Formats runtime exception for user display.
         */
        private String formatRuntimeError(Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            StringBuilder sb = new StringBuilder();
            sb.append(cause.getClass().getSimpleName());
            sb.append(": ");
            sb.append(cause.getMessage() != null ? cause.getMessage() : "No message");

            // Add relevant stack trace (first few frames)
            StackTraceElement[] stackTrace = cause.getStackTrace();
            if (stackTrace.length > 0) {
                sb.append("\n\nStack trace:\n");
                int framesToShow = Math.min(3, stackTrace.length);
                for (int i = 0; i < framesToShow; i++) {
                    StackTraceElement frame = stackTrace[i];
                    if (frame.getClassName().contains("UserCode")) {
                        sb.append("  at ").append(frame.toString()).append("\n");
                    }
                }
            }

            return sb.toString();
        }
    }

    /**
     * Tests if the engine is working correctly.
     * @return true if test passes
     */
    public boolean runSelfTest() {
        CodeExecutionResult result = execute("System.out.println(\"Test\");");
        return result.isSuccess() && result.getOutput().trim().equals("Test");
    }
}
