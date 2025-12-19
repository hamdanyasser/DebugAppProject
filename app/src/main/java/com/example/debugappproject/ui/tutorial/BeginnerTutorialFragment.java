package com.example.debugappproject.ui.tutorial;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - BEGINNER TUTORIAL SYSTEM                              ║
 * ║        Learn Debugging from Zero - All Programming Languages                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This tutorial is designed for COMPLETE BEGINNERS who have never programmed before.
 * It teaches:
 * 1. What is a bug?
 * 2. What is debugging?
 * 3. Common bug types in all languages
 * 4. How to spot bugs
 * 5. How to fix bugs
 * 6. Practice with real examples
 */
public class BeginnerTutorialFragment extends Fragment {

    private static final String PREFS_NAME = "tutorial_progress";
    private static final String KEY_CURRENT_LESSON = "current_lesson";
    private static final String KEY_COMPLETED_LESSONS = "completed_lessons";

    private SharedPreferences prefs;
    private SoundManager soundManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Views
    private LinearLayout rootLayout;
    private ScrollView scrollView;
    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textLessonNumber;
    private ProgressBar progressLesson;
    private MaterialCardView cardContent;
    private TextView textContent;
    private TextView textCodeExample;
    private LinearLayout layoutOptions;
    private MaterialButton buttonNext;
    private MaterialButton buttonBack;
    private View buttonClose;

    // Tutorial state
    private int currentLesson = 0;
    private List<TutorialLesson> lessons = new ArrayList<>();
    private boolean isQuizMode = false;
    private int correctAnswerIndex = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_beginner_tutorial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        soundManager = SoundManager.getInstance(requireContext());

        findViews(view);
        initializeLessons();
        setupButtons();

        // Load saved progress
        currentLesson = prefs.getInt(KEY_CURRENT_LESSON, 0);
        showLesson(currentLesson);

