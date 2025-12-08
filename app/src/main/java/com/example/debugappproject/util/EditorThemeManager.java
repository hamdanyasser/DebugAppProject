package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.debugappproject.R;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - EDITOR THEME MANAGER                              ║
 * ║           Customize your debugging experience! 🎨                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Features:
 * - 5 Editor Themes: Matrix, Hacker Green, Neon, Midnight, Sunset
 * - 3 Code Fonts: JetBrains Mono, Fira Code, Cascadia Code
 * - XP-based unlocking system
 * - Persistent preferences
 */
public class EditorThemeManager {

    private static final String PREFS_NAME = "editor_theme_prefs";
    private static final String KEY_CURRENT_THEME = "current_theme";
    private static final String KEY_CURRENT_FONT = "current_font";
    private static final String KEY_UNLOCKED_THEMES = "unlocked_themes";
    private static final String KEY_UNLOCKED_FONTS = "unlocked_fonts";

    private final Context context;
    private final SharedPreferences prefs;

    // ═══════════════════════════════════════════════════════════════════════════
    // EDITOR THEMES
    // ═══════════════════════════════════════════════════════════════════════════

    public enum EditorTheme {
        DEFAULT("Default", 0, "#1E1E2E", "#CDD6F4", "#89B4FA", "#A6E3A1", "#F38BA8", "#FAB387"),
        MATRIX("Matrix", 500, "#0D0D0D", "#00FF00", "#00FF00", "#33FF33", "#FF0000", "#00CC00"),
        HACKER_GREEN("Hacker Green", 1000, "#0A1F0A", "#39FF14", "#00FF00", "#7CFC00", "#FF4444", "#32CD32"),
        NEON("Neon", 2000, "#0F0F23", "#FF00FF", "#00FFFF", "#FF1493", "#FF6B6B", "#FFD93D"),
        MIDNIGHT("Midnight", 3000, "#0B1120", "#E0E7FF", "#818CF8", "#A5B4FC", "#F87171", "#C084FC"),
        SUNSET("Sunset", 5000, "#1A0A0A", "#FF7F50", "#FF6347", "#FFA07A", "#FF4500", "#FFD700");

        private final String displayName;
        private final int xpRequired;
        private final String backgroundColor;
        private final String textColor;
        private final String keywordColor;
        private final String stringColor;
        private final String errorColor;
        private final String commentColor;

        EditorTheme(String displayName, int xpRequired, String backgroundColor, String textColor,
                   String keywordColor, String stringColor, String errorColor, String commentColor) {
            this.displayName = displayName;
            this.xpRequired = xpRequired;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.keywordColor = keywordColor;
            this.stringColor = stringColor;
            this.errorColor = errorColor;
            this.commentColor = commentColor;
        }

        public String getDisplayName() { return displayName; }
        public int getXpRequired() { return xpRequired; }
        public int getBackgroundColor() { return Color.parseColor(backgroundColor); }
        public int getTextColor() { return Color.parseColor(textColor); }
        public int getKeywordColor() { return Color.parseColor(keywordColor); }
        public int getStringColor() { return Color.parseColor(stringColor); }
        public int getErrorColor() { return Color.parseColor(errorColor); }
        public int getCommentColor() { return Color.parseColor(commentColor); }

