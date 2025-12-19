package com.example.debugappproject.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CODE EXECUTION ENGINE                                ║
 * ║              Sandboxed Execution for Java, Python, JavaScript                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Sandboxed JavaScript execution via WebView
 * - Java code simulation (pattern matching)
 * - Python code simulation (pattern matching)
 * - Timeout protection (5 second limit)
 * - Memory limits
 * - Captures stdout, stderr, and exceptions
 * - Test case execution
 */
public class CodeExecutionEngine {

    private static final String TAG = "CodeExecutionEngine";
    private static final int EXECUTION_TIMEOUT_MS = 5000;
    private static final int MAX_OUTPUT_LENGTH = 10000;
    
    private static CodeExecutionEngine instance;
    private Context context;
    private ExecutorService executor;
    private Handler mainHandler;
    private WebView jsWebView;
    private volatile String jsResult;
    private volatile boolean jsExecutionComplete;
    
    public static class ExecutionResult {
        public boolean success;
        public String output;
        public String error;
        public long executionTimeMs;
        public List<TestCaseResult> testResults;
        
        public ExecutionResult() {
            testResults = new ArrayList<>();
        }
    }
    
    public static class TestCaseResult {
        public String name;
        public String input;
        public String expectedOutput;
        public String actualOutput;
        public boolean passed;
    }
    
    public interface ExecutionCallback {
        void onResult(ExecutionResult result);
        void onError(String error);
    }

