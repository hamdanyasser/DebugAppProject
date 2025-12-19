package com.example.debugappproject.ui.gamemodes;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.util.SoundManager;

import java.util.Calendar;
import java.util.Random;

/**
 * Game Modes Hub - All Ways to Master Debugging
 */
public class GameModesFragment extends Fragment {

    private static final String TAG = "GameModesFragment";
    private static final String PREFS_NAME = "game_modes_prefs";

    private ViewGroup rootView; // Clear in onDestroyView()
    private SoundManager soundManager;
    private BillingManager billingManager;
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    // Game mode cards
    private View cardQuickFix, cardBattleArena, cardSpeedRun, cardPuzzleMode;
    private View cardTutorialMode, cardDailyChallenge, cardMysteryBug, cardSurvivalMode;
    
    // NEW FEATURE CARDS
    private View cardAIMentor, cardDebugger, cardCoopMode, cardMultiFile, cardGithubImport;

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
        setupNewFeatureCards();
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
        cardSurvivalMode = rootView.findViewById(R.id.card_survival_mode);
        
        // NEW FEATURE CARDS
        cardAIMentor = rootView.findViewById(R.id.card_ai_mentor);
        cardDebugger = rootView.findViewById(R.id.card_debugger);
        cardCoopMode = rootView.findViewById(R.id.card_coop_mode);
        cardMultiFile = rootView.findViewById(R.id.card_multi_file);
        cardGithubImport = rootView.findViewById(R.id.card_github_import);
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

