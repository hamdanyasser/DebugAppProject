package com.example.debugappproject.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.LearningPath;

import java.util.List;

/**
 * ViewModel for Learning Paths screen.
 * Provides list of learning paths with progress data.
 */
public class LearningPathsViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<List<LearningPath>> allPaths;

    public LearningPathsViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        allPaths = repository.getAllLearningPaths();
    }

    public LiveData<List<LearningPath>> getAllPaths() {
        return allPaths;
    }

    /**
     * Get total bug count in a path.
     */
    public LiveData<Integer> getBugCountInPath(int pathId) {
        return repository.getBugCountInPath(pathId);
    }

    /**
     * Get completed bug count in a path.
     */
    public LiveData<Integer> getCompletedBugCountInPath(int pathId) {
        return repository.getCompletedBugCountInPath(pathId);
    }
}