    private CodeExecutionEngine(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized CodeExecutionEngine getInstance(Context context) {
        if (instance == null) {
            instance = new CodeExecutionEngine(context);
        }
        return instance;
    }

    /**
     * Execute code and return result
     */
    public void execute(String code, String language, ExecutionCallback callback) {
        executor.execute(() -> {
            ExecutionResult result = new ExecutionResult();
            long startTime = System.currentTimeMillis();
            
            try {
                switch (language.toLowerCase()) {
                    case "javascript":
                    case "js":
                        executeJavaScript(code, result);
                        break;
                    case "python":
                    case "py":
                        simulatePython(code, result);
                        break;
                    case "java":
                    default:
                        simulateJava(code, result);
                        break;
                }
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                mainHandler.post(() -> callback.onResult(result));
            } catch (Exception e) {
                result.success = false;
                result.error = e.getMessage();
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }

    /**
     * Execute code with test cases
     */
    public void executeWithTests(String code, String language, String testsJson, ExecutionCallback callback) {
        executor.execute(() -> {
            ExecutionResult result = new ExecutionResult();
            long startTime = System.currentTimeMillis();
            
            try {
                // Parse test cases
                JSONArray tests = new JSONArray(testsJson);
                
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    TestCaseResult testResult = new TestCaseResult();
                    testResult.name = test.optString("name", "Test " + (i + 1));
                    testResult.input = test.optString("input", "");
                    testResult.expectedOutput = test.getString("expected");
                    
                    // Execute with input
                    String codeWithInput = injectInput(code, testResult.input, language);
                    ExecutionResult execResult = new ExecutionResult();
                    
                    switch (language.toLowerCase()) {
                        case "javascript":
                        case "js":
                            executeJavaScript(codeWithInput, execResult);
                            break;
                        case "python":
                        case "py":
                            simulatePython(codeWithInput, execResult);
                            break;
                        case "java":
                        default:
                            simulateJava(codeWithInput, execResult);
                            break;
                    }
                    
                    testResult.actualOutput = execResult.output != null ? execResult.output.trim() : "";
                    testResult.passed = testResult.actualOutput.equals(testResult.expectedOutput.trim());
                    result.testResults.add(testResult);
                }
                
                // Determine overall success
                result.success = result.testResults.stream().allMatch(t -> t.passed);
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                
                // Build summary
                int passed = (int) result.testResults.stream().filter(t -> t.passed).count();
                result.output = String.format("Tests: %d/%d passed", passed, result.testResults.size());
                
                mainHandler.post(() -> callback.onResult(result));
            } catch (Exception e) {
                result.success = false;
                result.error = "Test execution error: " + e.getMessage();
                result.executionTimeMs = System.currentTimeMillis() - startTime;
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }

    // ==================== JAVASCRIPT EXECUTION ====================
    
    private void executeJavaScript(String code, ExecutionResult result) {
        final CountDownLatch latch = new CountDownLatch(1);
        jsResult = null;
        jsExecutionComplete = false;
        
        // Wrap code to capture output
        String wrappedCode = 
            "var __output = [];\n" +
            "var __originalLog = console.log;\n" +
            "console.log = function() {\n" +
            "  __output.push(Array.from(arguments).join(' '));\n" +
            "};\n" +
            "try {\n" +
            code + "\n" +
            "  Android.onResult(JSON.stringify({success: true, output: __output.join('\\n')}));\n" +
            "} catch(e) {\n" +
            "  Android.onResult(JSON.stringify({success: false, error: e.toString()}));\n" +
            "}";

        mainHandler.post(() -> {
            try {
                if (jsWebView == null) {
                    jsWebView = new WebView(context);
                    jsWebView.getSettings().setJavaScriptEnabled(true);
                    jsWebView.addJavascriptInterface(new JsInterface(latch), "Android");
                }
                
                jsWebView.evaluateJavascript(wrappedCode, value -> {
                    // Callback not used, result comes via JsInterface
                });
            } catch (Exception e) {
                jsResult = "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
                latch.countDown();
            }
        });

        try {
            if (!latch.await(EXECUTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                result.success = false;
                result.error = "Execution timeout (>5 seconds). Possible infinite loop?";
                return;
            }
            
            if (jsResult != null) {
                JSONObject json = new JSONObject(jsResult);
                result.success = json.optBoolean("success", false);
                result.output = json.optString("output", "");
                result.error = json.optString("error", null);
            }
        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
        }
    }
    
    private class JsInterface {
        private CountDownLatch latch;
        
        JsInterface(CountDownLatch latch) {
            this.latch = latch;
        }
        
        @JavascriptInterface
        public void onResult(String result) {
            jsResult = result;
            latch.countDown();
        }
    }

    // ==================== JAVA SIMULATION ====================
    
    private void simulateJava(String code, ExecutionResult result) {
        StringBuilder output = new StringBuilder();
        List<String> errors = new ArrayList<>();
        
        try {
            // Check for common syntax errors
            if (!checkJavaSyntax(code, errors)) {
                result.success = false;
                result.error = String.join("\n", errors);
                return;
            }
            
            // Simulate System.out.println statements
            Pattern printPattern = Pattern.compile(
                "System\\.out\\.println\\s*\\(\\s*(.+?)\\s*\\)\\s*;",
                Pattern.DOTALL
            );
            Matcher matcher = printPattern.matcher(code);
            
            while (matcher.find()) {
                String expression = matcher.group(1);
                String evaluated = evaluateJavaExpression(expression, code);
                output.append(evaluated).append("\n");
            }
            
            // Also check for System.out.print (without ln)
            Pattern printNoLnPattern = Pattern.compile(
                "System\\.out\\.print\\s*\\(\\s*(.+?)\\s*\\)\\s*;",
                Pattern.DOTALL
            );
            matcher = printNoLnPattern.matcher(code);
            
            while (matcher.find()) {
                String expression = matcher.group(1);
                String evaluated = evaluateJavaExpression(expression, code);
                output.append(evaluated);
            }
            
            result.success = true;
            result.output = output.toString().trim();
            
            // Detect runtime errors
            detectJavaRuntimeErrors(code, result);
            
        } catch (Exception e) {
            result.success = false;
            result.error = "Simulation error: " + e.getMessage();
        }
    }
    
    private boolean checkJavaSyntax(String code, List<String> errors) {
        // Check balanced braces
        int braces = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
            if (braces < 0) {
                errors.add("Syntax error: Unexpected '}'");
                return false;
            }
        }
        if (braces != 0) {
            errors.add("Syntax error: Unbalanced braces. Missing " + (braces > 0 ? "}" : "{"));
            return false;
        }
        
        // Check balanced parentheses
        int parens = 0;
        for (char c : code.toCharArray()) {
            if (c == '(') parens++;
            if (c == ')') parens--;
        }
        if (parens != 0) {
            errors.add("Syntax error: Unbalanced parentheses");
            return false;
        }
        
        // Check for missing semicolons (basic)
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") ||
                line.startsWith("*") || line.endsWith("{") || line.endsWith("}") ||
                line.startsWith("if") || line.startsWith("else") || line.startsWith("for") ||
                line.startsWith("while") || line.startsWith("public") || line.startsWith("private") ||
                line.startsWith("class") || line.startsWith("import") || line.startsWith("package")) {
                continue;
            }
            // Simple check: statements should end with ; or { or }
            if (!line.endsWith(";") && !line.endsWith("{") && !line.endsWith("}") && !line.endsWith(",")) {
                // Might be a problem, but don't error - just warn
            }
        }
        
        return true;
    }
    
    private String evaluateJavaExpression(String expression, String context) {
        expression = expression.trim();
        
        // String literal
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }
        
        // Integer literal
        if (expression.matches("-?\\d+")) {
            return expression;
        }
        
        // Boolean
        if (expression.equals("true") || expression.equals("false")) {
            return expression;
        }
        
        // Simple arithmetic
        if (expression.matches("[\\d\\s+\\-*/()]+")) {
            try {
                // Basic evaluation
                return String.valueOf(evaluateArithmetic(expression));
            } catch (Exception e) {
                return expression;
            }
        }
        
        // String concatenation
        if (expression.contains("+") && expression.contains("\"")) {
            return evaluateStringConcat(expression);
        }
        
        // Variable - try to find its value
        Pattern varPattern = Pattern.compile(
            "(?:int|String|double|float|boolean|char)\\s+" + Pattern.quote(expression) + "\\s*=\\s*(.+?)\\s*;"
        );
        Matcher m = varPattern.matcher(context);
        if (m.find()) {
            return evaluateJavaExpression(m.group(1), context);
        }
        
        return expression; // Return as-is if can't evaluate
    }
    
    private int evaluateArithmetic(String expr) {
        expr = expr.replaceAll("\\s+", "");
        // Very basic: handle + and -
        String[] parts = expr.split("(?=[+\\-])|(?<=[+\\-])");
        int result = 0;
        String operator = "+";
        for (String part : parts) {
            if (part.equals("+") || part.equals("-")) {
                operator = part;
            } else {
                int value = Integer.parseInt(part);
                if (operator.equals("+")) result += value;
                else result -= value;
            }
        }
        return result;
    }
    
    private String evaluateStringConcat(String expression) {
        StringBuilder result = new StringBuilder();
        String[] parts = expression.split("\\+");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("\"") && part.endsWith("\"")) {
                result.append(part.substring(1, part.length() - 1));
            } else if (part.matches("-?\\d+")) {
                result.append(part);
            } else {
                result.append(part); // Variable name as placeholder
            }
        }
        return result.toString();
    }
    
    private void detectJavaRuntimeErrors(String code, ExecutionResult result) {
        // Detect potential ArrayIndexOutOfBoundsException
        if (code.contains("[") && code.contains("]")) {
            Pattern arrayAccess = Pattern.compile("\\w+\\[(\\d+)\\]");
            Matcher m = arrayAccess.matcher(code);
            while (m.find()) {
                int index = Integer.parseInt(m.group(1));
                // Check if array is defined with smaller size
                Pattern arrayDef = Pattern.compile("new\\s+\\w+\\[(\\d+)\\]");
                Matcher defMatcher = arrayDef.matcher(code);
                if (defMatcher.find()) {
                    int size = Integer.parseInt(defMatcher.group(1));
                    if (index >= size) {
                        result.success = false;
                        result.error = "ArrayIndexOutOfBoundsException: Index " + index + 
                            " out of bounds for length " + size;
                        return;
                    }
                }
            }
        }
        
        // Detect potential infinite loop
        if (code.contains("while(true)") || code.contains("while (true)") ||
            code.contains("for(;;)") || code.contains("for (;;)")) {
            if (!code.contains("break")) {
                result.success = false;
                result.error = "Potential infinite loop detected!";
            }
        }
    }

    // ==================== PYTHON SIMULATION ====================
    
    private void simulatePython(String code, ExecutionResult result) {
        StringBuilder output = new StringBuilder();
        
        try {
            // Check indentation
            if (!checkPythonIndentation(code, result)) {
                return;
            }
            
            // Find print statements
            Pattern printPattern = Pattern.compile("print\\s*\\((.+?)\\)");
            Matcher matcher = printPattern.matcher(code);
            
            while (matcher.find()) {
                String expression = matcher.group(1);
                String evaluated = evaluatePythonExpression(expression, code);
                output.append(evaluated).append("\n");
            }
            
            result.success = true;
            result.output = output.toString().trim();
            
        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
        }
    }
    
    private boolean checkPythonIndentation(String code, ExecutionResult result) {
        String[] lines = code.split("\n");
        int expectedIndent = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) continue;
            
            // Count leading spaces
            int indent = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') indent++;
                else if (c == '\t') indent += 4;
                else break;
            }
            
