package com.example.debugappproject.coop;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.debugmaster.app.R;
import com.example.debugappproject.data.local.BugDao;
import com.example.debugappproject.data.local.DebugMasterDatabase;
import com.example.debugappproject.model.Bug;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - CO-OP PAIR PROGRAMMING                               ║
 * ║              Team Up with AI Partners to Debug Together!                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * FEATURES:
 * - Driver/Navigator role switching (like real pair programming!)
 * - AI partner that gives hints and encouragement
 * - Team score with combo bonuses
 * - Role-based gameplay (Driver writes, Navigator guides)
 */
public class CoopFragment extends Fragment {

    private SoundManager soundManager;
    private ExecutorService executor;
    private Handler handler;
    private Random random;
    
    // Views
    private View rootView;
    private LinearLayout menuLayout, sessionLayout;
    private MaterialButton btnQuickMatch, btnCreateRoom, btnJoinRoom, btnSwitchRole, btnSubmit, btnHint;
    private EditText editRoomCode, editCode;
    private TextView textRoomCode, textPartnerName, textRole, textBugTitle, textBugDesc;
    private TextView textPartnerMessage, textTimer, textScore, textStreak;
    private ProgressBar progressPartner;
    private ScrollView scrollCode;
    
    // Game State
    private String currentRoomCode;
    private boolean isDriver = true;
    private Bug currentBug;
    private int teamScore = 0;
    private int streak = 0;
    private int bugsFixed = 0;
    private CountDownTimer gameTimer;
    private String partnerName;
    
    // AI Partner messages
    private static final String[] PARTNER_HINTS_DRIVER = {
        "💬 Partner: Check the loop condition!",
        "💬 Partner: I think there's an off-by-one error...",
        "💬 Partner: What about null checks?",
        "💬 Partner: Try tracing the variable values!",
        "💬 Partner: The bug might be in the return statement"
    };
    
    private static final String[] PARTNER_HINTS_NAVIGATOR = {
        "💬 Partner: I see it! Line 5 looks suspicious",
        "💬 Partner: Wait, let me scroll down...",
        "💬 Partner: I think I found something!",
        "💬 Partner: Check the comparison operator",
        "💬 Partner: The index might be wrong"
    };
    
    private static final String[] PARTNER_ENCOURAGE = {
        "💬 Partner: Nice! Let's keep going!",
        "💬 Partner: Great teamwork! 🎉",
        "💬 Partner: You're on fire! 🔥",
        "💬 Partner: Perfect! Next one!",
        "💬 Partner: We make a great team!"
    };
    
    private static final String[] PARTNER_NAMES = {
        "DebugDuo_Alex", "CodeBuddy_Sam", "BugHunter_Jay", 
        "PairPro_Taylor", "TeamDebug_Morgan", "FixerFriend_Casey"
    };
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coop, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rootView = view;
        soundManager = SoundManager.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        random = new Random();
        
