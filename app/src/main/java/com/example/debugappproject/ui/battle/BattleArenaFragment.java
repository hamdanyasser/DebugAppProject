package com.example.debugappproject.ui.battle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.databinding.FragmentBattleArenaBinding;
import com.example.debugappproject.databinding.LayoutBattleGameBinding;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.multiplayer.BattleRoom;
import com.example.debugappproject.multiplayer.FirebaseMultiplayerManager;
import com.example.debugappproject.util.AnimationUtil;
import com.example.debugappproject.util.CodeComparator;
import com.example.debugappproject.util.SoundManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - BATTLE ARENA v3.0 (ULTRA ENGAGING!)                  ║
 * ║              Real-time Bug Fixing Competition with Real Players              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * FIXES v3.0:
 * - ✅ Timer now works! Starts immediately + syncs with Firebase
 * - ✅ 3-2-1-GO! Countdown animation before battle
 * - ✅ Combo system for fast correct answers
 * - ✅ Milestone celebrations (25%, 50%, 75% progress)
 * - ✅ Floating emoji reactions
 * - ✅ Enhanced haptic feedback
 * - ✅ Live typing indicator for opponent
 * - ✅ Dramatic low-time warnings
 */
@AndroidEntryPoint
public class BattleArenaFragment extends Fragment implements FirebaseMultiplayerManager.MultiplayerCallback {

    private static final String TAG = "BattleArenaFragment";
    private static final String PREFS_NAME = "battle_stats";
    private static final int BATTLE_DURATION_SECONDS = 180;
    private static final int MATCHMAKING_TIMEOUT_SECONDS = 30;
    
    private FragmentBattleArenaBinding menuBinding;
    private LayoutBattleGameBinding battleBinding;
    private ViewGroup rootContainer;
    
    private BillingManager billingManager;
    private SoundManager soundManager;
    private BugDao bugDao;
    private ExecutorService executor;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private FirebaseMultiplayerManager multiplayerManager;
    private Vibrator vibrator;
    
    private enum GameState { MENU, MATCHMAKING, COUNTDOWN, BATTLE, RESULT }
    private GameState currentState = GameState.MENU;
    
    private boolean isRealMultiplayer = false;
    private BattleRoom currentRoom;
    private CountDownTimer matchmakingTimer;
    
    private Bug currentBug;
    private CountDownTimer battleTimer;
    private String opponentName;
    private int timeRemaining = BATTLE_DURATION_SECONDS;
    private int timeTaken = 0;
    private boolean playerSubmitted = false;
    private boolean opponentSubmitted = false;
    private boolean hintUsed = false;
    private boolean isHost = false;
    
    // Enhanced engagement features
    private int submissionAttempts = 0;
    private boolean waitingForResult = false;
    private int lastMilestone = 0;
    private int comboCount = 0;
    private long lastCorrectTime = 0;
    private boolean timerStarted = false;
    
    // Floating emoji system
    private final String[] CELEBRATION_EMOJIS = {"🎉", "🔥", "⚡", "💪", "🚀", "✨", "💯", "🏆"};
    private final String[] TENSION_EMOJIS = {"😰", "😬", "🫣", "😮", "⏰", "💨"};
    
    // AI simulation
    private ValueAnimator opponentProgressAnimator;
    private int opponentTargetTime;
    private boolean opponentWillWin;
    
    // Stats
    private int wins = 0;
    private int losses = 0;
    private int trophies = 100;
    private int currentStreak = 0;
    private List<BattleHistoryItem> battleHistory = new ArrayList<>();
    