        public String getThemeEmoji() {
            switch (this) {
                case MATRIX: return "🟢";
                case HACKER_GREEN: return "💚";
                case NEON: return "✨";
                case MIDNIGHT: return "🌙";
                case SUNSET: return "🌅";
                default: return "🎨";
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDITOR FONTS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum EditorFont {
        DEFAULT("System Default", 0, null),
        JETBRAINS_MONO("JetBrains Mono", 1000, "jetbrains_mono"),
        FIRA_CODE("Fira Code", 2000, "fira_code"),
        CASCADIA_CODE("Cascadia Code", 3000, "cascadia_code");

        private final String displayName;
        private final int xpRequired;
        private final String fontFileName;

        EditorFont(String displayName, int xpRequired, String fontFileName) {
            this.displayName = displayName;
            this.xpRequired = xpRequired;
            this.fontFileName = fontFileName;
        }

        public String getDisplayName() { return displayName; }
        public int getXpRequired() { return xpRequired; }
        public String getFontFileName() { return fontFileName; }

        public String getFontEmoji() {
            switch (this) {
                case JETBRAINS_MONO: return "💎";
                case FIRA_CODE: return "🔥";
                case CASCADIA_CODE: return "⚡";
                default: return "📝";
            }
        }
    }

    public EditorThemeManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Ensure DEFAULT theme and font are always unlocked
        if (!isThemeUnlocked(EditorTheme.DEFAULT)) {
            unlockTheme(EditorTheme.DEFAULT);
        }
        if (!isFontUnlocked(EditorFont.DEFAULT)) {
            unlockFont(EditorFont.DEFAULT);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // THEME MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get the currently selected theme.
     */
    public EditorTheme getCurrentTheme() {
        String themeName = prefs.getString(KEY_CURRENT_THEME, EditorTheme.DEFAULT.name());
        try {
            return EditorTheme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return EditorTheme.DEFAULT;
        }
    }

    /**
     * Set the current theme (if unlocked).
     */
    public boolean setCurrentTheme(EditorTheme theme) {
        if (!isThemeUnlocked(theme)) {
            return false;
        }
        prefs.edit().putString(KEY_CURRENT_THEME, theme.name()).apply();
        return true;
    }

    /**
     * Check if a theme is unlocked.
     */
    public boolean isThemeUnlocked(EditorTheme theme) {
        String unlocked = prefs.getString(KEY_UNLOCKED_THEMES, EditorTheme.DEFAULT.name());
        return unlocked.contains(theme.name());
    }

    /**
     * Unlock a theme.
     */
    public void unlockTheme(EditorTheme theme) {
        String unlocked = prefs.getString(KEY_UNLOCKED_THEMES, EditorTheme.DEFAULT.name());
        if (!unlocked.contains(theme.name())) {
            unlocked = unlocked + "," + theme.name();
            prefs.edit().putString(KEY_UNLOCKED_THEMES, unlocked).apply();
        }
    }

    /**
     * Try to unlock themes based on XP.
     */
    public void checkAndUnlockThemes(int totalXp) {
        for (EditorTheme theme : EditorTheme.values()) {
            if (totalXp >= theme.getXpRequired() && !isThemeUnlocked(theme)) {
                unlockTheme(theme);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FONT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get the currently selected font.
     */
    public EditorFont getCurrentFont() {
        String fontName = prefs.getString(KEY_CURRENT_FONT, EditorFont.DEFAULT.name());
        try {
            return EditorFont.valueOf(fontName);
        } catch (IllegalArgumentException e) {
            return EditorFont.DEFAULT;
        }
    }

    /**
     * Set the current font (if unlocked).
     */
    public boolean setCurrentFont(EditorFont font) {
        if (!isFontUnlocked(font)) {
            return false;
        }
        prefs.edit().putString(KEY_CURRENT_FONT, font.name()).apply();
        return true;
    }

    /**
     * Check if a font is unlocked.
     */
    public boolean isFontUnlocked(EditorFont font) {
        String unlocked = prefs.getString(KEY_UNLOCKED_FONTS, EditorFont.DEFAULT.name());
        return unlocked.contains(font.name());
    }

    /**
     * Unlock a font.
     */
    public void unlockFont(EditorFont font) {
        String unlocked = prefs.getString(KEY_UNLOCKED_FONTS, EditorFont.DEFAULT.name());
        if (!unlocked.contains(font.name())) {
            unlocked = unlocked + "," + font.name();
            prefs.edit().putString(KEY_UNLOCKED_FONTS, unlocked).apply();
        }
    }

    /**
     * Try to unlock fonts based on XP.
     */
    public void checkAndUnlockFonts(int totalXp) {
        for (EditorFont font : EditorFont.values()) {
            if (totalXp >= font.getXpRequired() && !isFontUnlocked(font)) {
                unlockFont(font);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPLY THEME/FONT TO VIEWS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Apply the current theme to an EditText (code editor).
     */
    public void applyThemeToEditor(EditText editText) {
        if (editText == null) return;

        EditorTheme theme = getCurrentTheme();
        editText.setBackgroundColor(theme.getBackgroundColor());
        editText.setTextColor(theme.getTextColor());
        editText.setHintTextColor(theme.getCommentColor());
    }

    /**
     * Apply the current theme to a TextView (code display).
     */
    public void applyThemeToCodeView(TextView textView) {
        if (textView == null) return;

        EditorTheme theme = getCurrentTheme();
        textView.setBackgroundColor(theme.getBackgroundColor());
        textView.setTextColor(theme.getTextColor());
    }

    /**
     * Apply the current font to a TextView.
     */
    public void applyFontToView(TextView textView) {
        if (textView == null) return;

        EditorFont font = getCurrentFont();
        if (font == EditorFont.DEFAULT) {
            textView.setTypeface(Typeface.MONOSPACE);
            return;
        }

        // Try to load custom font from assets
        try {
            Typeface customFont = Typeface.createFromAsset(
                context.getAssets(),
                "fonts/" + font.getFontFileName() + ".ttf"
            );
            textView.setTypeface(customFont);
        } catch (Exception e) {
            // Fallback to monospace
            textView.setTypeface(Typeface.MONOSPACE);
        }
    }

    /**
     * Apply both theme and font to a code view.
     */
    public void applyFullStyle(TextView textView) {
        applyThemeToCodeView(textView);
        applyFontToView(textView);
    }

    /**
     * Apply both theme and font to an editor.
     */
    public void applyFullStyleToEditor(EditText editText) {
        applyThemeToEditor(editText);
        applyFontToView(editText);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UNLOCK STATUS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get count of unlocked themes.
     */
    public int getUnlockedThemeCount() {
        int count = 0;
        for (EditorTheme theme : EditorTheme.values()) {
            if (isThemeUnlocked(theme)) count++;
        }
        return count;
    }

    /**
     * Get count of unlocked fonts.
     */
    public int getUnlockedFontCount() {
        int count = 0;
        for (EditorFont font : EditorFont.values()) {
            if (isFontUnlocked(font)) count++;
        }
        return count;
    }

    /**
     * Get total available themes.
     */
    public int getTotalThemes() {
        return EditorTheme.values().length;
    }

    /**
     * Get total available fonts.
     */
    public int getTotalFonts() {
        return EditorFont.values().length;
    }

    /**
     * Get XP required for next theme unlock.
     */
    public int getXpForNextTheme(int currentXp) {
        for (EditorTheme theme : EditorTheme.values()) {
            if (!isThemeUnlocked(theme) && theme.getXpRequired() > currentXp) {
                return theme.getXpRequired();
            }
        }
        return -1; // All unlocked
    }

    /**
     * Get XP required for next font unlock.
     */
    public int getXpForNextFont(int currentXp) {
        for (EditorFont font : EditorFont.values()) {
            if (!isFontUnlocked(font) && font.getXpRequired() > currentXp) {
                return font.getXpRequired();
            }
        }
        return -1; // All unlocked
    }
}
