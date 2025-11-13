package com.example.debugappproject.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.debugappproject.auth.AuthManager;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.model.UserAchievement;
import com.example.debugappproject.model.UserProgress;

import java.util.List;

/**
 * Firebase-based implementation of ProgressSyncManager.
 *
 * Syncs user progress data with Firestore when:
 * - User is signed in with Firebase Auth
 * - Firebase is properly configured (google-services.json present)
 * - Network is available
 *
 * Merge Strategy:
 * - XP: Take maximum (user can only gain XP, never lose it)
 * - Levels: Derived from XP, so follows XP
 * - Streaks: Take maximum for longestStreakDays, smart merge for currentStreak
 * - Bugs solved: Take maximum counts
 * - Achievements: Union of local and remote (once unlocked, always unlocked)
 *
 * Firestore Structure:
 * users/{userId}/
 *   - progress/ (UserProgress data)
 *   - achievements/ (collection of UserAchievement)
 *   - bugCompletions/ (optional, per-bug completion timestamps)
 *
 * TODO: Uncomment and implement Firebase calls once google-services.json is added
 */
public class FirebaseProgressSyncManager implements ProgressSyncManager {

    private static final String PREFS_NAME = "DebugMasterSync";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";

    private final Context context;
    private final BugRepository repository;
    private final AuthManager authManager;
    private final SharedPreferences prefs;

