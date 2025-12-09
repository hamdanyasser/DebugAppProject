package com.example.debugappproject.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.debugappproject.model.AchievementDefinition;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.model.BugInPath;
import com.example.debugappproject.model.Hint;
import com.example.debugappproject.model.LearningPath;
import com.example.debugappproject.model.Lesson;
import com.example.debugappproject.model.LessonQuestion;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

/**
 * Room Database for DebugMaster app.
 *
 * Version 2 adds:
 * - Bug: starterCode, userNotes, testsJson fields
 * - UserProgress: xp, hintsUsed, bugsSolvedWithoutHints fields
 *
 * Version 3 adds (Mimo-style transformation):
 * - LearningPath: organized learning paths
 * - BugInPath: many-to-many relationship between bugs and paths
 * - Lesson: micro-lessons for bugs
 * - LessonQuestion: quiz questions for lessons
 * - AchievementDefinition: achievement definitions
 * - UserAchievement: unlocked achievements tracking
 * - UserProgress: longestStreakDays field
 *
 * Version 4 adds (Enhanced Learning Platform):
 * - LearningPath: category, estimatedMinutes, totalLessons, xpReward, prerequisites,
 *                 isFeatured, isNew, colorHex, skillTags fields
 * - More comprehensive learning paths (15 total)
 * - Many more achievements
 *
 * Version 5 adds:
 * - Bug: hint field for helpful hints in challenges
 *
 * Version 6 adds:
 * - Schema fixes and updates for new features
 *
 * Version 7 adds:
 * - UserProgress: gems field for in-game currency
 */
@Database(
    entities = {
        Bug.class,
        Hint.class,
        UserProgress.class,
        LearningPath.class,
        BugInPath.class,
        Lesson.class,
        LessonQuestion.class,
        AchievementDefinition.class,
        UserAchievement.class
    },
    version = 7,
    exportSchema = false
)
public abstract class DebugMasterDatabase extends RoomDatabase {

    private static volatile DebugMasterDatabase INSTANCE;

    // DAOs
    public abstract BugDao bugDao();
    public abstract HintDao hintDao();
    public abstract UserProgressDao userProgressDao();
    public abstract LearningPathDao learningPathDao();
    public abstract LessonDao lessonDao();
    public abstract AchievementDao achievementDao();

    /**
     * Migration from version 2 to 3.
     * Adds new tables for learning paths, lessons, and achievements.
     * Adds longestStreakDays field to user_progress.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create learning_paths table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS learning_paths (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, " +
                "description TEXT, " +
                "iconEmoji TEXT, " +
                "difficultyRange TEXT, " +
                "sortOrder INTEGER NOT NULL, " +
                "isLocked INTEGER NOT NULL)"
            );

            // Create bug_in_path junction table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS bug_in_path (" +
                "bugId INTEGER NOT NULL, " +
                "pathId INTEGER NOT NULL, " +
                "orderInPath INTEGER NOT NULL, " +
                "PRIMARY KEY(bugId, pathId), " +
                "FOREIGN KEY(bugId) REFERENCES bugs(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(pathId) REFERENCES learning_paths(id) ON DELETE CASCADE)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_bug_in_path_bugId ON bug_in_path(bugId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_bug_in_path_pathId ON bug_in_path(pathId)");

            // Create lessons table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS lessons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "bugId INTEGER NOT NULL, " +
                "title TEXT, " +
                "content TEXT, " +
                "estimatedMinutes INTEGER NOT NULL, " +
                "FOREIGN KEY(bugId) REFERENCES bugs(id) ON DELETE CASCADE)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_lessons_bugId ON lessons(bugId)");

            // Create lesson_questions table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS lesson_questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "lessonId INTEGER NOT NULL, " +
                "questionText TEXT, " +
                "questionType TEXT, " +
                "optionsJson TEXT, " +
                "correctOptionIndex INTEGER NOT NULL, " +
                "explanation TEXT, " +
                "orderInLesson INTEGER NOT NULL, " +
                "FOREIGN KEY(lessonId) REFERENCES lessons(id) ON DELETE CASCADE)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_lesson_questions_lessonId ON lesson_questions(lessonId)");

            // Create achievement_definitions table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS achievement_definitions (" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "name TEXT, " +
                "description TEXT, " +
                "iconEmoji TEXT, " +
                "xpReward INTEGER NOT NULL, " +
                "category TEXT, " +
                "sortOrder INTEGER NOT NULL)"
            );

            // Create user_achievements table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS user_achievements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "achievementId TEXT, " +
                "unlockedTimestamp INTEGER NOT NULL, " +
                "notificationShown INTEGER NOT NULL, " +
                "FOREIGN KEY(achievementId) REFERENCES achievement_definitions(id) ON DELETE CASCADE)"
            );
            database.execSQL("CREATE INDEX IF NOT EXISTS index_user_achievements_achievementId ON user_achievements(achievementId)");

            // Add longestStreakDays to user_progress
            database.execSQL("ALTER TABLE user_progress ADD COLUMN longestStreakDays INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Migration from version 3 to 4.
     * Adds new columns to learning_paths for enhanced learning experience.
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns to learning_paths
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN category TEXT DEFAULT 'Programming'");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN estimatedMinutes INTEGER NOT NULL DEFAULT 30");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN totalLessons INTEGER NOT NULL DEFAULT 10");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN xpReward INTEGER NOT NULL DEFAULT 100");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN prerequisites TEXT");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN isFeatured INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN isNew INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN colorHex TEXT DEFAULT '#6366F1'");
            database.execSQL("ALTER TABLE learning_paths ADD COLUMN skillTags TEXT");
            
            // Clear old paths and achievements to reseed with new comprehensive content
            database.execSQL("DELETE FROM bug_in_path");
            database.execSQL("DELETE FROM learning_paths");
            database.execSQL("DELETE FROM user_achievements");
            database.execSQL("DELETE FROM achievement_definitions");
        }
    };

    /**
     * Migration from version 4 to 5.
     * Adds hint column to bugs table.
     */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add hint column to bugs table
            database.execSQL("ALTER TABLE bugs ADD COLUMN hint TEXT");
        }
    };

    /**
     * Migration from version 6 to 7.
     * Adds gems column to user_progress table.
     */
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add gems column to user_progress with default value of 100
            database.execSQL("ALTER TABLE user_progress ADD COLUMN gems INTEGER NOT NULL DEFAULT 100");
        }
    };

    /**
     * Get singleton instance of database.
     * Now uses proper migrations instead of destructive migration.
     */
    public static DebugMasterDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DebugMasterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DebugMasterDatabase.class,
                            "debug_master_database"
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_6_7)
                    .fallbackToDestructiveMigration() // Fallback for dev builds
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Force reset the database by clearing the singleton and deleting the database file.
     * Use this when database is corrupted or in an invalid state.
     */
    public static void resetDatabase(Context context) {
        synchronized (DebugMasterDatabase.class) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
            context.deleteDatabase("debug_master_database");
        }
    }
}
