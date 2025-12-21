package com.example.debugappproject.ui.profile;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.debugappproject.R;
import com.example.debugappproject.ui.shop.ShopFragment;
import com.example.debugappproject.util.AuthManager;
import com.example.debugappproject.util.SoundManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - COSMETIC COLLECTION CHOOSER                          ║
 * ║              Premium experience for choosing between free & premium items    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * This dialog allows users who own premium cosmetics to choose between
 * free and premium collections before making a selection.
 * Features stunning animations and visual feedback.
 */
public class CosmeticChooserDialog extends DialogFragment {

    public enum ChooserType {
        AVATAR,
        TITLE
    }

    public interface OnCollectionChosenListener {
        void onFreeCollectionChosen();
        void onPremiumCollectionChosen();
    }

    private static final String ARG_TYPE = "chooser_type";
    private static final String ARG_CURRENT_FREE = "current_free";
    private static final String ARG_CURRENT_PREMIUM = "current_premium";
    private static final String ARG_IS_PREMIUM_EQUIPPED = "is_premium_equipped";

    private ChooserType chooserType;
    private String currentFreeSelection;
    private String currentPremiumSelection;
    private boolean isPremiumEquipped;

    private OnCollectionChosenListener listener;
    private SoundManager soundManager;

    // Views
    private FrameLayout cardFree;
    private FrameLayout cardPremium;
    private LinearLayout layoutFreeCard;
    private LinearLayout layoutPremiumCard;
    private TextView badgeFreeEquipped;
    private TextView badgePremiumEquipped;
    private TextView textFreeCurrent;
    private TextView textPremiumCurrent;
    private LinearLayout layoutFreeCurrent;
    private LinearLayout layoutPremiumCurrent;
    private TextView textHeaderIcon;
    private TextView textDialogTitle;
    private TextView textDialogSubtitle;
    private TextView textFreeCount;
    private TextView textPremiumCount;
    private ImageView iconSparkle;
    private TextView iconCrown;

    public static CosmeticChooserDialog newInstance(ChooserType type, 
                                                      String currentFree,
                                                      String currentPremium,
                                                      boolean isPremiumEquipped) {
        CosmeticChooserDialog dialog = new CosmeticChooserDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TYPE, type);
        args.putString(ARG_CURRENT_FREE, currentFree);
        args.putString(ARG_CURRENT_PREMIUM, currentPremium);
        args.putBoolean(ARG_IS_PREMIUM_EQUIPPED, isPremiumEquipped);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        soundManager = SoundManager.getInstance(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PremiumDialogTheme);

        if (getArguments() != null) {
            chooserType = (ChooserType) getArguments().getSerializable(ARG_TYPE);
            currentFreeSelection = getArguments().getString(ARG_CURRENT_FREE, "🐛");
            currentPremiumSelection = getArguments().getString(ARG_CURRENT_PREMIUM, "🦊");
            isPremiumEquipped = getArguments().getBoolean(ARG_IS_PREMIUM_EQUIPPED, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cosmetic_chooser, container, false);

        initViews(view);
        setupContent();
        setupClickListeners(view);
        startEntranceAnimations();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void initViews(View view) {
        cardFree = view.findViewById(R.id.card_free_collection);
        cardPremium = view.findViewById(R.id.card_premium_collection);
        layoutFreeCard = view.findViewById(R.id.layout_free_card);
        layoutPremiumCard = view.findViewById(R.id.layout_premium_card);
        badgeFreeEquipped = view.findViewById(R.id.badge_free_equipped);
        badgePremiumEquipped = view.findViewById(R.id.badge_premium_equipped);
        textFreeCurrent = view.findViewById(R.id.text_free_current);
        textPremiumCurrent = view.findViewById(R.id.text_premium_current);
        layoutFreeCurrent = view.findViewById(R.id.layout_free_current);
        layoutPremiumCurrent = view.findViewById(R.id.layout_premium_current);
        textHeaderIcon = view.findViewById(R.id.text_header_icon);
        textDialogTitle = view.findViewById(R.id.text_dialog_title);
        textDialogSubtitle = view.findViewById(R.id.text_dialog_subtitle);
        textFreeCount = view.findViewById(R.id.text_free_count);
        textPremiumCount = view.findViewById(R.id.text_premium_count);
        iconSparkle = view.findViewById(R.id.icon_sparkle);
        iconCrown = view.findViewById(R.id.icon_crown);
    }

    private void setupContent() {
        // Set up based on type (avatar or title)
        if (chooserType == ChooserType.AVATAR) {
            textHeaderIcon.setText("🎭");
            textDialogTitle.setText("Choose Avatar Collection");
            textDialogSubtitle.setText("Free classics or premium exclusives?");
            textFreeCount.setText("42 avatars available");
            textPremiumCount.setText("10 exclusive avatars");
        } else {
            textHeaderIcon.setText("🏆");
            textDialogTitle.setText("Choose Title Collection");
            textDialogSubtitle.setText("Show off your achievements");
            textFreeCount.setText("No free titles");
            textPremiumCount.setText("8 premium titles");
        }

        // Show current selection
        if (currentFreeSelection != null && !currentFreeSelection.isEmpty()) {
            textFreeCurrent.setText(currentFreeSelection);
            layoutFreeCurrent.setVisibility(View.VISIBLE);
        }

        if (currentPremiumSelection != null && !currentPremiumSelection.isEmpty()) {
            textPremiumCurrent.setText(currentPremiumSelection);
            layoutPremiumCurrent.setVisibility(View.VISIBLE);
        }

        // Show equipped badge on correct card
        updateEquippedState();
    }

    private void updateEquippedState() {
        if (isPremiumEquipped) {
            badgePremiumEquipped.setVisibility(View.VISIBLE);
            badgeFreeEquipped.setVisibility(View.GONE);
            layoutPremiumCard.setBackgroundResource(R.drawable.bg_collection_card_premium_selected);
            layoutFreeCard.setBackgroundResource(R.drawable.bg_collection_card_free);
        } else {
            badgeFreeEquipped.setVisibility(View.VISIBLE);
            badgePremiumEquipped.setVisibility(View.GONE);
            layoutFreeCard.setBackgroundResource(R.drawable.bg_collection_card_free_selected);
            layoutPremiumCard.setBackgroundResource(R.drawable.bg_collection_card_premium);
        }
    }

    private void setupClickListeners(View view) {
        // Close button
        view.findViewById(R.id.button_close).setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            animateDismiss();
        });

