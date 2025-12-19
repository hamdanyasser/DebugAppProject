package com.example.debugappproject.debugger;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║             🔍 INTERACTIVE DEBUGGER - STEP THROUGH CODE GAME                ║
 * ║                   Execute code step-by-step, watch variables!               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class DebuggerFragment extends Fragment {

    private SoundManager soundManager;
    private Handler handler;
    private Random random;
    
    // Views
    private View rootView;
    private LinearLayout codeContainer, variablesContainer, outputContainer;
    private ScrollView codeScroll, outputScroll;
    private TextView textChallenge, textXp, textStep, textOutput;
    private MaterialButton btnStepOver, btnStepInto, btnContinue, btnFindBug;
    private CardView cardVariables;
    
    // Game state
    private List<DebugChallenge> challenges;
    private DebugChallenge currentChallenge;
    private int currentLine = 0;
    private int totalXp = 0;
    private int challengeIndex = 0;
    private Map<String, String> variables;
    private List<String> consoleOutput;
    private StringBuilder fullOutput;
    private boolean bugFound = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debugger_game, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rootView = view;
        soundManager = SoundManager.getInstance(requireContext());
        handler = new Handler(Looper.getMainLooper());
        random = new Random();
        variables = new HashMap<>();
        consoleOutput = new ArrayList<>();
        fullOutput = new StringBuilder();
        
        initChallenges();
        findViews();
        setupUI();
        loadChallenge();
        
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void findViews() {
        codeContainer = rootView.findViewById(R.id.container_code);
        codeScroll = rootView.findViewById(R.id.scroll_code);
        variablesContainer = rootView.findViewById(R.id.container_variables);
        outputContainer = rootView.findViewById(R.id.container_output);
        outputScroll = rootView.findViewById(R.id.scroll_output);
        textChallenge = rootView.findViewById(R.id.text_challenge);
        textXp = rootView.findViewById(R.id.text_xp);
        textStep = rootView.findViewById(R.id.text_step);
        textOutput = rootView.findViewById(R.id.text_output);
        cardVariables = rootView.findViewById(R.id.card_variables);
        btnStepOver = rootView.findViewById(R.id.btn_step_over);
        btnStepInto = rootView.findViewById(R.id.btn_step_into);
        btnContinue = rootView.findViewById(R.id.btn_continue);
        btnFindBug = rootView.findViewById(R.id.btn_find_bug);
        
        View backBtn = rootView.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showExitConfirmation();
            });
        }
    }
    
    private void setupUI() {
        if (btnStepOver != null) {
            btnStepOver.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.TICK);
                stepOver();
            });
        }
        
        if (btnStepInto != null) {
            btnStepInto.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.TICK);
                stepInto();
            });
        }
        
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                runToEnd();
            });
        }
        
        if (btnFindBug != null) {
            btnFindBug.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showBugOptions();
            });
        }
        
        updateStats();
    }
    
    private void loadChallenge() {
        if (challengeIndex >= challenges.size()) {
            showCompletion();
            return;
        }
        
        currentChallenge = challenges.get(challengeIndex);
        currentLine = 0;
        bugFound = false;
        variables.clear();
        consoleOutput.clear();
        fullOutput = new StringBuilder();
        
        if (textChallenge != null) {
            textChallenge.setText("🔍 " + currentChallenge.title);
        }
        
        displayCode();
        updateVariables();
        updateOutput();
        updateStats();
        
        // Initial hint
        Toast.makeText(getContext(), "Step through the code to find the bug!", Toast.LENGTH_LONG).show();
    }
    
    private void displayCode() {
        if (codeContainer == null || currentChallenge == null) return;
        codeContainer.removeAllViews();
        
        String[] lines = currentChallenge.code.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            final int lineNum = i;
            
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
            
            // Line number
            TextView numView = new TextView(requireContext());
            numView.setText(String.format("%2d", i + 1));
            numView.setTextColor(Color.parseColor("#64748B"));
            numView.setTypeface(Typeface.MONOSPACE);
            numView.setTextSize(12);
            numView.setMinWidth(dpToPx(28));
            
            // Breakpoint indicator
            TextView indicator = new TextView(requireContext());
            indicator.setText(currentChallenge.bugLine == i ? "🔴" : "  ");
            indicator.setTextSize(10);
            indicator.setPadding(0, 0, dpToPx(4), 0);
            indicator.setVisibility(View.INVISIBLE); // Hidden until found
            
            // Current line indicator
            TextView arrow = new TextView(requireContext());
            arrow.setText(currentLine == i ? "▶" : " ");
            arrow.setTextColor(Color.parseColor("#22C55E"));
            arrow.setTextSize(12);
            arrow.setPadding(0, 0, dpToPx(4), 0);
            
            // Code text
            TextView codeView = new TextView(requireContext());
            SpannableStringBuilder builder = new SpannableStringBuilder(lines[i]);
            highlightSyntax(builder, lines[i]);
            codeView.setText(builder);
            codeView.setTypeface(Typeface.MONOSPACE);
            codeView.setTextSize(12);
            
            // Highlight current line
            if (currentLine == i) {
                row.setBackgroundColor(Color.parseColor("#1E3A5F"));
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
            }
            
            // Click to set as bug location
            row.setOnClickListener(v -> selectBugLine(lineNum));
            
            row.addView(indicator);
            row.addView(arrow);
            row.addView(numView);
            row.addView(codeView);
            codeContainer.addView(row);
        }
    }
    
    private void highlightSyntax(SpannableStringBuilder builder, String line) {
        String[] keywords = {"int", "String", "for", "while", "if", "else", "return", "public", "private", "void", "class", "new", "true", "false", "null"};
        
        for (String kw : keywords) {
            int idx = 0;
            while ((idx = line.indexOf(kw, idx)) != -1) {
                boolean validStart = idx == 0 || !Character.isLetterOrDigit(line.charAt(idx - 1));
                boolean validEnd = idx + kw.length() >= line.length() || !Character.isLetterOrDigit(line.charAt(idx + kw.length()));
                
                if (validStart && validEnd) {
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#C084FC")),
                            idx, idx + kw.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                idx += kw.length();
            }
        }
        
        // Comments
        int commentIdx = line.indexOf("//");
        if (commentIdx != -1) {
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#6B7280")),
                    commentIdx, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void stepOver() {
        if (currentChallenge == null) return;
        
        String[] lines = currentChallenge.code.split("\n");
        if (currentLine >= lines.length - 1) {
            Toast.makeText(getContext(), "End of code reached!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Execute current line
        executeLine(lines[currentLine]);
        
        currentLine++;
        displayCode();
        updateVariables();
        updateOutput();
        
        // Scroll to current line
        scrollToLine(currentLine);
        
        if (textStep != null) {
            textStep.setText("Line " + (currentLine + 1));
        }
    }
    
    private void stepInto() {
        // Same as step over for now (could be expanded for function calls)
        stepOver();
    }
    
    private void runToEnd() {
        if (currentChallenge == null) return;
        
        String[] lines = currentChallenge.code.split("\n");
        
        while (currentLine < lines.length) {
            executeLine(lines[currentLine]);
            currentLine++;
        }
        
        currentLine = lines.length - 1;
        displayCode();
        updateVariables();
        updateOutput();
        
        Toast.makeText(getContext(), "Execution complete. Now find the bug!", Toast.LENGTH_LONG).show();
    }
    
    private void executeLine(String line) {
        line = line.trim();
        
        // Variable declarations
        if (line.startsWith("int ") && line.contains("=")) {
            String[] parts = line.replace("int ", "").replace(";", "").split("=");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String value = evaluateExpression(parts[1].trim());
                variables.put(varName, value);
            }
        } else if (line.startsWith("String ") && line.contains("=")) {
            String[] parts = line.replace("String ", "").replace(";", "").split("=");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String value = parts[1].trim();
                variables.put(varName, value);
            }
        }
        // Variable assignments
        else if (line.contains("=") && !line.contains("==") && !line.startsWith("for") && !line.startsWith("if")) {
            String[] parts = line.replace(";", "").split("=");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                if (variables.containsKey(varName)) {
                    String value = evaluateExpression(parts[1].trim());
                    variables.put(varName, value);
                }
            }
        }
        // Increment
        else if (line.contains("++")) {
            String varName = line.replace("++", "").replace(";", "").trim();
            if (variables.containsKey(varName)) {
                try {
                    int val = Integer.parseInt(variables.get(varName));
                    variables.put(varName, String.valueOf(val + 1));
                } catch (Exception ignored) {}
            }
        }
        // Print statements
        else if (line.contains("System.out.print") || line.contains("console.log")) {
            String output = extractPrintContent(line);
            consoleOutput.add(output);
            fullOutput.append(output).append("\n");
        }
    }
    
    private String evaluateExpression(String expr) {
        expr = expr.trim();
        
        // Simple number
        try {
            return String.valueOf(Integer.parseInt(expr));
        } catch (Exception ignored) {}
        
        // Variable reference
        if (variables.containsKey(expr)) {
            return variables.get(expr);
        }
        
        // Simple arithmetic
        if (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/")) {
            try {
                // Replace variables
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    expr = expr.replace(entry.getKey(), entry.getValue());
                }
                // Evaluate (simple)
                return String.valueOf(eval(expr));
            } catch (Exception ignored) {}
        }
        
        return expr;
    }
    
    private int eval(String expr) {
        // Very simple expression evaluator
        expr = expr.replaceAll("\\s+", "");
        
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            int sum = 0;
            for (String p : parts) sum += Integer.parseInt(p);
            return sum;
        } else if (expr.contains("-")) {
            String[] parts = expr.split("-");
            int result = Integer.parseInt(parts[0]);
            for (int i = 1; i < parts.length; i++) result -= Integer.parseInt(parts[i]);
            return result;
        } else if (expr.contains("*")) {
            String[] parts = expr.split("\\*");
            int result = 1;
            for (String p : parts) result *= Integer.parseInt(p);
            return result;
        } else if (expr.contains("/")) {
            String[] parts = expr.split("/");
            return Integer.parseInt(parts[0]) / Integer.parseInt(parts[1]);
        }
        
        return Integer.parseInt(expr);
    }
    
    private String extractPrintContent(String line) {
        int start = line.indexOf("(") + 1;
        int end = line.lastIndexOf(")");
        if (start > 0 && end > start) {
            String content = line.substring(start, end).trim();
            // Replace variable references
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
            return content.replace("\"", "");
        }
        return "";
    }
    
    private void updateVariables() {
        if (variablesContainer == null) return;
        variablesContainer.removeAllViews();
        
        if (variables.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No variables yet");
            empty.setTextColor(Color.parseColor("#6B7280"));
            empty.setTextSize(12);
            variablesContainer.addView(empty);
            return;
        }
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            TextView tv = new TextView(requireContext());
            tv.setText(entry.getKey() + " = " + entry.getValue());
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(13);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setPadding(0, dpToPx(2), 0, dpToPx(2));
            variablesContainer.addView(tv);
        }
    }
    
    private void updateOutput() {
        if (textOutput != null) {
            if (fullOutput.length() == 0) {
                textOutput.setText("Console output will appear here...");
                textOutput.setTextColor(Color.parseColor("#6B7280"));
            } else {
                textOutput.setText(fullOutput.toString());
                textOutput.setTextColor(Color.parseColor("#22C55E"));
            }
        }
        
        if (outputScroll != null) {
            outputScroll.post(() -> outputScroll.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    private void scrollToLine(int line) {
        if (codeScroll != null && codeContainer != null && line < codeContainer.getChildCount()) {
            View lineView = codeContainer.getChildAt(line);
            codeScroll.smoothScrollTo(0, lineView.getTop() - dpToPx(50));
        }
    }
    
    private void selectBugLine(int line) {
        if (bugFound) return;
        
        if (line == currentChallenge.bugLine) {
            // CORRECT!
            bugFound = true;
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            
            int xpGain = currentChallenge.xpReward;
            totalXp += xpGain;
            
            Toast.makeText(getContext(), "🎉 CORRECT! +" + xpGain + " XP", Toast.LENGTH_LONG).show();
            
            // Highlight the bug
            highlightBugLine(line);
            
            // Show explanation
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                showExplanation();
            }, 1500);
            
        } else {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(getContext(), "❌ Not the bug! Keep stepping through.", Toast.LENGTH_SHORT).show();
        }
        
        updateStats();
    }
    
    private void highlightBugLine(int line) {
        if (codeContainer == null || line >= codeContainer.getChildCount()) return;
        
        View row = codeContainer.getChildAt(line);
        row.setBackgroundColor(Color.parseColor("#7F1D1D"));
        
        // Show breakpoint
        if (row instanceof LinearLayout) {
            View indicator = ((LinearLayout) row).getChildAt(0);
            if (indicator != null) indicator.setVisibility(View.VISIBLE);
        }
        
        // Pulse animation
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(row,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.02f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.02f, 1f));
        pulse.setDuration(500);
        pulse.setRepeatCount(2);
        pulse.start();
    }
    
    private void showBugOptions() {
        if (currentChallenge == null || getContext() == null) return;
        
        String[] options = currentChallenge.bugOptions;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("What's the bug?")
            .setItems(options, (d, which) -> {
                if (options[which].equals(currentChallenge.correctAnswer)) {
                    bugFound = true;
                    soundManager.playSound(SoundManager.Sound.SUCCESS);
                    totalXp += currentChallenge.xpReward;
                    Toast.makeText(getContext(), "🎉 CORRECT! +" + currentChallenge.xpReward + " XP", Toast.LENGTH_LONG).show();
                    highlightBugLine(currentChallenge.bugLine);
                    handler.postDelayed(this::showExplanation, 1500);
                } else {
                    soundManager.playSound(SoundManager.Sound.ERROR);
                    Toast.makeText(getContext(), "❌ Not quite! Keep debugging.", Toast.LENGTH_SHORT).show();
                }
                updateStats();
            })
            .show();
    }
    
    private void showExplanation() {
        if (currentChallenge == null || getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("💡 Bug Explanation")
            .setMessage("Bug: " + currentChallenge.bugDescription + "\n\n" +
                    "Fix: " + currentChallenge.fix + "\n\n" +
                    "XP Earned: " + currentChallenge.xpReward)
            .setPositiveButton("Next Challenge", (d, w) -> {
                challengeIndex++;
                loadChallenge();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showCompletion() {
        if (getContext() == null) return;
        
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🏆 Debugging Master!")
            .setMessage("You completed all debugging challenges!\n\nTotal XP: " + totalXp)
            .setPositiveButton("Play Again", (d, w) -> {
                challengeIndex = 0;
                totalXp = 0;
                Collections.shuffle(challenges);
                loadChallenge();
            })
            .setNegativeButton("Exit", (d, w) -> {
                if (getView() != null) Navigation.findNavController(getView()).navigateUp();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showExitConfirmation() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Exit Debugger?")
            .setMessage("XP Earned: " + totalXp)
            .setPositiveButton("Exit", (d, w) -> {
                if (getView() != null) Navigation.findNavController(getView()).navigateUp();
            })
            .setNegativeButton("Continue", null)
            .show();
    }
    
    private void updateStats() {
        if (textXp != null) textXp.setText("⭐ " + totalXp + " XP");
        if (textStep != null) textStep.setText("Line " + (currentLine + 1));
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         CHALLENGE DATABASE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void initChallenges() {
        challenges = new ArrayList<>();
        
        challenges.add(new DebugChallenge(
            "The Loop That Never Ends",
            "int i = 0;\nint sum = 0;\nwhile (i < 5) {\n    sum = sum + i;\n    // i++; <- MISSING!\n}\nSystem.out.println(sum);",
            4, // Bug on line 5 (0-indexed: 4)
            "Missing increment i++",
            "Add i++ inside the loop",
            new String[]{"Missing i++", "Wrong condition", "Wrong initialization", "Sum calculation error"},
            "Missing i++",
            50
        ));
        
        challenges.add(new DebugChallenge(
            "Array Index Chaos",
            "int[] nums = {1, 2, 3, 4, 5};\nint sum = 0;\nfor (int i = 0; i <= nums.length; i++) {\n    sum = sum + nums[i];\n}\nSystem.out.println(sum);",
            2, // Bug on line 3
            "Off-by-one error: i <= should be i <",
            "Change <= to < in loop condition",
            new String[]{"Change <= to <", "Change i = 0 to i = 1", "Wrong array", "Sum calculation error"},
            "Change <= to <",
            45
        ));
        
        challenges.add(new DebugChallenge(
            "Division Disaster",
            "int total = 7;\nint count = 2;\nint average = total / count;\nSystem.out.println(average);\n// Expected: 3.5, Got: 3",
            2, // Bug on line 3
            "Integer division loses decimals",
            "Use double instead of int for average",
            new String[]{"Use double for average", "Wrong total value", "Wrong count value", "Print error"},
            "Use double for average",
            55
        ));
        
        challenges.add(new DebugChallenge(
            "String Compare Trap",
            "String input = \"yes\";\nString expected = \"yes\";\nif (input == expected) {\n    System.out.println(\"Match!\");\n} else {\n    System.out.println(\"No match\");\n}",
            2, // Bug on line 3
            "Using == instead of .equals() for Strings",
            "Use input.equals(expected)",
            new String[]{"Use .equals()", "Wrong string values", "If condition syntax", "Print statement"},
            "Use .equals()",
            50
        ));
        
        challenges.add(new DebugChallenge(
            "Counter Confusion",
            "int count = 10;\nwhile (count > 0) {\n    System.out.println(count);\n    count++; // Should be count--!\n}",
            3, // Bug on line 4
            "Incrementing instead of decrementing",
            "Change count++ to count--",
            new String[]{"Change ++ to --", "Wrong initial value", "Wrong condition", "Print error"},
            "Change ++ to --",
            40
        ));
        
        Collections.shuffle(challenges, random);
    }
    
    private static class DebugChallenge {
        String title, code, bugDescription, fix, correctAnswer;
        String[] bugOptions;
        int bugLine, xpReward;
        
        DebugChallenge(String title, String code, int bugLine, String bugDescription, 
                      String fix, String[] bugOptions, String correctAnswer, int xpReward) {
            this.title = title;
            this.code = code;
            this.bugLine = bugLine;
            this.bugDescription = bugDescription;
            this.fix = fix;
            this.bugOptions = bugOptions;
            this.correctAnswer = correctAnswer;
            this.xpReward = xpReward;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        
        rootView = null;
        codeContainer = null;
        variablesContainer = null;
        outputContainer = null;
        codeScroll = null;
        outputScroll = null;
        textChallenge = null;
        textXp = null;
        textStep = null;
        textOutput = null;
        btnStepOver = null;
        btnStepInto = null;
        btnContinue = null;
        btnFindBug = null;
        cardVariables = null;
    }
}
