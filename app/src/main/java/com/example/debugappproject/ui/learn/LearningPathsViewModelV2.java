package com.example.debugappproject.ui.learn;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.DailyBugHuntManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - LEARNING PATHS VIEWMODEL V2                          ║
 * ║        Enhanced with filtering, search, and gamification features            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LearningPathsViewModelV2 extends AndroidViewModel {

    private static final String TAG = "LearningPathsViewModelV2";

    // Filter Types
    public enum FilterType {
        ALL,
        FREE,
        PRO
    }

    // Primary Categories for organization
    public static final String CATEGORY_ALL = "All";
    public static final String[] PRIMARY_CATEGORIES = {
        "All",
        "Languages",
        "Data Structures & Algorithms",
        "Clean Code",
        "Interview Prep",
        "Web",
        "Mobile",
        "AI & ML",
        "Advanced"
    };

    private final BugRepository repository;
    private final DailyBugHuntManager dailyBugHuntManager;
    
    // Source data
    private final LiveData<List<LearningPath>> allPathsSource;
    private final LiveData<UserProgress> userProgressSource;
    
    // Filtered data
    private final MediatorLiveData<List<PathWithProgress>> filteredPaths = new MediatorLiveData<>();
    private final MutableLiveData<FilterType> currentFilter = new MutableLiveData<>(FilterType.ALL);
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>(CATEGORY_ALL);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    
    // Continue path (last in-progress path)
    private final MutableLiveData<PathWithProgress> continuePathData = new MutableLiveData<>();
    
    // Daily challenge
    private final MutableLiveData<Bug> dailyBug = new MutableLiveData<>();
    
    // Available categories (derived from paths)
    private final MutableLiveData<List<String>> availableCategories = new MutableLiveData<>();
    
    // Cache for all paths with progress
    private List<PathWithProgress> allPathsWithProgress = new ArrayList<>();
    private boolean isProUser = false;

    public LearningPathsViewModelV2(@NonNull Application application) {
        super(application);
        repository = new BugRepository(application);
        dailyBugHuntManager = new DailyBugHuntManager(application);
        
        allPathsSource = repository.getAllLearningPaths();
        userProgressSource = repository.getUserProgress();
        
        // Set up filtered paths as mediator
        filteredPaths.addSource(allPathsSource, paths -> {
            if (paths != null) {
                loadPathsWithProgress(paths);
            }
        });
        
        filteredPaths.addSource(currentFilter, filter -> applyFilters());
        filteredPaths.addSource(currentCategory, category -> applyFilters());
        filteredPaths.addSource(searchQuery, query -> applyFilters());
        
        // Load daily bug
        loadDailyBug();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC GETTERS
    // ═══════════════════════════════════════════════════════════════════════

    public LiveData<List<PathWithProgress>> getFilteredPaths() {
        return filteredPaths;
    }

    public LiveData<FilterType> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<String> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<PathWithProgress> getContinuePath() {
        return continuePathData;
    }

    public LiveData<Bug> getDailyBug() {
        return dailyBug;
    }

    public LiveData<UserProgress> getUserProgress() {
        return userProgressSource;
    }

    public LiveData<List<String>> getAvailableCategories() {
        return availableCategories;
    }
    
    public DailyBugHuntManager getDailyBugHuntManager() {
        return dailyBugHuntManager;
    }

    // Legacy compatibility
    public LiveData<List<LearningPath>> getAllPaths() {
        return allPathsSource;
    }

    public LiveData<Integer> getBugCountInPath(int pathId) {
        return repository.getBugCountInPath(pathId);
    }

    public LiveData<Integer> getCompletedBugCountInPath(int pathId) {
        return repository.getCompletedBugCountInPath(pathId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FILTER ACTIONS
    // ═══════════════════════════════════════════════════════════════════════

    public void setFilter(FilterType filter) {
        currentFilter.setValue(filter);
    }

    public void setCategory(String category) {
        currentCategory.setValue(category);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query.trim() : "");
    }

    public void setProStatus(boolean isPro) {
        this.isProUser = isPro;
        applyFilters();
    }

    public void clearFilters() {
        currentFilter.setValue(FilterType.ALL);
        currentCategory.setValue(CATEGORY_ALL);
        searchQuery.setValue("");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERNAL: Load paths with progress
    // ═══════════════════════════════════════════════════════════════════════

    private void loadPathsWithProgress(List<LearningPath> paths) {
        if (paths == null || paths.isEmpty()) {
            allPathsWithProgress = new ArrayList<>();
            applyFilters();
            return;
        }

        // Create PathWithProgress for each path (with 0 progress initially)
        List<PathWithProgress> result = new ArrayList<>();
        Set<String> categories = new HashSet<>();
        categories.add(CATEGORY_ALL);
        
        for (LearningPath path : paths) {
            result.add(new PathWithProgress(path, 0, 0));
            
            // Collect categories
            String primaryCat = path.getPrimaryCategory();
            if (primaryCat != null && !primaryCat.isEmpty()) {
                categories.add(primaryCat);
            }
        }
        
        allPathsWithProgress = result;
        
        // Update available categories
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories, (a, b) -> {
            if (a.equals(CATEGORY_ALL)) return -1;
            if (b.equals(CATEGORY_ALL)) return 1;
            return a.compareTo(b);
        });
        availableCategories.setValue(sortedCategories);
        
        // Apply filters
        applyFilters();
        
        // Find continue path (first in-progress path)
        findContinuePath();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERNAL: Apply all filters
    // ═══════════════════════════════════════════════════════════════════════

    private void applyFilters() {
        if (allPathsWithProgress == null || allPathsWithProgress.isEmpty()) {
            filteredPaths.setValue(new ArrayList<>());
            return;
        }

        FilterType filter = currentFilter.getValue();
        String category = currentCategory.getValue();
        String query = searchQuery.getValue();

        if (filter == null) filter = FilterType.ALL;
        if (category == null) category = CATEGORY_ALL;
        if (query == null) query = "";

        List<PathWithProgress> result = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        for (PathWithProgress pathWithProgress : allPathsWithProgress) {
            LearningPath path = pathWithProgress.getPath();
            if (path == null) continue;

            // Filter by type (All/Free/Pro)
            if (filter == FilterType.FREE && path.isLocked()) continue;
            if (filter == FilterType.PRO && !path.isLocked()) continue;

            // Filter by category
            if (!CATEGORY_ALL.equals(category)) {
                String pathCategory = path.getPrimaryCategory();
                if (pathCategory == null || !pathCategory.equals(category)) {
                    continue;
                }
            }

            // Filter by search query
            if (!queryLower.isEmpty()) {
                String name = path.getName() != null ? path.getName().toLowerCase() : "";
                String desc = path.getDescription() != null ? path.getDescription().toLowerCase() : "";
                String tags = path.getSkillTags() != null ? path.getSkillTags().toLowerCase() : "";
                String cat = path.getCategory() != null ? path.getCategory().toLowerCase() : "";
                
                if (!name.contains(queryLower) && 
                    !desc.contains(queryLower) && 
                    !tags.contains(queryLower) &&
                    !cat.contains(queryLower)) {
                    continue;
                }
            }

            result.add(pathWithProgress);
        }

        // Sort: Featured first, then by sort order, then by popularity
        Collections.sort(result, (a, b) -> {
            LearningPath pa = a.getPath();
            LearningPath pb = b.getPath();
            
            // Featured first
            if (pa.isFeatured() != pb.isFeatured()) {
                return pa.isFeatured() ? -1 : 1;
            }
            
            // New items second
            if (pa.isNew() != pb.isNew()) {
                return pa.isNew() ? -1 : 1;
            }
            
            // Then by sort order
            return Integer.compare(pa.getSortOrder(), pb.getSortOrder());
        });

        filteredPaths.setValue(result);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERNAL: Find continue path
    // ═══════════════════════════════════════════════════════════════════════

    private void findContinuePath() {
        // Find the first path with progress > 0 and < 100
        for (PathWithProgress pathWithProgress : allPathsWithProgress) {
            int progress = pathWithProgress.getProgressPercentage();
            if (progress > 0 && progress < 100) {
                // Check if accessible (not locked or user is pro)
                LearningPath path = pathWithProgress.getPath();
                if (!path.isLocked() || isProUser) {
                    continuePathData.setValue(pathWithProgress);
                    return;
                }
            }
        }
        continuePathData.setValue(null);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INTERNAL: Load daily bug
    // ═══════════════════════════════════════════════════════════════════════

    private void loadDailyBug() {
        int bugId = dailyBugHuntManager.getTodayBugId();
        repository.getBugById(bugId).observeForever(bug -> {
            if (bug != null) {
                dailyBug.setValue(bug);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UPDATE PROGRESS
    // ═══════════════════════════════════════════════════════════════════════

    public void updatePathProgress(int pathId, int totalBugs, int completedBugs) {
        for (int i = 0; i < allPathsWithProgress.size(); i++) {
            PathWithProgress p = allPathsWithProgress.get(i);
            if (p.getPath().getId() == pathId) {
                allPathsWithProgress.set(i, new PathWithProgress(p.getPath(), totalBugs, completedBugs));
                applyFilters();
                findContinuePath();
                break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════════════

    public int getTotalPathsCount() {
        return allPathsWithProgress != null ? allPathsWithProgress.size() : 0;
    }

    public int getFreePathsCount() {
        if (allPathsWithProgress == null) return 0;
        int count = 0;
        for (PathWithProgress p : allPathsWithProgress) {
            if (!p.getPath().isLocked()) count++;
        }
        return count;
    }

    public int getProPathsCount() {
        if (allPathsWithProgress == null) return 0;
        int count = 0;
        for (PathWithProgress p : allPathsWithProgress) {
            if (p.getPath().isLocked()) count++;
        }
        return count;
    }

    public int getOverallProgress() {
        if (allPathsWithProgress == null || allPathsWithProgress.isEmpty()) return 0;
        int total = 0;
        int completed = 0;
        for (PathWithProgress p : allPathsWithProgress) {
            total += p.getTotalBugs();
            completed += p.getCompletedBugs();
        }
        return total > 0 ? (completed * 100) / total : 0;
    }
}
