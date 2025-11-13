package com.example.debugappproject.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.data.seeding.DatabaseSeeder;
import com.example.debugappproject.databinding.FragmentSplashBinding;
import com.example.debugappproject.ui.onboarding.OnboardingActivity;
import com.example.debugappproject.util.Constants;

/**
 * SplashFragment - Animated welcome screen for DebugMaster app.
 *
 * Displays:
 * - App icon (bug emoji)
 * - App name with fade-in animation
 * - Tagline with staggered fade-in
 *
 * Also handles:
 * - Database seeding on first launch (background thread)
 * - Auto-navigation to HomeFragment after animation completes
 *
 * Animation sequence:
 * 1. Icon fades in and scales (300ms)
 * 2. App name fades in (300ms, delayed 100ms)
 * 3. Tagline fades in (300ms, delayed 200ms)
 * 4. Navigate to home after total delay
 */
public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;
    private Handler navigationHandler;
    private Runnable navigationRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Seed database in background
        seedDatabase();

        // Start entrance animations
        animateEntrance();

        // Schedule navigation to home
        scheduleNavigation(view);
    }

    /**
     * Seeds the database with initial bug data on background thread.
     * The seeding is now fully synchronous, so it completes before returning.
     */
    private void seedDatabase() {
        new Thread(() -> {
            try {
                BugRepository repository = new BugRepository(requireActivity().getApplication());
                // This call now blocks until all data is inserted
                DatabaseSeeder.seedDatabase(requireContext(), repository);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Animates the splash screen elements with staggered fade-in and scale.
     */
    private void animateEntrance() {
        // Animate icon: fade in + scale
        binding.textIcon.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Check if binding is still valid before animating
                        if (binding != null && isAdded()) {
                            // Slight bounce back
                            binding.textIcon.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start();
                        }
                    }
                })
                .start();

        // Animate app name: fade in with delay
        binding.textAppName.animate()
                .alpha(1f)
                .translationY(0)
                .setStartDelay(100)
                .setDuration(300)
                .start();

        // Animate tagline: fade in with longer delay
        binding.textTagline.animate()
                .alpha(0.9f)
                .translationY(0)
                .setStartDelay(200)
                .setDuration(300)
                .start();
    }

    /**
     * Schedules navigation to Onboarding or Learning Paths screen after splash delay.
     * Checks if user has seen onboarding before.
     * Uses Handler to avoid memory leaks.
     */
    private void scheduleNavigation(View view) {
        navigationHandler = new Handler(Looper.getMainLooper());
        navigationRunnable = () -> {
            if (isAdded() && view != null) {
                if (OnboardingActivity.hasSeenOnboarding(requireContext())) {
                    // User has seen onboarding - go to main app
                    Navigation.findNavController(view).navigate(
                        R.id.action_splash_to_learn
                    );
                } else {
                    // First launch - show onboarding
                    Intent intent = new Intent(requireActivity(), OnboardingActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        };
        navigationHandler.postDelayed(navigationRunnable, Constants.SPLASH_DELAY_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancel pending navigation to avoid memory leaks
        if (navigationHandler != null && navigationRunnable != null) {
            navigationHandler.removeCallbacks(navigationRunnable);
        }

        // Cancel any running animations to prevent callbacks from accessing null binding
        if (binding != null) {
            binding.textIcon.animate().cancel();
            binding.textAppName.animate().cancel();
            binding.textTagline.animate().cancel();
        }

        binding = null;
    }
}
