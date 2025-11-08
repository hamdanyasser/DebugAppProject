package com.example.debugappproject.ui.bugdetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;

import java.util.List;

/**
 * ViewModel for BugDetailFragment.
 * Manages bug details, hints, and completion logic.
 */
public class BugDetailViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private LiveData<Bug> currentBug;
    private LiveData<List<Hint>> hints;
    private final MutableLiveData<Integer> currentHintLevel;
    private final MutableLiveData<Boolean> showingSolution;

    public BugDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        currentHintLevel = new MutableLiveData<>(0);
        showingSolution = new MutableLiveData<>(false);
    }

    /**
     * Load bug by ID.
     */
    public void loadBug(int bugId) {
        currentBug = repository.getBugById(bugId);
        hints = repository.getHintsForBug(bugId);
    }

    public LiveData<Bug> getCurrentBug() {
        return currentBug;
    }

    public LiveData<List<Hint>> getHints() {
        return hints;
    }

    public LiveData<Integer> getCurrentHintLevel() {
        return currentHintLevel;
    }

    public LiveData<Boolean> isShowingSolution() {
        return showingSolution;
    }

    /**
     * Reveal next hint (increment hint level).
     */
    public void revealNextHint() {
        Integer current = currentHintLevel.getValue();
        if (current != null) {
            currentHintLevel.setValue(current + 1);
        }
    }

    /**
     * Reset hints to hidden.
     */
    public void resetHints() {
        currentHintLevel.setValue(0);
    }

    /**
     * Show solution.
     */
    public void showSolution() {
        showingSolution.setValue(true);
    }

    /**
     * Mark current bug as completed.
     */
    public void markBugAsCompleted(int bugId, String difficulty) {
        repository.markBugAsCompleted(bugId, difficulty);
    }
}