        findViews();
        setupUI();
    }
    
    private void findViews() {
        if (rootView == null) return;
        
        menuLayout = rootView.findViewById(R.id.layout_menu);
        sessionLayout = rootView.findViewById(R.id.layout_session);
        btnQuickMatch = rootView.findViewById(R.id.btn_quick_match);
        btnCreateRoom = rootView.findViewById(R.id.btn_create_room);
        btnJoinRoom = rootView.findViewById(R.id.btn_join_room);
        editRoomCode = rootView.findViewById(R.id.edit_room_code);
        textRoomCode = rootView.findViewById(R.id.text_room_code);
        textPartnerName = rootView.findViewById(R.id.text_partner_name);
        textRole = rootView.findViewById(R.id.text_role);
        textBugTitle = rootView.findViewById(R.id.text_bug_title);
        textBugDesc = rootView.findViewById(R.id.text_bug_desc);
        editCode = rootView.findViewById(R.id.edit_code);
        textPartnerMessage = rootView.findViewById(R.id.text_partner_message);
        btnSwitchRole = rootView.findViewById(R.id.btn_switch_role);
        btnSubmit = rootView.findViewById(R.id.btn_submit);
        btnHint = rootView.findViewById(R.id.btn_hint);
        progressPartner = rootView.findViewById(R.id.progress_partner);
        textTimer = rootView.findViewById(R.id.text_timer);
        textScore = rootView.findViewById(R.id.text_score);
        textStreak = rootView.findViewById(R.id.text_streak);
        scrollCode = rootView.findViewById(R.id.scroll_code);
        
        View backBtn = rootView.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                soundManager.playButtonClick();
                if (sessionLayout != null && sessionLayout.getVisibility() == View.VISIBLE) {
                    showLeaveConfirmation();
                } else if (getView() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            });
        }
    }
    
    private void setupUI() {
        if (btnQuickMatch != null) {
            btnQuickMatch.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                quickMatch();
            });
        }
        
        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                createRoom();
            });
        }
        
        if (btnJoinRoom != null) {
            btnJoinRoom.setOnClickListener(v -> {
                soundManager.playButtonClick();
                if (editRoomCode == null) return;
                String code = editRoomCode.getText().toString().trim().toUpperCase();
                if (code.length() == 6) {
                    joinRoom(code);
                } else {
                    Toast.makeText(getContext(), "Enter 6-character room code", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (btnSwitchRole != null) {
            btnSwitchRole.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                switchRole();
            });
        }
        
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                soundManager.playButtonClick();
                submitSolution();
            });
        }
        
        if (btnHint != null) {
            btnHint.setOnClickListener(v -> {
                soundManager.playButtonClick();
                askPartnerForHint();
            });
        }
    }
    
    private void quickMatch() {
        // Simulate matchmaking
        if (menuLayout != null) menuLayout.setVisibility(View.GONE);
        
        // Show matching animation
        Toast.makeText(getContext(), "🔍 Finding partner...", Toast.LENGTH_SHORT).show();
        
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            currentRoomCode = generateRoomCode();
            isDriver = random.nextBoolean();
            partnerName = PARTNER_NAMES[random.nextInt(PARTNER_NAMES.length)];
            
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
            Toast.makeText(getContext(), "🎉 Matched with " + partnerName + "!", Toast.LENGTH_SHORT).show();
            
            showSession();
            loadBugAndStart();
        }, 2000);
    }
    
    private void createRoom() {
        currentRoomCode = generateRoomCode();
        isDriver = true;
        partnerName = null;
        
        if (textRoomCode != null) textRoomCode.setText("Room: " + currentRoomCode);
        if (textRole != null) textRole.setText("🚗 DRIVER - You write the code");
        if (textPartnerName != null) textPartnerName.setText("⏳ Waiting for partner...");
        
        showSession();
        waitForPartner();
    }
    
    private void joinRoom(String code) {
        currentRoomCode = code;
        isDriver = false;
        partnerName = PARTNER_NAMES[random.nextInt(PARTNER_NAMES.length)];
        
        Toast.makeText(getContext(), "🔗 Joining room...", Toast.LENGTH_SHORT).show();
        
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            if (textRoomCode != null) textRoomCode.setText("Room: " + currentRoomCode);
            if (textRole != null) textRole.setText("🧭 NAVIGATOR - You guide your partner");
            if (textPartnerName != null) textPartnerName.setText("👤 " + partnerName);
            
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
            showSession();
            loadBugAndStart();
        }, 1500);
    }
    
    private void waitForPartner() {
        // Simulate partner joining
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            partnerName = PARTNER_NAMES[random.nextInt(PARTNER_NAMES.length)];
            
            if (textPartnerName != null) textPartnerName.setText("👤 " + partnerName + " joined!");
            soundManager.playSound(SoundManager.Sound.LEVEL_UP);
            
            Toast.makeText(getContext(), "🎉 " + partnerName + " joined your room!", Toast.LENGTH_SHORT).show();
            
            handler.postDelayed(this::loadBugAndStart, 1000);
        }, 3000);
    }
    
    private void loadBugAndStart() {
        if (!isAdded()) return;
        
        executor.execute(() -> {
            if (!isAdded()) return;
            BugDao dao = DebugMasterDatabase.getInstance(requireContext()).bugDao();
            List<Bug> bugs = dao.getAllBugsSync();
            
            if (!bugs.isEmpty()) {
                currentBug = bugs.get(random.nextInt(bugs.size()));
            }
            
            if (isAdded()) {
                handler.post(() -> {
                    if (!isAdded()) return;
                    displayBug();
                    startTimer();
                    startPartnerSimulation();
                });
            }
        });
    }
    
    private void displayBug() {
        if (currentBug == null) return;
        
        if (textBugTitle != null) textBugTitle.setText("🐛 " + currentBug.getTitle());
        if (textBugDesc != null) textBugDesc.setText(currentBug.getDescription());
        
        if (editCode != null) {
            editCode.setText(currentBug.getBrokenCode());
            editCode.setEnabled(isDriver);
            editCode.setTextColor(isDriver ? Color.WHITE : Color.parseColor("#9CA3AF"));
        }
        
        updateRoleDisplay();
        updateScoreDisplay();
    }
    
    private void updateRoleDisplay() {
        if (textRole != null) {
            if (isDriver) {
                textRole.setText("🚗 DRIVER - Fix the code!");
                textRole.setTextColor(Color.parseColor("#22C55E"));
            } else {
                textRole.setText("🧭 NAVIGATOR - Guide your partner!");
                textRole.setTextColor(Color.parseColor("#3B82F6"));
            }
        }
        
        if (editCode != null) {
            editCode.setEnabled(isDriver);
            editCode.setBackgroundColor(isDriver ? Color.parseColor("#1E1E2E") : Color.parseColor("#0F0F1A"));
        }
    }
    
    private void switchRole() {
        isDriver = !isDriver;
        updateRoleDisplay();
        
        teamScore += 5; // Bonus for good teamwork
        updateScoreDisplay();
        
        // Partner acknowledges switch
        showPartnerMessage(isDriver ? "💬 " + partnerName + ": Your turn to drive!" : 
                "💬 " + partnerName + ": I'll take over coding now!");
        
        Toast.makeText(getContext(), "🔄 Roles switched! +5 teamwork bonus", Toast.LENGTH_SHORT).show();
    }
    
    private void startPartnerSimulation() {
        // Simulate partner activity
        schedulePartnerHint();
    }
    
    private void schedulePartnerHint() {
        if (!isAdded() || currentBug == null) return;
        
        int delay = 8000 + random.nextInt(12000); // 8-20 seconds
        
        handler.postDelayed(() -> {
            if (!isAdded() || sessionLayout == null || sessionLayout.getVisibility() != View.VISIBLE) return;
            
            String[] hints = isDriver ? PARTNER_HINTS_NAVIGATOR : PARTNER_HINTS_DRIVER;
            String hint = hints[random.nextInt(hints.length)];
            showPartnerMessage(hint);
            
            // Schedule next hint
            schedulePartnerHint();
        }, delay);
    }
    
    private void showPartnerMessage(String message) {
        if (textPartnerMessage != null && isAdded()) {
            textPartnerMessage.setText(message);
            textPartnerMessage.setVisibility(View.VISIBLE);
            
            // Animate
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(textPartnerMessage,
                    PropertyValuesHolder.ofFloat("alpha", 0f, 1f),
                    PropertyValuesHolder.ofFloat("translationY", 20f, 0f));
            anim.setDuration(300);
            anim.start();
            
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        }
    }
    
    private void askPartnerForHint() {
        if (currentBug == null || getContext() == null) return;
        
        showPartnerMessage("💬 " + partnerName + ": " + currentBug.getHintText());
    }
    
    private void submitSolution() {
        if (currentBug == null || editCode == null || getContext() == null) return;
        
        String submitted = editCode.getText().toString().trim();
        String expected = currentBug.getFixedCode().trim();
        
        // Normalize for comparison
        String normalizedSubmit = submitted.replaceAll("\\s+", " ");
        String normalizedExpect = expected.replaceAll("\\s+", " ");
        
        if (normalizedSubmit.equals(normalizedExpect)) {
            onCorrectSolution();
        } else {
            onWrongSolution();
        }
    }
    
    private void onCorrectSolution() {
        if (gameTimer != null) gameTimer.cancel();
        
        streak++;
        bugsFixed++;
        int xp = 50 + (streak * 10); // Bonus for streak
        teamScore += xp;
        
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
        
        // Partner celebration
        showPartnerMessage(PARTNER_ENCOURAGE[random.nextInt(PARTNER_ENCOURAGE.length)]);
        
        updateScoreDisplay();
        
        Toast.makeText(getContext(), "🎉 FIXED! +" + xp + " Team XP | 🔥 Streak: " + streak, Toast.LENGTH_LONG).show();
        
        // Load next bug after delay
        handler.postDelayed(() -> {
            if (isAdded()) loadBugAndStart();
        }, 2000);
    }
    
    private void onWrongSolution() {
        streak = 0;
        soundManager.playSound(SoundManager.Sound.ERROR);
        
        updateScoreDisplay();
        
        // Partner encouragement
        showPartnerMessage("💬 " + partnerName + ": Almost! Let me look again...");
        
        Toast.makeText(getContext(), "❌ Not quite! Keep trying!", Toast.LENGTH_SHORT).show();
    }
    
    private void updateScoreDisplay() {
        if (textScore != null) textScore.setText("⭐ " + teamScore);
        if (textStreak != null) {
            textStreak.setText("🔥 " + streak);
            textStreak.setVisibility(streak > 0 ? View.VISIBLE : View.GONE);
        }
    }
    
    private void startTimer() {
        if (gameTimer != null) gameTimer.cancel();
        
        gameTimer = new CountDownTimer(300000, 1000) { // 5 minutes
            @Override
            public void onTick(long ms) {
                if (!isAdded()) { cancel(); return; }
                int m = (int)(ms / 60000);
                int s = (int)((ms % 60000) / 1000);
                if (textTimer != null) textTimer.setText(String.format("⏱️ %d:%02d", m, s));
            }
            
            @Override
            public void onFinish() {
                if (!isAdded()) return;
                if (textTimer != null) textTimer.setText("⏱️ Time!");
                showGameOver();
            }
        }.start();
    }
    
    private void showGameOver() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("⏱️ Time's Up!")
                .setMessage("Bugs Fixed: " + bugsFixed + "\n" +
                        "Team Score: " + teamScore + " XP\n" +
                        "Best Streak: " + streak + "\n\n" +
                        "Great teamwork with " + partnerName + "!")
                .setPositiveButton("Play Again", (d, w) -> {
                    resetGame();
                    loadBugAndStart();
                })
                .setNegativeButton("Exit", (d, w) -> leaveRoom())
                .setCancelable(false)
                .show();
    }
    
    private void showLeaveConfirmation() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Leave Session?")
                .setMessage("Your team score: " + teamScore + " XP\n\n" + partnerName + " will miss you!")
                .setPositiveButton("Leave", (d, w) -> leaveRoom())
                .setNegativeButton("Stay", null)
                .show();
    }
    
    private void leaveRoom() {
        if (gameTimer != null) { gameTimer.cancel(); gameTimer = null; }
        handler.removeCallbacksAndMessages(null);
        
        resetGame();
        
        if (menuLayout != null) menuLayout.setVisibility(View.VISIBLE);
        if (sessionLayout != null) sessionLayout.setVisibility(View.GONE);
    }
    
    private void resetGame() {
        currentRoomCode = null;
        teamScore = 0;
        streak = 0;
        bugsFixed = 0;
        partnerName = null;
    }
    
    private void showSession() {
        if (menuLayout != null) menuLayout.setVisibility(View.GONE);
        if (sessionLayout != null) {
            sessionLayout.setVisibility(View.VISIBLE);
            sessionLayout.setAlpha(0f);
            sessionLayout.animate().alpha(1f).setDuration(300).start();
        }
    }
    
    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Exclude confusing chars
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        if (gameTimer != null) { gameTimer.cancel(); gameTimer = null; }
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (executor != null && !executor.isShutdown()) executor.shutdown();
        
        rootView = null;
        menuLayout = null;
        sessionLayout = null;
        btnQuickMatch = null;
        btnCreateRoom = null;
        btnJoinRoom = null;
        btnSwitchRole = null;
        btnSubmit = null;
        btnHint = null;
        editRoomCode = null;
        editCode = null;
        textRoomCode = null;
        textPartnerName = null;
        textRole = null;
        textBugTitle = null;
        textBugDesc = null;
        textPartnerMessage = null;
        textTimer = null;
        textScore = null;
        textStreak = null;
        progressPartner = null;
        scrollCode = null;
    }
}