        playEntranceAnimation();
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }

    private void findViews(View view) {
        rootLayout = view.findViewById(R.id.layout_root);
        scrollView = view.findViewById(R.id.scroll_content);
        textTitle = view.findViewById(R.id.text_title);
        textSubtitle = view.findViewById(R.id.text_subtitle);
        textLessonNumber = view.findViewById(R.id.text_lesson_number);
        progressLesson = view.findViewById(R.id.progress_lesson);
        cardContent = view.findViewById(R.id.card_content);
        textContent = view.findViewById(R.id.text_content);
        textCodeExample = view.findViewById(R.id.text_code_example);
        layoutOptions = view.findViewById(R.id.layout_options);
        buttonNext = view.findViewById(R.id.button_next);
        buttonBack = view.findViewById(R.id.button_back);
        buttonClose = view.findViewById(R.id.button_close);
    }

    private void setupButtons() {
        buttonNext.setOnClickListener(v -> {
            soundManager.playButtonClick();
            if (isQuizMode) {
                // Quiz not answered yet
                return;
            }
            nextLesson();
        });

        buttonBack.setOnClickListener(v -> {
            soundManager.playButtonClick();
            previousLesson();
        });

        buttonClose.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showExitConfirmation();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         TUTORIAL CONTENT
    // ═══════════════════════════════════════════════════════════════════════════

    private void initializeLessons() {
        lessons.clear();

        // PART 1: Introduction to Bugs
        lessons.add(new TutorialLesson(
            "🐛 What is a Bug?",
            "Welcome to Debugging!",
            "A **bug** is a mistake in code that makes a program do something wrong.\n\n" +
            "Just like typos in writing, programmers make small mistakes that cause problems.\n\n" +
            "🎯 **Example:** If you wrote \"2 + 2 = 5\" in a calculator app, that's a bug!\n\n" +
            "Bugs can:\n" +
            "• Make programs crash 💥\n" +
            "• Show wrong results ❌\n" +
            "• Cause weird behavior 🤔\n\n" +
            "**Fun Fact:** The term \"bug\" comes from 1947 when a real moth was found stuck in a computer!",
            null,
            null
        ));

        lessons.add(new TutorialLesson(
            "🔍 What is Debugging?",
            "Becoming a Code Detective",
            "**Debugging** is the process of finding and fixing bugs in code.\n\n" +
            "Think of yourself as a detective 🕵️:\n\n" +
            "1️⃣ **Find the clue** - Where is the problem?\n" +
            "2️⃣ **Investigate** - Why is it happening?\n" +
            "3️⃣ **Fix it** - Correct the mistake!\n\n" +
            "Debugging is a SKILL that gets better with practice.\n\n" +
            "Even expert programmers spend 50% of their time debugging! You're learning one of the most important skills in programming. 💪",
            null,
            null
        ));

        // PART 2: Common Bug Types
        lessons.add(new TutorialLesson(
            "📝 Bug Type 1: Typos",
            "The Most Common Bug",
            "**Typos** are spelling mistakes in code. Computers are VERY picky about spelling!\n\n" +
            "🔴 **Wrong:**\n```\npirnt(\"Hello\")\n```\n\n" +
            "🟢 **Correct:**\n```\nprint(\"Hello\")\n```\n\n" +
            "See the difference? Just ONE letter is wrong!\n\n" +
            "**Tips to avoid typos:**\n" +
            "• Read code carefully, letter by letter\n" +
            "• Look for red squiggly lines in editors\n" +
            "• Compare with working examples",
            "pirnt(\"Hello World\")",
            null
        ));

        lessons.add(new TutorialLesson(
            "🔢 Bug Type 2: Wrong Numbers",
            "Off-by-One Errors",
            "Using the **wrong number** is super common, especially \"off-by-one\" errors.\n\n" +
            "🔴 **Wrong:** (counts 0 to 10 = 11 numbers!)\n```\nfor i in range(0, 11):\n    print(i)\n```\n\n" +
            "🟢 **Correct:** (counts 0 to 9 = 10 numbers)\n```\nfor i in range(0, 10):\n    print(i)\n```\n\n" +
            "**Common mistakes:**\n" +
            "• Starting at 1 instead of 0\n" +
            "• Using < instead of <=\n" +
            "• Wrong array index",
            "for (int i = 0; i <= 10; i++)",
            null
        ));

        lessons.add(new TutorialLesson(
            "📦 Bug Type 3: Missing Parts",
            "Forgetting Important Code",
            "Sometimes we forget to write something important!\n\n" +
            "🔴 **Wrong:** (missing return statement)\n```\ndef add(a, b):\n    result = a + b\n    # Oops! Forgot to return!\n```\n\n" +
            "🟢 **Correct:**\n```\ndef add(a, b):\n    result = a + b\n    return result  # Don't forget!\n```\n\n" +
            "**Things often forgotten:**\n" +
            "• Return statements\n" +
            "• Closing brackets } ] )\n" +
            "• Semicolons ; in some languages\n" +
            "• Import statements",
            "function add(a, b) {\n    let sum = a + b\n    // Missing return!\n}",
            null
        ));

        lessons.add(new TutorialLesson(
            "🔀 Bug Type 4: Wrong Logic",
            "When Code Does the Opposite",
            "Sometimes the code runs but does the **wrong thing**.\n\n" +
            "🔴 **Wrong:** (checks if age is LESS than 18)\n```\nif (age < 18) {\n    print(\"You can vote!\")\n}\n```\n\n" +
            "🟢 **Correct:** (checks if age is 18 OR MORE)\n```\nif (age >= 18) {\n    print(\"You can vote!\")\n}\n```\n\n" +
            "**Logic bugs to watch for:**\n" +
            "• Using < instead of >\n" +
            "• Using AND instead of OR\n" +
            "• Wrong conditions in if statements",
            "if (temperature < 100) {\n    print(\"Water is boiling\")\n}",
            null
        ));

        // PART 3: How to Find Bugs
        lessons.add(new TutorialLesson(
            "🔎 How to Find Bugs",
            "Detective Techniques",
            "Here's how to find bugs like a pro:\n\n" +
            "**1. Read the Error Message 📋**\n" +
            "Error messages tell you WHAT went wrong and WHERE!\n\n" +
            "**2. Check Line by Line 📏**\n" +
            "Start from the error line and read carefully.\n\n" +
            "**3. Use Print Statements 🖨️**\n" +
            "```\nprint(\"Value of x is:\", x)\n```\n" +
            "This shows you what's happening inside your code.\n\n" +
            "**4. Compare with Working Code 📝**\n" +
            "Look at examples that work and spot differences.\n\n" +
            "**5. Take a Break ☕**\n" +
            "Sometimes fresh eyes find bugs faster!",
            null,
            null
        ));

        // PART 4: Language-Specific Tips
        lessons.add(new TutorialLesson(
            "🐍 Python Bugs",
            "Common Python Mistakes",
            "**Python** is beginner-friendly but has these common bugs:\n\n" +
            "**1. Indentation Errors**\n" +
            "Python uses spaces to organize code!\n```\nif True:\n    print(\"Correct\")  # 4 spaces\nprint(\"Wrong\")  # No spaces = error\n```\n\n" +
            "**2. Colon Missing**\n```\nif x > 5  # ❌ Missing :\nif x > 5:  # ✅ Correct\n```\n\n" +
            "**3. Using = instead of ==**\n```\nif x = 5:  # ❌ Assignment\nif x == 5:  # ✅ Comparison\n```",
            "def greet(name)\n    print(\"Hello \" + name)",
            null
        ));

        lessons.add(new TutorialLesson(
            "☕ Java Bugs",
            "Common Java Mistakes",
            "**Java** is strict! Watch for these:\n\n" +
            "**1. Missing Semicolons**\n```\nint x = 5  // ❌ Missing ;\nint x = 5;  // ✅ Correct\n```\n\n" +
            "**2. Case Sensitivity**\n```\nString name  // ❌ lowercase 's'\nString name  // ✅ Capital 'S'\n```\n\n" +
            "**3. Missing Brackets**\n```\nif (x > 5)\n    doOne();\n    doTwo();  // ❌ Only doOne is in if!\n\nif (x > 5) {\n    doOne();\n    doTwo();  // ✅ Both in if\n}\n```",
            "public class Main {\n    public static void main(String[] args)\n        System.out.println(\"Hello\")\n    }\n}",
            null
        ));

        lessons.add(new TutorialLesson(
            "🌐 JavaScript Bugs",
            "Common JavaScript Mistakes",
            "**JavaScript** is flexible but tricky:\n\n" +
            "**1. == vs ===**\n```\n5 == \"5\"   // true (loose)\n5 === \"5\"  // false (strict) ✅\n```\n\n" +
            "**2. Undefined Variables**\n```\nconsole.log(myVar)  // ❌ Not defined!\nlet myVar = 5;\nconsole.log(myVar)  // ✅ Correct\n```\n\n" +
            "**3. Forgetting 'let' or 'const'**\n```\nx = 5  // ❌ Creates global variable\nlet x = 5  // ✅ Proper declaration\n```",
            "function add(a, b) {\n    return a + b\n}\nconsole.log(ad(2, 3))",
            null
        ));

        // PART 5: Practice Quiz
        lessons.add(new TutorialLesson(
            "🎯 Quiz Time!",
            "Find the Bug",
            "Look at this Python code. What's wrong?\n\n" +
            "```python\ndef calculate_area(width, height)\n    area = width * height\n    return area\n```\n\n" +
            "Choose the correct answer:",
            "def calculate_area(width, height)\n    area = width * height\n    return area",
            new String[] {
                "A) Missing colon : after function definition",
                "B) Wrong variable names",
                "C) Missing print statement",
                "D) Nothing is wrong"
            }
        ));

        lessons.add(new TutorialLesson(
            "🎯 Quiz 2",
            "Spot the Error",
            "What's wrong with this Java code?\n\n" +
            "```java\npublic int double(int x) {\n    return x * 2\n}\n```\n\n" +
            "Choose the correct answer:",
            "public int double(int x) {\n    return x * 2\n}",
            new String[] {
                "A) Missing semicolon after return",
                "B) Wrong return type",
                "C) Both A and 'double' is a reserved word",
                "D) Nothing is wrong"
            }
        ));

        lessons.add(new TutorialLesson(
            "🎯 Quiz 3",
            "Fix the Logic",
            "This code should check if someone is a teenager (13-19). What's wrong?\n\n" +
            "```javascript\nif (age > 13 && age < 19) {\n    console.log(\"Teenager!\");\n}\n```\n\n" +
            "Choose the correct answer:",
            "if (age > 13 && age < 19) {\n    console.log(\"Teenager!\");\n}",
            new String[] {
                "A) Should use || instead of &&",
                "B) Should be >= 13 and <= 19",
                "C) Missing semicolon",
                "D) Nothing is wrong"
            }
        ));

        // PART 6: Completion
        lessons.add(new TutorialLesson(
            "🎉 Congratulations!",
            "You're a Debugger Now!",
            "**Amazing job!** You've learned:\n\n" +
            "✅ What bugs are\n" +
            "✅ The 4 main types of bugs\n" +
            "✅ How to find bugs\n" +
            "✅ Common bugs in Python, Java & JavaScript\n" +
            "✅ How to fix bugs\n\n" +
            "**Your Next Steps:**\n" +
            "1. 🎯 Try Quick Fix mode for practice\n" +
            "2. 📚 Explore Learning Paths\n" +
            "3. 💪 Challenge yourself with harder bugs\n\n" +
            "Remember: Every expert was once a beginner. Keep practicing! 🚀\n\n" +
            "**You earned: +500 XP! 🏆**",
            null,
            null
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         LESSON DISPLAY
    // ═══════════════════════════════════════════════════════════════════════════

    private void showLesson(int index) {
        if (index < 0 || index >= lessons.size()) return;

        TutorialLesson lesson = lessons.get(index);
        currentLesson = index;
        isQuizMode = lesson.options != null;

        // Update progress
        textLessonNumber.setText(String.format("Lesson %d of %d", index + 1, lessons.size()));
        progressLesson.setMax(lessons.size());
        progressLesson.setProgress(index + 1);

        // Update content
        textTitle.setText(lesson.title);
        textSubtitle.setText(lesson.subtitle);
        textContent.setText(lesson.content);

        // Show/hide code example
        if (lesson.codeExample != null && !lesson.codeExample.isEmpty()) {
            textCodeExample.setVisibility(View.VISIBLE);
            textCodeExample.setText(lesson.codeExample);
        } else {
            textCodeExample.setVisibility(View.GONE);
        }

        // Show/hide options for quiz
        layoutOptions.removeAllViews();
        if (lesson.options != null) {
            isQuizMode = true;
            buttonNext.setText("Select an Answer");
            buttonNext.setEnabled(false);
            buttonNext.setAlpha(0.5f);

            // Set correct answer based on quiz
            if (index == 10) correctAnswerIndex = 0; // Quiz 1: Missing colon
            else if (index == 11) correctAnswerIndex = 2; // Quiz 2: Both A and reserved word
            else if (index == 12) correctAnswerIndex = 1; // Quiz 3: >= and <=

            for (int i = 0; i < lesson.options.length; i++) {
                final int optionIndex = i;
                MaterialButton optionButton = createOptionButton(lesson.options[i], i);
                optionButton.setOnClickListener(v -> handleQuizAnswer(optionIndex));
                layoutOptions.addView(optionButton);
            }
        } else {
            isQuizMode = false;
            buttonNext.setText(index == lessons.size() - 1 ? "🎉 Finish Tutorial" : "Next →");
            buttonNext.setEnabled(true);
            buttonNext.setAlpha(1f);
        }

        // Show/hide back button
        buttonBack.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);

        // Save progress
        prefs.edit().putInt(KEY_CURRENT_LESSON, index).apply();

        // Scroll to top
        scrollView.smoothScrollTo(0, 0);

        // Animate content
        animateContentChange();
    }

    private MaterialButton createOptionButton(String text, int index) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundColor(Color.parseColor("#2D2D44"));
        button.setCornerRadius(24);
        button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#7C4DFF")));
        button.setStrokeWidth(2);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        button.setLayoutParams(params);
        button.setPadding(32, 24, 32, 24);

        return button;
    }

    private void handleQuizAnswer(int selectedIndex) {
        soundManager.playButtonClick();

        // Disable all options
        for (int i = 0; i < layoutOptions.getChildCount(); i++) {
            layoutOptions.getChildAt(i).setEnabled(false);
        }

        MaterialButton selectedButton = (MaterialButton) layoutOptions.getChildAt(selectedIndex);

        if (selectedIndex == correctAnswerIndex) {
            // Correct!
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            selectedButton.setBackgroundColor(Color.parseColor("#10B981"));
            selectedButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));

            buttonNext.setText("✅ Correct! Next →");
            buttonNext.setEnabled(true);
            buttonNext.setAlpha(1f);
            isQuizMode = false;

            // Celebrate animation
            animatePulse(selectedButton);
        } else {
            // Wrong
            soundManager.playSound(SoundManager.Sound.ERROR);
            selectedButton.setBackgroundColor(Color.parseColor("#EF4444"));
            selectedButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));

            // Show correct answer
            MaterialButton correctButton = (MaterialButton) layoutOptions.getChildAt(correctAnswerIndex);
            correctButton.setBackgroundColor(Color.parseColor("#10B981"));
            correctButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));

            buttonNext.setText("❌ Wrong. Next →");
            buttonNext.setEnabled(true);
            buttonNext.setAlpha(1f);
            isQuizMode = false;

            // Shake animation
            animateShake(selectedButton);
        }
    }

    private void nextLesson() {
        if (currentLesson < lessons.size() - 1) {
            showLesson(currentLesson + 1);
        } else {
            // Tutorial complete!
            completeTutorial();
        }
    }

    private void previousLesson() {
        if (currentLesson > 0) {
            showLesson(currentLesson - 1);
        }
    }

    private void completeTutorial() {
        soundManager.playSound(SoundManager.Sound.VICTORY);

        // Mark tutorial as complete
        int completedLessons = prefs.getInt(KEY_COMPLETED_LESSONS, 0);
        prefs.edit()
            .putInt(KEY_COMPLETED_LESSONS, lessons.size())
            .putBoolean("tutorial_completed", true)
            .apply();

        new AlertDialog.Builder(requireContext())
            .setTitle("🎉 Tutorial Complete!")
            .setMessage("Congratulations! You've learned the basics of debugging.\n\n" +
                       "You earned:\n" +
                       "🏆 +500 XP\n" +
                       "🎖️ Beginner Debugger Badge\n\n" +
                       "Ready to put your skills to the test?")
            .setPositiveButton("🎯 Try Quick Fix", (d, w) -> {
                Bundle args = new Bundle();
                args.putString("gameMode", "quick_fix");
                Navigation.findNavController(requireView())
                    .navigate(R.id.gameSessionFragment, args);
            })
            .setNegativeButton("🏠 Back Home", (d, w) -> {
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setCancelable(false)
            .show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Exit Tutorial?")
            .setMessage("Your progress is saved! You can continue later from Lesson " + (currentLesson + 1) + ".")
            .setPositiveButton("Exit", (d, w) -> {
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setNegativeButton("Continue Learning", null)
            .show();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private void playEntranceAnimation() {
        cardContent.setAlpha(0f);
        cardContent.setTranslationY(50f);
        cardContent.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateContentChange() {
        cardContent.setAlpha(0.5f);
        cardContent.setTranslationX(30f);
        cardContent.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animatePulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    private void animateShake(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
            0, 15, -15, 15, -15, 8, -8, 0);
        shake.setDuration(500);
        shake.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         LESSON MODEL
    // ═══════════════════════════════════════════════════════════════════════════

    private static class TutorialLesson {
        String title;
        String subtitle;
        String content;
        String codeExample;
        String[] options; // null for info pages, non-null for quiz

        TutorialLesson(String title, String subtitle, String content, String codeExample, String[] options) {
            this.title = title;
            this.subtitle = subtitle;
            this.content = content;
            this.codeExample = codeExample;
            this.options = options;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
