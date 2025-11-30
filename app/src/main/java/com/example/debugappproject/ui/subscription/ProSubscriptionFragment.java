package com.example.debugappproject.ui.subscription;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.billingclient.api.ProductDetails;
import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentProSubscriptionBinding;
import com.example.debugappproject.util.SoundManager;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║           DEBUGMASTER - PRO SUBSCRIPTION                                     ║
 * ║              Premium Purchase with Sound Effects                             ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * Pro Subscription screen with Google Play Billing integration.
 * Offers monthly, yearly, and lifetime plans with immersive audio.
 */
public class ProSubscriptionFragment extends Fragment implements BillingManager.BillingCallback {

    private FragmentProSubscriptionBinding binding;
    private BillingManager billingManager;
    private SoundManager soundManager;
    private String selectedPlan = BillingManager.PRODUCT_YEARLY;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        billingManager = BillingManager.getInstance(requireContext());
        billingManager.setCallback(this);
        soundManager = SoundManager.getInstance(requireContext());

        // Play premium entrance sound
        soundManager.playSound(SoundManager.Sound.POWER_UP);

        setupUI();
        observeBilling();
        playEntranceAnimations();
    }

    /**
     * Premium entrance animations for the subscription screen
     */
    private void playEntranceAnimations() {
        // Plan cards animate in staggered
        View[] cards = {binding.cardMonthly, binding.cardYearly, binding.cardLifetime};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card != null) {
                card.setAlpha(0f);
                card.setScaleX(0.9f);
                card.setScaleY(0.9f);
                card.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(400 + (i * 150L))
                        .setDuration(400)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }
        }

        // Subscribe button pops in
        if (binding.buttonSubscribe != null) {
            binding.buttonSubscribe.setAlpha(0f);
            binding.buttonSubscribe.setScaleX(0.8f);
            binding.buttonSubscribe.setScaleY(0.8f);
            binding.buttonSubscribe.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(900)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void setupUI() {
        // Close button
        binding.buttonClose.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            Navigation.findNavController(v).popBackStack();
        });

        // Plan selection with sounds
        binding.cardMonthly.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            animateCardSelection(v);
            selectPlan(BillingManager.PRODUCT_MONTHLY);
        });
        binding.cardYearly.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            animateCardSelection(v);
            selectPlan(BillingManager.PRODUCT_YEARLY);
        });
        binding.cardLifetime.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
            animateCardSelection(v);
            selectPlan(BillingManager.PRODUCT_LIFETIME);
        });

        binding.radioMonthly.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            selectPlan(BillingManager.PRODUCT_MONTHLY);
        });
        binding.radioYearly.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BLIP);
            selectPlan(BillingManager.PRODUCT_YEARLY);
        });
        binding.radioLifetime.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
            selectPlan(BillingManager.PRODUCT_LIFETIME);
        });

        // Subscribe button
        binding.buttonSubscribe.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_START);
            animateButton(v);
            if (BillingManager.isDemoMode()) {
                showDemoPurchaseDialog();
            } else {
                if (getActivity() != null) {
                    binding.buttonSubscribe.setEnabled(false);
                    binding.buttonSubscribe.setText("Processing...");
                    billingManager.purchaseSubscription(getActivity(), selectedPlan);
                }
            }
        });

        // Restore purchases
        binding.textRestore.setOnClickListener(v -> {
            soundManager.playButtonClick();
            if (BillingManager.isDemoMode()) {
                showDemoRestoreDialog();
            } else {
                Toast.makeText(getContext(), "Checking for existing purchases...", Toast.LENGTH_SHORT).show();
                billingManager.refreshPurchases();
            }
        });

        selectPlan(BillingManager.PRODUCT_YEARLY);
        updateButtonForDemoMode();
    }

    /**
     * Animate button press
     */
    private void animateButton(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * Animate card selection
     */
    private void animateCardSelection(View card) {
        card.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(100)
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }

    private void updateButtonForDemoMode() {
        if (BillingManager.isDemoMode()) {
            if (selectedPlan.equals(BillingManager.PRODUCT_LIFETIME)) {
                binding.buttonSubscribe.setText("🧪 Demo: Activate Lifetime");
            } else {
                binding.buttonSubscribe.setText("🧪 Demo: Activate Pro");
            }
        }
    }

    private void showDemoPurchaseDialog() {
        soundManager.playSound(SoundManager.Sound.NOTIFICATION);
        
        String planName;
        String planDetails;
        
        switch (selectedPlan) {
            case BillingManager.PRODUCT_MONTHLY:
                planName = "Monthly Pro";
                planDetails = "$4.99/month";
                break;
            case BillingManager.PRODUCT_YEARLY:
                planName = "Yearly Pro";
                planDetails = "$39.99/year (Best Value!)";
                break;
            case BillingManager.PRODUCT_LIFETIME:
                planName = "Lifetime Pro";
                planDetails = "$99.99 one-time";
                break;
            default:
                planName = "Pro";
                planDetails = "";
        }

        new AlertDialog.Builder(requireContext())
            .setTitle("🧪 Demo Mode Purchase")
            .setMessage("You're about to activate:\n\n" +
                "📦 " + planName + "\n" +
                "💰 " + planDetails + "\n\n" +
                "This is a DEMO purchase - no real payment will be made.\n\n" +
                "Pro features you'll unlock:\n" +
                "✓ All 100+ debugging challenges\n" +
                "✓ All 6 learning paths\n" +
                "✓ Battle Arena multiplayer\n" +
                "✓ Unlimited practice mode\n" +
                "✓ Ad-free experience\n\n" +
                "Activate Pro now?")
            .setPositiveButton("🚀 Activate Pro", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_START);
                binding.buttonSubscribe.setEnabled(false);
                binding.buttonSubscribe.setText("Activating...");
                
                binding.buttonSubscribe.postDelayed(() -> {
                    billingManager.demoPurchase(selectedPlan);
                }, 800);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
            })
            .show();
    }

    private void showDemoRestoreDialog() {
        boolean isPro = billingManager.isProUserSync();
        
        if (isPro) {
            soundManager.playSound(SoundManager.Sound.COIN_COLLECT);
            new AlertDialog.Builder(requireContext())
                .setTitle("👑 Pro Status Active")
                .setMessage("You currently have Pro access (Demo Mode).\n\n" +
                    "Would you like to deactivate it to test the free version again?")
                .setPositiveButton("Deactivate Pro", (dialog, which) -> {
                    soundManager.playSound(SoundManager.Sound.BUTTON_BACK);
                    billingManager.demoDeactivate();
                    Toast.makeText(getContext(), "Pro deactivated. You're now on free plan.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Keep Pro", (dialog, which) -> {
                    soundManager.playButtonClick();
                })
                .show();
        } else {
            soundManager.playSound(SoundManager.Sound.NOTIFICATION);
            new AlertDialog.Builder(requireContext())
                .setTitle("🧪 Demo Mode")
                .setMessage("No Pro subscription found.\n\n" +
                    "Select a plan above and tap the subscribe button to activate Pro for testing.")
                .setPositiveButton("OK", (dialog, which) -> {
                    soundManager.playButtonClick();
                })
                .show();
        }
    }

    private void selectPlan(String productId) {
        selectedPlan = productId;

        binding.radioMonthly.setChecked(productId.equals(BillingManager.PRODUCT_MONTHLY));
        binding.radioYearly.setChecked(productId.equals(BillingManager.PRODUCT_YEARLY));
        binding.radioLifetime.setChecked(productId.equals(BillingManager.PRODUCT_LIFETIME));

        int selectedStroke = (int) (2 * getResources().getDisplayMetrics().density);
        int unselectedStroke = 0;

        binding.cardMonthly.setStrokeWidth(productId.equals(BillingManager.PRODUCT_MONTHLY) ? selectedStroke : unselectedStroke);
        binding.cardYearly.setStrokeWidth(productId.equals(BillingManager.PRODUCT_YEARLY) ? selectedStroke : unselectedStroke);
        binding.cardLifetime.setStrokeWidth(productId.equals(BillingManager.PRODUCT_LIFETIME) ? selectedStroke : unselectedStroke);

        if (BillingManager.isDemoMode()) {
            updateButtonForDemoMode();
        } else {
            if (productId.equals(BillingManager.PRODUCT_LIFETIME)) {
                binding.buttonSubscribe.setText("Purchase Lifetime Access");
            } else {
                binding.buttonSubscribe.setText("Start 7-Day Free Trial");
            }
        }
    }

    private void observeBilling() {
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (isPro && isAdded()) {
                // Play EPIC purchase success sounds!
                soundManager.playSound(SoundManager.Sound.ACHIEVEMENT_UNLOCK);
                soundManager.vibrate(SoundManager.Haptic.SUCCESS);
                
                // Delayed celebration sounds
                binding.buttonSubscribe.postDelayed(() -> {
                    soundManager.playSound(SoundManager.Sound.VICTORY);
                }, 500);
                binding.buttonSubscribe.postDelayed(() -> {
                    soundManager.playSound(SoundManager.Sound.STAR_EARNED);
                }, 1000);
                
                Toast.makeText(getContext(), "🎉 Welcome to DebugMaster Pro!", Toast.LENGTH_LONG).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            }
        });

        billingManager.getProductDetails().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                updatePrices();
            }
        });
    }

    private void updatePrices() {
        binding.textPriceMonthly.setText(billingManager.getFormattedPrice(BillingManager.PRODUCT_MONTHLY) + "/mo");
        binding.textPriceYearly.setText(billingManager.getFormattedPrice(BillingManager.PRODUCT_YEARLY) + "/yr");
        binding.textPriceLifetime.setText(billingManager.getFormattedPrice(BillingManager.PRODUCT_LIFETIME));
    }

    @Override
    public void onPurchaseSuccess(String productId) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            binding.buttonSubscribe.setEnabled(true);
            updateButtonForDemoMode();
        });
    }

    @Override
    public void onPurchaseFailed(String error) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            soundManager.playSound(SoundManager.Sound.ERROR);
            binding.buttonSubscribe.setEnabled(true);
            updateButtonForDemoMode();
            Toast.makeText(getContext(), "Purchase failed: " + error, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBillingReady() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(this::updatePrices);
    }

    @Override
    public void onProductsLoaded(List<ProductDetails> products) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(this::updatePrices);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (soundManager != null) {
            soundManager.resumeAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pauseAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.clearCallback();
        }
        binding = null;
    }
}
