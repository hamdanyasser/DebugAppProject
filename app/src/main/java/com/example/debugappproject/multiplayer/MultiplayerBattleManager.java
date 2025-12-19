package com.example.debugappproject.multiplayer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - MULTIPLAYER BATTLE MANAGER                           ║
 * ║              Real-time PvP battles using Firebase Realtime Database          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MultiplayerBattleManager {

    private static final String TAG = "MultiplayerBattle";

    // Firebase paths
    private static final String PATH_BATTLES = "battles";
    private static final String PATH_MATCHMAKING = "matchmaking_queue";
    private static final String PATH_CHALLENGES = "challenges";
    private static final String PATH_USERS_ONLINE = "users_online";

    // Battle states
    public static final String STATE_WAITING = "waiting";
    public static final String STATE_READY = "ready";
    public static final String STATE_IN_PROGRESS = "in_progress";
    public static final String STATE_FINISHED = "finished";

    private static MultiplayerBattleManager instance;
    private final Context context;
    private FirebaseDatabase database;

    private String currentBattleId;
    private String currentUserId;
    private String currentUserName;
    private String currentUserEmail;

    private ValueEventListener battleListener;
    private ValueEventListener challengeListener;
    private DatabaseReference currentBattleRef;

    private final MutableLiveData<BattleState> battleState = new MutableLiveData<>();
    private final MutableLiveData<Challenge> incomingChallenge = new MutableLiveData<>();

    // ═══════════════════════════════════════════════════════════════════════════
    //                              DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class BattleState {
        public String battleId = "";
        public String state = "";
        public int bugId = 0;
        public String buggyCode = "";
        public String correctCode = "";
        public Player player1;
        public Player player2;
        public long startTime = 0;
        public long endTime = 0;
        public String winnerId = "";

        public BattleState() {}
    }

    public static class Player {
        public String userId = "";
        public String userName = "";
        public String userEmail = "";
        public int progress = 0;
        public boolean finished = false;
        public long finishTime = 0;
        public String submittedCode = "";
        public boolean isReady = false;

        public Player() {}

        public Player(String userId, String userName, String userEmail) {
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
        }
    }

    public static class Challenge {
        public String fromUserId = "";
        public String fromUserName = "";
        public String fromUserEmail = "";
        public String toUserEmail = "";
        public String status = "";
        public long timestamp = 0;
        public String battleId = "";

        public Challenge() {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              SINGLETON
    // ═══════════════════════════════════════════════════════════════════════════

    private MultiplayerBattleManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            this.database = FirebaseDatabase.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firebase not initialized: " + e.getMessage());
        }
    }

    public static synchronized MultiplayerBattleManager getInstance(Context context) {
        if (instance == null) {
            instance = new MultiplayerBattleManager(context);
        }
        return instance;
    }

    public boolean isFirebaseAvailable() {
        return database != null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    public void initialize(String userId, String userName, String userEmail) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserEmail = userEmail;

        if (database != null) {
            setUserOnline(true);
            listenForChallenges();
        }

        Log.d(TAG, "Initialized for user: " + userName);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              USER PRESENCE
    // ═══════════════════════════════════════════════════════════════════════════

    private void setUserOnline(boolean online) {
        if (database == null || currentUserEmail == null || currentUserEmail.isEmpty()) return;

        String emailKey = currentUserEmail.replace(".", ",");
        DatabaseReference userRef = database.getReference(PATH_USERS_ONLINE).child(emailKey);

        if (online) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", currentUserId);
            userData.put("name", currentUserName);
            userData.put("email", currentUserEmail);
            userData.put("lastSeen", ServerValue.TIMESTAMP);
            userData.put("status", "online");
            userRef.setValue(userData);
            userRef.child("status").onDisconnect().setValue("offline");
        } else {
            userRef.child("status").setValue("offline");
        }
    }

    public void checkUserOnline(String email, OnUserStatusCallback callback) {
        if (database == null) {
            callback.onResult(false, null);
            return;
        }

        String emailKey = email.replace(".", ",");
        database.getReference(PATH_USERS_ONLINE).child(emailKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String status = snapshot.child("status").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        callback.onResult("online".equals(status), name);
                    } else {
                        callback.onResult(false, null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onResult(false, null);
                }
            });
    }

    public interface OnUserStatusCallback {
        void onResult(boolean isOnline, String userName);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              CHALLENGE SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    public void sendChallenge(String toEmail, OnChallengeCallback callback) {
        if (database == null) {
            callback.onError("Firebase not available");
            return;
        }
        if (currentUserEmail == null) {
            callback.onError("You must be logged in");
            return;
        }

        String challengeId = UUID.randomUUID().toString();
        String toEmailKey = toEmail.replace(".", ",");

        Challenge challenge = new Challenge();
        challenge.fromUserId = currentUserId;
        challenge.fromUserName = currentUserName;
        challenge.fromUserEmail = currentUserEmail;
        challenge.toUserEmail = toEmail;
        challenge.status = "pending";
        challenge.timestamp = System.currentTimeMillis();

        database.getReference(PATH_CHALLENGES)
            .child(toEmailKey)
            .child(challengeId)
            .setValue(challenge)
            .addOnSuccessListener(aVoid -> {
                callback.onChallengeSent(challengeId);
                listenForChallengeResponse(toEmailKey, challengeId, callback);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));

        // Auto-expire after 30 seconds
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            database.getReference(PATH_CHALLENGES)
                .child(toEmailKey)
                .child(challengeId)
                .child("status")
                .setValue("expired");
        }, 30000);
    }

    private void listenForChallengeResponse(String toEmailKey, String challengeId, OnChallengeCallback callback) {
        DatabaseReference ref = database.getReference(PATH_CHALLENGES).child(toEmailKey).child(challengeId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String status = snapshot.child("status").getValue(String.class);
                if ("accepted".equals(status)) {
                    String acceptedBattleId = snapshot.child("battleId").getValue(String.class);
                    callback.onChallengeAccepted(acceptedBattleId);
                    ref.removeEventListener(this);
                } else if ("declined".equals(status)) {
                    callback.onChallengeDeclined();
                    ref.removeEventListener(this);
                    snapshot.getRef().removeValue();
                } else if ("expired".equals(status)) {
                    callback.onChallengeExpired();
                    ref.removeEventListener(this);
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void listenForChallenges() {
        if (database == null || currentUserEmail == null) return;

        String emailKey = currentUserEmail.replace(".", ",");
        DatabaseReference ref = database.getReference(PATH_CHALLENGES).child(emailKey);

        challengeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Challenge challenge = snap.getValue(Challenge.class);
                    if (challenge != null && "pending".equals(challenge.status)) {
                        if (System.currentTimeMillis() - challenge.timestamp < 30000) {
                            incomingChallenge.postValue(challenge);
                        } else {
                            snap.getRef().child("status").setValue("expired");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        ref.addValueEventListener(challengeListener);
    }

    public void acceptChallenge(Challenge challenge, int bugId, String buggyCode, String correctCode, OnBattleCallback callback) {
        if (database == null) return;

        String battleId = UUID.randomUUID().toString();

        BattleState battle = new BattleState();
        battle.battleId = battleId;
        battle.state = STATE_READY;
        battle.bugId = bugId;
        battle.buggyCode = buggyCode;
        battle.correctCode = correctCode;
        battle.player1 = new Player(challenge.fromUserId, challenge.fromUserName, challenge.fromUserEmail);
        battle.player2 = new Player(currentUserId, currentUserName, currentUserEmail);

        database.getReference(PATH_BATTLES).child(battleId).setValue(battle)
            .addOnSuccessListener(aVoid -> {
                // Update challenge
                String emailKey = currentUserEmail.replace(".", ",");
                database.getReference(PATH_CHALLENGES).child(emailKey)
                    .orderByChild("fromUserEmail").equalTo(challenge.fromUserEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                snap.getRef().child("status").setValue("accepted");
                                snap.getRef().child("battleId").setValue(battleId);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                currentBattleId = battleId;
                joinBattle(battleId, callback);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void declineChallenge(Challenge challenge) {
        if (database == null || currentUserEmail == null) return;

        String emailKey = currentUserEmail.replace(".", ",");
        database.getReference(PATH_CHALLENGES).child(emailKey)
            .orderByChild("fromUserEmail").equalTo(challenge.fromUserEmail)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        snap.getRef().child("status").setValue("declined");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    public interface OnChallengeCallback {
        void onChallengeSent(String challengeId);
        void onChallengeAccepted(String battleId);
        void onChallengeDeclined();
        void onChallengeExpired();
        void onError(String message);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              BATTLE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    public void joinBattle(String battleId, OnBattleCallback callback) {
        if (database == null) return;

        currentBattleId = battleId;
        currentBattleRef = database.getReference(PATH_BATTLES).child(battleId);

        battleListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BattleState state = snapshot.getValue(BattleState.class);
                if (state == null) return;

                battleState.postValue(state);

                if (STATE_READY.equals(state.state) && areBothPlayersReady(state)) {
                    startBattle();
                } else if (STATE_IN_PROGRESS.equals(state.state)) {
                    callback.onBattleStarted(state);
                } else if (STATE_FINISHED.equals(state.state)) {
                    callback.onBattleFinished(state);
                }

                Player opponent = getOpponent(state);
                if (opponent != null) {
                    callback.onOpponentProgress(opponent.progress, opponent.finished);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };

        currentBattleRef.addValueEventListener(battleListener);
    }

    private boolean areBothPlayersReady(BattleState state) {
        return state.player1 != null && state.player1.isReady &&
               state.player2 != null && state.player2.isReady;
    }

    private Player getOpponent(BattleState state) {
        if (state.player1 != null && currentUserId.equals(state.player1.userId)) {
            return state.player2;
        }
        return state.player1;
    }

    private String getPlayerPath(BattleState state) {
        if (state.player1 != null && currentUserId.equals(state.player1.userId)) {
            return "player1";
        }
        return "player2";
    }

    public void setReady() {
        if (currentBattleRef == null) return;

        currentBattleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BattleState state = snapshot.getValue(BattleState.class);
                if (state != null) {
                    String path = getPlayerPath(state);
                    currentBattleRef.child(path).child("isReady").setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startBattle() {
        if (currentBattleRef == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("state", STATE_IN_PROGRESS);
        updates.put("startTime", ServerValue.TIMESTAMP);
        currentBattleRef.updateChildren(updates);
    }

    public void updateProgress(int progress) {
        if (currentBattleRef == null) return;

        currentBattleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BattleState state = snapshot.getValue(BattleState.class);
                if (state != null) {
                    String path = getPlayerPath(state);
                    currentBattleRef.child(path).child("progress").setValue(progress);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void submitCode(String code, boolean isCorrect) {
        if (currentBattleRef == null) return;

        currentBattleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BattleState state = snapshot.getValue(BattleState.class);
                if (state == null || STATE_FINISHED.equals(state.state)) return;

                String path = getPlayerPath(state);
                Map<String, Object> updates = new HashMap<>();
                updates.put(path + "/submittedCode", code);
                updates.put(path + "/finished", true);
                updates.put(path + "/finishTime", ServerValue.TIMESTAMP);
                updates.put(path + "/progress", isCorrect ? 100 : 50);

                Player opponent = getOpponent(state);
                if (isCorrect && (opponent == null || !opponent.finished)) {
                    updates.put("state", STATE_FINISHED);
                    updates.put("endTime", ServerValue.TIMESTAMP);
                    updates.put("winnerId", currentUserId);
                }

                currentBattleRef.updateChildren(updates);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void leaveBattle() {
        if (currentBattleRef != null && battleListener != null) {
            currentBattleRef.removeEventListener(battleListener);
        }
        currentBattleId = null;
        currentBattleRef = null;
    }

    public interface OnBattleCallback {
        void onBattleStarted(BattleState state);
        void onOpponentProgress(int progress, boolean finished);
        void onBattleFinished(BattleState state);
        void onError(String message);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              MATCHMAKING
    // ═══════════════════════════════════════════════════════════════════════════

    public void joinMatchmaking(int bugId, String buggyCode, String correctCode, OnMatchCallback callback) {
        if (database == null) {
            callback.onError("Firebase not available");
            return;
        }

        DatabaseReference queueRef = database.getReference(PATH_MATCHMAKING);

        queueRef.orderByChild("status").equalTo("waiting").limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean foundOpponent = false;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String userId = snap.child("userId").getValue(String.class);
                        if (userId != null && !userId.equals(currentUserId)) {
                            createBattleFromQueue(snap, bugId, buggyCode, correctCode, callback);
                            foundOpponent = true;
                            break;
                        }
                    }
                    if (!foundOpponent) {
                        addToQueue(bugId, buggyCode, correctCode, callback);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    private void addToQueue(int bugId, String buggyCode, String correctCode, OnMatchCallback callback) {
        String entryId = UUID.randomUUID().toString();
        DatabaseReference entryRef = database.getReference(PATH_MATCHMAKING).child(entryId);

        Map<String, Object> entry = new HashMap<>();
        entry.put("userId", currentUserId);
        entry.put("userName", currentUserName);
        entry.put("userEmail", currentUserEmail);
        entry.put("bugId", bugId);
        entry.put("buggyCode", buggyCode);
        entry.put("correctCode", correctCode);
        entry.put("status", "waiting");
        entry.put("timestamp", ServerValue.TIMESTAMP);

        entryRef.setValue(entry);
        callback.onWaiting();

        entryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);
                if ("matched".equals(status)) {
                    String matchedBattleId = snapshot.child("battleId").getValue(String.class);
                    if (matchedBattleId != null) {
                        callback.onMatchFound(matchedBattleId);
                        entryRef.removeEventListener(this);
                        entryRef.removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createBattleFromQueue(DataSnapshot opponentSnap, int bugId, String buggyCode, String correctCode, OnMatchCallback callback) {
        String opponentUserId = opponentSnap.child("userId").getValue(String.class);
        String opponentName = opponentSnap.child("userName").getValue(String.class);
        String opponentEmail = opponentSnap.child("userEmail").getValue(String.class);

        String battleId = UUID.randomUUID().toString();

        BattleState battle = new BattleState();
        battle.battleId = battleId;
        battle.state = STATE_READY;
        battle.bugId = bugId;
        battle.buggyCode = buggyCode;
        battle.correctCode = correctCode;
        battle.player1 = new Player(opponentUserId, opponentName, opponentEmail);
        battle.player2 = new Player(currentUserId, currentUserName, currentUserEmail);

        database.getReference(PATH_BATTLES).child(battleId).setValue(battle)
            .addOnSuccessListener(aVoid -> {
                opponentSnap.getRef().child("status").setValue("matched");
                opponentSnap.getRef().child("battleId").setValue(battleId);
                callback.onMatchFound(battleId);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void leaveMatchmaking() {
        if (database == null) return;

        database.getReference(PATH_MATCHMAKING)
            .orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        snap.getRef().removeValue();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    public interface OnMatchCallback {
        void onWaiting();
        void onMatchFound(String battleId);
        void onError(String message);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              GETTERS
    // ═══════════════════════════════════════════════════════════════════════════

    public LiveData<BattleState> getBattleState() {
        return battleState;
    }

    public LiveData<Challenge> getIncomingChallenge() {
        return incomingChallenge;
    }

    public String getCurrentBattleId() {
        return currentBattleId;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              CLEANUP
    // ═══════════════════════════════════════════════════════════════════════════

    public void cleanup() {
        setUserOnline(false);
        leaveBattle();
        leaveMatchmaking();

        if (database != null && challengeListener != null && currentUserEmail != null) {
            String emailKey = currentUserEmail.replace(".", ",");
            database.getReference(PATH_CHALLENGES).child(emailKey).removeEventListener(challengeListener);
        }
    }
}
