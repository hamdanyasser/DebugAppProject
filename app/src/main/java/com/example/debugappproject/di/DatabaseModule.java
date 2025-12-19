package com.example.debugappproject.di;

import android.content.Context;

import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.HintDao;
import com.example.debugappproject.data.local.UserProgressDao;
import com.example.debugappproject.data.local.LearningPathDao;
import com.example.debugappproject.data.local.LessonDao;
import com.example.debugappproject.data.local.AchievementDao;
import com.example.debugappproject.data.local.MentalProfileDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * DatabaseModule - Provides database and DAO instances using Hilt.
 *
 * This module is installed in the SingletonComponent, meaning all provided
 * dependencies will have application-level scope (singletons).
 *
 * Benefits:
 * - Single database instance across the app (prevents multiple connections)
 * - DAOs are reused (better performance)
 * - Testable (can be mocked easily)
 * - Thread-safe initialization
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provides the singleton DebugMasterDatabase instance.
     * Room ensures thread-safe access to the database.
     */
    @Provides
    @Singleton
    public DebugMasterDatabase provideDatabase(@ApplicationContext Context context) {
        return DebugMasterDatabase.getInstance(context);
    }

    /**
     * Provides BugDao from the database.
     */
    @Provides
    @Singleton
    public BugDao provideBugDao(DebugMasterDatabase database) {
        return database.bugDao();
    }

    /**
     * Provides HintDao from the database.
     */
    @Provides
    @Singleton
    public HintDao provideHintDao(DebugMasterDatabase database) {
        return database.hintDao();
    }

    /**
     * Provides UserProgressDao from the database.
     */
    @Provides
    @Singleton
    public UserProgressDao provideUserProgressDao(DebugMasterDatabase database) {
        return database.userProgressDao();
    }

    /**
     * Provides LearningPathDao from the database.
     */
    @Provides
    @Singleton
    public LearningPathDao provideLearningPathDao(DebugMasterDatabase database) {
        return database.learningPathDao();
    }

    /**
     * Provides LessonDao from the database.
     */
    @Provides
    @Singleton
    public LessonDao provideLessonDao(DebugMasterDatabase database) {
        return database.lessonDao();
    }

    /**
     * Provides AchievementDao from the database.
     */
    @Provides
    @Singleton
    public AchievementDao provideAchievementDao(DebugMasterDatabase database) {
        return database.achievementDao();
    }

    /**
     * Provides MentalProfileDao from the database.
     * Used for the Mental Evolution System - cognitive skill tracking and ranked battles.
     */
    @Provides
    @Singleton
    public MentalProfileDao provideMentalProfileDao(DebugMasterDatabase database) {
        return database.mentalProfileDao();
    }
}
