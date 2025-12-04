package com.example.debugappproject.ui.gamemodes;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.util.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - GAME MODES HUB                                       ║
 * ║         8 Unique Ways to Learn Debugging - AAA Quality                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * GAME MODES:
 * 1. 🎯 Quick Fix - Fix bugs in under 60 seconds
 * 2. ⚔️ Battle Arena - 1v1 competitive debugging
 * 3. 🏃 Speed Run - Complete paths as fast as possible  
 * 4. 🧩 Puzzle Mode - Figure out what's wrong (no hints)
 * 5. 🎓 Tutorial Mode - Step-by-step guided learning
 * 6. 🏆 Daily Challenge - New challenge every day
 * 7. 🎰 Mystery Bug - Random difficulty, bonus rewards
 * 8. 👥 Co-op Mode - Team up with friends (Coming Soon)
 */
public class GameModesFragment extends Fragment {

    private static final String TAG = "GameModesFragment";
    private static final String PREFS_NAME = "game_modes_prefs";
    
    private ViewGroup rootView;
    private SoundManager soundManager;
    private BillingManager billingManager;
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // Game mode cards
    private View cardQuickFix;
    private View cardBattleArena;
    private View cardSpeedRun;
    private View cardPuzzleMode;
    private View cardTutorialMode;
    private View cardDailyChallenge;
    private View cardMysteryBug;
    private View cardCoopMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_game_modes, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        soundManager = SoundManager.getInstance(requireContext());
        billingManager = BillingManager.getInstance(requireContext());
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        findViews();
        setupGameModeCards();
        setupBackButton();
        playEntranceAnimations();
        
