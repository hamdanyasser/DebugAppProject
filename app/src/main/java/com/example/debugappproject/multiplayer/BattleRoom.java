package com.example.debugappproject.multiplayer;

import java.util.HashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    BATTLE ROOM MODEL                                         ║
 * ║         Represents a multiplayer battle room in Firebase                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class BattleRoom {
    
    public enum RoomState {
        WAITING,      // Waiting for second player
        STARTING,     // Both players ready, starting countdown
        IN_PROGRESS,  // Battle is active
        FINISHED      // Battle completed
    }
    
    private String roomId;
    private String roomCode;          // 6-character join code
    private String hostId;            // UID of room creator
    private String hostName;          // Display name of host
    private String guestId;           // UID of guest (null until joined)
    private String guestName;         // Display name of guest
    private RoomState state;
    private int bugId;                // The bug challenge for this battle
    private long createdAt;
    private long startedAt;
    private long expiresAt;
    
    // Player progress (0-100)
    private int hostProgress;
    private int guestProgress;
    
    // Player submissions
    private String hostSubmission;
    private String guestSubmission;
    private long hostSubmitTime;
    private long guestSubmitTime;
    private boolean hostCorrect;
    private boolean guestCorrect;
    
    // Results
    private String winnerId;
    private String winReason;
    
    // For Firebase deserialization
    public BattleRoom() {}
    
    public BattleRoom(String roomId, String roomCode, String hostId, String hostName, int bugId) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.hostId = hostId;
        this.hostName = hostName;
        this.bugId = bugId;
        this.state = RoomState.WAITING;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + (5 * 60 * 1000); // 5 minutes
        this.hostProgress = 0;
        this.guestProgress = 0;
    }
    
    // Convert to Firebase map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("roomCode", roomCode);
        map.put("hostId", hostId);
        map.put("hostName", hostName);
        map.put("guestId", guestId);
        map.put("guestName", guestName);
        map.put("state", state != null ? state.name() : RoomState.WAITING.name());
        map.put("bugId", bugId);
        map.put("createdAt", createdAt);
        map.put("startedAt", startedAt);
        map.put("expiresAt", expiresAt);
        map.put("hostProgress", hostProgress);
        map.put("guestProgress", guestProgress);
        map.put("hostSubmission", hostSubmission);
        map.put("guestSubmission", guestSubmission);
        map.put("hostSubmitTime", hostSubmitTime);
        map.put("guestSubmitTime", guestSubmitTime);
        map.put("hostCorrect", hostCorrect);
        map.put("guestCorrect", guestCorrect);
        map.put("winnerId", winnerId);
        map.put("winReason", winReason);
        return map;
    }
    
    // Getters and setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    
    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    
    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public RoomState getState() { return state; }
    public void setState(RoomState state) { this.state = state; }
    
    public int getBugId() { return bugId; }
    public void setBugId(int bugId) { this.bugId = bugId; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    
    public int getHostProgress() { return hostProgress; }
    public void setHostProgress(int hostProgress) { this.hostProgress = hostProgress; }
    
    public int getGuestProgress() { return guestProgress; }
    public void setGuestProgress(int guestProgress) { this.guestProgress = guestProgress; }
    
    public String getHostSubmission() { return hostSubmission; }
    public void setHostSubmission(String hostSubmission) { this.hostSubmission = hostSubmission; }
    
    public String getGuestSubmission() { return guestSubmission; }
    public void setGuestSubmission(String guestSubmission) { this.guestSubmission = guestSubmission; }
    
    public long getHostSubmitTime() { return hostSubmitTime; }
    public void setHostSubmitTime(long hostSubmitTime) { this.hostSubmitTime = hostSubmitTime; }
    
    public long getGuestSubmitTime() { return guestSubmitTime; }
    public void setGuestSubmitTime(long guestSubmitTime) { this.guestSubmitTime = guestSubmitTime; }

    public boolean isHostCorrect() { return hostCorrect; }
    public void setHostCorrect(boolean hostCorrect) { this.hostCorrect = hostCorrect; }

    public boolean isGuestCorrect() { return guestCorrect; }
    public void setGuestCorrect(boolean guestCorrect) { this.guestCorrect = guestCorrect; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    
    public String getWinReason() { return winReason; }
    public void setWinReason(String winReason) { this.winReason = winReason; }
    
    // Utility methods
    public boolean isHost(String userId) {
        return hostId != null && hostId.equals(userId);
    }
    
    public boolean isFull() {
        return guestId != null && !guestId.isEmpty();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    public String getOpponentName(String myUserId) {
        if (isHost(myUserId)) {
            return guestName != null ? guestName : "Opponent";
        } else {
            return hostName != null ? hostName : "Opponent";
        }
    }
    
    public int getOpponentProgress(String myUserId) {
        return isHost(myUserId) ? guestProgress : hostProgress;
    }
    
    public int getMyProgress(String myUserId) {
        return isHost(myUserId) ? hostProgress : guestProgress;
    }
}
