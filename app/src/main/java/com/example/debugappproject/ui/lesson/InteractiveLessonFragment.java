package com.example.debugappproject.ui.lesson;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.debugmaster.app.R;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - INTERACTIVE LESSON EXPERIENCE                        ║
 * ║              Step-by-Step Animated Learning Journey                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * FEATURES:
 * • Step-by-step guided lessons with animations
 * • Interactive code editor with syntax highlighting
 * • Live feedback as user types
 * • Character mascot with speech bubbles
 * • Confetti celebration on correct answers
 * • Hint system with progressive reveals
 * • XP rewards and level up animations
 * • Accessible for all ages (kid-friendly mode)
 */
public class InteractiveLessonFragment extends Fragment {

    private static final String TAG = "InteractiveLesson";
    
    // Views
    private ViewGroup rootView;
    private TextView textLessonTitle;
    private TextView textStepNumber;
    private ProgressBar progressLesson;
    private TextView textMascotSpeech;
    private ImageView imageMascot;
    private LinearLayout layoutMascotBubble;
    private CardView cardCodeEditor;
    private EditText editCode;
    private TextView textExpectedOutput;
    private MaterialButton buttonCheckAnswer;
    private MaterialButton buttonHint;
    private MaterialButton buttonSkip;
    private LinearLayout layoutFeedback;
    private TextView textFeedback;
    private FrameLayout confettiContainer;
    private View xpRewardOverlay;
    private TextView textXpReward;
    
    // State
    private SoundManager soundManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int currentStep = 0;
    private int totalSteps = 5;
    private int hintsUsed = 0;
    private int maxHints = 3;
    private boolean lessonComplete = false;
    private List<LessonStep> lessonSteps = new ArrayList<>();
    
    // Animation state
    private List<View> confettiParticles = new ArrayList<>();
    private Random random = new Random();
    
    // Mascot states
    private static final String[] ENCOURAGING_MESSAGES = {
        "You're doing great! 🌟",
        "Keep going, you've got this! 💪",
        "Excellent progress! 🎯",
        "You're a natural! ⭐",
        "Almost there! 🚀"
    };
    
    private static final String[] HINT_MESSAGES = {
        "Need a hint? No problem! 💡",
        "Let me help you out! 🤝",
        "Here's a little nudge! 👉"
    };
    
    private static final String[] CORRECT_MESSAGES = {
        "Perfect! You nailed it! 🎉",
        "Brilliant! Exactly right! ⭐",
        "Wow, you're amazing! 🏆",
        "Correct! Keep it up! 🌟",
        "That's it! You're a debugging pro! 💫"
    };
    
    private static final String[] WRONG_MESSAGES = {
        "Almost! Try again! 💪",
        "Not quite, but you're close! 🎯",
        "Give it another shot! 🔄",
        "Keep trying, you'll get it! ✨"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_interactive_lesson, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        soundManager = SoundManager.getInstance(requireContext());
        
        findViews();
        setupLesson();
        setupClickListeners();
        setupCodeEditor();
        
        // Start entrance animations
        playEntranceAnimations();
        
        // Show first step
        handler.postDelayed(() -> showStep(0), 800);
    }
    
    private void findViews() {
        textLessonTitle = rootView.findViewById(R.id.text_lesson_title);
        textStepNumber = rootView.findViewById(R.id.text_step_number);
        progressLesson = rootView.findViewById(R.id.progress_lesson);
        textMascotSpeech = rootView.findViewById(R.id.text_mascot_speech);
        imageMascot = rootView.findViewById(R.id.image_mascot);
        layoutMascotBubble = rootView.findViewById(R.id.layout_mascot_bubble);
        cardCodeEditor = rootView.findViewById(R.id.card_code_editor);
        editCode = rootView.findViewById(R.id.edit_code);
        textExpectedOutput = rootView.findViewById(R.id.text_expected_output);
        buttonCheckAnswer = rootView.findViewById(R.id.button_check_answer);
        buttonHint = rootView.findViewById(R.id.button_hint);
        buttonSkip = rootView.findViewById(R.id.button_skip);
        layoutFeedback = rootView.findViewById(R.id.layout_feedback);
        textFeedback = rootView.findViewById(R.id.text_feedback);
        confettiContainer = rootView.findViewById(R.id.confetti_container);
        xpRewardOverlay = rootView.findViewById(R.id.xp_reward_overlay);
        textXpReward = rootView.findViewById(R.id.text_xp_reward);
    }
    
