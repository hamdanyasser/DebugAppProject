package com.example.debugappproject.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.model.BugInPath;

import java.util.List;

/**
 * DAO for LearningPath and BugInPath operations.
 */
@Dao
public interface LearningPathDao {

    // Learning Path queries
    @Query("SELECT * FROM learning_paths ORDER BY sortOrder ASC")
    LiveData<List<LearningPath>> getAllPaths();

    @Query("SELECT * FROM learning_paths WHERE id = :pathId")
    LiveData<LearningPath> getPathById(int pathId);

    @Query("SELECT * FROM learning_paths WHERE id = :pathId")
    LearningPath getPathByIdSync(int pathId);

    @Query("SELECT COUNT(*) FROM learning_paths")
    int getPathCountSync();

    @Insert
    void insertPath(LearningPath path);

    @Insert
    void insertAllPaths(List<LearningPath> paths);

    // BugInPath queries
    @Query("SELECT bugId FROM bug_in_path WHERE pathId = :pathId ORDER BY orderInPath ASC")
    LiveData<List<Integer>> getBugIdsInPath(int pathId);

    @Query("SELECT bugId FROM bug_in_path WHERE pathId = :pathId ORDER BY orderInPath ASC")
    List<Integer> getBugIdsInPathSync(int pathId);

    @Query("SELECT * FROM bug_in_path WHERE pathId = :pathId ORDER BY orderInPath ASC")
    LiveData<List<BugInPath>> getBugsInPath(int pathId);

    @Query("SELECT COUNT(*) FROM bug_in_path WHERE pathId = :pathId")
    LiveData<Integer> getBugCountInPath(int pathId);

    @Query("SELECT COUNT(DISTINCT b.id) FROM bugs b " +
           "INNER JOIN bug_in_path bip ON b.id = bip.bugId " +
           "WHERE bip.pathId = :pathId AND b.isCompleted = 1")
    LiveData<Integer> getCompletedBugCountInPath(int pathId);

    @Insert
    void insertBugInPath(BugInPath bugInPath);

    @Insert
    void insertAllBugInPath(List<BugInPath> bugInPaths);

    @Transaction
    @Query("DELETE FROM bug_in_path")
    void clearAllBugInPath();
}
