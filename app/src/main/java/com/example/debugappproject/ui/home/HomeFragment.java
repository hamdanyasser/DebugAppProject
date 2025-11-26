package com.example.debugappproject.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.Calendar;

/**
 * HomeFragment - The main dashboard of DebugMaster!
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BillingManager billingManager;

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

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        billingManager = new BillingManager(requireContext());

        setupGreeting();
        setupObservers();
        setupClickListeners();
        
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), this::updateProStatus);
    }

    private void setupGreeting() {
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
                binding.progressXp.setProgress(xpInLevel);
            }

            if (binding.textStreakDays != null) {
                int streak = progress.getStreakDays();
                binding.textStreakDays.setText(String.valueOf(streak));
            }

            if (binding.textSolvedCount != null) {
                binding.textSolvedCount.setText(String.valueOf(progress.getTotalSolved()));
            }

            // Update level title
            if (binding.textLevelTitle != null) {
                binding.textLevelTitle.setText(getTitleForLevel(level));
            }
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error updating user stats", e);
        }
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
        if (binding.cardProUpsell != null) {
            binding.cardProUpsell.setVisibility(isPro ? View.GONE : View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        // Daily Challenge
        if (binding.cardDailyChallenge != null) {
            binding.cardDailyChallenge.setOnClickListener(v -> 
                navigateToDestination(R.id.action_home_to_bugDetail, "Daily Challenge"));
        }

        if (binding.buttonSolveNow != null) {
            binding.buttonSolveNow.setOnClickListener(v -> 
                navigateToDestination(R.id.action_home_to_bugDetail, "Daily Challenge"));
        }

        // Learning Paths
        if (binding.textViewAllPaths != null) {
            binding.textViewAllPaths.setOnClickListener(v -> 
                navigateToDestination(R.id.action_home_to_learn, "Learning Paths"));
        }

        // Practice Mode
        if (binding.cardPracticeMode != null) {
            binding.cardPracticeMode.setOnClickListener(v -> 
                navigateToDestination(R.id.action_home_to_practice, "Practice Mode"));
        }

        // Battle Arena
        if (binding.cardBattleArena != null) {
            binding.cardBattleArena.setOnClickListener(v -> {
                // Allow access in demo mode regardless of Pro status
                // In production, you might want to restrict this
                navigateToDestination(R.id.action_home_to_battle, "Battle Arena");
            });
        }

        // Pro Upgrade
        if (binding.cardProUpsell != null) {
            binding.cardProUpsell.setOnClickListener(v -> navigateToPremium());
        }

        if (binding.buttonUpgrade != null) {
            binding.buttonUpgrade.setOnClickListener(v -> navigateToPremium());
        }
    }

    private void navigateToDestination(int actionId, String destinationName) {
        try {
            if (getView() == null) return;
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(actionId);
        } catch (IllegalArgumentException e) {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.destroy();
        }
        binding = null;
    }
}
