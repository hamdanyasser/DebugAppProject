package com.example.debugappproject.debugger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - IN-APP DEBUGGER ENGINE                               ║
 * ║         Breakpoints, Step-Through, Variable Inspection                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class CodeDebugger {

    private static final String TAG = "CodeDebugger";
    
    private String[] codeLines;
    private int currentLine = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    private List<Integer> breakpoints = new ArrayList<>();
    private Map<String, VariableInfo> variables = new LinkedHashMap<>();
    private List<ExecutionStep> executionHistory = new ArrayList<>();
    private List<StackFrame> callStack = new ArrayList<>();
    private StringBuilder outputBuffer = new StringBuilder();
    
    private DebuggerListener listener;
    
    public interface DebuggerListener {
        void onLineExecuted(int lineNumber, String line);
        void onBreakpointHit(int lineNumber);
        void onVariableChanged(String name, VariableInfo info);
        void onOutput(String output);
        void onError(String error, int lineNumber);
        void onExecutionComplete();
        void onCallStackUpdated(List<StackFrame> stack);
    }
    
    public static class VariableInfo {
        public String name;
        public String type;
        public String value;
        public int declaredLine;
        public int lastModifiedLine;
        public List<String> history = new ArrayList<>();
        
        public VariableInfo(String name, String type, String value, int line) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.declaredLine = line;
            this.lastModifiedLine = line;
            this.history.add(value);
        }
        
        public void update(String newValue, int line) {
            if (!newValue.equals(this.value)) {
                this.value = newValue;
                this.lastModifiedLine = line;
                this.history.add(newValue);
            }
        }
    }
    
    public static class ExecutionStep {
        public int lineNumber;
        public String line;
        public Map<String, String> variableSnapshot;
        public long timestamp;
        
        public ExecutionStep(int lineNumber, String line, Map<String, String> vars) {
            this.lineNumber = lineNumber;
            this.line = line;
            this.variableSnapshot = new HashMap<>(vars);
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public static class StackFrame {
        public String methodName;
        public int lineNumber;
        public Map<String, String> localVariables;
        
        public StackFrame(String methodName, int lineNumber) {
            this.methodName = methodName;
            this.lineNumber = lineNumber;
            this.localVariables = new HashMap<>();
        }
    }
    
    public CodeDebugger() {
        callStack.add(new StackFrame("main", 0));
    }
    
    public void setListener(DebuggerListener listener) {
        this.listener = listener;
    }
    
    public void loadCode(String code) {
        this.codeLines = code.split("\n");
        this.currentLine = 0;
        this.variables.clear();
        this.executionHistory.clear();
        this.outputBuffer = new StringBuilder();
        this.isRunning = false;
        this.isPaused = false;
        callStack.clear();
        callStack.add(new StackFrame("main", 0));
    }
    
    public void toggleBreakpoint(int lineNumber) {
        if (breakpoints.contains(lineNumber)) {
            breakpoints.remove(Integer.valueOf(lineNumber));
        } else {
            breakpoints.add(lineNumber);
        }
    }
    
    public boolean hasBreakpoint(int lineNumber) {
        return breakpoints.contains(lineNumber);
    }
    
    public List<Integer> getBreakpoints() {
        return new ArrayList<>(breakpoints);
    }
    
    public void clearBreakpoints() {
        breakpoints.clear();
    }
    
    public void stepOver() {
        if (codeLines == null || currentLine >= codeLines.length) {
            if (listener != null) listener.onExecutionComplete();
            return;
        }
        
        executeLine(currentLine);
        currentLine++;
        
        if (currentLine < codeLines.length && breakpoints.contains(currentLine)) {
            isPaused = true;
            if (listener != null) listener.onBreakpointHit(currentLine);
        }
    }
    
    public void stepInto() {
        stepOver();
    }
    
    public void stepOut() {
        int braceCount = 0;
        while (currentLine < codeLines.length) {
            String line = codeLines[currentLine];
            if (line.contains("{")) braceCount++;
            if (line.contains("}")) {
                braceCount--;
                if (braceCount < 0) {
                    stepOver();
                    break;
                }
            }
            stepOver();
        }
    }
    
    public void continueExecution() {
        isPaused = false;
        isRunning = true;
        
        while (currentLine < codeLines.length && isRunning) {
            if (breakpoints.contains(currentLine)) {
                isPaused = true;
                if (listener != null) listener.onBreakpointHit(currentLine);
                break;
            }
            executeLine(currentLine);
            currentLine++;
        }
        
        if (currentLine >= codeLines.length) {
            isRunning = false;
            if (listener != null) listener.onExecutionComplete();
        }
    }
    
    public void runToLine(int targetLine) {
        while (currentLine < targetLine && currentLine < codeLines.length) {
            executeLine(currentLine);
            currentLine++;
        }
    }
    
    public void stop() {
        isRunning = false;
        isPaused = false;
    }
    
    public void restart() {
        currentLine = 0;
        variables.clear();
        executionHistory.clear();
        outputBuffer = new StringBuilder();
        callStack.clear();
        callStack.add(new StackFrame("main", 0));
    }
    
    private void executeLine(int lineNum) {
        if (lineNum >= codeLines.length) return;
        
        String line = codeLines[lineNum].trim();
        
        if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
            if (listener != null) listener.onLineExecuted(lineNum, line);
            return;
        }
        
        try {
            parseAndExecute(line, lineNum);
            
            Map<String, String> snapshot = new HashMap<>();
            for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                snapshot.put(entry.getKey(), entry.getValue().value);
            }
            executionHistory.add(new ExecutionStep(lineNum, line, snapshot));
            
            if (listener != null) {
                listener.onLineExecuted(lineNum, line);
                listener.onCallStackUpdated(callStack);
            }
            
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(e.getMessage(), lineNum);
            }
        }
    }
    
    private void parseAndExecute(String line, int lineNum) {
        // Variable declaration with initialization
        Pattern varDeclPattern = Pattern.compile("(int|double|float|long|String|boolean|char)\\s+(\\w+)\\s*=\\s*(.+);");
        Matcher varDeclMatcher = varDeclPattern.matcher(line);
        
        if (varDeclMatcher.find()) {
            String type = varDeclMatcher.group(1);
            String name = varDeclMatcher.group(2);
            String valueExpr = varDeclMatcher.group(3);
            String value = evaluateExpression(valueExpr);
            
            VariableInfo info = new VariableInfo(name, type, value, lineNum);
            variables.put(name, info);
            
            if (listener != null) listener.onVariableChanged(name, info);
            return;
        }
        
        // Variable declaration without initialization
        Pattern varDeclOnlyPattern = Pattern.compile("(int|double|float|long|String|boolean|char)\\s+(\\w+)\\s*;");
        Matcher varDeclOnlyMatcher = varDeclOnlyPattern.matcher(line);
        
        if (varDeclOnlyMatcher.find()) {
            String type = varDeclOnlyMatcher.group(1);
            String name = varDeclOnlyMatcher.group(2);
            String defaultValue = getDefaultValue(type);
            
            VariableInfo info = new VariableInfo(name, type, defaultValue, lineNum);
            variables.put(name, info);
            
            if (listener != null) listener.onVariableChanged(name, info);
            return;
        }
        
        // Assignment
        Pattern assignPattern = Pattern.compile("(\\w+)\\s*=\\s*(.+);");
        Matcher assignMatcher = assignPattern.matcher(line);
        
        if (assignMatcher.find()) {
            String name = assignMatcher.group(1);
            String valueExpr = assignMatcher.group(2);
            
            if (variables.containsKey(name)) {
                String value = evaluateExpression(valueExpr);
                VariableInfo info = variables.get(name);
                info.update(value, lineNum);
                
                if (listener != null) listener.onVariableChanged(name, info);
            }
            return;
        }
        
        // Increment/Decrement
        if (line.matches(".*\\+\\+.*") || line.matches(".*--.*")) {
            for (Map.Entry<String, VariableInfo> entry : variables.entrySet()) {
                if (line.contains(entry.getKey() + "++") || line.contains("++" + entry.getKey())) {
                    try {
                        int val = Integer.parseInt(entry.getValue().value);
                        entry.getValue().update(String.valueOf(val + 1), lineNum);
                        if (listener != null) listener.onVariableChanged(entry.getKey(), entry.getValue());
                    } catch (NumberFormatException e) {}
                }
                if (line.contains(entry.getKey() + "--") || line.contains("--" + entry.getKey())) {
                    try {
                        int val = Integer.parseInt(entry.getValue().value);
                        entry.getValue().update(String.valueOf(val - 1), lineNum);
                        if (listener != null) listener.onVariableChanged(entry.getKey(), entry.getValue());
                    } catch (NumberFormatException e) {}
                }
            }
            return;
        }
        
        // System.out.println
        Pattern printPattern = Pattern.compile("System\\.out\\.println\\((.*)\\);");
        Matcher printMatcher = printPattern.matcher(line);
        
        if (printMatcher.find()) {
            String expr = printMatcher.group(1);
            String output = evaluateExpression(expr);
            outputBuffer.append(output).append("\n");
            
            if (listener != null) listener.onOutput(output);
            return;
        }
        
        // Method call detection
        Pattern methodCallPattern = Pattern.compile("(\\w+)\\s*\\(.*\\)\\s*;");
        Matcher methodCallMatcher = methodCallPattern.matcher(line);
        
        if (methodCallMatcher.find() && !line.contains("System.out")) {
            String methodName = methodCallMatcher.group(1);
            callStack.add(new StackFrame(methodName, lineNum));
        }
        
        // Return statement
        if (line.startsWith("return")) {
            if (callStack.size() > 1) {
                callStack.remove(callStack.size() - 1);
            }
        }
    }
    
    private String evaluateExpression(String expr) {
        expr = expr.trim();
        
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length() - 1);
        }
        
        if (expr.matches("-?\\d+(\\.\\d+)?")) {
            return expr;
        }
        
        if (expr.equals("true") || expr.equals("false")) {
            return expr;
        }
        
        if (variables.containsKey(expr)) {
            return variables.get(expr).value;
        }
        
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            StringBuilder result = new StringBuilder();
            boolean allNumeric = true;
            for (String part : parts) {
                String val = evaluateExpression(part.trim());
                if (!val.matches("-?\\d+(\\.\\d+)?")) {
                    allNumeric = false;
                }
                result.append(val);
            }
            if (allNumeric) {
                try {
                    double sum = 0;
                    for (String part : parts) {
                        sum += Double.parseDouble(evaluateExpression(part.trim()));
                    }
                    return String.valueOf((int) sum);
                } catch (Exception e) {}
            }
            return result.toString();
        }
        
        return expr;
    }
    
    private String getDefaultValue(String type) {
        switch (type) {
            case "int":
            case "long":
                return "0";
            case "double":
            case "float":
                return "0.0";
            case "boolean":
                return "false";
            case "char":
                return "''";
            case "String":
                return "null";
            default:
                return "null";
        }
    }
    
    // Getters
    public int getCurrentLine() { return currentLine; }
    public boolean isRunning() { return isRunning; }
    public boolean isPaused() { return isPaused; }
    public Map<String, VariableInfo> getVariables() { return new LinkedHashMap<>(variables); }
    public List<ExecutionStep> getExecutionHistory() { return new ArrayList<>(executionHistory); }
    public List<StackFrame> getCallStack() { return new ArrayList<>(callStack); }
    public String getOutput() { return outputBuffer.toString(); }
    public String[] getCodeLines() { return codeLines; }
}