    // ═══════════════════════════════════════════════════════════════════════════
    //                         NEW FEATURES SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    private void setupNewFeatureCards() {
        // AI Mentor - Navigate to mentor chat
        if (cardAIMentor != null) {
            cardAIMentor.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCardPress(v, () -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.mentorChatFragment);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Opening AI Mentor...", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Interactive Debugger - Navigate to debugger
        if (cardDebugger != null) {
            cardDebugger.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCardPress(v, () -> {
                    try {
                        Bundle args = new Bundle();
                        args.putInt("bug_id", 1); // Default bug
                        Navigation.findNavController(requireView()).navigate(R.id.debuggerFragment, args);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Opening Debugger...", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Co-op Mode - Navigate to coop
        if (cardCoopMode != null) {
            cardCoopMode.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCardPress(v, () -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.coopFragment);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Opening Co-op Mode...", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Multi-File Debug
        if (cardMultiFile != null) {
            cardMultiFile.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCardPress(v, () -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.multiFileBugFragment);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Opening Multi-File Debug...", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // GitHub Import
        if (cardGithubImport != null) {
            cardGithubImport.setOnClickListener(v -> {
                soundManager.playSound(SoundManager.Sound.POWER_UP);
                animateCardPress(v, () -> {
                    try {
                        Navigation.findNavController(requireView()).navigate(R.id.githubImportFragment);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Opening GitHub Import...", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private void setupGameModeCards() {
        // 1. Quick Fix Mode
        setupGameModeCard(cardQuickFix, "🎯", "Quick Fix",
            "⏱️ 60 sec | ❤️ 3 lives | 🔥 Combos!",
            false, this::startQuickFixMode, "#10B981");

        // 2. Battle Arena
        setupGameModeCard(cardBattleArena, "⚔️", "Battle Arena",
            "⚔️ 1v1 | 🤖 AI Opponents | 🏆 Trophies",
            false, this::startBattleArena, "#EF4444");

        // 3. Speed Run
        setupGameModeCard(cardSpeedRun, "🏃", "Speed Run",
            "⏱️ 45 sec/bug | 🏆 15 bugs | ⚡ Fast bonus!",
            true, this::startSpeedRun, "#F59E0B");

        // 4. Puzzle Mode
        setupGameModeCard(cardPuzzleMode, "🧩", "Puzzle Mode",
            "🚫 No hints | ♾️ No timer | 💎 3x XP!",
            false, this::startPuzzleMode, "#8B5CF6");

        // 5. Tutorial Mode
        setupGameModeCard(cardTutorialMode, "🎓", "Tutorial Mode",
            "📚 Learn step-by-step | 💡 Guided hints",
            false, this::startTutorialMode, "#06B6D4");

        // 6. Daily Challenge
        setupGameModeCard(cardDailyChallenge, "🏆", "Daily Challenge",
            "🎁 " + getDailyTheme() + " | 🔥 Streak bonus",
            false, this::startDailyChallenge, "#EC4899");

        // 7. Mystery Bug
        setupGameModeCard(cardMysteryBug, "🎰", "Mystery Bug",
            "🎲 Random mods | 💰 Up to 5x XP!",
            true, this::startMysteryBug, "#F97316");

        // 8. Survival Mode
        setupGameModeCard(cardSurvivalMode, "💀", "Survival Mode",
            "💀 1 life | ⏱️ Timer shrinks | ∞ Endless!",
            false, this::startSurvivalMode, "#3B82F6");
    }

    private void setupGameModeCard(View card, String emoji, String title, String description,
                                   boolean isPro, Runnable onClickAction, String color) {
        if (card == null) return;

        TextView emojiView = card.findViewById(R.id.text_mode_emoji);
        if (emojiView != null) emojiView.setText(emoji);

        TextView titleView = card.findViewById(R.id.text_mode_title);
        if (titleView != null) titleView.setText(title);

        TextView descView = card.findViewById(R.id.text_mode_description);
        if (descView != null) descView.setText(description);

        TextView badge = card.findViewById(R.id.badge_mode);
        if (badge != null) {
            badge.setText(isPro ? "PRO" : "FREE");
            badge.setBackgroundResource(isPro ? R.drawable.bg_badge_pro : R.drawable.bg_badge_free);
            badge.setVisibility(View.VISIBLE);
        }

        View accentLine = card.findViewById(R.id.accent_line);
        if (accentLine != null) {
            try { accentLine.setBackgroundColor(Color.parseColor(color)); } catch (Exception ignored) {}
        }

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
        View[] allCards = {
            cardAIMentor, cardDebugger, cardCoopMode, cardMultiFile, cardGithubImport,
            cardQuickFix, cardBattleArena, cardSpeedRun, cardPuzzleMode,
            cardTutorialMode, cardDailyChallenge, cardMysteryBug, cardSurvivalMode
        };

        for (int i = 0; i < allCards.length; i++) {
            View card = allCards[i];
            if (card == null) continue;

            card.setAlpha(0f);
            card.setTranslationY(50f);
            card.setScaleX(0.9f);
            card.setScaleY(0.9f);

            int delay = 100 + (i * 60);
            handler.postDelayed(() -> {
                if (!isAdded()) return;

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(card, "translationY", 50f, 0f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 0.9f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0.9f, 1f);

                AnimatorSet anim = new AnimatorSet();
                anim.playTogether(fadeIn, slideUp, scaleX, scaleY);
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
        new AlertDialog.Builder(requireContext())
            .setTitle("🎯 Quick Fix Mode")
            .setMessage("Race against the clock!\n\n⏱️ 60 seconds per bug\n❤️ 3 lives\n🔥 Combo system!")
            .setPositiveButton("🚀 LET'S GO!", (d, w) -> navigateToPracticeWithMode("quick_fix"))
            .setNegativeButton("Cancel", null)
            .show();
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
        new AlertDialog.Builder(requireContext())
            .setTitle("🏃 Speed Run")
            .setMessage("Race through 15 bugs as fast as possible!\n\n⏱️ 45 seconds per bug\n⚡ Fast solves = 2x bonus!")
            .setPositiveButton("🚀 Start!", (d, w) -> navigateToPracticeWithMode("speed_run"))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void startPuzzleMode() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        new AlertDialog.Builder(requireContext())
            .setTitle("🧩 Puzzle Mode")
            .setMessage("TRUE debugging challenge!\n\n🚫 NO hints\n♾️ NO timer\n💎 3x XP REWARD!")
            .setPositiveButton("💪 I'M READY!", (d, w) -> navigateToPracticeWithMode("puzzle"))
            .setNegativeButton("Not yet", null)
            .show();
    }

    private void startTutorialMode() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        try {
            Navigation.findNavController(requireView()).navigate(R.id.beginnerTutorialFragment);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Opening tutorial...", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDailyChallenge() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        new AlertDialog.Builder(requireContext())
            .setTitle("🏆 " + getDailyTheme())
            .setMessage("Today's themed challenge!\n\n⏱️ 2 minutes per bug\n❤️ 1 life\n🎯 Bonus objectives!")
            .setPositiveButton("🚀 Accept", (d, w) -> navigateToPracticeWithMode("daily"))
            .setNegativeButton("Later", null)
            .show();
    }

    private void startMysteryBug() {
        soundManager.playSound(SoundManager.Sound.POWER_UP);
        new AlertDialog.Builder(requireContext())
            .setTitle("🎰 Mystery Bug")
            .setMessage("SPIN THE WHEEL!\n\n🎲 Random modifiers\n💰 Up to 5x XP!\n⚡ Feeling lucky?")
            .setPositiveButton("🎲 ROLL!", (d, w) -> navigateToPracticeWithMode("mystery"))
            .setNegativeButton("Not today", null)
            .show();
    }

    private void startSurvivalMode() {
        soundManager.playSound(SoundManager.Sound.CHALLENGE_START);
        new AlertDialog.Builder(requireContext())
            .setTitle("💀 Survival Mode")
            .setMessage("HOW LONG CAN YOU SURVIVE?\n\n💀 ONE LIFE\n⏱️ Timer shrinks each wave!\n🐛 Endless bugs!")
            .setPositiveButton("💀 BRING IT!", (d, w) -> navigateToPracticeWithMode("survival"))
            .setNegativeButton("Not brave enough", null)
            .show();
    }

    private void showProUpgradeDialog(String modeName) {
        new AlertDialog.Builder(requireContext())
            .setTitle("👑 Unlock " + modeName)
            .setMessage("This is a Pro feature!\n\nUpgrade to unlock all game modes and more!")
            .setPositiveButton("🚀 Go Pro", (d, w) -> {
                try { Navigation.findNavController(requireView()).navigate(R.id.proSubscriptionFragment); } catch (Exception ignored) {}
            })
            .setNegativeButton("Later", null)
            .show();
    }

    private void navigateToPracticeWithMode(String mode) {
        Bundle args = new Bundle();
        args.putString("gameMode", mode);
        try {
            Navigation.findNavController(requireView()).navigate(R.id.gameSessionFragment, args);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Starting " + mode + " mode...", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDailyTheme() {
        String[] themes = {"Super Sunday", "Memory Monday", "Type Tuesday", "Wild Wednesday", "Throwback Thursday", "Fast Friday", "Skill Saturday"};
        return themes[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        
        // Clear all view references to prevent memory leaks
        rootView = null;
        cardQuickFix = null;
        cardBattleArena = null;
        cardSpeedRun = null;
        cardPuzzleMode = null;
        cardTutorialMode = null;
        cardDailyChallenge = null;
        cardMysteryBug = null;
        cardSurvivalMode = null;
        cardAIMentor = null;
        cardDebugger = null;
        cardCoopMode = null;
        cardMultiFile = null;
        cardGithubImport = null;
    }
}
