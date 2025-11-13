package com.example.debugappproject.sync;

/**
 * Local-only implementation of ProgressSyncManager.
 *
 * This is the default implementation used when:
 * - User is in guest mode
 * - Firebase is not configured
 * - User has disabled cloud sync
 *
 * All sync operations are no-ops and immediately return success.
 * Data remains local only.
 */
public class LocalOnlyProgressSyncManager implements ProgressSyncManager {

    @Override
    public void pushLocalProgress(SyncCallback listener) {
        // No-op: Local-only mode, nothing to push
        if (listener != null) {
            listener.onSuccess();
        }
    }

    @Override
    public void pullAndMergeRemoteProgress(SyncCallback listener) {
        // No-op: Local-only mode, nothing to pull
        if (listener != null) {
            listener.onSuccess();
        }
    }

    @Override
    public void fullSync(SyncCallback listener) {
        // No-op: Local-only mode, no sync needed
        if (listener != null) {
            listener.onSuccess();
        }
    }

    @Override
    public long getLastSyncTimestamp() {
        // Never synced in local-only mode
        return 0;
    }
}
