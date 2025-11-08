package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.debugappproject.model.Hint;

import java.util.List;

/**
 * Data Access Object for Hint entities.
 * Provides methods to interact with the hints table.
 */
@Dao
public interface HintDao {

    /**
     * Insert a list of hints.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Hint> hints);

    /**
     * Get all hints for a specific bug, ordered by level.
     */
    @Query("SELECT * FROM hints WHERE bugId = :bugId ORDER BY level ASC")
    LiveData<List<Hint>> getHintsForBug(int bugId);

    /**
     * Get hints for a bug synchronously (for seeding).
     */
    @Query("SELECT * FROM hints WHERE bugId = :bugId ORDER BY level ASC")
    List<Hint> getHintsForBugSync(int bugId);

    /**
     * Get a specific hint by bug ID and level.
     */
    @Query("SELECT * FROM hints WHERE bugId = :bugId AND level = :level LIMIT 1")
    Hint getHintByLevel(int bugId, int level);
}
