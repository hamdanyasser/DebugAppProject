package com.example.debugappproject.ui.bugofday;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.DateUtils;

/**
 * ViewModel for Bug of the Day screen.
 * Provides today's bug challenge and user streak data.
 */
public class BugOfTheDayViewModel extends AndroidViewModel {
    private final BugRepository repository;

    public BugOfTheDayViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
    }

    /**
     * Gets today's bug challenge based on the date.
     * Uses DateUtils to calculate which bug ID should be shown today.
     */
    public LiveData<Bug> getTodaysBug() {
        int bugId = DateUtils.getBugOfTheDayId();
        return repository.getBugById(bugId);
    }

    /**
     * Gets the user's progress data including streak information.
     */
    public LiveData<UserProgress> getUserProgress() {
        return repository.getUserProgress();
    }

    /**
     * Gets the BugRepository instance for checking if today's bug is completed.
     */
    public BugRepository getRepository() {
        return repository;
    }
}
