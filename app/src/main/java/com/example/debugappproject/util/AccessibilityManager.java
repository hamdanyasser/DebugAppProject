package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - ACCESSIBILITY MANAGER                                ║
 * ║            Making Learning Fun for Everyone, Every Age!                      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * • Kids Mode - Larger text, simpler UI, friendly mascot
 * • Senior Mode - High contrast, extra large text
 * • Dyslexia-friendly fonts
 * • Color blindness support
 * • Screen reader optimization
 */
public class AccessibilityManager {

    private static final String PREFS_NAME = "accessibility_prefs";
    private static final String KEY_KIDS_MODE = "kids_mode";
    private static final String KEY_LARGE_TEXT = "large_text";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_DYSLEXIA_FONT = "dyslexia_font";
    private static final String KEY_REDUCE_MOTION = "reduce_motion";
    private static final String KEY_COLOR_BLIND_MODE = "color_blind_mode";
    private static final String KEY_AGE_GROUP = "age_group";
    
    private static AccessibilityManager instance;
    private final SharedPreferences prefs;
    private final Context context;
    
    // Age groups for content adaptation
    public enum AgeGroup {
        KIDS(6, 12),        // 6-12 years - Simple terms, mascot guides
        TEENS(13, 17),      // 13-17 years - Standard content
        ADULTS(18, 64),     // 18-64 years - Full complexity
        SENIORS(65, 99);    // 65+ years - Larger UI, slower pace
        
        public final int minAge;
        public final int maxAge;
        
        AgeGroup(int minAge, int maxAge) {
            this.minAge = minAge;
            this.maxAge = maxAge;
        }
    }
    
    public enum ColorBlindMode {
        NONE,
        PROTANOPIA,     // Red-blind
        DEUTERANOPIA,   // Green-blind
        TRITANOPIA      // Blue-blind
    }
    
