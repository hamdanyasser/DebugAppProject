package com.example.debugappproject.multiplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           FIREBASE MULTIPLAYER MANAGER v2.0                                  ║
 * ║              Real-time Battle Room Management with Synced Timer              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * FIXES:
 * - Server-synced battle timer
 * - Clear submission feedback (correct/wrong)
 * - Real-time opponent status updates
 * - Multiple submission attempts allowed
 */
public class FirebaseMultiplayerManager {

    private static final String TAG = "FirebaseMultiplayer";
    private static final String ROOMS_REF = "battle_rooms";
    private static final String ROOM_CODES_REF = "room_codes";
    private static final String MATCHMAKING_REF = "matchmaking_queue";
    private static final String SERVER_TIME_REF = ".info/serverTimeOffset";
    private static final String DATABASE_URL = "https://debugmaster-8ff5b-default-rtdb.firebaseio.com";
    public static final long BATTLE_DURATION_MS = 180 * 1000L; // 3 minutes
    
    private static FirebaseMultiplayerManager instance;
    
    private FirebaseDatabase database;
    private DatabaseReference roomsRef;
    private DatabaseReference roomCodesRef;
    private DatabaseReference matchmakingRef;
    private final Handler mainHandler;
    
    // Server time offset for sync
    private long serverTimeOffset = 0;
    private boolean serverTimeSynced = false;
    
    // Current session
    private String currentRoomId;
    private ValueEventListener roomListener;
    private MultiplayerCallback callback;
    
    // Stable user ID for this session
    private String sessionUserId;
    
    // Track submission state for feedback
    private int mySubmissionAttempts = 0;
    private long lastSubmissionTime = 0;
    
