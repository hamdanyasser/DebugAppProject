package com.example.debugappproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DEBUGMASTER - PREMIUM SOUND EFFECTS MANAGER                     ║
 * ║                 AAA Mobile Game Quality Audio System                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Central sound effects manager for the entire app.
 * Handles all game sounds, UI feedback, and haptics.
 * 
 * NOTE: Sound files should be added to res/raw folder. Until then, haptic
 * feedback will be used as a fallback for all sounds.
 *
 * RECOMMENDED SOUND FILES (free from freesound.org or similar):
 * - button_click.mp3 (short click sound)
 * - button_start.mp3 (game start whoosh)
 * - success.mp3 (positive chime)
 * - failure.mp3 (error buzz)
 * - level_up.mp3 (victory fanfare)
 * - etc.
 */
public class SoundManager {

    private static final String TAG = "SoundManager";
    private static SoundManager instance;

    private final Context context;
    private SoundPool soundPool;
    private final Map<Sound, Integer> soundIds = new HashMap<>();
    private final Map<Sound, Boolean> soundLoaded = new HashMap<>();

    private boolean soundEnabled = true;
    private boolean hapticEnabled = true;
    private float masterVolume = 1.0f;
    private float sfxVolume = 0.8f;

    private Vibrator vibrator;
    private SharedPreferences prefs;

    /**
     * All available sound effects in the game
     */
    public enum Sound {
        // ═══════════════ UI SOUNDS ═══════════════
        BUTTON_CLICK,       // Standard button tap
        BUTTON_START,       // Play/Start button (special)
        BUTTON_BACK,        // Back navigation
        BUTTON_APPEAR,      // Button appears on screen

        // ═══════════════ SPLASH SOUNDS ═══════════════
        AMBIENT_INTRO,      // Splash screen atmosphere
        LOGO_WHOOSH,        // Logo entrance
        TEXT_REVEAL,        // Text appearing
        GLITCH,             // Glitch effect
        TYPING,             // Typing sound
        LOADING_COMPLETE,   // Loading finished
        TRANSITION,         // Screen transition

        // ═══════════════ GAME SOUNDS ═══════════════
        SUCCESS,            // Correct answer / bug fixed
        FAILURE,            // Wrong answer
        LEVEL_UP,           // Level up fanfare
        ACHIEVEMENT_UNLOCK, // Achievement unlocked
        XP_GAIN,            // XP points earned
        COIN_COLLECT,       // Coins collected
        STREAK_INCREASE,    // Streak increased
        COMBO,              // Combo bonus

        // ═══════════════ CODE SOUNDS ═══════════════
        CODE_SUBMIT,        // Code submitted
        CODE_COMPILE,       // Code compiling
        CODE_ERROR,         // Compilation error
        CODE_RUN,           // Code running

        // ═══════════════ FEEDBACK SOUNDS ═══════════════
        ERROR,              // General error
        WARNING,            // Warning notification
        NOTIFICATION,       // General notification
        BLIP,               // Small UI blip
        TICK,               // Timer tick
        COUNTDOWN,          // Countdown beep

        // ═══════════════ SPECIAL SOUNDS ═══════════════
        POWER_UP,           // Power up activated
        HINT_REVEAL,        // Hint revealed
        STAR_EARNED,        // Star rating earned
        PERFECT_SCORE,      // Perfect score achieved
        CHALLENGE_START,    // Challenge begins
        CHALLENGE_COMPLETE, // Challenge completed
        VICTORY,            // Victory fanfare
        DEFEAT              // Defeat sound
    }

    /**
     * Haptic feedback patterns
     */
    public enum Haptic {
        LIGHT,          // Light tap
        MEDIUM,         // Medium tap
        HEAVY,          // Heavy tap
        SUCCESS,        // Success pattern
        ERROR,          // Error pattern
        SELECTION,      // Selection change
        BUTTON          // Button press
    }

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("debugmaster_sound_prefs", Context.MODE_PRIVATE);
        
