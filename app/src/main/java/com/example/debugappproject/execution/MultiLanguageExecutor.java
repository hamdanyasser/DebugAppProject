package com.example.debugappproject.execution;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multi-Language Code Executor - Java, Python, JavaScript
 */
public class MultiLanguageExecutor {

    private final Context context;
    private final Handler mainHandler;
    private WebView jsExecutor;
    
    public interface ExecutionCallback {
        void onOutput(String output);
        void onError(String error);
        void onComplete();
    }
    
    public enum Language {
        JAVA("Java", "java"),
        PYTHON("Python", "py"),
        JAVASCRIPT("JavaScript", "js");
        
        public final String displayName;
        public final String extension;
        
        Language(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }
        
        public static Language fromString(String lang) {
            if (lang == null) return JAVA;
            switch (lang.toLowerCase().trim()) {
                case "python": case "py": return PYTHON;
                case "javascript": case "js": return JAVASCRIPT;
                default: return JAVA;
            }
        }
    }
    
    public MultiLanguageExecutor(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void execute(String code, Language language, ExecutionCallback callback) {
        switch (language) {
            case JAVASCRIPT: executeJavaScript(code, callback); break;
            case PYTHON: executePython(code, callback); break;
            default: executeJava(code, callback); break;
        }
    }
    
    private void executeJavaScript(String code, ExecutionCallback callback) {
        mainHandler.post(() -> {
            if (jsExecutor == null) {
                jsExecutor = new WebView(context);
                jsExecutor.getSettings().setJavaScriptEnabled(true);
            }
            String wrappedCode = "var __output='';" +
                "var console={log:function(){__output+=Array.prototype.slice.call(arguments).join(' ')+'\\n';}};" +
                "try{" + code + "}catch(e){__output='Error: '+e.message;}" +
                "__output;";
            jsExecutor.evaluateJavascript(wrappedCode, result -> {
                if (result != null) {
                    String output = result.replace("\\n", "\n").replace("\\\"", "\"");
                    if (output.startsWith("\"")) output = output.substring(1);
                    if (output.endsWith("\"")) output = output.substring(0, output.length()-1);
                    callback.onOutput(output);
                }
                callback.onComplete();
            });
        });
    }
    
    private void executePython(String code, ExecutionCallback callback) {
        new Thread(() -> {
            StringBuilder output = new StringBuilder();
            Map<String, String> vars = new HashMap<>();
            for (String line : code.split("\n")) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("print(") && line.endsWith(")")) {
                    String content = line.substring(6, line.length() - 1);
                    output.append(evalPython(content, vars)).append("\n");
                } else if (line.contains("=") && !line.contains("==")) {
                    String[] parts = line.split("=", 2);
                    vars.put(parts[0].trim(), evalPython(parts[1].trim(), vars));
                }
            }
            mainHandler.post(() -> { callback.onOutput(output.toString()); callback.onComplete(); });
        }).start();
    }
    
    private void executeJava(String code, ExecutionCallback callback) {
        new Thread(() -> {
            StringBuilder output = new StringBuilder();
            Map<String, String> vars = new HashMap<>();
            for (String line : code.split("\n")) {
                line = line.trim();
                if (line.contains("System.out.println(")) {
                    int s = line.indexOf("System.out.println(") + 19;
                    int e = line.lastIndexOf(")");
                    if (s < e) output.append(evalJava(line.substring(s, e), vars)).append("\n");
                } else if (line.matches("(int|String|double|boolean)\\s+\\w+\\s*=.*")) {
                    Pattern p = Pattern.compile("(int|String|double|boolean)\\s+(\\w+)\\s*=\\s*(.+);");
                    Matcher m = p.matcher(line);
                    if (m.find()) vars.put(m.group(2), evalJava(m.group(3), vars));
                }
            }
            mainHandler.post(() -> { callback.onOutput(output.toString()); callback.onComplete(); });
        }).start();
    }
    
    private String evalPython(String expr, Map<String, String> vars) {
        expr = expr.trim();
        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'")))
            return expr.substring(1, expr.length() - 1);
        if (vars.containsKey(expr)) return vars.get(expr);
        if (expr.contains("+")) {
            StringBuilder sb = new StringBuilder();
            for (String p : expr.split("\\+")) sb.append(evalPython(p.trim(), vars));
            return sb.toString();
        }
        return expr;
    }
    
    private String evalJava(String expr, Map<String, String> vars) {
        expr = expr.trim();
        if (expr.startsWith("\"") && expr.endsWith("\"")) return expr.substring(1, expr.length() - 1);
        if (vars.containsKey(expr)) return vars.get(expr);
        if (expr.contains("+")) {
            StringBuilder sb = new StringBuilder();
            for (String p : expr.split("\\+")) sb.append(evalJava(p.trim(), vars));
            return sb.toString();
        }
        return expr;
    }
    
    public void destroy() {
        if (jsExecutor != null) { jsExecutor.destroy(); jsExecutor = null; }
    }
}
