package com.example.debugappproject.ui.mentor;

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
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
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

import com.debugmaster.app.R;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              🧠 AI DEBUG MENTOR - INTERACTIVE LEARNING GAME                  ║
 * ║                   Learn debugging through guided challenges!                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * NOT just a chatbot - an ACTUAL GAME where you:
 * 1. See buggy code
 * 2. Get progressive hints from AI mentor
 * 3. Choose the correct fix from multiple options
 * 4. Learn WHY it works
 * 5. Earn XP and level up your debugging skills!
 */
public class MentorChatFragment extends Fragment {

    private SoundManager soundManager;
    private Handler handler;
    private Random random;
    
    // Views
    private View rootView;
    private LinearLayout chatContainer;
    private ScrollView chatScroll;
    private CardView codeCard;
    private TextView textCode, textMentorMessage, textXp, textLevel, textStreak;
    private LinearLayout optionsContainer;
    private MaterialButton btnHint, btnSkip;
    
    // Game State
    private List<MentorChallenge> challenges;
    private MentorChallenge currentChallenge;
    private int currentIndex = 0;
    private int totalXp = 0;
    private int streak = 0;
    private int hintsUsed = 0;
    private int level = 1;
    private boolean answered = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mentor_game, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rootView = view;
        soundManager = SoundManager.getInstance(requireContext());
        handler = new Handler(Looper.getMainLooper());
        random = new Random();
        
