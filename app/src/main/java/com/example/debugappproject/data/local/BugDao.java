package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.debugappproject.model.Bug;

import java.util.List;

/**
 * Data Access Object for Bug entities.
 * Provides methods to interact with the bugs table.
 */
@Dao
public interface BugDao {

    /**
     * Insert a list of bugs. Replace on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Bug> bugs);

    /**
     * Insert a single bug.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Bug bug);

    /**
     * Update a bug.
     */
    @Update
    void update(Bug bug);

    /**
     * Get all bugs as LiveData for reactive updates.
     */
    @Query("SELECT * FROM bugs ORDER BY id ASC")
    LiveData<List<Bug>> getAllBugs();

    /**
     * Get a specific bug by ID.
     */
    @Query("SELECT * FROM bugs WHERE id = :bugId")
    LiveData<Bug> getBugById(int bugId);

    /**
     * Get bugs filtered by difficulty.
     */
    @Query("SELECT * FROM bugs WHERE difficulty = :difficulty ORDER BY id ASC")
    LiveData<List<Bug>> getBugsByDifficulty(String difficulty);

    /**
     * Get bugs filtered by category.
     */
    @Query("SELECT * FROM bugs WHERE category = :category ORDER BY id ASC")
    LiveData<List<Bug>> getBugsByCategory(String category);

    /**
     * Get bugs filtered by both difficulty and category.
     */
    @Query("SELECT * FROM bugs WHERE difficulty = :difficulty AND category = :category ORDER BY id ASC")
    LiveData<List<Bug>> getBugsByDifficultyAndCategory(String difficulty, String category);

    /**
     * Get completed bugs.
     */
    @Query("SELECT * FROM bugs WHERE isCompleted = 1 ORDER BY id ASC")
    LiveData<List<Bug>> getCompletedBugs();

    /**
     * Get count of all bugs.
     */
    @Query("SELECT COUNT(*) FROM bugs")
    int getBugCount();

    /**
     * Get count of completed bugs.
     */
    @Query("SELECT COUNT(*) FROM bugs WHERE isCompleted = 1")
    int getCompletedBugCount();

    /**
     * Get count of bugs by difficulty.
     */
    @Query("SELECT COUNT(*) FROM bugs WHERE difficulty = :difficulty")
    int getBugCountByDifficulty(String difficulty);

    /**
     * Get count of completed bugs by difficulty.
     */
    @Query("SELECT COUNT(*) FROM bugs WHERE difficulty = :difficulty AND isCompleted = 1")
    int getCompletedBugCountByDifficulty(String difficulty);

    /**
     * Get count of bugs by category.
     */
    @Query("SELECT COUNT(*) FROM bugs WHERE category = :category")
    int getBugCountByCategory(String category);

    /**
     * Get count of completed bugs by category.
     */
    @Query("SELECT COUNT(*) FROM bugs WHERE category = :category AND isCompleted = 1")
    int getCompletedBugsByCategory(String category);

    /**
     * Mark a bug as completed.
     */
    @Query("UPDATE bugs SET isCompleted = 1 WHERE id = :bugId")
    void markBugAsCompleted(int bugId);

    /**
     * Reset all bug completion status (for reset progress feature).
     */
    @Query("UPDATE bugs SET isCompleted = 0")
    void resetAllBugs();

    /**
     * Update user notes for a specific bug.
     */
    @Query("UPDATE bugs SET userNotes = :notes WHERE id = :bugId")
    void updateBugNotes(int bugId, String notes);
}
