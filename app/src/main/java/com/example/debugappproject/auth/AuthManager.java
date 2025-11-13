package com.example.debugappproject.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AuthManager - Manages authentication state for the app.
 *
 * Supports two modes:
 * 1. Guest Mode (default) - All data stored locally, no sync
 * 2. Signed-in Mode - User authenticated with Firebase, data can sync
 *
 * This is a lightweight wrapper that works even when Firebase is not configured.
 * If Firebase is unavailable, the app continues in guest mode.
 */
public class AuthManager {

    private static final String PREFS_NAME = "DebugMasterAuth";
    private static final String KEY_IS_SIGNED_IN = "is_signed_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String GUEST_USER_ID = "guest_local";

    private final SharedPreferences prefs;
    private final Context context;

    public AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if user is currently signed in (vs guest mode).
     */
    public boolean isSignedIn() {
        return prefs.getBoolean(KEY_IS_SIGNED_IN, false);
    }

    /**
     * Gets the current user ID.
     * Returns "guest_local" for guest mode, or Firebase UID for signed-in users.
     */
    public String getUserId() {
        if (isSignedIn()) {
            return prefs.getString(KEY_USER_ID, GUEST_USER_ID);
        }
        return GUEST_USER_ID;
    }

    /**
     * Gets the user's email (only available when signed in).
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Gets the user's display name (only available when signed in).
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Guest");
    }

    /**
     * Marks user as signed in with the given credentials.
     * Called after successful Firebase authentication.
     *
     * @param userId Firebase UID
     * @param email User email
     * @param name User display name
     */
    public void setSignedIn(String userId, String email, String name) {
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, true)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name != null ? name : "User")
            .apply();
    }

    /**
     * Signs out the user and returns to guest mode.
     * Local data is preserved.
     */
    public void signOut() {
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply();
    }

    /**
     * Checks if Firebase is available and configured.
     * Returns false if google-services.json is missing or Firebase initialization failed.
     */
    public boolean isFirebaseAvailable() {
        // TODO: Add actual Firebase availability check when google-services.json is added
        // For now, return false to ensure app works in guest-only mode
        //
        // Example implementation:
        // try {
        //     FirebaseApp.getInstance();
        //     return true;
        // } catch (IllegalStateException e) {
        //     return false;
        // }
        return false;
    }

    /**
     * Gets singleton instance (optional - can also use dependency injection).
     */
    private static AuthManager instance;

    public static AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }
}
