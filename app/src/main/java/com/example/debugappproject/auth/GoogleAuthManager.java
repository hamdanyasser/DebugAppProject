package com.example.debugappproject.auth;

import android.content.Context;

/**
 * Stub GoogleAuthManager - Google Sign-In disabled for demo version.
 * All methods return demo/guest values.
 */
public class GoogleAuthManager {

    private static GoogleAuthManager instance;
    private final Context context;

    private GoogleAuthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static GoogleAuthManager getInstance(Context context) {
        if (instance == null) {
            synchronized (GoogleAuthManager.class) {
                if (instance == null) {
                    instance = new GoogleAuthManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Always returns false - sign-in disabled in demo
     */
    public boolean isSignedIn() {
        return false;
    }

    /**
     * Returns null - no user in demo mode
     */
    public UserProfile getCurrentUser() {
        return null;
    }

    public String getUserName() {
        return "Guest";
    }

    public String getUserEmail() {
        return null;
    }

    public String getUserPhotoUrl() {
        return null;
    }

    /**
     * Stub user profile class
     */
    public static class UserProfile {
        public final String userId;
        public final String email;
        public final String displayName;
        public final String photoUrl;
        public final String idToken;

        public UserProfile(String userId, String email, String displayName, String photoUrl, String idToken) {
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
            this.photoUrl = photoUrl;
            this.idToken = idToken;
        }
    }
}
