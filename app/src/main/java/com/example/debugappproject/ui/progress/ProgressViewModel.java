package com.example.debugappproject.ui.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;

import java.util.List;

/**
 * ViewModel for ProgressFragment.
 * Manages user progress statistics and completed bugs.
 */
public class ProgressViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<UserProgress> userProgress;
    private final LiveData<List<Bug>> completedBugs;
    private final LiveData<List<Bug>> allBugs;

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        userProgress = repository.getUserProgress();
        completedBugs = repository.getCompletedBugs();
        allBugs = repository.getAllBugs();
    }

    public LiveData<UserProgress> getUserProgress() {
        return userProgress;
    }

    public LiveData<List<Bug>> getCompletedBugs() {
        return completedBugs;
    }

    public LiveData<List<Bug>> getAllBugs() {
        return allBugs;
    }

    /**
     * Reset all progress.
     */
    public void resetProgress() {
        repository.resetProgress();
    }
}
