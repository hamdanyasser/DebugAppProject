package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - FRIENDS MANAGER                                   ║
 * ║           Friends System & Social Leaderboard 👥                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - Unique user ID for friend adding
 * - Add/remove friends by ID
 * - Friends leaderboard
 * - Friend activity feed (future)
 */
public class FriendsManager {

    private static final String PREFS_NAME = "friends_manager_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FRIENDS_LIST = "friends_list";
    private static final String KEY_PENDING_REQUESTS = "pending_requests";
    private static final String KEY_DISPLAY_NAME = "display_name";

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public FriendsManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Generate user ID if not exists
        if (getUserId() == null) {
            generateUserId();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USER ID MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get the current user's unique ID.
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Generate a new unique user ID.
     */
    private void generateUserId() {
        // Generate a short, memorable ID (8 characters)
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        prefs.edit().putString(KEY_USER_ID, uuid).apply();
    }

    /**
     * Get formatted user ID for display (e.g., "DEBUG-XXXX-XXXX").
     */
    public String getFormattedUserId() {
        String id = getUserId();
        if (id == null || id.length() < 8) return "DEBUG-????-????";
        return "DEBUG-" + id.substring(0, 4) + "-" + id.substring(4, 8);
    }

    /**
     * Set display name.
     */
    public void setDisplayName(String name) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    /**
     * Get display name.
     */
    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "Anonymous Debugger");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FRIENDS LIST MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get list of friends.
     */
    public List<Friend> getFriends() {
        String json = prefs.getString(KEY_FRIENDS_LIST, "[]");
        Type listType = new TypeToken<List<Friend>>(){}.getType();
        List<Friend> friends = gson.fromJson(json, listType);
        return friends != null ? friends : new ArrayList<>();
    }

    /**
     * Save friends list.
     */
    private void saveFriends(List<Friend> friends) {
        prefs.edit().putString(KEY_FRIENDS_LIST, gson.toJson(friends)).apply();
    }

    /**
     * Add a friend by ID.
     * Returns true if friend was added successfully.
     */
    public AddFriendResult addFriend(String friendId) {
        if (friendId == null || friendId.isEmpty()) {
            return new AddFriendResult(false, "Invalid friend ID");
        }

        // Clean up the ID (remove dashes and DEBUG- prefix)
        String cleanId = friendId.toUpperCase()
            .replace("DEBUG-", "")
            .replace("-", "")
            .trim();

        if (cleanId.length() != 8) {
            return new AddFriendResult(false, "Friend ID must be 8 characters");
        }

        // Can't add yourself
        if (cleanId.equals(getUserId())) {
            return new AddFriendResult(false, "You can't add yourself as a friend!");
        }

        // Check if already friends
        List<Friend> friends = getFriends();
        for (Friend f : friends) {
            if (f.id.equals(cleanId)) {
                return new AddFriendResult(false, "Already friends with this user");
            }
        }

        // Add friend (in a real app, this would validate against server)
        Friend newFriend = new Friend();
        newFriend.id = cleanId;
        newFriend.displayName = "Debugger #" + cleanId.substring(0, 4);
        newFriend.addedAt = System.currentTimeMillis();
        // Simulate some stats (in real app, fetched from server)
        newFriend.level = (int) (Math.random() * 50) + 1;
        newFriend.totalXp = newFriend.level * 100 + (int) (Math.random() * 500);
        newFriend.streak = (int) (Math.random() * 30);

        friends.add(newFriend);
        saveFriends(friends);

        return new AddFriendResult(true, "Friend added successfully!");
    }

    /**
     * Remove a friend.
     */
    public boolean removeFriend(String friendId) {
        List<Friend> friends = getFriends();
        boolean removed = friends.removeIf(f -> f.id.equals(friendId));
        if (removed) {
            saveFriends(friends);
        }
        return removed;
    }

    /**
     * Get friend count.
     */
    public int getFriendCount() {
        return getFriends().size();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FRIENDS LEADERBOARD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get friends leaderboard sorted by XP.
     * Includes the current user.
     */
    public List<Friend> getFriendsLeaderboard(int userXp, int userLevel, int userStreak) {
        List<Friend> leaderboard = new ArrayList<>(getFriends());

        // Add current user
        Friend self = new Friend();
        self.id = getUserId();
        self.displayName = getDisplayName() + " (You)";
        self.totalXp = userXp;
        self.level = userLevel;
        self.streak = userStreak;
        self.isCurrentUser = true;
        leaderboard.add(self);

        // Sort by XP descending
        Collections.sort(leaderboard, (a, b) -> Integer.compare(b.totalXp, a.totalXp));

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).rank = i + 1;
        }

        return leaderboard;
    }

    /**
     * Get user's rank among friends.
     */
    public int getUserRankAmongFriends(int userXp) {
        List<Friend> friends = getFriends();
        int rank = 1;
        for (Friend f : friends) {
            if (f.totalXp > userXp) {
                rank++;
            }
        }
        return rank;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class Friend {
        public String id = "";
        public String displayName = "";
        public int totalXp = 0;
        public int level = 1;
        public int streak = 0;
        public long addedAt = 0;
        public int rank = 0;
        public boolean isCurrentUser = false;

        public String getFormattedId() {
            if (id.length() < 8) return "????-????";
            return id.substring(0, 4) + "-" + id.substring(4, 8);
        }

        public String getRankEmoji() {
            switch (rank) {
                case 1: return "🥇";
                case 2: return "🥈";
                case 3: return "🥉";
                default: return "#" + rank;
            }
        }
    }

    public static class AddFriendResult {
        public final boolean success;
        public final String message;

        public AddFriendResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