    private static final String[] OPPONENT_PREFIXES = {
        "Code", "Bug", "Debug", "Byte", "Pixel", "Logic", "Stack", "Heap", 
        "Java", "Python", "Swift", "Kotlin", "React", "Node", "Rust", "Go"
    };
    private static final String[] OPPONENT_SUFFIXES = {
        "Ninja", "Master", "Slayer", "Hunter", "Wizard", "Pro", "King", "Queen",
        "Lord", "Boss", "Chief", "Guru", "Sage", "Hero", "Legend", "Champion"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootContainer = new FrameLayout(requireContext());
        rootContainer.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        menuBinding = FragmentBattleArenaBinding.inflate(inflater, rootContainer, false);
        rootContainer.addView(menuBinding.getRoot());
        return rootContainer;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        billingManager = BillingManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        bugDao = DebugMasterDatabase.getInstance(requireContext()).bugDao();
        executor = Executors.newSingleThreadExecutor();
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        
        try {
            multiplayerManager = FirebaseMultiplayerManager.getInstance();
            multiplayerManager.setCallback(this);
            multiplayerManager.cleanupExpiredRooms();
        } catch (Exception e) {
            Log.w(TAG, "Firebase not available", e);
            multiplayerManager = null;
        }
        
        loadSavedStats();
        setupMenuUI();
        updateStatsDisplay();
        playEntranceAnimations();
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                    FIREBASE MULTIPLAYER CALLBACKS
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Override
    public void onRoomCreated(BattleRoom room) {
        currentRoom = room;
        isHost = true;
        Log.d(TAG, "Room created: " + room.getRoomCode());
    }
    
    @Override
    public void onRoomJoined(BattleRoom room) {
        currentRoom = room;
        isHost = room.isHost(multiplayerManager.getCurrentUserId());
        opponentName = room.getOpponentName(multiplayerManager.getCurrentUserId());
    }
    
    @Override
    public void onOpponentJoined(BattleRoom room) {
        if (!isAdded()) return;
        
        currentRoom = room;
        opponentName = room.getOpponentName(multiplayerManager.getCurrentUserId());
        
        if (matchmakingTimer != null) {
            matchmakingTimer.cancel();
            matchmakingTimer = null;
        }
        
        soundManager.playSound(SoundManager.Sound.SUCCESS);
        vibratePattern(new long[]{0, 100, 50, 100, 50, 100});
        showFloatingEmoji("🎮", null);
        Toast.makeText(requireContext(), "🎮 " + opponentName + " joined!", Toast.LENGTH_SHORT).show();
        
        if (menuBinding != null && menuBinding.textMatchmakingStatus != null) {
            menuBinding.textMatchmakingStatus.setText("⚔️ Opponent found: " + opponentName + "\n\n🚀 Get ready...");
        }
        
        handler.postDelayed(() -> {
            if (currentState == GameState.MATCHMAKING) {
                loadBugAndStartBattle(room.getBugId());
            }
        }, 1500);
    }
    
    @Override
    public void onOpponentLeft(BattleRoom room) {
        if (!isAdded()) return;
        if (currentState == GameState.BATTLE || currentState == GameState.COUNTDOWN) {
            showFloatingEmoji("🏃", battleBinding != null ? battleBinding.getRoot() : null);
            endBattle(true, "🏃 Opponent disconnected! You win!");
        }
    }
    
    @Override
    public void onGameStateChanged(BattleRoom room) {
        if (!isAdded()) return;
        currentRoom = room;
        
        if (room.getState() == BattleRoom.RoomState.STARTING && currentState == GameState.MATCHMAKING) {
            opponentName = room.getOpponentName(multiplayerManager.getCurrentUserId());
            loadBugAndStartBattle(room.getBugId());
        }
    }
    
    @Override
    public void onOpponentProgress(int progress) {
        if (!isAdded() || battleBinding == null) return;
        
        // Animate progress bar smoothly
        ObjectAnimator animator = ObjectAnimator.ofInt(
            battleBinding.progressOpponent, "progress", 
            battleBinding.progressOpponent.getProgress(), progress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        
        updateOpponentStatus(progress);
        
        // Tension when opponent is close!
        if (progress >= 90 && !playerSubmitted) {
            vibrateShort();
            showFloatingEmoji(TENSION_EMOJIS[new Random().nextInt(TENSION_EMOJIS.length)], battleBinding.getRoot());
        }
    }
    
    private void updateOpponentStatus(int progress) {
        if (battleBinding == null) return;
        
        String status;
        int color;
        
        if (progress >= 95) {
            status = "🔥 ALMOST DONE!";
            color = R.color.error;
            pulseView(battleBinding.textOpponentStatus);
        } else if (progress >= 75) {
            status = "⚡ Racing ahead...";
            color = R.color.xp_gold;
        } else if (progress >= 50) {
            status = "💻 Coding fast...";
            color = R.color.difficulty_easy;
        } else if (progress >= 25) {
            status = "🤔 Figuring it out...";
            color = R.color.text_secondary;
        } else {
            status = "👀 Reading bug...";
            color = R.color.text_secondary;
        }
        
        battleBinding.textOpponentStatus.setText(status);
        battleBinding.textOpponentStatus.setTextColor(getResources().getColor(color, null));
    }
    
    @Override
    public void onOpponentSubmitted(long submitTime) {
        if (!isAdded() || battleBinding == null) return;
        
        opponentSubmitted = true;
        battleBinding.textOpponentStatus.setText("📤 Submitted!");
        battleBinding.textOpponentStatus.setTextColor(getResources().getColor(R.color.xp_gold, null));
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        vibrateShort();
        pulseView(battleBinding.textOpponentStatus);
        showFloatingEmoji("📤", battleBinding.getRoot());
    }
    
    @Override
    public void onOpponentSubmissionResult(boolean isCorrect) {
        if (!isAdded() || battleBinding == null) return;
        
        if (isCorrect) {
            battleBinding.textOpponentStatus.setText("✅ GOT IT!");
            battleBinding.textOpponentStatus.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
            showFloatingEmoji("😱", battleBinding.getRoot());
            vibratePattern(new long[]{0, 200, 100, 200});
        } else {
            battleBinding.textOpponentStatus.setText("❌ Wrong! Retrying...");
            battleBinding.textOpponentStatus.setTextColor(getResources().getColor(R.color.error, null));
            opponentSubmitted = false;
            showFloatingEmoji("😅", battleBinding.getRoot());
        }
    }
    
    @Override
    public void onSubmissionResult(boolean isCorrect, String feedback, int attemptNumber) {
        if (!isAdded() || battleBinding == null) return;
        
        waitingForResult = false;
        
        if (isCorrect) {
            battleBinding.textPlayerStatus.setText("✅ CORRECT!");
            battleBinding.textPlayerStatus.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            vibrateSuccess();
            flashScreen(R.color.difficulty_easy);
            showFloatingEmoji("🎉", battleBinding.getRoot());
            showFloatingEmoji("✅", battleBinding.getRoot());
            
            // Combo bonus
            long now = System.currentTimeMillis();
            if (now - lastCorrectTime < 30000) {
                comboCount++;
                showComboToast(comboCount);
            } else {
                comboCount = 1;
            }
            lastCorrectTime = now;
            
        } else {
            playerSubmitted = false;
            battleBinding.buttonSubmitFix.setEnabled(true);
            battleBinding.buttonSubmitFix.setAlpha(1f);
            
            String statusText = "❌ Try again! (Attempt " + attemptNumber + ")";
            battleBinding.textPlayerStatus.setText(statusText);
            battleBinding.textPlayerStatus.setTextColor(getResources().getColor(R.color.error, null));
            
            soundManager.playSound(SoundManager.Sound.ERROR);
            vibrateError();
            flashScreen(R.color.error);
            AnimationUtil.shakeView(battleBinding.editBattleCode);
            showFloatingEmoji("❌", battleBinding.getRoot());
            
            // Encouraging messages based on attempts
            String encouragement;
            if (attemptNumber >= 5) {
                encouragement = "💡 Use the hint button!";
            } else if (attemptNumber >= 3) {
                encouragement = "🔍 Look closer at the bug...";
            } else {
                encouragement = feedback;
            }
            Toast.makeText(requireContext(), encouragement, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onBothPlayersReady(long serverStartTime) {
        if (!isAdded()) return;
        Log.d(TAG, "Both players ready! Server start time: " + serverStartTime);
    }
    
    @Override
    public void onTimerSync(long serverStartTime, long battleDurationMs) {
        if (!isAdded() || battleBinding == null) return;

        Log.d(TAG, "Timer sync received: serverStartTime=" + serverStartTime + ", duration=" + battleDurationMs);

        // Calculate remaining time
        long now = System.currentTimeMillis();
        long elapsed = now - serverStartTime;
        long remaining = battleDurationMs - elapsed;

        Log.d(TAG, "Timer sync: elapsed=" + elapsed + "ms, remaining=" + remaining + "ms");

        if (remaining <= 0) {
            if (!playerSubmitted && currentState == GameState.BATTLE) {
                endBattle(false, "⏰ Time ran out!");
            }
            return;
        }

        // Restart timer with synced time
        if (battleTimer != null) {
            battleTimer.cancel();
        }

        timeRemaining = (int) (remaining / 1000);
        timeTaken = (int) (elapsed / 1000);
        timerStarted = true;

        startBattleTimerWithDuration(remaining);
    }
    
    @Override
    public void onGameEnded(BattleRoom room) {
        if (!isAdded()) return;
        
        currentRoom = room;
        String myId = multiplayerManager.getCurrentUserId();
        boolean won = myId.equals(room.getWinnerId());
        String reason = room.getWinReason();
        
        if (currentState == GameState.BATTLE || currentState == GameState.COUNTDOWN) {
            endBattle(won, reason != null ? reason : (won ? "🏆 You won!" : "😢 Opponent won!"));
        }
    }
    
    @Override
    public void onMatchFound(BattleRoom room) {
        if (!isAdded()) return;

        currentRoom = room;
        isHost = room.isHost(multiplayerManager.getCurrentUserId());
        opponentName = room.getOpponentName(multiplayerManager.getCurrentUserId());

        if (matchmakingTimer != null) {
            matchmakingTimer.cancel();
            matchmakingTimer = null;
        }

        soundManager.playSound(SoundManager.Sound.SUCCESS);
        vibratePattern(new long[]{0, 100, 50, 100});

        if (menuBinding != null && menuBinding.textMatchmakingStatus != null) {
            menuBinding.textMatchmakingStatus.setText("⚔️ Match found!\n\n🎮 VS " + opponentName);
        }

        handler.postDelayed(() -> {
            if (currentState == GameState.MATCHMAKING) {
                loadBugAndStartBattle(room.getBugId());
            }
        }, 1500);
    }

    @Override
    public void onError(String error) {
        if (!isAdded()) return;
        Log.e(TAG, "Multiplayer error: " + error);
        
        if (currentState == GameState.MATCHMAKING) {
            Toast.makeText(requireContext(), "⚠️ Connection issue. Playing vs AI...", Toast.LENGTH_SHORT).show();
            isRealMultiplayer = false;
            startAIMatchmaking();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         VISUAL EFFECTS & ENGAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Show floating emoji animation
     */
    private void showFloatingEmoji(String emoji, View parent) {
        if (!isAdded() || parent == null) {
            if (battleBinding != null) parent = battleBinding.getRoot();
            else if (menuBinding != null) parent = menuBinding.getRoot();
            else return;
        }
        
        final ViewGroup container = (ViewGroup) parent;
        
        TextView emojiView = new TextView(requireContext());
        emojiView.setText(emoji);
        emojiView.setTextSize(32);
        
        Random random = new Random();
        int startX = random.nextInt(container.getWidth() > 0 ? container.getWidth() : 500);
        int startY = container.getHeight() > 0 ? container.getHeight() : 1000;
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = startX;
        params.topMargin = startY;
        emojiView.setLayoutParams(params);
        
        container.addView(emojiView);
        
        // Float up and fade out
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(emojiView, "translationY", 0, -400);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(emojiView, "alpha", 1f, 0f);
        ObjectAnimator scale = ObjectAnimator.ofFloat(emojiView, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(emojiView, "scaleY", 1f, 1.5f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveUp, fadeOut, scale, scaleY);
        animatorSet.setDuration(1500);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                container.removeView(emojiView);
            }
        });
        animatorSet.start();
    }
    
    /**
     * Show combo toast for consecutive correct answers
     */
    private void showComboToast(int combo) {
        if (combo < 2) return;
        
        String message;
        if (combo >= 5) {
            message = "🔥🔥🔥 " + combo + "x COMBO! UNSTOPPABLE!";
        } else if (combo >= 3) {
            message = "⚡⚡ " + combo + "x COMBO! ON FIRE!";
        } else {
            message = "✨ " + combo + "x COMBO!";
        }
        
        Toast toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
        
        vibratePattern(new long[]{0, 50, 30, 50, 30, 50});
    }
    
    /**
     * Show 3-2-1-GO countdown before battle
     */
    private void showCountdownAnimation(Runnable onComplete) {
        currentState = GameState.COUNTDOWN;
        
        // Create countdown overlay
        FrameLayout overlay = new FrameLayout(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(Color.parseColor("#DD000000"));
        
        TextView countdownText = new TextView(requireContext());
        countdownText.setTextSize(120);
        countdownText.setTextColor(Color.WHITE);
        countdownText.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        countdownText.setLayoutParams(params);
        overlay.addView(countdownText);
        
        rootContainer.addView(overlay);
        
        String[] countdown = {"3️⃣", "2️⃣", "1️⃣", "🚀 GO!"};
        
        for (int i = 0; i < countdown.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                
                countdownText.setText(countdown[index]);
                countdownText.setScaleX(0.3f);
                countdownText.setScaleY(0.3f);
                countdownText.setAlpha(0f);
                
                countdownText.animate()
                    .scaleX(1f).scaleY(1f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .withEndAction(() -> {
                        countdownText.animate()
                            .scaleX(1.5f).scaleY(1.5f)
                            .alpha(0f)
                            .setDuration(400)
                            .setStartDelay(200)
                            .start();
                    })
                    .start();
                
                soundManager.playSound(index == 3 ? SoundManager.Sound.CHALLENGE_START : SoundManager.Sound.NOTIFICATION);
                vibrateShort();
                
                if (index == countdown.length - 1) {
                    handler.postDelayed(() -> {
                        rootContainer.removeView(overlay);
                        if (onComplete != null) onComplete.run();
                    }, 800);
                }
            }, i * 800L);
        }
    }
    
    /**
     * Check and celebrate progress milestones
     */
    private void checkMilestone(int progress) {
        int milestone = 0;
        if (progress >= 75 && lastMilestone < 75) milestone = 75;
        else if (progress >= 50 && lastMilestone < 50) milestone = 50;
        else if (progress >= 25 && lastMilestone < 25) milestone = 25;
        
        if (milestone > 0) {
            lastMilestone = milestone;
            String emoji = milestone == 75 ? "🔥" : (milestone == 50 ? "💪" : "👍");
            showFloatingEmoji(emoji, battleBinding != null ? battleBinding.getRoot() : null);
            vibrateShort();
            
            String msg = milestone + "% complete!";
            Toast.makeText(requireContext(), emoji + " " + msg, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void flashScreen(int colorRes) {
        if (battleBinding == null || battleBinding.getRoot() == null) return;
        
        View overlay = new View(requireContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(getResources().getColor(colorRes, null));
        overlay.setAlpha(0.4f);
        
        ((ViewGroup) battleBinding.getRoot()).addView(overlay);
        
        overlay.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction(() -> {
                if (overlay.getParent() != null) {
                    ((ViewGroup) overlay.getParent()).removeView(overlay);
                }
            })
            .start();
    }
    
    private void pulseView(View view) {
        if (view == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);
        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(scaleX, scaleY);
        pulse.setDuration(300);
        pulse.setInterpolator(new BounceInterpolator());
        pulse.start();
    }
    
    // Haptic feedback methods
    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
    
    private void vibrateSuccess() {
        vibratePattern(new long[]{0, 100, 50, 100, 50, 150});
    }
    
    private void vibrateError() {
        vibratePattern(new long[]{0, 200, 100, 200});
    }
    
    private void vibratePattern(long[] pattern) {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         STATS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void loadSavedStats() {
        wins = prefs.getInt("wins", 0);
        losses = prefs.getInt("losses", 0);
        trophies = prefs.getInt("trophies", 100);
        currentStreak = prefs.getInt("streak", 0);
        
        String historyJson = prefs.getString("history", "[]");
        try {
            Type listType = new TypeToken<List<BattleHistoryItem>>(){}.getType();
            battleHistory = new Gson().fromJson(historyJson, listType);
            if (battleHistory == null) battleHistory = new ArrayList<>();
        } catch (Exception e) {
            battleHistory = new ArrayList<>();
        }
    }
    
    private void saveStats() {
        prefs.edit()
            .putInt("wins", wins)
            .putInt("losses", losses)
            .putInt("trophies", trophies)
            .putInt("streak", currentStreak)
            .putString("history", new Gson().toJson(battleHistory))
            .apply();
    }
    
    private void updateStatsDisplay() {
        if (menuBinding == null) return;
        
        if (menuBinding.textWins != null) menuBinding.textWins.setText(String.valueOf(wins));
        if (menuBinding.textLosses != null) menuBinding.textLosses.setText(String.valueOf(losses));
        if (menuBinding.textWinRate != null) {
            int total = wins + losses;
            int winRate = total > 0 ? (wins * 100) / total : 0;
            menuBinding.textWinRate.setText(winRate + "%");
        }
        if (menuBinding.textTrophies != null) menuBinding.textTrophies.setText(String.format("%,d", trophies));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         MENU UI
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void setupMenuUI() {
        if (menuBinding.buttonBack != null) {
            menuBinding.buttonBack.setOnClickListener(v -> {
                soundManager.playButtonClick();
                handleBackPress();
            });
        }

        if (menuBinding.buttonQuickMatch != null) {
            menuBinding.buttonQuickMatch.setOnClickListener(v -> {
                soundManager.playButtonClick();
                AnimationUtil.animatePress(v, () -> startMatchmaking("random"));
            });
        }

        if (menuBinding.buttonChallengeFriend != null) {
            menuBinding.buttonChallengeFriend.setOnClickListener(v -> {
                soundManager.playButtonClick();
                AnimationUtil.animatePress(v, this::showChallengeFriendDialog);
            });
        }

        if (menuBinding.buttonCreateRoom != null) {
            menuBinding.buttonCreateRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                AnimationUtil.animatePress(v, this::createPrivateRoom);
            });
        }

        if (menuBinding.buttonJoinRoom != null) {
            menuBinding.buttonJoinRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                AnimationUtil.animatePress(v, this::showJoinRoomDialog);
            });
        }

        if (menuBinding.buttonCancelMatchmaking != null) {
            menuBinding.buttonCancelMatchmaking.setOnClickListener(v -> {
                soundManager.playButtonClick();
                cancelMatchmaking();
            });
        }
    }
    
    private void playEntranceAnimations() {
        View[] buttons = {menuBinding.buttonQuickMatch, menuBinding.buttonChallengeFriend, 
                          menuBinding.buttonCreateRoom, menuBinding.buttonJoinRoom};
        for (int i = 0; i < buttons.length; i++) {
            View button = buttons[i];
            if (button != null) {
                button.setAlpha(0f);
                button.setTranslationX(-100f);
                button.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setStartDelay(200 + (i * 100L))
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                     ROOM CREATION & JOINING
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void createPrivateRoom() {
        if (multiplayerManager == null) {
            showFirebaseUnavailableDialog();
            return;
        }
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        showMatchmakingOverlay("🔨 Creating room...");
        currentState = GameState.MATCHMAKING;
        isRealMultiplayer = true;
        resetBattleState();
        
        executor.execute(() -> {
            try {
                int bugCount = bugDao.getBugCount();
                Random random = new Random();
                int bugId = random.nextInt(Math.max(1, bugCount)) + 1;
                
                handler.post(() -> {
                    multiplayerManager.createRoom(requireContext(), bugId, 
                        new FirebaseMultiplayerManager.CreateRoomCallback() {
                            @Override
                            public void onSuccess(BattleRoom room) {
                                if (!isAdded()) return;
                                currentRoom = room;
                                isHost = true;
                                showRoomCreatedDialog(room.getRoomCode());
                                startMatchmakingTimeout();
                            }
                            
                            @Override
                            public void onError(String error) {
                                if (!isAdded()) return;
                                soundManager.playSound(SoundManager.Sound.ERROR);
                                cancelMatchmaking();
                                Toast.makeText(requireContext(), "❌ " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                });
            } catch (Exception e) {
                handler.post(() -> {
                    cancelMatchmaking();
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showRoomCreatedDialog(String roomCode) {
        if (menuBinding != null && menuBinding.textMatchmakingStatus != null) {
            menuBinding.textMatchmakingStatus.setText(
                "🎮 Room Created!\n\n📋 Code: " + roomCode + "\n\nShare with your friend!\n⏳ Waiting...");
        }
        copyToClipboard(roomCode);
        Toast.makeText(requireContext(), "📋 Code copied: " + roomCode, Toast.LENGTH_LONG).show();
    }
    
    private void showJoinRoomDialog() {
        if (multiplayerManager == null) {
            showFirebaseUnavailableDialog();
            return;
        }
        
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter 6-character room code");
        input.setPadding(50, 40, 50, 40);
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        input.setHintTextColor(getResources().getColor(R.color.text_secondary, null));
        input.setFilters(new android.text.InputFilter[]{ new android.text.InputFilter.LengthFilter(6) });
        input.setAllCaps(true);
        input.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        
        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🚪 Join Room")
            .setMessage("Enter the room code from your friend.")
            .setView(container)
            .setPositiveButton("Join", (dialog, which) -> {
                String code = input.getText().toString().trim().toUpperCase();
                if (code.length() != 6) {
                    Toast.makeText(requireContext(), "⚠️ Enter a valid 6-character code", Toast.LENGTH_SHORT).show();
                } else {
                    joinRoom(code);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void joinRoom(String roomCode) {
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        showMatchmakingOverlay("🔗 Joining room " + roomCode + "...");
        currentState = GameState.MATCHMAKING;
        isRealMultiplayer = true;
        resetBattleState();
        
        multiplayerManager.joinRoomByCode(requireContext(), roomCode, 
            new FirebaseMultiplayerManager.JoinRoomCallback() {
                @Override
                public void onSuccess(BattleRoom room) {
                    if (!isAdded()) return;
                    currentRoom = room;
                    isHost = false;
                    opponentName = room.getHostName();
                    soundManager.playSound(SoundManager.Sound.SUCCESS);
                    vibrateSuccess();
                    
                    if (menuBinding != null && menuBinding.textMatchmakingStatus != null) {
                        menuBinding.textMatchmakingStatus.setText("✅ Joined!\n\n⚔️ VS " + opponentName);
                    }
                    
                    handler.postDelayed(() -> {
                        if (currentState == GameState.MATCHMAKING) {
                            loadBugAndStartBattle(room.getBugId());
                        }
                    }, 1500);
                }
                
                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    soundManager.playSound(SoundManager.Sound.ERROR);
                    vibrateError();
                    cancelMatchmaking();
                    
                    new AlertDialog.Builder(requireContext())
                        .setTitle("❌ Could Not Join")
                        .setMessage("Room not found or full.\n\nCode: " + roomCode)
                        .setPositiveButton("Try Again", (d, w) -> showJoinRoomDialog())
                        .setNegativeButton("Quick Match", (d, w) -> startMatchmaking("random"))
                        .show();
                }
            });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         MATCHMAKING
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void startMatchmaking(String mode) {
        currentState = GameState.MATCHMAKING;
        resetBattleState();
        showMatchmakingOverlay("🔍 Searching for opponent...");
        
        if (multiplayerManager != null && mode.equals("random")) {
            isRealMultiplayer = true;
            
            executor.execute(() -> {
                try {
                    int bugCount = bugDao.getBugCount();
                    Random random = new Random();
                    int bugId = random.nextInt(Math.max(1, bugCount)) + 1;
                    
                    handler.post(() -> {
                        multiplayerManager.startMatchmaking(requireContext(), bugId);
                        startMatchmakingTimeout();
                    });
                } catch (Exception e) {
                    handler.post(() -> {
                        isRealMultiplayer = false;
                        startAIMatchmaking();
                    });
                }
            });
        } else {
            isRealMultiplayer = false;
            startAIMatchmaking();
        }
    }
    
    private void startMatchmakingTimeout() {
        matchmakingTimer = new CountDownTimer(MATCHMAKING_TIMEOUT_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (menuBinding != null && menuBinding.textMatchmakingStatus != null) {
                    String currentText = menuBinding.textMatchmakingStatus.getText().toString();
                    if (!currentText.contains("Room Created") && !currentText.contains("Joined")) {
                        menuBinding.textMatchmakingStatus.setText("🔍 Searching...\n\n⏳ " + seconds + "s");
                    }
                }
            }
            
            @Override
            public void onFinish() {
                if (currentState == GameState.MATCHMAKING && isAdded()) {
                    soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                    
                    new AlertDialog.Builder(requireContext())
                        .setTitle("⏰ No Opponent Found")
                        .setMessage("No players available.\n\nPlay against AI instead?")
                        .setPositiveButton("🤖 Play vs AI", (d, w) -> {
                            if (multiplayerManager != null) multiplayerManager.cancelMatchmaking();
                            isRealMultiplayer = false;
                            startAIMatchmaking();
                        })
                        .setNegativeButton("⏳ Keep Waiting", (d, w) -> startMatchmakingTimeout())
                        .setNeutralButton("Cancel", (d, w) -> cancelMatchmaking())
                        .show();
                }
            }
        }.start();
    }
    
    private void startAIMatchmaking() {
        Random random = new Random();
        String prefix = OPPONENT_PREFIXES[random.nextInt(OPPONENT_PREFIXES.length)];
        String suffix = OPPONENT_SUFFIXES[random.nextInt(OPPONENT_SUFFIXES.length)];
        opponentName = prefix + suffix + random.nextInt(100);
        
        int total = wins + losses;
        int winRate = total > 0 ? (wins * 100) / total : 50;
        int opponentWinChance = 35 + (winRate / 5);
        opponentWillWin = random.nextInt(100) < opponentWinChance;
        opponentTargetTime = opponentWillWin ? (40 + random.nextInt(60)) : (80 + random.nextInt(80));
        
        String[] statusMessages = {
            "🔍 Searching for opponent...",
            "🤖 Found: " + opponentName,
            "📦 Loading challenge...",
            "⚔️ Get ready to battle!"
        };

        for (int i = 0; i < statusMessages.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (currentState == GameState.MATCHMAKING && menuBinding != null) {
                    if (menuBinding.textMatchmakingStatus != null) {
                        menuBinding.textMatchmakingStatus.setText(statusMessages[index]);
                    }
                    if (index == statusMessages.length - 1) {
                        handler.postDelayed(this::loadRandomBugForBattle, 800);
                    }
                }
            }, (i + 1) * 700L);
        }
    }
    
    private void showMatchmakingOverlay(String message) {
        if (menuBinding.layoutMatchmaking != null) {
            menuBinding.layoutMatchmaking.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(menuBinding.layoutMatchmaking);
            if (menuBinding.textMatchmakingStatus != null) {
                menuBinding.textMatchmakingStatus.setText(message);
            }
        }
        if (menuBinding.layoutMainMenu != null) {
            AnimationUtil.fadeOut(menuBinding.layoutMainMenu);
        }
    }
    
    private void cancelMatchmaking() {
        currentState = GameState.MENU;
        handler.removeCallbacksAndMessages(null);
        
        if (matchmakingTimer != null) {
            matchmakingTimer.cancel();
            matchmakingTimer = null;
        }
        if (multiplayerManager != null) multiplayerManager.cancelMatchmaking();
        
        isRealMultiplayer = false;
        currentRoom = null;
        
        if (menuBinding.layoutMatchmaking != null) menuBinding.layoutMatchmaking.setVisibility(View.GONE);
        if (menuBinding.layoutMainMenu != null) {
            menuBinding.layoutMainMenu.setVisibility(View.VISIBLE);
            AnimationUtil.fadeIn(menuBinding.layoutMainMenu);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         LOAD BUG & START BATTLE
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void resetBattleState() {
        submissionAttempts = 0;
        waitingForResult = false;
        lastMilestone = 0;
        comboCount = 0;
        playerSubmitted = false;
        opponentSubmitted = false;
        hintUsed = false;
        timerStarted = false;
        timeRemaining = BATTLE_DURATION_SECONDS;
        timeTaken = 0;
    }
    
    private void loadBugAndStartBattle(int bugId) {
        executor.execute(() -> {
            try {
                currentBug = bugDao.getBugByIdSync(bugId);
                if (currentBug == null) currentBug = bugDao.getBugByIdSync(1);
                
                handler.post(() -> {
                    if (currentBug != null && (currentState == GameState.MATCHMAKING || currentState == GameState.MENU)) {
                        initBattleUI();
                    } else {
                        showError("Failed to load challenge");
                        cancelMatchmaking();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    showError("Error: " + e.getMessage());
                    cancelMatchmaking();
                });
            }
        });
    }
    
    private void loadRandomBugForBattle() {
        executor.execute(() -> {
            try {
                int bugCount = bugDao.getBugCount();
                if (bugCount > 0) {
                    Random random = new Random();
                    currentBug = null;
                    
                    for (int attempts = 0; currentBug == null && attempts < 10; attempts++) {
                        currentBug = bugDao.getBugByIdSync(random.nextInt(bugCount) + 1);
                    }
                    
                    handler.post(() -> {
                        if ((currentState == GameState.MATCHMAKING || currentState == GameState.MENU) && currentBug != null) {
                            initBattleUI();
                        } else {
                            showError("No bugs available");
                            cancelMatchmaking();
                        }
                    });
                } else {
                    handler.post(() -> {
                        showError("No bugs in database");
                        cancelMatchmaking();
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    showError("Error: " + e.getMessage());
                    cancelMatchmaking();
                });
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         BATTLE GAMEPLAY
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void initBattleUI() {
        if (currentBug == null || getContext() == null) {
            showError("Failed to load battle");
            cancelMatchmaking();
            return;
        }
        
        if (matchmakingTimer != null) {
            matchmakingTimer.cancel();
            matchmakingTimer = null;
        }
        
        // Setup battle UI
        battleBinding = LayoutBattleGameBinding.inflate(getLayoutInflater(), rootContainer, false);
        menuBinding.getRoot().setVisibility(View.GONE);
        rootContainer.addView(battleBinding.getRoot());
        
        setupBattleUI();
        
        // Show countdown then start
        showCountdownAnimation(this::startBattle);
    }
    
    private void startBattle() {
        currentState = GameState.BATTLE;
        
        // Start battle on Firebase if host
        if (isRealMultiplayer && isHost && multiplayerManager != null) {
            multiplayerManager.startBattle();
        }
        
        // ALWAYS start timer immediately - will sync later if multiplayer
        if (!timerStarted) {
            startBattleTimerWithDuration(BATTLE_DURATION_SECONDS * 1000L);
            timerStarted = true;
        }
        
        // Start AI simulation if not real multiplayer
        if (!isRealMultiplayer) {
            startOpponentSimulation();
        }
        
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        vibrateSuccess();
    }
    
    private void setupBattleUI() {
        battleBinding.textChallengeTitle.setText(currentBug.getTitle());
        battleBinding.textChallengeDescription.setText(currentBug.getDescription());
        battleBinding.textBuggyCode.setText(currentBug.getBrokenCode());
        battleBinding.editBattleCode.setText(currentBug.getBrokenCode());
        
        String difficulty = currentBug.getDifficulty();
        battleBinding.textBattleDifficulty.setText(difficulty.toUpperCase());
        setDifficultyBackground(difficulty);
        
        String displayName = opponentName + (isRealMultiplayer ? " 👤" : " 🤖");
        battleBinding.textOpponentName.setText(displayName);
        
        battleBinding.editBattleCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                battleBinding.textCharCount.setText(s.length() + " chars");
                updatePlayerProgress(s.toString());
            }
        });
        
        battleBinding.buttonExitBattle.setOnClickListener(v -> {
            soundManager.playButtonClick();
            confirmExitBattle();
        });
        
        battleBinding.buttonHint.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showHint();
        });
        
        battleBinding.buttonSubmitFix.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.CODE_SUBMIT);
            submitSolution();
        });
        
        battleBinding.buttonResultDone.setOnClickListener(v -> {
            soundManager.playButtonClick();
            returnToMenu();
        });
        
        battleBinding.buttonPlayAgain.setOnClickListener(v -> {
            soundManager.playButtonClick();
            returnToMenu();
            handler.postDelayed(() -> startMatchmaking("random"), 300);
        });
    }
    
    private void setDifficultyBackground(String difficulty) {
        int bgRes;
        switch (difficulty.toLowerCase()) {
            case "easy": bgRes = R.drawable.bg_difficulty_easy; break;
            case "hard": bgRes = R.drawable.bg_difficulty_hard; break;
            default: bgRes = R.drawable.bg_difficulty_medium;
        }
        battleBinding.textBattleDifficulty.setBackgroundResource(bgRes);
    }
    
    private void updatePlayerProgress(String currentCode) {
        String fixedCode = currentBug.getFixedCode();
        String normalizedUser = CodeComparator.normalizeCode(currentCode);
        String normalizedFixed = CodeComparator.normalizeCode(fixedCode);
        
        double similarity = CodeComparator.calculateSimilarity(normalizedUser, normalizedFixed);
        int progress = (int) (similarity * 100);
        
        // Animate progress bar
        ObjectAnimator animator = ObjectAnimator.ofInt(
            battleBinding.progressPlayer, "progress", 
            battleBinding.progressPlayer.getProgress(), progress);
        animator.setDuration(200);
        animator.start();
        
        // Send to Firebase
        if (isRealMultiplayer && multiplayerManager != null) {
            multiplayerManager.updateProgress(progress);
        }
        
        // Update status with emoji
        String status;
        int color;
        if (progress >= 95) {
            status = "🔥 ALMOST THERE!";
            color = R.color.difficulty_easy;
        } else if (progress >= 70) {
            status = "⚡ Making progress...";
            color = R.color.xp_gold;
        } else if (progress >= 40) {
            status = "💻 Coding...";
            color = R.color.difficulty_easy;
        } else {
            status = "🤔 Thinking...";
            color = R.color.text_secondary;
        }
        
        battleBinding.textPlayerStatus.setText(status);
        battleBinding.textPlayerStatus.setTextColor(getResources().getColor(color, null));
        
        // Check milestones
        checkMilestone(progress);
    }
    
    private void startBattleTimerWithDuration(long durationMs) {
        if (battleTimer != null) {
            battleTimer.cancel();
        }
        
        battleTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                timeTaken = BATTLE_DURATION_SECONDS - timeRemaining;
                
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                
                if (battleBinding != null) {
                    battleBinding.textBattleTimer.setText(String.format("%d:%02d", minutes, seconds));
                    
                    // Dramatic low-time effects
                    if (timeRemaining <= 10) {
                        battleBinding.textBattleTimer.setTextColor(Color.RED);
                        pulseView(battleBinding.textBattleTimer);
                        soundManager.playSound(SoundManager.Sound.WARNING);
                        vibrateShort();
                        
                        if (!playerSubmitted) {
                            showFloatingEmoji("⏰", battleBinding.getRoot());
                        }
                    } else if (timeRemaining <= 30) {
                        battleBinding.textBattleTimer.setTextColor(getResources().getColor(R.color.error, null));
                        if (timeRemaining == 30) {
                            soundManager.playSound(SoundManager.Sound.WARNING);
                            vibratePattern(new long[]{0, 100, 50, 100});
                            Toast.makeText(requireContext(), "⚠️ 30 seconds left!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (timeRemaining <= 60) {
                        battleBinding.textBattleTimer.setTextColor(getResources().getColor(R.color.xp_gold, null));
                        if (timeRemaining == 60) {
                            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
                            Toast.makeText(requireContext(), "⏱️ 1 minute remaining!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            
            @Override
            public void onFinish() {
                if (currentState == GameState.BATTLE && !playerSubmitted) {
                    endBattle(false, "⏰ Time ran out!");
                }
            }
        }.start();
    }
    
    private void startOpponentSimulation() {
        String[] statuses = {"🤔 Thinking...", "💻 Typing...", "🧪 Testing...", "📝 Reviewing...", "🔍 Debugging..."};
        Random random = new Random();
        
        opponentProgressAnimator = ValueAnimator.ofInt(0, 100);
        opponentProgressAnimator.setDuration(opponentTargetTime * 1000L);
        opponentProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        opponentProgressAnimator.addUpdateListener(animation -> {
            if (battleBinding != null && currentState == GameState.BATTLE && !opponentSubmitted) {
                int progress = (int) animation.getAnimatedValue();
                battleBinding.progressOpponent.setProgress(progress);
                
                if (progress % 15 == 0) {
                    battleBinding.textOpponentStatus.setText(statuses[random.nextInt(statuses.length)]);
                }
                
                // Tension!
                if (progress >= 90 && !playerSubmitted) {
                    battleBinding.textOpponentStatus.setTextColor(getResources().getColor(R.color.error, null));
                }
            }
        });
        opponentProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentState == GameState.BATTLE && !opponentSubmitted && !playerSubmitted) {
                    opponentSubmitted = true;
                    if (battleBinding != null) {
                        battleBinding.textOpponentStatus.setText("📤 Submitted!");
                        battleBinding.textOpponentStatus.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
                        showFloatingEmoji("📤", battleBinding.getRoot());
                    }
                    if (opponentWillWin) {
                        handler.postDelayed(() -> {
                            if (!playerSubmitted && currentState == GameState.BATTLE) {
                                endBattle(false, "🤖 " + opponentName + " fixed it first!");
                            }
                        }, 2500);
                    }
                }
            }
        });
        opponentProgressAnimator.start();
    }
    
    private void showHint() {
        if (hintUsed) {
            Toast.makeText(requireContext(), "💡 Hint already used!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        hintUsed = true;
        battleBinding.textHint.setText(currentBug.getHintText());
        battleBinding.cardHint.setVisibility(View.VISIBLE);
        AnimationUtil.fadeInWithScale(battleBinding.cardHint);
        
        battleBinding.buttonHint.setEnabled(false);
        battleBinding.buttonHint.setAlpha(0.5f);
        battleBinding.buttonHint.setText("💡 Used");
        soundManager.playSound(SoundManager.Sound.HINT_REVEAL);
        showFloatingEmoji("💡", battleBinding.getRoot());
    }
    
    private void submitSolution() {
        if (waitingForResult) {
            Toast.makeText(requireContext(), "⏳ Checking your answer...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userCode = battleBinding.editBattleCode.getText().toString().trim();
        
        if (userCode.isEmpty()) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "⚠️ Please enter your fix!", Toast.LENGTH_SHORT).show();
            AnimationUtil.shakeView(battleBinding.editBattleCode);
            return;
        }
        
        if (CodeComparator.normalizeCode(userCode).equals(CodeComparator.normalizeCode(currentBug.getBrokenCode()))) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            Toast.makeText(requireContext(), "⚠️ You need to fix the bug first!", Toast.LENGTH_SHORT).show();
            AnimationUtil.shakeView(battleBinding.editBattleCode);
            vibrateError();
            return;
        }
        
        submissionAttempts++;
        
        battleBinding.buttonSubmitFix.setEnabled(false);
        battleBinding.buttonSubmitFix.setAlpha(0.5f);
        battleBinding.textPlayerStatus.setText("⏳ Checking...");
        
        // Validate solution
        String normalizedUser = CodeComparator.normalizeCode(userCode);
        String normalizedFixed = CodeComparator.normalizeCode(currentBug.getFixedCode());
        String normalizedBroken = CodeComparator.normalizeCode(currentBug.getBrokenCode());
        
        double similarityToFixed = CodeComparator.calculateSimilarity(normalizedUser, normalizedFixed);
        double similarityToBroken = CodeComparator.calculateSimilarity(normalizedUser, normalizedBroken);
        
        boolean isCorrect = CodeComparator.codesMatch(userCode, currentBug.getFixedCode());
        
        // Flexible matching
        if (!isCorrect && similarityToFixed >= 0.98 && similarityToFixed > similarityToBroken + 0.05) {
            isCorrect = true;
        }
        if (isCorrect && similarityToBroken > similarityToFixed) {
            isCorrect = false;
        }
        
        // Send to Firebase or handle locally
        if (isRealMultiplayer && multiplayerManager != null) {
            waitingForResult = true;
            playerSubmitted = isCorrect;
            multiplayerManager.submitSolution(userCode, isCorrect);
            if (!isCorrect) {
                waitingForResult = false;
            }
        } else {
            handleLocalSubmission(isCorrect, similarityToFixed);
        }
    }
    
    private void handleLocalSubmission(boolean isCorrect, double similarity) {
        if (isCorrect) {
            playerSubmitted = true;
            battleBinding.textPlayerStatus.setText("✅ CORRECT!");
            battleBinding.textPlayerStatus.setTextColor(getResources().getColor(R.color.difficulty_easy, null));
            soundManager.playSound(SoundManager.Sound.SUCCESS);
            vibrateSuccess();
            flashScreen(R.color.difficulty_easy);
            showFloatingEmoji("🎉", battleBinding.getRoot());
            showFloatingEmoji("✅", battleBinding.getRoot());
            endBattle(true, "🎉 You fixed the bug!");
        } else {
            playerSubmitted = false;
            battleBinding.buttonSubmitFix.setEnabled(true);
            battleBinding.buttonSubmitFix.setAlpha(1f);
            
            String feedback;
            if (similarity >= 0.90) {
                feedback = "🔥 SO close! Double-check syntax!";
            } else if (similarity >= 0.70) {
                feedback = "⚡ Getting there! Keep going!";
            } else {
                feedback = "❌ Not quite. (Attempt " + submissionAttempts + ")";
            }
            
            battleBinding.textPlayerStatus.setText(feedback);
            battleBinding.textPlayerStatus.setTextColor(getResources().getColor(R.color.error, null));
            
            soundManager.playSound(SoundManager.Sound.ERROR);
            vibrateError();
            flashScreen(R.color.error);
            AnimationUtil.shakeView(battleBinding.editBattleCode);
            showFloatingEmoji("❌", battleBinding.getRoot());
            
            if (submissionAttempts >= 3 && !hintUsed) {
                Toast.makeText(requireContext(), "💡 Try using the hint button!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), feedback, Toast.LENGTH_LONG).show();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                         BATTLE RESULTS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void endBattle(boolean won, String reason) {
        if (currentState == GameState.RESULT) return;
        currentState = GameState.RESULT;
        
        if (battleTimer != null) battleTimer.cancel();
        if (opponentProgressAnimator != null) opponentProgressAnimator.cancel();
        handler.removeCallbacksAndMessages(null);
        
        if (isRealMultiplayer && multiplayerManager != null) {
            multiplayerManager.leaveRoom();
        }
        
        Random random = new Random();
        int baseTrophies = won ? 25 : -15;
        int bonusTrophies = random.nextInt(15);
        int trophyChange = won ? (baseTrophies + bonusTrophies) : baseTrophies;
        
        if (won && isRealMultiplayer) trophyChange += 10;
        if (won && comboCount >= 2) trophyChange += comboCount * 3;
        
        if (won) {
            currentStreak++;
            if (currentStreak >= 3) trophyChange += currentStreak * 2;
        } else {
            currentStreak = 0;
        }
        
        int xpEarned = won ? (50 + random.nextInt(25)) : 10;
        if (hintUsed && won) xpEarned -= 10;
        if (timeTaken < 60 && won) xpEarned += 20; // Speed bonus
        
        if (won) {
            wins++;
            soundManager.playSound(SoundManager.Sound.VICTORY);
            for (int i = 0; i < 5; i++) {
                handler.postDelayed(() -> showFloatingEmoji(
                    CELEBRATION_EMOJIS[random.nextInt(CELEBRATION_EMOJIS.length)], 
                    battleBinding.getRoot()), i * 200L);
            }
        } else {
            losses++;
            soundManager.playSound(SoundManager.Sound.DEFEAT);
        }
        trophies = Math.max(0, trophies + trophyChange);
        
        battleHistory.add(0, new BattleHistoryItem(opponentName, won, trophyChange, System.currentTimeMillis()));
        if (battleHistory.size() > 20) battleHistory = battleHistory.subList(0, 20);
        
        saveStats();
        showResultOverlay(won, reason, trophyChange, xpEarned);
    }
    
    private void showResultOverlay(boolean won, String reason, int trophyChange, int xpEarned) {
        if (battleBinding == null) return;
        
        battleBinding.textResultEmoji.setText(won ? "🏆" : "😢");
        battleBinding.textResultTitle.setText(won ? "VICTORY!" : "DEFEAT");
        battleBinding.textResultTitle.setTextColor(won ? 
            getResources().getColor(R.color.difficulty_easy, null) :
            getResources().getColor(R.color.error, null));
        battleBinding.textResultMessage.setText(reason);
        
        String trophyText = (trophyChange >= 0 ? "+" : "") + trophyChange;
        battleBinding.textTrophyChange.setText(trophyText);
        battleBinding.textTrophyChange.setTextColor(trophyChange >= 0 ?
            getResources().getColor(R.color.difficulty_easy, null) :
            getResources().getColor(R.color.error, null));
        
        battleBinding.textXpEarned.setText("+" + xpEarned);
        
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        battleBinding.textTimeTaken.setText(String.format("%d:%02d", minutes, seconds));
        
        battleBinding.layoutResult.setVisibility(View.VISIBLE);
        battleBinding.layoutResult.setAlpha(0f);
        battleBinding.layoutResult.animate().alpha(1f).setDuration(300).start();
        
        // Bouncy emoji animation
        battleBinding.textResultEmoji.setScaleX(0f);
        battleBinding.textResultEmoji.setScaleY(0f);
        battleBinding.textResultEmoji.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setInterpolator(new BounceInterpolator())
            .setStartDelay(200)
            .start();
    }
    
    private void returnToMenu() {
        currentState = GameState.MENU;
        isRealMultiplayer = false;
        currentRoom = null;
        
        if (battleBinding != null) {
            rootContainer.removeView(battleBinding.getRoot());
            battleBinding = null;
        }
        
        menuBinding.getRoot().setVisibility(View.VISIBLE);
        if (menuBinding.layoutMatchmaking != null) menuBinding.layoutMatchmaking.setVisibility(View.GONE);
        if (menuBinding.layoutMainMenu != null) menuBinding.layoutMainMenu.setVisibility(View.VISIBLE);
        
        updateStatsDisplay();
        resetBattleState();
        currentBug = null;
        opponentName = null;
    }
    
    private void confirmExitBattle() {
        new AlertDialog.Builder(requireContext())
            .setTitle("🚪 Leave Battle?")
            .setMessage("You will forfeit and lose trophies.")
            .setPositiveButton("Leave", (d, w) -> endBattle(false, "You forfeited."))
            .setNegativeButton("Stay", null)
            .show();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //                      UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void showChallengeFriendDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("👥 Challenge a Friend")
            .setMessage("To challenge a friend:\n\n" +
                       "1. Tap 'Create Room' for a code\n" +
                       "2. Share the code with your friend\n" +
                       "3. They tap 'Join Room' and enter it\n\n" +
                       "Battle the same bug in real-time!")
            .setPositiveButton("Create Room", (d, w) -> createPrivateRoom())
            .setNeutralButton("Join Room", (d, w) -> showJoinRoomDialog())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showFirebaseUnavailableDialog() {
        soundManager.playSound(SoundManager.Sound.ERROR);
        new AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Multiplayer Unavailable")
            .setMessage("Real-time multiplayer requires internet.\n\nPlay against AI instead?")
            .setPositiveButton("🤖 Play vs AI", (d, w) -> startMatchmaking("ai"))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Room Code", text);
        clipboard.setPrimaryClip(clip);
    }
    
    private void handleBackPress() {
        switch (currentState) {
            case BATTLE: 
            case COUNTDOWN:
                confirmExitBattle(); 
                break;
            case MATCHMAKING: cancelMatchmaking(); break;
            case RESULT: returnToMenu(); break;
            default:
                if (getView() != null) Navigation.findNavController(getView()).navigateUp();
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (battleTimer != null) battleTimer.cancel();
        if (matchmakingTimer != null) matchmakingTimer.cancel();
        if (opponentProgressAnimator != null) opponentProgressAnimator.cancel();
        if (executor != null) executor.shutdown();
        
        if (multiplayerManager != null) {
            multiplayerManager.setCallback(null);
            if (multiplayerManager.isInRoom()) multiplayerManager.leaveRoom();
        }
        
        menuBinding = null;
        battleBinding = null;
    }
    
    public static class BattleHistoryItem {
        public String opponentName;
        public boolean won;
        public int trophyChange;
        public long timestamp;
        
        public BattleHistoryItem() {}
        
        public BattleHistoryItem(String opponentName, boolean won, int trophyChange, long timestamp) {
            this.opponentName = opponentName;
            this.won = won;
            this.trophyChange = trophyChange;
            this.timestamp = timestamp;
        }
    }
}
