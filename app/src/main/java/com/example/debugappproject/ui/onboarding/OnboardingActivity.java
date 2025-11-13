package com.example.debugappproject.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.debugappproject.MainActivity;
import com.example.debugappproject.R;
import com.example.debugappproject.databinding.ActivityOnboardingBinding;
import com.google.android.material.button.MaterialButton;

/**
 * OnboardingActivity - First-run experience introducing app features.
 *
 * Shows 4 screens explaining:
 * 1. Fix Real Java Bugs
 * 2. Learn Through Lessons
 * 3. Level Up & Earn Achievements
 * 4. Daily Practice
 *
 * After completion, sets a flag in SharedPreferences to skip on future launches.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DebugMasterPrefs";
    private static final String KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding";
    private static final int NUM_PAGES = 4;

    private ActivityOnboardingBinding binding;
    private OnboardingPagerAdapter adapter;
    private ImageView[] indicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupIndicators();
        setupButtons();
    }

    /**
     * Sets up the ViewPager2 with onboarding screens.
     */
    private void setupViewPager() {
        adapter = new OnboardingPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                updateButtons(position);
            }
        });
    }

    /**
     * Sets up page indicators (dots).
     */
    private void setupIndicators() {
        indicators = new ImageView[NUM_PAGES];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < NUM_PAGES; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                this, R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(params);
            binding.layoutIndicators.addView(indicators[i]);
        }

        // Set first indicator as active
        if (indicators.length > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(
                this, R.drawable.indicator_active
            ));
        }
    }

    /**
     * Updates indicator dots based on current page.
     */
    private void updateIndicators(int position) {
        for (int i = 0; i < NUM_PAGES; i++) {
            if (i == position) {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_active
                ));
            } else {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_inactive
                ));
            }
        }
    }

    /**
     * Updates button text based on current page.
     */
    private void updateButtons(int position) {
        if (position == NUM_PAGES - 1) {
            // Last page - show "Get Started"
            binding.buttonNext.setText("Get Started");
            binding.buttonSkip.setVisibility(View.GONE);
        } else {
            // Other pages - show "Next"
            binding.buttonNext.setText("Next");
            binding.buttonSkip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets up click listeners for buttons.
     */
    private void setupButtons() {
        binding.buttonNext.setOnClickListener(v -> {
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem < NUM_PAGES - 1) {
                // Go to next page
                binding.viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Last page - finish onboarding
                finishOnboarding();
            }
        });

        binding.buttonSkip.setOnClickListener(v -> {
            finishOnboarding();
        });
    }

    /**
     * Marks onboarding as completed and launches main activity.
     */
    private void finishOnboarding() {
        // Save flag to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, true)
            .apply();

        // Launch MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Checks if user has already seen onboarding.
     */
    public static boolean hasSeenOnboarding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
