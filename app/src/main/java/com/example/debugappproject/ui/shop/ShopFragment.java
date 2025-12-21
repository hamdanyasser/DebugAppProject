package com.example.debugappproject.ui.shop;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.databinding.FragmentShopBinding;
import com.example.debugappproject.util.SoundManager;
import com.example.debugappproject.ui.profile.PremiumAvatarSelectorDialog;
import com.example.debugappproject.ui.profile.PremiumTitleSelectorDialog;

/**
 * Shop Fragment for purchasing items with gems and buying gems with real money.
 */
public class ShopFragment extends Fragment {

    private static final String TAG = "ShopFragment";
    private static final String PREFS_NAME = "shop_prefs";
    private static final String KEY_HINTS_OWNED = "hints_owned";
    private static final String KEY_XP_BOOST_COUNT = "xp_boost_count";
    private static final String KEY_STREAK_SHIELD_ACTIVE = "streak_shield_active";
    private static final String KEY_STREAK_SHIELD_EXPIRY = "streak_shield_expiry";
    private static final String KEY_AVATARS_UNLOCKED = "avatars_unlocked";
    private static final String KEY_TITLES_UNLOCKED = "titles_unlocked";

    private FragmentShopBinding binding;
    private BugRepository repository;
    private SoundManager soundManager;
    private SharedPreferences shopPrefs;
    private int devTapCount = 0;
    private long lastDevTapTime = 0;

    // Item prices
    private static final int PRICE_HINTS = 50;
    private static final int PRICE_XP_BOOST = 75;
    private static final int PRICE_STREAK_SHIELD = 100;
    private static final int PRICE_AVATARS = 200;
    private static final int PRICE_TITLES = 150;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new BugRepository(requireActivity().getApplication());
        soundManager = SoundManager.getInstance(requireContext());
        shopPrefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        soundManager.playSound(SoundManager.Sound.TRANSITION);

