package com.example.debugappproject.ui.tutorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.debugappproject.R;
import com.example.debugappproject.ui.home.HomeFragment;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

/**
 * Redesigned Beginner Tutorial with 4 interactive lessons.
 *
 * Features:
 * - Swipeable ViewPager2 cards
 * - Live code execution in Lesson 3
 * - Smooth animations
 * - Full-screen experience (bottom nav hidden)
 */
public class BeginnerTutorialFragment extends Fragment
        implements TutorialPagerAdapter.OnLessonInteractionListener {

    private static final String PREFS_NAME = "tutorial_progress";
    private static final int NUM_LESSONS = 4;

    private SharedPreferences prefs;
    private SoundManager soundManager;

    // Views
    private ViewPager2 viewPagerLessons;
    private TutorialPagerAdapter adapter;
    private ProgressBar progressLesson;
    private TextView textLessonNumber;
    private MaterialButton buttonNext;
    private MaterialButton buttonBack;
    private View buttonClose;
    private LinearLayout layoutIndicators;
    private ImageView[] indicators;

    // State tracking
    private boolean[] lessonCompleted = new boolean[NUM_LESSONS];
    private boolean lesson3CodeFixed = false;

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
        setupViewPager();
        setupIndicators();
        setupButtons();

        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }

    private void findViews(View view) {
        viewPagerLessons = view.findViewById(R.id.viewpager_lessons);
        progressLesson = view.findViewById(R.id.progress_lesson);
        textLessonNumber = view.findViewById(R.id.text_lesson_number);
        buttonNext = view.findViewById(R.id.button_next);
        buttonBack = view.findViewById(R.id.button_back);
        buttonClose = view.findViewById(R.id.button_close);
        layoutIndicators = view.findViewById(R.id.layout_indicators);
    }

    private void setupViewPager() {
        adapter = new TutorialPagerAdapter(requireContext());
        adapter.setListener(this);
        viewPagerLessons.setAdapter(adapter);

        // Smooth page transformer with depth effect
        viewPagerLessons.setPageTransformer(new DepthPageTransformer());

        viewPagerLessons.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUI(position);
                soundManager.playSound(SoundManager.Sound.BLIP);
            }
        });
    }

    private void setupIndicators() {
        indicators = new ImageView[NUM_LESSONS];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < NUM_LESSONS; i++) {
            indicators[i] = new ImageView(requireContext());
            indicators[i].setImageResource(R.drawable.indicator_inactive);
            indicators[i].setLayoutParams(params);
            layoutIndicators.addView(indicators[i]);
        }

        if (indicators.length > 0) {
            indicators[0].setImageResource(R.drawable.indicator_active);
        }
    }

    private void updateUI(int position) {
        // Update progress
        textLessonNumber.setText(String.format("Lesson %d of %d", position + 1, NUM_LESSONS));
        progressLesson.setProgress(position + 1);

        // Update indicators
        for (int i = 0; i < NUM_LESSONS; i++) {
            indicators[i].setImageResource(
                i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive
            );
        }

        // Update buttons
        buttonBack.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);

        if (position == NUM_LESSONS - 1) {
            buttonNext.setText("\uD83C\uDF89 Finish");
        } else if (position == 2 && !lesson3CodeFixed) {
            // Lesson 3 requires code to be fixed first
            buttonNext.setText("Fix Code First");
            buttonNext.setEnabled(false);
            buttonNext.setAlpha(0.5f);
        } else {
            buttonNext.setText("Next \u2192");
            buttonNext.setEnabled(true);
            buttonNext.setAlpha(1f);
        }
    }

    private void setupButtons() {
        buttonNext.setOnClickListener(v -> {
            soundManager.playButtonClick();
            int current = viewPagerLessons.getCurrentItem();
            if (current < NUM_LESSONS - 1) {
                viewPagerLessons.setCurrentItem(current + 1, true);
            } else {
                completeTutorial();
            }
        });

        buttonBack.setOnClickListener(v -> {
            soundManager.playButtonClick();
            int current = viewPagerLessons.getCurrentItem();
            if (current > 0) {
                viewPagerLessons.setCurrentItem(current - 1, true);
            }
        });

        buttonClose.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showExitConfirmation();
        });
    }

    @Override
    public void onCodeExecutionComplete(int lessonIndex, boolean success) {
        if (success && lessonIndex == 2) {
            lesson3CodeFixed = true;
            lessonCompleted[2] = true;
            soundManager.playSound(SoundManager.Sound.SUCCESS);

            // Enable next button
            buttonNext.setText("Next \u2192");
            buttonNext.setEnabled(true);
            buttonNext.setAlpha(1f);
        }
    }

    @Override
    public void onLessonCompleted(int lessonIndex) {
        lessonCompleted[lessonIndex] = true;
    }

    private void completeTutorial() {
        soundManager.playSound(SoundManager.Sound.VICTORY);

        // Mark tutorial complete in both local prefs and HomeFragment's prefs
        prefs.edit()
            .putBoolean("tutorial_completed", true)
            .apply();

        // Also mark in HomeFragment prefs so auto-show doesn't trigger again
        HomeFragment.markTutorialCompleted(requireContext());

        new AlertDialog.Builder(requireContext())
            .setTitle("\uD83C\uDF89 Tutorial Complete!")
            .setMessage("Congratulations! You've learned the basics of debugging!\n\n" +
                       "\uD83C\uDFC6 +500 XP earned!\n" +
                       "\uD83C\uDF96 Beginner Debugger Badge unlocked!\n\n" +
                       "Ready to test your skills?")
            .setPositiveButton("\uD83C\uDFAF Try Quick Fix", (d, w) -> {
                Bundle args = new Bundle();
                args.putString("gameMode", "quick_fix");
                Navigation.findNavController(requireView())
                    .navigate(R.id.gameSessionFragment, args);
            })
            .setNegativeButton("\uD83C\uDFE0 Home", (d, w) -> {
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setCancelable(false)
            .show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Exit Tutorial?")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit", (d, w) -> {
                Navigation.findNavController(requireView()).navigateUp();
            })
            .setNegativeButton("Continue", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.shutdown();
        }
    }

    /**
     * Page transformer for smooth depth/zoom effect when swiping between lessons.
     */
    private static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            int pageWidth = page.getWidth();

            if (position < -1) {
                // Page is way off-screen to the left
                page.setAlpha(0f);
            } else if (position <= 0) {
                // Exiting page (sliding out to left)
                page.setAlpha(1f);
                page.setTranslationX(0f);
                page.setScaleX(1f);
                page.setScaleY(1f);
            } else if (position <= 1) {
                // Entering page (sliding in from right)
                page.setAlpha(1 - position);
                page.setTranslationX(pageWidth * -position);

                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            } else {
                // Page is way off-screen to the right
                page.setAlpha(0f);
            }
        }
    }
}
