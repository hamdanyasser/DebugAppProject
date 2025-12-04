package com.example.debugappproject.ui.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentOnboardingNewBinding;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ONBOARDING FRAGMENT                                  ║
 * ║              First-time user experience with animations                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class OnboardingFragment extends Fragment {

    private static final String PREFS_NAME = "DebugMasterPrefs";
    private static final String KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding";
    private static final int NUM_PAGES = 4;

    private FragmentOnboardingNewBinding binding;
    private OnboardingPagerAdapter adapter;
    private ImageView[] indicators;
    private SoundManager soundManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingNewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        soundManager = SoundManager.getInstance(requireContext());

        setupViewPager();
        setupIndicators();
        setupButtons();
        playEntranceAnimation();
    }

    private void setupViewPager() {
        adapter = new OnboardingPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                soundManager.playSound(SoundManager.Sound.BLIP);
                updateIndicators(position);
                updateButtons(position);
            }
        });
    }

    private void setupIndicators() {
        indicators = new ImageView[NUM_PAGES];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < NUM_PAGES; i++) {
            indicators[i] = new ImageView(requireContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    requireContext(), R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(params);
            binding.layoutIndicators.addView(indicators[i]);
        }

        if (indicators.length > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(
                    requireContext(), R.drawable.indicator_active
            ));
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < NUM_PAGES; i++) {
            if (i == position) {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(
                        requireContext(), R.drawable.indicator_active
                ));
                // Animate active indicator
                indicators[i].animate()
                        .scaleX(1.3f).scaleY(1.3f)
                        .setDuration(200)
                        .start();
            } else {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(
                        requireContext(), R.drawable.indicator_inactive
                ));
                indicators[i].animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .start();
            }
        }
    }

    private void updateButtons(int position) {
        if (position == NUM_PAGES - 1) {
            binding.buttonNext.setText("🚀 Get Started");
            binding.buttonSkip.setVisibility(View.GONE);
        } else {
            binding.buttonNext.setText("Next →");
            binding.buttonSkip.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        binding.buttonNext.setOnClickListener(v -> {
            soundManager.playButtonClick();
            int currentItem = binding.viewPager.getCurrentItem();
            if (currentItem < NUM_PAGES - 1) {
                binding.viewPager.setCurrentItem(currentItem + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.buttonSkip.setOnClickListener(v -> {
            soundManager.playButtonClick();
            finishOnboarding();
        });
    }

    private void playEntranceAnimation() {
        binding.viewPager.setAlpha(0f);
        binding.viewPager.setTranslationY(50f);
        binding.viewPager.animate()
                .alpha(1f).translationY(0f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1f))
                .start();

        binding.layoutIndicators.setAlpha(0f);
        binding.layoutIndicators.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(200)
                .start();

        binding.buttonNext.setAlpha(0f);
        binding.buttonNext.setTranslationY(30f);
        binding.buttonNext.animate()
                .alpha(1f).translationY(0f)
                .setDuration(400)
                .setStartDelay(300)
                .start();
    }

    private void finishOnboarding() {
        // Save flag
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply();

        // Play completion sound
        soundManager.playSound(SoundManager.Sound.LEVEL_UP);

        // Navigate to auth
        try {
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_onboarding_to_auth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