        // Free collection card
        cardFree.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            animateCardSelection(cardFree, false);
        });

        // Premium collection card
        cardPremium.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.POWER_UP);
            animateCardSelection(cardPremium, true);
        });
    }

    private void animateCardSelection(View card, boolean isPremium) {
        // Scale down then up with bounce
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                card.animate()
                    .scaleX(1.02f)
                    .scaleY(1.02f)
                    .setDuration(150)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .withEndAction(() -> {
                        // Return to normal
                        card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();

                        // Callback after animation
                        card.postDelayed(() -> {
                            if (listener != null) {
                                if (isPremium) {
                                    listener.onPremiumCollectionChosen();
                                } else {
                                    listener.onFreeCollectionChosen();
                                }
                            }
                            dismiss();
                        }, 150);
                    })
                    .start();
            })
            .start();
    }

    private void startEntranceAnimations() {
        // Fade in and slide cards from sides
        if (cardFree != null) {
            cardFree.setAlpha(0f);
            cardFree.setTranslationX(-100f);
            cardFree.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(100)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        }

        if (cardPremium != null) {
            cardPremium.setAlpha(0f);
            cardPremium.setTranslationX(100f);
            cardPremium.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        }

        // Pulse the crown icon
        startCrownPulseAnimation();

        // Sparkle rotation
        startSparkleAnimation();
    }

    private void startCrownPulseAnimation() {
        if (iconCrown == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconCrown, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconCrown, "scaleY", 1f, 1.15f, 1f);

        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(scaleX, scaleY);
        pulse.setDuration(1500);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();

        // Repeat
        iconCrown.postDelayed(this::startCrownPulseAnimation, 2000);
    }

    private void startSparkleAnimation() {
        if (iconSparkle == null) return;

        ObjectAnimator rotation = ObjectAnimator.ofFloat(iconSparkle, "rotation", 0f, 360f);
        rotation.setDuration(3000);
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.start();

        // Add scale pulse
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconSparkle, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconSparkle, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(1500);
        scaleY.setDuration(1500);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    private void animateDismiss() {
        View root = getView();
        if (root != null) {
            root.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(200)
                .withEndAction(this::dismiss)
                .start();
        } else {
            dismiss();
        }
    }

    public void setOnCollectionChosenListener(OnCollectionChosenListener listener) {
        this.listener = listener;
    }

    /**
     * Utility to check if we should show the chooser or go directly to a selector
     */
    public static boolean shouldShowChooser(Context context, ChooserType type) {
        if (type == ChooserType.AVATAR) {
            return ShopFragment.hasUnlockedAvatars(context);
        } else {
            return ShopFragment.hasUnlockedTitles(context);
        }
    }

    /**
     * Get the current avatar being used and determine if it's premium
     */
    public static boolean isCurrentAvatarPremium(Context context) {
        if (!ShopFragment.hasUnlockedAvatars(context)) {
            return false;
        }
        
        String selectedPremium = ShopFragment.getSelectedAvatar(context);
        String[] premiumAvatars = ShopFragment.getPremiumAvatars();
        
        for (String premium : premiumAvatars) {
            if (premium.equals(selectedPremium)) {
                // Check if they're actually using premium (not just have it selected)
                // In our system, if premium avatars are unlocked and one is selected, it's shown
                return selectedPremium != null && !selectedPremium.isEmpty();
            }
        }
        return false;
    }

    /**
     * Check if current title is from premium collection
     */
    public static boolean isCurrentTitlePremium(Context context) {
        if (!ShopFragment.hasUnlockedTitles(context)) {
            return false;
        }
        
        String selectedTitle = ShopFragment.getSelectedTitle(context);
        return selectedTitle != null && !selectedTitle.isEmpty();
    }
}
