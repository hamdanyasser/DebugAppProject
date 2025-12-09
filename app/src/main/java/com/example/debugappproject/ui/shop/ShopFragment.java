package com.example.debugappproject.ui.shop;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.debugappproject.R;
import com.example.debugappproject.data.repository.BugRepository;
import com.example.debugappproject.databinding.FragmentShopBinding;
import com.example.debugappproject.model.UserProgress;
import com.example.debugappproject.util.SoundManager;

/**
 * Shop Fragment for purchasing items with gems.
 */
public class ShopFragment extends Fragment {

    private static final String TAG = "ShopFragment";
    private static final String PREFS_NAME = "shop_prefs";
    private static final String KEY_HINTS_OWNED = "hints_owned";
    private static final String KEY_XP_BOOST_COUNT = "xp_boost_count";
    private static final String KEY_STREAK_SHIELD_ACTIVE = "streak_shield_active";
    private static final String KEY_AVATARS_UNLOCKED = "avatars_unlocked";
    private static final String KEY_TITLES_UNLOCKED = "titles_unlocked";

    private FragmentShopBinding binding;
    private BugRepository repository;
    private SoundManager soundManager;
    private SharedPreferences shopPrefs;

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

        repository = BugRepository.getInstance(requireContext());
        soundManager = SoundManager.getInstance(requireContext());
        shopPrefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        soundManager.playSound(SoundManager.Sound.TRANSITION);

        setupUI();
        updateGemsDisplay();
        observeProgress();
    }

    private void setupUI() {
        // Back button
        binding.buttonBack.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            Navigation.findNavController(v).popBackStack();
        });

        // Power-ups
        binding.buttonBuyHints.setOnClickListener(v -> purchaseItem("Hint Pack (3)", PRICE_HINTS, () -> {
            int current = shopPrefs.getInt(KEY_HINTS_OWNED, 0);
            shopPrefs.edit().putInt(KEY_HINTS_OWNED, current + 3).apply();
        }));

        binding.buttonBuyXpBoost.setOnClickListener(v -> purchaseItem("XP Booster (2x)", PRICE_XP_BOOST, () -> {
            int current = shopPrefs.getInt(KEY_XP_BOOST_COUNT, 0);
            shopPrefs.edit().putInt(KEY_XP_BOOST_COUNT, current + 5).apply();
        }));

        binding.buttonBuyStreakShield.setOnClickListener(v -> purchaseItem("Streak Shield", PRICE_STREAK_SHIELD, () -> {
            shopPrefs.edit().putBoolean(KEY_STREAK_SHIELD_ACTIVE, true).apply();
        }));

        // Cosmetics
        binding.buttonBuyAvatars.setOnClickListener(v -> {
            if (shopPrefs.getBoolean(KEY_AVATARS_UNLOCKED, false)) {
                soundManager.playSound(SoundManager.Sound.ERROR);
                Toast.makeText(getContext(), "You already own this!", Toast.LENGTH_SHORT).show();
                return;
            }
            purchaseItem("Premium Avatars", PRICE_AVATARS, () -> {
                shopPrefs.edit().putBoolean(KEY_AVATARS_UNLOCKED, true).apply();
                binding.buttonBuyAvatars.setText("Owned");
                binding.buttonBuyAvatars.setEnabled(false);
            });
        });

        binding.buttonBuyTitles.setOnClickListener(v -> {
            if (shopPrefs.getBoolean(KEY_TITLES_UNLOCKED, false)) {
                soundManager.playSound(SoundManager.Sound.ERROR);
                Toast.makeText(getContext(), "You already own this!", Toast.LENGTH_SHORT).show();
                return;
            }
            purchaseItem("Custom Titles", PRICE_TITLES, () -> {
                shopPrefs.edit().putBoolean(KEY_TITLES_UNLOCKED, true).apply();
                binding.buttonBuyTitles.setText("Owned");
                binding.buttonBuyTitles.setEnabled(false);
            });
        });

        // Update owned items display
        updateOwnedItems();
    }

    private void updateOwnedItems() {
        if (shopPrefs.getBoolean(KEY_AVATARS_UNLOCKED, false)) {
            binding.buttonBuyAvatars.setText("Owned");
            binding.buttonBuyAvatars.setEnabled(false);
        }
        if (shopPrefs.getBoolean(KEY_TITLES_UNLOCKED, false)) {
            binding.buttonBuyTitles.setText("Owned");
            binding.buttonBuyTitles.setEnabled(false);
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

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
            .setTitle("Confirm Purchase")
            .setMessage("Buy " + itemName + " for 💎 " + price + " gems?")
            .setPositiveButton("Buy", (dialog, which) -> {
                boolean success = repository.spendGems(price);
                if (success) {
                    soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
                    onSuccess.run();
                    updateGemsDisplay();
                    Toast.makeText(getContext(), "Purchased " + itemName + "!", Toast.LENGTH_SHORT).show();

                    // Animate the gems display
                    binding.layoutGems.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            binding.layoutGems.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(100)
                                .start();
                        })
                        .start();
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
                    "Earn gems by solving bugs and completing challenges.")
            .setPositiveButton("OK", (dialog, which) -> {
                soundManager.playButtonClick();
            })
            .show();
    }

    private void updateGemsDisplay() {
        int gems = repository.getGemsSync();
        binding.textGemsBalance.setText(String.valueOf(gems));
    }

    private void observeProgress() {
        repository.getUserProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                binding.textGemsBalance.setText(String.valueOf(progress.getGems()));
            }
        });
    }

    // Static helper methods for other parts of the app to check shop items
    public static int getOwnedHints(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_HINTS_OWNED, 0);
    }

    public static void useHint(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_HINTS_OWNED, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_HINTS_OWNED, current - 1).apply();
        }
    }

    public static int getXpBoostCount(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getInt(KEY_XP_BOOST_COUNT, 0);
    }

    public static void useXpBoost(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_XP_BOOST_COUNT, 0);
        if (current > 0) {
            prefs.edit().putInt(KEY_XP_BOOST_COUNT, current - 1).apply();
        }
    }

    public static boolean hasStreakShield(android.content.Context context) {
        return context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .getBoolean(KEY_STREAK_SHIELD_ACTIVE, false);
    }

    public static void useStreakShield(android.content.Context context) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_STREAK_SHIELD_ACTIVE, false).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
