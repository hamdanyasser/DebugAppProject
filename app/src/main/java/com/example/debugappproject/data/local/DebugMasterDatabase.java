package com.example.debugappproject.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.UserProgress;

/**
 * Room Database for DebugMaster app.
 * Contains bugs, hints, and user progress tables.
 */
@Database(entities = {Bug.class, Hint.class, UserProgress.class}, version = 1, exportSchema = false)
public abstract class DebugMasterDatabase extends RoomDatabase {

    private static volatile DebugMasterDatabase INSTANCE;

    // DAOs
    public abstract BugDao bugDao();
    public abstract HintDao hintDao();
    public abstract UserProgressDao userProgressDao();

    /**
     * Get singleton instance of database.
     */
    public static DebugMasterDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DebugMasterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DebugMasterDatabase.class,
                            "debug_master_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
