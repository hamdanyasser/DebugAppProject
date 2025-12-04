package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - AUTHENTICATION MANAGER                               ║
 * ║              Local Authentication with Secure Storage                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * • Local user registration and login
 * • Secure password hashing (SHA-256)
 * • Session management
 * • Guest mode support
 * • Profile customization
 */
public class AuthManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AVATAR_EMOJI = "avatar_emoji";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_IS_GUEST = "is_guest";
    
    // User database keys (stored as prefs for simplicity)
    private static final String USERS_PREFS = "users_db";

    private static AuthManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences usersDb;
    private final Context context;
    
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    public static class User {
        public String id;
        public String username;
        public String email;
        public String displayName;
        public String avatarEmoji;
        public long createdAt;
        public boolean isGuest;

        public User() {}

        public User(String id, String username, String email, String displayName, 
                   String avatarEmoji, long createdAt, boolean isGuest) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.displayName = displayName;
            this.avatarEmoji = avatarEmoji;
            this.createdAt = createdAt;
            this.isGuest = isGuest;
        }
    }

    public enum AuthResult {
        SUCCESS,
        EMAIL_EXISTS,
        USERNAME_EXISTS,
        INVALID_EMAIL,
        WEAK_PASSWORD,
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        NETWORK_ERROR
    }

    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.usersDb = context.getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE);
        
        // Load current session
        loadSession();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Register a new user
     */
    public AuthResult register(String username, String email, String password, String displayName) {
        // Validate email
        if (!isValidEmail(email)) {
            return AuthResult.INVALID_EMAIL;
        }
        
        // Validate password
        if (!isValidPassword(password)) {
            return AuthResult.WEAK_PASSWORD;
        }
        
        // Check if email exists
        if (emailExists(email)) {
            return AuthResult.EMAIL_EXISTS;
        }
        
        // Check if username exists
        if (usernameExists(username)) {
            return AuthResult.USERNAME_EXISTS;
        }
        
        // Create user
        String userId = UUID.randomUUID().toString();
        String hashedPassword = hashPassword(password);
        long createdAt = System.currentTimeMillis();
        
        // Store user in database
        SharedPreferences.Editor editor = usersDb.edit();
        editor.putString("user_" + email + "_id", userId);
        editor.putString("user_" + email + "_username", username);
        editor.putString("user_" + email + "_password", hashedPassword);
        editor.putString("user_" + email + "_displayName", displayName);
        editor.putString("user_" + email + "_avatar", "🐛");
        editor.putLong("user_" + email + "_createdAt", createdAt);
        editor.putString("email_" + username.toLowerCase(), email); // Username to email mapping
        editor.apply();
        
        // Auto login after registration
        return login(email, password);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                                 LOGIN
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Login with email/username and password
     */
    public AuthResult login(String emailOrUsername, String password) {
        String email = emailOrUsername;
        
        // Check if it's a username
        if (!emailOrUsername.contains("@")) {
            String mappedEmail = usersDb.getString("email_" + emailOrUsername.toLowerCase(), null);
            if (mappedEmail != null) {
                email = mappedEmail;
            } else {
                return AuthResult.USER_NOT_FOUND;
            }
        }
        
        // Check if user exists
        String storedPassword = usersDb.getString("user_" + email + "_password", null);
        if (storedPassword == null) {
            return AuthResult.USER_NOT_FOUND;
        }
        
        // Verify password
        String hashedPassword = hashPassword(password);
        if (!storedPassword.equals(hashedPassword)) {
            return AuthResult.INVALID_CREDENTIALS;
        }
        
        // Load user data and create session
        String userId = usersDb.getString("user_" + email + "_id", "");
        String username = usersDb.getString("user_" + email + "_username", "");
        String displayName = usersDb.getString("user_" + email + "_displayName", username);
        String avatar = usersDb.getString("user_" + email + "_avatar", "🐛");
        long createdAt = usersDb.getLong("user_" + email + "_createdAt", 0);
        
        // Save session
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_AVATAR_EMOJI, avatar);
        editor.putLong(KEY_CREATED_AT, createdAt);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.apply();
        
        // Update LiveData
        User user = new User(userId, username, email, displayName, avatar, createdAt, false);
        currentUser.postValue(user);
        isLoggedIn.postValue(true);
        
        return AuthResult.SUCCESS;
    }

    /**
     * Continue as guest
     */
    public void loginAsGuest() {
        String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, guestId);
        editor.putString(KEY_USERNAME, "Guest");
        editor.putString(KEY_EMAIL, "");
        editor.putString(KEY_DISPLAY_NAME, "Debug Rookie");
        editor.putString(KEY_AVATAR_EMOJI, "🐞");
        editor.putLong(KEY_CREATED_AT, System.currentTimeMillis());
        editor.putBoolean(KEY_IS_GUEST, true);
        editor.apply();
        
        User guest = new User(guestId, "Guest", "", "Debug Rookie", "🐞", 
                             System.currentTimeMillis(), true);
        currentUser.postValue(guest);
        isLoggedIn.postValue(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                                LOGOUT
    // ═══════════════════════════════════════════════════════════════════════════

    public void logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_AVATAR_EMOJI)
            .remove(KEY_CREATED_AT)
            .remove(KEY_IS_GUEST)
            .apply();
        
        currentUser.postValue(null);
        isLoggedIn.postValue(false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                           PROFILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    public void updateDisplayName(String newName) {
        String email = prefs.getString(KEY_EMAIL, "");
        
        prefs.edit().putString(KEY_DISPLAY_NAME, newName).apply();
        
        if (!email.isEmpty()) {
            usersDb.edit().putString("user_" + email + "_displayName", newName).apply();
        }
        
        // Update current user
        User user = currentUser.getValue();
        if (user != null) {
            user.displayName = newName;
            currentUser.postValue(user);
        }
    }

    public void updateAvatar(String emoji) {
        String email = prefs.getString(KEY_EMAIL, "");
        
        prefs.edit().putString(KEY_AVATAR_EMOJI, emoji).apply();
        
        if (!email.isEmpty()) {
            usersDb.edit().putString("user_" + email + "_avatar", emoji).apply();
        }
        
        User user = currentUser.getValue();
        if (user != null) {
            user.avatarEmoji = emoji;
            currentUser.postValue(user);
        }
    }

    public boolean changePassword(String currentPassword, String newPassword) {
        String email = prefs.getString(KEY_EMAIL, "");
        if (email.isEmpty()) return false;
        
        String storedPassword = usersDb.getString("user_" + email + "_password", "");
        if (!storedPassword.equals(hashPassword(currentPassword))) {
            return false;
        }
        
        if (!isValidPassword(newPassword)) {
            return false;
        }
        
        usersDb.edit()
            .putString("user_" + email + "_password", hashPassword(newPassword))
            .apply();
        
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              GETTERS
    // ═══════════════════════════════════════════════════════════════════════════

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public boolean isLoggedInSync() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, true);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "Debug Master");
    }

    public String getAvatarEmoji() {
        return prefs.getString(KEY_AVATAR_EMOJI, "🐛");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                              HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadSession() {
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            User user = new User(
                prefs.getString(KEY_USER_ID, ""),
                prefs.getString(KEY_USERNAME, ""),
                prefs.getString(KEY_EMAIL, ""),
                prefs.getString(KEY_DISPLAY_NAME, ""),
                prefs.getString(KEY_AVATAR_EMOJI, "🐛"),
                prefs.getLong(KEY_CREATED_AT, 0),
                prefs.getBoolean(KEY_IS_GUEST, true)
            );
            currentUser.postValue(user);
            isLoggedIn.postValue(true);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // At least 6 characters
        return password != null && password.length() >= 6;
    }

    private boolean emailExists(String email) {
        return usersDb.getString("user_" + email + "_id", null) != null;
    }

    private boolean usernameExists(String username) {
        return usersDb.getString("email_" + username.toLowerCase(), null) != null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback (not secure, but won't crash)
        }
    }

    /**
     * Get error message for auth result
     */
    public static String getErrorMessage(AuthResult result) {
        switch (result) {
            case EMAIL_EXISTS:
                return "This email is already registered";
            case USERNAME_EXISTS:
                return "This username is already taken";
            case INVALID_EMAIL:
                return "Please enter a valid email address";
            case WEAK_PASSWORD:
                return "Password must be at least 6 characters";
            case INVALID_CREDENTIALS:
                return "Incorrect email or password";
            case USER_NOT_FOUND:
                return "No account found with this email";
            case NETWORK_ERROR:
                return "Network error. Please try again";
            default:
                return "An error occurred";
        }
    }
}
