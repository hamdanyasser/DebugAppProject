package com.example.debugappproject.ui.tutorial;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import io.noties.markwon.Markwon;

/**
 * ViewPager2 adapter for the beginner tutorial.
 * Handles 4 interactive lessons with animations and code execution.
 */
public class TutorialPagerAdapter extends RecyclerView.Adapter<TutorialPagerAdapter.LessonViewHolder> {

    private final Context context;
    private final Markwon markwon;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private OnLessonInteractionListener listener;

    private static final int NUM_LESSONS = 4;

    private final int[] layouts = {
        R.layout.item_tutorial_lesson_1,
        R.layout.item_tutorial_lesson_2,
        R.layout.item_tutorial_lesson_3,
        R.layout.item_tutorial_lesson_4
    };

    public interface OnLessonInteractionListener {
        void onCodeExecutionComplete(int lessonIndex, boolean success);
        void onLessonCompleted(int lessonIndex);
    }

    public TutorialPagerAdapter(Context context) {
        this.context = context;
        this.markwon = Markwon.create(context);
    }

    public void setListener(OnLessonInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        return new LessonViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return NUM_LESSONS;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private final int lessonType;

        LessonViewHolder(@NonNull View itemView, int lessonType) {
            super(itemView);
            this.lessonType = lessonType;
        }

        void bind(int position) {
            switch (lessonType) {
                case 0:
                    bindLesson1();
                    break;
                case 1:
                    bindLesson2();
                    break;
                case 2:
                    bindLesson3();
                    break;
                case 3:
                    bindLesson4();
                    break;
            }
            startAnimations();
        }

        private void bindLesson1() {
            TextView textContent = itemView.findViewById(R.id.text_content);

            String content = "A **bug** is a mistake in code that makes a program do something wrong.\n\n" +
                "Just like typos in writing, programmers make small mistakes that cause problems.\n\n" +
                "Bugs can:\n" +
                "- Make programs crash \uD83D\uDCA5\n" +
                "- Show wrong results \u274C\n" +
                "- Cause weird behavior \uD83E\uDD14\n\n" +
                "Even the best programmers write bugs. The key skill is **learning to find and fix them!**";

            markwon.setMarkdown(textContent, content);
        }

        private void bindLesson2() {
            TextView textBuggyCode = itemView.findViewById(R.id.text_buggy_code);
            MaterialButton buttonReveal = itemView.findViewById(R.id.button_reveal);
            MaterialCardView cardAnswer = itemView.findViewById(R.id.card_answer);
            TextView textExplanation = itemView.findViewById(R.id.text_explanation);
            TextView textCorrectCode = itemView.findViewById(R.id.text_correct_code);

            textBuggyCode.setText("System.out.printIn(\"Hello World\");");

            buttonReveal.setOnClickListener(v -> {
                cardAnswer.setVisibility(View.VISIBLE);
                buttonReveal.setVisibility(View.GONE);

                String explanation = "The bug is in **printIn** - it should be **println** (lowercase L, not uppercase I)!\n\n" +
                    "This is called a **typo bug** - one of the most common types. Java is case-sensitive, so `printIn` and `println` are completely different.";
                markwon.setMarkdown(textExplanation, explanation);

                textCorrectCode.setText("System.out.println(\"Hello World\");");

                // Animate the card appearing
                cardAnswer.setAlpha(0f);
                cardAnswer.setTranslationY(20f);
                cardAnswer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();

                if (listener != null) {
                    listener.onLessonCompleted(1);
                }
            });
        }

        private void bindLesson3() {
            TextView textBuggyCode = itemView.findViewById(R.id.text_buggy_code);
            EditText editUserCode = itemView.findViewById(R.id.edit_user_code);
            MaterialButton buttonRun = itemView.findViewById(R.id.button_run_code);
            MaterialCardView cardResult = itemView.findViewById(R.id.card_result);
            TextView textResultTitle = itemView.findViewById(R.id.text_result_title);
            TextView textResultMessage = itemView.findViewById(R.id.text_result_message);
            TextView textOutput = itemView.findViewById(R.id.text_output);

            String buggyCode = "System.out.printIn(\"Hello World\");";
            textBuggyCode.setText(buggyCode);
            editUserCode.setText(buggyCode);

            buttonRun.setOnClickListener(v -> {
                String userCode = editUserCode.getText().toString().trim();
                buttonRun.setEnabled(false);
                buttonRun.setText("Running...");

                validateAndExecuteCode(userCode, cardResult, textResultTitle, textResultMessage, textOutput, buttonRun);
            });
        }

        private void validateAndExecuteCode(String userCode, MaterialCardView cardResult,
                                           TextView textResultTitle, TextView textResultMessage,
                                           TextView textOutput, MaterialButton buttonRun) {

            // Use pattern-based validation instead of actual code execution
            // (Janino doesn't work properly on Android runtime)
            handler.postDelayed(() -> {
                cardResult.setVisibility(View.VISIBLE);
                buttonRun.setEnabled(true);
                buttonRun.setText("\u25B6  Run Code");

                // Check if the user fixed the bug (printIn -> println)
                String normalizedCode = userCode.replaceAll("\\s+", "");
                boolean hasCorrectMethod = normalizedCode.contains("println(") || normalizedCode.contains("println (");
                boolean hasHelloWorld = userCode.contains("\"Hello World\"") || userCode.contains("\"Hello World\"");
                boolean hasBuggyMethod = userCode.contains("printIn");

                if (hasCorrectMethod && hasHelloWorld && !hasBuggyMethod) {
                    // SUCCESS!
                    textResultTitle.setText("\uD83C\uDF89 Perfect!");
                    textResultTitle.setTextColor(0xFF10B981);
                    textResultMessage.setText("You fixed the bug! The code now works correctly.");
                    textOutput.setText("Output: Hello World");
                    textOutput.setTextColor(0xFF10B981);

                    cardResult.setStrokeColor(0xFF10B981);

                    if (listener != null) {
                        listener.onCodeExecutionComplete(getAdapterPosition(), true);
                    }
                } else if (hasBuggyMethod) {
                    // Still has the bug
                    textResultTitle.setText("\u274C Error");
                    textResultTitle.setTextColor(0xFFFF5252);
                    textResultMessage.setText("Hint: Check the method name - is it 'printIn' or 'println'? (lowercase L, not uppercase I)");
                    textOutput.setText("Error: Cannot find method printIn");
                    textOutput.setTextColor(0xFFFF5252);

                    cardResult.setStrokeColor(0xFFFF5252);
                } else if (!hasHelloWorld) {
                    // Missing or wrong string
                    textResultTitle.setText("\uD83E\uDD14 Almost there...");
                    textResultTitle.setTextColor(0xFFFFC107);
                    textResultMessage.setText("Make sure the string says exactly \"Hello World\"");
                    textOutput.setText("Check your string content");
                    textOutput.setTextColor(0xFFFFC107);

                    cardResult.setStrokeColor(0xFFFFC107);
                } else {
                    // Some other issue
                    textResultTitle.setText("\u274C Error");
                    textResultTitle.setTextColor(0xFFFF5252);
                    textResultMessage.setText("Something's not right. Make sure to use System.out.println()");
                    textOutput.setText("Check your syntax");
                    textOutput.setTextColor(0xFFFF5252);

                    cardResult.setStrokeColor(0xFFFF5252);
                }

                // Animate result card
                cardResult.setAlpha(0f);
                cardResult.setTranslationY(20f);
                cardResult.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
            }, 500); // Small delay to simulate "running"
        }

        private void bindLesson4() {
            // Lesson 4 is mostly static content, just trigger animations
            if (listener != null) {
                listener.onLessonCompleted(3);
            }
        }

        private void startAnimations() {
            // Animate glow ring rotation
            View glowRing = itemView.findViewById(R.id.glow_ring);
            if (glowRing != null) {
                ObjectAnimator rotation = ObjectAnimator.ofFloat(glowRing, "rotation", 0f, 360f);
                rotation.setDuration(20000);
                rotation.setRepeatCount(ValueAnimator.INFINITE);
                rotation.setInterpolator(new LinearInterpolator());
                rotation.start();
            }

            // Animate icon bounce
            View iconContainer = itemView.findViewById(R.id.icon_container);
            if (iconContainer != null) {
                ObjectAnimator bounce = ObjectAnimator.ofPropertyValuesHolder(
                    iconContainer,
                    PropertyValuesHolder.ofFloat("translationY", 0f, -12f, 0f)
                );
                bounce.setDuration(2000);
                bounce.setRepeatCount(ValueAnimator.INFINITE);
                bounce.start();

                // Scale pulse
                ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(
                    iconContainer,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f)
                );
                pulse.setDuration(3000);
                pulse.setRepeatCount(ValueAnimator.INFINITE);
                pulse.start();
            }
        }
    }

    public void shutdown() {
        // No resources to clean up
    }
}
