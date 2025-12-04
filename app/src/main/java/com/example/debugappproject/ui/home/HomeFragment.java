package com.example.debugappproject.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentHomeBinding;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;

import java.util.Calendar;

/**
 * Home Fragment - Main dashboard with game modes and stats
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BillingManager billingManager;
    private SoundManager soundManager;
    private AuthManager authManager;
    private Handler animationHandler;

    private static final String[] MORNING_GREETINGS = {
        "Good morning! Ready to squash some bugs? 🌅",
        "Rise and debug! ☀️",
        "Early bird catches the bug! 🐦"
    };
    private static final String[] AFTERNOON_GREETINGS = {
        "Good afternoon! Keep the momentum going! 💪",
        "Debugging time! Let's go! 🚀",
        "Afternoon bug hunt begins! 🎯"
    };
    private static final String[] EVENING_GREETINGS = {
        "Good evening! Perfect time for coding! 🌙",
        "Night owl debugging session! 🦉",
        "Evening bug patrol! ⭐"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.util.Log.d("HomeFragment", "onViewCreated: Initializing");
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        authManager = AuthManager.getInstance(requireContext());
        animationHandler = new Handler(Looper.getMainLooper());

        // Play entrance sound
        soundManager.playSound(SoundManager.Sound.TRANSITION);
        
        setupGreeting();
        setupObservers();
        setupClickListeners();
        startEntranceAnimations();

        billingManager.getIsProUser().observe(getViewLifecycleOwner(), this::updateProStatus);
    }

    private void startEntranceAnimations() {
        if (binding.cardXpProgress != null) {
            binding.cardXpProgress.setAlpha(0f);
            binding.cardXpProgress.setTranslationY(-30f);
            
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.cardXpProgress, "alpha", 0f, 1f);
            ObjectAnimator slideDown = ObjectAnimator.ofFloat(binding.cardXpProgress, "translationY", -30f, 0f);
            
            AnimatorSet heroAnim = new AnimatorSet();
            heroAnim.playTogether(fadeIn, slideDown);
            heroAnim.setDuration(500);
            heroAnim.setInterpolator(new DecelerateInterpolator(2f));
            heroAnim.start();
        }

        if (binding.cardDailyChallenge != null) {
            binding.cardDailyChallenge.setAlpha(0f);
            binding.cardDailyChallenge.setScaleX(0.9f);
            binding.cardDailyChallenge.setScaleY(0.9f);
            
            animationHandler.postDelayed(() -> {
                if (binding == null || !isAdded()) return;
                
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.cardDailyChallenge, "alpha", 0f, 1f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.cardDailyChallenge, "scaleX", 0.9f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.cardDailyChallenge, "scaleY", 0.9f, 1f);
                
                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(fadeIn, scaleX, scaleY);
                anim.setDuration(400);
                anim.setInterpolator(new OvershootInterpolator(1.2f));
                anim.start();
            }, 200);
        }

        animateQuickActionCards();
        
        if (binding.cardProUpsell != null && binding.cardProUpsell.getVisibility() == View.VISIBLE) {
            binding.cardProUpsell.setAlpha(0f);
            animationHandler.postDelayed(() -> {
                if (binding == null || !isAdded()) return;
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.cardProUpsell, "alpha", 0f, 1f);
                fadeIn.setDuration(500);
                fadeIn.start();
            }, 600);
        }
    }

    private void animateQuickActionCards() {
        View[] cards = {binding.cardPracticeMode, binding.cardBattleArena};
        
        if (binding.cardGameModes != null) {
            cards = new View[]{binding.cardGameModes, binding.cardPracticeMode, binding.cardBattleArena};
        }
        
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;
            
            card.setAlpha(0f);
            card.setTranslationY(30f);
            
            final int delay = 350 + (i * 100);
            animationHandler.postDelayed(() -> {
                if (binding == null || !isAdded()) return;
                
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(card, "translationY", 30f, 0f);
                
                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(fadeIn, slideUp);
                anim.setDuration(400);
                anim.setInterpolator(new DecelerateInterpolator(1.5f));
                anim.start();
            }, delay);
        }
    }

    private void setupGreeting() {
        if (binding.textUserName != null) {
            String displayName = authManager.getDisplayName();
            binding.textUserName.setText("Hey, " + displayName + "! ");
        }
        
        if (binding.textGreeting != null) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String[] greetings;
            
            if (hour < 12) {
                greetings = MORNING_GREETINGS;
            } else if (hour < 18) {
                greetings = AFTERNOON_GREETINGS;
            } else {
                greetings = EVENING_GREETINGS;
            }
            
            int randomIndex = (int) (Math.random() * greetings.length);
            binding.textGreeting.setText(greetings[randomIndex]);
        }
    }

    private void setupObservers() {
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                updateUserStats(progress);
            }
        });

        viewModel.getDailyChallenge().observe(getViewLifecycleOwner(), bug -> {
            if (bug != null && binding.textBugOfDayTitle != null) {
                binding.textBugOfDayTitle.setText(bug.getTitle());
            }
        });
    }

    private void updateUserStats(UserProgress progress) {
        try {
            int level = progress.getLevel();
            int xpInLevel = progress.getXpProgressInLevel();
            int xpToNextLevel = 100;

            if (binding.textLevelNumber != null) {
                binding.textLevelNumber.setText(String.valueOf(level));
            }

            if (binding.textXpCurrent != null) {
                binding.textXpCurrent.setText(progress.getTotalXp() + " XP");
            }

            if (binding.textXpRemaining != null) {
                int remaining = xpToNextLevel - xpInLevel;
                binding.textXpRemaining.setText(remaining + " to level up");
            }

            if (binding.progressXp != null) {
                binding.progressXp.setMax(xpToNextLevel);
                animateProgress(binding.progressXp, xpInLevel);
            }

            if (binding.textStreakDays != null) {
                int streak = progress.getStreakDays();
                binding.textStreakDays.setText(String.valueOf(streak));
                if (streak > 0) {
                    animateStat(binding.textStreakDays);
                }
            }

            if (binding.textSolvedCount != null) {
                binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
            }

            if (binding.textLevelTitle != null) {
                binding.textLevelTitle.setText(getTitleForLevel(level));
            }
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error updating user stats", e);
        }
    }

    private void animateProgress(android.widget.ProgressBar progressBar, int targetProgress) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetProgress);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });
        animator.start();
    }

    private void animateStat(View view) {
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f));
        pulse.setDuration(500);
        pulse.setInterpolator(new OvershootInterpolator(2f));
        pulse.start();
    }

    private String getTitleForLevel(int level) {
        if (level < 5) return "Novice Debugger";
        if (level < 10) return "Bug Hunter";
        if (level < 20) return "Code Inspector";
        if (level < 35) return "Debug Expert";
        if (level < 50) return "Debug Master";
        if (level < 75) return "Debug Legend";
        return "Debug God";
    }

    private void updateProStatus(boolean isPro) {
        android.util.Log.d(TAG, "updateProStatus: isPro=" + isPro);
        
        if (binding.cardProUpsell != null) {
            if (isPro) {
                binding.cardProUpsell.setVisibility(View.GONE);
            } else {
                binding.cardProUpsell.setVisibility(View.VISIBLE);
            }
        }
        
        if (binding.textGreeting != null && isPro) {
            String currentGreeting = binding.textGreeting.getText().toString();
            if (!currentGreeting.contains("👑")) {
                binding.textGreeting.setText("👑 " + currentGreeting);
            }
        }
        
        if (binding.textLevelTitle != null && isPro) {
            String currentTitle = binding.textLevelTitle.getText().toString();
            if (!currentTitle.contains("Pro")) {
                binding.textLevelTitle.setText(currentTitle + " • Pro");
            }
        }
    }

    private void setupClickListeners() {
        if (binding.cardDailyChallenge != null) {
            binding.cardDailyChallenge.setOnClickListener(v -> {
                animateCardPress(v);
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToDestination(R.id.action_home_to_bugDetail, "Daily Challenge");
            });
        }

        if (binding.buttonSolveNow != null) {
            binding.buttonSolveNow.setOnClickListener(v -> {
                animateButtonPress(v);
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                navigateToDestination(R.id.action_home_to_bugDetail, "Daily Challenge");
            });
        }

        if (binding.textViewAllPaths != null) {
            binding.textViewAllPaths.setOnClickListener(v -> {
                soundManager.playButtonClick();
                navigateToDestination(R.id.action_home_to_learn, "Learning Paths");
            });
        }

        if (binding.cardGameModes != null) {
            binding.cardGameModes.setOnClickListener(v -> {
                animateCardPress(v);
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                navigateToDestination(R.id.action_home_to_gameModes, "Game Modes");
            });
        }

        if (binding.cardPracticeMode != null) {
            binding.cardPracticeMode.setOnClickListener(v -> {
                animateCardPress(v);
                soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
                navigateToDestination(R.id.action_home_to_practice, "Practice Mode");
            });
        }

        if (binding.cardBattleArena != null) {
            binding.cardBattleArena.setOnClickListener(v -> {
                animateCardPress(v);
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToDestination(R.id.action_home_to_battle, "Battle Arena");
            });
        }

        if (binding.cardProUpsell != null) {
            binding.cardProUpsell.setOnClickListener(v -> {
                animateCardPress(v);
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                navigateToPremium();
            });
        }

        if (binding.buttonUpgrade != null) {
            binding.buttonUpgrade.setOnClickListener(v -> {
                animateButtonPress(v);
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                navigateToPremium();
            });
        }

        if (binding.layoutCoins != null) {
            binding.layoutCoins.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
                animateCoinCollect(v);
            });
        }
    }

    private void animateCardPress(View card) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f);
        
        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleDownX, scaleDownY);
        pressDown.setDuration(80);
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 0.95f, 1f);
        
        AnimatorSet release = new AnimatorSet();
        release.playTogether(scaleUpX, scaleUpY);
        release.setDuration(150);
        release.setInterpolator(new OvershootInterpolator(2f));
        
        AnimatorSet fullAnim = new AnimatorSet();
        fullAnim.playSequentially(pressDown, release);
        fullAnim.start();
    }

    private void animateButtonPress(View button) {
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(button,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0.9f, 1.05f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0.9f, 1.05f, 1f));
        pulse.setDuration(250);
        pulse.start();
    }

    private void animateCoinCollect(View view) {
        ObjectAnimator bounce = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f),
                PropertyValuesHolder.ofFloat("rotation", 0f, 15f, -15f, 0f));
        bounce.setDuration(400);
        bounce.setInterpolator(new OvershootInterpolator(2f));
        bounce.start();
    }

    private void navigateToDestination(int actionId, String destinationName) {
        try {
            if (getView() == null) return;
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(actionId);
        } catch (IllegalArgumentException e) {
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
            showToast("Coming soon: " + destinationName + " 🚀");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation error", e);
        }
    }

    private void navigateToPremium() {
        try {
            if (getView() == null) return;
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_home_to_subscription);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Navigation to premium failed", e);
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
            showToast("✨ Pro features coming soon!");
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (billingManager != null) {
            billingManager.refreshPurchases();
        }
        if (soundManager != null) {
            soundManager.resumeAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pauseAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        if (billingManager != null) {
            billingManager.clearCallback();
        }
        binding = null;
    }
}