        initChallenges();
        findViews();
        setupUI();
        showWelcome();
    }
    
    private void findViews() {
        chatContainer = rootView.findViewById(R.id.container_chat);
        chatScroll = rootView.findViewById(R.id.scroll_chat);
        codeCard = rootView.findViewById(R.id.card_code);
        textCode = rootView.findViewById(R.id.text_code);
        textMentorMessage = rootView.findViewById(R.id.text_mentor_message);
        textXp = rootView.findViewById(R.id.text_xp);
        textLevel = rootView.findViewById(R.id.text_level);
        textStreak = rootView.findViewById(R.id.text_streak);
        optionsContainer = rootView.findViewById(R.id.container_options);
        btnHint = rootView.findViewById(R.id.btn_hint);
        btnSkip = rootView.findViewById(R.id.btn_skip);
        
        View backBtn = rootView.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showExitConfirmation();
            });
        }
    }
    
    private void setupUI() {
        if (btnHint != null) {
            btnHint.setOnClickListener(v -> {
                soundManager.playButtonClick();
                showHint();
            });
        }
        
        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                soundManager.playButtonClick();
                skipChallenge();
            });
        }
        
        updateStats();
    }
    
    private void showWelcome() {
        addMentorMessage("🧠 Welcome to AI Debug Mentor!", false);
        
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            addMentorMessage("I'll show you buggy code and help you find the fix. Ready to learn?", false);
            
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                loadChallenge();
            }, 1500);
        }, 1000);
    }
    
    private void loadChallenge() {
        if (currentIndex >= challenges.size()) {
            showCompletion();
            return;
        }
        
        currentChallenge = challenges.get(currentIndex);
        hintsUsed = 0;
        answered = false;
        
        // Show the challenge
        addMentorMessage("📝 Challenge " + (currentIndex + 1) + "/" + challenges.size() + ": " + currentChallenge.title, true);
        
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            showCode(currentChallenge.buggyCode);
            
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                addMentorMessage(currentChallenge.question, false);
                showOptions();
            }, 800);
        }, 500);
    }
    
    private void showCode(String code) {
        if (codeCard == null || textCode == null) return;
        
        codeCard.setVisibility(View.VISIBLE);
        codeCard.setAlpha(0f);
        codeCard.setScaleX(0.9f);
        codeCard.setScaleY(0.9f);
        
        // Apply syntax highlighting
        SpannableStringBuilder builder = new SpannableStringBuilder(code);
        highlightCode(builder, code);
        textCode.setText(builder);
        
        // Animate in
        codeCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
    }
    
    private void highlightCode(SpannableStringBuilder builder, String code) {
        String[] keywords = {"public", "private", "class", "void", "int", "String", "return", "if", "else", "for", "while", "new", "null", "true", "false", "boolean", "static", "final"};
        
        for (String kw : keywords) {
            int idx = 0;
            while ((idx = code.indexOf(kw, idx)) != -1) {
                boolean validStart = idx == 0 || !Character.isLetterOrDigit(code.charAt(idx - 1));
                boolean validEnd = idx + kw.length() >= code.length() || !Character.isLetterOrDigit(code.charAt(idx + kw.length()));
                
                if (validStart && validEnd) {
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#C084FC")),
                            idx, idx + kw.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                idx += kw.length();
            }
        }
        
        // Highlight comments
        int commentIdx = code.indexOf("//");
        while (commentIdx != -1) {
            int endIdx = code.indexOf("\n", commentIdx);
            if (endIdx == -1) endIdx = code.length();
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#6B7280")),
                    commentIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            commentIdx = code.indexOf("//", endIdx);
        }
        
        // Highlight strings
        boolean inString = false;
        int stringStart = -1;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '"' && (i == 0 || code.charAt(i-1) != '\\')) {
                if (!inString) {
                    stringStart = i;
                    inString = true;
                } else {
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")),
                            stringStart, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    inString = false;
                }
            }
        }
    }
    
    private void showOptions() {
        if (optionsContainer == null || currentChallenge == null) return;
        
        optionsContainer.removeAllViews();
        optionsContainer.setVisibility(View.VISIBLE);
        
        List<String> shuffledOptions = new ArrayList<>(currentChallenge.options);
        Collections.shuffle(shuffledOptions, random);
        
        for (int i = 0; i < shuffledOptions.size(); i++) {
            String option = shuffledOptions.get(i);
            final int index = i;
            
            MaterialButton btn = new MaterialButton(requireContext());
            btn.setText(option);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor("#1E293B"));
            btn.setCornerRadius(dpToPx(12));
            btn.setTextSize(13);
            btn.setAllCaps(false);
            btn.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, dpToPx(8), 0, 0);
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(v -> checkAnswer(option, btn));
            
            // Animate in
            btn.setAlpha(0f);
            btn.setTranslationX(100f);
            optionsContainer.addView(btn);
            
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                btn.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setStartDelay(index * 100L)
                    .start();
            }, 100);
        }
        
        // Enable hint button
        if (btnHint != null) btnHint.setEnabled(true);
    }
    
    private void checkAnswer(String selected, MaterialButton btn) {
        if (answered || currentChallenge == null) return;
        answered = true;
        
        boolean correct = selected.equals(currentChallenge.correctAnswer);
        
        if (correct) {
            // CORRECT!
            btn.setBackgroundColor(Color.parseColor("#22C55E"));
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            
            streak++;
            int xpGain = calculateXp();
            totalXp += xpGain;
            checkLevelUp();
            
            pulseView(btn);
            
            addMentorMessage("✅ CORRECT! +" + xpGain + " XP" + (streak > 1 ? " 🔥 " + streak + "x streak!" : ""), false);
            
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                addMentorMessage("💡 " + currentChallenge.explanation, false);
                
                handler.postDelayed(() -> {
                    if (!isAdded()) return;
                    currentIndex++;
                    loadChallenge();
                }, 2000);
            }, 1000);
            
        } else {
            // WRONG
            btn.setBackgroundColor(Color.parseColor("#EF4444"));
            soundManager.playSound(SoundManager.Sound.ERROR);
            
            streak = 0;
            
            shakeView(btn);
            
            addMentorMessage("❌ Not quite! Try again or use a hint.", false);
            
            // Re-enable after delay
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                answered = false;
                btn.setBackgroundColor(Color.parseColor("#374151"));
            }, 1000);
        }
        
        updateStats();
    }
    
    private int calculateXp() {
        int base = currentChallenge.xpReward;
        int hintPenalty = hintsUsed * 10;
        int streakBonus = streak > 1 ? (streak - 1) * 5 : 0;
        return Math.max(10, base - hintPenalty + streakBonus);
    }
    
    private void showHint() {
        if (currentChallenge == null || hintsUsed >= currentChallenge.hints.size()) {
            Toast.makeText(getContext(), "No more hints!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String hint = currentChallenge.hints.get(hintsUsed);
        hintsUsed++;
        
        addMentorMessage("💡 Hint " + hintsUsed + ": " + hint, false);
        soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
        
        if (hintsUsed >= currentChallenge.hints.size() && btnHint != null) {
            btnHint.setEnabled(false);
        }
    }
    
    private void skipChallenge() {
        if (currentChallenge == null) return;
        
        streak = 0;
        addMentorMessage("⏭️ Skipped. The answer was: " + currentChallenge.correctAnswer, false);
        
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            addMentorMessage("💡 " + currentChallenge.explanation, false);
            
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                currentIndex++;
                loadChallenge();
            }, 2000);
        }, 1000);
        
        updateStats();
    }
    
    private void addMentorMessage(String message, boolean isTitle) {
        if (chatContainer == null || !isAdded()) return;
        
        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(isTitle ? Color.parseColor("#F59E0B") : Color.WHITE);
        tv.setTextSize(isTitle ? 16 : 14);
        if (isTitle) tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        tv.setBackgroundColor(Color.parseColor("#1E293B"));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(8), 0, 0);
        tv.setLayoutParams(params);
        
        tv.setAlpha(0f);
        chatContainer.addView(tv);
        tv.animate().alpha(1f).setDuration(300).start();
        
        // Scroll to bottom
        if (chatScroll != null) {
            chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
        }
    }
    
    private void updateStats() {
        if (textXp != null) textXp.setText("⭐ " + totalXp + " XP");
        if (textLevel != null) textLevel.setText("Lv." + level);
        if (textStreak != null) {
            textStreak.setText("🔥 " + streak);
            textStreak.setVisibility(streak > 0 ? View.VISIBLE : View.GONE);
        }
    }
    
    private void checkLevelUp() {
        int newLevel = 1 + (totalXp / 200);
        if (newLevel > level) {
            level = newLevel;
            soundManager.playSound(SoundManager.Sound.LEVEL_UP);
            Toast.makeText(getContext(), "🎉 LEVEL UP! Now Level " + level, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showCompletion() {
        if (codeCard != null) codeCard.setVisibility(View.GONE);
        if (optionsContainer != null) optionsContainer.setVisibility(View.GONE);
        
        addMentorMessage("🏆 AMAZING! You completed all challenges!", true);
        addMentorMessage("Final Score: " + totalXp + " XP | Level " + level, false);
        
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🏆 Session Complete!")
            .setMessage("Total XP: " + totalXp + "\nLevel: " + level + "\nBest Streak: " + streak)
            .setPositiveButton("Play Again", (d, w) -> {
                currentIndex = 0;
                totalXp = 0;
                streak = 0;
                level = 1;
                Collections.shuffle(challenges);
                if (chatContainer != null) chatContainer.removeAllViews();
                showWelcome();
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
            .setTitle("Exit Mentor Session?")
            .setMessage("XP Earned: " + totalXp + "\nProgress will be saved!")
            .setPositiveButton("Exit", (d, w) -> {
                if (getView() != null) Navigation.findNavController(getView()).navigateUp();
            })
            .setNegativeButton("Stay", null)
            .show();
    }
    
    private void pulseView(View v) {
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f));
        pulse.setDuration(300);
        pulse.start();
    }
    
    private void shakeView(View v) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, 15, -15, 10, -10, 5, -5, 0);
        shake.setDuration(400);
        shake.start();
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         CHALLENGE DATABASE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void initChallenges() {
        challenges = new ArrayList<>();
        
        // Challenge 1: NullPointerException
        challenges.add(new MentorChallenge(
            "The Null Pointer Trap",
            "public String greet(User user) {\n    return \"Hello, \" + user.getName();\n}",
            "This crashes when user is null. What's the fix?",
            List.of(
                "Add: if (user == null) return \"Hello, Guest\";",
                "Add: try { } catch (Exception e) { }",
                "Change to: return \"Hello, \" + user;",
                "Add: user = new User();"
            ),
            "Add: if (user == null) return \"Hello, Guest\";",
            "Always check for null before calling methods on objects. This prevents NullPointerException!",
            List.of("What happens if user is null?", "We need to handle the null case", "Check before using!"),
            50
        ));
        
        // Challenge 2: Off-by-one
        challenges.add(new MentorChallenge(
            "The Sneaky Loop Bug",
            "int[] nums = {1, 2, 3, 4, 5};\nfor (int i = 0; i <= nums.length; i++) {\n    sum += nums[i];\n}",
            "This code crashes. Can you spot why?",
            List.of(
                "Change <= to <",
                "Change i = 0 to i = 1",
                "Add nums.length - 1",
                "Remove the = sign entirely"
            ),
            "Change <= to <",
            "Arrays are 0-indexed! With 5 elements, valid indices are 0-4. Using <= tries to access index 5 which doesn't exist!",
            List.of("How many elements are in the array?", "What indices are valid?", "What does <= do vs < ?"),
            40
        ));
        
        // Challenge 3: String comparison
        challenges.add(new MentorChallenge(
            "String Identity Crisis",
            "String input = getUserInput();\nif (input == \"yes\") {\n    proceed();\n}",
            "Users type 'yes' but it doesn't work. Why?",
            List.of(
                "Use input.equals(\"yes\")",
                "Use input.compareTo(\"yes\")",
                "Use input == \"yes\".toString()",
                "Use \"yes\" == input"
            ),
            "Use input.equals(\"yes\")",
            "In Java, == compares object references, not content. Use .equals() to compare String contents!",
            List.of("What does == actually compare?", "Strings are objects in Java", ".equals() compares content"),
            45
        ));
        
        // Challenge 4: Integer division
        challenges.add(new MentorChallenge(
            "The Math Mystery",
            "int total = 7;\nint count = 2;\ndouble avg = total / count;\n// avg = 3.0 ???",
            "Why is avg 3.0 instead of 3.5?",
            List.of(
                "Cast: (double) total / count",
                "Use: total / (double) count",
                "Change int to float",
                "Both A and B work"
            ),
            "Both A and B work",
            "Integer division truncates! 7/2 = 3 in integer math. Cast at least one operand to double first.",
            List.of("What type is total/count?", "Integer division drops decimals", "Force floating-point division"),
            50
        ));
        
        // Challenge 5: Infinite loop
        challenges.add(new MentorChallenge(
            "The Infinite Loop",
            "int i = 0;\nwhile (i < 10) {\n    System.out.println(i);\n}",
            "This program never ends! What's missing?",
            List.of(
                "Add i++ inside the loop",
                "Change < to <=",
                "Add break; at the end",
                "Change while to for"
            ),
            "Add i++ inside the loop",
            "Without incrementing i, the condition i < 10 is always true! Always ensure loop conditions will eventually be false.",
            List.of("Does i ever change?", "When will i < 10 be false?", "The loop needs to progress!"),
            35
        ));
        
        // Challenge 6: ArrayList modification
        challenges.add(new MentorChallenge(
            "List Surgery Gone Wrong",
            "for (String s : list) {\n    if (s.equals(\"remove\")) {\n        list.remove(s);\n    }\n}",
            "This throws ConcurrentModificationException. Fix?",
            List.of(
                "Use Iterator with iter.remove()",
                "Use list.removeIf(s -> s.equals(\"remove\"))",
                "Both A and B work",
                "Use a regular for loop"
            ),
            "Both A and B work",
            "You can't modify a list while iterating with for-each! Use Iterator.remove() or removeIf() instead.",
            List.of("Can you modify during iteration?", "Iterator has a special remove", "removeIf is even cleaner!"),
            60
        ));
        
        // Challenge 7: StringBuilder
        challenges.add(new MentorChallenge(
            "The Slow String",
            "String result = \"\";\nfor (int i = 0; i < 10000; i++) {\n    result += i + \",\";\n}",
            "This is very slow! What's better?",
            List.of(
                "Use StringBuilder",
                "Use StringBuffer",
                "Use String.format()",
                "Use result.concat()"
            ),
            "Use StringBuilder",
            "String concatenation in loops creates many temporary String objects. StringBuilder is O(n) vs O(n²)!",
            List.of("How many String objects are created?", "Strings are immutable", "Builder pattern is faster!"),
            55
        ));
        
        // Challenge 8: Resource leak
        challenges.add(new MentorChallenge(
            "The Resource Leak",
            "FileInputStream f = new FileInputStream(\"data.txt\");\nbyte[] data = f.readAllBytes();\n// done!",
            "This leaks resources! How to fix?",
            List.of(
                "Use try-with-resources",
                "Add f.close() at the end",
                "Wrap in try-finally",
                "All of the above work"
            ),
            "All of the above work",
            "Streams must be closed! Try-with-resources (try(FileInputStream f = ...) {}) is cleanest and handles exceptions.",
            List.of("What happens to the file handle?", "Streams need to be closed", "try-with-resources auto-closes!"),
            65
        ));
        
        // Shuffle for variety
        Collections.shuffle(challenges, new Random());
    }
    
    // Challenge data class
    private static class MentorChallenge {
        String title;
        String buggyCode;
        String question;
        List<String> options;
        String correctAnswer;
        String explanation;
        List<String> hints;
        int xpReward;
        
        MentorChallenge(String title, String buggyCode, String question, List<String> options,
                       String correctAnswer, String explanation, List<String> hints, int xpReward) {
            this.title = title;
            this.buggyCode = buggyCode;
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.hints = hints;
            this.xpReward = xpReward;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        rootView = null;
        chatContainer = null;
        chatScroll = null;
        codeCard = null;
        textCode = null;
        textMentorMessage = null;
        textXp = null;
        textLevel = null;
        textStreak = null;
        optionsContainer = null;
        btnHint = null;
        btnSkip = null;
    }
}