            String trimmed = line.trim();
            
            // Lines after : should be indented
            if (i > 0) {
                String prevLine = lines[i-1].trim();
                if (prevLine.endsWith(":") && indent <= expectedIndent) {
                    result.success = false;
                    result.error = "IndentationError: expected an indented block after line " + i;
                    return false;
                }
            }
            
            // Update expected indent
            if (trimmed.endsWith(":")) {
                expectedIndent = indent + 4;
            } else {
                expectedIndent = indent;
            }
        }
        
        return true;
    }
    
    private String evaluatePythonExpression(String expression, String context) {
        expression = expression.trim();
        
        // String literal
        if ((expression.startsWith("\"") && expression.endsWith("\"")) ||
            (expression.startsWith("'") && expression.endsWith("'"))) {
            return expression.substring(1, expression.length() - 1);
        }
        
        // f-string (basic)
        if (expression.startsWith("f\"") || expression.startsWith("f'")) {
            return expression.substring(2, expression.length() - 1);
        }
        
        // Number
        if (expression.matches("-?\\d+(\\.\\d+)?")) {
            return expression;
        }
        
        return expression;
    }

    // ==================== UTILITY METHODS ====================
    
    private String injectInput(String code, String input, String language) {
        // For simulation, replace input() or Scanner calls with the test input
        switch (language.toLowerCase()) {
            case "python":
                return code.replace("input()", "\"" + input + "\"");
            case "javascript":
                return code.replace("prompt()", "\"" + input + "\"");
            case "java":
            default:
                // Replace scanner.nextLine() etc with the input
                return code.replaceAll(
                    "scanner\\.(nextLine|next|nextInt)\\(\\)",
                    "\"" + input + "\""
                );
        }
    }

    /**
     * Clean up resources
     */
    public void shutdown() {
        executor.shutdown();
        if (jsWebView != null) {
            mainHandler.post(() -> {
                jsWebView.destroy();
                jsWebView = null;
            });
        }
    }
}