    private void setupLesson() {
        // Create sample lesson steps
        lessonSteps.add(new LessonStep(
            "Welcome to Debugging! 🐛",
            "Let's start with the basics. Debugging is like being a detective - you find clues to solve mysteries in code!",
            null,
            null,
            LessonStepType.INTRO
        ));
        
        lessonSteps.add(new LessonStep(
            "Find the Bug! 🔍",
            "Look at this code. There's a small mistake. Can you spot it?\n\nThe variable name is misspelled!",
            "int conut = 5;\nSystem.out.println(conut);",
            "count",
            LessonStepType.SPOT_THE_BUG
        ));
        
        lessonSteps.add(new LessonStep(
            "Fix the Syntax Error ✏️",
            "This code is missing something important at the end of the line. What's missing?",
            "int x = 10\nSystem.out.println(x);",
            ";",
            LessonStepType.FIX_THE_BUG
        ));
        
        lessonSteps.add(new LessonStep(
            "Complete the Code 🧩",
            "Fill in the blank to print 'Hello World'",
            "System.out._______(\"Hello World\");",
            "println",
            LessonStepType.FILL_IN_BLANK
        ));
        
        lessonSteps.add(new LessonStep(
            "You Did It! 🎉",
            "Congratulations! You've completed your first debugging lesson!\n\nYou learned:\n• How to spot typos\n• Missing semicolons\n• Basic Java syntax",
            null,
            null,
            LessonStepType.COMPLETE
        ));
        
        totalSteps = lessonSteps.size();
        
        if (progressLesson != null) {
            progressLesson.setMax(totalSteps);
            progressLesson.setProgress(0);
        }
    }
    