        soundManager.playSound(SoundManager.Sound.TRANSITION);
    }
    
    private void findViews() {
        cardQuickFix = rootView.findViewById(R.id.card_quick_fix);
        cardBattleArena = rootView.findViewById(R.id.card_battle_arena);
        cardSpeedRun = rootView.findViewById(R.id.card_speed_run);
        cardPuzzleMode = rootView.findViewById(R.id.card_puzzle_mode);
        cardTutorialMode = rootView.findViewById(R.id.card_tutorial_mode);
        cardDailyChallenge = rootView.findViewById(R.id.card_daily_challenge);
        cardMysteryBug = rootView.findViewById(R.id.card_mystery_bug);
        cardCoopMode = rootView.findViewById(R.id.card_coop_mode);
    }
    
    private void setupBackButton() {
        View backButton = rootView.findViewById(R.id.button_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                soundManager.playButtonClick();
                Navigation.findNavController(requireView()).navigateUp();
            });
        }
    }
    
    private void setupGameModeCards() {
        // 1. Quick Fix Mode - FREE
        setupGameModeCard(cardQuickFix, "🎯", "Quick Fix", 
            "Fix bugs in under 60 seconds! Race against the clock.",
            false, this::startQuickFixMode, "#10B981");
            
        // 2. Battle Arena - FREE
        setupGameModeCard(cardBattleArena, "⚔️", "Battle Arena",
            "Challenge AI or friends in 1v1 competitive debugging!",
            false, this::startBattleArena, "#EF4444");
            
        // 3. Speed Run - PRO
        setupGameModeCard(cardSpeedRun, "🏃", "Speed Run",
            "Complete entire paths as fast as possible. Leaderboards!",
            true, this::startSpeedRun, "#F59E0B");
            
        // 4. Puzzle Mode - FREE
        setupGameModeCard(cardPuzzleMode, "🧩", "Puzzle Mode",
            "Figure out what's wrong with no hints. True debugging!",
            false, this::startPuzzleMode, "#8B5CF6");
            
        // 5. Tutorial Mode - FREE
        setupGameModeCard(cardTutorialMode, "🎓", "Tutorial Mode",
            "Step-by-step guided learning. Perfect for beginners!",
            false, this::startTutorialMode, "#06B6D4");
            
        // 6. Daily Challenge - FREE
        setupGameModeCard(cardDailyChallenge, "🏆", "Daily Challenge",
            "New challenge every day. Earn bonus XP & streaks!",
            false, this::startDailyChallenge, "#EC4899");
            
        // 7. Mystery Bug - PRO
        setupGameModeCard(cardMysteryBug, "🎰", "Mystery Bug",
            "Random difficulty, mystery rewards. Feeling lucky?",
            true, this::startMysteryBug, "#F97316");
            
        // 8. Co-op Mode - COMING SOON
        setupGameModeCard(cardCoopMode, "👥", "Co-op Mode",
            "Team up with friends to solve bugs together!",
            false, this::showComingSoon, "#3B82F6");
        
        // Mark co-op as coming soon
        if (cardCoopMode != null) {
            TextView badge = cardCoopMode.findViewById(R.id.badge_mode);
            if (badge != null) {
                badge.setText("SOON");
                badge.setBackgroundResource(R.drawable.bg_badge_coming_soon);
                badge.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void setupGameModeCard(View card, String emoji, String title, String description,
                                   boolean isPro, Runnable onClickAction, String color) {
        if (card == null) return;
        
        // Set emoji
        TextView emojiView = card.findViewById(R.id.text_mode_emoji);
        if (emojiView != null) emojiView.setText(emoji);
        
        // Set title
        TextView titleView = card.findViewById(R.id.text_mode_title);
        if (titleView != null) titleView.setText(title);
        
        // Set description
        TextView descView = card.findViewById(R.id.text_mode_description);
        if (descView != null) descView.setText(description);
        
        // Set badge
        TextView badge = card.findViewById(R.id.badge_mode);
        if (badge != null) {
            if (isPro) {
                badge.setText("PRO");
                badge.setBackgroundResource(R.drawable.bg_badge_pro);
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setText("FREE");
                badge.setBackgroundResource(R.drawable.bg_badge_free);
                badge.setVisibility(View.VISIBLE);
            }
        }
        
        // Set accent color
        View accentLine = card.findViewById(R.id.accent_line);
        if (accentLine != null) {
            try {
                accentLine.setBackgroundColor(Color.parseColor(color));
            } catch (Exception e) {
                // Ignore color parse errors
            }
        }
        
        // Click listener with animation
        card.setOnClickListener(v -> {
            soundManager.playButtonClick();
            animateCardPress(v, () -> {
                if (isPro && !billingManager.isProUserSync()) {
                    showProUpgradeDialog(title);
                } else {
                    onClickAction.run();
                }
            });
        });
    }
    
    private void animateCardPress(View card, Runnable onComplete) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f);
        
        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleDownX, scaleDownY);
        pressDown.setDuration(100);
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 0.95f, 1f);
        
        AnimatorSet release = new AnimatorSet();
        release.playTogether(scaleUpX, scaleUpY);
        release.setDuration(100);
        release.setInterpolator(new OvershootInterpolator(2f));
        
        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(pressDown, release);
        fullAnimation.start();
        
        handler.postDelayed(onComplete, 200);
    }
    
    private void playEntranceAnimations() {
        View[] cards = {cardQuickFix, cardBattleArena, cardSpeedRun, cardPuzzleMode,
                       cardTutorialMode, cardDailyChallenge, cardMysteryBug, cardCoopMode};
        
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;
            
            card.setAlpha(0f);
            card.setTranslationY(50f);
            
            int delay = 100 + (i * 80);
            handler.postDelayed(() -> {
                if (!isAdded()) return;
                
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(card, "translationY", 50f, 0f);
                
                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(fadeIn, slideUp);
                anim.setDuration(400);
                anim.setInterpolator(new DecelerateInterpolator(2f));
                anim.start();
            }, delay);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //                         GAME MODE LAUNCHERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void startQuickFixMode() {
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        
        // Show countdown dialog then start
        showQuickFixCountdown();
    }
    
    private void showQuickFixCountdown() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("🎯 Quick Fix Mode");
        builder.setMessage("You have 60 seconds to fix each bug!\n\n" +
                          "⏱️ Time Limit: 60 seconds\n" +
                          "💰 Bonus XP for fast fixes\n" +
                          "❌ 3 lives per session\n\n" +
                          "Ready to race against the clock?");
        builder.setPositiveButton("🚀 START!", (dialog, which) -> {
            soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
            navigateToPracticeWithMode("quick_fix");
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void startBattleArena() {
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        try {
            Navigation.findNavController(requireView()).navigate(R.id.battleArenaFragment);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Opening Battle Arena...", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startSpeedRun() {
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        
        // Show speed run mode selection
        String[] paths = {"Getting Started", "Python Power", "Java Mastery", "Data Structures"};
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🏃 Speed Run")
            .setMessage("Complete an entire learning path as fast as possible!\n\n" +
                       "⏱️ Your time is recorded\n" +
                       "🏆 Compete on leaderboards\n" +
                       "⭐ Bonus XP for top times\n\n" +
                       "Select a path to speed run:")
            .setItems(paths, (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                // Navigate to path detail with speed run flag
                Bundle args = new Bundle();
                args.putInt("pathId", which + 1);
                args.putBoolean("speedRun", true);
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.pathDetailFragment, args);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Starting speed run...", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void startPuzzleMode() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🧩 Puzzle Mode")
            .setMessage("True debugging challenge - NO HINTS!\n\n" +
                       "🔍 Figure out what's wrong yourself\n" +
                       "🧠 Tests real debugging skills\n" +
                       "💎 3x XP reward for solving\n\n" +
                       "Are you ready for the real challenge?")
            .setPositiveButton("💪 Bring it on!", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToPracticeWithMode("puzzle");
            })
            .setNegativeButton("Maybe later", null)
            .show();
    }
    
    private void startTutorialMode() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🎓 Tutorial Mode")
            .setMessage("Perfect for beginners!\n\n" +
                       "📚 Step-by-step explanations\n" +
                       "💡 Detailed hints available\n" +
                       "🎯 Focus on learning, not speed\n" +
                       "🔄 Unlimited attempts\n\n" +
                       "Let's learn debugging the right way!")
            .setPositiveButton("📖 Start Learning", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.SUCCESS);
                // Navigate to learning paths
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.learningPathsFragment);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Opening tutorials...", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void startDailyChallenge() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        // Check if already completed today
        long lastCompleted = prefs.getLong("daily_challenge_last", 0);
        long today = System.currentTimeMillis() / 86400000;
        long lastDay = lastCompleted / 86400000;
        
        if (today == lastDay) {
            // Already completed
            new AlertDialog.Builder(requireContext())
                .setTitle("🏆 Daily Challenge")
                .setMessage("You've already completed today's challenge!\n\n" +
                           "✅ Completed\n" +
                           "🔥 Streak: " + prefs.getInt("daily_streak", 1) + " days\n\n" +
                           "Come back tomorrow for a new challenge!")
                .setPositiveButton("OK", null)
                .show();
        } else {
            // New challenge available
            new AlertDialog.Builder(requireContext())
                .setTitle("🏆 Daily Challenge")
                .setMessage("Today's challenge is ready!\n\n" +
                           "🎁 Bonus: 2x XP today!\n" +
                           "🔥 Current streak: " + prefs.getInt("daily_streak", 0) + " days\n\n" +
                           "Complete it to keep your streak going!")
                .setPositiveButton("🚀 Accept Challenge", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                    navigateToPracticeWithMode("daily");
                })
                .setNegativeButton("Later", null)
                .show();
        }
    }
    
    private void startMysteryBug() {
        soundManager.playSound(SoundManager.Sound.POWER_UP);
        
        // Random mystery effects
        String[] effects = {
            "🎁 BONUS: Double XP!",
            "⚡ BOOST: Extra time!",
            "💎 RARE: Triple hints!",
            "🔥 CHALLENGE: Hard mode!",
            "🌟 JACKPOT: 5x rewards!"
        };
        
        Random random = new Random();
        String effect = effects[random.nextInt(effects.length)];
        
        new AlertDialog.Builder(requireContext())
            .setTitle("🎰 Mystery Bug")
            .setMessage("You got a mystery modifier!\n\n" +
                       effect + "\n\n" +
                       "Difficulty: ???\n" +
                       "Rewards: ???\n\n" +
                       "Are you feeling lucky?")
            .setPositiveButton("🎲 Roll the dice!", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
                navigateToPracticeWithMode("mystery");
            })
            .setNegativeButton("Not today", null)
            .show();
    }
    
    private void showComingSoon() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("👥 Co-op Mode Coming Soon!")
            .setMessage("We're working hard on multiplayer co-op!\n\n" +
                       "📅 Expected: Next update\n\n" +
                       "Features:\n" +
                       "• Team up with friends\n" +
                       "• Solve bugs together\n" +
                       "• Voice chat support\n" +
                       "• Team leaderboards\n\n" +
                       "Stay tuned!")
            .setPositiveButton("Can't wait!", null)
            .show();
    }
    
    private void showProUpgradeDialog(String modeName) {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("👑 Unlock " + modeName)
            .setMessage("This game mode is for Pro members!\n\n" +
                       "Pro includes:\n" +
                       "• All 8 game modes\n" +
                       "• 15+ learning paths\n" +
                       "• Unlimited hints\n" +
                       "• No ads\n" +
                       "• Priority support\n\n" +
                       "Upgrade now to unlock everything!")
            .setPositiveButton("🚀 Go Pro", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                try {
                    Navigation.findNavController(requireView())
                        .navigate(R.id.proSubscriptionFragment);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Opening Pro upgrade...", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Maybe later", null)
            .show();
    }
    
    private void navigateToPracticeWithMode(String mode) {
        Bundle args = new Bundle();
        args.putString("gameMode", mode);
        try {
            Navigation.findNavController(requireView())
                .navigate(R.id.gameSessionFragment, args);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Starting " + mode + " mode...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
