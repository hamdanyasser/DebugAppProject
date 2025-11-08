package com.example.debugappproject.ui.home;

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

import java.util.List;

/**
 * ViewModel for HomeFragment.
 * Manages bug of the day and quick stats display.
 */
public class HomeViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<UserProgress> userProgress;
    private final LiveData<List<Bug>> allBugs;
    private final MutableLiveData<Bug> bugOfTheDay;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        userProgress = repository.getUserProgress();
        allBugs = repository.getAllBugs();
        bugOfTheDay = new MutableLiveData<>();

        // Update last opened timestamp
        repository.updateLastOpenedTimestamp();

        // Calculate Bug of the Day when bugs are loaded
        Transformations.map(allBugs, bugs -> {
            if (bugs != null && !bugs.isEmpty()) {
                int bugId = DateUtils.getBugOfTheDayId(bugs.size());
                // Find the bug with this ID
                for (Bug bug : bugs) {
                    if (bug.getId() == bugId) {
                        bugOfTheDay.setValue(bug);
                        break;
                    }
                }
            }
            return bugs;
        });
    }

    public LiveData<UserProgress> getUserProgress() {
        return userProgress;
    }

    public LiveData<Bug> getBugOfTheDay() {
        return bugOfTheDay;
    }

    public LiveData<List<Bug>> getAllBugs() {
        return allBugs;
    }
}
