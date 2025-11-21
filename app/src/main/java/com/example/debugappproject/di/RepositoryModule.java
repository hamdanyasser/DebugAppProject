package com.example.debugappproject.di;

import android.content.Context;

import com.example.debugappproject.data.repository.BugRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * RepositoryModule - Provides repository instances using Hilt.
 *
 * Repositories serve as the single source of truth for data access.
 * By providing them through Hilt, we ensure:
 * - Single instance across the app (singleton pattern)
 * - Easy testing (can mock repositories)
 * - Proper dependency injection into ViewModels
 * - Clean separation of concerns
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    /**
     * Provides the singleton BugRepository instance.
     *
     * The repository handles all data operations:
     * - Database access through DAOs
     * - Data transformation and mapping
     * - Background thread management
     * - Cache management
     *
     * @param context Application context for database access
     * @return Singleton BugRepository instance
     */
    @Provides
    @Singleton
    public BugRepository provideBugRepository(@ApplicationContext Context context) {
        return new BugRepository(context);
    }
}