    private AccessibilityManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AccessibilityManager getInstance(Context context) {
        if (instance == null) {
            instance = new AccessibilityManager(context);
        }
        return instance;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                              KIDS MODE
    // ═══════════════════════════════════════════════════════════════════════════
    
    public boolean isKidsMode() {
        return prefs.getBoolean(KEY_KIDS_MODE, false);
    }
    
    public void setKidsMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_KIDS_MODE, enabled).apply();
        if (enabled) {
            // Auto-enable friendly settings
            setLargeText(true);
            setReduceMotion(false); // Kids like animations!
        }
    }
    
    /**
     * Get kid-friendly version of text
     */
    public String getKidFriendlyText(String original, String kidVersion) {
        return isKidsMode() ? kidVersion : original;
    }
    
    /**
     * Get appropriate emoji for kids mode
     */
    public String getMascotEmoji() {
        return isKidsMode() ? "🐞" : "🐛";
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                           TEXT & DISPLAY
    // ═══════════════════════════════════════════════════════════════════════════
    
    public boolean isLargeText() {
        return prefs.getBoolean(KEY_LARGE_TEXT, false);
    }
    
    public void setLargeText(boolean enabled) {
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply();
    }
    
    /**
     * Get text scale multiplier based on settings
     */
    public float getTextScale() {
        if (isKidsMode()) return 1.2f;
        if (isLargeText()) return 1.3f;
        if (getAgeGroup() == AgeGroup.SENIORS) return 1.4f;
        return 1.0f;
    }
    
    public boolean isHighContrast() {
        return prefs.getBoolean(KEY_HIGH_CONTRAST, false);
    }
    
    public void setHighContrast(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }
    
    public boolean isDyslexiaFont() {
        return prefs.getBoolean(KEY_DYSLEXIA_FONT, false);
    }
    
    public void setDyslexiaFont(boolean enabled) {
        prefs.edit().putBoolean(KEY_DYSLEXIA_FONT, enabled).apply();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                              MOTION
    // ═══════════════════════════════════════════════════════════════════════════
    
    public boolean isReduceMotion() {
        return prefs.getBoolean(KEY_REDUCE_MOTION, false);
    }
    
    public void setReduceMotion(boolean enabled) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply();
    }
    
    /**
     * Get animation duration multiplier
     * Returns 0 if animations should be skipped
     */
    public float getAnimationScale() {
        if (isReduceMotion()) return 0f;
        if (getAgeGroup() == AgeGroup.SENIORS) return 1.5f; // Slower for seniors
        return 1.0f;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                           COLOR BLIND
    // ═══════════════════════════════════════════════════════════════════════════
    
    public ColorBlindMode getColorBlindMode() {
        String mode = prefs.getString(KEY_COLOR_BLIND_MODE, "NONE");
        try {
            return ColorBlindMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return ColorBlindMode.NONE;
        }
    }
    
    public void setColorBlindMode(ColorBlindMode mode) {
        prefs.edit().putString(KEY_COLOR_BLIND_MODE, mode.name()).apply();
    }
    
    /**
     * Get color adjusted for color blindness
     */
    public int getAccessibleColor(int originalColor, String colorType) {
        ColorBlindMode mode = getColorBlindMode();
        if (mode == ColorBlindMode.NONE) return originalColor;
        
        // Color adjustments based on type
        switch (mode) {
            case PROTANOPIA:
            case DEUTERANOPIA:
                // Replace red/green with blue/yellow
                if ("success".equals(colorType)) return 0xFF2196F3; // Blue instead of green
                if ("error".equals(colorType)) return 0xFFFF9800; // Orange instead of red
                break;
            case TRITANOPIA:
                // Replace blue with colors they can see
                if ("info".equals(colorType)) return 0xFF9C27B0; // Purple instead of blue
                break;
        }
        return originalColor;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                              AGE GROUP
    // ═══════════════════════════════════════════════════════════════════════════
    
    public AgeGroup getAgeGroup() {
        String group = prefs.getString(KEY_AGE_GROUP, "ADULTS");
        try {
            return AgeGroup.valueOf(group);
        } catch (IllegalArgumentException e) {
            return AgeGroup.ADULTS;
        }
    }
    
    public void setAgeGroup(AgeGroup group) {
        prefs.edit().putString(KEY_AGE_GROUP, group.name()).apply();
        
        // Auto-adjust settings based on age
        switch (group) {
            case KIDS:
                setKidsMode(true);
                break;
            case SENIORS:
                setLargeText(true);
                setReduceMotion(true);
                break;
            default:
                // Keep current settings
                break;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         CONTENT ADAPTATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Get difficulty label appropriate for age group
     */
    public String getDifficultyLabel(String difficulty) {
        if (isKidsMode()) {
            switch (difficulty.toLowerCase()) {
                case "easy": return "🌟 Beginner";
                case "medium": return "⭐ Explorer";
                case "hard": return "🏆 Champion";
                default: return difficulty;
            }
        }
        return difficulty;
    }
    
    /**
     * Get encouraging message appropriate for age
     */
    public String getEncouragingMessage(boolean success) {
        if (isKidsMode()) {
            if (success) {
                String[] messages = {
                    "Awesome job! 🎉",
                    "You're a superstar! ⭐",
                    "Amazing work! 🌟",
                    "You did it! 🎊",
                    "Fantastic! 🏆"
                };
                return messages[(int)(Math.random() * messages.length)];
            } else {
                String[] messages = {
                    "Almost there! Try again! 💪",
                    "You've got this! 🌈",
                    "Keep going, superhero! 🦸",
                    "Don't give up! ⭐",
                    "You're learning! 📚"
                };
                return messages[(int)(Math.random() * messages.length)];
            }
        }
        
        // Default adult messages
        if (success) {
            return "Correct! Well done.";
        }
        return "Not quite. Try again!";
    }
    
    /**
     * Check if hints should be more generous (for kids/beginners)
     */
    public boolean shouldGiveExtraHints() {
        return isKidsMode() || getAgeGroup() == AgeGroup.KIDS;
    }
    
    /**
     * Get timer duration multiplier (kids get more time)
     */
    public float getTimerMultiplier() {
        if (isKidsMode()) return 1.5f;
        if (getAgeGroup() == AgeGroup.SENIORS) return 1.3f;
        return 1.0f;
    }
}
