package com.example.debugappproject.ui.bugofday;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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
    private final MutableLiveData<Bug> todaysBug = new MutableLiveData<>();

    public BugOfTheDayViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        loadTodaysBug();
    }

    /**
     * Loads today's bug based on actual bug count in database.
     */
    private void loadTodaysBug() {
        repository.getExecutorService().execute(() -> {
            try {
                // Get actual bug count from database
                int totalBugs = repository.getBugDao().getBugCount();

                if (totalBugs == 0) {
                    android.util.Log.w("BugOfTheDayViewModel", "No bugs in database!");
                    return;
                }

                // Calculate bug ID based on actual count
                int bugId = DateUtils.getBugOfTheDayId(totalBugs);
                android.util.Log.d("BugOfTheDayViewModel", "Total bugs: " + totalBugs + ", Today's bug ID: " + bugId);

                // Get the bug synchronously
                Bug bug = repository.getBugDao().getBugByIdSync(bugId);

                if (bug != null) {
                    todaysBug.postValue(bug);
                } else {
                    android.util.Log.e("BugOfTheDayViewModel", "Bug with ID " + bugId + " not found!");
                }
            } catch (Exception e) {
                android.util.Log.e("BugOfTheDayViewModel", "Error loading today's bug", e);
            }
        });
    }

    /**
     * Gets today's bug challenge based on the date.
     * Uses DateUtils to calculate which bug ID should be shown today.
     */
    public LiveData<Bug> getTodaysBug() {
        return todaysBug;
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
