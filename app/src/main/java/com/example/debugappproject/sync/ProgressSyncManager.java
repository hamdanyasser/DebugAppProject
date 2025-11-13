package com.example.debugappproject.sync;

/**
 * Interface for syncing user progress between local storage and remote backend.
 *
 * Implementations:
 * - LocalOnlyProgressSyncManager: No-op, works offline (current behavior)
 * - FirebaseProgressSyncManager: Syncs with Firestore when user is signed in
 */
public interface ProgressSyncManager {

    /**
     * Pushes local progress data to the remote backend.
     * Called after significant local changes (bug solved, achievement unlocked).
     *
     * @param listener Callback for success/failure
     */
    void pushLocalProgress(SyncCallback listener);

    /**
     * Pulls remote progress data and merges with local data.
     * Called on app start or manual sync trigger.
     *
     * @param listener Callback for success/failure
     */
    void pullAndMergeRemoteProgress(SyncCallback listener);

    /**
     * Performs a full bidirectional sync (pull + merge + push).
     * Called on user login or manual sync request.
     *
     * @param listener Callback for success/failure
     */
    void fullSync(SyncCallback listener);

    /**
     * Gets the timestamp of the last successful sync.
     *
     * @return Timestamp in milliseconds, or 0 if never synced
     */
    long getLastSyncTimestamp();

    /**
     * Callback interface for sync operations.
     */
    interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