        setupUI();
        setupDevMode();
        updateGemsDisplay();
        updateItemCounts();
        observeProgress();
    }

    private void setupUI() {
        // Back button
        binding.buttonBack.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            Navigation.findNavController(v).popBackStack();
        });

        // Power-ups with count display
        binding.buttonBuyHints.setOnClickListener(v -> purchaseItem("Hint Pack (3)", PRICE_HINTS, () -> {
            int current = shopPrefs.getInt(KEY_HINTS_OWNED, 0);
            shopPrefs.edit().putInt(KEY_HINTS_OWNED, current + 3).apply();
            updateItemCounts();
        }));

        binding.buttonBuyXpBoost.setOnClickListener(v -> purchaseItem("XP Booster (2x)", PRICE_XP_BOOST, () -> {
            int current = shopPrefs.getInt(KEY_XP_BOOST_COUNT, 0);
            shopPrefs.edit().putInt(KEY_XP_BOOST_COUNT, current + 5).apply();
            updateItemCounts();
        }));

        binding.buttonBuyStreakShield.setOnClickListener(v -> purchaseItem("Streak Shield (24h)", PRICE_STREAK_SHIELD, () -> {
            // Shield lasts 24 hours
            long expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
            shopPrefs.edit()
                .putBoolean(KEY_STREAK_SHIELD_ACTIVE, true)
                .putLong(KEY_STREAK_SHIELD_EXPIRY, expiry)
                .apply();
            updateItemCounts();
        }));

        // Cosmetics
        binding.buttonBuyAvatars.setOnClickListener(v -> {
            if (shopPrefs.getBoolean(KEY_AVATARS_UNLOCKED, false)) {
                // Already owned - open selector to choose avatar
                soundManager.playButtonClick();
                showPremiumAvatarSelector();
                return;
            }
            purchaseItem("Premium Avatars", PRICE_AVATARS, () -> {
                shopPrefs.edit().putBoolean(KEY_AVATARS_UNLOCKED, true).apply();
                binding.buttonBuyAvatars.setText("Select ▸");
                // Don't disable - keep clickable to open selector
            });
        });

        binding.buttonBuyTitles.setOnClickListener(v -> {
            if (shopPrefs.getBoolean(KEY_TITLES_UNLOCKED, false)) {
                // Already owned - open selector to choose title
                soundManager.playButtonClick();
                showPremiumTitleSelector();
                return;
            }
            purchaseItem("Custom Titles", PRICE_TITLES, () -> {
                shopPrefs.edit().putBoolean(KEY_TITLES_UNLOCKED, true).apply();
                binding.buttonBuyTitles.setText("Select ▸");
                // Don't disable - keep clickable to open selector
            });
        });

        // Gem purchase buttons
        setupGemPurchases();

        // Update owned items display
        updateOwnedItems();
    }

    private void setupGemPurchases() {
        // Small gem pack - 100 gems
        if (binding.buttonBuyGems100 != null) {
            binding.buttonBuyGems100.setOnClickListener(v -> purchaseGemsWithMoney(100, "$0.99"));
        }
        // Medium gem pack - 500 gems
        if (binding.buttonBuyGems500 != null) {
            binding.buttonBuyGems500.setOnClickListener(v -> purchaseGemsWithMoney(500, "$3.99"));
        }
        // Large gem pack - 1200 gems
        if (binding.buttonBuyGems1200 != null) {
            binding.buttonBuyGems1200.setOnClickListener(v -> purchaseGemsWithMoney(1200, "$7.99"));
        }
    }

    private void purchaseGemsWithMoney(int gems, String price) {
        soundManager.playButtonClick();

        if (BillingManager.isDemoMode()) {
            // Demo mode - just grant gems
            new AlertDialog.Builder(requireContext())
                .setTitle("🧪 Demo Purchase")
                .setMessage("Buy " + gems + " gems for " + price + "?\n\n(Demo mode - no real payment)")
                .setPositiveButton("Buy", (d, w) -> {
                    soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
                    repository.addGems(gems);
                    updateGemsDisplay();
                    Toast.makeText(getContext(), "+" + gems + " gems added!", Toast.LENGTH_SHORT).show();
                    celebrateGemPurchase();
                })
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            // Real purchase - would use BillingManager
            Toast.makeText(getContext(), "IAP coming soon! Use demo mode for now.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDevMode() {
        // Tap gems balance 7 times quickly to access dev mode
        binding.layoutGems.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastDevTapTime > 2000) {
                devTapCount = 0;
            }
            lastDevTapTime = now;
            devTapCount++;

            if (devTapCount == 5) {
                Toast.makeText(getContext(), "2 more taps for dev mode...", Toast.LENGTH_SHORT).show();
            } else if (devTapCount >= 7) {
                devTapCount = 0;
                showDevModeDialog();
            }
        });
    }

    private void showDevModeDialog() {
        soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);

        EditText input = new EditText(requireContext());
        input.setHint("Enter gem amount");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 30, 50, 30);

        new AlertDialog.Builder(requireContext())
            .setTitle("🛠️ Developer Mode")
            .setMessage("Enter the amount of gems to add:")
            .setView(input)
            .setPositiveButton("Add Gems", (dialog, which) -> {
                try {
                    int amount = Integer.parseInt(input.getText().toString());
                    if (amount > 0 && amount <= 999999) {
                        repository.addGems(amount);
                        updateGemsDisplay();
                        soundManager.playSound(SoundManager.Sound.VICTORY);
                        Toast.makeText(getContext(), "🎉 Added " + amount + " gems!", Toast.LENGTH_SHORT).show();
                        celebrateGemPurchase();
                    } else {
                        Toast.makeText(getContext(), "Enter 1-999999", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }
            })
            .setNeutralButton("Max Gems", (dialog, which) -> {
                repository.addGems(99999);
                updateGemsDisplay();
                soundManager.playSound(SoundManager.Sound.VICTORY);
                Toast.makeText(getContext(), "🎉 Added 99,999 gems!", Toast.LENGTH_SHORT).show();
                celebrateGemPurchase();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void celebrateGemPurchase() {
        if (binding.layoutGems != null) {
            binding.layoutGems.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .rotation(10f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.layoutGems.animate()
                        .scaleX(1f).scaleY(1f)
                        .rotation(0f)
                        .setDuration(200)
                        .start();
                })
                .start();
        }
    }

    private void updateItemCounts() {
        // Update hint count display
        int hints = shopPrefs.getInt(KEY_HINTS_OWNED, 0);
        if (binding.textHintCount != null) {
            binding.textHintCount.setText("Owned: " + hints);
            binding.textHintCount.setVisibility(hints > 0 ? View.VISIBLE : View.GONE);
        }
        // Update hint button based on ownership
        if (binding.buttonBuyHints != null) {
            if (hints > 0) {
                binding.buttonBuyHints.setText("💡 Use");
                binding.buttonBuyHints.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF22C55E));
            } else {
                binding.buttonBuyHints.setText("💎 50");
                binding.buttonBuyHints.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6366F1));
            }
        }

        // Update XP boost count display
        int xpBoosts = shopPrefs.getInt(KEY_XP_BOOST_COUNT, 0);
        if (binding.textXpBoostCount != null) {
            binding.textXpBoostCount.setText("Remaining: " + xpBoosts);
            binding.textXpBoostCount.setVisibility(xpBoosts > 0 ? View.VISIBLE : View.GONE);
        }
        // Update XP boost button based on count
        if (binding.buttonBuyXpBoost != null) {
            if (xpBoosts > 0) {
                binding.buttonBuyXpBoost.setText("✨ Active");
                binding.buttonBuyXpBoost.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFBBF24));
            } else {
                binding.buttonBuyXpBoost.setText("💎 75");
                binding.buttonBuyXpBoost.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6366F1));
            }
        }

        // Update streak shield status
        boolean shieldActive = hasStreakShield(requireContext());
        if (binding.textStreakShieldStatus != null) {
            if (shieldActive) {
                long expiry = shopPrefs.getLong(KEY_STREAK_SHIELD_EXPIRY, 0);
                long hoursLeft = (expiry - System.currentTimeMillis()) / (60 * 60 * 1000);
                binding.textStreakShieldStatus.setText("Active: " + hoursLeft + "h left");
                binding.textStreakShieldStatus.setVisibility(View.VISIBLE);
            } else {
                binding.textStreakShieldStatus.setVisibility(View.GONE);
            }
        }
        // Update streak shield button based on status
        if (binding.buttonBuyStreakShield != null) {
            if (shieldActive) {
                binding.buttonBuyStreakShield.setText("🛡️ Protected");
                binding.buttonBuyStreakShield.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF60A5FA));
            } else {
                binding.buttonBuyStreakShield.setText("💎 100");
                binding.buttonBuyStreakShield.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6366F1));
            }
        }
    }

    private void updateOwnedItems() {
        if (shopPrefs.getBoolean(KEY_AVATARS_UNLOCKED, false)) {
            binding.buttonBuyAvatars.setText("Select ▸");
            // Keep enabled so user can select avatars
        }
        if (shopPrefs.getBoolean(KEY_TITLES_UNLOCKED, false)) {
            binding.buttonBuyTitles.setText("Select ▸");
            // Keep enabled so user can select titles
        }
    }

    private void purchaseItem(String itemName, int price, Runnable onSuccess) {
        soundManager.playButtonClick();

        int currentGems = repository.getGemsSync();
        if (currentGems < price) {
            soundManager.playSound(SoundManager.Sound.ERROR);
            showInsufficientGemsDialog(price, currentGems);
            return;
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("Confirm Purchase")
            .setMessage("Buy " + itemName + " for 💎 " + price + " gems?")
            .setPositiveButton("Buy", (dialog, which) -> {
                boolean success = repository.spendGems(price);
                if (success) {
                    soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
                    onSuccess.run();
                    updateGemsDisplay();
                    Toast.makeText(getContext(), "✓ Purchased " + itemName + "!", Toast.LENGTH_SHORT).show();
                    celebrateGemPurchase();
                } else {
                    soundManager.playSound(SoundManager.Sound.ERROR);
                    Toast.makeText(getContext(), "Purchase failed!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            })
            .show();
    }

    private void showInsufficientGemsDialog(int price, int currentGems) {
        int needed = price - currentGems;
        new AlertDialog.Builder(requireContext())
            .setTitle("Not Enough Gems")
            .setMessage("You need 💎 " + price + " gems but only have 💎 " + currentGems + ".\n\n" +
                    "You need " + needed + " more gems!\n\n" +
                    "Buy gems or earn them by solving bugs!")
            .setPositiveButton("OK", null)
            .show();
    }

    private void updateGemsDisplay() {
        int gems = repository.getGemsSync();
        if (binding != null && binding.textGemsBalance != null) {
            binding.textGemsBalance.setText(String.valueOf(gems));
        }
    }

    private void observeProgress() {
        repository.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && binding != null) {
                binding.textGemsBalance.setText(String.valueOf(progress.getGems()));
            }
        });
    }

    // ============ Static helper methods for game integration ============

    public static int getOwnedHints(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_HINTS_OWNED, 0);
    }

    public static boolean useHint(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_HINTS_OWNED, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_HINTS_OWNED, current - 1).apply();
            return true;
        }
        return false;
    }

    public static int getXpBoostCount(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_XP_BOOST_COUNT, 0);
    }

    public static boolean useXpBoost(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_XP_BOOST_COUNT, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_XP_BOOST_COUNT, current - 1).apply();
            return true;
        }
        return false;
    }

    public static boolean hasStreakShield(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        boolean active = prefs.getBoolean(KEY_STREAK_SHIELD_ACTIVE, false);
        if (active) {
            long expiry = prefs.getLong(KEY_STREAK_SHIELD_EXPIRY, 0);
            if (System.currentTimeMillis() > expiry) {
                // Shield expired
                prefs.edit().putBoolean(KEY_STREAK_SHIELD_ACTIVE, false).apply();
                return false;
            }
            return true;
        }
        return false;
    }

    public static void useStreakShield(android.content.Context context) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_STREAK_SHIELD_ACTIVE, false)
                .putLong(KEY_STREAK_SHIELD_EXPIRY, 0)
                .apply();
    }

    public static boolean hasUnlockedAvatars(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getBoolean(KEY_AVATARS_UNLOCKED, false);
    }

    public static boolean hasUnlockedTitles(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getBoolean(KEY_TITLES_UNLOCKED, false);
    }

    // ============ Game Power-ups Static Methods ============

    private static final String KEY_GAME_SHIELDS = "game_shields";
    private static final String KEY_TIME_FREEZES = "time_freezes";
    private static final String KEY_SKIP_TOKENS = "skip_tokens";
    private static final String KEY_COMBO_SAVERS = "combo_savers";
    private static final String KEY_SELECTED_AVATAR = "selected_avatar";
    private static final String KEY_SELECTED_TITLE = "selected_title";

    public static int getGameShields(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_GAME_SHIELDS, 0);
    }

    public static boolean useGameShield(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_GAME_SHIELDS, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_GAME_SHIELDS, current - 1).apply();
            return true;
        }
        return false;
    }

    public static int getTimeFreezes(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_TIME_FREEZES, 0);
    }

    public static boolean useTimeFreeze(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_TIME_FREEZES, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_TIME_FREEZES, current - 1).apply();
            return true;
        }
        return false;
    }

    public static int getSkipTokens(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_SKIP_TOKENS, 0);
    }

    public static boolean useSkipToken(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_SKIP_TOKENS, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_SKIP_TOKENS, current - 1).apply();
            return true;
        }
        return false;
    }

    public static int getComboSavers(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_COMBO_SAVERS, 0);
    }

    public static boolean useComboSaver(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_COMBO_SAVERS, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_COMBO_SAVERS, current - 1).apply();
            return true;
        }
        return false;
    }

    // ============ Avatar & Title Methods ============

    // 10 Exclusive Premium Avatars with legendary creatures and special emojis
    private static final String[] PREMIUM_AVATARS = {
        "🦊", "🐲", "🦄", "🐺", "🦅", "🐼", "🦁", "🐯", "🐉", "🦋"
    };

    // Premium Titles with special emoji prefixes for visual distinction
    private static final String[] PREMIUM_TITLES = {
        "🔥 Bug Slayer",
        "⚔️ Code Ninja",
        "👑 Debug Master",
        "🧙 Syntax Sage",
        "🎯 Logic Lord",
        "💥 Error Eliminator",
        "🏆 Bug Hunter Elite",
        "✨ Code Wizard"
    };

    public static String[] getPremiumAvatars() {
        return PREMIUM_AVATARS;
    }

    public static String[] getPremiumTitles() {
        return PREMIUM_TITLES;
    }

    public static String getSelectedAvatar(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getString(KEY_SELECTED_AVATAR, "");
    }

    public static void setSelectedAvatar(android.content.Context context, String avatar) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_AVATAR, avatar)
                .apply();
    }

    public static String getSelectedTitle(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getString(KEY_SELECTED_TITLE, "");
    }

    public static void setSelectedTitle(android.content.Context context, String title) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_TITLE, title)
                .apply();
    }

    public static int getTotalItemsOwned(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int count = 0;
        count += prefs.getInt(KEY_HINTS_OWNED, 0);
        count += prefs.getInt(KEY_XP_BOOST_COUNT, 0);
        count += prefs.getInt(KEY_GAME_SHIELDS, 0);
        count += prefs.getInt(KEY_TIME_FREEZES, 0);
        count += prefs.getInt(KEY_SKIP_TOKENS, 0);
        count += prefs.getInt(KEY_COMBO_SAVERS, 0);
        if (prefs.getBoolean(KEY_AVATARS_UNLOCKED, false)) count++;
        if (prefs.getBoolean(KEY_TITLES_UNLOCKED, false)) count++;
        return count;
    }

    /**
     * Show premium avatar selector dialog from shop
     */
    private void showPremiumAvatarSelector() {
        String currentAvatar = getSelectedAvatar(requireContext());
        
        PremiumAvatarSelectorDialog dialog = PremiumAvatarSelectorDialog.newInstance(currentAvatar);
        dialog.setOnPremiumAvatarSelectedListener(emoji -> {
            Toast.makeText(getContext(), "Avatar equipped: " + emoji, Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "premium_avatar_selector");
    }
    
    /**
     * Show premium title selector dialog from shop
     */
    private void showPremiumTitleSelector() {
        String currentTitle = getSelectedTitle(requireContext());
        
        PremiumTitleSelectorDialog dialog = PremiumTitleSelectorDialog.newInstance(currentTitle);
        dialog.setOnPremiumTitleSelectedListener(title -> {
            if (title != null && !title.isEmpty()) {
                Toast.makeText(getContext(), "Title equipped: " + title, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Title removed", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getChildFragmentManager(), "premium_title_selector");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear all field references to prevent memory leaks
        repository = null;
        soundManager = null;
        shopPrefs = null;
        binding = null;
    }
}