    public FirebaseProgressSyncManager(Context context, BugRepository repository) {
        this.context = context.getApplicationContext();
        this.repository = repository;
        this.authManager = AuthManager.getInstance(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void pushLocalProgress(SyncCallback listener) {
        // Check prerequisites
        if (!authManager.isSignedIn() || !authManager.isFirebaseAvailable()) {
            if (listener != null) {
                listener.onError("Not signed in or Firebase unavailable");
            }
            return;
        }

        repository.getExecutorService().execute(() -> {
            try {
                // Get local data
                UserProgress localProgress = repository.getUserProgressDao().getUserProgressSync();
                List<UserAchievement> localAchievements = repository.getAchievementDao()
                    .getAllUnlockedAchievementsSync();

                // TODO: Uncomment when Firebase is configured
                // FirebaseFirestore db = FirebaseFirestore.getInstance();
                // String userId = authManager.getUserId();
                //
                // // Push UserProgress
                // db.collection("users").document(userId)
                //     .collection("progress").document("data")
                //     .set(localProgress)
                //     .addOnSuccessListener(aVoid -> {
                //         // Push achievements
                //         pushAchievements(db, userId, localAchievements, listener);
                //     })
                //     .addOnFailureListener(e -> {
                //         if (listener != null) listener.onError(e.getMessage());
                //     });

                // Stub: Simulate success until Firebase is configured
                updateLastSyncTimestamp();
                if (listener != null) {
                    listener.onSuccess();
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("Push failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void pullAndMergeRemoteProgress(SyncCallback listener) {
        // Check prerequisites
        if (!authManager.isSignedIn() || !authManager.isFirebaseAvailable()) {
            if (listener != null) {
                listener.onError("Not signed in or Firebase unavailable");
            }
            return;
        }

        repository.getExecutorService().execute(() -> {
            try {
                // Get local data
                UserProgress localProgress = repository.getUserProgressDao().getUserProgressSync();
                List<UserAchievement> localAchievements = repository.getAchievementDao()
                    .getAllUnlockedAchievementsSync();

                // TODO: Uncomment when Firebase is configured
                // FirebaseFirestore db = FirebaseFirestore.getInstance();
                // String userId = authManager.getUserId();
                //
                // // Pull remote progress
                // db.collection("users").document(userId)
                //     .collection("progress").document("data")
                //     .get()
                //     .addOnSuccessListener(documentSnapshot -> {
                //         if (documentSnapshot.exists()) {
                //             UserProgress remoteProgress = documentSnapshot.toObject(UserProgress.class);
                //             UserProgress mergedProgress = mergeProgress(localProgress, remoteProgress);
                //
                //             // Save merged progress locally
                //             repository.getUserProgressDao().updateProgress(mergedProgress);
                //
                //             // Pull and merge achievements
                //             pullAndMergeAchievements(db, userId, localAchievements, listener);
                //         } else {
                //             // No remote data yet - push local data
                //             pushLocalProgress(listener);
                //         }
                //     })
                //     .addOnFailureListener(e -> {
                //         if (listener != null) listener.onError(e.getMessage());
                //     });

                // Stub: Simulate success until Firebase is configured
                updateLastSyncTimestamp();
                if (listener != null) {
                    listener.onSuccess();
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("Pull failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void fullSync(SyncCallback listener) {
        // Full sync = pull & merge first, then push any local-only changes
        pullAndMergeRemoteProgress(new SyncCallback() {
            @Override
            public void onSuccess() {
                // After pull/merge, push to ensure remote has latest
                pushLocalProgress(listener);
            }

            @Override
            public void onError(String errorMessage) {
                if (listener != null) {
                    listener.onError(errorMessage);
                }
            }
        });
    }

    @Override
    public long getLastSyncTimestamp() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    /**
     * Merges local and remote UserProgress data.
     *
     * Merge Rules:
     * - totalXp: Take maximum (XP can only increase)
     * - totalSolved: Take maximum
     * - hintsUsed: Take maximum
     * - bugsSolvedWithoutHints: Take maximum
     * - longestStreakDays: Take maximum
     * - currentStreakDays: Take maximum (conservative approach)
     * - lastCompletionDate: Take most recent
     *
     * @param local Local progress data
     * @param remote Remote progress data
     * @return Merged progress
     */
    private UserProgress mergeProgress(UserProgress local, UserProgress remote) {
        if (local == null) return remote;
        if (remote == null) return local;

        UserProgress merged = new UserProgress();

        // Take maximum XP (user can only gain XP)
        merged.setTotalXp(Math.max(local.getTotalXp(), remote.getTotalXp()));

        // Take maximum counts
        merged.setTotalSolved(Math.max(local.getTotalSolved(), remote.getTotalSolved()));
        merged.setHintsUsed(Math.max(local.getHintsUsed(), remote.getHintsUsed()));
        merged.setBugsSolvedWithoutHints(Math.max(
            local.getBugsSolvedWithoutHints(),
            remote.getBugsSolvedWithoutHints()
        ));

        // Take maximum streaks
        merged.setLongestStreakDays(Math.max(
            local.getLongestStreakDays(),
            remote.getLongestStreakDays()
        ));
        merged.setCurrentStreakDays(Math.max(
            local.getCurrentStreakDays(),
            remote.getCurrentStreakDays()
        ));

        // Take most recent completion date
        long localDate = local.getLastCompletionDate();
        long remoteDate = remote.getLastCompletionDate();
        merged.setLastCompletionDate(Math.max(localDate, remoteDate));

        return merged;
    }

    /**
     * Updates the last sync timestamp.
     */
    private void updateLastSyncTimestamp() {
        prefs.edit()
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply();
    }

    // TODO: Add these helper methods when Firebase is configured
    //
    // private void pushAchievements(FirebaseFirestore db, String userId,
    //                               List<UserAchievement> achievements,
    //                               SyncCallback listener) {
    //     WriteBatch batch = db.batch();
    //     CollectionReference achievementsRef = db.collection("users")
    //         .document(userId).collection("achievements");
    //
    //     for (UserAchievement achievement : achievements) {
    //         DocumentReference docRef = achievementsRef.document(String.valueOf(achievement.getId()));
    //         batch.set(docRef, achievement);
    //     }
    //
    //     batch.commit()
    //         .addOnSuccessListener(aVoid -> {
    //             updateLastSyncTimestamp();
    //             if (listener != null) listener.onSuccess();
    //         })
    //         .addOnFailureListener(e -> {
    //             if (listener != null) listener.onError(e.getMessage());
    //         });
    // }
    //
    // private void pullAndMergeAchievements(FirebaseFirestore db, String userId,
    //                                       List<UserAchievement> localAchievements,
    //                                       SyncCallback listener) {
    //     db.collection("users").document(userId)
    //         .collection("achievements")
    //         .get()
    //         .addOnSuccessListener(querySnapshot -> {
    //             List<UserAchievement> remoteAchievements = querySnapshot.toObjects(UserAchievement.class);
    //
    //             // Merge: Union of local and remote (once unlocked, always unlocked)
    //             Set<Integer> localIds = localAchievements.stream()
    //                 .map(UserAchievement::getAchievementId)
    //                 .collect(Collectors.toSet());
    //
    //             for (UserAchievement remoteAch : remoteAchievements) {
    //                 if (!localIds.contains(remoteAch.getAchievementId())) {
    //                     // Remote achievement not in local - add it
    //                     repository.getAchievementDao().insertUserAchievement(remoteAch);
    //                 }
    //             }
    //
    //             updateLastSyncTimestamp();
    //             if (listener != null) listener.onSuccess();
    //         })
    //         .addOnFailureListener(e -> {
    //             if (listener != null) listener.onError(e.getMessage());
    //         });
    // }
}
