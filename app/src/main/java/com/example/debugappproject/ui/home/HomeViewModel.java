package com.example.debugappproject.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.UserProgress;

import java.util.Calendar;
import java.util.List;

/**
 * HomeViewModel - Powers the best debugging game home screen! 🎮
 */
public class HomeViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<UserProgress> userProgress;
    private final LiveData<List<Bug>> allBugs;
    private final MutableLiveData<Bug> dailyChallenge = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        userProgress = repository.getUserProgress();
        allBugs = repository.getAllBugs();

        repository.updateLastOpenedTimestamp();
        loadDailyChallenge();
    }

    private void loadDailyChallenge() {
        isLoading.setValue(true);
        allBugs.observeForever(bugs -> {
            if (bugs != null && !bugs.isEmpty()) {
                int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                int bugIndex = dayOfYear % bugs.size();
                dailyChallenge.postValue(bugs.get(bugIndex));
            }
            isLoading.postValue(false);
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void retry() {
        errorMessage.setValue(null);
        loadDailyChallenge();
    }

    public LiveData<UserProgress> getUserProgress() {
        return userProgress;
    }

    public LiveData<Bug> getDailyChallenge() {
        return dailyChallenge;
    }

    public LiveData<List<Bug>> getAllBugs() {
        return allBugs;
    }
}
