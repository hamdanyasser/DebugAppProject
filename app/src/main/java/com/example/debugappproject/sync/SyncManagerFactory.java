package com.example.debugappproject.sync;

import android.content.Context;

import com.example.debugappproject.auth.AuthManager;
import com.example.debugappproject.data.repository.BugRepository;

/**
 * Factory for creating the appropriate ProgressSyncManager implementation.
 *
 * Returns:
 * - LocalOnlyProgressSyncManager: When user is guest or Firebase unavailable
 * - FirebaseProgressSyncManager: When user is signed in and Firebase is configured
 */
public class SyncManagerFactory {

    /**
     * Creates the appropriate sync manager based on current auth state.
     *
     * @param context Application context
     * @param repository BugRepository instance
     * @return ProgressSyncManager implementation
     */
    public static ProgressSyncManager createSyncManager(Context context, BugRepository repository) {
        AuthManager authManager = AuthManager.getInstance(context);

        // Use Firebase sync only if user is signed in AND Firebase is available
        if (authManager.isSignedIn() && authManager.isFirebaseAvailable()) {
            return new FirebaseProgressSyncManager(context, repository);
        }

        // Default to local-only mode
        return new LocalOnlyProgressSyncManager();
    }
}