    private void setupClickListeners() {
        // Back button
        View backButton = rootView.findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Navigation.findNavController(requireView()).navigateUp();
            });
        }
        
        // Check answer
        if (buttonCheckAnswer != null) {
            buttonCheckAnswer.setOnClickListener(v -> {
                soundManager.playButtonClick();
                checkAnswer();
            });
        }
        
        // Hint
        if (buttonHint != null) {
            buttonHint.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                showHint();
            });
        }
        
        // Skip
        if (buttonSkip != null) {
            buttonSkip.setOnClickListener(v -> {
                soundManager.playButtonClick();
                skipStep();
            });
        }
    }
    
    private void setupCodeEditor() {
        if (editCode == null) return;
        
        editCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Live feedback as user types
                if (currentStep < lessonSteps.size()) {
                    LessonStep step = lessonSteps.get(currentStep);
                    if (step.expectedAnswer != null && s.toString().toLowerCase().contains(step.expectedAnswer.toLowerCase())) {
                        // Show encouraging feedback
                        showMascotMessage(ENCOURAGING_MESSAGES[random.nextInt(ENCOURAGING_MESSAGES.length)]);
                    }
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                highlightSyntax(s);
            }
        });
    }
    
    private void highlightSyntax(Editable text) {
        // Simple syntax highlighting
        String code = text.toString();
        
        // Keywords
        String[] keywords = {"int", "String", "System", "out", "println", "void", "public", "private", "class", "if", "else", "for", "while"};
        for (String keyword : keywords) {
            int index = code.indexOf(keyword);
            while (index >= 0) {
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#C792EA")), 
                    index, index + keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = code.indexOf(keyword, index + keyword.length());
            }
        }
        
        // Strings (simple)
        int quoteStart = code.indexOf("\"");
        while (quoteStart >= 0) {
            int quoteEnd = code.indexOf("\"", quoteStart + 1);
            if (quoteEnd > quoteStart) {
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#C3E88D")), 
                    quoteStart, quoteEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            quoteStart = code.indexOf("\"", quoteEnd + 1);
        }
    }
    
    private void showStep(int stepIndex) {
        if (stepIndex >= lessonSteps.size()) {
            completeLessonSequence();
            return;
        }
        
        currentStep = stepIndex;
        LessonStep step = lessonSteps.get(stepIndex);
        hintsUsed = 0;
        
        // Update progress
        if (progressLesson != null) {
            animateProgressBar(stepIndex + 1);
        }
        
        if (textStepNumber != null) {
            textStepNumber.setText("Step " + (stepIndex + 1) + " of " + totalSteps);
        }
        
        // Show step content with animation
        animateStepTransition(() -> {
            if (textLessonTitle != null) {
                textLessonTitle.setText(step.title);
            }
            
            showMascotMessage(step.instruction);
            
            // Show/hide code editor based on step type
            if (cardCodeEditor != null) {
                if (step.type == LessonStepType.INTRO || step.type == LessonStepType.COMPLETE) {
                    cardCodeEditor.setVisibility(View.GONE);
                    if (buttonCheckAnswer != null) buttonCheckAnswer.setVisibility(View.GONE);
                    if (buttonHint != null) buttonHint.setVisibility(View.GONE);
                    
                    // Show continue button for intro/complete
                    if (buttonSkip != null) {
                        buttonSkip.setText(step.type == LessonStepType.COMPLETE ? "🎉 Finish" : "Continue →");
                        buttonSkip.setVisibility(View.VISIBLE);
                    }
                } else {
                    cardCodeEditor.setVisibility(View.VISIBLE);
                    if (buttonCheckAnswer != null) buttonCheckAnswer.setVisibility(View.VISIBLE);
                    if (buttonHint != null) buttonHint.setVisibility(View.VISIBLE);
                    if (buttonSkip != null) buttonSkip.setVisibility(View.VISIBLE);
                    
                    if (editCode != null && step.codeSnippet != null) {
                        editCode.setText(step.codeSnippet);
                        editCode.setSelection(editCode.getText().length());
                    }
                }
            }
            
            // Hide feedback
            if (layoutFeedback != null) {
                layoutFeedback.setVisibility(View.GONE);
            }
        });
        
        // Play step transition sound
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void checkAnswer() {
        if (currentStep >= lessonSteps.size()) return;
        
        LessonStep step = lessonSteps.get(currentStep);
        if (step.expectedAnswer == null) {
            nextStep();
            return;
        }
        
        String userAnswer = editCode != null ? editCode.getText().toString() : "";
        boolean isCorrect = userAnswer.toLowerCase().contains(step.expectedAnswer.toLowerCase());
        
        if (isCorrect) {
            // Correct answer!
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            showFeedback(true, CORRECT_MESSAGES[random.nextInt(CORRECT_MESSAGES.length)]);
            showMascotMessage(CORRECT_MESSAGES[random.nextInt(CORRECT_MESSAGES.length)]);
            
            // Celebration animation
            playConfettiAnimation();
            animateMascotCelebration();
            
            // Show XP reward
            int xpEarned = calculateXP();
            showXPReward(xpEarned);
            
            // Move to next step after delay
            handler.postDelayed(this::nextStep, 2000);
        } else {
            // Wrong answer
            soundManager.playSound(SoundManager.Sound.ERROR);
            showFeedback(false, WRONG_MESSAGES[random.nextInt(WRONG_MESSAGES.length)]);
            animateMascotSad();
            
            // Shake the code editor
            shakeView(cardCodeEditor);
        }
    }
    
    private void showHint() {
        if (currentStep >= lessonSteps.size()) return;
        
        LessonStep step = lessonSteps.get(currentStep);
        if (hintsUsed >= maxHints) {
            showMascotMessage("No more hints available! You can do it! 💪");
            return;
        }
        
        hintsUsed++;
        String hintMessage = HINT_MESSAGES[random.nextInt(HINT_MESSAGES.length)];
        
        // Progressive hints
        String hint;
        if (step.expectedAnswer != null) {
            switch (hintsUsed) {
                case 1:
                    hint = "Look for a spelling or syntax error...";
                    break;
                case 2:
                    hint = "The answer starts with: " + step.expectedAnswer.charAt(0) + "...";
                    break;
                case 3:
                    hint = "The answer is: " + step.expectedAnswer;
                    break;
                default:
                    hint = "Keep trying!";
            }
        } else {
            hint = "Just continue to the next step!";
        }
        
        showMascotMessage(hintMessage + "\n\n" + hint);
        
        // Update hint button
        if (buttonHint != null) {
            buttonHint.setText("💡 Hint (" + (maxHints - hintsUsed) + " left)");
        }
    }
    
    private void skipStep() {
        if (currentStep >= lessonSteps.size() - 1) {
            completeLessonSequence();
        } else {
            nextStep();
        }
    }
    
    private void nextStep() {
        showStep(currentStep + 1);
    }
    
    private int calculateXP() {
        int baseXP = 25;
        // Bonus for not using hints
        int bonus = (maxHints - hintsUsed) * 5;
        return baseXP + bonus;
    }
    
    private void showFeedback(boolean isCorrect, String message) {
        if (layoutFeedback == null || textFeedback == null) return;
        
        textFeedback.setText(message);
        layoutFeedback.setBackgroundResource(isCorrect ? 
            R.drawable.bg_feedback_correct : R.drawable.bg_feedback_wrong);
        
        layoutFeedback.setAlpha(0f);
        layoutFeedback.setVisibility(View.VISIBLE);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(layoutFeedback, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.start();
    }
    
    private void showMascotMessage(String message) {
        if (textMascotSpeech == null || layoutMascotBubble == null) return;
        
        // Animate speech bubble
        layoutMascotBubble.setAlpha(0f);
        layoutMascotBubble.setScaleX(0.8f);
        layoutMascotBubble.setScaleY(0.8f);
        layoutMascotBubble.setVisibility(View.VISIBLE);
        
        textMascotSpeech.setText(message);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(layoutMascotBubble, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(layoutMascotBubble, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(layoutMascotBubble, "scaleY", 0.8f, 1f);
        
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(fadeIn, scaleX, scaleY);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator(1.5f));
        anim.start();
        
        // Animate mascot bounce
        animateMascotBounce();
    }
    
    private void animateMascotBounce() {
        if (imageMascot == null) return;
        
        ObjectAnimator bounce = ObjectAnimator.ofPropertyValuesHolder(imageMascot,
            PropertyValuesHolder.ofFloat("translationY", 0f, -10f, 0f));
        bounce.setDuration(400);
        bounce.setInterpolator(new BounceInterpolator());
        bounce.start();
    }
    
    private void animateMascotCelebration() {
        if (imageMascot == null) return;
        
        ObjectAnimator celebration = ObjectAnimator.ofPropertyValuesHolder(imageMascot,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f),
            PropertyValuesHolder.ofFloat("rotation", 0f, 10f, -10f, 0f));
        celebration.setDuration(600);
        celebration.setInterpolator(new OvershootInterpolator(2f));
        celebration.start();
    }
    
    private void animateMascotSad() {
        if (imageMascot == null) return;
        
        ObjectAnimator sad = ObjectAnimator.ofPropertyValuesHolder(imageMascot,
            PropertyValuesHolder.ofFloat("rotation", 0f, -5f, 5f, 0f));
        sad.setDuration(300);
        sad.start();
    }
    
    private void shakeView(View view) {
        if (view == null) return;
        
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 10f, -10f, 5f, -5f, 0f);
        shake.setDuration(400);
        shake.start();
    }
    
    private void animateProgressBar(int progress) {
        if (progressLesson == null) return;
        
        ValueAnimator animator = ValueAnimator.ofInt(progressLesson.getProgress(), progress);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progressLesson.setProgress((int) animation.getAnimatedValue());
        });
        animator.start();
    }
    
    private void animateStepTransition(Runnable onComplete) {
        // Fade out current content
        View contentArea = rootView.findViewById(R.id.layout_content);
        if (contentArea == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(contentArea, "alpha", 1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) onComplete.run();
                
                // Fade in new content
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(contentArea, "alpha", 0f, 1f);
                fadeIn.setDuration(300);
                fadeIn.start();
            }
        });
        fadeOut.start();
    }
    
    private void showXPReward(int xp) {
        if (xpRewardOverlay == null || textXpReward == null) return;
        
        textXpReward.setText("+" + xp + " XP");
        xpRewardOverlay.setAlpha(0f);
        xpRewardOverlay.setScaleX(0.5f);
        xpRewardOverlay.setScaleY(0.5f);
        xpRewardOverlay.setVisibility(View.VISIBLE);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(xpRewardOverlay, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(xpRewardOverlay, "scaleX", 0.5f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(xpRewardOverlay, "scaleY", 0.5f, 1.2f, 1f);
        
        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(fadeIn, scaleX, scaleY);
        entrance.setDuration(500);
        entrance.setInterpolator(new OvershootInterpolator(2f));
        entrance.start();
        
        // Hide after delay
        handler.postDelayed(() -> {
            if (xpRewardOverlay != null) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(xpRewardOverlay, "alpha", 1f, 0f);
                fadeOut.setDuration(300);
                fadeOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        xpRewardOverlay.setVisibility(View.GONE);
                    }
                });
                fadeOut.start();
            }
        }, 1500);
    }
    
    private void playConfettiAnimation() {
        if (confettiContainer == null) return;
        
        confettiContainer.setVisibility(View.VISIBLE);
        
        int[] colors = {
            Color.parseColor("#FFD54F"),
            Color.parseColor("#FF6B35"),
            Color.parseColor("#7C4DFF"),
            Color.parseColor("#00E676"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#06B6D4")
        };
        
        int containerWidth = confettiContainer.getWidth() > 0 ? confettiContainer.getWidth() : 1080;
        int containerHeight = confettiContainer.getHeight() > 0 ? confettiContainer.getHeight() : 500;
        
        for (int i = 0; i < 30; i++) {
            View particle = new View(requireContext());
            int size = 8 + random.nextInt(12);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = random.nextInt(containerWidth);
            params.topMargin = -20;
            particle.setLayoutParams(params);
            particle.setBackgroundColor(colors[random.nextInt(colors.length)]);
            particle.setRotation(random.nextFloat() * 360);
            
            confettiContainer.addView(particle);
            confettiParticles.add(particle);
            
            // Animate falling
            float endX = (random.nextFloat() - 0.5f) * 200;
            ObjectAnimator fall = ObjectAnimator.ofFloat(particle, "translationY", 0f, containerHeight + 50);
            ObjectAnimator drift = ObjectAnimator.ofFloat(particle, "translationX", 0f, endX);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(particle, "rotation", 0f, 360f * (random.nextBoolean() ? 1 : -1));
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f);
            
            int duration = 1500 + random.nextInt(1000);
            fall.setDuration(duration);
            drift.setDuration(duration);
            rotate.setDuration(duration);
            fadeOut.setDuration(duration);
            fadeOut.setStartDelay(duration - 300);
            
            int delay = i * 30;
            fall.setStartDelay(delay);
            drift.setStartDelay(delay);
            rotate.setStartDelay(delay);
            
            AnimatorSet particleAnim = new AnimatorSet();
            particleAnim.playTogether(fall, drift, rotate, fadeOut);
            particleAnim.start();
        }
        
        // Clean up confetti after animation
        handler.postDelayed(() -> {
            for (View particle : confettiParticles) {
                confettiContainer.removeView(particle);
            }
            confettiParticles.clear();
        }, 3000);
    }
    
    private void playEntranceAnimations() {
        // Animate header
        View header = rootView.findViewById(R.id.layout_header);
        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-30f);
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(header, "alpha", 0f, 1f);
            ObjectAnimator slideDown = ObjectAnimator.ofFloat(header, "translationY", -30f, 0f);
            
            AnimatorSet anim = new AnimatorSet();
            anim.playTogether(fadeIn, slideDown);
            anim.setDuration(400);
            anim.setInterpolator(new DecelerateInterpolator(2f));
            anim.start();
        }
        
        // Animate mascot
        if (imageMascot != null) {
            imageMascot.setAlpha(0f);
            imageMascot.setScaleX(0f);
            imageMascot.setScaleY(0f);
            
            handler.postDelayed(() -> {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imageMascot, "alpha", 0f, 1f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageMascot, "scaleX", 0f, 1.1f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageMascot, "scaleY", 0f, 1.1f, 1f);
                
                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(fadeIn, scaleX, scaleY);
                anim.setDuration(500);
                anim.setInterpolator(new OvershootInterpolator(2f));
                anim.start();
            }, 300);
        }
        
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void completeLessonSequence() {
        if (lessonComplete) return;
        lessonComplete = true;
        
        // Play completion sound
        soundManager.playSound(SoundManager.Sound.LEVEL_UP);
        
        // Show celebration
        playConfettiAnimation();
        animateMascotCelebration();
        
        // Show completion dialog
        handler.postDelayed(() -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("🎉 Lesson Complete!")
                .setMessage("Congratulations! You've mastered the basics of debugging!\n\n" +
                           "✅ XP Earned: 100\n" +
                           "🏆 Achievement Unlocked: First Lesson\n" +
                           "🔥 Streak: +1")
                .setPositiveButton("Continue", (dialog, which) -> {
                    soundManager.playButtonClick();
                    try {
                        Navigation.findNavController(requireView()).navigateUp();
                    } catch (Exception e) {
                        // Ignore navigation errors
                    }
                })
                .setCancelable(false)
                .show();
        }, 1000);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        confettiParticles.clear();
    }
    
    // Inner class for lesson steps
    enum LessonStepType {
        INTRO, SPOT_THE_BUG, FIX_THE_BUG, FILL_IN_BLANK, COMPLETE
    }
    
    static class LessonStep {
        String title;
        String instruction;
        String codeSnippet;
        String expectedAnswer;
        LessonStepType type;
        
        LessonStep(String title, String instruction, String codeSnippet, String expectedAnswer, LessonStepType type) {
            this.title = title;
            this.instruction = instruction;
            this.codeSnippet = codeSnippet;
            this.expectedAnswer = expectedAnswer;
            this.type = type;
        }
    }
}