        loadPreferences();
        initSoundPool();
        initVibrator();
        loadAvailableSounds();
    }

    /**
     * Get singleton instance
     */
    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Load user preferences
     */
    private void loadPreferences() {
        soundEnabled = prefs.getBoolean("sound_enabled", true);
        hapticEnabled = prefs.getBoolean("haptic_enabled", true);
        masterVolume = prefs.getFloat("master_volume", 1.0f);
        sfxVolume = prefs.getFloat("sfx_volume", 0.8f);
    }

    /**
     * Initialize SoundPool with optimal settings
     */
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)  // Allow 10 simultaneous sounds
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                // Find which sound was loaded and mark it
                for (Map.Entry<Sound, Integer> entry : soundIds.entrySet()) {
                    if (entry.getValue() == sampleId) {
                        soundLoaded.put(entry.getKey(), true);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Initialize vibrator
     */
    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    /**
     * Load all available sound resources dynamically
     * This will only load sounds that actually exist in res/raw
     */
    private void loadAvailableSounds() {
        // Map sound enum names to resource names
        Map<Sound, String> soundResourceNames = new HashMap<>();
        soundResourceNames.put(Sound.BUTTON_CLICK, "button_click");
        soundResourceNames.put(Sound.BUTTON_START, "button_start");
        soundResourceNames.put(Sound.BUTTON_BACK, "button_back");
        soundResourceNames.put(Sound.BUTTON_APPEAR, "button_appear");
        soundResourceNames.put(Sound.AMBIENT_INTRO, "ambient_intro");
        soundResourceNames.put(Sound.LOGO_WHOOSH, "logo_whoosh");
        soundResourceNames.put(Sound.TEXT_REVEAL, "text_reveal");
        soundResourceNames.put(Sound.GLITCH, "glitch");
        soundResourceNames.put(Sound.TYPING, "typing");
        soundResourceNames.put(Sound.LOADING_COMPLETE, "loading_complete");
        soundResourceNames.put(Sound.TRANSITION, "transition");
        soundResourceNames.put(Sound.SUCCESS, "success");
        soundResourceNames.put(Sound.FAILURE, "failure");
        soundResourceNames.put(Sound.LEVEL_UP, "level_up");
        soundResourceNames.put(Sound.ACHIEVEMENT_UNLOCK, "achievement_unlock");
        soundResourceNames.put(Sound.XP_GAIN, "xp_gain");
        soundResourceNames.put(Sound.COIN_COLLECT, "coin_collect");
        soundResourceNames.put(Sound.STREAK_INCREASE, "streak_increase");
        soundResourceNames.put(Sound.COMBO, "combo");
        soundResourceNames.put(Sound.CODE_SUBMIT, "code_submit");
        soundResourceNames.put(Sound.CODE_COMPILE, "code_compile");
        soundResourceNames.put(Sound.CODE_ERROR, "code_error");
        soundResourceNames.put(Sound.CODE_RUN, "code_run");
        soundResourceNames.put(Sound.ERROR, "error");
        soundResourceNames.put(Sound.WARNING, "warning");
        soundResourceNames.put(Sound.NOTIFICATION, "notification");
        soundResourceNames.put(Sound.BLIP, "blip");
        soundResourceNames.put(Sound.TICK, "tick");
        soundResourceNames.put(Sound.COUNTDOWN, "countdown");
        soundResourceNames.put(Sound.POWER_UP, "power_up");
        soundResourceNames.put(Sound.HINT_REVEAL, "hint_reveal");
        soundResourceNames.put(Sound.STAR_EARNED, "star_earned");
        soundResourceNames.put(Sound.PERFECT_SCORE, "perfect_score");
        soundResourceNames.put(Sound.CHALLENGE_START, "challenge_start");
        soundResourceNames.put(Sound.CHALLENGE_COMPLETE, "challenge_complete");
        soundResourceNames.put(Sound.VICTORY, "victory");
        soundResourceNames.put(Sound.DEFEAT, "defeat");

        // Try to load each sound that exists
        for (Map.Entry<Sound, String> entry : soundResourceNames.entrySet()) {
            Sound sound = entry.getKey();
            String resourceName = entry.getValue();
            
            int resourceId = context.getResources().getIdentifier(
                    resourceName, "raw", context.getPackageName());
            
            if (resourceId != 0) {
                try {
                    int soundId = soundPool.load(context, resourceId, 1);
                    soundIds.put(sound, soundId);
                    soundLoaded.put(sound, false);
                    Log.d(TAG, "Loaded sound: " + sound.name());
                } catch (Exception e) {
                    Log.w(TAG, "Failed to load sound: " + sound.name());
                }
            }
        }
        
        if (soundIds.isEmpty()) {
            Log.i(TAG, "No sound files found in res/raw - using haptic feedback only");
        } else {
            Log.i(TAG, "Loaded " + soundIds.size() + " sound files");
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                           PLAY SOUND EFFECTS
     * ═══════════════════════════════════════════════════════════════════════════
     */

    /**
     * Play a sound effect
     */
    public void playSound(Sound sound) {
        playSound(sound, sfxVolume, false);
    }

    /**
     * Play a sound effect with custom volume
     */
    public void playSound(Sound sound, float volume) {
        playSound(sound, volume, false);
    }

    /**
     * Play a sound effect with options
     */
    public void playSound(Sound sound, float volume, boolean loop) {
        // Always provide haptic feedback
        provideHapticForSound(sound);
        
        if (!soundEnabled) {
            return;
        }

        Integer soundId = soundIds.get(sound);
        Boolean loaded = soundLoaded.get(sound);

        if (soundId != null && loaded != null && loaded) {
            float finalVolume = volume * masterVolume;
            int loopFlag = loop ? -1 : 0;
            soundPool.play(soundId, finalVolume, finalVolume, 1, loopFlag, 1.0f);
        }
    }

    /**
     * Stop a looping sound
     */
    public void stopSound(Sound sound) {
        Integer soundId = soundIds.get(sound);
        if (soundId != null) {
            soundPool.stop(soundId);
        }
    }

    /**
     * Provide haptic feedback based on sound type
     */
    private void provideHapticForSound(Sound sound) {
        if (!hapticEnabled) return;
        
        switch (sound) {
            case BUTTON_CLICK:
            case BUTTON_BACK:
            case BLIP:
            case TICK:
            case TYPING:
                vibrate(Haptic.LIGHT);
                break;
            case BUTTON_START:
            case TRANSITION:
            case LOADING_COMPLETE:
            case BUTTON_APPEAR:
            case TEXT_REVEAL:
                vibrate(Haptic.MEDIUM);
                break;
            case SUCCESS:
            case ACHIEVEMENT_UNLOCK:
            case LEVEL_UP:
            case VICTORY:
            case PERFECT_SCORE:
            case CHALLENGE_COMPLETE:
            case STAR_EARNED:
            case COMBO:
            case STREAK_INCREASE:
                vibrate(Haptic.SUCCESS);
                break;
            case FAILURE:
            case ERROR:
            case CODE_ERROR:
            case DEFEAT:
                vibrate(Haptic.ERROR);
                break;
            case GLITCH:
            case LOGO_WHOOSH:
            case POWER_UP:
            case CHALLENGE_START:
                vibrate(Haptic.HEAVY);
                break;
            case XP_GAIN:
            case COIN_COLLECT:
            case HINT_REVEAL:
            case CODE_SUBMIT:
            case CODE_COMPILE:
            case CODE_RUN:
                vibrate(Haptic.SELECTION);
                break;
            case AMBIENT_INTRO:
            case COUNTDOWN:
            case WARNING:
            case NOTIFICATION:
            default:
                vibrate(Haptic.LIGHT);
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                           HAPTIC FEEDBACK
     * ═══════════════════════════════════════════════════════════════════════════
     */

    /**
     * Vibrate with a specific pattern
     */
    public void vibrate(Haptic haptic) {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = createVibrationEffect(haptic);
                vibrator.vibrate(effect);
            } else {
                // Legacy vibration
                long duration = getHapticDuration(haptic);
                vibrator.vibrate(duration);
            }
        } catch (Exception e) {
            Log.w(TAG, "Vibration failed: " + e.getMessage());
        }
    }

    /**
     * Create VibrationEffect for modern devices
     */
    private VibrationEffect createVibrationEffect(Haptic haptic) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (haptic) {
                case LIGHT:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                case MEDIUM:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
                case HEAVY:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
                case SUCCESS:
                    return VibrationEffect.createWaveform(new long[]{0, 50, 50, 100}, -1);
                case ERROR:
                    return VibrationEffect.createWaveform(new long[]{0, 100, 50, 100, 50, 100}, -1);
                case SELECTION:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                case BUTTON:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
                default:
                    return VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
            }
        } else {
            // Fallback for API 26-28
            long duration = getHapticDuration(haptic);
            return VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
        }
    }

    /**
     * Get duration for legacy vibration
     */
    private long getHapticDuration(Haptic haptic) {
        switch (haptic) {
            case LIGHT: return 20;
            case MEDIUM: return 50;
            case HEAVY: return 100;
            case SUCCESS: return 150;
            case ERROR: return 200;
            case SELECTION: return 15;
            case BUTTON: return 30;
            default: return 30;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                           SETTINGS CONTROL
     * ═══════════════════════════════════════════════════════════════════════════
     */

    /**
     * Enable or disable sounds
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        prefs.edit().putBoolean("sound_enabled", enabled).apply();
    }

    /**
     * Check if sounds are enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Enable or disable haptic feedback
     */
    public void setHapticEnabled(boolean enabled) {
        this.hapticEnabled = enabled;
        prefs.edit().putBoolean("haptic_enabled", enabled).apply();
    }

    /**
     * Check if haptics are enabled
     */
    public boolean isHapticEnabled() {
        return hapticEnabled;
    }

    /**
     * Set master volume (0.0 - 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        prefs.edit().putFloat("master_volume", masterVolume).apply();
    }

    /**
     * Get master volume
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Set SFX volume (0.0 - 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
        prefs.edit().putFloat("sfx_volume", sfxVolume).apply();
    }

    /**
     * Get SFX volume
     */
    public float getSfxVolume() {
        return sfxVolume;
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                           CONVENIENCE METHODS
     * ═══════════════════════════════════════════════════════════════════════════
     */

    /**
     * Play button click sound + haptic
     */
    public void playButtonClick() {
        playSound(Sound.BUTTON_CLICK);
    }

    /**
     * Play success sound + haptic
     */
    public void playSuccess() {
        playSound(Sound.SUCCESS);
    }

    /**
     * Play failure sound + haptic
     */
    public void playFailure() {
        playSound(Sound.FAILURE);
    }

    /**
     * Play level up fanfare
     */
    public void playLevelUp() {
        playSound(Sound.LEVEL_UP);
    }

    /**
     * Play achievement unlocked
     */
    public void playAchievementUnlock() {
        playSound(Sound.ACHIEVEMENT_UNLOCK);
    }

    /**
     * Play XP gain with varying intensity based on amount
     */
    public void playXpGain(int amount) {
        float volume = Math.min(1.0f, 0.5f + (amount / 200f));
        playSound(Sound.XP_GAIN, volume);
    }

    /**
     * Play coin collect
     */
    public void playCoinCollect() {
        playSound(Sound.COIN_COLLECT);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     *                           LIFECYCLE MANAGEMENT
     * ═══════════════════════════════════════════════════════════════════════════
     */

    /**
     * Release resources when app is destroyed
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundIds.clear();
        soundLoaded.clear();
        instance = null;
    }

    /**
     * Pause all sounds (e.g., when app goes to background)
     */
    public void pauseAll() {
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    /**
     * Resume all sounds
     */
    public void resumeAll() {
        if (soundPool != null) {
            soundPool.autoResume();
        }
    }
}
