package com.example.debugappproject.ui.subscription;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.billingclient.api.ProductDetails;
import com.example.debugappproject.R;
import com.example.debugappproject.billing.BillingManager;
import com.example.debugappproject.databinding.FragmentProSubscriptionBinding;

import java.util.List;

/**
 * Pro Subscription screen with Google Play Billing integration.
 * Offers monthly, yearly, and lifetime plans.
 * 
 * In Demo Mode: Simulates purchases for testing without Play Store.
 */
public class ProSubscriptionFragment extends Fragment implements BillingManager.BillingCallback {

    private FragmentProSubscriptionBinding binding;
    private BillingManager billingManager;
    private String selectedPlan = BillingManager.PRODUCT_YEARLY; // Default to best value

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize billing
        billingManager = new BillingManager(requireContext());
        billingManager.setCallback(this);

        setupUI();
        observeBilling();
    }

    private void setupUI() {
        // Close button
        binding.buttonClose.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Plan selection
        binding.cardMonthly.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_MONTHLY));
        binding.cardYearly.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_YEARLY));
        binding.cardLifetime.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_LIFETIME));

        binding.radioMonthly.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_MONTHLY));
        binding.radioYearly.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_YEARLY));
        binding.radioLifetime.setOnClickListener(v -> selectPlan(BillingManager.PRODUCT_LIFETIME));

        // Subscribe button - handles both demo and real purchases
        binding.buttonSubscribe.setOnClickListener(v -> {
            if (BillingManager.isDemoMode()) {
                // Demo mode - show confirmation dialog
                showDemoPurchaseDialog();
            } else {
                // Real purchase through Play Store
                if (getActivity() != null) {
                    binding.buttonSubscribe.setEnabled(false);
                    binding.buttonSubscribe.setText("Processing...");
                    billingManager.purchaseSubscription(getActivity(), selectedPlan);
                }
            }
        });

        // Restore purchases
        binding.textRestore.setOnClickListener(v -> {
            if (BillingManager.isDemoMode()) {
                // In demo mode, show option to deactivate pro
                showDemoRestoreDialog();
            } else {
                Toast.makeText(getContext(), "Checking for existing purchases...", Toast.LENGTH_SHORT).show();
                billingManager.refreshPurchases();
            }
        });

        // Default selection
        selectPlan(BillingManager.PRODUCT_YEARLY);
        
        // Update button text for demo mode
        updateButtonForDemoMode();
    }

    private void updateButtonForDemoMode() {
        if (BillingManager.isDemoMode()) {
            // Show demo mode indicator in button
            if (selectedPlan.equals(BillingManager.PRODUCT_LIFETIME)) {
                binding.buttonSubscribe.setText("🧪 Demo: Activate Lifetime");
            } else {
                binding.buttonSubscribe.setText("🧪 Demo: Activate Pro");
            }
        }
    }

    private void showDemoPurchaseDialog() {
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
                // Demo purchase
                binding.buttonSubscribe.setEnabled(false);
                binding.buttonSubscribe.setText("Activating...");
                
                // Simulate a small delay for realism
                binding.buttonSubscribe.postDelayed(() -> {
                    billingManager.demoPurchase(selectedPlan);
                }, 800);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDemoRestoreDialog() {
        boolean isPro = billingManager.isProUserSync();
        
        if (isPro) {
            // Already pro - offer to deactivate
            new AlertDialog.Builder(requireContext())
                .setTitle("👑 Pro Status Active")
                .setMessage("You currently have Pro access (Demo Mode).\n\n" +
                    "Would you like to deactivate it to test the free version again?")
                .setPositiveButton("Deactivate Pro", (dialog, which) -> {
                    billingManager.demoDeactivate();
                    Toast.makeText(getContext(), "Pro deactivated. You're now on free plan.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Keep Pro", null)
                .show();
        } else {
            // Not pro - offer to activate
            new AlertDialog.Builder(requireContext())
                .setTitle("🧪 Demo Mode")
                .setMessage("No Pro subscription found.\n\n" +
                    "Select a plan above and tap the subscribe button to activate Pro for testing.")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void selectPlan(String productId) {
        selectedPlan = productId;

        // Update radio buttons
        binding.radioMonthly.setChecked(productId.equals(BillingManager.PRODUCT_MONTHLY));
        binding.radioYearly.setChecked(productId.equals(BillingManager.PRODUCT_YEARLY));
        binding.radioLifetime.setChecked(productId.equals(BillingManager.PRODUCT_LIFETIME));

        // Update card strokes
        int selectedStroke = (int) (2 * getResources().getDisplayMetrics().density);
        int unselectedStroke = 0;

        binding.cardMonthly.setStrokeWidth(productId.equals(BillingManager.PRODUCT_MONTHLY) ? selectedStroke : unselectedStroke);
        binding.cardYearly.setStrokeWidth(productId.equals(BillingManager.PRODUCT_YEARLY) ? selectedStroke : unselectedStroke);
        binding.cardLifetime.setStrokeWidth(productId.equals(BillingManager.PRODUCT_LIFETIME) ? selectedStroke : unselectedStroke);

        // Update button text
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
        // Observe pro status
        billingManager.getIsProUser().observe(getViewLifecycleOwner(), isPro -> {
            if (isPro && isAdded()) {
                // User is now pro - show success and close
                Toast.makeText(getContext(), "🎉 Welcome to DebugMaster Pro!", Toast.LENGTH_LONG).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            }
        });

        // Observe products (for real Play Store mode)
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
            
            // The observer will handle navigation
        });
    }

    @Override
    public void onPurchaseFailed(String error) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
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
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.destroy();
        }
        binding = null;
    }
}
