package com.example.debugappproject.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.LearningPath;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Path Detail screen.
 * Provides learning path details and associated bugs.
 */
public class PathDetailViewModel extends AndroidViewModel {
    private final BugRepository repository;
    private final MutableLiveData<LearningPath> currentPath = new MutableLiveData<>();
    private final MutableLiveData<List<BugInPathWithDetails>> bugsInPath = new MutableLiveData<>();

    public PathDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
    }

    /**
     * Gets the current learning path.
     */
    public LiveData<LearningPath> getCurrentPath() {
        return currentPath;
    }

    /**
     * Gets all bugs in the current path with completion status.
     */
    public LiveData<List<BugInPathWithDetails>> getBugsInPath() {
        return bugsInPath;
    }

    /**
     * Loads a learning path and its bugs.
     */
    public void loadPath(int pathId) {
        repository.getExecutorService().execute(() -> {
            // Load the path
            LearningPath path = repository.getLearningPathDao().getPathByIdSync(pathId);
            currentPath.postValue(path);

            // Load bug IDs in this path
            List<Integer> bugIds = repository.getLearningPathDao().getBugIdsInPath(pathId);

            // Load each bug and check if completed
            List<BugInPathWithDetails> bugs = new ArrayList<>();
            for (int bugId : bugIds) {
                Bug bug = repository.getBugDao().getBugByIdSync(bugId);
                if (bug != null) {
                    boolean isCompleted = repository.getBugDao().isBugCompleted(bugId);
                    bugs.add(new BugInPathWithDetails(bug, isCompleted));
                }
            }

            bugsInPath.postValue(bugs);
        });
    }

    /**
     * Gets the BugRepository instance.
     */
    public BugRepository getRepository() {
        return repository;
    }
}
