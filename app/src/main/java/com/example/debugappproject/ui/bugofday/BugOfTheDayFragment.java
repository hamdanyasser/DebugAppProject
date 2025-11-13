package com.example.debugappproject.ui.bugofday;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.databinding.FragmentBugOfDayBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.DateUtils;

import java.util.Calendar;

/**
 * Bug of the Day Fragment - Displays today's featured debugging challenge.
 *
 * Features:
 * - Shows a daily bug challenge with details
 * - Displays user's current and longest streak
 * - Countdown timer for next bug availability
 * - Navigate to bug detail on button click
 */
public class BugOfTheDayFragment extends Fragment {

    private FragmentBugOfDayBinding binding;
    private BugOfTheDayViewModel viewModel;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBugOfDayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BugOfTheDayViewModel.class);

        setupObservers();
        startCountdownTimer();
    }

    /**
     * Sets up LiveData observers for bug data and user progress.
     */
    private void setupObservers() {
        // Observe today's bug
        viewModel.getTodaysBug().observe(getViewLifecycleOwner(), bug -> {
            if (bug != null) {
                displayBug(bug);
            }
        });

        // Observe user progress for streak data
        viewModel.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                displayStreakInfo(progress);
            }
        });
    }

    /**
     * Displays the bug challenge information in the UI.
     */
    private void displayBug(Bug bug) {
        binding.textBugTitle.setText(bug.getTitle());
        binding.textDescription.setText(bug.getDescription());
        binding.textCategory.setText(bug.getCategory());

        // Set difficulty with color coding
        String difficulty = bug.getDifficulty().toUpperCase();
        binding.textDifficulty.setText(difficulty);

        int difficultyColor;
        switch (bug.getDifficulty().toLowerCase()) {
            case "easy":
                difficultyColor = Color.parseColor("#4CAF50"); // Green
                break;
            case "medium":
                difficultyColor = Color.parseColor("#FF9800"); // Orange
                break;
            case "hard":
                difficultyColor = Color.parseColor("#F44336"); // Red
                break;
            default:
                difficultyColor = Color.parseColor("#9E9E9E"); // Gray
                break;
        }
        binding.textDifficulty.setTextColor(difficultyColor);

        // Handle start button click
        binding.buttonStart.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("bugId", bug.getId());
            Navigation.findNavController(v).navigate(
                R.id.action_bugOfDay_to_bugDetail,
                args
            );
        });

        // Check if already completed today
        checkIfCompleted(bug.getId());
    }

    /**
     * Checks if today's bug has been completed and updates button text.
     */
    private void checkIfCompleted(int bugId) {
        viewModel.getRepository().getExecutorService().execute(() -> {
            boolean isCompleted = viewModel.getRepository().getBugDao().isBugCompleted(bugId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isCompleted) {
                        binding.buttonStart.setText("Review Solution");
                        binding.buttonStart.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_view));
                    } else {
                        binding.buttonStart.setText("Start Debugging");
                        binding.buttonStart.setIcon(null);
                    }
                });
            }
        });
    }

    /**
     * Displays user streak information.
     */
    private void displayStreakInfo(UserProgress progress) {
        int currentStreak = DateUtils.calculateCurrentStreak(
            progress.getLastCompletionDate(),
            progress.getCurrentStreakDays()
        );

        binding.textCurrentStreak.setText(currentStreak + " day streak");
        binding.textLongestStreak.setText("Longest: " + progress.getLongestStreakDays() + " days");
    }

    /**
     * Starts the countdown timer for next bug availability.
     * Updates every second to show time remaining until midnight.
     */
    private void startCountdownTimer() {
        countdownHandler = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    /**
     * Updates the countdown text showing time until next bug.
     */
    private void updateCountdown() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_MONTH, 1);

        long diff = midnight.getTimeInMillis() - now.getTimeInMillis();
        long hours = (diff / (1000 * 60 * 60)) % 24;
        long minutes = (diff / (1000 * 60)) % 60;
        long seconds = (diff / 1000) % 60;

        String countdown = String.format("Next bug available in %02d:%02d:%02d", hours, minutes, seconds);

        if (binding != null) {
            binding.textNextBug.setText(countdown);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Stop countdown timer
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        binding = null;
    }
}