    // Phase 3: Connection state tracking
    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
        RECONNECTING
    }
    private ConnectionState currentConnectionState = ConnectionState.CONNECTED;
    private ValueEventListener connectionListener;
    private boolean isReconnecting = false;
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         CALLBACK INTERFACE
    // ═══════════════════════════════════════════════════════════════════════════
    
    public interface MultiplayerCallback {
        void onRoomCreated(BattleRoom room);
        void onRoomJoined(BattleRoom room);
        void onOpponentJoined(BattleRoom room);
        void onOpponentLeft(BattleRoom room);
        void onGameStateChanged(BattleRoom room);
        void onOpponentProgress(int progress);
        void onOpponentSubmitted(long submitTime);
        void onGameEnded(BattleRoom room);
        void onError(String error);
        void onMatchFound(BattleRoom room);
        void onTimerSync(long serverStartTime, long battleDurationMs);
        
        // NEW: Enhanced feedback callbacks
        default void onSubmissionResult(boolean isCorrect, String feedback, int attemptNumber) {}
        default void onOpponentSubmissionResult(boolean isCorrect) {}
        default void onBothPlayersReady(long serverStartTime) {}
        
        // Phase 3: Connection state callbacks
        default void onConnectionStateChanged(ConnectionState state) {}
        default void onReconnectFailed() {}
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    private FirebaseMultiplayerManager() {
        mainHandler = new Handler(Looper.getMainLooper());
        initializeFirebase();
    }
    
    private void initializeFirebase() {
        try {
            database = FirebaseDatabase.getInstance(DATABASE_URL);
            roomsRef = database.getReference(ROOMS_REF);
            roomCodesRef = database.getReference(ROOM_CODES_REF);
            matchmakingRef = database.getReference(MATCHMAKING_REF);
            
            // Sync server time offset for accurate timer sync
            syncServerTime();
            
            // Phase 3: Start connection monitoring
            startConnectionMonitoring();
            
            Log.d(TAG, "Firebase initialized with URL: " + DATABASE_URL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
    
    /**
     * Phase 3: Monitor Firebase connection state
     */
    private void startConnectionMonitoring() {
        DatabaseReference connectedRef = database.getReference(".info/connected");
        connectionListener = connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                ConnectionState newState = connected ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
                
                if (newState != currentConnectionState) {
                    Log.d(TAG, "Connection state changed: " + newState);
                    currentConnectionState = newState;
                    
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onConnectionStateChanged(newState);
                        }
                    });
                    
                    // If disconnected during a battle, start reconnection timer
                    if (newState == ConnectionState.DISCONNECTED && currentRoomId != null) {
                        isReconnecting = true;
                        startReconnectionTimeout();
                    } else if (newState == ConnectionState.CONNECTED && isReconnecting) {
                        isReconnecting = false;
                        cancelReconnectionTimeout();
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Connection monitoring cancelled", error.toException());
            }
        });
    }
    
    private Runnable reconnectionTimeoutRunnable;
    
    /**
     * Phase 3: Start 10-second reconnection timeout
     */
    private void startReconnectionTimeout() {
        cancelReconnectionTimeout();
        
        reconnectionTimeoutRunnable = () -> {
            if (isReconnecting && currentRoomId != null) {
                Log.w(TAG, "Reconnection timeout - auto-forfeit");
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onReconnectFailed();
                    }
                });
            }
        };
        
        mainHandler.postDelayed(reconnectionTimeoutRunnable, 10000); // 10 seconds
    }
    
    private void cancelReconnectionTimeout() {
        if (reconnectionTimeoutRunnable != null) {
            mainHandler.removeCallbacks(reconnectionTimeoutRunnable);
            reconnectionTimeoutRunnable = null;
        }
    }
    
    /**
     * Phase 3: Get current connection state
     */
    public ConnectionState getConnectionState() {
        return currentConnectionState;
    }
    
    /**
     * Sync with Firebase server time for accurate battle timer
     */
    private void syncServerTime() {
        database.getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long offset = snapshot.getValue(Long.class);
                if (offset != null) {
                    serverTimeOffset = offset;
                    serverTimeSynced = true;
                    Log.d(TAG, "Server time offset synced: " + offset + "ms");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to sync server time", error.toException());
            }
        });
    }
    
    /**
     * Get estimated server time
     */
    public long getServerTime() {
        return System.currentTimeMillis() + serverTimeOffset;
    }
    
    public boolean isServerTimeSynced() {
        return serverTimeSynced;
    }
    
    public static synchronized FirebaseMultiplayerManager getInstance() {
        if (instance == null) {
            instance = new FirebaseMultiplayerManager();
        }
        return instance;
    }
    
    public void setCallback(MultiplayerCallback callback) {
        this.callback = callback;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         USER IDENTIFICATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    public String getCurrentUserId() {
        if (sessionUserId == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                sessionUserId = user.getUid();
            } else {
                sessionUserId = "anon_" + android.os.Build.MODEL.replace(" ", "_") + "_" + System.currentTimeMillis();
            }
            Log.d(TAG, "User ID: " + sessionUserId);
        }
        return sessionUserId;
    }
    
    public String getCurrentUserName(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        try {
            com.example.debugappproject.util.AuthManager authManager = 
                com.example.debugappproject.util.AuthManager.getInstance(context);
            String name = authManager.getDisplayName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get local display name", e);
        }
        return "Player" + (System.currentTimeMillis() % 10000);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         ROOM CREATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void createRoom(Context context, int bugId, CreateRoomCallback callback) {
        // Phase 3: Prevent creating room if already in one
        if (isInRoom()) {
            mainHandler.post(() -> callback.onError("Already in a room. Leave current room first."));
            return;
        }
        
        // Reset submission state for new game
        mySubmissionAttempts = 0;
        lastSubmissionTime = 0;
        
        String roomCode = generateRoomCode();
        String roomId = roomsRef.push().getKey();
        
        if (roomId == null) {
            mainHandler.post(() -> callback.onError("Failed to generate room ID"));
            return;
        }
        
        String hostId = getCurrentUserId();
        String hostName = getCurrentUserName(context);
        
        Log.d(TAG, "Creating room: " + roomCode + " with ID: " + roomId + " for host: " + hostId);
        
        BattleRoom room = new BattleRoom(roomId, roomCode, hostId, hostName, bugId);
        
        roomCodesRef.child(roomCode).setValue(roomId)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Room code saved: " + roomCode + " -> " + roomId);
                
                roomsRef.child(roomId).setValue(room.toMap())
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Room data saved successfully");
                        currentRoomId = roomId;
                        startListeningToRoom(roomId);
                        mainHandler.post(() -> callback.onSuccess(room));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save room data", e);
                        roomCodesRef.child(roomCode).removeValue();
                        mainHandler.post(() -> callback.onError("Failed to create room: " + e.getMessage()));
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save room code", e);
                mainHandler.post(() -> callback.onError("Failed to create room: " + e.getMessage()));
            });
    }
    
    public interface CreateRoomCallback {
        void onSuccess(BattleRoom room);
        void onError(String error);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         ROOM JOINING
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void joinRoomByCode(Context context, String roomCode, JoinRoomCallback callback) {
        // Phase 3: Validate room code format (6 chars, uppercase)
        if (roomCode == null || roomCode.trim().length() != 6) {
            mainHandler.post(() -> callback.onError("Invalid room code format. Must be 6 characters."));
            return;
        }
        
        String normalizedCode = roomCode.trim().toUpperCase();
        
        // Phase 3: Validate only alphanumeric
        if (!normalizedCode.matches("^[A-Z0-9]{6}$")) {
            mainHandler.post(() -> callback.onError("Invalid room code. Use only letters and numbers."));
            return;
        }
        
        // Phase 3: Prevent joining if already in a room
        if (isInRoom()) {
            mainHandler.post(() -> callback.onError("Already in a room. Leave current room first."));
            return;
        }
        
        // Reset submission state for new game
        mySubmissionAttempts = 0;
        lastSubmissionTime = 0;
        
        Log.d(TAG, "Looking up room code: " + normalizedCode);
        
        roomCodesRef.child(normalizedCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Room code lookup result - exists: " + snapshot.exists() + ", value: " + snapshot.getValue());
                
                if (!snapshot.exists() || snapshot.getValue() == null) {
                    mainHandler.post(() -> callback.onError("Room code not found: " + normalizedCode));
                    return;
                }
                
                String roomId = snapshot.getValue(String.class);
                Log.d(TAG, "Found room ID: " + roomId);
                joinRoomById(context, roomId, callback);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Room code lookup cancelled", error.toException());
                mainHandler.post(() -> callback.onError("Database error: " + error.getMessage()));
            }
        });
    }
    
    public void joinRoomById(Context context, String roomId, JoinRoomCallback callback) {
        Log.d(TAG, "Attempting to join room: " + roomId);
        
        roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Room lookup result - exists: " + snapshot.exists());
                
                if (!snapshot.exists()) {
                    mainHandler.post(() -> callback.onError("Room not found"));
                    return;
                }
                
                BattleRoom room = parseRoom(snapshot);
                if (room == null) {
                    mainHandler.post(() -> callback.onError("Invalid room data"));
                    return;
                }
                
                Log.d(TAG, "Room state: " + room.getState() + ", guestId: " + room.getGuestId());
                
                if (room.getGuestId() != null && !room.getGuestId().isEmpty()) {
                    mainHandler.post(() -> callback.onError("Room is already full"));
                    return;
                }
                
                if (room.getState() != BattleRoom.RoomState.WAITING) {
                    mainHandler.post(() -> callback.onError("Room is not accepting players"));
                    return;
                }
                
                if (room.isExpired()) {
                    mainHandler.post(() -> callback.onError("Room has expired"));
                    return;
                }
                
                String guestId = getCurrentUserId();
                String guestName = getCurrentUserName(context);
                
                Log.d(TAG, "Joining as guest: " + guestId + " (" + guestName + ")");
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("guestId", guestId);
                updates.put("guestName", guestName);
                updates.put("state", BattleRoom.RoomState.STARTING.name());
                updates.put("startedAt", ServerValue.TIMESTAMP);  // Server timestamp for sync!
                
                roomsRef.child(roomId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Successfully joined room!");
                        currentRoomId = roomId;
                        
                        room.setGuestId(guestId);
                        room.setGuestName(guestName);
                        room.setState(BattleRoom.RoomState.STARTING);
                        
                        startListeningToRoom(roomId);
                        mainHandler.post(() -> callback.onSuccess(room));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to join room", e);
                        mainHandler.post(() -> callback.onError("Failed to join: " + e.getMessage()));
                    });
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Room lookup cancelled", error.toException());
                mainHandler.post(() -> callback.onError("Database error: " + error.getMessage()));
            }
        });
    }
    
    public interface JoinRoomCallback {
        void onSuccess(BattleRoom room);
        void onError(String error);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         QUICK MATCHMAKING
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void startMatchmaking(Context context, int bugId) {
        // Reset submission state for new game
        mySubmissionAttempts = 0;
        lastSubmissionTime = 0;
        
        String myId = getCurrentUserId();
        
        matchmakingRef.orderByChild("timestamp").limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String oderId = child.getKey();
                            if (oderId != null && !oderId.equals(myId)) {
                                String roomId = child.child("roomId").getValue(String.class);
                                if (roomId != null) {
                                    matchmakingRef.child(oderId).removeValue();
                                    joinRoomById(context, roomId, new JoinRoomCallback() {
                                        @Override
                                        public void onSuccess(BattleRoom room) {
                                            if (callback != null) callback.onMatchFound(room);
                                        }
                                        @Override
                                        public void onError(String error) {
                                            createAndQueueRoom(context, bugId);
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                    createAndQueueRoom(context, bugId);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    createAndQueueRoom(context, bugId);
                }
            });
    }
    
    private void createAndQueueRoom(Context context, int bugId) {
        createRoom(context, bugId, new CreateRoomCallback() {
            @Override
            public void onSuccess(BattleRoom room) {
                Map<String, Object> queueEntry = new HashMap<>();
                queueEntry.put("roomId", room.getRoomId());
                queueEntry.put("timestamp", ServerValue.TIMESTAMP);
                queueEntry.put("bugId", bugId);
                
                matchmakingRef.child(getCurrentUserId()).setValue(queueEntry);
                
                if (callback != null) callback.onRoomCreated(room);
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    public void cancelMatchmaking() {
        matchmakingRef.child(getCurrentUserId()).removeValue();
        if (currentRoomId != null) {
            leaveRoom();
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         GAME STATE UPDATES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void updateProgress(int progress) {
        if (currentRoomId == null) return;
        
        String myId = getCurrentUserId();
        roomsRef.child(currentRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hostId = snapshot.child("hostId").getValue(String.class);
                String field = myId.equals(hostId) ? "hostProgress" : "guestProgress";
                roomsRef.child(currentRoomId).child(field).setValue(progress);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    
    /**
     * IMPROVED: Submit solution with proper feedback
     * Returns immediately with local validation result, then confirms with Firebase
     */
    public void submitSolution(String code, boolean isCorrect) {
        if (currentRoomId == null) return;

        String myId = getCurrentUserId();
        final String roomId = currentRoomId;
        
        // Track attempts
        mySubmissionAttempts++;
        lastSubmissionTime = getServerTime();
        final int attemptNumber = mySubmissionAttempts;

        roomsRef.child(roomId).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                String hostId = mutableData.child("hostId").getValue(String.class);
                String currentWinner = mutableData.child("winnerId").getValue(String.class);
                String currentState = mutableData.child("state").getValue(String.class);

                if (hostId == null) {
                    return Transaction.success(mutableData);
                }

                boolean isHost = myId.equals(hostId);

                // Update submission data with attempt tracking
                if (isHost) {
                    mutableData.child("hostSubmission").setValue(code);
                    mutableData.child("hostSubmitTime").setValue(ServerValue.TIMESTAMP);
                    mutableData.child("hostProgress").setValue(isCorrect ? 100 : 75);
                    mutableData.child("hostCorrect").setValue(isCorrect);
                    mutableData.child("hostAttempts").setValue(attemptNumber);
                } else {
                    mutableData.child("guestSubmission").setValue(code);
                    mutableData.child("guestSubmitTime").setValue(ServerValue.TIMESTAMP);
                    mutableData.child("guestProgress").setValue(isCorrect ? 100 : 75);
                    mutableData.child("guestCorrect").setValue(isCorrect);
                    mutableData.child("guestAttempts").setValue(attemptNumber);
                }

                // Only set winner if correct AND no winner yet AND game not finished
                if (isCorrect &&
                    (currentWinner == null || currentWinner.isEmpty()) &&
                    !"FINISHED".equals(currentState)) {
                    mutableData.child("winnerId").setValue(myId);
                    mutableData.child("winReason").setValue("Fixed the bug first!");
                    mutableData.child("state").setValue(BattleRoom.RoomState.FINISHED.name());
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                if (error != null) {
                    Log.e(TAG, "Submit solution transaction failed", error.toException());
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onError("Failed to submit: " + error.getMessage());
                            callback.onSubmissionResult(false, "Network error. Try again!", attemptNumber);
                        }
                    });
                } else {
                    Log.d(TAG, "Solution submitted successfully. Correct: " + isCorrect + ", Attempt: " + attemptNumber);
                    
                    // Provide immediate feedback
                    mainHandler.post(() -> {
                        if (callback != null) {
                            String feedback;
                            if (isCorrect) {
                                feedback = "✅ Correct! Waiting for result...";
                            } else {
                                if (attemptNumber >= 3) {
                                    feedback = "❌ Incorrect (Attempt " + attemptNumber + "). Check the hint!";
                                } else {
                                    feedback = "❌ Not quite right. Try again! (Attempt " + attemptNumber + ")";
                                }
                            }
                            callback.onSubmissionResult(isCorrect, feedback, attemptNumber);
                        }
                    });
                }
            }
        });
    }
    
    public void startBattle() {
        if (currentRoomId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("state", BattleRoom.RoomState.IN_PROGRESS.name());
        updates.put("startedAt", ServerValue.TIMESTAMP);  // Critical for timer sync!
        roomsRef.child(currentRoomId).updateChildren(updates);
    }
    
    public void endGame(String winnerId, String reason) {
        if (currentRoomId == null) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("winnerId", winnerId);
        updates.put("winReason", reason);
        updates.put("state", BattleRoom.RoomState.FINISHED.name());
        
        roomsRef.child(currentRoomId).updateChildren(updates);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         ROOM LISTENING - ENHANCED
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void startListeningToRoom(String roomId) {
        stopListeningToRoom();
        
        Log.d(TAG, "Starting to listen to room: " + roomId);
        
        roomListener = new ValueEventListener() {
            private String lastGuestId = null;
            private int lastOpponentProgress = -1;
            private BattleRoom.RoomState lastState = null;
            private Boolean lastOpponentCorrect = null;
            private long lastOpponentSubmitTime = 0;
            
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mainHandler.post(() -> {
                        if (callback != null) callback.onError("Room was deleted");
                    });
                    return;
                }
                
                BattleRoom room = parseRoom(snapshot);
                if (room == null) return;
                
                String myId = getCurrentUserId();
                boolean isHost = myId.equals(room.getHostId());
                
                // Check for opponent joining
                String currentGuestId = room.getGuestId();
                if (currentGuestId != null && !currentGuestId.isEmpty() && 
                    (lastGuestId == null || lastGuestId.isEmpty())) {
                    Log.d(TAG, "Opponent joined: " + room.getGuestName());
                    mainHandler.post(() -> {
                        if (callback != null) callback.onOpponentJoined(room);
                    });
                }
                lastGuestId = currentGuestId;
                
                // Check for opponent progress
                int opponentProgress = isHost ? room.getGuestProgress() : room.getHostProgress();
                if (opponentProgress != lastOpponentProgress) {
                    lastOpponentProgress = opponentProgress;
                    mainHandler.post(() -> {
                        if (callback != null) callback.onOpponentProgress(opponentProgress);
                    });
                }
                
                // Check for opponent submission (with correctness!)
                long opponentSubmitTime = isHost ? room.getGuestSubmitTime() : room.getHostSubmitTime();
                boolean opponentCorrect = isHost ? room.isGuestCorrect() : room.isHostCorrect();
                
                if (opponentSubmitTime > lastOpponentSubmitTime) {
                    lastOpponentSubmitTime = opponentSubmitTime;
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onOpponentSubmitted(opponentSubmitTime);
                            callback.onOpponentSubmissionResult(opponentCorrect);
                        }
                    });
                }
                
                // Check for state changes
                if (room.getState() != lastState) {
                    BattleRoom.RoomState previousState = lastState;
                    lastState = room.getState();
                    
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onGameStateChanged(room);

                            // Sync timer when battle starts - KEY FIX!
                            if ((room.getState() == BattleRoom.RoomState.IN_PROGRESS || 
                                 room.getState() == BattleRoom.RoomState.STARTING) &&
                                previousState != BattleRoom.RoomState.IN_PROGRESS &&
                                previousState != BattleRoom.RoomState.STARTING) {
                                
                                long startTime = room.getStartedAt();
                                if (startTime > 0) {
                                    Log.d(TAG, "Sending timer sync: startTime=" + startTime);
                                    callback.onTimerSync(startTime, BATTLE_DURATION_MS);
                                    callback.onBothPlayersReady(startTime);
                                }
                            }

                            if (room.getState() == BattleRoom.RoomState.FINISHED) {
                                callback.onGameEnded(room);
                            }
                        }
                    });
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Room listener cancelled", error.toException());
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(error.getMessage());
                });
            }
        };
        
        roomsRef.child(roomId).addValueEventListener(roomListener);
    }
    
    private void stopListeningToRoom() {
        if (roomListener != null && currentRoomId != null) {
            roomsRef.child(currentRoomId).removeEventListener(roomListener);
            roomListener = null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         CLEANUP
    // ═══════════════════════════════════════════════════════════════════════════
    
    public void leaveRoom() {
        if (currentRoomId == null) return;

        String myId = getCurrentUserId();
        if (myId != null) {
            matchmakingRef.child(myId).removeValue();
        }

        final String roomIdToLeave = currentRoomId;

        stopListeningToRoom();
        currentRoomId = null;
        
        // Reset submission state
        mySubmissionAttempts = 0;
        lastSubmissionTime = 0;

        roomsRef.child(roomIdToLeave).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String hostId = snapshot.child("hostId").getValue(String.class);
                String guestId = snapshot.child("guestId").getValue(String.class);
                String roomCode = snapshot.child("roomCode").getValue(String.class);
                String state = snapshot.child("state").getValue(String.class);

                if (hostId == null) return;

                if (myId != null && myId.equals(hostId) && (guestId == null || guestId.isEmpty())) {
                    if (roomCode != null && !roomCode.isEmpty()) {
                        roomCodesRef.child(roomCode).removeValue();
                    }
                    roomsRef.child(roomIdToLeave).removeValue();
                } else if (!"FINISHED".equals(state)) {
                    Map<String, Object> updates = new HashMap<>();
                    String winnerId = (myId != null && myId.equals(hostId)) ? guestId : hostId;
                    if (winnerId != null) {
                        updates.put("winnerId", winnerId);
                        updates.put("winReason", "Opponent left the game");
                        updates.put("state", BattleRoom.RoomState.FINISHED.name());
                        roomsRef.child(roomIdToLeave).updateChildren(updates);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error leaving room", error.toException());
            }
        });
    }
    
    public void cleanupExpiredRooms() {
        long now = System.currentTimeMillis();
        roomsRef.orderByChild("expiresAt").endAt(now)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String roomCode = child.child("roomCode").getValue(String.class);
                        if (roomCode != null) {
                            roomCodesRef.child(roomCode).removeValue();
                        }
                        child.getRef().removeValue();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════
    
    private String generateRoomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    private BattleRoom parseRoom(DataSnapshot snapshot) {
        try {
            BattleRoom room = new BattleRoom();
            room.setRoomId(snapshot.child("roomId").getValue(String.class));
            room.setRoomCode(snapshot.child("roomCode").getValue(String.class));
            room.setHostId(snapshot.child("hostId").getValue(String.class));
            room.setHostName(snapshot.child("hostName").getValue(String.class));
            room.setGuestId(snapshot.child("guestId").getValue(String.class));
            room.setGuestName(snapshot.child("guestName").getValue(String.class));
            
            String stateStr = snapshot.child("state").getValue(String.class);
            room.setState(stateStr != null ? BattleRoom.RoomState.valueOf(stateStr) : BattleRoom.RoomState.WAITING);
            
            Long bugId = snapshot.child("bugId").getValue(Long.class);
            room.setBugId(bugId != null ? bugId.intValue() : 0);
            
            Long createdAt = snapshot.child("createdAt").getValue(Long.class);
            room.setCreatedAt(createdAt != null ? createdAt : 0);
            
            Long startedAt = snapshot.child("startedAt").getValue(Long.class);
            room.setStartedAt(startedAt != null ? startedAt : 0);
            
            Long expiresAt = snapshot.child("expiresAt").getValue(Long.class);
            room.setExpiresAt(expiresAt != null ? expiresAt : 0);
            
            Long hostProgress = snapshot.child("hostProgress").getValue(Long.class);
            room.setHostProgress(hostProgress != null ? hostProgress.intValue() : 0);
            
            Long guestProgress = snapshot.child("guestProgress").getValue(Long.class);
            room.setGuestProgress(guestProgress != null ? guestProgress.intValue() : 0);
            
            room.setHostSubmission(snapshot.child("hostSubmission").getValue(String.class));
            room.setGuestSubmission(snapshot.child("guestSubmission").getValue(String.class));
            
            Long hostSubmitTime = snapshot.child("hostSubmitTime").getValue(Long.class);
            room.setHostSubmitTime(hostSubmitTime != null ? hostSubmitTime : 0);
            
            Long guestSubmitTime = snapshot.child("guestSubmitTime").getValue(Long.class);
            room.setGuestSubmitTime(guestSubmitTime != null ? guestSubmitTime : 0);

            Boolean hostCorrect = snapshot.child("hostCorrect").getValue(Boolean.class);
            room.setHostCorrect(hostCorrect != null && hostCorrect);

            Boolean guestCorrect = snapshot.child("guestCorrect").getValue(Boolean.class);
            room.setGuestCorrect(guestCorrect != null && guestCorrect);

            room.setWinnerId(snapshot.child("winnerId").getValue(String.class));
            room.setWinReason(snapshot.child("winReason").getValue(String.class));
            
            return room;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing room", e);
            return null;
        }
    }
    
    public String getCurrentRoomId() {
        return currentRoomId;
    }
    
    public boolean isInRoom() {
        return currentRoomId != null;
    }
    
    public int getMySubmissionAttempts() {
        return mySubmissionAttempts;
    }
}
