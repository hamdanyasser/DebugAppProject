package com.example.debugappproject.ui.buglist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for BugListFragment.
 * Manages bug list and filtering logic.
 * Supports search by title and description (Part 5).
 */
public class BugListViewModel extends AndroidViewModel {

    private final BugRepository repository;
    private final LiveData<List<Bug>> allBugs;
    private final MutableLiveData<String> selectedDifficulty;
    private final MutableLiveData<String> selectedCategory;
    private final MutableLiveData<String> searchQuery;  // NEW: Part 5
    private final LiveData<List<Bug>> filteredBugs;

    public BugListViewModel(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        allBugs = repository.getAllBugs();

        // Initialize filters
        selectedDifficulty = new MutableLiveData<>(Constants.DIFFICULTY_ALL);
        selectedCategory = new MutableLiveData<>(Constants.CATEGORY_ALL);
        searchQuery = new MutableLiveData<>("");  // NEW: Part 5

        // Create filtered bugs LiveData based on selected filters AND search query
        filteredBugs = Transformations.switchMap(selectedDifficulty, difficulty ->
            Transformations.switchMap(selectedCategory, category ->
                Transformations.switchMap(searchQuery, query ->
                    Transformations.map(allBugs, bugs -> filterBugs(bugs, difficulty, category, query))
                )
            )
        );
    }

    /**
     * Filter bugs based on difficulty, category, and search query.
     * Search matches against bug title and description (case-insensitive).
     */
    private List<Bug> filterBugs(List<Bug> bugs, String difficulty, String category, String query) {
        if (bugs == null) {
            return new ArrayList<>();
        }

        List<Bug> filtered = new ArrayList<>();
        String lowerQuery = (query != null) ? query.toLowerCase().trim() : "";

        for (Bug bug : bugs) {
            boolean matchesDifficulty = difficulty.equals(Constants.DIFFICULTY_ALL) ||
                                       bug.getDifficulty().equals(difficulty);
            boolean matchesCategory = category.equals(Constants.CATEGORY_ALL) ||
                                     bug.getCategory().equals(category);

            // Search filter: check if title or description contains query
            boolean matchesSearch = lowerQuery.isEmpty() ||
                                   bug.getTitle().toLowerCase().contains(lowerQuery) ||
                                   bug.getDescription().toLowerCase().contains(lowerQuery);

            if (matchesDifficulty && matchesCategory && matchesSearch) {
                filtered.add(bug);
            }
        }
        return filtered;
    }

    public LiveData<List<Bug>> getFilteredBugs() {
        return filteredBugs;
    }

    public void setDifficultyFilter(String difficulty) {
        selectedDifficulty.setValue(difficulty);
    }

    public void setCategoryFilter(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<String> getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    // NEW: Search methods (Part 5)
    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }
}
